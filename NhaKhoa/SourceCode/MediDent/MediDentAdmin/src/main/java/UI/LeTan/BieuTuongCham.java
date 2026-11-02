package UI.LeTan;

import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/** Chấm tròn nhỏ đứng trước dòng trạng thái / tiêu đề nhóm. */
public final class BieuTuongCham implements Icon {
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
