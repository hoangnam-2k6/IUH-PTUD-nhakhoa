package validation;

public final class Regex_HoaDon {
    private Regex_HoaDon() {}

    public static void kiemTraMa(int ma) {
        if (ma <= 0) throw new IllegalArgumentException("Mã chứng từ phải lớn hơn 0.");
    }

    public static void kiemTraLyDo(String lyDo) {
        if (lyDo == null || !lyDo.trim().matches("(?s).{10,500}"))
            throw new IllegalArgumentException("Lý do hủy cần từ 10 đến 500 ký tự.");
    }
}