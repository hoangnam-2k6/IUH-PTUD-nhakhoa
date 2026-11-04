package Class.ThucThe;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể đọc dữ liệu từ dbo.BangGiaDichVu.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maBG.
 */
public final class BangGiaDichVu {
    private Integer maBG; // Cột dbo.BangGiaDichVu.maBG
    private Integer maDV; // Cột dbo.BangGiaDichVu.maDV
    private LocalDate ngayApDung; // Cột dbo.BangGiaDichVu.ngayApDung
    private BigDecimal donGia; // Cột dbo.BangGiaDichVu.donGia
    private String trangThai; // Cột dbo.BangGiaDichVu.trangThai

    /** Đọc thuộc tính maBG của thực thể. */
    public Integer layMaBG() { return maBG; }
    /** Cập nhật thuộc tính maBG trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBG(Integer giaTri) { this.maBG = giaTri; }

    /** Đọc thuộc tính maDV của thực thể. */
    public Integer layMaDV() { return maDV; }
    /** Cập nhật thuộc tính maDV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaDV(Integer giaTri) { this.maDV = giaTri; }

    /** Đọc thuộc tính ngayApDung của thực thể. */
    public LocalDate layNgayApDung() { return ngayApDung; }
    /** Cập nhật thuộc tính ngayApDung trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayApDung(LocalDate giaTri) { this.ngayApDung = giaTri; }

    /** Đọc thuộc tính donGia của thực thể. */
    public BigDecimal layDonGia() { return donGia; }
    /** Cập nhật thuộc tính donGia trong bộ nhớ, chưa lưu SQL. */
    public void ganDonGia(BigDecimal giaTri) { this.donGia = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
