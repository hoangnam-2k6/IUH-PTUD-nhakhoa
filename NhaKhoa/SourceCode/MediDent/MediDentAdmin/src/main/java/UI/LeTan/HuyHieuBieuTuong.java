package UI.LeTan;

import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Ô vuông bo góc màu nhạt chứa biểu tượng vẽ bằng nét:
 * 0 người, 1 đồng hồ, 2 dấu tick, 3 phiếu, 4 thêm người, 5 cửa phòng.
 * Biểu tượng vẽ trên lưới 48x48 rồi co giãn theo cỡ yêu cầu.
 */
public final class HuyHieuBieuTuong extends JComponent {
    private final int kind;
    private final Color accent;
    private final int size;

    public HuyHieuBieuTuong(int kind, Color accent) {
        this(kind, accent, 48);
    }

    public HuyHieuBieuTuong(int kind, Color accent, int size) {
        this.kind = kind;
        this.accent = accent;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double scale = size / 48.0;
        g2.scale(scale, scale);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
        g2.fillRoundRect(0, 0, 47, 47, 16, 16);
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (kind) {
            case 0:
                g2.drawOval(18, 11, 12, 12);
                g2.drawArc(12, 27, 24, 18, 0, 180);
                break;
            case 1:
                g2.drawOval(12, 12, 24, 24);
                g2.drawLine(24, 24, 24, 17);
                g2.drawLine(24, 24, 30, 27);
                break;
            case 2:
                g2.drawOval(12, 12, 24, 24);
                g2.drawPolyline(new int[] {18, 22, 30}, new int[] {24, 28, 20}, 3);
                break;
            case 4:
                g2.drawOval(12, 12, 12, 12);
                g2.drawArc(7, 28, 22, 16, 0, 180);
                g2.drawLine(35, 15, 35, 25);
                g2.drawLine(30, 20, 40, 20);
                break;
            case 5:
                g2.drawRoundRect(14, 10, 20, 28, 5, 5);
                g2.drawLine(10, 38, 38, 38);
                g2.fillOval(27, 23, 3, 3);
                break;
            default:
                g2.drawRoundRect(15, 11, 18, 26, 5, 5);
                g2.drawLine(19, 19, 29, 19);
                g2.drawLine(19, 24, 29, 24);
                g2.drawLine(19, 29, 25, 29);
        }
        g2.dispose();
    }
}
