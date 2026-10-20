package Class.ThucThe;

import java.time.LocalDateTime;

/**
 * Thực thể đọc dữ liệu từ dbo.TaiKhoan.
 * Các thuộc tính trùng tên cột SQL; kiểu đối tượng giữ nguyên được giá trị NULL.
 * Khóa chính: maTK.
 */
public final class TaiKhoan {
    private Integer maTK; // Cột dbo.TaiKhoan.maTK
    private String tenDangNhap; // Cột dbo.TaiKhoan.tenDangNhap
    private String matKhau; // Cột dbo.TaiKhoan.matKhau
    private LocalDateTime ngayTao; // Cột dbo.TaiKhoan.ngayTao
    private LocalDateTime ngayDangNhapCuoi; // Cột dbo.TaiKhoan.ngayDangNhapCuoi
    private Boolean phaiDoiMatKhau; // Cột dbo.TaiKhoan.phaiDoiMatKhau

    /** Đọc thuộc tính maTK của thực thể. */
    public Integer layMaTK() { return maTK; }
    /** Cập nhật thuộc tính maTK trong bộ nhớ, chưa lưu SQL. */
    public void ganMaTK(Integer giaTri) { this.maTK = giaTri; }

    /** Đọc thuộc tính tenDangNhap của thực thể. */
    public String layTenDangNhap() { return tenDangNhap; }
    /** Cập nhật thuộc tính tenDangNhap trong bộ nhớ, chưa lưu SQL. */
    public void ganTenDangNhap(String giaTri) { this.tenDangNhap = giaTri; }

    /** Đọc thuộc tính matKhau của thực thể. */
    public String layMatKhau() { return matKhau; }
    /** Cập nhật thuộc tính matKhau trong bộ nhớ, chưa lưu SQL. */
    public void ganMatKhau(String giaTri) { this.matKhau = giaTri; }

    /** Đọc thuộc tính ngayTao của thực thể. */
    public LocalDateTime layNgayTao() { return ngayTao; }
    /** Cập nhật thuộc tính ngayTao trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayTao(LocalDateTime giaTri) { this.ngayTao = giaTri; }

    /** Đọc thuộc tính ngayDangNhapCuoi của thực thể. */
    public LocalDateTime layNgayDangNhapCuoi() { return ngayDangNhapCuoi; }
    /** Cập nhật thuộc tính ngayDangNhapCuoi trong bộ nhớ, chưa lưu SQL. */
    public void ganNgayDangNhapCuoi(LocalDateTime giaTri) { this.ngayDangNhapCuoi = giaTri; }

    /** Đọc thuộc tính phaiDoiMatKhau của thực thể. */
    public Boolean layPhaiDoiMatKhau() { return phaiDoiMatKhau; }
    /** Cập nhật thuộc tính phaiDoiMatKhau trong bộ nhớ, chưa lưu SQL. */
    public void ganPhaiDoiMatKhau(Boolean giaTri) { this.phaiDoiMatKhau = giaTri; }

}
