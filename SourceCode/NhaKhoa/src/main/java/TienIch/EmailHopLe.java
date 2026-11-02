package TienIch;

import java.util.regex.Pattern;

/** Kiểm tra cú pháp email chung, không hạn chế nhà cung cấp (không xác minh hộp thư). */
public final class EmailHopLe {
    private EmailHopLe() { }
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9_+%\\-]+(?:\\.[A-Za-z0-9_+%\\-]+)*@"
          + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
          + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");

    public static boolean kiemTra(String email) {
        if (email == null) return false;
        String value = email.trim();
        if (!value.equals(email) || value.length() > 254 || value.isEmpty()) return false;
        int at = value.indexOf('@');
        return at > 0 && at <= 64 && EMAIL.matcher(value).matches();
    }

    public static String huongDan() {
        return "Email phải có cú pháp hợp lệ (ví dụ: ten@congty.vn), tối đa 254 ký tự; "
                + "không giới hạn Gmail, Outlook hoặc tên miền của tổ chức.";
    }
}
