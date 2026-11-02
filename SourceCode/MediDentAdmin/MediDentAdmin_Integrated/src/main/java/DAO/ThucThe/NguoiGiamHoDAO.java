package DAO.ThucThe;

import Class.ThucThe.NguoiGiamHo;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.NguoiGiamHo.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class NguoiGiamHoDAO extends CoSoThucTheDAO<NguoiGiamHo> {
    public NguoiGiamHoDAO() { super("NguoiGiamHo", "maGH", "BENHNHAN", false); }

    @Override
    protected NguoiGiamHo anhXa(ResultSet ketQua) throws SQLException {
        NguoiGiamHo thucThe = new NguoiGiamHo();
        thucThe.ganMaGH((Integer) ketQua.getObject("maGH"));
        thucThe.ganMaBN((Integer) ketQua.getObject("maBN"));
        thucThe.ganHoTen(ketQua.getString("hoTen"));
        thucThe.ganSdt(ketQua.getString("sdt"));
        thucThe.ganQuanHe(ketQua.getString("quanHe"));
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
