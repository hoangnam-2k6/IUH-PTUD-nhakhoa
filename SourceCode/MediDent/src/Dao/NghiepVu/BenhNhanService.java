package Dao.NghiepVu;
import Dao.*;
import java.time.*;
import java.sql.*;
import java.util.*;
public final class BenhNhanService {
    private final PhanQuyen.PhienDangNhap actor;
    public BenhNhanService(PhanQuyen.PhienDangNhap a){
        actor=a;
    }
    public List<Map<String,Object>> list(){
        return TruyVanDAO.list("SELECT maBN AS ID,hoTen AS [Họ tên],sdt AS [SĐT],CONVERT(varchar(10),ngaySinh,23) AS [Ngày sinh],CASE WHEN ngaySinh IS NULL THEN NULL ELSE DATEDIFF(year,ngaySinh,CONVERT(date,GETDATE()))-CASE WHEN DATEADD(year,DATEDIFF(year,ngaySinh,CONVERT(date,GETDATE())),ngaySinh)>CONVERT(date,GETDATE()) THEN 1 ELSE 0 END END AS [Tuổi],gioiTinh AS [Giới tính],email AS [Email],diaChi AS [Địa chỉ],ISNULL(tienSuBenh,N'') AS [Tiền sử bệnh],ISNULL(trangThaiHoSo,N'Hoạt động') AS [Trạng thái] FROM dbo.BenhNhan ORDER BY maBN DESC");
    }
    public void save(Integer id,Map<String,String> m){
        String name=QuyTacNghiepVu.text(m.get("Họ tên"),"Họ tên",120),phone=QuyTacNghiepVu.phone(m.get("SĐT")),email=QuyTacNghiepVu.email(m.get("Email"));
        String address=QuyTacNghiepVu.text(m.get("Địa chỉ"),"Địa chỉ",255);
        LocalDate birth=QuyTacNghiepVu.birth(m.get("Ngày sinh"));
        String gender=QuyTacNghiepVu.text(m.get("Giới tính"),"Giới tính",15),medical=Objects.toString(m.get("Tiền sử bệnh"),"").trim();
        QuyTacNghiepVu.need(Set.of("Nam", "Nữ", "Khác").contains(gender), "Giới tính không hợp lệ");
        QuyTacNghiepVu.need(medical.length()<=2000,"Tiền sử bệnh tối đa 2.000 ký tự");
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            PhanQuyen.lock("PATIENT_"+phone+"_"+name.toLowerCase(Locale.ROOT),c);
            if(TruyVanDAO.one(c,"SELECT maBN FROM dbo.BenhNhan WHERE LOWER(LTRIM(RTRIM(hoTen)))=LOWER(?) AND sdt=? AND ngaySinh=? AND maBN<>? AND ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động'",name,phone,birth,id==null?0:id)!=null)throw new SQLException("Hồ sơ bệnh nhân trùng tên, SĐT và ngày sinh");
            if(id==null)TruyVanDAO.update(c,"INSERT dbo.BenhNhan(hoTen,sdt,ngaySinh,gioiTinh,email,diaChi,tienSuBenh,trangThai,trangThaiHoSo) VALUES(?,?,?,?,?,?,?,N'Mới',N'Hoạt động')",name,phone,birth,gender,email.isEmpty()?null:email,address,medical);
            else{
                Map<String,Object> old=TruyVanDAO.one(c,"SELECT trangThaiHoSo FROM dbo.BenhNhan WITH(UPDLOCK,HOLDLOCK) WHERE maBN=?",id);
                if(old==null)throw new SQLException("Không thấy bệnh nhân");
                if("Ngừng hồ sơ".equals(TruyVanDAO.s(old.get("trangThaiHoSo"))))throw new SQLException("Hồ sơ đã ngừng; khôi phục trước khi sửa");
                TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.BenhNhan SET hoTen=?,sdt=?,ngaySinh=?,gioiTinh=?,email=?,diaChi=?,tienSuBenh=? WHERE maBN=?",name,phone,birth,gender,email.isEmpty()?null:email,address,medical,id),"Không sửa được bệnh nhân");
            }
            TruyVanDAO.audit(c,actor.accountId(),id==null?"Thêm bệnh nhân":"Sửa bệnh nhân","BenhNhan",name);
            return null;
        }
        );
    }
    public void stop(int id){
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            Map<String,Object> old=TruyVanDAO.one(c,"SELECT maBN,trangThaiHoSo FROM dbo.BenhNhan WITH(UPDLOCK,HOLDLOCK) WHERE maBN=?",id);
            if(old==null)throw new SQLException("Không tìm thấy bệnh nhân");
            if(TruyVanDAO.one(c,"SELECT TOP 1 maLichHen FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maBN=? AND trangThai NOT IN(N'Đã hủy',N'Hoàn tất',N'Đã khám')",id)!=null)throw new SQLException("Bệnh nhân còn lịch khám chưa kết thúc");
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.BenhNhan SET trangThaiHoSo=N'Ngừng hồ sơ' WHERE maBN=? AND ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động'",id),"Hồ sơ đã ngừng");
            TruyVanDAO.audit(c,actor.accountId(),"Ngừng hồ sơ","BenhNhan","maBN="+id);
            return null;
        }
        );
    }
    public void restore(int id){
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.BenhNhan SET trangThaiHoSo=N'Hoạt động' WHERE maBN=? AND trangThaiHoSo=N'Ngừng hồ sơ'",id),"Hồ sơ không tồn tại hoặc chưa ngừng");
            TruyVanDAO.audit(c,actor.accountId(),"Khôi phục hồ sơ","BenhNhan","maBN="+id);
            return null;
        }
        );
    }
}
