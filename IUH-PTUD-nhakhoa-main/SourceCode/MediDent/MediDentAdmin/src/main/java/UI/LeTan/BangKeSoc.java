package UI.LeTan;

import static UI.LeTan.GiaoDien.ROW_ALT;
import static UI.LeTan.GiaoDien.ROW_HOVER;
import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Bảng kẻ sọc xen kẽ, làm nổi dòng khi rê chuột, có thông báo khi không có dữ liệu. */
public final class BangKeSoc extends JTable {
    int hoverRow = -1;

    public BangKeSoc(DefaultTableModel model) {
        super(model);
        MouseAdapter hover = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { setHover(rowAtPoint(e.getPoint())); }
            @Override public void mouseExited(MouseEvent e) { setHover(-1); }
        };
        addMouseMotionListener(hover);
        addMouseListener(hover);
    }

    private void setHover(int row) {
        if (row != hoverRow) {
            hoverRow = row;
            repaint();
        }
    }

    /** Màu nền dòng: chọn > rê chuột > kẻ sọc xen kẽ. */
    static Color rowBackground(JTable table, int row, boolean selected) {
        if (selected) return table.getSelectionBackground();
        if (table instanceof BangKeSoc && ((BangKeSoc) table).hoverRow == row) return ROW_HOVER;
        return row % 2 == 0 ? Color.WHITE : ROW_ALT;
    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JComponent && !(c instanceof BoVeNhan) && !(c instanceof BoVeAvatar)) {
            ((JComponent) c).setBorder(new EmptyBorder(0, 10, 0, 10));
        }
        if (!isRowSelected(row)) {
            c.setBackground(rowBackground(this, row, false));
        }
        return c;
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getRowCount() == 0) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            Rectangle visible = getVisibleRect();
            String message = "Không có dữ liệu để hiển thị";
            g2.setFont(font(Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(new Color(0x94A3B8));
            g2.drawString(message, visible.x + (visible.width - fm.stringWidth(message)) / 2,
                    visible.y + visible.height / 2);
            g2.dispose();
        }
    }
}
