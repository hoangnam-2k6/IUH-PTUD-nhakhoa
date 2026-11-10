package Entity;

import java.math.BigDecimal;

/** Một dịch vụ trong bảng giá của phòng khám. */
public class DichVu {
    private int maDV;
    private String tenDichVu;
    private BigDecimal donGia;
    private String trangThai;

    public DichVu() {}

    public DichVu(int maDV, String tenDichVu, BigDecimal donGia) {
        this.maDV = maDV;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
    }

    public int getMaDV() { return maDV; }
    public void setMaDV(int maDV) { this.maDV = maDV; }
    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return "Hoạt động".equals(trangThai); }

    @Override
    public String toString() {
        return tenDichVu;
    }
}
