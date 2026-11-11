package ConnectDB;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;

/** Mỗi DAO mượn một kết nối độc lập, close() chỉ trả về pool. */
public final class ConnectDB {
    private static final String MAY_CHU = "localhost:1433";
    private static final String TAI_KHOAN = "sa2";
    private static final String MAT_KHAU = "sapassword";
    private static final int TOI_DA_RANH = 4;
    private static final ArrayDeque<Connection> ranh = new ArrayDeque<>();
    private static final ConnectDB instance = new ConnectDB();
    private ConnectDB() { }

    public static ConnectDB layTheHien() { return instance; }
    /** Giữ tương thích với các nơi gọi instance.ketNoi(). */
    public void ketNoi() {
        try (Connection c = layConnection()) {
            if (!c.isValid(3)) throw new SQLException("Kết nối SQL Server không hợp lệ.");
        } catch (SQLException e) { throw new IllegalStateException("Không kết nối được SQL Server: " + e.getMessage(), e); }
    }

    public static Connection layConnection() throws SQLException {
        Connection vatLy = null;
        synchronized (ranh) {
            while (!ranh.isEmpty()) {
                Connection ungVien = ranh.removeFirst();
                try {
                    if (!ungVien.isClosed() && ungVien.isValid(2)) { vatLy = ungVien; break; }
                } catch (SQLException ignored) { }
                try { ungVien.close(); } catch (SQLException ignored) { }
            }
        }
        if (vatLy == null) vatLy = taoKetNoiVatLy();
        final Connection ketNoiVatLy = vatLy;
        final boolean[] daTra = {false};
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    String ten = method.getName();
                    if ("close".equals(ten)) {
                        if (!daTra[0]) {
                            daTra[0] = true;
                            try {
                                if (!ketNoiVatLy.getAutoCommit()) ketNoiVatLy.rollback();
                                ketNoiVatLy.setAutoCommit(true);
                                ketNoiVatLy.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                                ketNoiVatLy.setReadOnly(false);
                                ketNoiVatLy.clearWarnings();
                                if (!ketNoiVatLy.isClosed() && ketNoiVatLy.isValid(2)) {
                                    synchronized (ranh) {
                                        if (ranh.size() < TOI_DA_RANH) { ranh.addLast(ketNoiVatLy); return null; }
                                    }
                                }
                            } catch (SQLException ignored) { }
                            ketNoiVatLy.close();
                        }
                        return null;
                    }
                    if ("isClosed".equals(ten)) return daTra[0] || ketNoiVatLy.isClosed();
                    if (daTra[0]) throw new SQLException("Connection đã được trả về pool.");
                    try { return method.invoke(ketNoiVatLy, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                });
    }

    public static Connection taoKetNoiMoi() throws SQLException {
        return layConnection();
    }
    private static Connection taoKetNoiVatLy() throws SQLException {
        String url = "jdbc:sqlserver://" + MAY_CHU
                + ";databaseName=MediDent;encrypt=true;trustServerCertificate=true;loginTimeout=5";
        return DriverManager.getConnection(url, TAI_KHOAN, MAT_KHAU);
    }
    /** Chỉ gọi khi tắt ứng dụng, không gọi sau mỗi nút. */
    public static void ngatKetNoi() {
        synchronized (ranh) {
            while (!ranh.isEmpty()) {
                try { ranh.removeFirst().close(); } catch (SQLException ignored) { }
            }
        }
    }
}
