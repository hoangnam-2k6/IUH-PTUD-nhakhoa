package UI.LeTan;

import static UI.LeTan.GiaoDien.PRIMARY_DARK;
import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** Nhãn bo tròn (đếm số dòng, ngày hôm nay...) ở tiêu đề mỗi khu vực. */
public final class NhanDem extends JLabel {

    public NhanDem(String text) {
        super(text, SwingConstants.CENTER);
        setFont(font(Font.BOLD, 11));
        setForeground(PRIMARY_DARK);
        setBorder(new EmptyBorder(3, 10, 3, 10));
        setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        g2.setColor(PRIMARY_LIGHT);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(graphics);
    }
}
