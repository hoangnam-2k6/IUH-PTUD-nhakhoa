package DAO.ThucThe;

import Class.ThucThe.Quyen;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.Quyen.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class QuyenDAO extends CoSoThucTheDAO<Quyen> {
    public QuyenDAO() { super("Quyen", "maQuyen", "TAIKHOAN", true); }

    @Override
    protected Quyen anhXa(ResultSet ketQua) throws SQLException {
        Quyen thucThe = new Quyen();
        thucThe.ganMaQuyen(ketQua.getString("maQuyen"));
        thucThe.ganTenQuyen(ketQua.getString("tenQuyen"));
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
