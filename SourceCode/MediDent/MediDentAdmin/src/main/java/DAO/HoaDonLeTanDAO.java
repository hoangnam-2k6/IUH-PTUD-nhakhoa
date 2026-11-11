package DAO;

import ConnectDB.ConnectDB;
import Entity.BenhNhan;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.PhieuKham;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Khôi phục API hóa đơn mà các màn hình Lễ tân đang gọi, không thay đổi HoaDonDAO cũ. */
public final class HoaDonLeTanDAO {
    private static final String SELECT = "SELECT h.*,p.NgayKham,p.SoThuTu,p.MaBN,p.MaPhong,"
        + "bn.HoTen AS TenBenhNhan,pk.TenPhong,COALESCE(bsk.HoTen,bs.HoTen) AS TenBacSi,nv.HoTen AS TenNguoiLap "
        + "FROM dbo.HoaDon h JOIN dbo.PhieuKham p ON p.MaPK=h.MaPK "
        + "JOIN dbo.BenhNhan bn ON bn.MaBN=p.MaBN "
        + "LEFT JOIN dbo.PhongKham pk ON pk.MaPhong=p.MaPhong "
        + "LEFT JOIN dbo.LichHen lh ON lh.MaLH=p.MaLH "
        + "LEFT JOIN dbo.BacSi bs ON bs.MaBS=lh.MaBS "
        + "LEFT JOIN dbo.KetQuaKham kq ON kq.MaPK=p.MaPK "
        + "LEFT JOIN dbo.BacSi bsk ON bsk.MaBS=kq.MaBS "
        + "LEFT JOIN dbo.TaiKhoan tk ON tk.maTK=h.NguoiLap "
        + "LEFT JOIN dbo.NhanVien nv ON nv.maTK=tk.maTK ";

