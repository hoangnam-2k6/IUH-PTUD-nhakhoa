package DAO;

import ConnectDB.KetNoi;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO dành riêng cho hai trang báo cáo.
 *
 * Các truy vấn tương thích SQL Server 2012 và đúng tên bảng/cột trong
 * SQLData/01_MediDent.sql. Doanh thu chỉ lấy thanh toán thành công.
 */
public class BaoCaoDuLieuDAO {

    /** Một dòng dữ liệu của biểu đồ tháng. */
    public static final class Thang {
        public final YearMonth thang;
        public final BigDecimal doanhThu;
        public final int luotKham;

        public Thang(YearMonth thang, BigDecimal doanhThu, int luotKham) {
            this.thang = thang;
            this.doanhThu = doanhThu;
            this.luotKham = luotKham;
        }
    }

    /** Lượt THỰC HIỆN dịch vụ điều trị, không phải số lượt khám duy nhất. */
    public static final class DichVu {
        public final String ten;
        public final int soLuot;

        public DichVu(String ten, int soLuot) {
            this.ten = ten;
            this.soLuot = soLuot;
        }
    }

    /** Dữ liệu chụp cùng một khoảng ngày cho hai màn hình. */
    public static final class DuLieu {
        public LocalDate tuNgay;
        public LocalDate denNgay;
        public BigDecimal doanhThu = BigDecimal.ZERO;
        public int luotKham;
        public int soBenhNhan;
        public int benhNhanQuayLai;
        public final List<Thang> theoThang = new ArrayList<>();
        public final List<DichVu> dichVu = new ArrayList<>();
        public final List<String[]> vaiTro = new ArrayList<>();
        public final List<String[]> nhatKy = new ArrayList<>();

        public double tyLeQuayLai() {
            return soBenhNhan == 0 ? 0 : benhNhanQuayLai * 100.0 / soBenhNhan;
        }
    }

    /**
     * Lấy dữ liệu từ cùng cơ sở dữ liệu SQL Server.
     * @param quanTriVien true mới tải phân bố tài khoản và nhật ký hệ thống.
     */
    public DuLieu layBaoCao(LocalDate tuNgay, LocalDate denNgay,
                           boolean quanTriVien) throws SQLException {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
        }
        DuLieu ketQua = new DuLieu();
        ketQua.tuNgay = tuNgay;
        ketQua.denNgay = denNgay;

