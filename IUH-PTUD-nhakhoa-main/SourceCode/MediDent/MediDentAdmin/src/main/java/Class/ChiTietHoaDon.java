package Class;

import java.math.BigDecimal;

/** Một dòng dịch vụ trên hóa đơn. */
public final class ChiTietHoaDon {
    public final String tenDichVu;
    public final int soLuong;
    public final BigDecimal donGia;

    public ChiTietHoaDon(String tenDichVu, int soLuong, BigDecimal donGia) {
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGia = donGia == null ? BigDecimal.ZERO : donGia;
    }

    public BigDecimal thanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}
