package Class;

import java.time.LocalDate;

/** Thông tin bệnh nhân đã kiểm tra hợp lệ từ form tiếp nhận. */
public final class ThongTinBenhNhan {
    public final String hoTen;
    public final LocalDate ngaySinh;
    public final String gioiTinh;
    public final String sdt;
    public final String email;     // rỗng nếu không nhập
    public final String diaChi;    // rỗng nếu không nhập
    public final String tienSu;    // rỗng nếu không nhập

    public ThongTinBenhNhan(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
            String email, String diaChi, String tienSu) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.sdt = sdt;
        this.email = email;
        this.diaChi = diaChi;
        this.tienSu = tienSu;
    }
}
