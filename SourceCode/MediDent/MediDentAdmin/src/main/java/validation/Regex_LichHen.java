package validation;
import Entity.LichHen;
/** Kiểm tra nhập lịch hẹn trước khi chuyển tới DAO. */
public final class Regex_LichHen {
    private Regex_LichHen() {}
    public static void kiemTra(LichHen l) {
        if (l == null || l.maBN() == null || l.maBN().isBlank() || l.maNV() == null || l.maNV().isBlank()
            || l.ngayHen() == null || l.gioHen() == null || l.phongKham() == null || l.phongKham().isBlank())
            throw new IllegalArgumentException("Phải chọn bệnh nhân, bác sĩ, ngày giờ và phòng khám.");
        if (l.thoiLuongPhut() < 15 || l.thoiLuongPhut() > 240)
            throw new IllegalArgumentException("Thời gian khám phải từ 15 đến 240 phút.");
    }
}
