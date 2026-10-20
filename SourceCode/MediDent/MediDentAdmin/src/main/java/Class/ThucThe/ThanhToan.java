package Class.ThucThe;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.ThanhToan.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maTT.
 */
public final class ThanhToan {
    private Integer maTT; // Cột dbo.ThanhToan.maTT
    private Integer maPK; // Cột dbo.ThanhToan.maPK
    private LocalDateTime ngayThanhToan; // Cột dbo.ThanhToan.ngayThanhToan
    private BigDecimal soTien; // Cột dbo.ThanhToan.soTien
    private String phuongThuc; // Cột dbo.ThanhToan.phuongThuc
    private String trangThai; // Cột dbo.ThanhToan.trangThai
    private String maGiaoDich; // Cột dbo.ThanhToan.maGiaoDich

    /** Đọc thuộc tính maTT của thực thể. */
    public Integer layMaTT() { return maTT; }
    /** Cập nhật thuộc tính maTT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTT(Integer giaTri) { this.maTT = giaTri; }

    /** Đọc thuộc tính maPK của thực thể. */
    public Integer layMaPK() { return maPK; }
    /** Cập nhật thuộc tính maPK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPK(Integer giaTri) { this.maPK = giaTri; }

    /** Đọc thuộc tính ngayThanhToan của thực thể. */
    public LocalDateTime layNgayThanhToan() { return ngayThanhToan; }
    /** Cập nhật thuộc tính ngayThanhToan trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayThanhToan(LocalDateTime giaTri) { this.ngayThanhToan = giaTri; }

    /** Đọc thuộc tính soTien của thực thể. */
    public BigDecimal laySoTien() { return soTien; }
    /** Cập nhật thuộc tính soTien trong bộ nhớ, chưa lưu SQL. */
    public void ganSoTien(BigDecimal giaTri) { this.soTien = giaTri; }

    /** Đọc thuộc tính phuongThuc của thực thể. */
    public String layPhuongThuc() { return phuongThuc; }
    /** Cập nhật thuộc tính phuongThuc trong bộ nhớ, chưa lưu SQL. */
    public void ganPhuongThuc(String giaTri) { this.phuongThuc = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

    /** Đọc thuộc tính maGiaoDich của thực thể. */
    public String layMaGiaoDich() { return maGiaoDich; }
    /** Cập nhật thuộc tính maGiaoDich trong bộ nhớ, chưa lưu SQL. */
    public void ganMaGiaoDich(String giaTri) { this.maGiaoDich = giaTri; }

}
