package Entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private int maCT;
    private int maHD;
    private Integer maDV;
    private String tenDichVu;
    private int soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String tenDichVu, int soLuong, BigDecimal donGia) {
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public ChiTietHoaDon(Integer maDV, String tenDichVu, int soLuong, BigDecimal donGia) {
        this.maDV = maDV;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public int layMaCT() { return maCT; }
    public void datMaCT(int maCT) { this.maCT = maCT; }
    public int layMaHD() { return maHD; }
    public void datMaHD(int maHD) { this.maHD = maHD; }
    public Integer layMaDV() { return maDV; }
    public void datMaDV(Integer maDV) { this.maDV = maDV; }
    public String layTenDichVu() { return tenDichVu; }
    public void datTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public int laySoLuong() { return soLuong; }
    public void datSoLuong(int soLuong) { this.soLuong = soLuong; }
    public BigDecimal layDonGia() { return donGia; }
    public void datDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal layThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    @Override
    public String toString() {
        return tenDichVu;
    }
}