    private static HoaDon doc(ResultSet r) throws SQLException {
        HoaDon h=new HoaDon();
        h.datMaHD(r.getInt("MaHD"));h.datMaPK(r.getInt("MaPK"));
        h.datSoThuTu(r.getInt("SoThuTu"));h.datTenBenhNhan(r.getString("TenBenhNhan"));
        h.datTenPhong(r.getString("TenPhong"));h.datTenBacSi(r.getString("TenBacSi"));
        h.datTenNguoiLap(r.getString("TenNguoiLap"));h.datTrangThai(r.getString("TrangThai"));
        h.datPhuongThucTT(r.getString("PhuongThucTT"));h.datGhiChu(r.getString("GhiChu"));
        h.datLyDoHuy(r.getString("LyDoHuy"));h.datTongTien(r.getBigDecimal("TongTien"));
        BigDecimal goc=r.getBigDecimal("TienGoc");h.datTamTinh(goc==null?h.layTongTien():goc);
        BigDecimal giam=r.getBigDecimal("TienGiam");h.datGiamGia(giam==null?BigDecimal.ZERO:giam);
        h.datTienKhachDua(r.getBigDecimal("TienKhachDua"));h.datTienThua(r.getBigDecimal("TienThua"));
        Timestamp lap=r.getTimestamp("NgayLap");h.datNgayLap(lap==null?null:lap.toLocalDateTime());
        java.sql.Date ngay=r.getDate("NgayKham");h.datNgayKham(ngay==null?null:ngay.toLocalDate());
        return h;
    }
    public List<HoaDon> timTatCa() throws SQLException {return tim("","Tất cả",null,null);}
    public List<HoaDon> tim(String tuKhoa,String trangThai,LocalDate tu,LocalDate den) throws SQLException {
        String sql=SELECT+"WHERE (? IS NULL OR h.NgayLap>=?) AND (? IS NULL OR h.NgayLap<DATEADD(DAY,1,?)) "
            +"AND (?=N'Tất cả' OR h.TrangThai=?) AND (CAST(h.MaHD AS NVARCHAR(20)) LIKE ? "
            +"OR CAST(h.MaPK AS NVARCHAR(20)) LIKE ? OR bn.HoTen LIKE ? OR bn.Sdt LIKE ?) ORDER BY h.MaHD DESC";
        List<HoaDon> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
            if(tu==null){p.setNull(1,Types.DATE);p.setNull(2,Types.DATE);}else{p.setDate(1,Date.valueOf(tu));p.setDate(2,Date.valueOf(tu));}
            if(den==null){p.setNull(3,Types.DATE);p.setNull(4,Types.DATE);}else{p.setDate(3,Date.valueOf(den));p.setDate(4,Date.valueOf(den));}
            String tt=trangThai==null?"Tất cả":trangThai;p.setString(5,tt);p.setString(6,tt);
            String k="%"+(tuKhoa==null?"":tuKhoa.trim())+"%";
            for(int i=7;i<=10;i++)p.setString(i,k);
            try(ResultSet r=p.executeQuery()){while(r.next())ds.add(doc(r));}
        }
        return ds;
    }
    public HoaDon timTheoMaPK(int maPK) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(SELECT+"WHERE h.MaPK=? ORDER BY h.MaHD DESC")) {
            p.setInt(1,maPK);try(ResultSet r=p.executeQuery()){return r.next()?doc(r):null;}
        }
    }
    public HoaDon timDayDu(int maHD) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi()) {
            HoaDon h;
            try(PreparedStatement p=c.prepareStatement(SELECT+"WHERE h.MaHD=?")) {
                p.setInt(1,maHD);try(ResultSet r=p.executeQuery()) {if(!r.next())return null;h=doc(r);
                    int maBN=r.getInt("MaBN");h.datBenhNhan(new BenhNhanLeTanDAO().timTheoMa(maBN));}
            }
            List<ChiTietHoaDon> chiTiet=new ArrayList<>();
            try(PreparedStatement p=c.prepareStatement("SELECT MaCT,MaHD,MaDV,TenDichVu,SoLuong,DonGia FROM dbo.ChiTietHoaDon WHERE MaHD=? ORDER BY MaCT")) {
                p.setInt(1,maHD);try(ResultSet r=p.executeQuery()) {
                    while(r.next()) {
                        Integer maDV=(Integer)r.getObject("MaDV");
                        ChiTietHoaDon ct=new ChiTietHoaDon(maDV,r.getString("TenDichVu"),r.getInt("SoLuong"),r.getBigDecimal("DonGia"));
                        ct.datMaHD(maHD);ct.datMaCT(r.getInt("MaCT"));chiTiet.add(ct);
                    }
                }
            }
            h.datChiTiet(chiTiet);return h;
        }
    }
    public List<PhieuKham> timPhieuChuaLapHoaDon() throws SQLException {
        List<PhieuKham> ds=new ArrayList<>();
        String sql="SELECT p.MaPK,p.MaBN,p.MaPhong,p.MaLH,p.NgayKham,p.SoThuTu,p.TrangThai,p.GhiChu,"
            +"bn.HoTen AS TenBenhNhan,pk.TenPhong FROM dbo.PhieuKham p JOIN dbo.BenhNhan bn ON bn.MaBN=p.MaBN "
            +"LEFT JOIN dbo.PhongKham pk ON pk.MaPhong=p.MaPhong "
            +"WHERE NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.MaPK=p.MaPK AND h.TrangThai<>N'Đã hủy') ORDER BY p.MaPK DESC";
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
            while(r.next()) {
                PhieuKham pk=new PhieuKham();pk.datMaPK(r.getInt("MaPK"));pk.datMaBN(r.getInt("MaBN"));
                pk.datSoThuTu(r.getInt("SoThuTu"));pk.datTrangThai(r.getString("TrangThai"));
                pk.datTenBenhNhan(r.getString("TenBenhNhan"));pk.datTenPhong(r.getString("TenPhong"));
                pk.datGhiChu(r.getString("GhiChu"));Date d=r.getDate("NgayKham");if(d!=null)pk.datNgayKham(d.toLocalDate());
                ds.add(pk);
            }
        }return ds;
    }
    private static BigDecimal tong(List<ChiTietHoaDon> cts) {
        BigDecimal tong=BigDecimal.ZERO;for(ChiTietHoaDon ct:cts)tong=tong.add(ct.layThanhTien());return tong;
    }
    private static void themChiTiet(Connection c,int maHD,List<ChiTietHoaDon> cts) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT dbo.ChiTietHoaDon(MaHD,MaDV,TenDichVu,SoLuong,DonGia) VALUES(?,?,?,?,?)")) {
            for(ChiTietHoaDon ct:cts) {
                p.setInt(1,maHD);if(ct.layMaDV()==null)p.setNull(2,Types.INTEGER);else p.setInt(2,ct.layMaDV());
                p.setString(3,ct.layTenDichVu());p.setInt(4,ct.laySoLuong());p.setBigDecimal(5,ct.layDonGia());p.addBatch();
            }p.executeBatch();
        }
    }
    private static void kiemTraTien(List<ChiTietHoaDon> cts,BigDecimal giam) {
        if(cts==null||cts.isEmpty()||giam==null||giam.signum()<0||giam.compareTo(tong(cts))>0)
            throw new IllegalArgumentException("Dịch vụ hoặc số tiền giảm không hợp lệ.");
    }
    public int taoHoaDon(int maPK,Integer nguoiLap,List<ChiTietHoaDon> chiTiet,BigDecimal giam,String ghiChu) throws SQLException {
        kiemTraTien(chiTiet,giam);
        try(Connection c=ConnectDB.taoKetNoiMoi()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                int ma;
                try(PreparedStatement p=c.prepareStatement(
                    "INSERT dbo.HoaDon(MaPK,NgayLap,TongTien,TrangThai,TienGoc,TienGiam,NguoiLap,GhiChu) "
                    +"VALUES(?,SYSDATETIME(),?,N'Chưa thanh toán',?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    BigDecimal goc=tong(chiTiet);p.setInt(1,maPK);p.setBigDecimal(2,goc.subtract(giam));
                    p.setBigDecimal(3,goc);p.setBigDecimal(4,giam);
                    if(nguoiLap==null)p.setNull(5,Types.INTEGER);else p.setInt(5,nguoiLap);
                    p.setString(6,ghiChu);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Không lấy được mã hóa đơn.");ma=r.getInt(1);}
                }
                themChiTiet(c,ma,chiTiet);c.commit();return ma;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void capNhatHoaDon(int ma,List<ChiTietHoaDon> chiTiet,BigDecimal giam,String ghiChu) throws SQLException {
        kiemTraTien(chiTiet,giam);
        try(Connection c=ConnectDB.taoKetNoiMoi()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                BigDecimal goc=tong(chiTiet);
                try(PreparedStatement p=c.prepareStatement("UPDATE dbo.HoaDon SET TienGoc=?,TienGiam=?,TongTien=?,GhiChu=? "
                    +"WHERE MaHD=? AND TrangThai=N'Chưa thanh toán'")) {
                    p.setBigDecimal(1,goc);p.setBigDecimal(2,giam);p.setBigDecimal(3,goc.subtract(giam));p.setString(4,ghiChu);p.setInt(5,ma);
                    if(p.executeUpdate()!=1)throw new SQLException("Chỉ sửa được hóa đơn chưa thanh toán.");
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM dbo.ChiTietHoaDon WHERE MaHD=?")) {p.setInt(1,ma);p.executeUpdate();}
                themChiTiet(c,ma,chiTiet);c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public BigDecimal layTienDeQuet(int maHD) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement("SELECT TongTien FROM dbo.HoaDon WHERE MaHD=?")) {
            p.setInt(1,maHD);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Hóa đơn không tồn tại.");return r.getBigDecimal(1);}
        }
    }
    public void thuTien(int maHD,String phuongThuc,BigDecimal tienKhachDua,String ghiChu,BigDecimal tongXacNhan) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);c.setAutoCommit(false);
            try {
                BigDecimal phaiThu;int maPK;
                try(PreparedStatement p=c.prepareStatement("SELECT MaPK,TongTien FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE MaHD=? AND TrangThai=N'Chưa thanh toán'")) {
                    p.setInt(1,maHD);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new SQLException("Hóa đơn đã xử lý hoặc không tồn tại.");
                        maPK=r.getInt(1);phaiThu=r.getBigDecimal(2);
                    }
                }
                if(tongXacNhan==null || phaiThu.compareTo(tongXacNhan)!=0)throw new SQLException("Tổng thanh toán đã thay đổi. Hãy tải lại hóa đơn.");
                if(tienKhachDua==null||tienKhachDua.compareTo(phaiThu)<0)throw new SQLException("Số tiền nhận không đủ.");
                int maTT;
                try(PreparedStatement p=c.prepareStatement("INSERT dbo.ThanhToan(MaPK,NgayThanhToan,SoTien,PhuongThuc,TrangThai) "
                    +"VALUES(?,SYSDATETIME(),?,?,N'Thành công')",Statement.RETURN_GENERATED_KEYS)) {
                    p.setInt(1,maPK);p.setBigDecimal(2,phaiThu);p.setString(3,phuongThuc);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Không lấy được mã thanh toán.");maTT=r.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE dbo.HoaDon SET MaTT=?,TrangThai=N'Đã thanh toán',"
                    +"PhuongThucTT=?,TienKhachDua=?,TienThua=?,GhiChu=?,NgayLap=NgayLap WHERE MaHD=?")) {
                    p.setInt(1,maTT);p.setString(2,phuongThuc);p.setBigDecimal(3,tienKhachDua);
                    p.setBigDecimal(4,tienKhachDua.subtract(phaiThu));p.setString(5,ghiChu);p.setInt(6,maHD);
                    if(p.executeUpdate()!=1)throw new SQLException("Không thể cập nhật hóa đơn.");
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void huy(int maHD,String lyDo) throws SQLException {
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(
            "UPDATE dbo.HoaDon SET TrangThai=N'Đã hủy',LyDoHuy=? WHERE MaHD=? AND TrangThai=N'Chưa thanh toán'")) {
            p.setString(1,lyDo);p.setInt(2,maHD);
            if(p.executeUpdate()!=1)throw new SQLException("Chỉ hủy được hóa đơn chưa thanh toán.");
        }
    }
}
