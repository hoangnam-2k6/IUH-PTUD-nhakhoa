package Dao.NghiepVu;
import Dao.*;
import java.sql.*;
import java.util.*;
import java.math.*;
public final class DichVuService {
    private final PhanQuyen.PhienDangNhap actor;
    public DichVuService(PhanQuyen.PhienDangNhap a){
        actor=a;
    }
    public List<Map<String,Object>> list(){
        return TruyVanDAO.list("SELECT maDV AS ID,tenDV AS [Tên dịch vụ],donGia AS [Đơn giá],ISNULL(moTa,N'') AS [Mô tả],ISNULL(trangThai,N'Đang áp dụng') AS [Trạng thái] FROM dbo.DichVu ORDER BY maDV DESC");
    }
    public void save(Integer id,Map<String,String> m){
        String name=QuyTacNghiepVu.text(m.get("Tên dịch vụ"),"Tên dịch vụ",150),desc=Objects.toString(m.get("Mô tả"),"").trim(),status=QuyTacNghiepVu.text(m.get("Trạng thái"),"Trạng thái",30);
        QuyTacNghiepVu.need(desc.length()<=500,"Mô tả dài tối đa 500 ký tự");
        QuyTacNghiepVu.need(Set.of("Đang áp dụng","Ngừng áp dụng").contains(status),"Trạng thái không hợp lệ");
        BigDecimal price=QuyTacNghiepVu.amount(m.get("Đơn giá"));
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            PhanQuyen.lock("DICHVU_"+name.toLowerCase(Locale.ROOT),c);
            if(TruyVanDAO.one(c,"SELECT maDV FROM dbo.DichVu WHERE LOWER(tenDV)=LOWER(?) AND maDV<>?",name,id==null?0:id)!=null)throw new SQLException("Tên dịch vụ đã tồn tại");
            int key;
            boolean priceChanged=true;
            if(id==null){
                QuyTacNghiepVu.need("Đang áp dụng".equals(status),"Dịch vụ mới phải đang áp dụng");
                key=TruyVanDAO.insert(c,"INSERT dbo.DichVu(tenDV,donGia,moTa,trangThai) VALUES(?,?,?,?)",name,price,desc,status);
            }
            else{
                Map<String,Object> old=TruyVanDAO.one(c,"SELECT donGia FROM dbo.DichVu WITH(UPDLOCK,HOLDLOCK) WHERE maDV=?",id);
                if(old==null)throw new SQLException("Không tìm thấy dịch vụ");
                priceChanged=TruyVanDAO.money(old.get("donGia")).compareTo(price)!=0;
                key=id;
                TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.DichVu SET tenDV=?,donGia=?,moTa=?,trangThai=? WHERE maDV=?",name,price,desc,status,id),"Không sửa được dịch vụ");
            }
            if(priceChanged)TruyVanDAO.update(c,"INSERT dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai) VALUES(?,CONVERT(date,GETDATE()),?,?)",key,price,status);
            TruyVanDAO.audit(c,actor.accountId(),id==null?"Thêm dịch vụ":"Sửa dịch vụ","DichVu",name);
            return null;
        }
        );
    }
    public void stop(int id){
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.DichVu SET trangThai=N'Ngừng áp dụng' WHERE maDV=? AND ISNULL(trangThai,N'Đang áp dụng')<>N'Ngừng áp dụng'",id),"Dịch vụ đã ngừng hoặc không tồn tại");
            TruyVanDAO.audit(c,actor.accountId(),"Ngừng dịch vụ","DichVu","maDV="+id);
            return null;
        }
        );
    }
}
