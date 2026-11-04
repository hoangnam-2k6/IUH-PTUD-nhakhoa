package Dao.NghiepVu;
import Dao.*;
import java.sql.*;
import java.util.*;
import java.math.*;
public final class ThanhToanService {
    private final PhanQuyen.PhienDangNhap actor;
    public ThanhToanService(PhanQuyen.PhienDangNhap a){
        actor=a;
    }
    public List<Map<String,Object>> list(){
        return TruyVanDAO.list("SELECT h.maHD AS ID,h.maPK AS [Mã phiếu],b.hoTen AS [Bệnh nhân],h.tongTien AS [Số tiền],ISNULL(t.phuongThuc,N'') AS [Phương thức],h.trangThai AS [Trạng thái],CONVERT(varchar(19),t.ngayThanhToan,120) AS [Ngày thu],t.maTT AS [Mã TT] FROM dbo.HoaDon h JOIN dbo.ThanhToan t ON t.maTT=h.maTT JOIN dbo.PhieuKham pk ON pk.maPK=h.maPK JOIN dbo.BenhNhan b ON b.maBN=pk.maBN ORDER BY h.maHD DESC");
    }
    public List<Map<String,Object>> completed(){
        return TruyVanDAO.list("SELECT p.maPK AS ID,b.hoTen AS [Bệnh nhân],p.trangThai AS [Trạng thái],COALESCE((SELECT SUM(ct.thanhTien) FROM dbo.ChiTietDieuTri ct WHERE ct.maPK=p.maPK),0) AS [Tiền điều trị] FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN WHERE p.trangThai=N'Hoàn tất' AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maPK=p.maPK) ORDER BY p.maPK DESC");
    }
    public List<Map<String,Object>> requests(){
        return TruyVanDAO.list("SELECT y.maYC AS ID,h.maHD AS [Mã hóa đơn],y.maTT AS [Mã TT],y.lyDo AS [Lý do],y.trangThai AS [Trạng thái],CONVERT(varchar(19),y.ngayYeuCau,120) AS [Ngày gửi] FROM dbo.YeuCauHuyPhieuThu y JOIN dbo.HoaDon h ON h.maTT=y.maTT ORDER BY y.maYC DESC");
    }
    public void create(int maPK,String method){
        QuyTacNghiepVu.need(Set.of("Tiền mặt","Chuyển khoản","Thẻ ngân hàng").contains(method),"Phương thức thanh toán không hợp lệ");
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            PhanQuyen.lock("BILLING_PK_"+maPK,c);
            Map<String,Object> pk=TruyVanDAO.one(c,"SELECT trangThai FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?",maPK);
            if(pk==null||!"Hoàn tất".equals(TruyVanDAO.s(pk.get("trangThai"))))throw new SQLException("Phiếu khám không tồn tại hoặc chưa Hoàn tất");
            if(TruyVanDAO.one(c,"SELECT maHD FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?",maPK)!=null)throw new SQLException("Phiếu khám đã có hóa đơn");
            Map<String,Object> result=TruyVanDAO.one(c,"SELECT COALESCE(SUM(thanhTien),0) AS total FROM dbo.ChiTietDieuTri WHERE maPK=?",maPK);
            BigDecimal money=TruyVanDAO.money(result.get("total"));
            QuyTacNghiepVu.need(money.signum()>0,"Phiếu khám chưa có chi tiết điều trị/tiền hợp lệ");
            boolean cash="Tiền mặt".equals(method);
            int payment=TruyVanDAO.insert(c,"INSERT dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai) VALUES(?,"+(cash?"SYSDATETIME()":"NULL")+",?,?,?)",maPK,money,method,cash?"Đã thanh toán":"Chờ xử lý");
            TruyVanDAO.update(c,"INSERT dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,?)",payment,maPK,money,cash?"Đã thanh toán":"Chờ thanh toán");
            TruyVanDAO.audit(c,actor.accountId(),"Tạo thanh toán","ThanhToan","maPK="+maPK+", maTT="+payment);
            return null;
        }
        );
    }
    public void confirm(int maHD,String code){
        String tx=QuyTacNghiepVu.text(code,"Mã giao dịch",100);
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            Map<String,Object> h=TruyVanDAO.one(c,"SELECT h.maTT,h.trangThai AS invoiceStatus,t.trangThai AS payStatus,t.phuongThuc FROM dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) JOIN dbo.ThanhToan t WITH(UPDLOCK,HOLDLOCK) ON h.maTT=t.maTT WHERE h.maHD=?",maHD);
            if(h==null)throw new SQLException("Hóa đơn không tồn tại");
            if(!"Chờ thanh toán".equals(TruyVanDAO.s(h.get("invoiceStatus")))||!"Chờ xử lý".equals(TruyVanDAO.s(h.get("payStatus"))))throw new SQLException("Hóa đơn không còn chờ đối soát");
            if("Tiền mặt".equals(TruyVanDAO.s(h.get("phuongThuc"))))throw new SQLException("Tiền mặt không cần đối soát ngân hàng");
            if(TruyVanDAO.one(c,"SELECT maTT FROM dbo.ThanhToan WHERE maGiaoDich=? AND maTT<>?",tx,TruyVanDAO.n(h.get("maTT")))!=null)throw new SQLException("Mã giao dịch đã được sử dụng");
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.ThanhToan SET trangThai=N'Đã thanh toán',ngayThanhToan=SYSDATETIME(),maGiaoDich=? WHERE maTT=? AND trangThai=N'Chờ xử lý'",tx,TruyVanDAO.n(h.get("maTT"))),"Giao dịch vừa thay đổi");
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.HoaDon SET trangThai=N'Đã thanh toán' WHERE maHD=? AND trangThai=N'Chờ thanh toán'",maHD),"Hóa đơn vừa thay đổi");
            TruyVanDAO.audit(c,actor.accountId(),"Xác nhận thực thu","HoaDon","maHD="+maHD);
            return null;
        }
        );
    }
    public void requestCancel(int maHD,String reason){
        String why=QuyTacNghiepVu.text(reason,"Lý do hủy",500);
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            Map<String,Object> h=TruyVanDAO.one(c,"SELECT h.maTT,h.trangThai,t.trangThai AS paymentStatus FROM dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) JOIN dbo.ThanhToan t ON h.maTT=t.maTT WHERE h.maHD=?",maHD);
            if(h==null||!"Chờ thanh toán".equals(TruyVanDAO.s(h.get("trangThai")))||!"Chờ xử lý".equals(TruyVanDAO.s(h.get("paymentStatus"))))throw new SQLException("Chỉ gửi yêu cầu hủy hóa đơn chưa thực thu");
            int pid=TruyVanDAO.n(h.get("maTT"));
            if(TruyVanDAO.one(c,"SELECT maYC FROM dbo.YeuCauHuyPhieuThu WHERE maTT=? AND trangThai=N'Chờ duyệt'",pid)!=null)throw new SQLException("Đã có yêu cầu đang chờ duyệt");
            TruyVanDAO.update(c,"INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai,maTKNguoiGui) VALUES(?,?,N'Chờ duyệt',?)",pid,why,actor.accountId());
            TruyVanDAO.audit(c,actor.accountId(),"Gửi yêu cầu hủy","HoaDon","maHD="+maHD);
            return null;
        }
        );
    }
    /** Theo yêu cầu màn hình Quản lý, chỉ duyệt các giao dịch CHƯA THU; không hoàn tiền. */
    public void decide(int requestId,boolean approve,String note){
        String reason=QuyTacNghiepVu.text(note,"Lý do quyết định",500);
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            Map<String,Object> r=TruyVanDAO.one(c,"SELECT y.maTT,y.trangThai AS reqStatus,h.maHD,h.trangThai AS invoiceStatus,t.trangThai AS payStatus FROM dbo.YeuCauHuyPhieuThu y WITH(UPDLOCK,HOLDLOCK) JOIN dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) ON h.maTT=y.maTT JOIN dbo.ThanhToan t WITH(UPDLOCK,HOLDLOCK) ON t.maTT=y.maTT WHERE y.maYC=?",requestId);
            if(r==null||!"Chờ duyệt".equals(TruyVanDAO.s(r.get("reqStatus"))))throw new SQLException("Yêu cầu không tồn tại hoặc đã xử lý");
            if(approve){
                if(!"Chờ thanh toán".equals(TruyVanDAO.s(r.get("invoiceStatus")))||!"Chờ xử lý".equals(TruyVanDAO.s(r.get("payStatus"))))throw new SQLException("Đã thực thu hoặc hóa đơn đã đổi trạng thái; không thể hủy");
                TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=? AND trangThai=N'Chờ xử lý'",TruyVanDAO.n(r.get("maTT"))),"Không cập nhật được thanh toán");
                TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maHD=? AND trangThai=N'Chờ thanh toán'",TruyVanDAO.n(r.get("maHD"))),"Không cập nhật được hóa đơn");
            }
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.YeuCauHuyPhieuThu SET trangThai=? WHERE maYC=? AND trangThai=N'Chờ duyệt'",approve?"Đã duyệt":"Đã từ chối",requestId),"Yêu cầu đã xử lý");
            TruyVanDAO.update(c,"INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet) VALUES(?,SYSDATETIME(),?)",requestId,reason);
            TruyVanDAO.audit(c,actor.accountId(),approve?"Duyệt hủy chưa thu":"Từ chối hủy","YeuCauHuyPhieuThu","maYC="+requestId+"; "+reason);
            return null;
        }
        );
    }
}
