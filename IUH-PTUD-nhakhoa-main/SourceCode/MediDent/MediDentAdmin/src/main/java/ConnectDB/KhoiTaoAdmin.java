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

/**
 * Tao quan tri vien DAU TIEN khi CSDL chua co admin.
 * Chay duoc trong Eclipse (Swing form) hoac terminal (Console).
 * Khong hard-code tai khoan va mat khau mac dinh.
 */
public class KhoiTaoAdmin {
    public static void main(String[] args) {
        // Kiem tra truoc: tranh tao them admin qua cong cu khoi tao.
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
            matKhau = console.readPassword("Mật khẩu (ít nhất 10 ký tự, có chữ hoa và số): ");
        } else if (!GraphicsEnvironment.isHeadless()) {
            // Eclipse Console khong ho tro System.console(); dung form thay the.
            JTextField tenDangNhap = new JTextField(20);
            JTextField ten = new JTextField(20);
            JTextField thu = new JTextField(20);
            JPasswordField pw = new JPasswordField(20);
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.add(new JLabel("Tên đăng nhập:")); form.add(tenDangNhap);
            form.add(new JLabel("Họ tên:")); form.add(ten);
            form.add(new JLabel("Email:")); form.add(thu);
            form.add(new JLabel("Mật khẩu (≥ 10, có hoa và số):")); form.add(pw);
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
                    || !TienIch.MatKhauHopLe.kiemTra(new String(matKhau)))
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

    private static boolean daCoQuanTriVien(Connection c) throws SQLException {
        String sql = "SELECT TOP 1 1 FROM dbo.TaiKhoan t "
            + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
            + "WHERE v.tenVaiTro=N'Quản trị viên'";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }

    private static void tao(String username, String hoTen, String email, char[] pwd)
            throws SQLException {
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                // Serializable + kiểm tra trong giao dịch để tránh 2 lần khởi tạo song song.
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

    private static void thongBao(String msg) {
        if (GraphicsEnvironment.isHeadless()) System.out.println(msg);
        else JOptionPane.showMessageDialog(null, msg, "MEDIDENT", JOptionPane.INFORMATION_MESSAGE);
    }
    private static void baoLoi(String msg) {
        if (GraphicsEnvironment.isHeadless()) System.err.println(msg);
        else JOptionPane.showMessageDialog(null, msg, "MEDIDENT", JOptionPane.ERROR_MESSAGE);
    }
}
