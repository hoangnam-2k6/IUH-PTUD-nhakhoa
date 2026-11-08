
package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {

    public static Connection con;
    public static ConnectDB instance = new ConnectDB();

    private ConnectDB() {
    }

    // Lấy instance và tự động kết nối nếu chưa kết nối
    public static ConnectDB GetInstance() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return instance;
    }

    // Kết nối SQL Server
    public void connect() {

        String url =
                "jdbc:sqlserver://localhost:1433;"
                + "databaseName=MediDent;"
                + "encrypt=true;"
                + "trustServerCertificate=true";

        String user = "sa";
        String password = "sapassword";

        try {
            con = DriverManager.getConnection(url, user, password);

            System.out.println("Kết nối SQL Server thành công!");

        } catch (SQLException e) {
            con = null;

            System.out.println("Kết nối SQL Server thất bại!");
            e.printStackTrace();
        }
    }

    // Đóng kết nối
    public static void disconnect() {

        if (con != null) {
            try {
                con.close();
                con = null;

                System.out.println("Đã đóng kết nối SQL Server!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Lấy Connection
    public static Connection getConnection() {

        GetInstance();

        if (con == null) {
            throw new IllegalStateException(
                "Connection đang null! Không thể kết nối SQL Server."
            );
        }

        return con;
    }
}
