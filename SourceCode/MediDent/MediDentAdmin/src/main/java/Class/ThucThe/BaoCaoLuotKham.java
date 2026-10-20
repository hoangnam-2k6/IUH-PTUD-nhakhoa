package Class.ThucThe;

import java.time.LocalDate;

/**
 * Thực thể đọc dữ liệu từ dbo.BaoCaoLuotKham.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maBC.
 */
public final class BaoCaoLuotKham {
    private Integer maBC; // Cột dbo.BaoCaoLuotKham.maBC
    private Integer maPK; // Cột dbo.BaoCaoLuotKham.maPK
    private LocalDate tuNgay; // Cột dbo.BaoCaoLuotKham.tuNgay
    private LocalDate denNgay; // Cột dbo.BaoCaoLuotKham.denNgay
    private Integer soLuotKham; // Cột dbo.BaoCaoLuotKham.soLuotKham

    /** Đọc thuộc tính maBC của thực thể. */
    public Integer layMaBC() { return maBC; }
    /** Cập nhật thuộc tính maBC trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBC(Integer giaTri) { this.maBC = giaTri; }

    /** Đọc thuộc tính maPK của thực thể. */
    public Integer layMaPK() { return maPK; }
    /** Cập nhật thuộc tính maPK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPK(Integer giaTri) { this.maPK = giaTri; }

    /** Đọc thuộc tính tuNgay của thực thể. */
    public LocalDate layTuNgay() { return tuNgay; }
    /** Cập nhật thuộc tính tuNgay trong bộ nhớ, chưa lưu SQL. */
    public void ganTuNgay(LocalDate giaTri) { this.tuNgay = giaTri; }

    /** Đọc thuộc tính denNgay của thực thể. */
    public LocalDate layDenNgay() { return denNgay; }
    /** Cập nhật thuộc tính denNgay trong bộ nhớ, chưa lưu SQL. */
    public void ganDenNgay(LocalDate giaTri) { this.denNgay = giaTri; }

    /** Đọc thuộc tính soLuotKham của thực thể. */
    public Integer laySoLuotKham() { return soLuotKham; }
    /** Cập nhật thuộc tính soLuotKham trong bộ nhớ, chưa lưu SQL. */
    public void ganSoLuotKham(Integer giaTri) { this.soLuotKham = giaTri; }

}
