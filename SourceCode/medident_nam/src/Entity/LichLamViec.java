package Entity;

import java.time.LocalDate;
import java.time.LocalTime;

public record LichLamViec(int maLLV, String maNV, LocalDate ngayLamViec,
        LocalTime gioBatDau, LocalTime gioKetThuc, String phongKham, String trangThai) { }
