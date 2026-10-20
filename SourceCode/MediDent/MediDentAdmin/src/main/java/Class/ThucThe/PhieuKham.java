package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.PhieuKham.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maPK.
 */
public final class PhieuKham {
    private Integer maPK; // Cột dbo.PhieuKham.maPK
    private Integer maBN; // Cột dbo.PhieuKham.maBN
    private Integer maNV; // Cột dbo.PhieuKham.maNV
    private LocalDateTime ngayKham; // Cột dbo.PhieuKham.ngayKham
    private String trieuChung; // Cột dbo.PhieuKham.trieuChung
    private String chanDoan; // Cột dbo.PhieuKham.chanDoan
    private String phuongPhapDieuTri; // Cột dbo.PhieuKham.phuongPhapDieuTri
    private String ketQua; // Cột dbo.PhieuKham.ketQua
    private String ghiChu; // Cột dbo.PhieuKham.ghiChu
    private String trangThai; // Cột dbo.PhieuKham.trangThai

    /** Đọc thuộc tính maPK của thực thể. */
    public Integer layMaPK() { return maPK; }
    /** Cập nhật thuộc tính maPK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPK(Integer giaTri) { this.maPK = giaTri; }

    /** Đọc thuộc tính maBN của thực thể. */
    public Integer layMaBN() { return maBN; }
    /** Cập nhật thuộc tính maBN trong bộ nhớ, chưa lưu SQL. */
    public void ganMaBN(Integer giaTri) { this.maBN = giaTri; }

    /** Đọc thuộc tính maNV của thực thể. */
    public Integer layMaNV() { return maNV; }
    /** Cập nhật thuộc tính maNV trong bộ nhớ, chưa lưu SQL. */
    public void ganMaNV(Integer giaTri) { this.maNV = giaTri; }

    /** Đọc thuộc tính ngayKham của thực thể. */
    public LocalDateTime layNgayKham() { return ngayKham; }
    /** Cập nhật thuộc tính ngayKham trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayKham(LocalDateTime giaTri) { this.ngayKham = giaTri; }

    /** Đọc thuộc tính trieuChung của thực thể. */
    public String layTrieuChung() { return trieuChung; }
    /** Cập nhật thuộc tính trieuChung trong bộ nhớ, chưa lưu SQL. */
    public void ganTrieuChung(String giaTri) { this.trieuChung = giaTri; }

    /** Đọc thuộc tính chanDoan của thực thể. */
    public String layChanDoan() { return chanDoan; }
    /** Cập nhật thuộc tính chanDoan trong bộ nhớ, chưa lưu SQL. */
    public void ganChanDoan(String giaTri) { this.chanDoan = giaTri; }

    /** Đọc thuộc tính phuongPhapDieuTri của thực thể. */
    public String layPhuongPhapDieuTri() { return phuongPhapDieuTri; }
    /** Cập nhật thuộc tính phuongPhapDieuTri trong bộ nhớ, chưa lưu SQL. */
    public void ganPhuongPhapDieuTri(String giaTri) { this.phuongPhapDieuTri = giaTri; }

    /** Đọc thuộc tính ketQua của thực thể. */
    public String layKetQua() { return ketQua; }
    /** Cập nhật thuộc tính ketQua trong bộ nhớ, chưa lưu SQL. */
    public void ganKetQua(String giaTri) { this.ketQua = giaTri; }

    /** Đọc thuộc tính ghiChu của thực thể. */
    public String layGhiChu() { return ghiChu; }
    /** Cập nhật thuộc tính ghiChu trong bộ nhớ, chưa lưu SQL. */
    public void ganGhiChu(String giaTri) { this.ghiChu = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

}
