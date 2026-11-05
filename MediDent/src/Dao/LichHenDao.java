package Dao;

import ConnectDB.KetNoi;
import Entity.LichHen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LichHenDao {
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_DEN = "Đã đến";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";
    public static final String[] TRANG_THAI = {CHO_XAC_NHAN, DA_XAC_NHAN, DA_DEN, HOAN_THANH, DA_HUY};

    public static final String DOI_LICH = "Đổi lịch";
    public static final String HUY_LICH = "Hủy lịch";

    private static final String SELECT = "SELECT lh.MaLH, lh.MaBN, lh.MaBS, lh.NgayGio, lh.TrangThai, lh.YeuCau, "
            + "lh.NgayGioMoi, lh.GhiChu, bn.HoTen AS TenBN, bs.HoTen AS TenBS "
            + "FROM LichHen lh JOIN BenhNhan bn ON bn.MaBN = lh.MaBN JOIN BacSi bs ON bs.MaBS = lh.MaBS ";

    public List<LichHen> findAll() throws SQLException {
        List<LichHen> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "ORDER BY lh.NgayGio DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(doc(rs));
            }
        }
        return ds;
    }


    public int tao(int maBN, int maBS, LocalDateTime ngayGio, String ghiChu) throws SQLException {
        if (ngayGio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải ở tương lai.");
        }
        if (demTrung(maBS, ngayGio, DA_HUY, 0) > 0) {
            throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn vào thời gian này.");
        }
        LichHen lh = new LichHen();
        lh.setMaBN(maBN);
        lh.setMaBS(maBS);
        lh.setNgayGio(ngayGio);
        lh.setTrangThai(CHO_XAC_NHAN);
        lh.setGhiChu(ghiChu == null || ghiChu.trim().isEmpty() ? null : ghiChu.trim());
        return insert(lh);
    }

    public void capNhatTrangThai(int maLH, String trangThai) throws SQLException {
        if (!Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ.");
        }
        LichHen lh = layLichHen(maLH);
        if (DA_HUY.equals(lh.getTrangThai()) || HOAN_THANH.equals(lh.getTrangThai())) {
            throw new IllegalArgumentException("Lịch hẹn đã kết thúc, không thể đổi trạng thái.");
        }
        lh.setTrangThai(trangThai);
        update(lh);
    }

    public void xuLyYeuCau(int maLH, boolean chapNhan) throws SQLException {
        LichHen lh = layLichHen(maLH);
        if (lh.getYeuCau() == null) {
            throw new IllegalArgumentException("Lịch hẹn này không có yêu cầu đổi/hủy.");
        }
        if (chapNhan) {
            if (HUY_LICH.equals(lh.getYeuCau())) {
                lh.setTrangThai(DA_HUY);
            } else if (DOI_LICH.equals(lh.getYeuCau())) {
                LocalDateTime moi = lh.getNgayGioMoi();
                if (moi == null || moi.isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Thời gian mới không hợp lệ.");
                }
                if (demTrung(lh.getMaBS(), moi, DA_HUY, lh.getMaLH()) > 0) {
                    throw new IllegalArgumentException("Bác sĩ đã có lịch hẹn vào thời gian mới.");
                }
                lh.setNgayGio(moi);
                lh.setTrangThai(DA_XAC_NHAN);
            }
        }
        lh.setYeuCau(null);
        lh.setNgayGioMoi(null);
        update(lh);
    }

    private LichHen layLichHen(int maLH) throws SQLException {
        LichHen lh = findById(maLH);
        if (lh == null) {
            throw new IllegalArgumentException("Không tìm thấy lịch hẹn.");
        }
        return lh;
    }

    private LichHen findById(int maLH) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE lh.MaLH = ?")) {
            ps.setInt(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private int insert(LichHen lh) throws SQLException {
        String sql = "INSERT INTO LichHen (MaBN, MaBS, NgayGio, TrangThai, YeuCau, NgayGioMoi, GhiChu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lh.getMaBN());
            ps.setInt(2, lh.getMaBS());
            ps.setTimestamp(3, Timestamp.valueOf(lh.getNgayGio()));
            ps.setString(4, lh.getTrangThai());
            ganGiaTriYeuCau(ps, 5, lh);
            ps.setString(7, lh.getGhiChu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int update(LichHen lh) throws SQLException {
        String sql = "UPDATE LichHen SET NgayGio = ?, TrangThai = ?, YeuCau = ?, NgayGioMoi = ?, GhiChu = ? WHERE MaLH = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(lh.getNgayGio()));
            ps.setString(2, lh.getTrangThai());
            ganGiaTriYeuCau(ps, 3, lh);
            ps.setString(5, lh.getGhiChu());
            ps.setInt(6, lh.getMaLH());
            return ps.executeUpdate();
        }
    }

    private int demTrung(int maBS, LocalDateTime ngayGio, String trangThaiLoaiTru, int maLHLoaiTru) throws SQLException {
        String sql = "SELECT COUNT(*) FROM LichHen WHERE MaBS = ? AND NgayGio = ? AND TrangThai <> ? AND MaLH <> ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBS);
            ps.setTimestamp(2, Timestamp.valueOf(ngayGio));
            ps.setString(3, trangThaiLoaiTru);
            ps.setInt(4, maLHLoaiTru);
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
        lh.setTenBenhNhan(rs.getString("TenBN"));
        lh.setTenBacSi(rs.getString("TenBS"));
        return lh;
    }
}
