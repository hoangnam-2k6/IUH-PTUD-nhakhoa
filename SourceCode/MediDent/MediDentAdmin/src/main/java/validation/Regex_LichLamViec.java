package validation;
import Entity.LichLamViec;
/** Kiểm tra dữ liệu hình thức; DAO tiếp tục kiểm tra ràng buộc ca làm. */
public final class Regex_LichLamViec {
    private Regex_LichLamViec() {}
    public static void kiemTra(LichLamViec l) {
        if (l == null || l.maNV() == null || l.maNV().isBlank() || l.ngayLamViec() == null
            || l.gioBatDau() == null || l.gioKetThuc() == null || l.trangThai() == null)
            throw new IllegalArgumentException("Thiếu thông tin ca làm việc.");
        if (!l.gioBatDau().isBefore(l.gioKetThuc()))
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc.");
    }
}
