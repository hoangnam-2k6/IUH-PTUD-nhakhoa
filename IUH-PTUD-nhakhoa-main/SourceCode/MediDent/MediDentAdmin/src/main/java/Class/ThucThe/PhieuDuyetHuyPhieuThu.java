package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.PhieuDuyetHuyPhieuThu.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maPD.
 */
public final class PhieuDuyetHuyPhieuThu {
    private Integer maPD; // Cột dbo.PhieuDuyetHuyPhieuThu.maPD
    private Integer maYC; // Cột dbo.PhieuDuyetHuyPhieuThu.maYC
    private LocalDateTime ngayPheDuyet; // Cột dbo.PhieuDuyetHuyPhieuThu.ngayPheDuyet
    private String lyDoPheDuyet; // Cột dbo.PhieuDuyetHuyPhieuThu.lyDoPheDuyet

    /** Đọc thuộc tính maPD của thực thể. */
    public Integer layMaPD() { return maPD; }
    /** Cập nhật thuộc tính maPD trong bộ nhớ, chưa lưu SQL. */
    public void ganMaPD(Integer giaTri) { this.maPD = giaTri; }

    /** Đọc thuộc tính maYC của thực thể. */
    public Integer layMaYC() { return maYC; }
    /** Cập nhật thuộc tính maYC trong bộ nhớ, chưa lưu SQL. */
    public void ganMaYC(Integer giaTri) { this.maYC = giaTri; }

    /** Đọc thuộc tính ngayPheDuyet của thực thể. */
    public LocalDateTime layNgayPheDuyet() { return ngayPheDuyet; }
    /** Cập nhật thuộc tính ngayPheDuyet trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayPheDuyet(LocalDateTime giaTri) { this.ngayPheDuyet = giaTri; }

    /** Đọc thuộc tính lyDoPheDuyet của thực thể. */
    public String layLyDoPheDuyet() { return lyDoPheDuyet; }
    /** Cập nhật thuộc tính lyDoPheDuyet trong bộ nhớ, chưa lưu SQL. */
    public void ganLyDoPheDuyet(String giaTri) { this.lyDoPheDuyet = giaTri; }

}
