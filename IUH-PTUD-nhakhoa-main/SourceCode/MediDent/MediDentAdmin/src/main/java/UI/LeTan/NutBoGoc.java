package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.PRIMARY_DARK;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/**
 * Nút bo góc, có trạng thái rê chuột / nhấn / vô hiệu.
 * Kiểu nút: PHU = nền trắng, CHINH = xanh, MO = mờ (dùng trên nền gradient).
 */
public final class NutBoGoc extends JButton {
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
