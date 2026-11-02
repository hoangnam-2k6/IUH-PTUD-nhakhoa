package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.QuanLy.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maNV.
 */
public final class QuanLy {
    private Integer maNV; // Cột dbo.QuanLy.maNV
    private String phongBan; // Cột dbo.QuanLy.phongBan

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính phongBan của thực thể. */
    public String layPhongBan() { return phongBan; }
    /** Cập nhật thuộc tính phongBan trong bộ nhớ, chưa lưu SQL. */
    public void ganPhongBan(String giaTri) { this.phongBan = giaTri; }

}
