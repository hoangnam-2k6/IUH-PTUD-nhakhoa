package UI.DangNhap;
/** Thông tin lịch hẹn và thời lượng. */
public record LichHen(int maLichHen, int maBN, Integer maNV, java.time.LocalDate ngayHen, java.time.LocalTime gioHen, int thoiLuongPhut, String phongKham, String trangThai) {
}
