package DAO.ThucThe;

import Class.ThucThe.TaiKhoan;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.TaiKhoan.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class TaiKhoanDAO extends CoSoThucTheDAO<TaiKhoan> {
    public TaiKhoanDAO() { super("TaiKhoan", "maTK", "TAIKHOAN", true); }

    @Override
    protected TaiKhoan anhXa(ResultSet ketQua) throws SQLException {
        TaiKhoan thucThe = new TaiKhoan();
        thucThe.ganMaTK((Integer) ketQua.getObject("maTK"));
        thucThe.ganTenDangNhap(ketQua.getString("tenDangNhap"));
        thucThe.ganMatKhau(ketQua.getString("matKhau"));
        thucThe.ganNgayTao(docGioNgay(ketQua, "ngayTao"));
        thucThe.ganNgayDangNhapCuoi(docGioNgay(ketQua, "ngayDangNhapCuoi"));
        thucThe.ganPhaiDoiMatKhau((Boolean) ketQua.getObject("phaiDoiMatKhau"));
        return thucThe;
    }

    // Chuyển đổi DATE/TIME không gây mất ngày hoặc giờ khi gặp SQL NULL.
    private static java.time.LocalDate docNgay(ResultSet ketQua, String cot) throws SQLException {
        java.sql.Date ngay = ketQua.getDate(cot);
        return ngay == null ? null : ngay.toLocalDate();
    }
    private static java.time.LocalDateTime docGioNgay(ResultSet ketQua, String cot) throws SQLException {
        java.sql.Timestamp ngay = ketQua.getTimestamp(cot);
        return ngay == null ? null : ngay.toLocalDateTime();
    }
    private static java.time.LocalTime docGio(ResultSet ketQua, String cot) throws SQLException {
        java.sql.Time gio = ketQua.getTime(cot);
        return gio == null ? null : gio.toLocalTime();
    }
}
