package Class.ThucThe;

import java.math.BigDecimal;

/**
 * Thực thể đọc dữ liệu từ dbo.DichVu.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maDV.
 */
public final class DichVu {
    private Integer maDV; // Cột dbo.DichVu.maDV
    private String tenDV; // Cột dbo.DichVu.tenDV
    private String moTa; // Cột dbo.DichVu.moTa
    private BigDecimal donGia; // Cột dbo.DichVu.donGia
    private String trangThai; // Cột dbo.DichVu.trangThai

    /** Đọc thuộc tính maDV của thực thể. */
    public Integer layMaDV() { return maDV; }
    /** Cập nhật thuộc tính maDV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaDV(Integer giaTri) { this.maDV = giaTri; }

    /** Đọc thuộc tính tenDV của thực thể. */
    public String layTenDV() { return tenDV; }
    /** Cập nhật thuộc tính tenDV trong bộ nhớ, chưa lưu SQL. */
    public void ganTenDV(String giaTri) { this.tenDV = giaTri; }

    /** Đọc thuộc tính moTa của thực thể. */
    public String layMoTa() { return moTa; }
    /** Cập nhật thuộc tính moTa trong bộ nhớ, chưa lưu SQL. */
    public void ganMoTa(String giaTri) { this.moTa = giaTri; }

    /** Đọc thuộc tính donGia của thực thể. */
    public BigDecimal layDonGia() { return donGia; }
    /** Cập nhật thuộc tính donGia trong bộ nhớ, chưa lưu SQL. */
    public void ganDonGia(BigDecimal giaTri) { this.donGia = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
