package Class.ThucThe;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.ChiPhiHoatDong.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maCP.
 */
public final class ChiPhiHoatDong {
    private Integer maCP; // Cột dbo.ChiPhiHoatDong.maCP
    private LocalDate ngayChi; // Cột dbo.ChiPhiHoatDong.ngayChi
    private String loaiChi; // Cột dbo.ChiPhiHoatDong.loaiChi
    private String moTa; // Cột dbo.ChiPhiHoatDong.moTa
    private BigDecimal soTien; // Cột dbo.ChiPhiHoatDong.soTien
    private Integer maTKNguoiNhap; // Cột dbo.ChiPhiHoatDong.maTKNguoiNhap
    private LocalDateTime ngayTao; // Cột dbo.ChiPhiHoatDong.ngayTao

    /** Đọc thuộc tính maCP của thực thể. */
    public Integer layMaCP() { return maCP; }
    /** Cập nhật thuộc tính maCP trong bộ nhớ, chưa lưu SQL. */
    public void ganMaCP(Integer giaTri) { this.maCP = giaTri; }

    /** Đọc thuộc tính ngayChi của thực thể. */
    public LocalDate layNgayChi() { return ngayChi; }
    /** Cập nhật thuộc tính ngayChi trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayChi(LocalDate giaTri) { this.ngayChi = giaTri; }

    /** Đọc thuộc tính loaiChi của thực thể. */
    public String layLoaiChi() { return loaiChi; }
    /** Cập nhật thuộc tính loaiChi trong bộ nhớ, chưa lưu SQL. */
    public void ganLoaiChi(String giaTri) { this.loaiChi = giaTri; }

    /** Đọc thuộc tính moTa của thực thể. */
    public String layMoTa() { return moTa; }
    /** Cập nhật thuộc tính moTa trong bộ nhớ, chưa lưu SQL. */
    public void ganMoTa(String giaTri) { this.moTa = giaTri; }

    /** Đọc thuộc tính soTien của thực thể. */
    public BigDecimal laySoTien() { return soTien; }
    /** Cập nhật thuộc tính soTien trong bộ nhớ, chưa lưu SQL. */
    public void ganSoTien(BigDecimal giaTri) { this.soTien = giaTri; }

    /** Đọc thuộc tính maTKNguoiNhap của thực thể. */
    public Integer layMaTKNguoiNhap() { return maTKNguoiNhap; }
    /** Cập nhật thuộc tính maTKNguoiNhap trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTKNguoiNhap(Integer giaTri) { this.maTKNguoiNhap = giaTri; }

    /** Đọc thuộc tính ngayTao của thực thể. */
    public LocalDateTime layNgayTao() { return ngayTao; }
    /** Cập nhật thuộc tính ngayTao trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayTao(LocalDateTime giaTri) { this.ngayTao = giaTri; }

}
