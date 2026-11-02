import TienIch.EmailHopLe;
import TienIch.SoDienThoaiHopLe;
import TienIch.MatKhauHopLe;
import java.lang.reflect.Method;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Test nhỏ bằng Java thuần. Không cần JUnit, không ghi SQL.
 * Chạy: java -ea KiemTraNhapLieu (sau khi compile với source project).
 */
public class KiemTraNhapLieu {
    private static int total;

    private static void check(boolean result, String message) {
        total++;
        if (!result) throw new AssertionError("Sai test: " + message);
    }

    @SuppressWarnings("unchecked")
    private static void testSearch() throws Exception {
        // Chỉ kiểm tra bộ lọc của giao diện Lễ tân, không mở SQL Server.
        Method filter = Class.forName("UI.LeTan.TrangChuLeTan")
                .getDeclaredMethod("filter", TableRowSorter.class, String.class, boolean.class);
        filter.setAccessible(true);

        DefaultTableModel patients = new DefaultTableModel(
                new Object[] {"STT", "Mã BN", "Họ tên", "SĐT"}, 0);
        patients.addRow(new Object[] {0, 101, "Mai Lan", "0912345678"});
        patients.addRow(new Object[] {0, 1010, "Lê Hoa", "0987654321"});
        TableRowSorter<DefaultTableModel> p = new TableRowSorter<>(patients);
        filter.invoke(null, p, "BN 101", false);
        check(p.getViewRowCount() == 1 && patients.getValueAt(p.convertRowIndexToModel(0), 1).equals(101),
                "BN 101 trả đúng 101, không lẫn 1010");
        DefaultTableModel noZero = new DefaultTableModel(
                new Object[] {"STT", "Mã BN", "Tên"}, 0);
        noZero.addRow(new Object[] {0, 111, "Mai Lan"});
        noZero.addRow(new Object[] {0, 222, "Lê Hoa"});
        TableRowSorter<DefaultTableModel> noZeroSorter = new TableRowSorter<>(noZero);
        filter.invoke(null, noZeroSorter, "0", false);
        check(noZeroSorter.getViewRowCount() == 0,
                "Ký tự 0 không được khớp STT ẩn ở cả hai dòng");
        filter.invoke(null, p, "Bán", false);
        check(p.getViewRowCount() == 0, "Tìm chữ không có");

        DefaultTableModel tickets = new DefaultTableModel(
                new Object[] {"STT", "Mã phiếu", "Mã BN", "Tên"}, 0);
        tickets.addRow(new Object[] {0, 10, 101, "Mai Lan"});
        tickets.addRow(new Object[] {0, 100, 1010, "Lê Hoa"});
        TableRowSorter<DefaultTableModel> t = new TableRowSorter<>(tickets);
        filter.invoke(null, t, "PK10", true);
        check(t.getViewRowCount() == 1, "PK10 không khớp PK100");
        filter.invoke(null, t, "BN101", true);
        check(t.getViewRowCount() == 1, "BN101 khớp mã bệnh nhân trong bảng phiếu khám");
    }

    public static void main(String[] args) throws Exception {
        check(SoDienThoaiHopLe.kiemTra("0912345678"), "SĐT 0912...");
        check(SoDienThoaiHopLe.kiemTra("0381234567"), "SĐT 038...");
        check(!SoDienThoaiHopLe.kiemTra("0112345678"), "Đầu 01 bị loại");
        check(!SoDienThoaiHopLe.kiemTra("091234567"), "9 chữ số bị loại");
        check(!SoDienThoaiHopLe.kiemTra("09123abc78"), "Ký tự chữ bị loại");
        check(EmailHopLe.kiemTra("demo@gmail.com"), "Gmail đúng");
        check(EmailHopLe.kiemTra("demo@yahoo.com"), "Yahoo đúng");
        check(!EmailHopLe.kiemTra("demo@@gmail.com"), "Hai @ bị loại");
        check(!EmailHopLe.kiemTra("demo@gmail..com"), "Hai dấu chấm liên tiếp bị loại");
        check(!EmailHopLe.kiemTra("demo@khongbiet.com"), "Tên miền chưa cho phép bị loại");
        check(MatKhauHopLe.kiemTra("Matkhau1234"), "Mật khẩu đủ mạnh");
        check(!MatKhauHopLe.kiemTra("matkhau1234"), "Mật khẩu thiếu chữ hoa");
        testSearch();
        System.out.println("PASS " + total + " test ngoại tuyến; chưa chạy SQL Server.");
    }
}
