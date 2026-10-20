package DAO.ThucThe;

import Class.ThucThe.BenhNhan;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.BenhNhan.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class BenhNhanDAO extends CoSoThucTheDAO<BenhNhan> {
    public BenhNhanDAO() { super("BenhNhan", "maBN", "BENHNHAN", false); }

    @Override
    protected BenhNhan anhXa(ResultSet ketQua) throws SQLException {
        BenhNhan thucThe = new BenhNhan();
        thucThe.ganMaBN((Integer) ketQua.getObject("maBN"));
        thucThe.ganHoTen(ketQua.getString("hoTen"));
        thucThe.ganNgaySinh(docNgay(ketQua, "ngaySinh"));
        thucThe.ganGioiTinh(ketQua.getString("gioiTinh"));
        thucThe.ganSdt(ketQua.getString("sdt"));
        thucThe.ganEmail(ketQua.getString("email"));
        thucThe.ganDiaChi(ketQua.getString("diaChi"));
        thucThe.ganTienSuBenh(ketQua.getString("tienSuBenh"));
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
