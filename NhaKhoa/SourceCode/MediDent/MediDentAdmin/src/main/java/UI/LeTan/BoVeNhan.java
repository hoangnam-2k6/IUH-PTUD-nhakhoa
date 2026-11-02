package UI.LeTan;

import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY_DARK;
import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Hiển thị trạng thái / số khám dưới dạng nhãn bo tròn có màu theo ý nghĩa. */
public final class BoVeNhan extends DefaultTableCellRenderer {
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
