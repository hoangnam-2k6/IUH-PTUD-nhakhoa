package Class.ThucThe;

import java.time.LocalDate;

/**
 * Thực thể đọc dữ liệu từ dbo.BenhNhan.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maBN.
 */
public final class BenhNhan {
    private Integer maBN; // Cột dbo.BenhNhan.maBN
    private String hoTen; // Cột dbo.BenhNhan.hoTen
    private LocalDate ngaySinh; // Cột dbo.BenhNhan.ngaySinh
    private String gioiTinh; // Cột dbo.BenhNhan.gioiTinh
    private String sdt; // Cột dbo.BenhNhan.sdt
    private String email; // Cột dbo.BenhNhan.email
    private String diaChi; // Cột dbo.BenhNhan.diaChi
    private String tienSuBenh; // Cột dbo.BenhNhan.tienSuBenh
    private String trangThai; // Cột dbo.BenhNhan.trangThai

    /** Đọc thuộc tính maBN của thực thể. */
    public Integer layMaBN() { return maBN; }
    /** Cập nhật thuộc tính maBN trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBN(Integer giaTri) { this.maBN = giaTri; }

    /** Đọc thuộc tính hoTen của thực thể. */
    public String layHoTen() { return hoTen; }
    /** Cập nhật thuộc tính hoTen trong bộ nhớ, chưa lưu SQL. */
    public void ganHoTen(String giaTri) { this.hoTen = giaTri; }

    /** Đọc thuộc tính ngaySinh của thực thể. */
    public LocalDate layNgaySinh() { return ngaySinh; }
    /** Cập nhật thuộc tính ngaySinh trong bộ nhớ, chưa lưu SQL. */
    public void ganNgaySinh(LocalDate giaTri) { this.ngaySinh = giaTri; }

    /** Đọc thuộc tính gioiTinh của thực thể. */
    public String layGioiTinh() { return gioiTinh; }
    /** Cập nhật thuộc tính gioiTinh trong bộ nhớ, chưa lưu SQL. */
    public void ganGioiTinh(String giaTri) { this.gioiTinh = giaTri; }

    /** Đọc thuộc tính sdt của thực thể. */
    public String laySdt() { return sdt; }
    /** Cập nhật thuộc tính sdt trong bộ nhớ, chưa lưu SQL. */
    public void ganSdt(String giaTri) { this.sdt = giaTri; }

    /** Đọc thuộc tính email của thực thể. */
    public String layEmail() { return email; }
    /** Cập nhật thuộc tính email trong bộ nhớ, chưa lưu SQL. */
    public void ganEmail(String giaTri) { this.email = giaTri; }

    /** Đọc thuộc tính diaChi của thực thể. */
    public String layDiaChi() { return diaChi; }
    /** Cập nhật thuộc tính diaChi trong bộ nhớ, chưa lưu SQL. */
    public void ganDiaChi(String giaTri) { this.diaChi = giaTri; }

    /** Đọc thuộc tính tienSuBenh của thực thể. */
    public String layTienSuBenh() { return tienSuBenh; }
    /** Cập nhật thuộc tính tienSuBenh trong bộ nhớ, chưa lưu SQL. */
    public void ganTienSuBenh(String giaTri) { this.tienSuBenh = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
