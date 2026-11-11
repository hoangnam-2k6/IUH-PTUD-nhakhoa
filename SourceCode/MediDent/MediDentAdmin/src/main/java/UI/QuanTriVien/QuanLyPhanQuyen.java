package UI.QuanTriVien;

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


import Entity.TaiKhoan;
import Entity.VaiTro;
import DAO.TaiKhoanDAO;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Quản lý phân quyền: bảng ma trận quyền theo các VaiTro / VaiTroQuyen trong SQL.
 *
 * STT tăng từ 1 đến N; quyền có/không được vẽ bằng icon Java2D xanh/xám
 * (không dùng dấu ✓ hoặc × Unicode nên không còn ô vuông).
 */
public class QuanLyPhanQuyen extends JPanel {

    private final TaiKhoanDAO db;
    private final TaiKhoan actor;
    private List<VaiTro> roles = new ArrayList<>();
    private JScrollPane trangDanhSach;

    // Ma trận đọc mã quyền từ DAO, thống nhất cả quyền Nhân viên, Dịch vụ và Hỗ trợ.
    // Thứ tự tên quyền khớp trực tiếp với danh sách mã quyền trong DAO.
    private static final String[] TEN_QUYEN = TaiKhoanDAO.tenCacQuyen();
    private final DefaultTableModel model = new DefaultTableModel(taoTieuDe(), 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // Cột quyền lưu Boolean để phân biệt có quyền / không có quyền.
            return columnIndex >= 2 ? Boolean.class : super.getColumnClass(columnIndex);
        }
    };
    private final JTable table = new JTable(model) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                         int row, int column) {
            Component result = super.prepareRenderer(renderer, row, column);
            if (column == 0 && result instanceof JLabel) {
                ((JLabel) result).setText(String.valueOf(row + 1));
                ((JLabel) result).setHorizontalAlignment(SwingConstants.CENTER);
            }
            return result;
        }
    };

    private static String[] taoTieuDe() {
        String[] tieuDe = new String[TEN_QUYEN.length + 2];
        tieuDe[0] = "STT";
        tieuDe[1] = "Vai trò";
        System.arraycopy(TEN_QUYEN, 0, tieuDe, 2, TEN_QUYEN.length);
        return tieuDe;
    }

    public QuanLyPhanQuyen(TaiKhoanDAO db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDienQuanLyPhanQuyen.BG);

        JPanel page = GiaoDienQuanLyPhanQuyen.trang("Quản lý phân quyền",
                "Thiết lập vai trò và quyền truy cập cho từng nhóm người dùng.");
        trangDanhSach = GiaoDienQuanLyPhanQuyen.cuon(page);
        add(trangDanhSach, BorderLayout.CENTER);

        // 1. Các nút thao tác với VaiTro.
        JPanel actions = GiaoDienQuanLyPhanQuyen.hang();
        JButton edit = GiaoDienQuanLyPhanQuyen.nut("Chỉnh sửa quyền", true);
        actions.add(edit);
        page.add(actions);
        page.add(Box.createVerticalStrut(17));

        // 2. Tạo bảng có STT, vẽ icon cho từng ô quyền.
        JPanel card = GiaoDienQuanLyPhanQuyen.taoThe(new BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(740, 440));
        card.add(GiaoDienQuanLyPhanQuyen.bocBang(table), BorderLayout.CENTER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(165);
        for (int i = 2; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(130);
        trangTriCotQuyen();
        page.add(card);

        taiLai();
        edit.addActionListener(event -> {
            VaiTro selected = vaiTroDangChon();
            if (selected != null) {
                hienForm(selected);
            }
        });
    }

    /** 3. Renderer hiển thị chữ Có/Không, không cho JTable dùng checkbox mặc định. */
    private void trangTriCotQuyen() {
        for (int column = 2; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(
                    new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable source, Object value,
                        boolean selected, boolean focused, int row, int column) {
                    JLabel result = (JLabel) super.getTableCellRendererComponent(
                            source, "", selected, focused, row, column);
                    boolean allowed = Boolean.TRUE.equals(value);
                    result.setHorizontalAlignment(SwingConstants.CENTER);
                    result.setIcon(null);
                    result.setText(allowed ? "Có" : "Không");
                    result.setForeground(allowed ? GiaoDienQuanLyPhanQuyen.GREEN : GiaoDienQuanLyPhanQuyen.MUTED);
                    return result;
                }
            });
        }
    }

    /** 4. Nạp vai trò từ CSDL; không ghi ID thật vào cột STT. */
    private void taiLai() {
        try {
            roles = db.vaiTro();
            model.setRowCount(0);
            int number = 1;
            for (VaiTro item : roles) {
                Object[] row = new Object[TEN_QUYEN.length + 2];
                row[0] = number++;
                row[1] = item.ten;
                for (int index = 0; index < TEN_QUYEN.length; index++) {
                    row[index + 2] = index < item.quyen.length && item.quyen[index];
                }
                model.addRow(row);
            }
        } catch (Exception exception) {
            GiaoDienQuanLyPhanQuyen.loi(this, exception);
        }
    }

    /** 5. Lấy vai trò đang chọn qua model index, an toàn khi sắp xếp bảng. */
    private VaiTro vaiTroDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một vai trò.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < roles.size() ? roles.get(modelRow) : null;
    }

    /** 6. Form checkbox giúp tránh gõ sai tên quyền. */
    private void hienForm(VaiTro old) {
        if (old != null && "Quản trị viên".equals(old.ten)) {
            JOptionPane.showMessageDialog(this,
                    "Không chỉnh sửa vai trò Quản trị viên gốc.");
            return;
        }
        JTextField name = GiaoDienQuanLyPhanQuyen.taoOChu(old == null ? "" : old.ten);
        name.setEditable(false);
        JTextField description = GiaoDienQuanLyPhanQuyen.taoOChu(old == null ? "" : old.moTa);
        description.setEditable(false);
        // Tên và thứ tự các quyền luôn tương ứng TaiKhoanDAO.cacQuyen().
        String[] permissionNames = TEN_QUYEN;
        JCheckBox[] permissions = new JCheckBox[permissionNames.length];
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(GiaoDienQuanLyPhanQuyen.taoNhan("Tên vai trò *", 12, true));
        form.add(name);
        form.add(GiaoDienQuanLyPhanQuyen.taoNhan("Mô tả", 12, true));
        form.add(description);
        form.add(GiaoDienQuanLyPhanQuyen.taoNhan("Quyền truy cập", 12, true));
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = new JCheckBox(permissionNames[i],
                    old != null && i < old.quyen.length && old.quyen[i]);
            if (i < 2) permissions[i].setEnabled(false); // Chỉ Admin có quyền tài khoản / phân quyền.
            form.add(permissions[i]);
            permissions[i].setBackground(Color.white);
        }

        // Form sửa quyền hiển thị thay cho bảng, không mở hộp thoại.
        form.setOpaque(false);
        JPanel trangSua = GiaoDienQuanLyPhanQuyen.trang(
                "Chỉnh sửa quyền", "Tích các chức năng được phép hiển thị trong menu.");
        JPanel khungForm = GiaoDienQuanLyPhanQuyen.taoThe(new BorderLayout());
        khungForm.add(form, BorderLayout.CENTER);
        trangSua.add(khungForm);
        trangSua.add(Box.createVerticalStrut(16));

        JPanel thaoTac = GiaoDienQuanLyPhanQuyen.hang();
        JButton luu = GiaoDienQuanLyPhanQuyen.nut("Lưu quyền", true);
        JButton quayLai = GiaoDienQuanLyPhanQuyen.nut("Quay lại", false);
        thaoTac.add(luu);
        thaoTac.add(quayLai);
        trangSua.add(thaoTac);
        quayLai.addActionListener(e -> hienDanhSach());
        luu.addActionListener(e -> {
            boolean[] values = new boolean[permissions.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = permissions[i].isSelected();
            }
            try {
                db.luuVaiTro(old.id, name.getText().trim(),
                        description.getText().trim(), values, actor.id);
                taiLai();
                hienDanhSach();
            } catch (Exception exception) {
                GiaoDienQuanLyPhanQuyen.loi(this, exception);
            }
        });
        removeAll();
        add(GiaoDienQuanLyPhanQuyen.cuon(trangSua), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void hienDanhSach() {
        removeAll();
        add(trangDanhSach, BorderLayout.CENTER);
        revalidate();
        repaint();
    }



}


/**
 * Giao diện dùng chung giữa các vai trò.
 *
 * Chỉ chứa những hàm tạo giao diện / bảng màu; các màn hình riêng của quản trị
 * viên nằm trong UI.QuanTriVien và không chỉnh sửa ManHinhTrangChu cũ.
 */
final class GiaoDienQuanLyPhanQuyen {

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

    private GiaoDienQuanLyPhanQuyen() {
        // Không tạo đối tượng từ lớp tiện ích.
    }

    /** 2. Tạo nhãn có font và màu nhất quán. */
    public static JLabel taoNhan(String text, int fontSize, boolean bold) {
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

    /** 5. Khung trắng và viền nhạt dùng cho form, bảng và biểu đồ. */
    public static JPanel taoThe(LayoutManager layout) {
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
        page.add(taoNhan("Tổng quan   ›   " + title, 11, false));
        page.add(Box.createVerticalStrut(18));
        page.add(taoNhan(title, 24, true));
        page.add(Box.createVerticalStrut(7));
        page.add(taoNhan(description, 12, false));
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
                URL url = GiaoDienQuanLyPhanQuyen.class.getResource(path);
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
    public static JTextField taoOChu(String value) {
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
    public static JComponent bocBang(JTable table) {
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
