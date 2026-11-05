package Dao;

import ConnectDB.KetNoi;
import Entity.BacSi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BacSiDao {

    public List<BacSi> findAll() throws SQLException {
        String sql = "SELECT MaBS, HoTen, ChuyenKhoa FROM BacSi ORDER BY HoTen";
        List<BacSi> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new BacSi(rs.getInt("MaBS"), rs.getString("HoTen"), rs.getString("ChuyenKhoa")));
            }
        }
        return ds;
    }
}
