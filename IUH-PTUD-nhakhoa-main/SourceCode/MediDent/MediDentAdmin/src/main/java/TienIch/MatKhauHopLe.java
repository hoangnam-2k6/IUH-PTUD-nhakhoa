package TienIch;

/** Chính sách mật khẩu thống nhất với màn hình đổi mật khẩu bắt buộc. */
public final class MatKhauHopLe {
    private MatKhauHopLe() { }

    public static boolean kiemTra(String matKhau) {
        return matKhau != null && matKhau.length() >= 10
                && matKhau.length() <= 72
                && matKhau.matches("(?s).*[A-Z].*")
                && matKhau.matches("(?s).*\\d.*");
    }

    public static String huongDan() {
        return "Mật khẩu cần 10-72 ký tự, có ít nhất một chữ hoa và một chữ số.";
    }
}
