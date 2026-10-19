/*
 * TaiKhoan: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

public class TaiKhoan {

    public int id;
    public int vaiTroId;
    public String tenDangNhap;
    public String hoTen;
    public String email;
    public String tenVaiTro;
    public String trangThai;
    public boolean phaiDoiMatKhau;
    public String ngayTao;

    /** Tải lại dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public TaiKhoan(int id, String tenDangNhap, String hoTen, String email,
                    int vaiTroId, String tenVaiTro, String trangThai) {
        this(id,tenDangNhap,hoTen,email,vaiTroId,tenVaiTro,trangThai,false);
    }

    /** Tải lại dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public TaiKhoan(int id, String tenDangNhap, String hoTen, String email,
                    int vaiTroId, String tenVaiTro, String trangThai, boolean phaiDoiMatKhau) {
        this.id = id;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.email = email;
        this.vaiTroId = vaiTroId;
        this.tenVaiTro = tenVaiTro;
        this.trangThai = trangThai;
        this.phaiDoiMatKhau = phaiDoiMatKhau;
    }
}
