package Class.ThucThe;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể đọc dữ liệu từ dbo.BaoCaoDoanhThu.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maBC.
 */
public final class BaoCaoDoanhThu {
    private Integer maBC; // Cột dbo.BaoCaoDoanhThu.maBC
    private Integer maHD; // Cột dbo.BaoCaoDoanhThu.maHD
    private LocalDate tuNgay; // Cột dbo.BaoCaoDoanhThu.tuNgay
    private LocalDate denNgay; // Cột dbo.BaoCaoDoanhThu.denNgay
    private BigDecimal tongDoanhThu; // Cột dbo.BaoCaoDoanhThu.tongDoanhThu

    /** Đọc thuộc tính maBC của thực thể. */
    public Integer layMaBC() { return maBC; }
    /** Cập nhật thuộc tính maBC trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBC(Integer giaTri) { this.maBC = giaTri; }

    /** Đọc thuộc tính maHD của thực thể. */
    public Integer layMaHD() { return maHD; }
    /** Cập nhật thuộc tính maHD trong bộ nhớ, chưa lưu SQL. */
    public void ganMaHD(Integer giaTri) { this.maHD = giaTri; }

    /** Đọc thuộc tính tuNgay của thực thể. */
    public LocalDate layTuNgay() { return tuNgay; }
    /** Cập nhật thuộc tính tuNgay trong bộ nhớ, chưa lưu SQL. */
    public void ganTuNgay(LocalDate giaTri) { this.tuNgay = giaTri; }

    /** Đọc thuộc tính denNgay của thực thể. */
    public LocalDate layDenNgay() { return denNgay; }
    /** Cập nhật thuộc tính denNgay trong bộ nhớ, chưa lưu SQL. */
    public void ganDenNgay(LocalDate giaTri) { this.denNgay = giaTri; }

    /** Đọc thuộc tính tongDoanhThu của thực thể. */
    public BigDecimal layTongDoanhThu() { return tongDoanhThu; }
    /** Cập nhật thuộc tính tongDoanhThu trong bộ nhớ, chưa lưu SQL. */
    public void ganTongDoanhThu(BigDecimal giaTri) { this.tongDoanhThu = giaTri; }

}
