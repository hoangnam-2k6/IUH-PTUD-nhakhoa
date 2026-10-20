package DAO.ThucThe;

import Class.ThucThe.ChiTietDieuTri;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.ChiTietDieuTri.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class ChiTietDieuTriDAO extends CoSoThucTheDAO<ChiTietDieuTri> {
    public ChiTietDieuTriDAO() { super("ChiTietDieuTri", "maCT", "BENHNHAN", false); }

    @Override
    protected ChiTietDieuTri anhXa(ResultSet ketQua) throws SQLException {
        ChiTietDieuTri thucThe = new ChiTietDieuTri();
        thucThe.ganMaCT((Integer) ketQua.getObject("maCT"));
        thucThe.ganMaPK((Integer) ketQua.getObject("maPK"));
        thucThe.ganMaDDT((Integer) ketQua.getObject("maDDT"));
        thucThe.ganNoiDung(ketQua.getString("noiDung"));
        thucThe.ganSoLuong((Integer) ketQua.getObject("soLuong"));
        thucThe.ganDonGia(ketQua.getBigDecimal("donGia"));
        thucThe.ganThanhTien(ketQua.getBigDecimal("thanhTien"));
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
