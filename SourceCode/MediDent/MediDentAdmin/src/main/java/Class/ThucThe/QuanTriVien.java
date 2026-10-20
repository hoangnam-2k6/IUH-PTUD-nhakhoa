package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.QuanTriVien.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maNV.
 */
public final class QuanTriVien {
    private Integer maNV; // Cột dbo.QuanTriVien.maNV
    private String capDoQuyen; // Cột dbo.QuanTriVien.capDoQuyen

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính capDoQuyen của thực thể. */
    public String layCapDoQuyen() { return capDoQuyen; }
    /** Cập nhật thuộc tính capDoQuyen trong bộ nhớ, chưa lưu SQL. */
    public void ganCapDoQuyen(String giaTri) { this.capDoQuyen = giaTri; }

}
