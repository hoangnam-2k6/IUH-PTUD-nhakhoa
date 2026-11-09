package Dao.NghiepVu;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import Dao.TruyVanDAO;

/**
 * Truy vấn chỉ đọc cho Dashboard và Báo cáo Quản lý.
 * Dữ liệu luôn lấy từ SQL Server MediDent, không sinh dữ liệu trong Java.
 */
public final class BaoCaoService {

    public BaoCaoService(PhanQuyen.PhienDangNhap session) {
        // Chế độ xem riêng cho phép SELECT; quyền sửa vẫn được kiểm tra tại Service.
    }

    private void checkRange(LocalDate start, LocalDate end) {
        QuyTacNghiepVu.need(
                start != null && end != null && !start.isAfter(end),
                "Khoảng ngày không hợp lệ");
    }

    /** Doanh thu, số lượt khám và chi phí trong đúng kỳ do người dùng chọn. */
    public List<Map<String, Object>> metrics(LocalDate start, LocalDate end) {
        checkRange(start, end);
        String sql = "SELECT "
                + "(SELECT COALESCE(SUM(soTien),0) FROM dbo.ThanhToan "
                + "WHERE ngayThanhToan>=? AND ngayThanhToan<? "
                + "AND trangThai IN(N'Thành công',N'Đã thanh toán')) AS [Doanh thu thực thu],"
                + "(SELECT COUNT(*) FROM dbo.PhieuKham WHERE ngayKham>=? AND ngayKham<?) AS [Lượt khám],"
                + "(SELECT COUNT(*) FROM (SELECT maBN FROM dbo.PhieuKham "
                + "WHERE ngayKham>=? AND ngayKham<? GROUP BY maBN HAVING COUNT(*)>=2) q) "
                + "AS [Bệnh nhân quay lại],"
                + "(SELECT COALESCE(SUM(soTien),0) FROM dbo.ChiPhiHoatDong "
                + "WHERE ngayChi>=? AND ngayChi<=?) AS [Chi phí đã nhập]";
        return TruyVanDAO.list(
                sql,
                start, end.plusDays(1),
                start, end.plusDays(1),
                start, end.plusDays(1),
                start, end);
    }

    /** Giữ API báo cáo theo năm đang có để không ảnh hưởng các màn hình khác. */
    public List<Map<String, Object>> monthly(int year) {
        QuyTacNghiepVu.need(year >= 2000 && year <= 2100, "Năm không hợp lệ");
        return TruyVanDAO.list(
                "SELECT MONTH(ngayThanhToan) AS [Tháng],COUNT(*) AS [Giao dịch],"
                        + "COALESCE(SUM(soTien),0) AS [Doanh thu] "
                        + "FROM dbo.ThanhToan WHERE YEAR(ngayThanhToan)=? "
                        + "AND trangThai IN(N'Thành công',N'Đã thanh toán') "
                        + "GROUP BY MONTH(ngayThanhToan) ORDER BY [Tháng]",
                year);
    }

