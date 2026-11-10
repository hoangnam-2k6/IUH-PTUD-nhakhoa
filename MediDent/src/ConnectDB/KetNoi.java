package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối SQL Server cho phân hệ Lễ tân.
 *
 * <p>Cấu hình kết nối được nhúng trực tiếp trong lớp này (không dùng file
 * {@code db.properties}). Khi cần chạy trên máy khác, có thể ghi đè bằng
 * biến môi trường mà không phải sửa mã nguồn:</p>
 * <ul>
 *     <li>{@code MEDIDENT_DB_URL}</li>
 *     <li>{@code MEDIDENT_DB_USER}</li>
 *     <li>{@code MEDIDENT_DB_PASSWORD}</li>
 * </ul>
 *
 * <p>Mô hình 2 tầng: lớp UI gọi thẳng lớp DAO, DAO gọi {@link #getConnection()}
 * để làm việc với SQL Server.</p>
 */
public final class KetNoi {
    public static final String ENV_URL = "MEDIDENT_DB_URL";
    public static final String ENV_USER = "MEDIDENT_DB_USER";
    public static final String ENV_PASSWORD = "MEDIDENT_DB_PASSWORD";

    /** Chuỗi kết nối mặc định tới CSDL dùng chung của nhóm. */
    public static final String DB_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=MediDent;"
                    + "encrypt=true;trustServerCertificate=true;loginTimeout=10";

    /** Tài khoản SQL Server dùng cho ứng dụng (cấu hình theo máy triển khai). */
    public static final String DB_USER = "sa";
    public static final String DB_PASSWORD = "sapassword";

    private KetNoi() {
    }

    /**
     * Mở một kết nối mới tới SQL Server. Người gọi phải đóng kết nối
     * (nên dùng try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        String url = giaTri(ENV_URL, DB_URL);
        String user = giaTri(ENV_USER, DB_USER);
        String password = giaTri(ENV_PASSWORD, DB_PASSWORD);

        Properties thongTin = new Properties();
        if (!user.isEmpty()) {
            thongTin.setProperty("user", user);
            thongTin.setProperty("password", password);
        }

        try {
            return DriverManager.getConnection(url, thongTin);
        } catch (SQLException e) {
            throw new SQLException(taoThongBaoLoi(url, e), e.getSQLState(), e.getErrorCode(), e);
        }
    }

    private static String giaTri(String tenBienMoiTruong, String macDinh) {
        String giaTri = System.getenv(tenBienMoiTruong);
        return giaTri == null || giaTri.isBlank() ? macDinh : giaTri.trim();
    }

    private static String taoThongBaoLoi(String url, SQLException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Không kết nối được tới SQL Server.\n")
          .append("• Chuỗi kết nối: ").append(url).append('\n')
          .append("• Lỗi gốc: ").append(e.getMessage()).append('\n')
          .append("Gợi ý kiểm tra:\n")
          .append("  1. SQL Server đã chạy và mở cổng TCP 1433 chưa?\n")
          .append("  2. Đã chạy SqlData/00_KhoiTao_Va_DuLieuDemo_DAY_DU_SQL2012.sql chưa?\n")
          .append("  3. Tài khoản Tài khoản SQL Server có quyền truy cập CSDL MediDent không?\n")
          .append("  4. Có thể đặt biến môi trường MEDIDENT_DB_URL / MEDIDENT_DB_USER / ")
          .append("MEDIDENT_DB_PASSWORD để ghi đè cấu hình.");
        return sb.toString();
    }

    /** Mô tả ngắn gọn cấu hình hiện tại (không lộ mật khẩu) để hiển thị khi lỗi. */
    public static String moTaKetNoi() {
        String url = giaTri(ENV_URL, DB_URL);
        String nguon = System.getenv(ENV_URL) != null ? " (biến môi trường)" : " (mặc định)";
        return url + nguon;
    }
}
