package UI.LeTan;

import static UI.LeTan.GiaoDien.AVATAR_COLORS;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.TEXT;
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

/** Tên kèm avatar tròn có chữ cái đầu, màu theo tên. */
public final class BoVeAvatar extends DefaultTableCellRenderer {
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
