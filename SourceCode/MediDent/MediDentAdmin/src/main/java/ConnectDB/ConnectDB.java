package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Kết nối JDBC SQL Server. Mỗi DAO tự đóng Connection sau khi truy vấn. */
public final class ConnectDB {
    private ConnectDB() { }

    public static Connection mo() throws SQLException {
        // Đọc cấu hình thay vì ghi cứng tên database/mật khẩu trong mã Java.
        Properties cauHinh = new Properties();
        try (InputStream tep = ConnectDB.class.getResourceAsStream("/db.properties")) {
            if (tep == null) throw new SQLException("Không tìm thấy db.properties.");
            cauHinh.load(tep);
        } catch (IOException e) {
            throw new SQLException("Không đọc được db.properties.", e);
        }
        String url = cauHinh.getProperty("db.url", "").trim();
        String taiKhoan = cauHinh.getProperty("db.user", "").trim();
        String matKhau = cauHinh.getProperty("db.password", "");
        if (url.isEmpty() || taiKhoan.isEmpty() || matKhau.isEmpty()
                || "CHANGE_ME".equals(matKhau)) {
            throw new SQLException("Cấu hình db.url, db.user và db.password trong resources/db.properties.");
        }
        return DriverManager.getConnection(url, taiKhoan, matKhau);
    }
}
