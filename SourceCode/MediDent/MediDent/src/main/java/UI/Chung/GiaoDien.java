/*
 * GiaoDien: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
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

public final class GiaoDien {

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

    /** Ngăn tạo lớp tiện ích tập trung các phương thức dựng giao diện Swing. */
    private GiaoDien() {

    }

    /** Tạo nhãn Swing theo nội dung và định dạng chữ, không phụ thuộc dữ liệu SQL. */
    public static JLabel label(String text, int fontSize, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        result.setForeground(bold ? TEXT : MUTED);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        return result;
    }

    /** Tạo nút Swing với màu sắc và hành vi hiển thị chính/phụ có sẵn. */
    public static JButton nut(String title, boolean primary) {

        JButton button = new JButton(title) {
            @Override protected void paintComponent(Graphics source) {
                Graphics2D g = (Graphics2D) source.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (getModel().isPressed()) bg = primary ? HOVER : new Color(0xE2E8F0);
                g.setColor(bg);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 11, 11);
                g.setColor(primary ? bg : BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 11, 11);
                g.dispose();
                super.paintComponent(source);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(primary ? PRIMARY : WHITE);
        button.setForeground(primary ? WHITE : TEXT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                button.putClientProperty("MEDIDENT_HOVER_BASE", button.getBackground());
                button.setBackground(primary ? HOVER : new Color(0xF1F5F9));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                Object original = button.getClientProperty("MEDIDENT_HOVER_BASE");
                button.setBackground(original instanceof Color ? (Color) original :
                        (primary ? PRIMARY : WHITE));
            }
        });
        return button;
    }

    /** Gắn biểu tượng vào nút Swing được tạo theo cùng theme. */
    public static JButton nutIcon(String title, boolean primary, BieuTuong.Loai loai) {
        JButton button = nut(title, primary);
        button.setIcon(new BieuTuong(loai, primary ? WHITE : MUTED, 17));
        button.setIconTextGap(9);
        return button;
    }

    /** Tạo khung thẻ với nền trắng và viền như thiết kế ban đầu. */
    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            /** Vẽ nội dung đồ họa của thành phần Swing. */
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
            /** Trả kích thước tối đa phục vụ bố cục Swing hiện có. */
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

    private static final class CotNoiDung extends JPanel implements Scrollable {
        CotNoiDung() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        /** Căn lề con khi thêm một thành phần vào cột giao diện dùng chung. */
        protected void addImpl(Component child, Object constraints, int index) {
            if (child instanceof JComponent) {
                ((JComponent) child).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            super.addImpl(child, constraints, index);
        }

        @Override
        /** Cung cấp kích thước hiển thị vùng cuộn. */
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(750, 560);
        }

        @Override
        /** Xác định bước cuộn nhỏ khi người dùng cuộn nội dung. */
        public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) {
            return 18;
        }

        @Override
        /** Xác định bước cuộn theo trang của vùng hiển thị. */
        public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
            return Math.max(40, rect.height - 50);
        }

        @Override
        /** Cho vùng cuộn biết có bám theo chiều rộng viewport hay không. */
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        /** Cho vùng cuộn biết có bám theo chiều cao viewport hay không. */
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Tạo vùng trang gồm tiêu đề và mô tả theo mẫu hiển thị chung. */
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

    /** Bọc vùng nội dung trong JScrollPane để cuộn khi không đủ chiều cao. */
    public static JScrollPane cuon(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getVerticalScrollBar().setBlockIncrement(90);
        return scroll;
    }

    /** Đọc logo trong thư mục resources và trả ImageIcon đã co giãn. */
    public static ImageIcon logo(int kichThuoc) {
        return logo(kichThuoc, kichThuoc);
    }

    /** Đọc logo trong thư mục resources và trả ImageIcon đã co giãn. */
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
                "/Image/logo(1024x884).png",
                "/Image/logo.png",
                "/Image/logo_ai.png"
            };

            for (String path : tenAnh) {
                URL url = GiaoDien.class.getResource(path);
                if (url == null) {
                    continue;
                }
                try {
                    BufferedImage source = ImageIO.read(url);
                    if (source == null) {
                        continue;
                    }

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

    /** Hiển thị thông báo lỗi nghiệp vụ hoặc kết nối SQL cho người dùng. */
    public static void loi(Component owner, Exception exception) {
        exception.printStackTrace();
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.toString();
        }
        JOptionPane.showMessageDialog(owner, message,
                exception instanceof SQLException ? "Lỗi SQL Server" : "Lỗi ứng dụng",
                JOptionPane.ERROR_MESSAGE);
    }

    /** Tạo ô nhập văn bản với phông và kích thước thống nhất. */
    public static JTextField text(String value) {
        JTextField field = new JTextField(value, 23);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(9, 10, 9, 10)));
        return field;
    }

    /** Tạo hàng nút/nhãn có FlowLayout căn trái. */
    public static JPanel hang() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override
            /** Trả kích thước tối đa phục vụ bố cục Swing hiện có. */
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /** Đánh số thứ tự hiển thị của JTable độc lập với mã khóa chính. */
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
            /** Tạo ô hiển thị bảng theo giá trị và trạng thái chọn. */
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int columnIndex) {
                JLabel result = (JLabel) super.getTableCellRendererComponent(
                        source, row + 1, selected, focused, row, columnIndex);
                result.setHorizontalAlignment(SwingConstants.CENTER);
                return result;
            }
        });
    }

    /** Định dạng JTable và bọc bảng trong vùng cuộn để giữ kích thước trang. */
    public static JComponent wrapTable(JTable table) {
        table.setRowHeight(39);
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
