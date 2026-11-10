package validation;

import Entity.LichLamViec;

public final class Regex_LichLamViec {
    private Regex_LichLamViec() {}

    public static void kiemTra(LichLamViec l) {
        if (l == null || l.maNV() == null || !l.maNV().matches("NV\\d{3,}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ.");

        if (l.ngayLamViec() == null || l.gioBatDau() == null || l.gioKetThuc() == null
                || !l.gioBatDau().isBefore(l.gioKetThuc()))
            throw new IllegalArgumentException("Ngày làm việc hoặc khoảng giờ không hợp lệ.");

        if (l.phongKham() != null && !l.phongKham().isBlank()
                && !l.phongKham().matches("(?iu)Phòng\\s*\\d{1,3}"))
            throw new IllegalArgumentException("Phòng khám phải có dạng Phòng 01.");
    }
}