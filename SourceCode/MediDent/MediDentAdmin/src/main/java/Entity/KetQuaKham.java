package Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hồ sơ kết quả khám của một phiếu khám. */
public class KetQuaKham {
    private int maPK;
    private Integer maBS;
    private String tenBacSi;
    private String trieuChung;
    private String ketQuaLamSang;
    private String chanDoan;
    private String huongDieuTri;
    private String donThuoc;
    private String ghiChu;
    private LocalDate ngayTaiKham;
    private LocalDateTime ngayCapNhat;

    public int getMaPK() { return maPK; }
    public void setMaPK(int maPK) { this.maPK = maPK; }
    public Integer getMaBS() { return maBS; }
    public void setMaBS(Integer maBS) { this.maBS = maBS; }
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    public String getTrieuChung() { return trieuChung; }
    public void setTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }
    public String getKetQuaLamSang() { return ketQuaLamSang; }
    public void setKetQuaLamSang(String ketQuaLamSang) { this.ketQuaLamSang = ketQuaLamSang; }
    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
    public String getHuongDieuTri() { return huongDieuTri; }
    public void setHuongDieuTri(String huongDieuTri) { this.huongDieuTri = huongDieuTri; }
    public String getDonThuoc() { return donThuoc; }
    public void setDonThuoc(String donThuoc) { this.donThuoc = donThuoc; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public LocalDate getNgayTaiKham() { return ngayTaiKham; }
    public void setNgayTaiKham(LocalDate ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
