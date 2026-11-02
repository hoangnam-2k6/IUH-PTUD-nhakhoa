package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

/**
 * [CH4-NFR06] Tập trung cấu hình và mở kết nối JDBC tới cùng database MediDent.
 * KHÔNG hiển thị GUI; để kiểm tra kết nối hãy chạy ConnectDB.KiemTraKetNoi.
 */
public final class KetNoi {
    private KetNoi() { }

    /**
     * [FIX-RESOURCES] Ưu tiên cấu hình ngoại JAR do người dùng chỉ định.
     * Nếu không có: đọc db.properties ở gốc project, sau đó tới src/main/resources
     * (vị trí mà Eclipse của người dùng đã đặt file).
     * Không hiển thị giao diện; các vai trò đều dùng chung nguồn cấu hình này.
     */
    private record NapCauHinh(Properties data, String nguon) { }

    private static NapCauHinh napCauHinh() throws SQLException {
        Properties p = new Properties();
        String nguon = "Chưa có db.properties";
        String pathTuyChinh = System.getenv("MEDIDENT_DB_CONFIG");

        if (pathTuyChinh != null && !pathTuyChinh.isBlank()) {
            // [FIX-RESOURCES] Ưu tiên tuyệt đối file do MEDIDENT_DB_CONFIG chỉ định.
            Path file = Path.of(pathTuyChinh).toAbsolutePath().normalize();
            if (!Files.isRegularFile(file)) {
                throw new SQLException("Không tìm thấy MEDIDENT_DB_CONFIG: " + file);
            }
            docFileNeuCo(p, file);
            nguon = file.toString() + " (MEDIDENT_DB_CONFIG)";
        } else {
            // [FIX-RESOURCES] 1. File ngoài JAR, cạnh pom.xml (ưu tiên khi có cả hai).
            Path oGoc = Path.of("db.properties").toAbsolutePath().normalize();
            // [FIX-RESOURCES] 2. File thật trong src/main/resources như ảnh Eclipse.
            Path resources = Path.of("src", "main", "resources", "db.properties")
                    .toAbsolutePath().normalize();
            if (Files.isRegularFile(oGoc)) {
                docFileNeuCo(p, oGoc);
                nguon = oGoc.toString();
            } else if (Files.isRegularFile(resources)) {
                docFileNeuCo(p, resources);
                nguon = resources.toString();
            } else {
                // [FIX-RESOURCES] Hỗ trợ IDE chạy bằng classpath khác thư mục làm việc.
                try (InputStream in = KetNoi.class.getClassLoader()
                        .getResourceAsStream("db.properties")) {
                    if (in != null) {
                        p.load(in);
                        nguon = "classpath:/db.properties";
                    }
                } catch (IOException ex) {
                    throw new SQLException("Không đọc được db.properties trên classpath", ex);
                }
            }
        }

        // [FIX-RESOURCES] Biến môi trường vẫn được ưu tiên cho máy triển khai.
        ganNeuCo(p, "db.url", System.getenv("MEDIDENT_DB_URL"));
        ganNeuCo(p, "db.user", System.getenv("MEDIDENT_DB_USER"));
        ganNeuCo(p, "db.password", System.getenv("MEDIDENT_DB_PASSWORD"));
        return new NapCauHinh(p, nguon);
    }

    private static Properties layCauHinh() throws SQLException {
        return napCauHinh().data();
    }

