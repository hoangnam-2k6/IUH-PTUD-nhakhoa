package DAO;
import ConnectDB.ConnectDB;
import Entity.LichHen;
import java.sql.*;
import java.time.*;
import java.util.*;
/** DAO lịch hẹn mã số cho màn hình Lễ tân; không thay đổi DAO lịch hẹn cũ của Quản lý. */
public class LichHenLeTanDAO {
    public static final String CHO_XAC_NHAN="Chờ xác nhận",DA_XAC_NHAN="Đã xác nhận",DA_DEN="Đã đến",HOAN_THANH="Hoàn thành",DA_HUY="Đã hủy";
    private static final String SELECT="SELECT l.MaLH,l.MaBN,l.MaBS,l.NgayGio,l.TrangThai,l.GhiChu,l.YeuCau,l.NgayGioMoi, "
        +"b.HoTen AS BenhNhan,bs.HoTen AS BacSi FROM dbo.LichHen l JOIN dbo.BenhNhan b ON b.MaBN=l.MaBN "
        +"JOIN dbo.BacSi bs ON bs.MaBS=l.MaBS ";
    private static LichHen doc(ResultSet r) throws SQLException {
        LichHen l=new LichHen();l.datMaLH(r.getInt("MaLH"));l.datMaBN(r.getInt("MaBN"));l.datMaBS(r.getInt("MaBS"));
        Timestamp ngay=r.getTimestamp("NgayGio");l.datNgayGio(ngay==null?null:ngay.toLocalDateTime());
        Timestamp moi=r.getTimestamp("NgayGioMoi");l.datNgayGioMoi(moi==null?null:moi.toLocalDateTime());
        l.datTrangThai(r.getString("TrangThai"));l.datGhiChu(r.getString("GhiChu"));l.datYeuCau(r.getString("YeuCau"));
        l.datTenBenhNhan(r.getString("BenhNhan"));l.datTenBacSi(r.getString("BacSi"));return l;
    }
    public List<LichHen> timTatCa() throws SQLException {
        List<LichHen> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(SELECT+"ORDER BY l.NgayGio DESC");ResultSet r=p.executeQuery()) {
            while(r.next())ds.add(doc(r));
        }return ds;
    }
    public List<LichHen> timTheoNgay(LocalDate ngay) throws SQLException {
        List<LichHen> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                SELECT+"WHERE CONVERT(DATE,l.NgayGio)=? ORDER BY l.NgayGio")) {
            p.setDate(1,java.sql.Date.valueOf(ngay));try(ResultSet r=p.executeQuery()){while(r.next())ds.add(doc(r));}
        }return ds;
    }
    public int tao(int maBN,int maBS,LocalDateTime ngayGio,String ghiChu,Integer maTK) throws SQLException {
        String sql="INSERT dbo.LichHen(MaBN,MaBS,MaTK,NgayGio,TrangThai,GhiChu) VALUES(?,?,?,?,?,?)";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1,maBN);p.setInt(2,maBS);if(maTK==null)p.setNull(3,Types.INTEGER);else p.setInt(3,maTK);
            p.setTimestamp(4,Timestamp.valueOf(ngayGio));p.setString(5,CHO_XAC_NHAN);p.setString(6,ghiChu);
            p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(r.next())return r.getInt(1);}
            throw new SQLException("Không lấy được mã lịch hẹn.");
        }
    }
    public void capNhat(int ma,int maBN,int maBS,LocalDateTime ngayGio,String ghiChu) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "UPDATE dbo.LichHen SET MaBN=?,MaBS=?,NgayGio=?,GhiChu=? WHERE MaLH=? AND TrangThai IN (?,?)")) {
            p.setInt(1,maBN);p.setInt(2,maBS);p.setTimestamp(3,Timestamp.valueOf(ngayGio));p.setString(4,ghiChu);
            p.setInt(5,ma);p.setString(6,CHO_XAC_NHAN);p.setString(7,DA_XAC_NHAN);
            if(p.executeUpdate()!=1)throw new SQLException("Lịch không thể chỉnh sửa.");
        }
    }
    public void xacNhan(int ma) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "UPDATE dbo.LichHen SET TrangThai=? WHERE MaLH=? AND TrangThai=?")) {
            p.setString(1,DA_XAC_NHAN);p.setInt(2,ma);p.setString(3,CHO_XAC_NHAN);
            if(p.executeUpdate()!=1)throw new SQLException("Lịch đã được xử lý.");
        }
    }
    public void huy(int ma,String lyDo) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "UPDATE dbo.LichHen SET TrangThai=?,GhiChu=? WHERE MaLH=? AND TrangThai<>?")) {
            p.setString(1,DA_HUY);p.setString(2,lyDo);p.setInt(3,ma);p.setString(4,HOAN_THANH);
            if(p.executeUpdate()!=1)throw new SQLException("Không thể hủy lịch này.");
        }
    }
    public void xuLyYeuCau(int ma,boolean chapNhan) throws SQLException {
        String sql=chapNhan?"UPDATE dbo.LichHen SET NgayGio=ISNULL(NgayGioMoi,NgayGio),NgayGioMoi=NULL,YeuCau=NULL WHERE MaLH=?"
            :"UPDATE dbo.LichHen SET NgayGioMoi=NULL,YeuCau=NULL WHERE MaLH=?";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,ma);if(p.executeUpdate()!=1)throw new SQLException("Không tìm thấy lịch.");
        }
    }
}
