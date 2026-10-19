/*
 * EmailHopLe: các tiện ích xác thực dữ liệu.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package TienIch;

import java.util.Locale;
import java.util.Set;

public final class EmailHopLe {
    /** Ngăn khởi tạo lớp tiện ích chỉ chứa quy tắc kiểm tra địa chỉ email. */
    private EmailHopLe() {}

    private static final Set<String> TEN_MIEN = Set.of(
            "gmail.com", "yahoo.com", "yahoo.com.vn", "outlook.com",
            "hotmail.com", "live.com", "icloud.com", "proton.me",
            "protonmail.com", "medident.vn");

    /** Đối chiếu điều kiện hợp lệ trước khi thực hiện thao tác. */
    public static boolean kiemTra(String email) {
        if (email == null) return false;
        String value = email.trim();
        if (value.isEmpty() || value.length() > 254 || value.contains("..") || value.contains(" ")) return false;
        int at = value.indexOf('@');
        if (at < 1 || at != value.lastIndexOf('@')) return false;
        String local = value.substring(0, at);
        String domain = value.substring(at + 1).toLowerCase(Locale.ROOT);
        if (local.length() > 64 || local.startsWith(".") || local.endsWith(".")) return false;
        if (!local.matches("[A-Za-z0-9._%+\\-]+")) return false;
        return TEN_MIEN.contains(domain);
    }

    /** Trả thông báo hướng dẫn kiểm tra định dạng email. */
    public static String huongDan() {
        return "Email phải đúng cú pháp và có tên miền được hỗ trợ, ví dụ: "
                + "@gmail.com, @yahoo.com, @outlook.com, @hotmail.com, @icloud.com, @medident.vn.";
    }
}
