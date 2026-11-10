package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String MAY_CHU = "localhost:1433";
    private static final String TAI_KHOAN = "sa";
    private static final String MAT_KHAU = "sapassword";
    private static Connection con;
    private static final ConnectDB instance = new ConnectDB();

    private ConnectDB() { }

    public static synchronized ConnectDB GetInstance() {
        try {
            if (con == null || con.isClosed()) instance.connect();
        } catch (SQLException loi) {
            throw new IllegalStateException("Không kết nối được SQL Server: " + loi.getMessage(), loi);
        }
        return instance;
    }

    // Giữ tương thích với DAO cũ của các chức năng không được sửa.
    public synchronized void connect() {
        try { con = taoKetNoi("MediDent"); }
        catch (SQLException loi) { con = null; throw new IllegalStateException("Kết nối SQL Server thất bại: " + loi.getMessage(), loi); }
    }

    public static synchronized Connection getConnection() {
        GetInstance();
        return con;
    }

    // DAO mới đóng Connection riêng qua try-with-resources để không tranh chấp transaction.
    public static Connection taoKetNoiMoi() throws SQLException {
        return taoKetNoi("MediDent");
    }

    static Connection taoKetNoi(String tenCSDL) throws SQLException {
        String url = "jdbc:sqlserver://" + MAY_CHU + ";databaseName=" + tenCSDL
                + ";encrypt=true;trustServerCertificate=true;loginTimeout=5";
        return DriverManager.getConnection(url, TAI_KHOAN, MAT_KHAU);
    }

    public static synchronized void disconnect() {
        if (con != null) {
            try { con.close(); } catch (SQLException loi) { loi.printStackTrace(); }
            con = null;
        }
    }
}
