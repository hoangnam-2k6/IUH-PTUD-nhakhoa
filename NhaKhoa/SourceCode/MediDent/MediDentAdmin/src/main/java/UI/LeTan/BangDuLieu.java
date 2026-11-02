package UI.LeTan;

import static UI.LeTan.GiaoDien.BORDER;
import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.font;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/** Các hàm dựng bảng dữ liệu dùng chung: model chỉ đọc, bảng kẻ sọc, bộ sắp xếp, khung cuộn. */
public final class BangDuLieu {

    private BangDuLieu() { }

    public static DefaultTableModel taoModel(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : Object.class;
            }
        };
    }

    public static JTable taoBang(DefaultTableModel model) {
        JTable table = new BangKeSoc(model);
        table.setFont(font(Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(44);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(0xEEF2F7));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        // Chiều cao ưu tiên nhỏ để bảng không ép trang dài hơn khung nhìn;
        // bảng tự co theo khoảng trống và có thanh cuộn riêng.
        table.setPreferredScrollableViewportSize(new Dimension(300, 160));
        table.getTableHeader().setDefaultRenderer(new BoVeTieuDeCot());
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(48); // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(86); // Mã phiếu / Mã BN
        for (int i = 2; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(doRongCot(table.getColumnName(i)));
        }
        return table;
    }

    /** Bộ sắp xếp cho bảng; cột STT (cột 0) không cho sắp xếp. */
    public static TableRowSorter<DefaultTableModel> taoSorter(JTable table, DefaultTableModel model) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(0, false);
        return sorter;
    }

    public static void datCot(JTable table, int column, TableCellRenderer renderer) {
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    public static JScrollPane boCuon(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.getHorizontalScrollBar().setUnitIncrement(24);
        pane.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        pane.getHorizontalScrollBar().setUI(new ThanhCuonHienDai());
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        pane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return pane;
    }

    /** Độ rộng mỗi cột theo tên cột. */
    private static int doRongCot(String title) {
        switch (title) {
            case "Mã BN":
            case "Mã NV":
                return 78;
            case "Bệnh nhân":
            case "Họ tên":
            case "Bác sĩ":
                return 190;
            case "Ngày khám":
            case "Ngày sinh":
                return 126;
            case "Triệu chứng":
            case "Chẩn đoán":
            case "Phương pháp điều trị":
            case "Kết quả":
            case "Tiền sử bệnh":
            case "Địa chỉ":
            case "Ghi chú":
                return 180;
            case "Trạng thái":
                return 140;
            default:
                return 125;
        }
    }
}
