package UI.QuanLy;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Dao.NghiepVu.BaoCaoService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.GiaoDien;
import UI.Chung.XemTruoc;

/**
 * Trang chủ vận hành: chỉ đọc số liệu SQL Server, không sử dụng dữ liệu demo.
 * Tất cả biểu đồ, thông báo và lịch hôm nay lấy từ cùng database MediDent.
 */
public final class TrangChuPanel extends JPanel {

    private static final Locale VI = new Locale("vi", "VN");

    private final BaoCaoService service;
    private final String fullName;
    private final JLabel status = GiaoDien.label("Đang tải số liệu từ SQL Server...", 12, false);
    private final JLabel dateCaption = GiaoDien.label("", 12, false);
    private final JLabel todayCount = GiaoDien.label("—", 25, true);
    private final JLabel monthRevenue = GiaoDien.label("—", 25, true);
    private final JLabel patientCount = GiaoDien.label("—", 25, true);
    private final JLabel staffCount = GiaoDien.label("—", 25, true);
    private final JLabel serviceCount = GiaoDien.label("—", 13, true);
    private final JLabel pendingAppointments = GiaoDien.label("—", 22, true);
    private final JLabel pendingPayments = GiaoDien.label("—", 22, true);
    private final JLabel cancelRequests = GiaoDien.label("—", 22, true);
    private final JLabel lastUpdate = GiaoDien.label("Chưa có dữ liệu", 11, false);

    private final RevenueChart chart = new RevenueChart();
    private final DefaultTableModel appointmentsModel;
    private final JTable appointments;
    private boolean loading;

    public TrangChuPanel(PhanQuyen.PhienDangNhap session) {
        this.service = new BaoCaoService(session);
        this.fullName = session == null ? "Quản lý phòng khám" : session.fullName();

        appointmentsModel = new DefaultTableModel(
                new String[] { "Giờ", "Bệnh nhân", "Bác sĩ", "Phòng", "Trạng thái" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointments = new JTable(appointmentsModel);
        GiaoDien.styleTable(appointments);
        appointments.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setBackground(GiaoDien.BG);
        body.setBorder(new EmptyBorder(24, 28, 24, 28));
        body.add(buildWelcome(), BorderLayout.NORTH);
        body.add(buildDashboard(), BorderLayout.CENTER);
        body.add(status, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GiaoDien.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);


    }

    private JPanel buildWelcome() {
        JPanel banner = new JPanel(new BorderLayout(22, 8)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setPaint(new GradientPaint(
                        0, 0, new Color(0xE0F2FE),
                        Math.max(1, getWidth()), getHeight(), new Color(0xF0FAFF)));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel eyebrow = GiaoDien.label("TỔNG QUAN VẬN HÀNH", 11, true);
        eyebrow.setForeground(GiaoDien.BLUE_DARK);
        text.add(eyebrow);
        text.add(Box.createVerticalStrut(7));
        text.add(GiaoDien.label("Xin chào, " + fullName, 23, true));
        text.add(Box.createVerticalStrut(5));
        dateCaption.setText("Lịch hôm nay, việc chờ xử lý và doanh thu 6 tháng gần nhất");
        text.add(dateCaption);
        banner.add(text, BorderLayout.CENTER);

        JButton refresh = GiaoDien.button("Làm mới dữ liệu", true);
        refresh.setPreferredSize(new Dimension(152, 42));
        refresh.addActionListener(event -> refresh());
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(refresh, BorderLayout.NORTH);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JPanel buildDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 17));
        dashboard.setOpaque(false);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 13, 0));
        kpis.setOpaque(false);
        kpis.add(metricCard("LỊCH HẸN HÔM NAY", todayCount, "calendar", "Lịch trong ngày"));
        kpis.add(metricCard("DOANH THU THÁNG", monthRevenue, "bill", "Giao dịch đã thu"));
        kpis.add(metricCard("BỆNH NHÂN", patientCount, "patient", "Hồ sơ hoạt động"));
        kpis.add(metricCard("NHÂN VIÊN", staffCount, "staff", "Đang làm việc"));
        dashboard.add(kpis, BorderLayout.NORTH);

        JPanel operations = new JPanel(new GridBagLayout());
        operations.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.52;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.64;
        c.insets = new Insets(0, 0, 14, 8);
        operations.add(buildRevenueCard(), c);

