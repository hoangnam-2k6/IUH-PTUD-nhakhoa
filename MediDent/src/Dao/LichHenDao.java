package Dao;

import ConnectDB.KetNoi;
import Entity.LichHen;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LichHenDao {
    public static final String CHO_XAC_NHAN = LichHen.CHO_XAC_NHAN;
    public static final String DA_XAC_NHAN = LichHen.DA_XAC_NHAN;
    public static final String DA_DEN = LichHen.DA_DEN;
    public static final String HOAN_THANH = LichHen.HOAN_THANH;
    public static final String DA_HUY = LichHen.DA_HUY;
    public static final String[] TRANG_THAI = {CHO_XAC_NHAN, DA_XAC_NHAN, DA_DEN, HOAN_THANH, DA_HUY};

    public static final String DOI_LICH = LichHen.DOI_LICH;
    public static final String HUY_LICH = LichHen.HUY_LICH;

    /** Khoảng cách tối thiểu (phút) giữa hai lịch hẹn của cùng bác sĩ. */
    private static final int KHOANG_CACH_PHUT = 30;

    private static final String SELECT = "SELECT lh.MaLH, lh.MaBN, lh.MaBS, lh.NgayGio, lh.TrangThai, lh.YeuCau, "
            + "lh.NgayGioMoi, lh.GhiChu, lh.MaTK, bn.HoTen AS TenBN, bs.HoTen AS TenBS "
            + "FROM LichHen lh "
            + "JOIN BenhNhan bn ON bn.MaBN = lh.MaBN "
            + "JOIN BacSi bs ON bs.MaBS = lh.MaBS ";

    public List<LichHen> findAll() throws SQLException {
        return truyVan(SELECT + "ORDER BY lh.NgayGio DESC");
    }

    public List<LichHen> findSapToi() throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        String sql = SELECT + "WHERE lh.NgayGio >= ? AND lh.TrangThai NOT IN (?, ?) ORDER BY lh.NgayGio";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().withSecond(0).withNano(0)));
            ps.setString(2, DA_HUY);
            ps.setString(3, HOAN_THANH);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public List<LichHen> findTheoNgay(LocalDate ngay) throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        String sql = SELECT + "WHERE CAST(lh.NgayGio AS DATE) = ? ORDER BY lh.NgayGio";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public List<LichHen> findByBenhNhan(int maBN) throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT + "WHERE lh.MaBN = ? ORDER BY lh.NgayGio DESC")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public List<LichHen> findCoYeuCau() throws SQLException {
        return truyVan(SELECT + "WHERE lh.YeuCau IS NOT NULL ORDER BY lh.NgayGio");
    }

    public List<LichHen> findTheoTrangThai(String trangThai) throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT + "WHERE lh.TrangThai = ? ORDER BY lh.NgayGio")) {
            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public LichHen findById(int maLH) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE lh.MaLH = ?")) {
            ps.setInt(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int tao(int maBN, int maBS, LocalDateTime ngayGio, String ghiChu) throws SQLException {
        return tao(maBN, maBS, ngayGio, ghiChu, null);
    }

    public int tao(int maBN, int maBS, LocalDateTime ngayGio, String ghiChu, Integer maTK) throws SQLException {
        if (ngayGio == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giờ hẹn.");
        }
        if (ngayGio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai.");
        }
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (demTrung(conn, maBS, ngayGio, 0) > 0) {
                    throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn trong khoảng "
                            + KHOANG_CACH_PHUT + " phút quanh thời điểm này.");
                }
                LichHen lh = new LichHen();
                lh.setMaBN(maBN);
                lh.setMaBS(maBS);
                lh.setNgayGio(ngayGio);
                lh.setTrangThai(CHO_XAC_NHAN);
                lh.setGhiChu(rong(ghiChu));
                lh.setMaTK(maTK);
                int maLH = insert(conn, lh);
                conn.commit();
                return maLH;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Sửa thông tin lịch hẹn (chỉ lịch chưa kết thúc). */
    public void capNhat(int maLH, int maBN, int maBS, LocalDateTime ngayGio, String ghiChu) throws SQLException {
        if (ngayGio == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày giờ hẹn.");
        }
        if (ngayGio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai.");
        }
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LichHen hienTai = layVoiKhoa(conn, maLH);
                if (hienTai == null) throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
                if (hienTai.daKetThuc()) {
                    throw new IllegalArgumentException("Lịch hẹn đã kết thúc, không thể sửa.");
                }
                if (demTrung(conn, maBS, ngayGio, maLH) > 0) {
                    throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn trong khoảng "
                            + KHOANG_CACH_PHUT + " phút quanh thời điểm này.");
                }
                String sql = "UPDATE LichHen SET MaBN = ?, MaBS = ?, NgayGio = ?, GhiChu = ? WHERE MaLH = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, maBN);
                    ps.setInt(2, maBS);
                    ps.setTimestamp(3, Timestamp.valueOf(ngayGio));
                    ps.setString(4, rong(ghiChu));
                    ps.setInt(5, maLH);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void xacNhan(int maLH) throws SQLException {
        LichHen lh = layLichHen(maLH);
        if (!CHO_XAC_NHAN.equals(lh.getTrangThai())) {
            throw new IllegalArgumentException("Chỉ xác nhận được lịch đang ở trạng thái \"Chờ xác nhận\".");
        }
        lh.setTrangThai(DA_XAC_NHAN);
        update(lh);
    }

    public void capNhatTrangThai(int maLH, String trangThai) throws SQLException {
        if (!Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ.");
        }
        LichHen lh = layLichHen(maLH);
        if (lh.daKetThuc()) {
            throw new IllegalArgumentException("Lịch hẹn đã kết thúc, không thể đổi trạng thái.");
        }
        lh.setTrangThai(trangThai);
        if (DA_HUY.equals(trangThai)) {
            lh.setYeuCau(null);
            lh.setNgayGioMoi(null);
        }
        update(lh);
    }

    /** Hủy lịch hẹn kèm lý do (lý do được nối vào ghi chú). */
    public void huy(int maLH, String lyDo) throws SQLException {
        LichHen lh = layLichHen(maLH);
        if (lh.daKetThuc()) {
            throw new IllegalArgumentException("Lịch hẹn đã kết thúc.");
        }
        lh.setTrangThai(DA_HUY);
        lh.setYeuCau(null);
        lh.setNgayGioMoi(null);
        String ghiChu = lh.getGhiChu();
        if (lyDo != null && !lyDo.isBlank()) {
            ghiChu = (ghiChu == null || ghiChu.isBlank() ? "" : ghiChu + " | ") + "Hủy: " + lyDo.trim();
        }
        lh.setGhiChu(rong(ghiChu));
        update(lh);
    }

    /** Bệnh nhân gửi yêu cầu đổi lịch; lịch gốc giữ nguyên đến khi lễ tân duyệt. */
    public void guiYeuCauDoiLich(int maLH, LocalDateTime ngayGioMoi, String ghiChu) throws SQLException {
        if (ngayGioMoi == null || ngayGioMoi.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian mới phải ở tương lai.");
        }
        LichHen lh = layLichHen(maLH);
        if (lh.daKetThuc()) {
            throw new IllegalArgumentException("Lịch hẹn đã kết thúc.");
        }
        lh.setYeuCau(DOI_LICH);
        lh.setNgayGioMoi(ngayGioMoi);
        if (ghiChu != null && !ghiChu.isBlank()) {
            lh.setGhiChu(rong((lh.getGhiChu() == null ? "" : lh.getGhiChu() + " | ") + ghiChu.trim()));
        }
        update(lh);
    }

    /** Bệnh nhân gửi yêu cầu hủy lịch; lễ tân xử lý sau. */
    public void guiYeuCauHuy(int maLH, String ghiChu) throws SQLException {
        LichHen lh = layLichHen(maLH);
        if (lh.daKetThuc()) {
            throw new IllegalArgumentException("Lịch hẹn đã kết thúc.");
        }
        lh.setYeuCau(HUY_LICH);
        lh.setNgayGioMoi(null);
        if (ghiChu != null && !ghiChu.isBlank()) {
            lh.setGhiChu(rong((lh.getGhiChu() == null ? "" : lh.getGhiChu() + " | ") + ghiChu.trim()));
        }
        update(lh);
    }

    /**
     * Duyệt / từ chối yêu cầu đổi hoặc hủy lịch.
     * Chấp nhận hủy -> lịch chuyển "Đã hủy".
     * Chấp nhận đổi -> cập nhật sang giờ mới và "Đã xác nhận".
     * Từ chối -> chỉ xóa yêu cầu, giữ nguyên lịch.
     */
    public void xuLyYeuCau(int maLH, boolean chapNhan) throws SQLException {
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                LichHen lh = layVoiKhoa(conn, maLH);
                if (lh == null) throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
                if (!lh.coYeuCau()) {
                    throw new IllegalArgumentException("Lịch hẹn này không có yêu cầu đổi/hủy.");
                }
                if (chapNhan) {
                    if (HUY_LICH.equals(lh.getYeuCau())) {
                        lh.setTrangThai(DA_HUY);
                    } else if (DOI_LICH.equals(lh.getYeuCau())) {
                        LocalDateTime moi = lh.getNgayGioMoi();
                        if (moi == null || moi.isBefore(LocalDateTime.now())) {
                            throw new IllegalArgumentException("Thời gian mới của lịch hẹn không hợp lệ.");
                        }
                        if (demTrung(conn, lh.getMaBS(), moi, lh.getMaLH()) > 0) {
                            throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn trong khoảng "
                                    + KHOANG_CACH_PHUT + " phút quanh thời gian mới.");
                        }
                        lh.setNgayGio(moi);
                        lh.setTrangThai(DA_XAC_NHAN);
                    }
                }
                lh.setYeuCau(null);
                lh.setNgayGioMoi(null);
                update(conn, lh);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Đếm số lịch hẹn chưa kết thúc của một bác sĩ trong ngày. */
    public int demTheoBacSiNgay(int maBS, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE MaBS = ? AND CAST(NgayGio AS DATE) = ? "
                + "AND TrangThai NOT IN (?, ?)";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBS);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, DA_HUY);
            ps.setString(4, HOAN_THANH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ===================== Hỗ trợ =====================

    private List<LichHen> truyVan(String sql) throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        }
        return ds;
    }

    private LichHen layLichHen(int maLH) throws SQLException {
        LichHen lh = findById(maLH);
        if (lh == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
        }
        return lh;
    }

    private LichHen layVoiKhoa(Connection conn, int maLH) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                SELECT + "WHERE lh.MaLH = ?")) {
            ps.setInt(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private int insert(Connection conn, LichHen lh) throws SQLException {
        String sql = "INSERT INTO LichHen (MaBN, MaBS, NgayGio, TrangThai, YeuCau, NgayGioMoi, GhiChu, MaTK) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lh.getMaBN());
            ps.setInt(2, lh.getMaBS());
            ps.setTimestamp(3, Timestamp.valueOf(lh.getNgayGio()));
            ps.setString(4, lh.getTrangThai());
            ganGiaTriYeuCau(ps, 5, lh);
            ps.setString(7, lh.getGhiChu());
            if (lh.getMaTK() == null) ps.setNull(8, Types.INTEGER);
            else ps.setInt(8, lh.getMaTK());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int update(LichHen lh) throws SQLException {
        try (Connection conn = KetNoi.getConnection()) {
            return update(conn, lh);
        }
    }

    private int update(Connection conn, LichHen lh) throws SQLException {
        String sql = "UPDATE LichHen SET MaBN = ?, MaBS = ?, NgayGio = ?, TrangThai = ?, "
                + "YeuCau = ?, NgayGioMoi = ?, GhiChu = ? WHERE MaLH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lh.getMaBN());
            ps.setInt(2, lh.getMaBS());
            ps.setTimestamp(3, Timestamp.valueOf(lh.getNgayGio()));
            ps.setString(4, lh.getTrangThai());
            ganGiaTriYeuCau(ps, 5, lh);
            ps.setString(7, lh.getGhiChu());
            ps.setInt(8, lh.getMaLH());
            return ps.executeUpdate();
        }
    }

    private int demTrung(Connection conn, int maBS, LocalDateTime ngayGio, int maLHLoaiTru) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaBS = ? AND ABS(DATEDIFF(MINUTE, NgayGio, ?)) < ? "
                + "AND TrangThai NOT IN (?, ?) AND MaLH <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBS);
            ps.setTimestamp(2, Timestamp.valueOf(ngayGio));
            ps.setInt(3, KHOANG_CACH_PHUT);
            ps.setString(4, DA_HUY);
            ps.setString(5, HOAN_THANH);
            ps.setInt(6, maLHLoaiTru);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void ganGiaTriYeuCau(PreparedStatement ps, int viTri, LichHen lh) throws SQLException {
        if (lh.getYeuCau() == null) {
            ps.setNull(viTri, Types.NVARCHAR);
        } else {
            ps.setString(viTri, lh.getYeuCau());
        }
        if (lh.getNgayGioMoi() == null) {
            ps.setNull(viTri + 1, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(viTri + 1, Timestamp.valueOf(lh.getNgayGioMoi()));
        }
    }

    private static String rong(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private LichHen doc(ResultSet rs) throws SQLException {
        LichHen lh = new LichHen();
        lh.setMaLH(rs.getInt("MaLH"));
        lh.setMaBN(rs.getInt("MaBN"));
        lh.setMaBS(rs.getInt("MaBS"));
        lh.setNgayGio(rs.getTimestamp("NgayGio").toLocalDateTime());
        lh.setTrangThai(rs.getString("TrangThai"));
        lh.setYeuCau(rs.getString("YeuCau"));
        Timestamp moi = rs.getTimestamp("NgayGioMoi");
        lh.setNgayGioMoi(moi == null ? null : moi.toLocalDateTime());
        lh.setGhiChu(rs.getString("GhiChu"));
        int maTK = rs.getInt("MaTK");
        lh.setMaTK(rs.wasNull() ? null : maTK);
        lh.setTenBenhNhan(rs.getString("TenBN"));
        lh.setMaBenhNhanHienThi(String.format("BN%03d", lh.getMaBN()));
        lh.setTenBacSi(rs.getString("TenBS"));
        return lh;
    }
}
