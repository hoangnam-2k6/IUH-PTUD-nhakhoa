package DAO;
import ConnectDB.ConnectDB;
import Entity.LichLamViec;
import java.sql.*;
import java.util.*;
/** Dữ liệu ca làm việc được form Quản lý tham chiếu. */
public final class LichLamViecDAO {
    public List<LichLamViec> danhSach(String tuKhoa) throws SQLException {
        List<LichLamViec> ds=new ArrayList<>();
        String sql="SELECT maLLV,maNV,ngayLamViec,gioBatDau,gioKetThuc,phongKham,trangThai FROM dbo.LichLamViec "
            + "WHERE CONVERT(VARCHAR(20),maLLV) LIKE ? OR CONVERT(VARCHAR(20),maNV) LIKE ? "
            + "OR ISNULL(phongKham,N'') LIKE ? ORDER BY ngayLamViec DESC,gioBatDau";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            String k="%"+(tuKhoa==null?"":tuKhoa.trim())+"%";
            p.setString(1,k);p.setString(2,k);p.setString(3,k);
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) ds.add(new LichLamViec(r.getInt(1),r.getString(2),r.getDate(3).toLocalDate(),
                    r.getTime(4).toLocalTime(),r.getTime(5).toLocalTime(),r.getString(6),r.getString(7)));
            }
        }
        return ds;
    }
    public List<String[]> danhSachNhanVien() throws SQLException {
        List<String[]> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "SELECT maNV,hoTen FROM dbo.NhanVien WHERE trangThai=N'Đang làm việc' ORDER BY hoTen");ResultSet r=p.executeQuery()) {
            while(r.next()) ds.add(new String[]{r.getString(1),r.getString(2)});
        }
        return ds;
    }
    public void luu(LichLamViec l,boolean sua) throws SQLException {
        validation.Regex_LichLamViec.kiemTra(l);
        String sql=sua?"UPDATE dbo.LichLamViec SET maNV=?,ngayLamViec=?,gioBatDau=?,gioKetThuc=?,phongKham=?,trangThai=? WHERE maLLV=?"
            :"INSERT dbo.LichLamViec(maNV,ngayLamViec,gioBatDau,gioKetThuc,phongKham,trangThai) VALUES(?,?,?,?,?,?)";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,l.maNV());p.setDate(2,java.sql.Date.valueOf(l.ngayLamViec()));p.setTime(3,Time.valueOf(l.gioBatDau()));
            p.setTime(4,Time.valueOf(l.gioKetThuc()));p.setString(5,l.phongKham());p.setString(6,l.trangThai());
            if(sua)p.setInt(7,l.maLLV());
            if(p.executeUpdate()!=1)throw new SQLException("Không lưu được ca làm việc.");
        }
    }
    public void huy(int ma) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement("UPDATE dbo.LichLamViec SET trangThai=N'Đã hủy' WHERE maLLV=?")) {
            p.setInt(1,ma);if(p.executeUpdate()!=1)throw new SQLException("Không tìm thấy ca làm việc.");
        }
    }
}
