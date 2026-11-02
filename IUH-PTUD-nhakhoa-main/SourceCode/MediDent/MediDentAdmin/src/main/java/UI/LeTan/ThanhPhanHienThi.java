package UI.LeTan;

import java.awt.BasicStroke;

import java.awt.Color;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.Font;

import java.awt.Font;


import java.awt.FontMetrics;

import java.awt.Graphics2D;

import java.awt.Graphics2D;


import java.awt.Graphics;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;


import java.awt.RenderingHints;


import java.math.BigDecimal;

import java.math.RoundingMode;

import java.text.NumberFormat;

import java.util.Locale;


import javax.swing.BorderFactory;

import javax.swing.Box;

import javax.swing.BoxLayout;

import javax.swing.Icon;


import javax.swing.JComponent;


import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTable;

import javax.swing.ScrollPaneConstants;

import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

import javax.swing.border.EmptyBorder;


import javax.swing.table.DefaultTableCellRenderer;


import javax.swing.table.DefaultTableModel;


import static UI.LeTan.GiaoDien.AVATAR_COLORS;

import static UI.LeTan.GiaoDien.BORDER;

import static UI.LeTan.GiaoDien.MUTED;

import static UI.LeTan.GiaoDien.PRIMARY;

import static UI.LeTan.GiaoDien.PRIMARY_DARK;

import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;

import static UI.LeTan.GiaoDien.ROW_ALT;

import static UI.LeTan.GiaoDien.TEXT;

import static UI.LeTan.GiaoDien.escapeHtml;

import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.font;


import static UI.LeTan.GiaoDien.khuRangCua;


import static UI.LeTan.GiaoDien.label;



/** Chấm tròn nhỏ đứng trước dòng trạng thái / tiêu đề nhóm. */
final class BieuTuongCham implements Icon {
    private final Color color;

    public BieuTuongCham(Color color) {
        this.color = color;
    }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        khuRangCua(g2);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        g2.fillOval(x, y, 12, 12);
        g2.setColor(color);
        g2.fillOval(x + 3, y + 3, 6, 6);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 12; }
    @Override public int getIconHeight() { return 12; }
}

/** Biểu tượng chữ "i" trong vòng tròn, đứng trước dòng ghi chú. */
final class BieuTuongThongTin implements Icon {

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        khuRangCua(g2);
        g2.setColor(PRIMARY);
        g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(x, y, 13, 13);
        g2.drawLine(x + 7, y + 6, x + 7, y + 10);
        g2.fillOval(x + 6, y + 3, 2, 2);
        g2.dispose();
    }

    @Override public int getIconWidth() { return 14; }
    @Override public int getIconHeight() { return 14; }
}

/** Tên kèm avatar tròn có chữ cái đầu, màu theo tên. */
final class BoVeAvatar extends DefaultTableCellRenderer {
    private Color cellBackground = Color.WHITE;
    private Color avatarColor = PRIMARY;
    private String initial = "";

