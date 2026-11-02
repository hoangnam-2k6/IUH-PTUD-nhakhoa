package UI.DangNhap;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import UI.Chung.KhungChinh;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

/**
 * Màn hình đăng nhập chung: ảnh phòng khám bên trái, biểu mẫu bên phải.
 * Quyền được xác định sau xác thực từ SQL, không chọn vai trò trên giao diện.
 */
public final class DangNhap extends JFrame {

    /** Vẽ ảnh đúng tỷ lệ trên khung; không kéo méo nội thất/phòng khám. */
    private static final class AnhPhongKham extends JPanel {
        private final BufferedImage anh;

        AnhPhongKham() {
            BufferedImage taiNguyen = null;
            try {
                java.net.URL duongDan = getClass().getResource("/Image/phong_kham_dang_nhap.jpg");
                if (duongDan != null) taiNguyen = ImageIO.read(duongDan);
            } catch (IOException ignored) { /* Không dừng ứng dụng nếu thiếu ảnh. */ }
            anh = taiNguyen;
            setBackground(GiaoDien.PRIMARY);
            setLayout(new BorderLayout());

            JPanel khoiChu = new JPanel();
            khoiChu.setOpaque(false);
            khoiChu.setBorder(new EmptyBorder(18, 34, 34, 32));
            khoiChu.setLayout(new BoxLayout(khoiChu, BoxLayout.Y_AXIS));
            JLabel tieuDe = GiaoDien.label("Chăm sóc nụ cười,", 27, true);
            tieuDe.setForeground(Color.WHITE);
            JLabel phuDe = GiaoDien.label("kết nối sự tin tưởng.", 27, true);
            phuDe.setForeground(Color.WHITE);
            JLabel moTa = GiaoDien.label("MEDIDENT  |  Quản lý phòng khám nha khoa", 12, false);
            moTa.setForeground(new Color(0xE0F2FE));
            khoiChu.add(tieuDe);
            khoiChu.add(Box.createVerticalStrut(4));
            khoiChu.add(phuDe);
            khoiChu.add(Box.createVerticalStrut(16));
            khoiChu.add(moTa);
            add(khoiChu, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            if (anh != null) {
                double scale = Math.max((double) getWidth() / anh.getWidth(), (double) getHeight() / anh.getHeight());
                int rong = (int) Math.ceil(anh.getWidth() * scale);
                int cao = (int) Math.ceil(anh.getHeight() * scale);
                g.drawImage(anh, (getWidth() - rong) / 2, (getHeight() - cao) / 2, rong, cao, null);
            }
            GradientPaint phuAnh = new GradientPaint(0, getHeight() / 2f,
                    new Color(15, 23, 42, 0), 0, getHeight(), new Color(15, 23, 42, 209));
            g.setPaint(phuAnh);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    public DangNhap(KhoDuLieu db) {
        setTitle("MEDIDENT | Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1030, 660);
        setMinimumSize(new Dimension(850, 570));
        setLocationRelativeTo(null);

        JPanel noiDung = new JPanel(new GridLayout(1, 2));
        setContentPane(noiDung);
        noiDung.add(new AnhPhongKham()); // Bên trái: ảnh phòng khám người dùng cung cấp.

        JPanel benPhai = new JPanel(new GridBagLayout());
        benPhai.setBackground(GiaoDien.WHITE);
        benPhai.setBorder(new EmptyBorder(32, 45, 32, 45));
        noiDung.add(benPhai); // Bên phải: form đăng nhập thống nhất 3 vai trò.

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(370, 490));

        JPanel nhanHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        nhanHieu.setOpaque(false);
        nhanHieu.setAlignmentX(LEFT_ALIGNMENT);
        nhanHieu.add(new JLabel(GiaoDien.logo(67, 67)));
        JPanel vanBan = new JPanel();
        vanBan.setOpaque(false);
        vanBan.setLayout(new BoxLayout(vanBan, BoxLayout.Y_AXIS));
        JLabel logoChu = GiaoDien.label("MEDIDENT", 26, true);
        logoChu.setForeground(GiaoDien.PRIMARY);
        vanBan.add(logoChu);
        vanBan.add(GiaoDien.label("HỆ THỐNG QUẢN LÝ NHA KHOA", 10, false));
        nhanHieu.add(vanBan);
        form.add(nhanHieu);
        form.add(Box.createVerticalStrut(31));
        form.add(GiaoDien.label("Đăng nhập hệ thống", 23, true));
        form.add(Box.createVerticalStrut(8));
        form.add(GiaoDien.label("Nhập tài khoản được quản trị viên cấp để tiếp tục.", 12, false));
        form.add(Box.createVerticalStrut(31));

        form.add(GiaoDien.label("Tên đăng nhập", 12, true));
        form.add(Box.createVerticalStrut(8));
        JTextField taiKhoan = GiaoDien.text("");
        taiKhoan.setToolTipText("Tên đăng nhập do hệ thống cấp, ví dụ NV000001");
        taiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(taiKhoan);
        form.add(Box.createVerticalStrut(21));

        JPanel hangMatKhau = new JPanel(new BorderLayout(8, 0));
        hangMatKhau.setOpaque(false);
        hangMatKhau.setAlignmentX(LEFT_ALIGNMENT);
        hangMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        hangMatKhau.add(GiaoDien.label("Mật khẩu", 12, true), BorderLayout.WEST);
        JButton quen = new JButton("Quên mật khẩu?");
        quen.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        quen.setForeground(GiaoDien.PRIMARY);
        quen.setBorder(new EmptyBorder(2, 0, 2, 0));
        quen.setContentAreaFilled(false);
        quen.setFocusPainted(false);
        quen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quen.addActionListener(event -> moQuenMatKhau(db, taiKhoan));
        hangMatKhau.add(quen, BorderLayout.EAST);
        form.add(hangMatKhau);
        form.add(Box.createVerticalStrut(8));

        JPasswordField matKhau = new JPasswordField();
        matKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        matKhau.setBorder(new CompoundBorder(new LineBorder(GiaoDien.BORDER),
                new EmptyBorder(10, 11, 10, 11)));
        form.add(matKhau);
        form.add(Box.createVerticalStrut(26));
        JButton dangNhap = GiaoDien.nutIcon("Đăng nhập", true, BieuTuong.Loai.DANG_NHAP);
        dangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        dangNhap.setAlignmentX(LEFT_ALIGNMENT);
        form.add(dangNhap);
        form.add(Box.createVerticalStrut(26));
        JLabel ghiChu = GiaoDien.label("Tài khoản được phân quyền theo dữ liệu hệ thống.", 11, false);
        form.add(ghiChu);
        benPhai.add(form);

        Runnable thucHienDangNhap = () -> {
            String ten = taiKhoan.getText().trim();
            char[] kyTu = matKhau.getPassword();
            if (ten.isEmpty() || kyTu.length == 0 || ten.length() > 60) {
                JOptionPane.showMessageDialog(this, "Hãy nhập tên đăng nhập và mật khẩu hợp lệ.");
                Arrays.fill(kyTu, '\0');
                return;
            }
            String biMat = new String(kyTu); // BCrypt DAO hiện sử dụng String để so khớp.
            Arrays.fill(kyTu, '\0'); // Không giữ mảng ký tự mật khẩu trong bộ nhớ UI.
            dangNhap.setEnabled(false);
            dangNhap.setText("Đang xác thực...");
            new SwingWorker<TaiKhoan, Void>() {
                @Override
                protected TaiKhoan doInBackground() throws Exception {
                    return db.dangNhap(ten, biMat); // SQL Server xác thực tài khoản + vai trò.
                }

                @Override
                protected void done() {
                    dangNhap.setEnabled(true);
                    dangNhap.setText("Đăng nhập");
                    matKhau.setText("");
                    try {
                        TaiKhoan nguoiDung = get();
                        if (nguoiDung == null) {
                            JOptionPane.showMessageDialog(DangNhap.this,
                                    "Thông tin không đúng hoặc tài khoản không được phép truy cập.",
                                    "Đăng nhập thất bại", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (nguoiDung.phaiDoiMatKhau
                                && !DoiMatKhauBatBuoc.thucHien(DangNhap.this, db, nguoiDung)) return;
                        dispose();
                        new KhungChinh(nguoiDung, db).setVisible(true); // Điều hướng đúng vai trò.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        GiaoDien.loi(DangNhap.this, ex.getCause() instanceof Exception
                                ? (Exception) ex.getCause() : ex);
                    }
                }
            }.execute();
        };
        dangNhap.addActionListener(event -> thucHienDangNhap.run());
        matKhau.addActionListener(event -> thucHienDangNhap.run());
        getRootPane().setDefaultButton(dangNhap);
    }

    /**
     * Hiển thị hộp thoại yêu cầu đặt lại mật khẩu.
     *
     * Không đặt lại mật khẩu tại màn hình chưa đăng nhập. Admin kiểm tra yêu cầu
     * ở mục Hỗ trợ, xác minh danh tính nhân viên và dùng chức năng Reset trong
     * Quản lý tài khoản. Nhờ vậy người lạ không thể reset chỉ bằng ngày sinh.
     */
    private void moQuenMatKhau(KhoDuLieu db, JTextField username) {
        JTextField accountField = GiaoDien.text(username.getText().trim());
        accountField.setPreferredSize(new Dimension(320, 38));

        JPanel dialog = new JPanel();
        dialog.setLayout(new BoxLayout(dialog, BoxLayout.Y_AXIS));
        dialog.setBorder(new EmptyBorder(8, 8, 8, 8));
        dialog.add(GiaoDien.label("Tên đăng nhập MediDent", 12, true));
        dialog.add(Box.createVerticalStrut(8));
        dialog.add(accountField);
        dialog.add(Box.createVerticalStrut(12));
        dialog.add(GiaoDien.label("Yêu cầu sẽ chuyển đến Quản trị viên để xác minh.", 11, false));
        dialog.add(Box.createVerticalStrut(4));
        dialog.add(GiaoDien.label("Bạn không cần nhập ngày sinh hoặc mật khẩu cũ.", 11, false));

        int choice = JOptionPane.showConfirmDialog(
                this, dialog, "Yêu cầu đặt lại mật khẩu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String name = accountField.getText().trim();
        if (name.isEmpty() || name.length() > 80) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập hợp lệ.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Không khóa giao diện khi chờ kết nối SQL Server.
        // Cố tình không tiết lộ tài khoản có tồn tại hoặc đã gửi trùng hay không.
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                db.guiYeuCauQuenMatKhau(name);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(DangNhap.this,
                            "Nếu tên đăng nhập hợp lệ, yêu cầu đã được chuyển đến Admin.\n" +
                            "Vui lòng liên hệ quản trị viên để xác minh và nhận hướng dẫn.",
                            "Đã tiếp nhận yêu cầu", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DangNhap.this,
                            "Không gửi được yêu cầu. Hãy kiểm tra kết nối SQL Server và\n" +
                            "bảng YeuCauHoTro (script SQLData/04).\n" +
                            "Bạn cũng có thể liên hệ trực tiếp quản trị viên.",
                            "Yêu cầu chưa được gửi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Entry point chạy bằng Run As -> Java Application trong Eclipse. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap(new KhoDuLieu()).setVisible(true));
    }
}
