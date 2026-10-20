package DAO.ThucThe;

import Class.ThucThe.HoaDon;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.HoaDon.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class HoaDonDAO extends CoSoThucTheDAO<HoaDon> {
    public HoaDonDAO() { super("HoaDon", "maHD", "THANHTOAN", false); }

    @Override
    protected HoaDon anhXa(ResultSet ketQua) throws SQLException {
        HoaDon thucThe = new HoaDon();
        thucThe.ganMaHD((Integer) ketQua.getObject("maHD"));
        thucThe.ganMaTT((Integer) ketQua.getObject("maTT"));
        thucThe.ganMaPK((Integer) ketQua.getObject("maPK"));
        thucThe.ganNgayLap(docGioNgay(ketQua, "ngayLap"));
        thucThe.ganTongTien(ketQua.getBigDecimal("tongTien"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
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
