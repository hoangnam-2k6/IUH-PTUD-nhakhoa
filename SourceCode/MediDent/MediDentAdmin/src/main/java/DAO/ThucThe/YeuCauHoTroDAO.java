package DAO.ThucThe;

import Class.ThucThe.YeuCauHoTro;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.YeuCauHoTro.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class YeuCauHoTroDAO extends CoSoThucTheDAO<YeuCauHoTro> {
    public YeuCauHoTroDAO() { super("YeuCauHoTro", "maYC", "HOTRO", false); }

    @Override
    protected YeuCauHoTro anhXa(ResultSet ketQua) throws SQLException {
        YeuCauHoTro thucThe = new YeuCauHoTro();
        thucThe.ganMaYC((Integer) ketQua.getObject("maYC"));
        thucThe.ganMaTK((Integer) ketQua.getObject("maTK"));
        thucThe.ganTieuDe(ketQua.getString("tieuDe"));
        thucThe.ganNoiDung(ketQua.getString("noiDung"));
        thucThe.ganNgayTao(docGioNgay(ketQua, "ngayTao"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
        thucThe.ganPhanHoi(ketQua.getString("phanHoi"));
        thucThe.ganNgayXuLy(docGioNgay(ketQua, "ngayXuLy"));
        thucThe.ganCapBacHoTro((Integer) ketQua.getObject("capBacHoTro"));
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
