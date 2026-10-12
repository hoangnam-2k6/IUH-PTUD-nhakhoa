package DAO;

import Class.TaiKhoan;
import Class.VaiTro;
import ConnectDB.KetNoi;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Lop truy cap du lieu SQL Server.
 * Moi yeu cau dung PreparedStatement, moi thao tac nhieu bang dung transaction.
 * Ten bang va ten cot duoc dong bo voi so do ERD: TaiKhoan, NhanVien, VaiTro,
 * NhatKyHeThong, ThanhToan, HoaDon, PhieuKham.
 */
public class KhoDuLieu {
    private static final String[] QUYEN = {"TAIKHOAN", "BENHNHAN", "LICHHEN", "THANHTOAN", "BAOCAO", "HETHONG"};
    public static String[] cacQuyen() { return QUYEN.clone(); }

    // Ten/hop thu cua tai khoan nam trong NhanVien, khong nam trong TaiKhoan.
    private static final String COT_TAI_KHOAN =
        "t.maTK,t.tenDangNhap,COALESCE(n.hoTen,t.tenDangNhap) AS HoTen," +
        "COALESCE(n.email,'') AS Email,t.maVaiTro,v.tenVaiTro AS TenVaiTro,t.trangThai AS TrangThai ";
    private static final String JOIN_TAI_KHOAN =
        " FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro" +
        " LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK ";

