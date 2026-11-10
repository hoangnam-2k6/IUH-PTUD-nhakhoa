package UI.DangNhap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import DAO.TaiKhoanDAO;

import javax.swing.Box;
import javax.swing.BoxLayout;


import Entity.TaiKhoan;
import UI.QuanTriVien.QuanTriVienGUI;
import UI.QuanLy.QuanLyGUI;
import UI.LeTan.LeTanGUI;
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
            setBackground(GiaoDienDangNhap.PRIMARY);
            setLayout(new BorderLayout());

            JPanel khoiChu = new JPanel();
            khoiChu.setOpaque(false);
            khoiChu.setBorder(new EmptyBorder(18, 34, 34, 32));
            khoiChu.setLayout(new BoxLayout(khoiChu, BoxLayout.Y_AXIS));
            JLabel tieuDe = GiaoDienDangNhap.label("Chăm sóc nụ cười,", 27, true);
            tieuDe.setForeground(Color.WHITE);
            JLabel phuDe = GiaoDienDangNhap.label("kết nối sự tin tưởng.", 27, true);
            phuDe.setForeground(Color.WHITE);
            JLabel moTa = GiaoDienDangNhap.label("MEDIDENT  |  Quản lý phòng khám nha khoa", 12, false);
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

    public DangNhap(TaiKhoanDAO db) {
        setTitle("MEDIDENT | Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1030, 660);
        setMinimumSize(new Dimension(850, 570));
        setLocationRelativeTo(null);

        JPanel noiDung = new JPanel(new GridLayout(1, 2));
        setContentPane(noiDung);
        noiDung.add(new AnhPhongKham()); // Bên trái: ảnh phòng khám người dùng cung cấp.

        JPanel benPhai = new JPanel(new GridBagLayout());
        benPhai.setBackground(GiaoDienDangNhap.WHITE);
        benPhai.setBorder(new EmptyBorder(32, 45, 32, 45));
        noiDung.add(benPhai); // Bên phải: form đăng nhập thống nhất 3 vai trò.

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(370, 490));

        JPanel nhanHieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        nhanHieu.setOpaque(false);
        nhanHieu.setAlignmentX(LEFT_ALIGNMENT);
        nhanHieu.add(new JLabel(GiaoDienDangNhap.logo(67, 67)));
        JPanel vanBan = new JPanel();
        vanBan.setOpaque(false);
        vanBan.setLayout(new BoxLayout(vanBan, BoxLayout.Y_AXIS));
        JLabel logoChu = GiaoDienDangNhap.label("MEDIDENT", 26, true);
        logoChu.setForeground(GiaoDienDangNhap.PRIMARY);
        vanBan.add(logoChu);
        vanBan.add(GiaoDienDangNhap.label("HỆ THỐNG QUẢN LÝ NHA KHOA", 10, false));
        nhanHieu.add(vanBan);
        form.add(nhanHieu);
        form.add(Box.createVerticalStrut(31));
        form.add(GiaoDienDangNhap.label("Đăng nhập hệ thống", 23, true));
        form.add(Box.createVerticalStrut(8));
        form.add(GiaoDienDangNhap.label("Nhập tài khoản được quản trị viên cấp để tiếp tục.", 12, false));
        form.add(Box.createVerticalStrut(31));

        form.add(GiaoDienDangNhap.label("Tên đăng nhập", 12, true));
        form.add(Box.createVerticalStrut(8));
        JTextField taiKhoan = GiaoDienDangNhap.text("");
        taiKhoan.setToolTipText("Tên đăng nhập do hệ thống cấp, ví dụ nv1");
        taiKhoan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        form.add(taiKhoan);
        JLabel loiTaiKhoan = new JLabel("");
        loiTaiKhoan.setForeground(new Color(0xDC2626));
        loiTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        form.add(loiTaiKhoan);
        form.add(Box.createVerticalStrut(9));

        JPanel hangMatKhau = new JPanel(new BorderLayout(8, 0));
        hangMatKhau.setOpaque(false);
        hangMatKhau.setAlignmentX(LEFT_ALIGNMENT);
        hangMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        hangMatKhau.add(GiaoDienDangNhap.label("Mật khẩu", 12, true), BorderLayout.WEST);

        form.add(hangMatKhau);
        form.add(Box.createVerticalStrut(8));

        JPasswordField matKhau = new JPasswordField();
        matKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        matKhau.setBorder(new CompoundBorder(new LineBorder(GiaoDienDangNhap.BORDER),
                new EmptyBorder(10, 11, 10, 11)));
        form.add(matKhau);
        JLabel loiMatKhau = new JLabel("");
        loiMatKhau.setForeground(new Color(0xDC2626));
        loiMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        form.add(loiMatKhau);
        form.add(Box.createVerticalStrut(12));
        Runnable kiemTraTaiKhoan = () -> loiTaiKhoan.setText(
                taiKhoan.getText().trim().isEmpty() || taiKhoan.getText().trim().length() > 60
                ? "Tên đăng nhập phải có từ 1 đến 60 ký tự." : "");
        Runnable kiemTraMatKhau = () -> loiMatKhau.setText(
                matKhau.getPassword().length == 0 ? "Vui lòng nhập mật khẩu." : "");
        taiKhoan.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { kiemTraTaiKhoan.run(); }
        });
        matKhau.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { kiemTraMatKhau.run(); }
        });
        taiKhoan.addActionListener(e -> matKhau.requestFocusInWindow());
        form.setFocusable(true);
        java.awt.event.MouseAdapter roiO = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                form.requestFocusInWindow();
            }
        };
        form.addMouseListener(roiO);
        for (Component component : form.getComponents()) {
            if (component instanceof JLabel) component.addMouseListener(roiO);
        }
        for (Component component : hangMatKhau.getComponents()) {
            if (component instanceof JLabel) component.addMouseListener(roiO);
        }
        JButton dangNhap = GiaoDienDangNhap.nut("Đăng nhập", true);
        dangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        dangNhap.setAlignmentX(LEFT_ALIGNMENT);
        form.add(dangNhap);
        form.add(Box.createVerticalStrut(10));
        JButton quenMatKhau = new JButton("Quên mật khẩu?");
        quenMatKhau.setAlignmentX(LEFT_ALIGNMENT);
        quenMatKhau.setForeground(GiaoDienDangNhap.PRIMARY);
        quenMatKhau.setBackground(Color.WHITE);
        quenMatKhau.setBorder(new EmptyBorder(6, 0, 6, 0));
        quenMatKhau.setFocusPainted(false);
        form.add(quenMatKhau);
        form.add(Box.createVerticalStrut(16));
        quenMatKhau.addActionListener(e -> {
            JTextField tenCanLayLai = new JTextField(taiKhoan.getText().trim(), 23);
            JTextField ngaySinh = new JTextField(15);
            JLabel loiQuen = new JLabel(" ");
            loiQuen.setForeground(new Color(0xDC2626));
            JPanel khungQuen = new JPanel(new GridLayout(0, 1, 4, 5));
            khungQuen.add(new JLabel("Tên đăng nhập"));
            khungQuen.add(tenCanLayLai);
            khungQuen.add(new JLabel("Ngày sinh (dd/MM/yyyy)"));
            khungQuen.add(ngaySinh);
            khungQuen.add(loiQuen);

            Runnable kiemTra = () -> {
                if (tenCanLayLai.getText().trim().isEmpty()) {
                    loiQuen.setText("Hãy nhập tên đăng nhập.");
                    return;
                }
                try {
                    java.time.LocalDate.parse(ngaySinh.getText().trim(),
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(java.time.format.ResolverStyle.STRICT));
                    loiQuen.setText(" ");
                } catch (java.time.format.DateTimeParseException ex) {
                    loiQuen.setText("Ngày sinh không hợp lệ (dd/MM/yyyy).");
                }
            };
            tenCanLayLai.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent ev) { kiemTra.run(); }
            });
            ngaySinh.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent ev) { kiemTra.run(); }
            });

            while (JOptionPane.showConfirmDialog(this, khungQuen, "Quên mật khẩu",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                    == JOptionPane.OK_OPTION) {
                kiemTra.run();
                if (!loiQuen.getText().trim().isEmpty()) continue;
                try {
                    java.time.LocalDate ngay = java.time.LocalDate.parse(ngaySinh.getText().trim(),
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
                        .withResolverStyle(java.time.format.ResolverStyle.STRICT));
                    String matKhauTam = db.tuDatLaiMatKhau(tenCanLayLai.getText().trim(), ngay);
                    if (matKhauTam == null) {
                        loiQuen.setText("Tài khoản hoặc ngày sinh không đúng.");
                        continue;
                    }
                    JTextField hienMatKhau = new JTextField(matKhauTam, 23);
                    hienMatKhau.setEditable(false);
                    JPanel thongBao = new JPanel(new GridLayout(0, 1, 5, 5));
                    thongBao.add(new JLabel("Mật khẩu tạm của bạn:"));
                    thongBao.add(hienMatKhau);
                    thongBao.add(new JLabel("Đăng nhập lại và đổi mật khẩu mới."));
                    JOptionPane.showMessageDialog(this, thongBao, "Đã đặt lại mật khẩu",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;
                } catch (SQLException ex) {
                    GiaoDienDangNhap.loi(this, ex);
                    break;
                }
            }
        });
        benPhai.add(form);

        Runnable thucHienDangNhap = () -> {
            String ten = taiKhoan.getText().trim();
            char[] kyTu = matKhau.getPassword();
            kiemTraTaiKhoan.run();
            kiemTraMatKhau.run();
            if (!loiTaiKhoan.getText().isEmpty() || !loiMatKhau.getText().isEmpty()) {
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
                            loiMatKhau.setText("Tên đăng nhập hoặc mật khẩu không đúng.");
                            return;
                        }
                        if (nguoiDung.phaiDoiMatKhau
                                && !DoiMatKhauBatBuoc.thucHien(DangNhap.this, db, nguoiDung)) return;
                        dispose();
                        if ("full".equals(nguoiDung.tenDangNhap) &&
                                "Quản trị viên".equals(nguoiDung.tenVaiTro)) {
                            new UI.Chung.FullGUI(nguoiDung,db).setVisible(true);
                        } else switch (nguoiDung.tenVaiTro) {
                            case "Quản trị viên" -> new QuanTriVienGUI(nguoiDung, db).setVisible(true);
                            case "Quản lý phòng khám" -> new QuanLyGUI(nguoiDung, db).setVisible(true);
                            case "Lễ tân" -> new LeTanGUI(nguoiDung, db).setVisible(true);
                            default -> throw new IllegalStateException("Vai trò chưa được hỗ trợ: " + nguoiDung.tenVaiTro);
                        } // Chỉ điều hướng sau khi xác thực thành công.
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException ex) {
                        GiaoDienDangNhap.loi(DangNhap.this, ex.getCause() instanceof Exception
                                ? (Exception) ex.getCause() : ex);
                    }
                }
            }.execute();
        };
        dangNhap.addActionListener(event -> thucHienDangNhap.run());
        matKhau.addActionListener(event -> thucHienDangNhap.run());
        getRootPane().setDefaultButton(dangNhap);
    }

    /** Entry point chạy bằng Run As -> Java Application trong Eclipse. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap(new TaiKhoanDAO()).setVisible(true));
    }
}


/**
 * Giao diện dùng chung giữa các vai trò.
 *
 * Chỉ chứa những hàm tạo giao diện / bảng màu; các màn hình riêng của quản trị
 * viên nằm trong UI.QuanTriVien và không chỉnh sửa ManHinhTrangChu cũ.
 */
