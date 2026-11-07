package Dao.NghiepVu;
import ConnectDB.CauHinh;
import ConnectDB.KetNoi;
import ConnectDB.*;
import Dao.*;
import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;
/** Đăng ký Quản lý: tài khoản đầu tiên hoặc có mã mời; mật khẩu lưu BCrypt. */
public final class XacThucService {
    public PhanQuyen.PhienDangNhap login(String user,String password){
        QuyTacNghiepVu.text(user,"Tên đăng nhập",60);
        QuyTacNghiepVu.need(password!=null&&!password.isBlank(),"Thiếu mật khẩu");
        try{
            KetNoi.kiemTraCauTruc();
        }
        catch(SQLException ex){
            throw TruyVanDAO.bad(ex);
        }
        return TruyVanDAO.tx(c->{
            Map<String,Object> r=TruyVanDAO.one(c,"SELECT TOP 1 t.maTK,t.tenDangNhap,t.matKhau,t.trangThai,t.phaiDoiMatKhau,n.hoTen,n.trangThai AS nhanVienTT,v.tenVaiTro FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK WHERE t.tenDangNhap=?",user.trim());
            if(r==null||!"Hoạt động".equals(TruyVanDAO.s(r.get("trangThai")))||!"Quản lý phòng khám".equals(TruyVanDAO.s(r.get("tenVaiTro")))||!"Đang làm việc".equals(TruyVanDAO.s(r.get("nhanVienTT"))))throw new SQLException("Thông tin đăng nhập không hợp lệ hoặc tài khoản không thuộc Quản lý.");
            String hash=TruyVanDAO.s(r.get("matKhau"));
            boolean match=false;
            try{
                match=BCrypt.checkpw(password,hash);
            }
            catch(Exception ignored){
            }
            if(!match)throw new SQLException("Tên đăng nhập hoặc mật khẩu không đúng.");
            if(Boolean.TRUE.equals(r.get("phaiDoiMatKhau")))throw new SQLException("Tài khoản phải đổi mật khẩu lần đầu theo quy trình Admin.");
            TruyVanDAO.update(c,"UPDATE dbo.TaiKhoan SET ngayDangNhapCuoi=SYSDATETIME() WHERE maTK=?",TruyVanDAO.n(r.get("maTK")));
            return new PhanQuyen.PhienDangNhap(TruyVanDAO.n(r.get("maTK")),TruyVanDAO.s(r.get("tenDangNhap")),TruyVanDAO.s(r.get("hoTen")));
        }
        );
    }
    public boolean firstManager(){
        Map<String,Object> r=TruyVanDAO.first("SELECT COUNT(*) AS n FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE v.tenVaiTro=N'Quản lý phòng khám'");
        return TruyVanDAO.n(r.get("n"))==0;
    }
    public void register(String user,String pass,String confirm,String fullName,String phone,String address,String birth,String sex,String invite){
        String username=QuyTacNghiepVu.text(user,"Tên tài khoản",60);
        QuyTacNghiepVu.need(username.matches("[a-zA-Z][a-zA-Z0-9_.-]{3,59}"),"Tài khoản 4-60 ký tự, bắt đầu bằng chữ");
        QuyTacNghiepVu.need(pass!=null&&pass.length()>=10&&pass.length()<=72&&pass.matches("(?s).*[A-Za-z].*")&&pass.matches("(?s).*\\d.*"),"Mật khẩu 10-72 ký tự, có chữ và số");
        QuyTacNghiepVu.need(pass.equals(confirm),"Xác nhận mật khẩu không khớp");
        String name=QuyTacNghiepVu.text(fullName,"Họ tên",120),tel=QuyTacNghiepVu.phone(phone),home=QuyTacNghiepVu.text(address,"Địa chỉ",255),gender=QuyTacNghiepVu.text(sex,"Giới tính",15);
        java.time.LocalDate dob=QuyTacNghiepVu.birth(birth);
        QuyTacNghiepVu.need(java.time.Period.between(dob,java.time.LocalDate.now()).getYears()>=18,"Người đăng ký vai trò Quản lý cần đủ 18 tuổi");
        QuyTacNghiepVu.need(java.util.Set.of("Nam","Nữ","Khác").contains(gender),"Giới tính không hợp lệ");
        TruyVanDAO.tx(c->{
            PhanQuyen.lock("MEDIDENT_MANAGER_REGISTER",c);
            Map<String,Object> count=TruyVanDAO.one(c,"SELECT COUNT(*) AS n FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro WHERE v.tenVaiTro=N'Quản lý phòng khám'");
            boolean first=TruyVanDAO.n(count.get("n"))==0;
            if(!first||!"true".equalsIgnoreCase(CauHinh.value("register.allowFirst"))){
                String code=CauHinh.value("register.invite");
                QuyTacNghiepVu.need(!code.isBlank()&&code.length()>=12&&!code.startsWith("CHANGE_")&&code.equals(invite),"Cần mã mời đăng ký Quản lý đã cấu hình trong db.properties (ít nhất 12 ký tự)");
            }
            Map<String,Object> role=TruyVanDAO.one(c,"SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản lý phòng khám'");
            if(role==null)throw new SQLException("Chưa có vai trò Quản lý. Chạy SqlData/01_MediDent.sql trước.");
            if(TruyVanDAO.one(c,"SELECT maTK FROM dbo.TaiKhoan WHERE tenDangNhap=?",username)!=null)throw new SQLException("Tên tài khoản đã tồn tại");
            if(TruyVanDAO.one(c,"SELECT maNV FROM dbo.NhanVien WHERE sdt=?",tel)!=null)throw new SQLException("Số điện thoại đã được dùng bởi nhân viên khác");
            int account=TruyVanDAO.insert(c,"INSERT dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro) VALUES(?,?,N'Hoạt động',0,?)",username,BCrypt.hashpw(pass,BCrypt.gensalt(12)),TruyVanDAO.n(role.get("maVaiTro")));
            int staff=TruyVanDAO.insert(c,"INSERT dbo.NhanVien(hoTen,sdt,diaChi,ngaySinh,gioiTinh,chucVu,trangThai,maTK) VALUES(?,?,?,?,?,N'Quản lý phòng khám',N'Đang làm việc',?)",name,tel,home,dob,gender,account);
            TruyVanDAO.update(c,"INSERT dbo.QuanLy(maNV,phongBan) VALUES(?,N'Phòng khám')",staff);
            TruyVanDAO.audit(c,account,"Đăng ký tài khoản Quản lý","TaiKhoan","Tài khoản "+username);
            return null;
        }
        );
    }
}
