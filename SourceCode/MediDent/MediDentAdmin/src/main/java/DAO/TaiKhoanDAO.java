package DAO;

import Class.TaiKhoan;
import Class.VaiTro;
import Enum.VaiTroHeThong;
import TienIch.EmailHopLe;
import TienIch.MatKhauHopLe;
import ConnectDB.ConnectDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

/** Truy vấn đăng nhập, tài khoản, quyền và lịch sử đăng nhập. */
public class TaiKhoanDAO {
    private static final String[] QUYEN = {
        "TAIKHOAN", "PHANQUYEN", "HOTRO",
        "QL_NHANVIEN", "QL_DICHVU", "QL_BENHNHAN", "QL_LICHHEN", "QL_THANHTOAN", "QL_BAOCAO",
        "LT_KHAM", "LT_TIEPNHAN", "LT_LICHHEN", "LT_THANHTOAN", "LT_TRACUU"
    };
    private static final String[] TEN_QUYEN = {
        "Quản lý tài khoản", "Phân quyền tài khoản", "Hỗ trợ",
        "Quản lý nhân viên", "Quản lý dịch vụ", "Quản lý bệnh nhân", "Lịch hẹn (Quản lý)",
        "Thanh toán (Quản lý)", "Báo cáo (Quản lý)",
        "Khám và điều trị", "Tiếp nhận bệnh nhân", "Lịch hẹn (Lễ tân)",
        "Thanh toán (Lễ tân)", "Tra cứu thông tin"
    };
    private static final String COT_TAI_KHOAN =
        "t.maTK,t.tenDangNhap,COALESCE(n.hoTen,t.tenDangNhap) AS HoTen," +
        "COALESCE(n.email,'') AS Email,t.maVaiTro,v.tenVaiTro AS TenVaiTro," +
        "t.trangThai AS TrangThai,t.phaiDoiMatKhau AS PhaiDoiMatKhau," +
        "CONVERT(varchar(19),t.ngayTao,120) AS NgayTao ";
    private static final String JOIN_TAI_KHOAN =
        " FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro" +
        " LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK ";
    public static String[] cacQuyen() { return QUYEN.clone(); }
    public static String[] tenCacQuyen() { return TEN_QUYEN.clone(); }

    public TaiKhoan dangNhap(String username, String password) throws SQLException {
        String sql="SELECT "+COT_TAI_KHOAN+",t.matKhau "+JOIN_TAI_KHOAN+
                   " WHERE t.tenDangNhap=? AND n.trangThai=N'Đang làm việc' " +
                   "AND v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')";
        try(Connection c=ConnectDB.mo()) {
            TaiKhoan result=null;
            try(PreparedStatement s=c.prepareStatement(sql)) {
                s.setString(1,username);
                try(ResultSet r=s.executeQuery()) {
                    if(r.next() && "Hoạt động".equals(r.getString("TrangThai"))) {
                        String hash=r.getString("matKhau");
                        boolean valid=false;
                        try {valid=hash!=null&&BCrypt.checkpw(password,hash);}
                        catch(IllegalArgumentException ignored){/* Hash sai dinh dang */}
                        if(valid)result=docTK(r);
                    }
                }
            }
            if(result==null) {
                // Khong ghi mat khau vao nhat ky.
                ghiDangNhap(c, username, null);
                return null;
            }
            try(PreparedStatement s=c.prepareStatement(
                    "UPDATE dbo.TaiKhoan SET ngayDangNhapCuoi=SYSDATETIME() WHERE maTK=?")){
                s.setInt(1,result.id);s.executeUpdate();
            }
            ghiDangNhap(c, username, result);
            return result;
        }
    }