    @Override public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focus, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focus, row, column);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(font(Font.PLAIN, 12));
        setBorder(new EmptyBorder(0, 46, 0, 10));
        if (!selected) setForeground(TEXT);
        String name = value == null ? "" : String.valueOf(value).trim();
        cellBackground = BangKeSoc.rowBackground(table, row, selected);
        if (name.isEmpty()) {
            initial = "";
        } else {
            String[] parts = name.split("\\s+");
            String last = parts[parts.length - 1];
            initial = last.substring(0, 1).toUpperCase();
            avatarColor = AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
        }
        return this;
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(cellBackground);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (!initial.isEmpty()) {
            int size = 28;
            int y = (getHeight() - size) / 2;
            g2.setColor(new Color(avatarColor.getRed(), avatarColor.getGreen(), avatarColor.getBlue(), 36));
            g2.fillOval(10, y, size, size);
            g2.setColor(avatarColor);
            g2.setFont(font(Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(initial, 10 + (size - fm.stringWidth(initial)) / 2,
                    y + (size - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
        super.paintComponent(graphics);
    }
}

/** Ô căn giữa với màu chữ riêng (dùng cho cột STT, cột mã, Mã BN, Mã NV). */
final class BoVeCanGiua extends DefaultTableCellRenderer {
    private final Color color;
    private final boolean bold;

    public BoVeCanGiua(Color color, boolean bold) {
        this.color = color;
        this.bold = bold;
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object value,
            boolean selected, boolean focus, int row, int col) {
        super.getTableCellRendererComponent(t, value, selected, focus, row, col);
        setHorizontalAlignment(SwingConstants.CENTER);
        if (bold) setFont(font(Font.BOLD, 12));
        if (!selected) setForeground(color);
        return this;
    }
}

/** Hiển thị trạng thái / số khám dưới dạng nhãn bo tròn có màu theo ý nghĩa. */
final class BoVeNhan extends DefaultTableCellRenderer {
    private final boolean queueNumber;
    private Color cellBackground = Color.WHITE;
    private Color pillBackground = new Color(0xE2E8F0);
    private boolean pill;

    public BoVeNhan(boolean queueNumber) {
        this.queueNumber = queueNumber;
    }

    @Override public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focus, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, focus, row, column);
        setOpaque(false);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(font(Font.BOLD, 11));
        setBorder(new EmptyBorder(0, 20, 0, 10));
        String text = value == null ? "" : String.valueOf(value).trim();
        cellBackground = BangKeSoc.rowBackground(table, row, selected);
        pill = !text.isEmpty() && !(queueNumber && "—".equals(text));
        Color[] colors = pill ? badgeColors(text) : new Color[] {pillBackground, MUTED};
        pillBackground = colors[0];
        setForeground(pill ? colors[1] : MUTED);
        return this;
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(cellBackground);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (pill) {
            FontMetrics fm = getFontMetrics(getFont());
            int pillHeight = 24;
            g2.setColor(pillBackground);
            g2.fillRoundRect(10, (getHeight() - pillHeight) / 2,
                    fm.stringWidth(getText()) + 20, pillHeight, pillHeight, pillHeight);
        }
        g2.dispose();
        super.paintComponent(graphics);
    }

    /** Màu nhãn: xanh lá = hoàn tất/hoạt động, vàng = đang chờ, đỏ = hủy/khóa, xanh dương = còn lại. */
    private Color[] badgeColors(String text) {
        String t = text.toLowerCase();
        if (queueNumber) return new Color[] {PRIMARY_LIGHT, PRIMARY_DARK};
        if (t.contains("hủy") || t.contains("khóa") || t.contains("ngừng")) {
            return new Color[] {new Color(0xFEE2E2), new Color(0xB91C1C)};
        }
        if (t.contains("hoàn tất") || t.contains("hoạt động") || t.contains("đã xác nhận")
                || t.contains("đã khám") || t.contains("hoàn thành") || t.contains("đã thanh toán")) {
            return new Color[] {new Color(0xD1FAE5), new Color(0x047857)};
        }
        if (t.contains("chờ") || t.contains("đang") || t.contains("chưa")) {
            return new Color[] {new Color(0xFEF3C7), new Color(0xB45309)};
        }
        return new Color[] {new Color(0xE2E8F0), new Color(0x334155)};
    }
}

/** Ô hiển thị số tiền (BigDecimal): căn phải, có dấu chấm ngăn cách hàng nghìn, thêm "đ". */
final class BoVeTien extends DefaultTableCellRenderer {
    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private final Color color;

    public BoVeTien(Color color) {
        this.color = color;
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object value,
            boolean selected, boolean focus, int row, int col) {
        String text = value instanceof BigDecimal ? dinhDang((BigDecimal) value) : String.valueOf(value);
        super.getTableCellRendererComponent(t, text, selected, focus, row, col);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setFont(font(Font.BOLD, 12));
        setBorder(new EmptyBorder(0, 10, 0, 18));
        if (!selected) setForeground(color);
        return this;
    }

    /** 1234567 -> "1.234.567 đ". */
    public static String dinhDang(BigDecimal value) {
        return NumberFormat.getIntegerInstance(VI).format(value.setScale(0, RoundingMode.HALF_UP)) + " đ";
    }
}

/** Hộp thoại chi tiết một dòng của bảng: mỗi thuộc tính là một khối (nhãn nhỏ + giá trị), có cuộn dọc. */
final class HopThoaiChiTiet {

    private HopThoaiChiTiet() { }

    /**
     * @param model   model của bảng (cột 0 là STT nên bị bỏ qua)
     * @param row     chỉ số dòng theo model
     * @param titles  tên các thuộc tính, theo thứ tự cột từ cột 1
     * @param caption tiêu đề hộp thoại
     */
    public static void hien(Component parent, DefaultTableModel model, int row, String[] titles, String caption) {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < titles.length; i++) {
            Object raw = model.getValueAt(row, i + 1); // Bỏ cột STT.
            boolean empty = raw == null || String.valueOf(raw).isBlank();
            String text = empty ? "Chưa cập nhật" : String.valueOf(raw);

            JLabel name = label(titles[i].toUpperCase(), 10, false);
            name.setFont(font(Font.BOLD, 10));
            JLabel value = new JLabel("<html><body style='width:430px'>"
                    + escapeHtml(text).replace("\n", "<br>") + "</body></html>");
            value.setFont(font(empty ? Font.ITALIC : Font.PLAIN, 13));
            value.setForeground(empty ? new Color(0x94A3B8) : TEXT);

            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setBackground(i % 2 == 0 ? Color.WHITE : ROW_ALT);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(10, 18, 11, 18)));
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            value.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.add(name);
            item.add(Box.createVerticalStrut(4));
            item.add(value);
            gc.gridy = i;
            body.add(item, gc);
        }
        // Khối trống ở cuối để các thuộc tính dồn lên trên.
        gc.gridy = titles.length;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        body.add(filler, gc);

        JScrollPane view = new JScrollPane(body);
        view.setBorder(BorderFactory.createLineBorder(BORDER));
        view.getVerticalScrollBar().setUnitIncrement(18);
        view.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        view.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        view.setPreferredSize(new Dimension(520, 420));
        JOptionPane.showMessageDialog(parent, view, caption, JOptionPane.PLAIN_MESSAGE);
    }
}

