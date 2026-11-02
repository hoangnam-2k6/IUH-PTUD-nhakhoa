package DAO.ThucThe;

import Class.ThucThe.PhieuDuyetHuyPhieuThu;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO ánh xạ dbo.PhieuDuyetHuyPhieuThu.
 * Chỉ đọc và tra cứu; thêm/sửa/xóa nghiệp vụ dùng DAO có kiểm tra quyền.
 */
public final class PhieuDuyetHuyPhieuThuDAO extends CoSoThucTheDAO<PhieuDuyetHuyPhieuThu> {
    public PhieuDuyetHuyPhieuThuDAO() { super("PhieuDuyetHuyPhieuThu", "maPD", "THANHTOAN", false); }

    @Override
    protected PhieuDuyetHuyPhieuThu anhXa(ResultSet ketQua) throws SQLException {
        PhieuDuyetHuyPhieuThu thucThe = new PhieuDuyetHuyPhieuThu();
        thucThe.ganMaPD((Integer) ketQua.getObject("maPD"));
        thucThe.ganMaYC((Integer) ketQua.getObject("maYC"));
        thucThe.ganNgayPheDuyet(docGioNgay(ketQua, "ngayPheDuyet"));
        thucThe.ganLyDoPheDuyet(ketQua.getString("lyDoPheDuyet"));
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
