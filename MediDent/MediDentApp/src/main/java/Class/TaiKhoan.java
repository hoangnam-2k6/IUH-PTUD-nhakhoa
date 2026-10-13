package Class;

/**
 * Đối tượng tài khoản dùng để hiển thị dữ liệu nhận từ SQL Server.
 *
 * maTK/maVaiTro trong CSDL được đặt là id/vaiTroId ở tầng Java.
 * Họ tên và email đến từ bảng NhanVien, không nằm trong TaiKhoan.
 */
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

    public TaiKhoan(int id, String tenDangNhap, String hoTen, String email,
                    int vaiTroId, String tenVaiTro, String trangThai) {
        this(id,tenDangNhap,hoTen,email,vaiTroId,tenVaiTro,trangThai,false);
    }

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
