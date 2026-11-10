
package DAO;

import ConnectDB.ConnectDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongKeMoRongDAO {

    public record Bang(String[] cot, List<Object[]> dong) {}

    // Xác định kiểu nhóm báo cáo.
    private String nhomKy(String nhom, String cot) {
        return switch (nhom) {
            case "Ngày" -> "CONVERT(VARCHAR(10)," + cot + ",23)";
            case "Tháng" -> "CONVERT(VARCHAR(7)," + cot + ",23)";
            case "Năm" -> "CONVERT(VARCHAR(4)," + cot + ",23)";
            default -> throw new IllegalArgumentException("Kiểu nhóm không hợp lệ.");
        };
    }

    // Điều kiện lọc từ ngày đến ngày.
    private String locNgay(String cot, LocalDate tu) {
        return tu == null ? "" : " AND " + cot + " >= ? AND " + cot + " < DATEADD(DAY,1,?)";
    }

    // Gán ngày vào PreparedStatement.
    private void ganNgay(PreparedStatement ps, LocalDate tu, LocalDate den) throws SQLException {
        if (tu != null) {
            ps.setDate(1, java.sql.Date.valueOf(tu));
            ps.setDate(2, java.sql.Date.valueOf(den));
        }
    }

    // Gọi chức năng theo loại báo cáo.
    public Bang baoCao(String loai, String nhom, LocalDate tu, LocalDate den) throws SQLException {
        if ("Doanh thu".equals(loai)) return doanhThu(nhom, tu, den);
        if ("Lượt khám".equals(loai)) return luotKham(nhom, tu, den);
        throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
    }

    // Chức năng 1: Thống kê doanh thu.
    private Bang doanhThu(String nhom, LocalDate tu, LocalDate den) throws SQLException {
        String ky = nhomKy(nhom, "x.ngay");

        String sql = "SELECT " + ky + " ky, "
                + "SUM(CASE WHEN x.trangThai=N'Đã thanh toán' THEN 1 ELSE 0 END) daThu, "
                + "SUM(CASE WHEN x.trangThai=N'Chờ thanh toán' THEN 1 ELSE 0 END) choThu, "
                + "COALESCE(SUM(CASE WHEN x.trangThai=N'Đã thanh toán' THEN x.soTien ELSE 0 END),0) doanhThu "
                + "FROM (SELECT CASE WHEN h.trangThai=N'Đã thanh toán' THEN t.ngayThanhToan "
                + "ELSE h.ngayLap END ngay, h.trangThai, t.soTien "
                + "FROM dbo.HoaDon h JOIN dbo.ThanhToan t ON h.maTT=t.maTT AND h.maPK=t.maPK "
                + "WHERE (h.trangThai=N'Đã thanh toán' AND t.trangThai=N'Thành công' AND t.ngayThanhToan IS NOT NULL) "
                + "OR (h.trangThai=N'Chờ thanh toán' AND t.trangThai=N'Chờ xử lý')) x WHERE 1=1"
                + locNgay("x.ngay", tu) + " GROUP BY " + ky + " ORDER BY ky DESC";

        List<Object[]> ds = new ArrayList<>();

        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ganNgay(ps, tu, den);

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 0;

                while (rs.next()) {
                    ds.add(new Object[]{++stt, rs.getString("ky"), rs.getInt("daThu"),
                            rs.getInt("choThu"), rs.getBigDecimal("doanhThu")});
                }
            }
        }

        return new Bang(new String[]{
            "STT", "Kỳ báo cáo", "Hóa đơn đã thu",
            "Hóa đơn chờ", "Doanh thu (VNĐ)"
        }, ds);
    }

    // Chức năng 2: Thống kê lượt khám.
    private Bang luotKham(String nhom, LocalDate tu, LocalDate den) throws SQLException {
        String ky = nhomKy(nhom, "x.ngay");

        String sql = "SELECT " + ky + " ky, SUM(x.lichDat) lichDat, "
                + "SUM(x.daKham) daKham, SUM(x.phieuHoanThanh) phieuHoanThanh "
                + "FROM (SELECT CAST(l.ngayHen AS DATETIME2(0)) ngay, 1 lichDat, "
                + "CASE WHEN l.trangThai=N'Đã khám' THEN 1 ELSE 0 END daKham, 0 phieuHoanThanh "
                + "FROM dbo.LichHen l UNION ALL "
                + "SELECT p.ngayKham, 0, 0, 1 FROM dbo.PhieuKham p "
                + "WHERE p.trangThai IN(N'Hoàn thành',N'Đã khám')) x WHERE 1=1"
                + locNgay("x.ngay", tu) + " GROUP BY " + ky + " ORDER BY ky DESC";

        List<Object[]> ds = new ArrayList<>();

        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ganNgay(ps, tu, den);

            try (ResultSet rs = ps.executeQuery()) {
                int stt = 0;

                while (rs.next()) {
                    ds.add(new Object[]{++stt, rs.getString("ky"), rs.getInt("lichDat"),
                            rs.getInt("daKham"), rs.getInt("phieuHoanThanh")});
                }
            }
        }

        return new Bang(new String[]{
            "STT", "Kỳ báo cáo", "Lịch đã đặt",
            "Lịch đã khám", "Phiếu khám hoàn thành"
        }, ds);
    }
}
