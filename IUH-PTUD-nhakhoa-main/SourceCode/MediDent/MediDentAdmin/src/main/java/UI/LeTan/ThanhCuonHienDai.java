package UI.LeTan;

import static UI.LeTan.GiaoDien.datKichThuoc;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Thanh cuộn mảnh, bo tròn, không có nút mũi tên. */
public final class ThanhCuonHienDai extends BasicScrollBarUI {

    @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
    @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

    private static JButton zeroButton() {
        JButton button = new JButton();
        datKichThuoc(button, 0, 0);
        return button;
    }

    @Override protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
        g.setColor(Color.WHITE);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
        if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        khuRangCua(g2);
        g2.setColor(isThumbRollover() || isDragging ? new Color(0x94A3B8) : new Color(0xCBD5E1));
        g2.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 8, 8);
        g2.dispose();
    }
}