/**
 * Ô vuông bo góc màu nhạt chứa biểu tượng vẽ bằng nét:
 * 0 người, 1 đồng hồ, 2 dấu tick, 3 phiếu, 4 thêm người, 5 cửa phòng.
 * Biểu tượng vẽ trên lưới 48x48 rồi co giãn theo cỡ yêu cầu.
 */
final class HuyHieuBieuTuong extends JComponent {
    private final int kind;
    private final Color accent;
    private final int size;

    public HuyHieuBieuTuong(int kind, Color accent) {
        this(kind, accent, 48);
    }

    public HuyHieuBieuTuong(int kind, Color accent, int size) {
        this.kind = kind;
        this.accent = accent;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double scale = size / 48.0;
        g2.scale(scale, scale);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
        g2.fillRoundRect(0, 0, 47, 47, 16, 16);
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case 0:
                g2.drawOval(18, 11, 12, 12);
                g2.drawArc(12, 27, 24, 18, 0, 180);
                break;
            case 1:
                g2.drawOval(12, 12, 24, 24);
                g2.drawLine(24, 24, 24, 17);
                g2.drawLine(24, 24, 30, 27);
                break;
            case 2:
                g2.drawOval(12, 12, 24, 24);
                g2.drawPolyline(new int[] {18, 22, 30}, new int[] {24, 28, 20}, 3);
                break;
            case 4:
                g2.drawOval(12, 12, 12, 12);
                g2.drawArc(7, 28, 22, 16, 0, 180);
                g2.drawLine(35, 15, 35, 25);
                g2.drawLine(30, 20, 40, 20);
                break;
            case 5:
                g2.drawRoundRect(14, 10, 20, 28, 5, 5);
                g2.drawLine(10, 38, 38, 38);
                g2.fillOval(27, 23, 3, 3);
                break;
            default:
                g2.drawRoundRect(15, 11, 18, 26, 5, 5);
                g2.drawLine(19, 19, 29, 19);
                g2.drawLine(19, 24, 29, 24);
                g2.drawLine(19, 29, 25, 29);
        }
        g2.dispose();
    }
}

/** Nhãn bo tròn (đếm số dòng, ngày hôm nay...) ở tiêu đề mỗi khu vực. */
final class NhanDem extends JLabel {

    public NhanDem(String text) {
        super(text, SwingConstants.CENTER);
        setFont(font(Font.BOLD, 11));
        setForeground(PRIMARY_DARK);
        setBorder(new EmptyBorder(3, 10, 3, 10));
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(PRIMARY_LIGHT);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(graphics);
    }
}
