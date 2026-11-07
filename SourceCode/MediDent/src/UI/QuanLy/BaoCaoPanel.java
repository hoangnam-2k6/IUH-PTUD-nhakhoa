package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Dao.NghiepVu.BaoCaoService;
import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.QuyTacNghiepVu;
import UI.Chung.GiaoDien;
import UI.Chung.XemTruoc;

/**
 * Báo cáo theo ngày/tháng/năm từ một database SQL Server MediDent.
 * Bộ lọc và bốn nút ở trên cùng một hàng; toàn bộ chỉ số và bảng kết quả dùng
 * cùng khoảng ngày. Khi chưa phát sinh giao dịch, hiển thị số 0 thay vì trống.
 */
public final class BaoCaoPanel extends JPanel {

    private static final Locale VI = new Locale("vi", "VN");
    private static final String[] TYPES = {
            "Doanh thu theo ngày",
            "Doanh thu theo tháng",
            "Doanh thu theo năm",
            "Lượt khám theo ngày",
            "Lượt khám theo tháng",
            "Lượt khám theo năm"
    };

    private final BaoCaoService service;
    private final JTable table = new JTable();
    private final JLabel note = GiaoDien.label("Chọn kỳ và nhấn Áp dụng bộ lọc", 12, false);
    private final JLabel tableHeading = GiaoDien.label("Chi tiết báo cáo", 16, true);

    private final JTextField start = new JTextField(
            LocalDate.now().withDayOfMonth(1).toString());
    private final JTextField end = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> mode = new JComboBox<>(TYPES);
    private final JPanel metrics = new JPanel(new java.awt.GridLayout(1, 4, 10, 10));

    private JButton applyButton;
    private JButton monthButton;
    private JButton yearButton;
    private JButton exportButton;
    private int requestNumber;
    private ReportSnapshot applied;

    /** Snapshot giúp CSV chỉ xuất đúng dữ liệu đã áp dụng, không phải bộ lọc đang sửa dở. */
    private record ReportSnapshot(
            LocalDate from,
            LocalDate to,
            String type,
            Map<String, Object> totals,
            List<Map<String, Object>> rows) {
    }

