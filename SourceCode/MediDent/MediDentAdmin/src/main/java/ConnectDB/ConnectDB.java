package ConnectDB;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

/** Pool JDBC nhỏ cho Swing. DAO luôn đóng Connection; close() trả về pool, không đóng socket. */
public final class ConnectDB {
    private static final ConnectDB INSTANCE = new ConnectDB();
    private static final int TOI_DA = 5;
    private static final Deque<Connection> RANH = new ArrayDeque<>();
    private static int tongKetNoi;
    private static final String URL = cauHinh("MEDIDENT_DB_URL",
            "jdbc:sqlserver://localhost:1433;databaseName=MediDent;encrypt=false;trustServerCertificate=true;loginTimeout=5");
    private static final String USER = cauHinh("MEDIDENT_DB_USER", "sa2");
    private static final String PASSWORD = cauHinh("MEDIDENT_DB_PASSWORD", "sapassword");

    private ConnectDB() { }
    public static ConnectDB GetInstance() { return INSTANCE; }

    private static String cauHinh(String khoa, String macDinh) {
        String giaTri = System.getenv(khoa);
        return giaTri == null || giaTri.isBlank() ? macDinh : giaTri;
    }

    /** Kiểm tra kết nối; không giữ connection toàn cục. */
    public void connect() throws SQLException {
        try (Connection ignored = getConnection()) { /* trả kết nối về pool */ }
    }

    private static Connection layKetNoiVatLy() throws SQLException {
        long ketThuc = System.currentTimeMillis() + 10000;
        while (true) {
            Connection c;
            synchronized (RANH) {
                c = RANH.pollFirst();
                if (c == null && tongKetNoi < TOI_DA) {
                    tongKetNoi++;
                    break;
                }
                if (c == null) {
                    long conLai = ketThuc - System.currentTimeMillis();
                    if (conLai <= 0) throw new SQLException("Hết thời gian chờ kết nối JDBC.");
                    try { RANH.wait(conLai); }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Đã dừng thao tác chờ kết nối.", e);
                    }
                    continue;
                }
            }
            try { if (!c.isClosed() && c.isValid(2)) return c; }
            catch (SQLException ignored) { /* loại socket lỗi */ }
            try { c.close(); } catch (SQLException ignored) { }
            synchronized (RANH) { tongKetNoi--; RANH.notifyAll(); }
        }
        try { return DriverManager.getConnection(URL, USER, PASSWORD); }
        catch (SQLException e) {
            synchronized (RANH) { tongKetNoi--; RANH.notifyAll(); }
            throw e;
        }
    }

    private static void traKetNoi(Connection c) {
        try {
            if (!c.getAutoCommit()) c.rollback();
            c.setAutoCommit(true);
            c.setReadOnly(false);
            c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            if (c.isClosed() || !c.isValid(2)) throw new SQLException("Kết nối đã đóng.");
            synchronized (RANH) { RANH.addLast(c); RANH.notifyAll(); }
        } catch (SQLException e) {
            try { c.close(); } catch (SQLException ignored) { }
            synchronized (RANH) { tongKetNoi--; RANH.notifyAll(); }
        }
    }

    /** Mỗi bên gọi nhận một handle riêng; try-with-resources chỉ trả lại pool. */
    public static Connection getConnection() {
        Connection vatLy;
        try { vatLy = layKetNoiVatLy(); }
        catch (SQLException e) { throw new IllegalStateException("Không kết nối được SQL Server: " + e.getMessage(), e); }
        final boolean[] daDong = {false};
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                String ten = method.getName();
                if ("close".equals(ten)) {
                    if (!daDong[0]) { daDong[0] = true; traKetNoi(vatLy); }
                    return null;
                }
                if ("isClosed".equals(ten)) return daDong[0] || vatLy.isClosed();
                if ("unwrap".equals(ten)) return vatLy.unwrap((Class<?>) args[0]);
                if ("isWrapperFor".equals(ten)) return vatLy.isWrapperFor((Class<?>) args[0]);
                if (daDong[0]) throw new SQLException("Connection đã trả về pool.");
                try { return method.invoke(vatLy, args); }
                catch (InvocationTargetException e) { throw e.getCause(); }
            });
    }

    public static void disconnect() {
        synchronized (RANH) {
            while (!RANH.isEmpty()) {
                try { RANH.removeFirst().close(); } catch (SQLException ignored) { }
                tongKetNoi--;
            }
            RANH.notifyAll();
        }
    }
}
