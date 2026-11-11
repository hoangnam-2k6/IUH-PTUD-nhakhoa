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

    public int layMaPK() { return maPK; }
    public void datMaPK(int maPK) { this.maPK = maPK; }
    public Integer layMaBS() { return maBS; }
    public void datMaBS(Integer maBS) { this.maBS = maBS; }
    public String layTenBacSi() { return tenBacSi; }
    public void datTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
    public String layTrieuChung() { return trieuChung; }
    public void datTrieuChung(String trieuChung) { this.trieuChung = trieuChung; }
    public String layKetQuaLamSang() { return ketQuaLamSang; }
    public void datKetQuaLamSang(String ketQuaLamSang) { this.ketQuaLamSang = ketQuaLamSang; }
    public String layChanDoan() { return chanDoan; }
    public void datChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
    public String layHuongDieuTri() { return huongDieuTri; }
    public void datHuongDieuTri(String huongDieuTri) { this.huongDieuTri = huongDieuTri; }
    public String layDonThuoc() { return donThuoc; }
    public void datDonThuoc(String donThuoc) { this.donThuoc = donThuoc; }
    public String layGhiChu() { return ghiChu; }
    public void datGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public LocalDate layNgayTaiKham() { return ngayTaiKham; }
    public void datNgayTaiKham(LocalDate ngayTaiKham) { this.ngayTaiKham = ngayTaiKham; }
    public LocalDateTime layNgayCapNhat() { return ngayCapNhat; }
    public void datNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
