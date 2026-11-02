package UI.LeTan;

import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

import javax.swing.JPanel;

/** Khung bo góc có màu nền và màu viền tùy chọn (khung Phòng khám, khung ghi chú). */
public final class HopTo extends JPanel {
    private final Color fill;
    private final Color edge;

    public HopTo(LayoutManager layout, Color fill, Color edge) {
        super(layout);
        this.fill = fill;
        this.edge = edge;
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.setColor(edge);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
        g2.dispose();
    }
}
