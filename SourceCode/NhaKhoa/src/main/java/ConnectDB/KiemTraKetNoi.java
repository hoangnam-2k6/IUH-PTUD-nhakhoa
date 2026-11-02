package ConnectDB;

import java.sql.*;
import java.util.*;


public class KiemTraKetNoi {
    private static final Map<String,String[]> COT_CAN_CO=new LinkedHashMap<>();
    static {
        COT_CAN_CO.put("VaiTro",new String[]{"maVaiTro","tenVaiTro","moTa"});
        COT_CAN_CO.put("TaiKhoan",new String[]{"maTK","tenDangNhap","matKhau","trangThai","ngayTao","ngayDangNhapCuoi","maVaiTro"});
        COT_CAN_CO.put("NhanVien",new String[]{"maNV","hoTen","email","chucVu","maTK"});
        COT_CAN_CO.put("NhatKyHeThong",new String[]{"maNK","thoiGian","hanhDong","doiTuong","moTa"});
        COT_CAN_CO.put("PhieuKham",new String[]{"maPK","maBN","maNV","ngayKham"});
        COT_CAN_CO.put("ThanhToan",new String[]{"maTT","maPK","soTien","trangThai"});
        COT_CAN_CO.put("HoaDon",new String[]{"maHD","maTT","maPK","tongTien"});
        COT_CAN_CO.put("Quyen",new String[]{"maQuyen","tenQuyen"});
        COT_CAN_CO.put("VaiTroQuyen",new String[]{"maVaiTro","maQuyen"});
        COT_CAN_CO.put("CauHinh",new String[]{"Khoa","GiaTri"});
        // Kiểm tra các bảng cốt lõi theo ERD và 3 vai trò.
        COT_CAN_CO.put("BenhNhan",new String[]{"maBN","hoTen","trangThaiHoSo"});
        COT_CAN_CO.put("LichHen",new String[]{"maLichHen","maBN","maNV","phongKham","ngayHen","gioHen","trangThai","thoiLuongPhut"});
        COT_CAN_CO.put("LichLamViec",new String[]{"maLichLV","maNV","ngayLamViec","gioBatDau","gioKetThuc","trangThai"});
        COT_CAN_CO.put("PhongKham",new String[]{"maPhong","tenPhong","sucChua","trangThai"});
        COT_CAN_CO.put("DichVu",new String[]{"maDV","tenDV","donGia","trangThai"});
        COT_CAN_CO.put("BangGiaDichVu",new String[]{"maBG","maDV","donGia"});
        COT_CAN_CO.put("ChiTietDieuTri",new String[]{"maCT","maPK","soLuong","donGia","thanhTien"});
        COT_CAN_CO.put("YeuCauHoTro",new String[]{"maYC","maTK","tieuDe","noiDung","trangThai"});
        COT_CAN_CO.put("YeuCauHuyPhieuThu",new String[]{"maYC","maTT","trangThai"});
        COT_CAN_CO.put("PhieuDuyetHuyPhieuThu",new String[]{"maPD","maYC"});
        COT_CAN_CO.put("ChiPhiHoatDong",new String[]{"maCP","soTien"});
    }

    public static void main(String[] args) {
     //In nguồn cấu hình thực tế trước khi thử kết nối SQL.
        // Không hiển thị mật khẩu hoặc JDBC URL đầy đủ.
        try {
            System.out.println(KetNoi.chanDoanCauHinh());
        } catch (SQLException ex) {
            System.err.println("Không đọc được cấu hình SQL: " + ex.getMessage());
        }
        try(Connection c=KetNoi.mo()) {
            System.out.println("Kết nối SQL Server: THÀNH CÔNG");
            System.out.println("Database: "+c.getCatalog());
            DatabaseMetaData db=c.getMetaData();
            boolean good=true;
            for(Map.Entry<String,String[]> entry:COT_CAN_CO.entrySet()) {
                Set<String> columns=new HashSet<>();
                try(ResultSet r=db.getColumns(c.getCatalog(),"dbo",entry.getKey(),null)){
                    while(r.next())columns.add(r.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
                }
                if(columns.isEmpty()){
                    System.out.println("[THIẾU BẢNG] dbo."+entry.getKey());good=false;continue;
                }
                for(String col:entry.getValue()) {
                    if(!columns.contains(col.toLowerCase(Locale.ROOT))) {
                        System.out.println("[THIẾU CỘT] dbo."+entry.getKey()+"."+col);
                        good=false;
                    }
                }
            }
         
            String[] roles={"Quản trị viên", "Quản lý phòng khám", "Lễ tân"};
            for (String role:roles) {
                try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM dbo.VaiTro WHERE tenVaiTro=?")) {
                    p.setString(1,role);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) { System.out.println("[THIẾU VAI TRÒ] " + role); good=false; }
                    }
                }
            }
            
        }catch(SQLException ex){
            System.err.println("Không kết nối được SQL Server: "+ex.getMessage());
            System.err.println("Kiểm tra db.properties, TCP/IP, port 1433, SQL Authentication.");
        }
    }
}
