package DAO;

import ConnectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrangChuDAO {

    public record TongQuan(int benhNhan, int lichHen, int caLamViec,
                           int choThanhToan, BigDecimal doanhThu) {}

    public record LichSapToi(int maLich, String benhNhan, String ngay,
                             String gio, String bacSi, String trangThai) {}

    public TongQuan layTongQuan() throws SQLException {
        String sql = "SELECT (SELECT COUNT(*) FROM dbo.BenhNhan) AS benhNhan, "
                + "(SELECT COUNT(*) FROM dbo.LichHen WHERE ngayHen=CAST(GETDATE() AS DATE) AND trangThai<>N'Đã hủy') AS lichHen, "
                + "(SELECT COUNT(*) FROM dbo.LichLamViec WHERE ngayLamViec=CAST(GETDATE() AS DATE) AND trangThai=N'Hoạt động') AS caLamViec, "
                + "(SELECT COUNT(*) FROM dbo.HoaDon WHERE trangThai=N'Chờ thanh toán') AS choThanhToan, "
                + "(SELECT COALESCE(SUM(t.soTien),0) FROM dbo.ThanhToan t "
                + "JOIN dbo.HoaDon h ON h.maTT=t.maTT "
                + "WHERE t.trangThai=N'Thành công' AND h.trangThai=N'Đã thanh toán' "
                + "AND t.ngayThanhToan>=DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1) "
                + "AND t.ngayThanhToan<DATEADD(MONTH,1,DATEFROMPARTS(YEAR(GETDATE()),MONTH(GETDATE()),1))) AS doanhThu";

        try (Connection con = ConnectDB.taoKetNoiMoi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            rs.next();
            return new TongQuan(rs.getInt("benhNhan"), rs.getInt("lichHen"),
                    rs.getInt("caLamViec"), rs.getInt("choThanhToan"),
                    rs.getBigDecimal("doanhThu"));
        }
    }

    public List<LichSapToi> layLichSapToi() throws SQLException {
        String sql = "SELECT TOP 8 l.maLichHen,b.hoten AS benhNhan, "
                + "CONVERT(VARCHAR(10),l.ngayHen,103) AS ngayHen, "
                + "CONVERT(VARCHAR(5),l.gioHen,108) AS gioHen, "
                + "COALESCE(n.hoten,N'Chưa phân công') AS bacSi,l.trangThai "
                + "FROM dbo.LichHen l JOIN dbo.BenhNhan b ON b.maBN=l.maBN "
                + "LEFT JOIN dbo.NhanVien n ON n.maNV=l.maNV "
                + "WHERE l.ngayHen>=CAST(GETDATE() AS DATE) AND l.trangThai<>N'Đã hủy' "
                + "ORDER BY l.ngayHen,l.gioHen";

        List<LichSapToi> ds = new ArrayList<>();

        try (Connection con = ConnectDB.taoKetNoiMoi();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(new LichSapToi(rs.getInt("maLichHen"),
                        rs.getString("benhNhan"), rs.getString("ngayHen"),
                        rs.getString("gioHen"), rs.getString("bacSi"),
                        rs.getString("trangThai")));
            }
        }
        return ds;
    }
}