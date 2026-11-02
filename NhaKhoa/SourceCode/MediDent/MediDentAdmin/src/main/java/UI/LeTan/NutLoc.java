package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;
import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.datKichThuoc;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JButton;

/** Nút lọc nhỏ: biểu tượng phễu, sáng lên khi đang lọc theo một trường cụ thể. */
public final class NutLoc extends JButton {
    private boolean active;

    public NutLoc() {
        datKichThuoc(this, 32, 32);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setActive(boolean value) {
        active = value;
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        boolean hover = getModel().isRollover() || getModel().isPressed();
        g2.setColor(active ? PRIMARY_LIGHT : hover ? new Color(0xF1F5F9) : ROW_ALT);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
        g2.setColor(active ? PRIMARY : hover ? new Color(0xCBD5E1) : BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Vẽ hình cái phễu ở giữa nút.
        float cx = w / 2f;
        float cy = h / 2f;
        Path2D funnel = new Path2D.Float();
        funnel.moveTo(cx - 6.5f, cy - 5.5f);
        funnel.lineTo(cx + 6.5f, cy - 5.5f);
        funnel.lineTo(cx + 2f, cy);
        funnel.lineTo(cx + 2f, cy + 5.5f);
        funnel.lineTo(cx - 2f, cy + 3.5f);
        funnel.lineTo(cx - 2f, cy);
        funnel.closePath();
        g2.setColor(active ? PRIMARY : MUTED);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(funnel);
        g2.dispose();
    }
}