    /** Doanh thu theo tháng, chỉ trong khoảng ngày lọc (kể cả năm giao nhau). */
    public List<Map<String, Object>> monthly(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT YEAR(ngayThanhToan) AS [Năm],MONTH(ngayThanhToan) AS [Tháng],"
                        + "COUNT(*) AS [Giao dịch],COALESCE(SUM(soTien),0) AS [Doanh thu] "
                        + "FROM dbo.ThanhToan WHERE ngayThanhToan>=? AND ngayThanhToan<? "
                        + "AND trangThai IN(N'Thành công',N'Đã thanh toán') "
                        + "GROUP BY YEAR(ngayThanhToan),MONTH(ngayThanhToan) "
                        + "ORDER BY [Năm],[Tháng]",
                start,
                end.plusDays(1));
    }

    /** Lượt khám hằng ngày theo ngày bắt đầu khám, không lấy số liệu demo. */
    public List<Map<String, Object>> visits(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT CONVERT(varchar(10),CAST(ngayKham AS date),23) AS [Ngày khám],"
                        + "COUNT(*) AS [Lượt khám] "
                        + "FROM dbo.PhieuKham WHERE ngayKham>=? AND ngayKham<? "
                        + "GROUP BY CAST(ngayKham AS date) ORDER BY [Ngày khám]",
                start,
                end.plusDays(1));
    }


    /** Doanh thu theo từng ngày trong khoảng lọc; dùng ngày thực thu. */
    public List<Map<String, Object>> dailyRevenue(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT CONVERT(varchar(10),CAST(ngayThanhToan AS date),23) AS [Ngày], "
                        + "COUNT(*) AS [Giao dịch], COALESCE(SUM(soTien),0) AS [Doanh thu] "
                        + "FROM dbo.ThanhToan "
                        + "WHERE ngayThanhToan >= ? AND ngayThanhToan < ? "
                        + "AND trangThai IN(N'Thành công',N'Đã thanh toán') "
                        + "GROUP BY CAST(ngayThanhToan AS date) "
                        + "ORDER BY [Ngày]",
                start, end.plusDays(1));
    }

    /** Doanh thu theo từng năm (không lặp các dòng tháng khi chọn Năm nay). */
    public List<Map<String, Object>> yearlyRevenue(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT YEAR(ngayThanhToan) AS [Năm], "
                        + "COUNT(*) AS [Giao dịch], COALESCE(SUM(soTien),0) AS [Doanh thu] "
                        + "FROM dbo.ThanhToan "
                        + "WHERE ngayThanhToan >= ? AND ngayThanhToan < ? "
                        + "AND trangThai IN(N'Thành công',N'Đã thanh toán') "
                        + "GROUP BY YEAR(ngayThanhToan) "
                        + "ORDER BY [Năm]",
                start, end.plusDays(1));
    }

    /** Số lượt khám thực tế được nhóm theo tháng, không nhầm lịch hẹn với lượt khám. */
    public List<Map<String, Object>> monthlyVisits(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT YEAR(ngayKham) AS [Năm], MONTH(ngayKham) AS [Tháng], "
                        + "COUNT(*) AS [Lượt khám] "
                        + "FROM dbo.PhieuKham "
                        + "WHERE ngayKham >= ? AND ngayKham < ? "
                        + "GROUP BY YEAR(ngayKham), MONTH(ngayKham) "
                        + "ORDER BY [Năm], [Tháng]",
                start, end.plusDays(1));
    }

    /** Số lượt khám theo năm, đúng kỳ đã chọn. */
    public List<Map<String, Object>> yearlyVisits(LocalDate start, LocalDate end) {
        checkRange(start, end);
        return TruyVanDAO.list(
                "SELECT YEAR(ngayKham) AS [Năm], COUNT(*) AS [Lượt khám] "
                        + "FROM dbo.PhieuKham "
                        + "WHERE ngayKham >= ? AND ngayKham < ? "
                        + "GROUP BY YEAR(ngayKham) ORDER BY [Năm]",
                start, end.plusDays(1));
    }

    /**
     * Một API thống nhất cho sáu kiểu báo cáo.
     * Mỗi kiểu dùng cùng khoảng ngày, rồi lấp các kỳ không phát sinh bằng 0.
     */
    public List<Map<String, Object>> report(LocalDate start, LocalDate end, String type) {
        validateReportRange(start, end, type);
        List<Map<String, Object>> fetched = switch (type) {
            case "Doanh thu theo ngày" -> dailyRevenue(start, end);
            case "Doanh thu theo tháng" -> monthly(start, end);
            case "Doanh thu theo năm" -> yearlyRevenue(start, end);
            case "Lượt khám theo ngày" -> visits(start, end);
            case "Lượt khám theo tháng" -> monthlyVisits(start, end);
            case "Lượt khám theo năm" -> yearlyVisits(start, end);
            default -> throw new IllegalArgumentException("Loại báo cáo không hợp lệ: " + type);
        };
        return completePeriods(start, end, type, fetched);
    }

    public static void validateReportRange(LocalDate start, LocalDate end, String type) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng Đến ngày");
        }
        boolean day = type.endsWith("theo ngày");
        boolean month = type.endsWith("theo tháng");
        boolean year = type.endsWith("theo năm");
        if (!day && !month && !year) {
            throw new IllegalArgumentException("Chưa chọn loại báo cáo hợp lệ");
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (day && days > 365) {
            throw new IllegalArgumentException("Báo cáo theo ngày tối đa 366 ngày. Hãy chọn theo tháng/năm nếu khoảng dài hơn.");
        }
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                java.time.YearMonth.from(start), java.time.YearMonth.from(end));
        if (month && months > 119) {
            throw new IllegalArgumentException("Báo cáo theo tháng tối đa 120 tháng.");
        }
        if (year && end.getYear() - start.getYear() > 29) {
            throw new IllegalArgumentException("Báo cáo theo năm tối đa 30 năm.");
        }
    }

    /**
     * Kỳ không có doanh thu/lượt khám vẫn được biểu diễn là 0 thay vì bảng trống.
     * Không sinh giao dịch giả: dữ liệu thật vẫn lấy từ SELECT bên trên.
     */
    public static List<Map<String, Object>> completePeriods(
            LocalDate start, LocalDate end, String type, List<Map<String, Object>> fetched) {
        validateReportRange(start, end, type);
        if (fetched == null) {
            throw new IllegalArgumentException("Dữ liệu truy vấn không được null");
        }

        boolean revenue = type.startsWith("Doanh thu");
        boolean day = type.endsWith("theo ngày");
        boolean month = type.endsWith("theo tháng");

        Map<String, Map<String, Object>> byPeriod = new java.util.LinkedHashMap<>();
        for (Map<String, Object> record : fetched) {
            String key;
            if (day) {
                key = String.valueOf(record.get(revenue ? "Ngày" : "Ngày khám"));
            } else if (month) {
                key = record.get("Năm") + "-" + record.get("Tháng");
            } else {
                key = String.valueOf(record.get("Năm"));
            }
            byPeriod.put(key, record);
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        if (day) {
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put(revenue ? "Ngày" : "Ngày khám", date.toString());
                if (revenue) {
                    row.put("Giao dịch", 0);
                    row.put("Doanh thu", java.math.BigDecimal.ZERO);
                } else {
                    row.put("Lượt khám", 0);
                }
                result.add(byPeriod.getOrDefault(date.toString(), row));
            }
        } else if (month) {
            java.time.YearMonth first = java.time.YearMonth.from(start);
            java.time.YearMonth last = java.time.YearMonth.from(end);
            for (java.time.YearMonth period = first;
                    !period.isAfter(last); period = period.plusMonths(1)) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("Năm", period.getYear());
                row.put("Tháng", period.getMonthValue());
                if (revenue) {
                    row.put("Giao dịch", 0);
                    row.put("Doanh thu", java.math.BigDecimal.ZERO);
                } else {
                    row.put("Lượt khám", 0);
                }
                result.add(byPeriod.getOrDefault(period.getYear() + "-" + period.getMonthValue(), row));
            }
        } else {
            for (int year = start.getYear(); year <= end.getYear(); year++) {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("Năm", year);
                if (revenue) {
                    row.put("Giao dịch", 0);
                    row.put("Doanh thu", java.math.BigDecimal.ZERO);
                } else {
                    row.put("Lượt khám", 0);
                }
                result.add(byPeriod.getOrDefault(String.valueOf(year), row));
            }
        }
        return result;
    }

    /** Chỉ số cập nhật tại thời điểm mở trang chủ Quản lý. */
    public List<Map<String, Object>> dashboard() {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM dbo.NhanVien WHERE trangThai=N'Đang làm việc') AS [Nhân viên],"
                + "(SELECT COUNT(*) FROM dbo.BenhNhan "
                + "WHERE ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động') AS [Bệnh nhân],"
                + "(SELECT COUNT(*) FROM dbo.LichHen "
                + "WHERE ngayHen=CAST(GETDATE() AS date)) AS [Lịch hôm nay],"
                + "(SELECT COUNT(*) FROM dbo.DichVu "
                + "WHERE trangThai=N'Đang áp dụng') AS [Dịch vụ],"
                + "(SELECT COALESCE(SUM(soTien),0) FROM dbo.ThanhToan "
                + "WHERE ngayThanhToan>=DATEADD(month,DATEDIFF(month,0,GETDATE()),0) "
                + "AND ngayThanhToan<DATEADD(month,DATEDIFF(month,0,GETDATE())+1,0) "
                + "AND trangThai IN(N'Đã thanh toán',N'Thành công')) AS [Thu tháng]";
        return TruyVanDAO.list(sql);
    }

    /** Thống kê các công việc cần quản lý xử lý tiếp; chỉ thực hiện SELECT. */
    public List<Map<String, Object>> pendingSummary() {
        return TruyVanDAO.list(
                "SELECT (SELECT COUNT(*) FROM dbo.LichHen "
                        + "WHERE ngayHen>=CAST(GETDATE() AS date) AND trangThai=N'Chờ xác nhận') "
                        + "AS [Lịch chờ xác nhận],"
                        + "(SELECT COUNT(*) FROM dbo.HoaDon WHERE trangThai=N'Chờ thanh toán') "
                        + "AS [Hóa đơn chờ thanh toán],"
                        + "(SELECT COUNT(*) FROM dbo.YeuCauHuyPhieuThu WHERE trangThai=N'Chờ duyệt') "
                        + "AS [Yêu cầu hủy chờ duyệt]");
    }

    /** Lịch khám trong ngày giúp quản lý theo dõi trạng thái và phòng/bác sĩ. */
    public List<Map<String, Object>> todaySchedule() {
        return TruyVanDAO.list(
                "SELECT TOP 8 CONVERT(varchar(5),h.gioHen,108) AS [Giờ],"
                        + "b.hoTen AS [Bệnh nhân],"
                        + "ISNULL(n.hoTen,N'Chưa phân công') AS [Bác sĩ],"
                        + "ISNULL(h.phongKham,N'Chưa chọn') AS [Phòng],"
                        + "h.trangThai AS [Trạng thái] "
                        + "FROM dbo.LichHen h "
                        + "JOIN dbo.BenhNhan b ON b.maBN=h.maBN "
                        + "LEFT JOIN dbo.NhanVien n ON n.maNV=h.maNV "
                        + "WHERE h.ngayHen=CAST(GETDATE() AS date) "
                        + "ORDER BY h.gioHen ASC,h.maLichHen ASC");
    }
}
