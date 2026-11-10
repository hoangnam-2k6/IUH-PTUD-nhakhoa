package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Dinh dang dung chung cho cac JTable trong giao dien quan ly MediDent.
 * Chi thay doi cach hien thi, khong thay doi du lieu hay xu ly nghiep vu.
 */
public final class BangStyle {


    private static final Font FONT_NOI_DUNG = new Font("Arial", Font.PLAIN, 13);
    private static final Font FONT_TIEU_DE = new Font("Arial", Font.BOLD, 13);

    private BangStyle() {
    }

    /** Ap dung giao dien dong bo cho JTable. Goi sau khi khoi tao JTable. */
    public static void apDung(JTable bang) {
        if (bang == null) {
            return;
        }

        bang.setFont(FONT_NOI_DUNG);
        bang.setForeground(new Color(45, 58, 72));
        bang.setBackground(Color.WHITE);
        bang.setSelectionBackground(new Color(222, 239, 253));
        bang.setSelectionForeground(new Color(25, 75, 115));
        bang.setGridColor(new Color(230, 237, 244));
        bang.setShowGrid(false);
        bang.setIntercellSpacing(new Dimension(0, 0));
        bang.setRowHeight(36);
        bang.setRowMargin(0);
        bang.setFillsViewportHeight(true);
        bang.setOpaque(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = bang.getTableHeader();
        header.setFont(FONT_TIEU_DE);
        header.setForeground(new Color(35, 73, 105));
        header.setBackground(new Color(235, 244, 251));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());

        // Tat ca cac cot dung cung mot cach hien thi, khong tu dong doan ten cot.
        for (int i = 0; i < bang.getColumnModel().getColumnCount(); i++) {
            TableColumn cot = bang.getColumnModel().getColumn(i);
            cot.setCellRenderer(new BodyRenderer());
        }
    }

    /** Lam gon vien va nen cua JScrollPane chua JTable. */
    public static void apDungScrollPane(JScrollPane scroll) {
        if (scroll == null) {
            return;
        }

        scroll.setBorder(BorderFactory.createLineBorder(new Color(218, 229, 239), 1));
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
    }

    /** Renderer dung chung cho tieu de cot. */
    private static class HeaderRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        HeaderRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setOpaque(true);
            setFont(FONT_TIEU_DE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(55, 126, 184)),
                    BorderFactory.createEmptyBorder(0, 7, 0, 7)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBackground(new Color(235, 244, 251));
            setForeground(new Color(35, 73, 105));
            return this;
        }
    }

    /** Renderer dung chung cho noi dung cac o: hang xen ke va mau khi duoc chon. */
    private static class BodyRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        BodyRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 237, 244)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(FONT_NOI_DUNG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 237, 244)),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));

            if (isSelected) {
                setBackground(new Color(222, 239, 253));
                setForeground(new Color(25, 75, 115));
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(247, 250, 253));
                setForeground(new Color(45, 58, 72));
            }
            return this;
        }
    }
}
