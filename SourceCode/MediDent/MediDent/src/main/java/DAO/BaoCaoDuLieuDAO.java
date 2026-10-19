/*
 * BaoCaoDuLieuDAO: truy cập dữ liệu và kiểm soát nghiệp vụ SQL Server.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
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

public class BaoCaoDuLieuDAO {

    public static final class Thang {
        public final YearMonth thang;
        public final BigDecimal doanhThu;
        public final int luotKham;
        public final BigDecimal chiPhi;

        /** Lưu số liệu doanh thu, lượt khám và chi phí của một tháng báo cáo. */
        public Thang(YearMonth thang, BigDecimal doanhThu, int luotKham) {
            this(thang, doanhThu, luotKham, BigDecimal.ZERO);
        }

        /** Lưu số liệu doanh thu, lượt khám và chi phí của một tháng báo cáo. */
        public Thang(YearMonth thang, BigDecimal doanhThu, int luotKham,
                    BigDecimal chiPhi) {
            this.thang = thang;
            this.doanhThu = doanhThu;
            this.luotKham = luotKham;
            this.chiPhi = chiPhi;
        }

        /** Tính chênh lệch doanh thu đã ghi nhận và chi phí đã nhập. */
        public BigDecimal laiLoTamTinh() {
            return doanhThu.subtract(chiPhi);
        }
    }

    public static final class DichVu {
        public final String ten;
        public final int soLuot;

        /** Gắn tên dịch vụ và số lượt đã thực hiện để tổng hợp thống kê. */
        public DichVu(String ten, int soLuot) {
            this.ten = ten;
            this.soLuot = soLuot;
        }
    }

    public static final class DuLieu {
        public LocalDate tuNgay;
        public LocalDate denNgay;
        public BigDecimal doanhThu = BigDecimal.ZERO;

        public BigDecimal chiPhi = BigDecimal.ZERO;
        public int soKhoanChi;
        public int luotKham;
        public int soBenhNhan;
        public int benhNhanQuayLai;
        public final List<Thang> theoThang = new ArrayList<>();
        public final List<DichVu> dichVu = new ArrayList<>();
        public final List<String[]> vaiTro = new ArrayList<>();
        public final List<String[]> nhatKy = new ArrayList<>();

        /** Tính tỷ lệ bệnh nhân quay lại trên tập lượt khám đang xem. */
        public double tyLeQuayLai() {
            return soBenhNhan == 0 ? 0 : benhNhanQuayLai * 100.0 / soBenhNhan;
        }

        /** Tính chênh lệch doanh thu đã ghi nhận và chi phí đã nhập. */
        public BigDecimal laiLoTamTinh() {
            return doanhThu.subtract(chiPhi);
        }
    }

    /** Tổng hợp dữ liệu báo cáo theo khoảng ngày chỉ định từ SQL. */
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
            if (!quanTriVien) {

                layChiPhi(ketNoi, ketQua);
            }
            layTheoDichVu(ketNoi, ketQua);

            if (quanTriVien) {
                layPhanBoVaiTro(ketNoi, ketQua);
                layNhatKy(ketNoi, ketQua);
            }
        }
        return ketQua;
    }

    /** Chỉ cộng giao dịch có trạng thái thanh toán thành công để tránh tăng doanh thu giả. */
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

    /** Đếm số lượt khám, số bệnh nhân riêng biệt và bệnh nhân quay lại trong khoảng ngày. */
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

    /** Gộp doanh thu và lượt khám theo từng tháng, bao gồm tháng không có phát sinh. */
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

    /** Tải tổng chi phí hoạt động trong kỳ nếu bảng dữ liệu chi phí đã được triển khai. */
    private void layChiPhi(Connection c, DuLieu dl) throws SQLException {
        try (PreparedStatement check = c.prepareStatement(
                "SELECT OBJECT_ID(N'dbo.ChiPhiHoatDong', N'U')" );
             ResultSet r = check.executeQuery()) {
            if (!r.next() || r.getObject(1) == null) {
                throw new SQLException("Chưa có bảng chi phí. Hãy chạy SQLData/08_ChiPhiVaLaiLo_SQL2012.sql trước.");
            }
        }
        String sqlTong = "SELECT COUNT(*), COALESCE(SUM(soTien), 0) FROM dbo.ChiPhiHoatDong "
                + "WHERE ngayChi >= ? AND ngayChi < ?";
        try (PreparedStatement ps = c.prepareStatement(sqlTong)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dl.soKhoanChi = rs.getInt(1);
                    dl.chiPhi = rs.getBigDecimal(2);
                }
            }
        }
        String sqlThang = "SELECT YEAR(ngayChi), MONTH(ngayChi), COALESCE(SUM(soTien),0) "
                + "FROM dbo.ChiPhiHoatDong WHERE ngayChi >= ? AND ngayChi < ? "
                + "GROUP BY YEAR(ngayChi), MONTH(ngayChi)";
        try (PreparedStatement ps = c.prepareStatement(sqlThang)) {
            datKhoangNgay(ps, dl.tuNgay, dl.denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    YearMonth ky = YearMonth.of(rs.getInt(1), rs.getInt(2));
                    for (int i = 0; i < dl.theoThang.size(); i++) {
                        Thang current = dl.theoThang.get(i);
                        if (current.thang.equals(ky)) {
                            dl.theoThang.set(i, new Thang(current.thang,
                                    current.doanhThu, current.luotKham, rs.getBigDecimal(3)));
                            break;
                        }
                    }
                }
            }
        }
    }

    /** Ghi dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public void ghiChiPhi(LocalDate ngayChi, String loaiChi,
                          String moTa, BigDecimal soTien, int maTaiKhoan) throws SQLException {
        if (ngayChi == null || ngayChi.isAfter(LocalDate.now())
                || ngayChi.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new SQLException("Ngày chi phí phải hợp lệ, không ở tương lai.");
        }
        if (loaiChi == null || !java.util.Arrays.asList(
                "Vật tư", "Nhân sự", "Vận hành", "Thuê mặt bằng", "Khác").contains(loaiChi)) {
            throw new SQLException("Nhóm chi phí không hợp lệ.");
        }
        if (soTien == null || soTien.signum() <= 0 || soTien.scale() > 2
                || soTien.compareTo(new BigDecimal("9999999999999999.99")) > 0) {
            throw new SQLException("Số tiền chi phải lớn hơn 0 và nằm trong giới hạn hợp lệ.");
        }
        if (moTa == null || moTa.trim().isEmpty() || moTa.trim().length() > 500) {
            throw new SQLException("Mô tả chi phí bắt buộc (tối đa 500 ký tự).");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                String sqlQuyen = "SELECT 1 FROM dbo.TaiKhoan t "
                        + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                        + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
                        + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                        + "AND v.tenVaiTro IN (N'Quản lý', N'Quản lý phòng khám') "
                        + "AND q.maQuyen='BAOCAO'";
                try (PreparedStatement ps = c.prepareStatement(sqlQuyen)) {
                    ps.setInt(1, maTaiKhoan);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Chỉ tài khoản Quản lý có quyền Báo cáo mới được ghi chi phí.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT dbo.ChiPhiHoatDong(ngayChi,loaiChi,moTa,soTien,maTKNguoiNhap) "
                      + "VALUES(?,?,?,?,?)")) {
                    ps.setDate(1, Date.valueOf(ngayChi));
                    ps.setString(2, loaiChi);
                    ps.setString(3, moTa.trim());
                    ps.setBigDecimal(4, soTien);
                    ps.setInt(5, maTaiKhoan);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không lưu được chi phí.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    ps.setString(1, "Ghi chi phí");
                    ps.setString(2, "ChiPhiHoatDong");
                    ps.setString(3, "maTK=" + maTaiKhoan + " | " + loaiChi + " | " + soTien);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Thống kê sáu loại điều trị có số lượt thực hiện nhiều nhất. */
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

    /** Đếm tài khoản thuộc từng vai trò để tạo biểu đồ phân bổ tài khoản. */
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

    /** Tải các sự kiện hệ thống gần nhất phục vụ báo cáo quản trị. */
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

    /** Gắn khoảng lọc ngày bắt đầu và ngày kết thúc (loại trừ đầu ngày kế tiếp) cho truy vấn. */
    private static void datKhoangNgay(PreparedStatement ps,
                                     LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        ps.setDate(1, Date.valueOf(tuNgay));
        ps.setDate(2, Date.valueOf(denNgay.plusDays(1)));
    }
}
