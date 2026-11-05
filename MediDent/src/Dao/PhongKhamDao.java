package Dao;

import ConnectDB.KetNoi;
import Entity.PhongKham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhongKhamDao {

    public List<PhongKham> findAll() throws SQLException {
        String sql = "SELECT MaPhong, TenPhong FROM PhongKham ORDER BY TenPhong";
        List<PhongKham> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new PhongKham(rs.getInt("MaPhong"), rs.getString("TenPhong")));
            }
        }
        return ds;
    }
}