final class GiaoDienDangNhap {

    // 1. Màu sắc chuẩn của MediDent.
    public static final Color PRIMARY = new Color(0x0284C7);
    public static final Color HOVER = new Color(0x0369A1);
    public static final Color BG = new Color(0xF8FAFC);
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color WARN = new Color(0xF59E0B);
    public static final Color RED = new Color(0xEF4444);

    private GiaoDienDangNhap() {
        // Không tạo đối tượng từ lớp tiện ích.
    }

    /** 2. Tạo nhãn có font và màu nhất quán. */
    public static JLabel label(String text, int fontSize, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        result.setForeground(bold ? TEXT : MUTED);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        return result;
    }

    /** 3. Tạo nút theo hai kiểu chính/phụ của hệ thống. */
    public static JButton nut(String title, boolean primary) {
        // Nút trắng, viền xám, bo góc; xanh khi nhấn.
        JButton button = new JButton(title) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(primary ? PRIMARY : getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(primary ? PRIMARY : BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(WHITE);
        button.setForeground(primary ? WHITE : TEXT);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 5. Khung trắng và viền nhạt dùng cho form, bảng và biểu đồ. */
    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));
        return panel;
    }

    /**
     * 6. Nội dung co theo chiều ngang viewport để không trôi sang bên phải.
     * Khi dài, người dùng chỉ cuộn dọc ở toàn trang.
     */
    private static final class CotNoiDung extends JPanel implements Scrollable {
        CotNoiDung() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        protected void addImpl(Component child, Object constraints, int index) {
            if (child instanceof JComponent) {
                ((JComponent) child).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            super.addImpl(child, constraints, index);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(750, 560);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
            return Math.max(40, rect.height - 50);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** 7. Đầu trang thống nhất gồm breadcrumb, tiêu đề và mô tả. */
    public static JPanel trang(String title, String description) {
        JPanel page = new CotNoiDung();
        page.setBorder(new EmptyBorder(26, 28, 44, 28));
        page.add(label("Tổng quan   ›   " + title, 11, false));
        page.add(Box.createVerticalStrut(18));
        page.add(label(title, 24, true));
        page.add(Box.createVerticalStrut(7));
        page.add(label(description, 12, false));
        page.add(Box.createVerticalStrut(23));
        return page;
    }

    /** 8. Trang cuộn dọc, bảng riêng có thể cuộn ngang. */
    public static JScrollPane cuon(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getVerticalScrollBar().setBlockIncrement(90);
        return scroll;
    }

    /**
     * 9. Đọc logo từ tài nguyên: /Image/logo(1024x884).png.
     *
     * Không bóp méo ảnh: luôn giữ tỷ lệ gốc và căn giữa khung hiển thị.
     * Nếu thiếu tài nguyên, dùng logo dự phòng và không phát sinh lỗi URL null.
     */
    public static ImageIcon logo(int kichThuoc) {
        return logo(kichThuoc, kichThuoc);
    }

    public static ImageIcon logo(int rong, int cao) {
        if (rong < 1 || cao < 1) {
            throw new IllegalArgumentException("Kích thước logo phải lớn hơn 0.");
        }

        BufferedImage canvas = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            String[] tenAnh = {
                "/Image/medident_logo.png",
                "/Image/logo(1024x884).png",
                "/Image/logo.png",
                "/Image/logo_ai.png"
            };

            for (String path : tenAnh) {
                URL url = GiaoDienDangNhap.class.getResource(path);
                if (url == null) {
                    continue;
                }
                try {
                    BufferedImage source = ImageIO.read(url);
                    if (source == null) {
                        continue;
                    }

                    // Scale theo cạnh nhỏ hơn để ảnh không bị méo.
                    double tyLe = Math.min(rong / (double) source.getWidth(),
                            cao / (double) source.getHeight());
                    int displayWidth = Math.max(1, (int) Math.round(source.getWidth() * tyLe));
                    int displayHeight = Math.max(1, (int) Math.round(source.getHeight() * tyLe));
                    int left = (rong - displayWidth) / 2;
                    int top = (cao - displayHeight) / 2;

                    graphics.drawImage(source, left, top, displayWidth, displayHeight, null);
                    return new ImageIcon(canvas);
                } catch (IOException ex) {
                    System.err.println("Không đọc được ảnh " + path + ": " + ex.getMessage());
                }
            }

            // Logo dự phòng: hình tròn xanh và chữ M; không cần font biểu tượng.
            graphics.setColor(PRIMARY);
            graphics.fillRoundRect(1, 1, rong - 2, cao - 2, 14, 14);
            graphics.setColor(WHITE);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, cao / 2)));
            String text = "M";
            int textX = (rong - graphics.getFontMetrics().stringWidth(text)) / 2;
            int textY = (cao + graphics.getFontMetrics().getAscent()) / 2 - 3;
            graphics.drawString(text, textX, textY);
            return new ImageIcon(canvas);
        } finally {
            graphics.dispose();
        }
    }

    /** 10. Hộp lỗi phân biệt SQL Server với lỗi giao diện. */
    public static void loi(Component owner, Exception exception) {
        exception.printStackTrace();
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.toString();
        }
        if (message.contains("Invalid column name") || message.contains("Invalid object name")) {
            message = "Dữ liệu hệ thống chưa được cập nhật.";
        }
        JOptionPane.showMessageDialog(owner, message,
                "Không thể thực hiện",
                JOptionPane.ERROR_MESSAGE);
    }

    /** 11. Ô nhập liệu có padding và viền đồng nhất. */
    public static JTextField text(String value) {
        JTextField field = new JTextField(value, 23);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(9, 10, 9, 10)));
        return field;
    }

    /** 12. Hàng chứa bộ lọc, nút hành động căn trái. */
    public static JPanel hang() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 13. Cột STT hiển thị 1..N theo vị trí thật trên màn hình.
     * Nếu người dùng sắp xếp lại bằng header, STT vẫn liên tục 1..N.
     */
    public static void cotSoThuTu(JTable table) {
        if (table.getColumnCount() == 0 || !"STT".equals(table.getColumnName(0))) {
            return;
        }

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(45);
        column.setMaxWidth(65);
        column.setPreferredWidth(52);
        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int columnIndex) {
                JLabel result = (JLabel) super.getTableCellRendererComponent(
                        source, row + 1, selected, focused, row, columnIndex);
                result.setHorizontalAlignment(SwingConstants.CENTER);
                return result;
            }
        });
    }

    /** 14. Trang trí bảng và cho phép cuộn ngang ở bảng nhiều cột. */
    public static JComponent wrapTable(JTable table) {
        table.setRowHeight(39);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        // Không chọn nhầm dòng khi người dùng lọc/sắp xếp JTable.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) table.setRowSelectionInterval(row, row);
                }
            }
        });
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(BG);
        table.setSelectionBackground(new Color(0xE0F2FE));

        if (table.getColumnCount() >= 5) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < table.getColumnCount(); i++) {
                String column = table.getColumnName(i).toLowerCase();
                int width;
                if (column.equals("stt")) {
                    width = 52;
                } else if (column.contains("email")) {
                    width = 190;
                } else if (column.contains("họ tên")) {
                    width = 165;
                } else if (column.contains("đăng nhập")) {
                    width = 130;
                } else if (column.contains("vai trò")) {
                    width = 170;
                } else if (column.contains("trạng thái")) {
                    width = 120;
                } else {
                    width = 115;
                }
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
        cotSoThuTu(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        return scroll;
    }
}
