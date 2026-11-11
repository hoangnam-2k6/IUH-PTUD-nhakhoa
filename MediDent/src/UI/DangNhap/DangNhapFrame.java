package UI.DangNhap;

import Dao.TaiKhoanDao;
import Entity.TaiKhoan;
import UI.LeTan.LeTanFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static UI.LeTan.GiaoDien.font;

/**
 * Màn hình đăng nhập của phân hệ Lễ tân.
 *
 * <p>Bố cục hai cột: bên trái là ảnh phòng khám nha khoa, bên phải là biểu mẫu
 * đăng nhập trên nền trắng. Đăng nhập thành công sẽ mở {@link LeTanFrame}.</p>
 */
public class DangNhapFrame extends JFrame {
    private static final Color MAU_CHINH     = Color.decode("#0284C7");
    private static final Color MAU_CHINH_DAM = Color.decode("#0369A1");
    private static final Color CHU_CHINH     = Color.decode("#0F172A");
    private static final Color CHU_PHU       = Color.decode("#64748B");
    private static final Color CHU_MO        = Color.decode("#94A3B8");
    private static final Color VIEN          = Color.decode("#E2E8F0");
    private static final Color LOI           = Color.decode("#DC2626");
    private static final Color NEN_FORM      = Color.WHITE;

    private static final int RONG_FORM = 432;
    private static final int CAO_O     = 46;

    private final TaiKhoanDao taiKhoanDao = new TaiKhoanDao();

    private final JTextField oTenDangNhap = new OText();
    private final JPasswordField oMatKhau = new OMatKhau();
    private final JButton nutDangNhap = new NutDangNhap();
    private final JLabel trangThai = new JLabel(" ");
    private boolean dangXuLy;

