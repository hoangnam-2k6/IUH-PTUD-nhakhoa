package UI.LeTan;

import java.awt.BasicStroke;

import java.awt.Color;

import java.awt.Component;

import java.awt.Cursor;

import java.awt.Dimension;

import java.awt.Font;

import java.awt.FontMetrics;

import java.awt.GradientPaint;

import java.awt.Graphics2D;

import java.awt.Graphics2D;


import java.awt.Graphics;

import java.awt.Insets;

import java.awt.LayoutManager;


import java.awt.Rectangle;


import java.awt.geom.RoundRectangle2D;


import javax.swing.JButton;

import javax.swing.JComponent;

import javax.swing.JPanel;

import javax.swing.JPanel;


import javax.swing.Scrollable;


import javax.swing.border.AbstractBorder;


import javax.swing.border.EmptyBorder;


import javax.swing.plaf.basic.BasicScrollBarUI;


import static UI.LeTan.GiaoDien.BORDER;

import static UI.LeTan.GiaoDien.PRIMARY;

import static UI.LeTan.GiaoDien.PRIMARY_DARK;

import static UI.LeTan.GiaoDien.TEXT;

import static UI.LeTan.GiaoDien.datKichThuoc;

import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.khuRangCua;



/** Khung bo góc có màu nền và màu viền tùy chọn (khung Phòng khám, khung ghi chú). */
final class HopTo extends JPanel {
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

/** Thẻ trắng bo góc, viền mảnh và bóng đổ mềm nhiều lớp. */
class KhungBoGoc extends JPanel {

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

/** Khung chứa form: rộng đúng bằng khung nhìn (không cuộn ngang), cao theo nội dung để cuộn dọc. */
final class KhungCuon extends JPanel implements Scrollable {
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 18; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 72; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}

/**
 * Nút bo góc, có trạng thái rê chuột / nhấn / vô hiệu.
 * Kiểu nút: PHU = nền trắng, CHINH = xanh, MO = mờ (dùng trên nền gradient).
 */
final class NutBoGoc extends JButton {
    public static final int PHU = 0;
    public static final int CHINH = 1;
    public static final int MO = 2;

    private final int kind;

    public NutBoGoc(String text, int kind) {
        super(text);
        this.kind = kind;
        setFont(font(Font.BOLD, 12));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        boolean on = isEnabled();
        boolean hover = on && getModel().isRollover();
        boolean pressed = on && getModel().isPressed();

        Color fill;
        Color edge;
        Color textColor;
        if (kind == CHINH) {
            fill = !on ? new Color(0x94A3B8) : pressed ? new Color(0x075985)
                    : hover ? PRIMARY_DARK : PRIMARY;
            edge = fill;
            textColor = Color.WHITE;
        } else if (kind == MO) {
            fill = new Color(255, 255, 255, !on ? 25 : pressed ? 95 : hover ? 70 : 42);
            edge = new Color(255, 255, 255, on ? 130 : 60);
            textColor = on ? Color.WHITE : new Color(255, 255, 255, 140);
        } else {
            fill = !on ? new Color(0xF1F5F9) : pressed ? new Color(0xE2E8F0)
                    : hover ? new Color(0xF1F5F9) : Color.WHITE;
            edge = hover || pressed ? new Color(0xCBD5E1) : BORDER;
            textColor = on ? TEXT : new Color(0x94A3B8);
        }
        if (kind == CHINH && on) {
            Color top = pressed ? new Color(0x075985) : hover ? new Color(0x0284C7) : new Color(0x0EA5E9);
            Color bottom = pressed ? new Color(0x075985) : hover ? new Color(0x0369A1) : new Color(0x0284C7);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        } else {
            g2.setColor(fill);
        }
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
        g2.setColor(edge);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(textColor);
        g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                (h - fm.getHeight()) / 2 + fm.getAscent());
        g2.dispose();
    }
}

/** Thanh cuộn mảnh, bo tròn, không có nút mũi tên. */
final class ThanhCuonHienDai extends BasicScrollBarUI {

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

/** Viền bo góc cho ô tìm kiếm (chừa chỗ bên trái cho biểu tượng kính lúp). */
final class VienBo extends AbstractBorder {
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
