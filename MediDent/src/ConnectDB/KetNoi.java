package ConnectDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class KetNoi {
    private static Properties cauHinh;

    private KetNoi() {}

    private static synchronized Properties docCauHinh() throws SQLException {
        if (cauHinh == null) {
            try (InputStream in = KetNoi.class.getResourceAsStream("/Resource/db.properties")) {
                if (in == null) {
                    throw new SQLException("Không tìm thấy file Resource/db.properties");
                }
                Properties p = new Properties();
                p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                cauHinh = p;
            } catch (IOException e) {
                throw new SQLException("Không đọc được file db.properties", e);
            }
        }
        return cauHinh;
    }

    public static Connection getConnection() throws SQLException {
        Properties p = docCauHinh();
        return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
    }
}
