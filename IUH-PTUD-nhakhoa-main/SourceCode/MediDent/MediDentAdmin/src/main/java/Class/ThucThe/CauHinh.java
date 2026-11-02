package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.CauHinh.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: Khoa.
 */
public final class CauHinh {
    private String Khoa; // Cột dbo.CauHinh.Khoa
    private String GiaTri; // Cột dbo.CauHinh.GiaTri

    /** Đọc thuộc tính Khoa của thực thể. */
    public String layKhoa() { return Khoa; }
    /** Cập nhật thuộc tính Khoa trong bộ nhớ, chưa lưu SQL. */
    public void ganKhoa(String giaTri) { this.Khoa = giaTri; }

    /** Đọc thuộc tính GiaTri của thực thể. */
    public String layGiaTri() { return GiaTri; }
    /** Cập nhật thuộc tính GiaTri trong bộ nhớ, chưa lưu SQL. */
    public void ganGiaTri(String giaTri) { this.GiaTri = giaTri; }

}
