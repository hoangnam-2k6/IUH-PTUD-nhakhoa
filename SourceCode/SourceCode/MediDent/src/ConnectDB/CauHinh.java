package ConnectDB;

/**
 * Thông tin kết nối được khai báo ngay trong mã Java, không đọc db.properties.
 * Khi chuyển máy chỉ cần sửa ba hằng số dưới đây rồi biên dịch lại.
 * Lưu ý: không đẩy mật khẩu thật lên GitHub.
 */
public final class CauHinh {
    // SỬA Ở ĐÂY: IP, cổng và tên cơ sở dữ liệu SQL Server.
    public static final String ĐƯỜNG_DẪN_SQL = "jdbc:sqlserver://localhost:1433;databaseName=MediDent;encrypt=true;trustServerCertificate=true;sendTimeAsDatetime=false;loginTimeout=8";
    public static final String TÀI_KHOẢN_SQL = "sa";
    public static final String MẬT_KHẨU_SQL = "sapassword";

    // Chỉ dùng khi đăng ký Quản lý; không cấp quyền đăng ký tự do.
    public static final boolean CHO_Phep_Tai_Khoan_Dau = false;
    public static final String Ma_Moi_Dang_Ky = "sapassword";

    private CauHinh() { }

    // Giữ cách gọi cũ để không làm ảnh hưởng các phần nhóm sẽ merge sau.
    public static String value(String key) {
        return switch (key) {
            case "db.url" -> ĐƯỜNG_DẪN_SQL;
            case "db.user" -> TÀI_KHOẢN_SQL;
            case "db.password" -> MẬT_KHẨU_SQL;
            case "register.allowFirst" -> String.valueOf(CHO_Phep_Tai_Khoan_Dau);
            case "register.invite" -> Ma_Moi_Dang_Ky;
            default -> "";
        };
    }

    public static String duongDanDangDoc() {
        return "Các hằng số trong ConnectDB.CauHinh.java";
    }

    public static void require() {
        if (!ĐƯỜNG_DẪN_SQL.startsWith("jdbc:sqlserver://")) {
            throw new IllegalStateException("Đường dẫn SQL Server không hợp lệ trong CauHinh.java");
        }
        if (TÀI_KHOẢN_SQL.isBlank() || MẬT_KHẨU_SQL.isBlank()
                || MẬT_KHẨU_SQL.equals("NHAP_MAT_KHAU_SQL_CUA_BAN")) {
            throw new IllegalStateException("Hãy nhập tài khoản và mật khẩu SQL thật trong ConnectDB/CauHinh.java");
        }
    }
}
