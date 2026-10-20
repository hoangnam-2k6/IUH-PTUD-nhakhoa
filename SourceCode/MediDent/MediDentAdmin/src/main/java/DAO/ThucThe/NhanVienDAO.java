package DAO.ThucThe;

import Class.ThucThe.NhanVien;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.NhanVien.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class NhanVienDAO extends CoSoThucTheDAO<NhanVien> {
    public NhanVienDAO() { super("NhanVien", "maNV", "NHANVIEN", false); }

    @Override
    protected NhanVien anhXa(ResultSet ketQua) throws SQLException {
        NhanVien thucThe = new NhanVien();
        thucThe.ganMaNV((Integer) ketQua.getObject("maNV"));
        thucThe.ganHoTen(ketQua.getString("hoTen"));
        thucThe.ganNgaySinh(docNgay(ketQua, "ngaySinh"));
        thucThe.ganGioiTinh(ketQua.getString("gioiTinh"));
        thucThe.ganSdt(ketQua.getString("sdt"));
        thucThe.ganEmail(ketQua.getString("email"));
        thucThe.ganDiaChi(ketQua.getString("diaChi"));
        thucThe.ganChucVu(ketQua.getString("chucVu"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
        thucThe.ganMaTK((Integer) ketQua.getObject("maTK"));
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
