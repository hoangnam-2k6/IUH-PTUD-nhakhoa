package DAO;

import ConnectDB.ConnectDB;
import Entity.BacSi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BacSiDao {
    public static final String HOAT_DONG = BacSi.HOAT_DONG;
    public static final String NGUNG_HOAT_DONG = BacSi.NGUNG_HOAT_DONG;

    private static final String COT = "MaBS, HoTen, ChuyenKhoa, TrangThai";

    public List<BacSi> findAll() throws SQLException {
        return tim(null, false);
    }

    public List<BacSi> findDangHoatDong() throws SQLException {
        return tim(null, true);
    }

    public List<BacSi> tim(String tuKhoa, boolean chiHoatDong) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COT + " FROM BacSi WHERE 1 = 1");
        if (chiHoatDong) sql.append(" AND TrangThai = N'Hoạt động'");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" AND (HoTen COLLATE Vietnamese_CI_AI LIKE ? OR ChuyenKhoa COLLATE Vietnamese_CI_AI LIKE ?)");
        }
        sql.append(" ORDER BY HoTen");
        List<BacSi> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (tuKhoa != null && !tuKhoa.isBlank()) {
                String mau = "%" + tuKhoa.trim() + "%";
                ps.setString(1, mau);
                ps.setString(2, mau);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public BacSi findById(int maBS) throws SQLException {
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT " + COT + " FROM BacSi WHERE MaBS = ?")) {
            ps.setInt(1, maBS);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int them(BacSi bs) throws SQLException {
        kiemTra(bs);
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO BacSi (HoTen, ChuyenKhoa, TrangThai) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bs.getHoTen().trim());
            ps.setString(2, rong(bs.getChuyenKhoa()));
            ps.setString(3, bs.getTrangThai() == null ? HOAT_DONG : bs.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void capNhat(BacSi bs) throws SQLException {
        kiemTra(bs);
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE BacSi SET HoTen = ?, ChuyenKhoa = ?, TrangThai = ? WHERE MaBS = ?")) {
            ps.setString(1, bs.getHoTen().trim());
            ps.setString(2, rong(bs.getChuyenKhoa()));
            ps.setString(3, bs.getTrangThai() == null ? HOAT_DONG : bs.getTrangThai());
            ps.setInt(4, bs.getMaBS());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy bác sĩ.");
            }
        }
    }

    public void doiTrangThai(int maBS, String trangThai) throws SQLException {
        if (!HOAT_DONG.equals(trangThai) && !NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái bác sĩ không hợp lệ.");
        }
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE BacSi SET TrangThai = ? WHERE MaBS = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maBS);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy bác sĩ.");
            }
        }
    }

    private void kiemTra(BacSi bs) {
        if (bs.getHoTen() == null || bs.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Họ tên bác sĩ không được để trống.");
        }
    }

    private static String rong(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private BacSi doc(ResultSet rs) throws SQLException {
        BacSi bs = new BacSi();
        bs.setMaBS(rs.getInt("MaBS"));
        bs.setHoTen(rs.getString("HoTen"));
        bs.setChuyenKhoa(rs.getString("ChuyenKhoa"));
        bs.setTrangThai(rs.getString("TrangThai"));
        return bs;
    }
}
