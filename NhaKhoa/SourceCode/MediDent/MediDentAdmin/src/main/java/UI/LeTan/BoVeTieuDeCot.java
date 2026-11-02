package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.font;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Tiêu đề cột: nền xám nhạt, chữ nhỏ đậm, kẻ chân. STT / Mã phiếu / Mã BN / Mã NV căn giữa. */
public final class BoVeTieuDeCot extends DefaultTableCellRenderer {
    @Override public Component getTableCellRendererComponent(JTable table, Object value,
            boolean selected, boolean focus, int row, int column) {
        super.getTableCellRendererComponent(table, value, false, false, row, column);
        setBackground(new Color(0xF1F5F9));
        setForeground(MUTED);
        setFont(font(Font.BOLD, 11));
        String name = table.getColumnName(column);
        boolean center = column == 0 || "Mã phiếu".equals(name)
                || "Mã BN".equals(name) || "Mã NV".equals(name);
        setHorizontalAlignment(center ? SwingConstants.CENTER : SwingConstants.LEFT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(0, 10, 0, 10)));
        setPreferredSize(null);
        setPreferredSize(new Dimension(getPreferredSize().width, 40));
        return this;
    }
}
