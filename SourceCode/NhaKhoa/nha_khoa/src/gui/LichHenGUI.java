package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class LichHenGUI extends BaseQuanLyGUI {
    private static final Object[][] APPOINTMENTS = {
        {"08:00", "Nguyễn Minh Anh", "BN-00241", "Chỉnh nha định kỳ", "BS. Hoàng Việt", "Phòng 02", "Đã xác nhận"},
        {"08:30", "Trần Quốc Huy", "BN-00240", "Khám tổng quát", "BS. Minh Thắng", "Phòng 01", "Đang chờ"},
        {"09:15", "Lê Thị Thu Hà", "BN-00239", "Điều trị tủy", "BS. Hoàng Việt", "Phòng 03", "Đang khám"},
        {"10:30", "Phạm Hoàng Nam", "BN-00238", "Tẩy trắng răng", "BS. Minh Thắng", "Phòng 01", "Chưa xác nhận"},
        {"11:00", "Võ Ngọc Linh", "BN-00237", "Lấy cao răng", "BS. Tuấn Anh", "Phòng 02", "Đã hủy"}
    };

    public LichHenGUI() {
        super("Lịch hẹn", "Quản lý lịch hẹn");
        content.add(pageHeading("Quản lý lịch hẹn", "Quản lý lịch hẹn",
                "Theo dõi và sắp xếp toàn bộ lịch hẹn của phòng khám.", "Tạo lịch hẹn",
                event -> createAppointment()));

        JPanel stats = new JPanel(new java.awt.GridLayout(1, 5, 10, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.add(appointmentStat("Hôm nay", "18", "Tổng lịch hẹn", BLUE, true));
        stats.add(appointmentStat("Tổng lịch hẹn", "38", "Trong ngày", BLUE, false));
        stats.add(appointmentStat("Đã xác nhận", "24", "Đúng kế hoạch", GREEN, false));
        stats.add(appointmentStat("Đang chờ", "8", "Cần xác nhận", ORANGE, false));
        long cancelledCount = java.util.Arrays.stream(APPOINTMENTS)
                .filter(appointment -> "Đã hủy".equals(appointment[6]))
                .count();
        stats.add(appointmentStat("Đã hủy", Long.toString(cancelledCount),
                "Lịch hẹn đã hủy", RED, false));
        content.add(stats);
        content.add(BoxGap.vertical(14));

        DefaultTableModel model = new DefaultTableModel(new Object[] {
            "GIỜ", "BỆNH NHÂN", "DỊCH VỤ", "BÁC SĨ PHỤ TRÁCH", "PHÒNG KHÁM", "TRẠNG THÁI", ""
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] appointment : APPOINTMENTS) {
            model.addRow(new Object[] {
                appointment[0] + "\n30 phút",
                appointment[1] + "\n" + appointment[2],
                appointment[3], appointment[4], appointment[5], appointment[6], "•••"
            });
        }
        JTable table = new JTable(model);
        styleTable(table);
        table.setRowHeight(64);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(40);

        JButton date = outlineButton("18/06/2026   ▾");
        JComboBox<String> filter = new JComboBox<>(new String[] {"Tất cả bác sĩ", "BS. Hoàng Việt", "BS. Minh Thắng", "BS. Tuấn Anh"});
        filter.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        filter.setBackground(Color.WHITE);
        filter.setPreferredSize(new Dimension(150, 35));
        filter.setBorder(fieldBorder());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(date);
        actions.add(filter);
        actions.add(filterButton());
        JPanel viewToggle = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        viewToggle.setOpaque(false);
        viewToggle.add(flatButton("Danh sách", true));
        viewToggle.add(flatButton("Lịch tuần", false));
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 1, LINE),
                BorderFactory.createEmptyBorder(9, 10, 9, 10)));
        toolbar.add(viewToggle, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setOpaque(false);
        tableArea.setAlignmentX(LEFT_ALIGNMENT);
        tableArea.add(toolbar, BorderLayout.NORTH);
        tableArea.add(tableCard(table), BorderLayout.CENTER);
        content.add(tableArea);
        showWindow();
    }

    private JPanel appointmentStat(String title, String value, String caption, Color accent, boolean featured) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(featured ? DARK_BLUE : Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(featured ? DARK_BLUE : LINE),
                BorderFactory.createEmptyBorder(13, 15, 13, 15)));
        panel.add(label(title, 10, false, featured ? new Color(218, 239, 255) : MUTED), BorderLayout.NORTH);
        panel.add(label(value, 26, true, featured ? Color.WHITE : TEXT), BorderLayout.CENTER);
        panel.add(label(caption, 10, false, featured ? Color.WHITE : MUTED), BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(140, 83));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return panel;
    }

    private void createAppointment() {
        new TaoLichHenGUI();
    }
}
