package ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Công cụ nhỏ để kiểm tra nhanh kết nối CSDL trước khi chạy ứng dụng.
 *
 * <p>Cách chạy (sau khi biên dịch):</p>
 * <pre>
 * java -cp "bin;lib/mssql-jdbc.jar" ConnectDB.KiemTraKetNoi
 * </pre>
 */
public final class KiemTraKetNoi {
    private KiemTraKetNoi() {
    }

    public static void main(String[] args) {
        System.out.println("Chuỗi kết nối: " + KetNoi.moTaKetNoi());
        try (Connection conn = KetNoi.getConnection()) {
            System.out.println("Kết nối thành công!");
            System.out.println("  Server : " + conn.getMetaData().getDatabaseProductName()
                    + " " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("  CSDL   : " + conn.getCatalog());
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT DB_NAME() AS Db, SUSER_SNAME() AS NguoiDung, COUNT(*) AS SoBang "
                            + "FROM sys.tables");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("  Database: " + rs.getString("Db"));
                    System.out.println("  Tài khoản: " + rs.getString("NguoiDung"));
                    System.out.println("  Số bảng:  " + rs.getInt("SoBang"));
                }
            }
        } catch (SQLException e) {
            System.err.println("KẾT NỐI THẤT BẠI");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
