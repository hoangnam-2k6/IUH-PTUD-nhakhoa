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

    public int getMaCT() { return maCT; }
    public void setMaCT(int maCT) { this.maCT = maCT; }
    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }
    public Integer getMaDV() { return maDV; }
    public void setMaDV(Integer maDV) { this.maDV = maDV; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() {
        if (donGia == null) return BigDecimal.ZERO;
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    @Override
    public String toString() {
        return tenDichVu;
    }
}
