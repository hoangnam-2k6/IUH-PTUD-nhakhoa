package Class.ThucThe;

import java.math.BigDecimal;

/**
 * Thực thể đọc dữ liệu từ dbo.ChiTietDieuTri.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maCT.
 */
public final class ChiTietDieuTri {
    private Integer maCT; // Cột dbo.ChiTietDieuTri.maCT
    private Integer maPK; // Cột dbo.ChiTietDieuTri.maPK
    private Integer maDDT; // Cột dbo.ChiTietDieuTri.maDDT
    private String noiDung; // Cột dbo.ChiTietDieuTri.noiDung
    private Integer soLuong; // Cột dbo.ChiTietDieuTri.soLuong
    private BigDecimal donGia; // Cột dbo.ChiTietDieuTri.donGia
    private BigDecimal thanhTien; // Cột dbo.ChiTietDieuTri.thanhTien

    /** Đọc thuộc tính maCT của thực thể. */
    public Integer layMaCT() { return maCT; }
    /** Cập nhật thuộc tính maCT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaCT(Integer giaTri) { this.maCT = giaTri; }

    /** Đọc thuộc tính maPK của thực thể. */
    public Integer layMaPK() { return maPK; }
    /** Cập nhật thuộc tính maPK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPK(Integer giaTri) { this.maPK = giaTri; }

    /** Đọc thuộc tính maDDT của thực thể. */
    public Integer layMaDDT() { return maDDT; }
    /** Cập nhật thuộc tính maDDT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaDDT(Integer giaTri) { this.maDDT = giaTri; }

    /** Đọc thuộc tính noiDung của thực thể. */
    public String layNoiDung() { return noiDung; }
    /** Cập nhật thuộc tính noiDung trong bộ nhớ, chưa lưu SQL. */
    public void ganNoiDung(String giaTri) { this.noiDung = giaTri; }

    /** Đọc thuộc tính soLuong của thực thể. */
    public Integer laySoLuong() { return soLuong; }
    /** Cập nhật thuộc tính soLuong trong bộ nhớ, chưa lưu SQL. */
    public void ganSoLuong(Integer giaTri) { this.soLuong = giaTri; }

    /** Đọc thuộc tính donGia của thực thể. */
    public BigDecimal layDonGia() { return donGia; }
    /** Cập nhật thuộc tính donGia trong bộ nhớ, chưa lưu SQL. */
    public void ganDonGia(BigDecimal giaTri) { this.donGia = giaTri; }

    /** Đọc thuộc tính thanhTien của thực thể. */
    public BigDecimal layThanhTien() { return thanhTien; }
    /** Cập nhật thuộc tính thanhTien trong bộ nhớ, chưa lưu SQL. */
    public void ganThanhTien(BigDecimal giaTri) { this.thanhTien = giaTri; }

}
