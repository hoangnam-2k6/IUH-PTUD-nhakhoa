package DAO;
import ConnectDB.ConnectDB;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
/** Tổng hợp dữ liệu cho bảng báo cáo có sẵn. */
public final class ThongKeMoRongDAO {
    public record Bang(String[] cot, List<Object[]> dong) {}
    public Bang baoCao(String loai,String nhom,LocalDate tu,LocalDate den) throws SQLException {
        String ngay="Doanh thu".equals(loai)?"NgayLap":"NgayKham";
        String ky=switch(nhom) {
            case "Năm" -> "CONVERT(VARCHAR(4),YEAR("+ngay+"))";
            case "Tháng" -> "CONVERT(VARCHAR(7),"+ngay+",23)";
            default -> "CONVERT(VARCHAR(10),"+ngay+",23)";
        };
        boolean thu="Doanh thu".equals(loai);
        String bang=thu?"dbo.HoaDon":"dbo.PhieuKham";
        String sql=thu?
                "SELECT "+ky+" Ky, SUM(CASE WHEN TrangThai=N'Đã thanh toán' THEN 1 ELSE 0 END) Thu, "
                +"SUM(CASE WHEN TrangThai=N'Chưa thanh toán' THEN 1 ELSE 0 END) Cho, "
                +"ISNULL(SUM(CASE WHEN TrangThai=N'Đã thanh toán' THEN TongTien ELSE 0 END),0) Tien "
                +"FROM "+bang+" WHERE (? IS NULL OR CONVERT(DATE,"+ngay+")>=?) AND (? IS NULL OR CONVERT(DATE,"+ngay+")<=?) GROUP BY "+ky+" ORDER BY Ky":
                "SELECT "+ky+" Ky, COUNT(*) Tong, SUM(CASE WHEN TrangThai=N'Chờ khám' THEN 1 ELSE 0 END) Cho, "
                +"SUM(CASE WHEN TrangThai=N'Hoàn thành' THEN 1 ELSE 0 END) HoanThanh FROM "+bang
                +" WHERE (? IS NULL OR CONVERT(DATE,"+ngay+")>=?) AND (? IS NULL OR CONVERT(DATE,"+ngay+")<=?) GROUP BY "+ky+" ORDER BY Ky";
        List<Object[]> ds=new ArrayList<>();
        try(Connection c=ConnectDB.taoKetNoiMoi();PreparedStatement p=c.prepareStatement(sql)) {
                if(tu==null){p.setNull(1,Types.DATE);p.setNull(2,Types.DATE);} else {p.setDate(1,java.sql.Date.valueOf(tu));p.setDate(2,java.sql.Date.valueOf(tu));}
                if(den==null){p.setNull(3,Types.DATE);p.setNull(4,Types.DATE);} else {p.setDate(3,java.sql.Date.valueOf(den));p.setDate(4,java.sql.Date.valueOf(den));}
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    int i=ds.size()+1;
                    if(thu) ds.add(new Object[]{i,r.getString(1),r.getInt(2),r.getInt(3),r.getBigDecimal(4)});
                    else ds.add(new Object[]{i,r.getString(1),r.getInt(2),r.getInt(3),r.getInt(4)});
                }
            }
        }
        return new Bang(thu?new String[]{"STT","Kỳ báo cáo","Hóa đơn đã thu","Hóa đơn chờ","Doanh thu (VNĐ)"}
            :new String[]{"STT","Kỳ báo cáo","Tổng phiếu","Chờ khám","Hoàn thành"},ds);
    }
}
