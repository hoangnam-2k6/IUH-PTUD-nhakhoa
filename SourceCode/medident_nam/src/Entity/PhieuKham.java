
package Entity;

import java.time.LocalDate;

/** Thông tin phiếu khám của bệnh nhân. */
public class PhieuKham {
    public static final String CHO_KHAM = "Chờ khám";
    public static final String DANG_KHAM = "Đang khám";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";

    private int maPK, maBN, soThuTu;
    private Integer maPhong, maLH;
    private LocalDate ngayKham;
    private String trangThai = CHO_KHAM;
    private String ghiChu, tenBenhNhan, tenPhong, maBenhNhanHienThi;

    public PhieuKham() {}

    public int getMaPK() { return maPK; }
    public void setMaPK(int maPK) { this.maPK = maPK; }

    public int getMaBN() { return maBN; }
    public void setMaBN(int maBN) { this.maBN = maBN; }

    public Integer getMaPhong() { return maPhong; }
    public void setMaPhong(Integer maPhong) { this.maPhong = maPhong; }

    public Integer getMaLH() { return maLH; }
    public void setMaLH(Integer maLH) { this.maLH = maLH; }

    public LocalDate getNgayKham() { return ngayKham; }
    public void setNgayKham(LocalDate ngayKham) { this.ngayKham = ngayKham; }

    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public String getMaBenhNhanHienThi() { return maBenhNhanHienThi; }
    public void setMaBenhNhanHienThi(String ma) { this.maBenhNhanHienThi = ma; }

    // Kiểm tra trạng thái để sử dụng trong các Panel.
    public boolean choKham() {
        return CHO_KHAM.equals(trangThai);
    }

    public boolean dangKham() {
        return DANG_KHAM.equals(trangThai);
    }

    public boolean daHuy() {
        return DA_HUY.equals(trangThai);
    }

    public boolean daKetThuc() {
        return HOAN_THANH.equals(trangThai) || DA_HUY.equals(trangThai);
    }
}
