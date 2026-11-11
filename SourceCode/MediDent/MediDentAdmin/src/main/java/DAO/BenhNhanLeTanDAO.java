package DAO;
import ConnectDB.ConnectDB;
import Entity.BenhNhan;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
/** DAO hồ sơ bệnh nhân dạng mã số, sử dụng bởi màn hình Lễ tân. */
public class BenhNhanLeTanDAO {
    private static final String COT="MaBN,HoTen,NgaySinh,GioiTinh,Sdt,Email,DiaChi,TienSuBenh,TrangThaiHoSo,Cccd,NgheNghiep,TenNguoiLienHe,SdtNguoiLienHe,DiUng,GhiChu,NgayTao";
    private static BenhNhan doc(ResultSet r) throws SQLException {
        BenhNhan b=new BenhNhan();b.datMaBN(r.getInt("MaBN"));b.datHoTen(r.getString("HoTen"));
        java.sql.Date d=r.getDate("NgaySinh");b.datNgaySinh(d==null?null:d.toLocalDate());
        b.datGioiTinh(r.getString("GioiTinh"));b.datSoDienThoai(r.getString("Sdt"));b.datEmail(r.getString("Email"));
        b.datDiaChi(r.getString("DiaChi"));b.datTienSuBenh(r.getString("TienSuBenh"));
        b.datTrangThai(r.getString("TrangThaiHoSo"));b.datCccd(r.getString("Cccd"));
        b.datNgheNghiep(r.getString("NgheNghiep"));b.datTenNguoiLienHe(r.getString("TenNguoiLienHe"));
        b.datSdtNguoiLienHe(r.getString("SdtNguoiLienHe"));b.datDiUng(r.getString("DiUng"));b.datGhiChu(r.getString("GhiChu"));
        Timestamp ts=r.getTimestamp("NgayTao");b.datNgayTao(ts==null?null:ts.toLocalDateTime());return b;
    }
    public List<BenhNhan> timTatCa() throws SQLException {return tim("");}
    public List<BenhNhan> tim(String tuKhoa) throws SQLException {
        String sql="SELECT "+COT+" FROM dbo.BenhNhan WHERE HoTen LIKE ? OR Sdt LIKE ? "
            +"OR CAST(MaBN AS VARCHAR(20)) LIKE ? OR Cccd LIKE ? ORDER BY MaBN DESC";
        List<BenhNhan> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            String k="%"+(tuKhoa==null?"":tuKhoa.trim())+"%";for(int i=1;i<=4;i++)p.setString(i,k);
            try(ResultSet r=p.executeQuery()){while(r.next())ds.add(doc(r));}
        }return ds;
    }
    public BenhNhan timTheoMa(int ma) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement("SELECT "+COT+" FROM dbo.BenhNhan WHERE MaBN=?")) {
            p.setInt(1,ma);try(ResultSet r=p.executeQuery()){return r.next()?doc(r):null;}
        }
    }
    public BenhNhan timTheoTenVaSdt(String hoTen,String sdt) throws SQLException {
        String sql="SELECT "+COT+" FROM dbo.BenhNhan WHERE HoTen=? AND Sdt=?";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,hoTen);p.setString(2,sdt);try(ResultSet r=p.executeQuery()){return r.next()?doc(r):null;}
        }
    }
    public int demDangHoatDong() throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "SELECT COUNT(*) FROM dbo.BenhNhan WHERE TrangThaiHoSo=?")) {
            p.setString(1,BenhNhan.HOAT_DONG);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):0;}
        }
    }
    private static void datThongTin(PreparedStatement p,BenhNhan b) throws SQLException {
        p.setString(1,b.layHoTen());
        if(b.layNgaySinh()==null)p.setNull(2,Types.DATE);else p.setDate(2,java.sql.Date.valueOf(b.layNgaySinh()));
        p.setString(3,b.layGioiTinh());p.setString(4,b.laySoDienThoai());p.setString(5,b.layEmail());
        p.setString(6,b.layDiaChi());p.setString(7,b.layTienSuBenh());p.setString(8,b.layTrangThai());
        p.setString(9,b.layCccd());p.setString(10,b.layNgheNghiep());p.setString(11,b.layTenNguoiLienHe());
        p.setString(12,b.laySdtNguoiLienHe());p.setString(13,b.layDiUng());p.setString(14,b.layGhiChu());
    }
    public int them(BenhNhan b) throws SQLException {
        String sql="INSERT dbo.BenhNhan(HoTen,NgaySinh,GioiTinh,Sdt,Email,DiaChi,TienSuBenh,TrangThaiHoSo,Cccd,"
            +"NgheNghiep,TenNguoiLienHe,SdtNguoiLienHe,DiUng,GhiChu) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            datThongTin(p,b);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){if(r.next())return r.getInt(1);}
            throw new SQLException("Không lấy được mã bệnh nhân vừa tạo.");
        }
    }
    public int themMoi(BenhNhan b) throws SQLException {return them(b);}
    public void capNhat(BenhNhan b) throws SQLException {
        String sql="UPDATE dbo.BenhNhan SET HoTen=?,NgaySinh=?,GioiTinh=?,Sdt=?,Email=?,DiaChi=?,TienSuBenh=?,"
            +"TrangThaiHoSo=?,Cccd=?,NgheNghiep=?,TenNguoiLienHe=?,SdtNguoiLienHe=?,DiUng=?,GhiChu=? WHERE MaBN=?";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            datThongTin(p,b);p.setInt(15,b.layMaBNSo());if(p.executeUpdate()!=1)throw new SQLException("Không tìm thấy bệnh nhân.");
        }
    }
    public void doiTrangThai(int ma,String trangThai) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
                "UPDATE dbo.BenhNhan SET TrangThaiHoSo=? WHERE MaBN=?")) {
            p.setString(1,trangThai);p.setInt(2,ma);if(p.executeUpdate()!=1)throw new SQLException("Không tìm thấy bệnh nhân.");
        }
    }
    public Object[] thongKeKham(int maBN) throws SQLException {
        String sql="SELECT COUNT(*),MAX(NgayKham) "
            +"FROM dbo.PhieuKham WHERE MaBN=?";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,maBN);
            try(ResultSet r=p.executeQuery()) {if(r.next())return new Object[]{r.getInt(1),r.getDate(2)==null?null:r.getDate(2).toLocalDate()};}
        }return new Object[]{0,null};
    }
    public BigDecimal tongDaThanhToan(int maBN) throws SQLException {
        String sql="SELECT ISNULL(SUM(h.TongTien),0) FROM dbo.HoaDon h JOIN dbo.PhieuKham p ON p.MaPK=h.MaPK "
            +"WHERE p.MaBN=? AND h.TrangThai=N'Đã thanh toán'";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,maBN);try(ResultSet r=p.executeQuery()){return r.next()?r.getBigDecimal(1):BigDecimal.ZERO;}
        }
    }
}
