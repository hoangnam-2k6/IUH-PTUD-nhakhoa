package Entity;
/** Chỉ dữ liệu định danh, không lưu mật khẩu plaintext. */
public record TaiKhoan(int maTK, String tenDangNhap, String trangThai, int maVaiTro) {
}
