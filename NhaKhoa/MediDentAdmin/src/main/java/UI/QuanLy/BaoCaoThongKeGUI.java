package UI.QuanLy;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class BaoCaoThongKeGUI extends BaseQuanLyGUI {
    private static final long TOTAL_REVENUE = 3_840_000_000L;
    private static final int TOTAL_VISITS = 6_248;
    private static final int RETURNING_VISITS = 4_536;
    private static final int[] MONTHLY_REVENUE_MILLIONS = {510, 540, 575, 600, 730, 885};
    private static final String[] MONTHS = {"Tháng 01", "Tháng 02", "Tháng 03",
        "Tháng 04", "Tháng 05", "Tháng 06"};
    private static final String[] SERVICE_NAMES = {
        "Khám tổng quát", "Chỉnh nha", "Điều trị", "Trám răng", "Khác"
    };
    private static final int[] SERVICE_VISITS = {2124, 1487, 1312, 750, 575};
    private static final Color[] SERVICE_COLORS = {
        new Color(0, 139, 207), new Color(44, 184, 236), new Color(0, 184, 126),
        new Color(242, 166, 0), new Color(174, 187, 205)
    };
    private static final NumberFormat CURRENCY = NumberFormat.getIntegerInstance(
            Locale.forLanguageTag("vi-VN"));
    private final JPanel reportScope = new JPanel(new BorderLayout());
    private final RevenueChart revenueChart = new RevenueChart();
    private String selectedPeriod = "01/01/2026 – 18/06/2026";
    private String selectedBranch = "Tất cả chi nhánh";

    public BaoCaoThongKeGUI() {
        super("Báo cáo - thống kê", "Báo cáo và thống kê");
        content.add(pageHeading("Báo cáo - thống kê", "Báo cáo - thống kê",
                "Tổng hợp hoạt động phòng khám, thống kê từng chỉ tiêu và rút ra kết luận.",
                "Xuất báo cáo", event -> exportSummary()));

        content.add(createCriteriaSection());
        content.add(BoxGap.vertical(12));
        reportScope.setOpaque(false);
        reportScope.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateReportScope();
        content.add(reportScope);
        content.add(BoxGap.vertical(13));

        content.add(createBusinessSection());
        content.add(BoxGap.vertical(13));
        content.add(sectionHeading("KẾT QUẢ THỐNG KÊ", "Tổng quan các chỉ tiêu chính",
                "Các con số tổng hợp trong phạm vi đã chọn."));
        content.add(BoxGap.vertical(9));
        content.add(statRow(
                statCard(null, "Doanh thu đã thu", "3,84 tỷ đ",
                        "Tăng 18,4% so với kỳ trước", BLUE),
                statCard(null, "Lượt khám hoàn tất", "6.248",
                        "Tăng 12,3% so với kỳ trước", GREEN),
                statCard(null, "Thu bình quân / lượt", "614.600 đ",
                        "Doanh thu / lượt khám", ORANGE),
                statCard(null, "Tỷ lệ khách quay lại", "72,6%",
                        "4.536 / 6.248 lượt khám", GREEN)));
        content.add(BoxGap.vertical(14));

        content.add(sectionHeading("CHI TIẾT THEO NGHIỆP VỤ",
                "Doanh thu và hoạt động khám",
                "Phân tích diễn biến doanh thu theo tháng và cơ cấu lượt khám theo dịch vụ."));
        content.add(BoxGap.vertical(9));
        JPanel charts = new JPanel(new BorderLayout(12, 0));
        charts.setOpaque(false);
        charts.setAlignmentX(Component.LEFT_ALIGNMENT);
        charts.add(createRevenueSection(), BorderLayout.CENTER);
        charts.add(createServiceSection(), BorderLayout.EAST);
        charts.setPreferredSize(new Dimension(900, 330));
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        content.add(charts);
        content.add(BoxGap.vertical(13));

        content.add(createCustomerSection());
        content.add(BoxGap.vertical(13));
        content.add(createConclusionSection());
        showWindow();
    }

    private JPanel createCriteriaSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel description = new JPanel();
        description.setOpaque(false);
        description.setLayout(new javax.swing.BoxLayout(description, javax.swing.BoxLayout.Y_AXIS));
        description.add(label("TIÊU CHÍ THỐNG KÊ", 12, true, TEXT));
        description.add(label("Chọn kỳ báo cáo, chi nhánh và chỉ tiêu cần phân tích.",
                10, false, MUTED));
        panel.add(description, BorderLayout.NORTH);

        JComboBox<String> period = formCombo(
                "01/01/2026 – 18/06/2026",
                "01/06/2026 – 18/06/2026",
                "01/01/2025 – 31/12/2025");
        period.setPreferredSize(new Dimension(215, 36));
        JComboBox<String> branch = formCombo("Tất cả chi nhánh", "Chi nhánh Quận 1");
        branch.setPreferredSize(new Dimension(185, 36));
        JComboBox<String> metric = formCombo(
                "Tất cả chỉ tiêu", "Doanh thu", "Lượt khám", "Dịch vụ", "Khách hàng quay lại");
        metric.setPreferredSize(new Dimension(190, 36));
        javax.swing.JButton apply = primaryButton("Áp dụng tiêu chí");
        JPanel controls = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 9, 0));
        controls.setOpaque(false);
        controls.add(labeledControl("Khoảng thời gian", period));
        controls.add(labeledControl("Chi nhánh", branch));
        controls.add(labeledControl("Nhóm chỉ tiêu", metric));
        controls.add(apply);
        panel.add(controls, BorderLayout.CENTER);

        apply.addActionListener(event -> {
            selectedPeriod = (String) period.getSelectedItem();
            selectedBranch = (String) branch.getSelectedItem();
            String selectedMetric = (String) metric.getSelectedItem();
            updateReportScope();
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BLUE),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            panel.setToolTipText("Đang xem nhóm chỉ tiêu: " + selectedMetric);
        });
        return panel;
    }

    private JPanel labeledControl(String title, Component control) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new javax.swing.BoxLayout(group, javax.swing.BoxLayout.Y_AXIS));
        group.add(label(title, 10, true, MUTED));
        group.add(BoxGap.vertical(5));
        group.add(control);
        return group;
    }

    private void updateReportScope() {
        reportScope.removeAll();
        reportScope.setBackground(new Color(239, 248, 255));
        reportScope.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(202, 232, 251)),
                BorderFactory.createEmptyBorder(9, 13, 9, 13)));
        reportScope.add(label("PHẠM VI BÁO CÁO", 9, true, DARK_BLUE), BorderLayout.WEST);
        reportScope.add(label(selectedPeriod + "    ·    " + selectedBranch
                + "    ·    Dữ liệu minh họa", 10, false, TEXT), BorderLayout.CENTER);
        reportScope.revalidate();
        reportScope.repaint();
    }

    private JPanel createBusinessSection() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel explanation = new JPanel();
        explanation.setOpaque(false);
        explanation.setLayout(new javax.swing.BoxLayout(explanation, javax.swing.BoxLayout.Y_AXIS));
        explanation.add(label("NGHIỆP VỤ BÁO CÁO", 12, true, DARK_BLUE));
        explanation.add(BoxGap.vertical(5));
        explanation.add(label(
                "Báo cáo hỗ trợ quản lý đánh giá kết quả kinh doanh và tình hình phục vụ bệnh nhân.",
                11, false, TEXT));
        explanation.add(BoxGap.vertical(4));
        explanation.add(label(
                "Doanh thu được tổng hợp từ hóa đơn đã thanh toán; lượt khám tính theo dịch vụ hoàn tất.",
                10, false, MUTED));
        panel.add(explanation, BorderLayout.CENTER);

        JPanel steps = new JPanel(new GridLayout(1, 3, 8, 0));
        steps.setOpaque(false);
        steps.add(processStep("1", "Chọn kỳ và chi nhánh"));
        steps.add(processStep("2", "Tổng hợp chỉ tiêu"));
        steps.add(processStep("3", "Đánh giá và kết luận"));
        panel.add(steps, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel processStep(String number, String description) {
        JPanel step = new JPanel(new BorderLayout(7, 0));
        step.setOpaque(false);
        step.add(label(number, 11, true, BLUE), BorderLayout.WEST);
        step.add(label(description, 10, false, MUTED), BorderLayout.CENTER);
        return step;
    }

    private JPanel sectionHeading(String eyebrow, String title, String subtitle) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(label(eyebrow, 9, true, BLUE));
        heading.add(label(title, 15, true, TEXT));
        heading.add(label(subtitle, 10, false, MUTED));
        return heading;
    }

    private JPanel createRevenueSection() {
        JPanel panel = whitePanel();
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new javax.swing.BoxLayout(title, javax.swing.BoxLayout.Y_AXIS));
        title.setBorder(BorderFactory.createEmptyBorder(13, 15, 5, 15));
        title.add(label("1. Doanh thu theo tháng", 13, true, TEXT));
        title.add(label("Đơn vị: triệu đồng · Tổng kỳ: 3.840 triệu đồng", 10, false, MUTED));
        panel.add(title, BorderLayout.NORTH);
        panel.add(revenueChart, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(580, 315));
        return panel;
    }

    private JPanel createServiceSection() {
        JPanel panel = whitePanel();
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new javax.swing.BoxLayout(title, javax.swing.BoxLayout.Y_AXIS));
        title.setBorder(BorderFactory.createEmptyBorder(13, 15, 4, 15));
        title.add(label("2. Lượt khám theo dịch vụ", 13, true, TEXT));
        title.add(label("Số lượt và tỷ trọng trên 6.248 lượt khám", 10, false, MUTED));
        panel.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(8, 4));
        body.setOpaque(false);
        DoughnutChart doughnut = new DoughnutChart();
        doughnut.setPreferredSize(new Dimension(165, 150));
        body.add(doughnut, BorderLayout.NORTH);
        JPanel breakdown = new JPanel(new GridLayout(SERVICE_NAMES.length, 1, 0, 5));
        breakdown.setOpaque(false);
        for (int i = 0; i < SERVICE_NAMES.length; i++) {
            JPanel line = new JPanel(new BorderLayout(7, 0));
            line.setOpaque(false);
            JPanel category = new JPanel(new BorderLayout(6, 0));
            category.setOpaque(false);
            JPanel marker = new JPanel();
            marker.setBackground(SERVICE_COLORS[i]);
            marker.setPreferredSize(new Dimension(8, 8));
            category.add(marker, BorderLayout.WEST);
            category.add(label(SERVICE_NAMES[i], 9, false, MUTED), BorderLayout.CENTER);
            int count = SERVICE_VISITS[i];
            String detail = String.format("%,d · %.1f%%", count, share(count));
            line.add(category, BorderLayout.WEST);
            line.add(label(detail, 9, true, TEXT), BorderLayout.EAST);
            breakdown.add(line);
        }
        body.add(breakdown, BorderLayout.CENTER);
        body.setBorder(BorderFactory.createEmptyBorder(0, 15, 12, 15));
        panel.add(body, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(345, 315));
        return panel;
    }

    private JPanel createCustomerSection() {
        JPanel panel = new JPanel(new BorderLayout(14, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        text.add(label("3. Khách hàng quay lại", 13, true, TEXT));
        text.add(label("Đánh giá mức độ duy trì khách hàng trong tổng lượt khám.", 10, false, MUTED));
        panel.add(text, BorderLayout.NORTH);

        JPanel measures = new JPanel(new GridLayout(1, 3, 12, 0));
        measures.setOpaque(false);
        measures.add(customerMetric("Lượt khám quay lại", "4.536", "72,6%", GREEN));
        measures.add(customerMetric("Lượt khám lần đầu", "1.712", "27,4%", BLUE));
        measures.add(customerMetric("Tổng lượt khám", "6.248", "100%", DARK_BLUE));
        panel.add(measures, BorderLayout.CENTER);
        return panel;
    }

    private JPanel customerMetric(String title, String value, String percent, Color color) {
        JPanel metric = new JPanel(new BorderLayout());
        metric.setOpaque(false);
        metric.add(label(title, 10, false, MUTED), BorderLayout.NORTH);
        metric.add(label(value, 18, true, TEXT), BorderLayout.CENTER);
        metric.add(label(percent, 10, true, color), BorderLayout.SOUTH);
        return metric;
    }

    private JPanel createConclusionSection() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(new Color(239, 248, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(202, 232, 251)),
                BorderFactory.createEmptyBorder(15, 17, 15, 17)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label("KẾT LUẬN", 12, true, DARK_BLUE), BorderLayout.NORTH);

        JPanel conclusions = new JPanel();
        conclusions.setOpaque(false);
        conclusions.setLayout(new javax.swing.BoxLayout(conclusions, javax.swing.BoxLayout.Y_AXIS));
        conclusions.add(label(
                "• Doanh thu tăng đều trong kỳ, đạt đỉnh 885 triệu đồng vào tháng 06.",
                11, false, TEXT));
        conclusions.add(BoxGap.vertical(6));
        conclusions.add(label(
                "• Khám tổng quát chiếm tỷ trọng lớn nhất: 2.124 lượt (34,0%); nên duy trì đủ lịch phục vụ.",
                11, false, TEXT));
        conclusions.add(BoxGap.vertical(6));
        conclusions.add(label(
                "• Khách quay lại đạt 72,6%; tiếp tục chăm sóc sau điều trị để duy trì tỷ lệ này.",
                11, false, TEXT));
        panel.add(conclusions, BorderLayout.CENTER);
        return panel;
    }

    private double share(int value) {
        return value * 100.0 / TOTAL_VISITS;
    }

    private void exportSummary() {
        String message = "Phạm vi: " + selectedPeriod + " · " + selectedBranch
                + "\nDoanh thu: " + CURRENCY.format(TOTAL_REVENUE) + " đ"
                + "\nLượt khám: " + String.format("%,d", TOTAL_VISITS)
                + "\nKhách quay lại: " + String.format("%,d", RETURNING_VISITS)
                + " (" + String.format("%.1f", share(RETURNING_VISITS)) + "%)"
                + "\n\nĐây là số liệu minh họa trên giao diện.";
        javax.swing.JOptionPane.showMessageDialog(this, message,
                "Tóm tắt báo cáo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private static final class RevenueChart extends JPanel {
        RevenueChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(560, 245));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 51, right = getWidth() - 15, top = 19, bottom = getHeight() - 38;
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int y = top + (bottom - top) * i / 4;
                g.setColor(new Color(232, 238, 245));
                g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10, new float[] {2, 3}, 0));
                g.drawLine(left, y, right, y);
                g.setColor(MUTED);
                g.drawString(String.valueOf(1000 - i * 250), 11, y + 3);
            }
            int max = 1000;
            int width = right - left;
            int step = width / MONTHLY_REVENUE_MILLIONS.length;
            int barWidth = Math.max(18, step / 3);
            for (int i = 0; i < MONTHLY_REVENUE_MILLIONS.length; i++) {
                int height = (bottom - top) * MONTHLY_REVENUE_MILLIONS[i] / max;
                int x = left + i * step + (step - barWidth) / 2;
                int y = bottom - height;
                g.setPaint(new java.awt.GradientPaint(x, y, new Color(47, 184, 237),
                        x, bottom, new Color(0, 137, 204)));
                g.fillRoundRect(x, y, barWidth, height, 5, 5);
                g.setColor(TEXT);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String amount = String.valueOf(MONTHLY_REVENUE_MILLIONS[i]);
                g.drawString(amount, x + (barWidth - g.getFontMetrics().stringWidth(amount)) / 2,
                        y - 4);
                g.setColor(MUTED);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String month = "T" + (i + 1);
                g.drawString(month, x + (barWidth - g.getFontMetrics().stringWidth(month)) / 2,
                        bottom + 15);
            }
            g.dispose();
        }
    }

    private static final class DoughnutChart extends JPanel {
        private DoughnutChart() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth() - 18, getHeight() - 10);
            int x = (getWidth() - size) / 2;
            int y = Math.max(0, (getHeight() - size) / 2);
            double start = 90;
            for (int i = 0; i < SERVICE_VISITS.length; i++) {
                double extent = SERVICE_VISITS[i] * 360.0 / TOTAL_VISITS;
                g.setColor(SERVICE_COLORS[i]);
                g.fill(new Arc2D.Double(x, y, size, size, start, -extent, Arc2D.PIE));
                start -= extent;
            }
            int inset = size / 4;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(x + inset, y + inset, size - inset * 2, size - inset * 2));
            g.setColor(TEXT);
            g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String count = String.format("%,d", TOTAL_VISITS);
            g.drawString(count, getWidth() / 2 - g.getFontMetrics().stringWidth(count) / 2,
                    getHeight() / 2 + 2);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(MUTED);
            String caption = "lượt khám";
            g.drawString(caption, getWidth() / 2 - g.getFontMetrics().stringWidth(caption) / 2,
                    getHeight() / 2 + 17);
            g.dispose();
        }
    }
}
