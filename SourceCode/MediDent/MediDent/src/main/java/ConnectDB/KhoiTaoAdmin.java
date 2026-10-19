/*
 * KhoiTaoAdmin: cấu hình kết nối và khởi tạo cơ sở dữ liệu.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package ConnectDB;

import java.awt.GraphicsEnvironment;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;
import javax.swing.*;
import java.awt.*;

public class KhoiTaoAdmin {
    /** Khởi chạy chương trình theo điểm vào được định nghĩa tại lớp này. */
    public static void main(String[] args) {

        try (Connection c = KetNoi.mo()) {
            if (daCoQuanTriVien(c)) {
                thongBao("CSDL đã có tài khoản Quản trị viên.\n"
                    + "Hãy dùng màn hình đăng nhập hoặc quản lý tài khoản để cấp quyền.");
                return;
            }
        } catch (SQLException e) {
            baoLoi("Không thể kiểm tra CSDL: " + e.getMessage());
            return;
        }

        String username;
        String hoTen;
        String email;
        char[] matKhau;
        java.io.Console console = System.console();
        if (console != null) {
            username = console.readLine("Tên đăng nhập Admin: ");
            hoTen = console.readLine("Họ tên nhân viên: ");
            email = console.readLine("Email: ");
            matKhau = console.readPassword("Mật khẩu (ít nhất 8 ký tự): ");
        } else if (!GraphicsEnvironment.isHeadless()) {

            JTextField tenDangNhap = new JTextField(20);
            JTextField ten = new JTextField(20);
            JTextField thu = new JTextField(20);
            JPasswordField pw = new JPasswordField(20);
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Tên đăng nhập:")); form.add(tenDangNhap);
            form.add(new JLabel("Họ tên:")); form.add(ten);
            form.add(new JLabel("Email:")); form.add(thu);
            form.add(new JLabel("Mật khẩu (≥ 8 ký tự):")); form.add(pw);
            int answer = JOptionPane.showConfirmDialog(null, form,
                "MEDIDENT - Khởi tạo Admin đầu tiên", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            username = tenDangNhap.getText();
            hoTen = ten.getText();
            email = thu.getText();
            matKhau = pw.getPassword();
            pw.setText("");
        } else {
            System.err.println("Không có Console hoặc màn hình để nhập mật khẩu.");
            return;
        }
        if (matKhau == null) return;
        try {
            username = username == null ? "" : username.trim();
            hoTen = hoTen == null ? "" : hoTen.trim();
            email = email == null ? "" : email.trim();
            if (username.isEmpty() || hoTen.isEmpty()
                    || !TienIch.EmailHopLe.kiemTra(email)
                    || matKhau.length < 8)
                throw new SQLException("Tên đăng nhập, họ tên hoặc mật khẩu chưa hợp lệ. " + TienIch.EmailHopLe.huongDan());
            tao(username, hoTen, email, matKhau);
            thongBao("Đã tạo tài khoản quản trị viên đầu tiên thành công.\n"
                + "Bạn có thể chạy UI.DangNhap.DangNhap để đăng nhập.");
        } catch (SQLException e) {
            baoLoi("Không thể tạo Admin: " + e.getMessage());
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    /** Kiểm tra SQL đã có tài khoản quản trị đang hoạt động trước khi khởi tạo tài khoản đầu tiên. */
    private static boolean daCoQuanTriVien(Connection c) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM dbo.TaiKhoan t "
            + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
            + "WHERE v.tenVaiTro=N'Quản trị viên'";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }

    /** Tạo bản ghi nghiệp vụ của lớp; kết quả được chuyển cho nơi gọi. */
    private static void tao(String username, String hoTen, String email, char[] pwd)
            throws SQLException {
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {

                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                if (daCoQuanTriVien(c))
                    throw new SQLException("Đã tồn tại Admin. Không thể khởi tạo lần nữa.");
                int maVaiTro;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản trị viên'")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Chưa có VaiTro. Hãy chạy script SQL trước.");
                        maVaiTro = rs.getInt(1);
                    }
                }
                int maTK;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,maVaiTro) "
                        + "VALUES(?,?,N'Hoạt động',?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, BCrypt.hashpw(new String(pwd), BCrypt.gensalt(11)));
                    ps.setInt(3, maVaiTro);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Không lấy được mã tài khoản.");
                        maTK = rs.getInt(1);
                    }
                }
                int maNV;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dbo.NhanVien(hoTen,email,chucVu,maTK) "
                        + "VALUES(?,?,N'Quản trị viên',?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, hoTen); ps.setString(2, email); ps.setInt(3, maTK);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Không lấy được mã nhân viên.");
                        maNV = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO dbo.QuanTriVien(maNV) VALUES(?)")) {
                    ps.setInt(1, maNV); ps.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException) throw (SQLException)e;
                throw new SQLException("Không thể tạo Admin.", e);
            } finally { c.setAutoCommit(true); }
        }
    }

    /** Hiển thị thông báo qua Swing hoặc ghi ra console trong môi trường không có màn hình. */
    private static void thongBao(String msg) {
        if (GraphicsEnvironment.isHeadless()) System.out.println(msg);
        else JOptionPane.showMessageDialog(null, msg, "MEDIDENT", JOptionPane.INFORMATION_MESSAGE);
    }
    /** Hiển thị lỗi khởi tạo hoặc ghi ra stderr khi chạy chế độ headless. */
    private static void baoLoi(String msg) {
        if (GraphicsEnvironment.isHeadless()) System.err.println(msg);
        else JOptionPane.showMessageDialog(null, msg, "MEDIDENT", JOptionPane.ERROR_MESSAGE);
    }
}
