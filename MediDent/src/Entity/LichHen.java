package Entity;

import java.time.LocalDateTime;

public class LichHen {
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_DEN = "Đã đến";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";

    public static final String DOI_LICH = "Đổi lịch";
    public static final String HUY_LICH = "Hủy lịch";

    private int maLH;
    private int maBN;
    private int maBS;
    private LocalDateTime ngayGio;
    private String trangThai;
    private String yeuCau;
    private LocalDateTime ngayGioMoi;
    private String ghiChu;
    private Integer maTK;
    private String tenBenhNhan;
    private String maBenhNhanHienThi;
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
    public Integer getMaTK() { return maTK; }
    public void setMaTK(Integer maTK) { this.maTK = maTK; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
    public String getMaBenhNhanHienThi() { return maBenhNhanHienThi; }
    public void setMaBenhNhanHienThi(String maBenhNhanHienThi) { this.maBenhNhanHienThi = maBenhNhanHienThi; }
    public String getTenBacSi() { return tenBacSi; }
    public void setTenBacSi(String tenBacSi) { this.tenBacSi = tenBacSi; }

    public boolean coYeuCau() { return yeuCau != null && !yeuCau.isBlank(); }
    public boolean daKetThuc() { return HOAN_THANH.equals(trangThai) || DA_HUY.equals(trangThai); }
    public boolean chuaKetThuc() { return !daKetThuc(); }

    @Override
    public String toString() {
        return String.format("LH%03d", maLH);
    }
}
