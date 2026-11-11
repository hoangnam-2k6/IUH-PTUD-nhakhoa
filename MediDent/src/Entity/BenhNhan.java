package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class BenhNhan {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private int maBN;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String soDienThoai;
    private String diaChi;
    // Thông tin mở rộng
    private String cccd;
    private String email;
    private String ngheNghiep;
    private String tenNguoiLienHe;
    private String sdtNguoiLienHe;
    private String diUng;
    private String tienSuBenh;
    private String ghiChu;
    private String trangThai = HOAT_DONG;
    private LocalDateTime ngayTao;

    public BenhNhan() {}

    public BenhNhan(int maBN, String hoTen, LocalDate ngaySinh, String gioiTinh, String soDienThoai, String diaChi) {
        this.maBN = maBN;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
        this.diaChi = diaChi;
    }

    public int getMaBN() { return maBN; }
    public void setMaBN(int maBN) { this.maBN = maBN; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNgheNghiep() { return ngheNghiep; }
    public void setNgheNghiep(String ngheNghiep) { this.ngheNghiep = ngheNghiep; }
    public String getTenNguoiLienHe() { return tenNguoiLienHe; }
    public void setTenNguoiLienHe(String tenNguoiLienHe) { this.tenNguoiLienHe = tenNguoiLienHe; }
    public String getSdtNguoiLienHe() { return sdtNguoiLienHe; }
    public void setSdtNguoiLienHe(String sdtNguoiLienHe) { this.sdtNguoiLienHe = sdtNguoiLienHe; }
    public String getDiUng() { return diUng; }
    public void setDiUng(String diUng) { this.diUng = diUng; }
    public String getTienSuBenh() { return tienSuBenh; }
    public void setTienSuBenh(String tienSuBenh) { this.tienSuBenh = tienSuBenh; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    /** Tuổi tính theo ngày sinh; null nếu chưa có ngày sinh. */
    public Integer getTuoi() {
        return ngaySinh == null ? null : Period.between(ngaySinh, LocalDate.now()).getYears();
    }

    public boolean dangHoatDong() {
        return HOAT_DONG.equals(trangThai);
    }

    @Override
    public String toString() {
        return String.format("BN%03d - %s", maBN, hoTen);
    }
}
