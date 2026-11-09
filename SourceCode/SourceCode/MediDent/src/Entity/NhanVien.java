package Entity;
/** Thông tin hồ sơ nhân viên. Quản lý hiện dùng Map JDBC; record là kiểu dữ liệu để tích hợp sau. */
public record NhanVien(int maNV, String hoTen, String soDienThoai, String email, String diaChi, java.time.LocalDate ngaySinh, String gioiTinh, String chucVu, String trangThai) {
}
