package DAO.ThucThe;

import Class.ThucThe.VaiTro;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.VaiTro.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class VaiTroDAO extends CoSoThucTheDAO<VaiTro> {
    public VaiTroDAO() { super("VaiTro", "maVaiTro", "TAIKHOAN", true); }

    @Override
    protected VaiTro anhXa(ResultSet ketQua) throws SQLException {
        VaiTro thucThe = new VaiTro();
        thucThe.ganMaVaiTro((Integer) ketQua.getObject("maVaiTro"));
        thucThe.ganTenVaiTro(ketQua.getString("tenVaiTro"));
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
