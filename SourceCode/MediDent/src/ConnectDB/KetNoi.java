package ConnectDB;
import java.sql.*;
public final class KetNoi {
    private KetNoi(){
    }
    public static Connection mo() throws SQLException {
        CauHinh.require();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        catch(ClassNotFoundException e){
            throw new SQLException("Thiếu JDBC driver. Chạy bằng CHAY_MEDIDENT.bat hoặc sử dụng Maven.",e);
        }
        Connection c=DriverManager.getConnection(CauHinh.value("db.url"),CauHinh.value("db.user"),CauHinh.value("db.password"));
        if(!"MediDent".equalsIgnoreCase(c.getCatalog())){
            c.close();
            throw new SQLException("Đang kết nối sai database; cần MediDent.");
        }
        return c;
    }
    public static void kiemTra() throws SQLException {
        try(Connection c=mo();Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT 1")){
            if(!rs.next())throw new SQLException("Không thể đọc SQL Server");
        }
    }
    public static void kiemTraCauTruc() throws SQLException {
        String[] tables={
            "VaiTro","TaiKhoan","QuanLy","NhanVien","BenhNhan","DichVu","BangGiaDichVu","LichLamViec","LichHen","PhongKham","PhieuKham","ChiTietDieuTri","ThanhToan","HoaDon","YeuCauHuyPhieuThu","PhieuDuyetHuyPhieuThu","ChiPhiHoatDong","NhatKyHeThong"
        }
        ;
        try(Connection c=mo();PreparedStatement p=c.prepareStatement("SELECT OBJECT_ID(?, N'U')")) {
            for(String table:tables){
                p.setString(1,"dbo."+table);
                try(ResultSet rs=p.executeQuery()){
                    if(!rs.next()||rs.getObject(1)==null)throw new SQLException("Thiếu bảng "+table+". Hãy chạy SQL 01, 07, 08, 13 theo hướng dẫn.");
                }
            }
            try(PreparedStatement col=c.prepareStatement("SELECT COL_LENGTH(?,?)")){
                String[][] columns={
                    {
                        "BenhNhan","trangThaiHoSo"
                    }
                    ,{
                        "LichHen","thoiLuongPhut"
                    }
                    ,{
                        "ThanhToan","ngayThanhToan"
                    }
                    ,{
                        "YeuCauHuyPhieuThu","maTKNguoiGui"
                    }
                }
                ;
                for(String[] x:columns){
                    col.setString(1,"dbo."+x[0]);
                    col.setString(2,x[1]);
                    try(ResultSet rs=col.executeQuery()){
                        if(!rs.next()||rs.getObject(1)==null)throw new SQLException("Thiếu cột "+x[0]+"."+x[1]+". Chạy SqlData/13_NangCap_QuanLy_KhongXoaDuLieu.sql.");
                    }
                }
            }
        }
    }
    /** Có thể chạy trực tiếp class này để kiểm tra SQL mà không mở giao diện. */
    public static void main(String[] args) {
        try {
            System.out.println("[MEDIDENT] Nguon cau hinh: " + CauHinh.duongDanDangDoc());
            kiemTraCauTruc();
            System.out.println("[MEDIDENT] Kết nối và kiểm tra cấu trúc SQL thành công.");
        }
        catch (Exception e) {
            System.err.println("[MEDIDENT] Lỗi kết nối/cấu trúc: " + e.getMessage());
            System.exit(1);
        }
    }
}
