package DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Nghiệp vụ thanh toán và khuyến mãi; mọi dữ liệu tiền lấy từ SQL, không tin UI. */
public final class ThanhToanKhuyenMaiDAO {
    @FunctionalInterface
    public interface NguonKetNoi { Connection mo() throws SQLException; }
    private final NguonKetNoi ketNoi;

    public ThanhToanKhuyenMaiDAO(NguonKetNoi ketNoi) {
        this.ketNoi = Objects.requireNonNull(ketNoi);
    }

    public static final class Phieu {
        public final int maPK, maHD;
        public final String benhNhan, trangThai;
        public final BigDecimal tongGoc;
        public Phieu(int maPK, String benhNhan, String trangThai, BigDecimal tongGoc) {
            this(maPK, 0, benhNhan, trangThai, tongGoc);
        }
        public Phieu(int maPK, int maHD, String benhNhan, String trangThai, BigDecimal tongGoc) {
            this.maPK=maPK; this.maHD=maHD; this.benhNhan=benhNhan;
            this.trangThai=trangThai; this.tongGoc=tongGoc;
        }
        @Override public String toString() { return "PK"+maPK+" - "+benhNhan; }
    }
    public static final class ChiTiet {
        public final String dichVu;
        public final int soLuong;
        public final BigDecimal donGia, thanhTien;
        public ChiTiet(String dichVu, int soLuong, BigDecimal donGia, BigDecimal thanhTien) {
            this.dichVu=dichVu; this.soLuong=soLuong; this.donGia=donGia; this.thanhTien=thanhTien;
        }
    }
    public static final class KhuyenMai {
        public final String ma, ten, loai;
        public final BigDecimal giaTri;
        public final LocalDate batDau, ketThuc;
        public final boolean bat;
        public KhuyenMai(String ma,String ten,String loai,BigDecimal giaTri,LocalDate batDau,
                         LocalDate ketThuc,boolean bat) {
            this.ma=ma; this.ten=ten; this.loai=loai; this.giaTri=giaTri;
            this.batDau=batDau; this.ketThuc=ketThuc; this.bat=bat;
        }
    }
    public static final class DuToan {
        public final Phieu phieu;
        public final KhuyenMai khuyenMai;
        public final BigDecimal giam, phaiThu;
        public DuToan(Phieu phieu,KhuyenMai khuyenMai,BigDecimal giam) {
            this.phieu=phieu; this.khuyenMai=khuyenMai; this.giam=giam;
            this.phaiThu=phieu.tongGoc.subtract(giam);
        }
    }
    public static final class HoaDon {
        public final int maHD, maPK;
        public final String benhNhan, ngayLap, phuongThuc, trangThai, maKM;
        public final BigDecimal tienGoc, tienGiam, phaiThu;
        public HoaDon(int maHD,int maPK,String benhNhan,String ngayLap,String phuongThuc,
                      String trangThai,String maKM,BigDecimal tienGoc,BigDecimal tienGiam,BigDecimal phaiThu) {
            this.maHD=maHD; this.maPK=maPK; this.benhNhan=benhNhan; this.ngayLap=ngayLap;
            this.phuongThuc=phuongThuc; this.trangThai=trangThai; this.maKM=maKM;
            this.tienGoc=tienGoc; this.tienGiam=tienGiam; this.phaiThu=phaiThu;
        }
    }
    public static final class KetQua {
        public final int maHD, maTT;
        public final DuToan duToan;
        public final BigDecimal tienThua;
        public KetQua(int maHD,int maTT,DuToan duToan,BigDecimal tienThua) {
            this.maHD=maHD;this.maTT=maTT;this.duToan=duToan;this.tienThua=tienThua;
        }
    }

