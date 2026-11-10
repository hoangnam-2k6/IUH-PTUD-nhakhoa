package DAO;

import ConnectDB.ConnectDB;
import Entity.PhongKham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhongKhamDAO {
    public static final String HOAT_DONG = PhongKham.HOAT_DONG;
    public static final String NGUNG_HOAT_DONG = PhongKham.NGUNG_HOAT_DONG;

    private static final String COT = "MaPhong, TenPhong, SucChua, TrangThai";

    public List<PhongKham> findAll() throws SQLException {
        List<PhongKham> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT " + COT + " FROM PhongKham ORDER BY TenPhong");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        }
        return ds;
    }

    public List<PhongKham> findDangHoatDong() throws SQLException {
        List<PhongKham> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT " + COT + " FROM PhongKham WHERE TrangThai = N'Hoạt động' ORDER BY TenPhong");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        }
        return ds;
    }

    public PhongKham findById(int maPhong) throws SQLException {
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT " + COT + " FROM PhongKham WHERE MaPhong = ?")) {
            ps.setInt(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int them(PhongKham p) throws SQLException {
        kiemTra(p);
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO PhongKham (TenPhong, SucChua, TrangThai) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTenPhong().trim());
            ps.setInt(2, p.getSucChua());
            ps.setString(3, p.getTrangThai() == null ? HOAT_DONG : p.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void capNhat(PhongKham p) throws SQLException {
        kiemTra(p);
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE PhongKham SET TenPhong = ?, SucChua = ?, TrangThai = ? WHERE MaPhong = ?")) {
            ps.setString(1, p.getTenPhong().trim());
            ps.setInt(2, p.getSucChua());
            ps.setString(3, p.getTrangThai() == null ? HOAT_DONG : p.getTrangThai());
            ps.setInt(4, p.getMaPhong());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy phòng khám.");
            }
        }
    }

    public void doiTrangThai(int maPhong, String trangThai) throws SQLException {
        if (!HOAT_DONG.equals(trangThai) && !NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phòng khám không hợp lệ.");
        }
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE PhongKham SET TrangThai = ? WHERE MaPhong = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maPhong);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy phòng khám.");
            }
        }
    }

    /** Số bệnh nhân (chưa hủy) đã nhận trong phòng ở một ngày. */
    public int demBenhNhanTrongPhong(int maPhong, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuKham WHERE MaPhong = ? AND NgayKham = ? AND TrangThai <> ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhong);
            ps.setDate(2, java.sql.Date.valueOf(ngay));
            ps.setString(3, PhieuKhamDAO.DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Số phòng đang hoạt động. */
    public int demDangHoatDong() throws SQLException {
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM PhongKham WHERE TrangThai = N'Hoạt động'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void kiemTra(PhongKham p) {
        if (p.getTenPhong() == null || p.getTenPhong().isBlank()) {
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        }
        if (p.getSucChua() <= 0) {
            throw new IllegalArgumentException("Sức chứa phải lớn hơn 0.");
        }
    }

    private PhongKham doc(ResultSet rs) throws SQLException {
        PhongKham p = new PhongKham();
        p.setMaPhong(rs.getInt("MaPhong"));
        p.setTenPhong(rs.getString("TenPhong"));
        p.setSucChua(rs.getInt("SucChua"));
        p.setTrangThai(rs.getString("TrangThai"));
        return p;
    }
}
