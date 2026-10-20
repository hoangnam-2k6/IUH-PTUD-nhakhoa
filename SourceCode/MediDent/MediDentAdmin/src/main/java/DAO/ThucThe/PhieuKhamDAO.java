package DAO.ThucThe;

import Class.ThucThe.PhieuKham;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.PhieuKham.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class PhieuKhamDAO extends CoSoThucTheDAO<PhieuKham> {
    public PhieuKhamDAO() { super("PhieuKham", "maPK", "BENHNHAN", false); }

    @Override
    protected PhieuKham anhXa(ResultSet ketQua) throws SQLException {
        PhieuKham thucThe = new PhieuKham();
        thucThe.ganMaPK((Integer) ketQua.getObject("maPK"));
        thucThe.ganMaBN((Integer) ketQua.getObject("maBN"));
        thucThe.ganMaNV((Integer) ketQua.getObject("maNV"));
        thucThe.ganNgayKham(docGioNgay(ketQua, "ngayKham"));
        thucThe.ganTrieuChung(ketQua.getString("trieuChung"));
        thucThe.ganChanDoan(ketQua.getString("chanDoan"));
        thucThe.ganPhuongPhapDieuTri(ketQua.getString("phuongPhapDieuTri"));
        thucThe.ganKetQua(ketQua.getString("ketQua"));
        thucThe.ganGhiChu(ketQua.getString("ghiChu"));
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
