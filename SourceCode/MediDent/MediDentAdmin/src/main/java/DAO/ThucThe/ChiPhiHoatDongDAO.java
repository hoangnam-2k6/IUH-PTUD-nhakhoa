package DAO.ThucThe;

import Class.ThucThe.ChiPhiHoatDong;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.ChiPhiHoatDong.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class ChiPhiHoatDongDAO extends CoSoThucTheDAO<ChiPhiHoatDong> {
    public ChiPhiHoatDongDAO() { super("ChiPhiHoatDong", "maCP", "BAOCAO", false); }

    @Override
    protected ChiPhiHoatDong anhXa(ResultSet ketQua) throws SQLException {
        ChiPhiHoatDong thucThe = new ChiPhiHoatDong();
        thucThe.ganMaCP((Integer) ketQua.getObject("maCP"));
        thucThe.ganNgayChi(docNgay(ketQua, "ngayChi"));
        thucThe.ganLoaiChi(ketQua.getString("loaiChi"));
        thucThe.ganMoTa(ketQua.getString("moTa"));
        thucThe.ganSoTien(ketQua.getBigDecimal("soTien"));
        thucThe.ganMaTKNguoiNhap((Integer) ketQua.getObject("maTKNguoiNhap"));
        thucThe.ganNgayTao(docGioNgay(ketQua, "ngayTao"));
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