    public DangNhapFrame() {
        setTitle("MediDent - Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null);

        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        add(taoPanelTrai(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        add(taoPanelPhai(), gbc);

        getRootPane().setDefaultButton(nutDangNhap);
        nutDangNhap.addActionListener(this::dangNhap);
        oTenDangNhap.addActionListener(e -> oMatKhau.requestFocusInWindow());
        oMatKhau.addActionListener(this::dangNhap);
        oTenDangNhap.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                oTenDangNhap.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                oTenDangNhap.repaint();
            }
        });
        oMatKhau.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                oMatKhau.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                oMatKhau.repaint();
            }
        });

        SwingUtilities.invokeLater(oTenDangNhap::requestFocusInWindow);
    }

    // =====================================================================
    // Panel trái: ảnh phòng khám nha khoa
    // =====================================================================

    private JPanel taoPanelTrai() {
        JPanel trai = new JPanel() {
            private final BufferedImage anh = docAnhNen();

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                if (anh != null) {
                    // Lấp đầy panel, giữ tỉ lệ ảnh và cắt phần dư ở giữa.
                    double tiLe = Math.max((double) getWidth() / anh.getWidth(),
                            (double) getHeight() / anh.getHeight());
                    int rong = (int) Math.ceil(anh.getWidth() * tiLe);
                    int cao = (int) Math.ceil(anh.getHeight() * tiLe);
                    int x = (getWidth() - rong) / 2;
                    int y = (getHeight() - cao) / 2;
                    g2.drawImage(anh, x, y, rong, cao, null);
                } else {
                    // Dự phòng khi thiếu file ảnh: nền chuyển sắc ấm.
                    g2.setPaint(new GradientPaint(0, 0, Color.decode("#5B2E28"),
                            getWidth(), getHeight(), Color.decode("#D2C0B2")));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.WHITE);
                    g2.setFont(font(Font.BOLD, 34));
                    g2.drawString("MEDIDENT", 44, 84);
                    g2.setFont(font(Font.PLAIN, 14));
                    g2.drawString("Quản lý phòng khám nha khoa", 44, 110);
                }
                g2.dispose();
            }
        };
        trai.setMinimumSize(new Dimension(380, 0));
        trai.setPreferredSize(new Dimension(590, 0));
        return trai;
    }

    private static BufferedImage docAnhNen() {
        try (InputStream in = DangNhapFrame.class
                .getResourceAsStream("/Resource/Image/dangnhap_nhakhoa.jpg")) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (IOException ignored) {
            // Không có ảnh thì dùng nền dự phòng.
        }
        return null;
    }

    // =====================================================================
    // Panel phải: biểu mẫu đăng nhập
    // =====================================================================

    private JPanel taoPanelPhai() {
        JPanel phai = new JPanel(new GridBagLayout());
        phai.setBackground(NEN_FORM);
        // Cùng bề rộng ưu tiên với panel trái để hai cột chia đôi cửa sổ.
        phai.setPreferredSize(new Dimension(590, 0));
        phai.setMinimumSize(new Dimension(420, 0));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(RONG_FORM, 606));
        form.setMaximumSize(new Dimension(RONG_FORM, 606));

        LogoPanel logo = new LogoPanel();
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(RONG_FORM, 72));
        logo.setPreferredSize(new Dimension(RONG_FORM, 72));
        form.add(logo);
        form.add(Box.createVerticalStrut(124));

        JLabel tieuDe = new JLabel("Đăng nhập hệ thống");
        tieuDe.setFont(font(Font.BOLD, 27));
        tieuDe.setForeground(CHU_CHINH);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tieuDe);
        form.add(Box.createVerticalStrut(10));

        JLabel phu = new JLabel("Nhập tài khoản được quản trị viên cấp để tiếp tục.");
        phu.setFont(font(Font.PLAIN, 14));
        phu.setForeground(CHU_PHU);
        phu.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(phu);
        form.add(Box.createVerticalStrut(46));

        form.add(nhan("Tên đăng nhập"));
        form.add(Box.createVerticalStrut(7));
        cauHinhO(oTenDangNhap);
        form.add(oTenDangNhap);
        form.add(Box.createVerticalStrut(25));

        form.add(taoHangMatKhau());
        form.add(Box.createVerticalStrut(7));
        cauHinhO(oMatKhau);
        form.add(oMatKhau);
        form.add(Box.createVerticalStrut(8));

        trangThai.setFont(font(Font.PLAIN, 12));
        trangThai.setForeground(LOI);
        trangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        trangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        trangThai.setPreferredSize(new Dimension(RONG_FORM, 18));
        form.add(trangThai);
        form.add(Box.createVerticalStrut(8));

        nutDangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        nutDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, CAO_O));
        nutDangNhap.setPreferredSize(new Dimension(RONG_FORM, CAO_O));
        form.add(nutDangNhap);
        form.add(Box.createVerticalStrut(37));

        JLabel chan = new JLabel("Tài khoản được phân quyền theo dữ liệu hệ thống.");
        chan.setFont(font(Font.PLAIN, 12));
        chan.setForeground(CHU_MO);
        chan.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(chan);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        phai.add(form, gbc);
        return phai;
    }

    private JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        l.setPreferredSize(new Dimension(RONG_FORM, 18));
        return l;
    }

    private JPanel taoHangMatKhau() {
        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        hang.setPreferredSize(new Dimension(RONG_FORM, 18));

        JLabel l = new JLabel("Mật khẩu");
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        hang.add(l, BorderLayout.WEST);

        JButton quenMatKhau = new JButton("Quên mật khẩu?");
        quenMatKhau.setFont(font(Font.PLAIN, 12));
        quenMatKhau.setForeground(MAU_CHINH);
        quenMatKhau.setFocusPainted(false);
        quenMatKhau.setBorderPainted(false);
        quenMatKhau.setContentAreaFilled(false);
        quenMatKhau.setOpaque(false);
        quenMatKhau.setMargin(new Insets(0, 0, 0, 0));
        quenMatKhau.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quenMatKhau.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                quenMatKhau.setForeground(MAU_CHINH_DAM);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                quenMatKhau.setForeground(MAU_CHINH);
            }
        });
        quenMatKhau.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Vui lòng liên hệ quản trị viên để được cấp lại mật khẩu.",
                "Quên mật khẩu", JOptionPane.INFORMATION_MESSAGE));
        hang.add(quenMatKhau, BorderLayout.EAST);
        return hang;
    }

    private static void cauHinhO(JComponent o) {
        o.setAlignmentX(Component.LEFT_ALIGNMENT);
        o.setMaximumSize(new Dimension(Integer.MAX_VALUE, CAO_O));
        o.setPreferredSize(new Dimension(RONG_FORM, CAO_O));
        o.setMinimumSize(new Dimension(120, CAO_O));
    }

    // =====================================================================
    // Đăng nhập
    // =====================================================================

    private void dangNhap(ActionEvent e) {
        if (dangXuLy) return;
        String ten = oTenDangNhap.getText() == null ? "" : oTenDangNhap.getText().trim();
        String matKhau = new String(oMatKhau.getPassword());
        if (ten.isEmpty()) {
            hienLoi("Vui lòng nhập tên đăng nhập.");
            oTenDangNhap.requestFocusInWindow();
            return;
        }
        if (matKhau.isEmpty()) {
            hienLoi("Vui lòng nhập mật khẩu.");
            oMatKhau.requestFocusInWindow();
            return;
        }

        trangThai.setText(" ");
        setDangXuLy(true);
        new SwingWorker<TaiKhoan, Void>() {
            @Override
            protected TaiKhoan doInBackground() throws Exception {
                return taiKhoanDao.dangNhap(ten, matKhau);
            }

            @Override
            protected void done() {
                setDangXuLy(false);
                try {
                    TaiKhoan tk = get();
                    dispose();
                    SwingUtilities.invokeLater(() -> new LeTanFrame(tk).setVisible(true));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    hienLoi("Đăng nhập bị gián đoạn.");
                } catch (ExecutionException ex) {
                    Throwable loi = ex.getCause() == null ? ex : ex.getCause();
                    if (loi instanceof IllegalArgumentException) {
                        hienLoi(loi.getMessage());
                        oMatKhau.selectAll();
                        oMatKhau.requestFocusInWindow();
                    } else if (loi instanceof SQLException) {
                        hienLoi("Không thể kết nối cơ sở dữ liệu.");
                        JOptionPane.showMessageDialog(DangNhapFrame.this,
                                loi.getMessage(), "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
                    } else {
                        hienLoi("Đăng nhập thất bại: " + loi.getMessage());
                    }
                }
            }
        }.execute();
    }

    private void hienLoi(String noiDung) {
        trangThai.setForeground(LOI);
        trangThai.setText(noiDung == null ? " " : noiDung);
    }

    private void setDangXuLy(boolean b) {
        dangXuLy = b;
        nutDangNhap.setEnabled(!b);
        oTenDangNhap.setEnabled(!b);
        oMatKhau.setEnabled(!b);
        oTenDangNhap.repaint();
        oMatKhau.repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new DangNhapFrame().setVisible(true));
    }

    // =====================================================================
    // Thành phần đồ họa tự vẽ
    // =====================================================================

    /** Logo đầu biểu mẫu: biểu tượng răng + chữ MEDIDENT + dòng mô tả. */
    private static final class LogoPanel extends JPanel {
        private static final String TEN = "MEDIDENT";
        private static final String MO_TA = "HỆ THỐNG QUẢN LÝ NHA KHOA";
        private static final Font FONT_TEN = font(Font.BOLD, 30);
        private static final Font FONT_MO_TA = fontTheoDoi(11, 0.16f);

        private LogoPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontMetrics fmTen = g2.getFontMetrics(FONT_TEN);
            int rongTen = fmTen.stringWidth(TEN);
            int xTen = (getWidth() - rongTen) / 2;
            int yTen = 40;

            // Biểu tượng răng nằm bên trái chữ MEDIDENT.
            int coIcon = 48;
            int xIcon = Math.max(0, xTen - coIcon - 28);
            int yIcon = yTen - fmTen.getAscent() + 1;
            veRang(g2, xIcon, yIcon, coIcon);

            g2.setFont(FONT_TEN);
            g2.setColor(MAU_CHINH);
            g2.drawString(TEN, xTen, yTen);

            FontMetrics fmMoTa = g2.getFontMetrics(FONT_MO_TA);
            int rongMoTa = fmMoTa.stringWidth(MO_TA);
            g2.setFont(FONT_MO_TA);
            g2.setColor(CHU_MO);
            g2.drawString(MO_TA, (getWidth() - rongMoTa) / 2, 68);

            g2.dispose();
        }
    }

    /** Vẽ biểu tượng răng bằng nét viền xanh. */
    private static void veRang(Graphics2D g2, int x, int y, int co) {
        Graphics2D g = (Graphics2D) g2.create();
        g.translate(x, y);
        double tiLe = co / 64.0;
        g.scale(tiLe, tiLe);

        Path2D than = new Path2D.Double();
        than.moveTo(20, 8);
        than.curveTo(13, 8, 8, 14, 8, 24);
        than.curveTo(8, 34, 12, 40, 14, 50);
        than.curveTo(15, 56, 18, 58, 20, 52);
        than.curveTo(22, 45, 23, 38, 32, 38);
        than.curveTo(41, 38, 42, 45, 44, 52);
        than.curveTo(46, 58, 49, 56, 50, 50);
        than.curveTo(52, 40, 56, 34, 56, 24);
        than.curveTo(56, 14, 51, 8, 44, 8);
        than.curveTo(38, 8, 35, 12, 32, 12);
        than.curveTo(29, 12, 26, 8, 20, 8);
        than.closePath();

        g.setColor(MAU_CHINH);
        g.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(than);

        // Nét cong nhỏ phía trên tạo điểm nhấn.
        Path2D nhan = new Path2D.Double();
        nhan.moveTo(20, 21);
        nhan.curveTo(26, 16, 38, 16, 44, 21);
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(nhan);

        g.dispose();
    }

    /** Ô nhập chữ bo góc, viền xám, đổi viền xanh khi được chọn. */
    private static class OText extends JTextField {
        private OText() {
            setOpaque(false);
            setFont(font(Font.PLAIN, 15));
            setForeground(CHU_CHINH);
            setCaretColor(MAU_CHINH);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            veNenO(g, this);
            super.paintComponent(g);
        }
    }

    /** Ô nhập mật khẩu có cùng kiểu dáng với ô tên đăng nhập. */
    private static final class OMatKhau extends JPasswordField {
        private OMatKhau() {
            setOpaque(false);
            setFont(font(Font.PLAIN, 15));
            setForeground(CHU_CHINH);
            setCaretColor(MAU_CHINH);
            setEchoChar('•');
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            veNenO(g, this);
            super.paintComponent(g);
        }
    }

    private static void veNenO(Graphics g, JComponent o) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int rong = o.getWidth();
        int cao = o.getHeight();
        g2.setColor(o.isEnabled() ? Color.WHITE : Color.decode("#F8FAFC"));
        g2.fillRoundRect(0, 0, rong - 1, cao - 1, 10, 10);
        g2.setColor(o.hasFocus() ? MAU_CHINH : VIEN);
        g2.setStroke(new BasicStroke(o.hasFocus() ? 1.6f : 1f));
        g2.drawRoundRect(0, 0, rong - 1, cao - 1, 10, 10);
        g2.dispose();
    }

    /** Nút đăng nhập nền xanh, bo góc, sáng lên khi rê chuột. */
    private static final class NutDangNhap extends JButton {
        private boolean dangRe;

        private NutDangNhap() {
            super("Đăng nhập");
            setFont(font(Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    dangRe = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dangRe = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color nen = !isEnabled() ? Color.decode("#7DD3FC")
                    : (dangRe ? MAU_CHINH_DAM : MAU_CHINH);
            g2.setColor(nen);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Font có giãn chữ nhẹ cho dòng mô tả in hoa. */
    private static Font fontTheoDoi(int coChu, float tiLe) {
        Map<TextAttribute, Object> thuocTinh = new HashMap<>();
        thuocTinh.put(TextAttribute.TRACKING, tiLe);
        return font(Font.PLAIN, coChu).deriveFont(thuocTinh);
    }
}
