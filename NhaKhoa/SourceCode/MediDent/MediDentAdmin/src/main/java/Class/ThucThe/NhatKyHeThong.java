package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.NhatKyHeThong.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maNK.
 */
public final class NhatKyHeThong {
    private Long maNK; // Cột dbo.NhatKyHeThong.maNK
    private LocalDateTime thoiGian; // Cột dbo.NhatKyHeThong.thoiGian
    private String hanhDong; // Cột dbo.NhatKyHeThong.hanhDong
    private String doiTuong; // Cột dbo.NhatKyHeThong.doiTuong
    private String moTa; // Cột dbo.NhatKyHeThong.moTa

    /** Đọc thuộc tính maNK của thực thể. */
    public Long layMaNK() { return maNK; }
    /** Cập nhật thuộc tính maNK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNK(Long giaTri) { this.maNK = giaTri; }

    /** Đọc thuộc tính thoiGian của thực thể. */
    public LocalDateTime layThoiGian() { return thoiGian; }
    /** Cập nhật thuộc tính thoiGian trong bộ nhớ, chưa lưu SQL. */
    public void ganThoiGian(LocalDateTime giaTri) { this.thoiGian = giaTri; }

    /** Đọc thuộc tính hanhDong của thực thể. */
    public String layHanhDong() { return hanhDong; }
    /** Cập nhật thuộc tính hanhDong trong bộ nhớ, chưa lưu SQL. */
    public void ganHanhDong(String giaTri) { this.hanhDong = giaTri; }

    /** Đọc thuộc tính doiTuong của thực thể. */
    public String layDoiTuong() { return doiTuong; }
    /** Cập nhật thuộc tính doiTuong trong bộ nhớ, chưa lưu SQL. */
    public void ganDoiTuong(String giaTri) { this.doiTuong = giaTri; }

    /** Đọc thuộc tính moTa của thực thể. */
    public String layMoTa() { return moTa; }
    /** Cập nhật thuộc tính moTa trong bộ nhớ, chưa lưu SQL. */
    public void ganMoTa(String giaTri) { this.moTa = giaTri; }

}
