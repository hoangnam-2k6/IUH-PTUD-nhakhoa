package Class.ThucThe;

import java.time.LocalDate;

/**
 * Thực thể đọc dữ liệu từ dbo.NhanVien.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maNV.
 */
public final class NhanVien {
    private Integer maNV; // Cột dbo.NhanVien.maNV
    private String hoTen; // Cột dbo.NhanVien.hoTen
    private LocalDate ngaySinh; // Cột dbo.NhanVien.ngaySinh
    private String gioiTinh; // Cột dbo.NhanVien.gioiTinh
    private String sdt; // Cột dbo.NhanVien.sdt
    private String email; // Cột dbo.NhanVien.email
    private String diaChi; // Cột dbo.NhanVien.diaChi
    private String chucVu; // Cột dbo.NhanVien.chucVu
    private String trangThai; // Cột dbo.NhanVien.trangThai
    private Integer maTK; // Cột dbo.NhanVien.maTK

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

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

    /** Đọc thuộc tính chucVu của thực thể. */
    public String layChucVu() { return chucVu; }
    /** Cập nhật thuộc tính chucVu trong bộ nhớ, chưa lưu SQL. */
    public void ganChucVu(String giaTri) { this.chucVu = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

    /** Đọc thuộc tính maTK của thực thể. */
    public Integer layMaTK() { return maTK; }
    /** Cập nhật thuộc tính maTK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTK(Integer giaTri) { this.maTK = giaTri; }

}
