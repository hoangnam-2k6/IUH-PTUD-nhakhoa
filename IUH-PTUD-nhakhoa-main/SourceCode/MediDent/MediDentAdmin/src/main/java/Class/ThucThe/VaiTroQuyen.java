package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.VaiTroQuyen.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maVaiTro, maQuyen.
 */
public final class VaiTroQuyen {
    private Integer maVaiTro; // Cột dbo.VaiTroQuyen.maVaiTro
    private String maQuyen; // Cột dbo.VaiTroQuyen.maQuyen

    /** Đọc thuộc tính maVaiTro của thực thể. */
    public Integer layMaVaiTro() { return maVaiTro; }
    /** Cập nhật thuộc tính maVaiTro trong bộ nhớ, chưa lưu SQL. */
    public void ganMaVaiTro(Integer giaTri) { this.maVaiTro = giaTri; }

    /** Đọc thuộc tính maQuyen của thực thể. */
    public String layMaQuyen() { return maQuyen; }
    /** Cập nhật thuộc tính maQuyen trong bộ nhớ, chưa lưu SQL. */
    public void ganMaQuyen(String giaTri) { this.maQuyen = giaTri; }

}
