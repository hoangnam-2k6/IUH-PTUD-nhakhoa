package TienIch;

/**
 * Kiểm tra số di động Việt Nam và chuẩn hóa về 10 chữ số bắt đầu bằng 0.
 * Chấp nhận +84/84, dấu cách, dấu chấm và dấu gạch ngang để hỗ trợ nhập liệu.
 */
public final class SoDienThoaiHopLe {
    private SoDienThoaiHopLe() { }

    public static String chuanHoa(String duLieu) {
        if (duLieu == null) throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        String so = duLieu.trim().replaceAll("[.\\s-]", "");
        if (so.startsWith("+84")) so = "0" + so.substring(3);
        else if (so.startsWith("84") && so.length() == 11) so = "0" + so.substring(2);
        if (!so.matches("0(?:3[2-9]|5[25689]|7[06-9]|8[1-9]|9[0-46-9])[0-9]{7}")) {
            throw new IllegalArgumentException("Số di động Việt Nam phải có 10 chữ số và đầu số hợp lệ.");
        }
        return so;
    }

    public static boolean kiemTra(String duLieu) {
        try { chuanHoa(duLieu); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }
}
