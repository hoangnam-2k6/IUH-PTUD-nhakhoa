package Dao.NghiepVu;
import Dao.*;
import java.time.*;
import java.sql.*;
import java.util.*;
public final class NhanVienService {
    private final PhanQuyen.PhienDangNhap actor;
    public NhanVienService(PhanQuyen.PhienDangNhap a){
        actor=a;
    }
    public List<Map<String,Object>> list(){
        return TruyVanDAO.list("SELECT maNV AS ID,hoTen AS [Họ tên],sdt AS [SĐT],CONVERT(varchar(10),ngaySinh,23) AS [Ngày sinh],CASE WHEN ngaySinh IS NULL THEN NULL ELSE DATEDIFF(year,ngaySinh,CONVERT(date,GETDATE()))-CASE WHEN DATEADD(year,DATEDIFF(year,ngaySinh,CONVERT(date,GETDATE())),ngaySinh)>CONVERT(date,GETDATE()) THEN 1 ELSE 0 END END AS [Tuổi],gioiTinh AS [Giới tính],email AS [Email],diaChi AS [Địa chỉ],chucVu AS [Chức vụ],trangThai AS [Trạng thái] FROM dbo.NhanVien ORDER BY maNV DESC");
    }
    public void save(Integer id,Map<String,String> m){
        String name=QuyTacNghiepVu.text(m.get("Họ tên"),"Họ tên",120),phone=QuyTacNghiepVu.phone(m.get("SĐT")),email=QuyTacNghiepVu.email(m.get("Email"));
        String address=QuyTacNghiepVu.text(m.get("Địa chỉ"),"Địa chỉ",255),gender=QuyTacNghiepVu.text(m.get("Giới tính"),"Giới tính",15),job=QuyTacNghiepVu.text(m.get("Chức vụ"),"Chức vụ",100),status=QuyTacNghiepVu.text(m.get("Trạng thái"),"Trạng thái",30);
        QuyTacNghiepVu.need(Set.of("Nam", "Nữ", "Khác").contains(gender), "Giới tính không hợp lệ");
        QuyTacNghiepVu.need(Set.of("Bác sĩ","Lễ tân","Kế toán","Quản lý phòng khám","Nhân viên").contains(job),"Chức vụ không hợp lệ");
        QuyTacNghiepVu.need(Set.of("Đang làm việc","Tạm nghỉ","Nghỉ việc").contains(status),"Trạng thái không hợp lệ");
        LocalDate birth=QuyTacNghiepVu.birth(m.get("Ngày sinh"));
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            PhanQuyen.lock("NHANVIEN_PHONE_"+phone,c);
            if(TruyVanDAO.one(c,"SELECT maNV FROM dbo.NhanVien WHERE sdt=? AND maNV<>?",phone,id==null?0:id)!=null)throw new SQLException("Số điện thoại nhân viên đã tồn tại");
            if(id==null){
                QuyTacNghiepVu.need("Đang làm việc".equals(status),"Nhân viên mới phải đang làm việc");
                TruyVanDAO.update(c,"INSERT dbo.NhanVien(hoTen,sdt,email,diaChi,ngaySinh,gioiTinh,chucVu,trangThai) VALUES(?,?,?,?,?,?,?,?)",name,phone,email.isEmpty()?null:email,address,birth,gender,job,status);
            }
            else{
                Map<String,Object> old=TruyVanDAO.one(c,"SELECT maTK,chucVu FROM dbo.NhanVien WITH(UPDLOCK,HOLDLOCK) WHERE maNV=?",id);
                if(old==null)throw new SQLException("Nhân viên không tồn tại");
                if(old.get("maTK")!=null&&!job.equals(TruyVanDAO.s(old.get("chucVu"))))throw new SQLException("Nhân viên đã có tài khoản; không được tự đổi chức vụ liên kết");
                if((!"Bác sĩ".equals(job)||!"Đang làm việc".equals(status)))ensureNoOpenSchedules(c,id);
                TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.NhanVien SET hoTen=?,sdt=?,email=?,diaChi=?,ngaySinh=?,gioiTinh=?,chucVu=?,trangThai=? WHERE maNV=?",name,phone,email.isEmpty()?null:email,address,birth,gender,job,status,id),"Không lưu được nhân viên");
            }
            TruyVanDAO.audit(c,actor.accountId(),id==null?"Thêm nhân viên":"Sửa nhân viên","NhanVien",name);
            return null;
        }
        );
    }
    private void ensureNoOpenSchedules(Connection c,int id)throws SQLException{
        if(TruyVanDAO.one(c,"SELECT TOP 1 maLichHen FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maNV=? AND ngayHen>=CAST(GETDATE() AS date) AND trangThai NOT IN(N'Hoàn tất',N'Đã hủy',N'Đã khám')",id)!=null)throw new SQLException("Bác sĩ còn lịch hẹn chưa kết thúc, hãy phân công lại trước");
        if(TruyVanDAO.one(c,"SELECT TOP 1 maLichLV FROM dbo.LichLamViec WITH(UPDLOCK,HOLDLOCK) WHERE maNV=? AND ngayLamViec>=CAST(GETDATE() AS date) AND ISNULL(trangThai,N'') NOT IN(N'Ngừng xếp lịch',N'Đã hủy')",id)!=null)throw new SQLException("Nhân viên còn ca làm việc chưa xử lý");
    }
    public void stop(int id){
        TruyVanDAO.tx(c->{
            PhanQuyen.requireManager(c,actor);
            Map<String,Object> old=TruyVanDAO.one(c,"SELECT maTK,trangThai FROM dbo.NhanVien WITH(UPDLOCK,HOLDLOCK) WHERE maNV=?",id);
            if(old==null)throw new SQLException("Không tìm thấy nhân viên");
            if(old.get("maTK")!=null)throw new SQLException("Nhân viên có tài khoản: phải xử lý tài khoản trước");
            ensureNoOpenSchedules(c,id);
            TruyVanDAO.exactlyOne(TruyVanDAO.update(c,"UPDATE dbo.NhanVien SET trangThai=N'Nghỉ việc' WHERE maNV=? AND trangThai<>N'Nghỉ việc'",id),"Nhân viên đã nghỉ việc");
            TruyVanDAO.audit(c,actor.accountId(),"Cho nghỉ việc","NhanVien","maNV="+id);
            return null;
        }
        );
    }
}
