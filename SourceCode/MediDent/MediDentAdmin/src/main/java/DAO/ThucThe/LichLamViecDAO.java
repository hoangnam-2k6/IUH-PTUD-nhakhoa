package DAO.ThucThe;

import Class.ThucThe.LichLamViec;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.LichLamViec.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class LichLamViecDAO extends CoSoThucTheDAO<LichLamViec> {
    public LichLamViecDAO() { super("LichLamViec", "maLichLV", "NHANVIEN", false); }

    @Override
    protected LichLamViec anhXa(ResultSet ketQua) throws SQLException {
        LichLamViec thucThe = new LichLamViec();
        thucThe.ganMaLichLV((Integer) ketQua.getObject("maLichLV"));
        thucThe.ganMaNV((Integer) ketQua.getObject("maNV"));
        thucThe.ganThu(ketQua.getString("thu"));
        thucThe.ganNgayLamViec(docNgay(ketQua, "ngayLamViec"));
        thucThe.ganCaLamViec(ketQua.getString("caLamViec"));
        thucThe.ganGioBatDau(docGio(ketQua, "gioBatDau"));
        thucThe.ganGioKetThuc(docGio(ketQua, "gioKetThuc"));
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
