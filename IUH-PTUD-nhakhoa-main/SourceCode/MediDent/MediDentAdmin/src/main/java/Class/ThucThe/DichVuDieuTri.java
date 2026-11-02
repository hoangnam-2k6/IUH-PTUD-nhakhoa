package Class.ThucThe;

import java.math.BigDecimal;

/**
 * Thực thể đọc dữ liệu từ dbo.DichVuDieuTri.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maDDT.
 */
public final class DichVuDieuTri {
    private Integer maDDT; // Cột dbo.DichVuDieuTri.maDDT
    private String tenDDT; // Cột dbo.DichVuDieuTri.tenDDT
    private BigDecimal donGia; // Cột dbo.DichVuDieuTri.donGia
    private String trangThai; // Cột dbo.DichVuDieuTri.trangThai

    /** Đọc thuộc tính maDDT của thực thể. */
    public Integer layMaDDT() { return maDDT; }
    /** Cập nhật thuộc tính maDDT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaDDT(Integer giaTri) { this.maDDT = giaTri; }

    /** Đọc thuộc tính tenDDT của thực thể. */
    public String layTenDDT() { return tenDDT; }
    /** Cập nhật thuộc tính tenDDT trong bộ nhớ, chưa lưu SQL. */
    public void ganTenDDT(String giaTri) { this.tenDDT = giaTri; }

    /** Đọc thuộc tính donGia của thực thể. */
    public BigDecimal layDonGia() { return donGia; }
    /** Cập nhật thuộc tính donGia trong bộ nhớ, chưa lưu SQL. */
    public void ganDonGia(BigDecimal giaTri) { this.donGia = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
