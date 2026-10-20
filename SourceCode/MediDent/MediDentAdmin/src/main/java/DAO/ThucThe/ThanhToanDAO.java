package DAO.ThucThe;

import Class.ThucThe.ThanhToan;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.ThanhToan.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class ThanhToanDAO extends CoSoThucTheDAO<ThanhToan> {
    public ThanhToanDAO() { super("ThanhToan", "maTT", "THANHTOAN", false); }

    @Override
    protected ThanhToan anhXa(ResultSet ketQua) throws SQLException {
        ThanhToan thucThe = new ThanhToan();
        thucThe.ganMaTT((Integer) ketQua.getObject("maTT"));
        thucThe.ganMaPK((Integer) ketQua.getObject("maPK"));
        thucThe.ganNgayThanhToan(docGioNgay(ketQua, "ngayThanhToan"));
        thucThe.ganSoTien(ketQua.getBigDecimal("soTien"));
        thucThe.ganPhuongThuc(ketQua.getString("phuongThuc"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
        thucThe.ganMaGiaoDich(ketQua.getString("maGiaoDich"));
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
