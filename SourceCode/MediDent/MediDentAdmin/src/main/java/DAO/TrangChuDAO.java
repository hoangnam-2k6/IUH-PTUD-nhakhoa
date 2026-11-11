package DAO;
import ConnectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/** Nguồn dữ liệu cho trang chủ Quản lý (không chứa GUI). */
public final class TrangChuDAO {
    public record TongQuan(int benhNhan, int lichHen, int caLamViec, BigDecimal doanhThu, int choThanhToan) {}
    public record LichSapToi(int maLich, String benhNhan, String ngay, String gio, String bacSi, String trangThai) {}
    private int dem(Connection c, String sql) throws SQLException {
        try (Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)) {return r.next()?r.getInt(1):0;}
    }
    public TongQuan layTongQuan() throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi()) {
            int bn=dem(c,"SELECT COUNT(*) FROM dbo.BenhNhan");
            int lh=dem(c,"SELECT COUNT(*) FROM dbo.LichHen WHERE CONVERT(DATE,NgayGio)>=CONVERT(DATE,GETDATE())");
            int ca=dem(c,"SELECT COUNT(*) FROM dbo.BacSi WHERE TrangThai=N'Hoạt động'");
            int cho=dem(c,"SELECT COUNT(*) FROM dbo.HoaDon WHERE TrangThai=N'Chưa thanh toán'");
            BigDecimal doanh=BigDecimal.ZERO;
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT ISNULL(SUM(TongTien),0) FROM dbo.HoaDon WHERE TrangThai=N'Đã thanh toán'")) {
                if(r.next()) doanh=r.getBigDecimal(1);
            }
            return new TongQuan(bn,lh,ca,doanh,cho);
        }
    }
    public List<LichSapToi> layLichSapToi() throws SQLException {
        List<LichSapToi> ds=new ArrayList<>();
        String sql="SELECT TOP 15 l.MaLH,b.HoTen,CONVERT(VARCHAR(10),l.NgayGio,103) Ngay,CONVERT(VARCHAR(5),l.NgayGio,108) Gio,"
            + "bs.HoTen AS BacSi,l.TrangThai FROM dbo.LichHen l "
            + "JOIN dbo.BenhNhan b ON b.MaBN=l.MaBN JOIN dbo.BacSi bs ON bs.MaBS=l.MaBS "
            + "WHERE l.NgayGio>=GETDATE() ORDER BY l.NgayGio";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
            while(r.next()) ds.add(new LichSapToi(r.getInt(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getString(6)));
        }
        return ds;
    }
}
