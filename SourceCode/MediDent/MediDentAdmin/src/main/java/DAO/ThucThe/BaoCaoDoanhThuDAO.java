package DAO.ThucThe;

import Class.ThucThe.BaoCaoDoanhThu;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.BaoCaoDoanhThu.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class BaoCaoDoanhThuDAO extends CoSoThucTheDAO<BaoCaoDoanhThu> {
    public BaoCaoDoanhThuDAO() { super("BaoCaoDoanhThu", "maBC", "BAOCAO", false); }

    @Override
    protected BaoCaoDoanhThu anhXa(ResultSet ketQua) throws SQLException {
        BaoCaoDoanhThu thucThe = new BaoCaoDoanhThu();
        thucThe.ganMaBC((Integer) ketQua.getObject("maBC"));
        thucThe.ganMaHD((Integer) ketQua.getObject("maHD"));
        thucThe.ganTuNgay(docNgay(ketQua, "tuNgay"));
        thucThe.ganDenNgay(docNgay(ketQua, "denNgay"));
        thucThe.ganTongDoanhThu(ketQua.getBigDecimal("tongDoanhThu"));
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
