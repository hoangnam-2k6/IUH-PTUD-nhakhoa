package UI.LeTan;

import static UI.LeTan.GiaoDien.font;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/** Ô căn giữa với màu chữ riêng (dùng cho cột STT, cột mã, Mã BN, Mã NV). */
public final class BoVeCanGiua extends DefaultTableCellRenderer {
    private final Color color;
    private final boolean bold;

    public BoVeCanGiua(Color color, boolean bold) {
        this.color = color;
        this.bold = bold;
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object value,
            boolean selected, boolean focus, int row, int col) {
        super.getTableCellRendererComponent(t, value, selected, focus, row, col);
        setHorizontalAlignment(SwingConstants.CENTER);
        if (bold) setFont(font(Font.BOLD, 12));
        if (!selected) setForeground(color);
        return this;
    }
}
