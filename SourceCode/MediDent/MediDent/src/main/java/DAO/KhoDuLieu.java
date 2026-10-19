/*
 * KhoDuLieu: truy cập dữ liệu và kiểm soát nghiệp vụ SQL Server.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package DAO;

import Class.TaiKhoan;
import Class.VaiTro;
import TienIch.EmailHopLe;
import ConnectDB.KetNoi;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class KhoDuLieu {
    private static final String[] QUYEN = {"TAIKHOAN", "BENHNHAN", "LICHHEN", "THANHTOAN", "BAOCAO", "HETHONG", "NHANVIEN", "DICHVU", "HOTRO"};
    /** Trả bản sao danh sách mã quyền để tránh nơi gọi sửa cấu hình gốc. */
    public static String[] cacQuyen() { return QUYEN.clone(); }

    private static final String COT_TAI_KHOAN =
        "t.maTK,t.tenDangNhap,COALESCE(n.hoTen,t.tenDangNhap) AS HoTen," +
        "COALESCE(n.email,'') AS Email,t.maVaiTro,v.tenVaiTro AS TenVaiTro," +
        "t.trangThai AS TrangThai,t.phaiDoiMatKhau AS PhaiDoiMatKhau," +
        "CONVERT(varchar(19),t.ngayTao,120) AS NgayTao ";
    private static final String JOIN_TAI_KHOAN =
        " FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro" +
        " LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK ";

    /** Đối chiếu tài khoản và mật khẩu BCrypt với SQL; chặn nhân viên ngừng hoạt động và ghi nhật ký. */
    public TaiKhoan dangNhap(String username, String password) throws SQLException {
        String sql="SELECT "+COT_TAI_KHOAN+",t.matKhau "+JOIN_TAI_KHOAN+
                   " WHERE t.tenDangNhap=? AND (n.maNV IS NULL OR n.trangThai=N'Đang làm việc') "
                 + "AND v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')";
        // Chuẩn hóa tên để không thể lách giới hạn đăng nhập bằng dấu cách hoặc đổi hoa/thường.
        String loginName = username == null ? "" : username.trim();
        if (loginName.length() > 60) return null;
        String auditLogin = "Tên đăng nhập: " + loginName.toUpperCase(java.util.Locale.ROOT);
        try(Connection c=KetNoi.mo()) {
            // Dựa trên nhật ký hiện có, giới hạn tối đa 5 lần sai trong 15 phút.
            // Không thêm bảng, cột hay màn hình quản trị mới.
            try (PreparedStatement guard = c.prepareStatement(
                    "SELECT COUNT(*) FROM dbo.NhatKyHeThong "
                  + "WHERE hanhDong=N'Đăng nhập thất bại' AND moTa=? "
                  + "AND thoiGian>=DATEADD(MINUTE,-15,SYSDATETIME())")) {
                guard.setString(1, "maTK=0 | " + auditLogin);
                try (ResultSet failures = guard.executeQuery()) {
                    failures.next();
                    if (failures.getInt(1) >= 5)
                        throw new SQLException("Đăng nhập tạm thời bị giới hạn. Hãy thử lại sau 15 phút.");
                }
            }
            TaiKhoan result=null;
            try(PreparedStatement s=c.prepareStatement(sql)) {
                s.setString(1,loginName);
                try(ResultSet r=s.executeQuery()) {
                    if(r.next() && "Hoạt động".equals(r.getString("TrangThai"))) {
                        String hash=r.getString("matKhau");
                        boolean valid=false;
                        try {valid=hash!=null&&password!=null&&BCrypt.checkpw(password,hash);}
                        catch(IllegalArgumentException ignored){                        }
                        if(valid)result=docTK(r);
                    }
                }
            }
            if(result==null) {

                ghi(c,0,"Đăng nhập thất bại","TaiKhoan",auditLogin);
                return null;
            }
            try(PreparedStatement s=c.prepareStatement(
                    "UPDATE dbo.TaiKhoan SET ngayDangNhapCuoi=SYSDATETIME() WHERE maTK=?")){
                s.setInt(1,result.id);s.executeUpdate();
            }
            ghi(c,result.id,"Đăng nhập thành công","TaiKhoan","maTK="+result.id);
            return result;
        }
    }

    /** Tạo yêu cầu hỗ trợ đặt lại mật khẩu, không tiết lộ tên đăng nhập có tồn tại. */
    public void guiYeuCauQuenMatKhau(String tenDangNhap) throws SQLException {
        String username = tenDangNhap == null ? "" : tenDangNhap.trim();
        if (username.isEmpty() || username.length() > 80) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập hợp lệ.");
        }

        String sql =
                "INSERT INTO dbo.YeuCauHoTro(maTK, tieuDe, noiDung) " +
                "SELECT t.maTK, N'Yêu cầu đặt lại mật khẩu', " +
                "N'Gửi từ màn hình đăng nhập. Admin phải xác minh danh tính " +
                "ngoài ứng dụng trước khi đặt lại mật khẩu trong Quản lý tài khoản.' " +
                "FROM dbo.TaiKhoan t " +
                "WHERE t.tenDangNhap=? AND t.trangThai=N'Hoạt động' " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM dbo.YeuCauHoTro y WITH (UPDLOCK, HOLDLOCK) " +
                "WHERE y.maTK=t.maTK " +
                "AND y.tieuDe=N'Yêu cầu đặt lại mật khẩu' " +
                "AND y.trangThai=N'Chờ xử lý')";

        try (Connection c = KetNoi.mo()) {

            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, username);
                p.executeUpdate();                                                          
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /** Chuyển một hàng ResultSet có liên kết vai trò và nhân viên thành đối tượng TaiKhoan. */
    private TaiKhoan docTK(ResultSet r) throws SQLException {
        TaiKhoan t = new TaiKhoan(r.getInt("maTK"),r.getString("tenDangNhap"),
            r.getString("HoTen"),r.getString("Email"),r.getInt("maVaiTro"),
            r.getString("TenVaiTro"),r.getString("TrangThai"),r.getBoolean("PhaiDoiMatKhau"));
        t.ngayTao = r.getString("NgayTao");
        return t;
    }

    /** Tải lại dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public List<TaiKhoan> taiKhoan(String keyword,int role,String status) throws SQLException {
        List<TaiKhoan> ds=new ArrayList<>();
        String sql="SELECT "+COT_TAI_KHOAN+JOIN_TAI_KHOAN+
            " WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')"+
            " AND (n.hoTen LIKE ? OR n.email LIKE ? OR t.tenDangNhap LIKE ?)"+
            " AND (?=0 OR t.maVaiTro=?) AND (?='' OR t.trangThai=?) ORDER BY t.maTK DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)) {
            String term="%"+keyword+"%";
            for(int i=1;i<=3;i++)s.setString(i,term);
            s.setInt(4,role);s.setInt(5,role);s.setString(6,status);s.setString(7,status);
            try(ResultSet r=s.executeQuery()) { while(r.next())ds.add(docTK(r)); }
        }
        return ds;
    }

    /** Đọc các vai trò và quyền hiện tại từ cơ sở dữ liệu. */
    public List<VaiTro> vaiTro() throws SQLException {
        List<VaiTro> ds=new ArrayList<>();
        String sql="SELECT v.maVaiTro,v.tenVaiTro,v.moTa,q.maQuyen"+
            " FROM dbo.VaiTro v LEFT JOIN dbo.VaiTroQuyen vq ON v.maVaiTro=vq.maVaiTro"+
            " LEFT JOIN dbo.Quyen q ON vq.maQuyen=q.maQuyen"+
            " WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân') ORDER BY v.maVaiTro";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()) {
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

    /** Đối chiếu mã vai trò với tên Quản trị viên trong bảng VaiTro. */
    public boolean laQuanTriVien(int role) throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT 1 FROM dbo.VaiTro WHERE maVaiTro=? AND tenVaiTro=N'Quản trị viên'")) {
            s.setInt(1,role);try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /** Kiểm tra điều kiện của lớp; kết quả được chuyển cho nơi gọi. */
    public boolean coQuyen(int role,String ma) throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=? AND maQuyen=?")) {
            s.setInt(1,role);s.setString(2,ma);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /** Tải lại dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public boolean taiKhoanDangHoatDong(int maTK) throws SQLException {
        try (Connection c=KetNoi.mo(); PreparedStatement p=c.prepareStatement(
                "SELECT 1 FROM dbo.TaiKhoan t "
              + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
              + "AND NOT EXISTS (SELECT 1 FROM dbo.NhanVien n WHERE n.maTK=t.maTK "
              + "AND (n.trangThai IS NULL OR n.trangThai<>N'Đang làm việc'))")) {
            p.setInt(1,maTK);
            try(ResultSet r=p.executeQuery()) { return r.next(); }
        }
    }

    /** Kiểm tra quyền hiện tại từ SQL, không tin quyền cũ đang cache trong UI. */
    public boolean coQuyenTaiKhoan(int maTK,String maQuyen) throws SQLException {

        if (java.util.Arrays.asList("BENHNHAN", "LICHHEN", "THANHTOAN",
                "NHANVIEN", "DICHVU").contains(maQuyen)
                && laQuanTriVienTaiKhoan(maTK)) {
            return false;
        }
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTroQuyen q" +
            " ON t.maVaiTro=q.maVaiTro WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND q.maQuyen=?" +
            " AND NOT EXISTS(SELECT 1 FROM dbo.NhanVien n WHERE n.maTK=t.maTK " +
            "AND (n.trangThai IS NULL OR n.trangThai<>N'Đang làm việc'))";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);s.setString(2,maQuyen);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /** Xác minh chính xác vai trò Admin đang hoạt động theo liên kết TaiKhoan–VaiTro. */
    public boolean laQuanTriVienTaiKhoan(int maTK) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro " +
            "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'" +
            " AND NOT EXISTS(SELECT 1 FROM dbo.NhanVien n WHERE n.maTK=t.maTK " +
            "AND (n.trangThai IS NULL OR n.trangThai<>N'Đang làm việc'))";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /** Đọc bộ đếm IDENTITY để chỉ hiển thị mã dự kiến; không giữ chỗ. */
    public int duKienMaTaiKhoan(int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        String sql =
                "SELECT CASE WHEN last_value IS NULL " +
                "THEN CONVERT(bigint, seed_value) ELSE CONVERT(bigint, last_value) + " +
                "CONVERT(bigint, increment_value) END " +
                "FROM sys.identity_columns " +
                "WHERE object_id = OBJECT_ID(N'dbo.TaiKhoan') " +
                "AND name = N'maTK'";
        try (Connection c = KetNoi.mo();
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

    /** Tạo tên đăng nhập theo mẫu NVxxxxxx từ mã tài khoản thực. */
    public static String tenDangNhapTheoMa(int maTaiKhoan) {
        return String.format(Locale.ROOT, "NV%06d", maTaiKhoan);
    }

    /** Lưu dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public void luuTaiKhoan(int id, String username, String name, String email,
                            int role, String status, String newPassword,
                            LocalDate ngaySinh, int actor) throws SQLException {
        luuTaiKhoanTraVeMa(id, username, name, email, role, status,
                newPassword, ngaySinh, actor);
    }

    /** Lưu tài khoản và hồ sơ nhân viên nguyên tử; chặn khóa hoặc hạ quyền Admin cuối cùng. */
    public int luuTaiKhoanTraVeMa(int id, String username, String name, String email,
                                 int role, String status, String newPassword,
                                 LocalDate ngaySinh, int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");

        if(name==null||name.trim().isEmpty())
            throw new SQLException("Thiếu tên nhân viên.");
        // Tránh ghi trạng thái tài khoản khác hai giá trị được ràng buộc ở SQL.
        if (!"Hoạt động".equals(status) && !"Tạm khóa".equals(status))
            throw new SQLException("Trạng thái tài khoản không hợp lệ.");
        if (!EmailHopLe.kiemTra(email))
            throw new SQLException(EmailHopLe.huongDan());
        email = email.trim();

        if (id == 0 && ngaySinh == null)
            throw new SQLException("Hãy chọn ngày sinh thực tế của nhân viên.");
        if (ngaySinh != null && (ngaySinh.isAfter(LocalDate.now()) ||
                ngaySinh.isBefore(LocalDate.now().minusYears(100))))
            throw new SQLException("Ngày sinh nhân viên không hợp lệ.");
        if(id==0 && (newPassword==null||newPassword.length()<8))
            throw new SQLException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                int accountId=id;
                if (id != 0) {

                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM dbo.TaiKhoan t WITH (TABLOCKX,HOLDLOCK) "
                          + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                          + "WHERE t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) == 1) {
                                try (PreparedStatement last = c.prepareStatement(
                                        "SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                                      + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                                      + "AND v.tenVaiTro=N'Quản trị viên' "
                                      + "AND (?<>t.maVaiTro OR ?<>N'Hoạt động')")) {
                                    last.setInt(1, id);
                                    last.setInt(2, role);
                                    last.setString(3, status);
                                    try (ResultSet blocked = last.executeQuery()) {
                                        if (blocked.next()) throw new SQLException(
                                                "Không thể khóa hoặc hạ quyền Quản trị viên hoạt động cuối cùng.");
                                    }
                                }
                            }
                        }
                    }
                }
                if(id!=0&&id==actor){

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
                    if(newPassword!=null&&!newPassword.isEmpty()) {
                        if(newPassword.length()<8)throw new SQLException("Mật khẩu phải dài tối thiểu 8 ký tự.");
                        try(PreparedStatement s=c.prepareStatement("UPDATE dbo.TaiKhoan SET matKhau=?,phaiDoiMatKhau=1 WHERE maTK=?")) {
                            s.setString(1,BCrypt.hashpw(newPassword,BCrypt.gensalt(11)));
                            s.setInt(2,id);s.executeUpdate();
                        }
                    }
                }

                String tenVaiTro;
                try(PreparedStatement s=c.prepareStatement("SELECT tenVaiTro FROM dbo.VaiTro WHERE maVaiTro=?")) {
                    s.setInt(1,role);
                    try(ResultSet r=s.executeQuery()) {
                        if(!r.next())throw new SQLException("Vai trò không tồn tại.");
                        tenVaiTro=r.getString(1);
                        if (!java.util.Set.of("Quản trị viên", "Quản lý phòng khám", "Lễ tân").contains(tenVaiTro))
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
                        s.setString(4,"Hoạt động".equals(status)?"Đang làm việc":"Nghỉ việc");
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
                            "UPDATE dbo.NhanVien SET hoTen=?,email=?,chucVu=?,trangThai=?,ngaySinh=COALESCE(?,ngaySinh) WHERE maNV=?")) {
                        s.setString(1,name);s.setString(2,email);s.setString(3,tenVaiTro);
                        s.setString(4,"Hoạt động".equals(status)?"Đang làm việc":"Nghỉ việc");
                        if (ngaySinh == null) s.setNull(5,Types.DATE);
                        else s.setDate(5,java.sql.Date.valueOf(ngaySinh));
                        s.setInt(6,staffId);s.executeUpdate();
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

    /** Đồng bộ quan hệ nhân viên với bảng vai trò nghiệp vụ tương ứng. */
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

    /** Vô hiệu hóa tài khoản và hồ sơ nhân viên thay vì xóa cứng khóa chính. */
    public void xoaTK(int id, int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        if (id == actor) throw new SQLException("Không thể ngừng tài khoản đang đăng nhập.");
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {

                int adminConLai;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM dbo.TaiKhoan t WITH (TABLOCKX,HOLDLOCK) "
                      + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                      + "WHERE t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'")) {
                    try (ResultSet r = ps.executeQuery()) {
                        r.next(); adminConLai = r.getInt(1);
                    }
                }
                if (adminConLai <= 1) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                          + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                          + "AND v.tenVaiTro=N'Quản trị viên'")) {
                        ps.setInt(1, id);
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next()) throw new SQLException("Không thể ngừng Admin cuối cùng.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.TaiKhoan SET trangThai=N'Tạm khóa' WHERE maTK=?")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy tài khoản.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.NhanVien SET trangThai=N'Nghỉ việc' WHERE maTK=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                ghi(c, actor, "Ngừng tài khoản", "TaiKhoan", "maTK=" + id);
                c.commit();
            } catch (SQLException ex) {
                c.rollback(); throw ex;
            } finally { c.setAutoCommit(true); }
        }
    }

    /** Kiểm tra vai trò gốc và lưu ma trận quyền trong một transaction; không cho đổi danh tính vai trò. */
    public void luuVaiTro(int id,String ten,String moTa,boolean[] rights,int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        if (rights == null || rights.length != QUYEN.length)
            throw new SQLException("Danh sách quyền không đầy đủ, không thể lưu.");
        if(ten==null||ten.trim().isEmpty())throw new SQLException("Tên vai trò không được để trống.");
        if (id == 0 || !java.util.Set.of("Quản trị viên", "Quản lý phòng khám", "Lễ tân").contains(ten.trim()))
            throw new SQLException("Chỉ được sửa quyền của vai trò Quản lý phòng khám và Lễ tân; không thêm hoặc đổi tên vai trò.");
        if ((id != 0 && laQuanTriVien(id)) || "Quản trị viên".equalsIgnoreCase(ten.trim()))
            throw new SQLException("Không sửa hoặc cấp lại vai trò Admin gốc tại ma trận phân quyền.");
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                // Dù tên vai trò được gửi trực tiếp tới DAO, chỉ sửa đúng vai trò cố định.
                try (PreparedStatement verify = c.prepareStatement(
                        "SELECT tenVaiTro FROM dbo.VaiTro WITH (UPDLOCK,HOLDLOCK) WHERE maVaiTro=?")) {
                    verify.setInt(1, id);
                    try (ResultSet r = verify.executeQuery()) {
                        if (!r.next() || !ten.trim().equals(r.getString(1)))
                            throw new SQLException("Không được đổi tên hoặc thay mã vai trò cố định.");
                    }
                }
                int rid=id;
                if(id==0)try(PreparedStatement s=c.prepareStatement(
                        "INSERT INTO dbo.VaiTro(tenVaiTro,moTa) VALUES(?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1,ten);s.setString(2,moTa);s.executeUpdate();
                    try(ResultSet r=s.getGeneratedKeys()){if(r.next())rid=r.getInt(1);}
                } else try(PreparedStatement s=c.prepareStatement(
                        "UPDATE dbo.VaiTro SET tenVaiTro=?,moTa=? WHERE maVaiTro=?")) {
                    s.setString(1,ten);s.setString(2,moTa);s.setInt(3,id);
                    if(s.executeUpdate()!=1)throw new SQLException("Vai trò không tồn tại.");
                }
                try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo.VaiTroQuyen WHERE maVaiTro=?")){
                    s.setInt(1,rid);s.executeUpdate();
                }
                try(PreparedStatement s=c.prepareStatement(
                        "INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen) VALUES(?,?)")) {
                    for(int i=0;i<QUYEN.length && i<rights.length;i++)if(rights[i]){
                        s.setInt(1,rid);s.setString(2,QUYEN[i]);s.addBatch();
                    }
                    s.executeBatch();
                }

                try(PreparedStatement s=c.prepareStatement(
                        "UPDATE n SET chucVu=v.tenVaiTro FROM dbo.NhanVien n"+
                        " JOIN dbo.TaiKhoan t ON n.maTK=t.maTK"+
                        " JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE t.maVaiTro=?")) {
                    s.setInt(1,rid);s.executeUpdate();
                }
                ghi(c,actor,id==0?"Thêm vai trò":"Sửa vai trò","VaiTro",ten);
                c.commit();
            }catch(SQLException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Từ chối xóa ba vai trò cố định để không làm mất ma trận phân quyền. */
    public void xoaVaiTro(int id,int actor) throws SQLException {
        throw new SQLException("Không xóa vai trò cố định của MediDent.");
    }

    /** Lấy cặp khóa/giá trị cấu hình hệ thống từ SQL. */
    public Map<String,String> cauHinh()throws SQLException {
        Map<String,String> m=new LinkedHashMap<>();
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT Khoa,GiaTri FROM dbo.CauHinh");ResultSet r=s.executeQuery()){
            while(r.next())m.put(r.getString(1),r.getString(2));
        }
        return m;
    }
    /** Kiểm tra khóa cấu hình có tồn tại; từ chối bật chế độ chưa triển khai thật. */
    public void luuCauHinh(Map<String,String> m,int actor)throws SQLException {
        yeuCauQuyen(actor, "HETHONG");
        if (m == null || !EmailHopLe.kiemTra(m.get("EmailQuanTri")))
            throw new SQLException(EmailHopLe.huongDan());
        for (String feature : new String[]{"Bat2FA", "KhoaPhien", "SaoLuuTuDong", "ThongBaoQuyen"}) {
            String v = m.get(feature);
            if ("1".equals(v) || "true".equalsIgnoreCase(v))
                throw new SQLException("Cấu hình '" + feature + "' chưa có cơ chế thực thi; không thể bật giả.");
        }
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                for (Map.Entry<String,String> entry : m.entrySet()) {

                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.CauHinh SET GiaTri=? WHERE Khoa=?")) {
                        ps.setString(1, entry.getValue());
                        ps.setString(2, entry.getKey());
                        if (ps.executeUpdate() != 1)
                            throw new SQLException("Không tìm thấy khóa cấu hình: " + entry.getKey());
                    }
                }
                ghi(c,actor,"Cập nhật cấu hình","CauHinh","Cấu hình hệ thống");
                c.commit();
            } catch(SQLException ex) {c.rollback();throw ex;}
            finally {c.setAutoCommit(true);}
        }
    }

    /** Chặn nghiệp vụ quản trị khi actor không còn quyền tương ứng. */
    private void yeuCauQuyen(int actor, String code) throws SQLException {

        if (("TAIKHOAN".equals(code) || "HETHONG".equals(code))
                && !laQuanTriVienTaiKhoan(actor)) {
            throw new SQLException("Chỉ Quản trị viên được quản trị tài khoản và hệ thống.");
        }
        if (!coQuyenTaiKhoan(actor, code)) {
            throw new SQLException("Tài khoản không còn quyền " + code + ".");
        }
    }

    /** Lấy ngày sinh nhân viên theo tài khoản nếu actor là quản trị viên. */
    public LocalDate ngaySinhNhanVien(int maTK, int actor) throws SQLException {
        if (!laQuanTriVienTaiKhoan(actor) || !coQuyenTaiKhoan(actor, "TAIKHOAN"))
            throw new SQLException("Không có quyền đọc ngày sinh nhân viên.");
        try (Connection c=KetNoi.mo(); PreparedStatement p=c.prepareStatement(
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

    /** Giữ tên hàm tương thích lời gọi cũ; nay sinh mật khẩu ngẫu nhiên và bắt buộc đổi sau đăng nhập. */
    public String datLaiMatKhauTheoNgaySinh(int maTK, int actor) throws SQLException {
        yeuCauQuyen(actor, "TAIKHOAN");
        if (!laQuanTriVienTaiKhoan(actor))
            throw new SQLException("Chỉ Quản trị viên được đặt lại mật khẩu.");
        if (maTK == actor) throw new SQLException("Không thể tự cấp lại mật khẩu tại đây.");
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder temporary = new StringBuilder();
        for (int i = 0; i < 18; i++)
            temporary.append(alphabet.charAt(random.nextInt(alphabet.length())));
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {

                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT v.tenVaiTro FROM dbo.TaiKhoan t WITH (UPDLOCK,HOLDLOCK) "
                      + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE t.maTK=?")) {
                    ps.setInt(1, maTK);
                    try (ResultSet r = ps.executeQuery()) {
                        if (!r.next()) throw new SQLException("Tài khoản không còn tồn tại.");
                        if ("Quản trị viên".equals(r.getString(1)))
                            throw new SQLException("Tài khoản Admin phải dùng quy trình riêng.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.TaiKhoan SET matKhau=?,phaiDoiMatKhau=1 WHERE maTK=?")) {
                    ps.setString(1, BCrypt.hashpw(temporary.toString(), BCrypt.gensalt(11)));
                    ps.setInt(2, maTK);
                    if (ps.executeUpdate() != 1) throw new SQLException("Tài khoản không còn tồn tại.");
                }
                ghi(c, actor, "Đặt lại mật khẩu ngẫu nhiên", "TaiKhoan", "maTK=" + maTK);
                c.commit();

                return temporary.toString();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Xác minh mật khẩu tạm, ghi hash BCrypt mới và xóa cờ buộc đổi trong transaction. */
    public void doiMatKhauLanDau(int maTK, String matKhauCu, String matKhauMoi)
            throws SQLException {
        if(matKhauMoi==null || matKhauMoi.length()<10 ||
                !matKhauMoi.matches("(?s).*\\d.*") || !matKhauMoi.matches("(?s).*[A-Z].*"))
            throw new SQLException("Mật khẩu mới ít nhất 10 ký tự, có chữ hoa và số.");
        try(Connection c=KetNoi.mo()) {
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

    /** Đếm dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public int dem(String sql)throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()) {
            return r.next()?r.getInt(1):0;
        }
    }

    /** Tính số lượng tài khoản tăng theo các tháng trong năm. */
    public int[] tangTruong(int year)throws SQLException {
        int[] out=new int[12];
        String sql="SELECT MONTH(ngayTao) AS thang,COUNT(*) AS soLuong FROM dbo.TaiKhoan " +
            "WHERE ngayTao>=? AND ngayTao<? GROUP BY MONTH(ngayTao)";
        try(Connection c=KetNoi.mo()) {
            int base=0;
            try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM dbo.TaiKhoan WHERE ngayTao<?")){
                s.setDate(1,java.sql.Date.valueOf(LocalDate.of(year,1,1)));
                try(ResultSet r=s.executeQuery()){if(r.next())base=r.getInt(1);}
            }
            try(PreparedStatement s=c.prepareStatement(sql)){
                s.setDate(1,java.sql.Date.valueOf(LocalDate.of(year,1,1)));
                s.setDate(2,java.sql.Date.valueOf(LocalDate.of(year+1,1,1)));
                try(ResultSet r=s.executeQuery()) {while(r.next())out[r.getInt("thang")-1]=r.getInt("soLuong");}
            }
            for(int i=0;i<12;i++){base+=out[i];out[i]=base;}
        }
        return out;
    }

    /** Đếm số tài khoản theo từng vai trò từ các bảng SQL. */
    public List<String[]> phanBo()throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT v.tenVaiTro,COUNT(t.maTK) FROM dbo.VaiTro v LEFT JOIN dbo.TaiKhoan t"+
            " ON v.maVaiTro=t.maVaiTro WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')"+
            " GROUP BY v.maVaiTro,v.tenVaiTro ORDER BY COUNT(t.maTK) DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),String.valueOf(r.getInt(2))});
        }
        return out;
    }

    /** Đọc dòng sự kiện nhật ký để hiển thị phần quản trị. */
    public List<String[]> nhatKy()throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT TOP 100 CONVERT(varchar(19),thoiGian,120),hanhDong,"+
            "CONCAT(ISNULL(doiTuong,''),N' — ',ISNULL(moTa,'')) FROM dbo.NhatKyHeThong ORDER BY maNK DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});
        }
        return out;
    }

    /** Tổng hợp doanh thu đã xác nhận theo khoảng ngày báo cáo. */
    public BigDecimal tongDoanhThu(LocalDate start,LocalDate end)throws SQLException {
        String sql="SELECT COALESCE(SUM(soTien),0) FROM dbo.ThanhToan"+
            " WHERE ngayThanhToan>=? AND ngayThanhToan<?"+
            " AND trangThai IN (N'Thành công',N'Đã thanh toán')";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setDate(1,java.sql.Date.valueOf(start));s.setDate(2,java.sql.Date.valueOf(end.plusDays(1)));
            try(ResultSet r=s.executeQuery()){return r.next()?r.getBigDecimal(1):BigDecimal.ZERO;}
        }
    }
    /** Đếm số lượt khám phát sinh trong khoảng thời gian đã chọn. */
    public int tongLuotKham(LocalDate start,LocalDate end)throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT COUNT(*) FROM dbo.PhieuKham WHERE ngayKham>=? AND ngayKham<?")) {
            s.setDate(1,java.sql.Date.valueOf(start));s.setDate(2,java.sql.Date.valueOf(end.plusDays(1)));
            try(ResultSet r=s.executeQuery()){return r.next()?r.getInt(1):0;}
        }
    }

    /** Trả doanh thu hoặc số lượt khám theo từng tháng. */
    public List<String[]> baoCaoTheoThang(int year,boolean doanhThu)throws SQLException {
        String dateCol=doanhThu?"ngayThanhToan":"ngayKham";
        String table=doanhThu?"ThanhToan":"PhieuKham";
        String aggregate=doanhThu?"COALESCE(SUM(soTien),0)":"COUNT(*)";
        String condition=doanhThu?" AND trangThai IN(N'Thành công',N'Đã thanh toán')":"";
        String sql="SELECT MONTH("+dateCol+"),"+aggregate+" FROM dbo."+table+
            " WHERE "+dateCol+">=? AND "+dateCol+"<?"+condition+
            " GROUP BY MONTH("+dateCol+") ORDER BY MONTH("+dateCol+")";
        String[] count=new String[12];Arrays.fill(count,"0");
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)) {
            s.setDate(1,java.sql.Date.valueOf(LocalDate.of(year,1,1)));
            s.setDate(2,java.sql.Date.valueOf(LocalDate.of(year+1,1,1)));
            try(ResultSet r=s.executeQuery()) {
                while(r.next())count[r.getInt(1)-1]=r.getString(2);
            }
        }
        List<String[]> out=new ArrayList<>();
        for(int i=0;i<12;i++)out.add(new String[]{"Tháng "+(i+1),count[i]});
        return out;
    }

    /** Tính tỷ lệ bệnh nhân quay lại trên tập lượt khám đang xem. */
    public double tyLeQuayLai(LocalDate start,LocalDate end)throws SQLException {
        String sql="WITH LanKham AS (SELECT maBN,COUNT(*) AS soLan FROM dbo.PhieuKham " +
            "WHERE ngayKham>=? AND ngayKham<? GROUP BY maBN) " +
            "SELECT COALESCE(100.0*SUM(CASE WHEN soLan>=2 THEN 1 ELSE 0 END)" +
            "/NULLIF(COUNT(*),0),0) FROM LanKham";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setDate(1,java.sql.Date.valueOf(start));
            s.setDate(2,java.sql.Date.valueOf(end.plusDays(1)));
            try(ResultSet r=s.executeQuery()){return r.next()?r.getDouble(1):0.0;}
        }
    }

    /** Ghi sự kiện nghiệp vụ vào nhật ký trên chính Connection của transaction. */
    private void ghi(Connection c,int actor,String action,String object,String detail)throws SQLException {
        try(PreparedStatement s=c.prepareStatement(
                "INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            s.setString(1,action);s.setString(2,object);
            s.setString(3,"Tài khoản thao tác #"+actor+" | "+detail);
            s.executeUpdate();
        }
    }
}
