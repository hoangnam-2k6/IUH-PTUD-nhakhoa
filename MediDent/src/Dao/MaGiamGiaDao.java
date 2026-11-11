package Dao;

import ConnectDB.KetNoi;
import Entity.MaGiamGia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO danh mục mã giảm giá do quản lý tạo sẵn. */
public class MaGiamGiaDao {
    private static final String COT = "MaGiam, Ma, Ten, LoaiGiam, GiaTri, TrangThai";

    public List<MaGiamGia> findAll() throws SQLException {
        return tim(false);
    }

    public List<MaGiamGia> findDangHoatDong() throws SQLException {
        return tim(true);
    }

    private List<MaGiamGia> tim(boolean chiHoatDong) throws SQLException {
        String sql = "SELECT " + COT + " FROM MaGiamGia"
                + (chiHoatDong ? " WHERE TrangThai = N'Hoạt động'" : "")
                + " ORDER BY Ma";
        List<MaGiamGia> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        }
        return ds;
    }

    public MaGiamGia findById(int maGiam) throws SQLException {
        String sql = "SELECT " + COT + " FROM MaGiamGia WHERE MaGiam = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiam);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private MaGiamGia doc(ResultSet rs) throws SQLException {
        MaGiamGia m = new MaGiamGia();
        m.setMaGiam(rs.getInt("MaGiam"));
        m.setMa(rs.getString("Ma"));
        m.setTen(rs.getString("Ten"));
        m.setLoaiGiam(rs.getString("LoaiGiam"));
        m.setGiaTri(rs.getBigDecimal("GiaTri"));
        m.setTrangThai(rs.getString("TrangThai"));
        return m;
    }
}
