package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
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

import javax.swing.JTextField;

/** Ô tìm kiếm bo tròn có nền xám nhạt, biểu tượng kính lúp và chữ gợi ý; viền đổi màu khi focus. */
public final class OTimKiem extends JTextField {
    private String prompt;

    public OTimKiem(String prompt) {
        this.prompt = prompt;
        setToolTipText(prompt);
        setFont(font(Font.PLAIN, 12));
        setForeground(TEXT);
        setOpaque(false);
        setPreferredSize(new Dimension(200, 40));
        setBorder(new VienBo(BORDER, 1.2f));
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                setBorder(new VienBo(PRIMARY, 1.8f));
                repaint();
            }
            @Override public void focusLost(FocusEvent event) {
                setBorder(new VienBo(BORDER, 1.2f));
                repaint();
            }
        });
    }

    public void setPrompt(String value) {
        prompt = value;
        setToolTipText(value);
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        // Nền bo tròn.
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(hasFocus() ? Color.WHITE : ROW_ALT);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(graphics);

        // Kính lúp và chữ gợi ý (chỉ hiện khi ô trống và chưa focus).
        g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        int cy = getHeight() / 2;
        g2.setColor(hasFocus() ? PRIMARY : MUTED);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(13, cy - 7, 9, 9);
        g2.drawLine(21, cy + 1, 25, cy + 5);
        if (getText().isEmpty() && !hasFocus()) {
            g2.setColor(new Color(0x94A3B8));
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prompt, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        }
        g2.dispose();
    }
}