    private static void docFileNeuCo(Properties properties, Path file) throws SQLException {
        if (!Files.isRegularFile(file)) return;
        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        } catch (IOException ex) {
            throw new SQLException("Không đọc được file cấu hình SQL: " + file, ex);
        }
    }

    private static void ganNeuCo(Properties properties, String key, String value) {
        if (value != null && !value.isBlank()) properties.setProperty(key, value);
    }

    /**
     * [FIX-SSL-DIAG] Chẩn đoán nguồn cấu hình SQL đang được JVM sử dụng.
     * Chỉ in đường dẫn, nguồn ghi đè và cờ SSL; KHÔNG in mật khẩu hoặc URL đầy đủ.
     * Chạy từ ConnectDB.KiemTraKetNoi để tìm cấu hình false đang ghi đè.
     */
    public static String chanDoanCauHinh() throws SQLException {
        NapCauHinh loaded = napCauHinh(); // [FIX-RESOURCES] Dùng chung logic với mo().
        String url = loaded.data().getProperty("db.url", "");
        Path goc = Path.of("db.properties").toAbsolutePath().normalize();
        Path resources = Path.of("src", "main", "resources", "db.properties")
                .toAbsolutePath().normalize();
        return "Thư mục chạy: " + Path.of("").toAbsolutePath().normalize()
                + System.lineSeparator() + "Cấu hình đang đọc: " + loaded.nguon()
                + System.lineSeparator() + "File ở gốc: " + goc
                + " (tồn tại=" + Files.isRegularFile(goc) + ")"
                + System.lineSeparator() + "File src/main/resources: " + resources
                + " (tồn tại=" + Files.isRegularFile(resources) + ")"
                + System.lineSeparator() + "MEDIDENT_DB_CONFIG ghi đè file: " + hasEnv("MEDIDENT_DB_CONFIG")
                + System.lineSeparator() + "MEDIDENT_DB_URL ghi đè JDBC URL: " + hasEnv("MEDIDENT_DB_URL")
                + System.lineSeparator() + "SSL encrypt: " + giaTriUrl(url, "encrypt")
                + System.lineSeparator() + "SSL trustServerCertificate: "
                + giaTriUrl(url, "trustServerCertificate");
    }

    private static boolean hasEnv(String name) {
        String value = System.getenv(name); // [FIX-SSL-DIAG] Không in giá trị env.
        return value != null && !value.isBlank();
    }

    private static String giaTriUrl(String url, String key) {
        // [FIX-SSL-DIAG] Tách đúng tham số JDBC theo dấu ;, không lộ mật khẩu.
        for (String part : url.split(";")) {
            int pos = part.indexOf('=');
            if (pos > 0 && key.equalsIgnoreCase(part.substring(0, pos).trim())) {
                return part.substring(pos + 1).trim();
            }
        }
        return "(không ghi trong URL)";
    }

    /**
     * [CH3-UC001] Chỉ mở kết nối; mọi UI/DAO phải gọi hàm này để dùng cùng database.
     * JDBC kiểm tra URL, xác thực SQL Server và báo lỗi rõ ràng (không in mật khẩu).
     */
    public static Connection mo() throws SQLException {
        Properties config = layCauHinh();
        String url = config.getProperty("db.url", "").trim();
        String user = config.getProperty("db.user", "").trim();
        String password = config.getProperty("db.password", "");

        // [FIX-CONFIG] Không yêu cầu tạo hộp thoại; chỉ rõ vị trí file cấu hình.
        if (!url.startsWith("jdbc:sqlserver://") || user.isBlank() || password.isBlank()) {
            throw new SQLException("Thiếu db.url/db.user/db.password. "
                    + "Đặt db.properties tại thư mục gốc hoặc src/main/resources, sau đó chạy ConnectDB.KiemTraKetNoi.");
        }
        // [CH4-ERD] Ngăn đăng nhập nhầm database dùng cho ứng dụng khác.
        if (!url.toLowerCase(Locale.ROOT).contains("databasename=medident")) {
            throw new SQLException("db.url phải trỏ tới databaseName=MediDent.");
        }
        try {
            Connection c = DriverManager.getConnection(url, user, password);
            // [CH4-ERD] Cả ba vai trò phải dùng đúng cùng database MediDent.
            if (!"MediDent".equalsIgnoreCase(c.getCatalog())) {
                String actual = c.getCatalog();
                c.close();
                throw new SQLException("Đang kết nối database " + actual + ", không phải MediDent.");
            }
            return c;
        } catch (SQLException ex) {
            // [FIX-SSL] Không tự động bỏ xác thực TLS; hướng dẫn riêng cho chứng chỉ tự ký.
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            if (message.contains("pkix") || message.contains("certificate_unknown")
                    || message.contains("certificate path")) {
                // [FIX-SSL-DIAG] Chỉ ra JVM thực sự đọc cờ nào; không tiết lộ mật khẩu.
                throw new SQLException("SQL Server dùng chứng chỉ TLS chưa được tin cậy. "
                        + "Máy LAB/local có thể dùng encrypt=true;trustServerCertificate=true. "
                        + "Nếu bạn đã chỉnh true nhưng lỗi vẫn ghi false, hãy xem nguồn ghi đè dưới đây:\n"
                        + chanDoanCauHinh() + System.lineSeparator()
                        + "Khi triển khai thật: sử dụng chứng chỉ TLS hợp lệ.", ex);
            }
            throw ex;
        }
    }
}
