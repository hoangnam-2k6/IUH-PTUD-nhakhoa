package Entity;

import java.time.LocalDateTime;

/** Tài khoản đăng nhập của nhân viên (phân hệ Lễ tân). */
public class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String hoTen;
    private String vaiTro;
    private String trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayDangNhapCuoi;

    public TaiKhoan() {}

    public TaiKhoan(int maTK, String tenDangNhap, String hoTen, String vaiTro) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
    }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public LocalDateTime getNgayDangNhapCuoi() { return ngayDangNhapCuoi; }
    public void setNgayDangNhapCuoi(LocalDateTime ngayDangNhapCuoi) { this.ngayDangNhapCuoi = ngayDangNhapCuoi; }

    public boolean laLeTan() { return "Lễ tân".equals(vaiTro); }
    public boolean laQuanTri() { return "Quản trị viên".equals(vaiTro); }
    public boolean dangHoatDong() { return "Hoạt động".equals(trangThai); }

    @Override
    public String toString() {
        return hoTen == null ? tenDangNhap : hoTen;
    }
}