        try (Connection ketNoi = KetNoi.mo()) {
            layTongDoanhThu(ketNoi, ketQua);
            layThongKeLuotKham(ketNoi, ketQua);
            layTheoThang(ketNoi, ketQua);
            layTheoDichVu(ketNoi, ketQua);

            if (quanTriVien) {
                layPhanBoVaiTro(ketNoi, ketQua);
                layNhatKy(ketNoi, ketQua);
            }
        }
        return ketQua;
    }

    /** Chỉ SUM các giao dịch đã thành công trong khoảng [từ, đến]. */
    private void layTongDoanhThu(Connection ketNoi, DuLieu dl) throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(soTien), 0) FROM dbo.ThanhToan " +
            "WHERE ngayThanhToan >= ? AND ngayThanhToan < ? " +
            "AND trangThai IN (N'Thành công', N'Đã thanh toán')";

        try (PreparedStatement ps = ketNoi.prepareStatement(sql)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dl.doanhThu = rs.getBigDecimal(1);
            }
        }
    }

    /**
     * Khách quay lại = số bệnh nhân có từ 2 phiếu khám trong kỳ.
     * Tỷ lệ này tính theo bệnh nhân duy nhất, không theo lượt khám.
     */
    private void layThongKeLuotKham(Connection ketNoi, DuLieu dl) throws SQLException {
        String sql =
            "SELECT COUNT(*) AS soBenhNhan, " +
            "COALESCE(SUM(soLan), 0) AS soLuot, " +
            "COALESCE(SUM(CASE WHEN soLan >= 2 THEN 1 ELSE 0 END), 0) AS quayLai " +
            "FROM (SELECT maBN, COUNT(*) AS soLan FROM dbo.PhieuKham " +
            "WHERE ngayKham >= ? AND ngayKham < ? GROUP BY maBN) AS T";

        try (PreparedStatement ps = ketNoi.prepareStatement(sql)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dl.soBenhNhan = rs.getInt("soBenhNhan");
                    dl.luotKham = rs.getInt("soLuot");
                    dl.benhNhanQuayLai = rs.getInt("quayLai");
                }
            }
        }
    }

    /**
     * Gộp doanh thu và lượt khám theo tháng, bổ sung tháng trống bằng giá trị 0.
     * Khoảng ngày có thể bắt đầu hoặc kết thúc giữa một tháng.
     */
    private void layTheoThang(Connection ketNoi, DuLieu dl) throws SQLException {
        List<YearMonth> danhSachThang = new ArrayList<>();
        List<BigDecimal> doanhThu = new ArrayList<>();
        List<Integer> luotKham = new ArrayList<>();

        for (YearMonth thang = YearMonth.from(dl.tuNgay);
             !thang.isAfter(YearMonth.from(dl.denNgay));
             thang = thang.plusMonths(1)) {
            danhSachThang.add(thang);
            doanhThu.add(BigDecimal.ZERO);
            luotKham.add(0);
        }

        String sqlThu =
            "SELECT YEAR(ngayThanhToan), MONTH(ngayThanhToan), SUM(soTien) " +
            "FROM dbo.ThanhToan WHERE ngayThanhToan >= ? AND ngayThanhToan < ? " +
            "AND trangThai IN (N'Thành công', N'Đã thanh toán') " +
            "GROUP BY YEAR(ngayThanhToan), MONTH(ngayThanhToan)";

        try (PreparedStatement ps = ketNoi.prepareStatement(sqlThu)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int viTri = danhSachThang.indexOf(YearMonth.of(rs.getInt(1), rs.getInt(2)));
                    if (viTri >= 0) doanhThu.set(viTri, rs.getBigDecimal(3));
                }
            }
        }

        String sqlKham =
            "SELECT YEAR(ngayKham), MONTH(ngayKham), COUNT(*) " +
            "FROM dbo.PhieuKham WHERE ngayKham >= ? AND ngayKham < ? " +
            "GROUP BY YEAR(ngayKham), MONTH(ngayKham)";

        try (PreparedStatement ps = ketNoi.prepareStatement(sqlKham)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int viTri = danhSachThang.indexOf(YearMonth.of(rs.getInt(1), rs.getInt(2)));
                    if (viTri >= 0) luotKham.set(viTri, rs.getInt(3));
                }
            }
        }

        for (int i = 0; i < danhSachThang.size(); i++) {
            dl.theoThang.add(new Thang(danhSachThang.get(i), doanhThu.get(i), luotKham.get(i)));
        }
    }

    /** Lấy TOP 6 dịch vụ điều trị theo số lần thực hiện trong kỳ. */
    private void layTheoDichVu(Connection ketNoi, DuLieu dl) throws SQLException {
        String sql =
            "SELECT TOP 6 dv.tenDDT, SUM(ct.soLuong) AS soLuot " +
            "FROM dbo.ChiTietDieuTri ct " +
            "JOIN dbo.DichVuDieuTri dv ON ct.maDDT = dv.maDDT " +
            "JOIN dbo.PhieuKham pk ON ct.maPK = pk.maPK " +
            "WHERE pk.ngayKham >= ? AND pk.ngayKham < ? " +
            "GROUP BY dv.maDDT, dv.tenDDT ORDER BY soLuot DESC, dv.tenDDT";

        try (PreparedStatement ps = ketNoi.prepareStatement(sql)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) dl.dichVu.add(new DichVu(rs.getString(1), rs.getInt(2)));
            }
        }
    }

    /** Snapshot phân bố vai trò, không áp dụng bộ lọc thời gian. */
    private void layPhanBoVaiTro(Connection ketNoi, DuLieu dl) throws SQLException {
        String sql =
            "SELECT v.tenVaiTro, COUNT(t.maTK) AS soTaiKhoan " +
            "FROM dbo.VaiTro v LEFT JOIN dbo.TaiKhoan t ON v.maVaiTro = t.maVaiTro " +
            "GROUP BY v.maVaiTro, v.tenVaiTro ORDER BY soTaiKhoan DESC";

        try (PreparedStatement ps = ketNoi.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                dl.vaiTro.add(new String[] {rs.getString(1), String.valueOf(rs.getInt(2))});
            }
        }
    }

    /** Chỉ Admin: nhật ký trong khoảng ngày đã chọn (tối đa 150 dòng). */
    private void layNhatKy(Connection ketNoi, DuLieu dl) throws SQLException {
        String sql =
            "SELECT TOP 150 CONVERT(VARCHAR(19), thoiGian, 120), hanhDong, " +
            "COALESCE(doiTuong, N'') + N' - ' + COALESCE(moTa, N'') " +
            "FROM dbo.NhatKyHeThong WHERE thoiGian >= ? AND thoiGian < ? " +
            "ORDER BY thoiGian DESC, maNK DESC";

        try (PreparedStatement ps = ketNoi.prepareStatement(sql)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dl.nhatKy.add(new String[] {
                        rs.getString(1), rs.getString(2), rs.getString(3)
                    });
                }
            }
        }
    }

    /** Bộ lọc có cận trên loại trừ để lấy trọn cả ngày đến. */
    private static void datKhoangNgay(PreparedStatement ps,
                                     LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        ps.setDate(1, Date.valueOf(tuNgay));
        ps.setDate(2, Date.valueOf(denNgay.plusDays(1)));
    }
}
