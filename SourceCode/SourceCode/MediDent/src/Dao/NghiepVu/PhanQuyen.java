package Dao.NghiepVu;
import Dao.*;
import java.sql.*;
import java.util.*;
/** Quyền phải kiểm tra ngay tại tầng Service trong cùng transaction, không dựa vào ẩn nút. */
public final class PhanQuyen {
    public record PhienDangNhap(int accountId,String username,String fullName){
    }
    private PhanQuyen(){
    }
    public static void requireManager(Connection c,PhienDangNhap session)throws SQLException {
        if(session==null)throw new SQLException("Chế độ chạy màn hình độc lập chỉ được xem. Hãy đăng nhập để chỉnh sửa.");
        Map<String,Object> r=TruyVanDAO.one(c,"SELECT TOP 1 t.maTK FROM dbo.TaiKhoan t WITH(UPDLOCK,HOLDLOCK) JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro JOIN dbo.NhanVien n ON n.maTK=t.maTK WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND n.trangThai=N'Đang làm việc' AND v.tenVaiTro=N'Quản lý phòng khám'",session.accountId());
        if(r==null)throw new SQLException("Tài khoản không còn quyền Quản lý hoặc chưa liên kết nhân viên hoạt động.");
    }
    public static void lock(String scope,Connection c)throws SQLException{
        try(CallableStatement cs=c.prepareCall("{? = call sys.sp_getapplock(?, ?, ?, ?)}")){
            cs.registerOutParameter(1,Types.INTEGER);
            cs.setString(2,scope);
            cs.setString(3,"Exclusive");
            cs.setString(4,"Transaction");
            cs.setInt(5,10000);
            cs.execute();
            if(cs.getInt(1)<0)throw new SQLException("Nguồn lực đang bận. Hãy thử lại.");
        }
    }
}
