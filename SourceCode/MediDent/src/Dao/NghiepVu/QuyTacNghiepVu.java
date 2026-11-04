package Dao.NghiepVu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public final class QuyTacNghiepVu {

    private QuyTacNghiepVu() {
    }

    public static void need(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String text(String value, String name, int max) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        need(!normalized.isEmpty(), name + " không được để trống");
        need(normalized.length() <= max, name + " tối đa " + max + " ký tự");
        return normalized;
    }

    public static String optionalText(String value, int max, String name) {
        String normalized = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        need(normalized.length() <= max, name + " tối đa " + max + " ký tự");
        return normalized;
    }

    public static String phone(String value) {
        String phone = text(value, "Số điện thoại", 20).replaceAll("[ .-]", "");
        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        }
        need(phone.matches("0(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-46-9])[0-9]{7}"), "SĐT di động Việt Nam không hợp lệ");
        return phone;
    }

    /**
     * Giữ quy tắc email đồng bộ với CK_NhanVien_Email_TenMien và
     * CK_BenhNhan_Email_TenMien trong SQL 01.
     */
    public static String email(String value) {
        String email = value == null ? "" : value.trim();
        if (email.isEmpty()) {
            return email;
        }
        need(email.length() <= 150
                && !email.contains("..")
                && email.matches("(?i)^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"),
                "Email không đúng định dạng");

        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(java.util.Locale.ROOT);
        need(Set.of("gmail.com", "yahoo.com", "yahoo.com.vn", "outlook.com",
                "hotmail.com", "live.com", "icloud.com", "proton.me",
                "protonmail.com", "medident.vn").contains(domain),
                "Email phải thuộc tên miền đã được MediDent hỗ trợ");
        return email;
    }

    public static LocalDate date(String value, String label) {
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " phải có dạng yyyy-MM-dd");
        }
    }

    public static LocalDate birth(String value) {
        LocalDate birth = date(value, "Ngày sinh");
        need(!birth.isAfter(LocalDate.now()), "Ngày sinh không được ở tương lai");
        need(!birth.isBefore(LocalDate.now().minusYears(100)), "Ngày sinh không hợp lệ");
        return birth;
    }

    public static LocalTime time(String value, String label) {
        try {
            return LocalTime.parse(value.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " phải có dạng HH:mm");
        }
    }

    public static int id(String value, String label) {
        try {
            int number = Integer.parseInt(value.trim());
            need(number > 0, label + " phải lớn hơn 0");
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " phải là số nguyên");
        }
    }

    public static int durationMinutes(String value, String label) {
        int minutes = id(value, label);
        need(minutes >= 15 && minutes <= 240, label + " phải trong khoảng 15-240 phút");
        need(minutes % 15 == 0, label + " phải là bội số của 15 phút");
        return minutes;
    }

    public static BigDecimal amount(String value) {
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            need(amount.signum() > 0, "Số tiền phải lớn hơn 0");
            need(amount.scale() <= 2, "Số tiền tối đa 2 chữ số thập phân");
            need(amount.precision() <= 18, "Số tiền vượt quá độ dài cho phép");
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Số tiền không hợp lệ");
        }
    }

    public static String oneOf(String value, String label, Set<String> allowed) {
        String text = text(value, label, 100);
        need(allowed.contains(text), label + " không hợp lệ");
        return text;
    }

    public static boolean overlap(LocalTime a, LocalTime b, LocalTime c, LocalTime d) {
        return a.isBefore(d) && b.isAfter(c);
    }

    public static boolean move(String before, String after) {
        return Map.of(
                "Chờ xác nhận", Set.of("Đã xác nhận", "Đã hủy"),
                "Đã xác nhận", Set.of("Đã đến", "Đã hủy"),
                "Đã đến", Set.of("Chờ khám", "Đã hủy"),
                "Chờ khám", Set.of("Đang khám", "Đã hủy"),
                "Đang khám", Set.of("Hoàn tất"))
                .getOrDefault(before, Set.of())
                .contains(after);
    }
}
