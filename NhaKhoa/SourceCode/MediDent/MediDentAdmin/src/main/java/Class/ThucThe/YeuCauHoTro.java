package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.YeuCauHoTro.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maYC.
 */
public final class YeuCauHoTro {
    private Integer maYC; // Cột dbo.YeuCauHoTro.maYC
    private Integer maTK; // Cột dbo.YeuCauHoTro.maTK
    private String tieuDe; // Cột dbo.YeuCauHoTro.tieuDe
    private String noiDung; // Cột dbo.YeuCauHoTro.noiDung
    private LocalDateTime ngayTao; // Cột dbo.YeuCauHoTro.ngayTao
    private String trangThai; // Cột dbo.YeuCauHoTro.trangThai
    private String phanHoi; // Cột dbo.YeuCauHoTro.phanHoi
    private LocalDateTime ngayXuLy; // Cột dbo.YeuCauHoTro.ngayXuLy
    private Integer capBacHoTro; // Cột dbo.YeuCauHoTro.capBacHoTro

    /** Đọc thuộc tính maYC của thực thể. */
    public Integer layMaYC() { return maYC; }
    /** Cập nhật thuộc tính maYC trong bộ nhớ, chưa lưu SQL. */
    public void ganMaYC(Integer giaTri) { this.maYC = giaTri; }

    /** Đọc thuộc tính maTK của thực thể. */
    public Integer layMaTK() { return maTK; }
    /** Cập nhật thuộc tính maTK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTK(Integer giaTri) { this.maTK = giaTri; }

    /** Đọc thuộc tính tieuDe của thực thể. */
    public String layTieuDe() { return tieuDe; }
    /** Cập nhật thuộc tính tieuDe trong bộ nhớ, chưa lưu SQL. */
    public void ganTieuDe(String giaTri) { this.tieuDe = giaTri; }

    /** Đọc thuộc tính noiDung của thực thể. */
    public String layNoiDung() { return noiDung; }
    /** Cập nhật thuộc tính noiDung trong bộ nhớ, chưa lưu SQL. */
    public void ganNoiDung(String giaTri) { this.noiDung = giaTri; }

    /** Đọc thuộc tính ngayTao của thực thể. */
    public LocalDateTime layNgayTao() { return ngayTao; }
    /** Cập nhật thuộc tính ngayTao trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayTao(LocalDateTime giaTri) { this.ngayTao = giaTri; }

    /** Đọc thuộc tính trangThai của thực thể. */
    public String layTrangThai() { return trangThai; }
    /** Cập nhật thuộc tính trangThai trong bộ nhớ, chưa lưu SQL. */
    public void ganTrangThai(String giaTri) { this.trangThai = giaTri; }

    /** Đọc thuộc tính phanHoi của thực thể. */
    public String layPhanHoi() { return phanHoi; }
    /** Cập nhật thuộc tính phanHoi trong bộ nhớ, chưa lưu SQL. */
    public void ganPhanHoi(String giaTri) { this.phanHoi = giaTri; }

    /** Đọc thuộc tính ngayXuLy của thực thể. */
    public LocalDateTime layNgayXuLy() { return ngayXuLy; }
    /** Cập nhật thuộc tính ngayXuLy trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayXuLy(LocalDateTime giaTri) { this.ngayXuLy = giaTri; }

    /** Đọc thuộc tính capBacHoTro của thực thể. */
    public Integer layCapBacHoTro() { return capBacHoTro; }
    /** Cập nhật thuộc tính capBacHoTro trong bộ nhớ, chưa lưu SQL. */
    public void ganCapBacHoTro(Integer giaTri) { this.capBacHoTro = giaTri; }

}
