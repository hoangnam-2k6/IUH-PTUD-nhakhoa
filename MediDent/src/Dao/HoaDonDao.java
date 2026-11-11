package Dao;

import ConnectDB.KetNoi;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.PhieuKham;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDao {
    public static final String CHUA_THANH_TOAN = HoaDon.CHUA_THANH_TOAN;
    public static final String DA_THANH_TOAN = HoaDon.DA_THANH_TOAN;
    public static final String DA_HUY = HoaDon.DA_HUY;
    public static final String[] TRANG_THAI = {CHUA_THANH_TOAN, DA_THANH_TOAN, DA_HUY};

    private static final String SELECT = "SELECT hd.MaHD, hd.MaPK, hd.MaTK, tk.HoTen AS TenNguoiLap, hd.NgayLap, "
            + "hd.TongTien, hd.GiamGia, hd.MaGiam, mg.Ma AS MaGiamCode, "
            + "hd.PhuongThucTT, hd.NgayThanhToan, hd.TienKhachDua, hd.TienThua, "
            + "hd.TrangThai, hd.GhiChu, hd.LyDoHuy, bn.MaBN, bn.HoTen AS TenBN, bn.SoDienThoai, "
            + "pk.NgayKham, pk.SoThuTu, pk.TrangThai AS TrangThaiPhieu, p.TenPhong "
            + "FROM HoaDon hd "
            + "JOIN PhieuKham pk ON pk.MaPK = hd.MaPK "
            + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
            + "LEFT JOIN TaiKhoan tk ON tk.MaTK = hd.MaTK "
            + "LEFT JOIN PhongKham p ON p.MaPhong = pk.MaPhong "
            + "LEFT JOIN MaGiamGia mg ON mg.MaGiam = hd.MaGiam ";

    /** Bản SELECT có khóa dòng hóa đơn để dùng trong giao dịch. */
    private static final String SELECT_LOCK = SELECT.replace(
            "FROM HoaDon hd ", "FROM HoaDon hd WITH (UPDLOCK, HOLDLOCK) ");

    public List<HoaDon> findAll() throws SQLException {
        return tim(null, null, null, null);
    }

    /**
     * Tìm hóa đơn theo từ khóa / trạng thái / khoảng ngày lập.
     * Các tham số null hoặc rỗng được bỏ qua.
     */
    public List<HoaDon> tim(String tuKhoa, String trangThai, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        return tim(tuKhoa, "Tất cả", trangThai, tuNgay, denNgay);
    }

    /** Tìm hóa đơn theo tiêu chí cụ thể; trạng thái và khoảng ngày vẫn là bộ lọc bổ sung. */
    public List<HoaDon> tim(String tuKhoa, String truongLoc, String trangThai,
                            LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1");
        List<Object> thamSo = new ArrayList<>();

        if (trangThai != null && !trangThai.isBlank() && !"Tất cả".equals(trangThai)) {
            sql.append(" AND hd.TrangThai = ?");
            thamSo.add(trangThai);
        }
        if (tuNgay != null) {
            sql.append(" AND hd.NgayLap >= ?");
            thamSo.add(Timestamp.valueOf(tuNgay.atStartOfDay()));
        }
        if (denNgay != null) {
            sql.append(" AND hd.NgayLap < ?");
            thamSo.add(Timestamp.valueOf(denNgay.plusDays(1).atStartOfDay()));
        }
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            String k = tuKhoa.trim();
            String mau = "%" + k + "%";
            String truong = truongLoc == null ? "Tất cả" : truongLoc;
            switch (truong) {
                case "Mã hóa đơn":
                    sql.append(" AND (CAST(hd.MaHD AS VARCHAR(20)) LIKE ? "
                            + "OR ('HD' + RIGHT('000' + CAST(hd.MaHD AS VARCHAR(20)), 3)) LIKE ?)");
                    thamSo.add(mau);
                    thamSo.add(k.toUpperCase() + "%");
                    break;
                case "Bệnh nhân":
                    sql.append(" AND bn.HoTen COLLATE Vietnamese_CI_AI LIKE ?");
                    thamSo.add(mau);
                    break;
                case "Số điện thoại":
                    sql.append(" AND bn.SoDienThoai LIKE ?");
                    thamSo.add(mau);
                    break;
                case "Mã bệnh nhân":
                    sql.append(" AND (CAST(bn.MaBN AS VARCHAR(20)) LIKE ? "
                            + "OR ('BN' + RIGHT('000' + CAST(bn.MaBN AS VARCHAR(20)), 3)) LIKE ?)");
                    thamSo.add(mau);
                    thamSo.add(k.toUpperCase() + "%");
                    break;
                case "Mã phiếu khám":
                    sql.append(" AND CAST(hd.MaPK AS VARCHAR(20)) LIKE ?");
                    thamSo.add(mau);
                    break;
                case "Phương thức":
                    sql.append(" AND hd.PhuongThucTT COLLATE Vietnamese_CI_AI LIKE ?");
                    thamSo.add(mau);
                    break;
                default:
                    sql.append(" AND (bn.HoTen COLLATE Vietnamese_CI_AI LIKE ? OR bn.SoDienThoai LIKE ? "
                            + "OR CAST(hd.MaHD AS VARCHAR(20)) = ? OR 'HD' + RIGHT('000' + CAST(hd.MaHD AS VARCHAR(20)), 3) = ? "
                            + "OR CAST(bn.MaBN AS VARCHAR(20)) = ? OR 'BN' + RIGHT('000' + CAST(bn.MaBN AS VARCHAR(20)), 3) = ? "
                            + "OR CAST(hd.MaPK AS VARCHAR(20)) = ? OR hd.PhuongThucTT COLLATE Vietnamese_CI_AI LIKE ?)");
                    thamSo.add(mau);
                    thamSo.add(mau);
                    thamSo.add(k);
                    thamSo.add(k.toUpperCase());
                    thamSo.add(k);
                    thamSo.add(k.toUpperCase());
                    thamSo.add(k);
                    thamSo.add(mau);
                    break;
            }
        }
        sql.append(" ORDER BY hd.NgayLap DESC, hd.MaHD DESC");

        List<HoaDon> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setObject(i + 1, thamSo.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    /** Lấy đầy đủ một hóa đơn để xem/in: thông tin bệnh nhân, phiếu khám, phòng và các dòng dịch vụ. */
    public HoaDon findDayDu(int maHD) throws SQLException {
        HoaDon hd;
        int maBN;
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE hd.MaHD = ?")) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                hd = doc(rs);
                maBN = rs.getInt("MaBN");
            }
            hd.setChiTiet(docChiTiet(conn, maHD));
        }
        hd.setBenhNhan(new BenhNhanDao().findById(maBN));
        if (hd.getChiTiet().isEmpty()) {
            hd.getChiTiet().add(new ChiTietHoaDon("Dịch vụ khám & điều trị", 1, hd.getTamTinh()));
        }
        return hd;
    }

    /** Hóa đơn còn hiệu lực của một phiếu khám (null nếu chưa lập). */
    public HoaDon findByMaPK(int maPK) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT + "WHERE hd.MaPK = ? AND hd.TrangThai <> ?")) {
            ps.setInt(1, maPK);
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    /** Danh sách phiếu khám ĐÃ HOÀN THÀNH và chưa có hóa đơn còn hiệu lực (dùng khi lập hóa đơn). */
    public List<PhieuKham> findPhieuChuaLapHoaDon() throws SQLException {
        String sql = "SELECT pk.MaPK, pk.MaBN, pk.MaPhong, pk.MaLH, pk.NgayKham, pk.SoThuTu, pk.TrangThai, "
                + "pk.GhiChu, bn.HoTen AS TenBN, p.TenPhong "
                + "FROM PhieuKham pk "
                + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
                + "LEFT JOIN PhongKham p ON p.MaPhong = pk.MaPhong "
                + "WHERE pk.TrangThai = ? AND NOT EXISTS ("
                + "  SELECT 1 FROM HoaDon hd WHERE hd.MaPK = pk.MaPK AND hd.TrangThai <> ?"
                + ") ORDER BY pk.NgayKham DESC, pk.SoThuTu DESC";
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PhieuKhamDao.HOAN_THANH);
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuKham pk = new PhieuKham();
                    pk.setMaPK(rs.getInt("MaPK"));
                    pk.setMaBN(rs.getInt("MaBN"));
                    int maPhong = rs.getInt("MaPhong");
                    pk.setMaPhong(rs.wasNull() ? null : maPhong);
                    int maLH = rs.getInt("MaLH");
                    pk.setMaLH(rs.wasNull() ? null : maLH);
                    Date ngay = rs.getDate("NgayKham");
                    pk.setNgayKham(ngay == null ? null : ngay.toLocalDate());
                    pk.setSoThuTu(rs.getInt("SoThuTu"));
                    pk.setTrangThai(rs.getString("TrangThai"));
                    pk.setGhiChu(PhieuKhamDao.boTheSoLuong(rs.getString("GhiChu")));
                    pk.setTenBenhNhan(rs.getString("TenBN"));
                    pk.setMaBenhNhanHienThi(String.format("BN%03d", pk.getMaBN()));
                    pk.setTenPhong(rs.getString("TenPhong"));
                    ds.add(pk);
                }
            }
        }
        return ds;
    }

    public List<PhieuKham> findPhieuChuaLapHoaDonHomNay() throws SQLException {
        List<PhieuKham> tatCa = findPhieuChuaLapHoaDon();
        List<PhieuKham> homNay = new ArrayList<>();
        for (PhieuKham pk : tatCa) {
            if (LocalDate.now().equals(pk.getNgayKham())) homNay.add(pk);
        }
        return homNay;
    }

    /**
     * Lập hóa đơn mới cho một phiếu khám trong một giao dịch:
     * kiểm tra phiếu chưa có hóa đơn hiệu lực, tính tổng tiền, ghi đầu hóa đơn và chi tiết.
     *
     * @return mã hóa đơn vừa tạo
     */
    public int taoHoaDon(int maPK, Integer maTK, List<ChiTietHoaDon> chiTiet,
                         BigDecimal giamGia, String ghiChu) throws SQLException {
        return taoHoaDon(maPK, maTK, chiTiet, null, giamGia, ghiChu);
    }

    /** Lập hóa đơn mới kèm mã giảm giá đã chọn từ danh mục. */
    public int taoHoaDon(int maPK, Integer maTK, List<ChiTietHoaDon> chiTiet, Integer maGiam,
                         BigDecimal giamGia, String ghiChu) throws SQLException {
        kiemTraChiTiet(chiTiet);
        BigDecimal giam = giamGia == null ? BigDecimal.ZERO : giamGia;
        if (giam.signum() < 0) {
            throw new IllegalArgumentException("Giảm giá không được âm.");
        }
        BigDecimal tongDichVu = tinhTong(chiTiet);
        if (giam.compareTo(tongDichVu) > 0) {
            throw new IllegalArgumentException("Giảm giá không được lớn hơn tổng tiền dịch vụ.");
        }
        BigDecimal tongTien = tongDichVu.subtract(giam);

        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraPhieuVaHoaDon(conn, maPK);
                int maHD = insertHoaDon(conn, maPK, maTK, tongTien, maGiam, giam, ghiChu);
                insertChiTiet(conn, maHD, chiTiet);
                conn.commit();
                return maHD;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Sửa hóa đơn chưa thanh toán: thay toàn bộ dòng chi tiết và tính lại tổng tiền. */
    public void capNhatHoaDon(int maHD, List<ChiTietHoaDon> chiTiet, BigDecimal giamGia, String ghiChu)
            throws SQLException {
        capNhatHoaDon(maHD, chiTiet, null, giamGia, ghiChu);
    }

    /** Sửa hóa đơn chưa thanh toán, cho phép đổi mã giảm giá đã chọn. */
    public void capNhatHoaDon(int maHD, List<ChiTietHoaDon> chiTiet, Integer maGiam,
                              BigDecimal giamGia, String ghiChu) throws SQLException {
        kiemTraChiTiet(chiTiet);
        BigDecimal giam = giamGia == null ? BigDecimal.ZERO : giamGia;
        if (giam.signum() < 0) {
            throw new IllegalArgumentException("Giảm giá không được âm.");
        }
        BigDecimal tongDichVu = tinhTong(chiTiet);
        if (giam.compareTo(tongDichVu) > 0) {
            throw new IllegalArgumentException("Giảm giá không được lớn hơn tổng tiền dịch vụ.");
        }
        BigDecimal tongTien = tongDichVu.subtract(giam);

        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                HoaDon hd = layVoiKhoa(conn, maHD);
                if (hd == null) throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
                if (!hd.chuaThanhToan()) {
                    throw new IllegalArgumentException("Chỉ sửa được hóa đơn chưa thanh toán.");
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoaDon SET TongTien = ?, GiamGia = ?, MaGiam = ?, GhiChu = ? WHERE MaHD = ?")) {
                    ps.setBigDecimal(1, tongTien);
                    ps.setBigDecimal(2, giam);
                    if (maGiam == null || maGiam <= 0) ps.setNull(3, Types.INTEGER);
                    else ps.setInt(3, maGiam);
                    ps.setString(4, rong(ghiChu));
                    ps.setInt(5, maHD);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM ChiTietHoaDon WHERE MaHD = ?")) {
                    ps.setInt(1, maHD);
                    ps.executeUpdate();
                }
                insertChiTiet(conn, maHD, chiTiet);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Xác nhận thanh toán; tính tiền thừa và lưu phương thức / số tiền khách đưa. */
    public void thanhToan(int maHD, String phuongThuc, BigDecimal tienKhachDua, String ghiChu)
            throws SQLException {
        if (phuongThuc == null || phuongThuc.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        }
        boolean hopLe = false;
        for (String pt : HoaDon.PHUONG_THUC) {
            if (pt.equals(phuongThuc)) hopLe = true;
        }
        if (!hopLe) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }

        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                HoaDon hd = layVoiKhoa(conn, maHD);
                if (hd == null) throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
                if (!hd.chuaThanhToan()) {
                    throw new IllegalArgumentException("Hóa đơn này không còn ở trạng thái chưa thanh toán.");
                }
                BigDecimal tong = hd.getTongTien();
                BigDecimal khachDua = tienKhachDua == null ? tong : tienKhachDua;
                if (khachDua.signum() < 0) {
                    throw new IllegalArgumentException("Tiền khách đưa không được âm.");
                }
                if (khachDua.compareTo(tong) < 0) {
                    throw new IllegalArgumentException("Tiền khách đưa chưa đủ. Cần tối thiểu "
                            + dinhDang(tong) + " đ.");
                }
                BigDecimal thua = khachDua.subtract(tong);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoaDon SET TrangThai = ?, PhuongThucTT = ?, NgayThanhToan = SYSDATETIME(), "
                                + "TienKhachDua = ?, TienThua = ?, GhiChu = COALESCE(?, GhiChu) WHERE MaHD = ?")) {
                    ps.setString(1, DA_THANH_TOAN);
                    ps.setString(2, phuongThuc);
                    ps.setBigDecimal(3, khachDua);
                    ps.setBigDecimal(4, thua);
                    ps.setString(5, rong(ghiChu));
                    ps.setInt(6, maHD);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Hủy hóa đơn chưa thanh toán kèm lý do. */
    public void huy(int maHD, String lyDo) throws SQLException {
        if (lyDo == null || lyDo.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do hủy hóa đơn.");
        }
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                HoaDon hd = layVoiKhoa(conn, maHD);
                if (hd == null) throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
                if (!hd.chuaThanhToan()) {
                    throw new IllegalArgumentException("Chỉ hủy được hóa đơn chưa thanh toán.");
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE HoaDon SET TrangThai = ?, LyDoHuy = ? WHERE MaHD = ?")) {
                    ps.setString(1, DA_HUY);
                    ps.setString(2, lyDo.trim());
                    ps.setInt(3, maHD);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<ChiTietHoaDon> findChiTiet(int maHD) throws SQLException {
        try (Connection conn = KetNoi.getConnection()) {
            return docChiTiet(conn, maHD);
        }
    }

    // ===================== Hỗ trợ giao dịch =====================

    private void kiemTraPhieuVaHoaDon(Connection conn, int maPK) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TrangThai FROM PhieuKham WITH (UPDLOCK, HOLDLOCK) WHERE MaPK = ?")) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
                String trangThai = rs.getString(1);
                if (PhieuKhamDao.DA_HUY.equals(trangThai)) {
                    throw new IllegalArgumentException("Phiếu khám đã hủy, không thể lập hóa đơn.");
                }
                if (!PhieuKhamDao.HOAN_THANH.equals(trangThai)) {
                    throw new IllegalArgumentException("Chỉ lập hóa đơn khi bệnh nhân đã hoàn thành khám "
                            + "(phiếu đang ở trạng thái \"" + trangThai + "\").");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM HoaDon WITH (UPDLOCK, HOLDLOCK) WHERE MaPK = ? AND TrangThai <> ?")) {
            ps.setInt(1, maPK);
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Phiếu khám này đã có hóa đơn còn hiệu lực.");
                }
            }
        }
    }

    private int insertHoaDon(Connection conn, int maPK, Integer maTK, BigDecimal tongTien,
                             Integer maGiam, BigDecimal giamGia, String ghiChu) throws SQLException {
        String sql = "INSERT INTO HoaDon (MaPK, MaTK, TongTien, GiamGia, MaGiam, TrangThai, GhiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maPK);
            if (maTK == null || maTK <= 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, maTK);
            ps.setBigDecimal(3, tongTien);
            ps.setBigDecimal(4, giamGia);
            if (maGiam == null || maGiam <= 0) ps.setNull(5, Types.INTEGER);
            else ps.setInt(5, maGiam);
            ps.setString(6, CHUA_THANH_TOAN);
            ps.setString(7, rong(ghiChu));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private void insertChiTiet(Connection conn, int maHD, List<ChiTietHoaDon> chiTiet) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (MaHD, MaDV, TenDichVu, SoLuong, DonGia) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ChiTietHoaDon ct : chiTiet) {
                ps.setInt(1, maHD);
                if (ct.getMaDV() == null) ps.setNull(2, Types.INTEGER);
                else ps.setInt(2, ct.getMaDV());
                ps.setString(3, ct.getTenDichVu().trim());
                ps.setInt(4, ct.getSoLuong());
                ps.setBigDecimal(5, ct.getDonGia());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private HoaDon layVoiKhoa(Connection conn, int maHD) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT_LOCK + "WHERE hd.MaHD = ?")) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private List<ChiTietHoaDon> docChiTiet(Connection conn, int maHD) throws SQLException {
        String sql = "SELECT MaCT, MaHD, MaDV, TenDichVu, SoLuong, DonGia "
                + "FROM ChiTietHoaDon WHERE MaHD = ? ORDER BY MaCT";
        List<ChiTietHoaDon> ds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    ct.setMaCT(rs.getInt("MaCT"));
                    ct.setMaHD(rs.getInt("MaHD"));
                    int maDV = rs.getInt("MaDV");
                    ct.setMaDV(rs.wasNull() ? null : maDV);
                    ct.setTenDichVu(rs.getString("TenDichVu"));
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGia(rs.getBigDecimal("DonGia"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    private void kiemTraChiTiet(List<ChiTietHoaDon> chiTiet) {
        if (chiTiet == null || chiTiet.isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một dịch vụ.");
        }
        for (ChiTietHoaDon ct : chiTiet) {
            if (ct.getTenDichVu() == null || ct.getTenDichVu().isBlank()) {
                throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
            }
            if (ct.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0.");
            }
            if (ct.getDonGia() == null || ct.getDonGia().signum() < 0) {
                throw new IllegalArgumentException("Đơn giá dịch vụ không hợp lệ.");
            }
        }
    }

    private static BigDecimal tinhTong(List<ChiTietHoaDon> chiTiet) {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : chiTiet) {
            tong = tong.add(ct.getThanhTien());
        }
        return tong;
    }

    private static String dinhDang(BigDecimal tien) {
        return String.format("%,.0f", tien);
    }

    private static String rong(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private HoaDon doc(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getInt("MaHD"));
        hd.setMaPK(rs.getInt("MaPK"));
        int maTK = rs.getInt("MaTK");
        hd.setMaTK(rs.wasNull() ? null : maTK);
        hd.setTenNguoiLap(rs.getString("TenNguoiLap"));
        hd.setNgayLap(rs.getTimestamp("NgayLap").toLocalDateTime());
        hd.setTongTien(rs.getBigDecimal("TongTien"));
        BigDecimal giamGia = rs.getBigDecimal("GiamGia");
        hd.setGiamGia(giamGia == null ? BigDecimal.ZERO : giamGia);
        int maGiam = rs.getInt("MaGiam");
        hd.setMaGiam(rs.wasNull() ? null : maGiam);
        hd.setMaGiamCode(rs.getString("MaGiamCode"));
        hd.setPhuongThucTT(rs.getString("PhuongThucTT"));
        Timestamp tt = rs.getTimestamp("NgayThanhToan");
        hd.setNgayThanhToan(tt == null ? null : tt.toLocalDateTime());
        hd.setTienKhachDua(rs.getBigDecimal("TienKhachDua"));
        hd.setTienThua(rs.getBigDecimal("TienThua"));
        hd.setTrangThai(rs.getString("TrangThai"));
        hd.setGhiChu(rs.getString("GhiChu"));
        hd.setLyDoHuy(rs.getString("LyDoHuy"));
        hd.setTenBenhNhan(rs.getString("TenBN"));
        int maBN = rs.getInt("MaBN");
        hd.setMaBenhNhanHienThi(String.format("BN%03d", maBN));
        Date ngay = rs.getDate("NgayKham");
        hd.setNgayKham(ngay == null ? null : ngay.toLocalDate());
        hd.setSoThuTu(rs.getInt("SoThuTu"));
        hd.setTenPhong(rs.getString("TenPhong"));
        return hd;
    }
}
