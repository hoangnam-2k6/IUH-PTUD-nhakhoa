package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.DANGER;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JTextField;

/**
 * Ô nhập bo tròn của form tiếp nhận:
 * chữ gợi ý mờ khi trống, vòng sáng xanh khi focus, viền + nền đỏ nhạt khi nhập sai.
 */
public final class TruongNhap extends JTextField {
    private final String prompt;
    private boolean focused;
    private boolean loi;

    public TruongNhap() {
        this("");
    }

    public TruongNhap(String prompt) {
        this.prompt = prompt;
        setFont(font(Font.PLAIN, 13));
        setForeground(TEXT);
        setDisabledTextColor(MUTED);
        setCaretColor(PRIMARY);
        setSelectionColor(new Color(0xBAE6FD));
        setOpaque(false);
        setPreferredSize(new Dimension(120, 38));
        setBorder(new VienNhap(BORDER, 1.2f));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                focused = true;
                capNhatVien();
            }
            @Override public void focusLost(FocusEvent event) {
                focused = false;
                capNhatVien();
            }
        });
        // Bắt đầu gõ lại thì bỏ trạng thái lỗi.
        getDocument().addDocumentListener(new BoLangNgheTimKiem(() -> {
            if (loi) datLoi(false);
        }));
    }

    /** Bật / tắt trạng thái nhập sai (viền đỏ). */
    public void datLoi(boolean value) {
        loi = value;
        capNhatVien();
    }

    private void capNhatVien() {
        if (loi) setBorder(new VienNhap(DANGER, 1.8f));
        else if (focused) setBorder(new VienNhap(PRIMARY, 1.8f));
        else setBorder(new VienNhap(BORDER, 1.2f));
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(loi ? new Color(0xFEF2F2) : isEnabled() ? Color.WHITE : ROW_ALT);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
        if (focused && !loi) {
            g2.setColor(new Color(2, 132, 199, 38));
            g2.setStroke(new BasicStroke(3f));
            g2.draw(new RoundRectangle2D.Float(2.5f, 2.5f, w - 6, h - 6, 10, 10));
        }
        g2.dispose();
        super.paintComponent(graphics);

        if (getText().isEmpty() && !prompt.isEmpty()) {
            g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setFont(getFont());
            g2.setColor(new Color(0x94A3B8));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prompt, getInsets().left, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}
