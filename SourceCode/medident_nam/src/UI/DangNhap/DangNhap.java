
package UI.DangNhap;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import UI.Chung.FullGUI;
import UI.Chung.GiaoDienHeThong;
import UI.LeTan.LeTanGUI;
import UI.QuanLy.QuanLy_GUI;
import UI.QuanTriVien.QuanTriVienGUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Đăng nhập chung, vai trò được xác thực qua TaiKhoanDAO. */
public final class DangNhap extends JFrame {
    private final TaiKhoanDAO dao;
    private final JTextField tenDangNhap = GiaoDienHeThong.text("");
    private final JPasswordField matKhau = new JPasswordField();
    private final JLabel loiTen = new JLabel(" ");
    private final JLabel loiMatKhau = new JLabel(" ");
    private final JButton nutDangNhap = GiaoDienHeThong.nut("Đăng nhập", true);

    public DangNhap(TaiKhoanDAO dao) {
        if (dao == null) {
            throw new IllegalArgumentException("Chưa có lớp xử lý tài khoản.");
        }

        this.dao = dao;
        setTitle("MediDent - Đăng nhập");
        setSize(1040, 660);
        setMinimumSize(new Dimension(840, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel chinh = new JPanel(new GridLayout(1, 2));
        setContentPane(chinh);
        chinh.add(new AnhPhongKham());

        JPanel benPhai = new JPanel(new GridBagLayout());
        benPhai.setBackground(Color.WHITE);
        benPhai.setBorder(new EmptyBorder(25, 34, 25, 34));
        chinh.add(benPhai);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(370, 450));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Thương hiệu MediDent.
        JPanel thuongHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        thuongHieu.setOpaque(false);
        thuongHieu.setAlignmentX(Component.LEFT_ALIGNMENT);
        thuongHieu.add(new JLabel(GiaoDienHeThong.logo(54, 54)));

        JLabel tenHeThong = GiaoDienHeThong.label("MEDIDENT", 24, true);
        tenHeThong.setForeground(GiaoDienHeThong.PRIMARY);
        thuongHieu.add(tenHeThong);

        form.add(thuongHieu);
        form.add(Box.createVerticalStrut(24));
        form.add(GiaoDienHeThong.label("Đăng nhập hệ thống", 20, true));
        form.add(Box.createVerticalStrut(8));
        form.add(GiaoDienHeThong.label(
                "Sử dụng tài khoản được quản trị viên cấp.", 12, false));
        form.add(Box.createVerticalStrut(24));

        // Nhập tên đăng nhập.
        form.add(GiaoDienHeThong.label("Tên đăng nhập", 12, true));
        form.add(Box.createVerticalStrut(6));
        tenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(tenDangNhap);
        form.add(loiTen);
        form.add(Box.createVerticalStrut(12));

        // Nhập mật khẩu.
        form.add(GiaoDienHeThong.label("Mật khẩu", 12, true));
        form.add(Box.createVerticalStrut(6));

        matKhau.setFont(GiaoDienHeThong.FONT);
        matKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDienHeThong.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        matKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        form.add(matKhau);
        form.add(loiMatKhau);
        form.add(Box.createVerticalStrut(18));

        // Nút đăng nhập và quên mật khẩu.
        nutDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        nutDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(nutDangNhap);
        form.add(Box.createVerticalStrut(10));

        JButton quen = GiaoDienHeThong.nut("Quên mật khẩu?", false);
        quen.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(quen);
        benPhai.add(form);

        // Định dạng lỗi validation.
        for (JLabel loi : new JLabel[]{loiTen, loiMatKhau}) {
            loi.setFont(new Font("Arial", Font.PLAIN, 11));
            loi.setForeground(new Color(191, 59, 59));
        }

        // Sự kiện của các ô nhập.
        tenDangNhap.addActionListener(e -> matKhau.requestFocusInWindow());

        tenDangNhap.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                kiemTraTen();
            }
        });

        matKhau.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                kiemTraMatKhau();
            }
        });

        nutDangNhap.addActionListener(e -> dangNhap());
        matKhau.addActionListener(e -> dangNhap());
        quen.addActionListener(e -> quenMatKhau());

        getRootPane().setDefaultButton(nutDangNhap);
    }

    // Kiểm tra tên đăng nhập.
    private boolean kiemTraTen() {
        int doDai = tenDangNhap.getText().trim().length();
        boolean hopLe = doDai > 0 && doDai <= 60;

        loiTen.setText(hopLe ? " " :
                "Tên đăng nhập phải có từ 1 đến 60 ký tự.");
        return hopLe;
    }

    // Kiểm tra mật khẩu.
    private boolean kiemTraMatKhau() {
        boolean hopLe = matKhau.getPassword().length > 0;
        loiMatKhau.setText(hopLe ? " " : "Vui lòng nhập mật khẩu.");
        return hopLe;
    }

    // Xác thực SQL Server bằng SwingWorker.
    private void dangNhap() {
        boolean tenHopLe = kiemTraTen();
        boolean matKhauHopLe = kiemTraMatKhau();

        if (!tenHopLe || !matKhauHopLe) return;

        String ten = tenDangNhap.getText().trim();
        char[] kyTu = matKhau.getPassword();
        String biMat = new String(kyTu);

        Arrays.fill(kyTu, '\0');

        nutDangNhap.setEnabled(false);
        nutDangNhap.setText("Đang xác thực...");

        new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return dao.dangNhap(ten, biMat);
            }

            @Override
            protected void done() {
                nutDangNhap.setEnabled(true);
                nutDangNhap.setText("Đăng nhập");
                matKhau.setText("");

                try {
                    TaiKhoan user = get();

                    if (user == null) {
                        loiMatKhau.setText(
                                "Tên đăng nhập hoặc mật khẩu không đúng.");
                        return;
                    }

                    // Đổi mật khẩu bắt buộc trước khi vào hệ thống.
                    if (user.phaiDoiMatKhau
                            && !DoiMatKhauBatBuoc.thucHien(
                                    DangNhap.this, dao, user)) return;

                    // Chỉ đóng đăng nhập sau khi tạo màn hình thành công.
                    JFrame manHinh = moManHinh(user);
                    manHinh.setVisible(true);
                    dispose();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } catch (ExecutionException e) {
                    Throwable goc = e.getCause();
                    GiaoDienHeThong.loi(DangNhap.this,
                            goc instanceof Exception ? (Exception) goc : e);

                } catch (Exception e) {
                    GiaoDienHeThong.loi(DangNhap.this, e);
                }
            }
        }.execute();
    }

    // Điều hướng theo vai trò đã được SQL xác thực.
    private JFrame moManHinh(TaiKhoan user) {
        if ("full".equals(user.tenDangNhap)
                && "Quản trị viên".equals(user.tenVaiTro)) {
            return new FullGUI(user, dao);
        }

        switch (user.tenVaiTro) {
            case "Quản trị viên":
                return new QuanTriVienGUI(user, dao);

            case "Quản lý phòng khám":
                return new QuanLy_GUI();

            case "Lễ tân":
                return new LeTanGUI(user, dao);

            default:
                throw new IllegalStateException(
                        "Vai trò không được hỗ trợ: " + user.tenVaiTro);
        }
    }

    // Quên mật khẩu theo tên đăng nhập và ngày sinh.
    private void quenMatKhau() {
        JTextField ten = GiaoDienHeThong.text(
                tenDangNhap.getText().trim());

        JTextField ngaySinh = GiaoDienHeThong.text("");
        JLabel loi = new JLabel(" ");
        loi.setForeground(GiaoDienHeThong.RED);

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(ten);
        form.add(new JLabel("Ngày sinh (dd/MM/yyyy):"));
        form.add(ngaySinh);
        form.add(loi);

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);

        while (JOptionPane.showConfirmDialog(
                this, form, "Quên mật khẩu",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {

            String username = ten.getText().trim();

            if (username.isEmpty()) {
                loi.setText("Hãy nhập tên đăng nhập.");
                continue;
            }

            LocalDate ngay;

            try {
                ngay = LocalDate.parse(ngaySinh.getText().trim(), df);
            } catch (DateTimeParseException e) {
                loi.setText("Ngày sinh không hợp lệ (dd/MM/yyyy).");
                continue;
            }

            try {
                String tam = dao.tuDatLaiMatKhau(username, ngay);

                if (tam == null) {
                    loi.setText("Tài khoản hoặc ngày sinh không đúng.");
                    continue;
                }

                JTextField hien = GiaoDienHeThong.text(tam);
                hien.setEditable(false);

                JPanel noiDung = new JPanel(new GridLayout(0, 1, 5, 5));
                noiDung.add(new JLabel("Mật khẩu tạm của bạn:"));
                noiDung.add(hien);
                noiDung.add(new JLabel(
                        "Đăng nhập lại để đổi mật khẩu mới."));

                JOptionPane.showMessageDialog(
                        this, noiDung, "Đã đặt lại mật khẩu",
                        JOptionPane.INFORMATION_MESSAGE);

                hien.setText("");
                return;

            } catch (Exception e) {
                GiaoDienHeThong.loi(this, e);
                return;
            }
        }
    }

    // Hiển thị ảnh phòng khám bên trái.
    private static final class AnhPhongKham extends JPanel {
        private final BufferedImage anh;

        AnhPhongKham() {
            BufferedImage img = null;

            try {
                java.net.URL url = getClass().getResource(
                        "/Image/phong_kham_dang_nhap.jpg");
                if (url != null) img = ImageIO.read(url);
            } catch (IOException ignored) {}

            anh = img;
            setBackground(GiaoDienHeThong.PRIMARY);
            setLayout(new BorderLayout());

            JLabel tieuDe = new JLabel(
                    "MEDIDENT - Quản lý nha khoa",
                    SwingConstants.CENTER);

            tieuDe.setFont(new Font("Arial", Font.BOLD, 19));
            tieuDe.setForeground(Color.WHITE);
            tieuDe.setBorder(new EmptyBorder(10, 10, 26, 10));
            add(tieuDe, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (anh == null) return;

            Graphics2D g = (Graphics2D) graphics.create();

            double tiLe = Math.max(
                    (double) getWidth() / anh.getWidth(),
                    (double) getHeight() / anh.getHeight());

            int w = (int) Math.ceil(anh.getWidth() * tiLe);
            int h = (int) Math.ceil(anh.getHeight() * tiLe);

            g.drawImage(anh, (getWidth() - w) / 2,
                    (getHeight() - h) / 2, w, h, null);

            g.setPaint(new GradientPaint(
                    0, getHeight() / 2f,
                    new Color(0, 0, 0, 0),
                    0, getHeight(),
                    new Color(0, 0, 0, 160)));

            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    // Class chạy chính của ứng dụng.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new DangNhap(new TaiKhoanDAO()).setVisible(true));
    }
}
