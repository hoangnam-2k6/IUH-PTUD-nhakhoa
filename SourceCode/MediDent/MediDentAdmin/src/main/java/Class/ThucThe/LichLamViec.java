package Class.ThucThe;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thực thể đọc dữ liệu từ dbo.LichLamViec.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maLichLV.
 */
public final class LichLamViec {
    private Integer maLichLV; // Cột dbo.LichLamViec.maLichLV
    private Integer maNV; // Cột dbo.LichLamViec.maNV
    private String thu; // Cột dbo.LichLamViec.thu
    private LocalDate ngayLamViec; // Cột dbo.LichLamViec.ngayLamViec
    private String caLamViec; // Cột dbo.LichLamViec.caLamViec
    private LocalTime gioBatDau; // Cột dbo.LichLamViec.gioBatDau
    private LocalTime gioKetThuc; // Cột dbo.LichLamViec.gioKetThuc
    private String trangThai; // Cột dbo.LichLamViec.trangThai

    /** Đọc thuộc tính maLichLV của thực thể. */
    public Integer layMaLichLV() { return maLichLV; }
    /** Cập nhật thuộc tính maLichLV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaLichLV(Integer giaTri) { this.maLichLV = giaTri; }

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính thu của thực thể. */
    public String layThu() { return thu; }
    /** Cập nhật thuộc tính thu trong bộ nhớ, chưa lưu SQL. */
    public void ganThu(String giaTri) { this.thu = giaTri; }

    /** Đọc thuộc tính ngayLamViec của thực thể. */
    public LocalDate layNgayLamViec() { return ngayLamViec; }
    /** Cập nhật thuộc tính ngayLamViec trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayLamViec(LocalDate giaTri) { this.ngayLamViec = giaTri; }

    /** Đọc thuộc tính caLamViec của thực thể. */
    public String layCaLamViec() { return caLamViec; }
    /** Cập nhật thuộc tính caLamViec trong bộ nhớ, chưa lưu SQL. */
    public void ganCaLamViec(String giaTri) { this.caLamViec = giaTri; }

    /** Đọc thuộc tính gioBatDau của thực thể. */
    public LocalTime layGioBatDau() { return gioBatDau; }
    /** Cập nhật thuộc tính gioBatDau trong bộ nhớ, chưa lưu SQL. */
    public void ganGioBatDau(LocalTime giaTri) { this.gioBatDau = giaTri; }

    /** Đọc thuộc tính gioKetThuc của thực thể. */
    public LocalTime layGioKetThuc() { return gioKetThuc; }
    /** Cập nhật thuộc tính gioKetThuc trong bộ nhớ, chưa lưu SQL. */
    public void ganGioKetThuc(LocalTime giaTri) { this.gioKetThuc = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
