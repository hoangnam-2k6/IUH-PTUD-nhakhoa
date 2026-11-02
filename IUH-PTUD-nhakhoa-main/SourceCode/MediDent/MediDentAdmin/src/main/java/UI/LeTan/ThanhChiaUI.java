package UI.LeTan;

import static UI.LeTan.GiaoDien.BACKGROUND;

import java.awt.Graphics;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/** Thanh chia hai khu vực: chỉ là khoảng trống cùng màu nền, không viền, không nút mũi tên. */
public final class ThanhChiaUI extends BasicSplitPaneUI {
    @Override public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
            @Override public void paint(Graphics graphics) {
                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
}
