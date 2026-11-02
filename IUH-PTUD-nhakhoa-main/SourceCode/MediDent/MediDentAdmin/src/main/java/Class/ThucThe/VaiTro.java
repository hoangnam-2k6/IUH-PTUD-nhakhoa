package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.VaiTro.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maVaiTro.
 */
public final class VaiTro {
    private Integer maVaiTro; // Cột dbo.VaiTro.maVaiTro
    private String tenVaiTro; // Cột dbo.VaiTro.tenVaiTro
    private String moTa; // Cột dbo.VaiTro.moTa

    /** Đọc thuộc tính maVaiTro của thực thể. */
    public Integer layMaVaiTro() { return maVaiTro; }
    /** Cập nhật thuộc tính maVaiTro trong bộ nhớ, chưa lưu SQL. */
    public void ganMaVaiTro(Integer giaTri) { this.maVaiTro = giaTri; }

    /** Đọc thuộc tính tenVaiTro của thực thể. */
    public String layTenVaiTro() { return tenVaiTro; }
    /** Cập nhật thuộc tính tenVaiTro trong bộ nhớ, chưa lưu SQL. */
    public void ganTenVaiTro(String giaTri) { this.tenVaiTro = giaTri; }

    /** Đọc thuộc tính moTa của thực thể. */
    public String layMoTa() { return moTa; }
    /** Cập nhật thuộc tính moTa trong bộ nhớ, chưa lưu SQL. */
    public void ganMoTa(String giaTri) { this.moTa = giaTri; }

}
