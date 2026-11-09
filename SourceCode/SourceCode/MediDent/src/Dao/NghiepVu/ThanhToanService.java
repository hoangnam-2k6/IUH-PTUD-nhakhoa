package Dao.NghiepVu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import Dao.TruyVanDAO;

/** Xử lý SQL hóa đơn; các sự kiện nút bấm nằm trong ThanhToanPanel.java. */
public final class ThanhToanService {
    private final PhanQuyen.PhienDangNhap ngườiDùng;

    public ThanhToanService(PhanQuyen.PhienDangNhap phiên) { ngườiDùng = phiên; }

    public List<Map<String, Object>> list() {
        return list(0, 0); // Mặc định: hiện tất cả năm, tất cả tháng.
    }

    /** Lọc theo NGÀY LẬP hóa đơn, kể cả hóa đơn chưa thanh toán (ngày thu vẫn NULL). */
    public List<Map<String, Object>> list(int năm, int tháng) {
        QuyTacNghiepVu.need(năm == 0 || (năm >= 2000 && năm <= 2100), "Năm lọc hóa đơn không hợp lệ");
        QuyTacNghiepVu.need(tháng >= 0 && tháng <= 12, "Tháng phải từ 1 đến 12 hoặc Tất cả");
        QuyTacNghiepVu.need(tháng == 0 || năm != 0, "Hãy nhập năm khi chọn tháng cụ thể");

        String sql = "SELECT h.maHD AS ID,h.maPK AS [Mã phiếu],b.hoTen AS [Bệnh nhân],"
                + "h.tongTien AS [Số tiền],ISNULL(t.phuongThuc,N'') AS [Phương thức],"
                + "h.trangThai AS [Trạng thái],CONVERT(varchar(10),h.ngayLap,23) AS [Ngày lập],"
                + "CONVERT(varchar(19),t.ngayThanhToan,120) AS [Ngày thu],t.maTT AS [Mã TT] "
                + "FROM dbo.HoaDon h JOIN dbo.ThanhToan t ON t.maTT=h.maTT "
                + "JOIN dbo.PhieuKham pk ON pk.maPK=h.maPK JOIN dbo.BenhNhan b ON b.maBN=pk.maBN ";

        if (năm == 0) return TruyVanDAO.list(sql + "ORDER BY h.ngayLap DESC,h.maHD DESC");
        LocalDate từ = LocalDate.of(năm, tháng == 0 ? 1 : tháng, 1);
        LocalDate đến = tháng == 0 ? từ.plusYears(1) : từ.plusMonths(1);
        return TruyVanDAO.list(sql + "WHERE h.ngayLap>=? AND h.ngayLap<? ORDER BY h.ngayLap DESC,h.maHD DESC", từ, đến);
    }

