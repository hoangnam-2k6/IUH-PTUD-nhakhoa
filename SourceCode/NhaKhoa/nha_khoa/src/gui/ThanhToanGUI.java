package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ThanhToanGUI extends BaseQuanLyGUI {
    private static final Object[][] INVOICES = {
        {"HD-260618-024", "Nguyễn Minh Anh\nBN-00241", "18/06/2026 · 09:42", "3.500.000 đ", "Chuyển khoản", "Đã thanh toán"},
        {"HD-260618-023", "Trần Quốc Huy\nBN-00240", "18/06/2026 · 09:10", "500.000 đ", "Tiền mặt", "Đã thanh toán"},
        {"HD-260617-022", "Lê Thị Thu Hà\nBN-00239", "17/06/2026 · 16:35", "2.800.000 đ", "Thẻ ngân hàng", "Chờ thanh toán"},
        {"HD-260617-021", "Phạm Hoàng Nam\nBN-00238", "17/06/2026 · 14:20", "1.200.000 đ", "Chuyển khoản", "Yêu cầu hủy"},
        {"HD-260616-020", "Võ Ngọc Linh\nBN-00237", "16/06/2026 · 10:05", "450.000 đ", "Tiền mặt", "Đã hoàn tất"}
    };

    public ThanhToanGUI() {
        super("Thanh toán & hóa đơn", "Thanh toán và hóa đơn");
        content.add(pageHeading("Thanh toán & hóa đơn", "Thanh toán & hóa đơn",
                "Tra cứu hóa đơn, phiếu thu và trạng thái thanh toán.", null, null));
        content.add(statRow(
                statCard(GuiIcons.INVOICE, "Doanh thu hôm nay", "48.650.000 đ", "+12,4% so với hôm qua", BLUE),
                statCard(GuiIcons.CHECK, "Đã thanh toán", "42 phiếu", "44.200.000 đ", GREEN),
                statCard(GuiIcons.CALENDAR, "Chờ thanh toán", "6 phiếu", "4.450.000 đ", ORANGE),
                statCard(GuiIcons.INVOICE, "Hóa đơn tháng này", "50 phiếu", "Tháng 06/2026", BLUE)));
        content.add(BoxGap.vertical(14));

        DefaultTableModel model = new DefaultTableModel(new Object[] {
            "MÃ HÓA ĐƠN", "BỆNH NHÂN", "THỜI GIAN", "TỔNG TIỀN", "PHƯƠNG THỨC", "TRẠNG THÁI"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] invoice : INVOICES) {
            model.addRow(invoice);
        }
        JTable table = new JTable(model);
        styleTable(table);
        table.setRowHeight(54);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JTextField search = searchField("Tìm mã hóa đơn hoặc tên bệnh nhân...", 305);
        search.getDocument().addDocumentListener(SharedListeners.document(() -> {
            String text = search.getText().trim();
            sorter.setRowFilter(text.isEmpty()
                    ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }));
        JComboBox<String> month = new JComboBox<>(new String[] {"Tháng 06/2026", "Tháng 05/2026"});
        month.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        month.setBackground(Color.WHITE);
        month.setPreferredSize(new Dimension(140, 35));
        month.setBorder(fieldBorder());
        JComboBox<String> status = new JComboBox<>(new String[] {
            "Tất cả trạng thái", "Đã thanh toán", "Chờ thanh toán", "Yêu cầu hủy", "Đã hoàn tất"
        });
        status.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        status.setBackground(Color.WHITE);
        status.setPreferredSize(new Dimension(155, 35));
        status.setBorder(fieldBorder());
        status.addActionListener(event -> {
            String selected = (String) status.getSelectedItem();
            sorter.setRowFilter("Tất cả trạng thái".equals(selected)
                    ? null : RowFilter.regexFilter(Pattern.quote(selected), 5));
        });
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        controls.add(month);
        controls.add(status);
        controls.add(outlineButton("⇩"));
        JPanel toolbar = toolbar(search, javax.swing.Box.createHorizontalGlue(), controls);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 1, LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setOpaque(false);
        tableArea.setAlignmentX(LEFT_ALIGNMENT);
        tableArea.add(toolbar, BorderLayout.NORTH);
        tableArea.add(tableCard(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        footer.add(label("Hiển thị " + INVOICES.length + " hóa đơn", 10, false, MUTED), BorderLayout.WEST);
        footer.add(label("‹     1     2     3     …     12     ›", 10, false, BLUE), BorderLayout.EAST);
        tableArea.add(footer, BorderLayout.SOUTH);
        content.add(tableArea);
        showWindow();
    }

}
