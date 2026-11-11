package Entity;
import java.time.LocalDate;
import java.time.LocalTime;
/** Dữ liệu lịch làm việc của nhân viên. */
public record LichLamViec(int maLLV, String maNV, LocalDate ngayLamViec,
    LocalTime gioBatDau, LocalTime gioKetThuc, String phongKham, String trangThai) {}
