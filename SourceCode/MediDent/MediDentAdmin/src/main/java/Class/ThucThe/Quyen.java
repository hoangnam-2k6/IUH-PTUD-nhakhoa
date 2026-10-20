package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.Quyen.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maQuyen.
 */
public final class Quyen {
    private String maQuyen; // Cột dbo.Quyen.maQuyen
    private String tenQuyen; // Cột dbo.Quyen.tenQuyen

    /** Đọc thuộc tính maQuyen của thực thể. */
    public String layMaQuyen() { return maQuyen; }
    /** Cập nhật thuộc tính maQuyen trong bộ nhớ, chưa lưu SQL. */
    public void ganMaQuyen(String giaTri) { this.maQuyen = giaTri; }

    /** Đọc thuộc tính tenQuyen của thực thể. */
    public String layTenQuyen() { return tenQuyen; }
    /** Cập nhật thuộc tính tenQuyen trong bộ nhớ, chưa lưu SQL. */
    public void ganTenQuyen(String giaTri) { this.tenQuyen = giaTri; }

}
