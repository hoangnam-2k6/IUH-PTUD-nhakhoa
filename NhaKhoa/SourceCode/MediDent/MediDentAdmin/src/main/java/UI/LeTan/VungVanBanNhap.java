package UI.LeTan;

import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY;
import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JTextArea;

/** Vùng nhập nhiều dòng (tiền sử bệnh) có chữ gợi ý mờ khi trống. */
public final class VungVanBanNhap extends JTextArea {
    private final String prompt;

    public VungVanBanNhap(String prompt) {
        this.prompt = prompt;
        setFont(font(Font.PLAIN, 13));
        setForeground(TEXT);
        setDisabledTextColor(MUTED);
        setCaretColor(PRIMARY);
        setSelectionColor(new Color(0xBAE6FD));
        setLineWrap(true);
        setWrapStyleWord(true);
        setBorder(null);
    }

    @Override public void setEnabled(boolean on) {
        super.setEnabled(on);
        setBackground(on ? Color.WHITE : ROW_ALT);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getText().isEmpty()) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setFont(getFont());
            g2.setColor(new Color(0x94A3B8));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(prompt, getInsets().left, getInsets().top + fm.getAscent());
            g2.dispose();
        }
    }
}
