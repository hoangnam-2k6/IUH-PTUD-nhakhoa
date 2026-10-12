package UI.DangNhap;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import UI.Chung.KhungChinh;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Màn hình đăng nhập chung của MediDent.
 *
 * Kiểm tra tên đăng nhập và BCrypt bằng KhoDuLieu (SQL Server).
 * Quyền/menu được lấy từ CSDL sau khi xác thực, không chọn vai trò thủ công.
 */
public class DangNhap extends JFrame {

    public DangNhap(KhoDuLieu db) {
        setTitle("MEDIDENT | Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 610);
        setMinimumSize(new Dimension(760, 510));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        setContentPane(root);

        // 1. Banner bên trái: nhận diện thương hiệu và lời giới thiệu.
        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(new Color(0x075985));
        JLabel brand = new JLabel("<html><div style='color:white;font-size:26px'>"
                + "<b>MEDIDENT</b></div><br>"
                + "<div style='color:white;font-size:18px'>Vận hành hiệu quả."
                + "<br>Chăm sóc tận tâm.</div><br>"
                + "<span style='color:white'>Hệ thống quản lý nha khoa</span></html>");
        banner.add(brand);
        root.add(banner);

        // 2. Form bên phải và logo lấy từ resources/Image.
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(35, 48, 35, 48));
        root.add(form);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel(GiaoDien.logo(82, 72));
        fields.add(logo);
        fields.add(Box.createVerticalStrut(12));
        fields.add(GiaoDien.label("Đăng nhập vào hệ thống", 24, true));
        fields.add(Box.createVerticalStrut(10));
        fields.add(GiaoDien.label("Sử dụng tài khoản được phòng khám cấp để tiếp tục.",
                12, false));
        fields.add(Box.createVerticalStrut(42));

        fields.add(GiaoDien.label("Tên đăng nhập", 12, true));
        fields.add(Box.createVerticalStrut(7));
        JTextField username = GiaoDien.text("");
        fields.add(username);
        fields.add(Box.createVerticalStrut(19));

        fields.add(GiaoDien.label("Mật khẩu", 12, true));
        fields.add(Box.createVerticalStrut(7));
        JPasswordField password = new JPasswordField();
        password.setBorder(new CompoundBorder(new LineBorder(GiaoDien.BORDER),
                new EmptyBorder(9, 10, 9, 10)));
        fields.add(password);
        fields.add(Box.createVerticalStrut(25));

        JButton login = GiaoDien.nutIcon("Đăng nhập", true, BieuTuong.Loai.DANG_NHAP);
        fields.add(login);
        form.add(fields);

        // 3. Chỉ gọi DAO để xác thực, không ghi mật khẩu thô vào log.
        Runnable loginAction = () -> {
            char[] secret = password.getPassword();
            String typedPassword = new String(secret);
            java.util.Arrays.fill(secret, '\0');
            if (username.getText().trim().isEmpty() || typedPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Nhập tên đăng nhập và mật khẩu.");
                return;
            }
            try {
                TaiKhoan account = db.dangNhap(username.getText().trim(), typedPassword);
                if (account == null) {
                    JOptionPane.showMessageDialog(this,
                            "Sai thông tin hoặc tài khoản bị khóa.");
                    return;
                }
                // Nếu tài khoản vừa được cấp hoặc reset, chỉ mở khung chính SAU khi đổi mật khẩu.
                if (account.phaiDoiMatKhau && !DoiMatKhauBatBuoc.thucHien(this, db, account)) {
                    return;
                }
                dispose();
                new KhungChinh(account, db).setVisible(true);
            } catch (Exception exception) {
                GiaoDien.loi(this, exception);
            } finally {
                password.setText("");
            }
        };
        login.addActionListener(event -> loginAction.run());
        password.addActionListener(event -> loginAction.run());
    }

    /** Entry point chạy bằng Run As -> Java Application trong Eclipse. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap(new KhoDuLieu()).setVisible(true));
    }
}
