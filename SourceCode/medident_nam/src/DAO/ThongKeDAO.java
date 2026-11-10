package DAO;

import ConnectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Tổng hợp doanh thu thực thu hoặc lượt khám hoàn thành; trả dữ liệu cho Panel. */
public class ThongKeDAO {
    public List<Object[]> layBaoCao(boolean doanhThu,String theo,LocalDate tuNgay,LocalDate denNgay) throws SQLException {
        if ((tuNgay==null)!=(denNgay==null) || (tuNgay!=null&&tuNgay.isAfter(denNgay)))
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        String ngay=doanhThu ? "t.ngayThanhToan" : "p.ngayKham";
        String ky=switch(theo){
            case "Ngày" -> "CONVERT(VARCHAR(10),"+ngay+",23)";
            case "Tháng" -> "CONVERT(VARCHAR(7),"+ngay+",23)";
            case "Năm" -> "CONVERT(VARCHAR(4),YEAR("+ngay+"))";
            default -> throw new IllegalArgumentException("Kiểu báo cáo phải là Ngày, Tháng hoặc Năm.");
        };
        String sql=doanhThu
                ? "SELECT "+ky+" AS ky,COUNT(*) AS soLuong,COALESCE(SUM(t.soTien),0) AS tongTien "
                + "FROM dbo.ThanhToan t JOIN dbo.HoaDon h ON h.maTT=t.maTT "
                + "WHERE t.trangThai=N'Thành công' AND h.trangThai=N'Đã thanh toán' "
                : "SELECT "+ky+" AS ky,COUNT(*) AS soLuong,CAST(0 AS DECIMAL(18,2)) AS tongTien "
                + "FROM dbo.PhieuKham p WHERE p.trangThai IN (N'Hoàn thành',N'Đã khám') ";
        if(tuNgay!=null)sql+="AND "+ngay+">=? AND "+ngay+"<DATEADD(DAY,1,?) ";
        sql+="GROUP BY "+ky+" ORDER BY ky";
        List<Object[]> ds=new ArrayList<>();
        try(Connection con=ConnectDB.taoKetNoiMoi();PreparedStatement ps=con.prepareStatement(sql)){
            if(tuNgay!=null){ps.setDate(1,java.sql.Date.valueOf(tuNgay));ps.setDate(2,java.sql.Date.valueOf(denNgay));}
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next())ds.add(new Object[]{rs.getString("ky"),rs.getInt("soLuong"),
                        Optional.ofNullable(rs.getBigDecimal("tongTien")).orElse(BigDecimal.ZERO)});
            }
        }
        return ds;
    }
}