    /** Kiểm tra quyền thực tế theo maQuyen của CSDL; full là ngoại lệ demo riêng. */
    private void canQuyen(Connection c, int maTK, String quyen) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
            +"JOIN dbo.NhanVien n ON n.maTK=t.maTK "
            +"WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND n.trangThai=N'Đang làm việc' "
            +"AND ((t.tenDangNhap='full' AND v.tenVaiTro=N'Quản trị viên') "
            +"OR EXISTS (SELECT 1 FROM dbo.VaiTroQuyen p WHERE p.maVaiTro=t.maVaiTro "
            +"AND p.maQuyen IN ('LT_THANHTOAN','QL_THANHTOAN') "
            +"AND v.tenVaiTro IN (N'Lễ tân',N'Quản lý phòng khám')))";
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,maTK);
            try (ResultSet r=ps.executeQuery()) {
                if (!r.next()) throw new SQLException("Tài khoản không được phép thanh toán hóa đơn.");
            }
        }
    }

    private void canQuanLy(Connection c, int maTK) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
            +"JOIN dbo.NhanVien n ON n.maTK=t.maTK WHERE t.maTK=? "
            +"AND t.trangThai=N'Hoạt động' AND n.trangThai=N'Đang làm việc' "
            +"AND ((t.tenDangNhap='full' AND v.tenVaiTro=N'Quản trị viên') "
            +"OR (v.tenVaiTro IN (N'Quản lý phòng khám',N'Lễ tân') AND EXISTS (SELECT 1 FROM dbo.VaiTroQuyen p "
            +"WHERE p.maVaiTro=t.maVaiTro AND p.maQuyen='QL_KHUYENMAI')))";
        try (PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,maTK);
            try (ResultSet r=ps.executeQuery()) {
                if (!r.next()) throw new SQLException("Bạn không có quyền quản lý mã khuyến mãi.");
            }
        }
    }

    /** Chỉ các phiếu khám hoàn tất, có điều trị, chưa có giao dịch chưa hủy. */
    public List<Phieu> phieuChoThu(int actor) throws SQLException {
        String sql="SELECT TOP 150 p.maPK,COALESCE(h.maHD,0) AS maHD,b.hoTen,p.trangThai," 
                +"SUM(c.thanhTien) AS tongGoc "
                +"FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                +"JOIN dbo.ChiTietDieuTri c ON c.maPK=p.maPK "
                +"JOIN dbo.HoaDon h ON h.maPK=p.maPK AND h.trangThai IN (N'Chưa thanh toán',N'Chờ thanh toán') "
                +"WHERE p.trangThai IN (N'Hoàn tất',N'Hoàn thành',N'Đã hoàn thành') "
                +"AND NOT EXISTS (SELECT 1 FROM dbo.ThanhToan t WHERE t.maPK=p.maPK "
                +"AND t.trangThai NOT IN (N'Đã hủy',N'Thất bại',N'Chờ xử lý',N'Chờ thanh toán')) "
                +"AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon h2 WHERE h2.maPK=p.maPK "
                +"AND h2.trangThai NOT IN (N'Đã hủy',N'Chưa thanh toán',N'Chờ thanh toán')) "
                +"GROUP BY p.maPK,h.maHD,b.hoTen,p.trangThai "
                +"HAVING SUM(c.thanhTien)>0 ORDER BY p.maPK DESC";
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(sql);ResultSet r=ps.executeQuery()) {
                List<Phieu> ds=new ArrayList<>();
                while(r.next()) ds.add(new Phieu(r.getInt(1),r.getInt(2),r.getString(3),r.getString(4),r.getBigDecimal(5)));
                return ds;
            }
        }
    }

    public List<ChiTiet> chiTietDieuTri(int maPK,int actor) throws SQLException {
        String sql="SELECT d.tenDDT,c.soLuong,c.donGia,c.thanhTien FROM dbo.ChiTietDieuTri c "
                +"JOIN dbo.DichVuDieuTri d ON c.maDDT=d.maDDT WHERE c.maPK=? ORDER BY c.maCT";
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                ps.setInt(1,maPK);
                try(ResultSet r=ps.executeQuery()) {
                    List<ChiTiet> ds=new ArrayList<>();
                    while(r.next()) ds.add(new ChiTiet(r.getString(1),r.getInt(2),r.getBigDecimal(3),r.getBigDecimal(4)));
                    return ds;
                }
            }
        }
    }

    private Phieu phieuHopLe(Connection c,int maPK,boolean khoa) throws SQLException {
        String lock=khoa?" WITH (UPDLOCK,HOLDLOCK) ":" ";
        String sql="SELECT p.maPK,b.hoTen,p.trangThai,"
                +"(SELECT COALESCE(SUM(d.thanhTien),0) FROM dbo.ChiTietDieuTri d WHERE d.maPK=p.maPK) AS tongGoc "
                +"FROM dbo.PhieuKham p"+lock+"JOIN dbo.BenhNhan b ON p.maBN=b.maBN WHERE p.maPK=?";
        Phieu p;
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,maPK);
            try(ResultSet r=ps.executeQuery()) {
                if(!r.next())throw new SQLException("Không tìm thấy phiếu khám.");
                p=new Phieu(r.getInt(1),r.getString(2),r.getString(3),r.getBigDecimal(4));
            }
        }
        if(!java.util.Arrays.asList("Hoàn tất","Hoàn thành","Đã hoàn thành").contains(p.trangThai))
            throw new SQLException("Phiếu khám chưa hoàn tất điều trị.");
        if(p.tongGoc==null || p.tongGoc.signum()<=0)
            throw new SQLException("Phiếu khám chưa có chi tiết điều trị tính tiền.");
        try(PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) "
                +"WHERE maPK=? AND trangThai NOT IN (N'Đã hủy',N'Thất bại',N'Chờ xử lý',N'Chờ thanh toán')")) {
            ps.setInt(1,maPK);
            try(ResultSet r=ps.executeQuery()) {
                r.next();if(r.getInt(1)>0)throw new SQLException("Phiếu khám đã có giao dịch; không được thu trùng.");
            }
        }
        try (PreparedStatement ps=c.prepareStatement(
                "SELECT 1 FROM dbo.HoaDon WITH (UPDLOCK,HOLDLOCK) WHERE maPK=? "
                +"AND trangThai NOT IN (N'Đã hủy',N'Chưa thanh toán',N'Chờ thanh toán')")) {
            ps.setInt(1,maPK);
            try(ResultSet r=ps.executeQuery()) {
                if(r.next()) throw new SQLException("Phiếu khám đã có hóa đơn được thanh toán.");
            }
        }
        return p;
    }

    /** Tiền giảm không bao giờ vượt tiền gốc, làm tròn VND. */
    public static BigDecimal tinhGiam(BigDecimal tienGoc,String loai,BigDecimal giaTri) {
        if(tienGoc==null || tienGoc.signum()<0 || giaTri==null || giaTri.signum()<=0)
            throw new IllegalArgumentException("Giá trị khuyến mãi không hợp lệ.");
        if("PHAN_TRAM".equals(loai)) {
            if(giaTri.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("Phần trăm quá 100.");
            return tienGoc.multiply(giaTri).divide(new BigDecimal("100"),0,RoundingMode.HALF_UP)
                    .min(tienGoc);
        }
        if("SO_TIEN".equals(loai))return giaTri.min(tienGoc);
        throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ.");
    }

    /** Lấy ưu đãi có số tiền giảm thực tế lớn nhất trên phiếu, không cộng dồn. */
    private DuToan duToan(Connection c,Phieu phieu,String maChon) throws SQLException {
        String sql="SELECT maKM,tenKM,loaiGiam,giaTri,ngayBatDau,ngayKetThuc,trangThai "
                +"FROM dbo.KhuyenMai WITH (HOLDLOCK) WHERE trangThai=1 "
                +"AND CONVERT(date,GETDATE()) BETWEEN ngayBatDau AND ngayKetThuc ORDER BY maKM";
        KhuyenMai totNhat=null;
        BigDecimal giamMax=BigDecimal.ZERO;
        try(PreparedStatement ps=c.prepareStatement(sql);ResultSet r=ps.executeQuery()) {
            while(r.next()) {
                KhuyenMai km=new KhuyenMai(r.getString(1),r.getString(2),r.getString(3),
                    r.getBigDecimal(4),r.getDate(5).toLocalDate(),r.getDate(6).toLocalDate(),r.getBoolean(7));
                BigDecimal giam=tinhGiam(phieu.tongGoc,km.loai,km.giaTri);
                if (maChon==null ? giam.compareTo(giamMax)>0 : maChon.equals(km.ma)) { totNhat=km;giamMax=giam; }
            }
        }
        if (maChon!=null && totNhat==null) throw new SQLException("Mã khuyến mãi đã hết hiệu lực hoặc ngừng áp dụng.");
        return new DuToan(phieu,totNhat,giamMax);
    }

    public DuToan xemDuToan(int maPK,int actor) throws SQLException {
        return xemDuToan(maPK,actor,null);
    }

    public DuToan xemDuToan(int maPK,int actor,String maKhuyenMai) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            return duToan(c,phieuHopLe(c,maPK,false),maKhuyenMai);
        }
    }

    /** Danh sách chỉ gồm mã đang bật và còn trong thời gian hiệu lực. */
    public List<KhuyenMai> khuyenMaiHieuLuc(int actor) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(
                    "SELECT maKM,tenKM,loaiGiam,giaTri,ngayBatDau,ngayKetThuc,trangThai "
                    +"FROM dbo.KhuyenMai WHERE trangThai=1 "
                    +"AND CONVERT(date,GETDATE()) BETWEEN ngayBatDau AND ngayKetThuc ORDER BY maKM");
                ResultSet r=ps.executeQuery()) {
                List<KhuyenMai> ds=new ArrayList<>();
                while(r.next()) ds.add(new KhuyenMai(r.getString(1),r.getString(2),r.getString(3),
                    r.getBigDecimal(4),r.getDate(5).toLocalDate(),r.getDate(6).toLocalDate(),r.getBoolean(7)));
                return ds;
            }
        }
    }

    public static final class NganHang {
        public final String bin,soTaiKhoan,chuTaiKhoan;
        public NganHang(String bin,String soTaiKhoan,String chuTaiKhoan) {
            this.bin=bin;this.soTaiKhoan=soTaiKhoan;this.chuTaiKhoan=chuTaiKhoan;
        }
    }
    public NganHang nganHang(int actor) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(
                    "SELECT BinNganHang,SoTaiKhoan,ChuTaiKhoan FROM dbo.CauHinhChuyenKhoan WHERE Id=1");
                ResultSet r=ps.executeQuery()) {
                if (!r.next() || r.getString(1)==null || !r.getString(1).matches("[0-9]{6}")
                        || r.getString(2)==null || !r.getString(2).matches("[0-9]{6,19}")
                        || r.getString(3)==null || r.getString(3).isBlank())
                    throw new SQLException("Chưa cấu hình BIN, số tài khoản và tên chủ tài khoản ngân hàng. Hãy cập nhật dbo.CauHinhChuyenKhoan.");
                return new NganHang(r.getString(1),r.getString(2),r.getString(3));
            }
        }
    }

    /** Thu thực tế và tạo hóa đơn đồng thời. Có khóa phiếu khám và rollback khi lỗi. */
    public KetQua thuTien(int maPK,int actor,String phuongThuc,BigDecimal tienNhan,
                          String maGiaoDich,boolean daXacNhanNhanTien) throws SQLException {
        return thuTien(maPK,actor,phuongThuc,tienNhan,maGiaoDich,daXacNhanNhanTien,null,null);
    }
    public KetQua thuTien(int maPK,int actor,String phuongThuc,BigDecimal tienNhan,
                          String maGiaoDich,boolean daXacNhanNhanTien,String maKhuyenMai,
                          BigDecimal soTienDaXacNhan) throws SQLException {
        if(!"Tiền mặt".equals(phuongThuc)&&!"Chuyển khoản".equals(phuongThuc))
            throw new SQLException("Chỉ hỗ trợ Tiền mặt hoặc Chuyển khoản.");
        if(!daXacNhanNhanTien)throw new SQLException("Phải xác nhận đã nhận tiền trước khi lưu.");
        String thamChieu=maGiaoDich==null?"":maGiaoDich.trim();
        if("Chuyển khoản".equals(phuongThuc) && (thamChieu.isEmpty()||thamChieu.length()>100))
            throw new SQLException("Chuyển khoản cần mã giao dịch ngân hàng (tối đa 100 ký tự).");
        try(Connection c=ketNoi.mo()) {
            int isolation=c.getTransactionIsolation();
            boolean auto=c.getAutoCommit();
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try {
                canQuyen(c,actor,"THANHTOAN");
                DuToan d=duToan(c,phieuHopLe(c,maPK,true),maKhuyenMai);
                if (soTienDaXacNhan!=null && d.phaiThu.compareTo(soTienDaXacNhan)!=0)
                    throw new SQLException("Tiền phải thu đã thay đổi. Tải lại dự toán và mã QR trước khi xác nhận.");
                BigDecimal tienThua=BigDecimal.ZERO;
                if("Tiền mặt".equals(phuongThuc)) {
                    if(tienNhan==null || tienNhan.compareTo(d.phaiThu)<0)
                        throw new SQLException("Tiền khách đưa không đủ thanh toán.");
                    tienThua=tienNhan.subtract(d.phaiThu);
                } else {
                    try(PreparedStatement ps=c.prepareStatement(
                            "SELECT COUNT(*) FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) WHERE maGiaoDich=?")) {
                        ps.setString(1,thamChieu);
                        try(ResultSet r=ps.executeQuery()) {r.next();if(r.getInt(1)>0)
                            throw new SQLException("Mã giao dịch ngân hàng đã được sử dụng.");}
                    }
                }
                int maHD=0, maTT=0;
                try (PreparedStatement ps=c.prepareStatement(
                        "SELECT maHD,maTT FROM dbo.HoaDon WITH (UPDLOCK,HOLDLOCK) "
                        +"WHERE maPK=? AND trangThai IN (N'Chưa thanh toán',N'Chờ thanh toán')")) {
                    ps.setInt(1,maPK);
                    try(ResultSet r=ps.executeQuery()) {
                        if(r.next()) {maHD=r.getInt(1);maTT=r.getInt(2);if(r.wasNull())maTT=0;}
                    }
                }
                if(maTT>0) {
                    try(PreparedStatement ps=c.prepareStatement(
                            "UPDATE dbo.ThanhToan SET ngayThanhToan=SYSDATETIME(),soTien=?,phuongThuc=?," 
                            +"trangThai=N'Thành công',maGiaoDich=? WHERE maTT=? "
                            +"AND trangThai IN (N'Chờ xử lý',N'Chờ thanh toán')")) {
                        ps.setBigDecimal(1,d.phaiThu);ps.setString(2,phuongThuc);
                        if("Tiền mặt".equals(phuongThuc))ps.setNull(3,java.sql.Types.VARCHAR);
                        else ps.setString(3,thamChieu);
                        ps.setInt(4,maTT);
                        if(ps.executeUpdate()!=1) throw new SQLException("Giao dịch chờ đã được xử lý.");
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement(
                            "INSERT dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai,maGiaoDich) "
                            +"OUTPUT INSERTED.maTT VALUES(?,SYSDATETIME(),?,?,N'Thành công',?)")) {
                        ps.setInt(1,maPK);ps.setBigDecimal(2,d.phaiThu);ps.setString(3,phuongThuc);
                        if("Tiền mặt".equals(phuongThuc))ps.setNull(4,java.sql.Types.VARCHAR);
                        else ps.setString(4,thamChieu);
                        try(ResultSet r=ps.executeQuery()) {
                            if(!r.next())throw new SQLException("Không lấy được mã thanh toán.");
                            maTT=r.getInt(1);
                        }
                    }
                }
                if(maHD>0) {
                    try(PreparedStatement ps=c.prepareStatement(
                            "UPDATE dbo.HoaDon SET maTT=?,tongTien=?,trangThai=N'Đã thanh toán'," 
                            +"maKM=?,tienGoc=?,tienGiam=?,PhuongThucTT=?,TienKhachDua=?,TienThua=? "
                            +"WHERE maHD=? AND trangThai IN (N'Chưa thanh toán',N'Chờ thanh toán')")) {
                        ps.setInt(1,maTT);ps.setBigDecimal(2,d.phaiThu);
                        if(d.khuyenMai==null)ps.setNull(3,java.sql.Types.VARCHAR);
                        else ps.setString(3,d.khuyenMai.ma);
                        ps.setBigDecimal(4,d.phieu.tongGoc);ps.setBigDecimal(5,d.giam);
                        ps.setString(6,phuongThuc);
                        ps.setBigDecimal(7,"Tiền mặt".equals(phuongThuc)?tienNhan:d.phaiThu);
                        ps.setBigDecimal(8,tienThua);ps.setInt(9,maHD);
                        if(ps.executeUpdate()!=1) throw new SQLException("Hóa đơn chờ đã thay đổi.");
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement(
                            "INSERT dbo.HoaDon(maTT,maPK,ngayLap,tongTien,trangThai,maKM,tienGoc,tienGiam," 
                            +"PhuongThucTT,TienKhachDua,TienThua) "
                            +"OUTPUT INSERTED.maHD VALUES(?,?,SYSDATETIME(),?,N'Đã thanh toán',?,?,?,?,?,?)")) {
                        ps.setInt(1,maTT);ps.setInt(2,maPK);ps.setBigDecimal(3,d.phaiThu);
                        if(d.khuyenMai==null)ps.setNull(4,java.sql.Types.VARCHAR);
                        else ps.setString(4,d.khuyenMai.ma);
                        ps.setBigDecimal(5,d.phieu.tongGoc);ps.setBigDecimal(6,d.giam);
                        ps.setString(7,phuongThuc);
                        ps.setBigDecimal(8,"Tiền mặt".equals(phuongThuc)?tienNhan:d.phaiThu);
                        ps.setBigDecimal(9,tienThua);
                        try(ResultSet r=ps.executeQuery()) {
                            if(!r.next())throw new SQLException("Không lấy được mã hóa đơn.");
                            maHD=r.getInt(1);
                        }
                    }
                }
                // Lưu chi tiết dịch vụ đã thu tại thời điểm thanh toán.
                try (PreparedStatement ps=c.prepareStatement(
                        "INSERT dbo.ChiTietHoaDon(maHD,maDV,tenDichVu,soLuong,donGia) "
                        +"SELECT ?, dv.MaDV, d.tenDDT, c.soLuong, c.donGia "
                        +"FROM dbo.ChiTietDieuTri c JOIN dbo.DichVuDieuTri d ON d.maDDT=c.maDDT "
                        +"LEFT JOIN dbo.DichVu dv ON dv.TenDV=d.tenDDT WHERE c.maPK=? "
                        +"AND NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon h WHERE h.maHD=?)")) {
                    ps.setInt(1,maHD);ps.setInt(2,maPK);ps.setInt(3,maHD);
                    ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement(
                        "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    ps.setString(1,"Thanh toán hóa đơn");ps.setString(2,"HoaDon");
                    ps.setString(3,"maTK="+actor+" | maPK="+maPK+" | maHD="+maHD+
                        " | maKM="+(d.khuyenMai==null?"":d.khuyenMai.ma));
                    ps.executeUpdate();
                }
                c.commit();
                return new KetQua(maHD,maTT,d,tienThua);
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
            finally {c.setAutoCommit(auto);c.setTransactionIsolation(isolation);}
        }
    }

    /** Chỉ hủy hóa đơn chưa thu tiền, không hủy các hóa đơn đã thanh toán. */
    public void huyHoaDonCho(int maHD,int actor) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            boolean auto=c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                canQuyen(c,actor,"THANHTOAN");
                int maTT=0;
                try(PreparedStatement ps=c.prepareStatement(
                        "SELECT maTT FROM dbo.HoaDon WITH (UPDLOCK,HOLDLOCK) WHERE maHD=? "
                        +"AND trangThai IN (N'Chưa thanh toán',N'Chờ thanh toán')")) {
                    ps.setInt(1,maHD);
                    try(ResultSet r=ps.executeQuery()) {
                        if(!r.next()) throw new SQLException("Không tìm thấy hóa đơn chưa thanh toán.");
                        maTT=r.getInt(1);if(r.wasNull())maTT=0;
                    }
                }
                if(maTT>0) {
                    try(PreparedStatement ps=c.prepareStatement(
                            "UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=? "
                            +"AND trangThai IN (N'Chờ xử lý',N'Chờ thanh toán')")) {
                        ps.setInt(1,maTT);
                        if(ps.executeUpdate()!=1) throw new SQLException("Giao dịch đã được xử lý, không thể hủy.");
                    }
                }
                try(PreparedStatement ps=c.prepareStatement(
                        "UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maHD=? "
                        +"AND trangThai IN (N'Chưa thanh toán',N'Chờ thanh toán')")) {
                    ps.setInt(1,maHD);
                    if(ps.executeUpdate()!=1) throw new SQLException("Hóa đơn đã thay đổi.");
                }
                try(PreparedStatement ps=c.prepareStatement(
                        "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    ps.setString(1,"Hủy hóa đơn chưa thanh toán");ps.setString(2,"HoaDon");
                    ps.setString(3,"maTK="+actor+" | maHD="+maHD);ps.executeUpdate();
                }
                c.commit();
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
            finally {c.setAutoCommit(auto);}
        }
    }

    /** Chi tiết dịch vụ đã chốt trên hóa đơn, không lấy theo bảng giá hiện tại. */
    public List<ChiTiet> chiTietHoaDon(int maHD,int actor) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(
                    "SELECT TenDichVu,SoLuong,DonGia,ThanhTien FROM dbo.ChiTietHoaDon "
                    +"WHERE maHD=? ORDER BY maCT")) {
                ps.setInt(1,maHD);
                try(ResultSet r=ps.executeQuery()) {
                    List<ChiTiet> ds=new ArrayList<>();
                    while(r.next()) ds.add(new ChiTiet(r.getString(1),r.getInt(2),
                            r.getBigDecimal(3),r.getBigDecimal(4)));
                    return ds;
                }
            }
        }
    }

    public List<HoaDon> hoaDonGanDay(int actor) throws SQLException {
        String sql="SELECT TOP 150 h.maHD,h.maPK,b.hoTen,CONVERT(varchar(19),h.ngayLap,120) AS ngayLap,"
                +"ISNULL(t.phuongThuc,'') AS phuongThuc,h.trangThai,ISNULL(h.maKM,'') AS maKM,"
                +"COALESCE(h.tienGoc,h.tongTien) AS tienGoc,COALESCE(h.tienGiam,0) AS tienGiam,h.tongTien "
                +"FROM dbo.HoaDon h JOIN dbo.ThanhToan t ON h.maTT=t.maTT "
                +"JOIN dbo.PhieuKham p ON h.maPK=p.maPK JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                +"WHERE h.trangThai=N'Đã thanh toán' AND t.trangThai=N'Thành công' "
                +"ORDER BY h.maHD DESC";
        try(Connection c=ketNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement(sql);ResultSet r=ps.executeQuery()) {
                List<HoaDon> ds=new ArrayList<>();
                while(r.next())ds.add(new HoaDon(r.getInt(1),r.getInt(2),r.getString(3),r.getString(4),
                    r.getString(5),r.getString(6),r.getString(7),r.getBigDecimal(8),
                    r.getBigDecimal(9),r.getBigDecimal(10)));
                return ds;
            }
        }
    }

    public List<KhuyenMai> danhSachKhuyenMai(int actor) throws SQLException {
        try(Connection c=ketNoi.mo()) {
            canQuanLy(c,actor);
            try(PreparedStatement ps=c.prepareStatement(
                    "SELECT maKM,tenKM,loaiGiam,giaTri,ngayBatDau,ngayKetThuc,trangThai "
                    +"FROM dbo.KhuyenMai ORDER BY trangThai DESC,ngayKetThuc DESC,maKM");
                ResultSet r=ps.executeQuery()) {
                List<KhuyenMai> ds=new ArrayList<>();
                while(r.next()) ds.add(new KhuyenMai(r.getString(1),r.getString(2),r.getString(3),
                    r.getBigDecimal(4),r.getDate(5).toLocalDate(),r.getDate(6).toLocalDate(),r.getBoolean(7)));
                return ds;
            }
        }
    }

    /** Không cho đổi mã KM đã tạo; chỉ tạo mới hoặc chỉnh thông tin và bật/tắt. */
    public String luuKhuyenMai(KhuyenMai km,boolean taoMoi,int actor) throws SQLException {
        if(km==null || (!taoMoi && (km.ma==null || !km.ma.matches("[A-Za-z0-9_-]{2,30}"))))
            throw new SQLException("Mã khuyến mãi 2-30 ký tự (chữ, số, _, -).");
        if(km.ten==null || km.ten.isBlank() || km.ten.length()>120)
            throw new SQLException("Tên khuyến mãi không hợp lệ.");
        if(km.batDau==null || km.ketThuc==null || km.batDau.isAfter(km.ketThuc))
            throw new SQLException("Khoảng ngày khuyến mãi không hợp lệ.");
        try {tinhGiam(new BigDecimal("1000000"),km.loai,km.giaTri);}
        catch(IllegalArgumentException e) {throw new SQLException(e.getMessage(),e);}
        String sql=taoMoi
            ? "INSERT dbo.KhuyenMai(maKM,tenKM,loaiGiam,giaTri,ngayBatDau,ngayKetThuc,trangThai) "
               +"VALUES(?,?,?,?,?,?,?)"
            : "UPDATE dbo.KhuyenMai SET tenKM=?,loaiGiam=?,giaTri=?,ngayBatDau=?,ngayKetThuc=?,"
               +"trangThai=? WHERE maKM=?";
        try(Connection c=ketNoi.mo()) {
            boolean auto=c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                canQuanLy(c,actor);
                String maLuu=km.ma;
                if (taoMoi) {
                    try (PreparedStatement ps=c.prepareStatement("SELECT NEXT VALUE FOR dbo.SeqKhuyenMai")) {
                        try (ResultSet r=ps.executeQuery()) {
                            if (!r.next()) throw new SQLException("Không tạo được mã khuyến mãi.");
                            maLuu="KM"+r.getLong(1);
                        }
                    }
                }
                try(PreparedStatement ps=c.prepareStatement(sql)) {
                    if(taoMoi)ps.setString(1,maLuu);
                    ps.setString(taoMoi?2:1,km.ten.trim());
                    ps.setString(taoMoi?3:2,km.loai);
                    ps.setBigDecimal(taoMoi?4:3,km.giaTri);
                    ps.setDate(taoMoi?5:4,Date.valueOf(km.batDau));
                    ps.setDate(taoMoi?6:5,Date.valueOf(km.ketThuc));
                    ps.setBoolean(taoMoi?7:6,km.bat);
                    if(!taoMoi)ps.setString(7,maLuu);
                    if(ps.executeUpdate()!=1) throw new SQLException(
                        taoMoi?"Không thêm được mã (có thể bị trùng).":"Không tìm thấy mã khuyến mãi.");
                }
                try(PreparedStatement ps=c.prepareStatement(
                    "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    ps.setString(1,taoMoi?"Tạo khuyến mãi":"Cập nhật khuyến mãi");
                    ps.setString(2,"KhuyenMai");
                    ps.setString(3,"maTK="+actor+" | maKM="+maLuu+" | trangThai="+km.bat);
                    ps.executeUpdate();
                }
                c.commit();
                return maLuu;
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
            finally {c.setAutoCommit(auto);}
        }
    }
}
