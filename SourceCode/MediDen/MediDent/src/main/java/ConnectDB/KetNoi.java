package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Mở kết nối JDBC đến SQL Server dùng chung cho toàn bộ DAO.
 *
 * Đường dẫn, tên đăng nhập và mật khẩu lấy từ db.properties trong resources.
 * Mỗi DAO tự đóng Connection bằng try-with-resources sau khi sử dụng.
 */
public final class KetNoi {

    private KetNoi() {
        // Không khởi tạo lớp tiện ích.
    }

    public static Connection mo() throws SQLException {
        // 1. Đọc cấu hình trong classpath của Maven.
        Properties properties = new Properties();
        try (InputStream input = KetNoi.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new SQLException("Không tìm thấy db.properties trong resources.");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new SQLException("Không đọc được db.properties.", exception);
        }

        // 2. Kiểm tra người dùng đã cấu hình đúng chưa.
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        if (url == null || url.isBlank() || user == null || user.isBlank()
                || password == null || password.equals("CHANGE_ME")) {
            throw new SQLException("Chưa cấu hình kết nối. Hãy mở "
                    + "src/main/resources/db.properties để nhập máy chủ, user, password.");
        }

        // 3. Driver Microsoft JDBC tạo kết nối đến SQL Server.
        return DriverManager.getConnection(url, user, password);
    }
}
