package UI.Chung;

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
import javax.swing.Box;
import javax.swing.BoxLayout;
import UI.Chung.BieuTuong;

import Class.TaiKhoan;
import DAO.HoTroDAO;
import DAO.TaiKhoanDAO;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Trung tâm yêu cầu chung cho Admin / Quản lý / Lễ tân.
 *
 * Dữ liệu được lọc THEO CẤP BẬC TRONG SQL SERVER, không lọc ở mỗi JTable.
 * Gồm 2 nhóm yêu cầu đã có trong MediDent:
 * 1) YeuCauHoTro - phản ánh, quên mật khẩu, hỗ trợ nghiệp vụ.
 * 2) YeuCauHuyPhieuThu - yêu cầu hủy hóa đơn/phiếu thu.
 */
public final class HoTroPage extends JPanel {

    private final HoTroDAO dao;
    private final TaiKhoan user;
    private final boolean duocPhanHoi;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Mã YC", "Người gửi", "Vai trò", "Thời điểm",
                "Tiêu đề", "Trạng thái", "Phản hồi"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // STT theo hàng đang hiển thị, vẫn đúng khi người dùng sắp xếp bảng.
    private final JTable bang = new JTable(model) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
            Component component = super.prepareRenderer(renderer, row, col);
            if (col == 0 && component instanceof javax.swing.JLabel) {
                javax.swing.JLabel label = (javax.swing.JLabel) component;
                label.setText(String.valueOf(row + 1));
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            }
            return component;
        }
    };
    private List<HoTroDAO.Hang> duLieu = Collections.emptyList();

    /** @param admin true khi đăng nhập là Admin có quyền HOTRO. */
    public HoTroPage(HoTroDAO dao, TaiKhoan user, boolean admin) {
        this.dao = dao;
        this.user = user;
        boolean coThePhanHoi = false;
        try {
            coThePhanHoi = dao.coQuyenPhanHoiHoTro(user.id);
        } catch (Exception ex) {
            // Không truy vấn được quyền: ẩn chức năng phản hồi.
        }
        this.duocPhanHoi = coThePhanHoi;

        setLayout(new BorderLayout());
        setBackground(GiaoDienHoTroPage.BG);

        add(taoHoTro(), BorderLayout.CENTER);
    }

    private JPanel taoHoTro() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(GiaoDienHoTroPage.BG);
        JPanel page = GiaoDienHoTroPage.trang("Trung tâm hỗ trợ - yêu cầu",
                "Xem và gửi yêu cầu hỗ trợ.");
        container.add(GiaoDienHoTroPage.cuon(page), BorderLayout.CENTER);

        JPanel toolbar = GiaoDienHoTroPage.hang();
        JButton gui = GiaoDienHoTroPage.nutIcon("Gửi yêu cầu", true, BieuTuong.Loai.THEM);
        JButton xem = GiaoDienHoTroPage.nutIcon("Xem nội dung", false, BieuTuong.Loai.TIM_KIEM);
        JButton taiLai = GiaoDienHoTroPage.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        toolbar.add(gui);
        toolbar.add(xem);
        toolbar.add(taiLai);

        if (duocPhanHoi) {
            JButton phanHoi = GiaoDienHoTroPage.nutIcon("Phản hồi yêu cầu", false,
                    BieuTuong.Loai.SUA);
            phanHoi.addActionListener(event -> phanHoi());
            toolbar.add(phanHoi);
        }
        if ("Quản trị viên".equals(user.tenVaiTro)) {
            JButton reset = GiaoDienHoTroPage.nutIcon("Đặt lại mật khẩu", false,
                    BieuTuong.Loai.SUA);
            reset.addActionListener(event -> datLaiMatKhau());
            toolbar.add(reset);
        }
        page.add(toolbar);
        page.add(Box.createVerticalStrut(14));

        JPanel card = GiaoDienHoTroPage.card(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(900, 450));
        card.add(GiaoDienHoTroPage.label("Danh sách yêu cầu theo cấp bậc", 14, true),
                BorderLayout.NORTH);

        bang.setAutoCreateRowSorter(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.setRowHeight(34);
        int[] widths = {50, 65, 170, 130, 145, 265, 115, 240};
        for (int col = 0; col < widths.length; col++) {
            bang.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        }
        card.add(GiaoDienHoTroPage.wrapTable(bang), BorderLayout.CENTER);
        page.add(card);

        gui.addActionListener(event -> guiYeuCau());
        xem.addActionListener(event -> xemNoiDung());
        taiLai.addActionListener(event -> tai());
        tai();
        return container;
    }

    private void tai() {
        try {
            duLieu = dao.danhSachHoTro(user.id);
            model.setRowCount(0);
            for (int i = 0; i < duLieu.size(); i++) {
                HoTroDAO.Hang h = duLieu.get(i);
                model.addRow(new Object[]{i + 1, h.id, h.cot[0], h.cot[6],
                        h.cot[1], h.cot[2], h.cot[3], h.cot[5]});
            }
        } catch (Exception ex) {
            GiaoDienHoTroPage.loi(this, ex);
        }
    }

    private HoTroDAO.Hang daChon() {
        int viewIndex = bang.getSelectedRow();
        if (viewIndex < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một yêu cầu.");
            return null;
        }
        int modelRow = bang.convertRowIndexToModel(viewIndex);
        int id = ((Number) model.getValueAt(modelRow, 1)).intValue();
        for (HoTroDAO.Hang hang : duLieu) {
            if (hang.id == id) return hang;
        }
        JOptionPane.showMessageDialog(this, "Dữ liệu đã thay đổi, hãy tải lại danh sách.");
        return null;
    }

    private void guiYeuCau() {
        JTextField tieuDe = GiaoDienHoTroPage.text("");
        JTextArea noiDung = new JTextArea(6, 36);
        noiDung.setLineWrap(true);
        noiDung.setWrapStyleWord(true);
        JLabel loiTieuDe = nhanLoi();
        JLabel loiNoiDung = nhanLoi();
        Runnable kiemTraTieuDe = () -> loiTieuDe.setText(
                tieuDe.getText().trim().isEmpty() ? "Vui lòng nhập tiêu đề."
                        : tieuDe.getText().trim().length() > 160 ? "Tiêu đề tối đa 160 ký tự." : "");
        Runnable kiemTraNoiDung = () -> loiNoiDung.setText(
                noiDung.getText().trim().isEmpty() ? "Vui lòng nhập nội dung."
                        : noiDung.getText().trim().length() > 1500 ? "Nội dung tối đa 1500 ký tự." : "");
        batLoiKhiRoiO(tieuDe, kiemTraTieuDe);
        batLoiKhiRoiO(noiDung, kiemTraNoiDung);

        JPanel form = new JPanel(new BorderLayout(0, 9));
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.add(new JLabel("Tiêu đề yêu cầu"), BorderLayout.NORTH);
        top.add(tieuDe, BorderLayout.CENTER);
        top.add(loiTieuDe, BorderLayout.SOUTH);
        form.add(top, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.add(new JScrollPane(noiDung), BorderLayout.CENTER);
        body.add(loiNoiDung, BorderLayout.SOUTH);
        form.add(body, BorderLayout.CENTER);
        form.setFocusable(true);
        form.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                form.requestFocusInWindow();
            }
        });
        while (true) {
            if (JOptionPane.showConfirmDialog(this, form, "Gửi yêu cầu hỗ trợ",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            kiemTraTieuDe.run();
            kiemTraNoiDung.run();
            if (!loiTieuDe.getText().isEmpty() || !loiNoiDung.getText().isEmpty()) continue;
            try {
                dao.guiHoTro(user.id, tieuDe.getText(), noiDung.getText());
                JOptionPane.showMessageDialog(this, "Đã ghi nhận yêu cầu.");
                tai();
                return;
            } catch (Exception ex) {
                GiaoDienHoTroPage.loi(this, ex);
            }
        }
    }

    private void xemNoiDung() {
        HoTroDAO.Hang h = daChon();
        if (h == null) return;
        JTextArea details = new JTextArea("Người gửi: " + h.cot[0]
                + "\nVai trò: " + h.cot[6]
                + "\nNgày tạo: " + h.cot[1]
                + "\nTrạng thái: " + h.cot[3]
                + "\n\nNội dung:\n" + h.cot[4]
                + "\n\nPhản hồi:\n" + h.cot[5], 12, 48);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(details),
                "Yêu cầu #" + h.id + " — " + h.cot[2], JOptionPane.INFORMATION_MESSAGE);
    }

    private void phanHoi() {
        if (!duocPhanHoi) return;
        HoTroDAO.Hang h = daChon();
        if (h == null) return;
        JTextArea text = new JTextArea(6, 35);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        JLabel loi = nhanLoi();
        Runnable kiemTra = () -> loi.setText(text.getText().trim().isEmpty()
                ? "Vui lòng nhập phản hồi."
                : text.getText().trim().length() > 1500 ? "Phản hồi tối đa 1500 ký tự." : "");
        batLoiKhiRoiO(text, kiemTra);
        JPanel form = new JPanel(new BorderLayout(0, 5));
        form.add(new JScrollPane(text), BorderLayout.CENTER);
        form.add(loi, BorderLayout.SOUTH);
        while (true) {
            int option = JOptionPane.showConfirmDialog(this, form,
                    "Phản hồi: " + h.cot[2], JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;
            kiemTra.run();
            if (!loi.getText().isEmpty()) continue;
            try {
                dao.phanHoiHoTro(h.id, text.getText(), user.id);
                JOptionPane.showMessageDialog(this, "Đã lưu phản hồi.");
                tai();
                return;
            } catch (Exception ex) {
                GiaoDienHoTroPage.loi(this, ex);
            }
        }
    }

    private void datLaiMatKhau() {
        HoTroDAO.Hang hang = daChon();
        if (hang == null) return;
        if (!"Quên mật khẩu".equals(hang.cot[2]) || !"Chờ xử lý".equals(hang.cot[3])) {
            JOptionPane.showMessageDialog(this, "Hãy chọn yêu cầu quên mật khẩu đang chờ xử lý.");
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Đặt lại mật khẩu cho " + hang.cot[0] + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            String matKhau = new TaiKhoanDAO().datLaiMatKhauTheoYeuCau(hang.id, user.id);
            JTextField hienThi = new JTextField(matKhau, 24);
            hienThi.setEditable(false);
            JOptionPane.showMessageDialog(this, new Object[]{
                    "Mật khẩu mới (chỉ hiển thị tại đây):", hienThi,
                    "Nhân viên phải đổi mật khẩu khi đăng nhập."},
                    "Đã đặt lại mật khẩu", JOptionPane.INFORMATION_MESSAGE);
            hienThi.setText("");
            tai();
        } catch (Exception ex) {
            GiaoDienHoTroPage.loi(this, ex);
        }
    }

    private static JLabel nhanLoi() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(0xDC2626));
        return label;
    }

    private static void batLoiKhiRoiO(javax.swing.text.JTextComponent field, Runnable check) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { check.run(); }
        });
    }

}


/**
 * Giao diện dùng chung giữa các vai trò.
 *
 * Chỉ chứa những hàm tạo giao diện / bảng màu; các màn hình riêng của quản trị
 * viên nằm trong UI.QuanTriVien và không chỉnh sửa ManHinhTrangChu cũ.
 */
final class GiaoDienHoTroPage {

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

    private GiaoDienHoTroPage() {
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
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(WHITE);
        button.setForeground(TEXT);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 4. Nút có icon Java2D, không còn các ký tự Unicode gây ô vuông. */
    public static JButton nutIcon(String title, boolean primary, BieuTuong.Loai loai) {
        JButton button = nut(title, primary);
        button.setIcon(new BieuTuong(loai, MUTED, 17));
        button.setIconTextGap(9);
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
                URL url = GiaoDienHoTroPage.class.getResource(path);
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
