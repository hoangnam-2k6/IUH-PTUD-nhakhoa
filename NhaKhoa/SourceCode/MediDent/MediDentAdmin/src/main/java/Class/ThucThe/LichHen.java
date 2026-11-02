package Class.ThucThe;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Thực thể đọc dữ liệu từ dbo.LichHen.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maLichHen.
 */
public final class LichHen {
    private Integer maLichHen; // Cột dbo.LichHen.maLichHen
    private Integer maBN; // Cột dbo.LichHen.maBN
    private Integer maNV; // Cột dbo.LichHen.maNV
    private String phongKham; // Cột dbo.LichHen.phongKham
    private LocalDate ngayHen; // Cột dbo.LichHen.ngayHen
    private LocalTime gioHen; // Cột dbo.LichHen.gioHen
    private String lyDo; // Cột dbo.LichHen.lyDo
    private String trangThai; // Cột dbo.LichHen.trangThai
    private String ghiChu; // Cột dbo.LichHen.ghiChu
    private Integer soThuTuKham; // Cột dbo.LichHen.soThuTuKham

    /** Đọc thuộc tính maLichHen của thực thể. */
    public Integer layMaLichHen() { return maLichHen; }
    /** Cập nhật thuộc tính maLichHen trong bộ nhớ, chưa lưu SQL. */
    public void ganMaLichHen(Integer giaTri) { this.maLichHen = giaTri; }

    /** Đọc thuộc tính maBN của thực thể. */
    public Integer layMaBN() { return maBN; }
    /** Cập nhật thuộc tính maBN trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBN(Integer giaTri) { this.maBN = giaTri; }

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính phongKham của thực thể. */
    public String layPhongKham() { return phongKham; }
    /** Cập nhật thuộc tính phongKham trong bộ nhớ, chưa lưu SQL. */
    public void ganPhongKham(String giaTri) { this.phongKham = giaTri; }

    /** Đọc thuộc tính ngayHen của thực thể. */
    public LocalDate layNgayHen() { return ngayHen; }
    /** Cập nhật thuộc tính ngayHen trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayHen(LocalDate giaTri) { this.ngayHen = giaTri; }

    /** Đọc thuộc tính gioHen của thực thể. */
    public LocalTime layGioHen() { return gioHen; }
    /** Cập nhật thuộc tính gioHen trong bộ nhớ, chưa lưu SQL. */
    public void ganGioHen(LocalTime giaTri) { this.gioHen = giaTri; }

    /** Đọc thuộc tính lyDo của thực thể. */
    public String layLyDo() { return lyDo; }
    /** Cập nhật thuộc tính lyDo trong bộ nhớ, chưa lưu SQL. */
    public void ganLyDo(String giaTri) { this.lyDo = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

    /** Đọc thuộc tính ghiChu của thực thể. */
    public String layGhiChu() { return ghiChu; }
    /** Cập nhật thuộc tính ghiChu trong bộ nhớ, chưa lưu SQL. */
    public void ganGhiChu(String giaTri) { this.ghiChu = giaTri; }

    /** Đọc thuộc tính soThuTuKham của thực thể. */
    public Integer laySoThuTuKham() { return soThuTuKham; }
    /** Cập nhật thuộc tính soThuTuKham trong bộ nhớ, chưa lưu SQL. */
    public void ganSoThuTuKham(Integer giaTri) { this.soThuTuKham = giaTri; }

}
