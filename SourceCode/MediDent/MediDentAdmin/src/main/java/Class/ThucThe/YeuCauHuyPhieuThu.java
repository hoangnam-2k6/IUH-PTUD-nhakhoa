package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.YeuCauHuyPhieuThu.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maYC.
 */
public final class YeuCauHuyPhieuThu {
    private Integer maYC; // Cột dbo.YeuCauHuyPhieuThu.maYC
    private Integer maTT; // Cột dbo.YeuCauHuyPhieuThu.maTT
    private LocalDateTime ngayYeuCau; // Cột dbo.YeuCauHuyPhieuThu.ngayYeuCau
    private String lyDo; // Cột dbo.YeuCauHuyPhieuThu.lyDo
    private String trangThai; // Cột dbo.YeuCauHuyPhieuThu.trangThai
    private Integer maTKNguoiGui; // Cột dbo.YeuCauHuyPhieuThu.maTKNguoiGui

    /** Đọc thuộc tính maYC của thực thể. */
    public Integer layMaYC() { return maYC; }
    /** Cập nhật thuộc tính maYC trong bộ nhớ, chưa lưu SQL. */
    public void ganMaYC(Integer giaTri) { this.maYC = giaTri; }

    /** Đọc thuộc tính maTT của thực thể. */
    public Integer layMaTT() { return maTT; }
    /** Cập nhật thuộc tính maTT trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTT(Integer giaTri) { this.maTT = giaTri; }

    /** Đọc thuộc tính ngayYeuCau của thực thể. */
    public LocalDateTime layNgayYeuCau() { return ngayYeuCau; }
    /** Cập nhật thuộc tính ngayYeuCau trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayYeuCau(LocalDateTime giaTri) { this.ngayYeuCau = giaTri; }

    /** Đọc thuộc tính lyDo của thực thể. */
    public String layLyDo() { return lyDo; }
    /** Cập nhật thuộc tính lyDo trong bộ nhớ, chưa lưu SQL. */
    public void ganLyDo(String giaTri) { this.lyDo = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

    /** Đọc thuộc tính maTKNguoiGui của thực thể. */
    public Integer layMaTKNguoiGui() { return maTKNguoiGui; }
    /** Cập nhật thuộc tính maTKNguoiGui trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTKNguoiGui(Integer giaTri) { this.maTKNguoiGui = giaTri; }

}
