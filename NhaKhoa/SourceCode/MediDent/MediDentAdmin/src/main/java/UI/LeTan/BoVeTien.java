package UI.LeTan;

import static UI.LeTan.GiaoDien.font;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Ô hiển thị số tiền (BigDecimal): căn phải, có dấu chấm ngăn cách hàng nghìn, thêm "đ". */
public final class BoVeTien extends DefaultTableCellRenderer {
    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private final Color color;

    public BoVeTien(Color color) {
        this.color = color;
    }

    @Override public Component getTableCellRendererComponent(JTable t, Object value,
            boolean selected, boolean focus, int row, int col) {
        String text = value instanceof BigDecimal ? dinhDang((BigDecimal) value) : String.valueOf(value);
        super.getTableCellRendererComponent(t, text, selected, focus, row, col);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setFont(font(Font.BOLD, 12));
        setBorder(new EmptyBorder(0, 10, 0, 18));
        if (!selected) setForeground(color);
        return this;
    }

    /** 1234567 -> "1.234.567 đ". */
    public static String dinhDang(BigDecimal value) {
        return NumberFormat.getIntegerInstance(VI).format(value.setScale(0, RoundingMode.HALF_UP)) + " đ";
    }
}