        c.gridx = 1;
        c.weightx = 0.36;
        c.insets = new Insets(0, 7, 14, 0);
        operations.add(buildTaskCard(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.48;
        c.insets = new Insets(0, 0, 0, 0);
        operations.add(buildAppointmentsCard(), c);

        dashboard.add(operations, BorderLayout.CENTER);
        return dashboard;
    }

    private JPanel metricCard(String name, JLabel value, String icon, String hint) {
        JPanel card = GiaoDien.white(new BorderLayout(4, 8));
        card.setPreferredSize(new Dimension(165, 117));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = GiaoDien.label(name, 11, true);
        title.setForeground(GiaoDien.MUTED);
        top.add(title, BorderLayout.WEST);
        top.add(new JLabel(GiaoDien.icon(icon, 23)), BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        value.setForeground(GiaoDien.DARK);
        card.add(value, BorderLayout.CENTER);
        card.add(GiaoDien.label(hint, 11, false), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRevenueCard() {
        JPanel card = GiaoDien.white(new BorderLayout(0, 10));
        card.setMinimumSize(new Dimension(360, 260));
        card.setPreferredSize(new Dimension(650, 265));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(GiaoDien.label("Biểu đồ doanh thu 6 tháng", 17, true), BorderLayout.WEST);
        head.add(GiaoDien.label("Đơn vị: VNĐ", 11, false), BorderLayout.EAST);
        card.add(head, BorderLayout.NORTH);
        card.add(chart, BorderLayout.CENTER);

        JLabel explanation = GiaoDien.label("Chỉ tính giao dịch đã thực thu trên SQL Server", 11, false);
        card.add(explanation, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildTaskCard() {
        JPanel card = GiaoDien.white(new BorderLayout(0, 9));
        card.setMinimumSize(new Dimension(270, 260));
        card.setPreferredSize(new Dimension(370, 265));
        card.add(GiaoDien.label("Việc cần xử lý", 17, true), BorderLayout.NORTH);

        JPanel items = new JPanel(new GridLayout(3, 1, 0, 10));
        items.setOpaque(false);
        items.add(todoRow("Lịch chờ xác nhận", pendingAppointments, GiaoDien.AMBER_SOFT));
        items.add(todoRow("Hóa đơn chờ thanh toán", pendingPayments, GiaoDien.BLUE_SOFT));
        items.add(todoRow("Yêu cầu hủy chờ duyệt", cancelRequests, GiaoDien.RED_SOFT));
        card.add(items, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(GiaoDien.label("Dịch vụ đang áp dụng:", 11, false), BorderLayout.WEST);
        footer.add(serviceCount, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel todoRow(String title, JLabel count, Color background) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(background);
        row.setBorder(new EmptyBorder(9, 12, 9, 12));
        row.add(GiaoDien.label(title, 12, true), BorderLayout.CENTER);
        count.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(count, BorderLayout.EAST);
        return row;
    }

    private JPanel buildAppointmentsCard() {
        JPanel card = GiaoDien.white(new BorderLayout(0, 10));
        card.setMinimumSize(new Dimension(630, 260));
        card.setPreferredSize(new Dimension(650, 280));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(GiaoDien.label("Lịch hẹn hôm nay", 17, true), BorderLayout.WEST);
        top.add(lastUpdate, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(appointments);
        scroll.setBorder(BorderFactory.createLineBorder(GiaoDien.BORDER));
        scroll.getViewport().setBackground(GiaoDien.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        if (loading) {
            return;
        }
        loading = true;
        status.setText("Đang đọc dữ liệu mới từ SQL Server...");
        LocalDate end = LocalDate.now();
        LocalDate from = end.minusMonths(5).withDayOfMonth(1);

        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() {
                return new Object[] {
                        service.dashboard(),
                        service.monthly(from, end),
                        service.pendingSummary(),
                        service.todaySchedule()
                };
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    Object[] data = get();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> summary = (List<Map<String, Object>>) data[0];
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> revenue = (List<Map<String, Object>>) data[1];
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> pending = (List<Map<String, Object>>) data[2];
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> schedules = (List<Map<String, Object>>) data[3];

                    Map<String, Object> main = summary.isEmpty() ? Map.of() : summary.get(0);
                    todayCount.setText(String.valueOf(main.getOrDefault("Lịch hôm nay", "0")));
                    monthRevenue.setText(money(main.get("Thu tháng")));
                    patientCount.setText(String.valueOf(main.getOrDefault("Bệnh nhân", "0")));
                    staffCount.setText(String.valueOf(main.getOrDefault("Nhân viên", "0")));
                    serviceCount.setText(String.valueOf(main.getOrDefault("Dịch vụ", "0")));

                    Map<String, Object> tasks = pending.isEmpty() ? Map.of() : pending.get(0);
                    pendingAppointments.setText(String.valueOf(tasks.getOrDefault("Lịch chờ xác nhận", "0")));
                    pendingPayments.setText(String.valueOf(tasks.getOrDefault("Hóa đơn chờ thanh toán", "0")));
                    cancelRequests.setText(String.valueOf(tasks.getOrDefault("Yêu cầu hủy chờ duyệt", "0")));

                    chart.setSeries(revenue, from, end);
                    appointmentsModel.setRowCount(0);
                    for (Map<String, Object> appointment : schedules) {
                        appointmentsModel.addRow(new Object[] {
                                appointment.get("Giờ"),
                                appointment.get("Bệnh nhân"),
                                appointment.get("Bác sĩ"),
                                appointment.get("Phòng"),
                                appointment.get("Trạng thái")
                        });
                    }
                    if (schedules.isEmpty()) {
                        lastUpdate.setText("Không có lịch hẹn trong ngày");
                    } else {
                        lastUpdate.setText("Hiển thị " + schedules.size() + " lịch gần nhất");
                    }
                    status.setText("Cập nhật từ SQL MediDent • " + java.time.LocalTime.now().withNano(0));
                } catch (Exception exception) {
                    status.setText("Không tải được dashboard. Kiểm tra cấu trúc và kết nối SQL Server.");
                    GiaoDien.error(TrangChuPanel.this, exception);
                }
            }
        }.execute();
    }

    private static String money(Object amount) {
        if (!(amount instanceof Number number)) {
            return "0 ₫";
        }
        return NumberFormat.getIntegerInstance(VI).format(number) + " ₫";
    }

    /** Biểu đồ tự vẽ bằng Java2D; dữ liệu thực tế được nhận từ BaoCaoService. */
    private static final class RevenueChart extends JPanel {

        private final List<YearMonth> months = new ArrayList<>();
        private final List<BigDecimal> values = new ArrayList<>();

        private RevenueChart() {
            setBackground(GiaoDien.WHITE);
            setPreferredSize(new Dimension(500, 190));
        }

        private void setSeries(List<Map<String, Object>> data, LocalDate from, LocalDate to) {
            months.clear();
            values.clear();
            Map<YearMonth, BigDecimal> byMonth = new HashMap<>();
            for (Map<String, Object> row : data) {
                if (row.get("Năm") instanceof Number y && row.get("Tháng") instanceof Number m) {
                    Number amount = row.get("Doanh thu") instanceof Number n ? n : 0;
                    byMonth.put(YearMonth.of(y.intValue(), m.intValue()), new BigDecimal(amount.toString()));
                }
            }
            YearMonth cursor = YearMonth.from(from);
            while (!cursor.isAfter(YearMonth.from(to))) {
                months.add(cursor);
                values.add(byMonth.getOrDefault(cursor, BigDecimal.ZERO));
                cursor = cursor.plusMonths(1);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 48;
            int right = 16;
            int top = 20;
            int bottom = height - 27;
            int graphHeight = Math.max(1, bottom - top);
            int graphWidth = Math.max(1, width - left - right);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setStroke(new BasicStroke(1f));

            BigDecimal maximum = BigDecimal.ZERO;
            for (BigDecimal value : values) {
                maximum = maximum.max(value);
            }
            double scale = maximum.signum() == 0 ? 1 : maximum.doubleValue();
            for (int tick = 0; tick <= 4; tick++) {
                int y = bottom - Math.round(graphHeight * tick / 4.0f);
                g.setColor(new Color(0xE8EFF5));
                g.drawLine(left, y, left + graphWidth, y);
                g.setColor(GiaoDien.MUTED);
                String label = maximum.signum() == 0 ? "0" : compact(scale * tick / 4.0);
                g.drawString(label, 4, y + 4);
            }

            if (months.isEmpty()) {
                g.setColor(GiaoDien.MUTED);
                g.drawString("Chưa tải biểu đồ", left + 20, height / 2);
                g.dispose();
                return;
            }

            int count = months.size();
            int columnWidth = Math.max(14, graphWidth / Math.max(count, 1) - 24);
            for (int i = 0; i < count; i++) {
                int center = left + Math.round(graphWidth * (i + 0.5f) / count);
                int barHeight = (int) Math.round(graphHeight * values.get(i).doubleValue() / scale);
                int x = center - columnWidth / 2;
                int y = bottom - barHeight;
                g.setColor(new Color(0x0284C7));
                if (barHeight > 0) {
                    g.fillRoundRect(x, y, columnWidth, barHeight, 7, 7);
                }
                g.setColor(GiaoDien.MUTED);
                String text = "T" + months.get(i).getMonthValue();
                int textWidth = g.getFontMetrics().stringWidth(text);
                g.drawString(text, center - textWidth / 2, height - 9);
            }

            if (maximum.signum() == 0) {
                g.setColor(GiaoDien.MUTED);
                String message = "Chưa có doanh thu thực thu trong 6 tháng";
                g.drawString(message, left + Math.max(0, (graphWidth - g.getFontMetrics().stringWidth(message)) / 2), top + graphHeight / 2);
            }
            g.dispose();
        }

        private static String compact(double value) {
            if (value >= 1_000_000_000) {
                return String.format(VI, "%.1f tỷ", value / 1_000_000_000);
            }
            if (value >= 1_000_000) {
                return String.format(VI, "%.0f tr", value / 1_000_000);
            }
            if (value >= 1_000) {
                return String.format(VI, "%.0f k", value / 1_000);
            }
            return String.format(VI, "%.0f", value);
        }
    }

    public static void main(String[] args) {
        XemTruoc.open("Trang chủ", () -> new TrangChuPanel(null));
    }
}
