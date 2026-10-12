package ConnectDB;

import java.sql.*;
import java.util.*;

/**
 * Chay class nay de KIEM TRA ket noi, ten bang, cot; chi SELECT, khong thay doi CSDL.
 * Eclipse: Run As > Java Application > ConnectDB.KiemTraKetNoi
 */
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
    }

    public static void main(String[] args) {
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
            System.out.println(good?"ERD module Admin: đã có các cột cần thiết.":
                "ERD module Admin: thiếu bảng hoặc cột, hãy đối chiếu SQLData/01_MediDent.sql.");
        }catch(SQLException ex){
            System.err.println("Không kết nối được SQL Server: "+ex.getMessage());
            System.err.println("Kiểm tra db.properties, TCP/IP, port 1433, SQL Authentication.");
        }
    }
}
