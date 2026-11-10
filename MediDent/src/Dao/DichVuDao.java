package Dao;

import ConnectDB.KetNoi;
import Entity.DichVu;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO bảng giá dịch vụ nha khoa. */
public class DichVuDao {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private static final String COT = "MaDV, TenDichVu, DonGia, TrangThai";

    public List<DichVu> findAll() throws SQLException {
        return tim(null, false);
    }

    public List<DichVu> findDangHoatDong() throws SQLException {
        return tim(null, true);
    }

    public List<DichVu> tim(String tuKhoa, boolean chiHoatDong) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COT + " FROM DichVu WHERE 1 = 1");
        if (chiHoatDong) sql.append(" AND TrangThai = N'Hoạt động'");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" AND TenDichVu COLLATE Vietnamese_CI_AI LIKE ?");
        }
        sql.append(" ORDER BY TenDichVu");
        List<DichVu> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (tuKhoa != null && !tuKhoa.isBlank()) {
                ps.setString(1, "%" + tuKhoa.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public DichVu findById(int maDV) throws SQLException {
        String sql = "SELECT " + COT + " FROM DichVu WHERE MaDV = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int them(DichVu dv) throws SQLException {
        kiemTra(dv);
        String sql = "INSERT INTO DichVu (TenDichVu, DonGia, TrangThai) VALUES (?, ?, ?)";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dv.getTenDichVu().trim());
            ps.setBigDecimal(2, dv.getDonGia());
            ps.setString(3, dv.getTrangThai() == null ? HOAT_DONG : dv.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void capNhat(DichVu dv) throws SQLException {
        kiemTra(dv);
        String sql = "UPDATE DichVu SET TenDichVu = ?, DonGia = ?, TrangThai = ? WHERE MaDV = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDichVu().trim());
            ps.setBigDecimal(2, dv.getDonGia());
            ps.setString(3, dv.getTrangThai() == null ? HOAT_DONG : dv.getTrangThai());
            ps.setInt(4, dv.getMaDV());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ cần cập nhật.");
            }
        }
    }

    public void doiTrangThai(int maDV, String trangThai) throws SQLException {
        if (!HOAT_DONG.equals(trangThai) && !NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái dịch vụ không hợp lệ.");
        }
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE DichVu SET TrangThai = ? WHERE MaDV = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maDV);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
            }
        }
    }

    private void kiemTra(DichVu dv) {
        if (dv.getTenDichVu() == null || dv.getTenDichVu().isBlank()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (dv.getDonGia() == null || dv.getDonGia().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ phải lớn hơn hoặc bằng 0.");
        }
    }

    private DichVu doc(ResultSet rs) throws SQLException {
        DichVu dv = new DichVu();
        dv.setMaDV(rs.getInt("MaDV"));
        dv.setTenDichVu(rs.getString("TenDichVu"));
        dv.setDonGia(rs.getBigDecimal("DonGia"));
        dv.setTrangThai(rs.getString("TrangThai"));
        return dv;
    }
}
