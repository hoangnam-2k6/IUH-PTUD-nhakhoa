package DAO.ThucThe;

import Class.ThucThe.NhatKyHeThong;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.NhatKyHeThong.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class NhatKyHeThongDAO extends CoSoThucTheDAO<NhatKyHeThong> {
    public NhatKyHeThongDAO() { super("NhatKyHeThong", "maNK", "HETHONG", true); }

    @Override
    protected NhatKyHeThong anhXa(ResultSet ketQua) throws SQLException {
        NhatKyHeThong thucThe = new NhatKyHeThong();
        thucThe.ganMaNK((Long) ketQua.getObject("maNK"));
        thucThe.ganThoiGian(docGioNgay(ketQua, "thoiGian"));
        thucThe.ganHanhDong(ketQua.getString("hanhDong"));
        thucThe.ganDoiTuong(ketQua.getString("doiTuong"));
        thucThe.ganMoTa(ketQua.getString("moTa"));
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