    public List<Map<String, Object>> completed() {
        String sql = "SELECT p.maPK AS ID,b.hoTen AS [Bệnh nhân],p.trangThai AS [Trạng thái],"
                + "COALESCE((SELECT SUM(ct.thanhTien) FROM dbo.ChiTietDieuTri ct WHERE ct.maPK=p.maPK),0) AS [Tiền điều trị] "
                + "FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                + "WHERE p.trangThai=N'Hoàn tất' AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maPK=p.maPK) "
                + "ORDER BY p.maPK DESC";
        return TruyVanDAO.list(sql);
    }

    public List<Map<String, Object>> requests() {
        String sql = "SELECT y.maYC AS ID,h.maHD AS [Mã hóa đơn],y.maTT AS [Mã TT],"
                + "y.lyDo AS [Lý do],y.trangThai AS [Trạng thái],"
                + "CONVERT(varchar(19),y.ngayYeuCau,120) AS [Ngày gửi] "
                + "FROM dbo.YeuCauHuyPhieuThu y JOIN dbo.HoaDon h ON h.maTT=y.maTT ORDER BY y.maYC DESC";
        return TruyVanDAO.list(sql);
    }

    public void create(int mãPhiếu, String phươngThức) {
        QuyTacNghiepVu.need(Set.of("Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng").contains(phươngThức), "Phương thức không hợp lệ");
        TruyVanDAO.tx(kếtNối -> {
            PhanQuyen.requireManager(kếtNối, ngườiDùng);
            PhanQuyen.lock("BILLING_PK_" + mãPhiếu, kếtNối);
            Map<String, Object> phiếu = TruyVanDAO.one(kếtNối,
                    "SELECT trangThai FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?", mãPhiếu);
            if (phiếu == null || !"Hoàn tất".equals(TruyVanDAO.s(phiếu.get("trangThai")))) {
                throw new SQLException("Phiếu khám chưa hoàn tất hoặc không tồn tại");
            }
            if (TruyVanDAO.one(kếtNối, "SELECT maHD FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?", mãPhiếu) != null) {
                throw new SQLException("Phiếu khám đã được lập hóa đơn");
            }
            Map<String, Object> chiTiết = TruyVanDAO.one(kếtNối,
                    "SELECT COALESCE(SUM(thanhTien),0) AS tổng FROM dbo.ChiTietDieuTri WHERE maPK=?", mãPhiếu);
            BigDecimal sốTiền = TruyVanDAO.money(chiTiết.get("tổng"));
            QuyTacNghiepVu.need(sốTiền.signum() > 0, "Phiếu chưa có tiền điều trị hợp lệ");

            // Tiền mặt đã thu; chuyển khoản/thẻ chờ đối soát, ngày thực thu để NULL.
            boolean đãThu = "Tiền mặt".equals(phươngThức);
            String sql = "INSERT dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai) VALUES(?,"
                    + (đãThu ? "SYSDATETIME()" : "NULL") + ",?,?,?)";
            int mãThanhToán = TruyVanDAO.insert(kếtNối, sql, mãPhiếu, sốTiền, phươngThức,
                    đãThu ? "Đã thanh toán" : "Chờ xử lý");
            TruyVanDAO.update(kếtNối, "INSERT dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,?)",
                    mãThanhToán, mãPhiếu, sốTiền, đãThu ? "Đã thanh toán" : "Chờ thanh toán");
            TruyVanDAO.audit(kếtNối, ngườiDùng.accountId(), "Lập hóa đơn", "HoaDon", "Phiếu khám: " + mãPhiếu);
            return null;
        });
    }

    public void confirm(int mãHóaĐơn, String mãGiaoDịch) {
        String mãĐốiSoát = QuyTacNghiepVu.text(mãGiaoDịch, "Mã giao dịch", 100);
        TruyVanDAO.tx(kếtNối -> {
            PhanQuyen.requireManager(kếtNối, ngườiDùng);
            String sql = "SELECT h.maTT,h.trangThai AS trangThaiHoaDon,t.trangThai AS trangThaiThu,t.phuongThuc "
                    + "FROM dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) "
                    + "JOIN dbo.ThanhToan t WITH(UPDLOCK,HOLDLOCK) ON h.maTT=t.maTT WHERE h.maHD=?";
            Map<String, Object> hóaĐơn = TruyVanDAO.one(kếtNối, sql, mãHóaĐơn);
            if (hóaĐơn == null) throw new SQLException("Không tồn tại hóa đơn");
            if (!"Chờ thanh toán".equals(hóaĐơn.get("trangThaiHoaDon")) || !"Chờ xử lý".equals(hóaĐơn.get("trangThaiThu"))) {
                throw new SQLException("Hóa đơn không còn chờ đối soát");
            }
            if ("Tiền mặt".equals(hóaĐơn.get("phuongThuc"))) throw new SQLException("Tiền mặt không cần đối soát");
            int mãThanhToán = TruyVanDAO.n(hóaĐơn.get("maTT"));
            if (TruyVanDAO.one(kếtNối, "SELECT maTT FROM dbo.ThanhToan WHERE maGiaoDich=? AND maTT<>?",
                    mãĐốiSoát, mãThanhToán) != null) throw new SQLException("Mã giao dịch đã được sử dụng");
            TruyVanDAO.exactlyOne(TruyVanDAO.update(kếtNối,
                    "UPDATE dbo.ThanhToan SET trangThai=N'Đã thanh toán',ngayThanhToan=SYSDATETIME(),maGiaoDich=? "
                            + "WHERE maTT=? AND trangThai=N'Chờ xử lý'", mãĐốiSoát, mãThanhToán), "Giao dịch vừa thay đổi");
            TruyVanDAO.exactlyOne(TruyVanDAO.update(kếtNối,
                    "UPDATE dbo.HoaDon SET trangThai=N'Đã thanh toán' WHERE maHD=? AND trangThai=N'Chờ thanh toán'",
                    mãHóaĐơn), "Hóa đơn vừa thay đổi");
            TruyVanDAO.audit(kếtNối, ngườiDùng.accountId(), "Xác nhận thực thu", "HoaDon", "Mã hóa đơn: " + mãHóaĐơn);
            return null;
        });
    }

    public void requestCancel(int mãHóaĐơn, String lýDo) {
        String nộiDung = QuyTacNghiepVu.text(lýDo, "Lý do hủy", 500);
        TruyVanDAO.tx(kếtNối -> {
            PhanQuyen.requireManager(kếtNối, ngườiDùng);
            String sql = "SELECT h.maTT,h.trangThai,t.trangThai AS trangThaiThu FROM dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) "
                    + "JOIN dbo.ThanhToan t ON h.maTT=t.maTT WHERE h.maHD=?";
            Map<String, Object> hóaĐơn = TruyVanDAO.one(kếtNối, sql, mãHóaĐơn);
            if (hóaĐơn == null || !"Chờ thanh toán".equals(hóaĐơn.get("trangThai"))
                    || !"Chờ xử lý".equals(hóaĐơn.get("trangThaiThu"))) {
                throw new SQLException("Chỉ được gửi yêu cầu hủy hóa đơn chưa thực thu");
            }
            int mãThanhToán = TruyVanDAO.n(hóaĐơn.get("maTT"));
            if (TruyVanDAO.one(kếtNối,
                    "SELECT maYC FROM dbo.YeuCauHuyPhieuThu WHERE maTT=? AND trangThai=N'Chờ duyệt'", mãThanhToán) != null) {
                throw new SQLException("Hóa đơn đã có yêu cầu hủy đang chờ duyệt");
            }
            TruyVanDAO.update(kếtNối,
                    "INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai,maTKNguoiGui) VALUES(?,?,N'Chờ duyệt',?)",
                    mãThanhToán, nộiDung, ngườiDùng.accountId());
            TruyVanDAO.audit(kếtNối, ngườiDùng.accountId(), "Gửi yêu cầu hủy", "HoaDon", "Mã hóa đơn: " + mãHóaĐơn);
            return null;
        });
    }

    public void decide(int mãYêuCầu, boolean đồngÝ, String lýDo) {
        String nộiDung = QuyTacNghiepVu.text(lýDo, "Lý do quyết định", 500);
        TruyVanDAO.tx(kếtNối -> {
            PhanQuyen.requireManager(kếtNối, ngườiDùng);
            String sql = "SELECT y.maTT,y.trangThai AS reqStatus,h.maHD,h.trangThai AS invoiceStatus,"
                    + "t.trangThai AS payStatus FROM dbo.YeuCauHuyPhieuThu y WITH(UPDLOCK,HOLDLOCK) "
                    + "JOIN dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) ON h.maTT=y.maTT "
                    + "JOIN dbo.ThanhToan t WITH(UPDLOCK,HOLDLOCK) ON t.maTT=y.maTT WHERE y.maYC=?";
            Map<String, Object> yêuCầu = TruyVanDAO.one(kếtNối, sql, mãYêuCầu);
            if (yêuCầu == null || !"Chờ duyệt".equals(yêuCầu.get("reqStatus"))) {
                throw new SQLException("Yêu cầu không tồn tại hoặc đã được xử lý");
            }
            if (đồngÝ) {
                if (!"Chờ thanh toán".equals(yêuCầu.get("invoiceStatus"))
                        || !"Chờ xử lý".equals(yêuCầu.get("payStatus"))) {
                    throw new SQLException("Đã nhận tiền nên không thể hủy bằng quy trình này");
                }
                TruyVanDAO.exactlyOne(TruyVanDAO.update(kếtNối,
                        "UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=? AND trangThai=N'Chờ xử lý'",
                        TruyVanDAO.n(yêuCầu.get("maTT"))), "Không cập nhật được thanh toán");
                TruyVanDAO.exactlyOne(TruyVanDAO.update(kếtNối,
                        "UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maHD=? AND trangThai=N'Chờ thanh toán'",
                        TruyVanDAO.n(yêuCầu.get("maHD"))), "Không cập nhật được hóa đơn");
            }
            TruyVanDAO.exactlyOne(TruyVanDAO.update(kếtNối,
                    "UPDATE dbo.YeuCauHuyPhieuThu SET trangThai=? WHERE maYC=? AND trangThai=N'Chờ duyệt'",
                    đồngÝ ? "Đã duyệt" : "Đã từ chối", mãYêuCầu), "Yêu cầu đã được xử lý");
            TruyVanDAO.update(kếtNối,
                    "INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet) VALUES(?,SYSDATETIME(),?)",
                    mãYêuCầu, nộiDung);
            TruyVanDAO.audit(kếtNối, ngườiDùng.accountId(), đồngÝ ? "Duyệt hủy" : "Từ chối hủy",
                    "YeuCauHuyPhieuThu", "Mã yêu cầu: " + mãYêuCầu + "; " + nộiDung);
            return null;
        });
    }
}