    // Lịch sử đăng nhập không ghi mật khẩu.
    private void ghiDangNhap(Connection c, String tenDangNhap, TaiKhoan user) throws SQLException {
        String sql = "INSERT INTO dbo.LichSuDangNhap(maTK,tenDangNhap,tenVaiTro,ketQua) "
                   + "VALUES(?,?,?,?)";
        Integer id = user == null ? null : user.id;
        String vaiTro = user == null ? null : user.tenVaiTro;
        if (user == null) {
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT t.maTK,v.tenVaiTro FROM dbo.TaiKhoan t "
                  + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro WHERE t.tenDangNhap=?")) {
                p.setString(1, tenDangNhap);
                try (ResultSet r = p.executeQuery()) {
                    if (r.next()) { id = r.getInt(1); vaiTro = r.getString(2); }
                }
            }
        }
        try (PreparedStatement p = c.prepareStatement(sql)) {
            if (id == null) p.setNull(1, Types.INTEGER); else p.setInt(1, id);
            p.setString(2, tenDangNhap);
            p.setString(3, vaiTro);
            p.setString(4, user == null ? "Thất bại" : "Thành công");
            p.executeUpdate();
        }
    }

    /** Tự cấp mật khẩu tạm khi xác nhận đúng tên đăng nhập và ngày sinh. */
    public String tuDatLaiMatKhau(String tenDangNhap, LocalDate ngaySinh) throws SQLException {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty() || ngaySinh == null)
            return null;
        try (Connection c = ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                int maTK;
                String hoTen;
                String sql = "SELECT t.maTK,n.hoTen FROM dbo.TaiKhoan t WITH (UPDLOCK,HOLDLOCK) "
                        + "JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                        + "WHERE t.tenDangNhap=? AND n.ngaySinh=? "
                        + "AND t.trangThai=N'Hoạt động' AND n.trangThai=N'Đang làm việc'";
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    p.setString(1, tenDangNhap.trim());
                    p.setDate(2, java.sql.Date.valueOf(ngaySinh));
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) { c.rollback(); return null; }
                        maTK = r.getInt(1);
                        hoTen = r.getString(2);
                    }
                }
                String matKhauTam = taoMatKhauMacDinh(hoTen, ngaySinh);
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE dbo.TaiKhoan SET matKhau=?,phaiDoiMatKhau=1 WHERE maTK=?")) {
                    p.setString(1, BCrypt.hashpw(matKhauTam, BCrypt.gensalt(11)));
                    p.setInt(2, maTK);
                    if (p.executeUpdate() != 1) throw new SQLException("Không đặt lại được mật khẩu.");
                }
                ghi(c, maTK, "Tự đặt lại mật khẩu", "TaiKhoan", "maTK=" + maTK);
                c.commit();
                return matKhauTam;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Dùng ở Dashboard quản trị viên. */
    public List<String[]> lichSuDangNhap(int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT TOP 100 CONVERT(varchar(19),thoiGian,120) AS gio, "
                + "tenDangNhap,ISNULL(tenVaiTro,N'Không xác định') AS vaiTro,ketQua "
                + "FROM dbo.LichSuDangNhap ORDER BY thoiGian DESC,maLichSu DESC";
        try (Connection c = ConnectDB.mo(); PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) rows.add(new String[]{r.getString(1),r.getString(2),
                    r.getString(3),r.getString(4)});
        }
        return rows;
    }

    /** Dashboard: tổng, đang hoạt động, tạm khóa và số vai trò. */
    public int[] thongKeTaiKhoan(int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        String sql = "SELECT COUNT(*), "
                + "COUNT(CASE WHEN t.trangThai=N'Hoạt động' THEN 1 END), "
                + "COUNT(CASE WHEN t.trangThai=N'Tạm khóa' THEN 1 END) "
                + "FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')";
        try (Connection c = ConnectDB.mo();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            if (r.next()) return new int[]{r.getInt(1), r.getInt(2), r.getInt(3), 3};
        }
        return new int[4];
    }

    private TaiKhoan docTK(ResultSet r) throws SQLException {
        TaiKhoan t = new TaiKhoan(r.getInt("maTK"),r.getString("tenDangNhap"),
            r.getString("HoTen"),r.getString("Email"),r.getInt("maVaiTro"),
            r.getString("TenVaiTro"),r.getString("TrangThai"),r.getBoolean("PhaiDoiMatKhau"));
        t.ngayTao = r.getString("NgayTao");
        return t;
    }

    public List<TaiKhoan> taiKhoan(String keyword,int role,String status,int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN"); // Chỉ Admin được đọc danh sách tài khoản.
        List<TaiKhoan> ds=new ArrayList<>();
        String sql="SELECT "+COT_TAI_KHOAN+JOIN_TAI_KHOAN+
            " WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')"+
            " AND (n.hoTen LIKE ? OR n.email LIKE ? OR t.tenDangNhap LIKE ?)"+
            " AND (?=0 OR t.maVaiTro=?) AND (?='' OR t.trangThai=?) ORDER BY t.maTK DESC";
        try(Connection c=ConnectDB.mo();PreparedStatement s=c.prepareStatement(sql)) {
            String term="%"+keyword+"%";
            for(int i=1;i<=3;i++)s.setString(i,term);
            s.setInt(4,role);s.setInt(5,role);s.setString(6,status);s.setString(7,status);
            try(ResultSet r=s.executeQuery()) { while(r.next())ds.add(docTK(r)); }
        }
        return ds;
    }

    public List<VaiTro> vaiTro() throws SQLException {
        List<VaiTro> ds=new ArrayList<>();
        String sql="SELECT v.maVaiTro,v.tenVaiTro,v.moTa,q.maQuyen"+
            " FROM dbo.VaiTro v LEFT JOIN dbo.VaiTroQuyen vq ON v.maVaiTro=vq.maVaiTro"+
            " LEFT JOIN dbo.Quyen q ON vq.maQuyen=q.maQuyen"+
            " WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân') ORDER BY v.maVaiTro";
        try(Connection c=ConnectDB.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()) {
            VaiTro old=null;
            while(r.next()) {
                int id=r.getInt(1);
                if(old==null||old.id!=id) {
                    old=new VaiTro(id,r.getString(2),r.getString(3),new boolean[QUYEN.length]);
                    ds.add(old);
                }
                String q=r.getString(4);
                for(int i=0;i<QUYEN.length;i++)if(QUYEN[i].equals(q))old.quyen[i]=true;
            }
        }
        return ds;
    }

    public boolean laQuanTriVien(int role) throws SQLException {
        try(Connection c=ConnectDB.mo();PreparedStatement s=c.prepareStatement(
                "SELECT 1 FROM dbo.VaiTro WHERE maVaiTro=? AND tenVaiTro=N'Quản trị viên'")) {
            s.setInt(1,role);try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public boolean coQuyenTaiKhoan(int maTK,String maQuyen) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTroQuyen q" +
            " ON t.maVaiTro=q.maVaiTro JOIN dbo.NhanVien n ON n.maTK=t.maTK " +
            "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' " +
            "AND n.trangThai=N'Đang làm việc' AND q.maQuyen=?";
        try(Connection c=ConnectDB.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);s.setString(2,maQuyen);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public boolean laQuanTriVienTaiKhoan(int maTK) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro " +
            "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên' " +
            "AND EXISTS (SELECT 1 FROM dbo.NhanVien n WHERE n.maTK=t.maTK AND n.trangThai=N'Đang làm việc')";
        try(Connection c=ConnectDB.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public int duKienMaTaiKhoan(int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        String sql =
                "SELECT CASE WHEN last_value IS NULL " +
                "THEN CONVERT(bigint, seed_value) ELSE CONVERT(bigint, last_value) + " +
                "CONVERT(bigint, increment_value) END " +
                "FROM sys.identity_columns " +
                "WHERE object_id = OBJECT_ID(N'dbo.TaiKhoan') " +
                "AND name = N'maTK'";
        try (Connection c = ConnectDB.mo();
             PreparedStatement statement = c.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Không tìm thấy cột IDENTITY dbo.TaiKhoan.maTK.");
            }
            long maDuKien = rs.getLong(1);
            if (maDuKien < 1 || maDuKien > Integer.MAX_VALUE) {
                throw new SQLException("Mã tài khoản dự kiến vượt giới hạn INT.");
            }
            return (int) maDuKien;
        }
    }

    public static String tenDangNhapTheoMa(int maTaiKhoan) {
        return "nv" + maTaiKhoan;
    }

    public int luuTaiKhoanTraVeMa(int id, String name, String email,
                                 int role, String status, String newPassword,
                                 LocalDate ngaySinh, int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        // Khi them moi, tenDangNhap tu sinh nv1, nv2, nv3...; sua thi giu nguyen.
        if(name==null||name.trim().isEmpty())
            throw new SQLException("Thiếu tên nhân viên.");
        if (!EmailHopLe.kiemTra(email))
            throw new SQLException(EmailHopLe.huongDan());
        email = email.trim();
        // Ngày sinh nhân viên phải do người quản trị xác nhận, không tự tạo ngày giả.
        if (id == 0 && ngaySinh == null)
            throw new SQLException("Hãy chọn ngày sinh thực tế của nhân viên.");
        if (ngaySinh != null && (ngaySinh.isAfter(LocalDate.now()) ||
                ngaySinh.isBefore(LocalDate.now().minusYears(100))))
            throw new SQLException("Ngày sinh nhân viên không hợp lệ.");
        if (id == 0 && !MatKhauHopLe.kiemTra(newPassword))
            throw new SQLException(MatKhauHopLe.huongDan());
        try(Connection c=ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                int accountId=id;
                if(id!=0&&id==actor){
                    // Khong cho tai khoan quan tri dang dang nhap tu khoa/tu ha quyen chinh minh.
                    try(PreparedStatement s=c.prepareStatement(
                            "SELECT maVaiTro FROM dbo.TaiKhoan WHERE maTK=?")){
                        s.setInt(1,id);try(ResultSet r=s.executeQuery()){
                            if(!r.next())throw new SQLException("Tài khoản không còn tồn tại.");
                            if(r.getInt(1)!=role||!"Hoạt động".equals(status))
                                throw new SQLException("Không thể tự đổi vai trò hoặc tự khóa tài khoản đang đăng nhập.");
                        }
                    }
                }
                if(id==0) {
                    String sql="INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,maVaiTro,trangThai,phaiDoiMatKhau) VALUES(?,?,?,?,1)";
                    try(PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                        // Dung username tam duy nhat; sau khi lay ID se doi thanh NV + 6 chu so.
                        s.setString(1,"tmp_"+UUID.randomUUID().toString().replace("-",""));
                        s.setString(2,BCrypt.hashpw(newPassword,BCrypt.gensalt(11)));
                        s.setInt(3,role);s.setString(4,status);
                        s.executeUpdate();
                        try(ResultSet keys=s.getGeneratedKeys()) {
                            if(!keys.next())throw new SQLException("Không lấy được mã tài khoản mới.");
                            accountId=keys.getInt(1);
                        }
                    }
                    try(PreparedStatement s=c.prepareStatement(
                            "UPDATE dbo.TaiKhoan SET tenDangNhap=? WHERE maTK=?")) {
                        s.setString(1,tenDangNhapTheoMa(accountId));
                        s.setInt(2,accountId);
                        s.executeUpdate();
                    }
                } else {
                    try(PreparedStatement s=c.prepareStatement(
                            "UPDATE dbo.TaiKhoan SET maVaiTro=?,trangThai=? WHERE maTK=?")) {
                        s.setInt(1,role);s.setString(2,status);s.setInt(3,id);
                        if(s.executeUpdate()!=1)throw new SQLException("Không tìm thấy tài khoản cần sửa.");
                    }
                    // Sửa thông tin không đụng đến mật khẩu; dùng nút Reset riêng.
                }

                // ChucVu duoc chon tu VaiTro, khong bat nguoi dung go thu cong.
                String tenVaiTro;
                try(PreparedStatement s=c.prepareStatement("SELECT tenVaiTro FROM dbo.VaiTro WHERE maVaiTro=?")) {
                    s.setInt(1,role);
                    try(ResultSet r=s.executeQuery()) {
                        if(!r.next())throw new SQLException("Vai trò không tồn tại.");
                        tenVaiTro=r.getString(1);
                        if (java.util.Arrays.stream(VaiTroHeThong.values())
                                .noneMatch(v -> v.ten().equals(tenVaiTro)))
                            throw new SQLException("Chỉ cho phép 3 vai trò đăng nhập: Quản trị viên, Quản lý phòng khám, Lễ tân.");
                    }
                }
                int staffId=0;
                try(PreparedStatement s=c.prepareStatement("SELECT maNV FROM dbo.NhanVien WHERE maTK=?")) {
                    s.setInt(1,accountId);
                    try(ResultSet r=s.executeQuery()){if(r.next())staffId=r.getInt(1);}
                }
                if(staffId==0) {
                    try(PreparedStatement s=c.prepareStatement(
                            "INSERT INTO dbo.NhanVien(hoTen,email,chucVu,trangThai,maTK,ngaySinh) VALUES(?,?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        s.setString(1,name);s.setString(2,email);s.setString(3,tenVaiTro);
                        // Trạng thái tài khoản đăng nhập và tình trạng nhân sự là hai khái niệm độc lập.
                        // Tài khoản có thể tạm khóa nhưng nhân viên vẫn đang làm việc.
                        s.setString(4,"Đang làm việc");
                        s.setInt(5,accountId);
                        if (ngaySinh == null) s.setNull(6,Types.DATE);
                        else s.setDate(6,java.sql.Date.valueOf(ngaySinh));
                        s.executeUpdate();
                        try(ResultSet r=s.getGeneratedKeys()){
                            if(!r.next())throw new SQLException("Không lấy được mã nhân viên mới.");
                            staffId=r.getInt(1);
                        }
                    }
                } else {
                    try(PreparedStatement s=c.prepareStatement(
                            "UPDATE dbo.NhanVien SET hoTen=?,email=?,chucVu=?,ngaySinh=COALESCE(?,ngaySinh) WHERE maNV=?")) {
                        s.setString(1,name);s.setString(2,email);s.setString(3,tenVaiTro);
                        if (ngaySinh == null) s.setNull(4,Types.DATE);
                        else s.setDate(4,java.sql.Date.valueOf(ngaySinh));
                        s.setInt(5,staffId);s.executeUpdate();
                    }
                }
                dongBoPhanLoai(c,staffId,tenVaiTro);
                ghi(c,actor,id==0?"Thêm tài khoản":"Sửa tài khoản","TaiKhoan",name+" (maTK="+accountId+")");
                c.commit();
                return accountId;
            }catch(Exception e) {
                c.rollback();
                if(e instanceof SQLException)throw(SQLException)e;
                throw new SQLException("Không thể lưu tài khoản.",e);
            }finally{c.setAutoCommit(true);}
        }
    }

    private void dongBoPhanLoai(Connection c,int maNV,String vaiTro) throws SQLException {
        String target=null;
        if("Quản trị viên".equals(vaiTro))target="QuanTriVien";
        else if("Quản lý phòng khám".equals(vaiTro))target="QuanLy";
        else if("Lễ tân".equals(vaiTro))target="LeTan";
        for(String bang:new String[]{"QuanTriVien","QuanLy","LeTan"}) {
            if(bang.equals(target))continue;
            try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo."+bang+" WHERE maNV=?")) {
                s.setInt(1,maNV);s.executeUpdate();
            }
        }
        if(target!=null) {
            String sql="IF NOT EXISTS(SELECT 1 FROM dbo."+target+" WHERE maNV=?) " +
                       "INSERT INTO dbo."+target+"(maNV) VALUES(?)";
            try(PreparedStatement s=c.prepareStatement(sql)){
                s.setInt(1,maNV);s.setInt(2,maNV);s.executeUpdate();
            }
        }
    }

    public void xoaTK(int id,int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        if(id==actor)throw new SQLException("Không thể xóa tài khoản đang đăng nhập.");
        try(Connection c=ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                Integer staffId=null;
                try(PreparedStatement s=c.prepareStatement("SELECT maNV FROM dbo.NhanVien WHERE maTK=?")) {
                    s.setInt(1,id);try(ResultSet r=s.executeQuery()){if(r.next())staffId=r.getInt(1);}
                }
                if(staffId!=null) {
                    for(String bang:new String[]{"QuanTriVien","QuanLy","LeTan"}) {
                        try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo."+bang+" WHERE maNV=?")){
                            s.setInt(1,staffId);s.executeUpdate();
                        }
                    }
                    try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo.NhanVien WHERE maNV=?")) {
                        s.setInt(1,staffId);s.executeUpdate();
                    }
                }
                try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo.TaiKhoan WHERE maTK=?")) {
                    s.setInt(1,id);
                    if(s.executeUpdate()!=1)throw new SQLException("Không tìm thấy tài khoản cần xóa.");
                }
                ghi(c,actor,"Xóa tài khoản","TaiKhoan","maTK="+id);
                c.commit();
            }catch(SQLException e) {
                c.rollback();
                if(e.getErrorCode()==547)throw new SQLException(
                    "Nhân viên đã có dữ liệu nghiệp vụ. Hãy chọn 'Tạm khóa' thay vì xóa để bảo toàn lịch sử.",e);
                throw e;
            }finally{c.setAutoCommit(true);}
        }
    }

    /** Vai trò cố định: chỉ chỉnh sửa ma trận quyền của Quản lý và Lễ tân. */
    public void luuVaiTro(int id, String ten, String moTa, boolean[] rights, int actor)
            throws SQLException {
        yeuCauQuyen(actor, "PHANQUYEN");
        if (id <= 0 || laQuanTriVien(id))
            throw new SQLException("Chỉ được chỉnh sửa quyền của Quản lý và Lễ tân.");
        if (rights == null || rights.length != QUYEN.length)
            throw new SQLException("Danh sách quyền không hợp lệ.");
        if (rights[0] || rights[1])
            throw new SQLException("Không được cấp quyền quản trị cho vai trò khác.");
        try (Connection c = ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE dbo.VaiTro SET moTa=? WHERE maVaiTro=? AND "
                      + "tenVaiTro IN(N'Quản lý phòng khám',N'Lễ tân')")) {
                    p.setString(1, moTa);
                    p.setInt(2, id);
                    if (p.executeUpdate() != 1)
                        throw new SQLException("Vai trò không còn tồn tại.");
                }
                try (PreparedStatement p = c.prepareStatement(
                        "DELETE FROM dbo.VaiTroQuyen WHERE maVaiTro=?")) {
                    p.setInt(1, id);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen) VALUES(?,?)")) {
                    for (int i = 0; i < QUYEN.length; i++) {
                        if (!rights[i]) continue;
                        p.setInt(1, id);
                        p.setString(2, QUYEN[i]);
                        p.addBatch();
                    }
                    p.executeBatch();
                }
                ghi(c, actor, "Sửa phân quyền", "VaiTro", "maVaiTro=" + id);
                c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    private void yeuCauQuyen(int actor, String code) throws SQLException {
        // Hai màn hình quản trị luôn yêu cầu đúng tài khoản Admin.
        if (("TAIKHOAN".equals(code) || "PHANQUYEN".equals(code))
                && !laQuanTriVienTaiKhoan(actor)) {
            throw new SQLException("Chỉ Quản trị viên được quản trị tài khoản và hệ thống.");
        }
        if (!coQuyenTaiKhoan(actor, code)) {
            throw new SQLException("Tài khoản không còn quyền " + code + ".");
        }
    }

    public LocalDate ngaySinhNhanVien(int maTK, int actor) throws SQLException {
        if (!laQuanTriVienTaiKhoan(actor) || !coQuyenTaiKhoan(actor, "TAIKHOAN"))
            throw new SQLException("Không có quyền đọc ngày sinh nhân viên.");
        try (Connection c=ConnectDB.mo(); PreparedStatement p=c.prepareStatement(
                "SELECT ngaySinh FROM dbo.NhanVien WHERE maTK=?")) {
            p.setInt(1,maTK);
            try (ResultSet r=p.executeQuery()) {
                if (r.next()) {
                    java.sql.Date d=r.getDate(1);
                    return d==null?null:d.toLocalDate();
                }
            }
        }
        return null;
    }

    /** Mật khẩu cấp lại: Tên (viết hoa chữ đầu) + ddMMyyyy, bắt buộc đổi sau đăng nhập. */
    public String datLaiMatKhauTheoNgaySinh(int maTK, int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        try (Connection c = ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                String matKhau = datLaiMatKhau(c, maTK, actor);
                ghi(c, actor, "Đặt lại mật khẩu", "TaiKhoan", "maTK=" + maTK);
                c.commit();
                return matKhau;
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    private String datLaiMatKhau(Connection c, int maTK, int actor) throws SQLException {
        if (maTK == actor) throw new SQLException("Không được tự đặt lại mật khẩu.");
        String sql = "SELECT n.hoTen,n.ngaySinh,v.tenVaiTro "
                   + "FROM dbo.TaiKhoan t WITH (UPDLOCK,HOLDLOCK) "
                   + "JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                   + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro WHERE t.maTK=?";
        String hoTen;
        java.sql.Date ngaySinh;
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, maTK);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new SQLException("Không tìm thấy nhân viên.");
                if ("Quản trị viên".equals(r.getString(3)))
                    throw new SQLException("Không đặt lại mật khẩu Admin tại màn hình này.");
                hoTen = r.getString(1);
                ngaySinh = r.getDate(2);
            }
        }
        if (ngaySinh == null || hoTen == null || hoTen.trim().isEmpty())
            throw new SQLException("Tài khoản thiếu họ tên hoặc ngày sinh; hãy cập nhật trước.");
        String matKhau = taoMatKhauMacDinh(hoTen, ngaySinh.toLocalDate());
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE dbo.TaiKhoan SET matKhau=?,phaiDoiMatKhau=1 WHERE maTK=?")) {
            p.setString(1, BCrypt.hashpw(matKhau, BCrypt.gensalt(11)));
            p.setInt(2, maTK);
            if (p.executeUpdate() != 1) throw new SQLException("Không tìm thấy tài khoản.");
        }
        return matKhau;
    }

    /** Quy tắc demo: tên gọi viết hoa chữ đầu + ngày sinh ddMMyyyy. */
    public static String taoMatKhauMacDinh(String hoTen, LocalDate ngaySinh) {
        if (hoTen == null || hoTen.trim().isEmpty() || ngaySinh == null)
            throw new IllegalArgumentException("Thiếu tên hoặc ngày sinh.");
        String[] cacTu = hoTen.trim().split("\\s+");
        String ten = cacTu[cacTu.length - 1].toLowerCase(new Locale("vi", "VN"));
        ten = ten.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + ten.substring(1);
        return ten + ngaySinh.format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"));
    }

    public void doiMatKhauLanDau(int maTK, String matKhauCu, String matKhauMoi)
            throws SQLException {
        if (!MatKhauHopLe.kiemTra(matKhauMoi))
            throw new SQLException("Mật khẩu mới ít nhất 10 ký tự, có chữ hoa và số.");
        try(Connection c=ConnectDB.mo()) {
            c.setAutoCommit(false);
            try {
                String oldHash;
                try(PreparedStatement p=c.prepareStatement(
                        "SELECT matKhau FROM dbo.TaiKhoan WITH(UPDLOCK) " +
                        "WHERE maTK=? AND trangThai=N'Hoạt động'")) {
                    p.setInt(1,maTK);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next())throw new SQLException("Tài khoản không hoạt động.");
                        oldHash=r.getString(1);
                    }
                }
                if(!BCrypt.checkpw(matKhauCu,oldHash))
                    throw new SQLException("Mật khẩu hiện tại không đúng.");
                if(BCrypt.checkpw(matKhauMoi,oldHash))
                    throw new SQLException("Mật khẩu mới phải khác mật khẩu cũ.");
                try(PreparedStatement p=c.prepareStatement(
                        "UPDATE dbo.TaiKhoan SET matKhau=?,phaiDoiMatKhau=0 WHERE maTK=?")) {
                    p.setString(1,BCrypt.hashpw(matKhauMoi,BCrypt.gensalt(11)));
                    p.setInt(2,maTK);
                    p.executeUpdate();
                }
                ghi(c,maTK,"Đổi mật khẩu sau cấp lại","TaiKhoan","maTK="+maTK);
                c.commit();
            }catch(Exception ex){
                c.rollback();
                if(ex instanceof SQLException)throw (SQLException)ex;
                throw new SQLException("Không đổi được mật khẩu.",ex);
            }finally{c.setAutoCommit(true);}
        }
    }

    private void ghi(Connection c,int actor,String action,String object,String detail)throws SQLException {
        try(PreparedStatement s=c.prepareStatement(
                "INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            s.setString(1,action);s.setString(2,object);
            s.setString(3,"Tài khoản thao tác #"+actor+" | "+detail);
            s.executeUpdate();
        }
    }
}
