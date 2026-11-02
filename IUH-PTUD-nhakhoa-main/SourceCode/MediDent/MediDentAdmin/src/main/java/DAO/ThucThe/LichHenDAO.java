package DAO.ThucThe;

import Class.ThucThe.LichHen;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.LichHen.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class LichHenDAO extends CoSoThucTheDAO<LichHen> {
    public LichHenDAO() { super("LichHen", "maLichHen", "LICHHEN", false); }

    @Override
    protected LichHen anhXa(ResultSet ketQua) throws SQLException {
        LichHen thucThe = new LichHen();
        thucThe.ganMaLichHen((Integer) ketQua.getObject("maLichHen"));
        thucThe.ganMaBN((Integer) ketQua.getObject("maBN"));
        thucThe.ganMaNV((Integer) ketQua.getObject("maNV"));
        thucThe.ganPhongKham(ketQua.getString("phongKham"));
        thucThe.ganNgayHen(docNgay(ketQua, "ngayHen"));
        thucThe.ganGioHen(docGio(ketQua, "gioHen"));
        thucThe.ganLyDo(ketQua.getString("lyDo"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
        thucThe.ganGhiChu(ketQua.getString("ghiChu"));
        thucThe.ganSoThuTuKham((Integer) ketQua.getObject("soThuTuKham"));
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
