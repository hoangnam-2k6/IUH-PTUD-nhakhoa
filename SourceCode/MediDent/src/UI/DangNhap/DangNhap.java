package UI.DangNhap;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.XacThucService;
import UI.Chung.GiaoDien;
import UI.QuanLy.KhungChinh;

/**
 * Đăng nhập/đăng ký cho vai trò Quản lý phòng khám.
 * Không hiển thị nút Kiểm tra SQL; nhiệm vụ kiểm tra được giữ trong ConnectDB.KetNoi.
 */
public final class DangNhap extends JFrame {

    private final XacThucService auth = new XacThucService();

    private final JTextField loginUser = new JTextField(24);
    private final JPasswordField loginPassword = new JPasswordField(24);
    private final JTextField registerUser = new JTextField(24);
    private final JTextField name = new JTextField(24);
    private final JTextField phone = new JTextField(24);
    private final JTextField address = new JTextField(24);
    private final JTextField birth = new JTextField(24);
    private final JTextField invite = new JTextField(24);
    private final JComboBox<String> gender = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
    private final JPasswordField registerPassword = new JPasswordField(24);
    private final JPasswordField confirmPassword = new JPasswordField(24);

    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JLabel status = GiaoDien.label("SQL Server • Database MediDent", 12, false);

    public DangNhap() {
        super("MEDIDENT – Đăng nhập / Đăng ký Quản lý");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 740);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(GiaoDien.BG);

        JPanel art = clinicPicture();
        art.setPreferredSize(new Dimension(410, 0));
        art.setBorder(new EmptyBorder(50, 30, 30, 30));

        JLabel logo = new JLabel(GiaoDien.logo(120));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        art.add(logo, BorderLayout.NORTH);

        JLabel slogan = new JLabel(
                "<html><center>MEDIDENT<br><br>Quản lý phòng khám"
                + "<br><small>Một hệ thống · Một cơ sở dữ liệu</small></center></html>");
        slogan.setForeground(Color.WHITE);
        slogan.setHorizontalAlignment(SwingConstants.CENTER);
        slogan.setFont(new Font("Segoe UI", Font.BOLD, 26));
        art.add(slogan, BorderLayout.CENTER);
        add(art, BorderLayout.WEST);

        JPanel formRoot = new JPanel(new BorderLayout());
        formRoot.setBackground(GiaoDien.WHITE);
        formRoot.setBorder(new EmptyBorder(25, 36, 20, 36));
        formRoot.add(GiaoDien.label("Chào mừng đến MediDent", 25, true), BorderLayout.NORTH);

        center.setBackground(GiaoDien.WHITE);
        center.add(loginPanel(), "login");
        JScrollPane registerScroll = new JScrollPane(registerPanel());
        registerScroll.setBorder(null);
        registerScroll.getVerticalScrollBar().setUnitIncrement(18);
        registerScroll.getViewport().setBackground(Color.WHITE);
        center.add(registerScroll, "register");

        formRoot.add(center, BorderLayout.CENTER);
        formRoot.add(status, BorderLayout.SOUTH);
        add(formRoot, BorderLayout.CENTER);
    }

    private JPanel clinicPicture() {
        return new JPanel(new BorderLayout()) {
            private java.awt.Image image;
            {
                java.net.URL url = DangNhap.class.getResource("/Image/phong_kham.jpg");
                if (url != null) {
                    image = new ImageIcon(url).getImage();
                }
                setBackground(GiaoDien.BLUE_DARK);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (image != null) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(2, 90, 150, 180));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.dispose();
                }
            }
        };
    }

    private JPanel loginPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = verticalFields();
        field(form, "Tên đăng nhập", loginUser);
        field(form, "Mật khẩu", loginPassword);

        JButton login = GiaoDien.button("Đăng nhập Quản lý", true);
        login.addActionListener(event -> performLogin());
        addButton(form, login);
        form.add(Box.createVerticalStrut(15));

        JButton register = GiaoDien.button("Đăng ký tài khoản Quản lý", false);
        register.addActionListener(event -> {
            registerPassword.setText("");
            cards.show(center, "register");
        });
        addButton(form, register);

        outer.add(form);
        return outer;
    }

    private JPanel registerPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        JPanel form = verticalFields();

        field(form, "Tên đăng nhập (ít nhất 4 ký tự)", registerUser);
        field(form, "Họ tên quản lý", name);
        field(form, "Số điện thoại", phone);
        field(form, "Địa chỉ", address);
        field(form, "Ngày sinh (yyyy-MM-dd)", birth);
        field(form, "Giới tính", gender);
        field(form, "Mật khẩu mạnh", registerPassword);
        field(form, "Nhập lại mật khẩu", confirmPassword);
        field(form, "Mã mời (theo cấu hình hoặc đăng ký quản lý đầu tiên)", invite);

        JButton save = GiaoDien.button("Tạo tài khoản trong SQL", true);
        save.addActionListener(event -> performRegister());
        addButton(form, save);
        form.add(Box.createVerticalStrut(12));

        JButton back = GiaoDien.button("Trở về đăng nhập", false);
        back.addActionListener(event -> cards.show(center, "login"));
        addButton(form, back);

        outer.add(form);
        return outer;
    }

    private JPanel verticalFields() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        return form;
    }

    private void field(JPanel form, String title, JComponent input) {
        JLabel label = GiaoDien.label(title, 12, true);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        GiaoDien.styleInput(input);
        input.setMaximumSize(new Dimension(335, 42));
        input.setPreferredSize(new Dimension(335, 42));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(label);
        form.add(Box.createVerticalStrut(5));
        form.add(input);
        form.add(Box.createVerticalStrut(11));
    }

    private void addButton(JPanel form, JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(335, 44));
        button.setPreferredSize(new Dimension(335, 44));
        form.add(button);
    }

    private void runDatabaseAction(Runnable task) {
        status.setText("Đang xử lý SQL Server...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    status.setText("Kết nối SQL thành công");
                } catch (Exception exception) {
                    status.setText("Không thành công – kiểm tra tài khoản hoặc cấu hình SQL");
                    GiaoDien.error(DangNhap.this, exception);
                }
            }
        }.execute();
    }

    private void performLogin() {
        String username = loginUser.getText().trim();
        String password = new String(loginPassword.getPassword());
        if (username.isBlank() || password.isEmpty()) {
            GiaoDien.ok(this, "Nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        runDatabaseAction(() -> {
            PhanQuyen.PhienDangNhap session = auth.login(username, password);
            SwingUtilities.invokeLater(() -> {
                dispose();
                new KhungChinh(session).setVisible(true);
            });
        });
    }

    private void performRegister() {
        String username = registerUser.getText().trim();
        String fullName = name.getText().trim();
        String phoneNumber = phone.getText().trim();
        String homeAddress = address.getText().trim();
        String dob = birth.getText().trim();
        String sex = String.valueOf(gender.getSelectedItem());
        String password = new String(registerPassword.getPassword());
        String confirmation = new String(confirmPassword.getPassword());
        String inviteCode = invite.getText().trim();

        runDatabaseAction(() -> {
            auth.register(
                    username, password, confirmation, fullName,
                    phoneNumber, homeAddress, dob, sex, inviteCode);
            SwingUtilities.invokeLater(() -> {
                registerPassword.setText("");
                confirmPassword.setText("");
                loginUser.setText(username);
                cards.show(center, "login");
                GiaoDien.ok(this, "Đăng ký thành công. Đăng nhập bằng tài khoản mới.");
            });
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GiaoDien.setup();
            new DangNhap().setVisible(true);
        });
    }
}
