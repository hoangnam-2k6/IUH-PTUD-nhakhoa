package DAO.ThucThe;

import Class.ThucThe.BaoCaoLuotKham;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.BaoCaoLuotKham.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class BaoCaoLuotKhamDAO extends CoSoThucTheDAO<BaoCaoLuotKham> {
    public BaoCaoLuotKhamDAO() { super("BaoCaoLuotKham", "maBC", "BAOCAO", false); }

    @Override
    protected BaoCaoLuotKham anhXa(ResultSet ketQua) throws SQLException {
        BaoCaoLuotKham thucThe = new BaoCaoLuotKham();
        thucThe.ganMaBC((Integer) ketQua.getObject("maBC"));
        thucThe.ganMaPK((Integer) ketQua.getObject("maPK"));
        thucThe.ganTuNgay(docNgay(ketQua, "tuNgay"));
        thucThe.ganDenNgay(docNgay(ketQua, "denNgay"));
        thucThe.ganSoLuotKham((Integer) ketQua.getObject("soLuotKham"));
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
