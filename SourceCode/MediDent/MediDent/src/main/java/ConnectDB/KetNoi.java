package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Mở kết nối JDBC đến SQL Server dùng chung cho toàn bộ DAO.
 *
 * Khi chạy JAR, cấu hình lấy từ db.properties cạnh JAR; khi chạy trong IDE,
 * cấu hình lấy từ resources.
 * Mỗi DAO tự đóng Connection bằng try-with-resources sau khi sử dụng.
 */
public final class KetNoi {

    private KetNoi() {
        // Không khởi tạo lớp tiện ích.
    }

    public static Connection mo() throws SQLException {
        Properties properties = docCauHinh();

        // 2. Kiểm tra người dùng đã cấu hình đúng chưa.
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        if (url == null || url.isBlank() || user == null || user.isBlank()
                || "CHANGE_ME".equals(user) || password == null || password.isBlank()
                || "CHANGE_ME".equals(password)) {
            throw new SQLException("Chưa cấu hình kết nối. Hãy điền db.url, db.user và "
                    + "db.password trong db.properties cạnh file JAR.");
        }

        // 3. Driver Microsoft JDBC tạo kết nối đến SQL Server.
        return DriverManager.getConnection(url, user, password);
    }

    static Properties docCauHinh() throws SQLException {
        Properties properties = new Properties();
        Path viTriChay = layViTriChay();
        if (!Files.isDirectory(viTriChay)) {
            Path cauHinhNgoai = viTriChay.resolveSibling("db.properties");
            if (!Files.isRegularFile(cauHinhNgoai)) {
                throw new SQLException("Không tìm thấy db.properties cạnh file JAR: "
                        + cauHinhNgoai);
            }
            try (InputStream input = Files.newInputStream(cauHinhNgoai)) {
                properties.load(input);
                return properties;
            } catch (IOException exception) {
                throw new SQLException("Không đọc được db.properties cạnh file JAR.", exception);
            }
        }

        try (InputStream input = KetNoi.class.getResourceAsStream("/db.properties")) {
            if (input == null) {
                throw new SQLException("Không tìm thấy db.properties trong resources.");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new SQLException("Không đọc được db.properties.", exception);
        }
    }

    private static Path layViTriChay() throws SQLException {
        try {
            return Paths.get(KetNoi.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException | NullPointerException exception) {
            throw new SQLException("Không xác định được vị trí chạy MediDent.", exception);
        }
    }
}
