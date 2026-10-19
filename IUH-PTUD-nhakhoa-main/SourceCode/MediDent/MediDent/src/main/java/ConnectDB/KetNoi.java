/*
 * KetNoi: cấu hình kết nối và khởi tạo cơ sở dữ liệu.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class KetNoi {

    /** Ngăn tạo đối tượng tiện ích; kết nối SQL Server được mở qua phương thức tĩnh mo(). */
    private KetNoi() {

    }

    /** Mở kết nối SQL Server từ cấu hình JDBC và chuyển lỗi cấu hình thành thông báo. */
    public static Connection mo() throws SQLException {

        Properties properties = new Properties();
        try (InputStream input = KetNoi.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new SQLException("Không tìm thấy db.properties trong resources.");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new SQLException("Không đọc được db.properties.", exception);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        if (url == null || url.isBlank() || user == null || user.isBlank()
                || password == null || password.equals("CHANGE_ME")) {
            throw new SQLException("Chưa cấu hình kết nối. Hãy mở "
                    + "src/main/resources/db.properties để nhập máy chủ, user, password.");
        }

        return DriverManager.getConnection(url, user, password);
    }
}
