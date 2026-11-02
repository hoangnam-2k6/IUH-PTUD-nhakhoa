package UI.LeTan;

import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Banner gradient xanh với vài vòng tròn trang trí mờ phía phải. */
public final class BangTieuDe extends JPanel {

    public BangTieuDe() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(20, 26, 20, 26));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        g2.setPaint(new GradientPaint(0, 0, new Color(0x0369A1), w, h, new Color(0x0EA5E9)));
        g2.fillRoundRect(0, 0, w, h, 20, 20);
        g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
        g2.setColor(new Color(255, 255, 255, 22));
        g2.fillOval(w - 190, -70, 230, 230);
        g2.setColor(new Color(255, 255, 255, 16));
        g2.fillOval(w - 310, h - 70, 170, 170);
        g2.dispose();
    }
}