    public BaoCaoPanel(PhanQuyen.PhienDangNhap session) {
        this.service = new BaoCaoService(session);
        setLayout(new BorderLayout(0, 16));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(22, 24, 18, 24));

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(GiaoDien.label("Báo cáo – thống kê", 25, true), BorderLayout.WEST);
        title.add(new JLabel(GiaoDien.icon("chart", 34)), BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(filters(), BorderLayout.NORTH);

        JPanel results = new JPanel(new BorderLayout(0, 14));
        results.setOpaque(false);
        metrics.setOpaque(false);
        renderMetrics(Map.of());
        results.add(metrics, BorderLayout.NORTH);

        JPanel card = GiaoDien.white(new BorderLayout(0, 12));
        card.add(tableHeading, BorderLayout.NORTH);
        GiaoDien.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setAutoCreateRowSorter(true);
        setTableColumns(TYPES[0], List.of());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(javax.swing.BorderFactory.createLineBorder(GiaoDien.BORDER));
        card.add(scroll, BorderLayout.CENTER);
        results.add(card, BorderLayout.CENTER);

        body.add(results, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(note, BorderLayout.WEST);
        footer.add(GiaoDien.label("Doanh thu = tiền đã thực thu • Lượt khám = phiếu khám", 11, false),
                BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Cả ngày, loại báo cáo và bốn nút đều nằm trên MỘT HÀNG.
     * Khi màn hình hẹp, chỉ dải nút được cuộn ngang, không chồng lên ô nhập.
     */
    private JPanel filters() {
        JPanel toolbar = GiaoDien.white(new BorderLayout(12, 0));

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        inputs.setOpaque(false);
        GiaoDien.styleTextField(start, 10);
        GiaoDien.styleTextField(end, 10);
        start.setPreferredSize(new Dimension(108, 42));
        end.setPreferredSize(new Dimension(108, 42));
        start.setToolTipText("Từ ngày yyyy-MM-dd");
        end.setToolTipText("Đến ngày yyyy-MM-dd");
        GiaoDien.styleInput(mode);
        mode.setPreferredSize(new Dimension(197, 42));

        inputs.add(GiaoDien.label("Từ ngày", 12, true));
        inputs.add(start);
        inputs.add(GiaoDien.label("Đến ngày", 12, true));
        inputs.add(end);
        inputs.add(GiaoDien.label("Loại", 12, true));
        inputs.add(mode);
        toolbar.add(inputs, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        buttons.setOpaque(false);

        applyButton = GiaoDien.button("Áp dụng bộ lọc", true);
        monthButton = GiaoDien.button("Tháng này", false);
        yearButton = GiaoDien.button("Năm nay", false);
        exportButton = GiaoDien.button("Xuất CSV", false);

        applyButton.addActionListener(e -> refresh());
        monthButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            start.setText(today.withDayOfMonth(1).toString());
            end.setText(today.toString());
            // Tháng hiện tại hiển thị các ngày, kể cả những ngày không có giao dịch.
            mode.setSelectedIndex(mode.getSelectedIndex() >= 3 ? 3 : 0);
            refresh();
        });
        yearButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            start.setText(today.withDayOfYear(1).toString());
            end.setText(today.toString());
            // Năm hiện tại chỉ có một dòng tổng hợp của năm, KHÔNG lặp các tháng.
            mode.setSelectedIndex(mode.getSelectedIndex() >= 3 ? 5 : 2);
            refresh();
        });
        exportButton.addActionListener(e -> exportCsv());

        buttons.add(applyButton);
        buttons.add(monthButton);
        buttons.add(yearButton);
        buttons.add(exportButton);

        JScrollPane actionStrip = new JScrollPane(
                buttons,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actionStrip.setBorder(null);
        actionStrip.getViewport().setBackground(GiaoDien.WHITE);
        actionStrip.setPreferredSize(new Dimension(420, 50));
        actionStrip.getHorizontalScrollBar().setUnitIncrement(24);
        toolbar.add(actionStrip, BorderLayout.CENTER);
        return toolbar;
    }

    private void renderMetrics(Map<String, Object> values) {
        metrics.removeAll();
        addMetric("Doanh thu thực thu", money(values.get("Doanh thu thực thu")), true);
        addMetric("Lượt khám", String.valueOf(values.getOrDefault("Lượt khám", 0)), false);
        addMetric("Bệnh nhân quay lại", String.valueOf(values.getOrDefault("Bệnh nhân quay lại", 0)), false);
        addMetric("Chi phí đã nhập", money(values.get("Chi phí đã nhập")), true);
        metrics.revalidate();
        metrics.repaint();
    }

    private void addMetric(String name, String value, boolean currency) {
        JPanel card = GiaoDien.white(new BorderLayout(0, 7));
        card.add(GiaoDien.label(name, 12, false), BorderLayout.NORTH);
        JLabel amount = GiaoDien.label(value, 20, true);
        amount.setForeground(currency ? GiaoDien.BLUE_DARK : GiaoDien.DARK);
        card.add(amount, BorderLayout.CENTER);
        metrics.add(card);
    }

    private static String money(Object value) {
        Number number = value instanceof Number n ? n : BigDecimal.ZERO;
        return NumberFormat.getIntegerInstance(VI).format(number) + " ₫";
    }

    /** Lấy cột theo đúng loại báo cáo; không suy luận cột từ dòng dữ liệu đầu tiên. */
    public static String[] reportColumns(String type) {
        return switch (type) {
            case "Doanh thu theo ngày" -> new String[] { "Ngày", "Giao dịch", "Doanh thu" };
            case "Doanh thu theo tháng" -> new String[] { "Năm", "Tháng", "Giao dịch", "Doanh thu" };
            case "Doanh thu theo năm" -> new String[] { "Năm", "Giao dịch", "Doanh thu" };
            case "Lượt khám theo ngày" -> new String[] { "Ngày khám", "Lượt khám" };
            case "Lượt khám theo tháng" -> new String[] { "Năm", "Tháng", "Lượt khám" };
            case "Lượt khám theo năm" -> new String[] { "Năm", "Lượt khám" };
            default -> throw new IllegalArgumentException("Loại báo cáo không hợp lệ: " + type);
        };
    }

    private void setTableColumns(String type, List<Map<String, Object>> rows) {
        String[] headers = reportColumns(type);
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Map<String, Object> row : rows) {
            Object[] record = new Object[headers.length];
            for (int i = 0; i < headers.length; i++) {
                record[i] = row.get(headers[i]);
            }
            model.addRow(record);
        }

        table.setModel(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        GiaoDien.styleTable(table);
        tableHeading.setText("Chi tiết • " + type);
    }

    public void refresh() {
        LocalDate from;
        LocalDate to;
        String type = (String) mode.getSelectedItem();

        try {
            from = QuyTacNghiepVu.date(start.getText(), "Từ ngày");
            to = QuyTacNghiepVu.date(end.getText(), "Đến ngày");
            BaoCaoService.validateReportRange(from, to, type);
        } catch (Exception ex) {
            GiaoDien.error(this, ex);
            return;
        }

        int request = ++requestNumber;
        applied = null;
        exportButton.setEnabled(false);
        note.setText("Đang tải " + type + " từ " + from + " đến " + to + "...");

        new SwingWorker<ReportSnapshot, Void>() {
            @Override
            protected ReportSnapshot doInBackground() {
                List<Map<String, Object>> totals = service.metrics(from, to);
                List<Map<String, Object>> rows = service.report(from, to, type);
                Map<String, Object> overview = totals.isEmpty() ? Map.of() : totals.get(0);
                return new ReportSnapshot(from, to, type, overview, rows);
            }

            @Override
            protected void done() {
                if (request != requestNumber) {
                    return; // Bỏ kết quả cũ khi người dùng thay đổi bộ lọc liên tiếp.
                }

                try {
                    applied = get();
                    renderMetrics(applied.totals());
                    setTableColumns(applied.type(), applied.rows());
                    note.setText("Kỳ " + applied.from() + " → " + applied.to()
                            + " • " + applied.rows().size() + " dòng • Không phát sinh = 0");
                } catch (Exception ex) {
                    applied = null;
                    note.setText("Không tải được báo cáo. Kiểm tra SQL Server và schema MediDent.");
                    GiaoDien.error(BaoCaoPanel.this, ex);
                } finally {
                    exportButton.setEnabled(applied != null);
                }
            }
        }.execute();
    }

    /**
     * Dữ liệu CSV viết theo đúng cột đang hiển thị, có BOM UTF-8 và chống công thức Excel.
     * CSV vẫn xuất được kỳ có 0 lượt khám / 0 giao dịch.
     */
    public static String buildCsv(
            String type,
            LocalDate from,
            LocalDate to,
            Map<String, Object> totals,
            List<Map<String, Object>> detail) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(csvRow("MEDIDENT - BÁO CÁO THỐNG KÊ"));
        csv.append(csvRow("Loại báo cáo", type));
        csv.append(csvRow("Từ ngày", from.toString()));
        csv.append(csvRow("Đến ngày", to.toString()));
        csv.append(csvRow("Doanh thu thực thu", String.valueOf(totals.getOrDefault("Doanh thu thực thu", 0))));
        csv.append(csvRow("Lượt khám", String.valueOf(totals.getOrDefault("Lượt khám", 0))));
        csv.append(csvRow("Bệnh nhân quay lại", String.valueOf(totals.getOrDefault("Bệnh nhân quay lại", 0))));
        csv.append(csvRow("Chi phí đã nhập", String.valueOf(totals.getOrDefault("Chi phí đã nhập", 0))));
        csv.append('\n');

        String[] columns = reportColumns(type);
        csv.append(csvRow(columns));
        for (Map<String, Object> row : detail) {
            String[] values = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                Object value = row.get(columns[i]);
                values[i] = value == null ? "" : String.valueOf(value);
            }
            csv.append(csvRow(values));
        }
        return csv.toString();
    }

    private static String csvRow(String... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            String value = fields[i] == null ? "" : fields[i];
            if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
                value = "'" + value;
            }
            line.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        return line.append('\n').toString();
    }

    private void exportCsv() {
        if (applied == null) {
            GiaoDien.ok(this, "Chưa có kết quả. Nhấn Áp dụng bộ lọc trước khi xuất CSV.");
            return;
        }

        // Không xuất nhầm báo cáo cũ nếu người dùng vừa sửa bộ lọc mà chưa Áp dụng.
        if (!applied.from().toString().equals(start.getText().trim())
                || !applied.to().toString().equals(end.getText().trim())
                || !applied.type().equals(mode.getSelectedItem())) {
            GiaoDien.ok(this, "Bộ lọc đã thay đổi. Hãy Áp dụng bộ lọc trước khi xuất CSV.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(
                "MediDent_" + applied.type().replace(' ', '_') + "_"
                        + applied.from() + "_" + applied.to() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();
        if (!file.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = Path.of(file + ".csv");
        }
        if (Files.exists(file) && JOptionPane.showConfirmDialog(this,
                "File đã tồn tại. Bạn muốn ghi đè?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String csv = buildCsv(applied.type(), applied.from(), applied.to(),
                    applied.totals(), applied.rows());
            Files.writeString(file, csv, StandardCharsets.UTF_8);
            GiaoDien.ok(this, "Đã xuất báo cáo CSV: " + file.toAbsolutePath());
        } catch (Exception ex) {
            GiaoDien.error(this, ex);
        }
    }

    public static void main(String[] args) {
        XemTruoc.open("Báo cáo - thống kê", () -> new BaoCaoPanel(null));
    }
}
