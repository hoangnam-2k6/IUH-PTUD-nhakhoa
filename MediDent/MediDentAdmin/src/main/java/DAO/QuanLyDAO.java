package DAO;

import ConnectDB.KetNoi;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nghiệp vụ vận hành phòng khám, dùng chung cho Admin / Quản lý / Lễ tân.
 * Đọc và ghi trực tiếp SQL Server 2012 bằng PreparedStatement.
 * Mọi thao tác ghi đều kiểm tra quyền tài khoản từ CSDL, không tin vào nút UI.
 */
public final class QuanLyDAO {

    /** ID SQL được giữ tách biệt khỏi STT hiển thị trên JTable. */
    public static final class Hang {
        public final int id;
        public final String[] cot;
        public Hang(int id, String... cot) { this.id = id; this.cot = cot; }
    }

    /** ComboBox giữ ID để tránh người dùng phải nhập khóa ngoại. */
    public static final class LuaChon {
        public final int id;
        public final String ten;
        public LuaChon(int id, String ten) { this.id = id; this.ten = ten; }
        @Override public String toString() { return ten; }
    }

    private interface DocHang { Hang doc(ResultSet rs) throws SQLException; }

    /** Gom các lệnh SELECT về một chỗ và luôn đóng Connection/ResultSet. */
    private List<Hang> truyVan(String sql, DocHang doc, Object... args) throws SQLException {
        List<Hang> ketQua = new ArrayList<>();
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql)) {
            datThamSo(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ketQua.add(doc.doc(rs));
            }
        }
        return ketQua;
    }

    private static void datThamSo(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof LocalDate) ps.setDate(i + 1, Date.valueOf((LocalDate) arg));
            else if (arg instanceof LocalTime) ps.setTime(i + 1, Time.valueOf((LocalTime) arg));
            else ps.setObject(i + 1, arg);
        }
    }

    private static String chuoi(ResultSet rs, String key) throws SQLException {
        String value = rs.getString(key);
        return value == null ? "" : value;
    }

    private static void batBuoc(String value, String message) throws SQLException {
        if (value == null || value.trim().isEmpty()) throw new SQLException(message);
    }

    /** Đọc quyền hiện hành từ VaiTroQuyen mỗi lần trước khi ghi. */
    private static void canQuyen(Connection c, int actor, String maQuyen) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON t.maVaiTro=q.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro<>N'Quản trị viên' AND q.maQuyen=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);
            ps.setString(2, maQuyen);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Bạn không có quyền " + maQuyen + ".");
            }
        }
    }

    /** Lưu hành động và ID người thực hiện vào nhật ký hệ thống. */
    private static void ghi(Connection c, int actor, String hanhDong, String doiTuong, String moTa)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            ps.setString(1, hanhDong);
            ps.setString(2, doiTuong);
            ps.setString(3, "maTK=" + actor + " | " + moTa);
            ps.executeUpdate();
        }
    }

    public int dem(String table) throws SQLException {
        // Không chấp nhận tên bảng tùy ý từ người dùng: chỉ tên cố định.
        if (!java.util.Arrays.asList("BenhNhan", "LichHen", "NhanVien", "DichVu", "HoaDon",
                "PhieuKham", "YeuCauHoTro").contains(table)) {
            throw new IllegalArgumentException("Bảng không được cho phép.");
        }
        try (Connection c = KetNoi.mo(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM dbo." + table)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public List<Hang> benhNhan() throws SQLException {
        return truyVan("SELECT maBN,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(gioiTinh,'') AS gioiTinh, "
                + "CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,ISNULL(email,'') AS email, "
                + "ISNULL(trangThai,'') AS trangThai FROM dbo.BenhNhan ORDER BY maBN DESC",
                r -> new Hang(r.getInt("maBN"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"gioiTinh"), chuoi(r,"ngaySinh"), chuoi(r,"email"),
                        chuoi(r,"trangThai")));
    }

    public void luuBenhNhan(int id, String hoTen, String sdt, LocalDate ngaySinh,
            String gioiTinh, String email, String diaChi, String tienSu, String trangThai,
            int actor) throws SQLException {
        batBuoc(hoTen, "Họ tên bệnh nhân không được trống.");
        if (sdt == null || !sdt.matches("[0-9+ -]{9,16}"))
            throw new SQLException("Số điện thoại phải từ 9-16 ký tự hợp lệ.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "BENHNHAN");
            String sql = id == 0
                ? "INSERT dbo.BenhNhan(hoTen,sdt,ngaySinh,gioiTinh,email,diaChi,tienSuBenh,trangThai) VALUES(?,?,?,?,?,?,?,?)"
                : "UPDATE dbo.BenhNhan SET hoTen=?,sdt=?,ngaySinh=?,gioiTinh=?,email=?,diaChi=?,tienSuBenh=?,trangThai=? WHERE maBN=?";
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                datThamSo(ps, hoTen.trim(), sdt.trim(), ngaySinh, gioiTinh, email, diaChi, tienSu, trangThai);
                if (ngaySinh == null) ps.setNull(3, java.sql.Types.DATE);
                if (id != 0) ps.setInt(9, id);
                if (ps.executeUpdate() != 1) throw new SQLException("Không thể lưu bệnh nhân.");
                ghi(c, actor, id == 0 ? "Thêm bệnh nhân" : "Sửa bệnh nhân", "BenhNhan", hoTen);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Khi sửa, nạp đầy đủ thông tin để không xóa nhầm địa chỉ / tiền sử bệnh. */
    public String[] chiTietBenhNhan(int maBN) throws SQLException {
        try (Connection c=KetNoi.mo(); PreparedStatement ps=c.prepareStatement(
                "SELECT ISNULL(hoTen,'') AS hoTen,ISNULL(sdt,'') AS sdt,"
              + "ISNULL(gioiTinh,'') AS gioiTinh,CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,"
              + "ISNULL(email,'') AS email,ISNULL(diaChi,'') AS diaChi,"
              + "ISNULL(tienSuBenh,'') AS tienSuBenh,ISNULL(trangThai,'') AS trangThai "
              + "FROM dbo.BenhNhan WHERE maBN=?")) {
            ps.setInt(1,maBN);
            try(ResultSet r=ps.executeQuery()) {
                if(!r.next()) throw new SQLException("Không tìm thấy bệnh nhân.");
                return new String[]{chuoi(r,"hoTen"),chuoi(r,"sdt"),chuoi(r,"gioiTinh"),
                        chuoi(r,"ngaySinh"),chuoi(r,"email"),chuoi(r,"diaChi"),
                        chuoi(r,"tienSuBenh"),chuoi(r,"trangThai")};
            }
        }
    }

    /** Xóa hồ sơ chỉ khi không có ràng buộc nghiệp vụ liên quan. */
    public void xoaBenhNhan(int id, int actor) throws SQLException {
        xoaAnToan("BENHNHAN", "dbo.BenhNhan", "maBN", id, actor, "Bệnh nhân");
    }

    public List<Hang> nhanVien() throws SQLException {
        return truyVan("SELECT maNV,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(email,'') AS email, "
                + "ISNULL(chucVu,'') AS chucVu,ISNULL(trangThai,'') AS trangThai "
                + "FROM dbo.NhanVien ORDER BY maNV DESC",
                r -> new Hang(r.getInt("maNV"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"email"), chuoi(r,"chucVu"), chuoi(r,"trangThai")));
    }

    /** Nhân viên mới không tự tạo tài khoản: Admin cấp tài khoản ở phần Tài khoản. */
    /**
     * Nhân viên liên kết tài khoản không được đổi vai trò qua form vận hành:
     * Admin cần cấp quyền tại Quản lý tài khoản / Phân quyền.
     */
    public void luuNhanVien(int id, String ten, String sdt, String email, String chucVu,
            String trangThai, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên nhân viên.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {
                if (id != 0) {
                    String check = "SELECT v.tenVaiTro FROM dbo.NhanVien n "
                            + "JOIN dbo.TaiKhoan t ON n.maTK=t.maTK "
                            + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE n.maNV=?";
                    try(PreparedStatement ps=c.prepareStatement(check)) {
                        ps.setInt(1,id);
                        try(ResultSet r=ps.executeQuery()) {
                            if(r.next() && !r.getString(1).equals(chucVu))
                                throw new SQLException("Nhân viên đã có tài khoản. Vai trò phải đổi tại trang Tài khoản của Admin.");
                        }
                    }
                }
                String sql = id == 0
                    ? "INSERT dbo.NhanVien(hoTen,sdt,email,chucVu,trangThai) VALUES(?,?,?,?,?)"
                    : "UPDATE dbo.NhanVien SET hoTen=?,sdt=?,email=?,chucVu=?,trangThai=? WHERE maNV=?";
                try (PreparedStatement ps=c.prepareStatement(sql)) {
                    datThamSo(ps,ten,sdt,email,chucVu,trangThai);
                    if(id!=0)ps.setInt(6,id);
                    if(ps.executeUpdate()!=1)throw new SQLException("Không thể lưu nhân viên.");
                }
                ghi(c, actor, id == 0 ? "Thêm nhân viên" : "Sửa nhân viên", "NhanVien", ten);
                c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Nhân viên đã có tài khoản/lịch khám có thể bị FK ngăn xóa. */
    public void xoaNhanVien(int id, int actor) throws SQLException {
        xoaAnToan("NHANVIEN", "dbo.NhanVien", "maNV", id, actor, "Nhân viên");
    }

    public List<Hang> dichVu() throws SQLException {
        return truyVan("SELECT maDV,tenDV,CAST(donGia AS DECIMAL(18,0)) AS donGia, "
                + "ISNULL(trangThai,'') AS trangThai,ISNULL(moTa,'') AS moTa "
                + "FROM dbo.DichVu ORDER BY maDV DESC",
                r -> new Hang(r.getInt("maDV"), chuoi(r,"tenDV"),
                        chuoi(r,"donGia"), chuoi(r,"trangThai"), chuoi(r,"moTa")));
    }

    /** Thay đổi giá sẽ ghi thêm lịch sử BangGiaDichVu trong cùng transaction. */
    public void luuDichVu(int id, String ten, BigDecimal gia, String trangThai,
            String moTa, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên dịch vụ.");
        if(gia==null || gia.signum()<0)throw new SQLException("Giá dịch vụ không hợp lệ.");
        try(Connection c=KetNoi.mo()) {
            canQuyen(c,actor,"DICHVU");
            c.setAutoCommit(false);
            try {
                int maDV=id;
                if(id==0) {
                    try(PreparedStatement ps=c.prepareStatement(
                            "INSERT dbo.DichVu(tenDV,donGia,trangThai,moTa) VALUES(?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        datThamSo(ps,ten,gia,trangThai,moTa);
                        ps.executeUpdate();
                        try(ResultSet keys=ps.getGeneratedKeys()) {
                            if(!keys.next())throw new SQLException("Không có mã dịch vụ mới.");
                            maDV=keys.getInt(1);
                        }
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement(
                            "UPDATE dbo.DichVu SET tenDV=?,donGia=?,trangThai=?,moTa=? WHERE maDV=?")) {
                        datThamSo(ps,ten,gia,trangThai,moTa,id);
                        if(ps.executeUpdate()!=1)throw new SQLException("Không tìm thấy dịch vụ.");
                    }
                }
                // Mỗi lần đổi giá lưu lịch sử áp dụng để không mất số liệu cũ.
                try(PreparedStatement ps=c.prepareStatement(
                        "INSERT dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai) "
                        + "VALUES(?,CAST(GETDATE() AS date),?,?)")) {
                    datThamSo(ps,maDV,gia,trangThai);
                    ps.executeUpdate();
                }
                ghi(c,actor,id==0?"Thêm dịch vụ":"Sửa dịch vụ","DichVu",ten);
                c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    public void xoaDichVu(int id, int actor) throws SQLException {
        xoaAnToan("DICHVU", "dbo.DichVu", "maDV", id, actor, "Dịch vụ");
    }

    /** Các tên bảng/cột được cố định ở code, không ghép dữ liệu người dùng vào SQL. */
    private void xoaAnToan(String quyen, String bang, String cot, int id,
                           int actor, String doiTuong) throws SQLException {
        try(Connection c=KetNoi.mo()) {
            canQuyen(c,actor,quyen);
            c.setAutoCommit(false);
            try {
                try(PreparedStatement ps=c.prepareStatement(
                        "DELETE FROM " + bang + " WHERE " + cot + "=?")) {
                    ps.setInt(1,id);
                    if(ps.executeUpdate()!=1)throw new SQLException("Bản ghi không tồn tại.");
                }
                ghi(c,actor,"Xóa "+doiTuong,bang,cot+"="+id);
                c.commit();
            }catch(SQLException ex) {
                c.rollback();
                if(ex.getErrorCode()==547)
                    throw new SQLException("Đã có dữ liệu liên quan. Không xóa để tránh mất lịch sử; hãy cập nhật trạng thái.",ex);
                throw ex;
            }finally{c.setAutoCommit(true);}
        }
    }

    public List<LuaChon> luaChon(String bang) throws SQLException {
        // Toàn bộ tên bảng/cột đến từ whitelist; dữ liệu người dùng chỉ là lựa chọn ID.
        String sql;
        switch (bang) {
            case "BenhNhan": sql = "SELECT maBN,hoTen FROM dbo.BenhNhan ORDER BY hoTen"; break;
            case "NhanVien": sql = "SELECT maNV,hoTen FROM dbo.NhanVien "
                    + "WHERE trangThai=N'Đang làm việc' AND chucVu=N'Bác sĩ' ORDER BY hoTen"; break;
            case "DichVu": sql = "SELECT maDV,tenDV FROM dbo.DichVu WHERE trangThai<>N'Ngừng áp dụng' ORDER BY tenDV"; break;
            case "PhieuKham": sql = "SELECT p.maPK, N'PK #'+CONVERT(nvarchar(20),p.maPK)+N' - '+b.hoTen "
                    + "FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                    + "ORDER BY p.maPK DESC"; break;
            default: throw new IllegalArgumentException("Danh mục không hợp lệ: " + bang);
        }
        List<LuaChon> out = new ArrayList<>();
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) out.add(new LuaChon(r.getInt(1),r.getString(2)));
        }
        return out;
    }

    public List<Hang> lichHen() throws SQLException {
        return truyVan("SELECT l.maLichHen,CONVERT(varchar(10),l.ngayHen,103) AS ngay, "
                + "CONVERT(varchar(5),l.gioHen,108) AS gio,b.hoTen AS benhNhan, "
                + "ISNULL(n.hoTen,'Chưa phân công') AS bacSi,ISNULL(l.phongKham,'') AS phongKham, "
                + "ISNULL(l.lyDo,'') AS lyDo,l.trangThai FROM dbo.LichHen l "
                + "JOIN dbo.BenhNhan b ON l.maBN=b.maBN "
                + "LEFT JOIN dbo.NhanVien n ON l.maNV=n.maNV "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC",
                r -> new Hang(r.getInt("maLichHen"),chuoi(r,"ngay"),chuoi(r,"gio"),
                        chuoi(r,"benhNhan"),chuoi(r,"bacSi"),chuoi(r,"phongKham"),
                        chuoi(r,"lyDo"),chuoi(r,"trangThai")));
    }

    /** 8 lịch gần đây cho Dashboard Quản lý, dữ liệu SQL thực. */
    public List<Hang> lichHenGanDay() throws SQLException {
        return truyVan("SELECT TOP 8 l.maLichHen,CONVERT(varchar(10),l.ngayHen,103) AS ngay, "
                + "CONVERT(varchar(5),l.gioHen,108) AS gio,b.hoTen,l.trangThai "
                + "FROM dbo.LichHen l JOIN dbo.BenhNhan b ON l.maBN=b.maBN "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC",
                r->new Hang(r.getInt(1),chuoi(r,"ngay"),chuoi(r,"gio"),
                        r.getString(4),chuoi(r,"trangThai")));
    }

    /** Chặn trùng bác sĩ cùng một ngày/giờ bằng khóa giao dịch. */
    public void taoLichHen(int benhNhan, Integer nhanVien, LocalDate ngay, LocalTime gio,
            String phong, String lyDo, int actor) throws SQLException {
        if (benhNhan <= 0 || ngay == null || gio == null) throw new SQLException("Thiếu thông tin lịch hẹn.");
        if (ngay.isBefore(LocalDate.now())) throw new SQLException("Ngày hẹn không được ở quá khứ.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c,actor,"LICHHEN");
            c.setAutoCommit(false);
            try {
                if (nhanVien != null) {
                    try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                            + "WHERE maNV=? AND ngayHen=? AND gioHen=? AND trangThai<>N'Đã hủy'")) {
                        datThamSo(ps,nhanVien,ngay,gio);
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next() && r.getInt(1) > 0) throw new SQLException("Bác sĩ đã có lịch ở giờ này.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT dbo.LichHen(" 
                        + "maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai) VALUES(?,?,?,?,?,?,N'Chờ xác nhận')")) {
                    ps.setInt(1,benhNhan);
                    if (nhanVien == null) ps.setNull(2,java.sql.Types.INTEGER);
                    else ps.setInt(2,nhanVien);
                    datThamSoTail(ps,3,phong,ngay,gio,lyDo);
                    ps.executeUpdate();
                }
                ghi(c,actor,"Tạo lịch hẹn","LichHen","maBN="+benhNhan);
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    private static void datThamSoTail(PreparedStatement ps, int from, Object... args) throws SQLException {
        for (int i=0;i<args.length;i++) {
            Object arg=args[i];
            if (arg instanceof LocalDate) ps.setDate(i+from,Date.valueOf((LocalDate)arg));
            else if(arg instanceof LocalTime) ps.setTime(i+from,Time.valueOf((LocalTime)arg));
            else ps.setObject(i+from,arg);
        }
    }

    public void trangThaiLichHen(int id, String status, int actor) throws SQLException {
        if (!java.util.Arrays.asList("Chờ xác nhận","Đã xác nhận","Đang khám","Hoàn tất","Đã hủy").contains(status))
            throw new SQLException("Trạng thái lịch hẹn không hợp lệ.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c,actor,"LICHHEN");
            try (PreparedStatement ps = c.prepareStatement("UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=?")) {
                datThamSo(ps,status,id);
                if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy lịch hẹn.");
            }
            ghi(c,actor,"Cập nhật lịch hẹn","LichHen","maLichHen="+id+" -> "+status);
        }
    }

    public List<Hang> hoaDon() throws SQLException {
        return truyVan("SELECT h.maHD,b.hoTen,CONVERT(varchar(19),h.ngayLap,120) AS ngay, "
                + "CONVERT(varchar(35),CAST(h.tongTien AS DECIMAL(18,0))) AS tien, "
                + "ISNULL(t.phuongThuc,'') AS phuongThuc,h.trangThai, "
                + "CONVERT(varchar(20),t.maTT) AS maTT FROM dbo.HoaDon h "
                + "JOIN dbo.PhieuKham p ON h.maPK=p.maPK JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                + "JOIN dbo.ThanhToan t ON h.maTT=t.maTT ORDER BY h.maHD DESC",
                r -> new Hang(r.getInt("maHD"),chuoi(r,"hoTen"),chuoi(r,"ngay"),
                        chuoi(r,"tien"),chuoi(r,"phuongThuc"),chuoi(r,"trangThai"),chuoi(r,"maTT")));
    }

    /** Giá trị từ ChiTietDieuTri để không nhập tùy ý số tiền thanh toán. */
    public BigDecimal soTienPhieuKham(int maPK) throws SQLException {
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WHERE maPK=?")) {
            ps.setInt(1,maPK);
            try (ResultSet r = ps.executeQuery()) { return r.next()?r.getBigDecimal(1):BigDecimal.ZERO; }
        }
    }

    public void taoThanhToan(int maPK, String phuongThuc, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            c.setAutoCommit(false);
            try {
                BigDecimal soTien;
                try (PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WHERE maPK=?")) {
                    ps.setInt(1,maPK);
                    try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new SQLException("Không có phiếu khám."); soTien=rs.getBigDecimal(1); }
                }
                if(soTien.signum()<=0) throw new SQLException("Phiếu khám chưa có chi tiết điều trị hoặc số tiền bằng 0.");
                try (PreparedStatement ps=c.prepareStatement("SELECT COUNT(*) FROM dbo.ThanhToan WITH(UPDLOCK,HOLDLOCK) WHERE maPK=? AND trangThai IN(N'Thành công',N'Đã thanh toán')")) {
                    ps.setInt(1,maPK);
                    try(ResultSet rs=ps.executeQuery()) { if(rs.next()&&rs.getInt(1)>0) throw new SQLException("Phiếu khám đã được thanh toán."); }
                }
                int maTT;
                try (PreparedStatement ps=c.prepareStatement("INSERT dbo.ThanhToan(maPK,soTien,phuongThuc,trangThai) "
                        + "VALUES(?,?,?,N'Đã thanh toán')",Statement.RETURN_GENERATED_KEYS)) {
                    datThamSo(ps,maPK,soTien,phuongThuc);
                    ps.executeUpdate();
                    try(ResultSet k=ps.getGeneratedKeys()) { if(!k.next())throw new SQLException("Không lấy được mã thanh toán.");maTT=k.getInt(1); }
                }
                try (PreparedStatement ps=c.prepareStatement("INSERT dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,N'Đã thanh toán')")) {
                    datThamSo(ps,maTT,maPK,soTien);
                    ps.executeUpdate();
                }
                ghi(c,actor,"Thanh toán","ThanhToan","maPK="+maPK+", maTT="+maTT);
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Quản lý/Lễ tân gửi yêu cầu, Admin quyết định hủy. */
    public void yeuCauHuy(int maTT, String lyDo, int actor) throws SQLException {
        batBuoc(lyDo,"Phải nhập lý do yêu cầu hủy.");
        try(Connection c=KetNoi.mo()) {
            canQuyen(c,actor,"THANHTOAN");
            try(PreparedStatement ps=c.prepareStatement("INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai) VALUES(?,?,N'Chờ duyệt')")) {
                datThamSo(ps,maTT,lyDo);
                ps.executeUpdate();
            }
            ghi(c,actor,"Yêu cầu hủy phiếu thu","ThanhToan","maTT="+maTT);
        }
    }

    public List<Hang> yeuCauHuyDangCho() throws SQLException {
        return truyVan("SELECT y.maYC,CONVERT(varchar(19),y.ngayYeuCau,120) AS ngay, "
                + "CONVERT(varchar(20),y.maTT) AS maTT,y.lyDo,y.trangThai "
                + "FROM dbo.YeuCauHuyPhieuThu y ORDER BY y.maYC DESC",
                r->new Hang(r.getInt("maYC"),chuoi(r,"ngay"),chuoi(r,"maTT"),chuoi(r,"lyDo"),chuoi(r,"trangThai")));
    }

    public void duyetYeuCauHuy(int maYC, boolean chapThuan, String note, int actor) throws SQLException {
        try(Connection c=KetNoi.mo()) {
            // Duyệt hủy thuộc quyền quản trị viên; không tự cấp cho lễ tân/quản lý.
            if(!laAdmin(c,actor)) throw new SQLException("Chỉ Quản trị viên được duyệt hủy phiếu thu.");
            c.setAutoCommit(false);
            try {
                int maTT;
                try(PreparedStatement ps=c.prepareStatement("SELECT maTT FROM dbo.YeuCauHuyPhieuThu WITH(UPDLOCK) WHERE maYC=? AND trangThai=N'Chờ duyệt'")) {
                    ps.setInt(1,maYC);
                    try(ResultSet r=ps.executeQuery()) { if(!r.next())throw new SQLException("Yêu cầu không còn chờ duyệt.");maTT=r.getInt(1); }
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE dbo.YeuCauHuyPhieuThu SET trangThai=? WHERE maYC=?")) {
                    datThamSo(ps,chapThuan?"Đã duyệt":"Từ chối",maYC);ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet) VALUES(?,SYSDATETIME(),?)")) {
                    datThamSo(ps,maYC,note);ps.executeUpdate();
                }
                if(chapThuan) {
                    try(PreparedStatement ps=c.prepareStatement("UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=?")){ps.setInt(1,maTT);ps.executeUpdate();}
                    try(PreparedStatement ps=c.prepareStatement("UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maTT=?")){ps.setInt(1,maTT);ps.executeUpdate();}
                }
                ghi(c,actor,chapThuan?"Duyệt hủy phiếu thu":"Từ chối hủy phiếu thu","ThanhToan","maYC="+maYC);
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    private boolean laAdmin(Connection c,int actor) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'")) {
            ps.setInt(1,actor);
            try(ResultSet r=ps.executeQuery()) { return r.next(); }
        }
    }

    public void guiHoTro(int actor,String tieuDe,String noiDung) throws SQLException {
        batBuoc(tieuDe,"Nhập tiêu đề yêu cầu."); batBuoc(noiDung,"Nhập nội dung cần hỗ trợ.");
        try(Connection c=KetNoi.mo()) {
            try(PreparedStatement valid=c.prepareStatement(
                    "SELECT 1 FROM dbo.TaiKhoan WHERE maTK=? AND trangThai=N'Hoạt động'")) {
                valid.setInt(1,actor);
                try(ResultSet r=valid.executeQuery()) {
                    if(!r.next()) throw new SQLException("Tài khoản đã bị khóa hoặc đăng xuất.");
                }
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT dbo.YeuCauHoTro(maTK,tieuDe,noiDung) VALUES(?,?,?)")) {
                datThamSo(ps,actor,tieuDe,noiDung);ps.executeUpdate();
            }
            ghi(c,actor,"Gửi yêu cầu hỗ trợ","YeuCauHoTro",tieuDe);
        }
    }

    private boolean duocXemHoTro(Connection c,int actor) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement(
                "SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTroQuyen vq "
               + "ON t.maVaiTro=vq.maVaiTro "
               + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND vq.maQuyen='HOTRO'")) {
            ps.setInt(1,actor);
            try(ResultSet r=ps.executeQuery()) { return r.next(); }
        }
    }

    public List<Hang> danhSachHoTro(int actor) throws SQLException {
        try(Connection c=KetNoi.mo()) {
            boolean laAdmin=laAdmin(c,actor) && duocXemHoTro(c,actor);
            String sql="SELECT y.maYC,ISNULL(n.hoTen,t.tenDangNhap) AS nguoi, "
                    + "CONVERT(varchar(19),y.ngayTao,120) AS ngay,y.tieuDe,y.trangThai, "
                    + "ISNULL(y.noiDung,'') AS noiDung,ISNULL(y.phanHoi,'') AS phanHoi FROM dbo.YeuCauHoTro y "
                    + "JOIN dbo.TaiKhoan t ON y.maTK=t.maTK "
                    + "LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                    + (laAdmin ? "" : "WHERE y.maTK=? ") + "ORDER BY y.maYC DESC";
            List<Hang> rows=new ArrayList<>();
            try(PreparedStatement ps=c.prepareStatement(sql)) {
                if(!laAdmin) ps.setInt(1,actor);
                try(ResultSet r=ps.executeQuery()) {
                    while(r.next()) rows.add(new Hang(r.getInt("maYC"),chuoi(r,"nguoi"),chuoi(r,"ngay"),
                            chuoi(r,"tieuDe"),chuoi(r,"trangThai"),chuoi(r,"noiDung"),chuoi(r,"phanHoi")));
                }
            }
            return rows;
        }
    }

    public void phanHoiHoTro(int maYC,String phanHoi,int actor) throws SQLException {
        batBuoc(phanHoi,"Nhập nội dung phản hồi.");
        try(Connection c=KetNoi.mo()) {
            canQuyen(c,actor,"HOTRO");
            if(!laAdmin(c,actor)) throw new SQLException("Chỉ Admin xử lý yêu cầu hỗ trợ.");
            try(PreparedStatement ps=c.prepareStatement("UPDATE dbo.YeuCauHoTro "
                    + "SET phanHoi=?,trangThai=N'Đã xử lý',ngayXuLy=SYSDATETIME() WHERE maYC=?")) {
                datThamSo(ps,phanHoi,maYC);
                if(ps.executeUpdate()!=1) throw new SQLException("Không còn yêu cầu hỗ trợ.");
            }
            ghi(c,actor,"Phản hồi yêu cầu hỗ trợ","YeuCauHoTro","maYC="+maYC);
        }
    }
}
