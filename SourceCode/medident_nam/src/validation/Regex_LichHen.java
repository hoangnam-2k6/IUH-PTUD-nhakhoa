package validation;

import Entity.LichHen;
import java.time.LocalDateTime;

public final class Regex_LichHen {
    private Regex_LichHen() {}

    public static void kiemTra(LichHen l) {
        if (l == null) throw new IllegalArgumentException("Thiếu thông tin lịch hẹn.");

        if (l.maBN() == null || !l.maBN().matches("BN\\d{3,}"))
            throw new IllegalArgumentException("Mã bệnh nhân phải có dạng BN001.");

        if (l.maNV() == null || !l.maNV().matches("NV\\d{3,}"))
            throw new IllegalArgumentException("Mã bác sĩ phải có dạng NV001.");

        if (l.ngayHen() == null || l.gioHen() == null)
            throw new IllegalArgumentException("Phải chọn ngày và giờ hẹn.");

        if (l.phongKham() == null || !l.phongKham().trim().matches("(?iu)Phòng\\s*\\d{1,3}"))
            throw new IllegalArgumentException("Phòng khám phải có dạng Phòng 01.");

        if (l.thoiLuongPhut() < 15 || l.thoiLuongPhut() > 240 || l.thoiLuongPhut() % 15 != 0)
            throw new IllegalArgumentException("Thời lượng từ 15 đến 240 phút, chia hết cho 15.");

        if (!LocalDateTime.of(l.ngayHen(), l.gioHen()).isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Lịch hẹn phải ở tương lai.");
    }
}