    /**
     * Kiem tra BCrypt, trang thai, vai tro tu SQL.
     * Ghi NhatKyHeThong cho ca thanh cong / that bai de Dashboard co canh bao that.
     */
    public TaiKhoan dangNhap(String username, String password) throws SQLException {
        String sql="SELECT "+COT_TAI_KHOAN+",t.matKhau "+JOIN_TAI_KHOAN+
                   " WHERE t.tenDangNhap=?";
        try(Connection c=KetNoi.mo()) {
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
                ghi(c,0,"Đăng nhập thất bại","TaiKhoan","Tên đăng nhập: "+username);
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

    private TaiKhoan docTK(ResultSet r) throws SQLException {
        return new TaiKhoan(r.getInt("maTK"),r.getString("tenDangNhap"),
            r.getString("HoTen"),r.getString("Email"),r.getInt("maVaiTro"),
            r.getString("TenVaiTro"),r.getString("TrangThai"));
    }

    public List<TaiKhoan> taiKhoan(String keyword,int role,String status) throws SQLException {
        List<TaiKhoan> ds=new ArrayList<>();
        String sql="SELECT "+COT_TAI_KHOAN+JOIN_TAI_KHOAN+
            " WHERE (n.hoTen LIKE ? OR n.email LIKE ? OR t.tenDangNhap LIKE ?)"+
            " AND (?=0 OR t.maVaiTro=?) AND (?='' OR t.trangThai=?) ORDER BY t.maTK DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)) {
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
            " LEFT JOIN dbo.Quyen q ON vq.maQuyen=q.maQuyen ORDER BY v.maVaiTro";
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

    /** Quyen trang quan tri khong duoc quyet dinh bang ID vai tro mac dinh = 1. */
    public boolean laQuanTriVien(int role) throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT 1 FROM dbo.VaiTro WHERE maVaiTro=? AND tenVaiTro=N'Quản trị viên'")) {
            s.setInt(1,role);try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public boolean coQuyen(int role,String ma) throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=? AND maQuyen=?")) {
            s.setInt(1,role);s.setString(2,ma);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /**
     * Kiem tra maVaiTro HIEN TAI cua tai khoan tai SQL, khong tin ma quyen da cache.
     * Neu bi tam khoa/doi quyen trong luc dang su dung, lan chuyen trang ke tiep bi chan.
     */
    public boolean coQuyenTaiKhoan(int maTK,String maQuyen) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTroQuyen q" +
            " ON t.maVaiTro=q.maVaiTro WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND q.maQuyen=?";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);s.setString(2,maQuyen);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public boolean laQuanTriVienTaiKhoan(int maTK) throws SQLException {
        String sql="SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro " +
            "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,maTK);
            try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    /** Tao/Sua TaiKhoan va NhanVien trong CUNG mot giao dich (transaction). */
    public void luuTaiKhoan(int id,String username,String name,String email,int role,
                            String status,String newPassword,int actor) throws SQLException {
        // Khi them moi, tenDangNhap tu sinh NV000001...; sua thi giu nguyen.
        if(name==null||name.trim().isEmpty())
            throw new SQLException("Thiếu tên nhân viên.");
        if(email==null||!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new SQLException("Email không hợp lệ.");
        if(id==0 && (newPassword==null||newPassword.length()<8))
            throw new SQLException("Mật khẩu mới phải có ít nhất 8 ký tự.");
        try(Connection c=KetNoi.mo()) {
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
                    String sql="INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,maVaiTro,trangThai) VALUES(?,?,?,?)";
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
                        s.setString(1,String.format("NV%06d",accountId));
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
                        try(PreparedStatement s=c.prepareStatement("UPDATE dbo.TaiKhoan SET matKhau=? WHERE maTK=?")) {
                            s.setString(1,BCrypt.hashpw(newPassword,BCrypt.gensalt(11)));
                            s.setInt(2,id);s.executeUpdate();
                        }
                    }
                }

                // ChucVu duoc chon tu VaiTro, khong bat nguoi dung go thu cong.
                String tenVaiTro;
                try(PreparedStatement s=c.prepareStatement("SELECT tenVaiTro FROM dbo.VaiTro WHERE maVaiTro=?")) {
                    s.setInt(1,role);
                    try(ResultSet r=s.executeQuery()) {
                        if(!r.next())throw new SQLException("Vai trò không tồn tại.");
                        tenVaiTro=r.getString(1);
                    }
                }
                int staffId=0;
                try(PreparedStatement s=c.prepareStatement("SELECT maNV FROM dbo.NhanVien WHERE maTK=?")) {
                    s.setInt(1,accountId);
                    try(ResultSet r=s.executeQuery()){if(r.next())staffId=r.getInt(1);}
                }
                if(staffId==0) {
                    try(PreparedStatement s=c.prepareStatement(
                            "INSERT INTO dbo.NhanVien(hoTen,email,chucVu,trangThai,maTK) VALUES(?,?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        s.setString(1,name);s.setString(2,email);s.setString(3,tenVaiTro);
                        s.setString(4,"Hoạt động".equals(status)?"Đang làm việc":"Ngừng làm việc");
                        s.setInt(5,accountId);s.executeUpdate();
                        try(ResultSet r=s.getGeneratedKeys()){
                            if(!r.next())throw new SQLException("Không lấy được mã nhân viên mới.");
                            staffId=r.getInt(1);
                        }
                    }
                } else {
                    try(PreparedStatement s=c.prepareStatement(
                            "UPDATE dbo.NhanVien SET hoTen=?,email=?,chucVu=?,trangThai=? WHERE maNV=?")) {
                        s.setString(1,name);s.setString(2,email);s.setString(3,tenVaiTro);
                        s.setString(4,"Hoạt động".equals(status)?"Đang làm việc":"Ngừng làm việc");
                        s.setInt(5,staffId);s.executeUpdate();
                    }
                }
                dongBoPhanLoai(c,staffId,tenVaiTro);
                ghi(c,actor,id==0?"Thêm tài khoản":"Sửa tài khoản","TaiKhoan",name+" (maTK="+accountId+")");
                c.commit();
            }catch(Exception e) {
                c.rollback();
                if(e instanceof SQLException)throw(SQLException)e;
                throw new SQLException("Không thể lưu tài khoản.",e);
            }finally{c.setAutoCommit(true);}
        }
    }

    /**
     * Dong bo nhan vien voi bang subtype dung vai tro.
     * Khong xoa/re-insert subtype neu vai tro giu nguyen (tranh mat du lieu rieng).
     */
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

    /** Chi xoa duoc nhan vien chua co lich hen, lich lam viec, phieu kham... */
    public void xoaTK(int id,int actor) throws SQLException {
        if(id==actor)throw new SQLException("Không thể xóa tài khoản đang đăng nhập.");
        try(Connection c=KetNoi.mo()) {
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

    public void luuVaiTro(int id,String ten,String moTa,boolean[] rights,int actor) throws SQLException {
        if(ten==null||ten.trim().isEmpty())throw new SQLException("Tên vai trò không được để trống.");
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
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
                    for(int i=0;i<QUYEN.length;i++)if(rights[i]){
                        s.setInt(1,rid);s.setString(2,QUYEN[i]);s.addBatch();
                    }
                    s.executeBatch();
                }
                // Giup chucVu cua nhan vien luon khop voi vai tro sau khi sua ten.
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

    public void xoaVaiTro(int id,int actor) throws SQLException {
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                // Khong xoa vai tro dang duoc su dung, de phong tai khoan mat quyen.
                try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM dbo.TaiKhoan WHERE maVaiTro=?")){
                    s.setInt(1,id);try(ResultSet r=s.executeQuery()){
                        if(r.next()&&r.getInt(1)>0)throw new SQLException("Vai trò đang có tài khoản; không thể xóa.");
                    }
                }
                try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo.VaiTroQuyen WHERE maVaiTro=?")){
                    s.setInt(1,id);s.executeUpdate();
                }
                try(PreparedStatement s=c.prepareStatement("DELETE FROM dbo.VaiTro WHERE maVaiTro=?")){
                    s.setInt(1,id);if(s.executeUpdate()!=1)throw new SQLException("Vai trò không tồn tại.");
                }
                ghi(c,actor,"Xóa vai trò","VaiTro","maVaiTro="+id);
                c.commit();
            }catch(SQLException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }

    public Map<String,String> cauHinh()throws SQLException {
        Map<String,String> m=new LinkedHashMap<>();
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT Khoa,GiaTri FROM dbo.CauHinh");ResultSet r=s.executeQuery()){
            while(r.next())m.put(r.getString(1),r.getString(2));
        }
        return m;
    }
    public void luuCauHinh(Map<String,String> m,int actor)throws SQLException {
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement s=c.prepareStatement("UPDATE dbo.CauHinh SET GiaTri=? WHERE Khoa=?")){
                    for(Map.Entry<String,String> e:m.entrySet()) {
                        s.setString(1,e.getValue());s.setString(2,e.getKey());s.addBatch();
                    }
                    s.executeBatch();
                }
                ghi(c,actor,"Cập nhật cấu hình","CauHinh","Cấu hình hệ thống");c.commit();
            }catch(SQLException e){c.rollback();throw e;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Chi nhan cau SQL CO DINH tu code, KHONG truyen SQL do nguoi dung nhap. */
    public int dem(String sql)throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()) {
            return r.next()?r.getInt(1):0;
        }
    }

    /** So tai khoan da tao luy ke den cuoi moi thang (co ca nam truoc). */
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

    public List<String[]> phanBo()throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT v.tenVaiTro,COUNT(t.maTK) FROM dbo.VaiTro v LEFT JOIN dbo.TaiKhoan t"+
            " ON v.maVaiTro=t.maVaiTro GROUP BY v.maVaiTro,v.tenVaiTro ORDER BY COUNT(t.maTK) DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),String.valueOf(r.getInt(2))});
        }
        return out;
    }

    public List<String[]> nhatKy()throws SQLException {
        List<String[]> out=new ArrayList<>();
        String sql="SELECT TOP 100 CONVERT(varchar(19),thoiGian,120),hanhDong,"+
            "CONCAT(ISNULL(doiTuong,''),N' — ',ISNULL(moTa,'')) FROM dbo.NhatKyHeThong ORDER BY maNK DESC";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){
            while(r.next())out.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});
        }
        return out;
    }

    /** Thong ke THUC so tien da thanh toan trong khoang (khong dung so minh hoa). */
    public BigDecimal tongDoanhThu(LocalDate start,LocalDate end)throws SQLException {
        String sql="SELECT COALESCE(SUM(soTien),0) FROM dbo.ThanhToan"+
            " WHERE ngayThanhToan>=? AND ngayThanhToan<?"+
            " AND trangThai IN (N'Thành công',N'Đã thanh toán')";
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(sql)){
            s.setDate(1,java.sql.Date.valueOf(start));s.setDate(2,java.sql.Date.valueOf(end.plusDays(1)));
            try(ResultSet r=s.executeQuery()){return r.next()?r.getBigDecimal(1):BigDecimal.ZERO;}
        }
    }
    public int tongLuotKham(LocalDate start,LocalDate end)throws SQLException {
        try(Connection c=KetNoi.mo();PreparedStatement s=c.prepareStatement(
                "SELECT COUNT(*) FROM dbo.PhieuKham WHERE ngayKham>=? AND ngayKham<?")) {
            s.setDate(1,java.sql.Date.valueOf(start));s.setDate(2,java.sql.Date.valueOf(end.plusDays(1)));
            try(ResultSet r=s.executeQuery()){return r.next()?r.getInt(1):0;}
        }
    }

    /** Tong hop tung thang tu PhieuKham / ThanhToan, loc nam theo input. */
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

    /** Ty le benh nhan den kham tu 2 lan tro len trong khoang dang chon. */
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

    /** Nhat ky khong co maTK trong ERD: luu actor trong moTa va doiTuong. */
    private void ghi(Connection c,int actor,String action,String object,String detail)throws SQLException {
        try(PreparedStatement s=c.prepareStatement(
                "INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            s.setString(1,action);s.setString(2,object);
            s.setString(3,"Tài khoản thao tác #"+actor+" | "+detail);
            s.executeUpdate();
        }
    }
}
