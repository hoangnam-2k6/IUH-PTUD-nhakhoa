package Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mã giảm giá do bộ phận quản lý tạo sẵn trong bảng MaGiamGia.
 * Hóa đơn chỉ được chọn mã từ danh sách này, không tự nhập số tiền giảm.
 */
public class MaGiamGia {
    public static final String PHAN_TRAM = "Phần trăm";
    public static final String SO_TIEN = "Số tiền";
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private int maGiam;
    private String ma;
    private String ten;
    private String loaiGiam;
    private BigDecimal giaTri = BigDecimal.ZERO;
    private String trangThai;

    public MaGiamGia() {}

    public MaGiamGia(int maGiam, String ma, String ten, String loaiGiam,
                     BigDecimal giaTri, String trangThai) {
        this.maGiam = maGiam;
        this.ma = ma;
        this.ten = ten;
        this.loaiGiam = loaiGiam;
        this.giaTri = giaTri == null ? BigDecimal.ZERO : giaTri;
        this.trangThai = trangThai;
    }

    /** Mã rỗng dùng cho lựa chọn "Không giảm giá" trong combobox. */
    public static MaGiamGia khongGiamGia() {
        return new MaGiamGia(0, "", "Không giảm giá", SO_TIEN, BigDecimal.ZERO, HOAT_DONG);
    }

    public int getMaGiam() { return maGiam; }
    public void setMaGiam(int maGiam) { this.maGiam = maGiam; }
    public String getMa() { return ma; }
    public void setMa(String ma) { this.ma = ma; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getLoaiGiam() { return loaiGiam; }
    public void setLoaiGiam(String loaiGiam) { this.loaiGiam = loaiGiam; }
    public BigDecimal getGiaTri() { return giaTri == null ? BigDecimal.ZERO : giaTri; }
    public void setGiaTri(BigDecimal giaTri) { this.giaTri = giaTri; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return HOAT_DONG.equals(trangThai); }

    /** Mã rỗng (không giảm) hoặc bản ghi cũ không còn giá trị giảm. */
    public boolean laKhongGiam() { return maGiam <= 0 && getGiaTri().signum() <= 0; }

    /** Số tiền giảm thực tế trên một tạm tính; không vượt quá tạm tính. */
    public BigDecimal tinhGiam(BigDecimal tamTinh) {
        if (tamTinh == null || tamTinh.signum() <= 0 || laKhongGiam()) return BigDecimal.ZERO;
        BigDecimal giam;
        if (PHAN_TRAM.equals(loaiGiam)) {
            giam = tamTinh.multiply(getGiaTri()).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        } else {
            giam = getGiaTri();
        }
        if (giam.signum() < 0) return BigDecimal.ZERO;
        return giam.min(tamTinh);
    }

    /** Chuỗi hiển thị trong combobox: mã - tên (giá trị giảm). */
    public String moTa() {
        if (laKhongGiam()) return ten;
        String tri = PHAN_TRAM.equals(loaiGiam)
                ? getGiaTri().stripTrailingZeros().toPlainString() + "%"
                : String.format("%,.0f đ", getGiaTri());
        return ma + " - " + ten + " (" + tri + ")";
    }

    @Override
    public String toString() {
        return moTa();
    }
}
