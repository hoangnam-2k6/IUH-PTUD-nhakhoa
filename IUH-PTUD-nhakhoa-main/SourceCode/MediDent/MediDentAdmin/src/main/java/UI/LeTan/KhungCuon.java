package UI.LeTan;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/** Khung chứa form: rộng đúng bằng khung nhìn (không cuộn ngang), cao theo nội dung để cuộn dọc. */
public final class KhungCuon extends JPanel implements Scrollable {
    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 18; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 72; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
