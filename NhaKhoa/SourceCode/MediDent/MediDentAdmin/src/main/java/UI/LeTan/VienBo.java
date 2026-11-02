package UI.LeTan;

import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

/** Viền bo góc cho ô tìm kiếm (chừa chỗ bên trái cho biểu tượng kính lúp). */
public final class VienBo extends AbstractBorder {
    private final Color color;
    private final float thickness;

    public VienBo(Color color, float thickness) {
        this.color = color;
        this.thickness = thickness;
    }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        khuRangCua(g2);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(thickness));
        float o = thickness / 2f;
        g2.draw(new RoundRectangle2D.Float(x + o, y + o, w - thickness, h - thickness, 12, 12));
        g2.dispose();
    }

    @Override public Insets getBorderInsets(Component c) {
        return new Insets(7, 36, 7, 12);
    }
}
