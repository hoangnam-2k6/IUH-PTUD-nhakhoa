package UI.LeTan;

import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/** Biểu tượng chữ "i" trong vòng tròn, đứng trước dòng ghi chú. */
public final class BieuTuongThongTin implements Icon {

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
