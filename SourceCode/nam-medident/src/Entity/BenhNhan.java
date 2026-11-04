package Entity;
/** Thông tin hồ sơ bệnh nhân. Bản hiện tại chưa ánh xạ trực tiếp vào record này. */
public record BenhNhan(int maBN, String hoTen, String soDienThoai, String email, String diaChi, java.time.LocalDate ngaySinh, String gioiTinh, String trangThaiHoSo) {
}
