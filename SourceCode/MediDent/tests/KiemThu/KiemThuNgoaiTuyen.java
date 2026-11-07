package KiemThu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import Dao.NghiepVu.QuyTacNghiepVu;
import Dao.NghiepVu.BaoCaoService;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.GiaoDien;
import UI.DangNhap.DangNhap;
import UI.QuanLy.BaoCaoPanel;
import UI.QuanLy.BenhNhanPanel;
import UI.QuanLy.DichVuPanel;
import UI.QuanLy.LichHenPanel;
import UI.QuanLy.NhanVienPanel;
import UI.QuanLy.ThanhToanPanel;
import UI.QuanLy.TrangChuPanel;

/**
 * Chạy độc lập KHÔNG CẦN SQL Server:
 * kiểm tra các quy tắc Java, định dạng UI, sự tồn tại của bảy màn hình.
 * Không thay thế kiểm thử CRUD tích hợp bằng SQL Server thật.
 */
public final class KiemThuNgoaiTuyen {

    private static int passed;
    private static int failed;

    private KiemThuNgoaiTuyen() {
    }

    private static void pass(String title, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.err.println("[FAIL] " + title);
        }
    }

    private static void bad(String title, Runnable command) {
        boolean rejected = false;
        try {
            command.run();
        } catch (IllegalArgumentException ex) {
            rejected = true;
        }
        pass(title, rejected);
    }

    private static void good(String title, Runnable command) {
        try {
            command.run();
            pass(title, true);
        } catch (Throwable ex) {
            failed++;
            System.err.println("[FAIL] " + title + " :: " + ex.getMessage());
        }
    }

    private static JButton findButton(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && name.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                JButton found = findButton(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        // Văn bản và email đồng bộ whitelist CSDL.
        good("Tên hợp lệ", () -> QuyTacNghiepVu.text("  Trần Minh Trang  ", "Họ tên", 120));
        pass("Chuẩn hóa khoảng trắng", QuyTacNghiepVu.text("  A   B  ", "Tên", 12).equals("A B"));
        bad("Tên rỗng", () -> QuyTacNghiepVu.text("  ", "Tên", 10));
        bad("Tên vượt chiều dài", () -> QuyTacNghiepVu.text("12345678901", "Tên", 10));
        good("Mô tả bỏ trống", () -> QuyTacNghiepVu.optionalText(null, 10, "Mô tả"));
        bad("Mô tả vượt chiều dài", () -> QuyTacNghiepVu.optionalText("12345678901", 10, "Mô tả"));

        good("SĐT 09", () -> QuyTacNghiepVu.phone("0912345678"));
        good("SĐT 03", () -> QuyTacNghiepVu.phone("0321234567"));
        good("SĐT +84", () -> QuyTacNghiepVu.phone("+84912345678"));
        pass("Chuẩn hóa +84", QuyTacNghiepVu.phone("+84912345678").equals("0912345678"));
        good("SĐT có dấu cách", () -> QuyTacNghiepVu.phone("091 234 5678"));
        bad("SĐT thiếu số", () -> QuyTacNghiepVu.phone("091234567"));
        bad("SĐT sai đầu", () -> QuyTacNghiepVu.phone("0012345678"));
        bad("SĐT toàn ký tự", () -> QuyTacNghiepVu.phone("abcdefghij"));
        bad("SĐT bỏ trống", () -> QuyTacNghiepVu.phone(""));

        good("Email hợp lệ Gmail", () -> QuyTacNghiepVu.email("abc@gmail.com"));
        good("Email hợp lệ Outlook", () -> QuyTacNghiepVu.email("abc@outlook.com"));
        good("Email hợp lệ doanh nghiệp", () -> QuyTacNghiepVu.email("abc@medident.vn"));
        good("Email không bắt buộc", () -> QuyTacNghiepVu.email(""));
        bad("Email thiếu @", () -> QuyTacNghiepVu.email("abc.gmail.com"));
        bad("Email sai tên miền SQL", () -> QuyTacNghiepVu.email("abc@example.com"));
        bad("Email có hai chấm", () -> QuyTacNghiepVu.email("abc..x@gmail.com"));
        bad("Email sai dấu cách", () -> QuyTacNghiepVu.email("abc @gmail.com"));

        good("Ngày ISO hợp lệ", () -> QuyTacNghiepVu.date("2026-01-19", "Ngày"));
        bad("Ngày sai định dạng", () -> QuyTacNghiepVu.date("19/01/2026", "Ngày"));
        bad("Ngày 31/02", () -> QuyTacNghiepVu.date("2026-02-31", "Ngày"));
        bad("Ngày rỗng", () -> QuyTacNghiepVu.date("", "Ngày"));
        good("Ngày sinh hiện tại", () -> QuyTacNghiepVu.birth(LocalDate.now().toString()));
        bad("Ngày sinh tương lai", () -> QuyTacNghiepVu.birth(LocalDate.now().plusDays(1).toString()));
        bad("Ngày sinh trên 100 năm", () -> QuyTacNghiepVu.birth(LocalDate.now().minusYears(101).toString()));

        good("Giờ HH:mm", () -> QuyTacNghiepVu.time("08:30", "Giờ"));
        bad("Giờ 25:30", () -> QuyTacNghiepVu.time("25:30", "Giờ"));
        bad("Giờ abc", () -> QuyTacNghiepVu.time("abc", "Giờ"));
        good("Thời lượng 15", () -> QuyTacNghiepVu.durationMinutes("15", "Phút"));
        good("Thời lượng 60", () -> QuyTacNghiepVu.durationMinutes("60", "Phút"));
        good("Thời lượng 240", () -> QuyTacNghiepVu.durationMinutes("240", "Phút"));
        bad("Thời lượng 0", () -> QuyTacNghiepVu.durationMinutes("0", "Phút"));
        bad("Thời lượng 14", () -> QuyTacNghiepVu.durationMinutes("14", "Phút"));
        bad("Thời lượng 241", () -> QuyTacNghiepVu.durationMinutes("241", "Phút"));
        bad("Thời lượng không chia hết cho 15", () -> QuyTacNghiepVu.durationMinutes("61", "Phút"));

        good("Đơn giá số dương", () -> QuyTacNghiepVu.amount("250000"));
        good("Đơn giá hai số lẻ", () -> QuyTacNghiepVu.amount("250000.50"));
        bad("Đơn giá không hợp lệ", () -> QuyTacNghiepVu.amount("xx"));
        bad("Đơn giá bằng 0", () -> QuyTacNghiepVu.amount("0"));
        bad("Đơn giá âm", () -> QuyTacNghiepVu.amount("-1"));
        bad("Đơn giá quá hai chữ số lẻ", () -> QuyTacNghiepVu.amount("1.234"));
        bad("Đơn giá quá lớn", () -> QuyTacNghiepVu.amount("1234567890123456789"));

        good("ID dương", () -> QuyTacNghiepVu.id("19", "ID"));
        bad("ID bằng 0", () -> QuyTacNghiepVu.id("0", "ID"));
        bad("ID âm", () -> QuyTacNghiepVu.id("-3", "ID"));
        bad("ID chứa ký tự", () -> QuyTacNghiepVu.id("19x", "ID"));

        pass("Xung đột khoảng 8-9 và 8:30-9:30", QuyTacNghiepVu.overlap(
                LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(8, 30), LocalTime.of(9, 30)));
        pass("Giáp ranh 9:00 không trùng", !QuyTacNghiepVu.overlap(
                LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(9, 0), LocalTime.of(10, 0)));
        pass("Khoảng tách biệt", !QuyTacNghiepVu.overlap(
                LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0)));
        pass("Chờ xác nhận -> Đã xác nhận", QuyTacNghiepVu.move("Chờ xác nhận", "Đã xác nhận"));
        pass("Chờ xác nhận -> Đã hủy", QuyTacNghiepVu.move("Chờ xác nhận", "Đã hủy"));
        pass("Đang khám -> Hoàn tất", QuyTacNghiepVu.move("Đang khám", "Hoàn tất"));
        pass("Không bỏ bước sang Hoàn tất", !QuyTacNghiepVu.move("Chờ xác nhận", "Hoàn tất"));
        pass("Không sửa lịch Hoàn tất", !QuyTacNghiepVu.move("Hoàn tất", "Đã xác nhận"));
        pass("Không mở lại lịch Đã hủy", !QuyTacNghiepVu.move("Đã hủy", "Đã xác nhận"));
        good("Giá trị thuộc tập hợp", () -> QuyTacNghiepVu.oneOf("Nam", "Giới tính", Set.of("Nam", "Nữ")));
        bad("Giới tính sai", () -> QuyTacNghiepVu.oneOf("Không xác định", "Giới tính", Set.of("Nam", "Nữ")));

        // Báo cáo không phụ thuộc SQL: kiểm tra cấu trúc, kỳ 0 và CSV.
        String[] types = {
                "Doanh thu theo ngày", "Doanh thu theo tháng", "Doanh thu theo năm",
                "Lượt khám theo ngày", "Lượt khám theo tháng", "Lượt khám theo năm"
        };
        for (String type : types) {
            good("Đầy đủ cột cho " + type, () -> {
                if (BaoCaoPanel.reportColumns(type).length < 2) {
                    throw new IllegalArgumentException("Thiếu cột");
                }
            });
        }
        good("Khoảng lọc hợp lệ", () -> BaoCaoService.validateReportRange(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 4), "Doanh thu theo ngày"));
        bad("Khoảng lọc ngược", () -> BaoCaoService.validateReportRange(
                LocalDate.of(2026, 12, 3), LocalDate.of(2026, 11, 1), "Doanh thu theo ngày"));
        bad("Báo cáo ngày vượt 366", () -> BaoCaoService.validateReportRange(
                LocalDate.of(2020, 1, 1), LocalDate.of(2023, 1, 1), "Doanh thu theo ngày"));
        bad("Loại báo cáo sai", () -> BaoCaoService.validateReportRange(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1), "Loại không tồn tại"));

        LocalDate first = LocalDate.of(2026, 11, 1);
        LocalDate last = LocalDate.of(2026, 11, 4);
        List<Map<String, Object>> dailyZero = BaoCaoService.completePeriods(first, last,
                "Doanh thu theo ngày", List.of());
        pass("Tháng không giao dịch vẫn có ngày", dailyZero.size() == 4);
        pass("Ngày không có doanh thu bằng 0", BigDecimal.ZERO.compareTo(
                (BigDecimal) dailyZero.get(1).get("Doanh thu")) == 0);
        pass("Ngày trong kỳ được sắp đúng", dailyZero.get(0).get("Ngày").equals("2026-11-01"));

        List<Map<String, Object>> visitsZero = BaoCaoService.completePeriods(first, last,
                "Lượt khám theo ngày", List.of());
        pass("Lượt khám mỗi ngày không có dữ liệu vẫn bằng 0",
                visitsZero.size() == 4 && (int) visitsZero.get(3).get("Lượt khám") == 0);

        List<Map<String, Object>> monthlyZero = BaoCaoService.completePeriods(first, last,
                "Doanh thu theo tháng", List.of());
        pass("Tháng này hiện một dòng tổng hợp tháng", monthlyZero.size() == 1
                && monthlyZero.get(0).get("Tháng").equals(11));

        List<Map<String, Object>> annualZero = BaoCaoService.completePeriods(
                LocalDate.of(2026, 1, 1), last, "Doanh thu theo năm", List.of());
        pass("Năm nay chỉ hiện năm, không lặp 12 tháng", annualZero.size() == 1
                && annualZero.get(0).containsKey("Năm") && !annualZero.get(0).containsKey("Tháng"));

        Map<String, Object> realDay = new LinkedHashMap<>();
        realDay.put("Ngày", "2026-11-02");
        realDay.put("Giao dịch", 2);
        realDay.put("Doanh thu", new BigDecimal("125000.00"));
        List<Map<String, Object>> filled = BaoCaoService.completePeriods(first, last,
                "Doanh thu theo ngày", List.of(realDay));
        pass("Bảo toàn doanh thu SQL ở ngày có giao dịch", new BigDecimal("125000.00").equals(
                filled.get(1).get("Doanh thu")));
        pass("Kỳ không giao dịch vẫn bằng 0 xen giữa ngày có giao dịch",
                ((Number) filled.get(2).get("Giao dịch")).intValue() == 0);

        String csv = BaoCaoPanel.buildCsv("Doanh thu theo ngày", first, last,
                Map.of("Doanh thu thực thu", new BigDecimal("125000.00"), "Lượt khám", 1), filled);
        pass("CSV có BOM UTF-8", csv.startsWith("\uFEFF"));
        pass("CSV có đúng kiểu báo cáo và khoảng lọc", csv.contains("2026-11-01")
                && csv.contains("Doanh thu theo ngày"));
        pass("CSV có tiêu đề và số tiền", csv.contains("Giao dịch") && csv.contains("125000.00"));
        String csvEmpty = BaoCaoPanel.buildCsv("Lượt khám theo ngày", first, last,
                Map.of(), List.of());
        pass("CSV xuất được dù không có dữ liệu", csvEmpty.contains("Ngày khám")
                && csvEmpty.contains("\uFEFF"));
        Map<String, Object> escaped = new LinkedHashMap<>();
        escaped.put("Ngày", "=DANGEROUS\"x");
        escaped.put("Giao dịch", 1);
        escaped.put("Doanh thu", BigDecimal.ONE);
        String safeCsv = BaoCaoPanel.buildCsv("Doanh thu theo ngày", first, last,
                Map.of(), List.of(escaped));
        pass("CSV chống Excel formula injection", safeCsv.contains("'=DANGEROUS"));
        pass("CSV escape dấu nháy kép", safeCsv.contains("\"\""));

        SwingUtilities.invokeAndWait(() -> {
            GiaoDien.setup();
            JPanel[] screens = {
                    new TrangChuPanel(null),
                    new NhanVienPanel(null),
                    new DichVuPanel(null),
                    new BenhNhanPanel(null),
                    new LichHenPanel(null),
                    new ThanhToanPanel(null),
                    new BaoCaoPanel(null)
            };
            for (int i = 0; i < screens.length; i++) {
                pass("JPanel thứ " + (i + 1) + " khởi tạo không SQL", screens[i] != null);
            }
            // Tất cả bảng nghiệp vụ bắt đầu với mã ID để chọn bản ghi chính xác.
            for (int i = 1; i <= 5; i++) {
                try {
                    var f = BangDuLieuPanel.class.getDeclaredField("table");
                    f.setAccessible(true);
                    JTable grid = (JTable) f.get(screens[i]);
                    pass("ID là cột đầu bảng nghiệp vụ " + i, "ID".equals(grid.getColumnName(0)));
                } catch (Exception ex) {
                    pass("Không đọc được ID cột đầu " + i, false);
                }
            }
            pass("Xuất CSV có nút riêng trên báo cáo",
                    findButton(screens[6], "Xuất CSV") != null);
            pass("Bộ lọc trên một hàng",
                    findButton(screens[6], "Tháng này") != null
                            && findButton(screens[6], "Năm nay") != null
                            && findButton(screens[6], "Áp dụng bộ lọc") != null);
            pass("Mở riêng không cho Thêm nhân viên khi chưa đăng nhập",
                    !findButton(screens[1], "+ Thêm nhân viên").isEnabled());
            pass("Mở riêng không cho Tạo lịch hẹn khi chưa đăng nhập",
                    !findButton(screens[4], "+ Tạo lịch hẹn").isEnabled());
            pass("Mở riêng không cho Tạo thanh toán khi chưa đăng nhập",
                    !findButton(screens[5], "+ Tạo thanh toán").isEnabled());

            JButton primary = GiaoDien.button("+ Thêm", true);
            primary.setSize(140, 44);
            BufferedImage image = new BufferedImage(140, 44, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            primary.paint(graphics);
            graphics.dispose();
            pass("Nút chính có nền xanh thực sự", new Color(image.getRGB(7, 7), true).getBlue() > 95);
            pass("Nút chính có chữ trắng", primary.getForeground().equals(Color.WHITE));

            JTable table = new JTable(new DefaultTableModel(
                    new Object[][]{{"Đã thanh toán", new BigDecimal("250000.00")}},
                    new String[]{"Trạng thái", "Số tiền"}));
            GiaoDien.styleTable(table);
            TableCellRenderer renderer = table.getCellRenderer(0, 1);
            Component money = renderer.getTableCellRendererComponent(table, new BigDecimal("250000"), false, false, 0, 1);
            pass("Tiền được định dạng VND", ((JLabel) money).getText().contains("250.000"));
            Component status = renderer.getTableCellRendererComponent(table, "Đã thanh toán", false, false, 0, 0);
            pass("Trạng thái có nền màu", !status.getBackground().equals(Color.WHITE));

            // Kiểm thử bảng ít cột phải tự lấp chiều ngang, không còn vùng trắng bên phải.
            try {
                BangDuLieuPanel small = new BangDuLieuPanel(
                        "Kiểm thử bảng", "Tự giãn", "staff",
                        new String[] { "Tên dịch vụ", "Đơn giá", "Trạng thái" }, false, List::of);
                var field = BangDuLieuPanel.class.getDeclaredField("table");
                field.setAccessible(true);
                JTable grid = (JTable) field.get(small);
                JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, grid);
                JViewport viewport = scroller.getViewport();
                viewport.setExtentSize(new Dimension(1030, 500));
                var fit = BangDuLieuPanel.class.getDeclaredMethod("fitColumns", JViewport.class);
                fit.setAccessible(true);
                fit.invoke(small, viewport);
                pass("Bảng ít cột tự giãn kín 1030px",
                        Math.abs(grid.getColumnModel().getTotalColumnWidth() - 1030) <= 2);
                pass("Cột tên dịch vụ giãn rộng",
                        grid.getColumnModel().getColumn(0).getWidth() > 165);

                BangDuLieuPanel crowded = new BangDuLieuPanel(
                        "Kiểm thử nhiều cột", "Cuộn ngang", "staff",
                        new String[] { "Họ tên", "Chức vụ", "Trạng thái", "SĐT", "Ngày sinh",
                                "Tuổi", "Email", "Địa chỉ", "Giới tính", "ID" }, false, List::of);
                JTable grid2 = (JTable) field.get(crowded);
                JScrollPane scroller2 = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, grid2);
                JViewport viewport2 = scroller2.getViewport();
                viewport2.setExtentSize(new Dimension(720, 500));
                fit.invoke(crowded, viewport2);
                pass("Bảng nhiều cột giữ chiều rộng tối thiểu và cuộn ngang",
                        grid2.getColumnModel().getTotalColumnWidth() > 720);
            } catch (Exception ex) {
                failed++;
                System.err.println("[FAIL] Bảng tự giãn: " + ex.getMessage());
            }

            // Sidebar có nền khác vùng nội dung nhưng vẫn đủ sáng để đọc rõ chữ.
            pass("Sidebar sử dụng tông màu xanh nhạt", !GiaoDien.NAV_BG.equals(Color.WHITE)
                    && GiaoDien.NAV_BG.getRed() >= 220);
            pass("Màu đang chọn khác nền sidebar", !GiaoDien.NAV_ACTIVE.equals(GiaoDien.NAV_BG));
            // Chỉ hiển thị Đăng nhập/Đăng ký: kiểm tra SQL thuộc ConnectDB, không còn nút trên màn hình.
            DangNhap loginScreen = new DangNhap();
            pass("Đã bỏ nút Kiểm tra SQL khỏi Đăng nhập",
                    findButton(loginScreen.getContentPane(), "Kiểm tra kết nối và cấu trúc SQL") == null);
            loginScreen.dispose();


            // Kiểm tra client-side validation không gửi SQL khi dữ liệu sai.
            BangDuLieuPanel form = new BangDuLieuPanel(
                    "Kiểm thử", "Form", "staff", new String[]{"ID"}, false, List::of);
            try {
                Method method = BangDuLieuPanel.class.getDeclaredMethod(
                        "validateForm", String.class, Map.class, Map.class);
                method.setAccessible(true);
                Map<String, String> wrong = new LinkedHashMap<>();
                wrong.put("Họ tên", "Trần Minh Trang");
                wrong.put("SĐT", "abc");
                Map<String, JLabel> errors = new LinkedHashMap<>();
                errors.put("Họ tên", new JLabel());
                errors.put("SĐT", new JLabel());
                String invalid = (String) method.invoke(form, "Thêm nhân viên", wrong, errors);
                pass("Form từ chối SĐT sai trước SQL", "SĐT".equals(invalid));
                wrong.put("SĐT", "0912345678");
                String valid = (String) method.invoke(form, "Thêm nhân viên", wrong, errors);
                pass("Form chấp nhận dữ liệu đúng", valid == null);
            } catch (Exception ex) {
                failed++;
                System.err.println("[FAIL] Form validation: " + ex.getMessage());
            }
        });

        System.out.println("MEDIDENT OFFLINE QA: " + passed + " DAT / " + (passed + failed) + " TRUONG HOP");
        if (failed != 0) {
            System.exit(1);
        }
    }
}
