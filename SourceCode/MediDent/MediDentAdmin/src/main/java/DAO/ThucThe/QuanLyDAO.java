package DAO.ThucThe;

import Class.ThucThe.QuanLy;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.QuanLy.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class QuanLyDAO extends CoSoThucTheDAO<QuanLy> {
    public QuanLyDAO() { super("QuanLy", "maNV", "TAIKHOAN", true); }

    @Override
    protected QuanLy anhXa(ResultSet ketQua) throws SQLException {
        QuanLy thucThe = new QuanLy();
        thucThe.ganMaNV((Integer) ketQua.getObject("maNV"));
        thucThe.ganPhongBan(ketQua.getString("phongBan"));
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
