package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.LeTan.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maNV.
 */
public final class LeTan {
    private Integer maNV; // Cột dbo.LeTan.maNV
    private String caLamViec; // Cột dbo.LeTan.caLamViec

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính caLamViec của thực thể. */
    public String layCaLamViec() { return caLamViec; }
    /** Cập nhật thuộc tính caLamViec trong bộ nhớ, chưa lưu SQL. */
    public void ganCaLamViec(String giaTri) { this.caLamViec = giaTri; }

}
