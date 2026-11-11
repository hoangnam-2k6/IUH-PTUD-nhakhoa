package validation;
/** Kiểm tra các tham số gửi yêu cầu hủy hóa đơn. */
public final class Regex_HoaDon {
    private Regex_HoaDon() {}
    public static void kiemTraMa(int ma) {
        if (ma <= 0) throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
    }
    public static void kiemTraLyDo(String lyDo) {
        if (lyDo == null || lyDo.trim().isEmpty())
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy.");
    }
}
