package DAO.ThucThe;

import Class.ThucThe.YeuCauHuyPhieuThu;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.YeuCauHuyPhieuThu.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class YeuCauHuyPhieuThuDAO extends CoSoThucTheDAO<YeuCauHuyPhieuThu> {
    public YeuCauHuyPhieuThuDAO() { super("YeuCauHuyPhieuThu", "maYC", "THANHTOAN", false); }

    @Override
    protected YeuCauHuyPhieuThu anhXa(ResultSet ketQua) throws SQLException {
        YeuCauHuyPhieuThu thucThe = new YeuCauHuyPhieuThu();
        thucThe.ganMaYC((Integer) ketQua.getObject("maYC"));
        thucThe.ganMaTT((Integer) ketQua.getObject("maTT"));
        thucThe.ganNgayYeuCau(docGioNgay(ketQua, "ngayYeuCau"));
        thucThe.ganLyDo(ketQua.getString("lyDo"));
        thucThe.ganTrangThai(ketQua.getString("trangThai"));
        thucThe.ganMaTKNguoiGui((Integer) ketQua.getObject("maTKNguoiGui"));
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
