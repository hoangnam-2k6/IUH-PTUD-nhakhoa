package Class.ThucThe;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.HoaDon.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maHD.
 */
public final class HoaDon {
    private Integer maHD; // Cột dbo.HoaDon.maHD
    private Integer maTT; // Cột dbo.HoaDon.maTT
    private Integer maPK; // Cột dbo.HoaDon.maPK
    private LocalDateTime ngayLap; // Cột dbo.HoaDon.ngayLap
    private BigDecimal tongTien; // Cột dbo.HoaDon.tongTien
    private String trangThai; // Cột dbo.HoaDon.trangThai

    /** Đọc thuộc tính maHD của thực thể. */
    public Integer layMaHD() { return maHD; }
    /** Cập nhật thuộc tính maHD trong bộ nhớ, chưa lưu SQL. */
    public void ganMaHD(Integer giaTri) { this.maHD = giaTri; }

    /** Đọc thuộc tính maTT của thực thể. */
    public Integer layMaTT() { return maTT; }
    /** Cập nhật thuộc tính maTT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTT(Integer giaTri) { this.maTT = giaTri; }

    /** Đọc thuộc tính maPK của thực thể. */
    public Integer layMaPK() { return maPK; }
    /** Cập nhật thuộc tính maPK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPK(Integer giaTri) { this.maPK = giaTri; }

    /** Đọc thuộc tính ngayLap của thực thể. */
    public LocalDateTime layNgayLap() { return ngayLap; }
    /** Cập nhật thuộc tính ngayLap trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayLap(LocalDateTime giaTri) { this.ngayLap = giaTri; }

    /** Đọc thuộc tính tongTien của thực thể. */
    public BigDecimal layTongTien() { return tongTien; }
    /** Cập nhật thuộc tính tongTien trong bộ nhớ, chưa lưu SQL. */
    public void ganTongTien(BigDecimal giaTri) { this.tongTien = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
