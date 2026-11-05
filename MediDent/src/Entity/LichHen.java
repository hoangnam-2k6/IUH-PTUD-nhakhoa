package Entity;

import java.time.LocalDateTime;

public class LichHen {
    private int maLH;
    private int maBN;
    private int maBS;
    private LocalDateTime ngayGio;
    private String trangThai;
    private String yeuCau;
    private LocalDateTime ngayGioMoi;
    private String ghiChu;
    private String tenBenhNhan;
    private String tenBacSi;

    public LichHen() {}

    public int getMaLH() { return maLH; }
    public void setMaLH(int maLH) { this.maLH = maLH; }
    public int getMaBN() { return maBN; }
    public void setMaBN(int maBN) { this.maBN = maBN; }
    public int getMaBS() { return maBS; }
    public void setMaBS(int maBS) { this.maBS = maBS; }
    public LocalDateTime getNgayGio() { return ngayGio; }
    public void setNgayGio(LocalDateTime ngayGio) { this.ngayGio = ngayGio; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getYeuCau() { return yeuCau; }
    public void setYeuCau(String yeuCau) { this.yeuCau = yeuCau; }
    public LocalDateTime getNgayGioMoi() { return ngayGioMoi; }
    public void setNgayGioMoi(LocalDateTime ngayGioMoi) { this.ngayGioMoi = ngayGioMoi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }
}
