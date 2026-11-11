package Entity;

import java.math.BigDecimal;

/** Một dịch vụ trong bảng giá của phòng khám. */
public class DichVu {
    private int maDV;
    private String tenDichVu;
    private BigDecimal donGia;
    private String trangThai;
    /** Số lượng đã chỉ định trên một phiếu khám (chỉ có nghĩa khi đọc từ PhieuKhamDichVu). */
    private int soLuong = 1;

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

    /** Từ khóa (không dấu, chữ thường) của các dịch vụ tính theo răng/vị trí nên được chọn số lượng. */
    private static final String[] DICH_VU_THEO_DON_VI = {"boc rang su", "tram rang", "nho rang", "dieu tri tuy", "implant"};

    /**
     * true nếu dịch vụ tính theo đơn vị (mỗi răng / vị trí) nên cho chọn số lượng khi chỉ định.
     * Xác định theo tên dịch vụ, không cần thêm cột trong CSDL; dịch vụ làm một lần (khám, X-quang,
     * cạo vôi, tẩy trắng, tư vấn...) luôn có số lượng 1.
     */
    public boolean isChoChonSoLuong() {
        if (tenDichVu == null) return false;
        String ten = java.text.Normalizer.normalize(tenDichVu, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
        for (String tuKhoa : DICH_VU_THEO_DON_VI) if (ten.contains(tuKhoa)) return true;
        return false;
    }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = Math.max(1, soLuong); }

    public boolean dangHoatDong() { return "Hoạt động".equals(trangThai); }

    @Override
    public String toString() {
        return tenDichVu;
    }
}
