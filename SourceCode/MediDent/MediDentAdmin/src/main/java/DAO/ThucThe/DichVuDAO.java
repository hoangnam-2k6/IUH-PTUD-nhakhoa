package DAO.ThucThe;

import Class.ThucThe.DichVu;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.DichVu.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class DichVuDAO extends CoSoThucTheDAO<DichVu> {
    public DichVuDAO() { super("DichVu", "maDV", "DICHVU", false); }

    @Override
    protected DichVu anhXa(ResultSet ketQua) throws SQLException {
        DichVu thucThe = new DichVu();
        thucThe.ganMaDV((Integer) ketQua.getObject("maDV"));
        thucThe.ganTenDV(ketQua.getString("tenDV"));
        thucThe.ganMoTa(ketQua.getString("moTa"));
        thucThe.ganDonGia(ketQua.getBigDecimal("donGia"));
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
