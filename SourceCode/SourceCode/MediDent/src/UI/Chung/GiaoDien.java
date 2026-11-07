package UI.Chung;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import java.awt.event.MouseWheelListener;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public final class GiaoDien {

    public static final Color BLUE = new Color(0x0284C7);
    public static final Color BLUE_DARK = new Color(0x0369A1);
    public static final Color BLUE_LIGHT = new Color(0xE0F2FE);
    public static final Color BLUE_SOFT = new Color(0xF0F9FF);
    public static final Color DARK = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color BG = new Color(0xF8FAFC);
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(0xD9E2EC);
    public static final Color BORDER_STRONG = new Color(0xCBD5E1);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color GREEN_SOFT = new Color(0xECFDF5);
    public static final Color RED = new Color(0xEF4444);
    public static final Color RED_SOFT = new Color(0xFEF2F2);
    public static final Color AMBER = new Color(0xF59E0B);
    public static final Color AMBER_SOFT = new Color(0xFFFBEB);
    public static final Color ROW_ALT = new Color(0xFAFCFF);
    // Tông xanh khói nhẹ cho thanh điều hướng, tách biệt vùng làm việc trắng.
    public static final Color NAV_BG = new Color(0xEDF5FA);
    public static final Color NAV_ITEM = new Color(0xF8FBFE);
    public static final Color NAV_ACTIVE = new Color(0xD8EDF9);
    public static final Color NAV_DIVIDER = new Color(0xD4E4EF);

    private GiaoDien() {
    }

    public static void setup() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 12));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 12));
    }

    public static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(bold ? DARK : MUTED);
        return label;
    }

    public static javax.swing.JPanel white(java.awt.LayoutManager layout) {
        javax.swing.JPanel panel = new javax.swing.JPanel(layout);
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    /**
     * Tự vẽ nền nút thay vì giao cho Windows Look & Feel.
     * Cách này tránh lỗi chữ trắng trên nút trắng ở máy người dùng.
     */
    public static JButton button(String name, boolean primary) {
        JButton button = new JButton(name) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base;
                if (!isEnabled()) {
                    base = new Color(0xE2E8F0);
                } else if (primary) {
                    base = getModel().isRollover() ? new Color(0x075985) : BLUE_DARK;
                } else {
                    base = getModel().isRollover() ? BLUE_SOFT : WHITE;
                }
                g.setColor(base);
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g.setColor(primary && isEnabled() ? BLUE_DARK : BORDER_STRONG);
                g.setStroke(new BasicStroke(1));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(primary ? WHITE : DARK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 16, 11, 16));
        button.setPreferredSize(new Dimension(Math.max(108, button.getPreferredSize().width + 12), 42));
        button.setMinimumSize(new Dimension(108, 42));
        return button;
    }

    public static JButton menuButton(String name, Icon icon) {
        JButton button = new JButton(name, icon) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = getBackground();
                g.setColor(background);
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                boolean selected = background.equals(NAV_ACTIVE);
                g.setColor(selected ? BLUE : NAV_DIVIDER);
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                if (selected) {
                    g.setColor(BLUE);
                    g.fillRoundRect(1, 4, 4, getHeight() - 8, 3, 3);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(MUTED);
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setBackground(NAV_ITEM);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(13, 16, 13, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setPreferredSize(new Dimension(270, 52));
        return button;
    }

    /**
     * Ẩn thanh cuộn ngang nhưng vẫn giữ khả năng cuộn bằng touchpad.
     * Trên một số máy cần giữ Shift rồi cuộn hai ngón hoặc cuộn con lăn.
     */
    public static void hỗTrợVuốtNgang(JScrollPane khung) {
        khung.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        khung.getHorizontalScrollBar().setUnitIncrement(40);
        MouseWheelListener vuốt = e -> {
            JScrollBar ngang = khung.getHorizontalScrollBar();
            boolean cóNgang = ngang.getMaximum() > ngang.getVisibleAmount();
            boolean cóDọc = khung.getVerticalScrollBar().getMaximum() > khung.getVerticalScrollBar().getVisibleAmount();
            if (cóNgang && (e.isShiftDown() || !cóDọc)) {
                int bước = (int) Math.round(e.getPreciseWheelRotation() * 45);
                ngang.setValue(ngang.getValue() + bước);
                e.consume();
            }
        };
        // Không gắn listener vào JTable: giữ nguyên cuộn dọc của JScrollPane.
        khung.addMouseWheelListener(vuốt);
    }

    public static void styleInput(JComponent component) {
        component.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER_STRONG, 1, true),
                new EmptyBorder(8, 10, 8, 10)));
        component.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        component.setBackground(WHITE);
        component.setForeground(DARK);
    }

    public static void styleTextField(JTextField field, int columns) {
        field.setColumns(columns);
        field.setPreferredSize(new Dimension(Math.max(180, columns * 11), 38));
        styleInput(field);
    }

    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(36);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(BLUE_LIGHT);
        table.setSelectionForeground(DARK);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(BLUE_SOFT);
        header.setForeground(DARK);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String columnName = table.getColumnName(column);
                boolean important = isImportantColumn(columnName);

                setBorder(new EmptyBorder(0, 10, 0, 10));
                setForeground(DARK);
                setFont(new Font("Segoe UI", important ? Font.BOLD : Font.PLAIN, 12));

                if (isSelected) {
                    setBackground(BLUE_LIGHT);
                } else if (columnName.equals("Trạng thái")) {
                    Color background = statusBackground(String.valueOf(value));
                    setBackground(background);
                    setForeground(statusForeground(String.valueOf(value)));
                } else {
                    setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                }

                if (isMoneyColumn(columnName) && value instanceof Number number) {
                    NumberFormat money = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
                    setText(money.format(number) + " ₫");
                } else if (columnName.contains("Ngày") && value != null) {
                    setText(displayDate(String.valueOf(value)));
                }

                if (isCenterAligned(columnName)) {
                    setHorizontalAlignment(CENTER);
                } else if (isRightAligned(columnName, value)) {
                    setHorizontalAlignment(RIGHT);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return this;
            }
        };

        table.setDefaultRenderer(Object.class, renderer);
    }

    private static boolean isImportantColumn(String columnName) {
        return columnName.contains("Họ tên")
                || columnName.contains("Tên")
                || columnName.contains("Bệnh nhân")
                || columnName.contains("Bác sĩ")
                || columnName.contains("Trạng thái")
                || columnName.contains("Số tiền")
                || columnName.contains("Đơn giá")
                || columnName.contains("Ngày hẹn");
    }

    private static boolean isCenterAligned(String columnName) {
        return columnName.equals("ID")
                || columnName.contains("Giới tính")
                || columnName.contains("Tuổi")
                || columnName.contains("Giờ")
                || columnName.contains("Phút")
                || columnName.contains("Tháng")
                || columnName.contains("Giao dịch")
                || columnName.contains("Lượt");
    }

    private static boolean isMoneyColumn(String name) {
        return name.contains("Số tiền") || name.contains("Đơn giá")
                || name.contains("Doanh thu") || name.contains("Chi phí")
                || name.contains("Thu tháng");
    }

    private static boolean isRightAligned(String columnName, Object value) {
        return isMoneyColumn(columnName);
    }

    private static Color statusBackground(String status) {
        if (status.contains("Đã hủy") || status.contains("Nghỉ việc")
                || status.contains("Ngừng") || status.contains("Từ chối")) {
            return RED_SOFT;
        }
        if (status.contains("Chờ") || status.contains("Tạm")) {
            return AMBER_SOFT;
        }
        if (status.contains("Đã thanh toán") || status.contains("Hoàn tất")
                || status.contains("Đang làm việc") || status.contains("Hoạt động")
                || status.contains("Đang áp dụng")) {
            return GREEN_SOFT;
        }
        return BLUE_SOFT;
    }

    private static Color statusForeground(String status) {
        if (status.contains("Đã hủy") || status.contains("Nghỉ việc")
                || status.contains("Ngừng") || status.contains("Từ chối")) {
            return new Color(0x991B1B);
        }
        if (status.contains("Chờ") || status.contains("Tạm")) {
            return new Color(0x92400E);
        }
        if (status.contains("Đã thanh toán") || status.contains("Hoàn tất")
                || status.contains("Đang làm việc") || status.contains("Hoạt động")
                || status.contains("Đang áp dụng")) {
            return new Color(0x166534);
        }
        return BLUE_DARK;
    }

    private static String displayDate(String text) {
        try {
            String iso = text.length() >= 10 ? text.substring(0, 10) : text;
            LocalDate date = LocalDate.parse(iso);
            String formatted = String.format("%02d/%02d/%04d", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
            return text.length() > 10 ? formatted + text.substring(10) : formatted;
        } catch (RuntimeException ignored) {
            return text;
        }
    }

    public static void error(Component parent, Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root != root.getCause()) {
            root = root.getCause();
        }

        JOptionPane.showMessageDialog(
                parent,
                root.getMessage() == null ? "Lỗi không xác định" : root.getMessage(),
                "MediDent - Lỗi",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void ok(Component parent, String text) {
        JOptionPane.showMessageDialog(parent, text, "MediDent", JOptionPane.INFORMATION_MESSAGE);
    }

    public static Icon icon(String kind, int size) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D graphics = (Graphics2D) g.create();
                graphics.translate(x, y);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setStroke(new BasicStroke(Math.max(1.8f, size / 13f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.setColor(BLUE);

                double scale = size / 24.0;
                graphics.scale(scale, scale);

                switch (kind) {
                    case "home" -> {
                        graphics.drawRoundRect(3, 10, 18, 12, 2, 2);
                        Path2D.Double roof = new Path2D.Double();
                        roof.moveTo(2, 11);
                        roof.lineTo(12, 2);
                        roof.lineTo(22, 11);
                        graphics.draw(roof);
                        graphics.drawRect(10, 15, 4, 7);
                    }
                    case "staff" -> {
                        graphics.drawOval(8, 3, 8, 8);
                        graphics.drawArc(3, 13, 18, 12, 0, 180);
                        graphics.drawOval(2, 8, 4, 4);
                        graphics.drawOval(18, 8, 4, 4);
                    }
                    case "tooth" -> {
                        Path2D.Double tooth = new Path2D.Double();
                        tooth.moveTo(7, 3);
                        tooth.curveTo(-1, 0, 1, 14, 6, 21);
                        tooth.curveTo(8, 24, 9, 14, 12, 14);
                        tooth.curveTo(15, 14, 16, 24, 18, 21);
                        tooth.curveTo(23, 11, 24, 0, 16, 3);
                        tooth.curveTo(12, 5, 11, 5, 7, 3);
                        graphics.draw(tooth);
                    }
                    case "patient" -> {
                        graphics.drawOval(8, 2, 8, 8);
                        graphics.drawRoundRect(4, 13, 16, 9, 3, 3);
                        graphics.drawLine(16, 17, 20, 17);
                        graphics.drawLine(18, 15, 18, 19);
                    }
                    case "calendar" -> {
                        graphics.drawRoundRect(3, 5, 18, 17, 2, 2);
                        graphics.drawLine(3, 10, 21, 10);
                        graphics.drawLine(8, 2, 8, 8);
                        graphics.drawLine(16, 2, 16, 8);
                        graphics.drawLine(8, 15, 12, 19);
                        graphics.drawLine(12, 19, 18, 13);
                    }
                    case "bill" -> {
                        graphics.drawRoundRect(5, 2, 14, 21, 2, 2);
                        graphics.drawLine(8, 8, 16, 8);
                        graphics.drawLine(8, 12, 16, 12);
                        graphics.drawLine(8, 16, 13, 16);
                    }
                    case "chart" -> {
                        graphics.drawLine(3, 21, 22, 21);
                        graphics.drawLine(3, 21, 3, 3);
                        graphics.drawRect(6, 13, 3, 8);
                        graphics.drawRect(12, 8, 3, 13);
                        graphics.drawRect(18, 4, 3, 17);
                    }
                    default -> {
                        graphics.drawRoundRect(4, 4, 16, 16, 3, 3);
                        graphics.drawLine(8, 12, 16, 12);
                        graphics.drawLine(12, 8, 12, 16);
                    }
                }
                graphics.dispose();
            }
        };
    }

    public static Icon logo(int size) {
        try {
            java.net.URL url = GiaoDien.class.getResource("/Image/medident_logo.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return icon("tooth", size);
    }

    public static String display(Object value) {
        return value == null ? "—" : String.valueOf(value);
    }
}
