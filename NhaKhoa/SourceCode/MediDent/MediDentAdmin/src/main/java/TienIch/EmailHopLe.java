package TienIch;

import java.util.Locale;
import java.util.Set;

/** Danh sach nha cung cap email duoc he thong MediDent chap nhan.
 *  Chi kiem tra cu phap va ten mien: KHONG xac minh hop thu co ton tai.
 */
public final class EmailHopLe {
    private EmailHopLe() {}

    private static final Set<String> TEN_MIEN = Set.of(
            "gmail.com", "yahoo.com", "yahoo.com.vn", "outlook.com",
            "hotmail.com", "live.com", "icloud.com", "proton.me",
            "protonmail.com", "medident.vn");

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

    public static String huongDan() {
        return "Email phải đúng cú pháp và có tên miền được hỗ trợ, ví dụ: "
                + "@gmail.com, @yahoo.com, @outlook.com, @hotmail.com, @icloud.com, @medident.vn.";
    }
}
