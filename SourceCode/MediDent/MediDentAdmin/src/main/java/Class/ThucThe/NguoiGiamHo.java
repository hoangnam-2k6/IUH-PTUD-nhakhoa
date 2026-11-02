package Class.ThucThe;


/**
 * Thực thể đọc dữ liệu từ dbo.NguoiGiamHo.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maGH.
 */
public final class NguoiGiamHo {
    private Integer maGH; // Cột dbo.NguoiGiamHo.maGH
    private Integer maBN; // Cột dbo.NguoiGiamHo.maBN
    private String hoTen; // Cột dbo.NguoiGiamHo.hoTen
    private String sdt; // Cột dbo.NguoiGiamHo.sdt
    private String quanHe; // Cột dbo.NguoiGiamHo.quanHe

    /** Đọc thuộc tính maGH của thực thể. */
    public Integer layMaGH() { return maGH; }
    /** Cập nhật thuộc tính maGH trong bộ nhớ, chưa lưu SQL. */
    public void ganMaGH(Integer giaTri) { this.maGH = giaTri; }

    /** Đọc thuộc tính maBN của thực thể. */
    public Integer layMaBN() { return maBN; }
    /** Cập nhật thuộc tính maBN trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBN(Integer giaTri) { this.maBN = giaTri; }

    /** Đọc thuộc tính hoTen của thực thể. */
    public String layHoTen() { return hoTen; }
    /** Cập nhật thuộc tính hoTen trong bộ nhớ, chưa lưu SQL. */
    public void ganHoTen(String giaTri) { this.hoTen = giaTri; }

    /** Đọc thuộc tính sdt của thực thể. */
    public String laySdt() { return sdt; }
    /** Cập nhật thuộc tính sdt trong bộ nhớ, chưa lưu SQL. */
    public void ganSdt(String giaTri) { this.sdt = giaTri; }

    /** Đọc thuộc tính quanHe của thực thể. */
    public String layQuanHe() { return quanHe; }
    /** Cập nhật thuộc tính quanHe trong bộ nhớ, chưa lưu SQL. */
    public void ganQuanHe(String giaTri) { this.quanHe = giaTri; }

}
