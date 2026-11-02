package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/** Thẻ trắng bo góc, viền mảnh và bóng đổ mềm nhiều lớp. */
public class KhungBoGoc extends JPanel {

    public KhungBoGoc(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        for (int k = 3; k >= 1; k--) {
            g2.setColor(new Color(15, 23, 42, 8));
            g2.fillRoundRect(3 - k, k, w - 7 + 2 * k, h - 7 + k, 20, 20);
        }
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(3, 0, w - 7, h - 7, 18, 18);
        g2.setColor(BORDER);
        g2.drawRoundRect(3, 0, w - 7, h - 7, 18, 18);
        g2.dispose();
    }
}
