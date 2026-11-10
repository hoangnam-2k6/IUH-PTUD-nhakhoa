package DAO;

import ConnectDB.ConnectDB;
import Entity.LichLamViec;
import enums.TrangThaiLichLamViec;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Xử lý tạo, sửa, hủy và kiểm tra ca làm việc. */
public class LichLamViecDAO {
    public List<LichLamViec> danhSach(String tuKhoa) throws SQLException {
        String sql="SELECT l.maLLV,l.maNV,l.ngayLamViec,l.gioBatDau,l.gioKetThuc,l.phongKham,l.trangThai "
                + "FROM dbo.LichLamViec l LEFT JOIN dbo.NhanVien n ON n.maNV=l.maNV "
                + "WHERE CAST(l.maLLV AS NVARCHAR(20)) LIKE ? OR l.maNV LIKE ? OR ISNULL(n.hoten,N'') LIKE ? "
                + "OR ISNULL(l.phongKham,N'') LIKE ? ORDER BY l.ngayLamViec DESC,l.gioBatDau,l.maLLV DESC";
        List<LichLamViec> ds=new ArrayList<>();
        try (Connection con=ConnectDB.taoKetNoiMoi();PreparedStatement ps=con.prepareStatement(sql)) {
            String tim="%"+(tuKhoa==null?"":tuKhoa.trim())+"%";
            for(int i=1;i<=4;i++)ps.setString(i,tim);
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) ds.add(new LichLamViec(rs.getInt(1),rs.getString(2),rs.getDate(3).toLocalDate(),
                        rs.getTime(4).toLocalTime(),rs.getTime(5).toLocalTime(),rs.getString(6),rs.getString(7)));
            }
        }
        return ds;
    }

    public List<String[]> danhSachNhanVien() throws SQLException {
        List<String[]> ds=new ArrayList<>();
        String sql="SELECT maNV,hoten FROM dbo.NhanVien WHERE trangthai=N'DANGLAMVIEC' ORDER BY maNV";
        try (Connection con=ConnectDB.taoKetNoiMoi();Statement st=con.createStatement();ResultSet rs=st.executeQuery(sql)) {
            while(rs.next()) ds.add(new String[]{rs.getString(1),rs.getString(2)});
        }
        return ds;
    }

    public void luu(LichLamViec l,boolean laSua) throws SQLException {
        if (l==null || l.maNV()==null || l.maNV().isBlank() || l.ngayLamViec()==null || l.gioBatDau()==null || l.gioKetThuc()==null)
            throw new IllegalArgumentException("Hãy chọn nhân viên, ngày làm và giờ bắt đầu/kết thúc.");
        if (!l.gioBatDau().isBefore(l.gioKetThuc())) throw new IllegalArgumentException("Giờ bắt đầu phải nhỏ hơn giờ kết thúc.");
        if (l.trangThai()==null || Arrays.stream(TrangThaiLichLamViec.values()).noneMatch(t->t.toString().equals(l.trangThai())))
            throw new IllegalArgumentException("Trạng thái ca không hợp lệ.");
        if (laSua && l.maLLV()<=0) throw new IllegalArgumentException("Mã ca không hợp lệ.");
        try(Connection con=ConnectDB.taoKetNoiMoi()) {
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);
            try {
                try(PreparedStatement ps=con.prepareStatement("SELECT trangthai FROM dbo.NhanVien WHERE maNV=?")) {
                    ps.setString(1,l.maNV());
                    try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("Nhân viên không tồn tại.");
                        if(!"DANGLAMVIEC".equalsIgnoreCase(rs.getString(1))) throw new SQLException("Nhân viên đang nghỉ/tạm nghỉ.");
                    }
                }
                if(laSua) kiemTraLichHenBiAnhHuong(con,l.maLLV());
                if(TrangThaiLichLamViec.HOAT_DONG.toString().equals(l.trangThai())) kiemTraTrungCa(con,l);
                String sql=laSua ? "UPDATE dbo.LichLamViec SET maNV=?,ngayLamViec=?,gioBatDau=?,gioKetThuc=?,phongKham=?,trangThai=? WHERE maLLV=?"
                        : "INSERT INTO dbo.LichLamViec(maNV,ngayLamViec,gioBatDau,gioKetThuc,phongKham,trangThai) VALUES(?,?,?,?,?,?)";
                try(PreparedStatement ps=con.prepareStatement(sql)) {
                    ps.setString(1,l.maNV());ps.setDate(2,java.sql.Date.valueOf(l.ngayLamViec()));
                    ps.setTime(3,Time.valueOf(l.gioBatDau()));ps.setTime(4,Time.valueOf(l.gioKetThuc()));
                    ps.setString(5,l.phongKham()==null||l.phongKham().isBlank()?null:l.phongKham().trim());
                    ps.setString(6,l.trangThai());if(laSua)ps.setInt(7,l.maLLV());
                    if(ps.executeUpdate()!=1)throw new SQLException("Không lưu được lịch làm việc.");
                }
                con.commit();
            }catch(SQLException|RuntimeException loi){con.rollback();throw loi;}
        }
    }

    private void kiemTraTrungCa(Connection con,LichLamViec l) throws SQLException {
        String sql="SELECT COUNT(*) FROM dbo.LichLamViec WITH(UPDLOCK,HOLDLOCK) WHERE maLLV<>? AND ngayLamViec=? "
                + "AND trangThai=N'Hoạt động' AND gioBatDau<? AND gioKetThuc>? "
                + "AND (maNV=? OR (phongKham IS NOT NULL AND phongKham=?))";
        try(PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,l.maLLV());ps.setDate(2,java.sql.Date.valueOf(l.ngayLamViec()));
            ps.setTime(3,Time.valueOf(l.gioKetThuc()));ps.setTime(4,Time.valueOf(l.gioBatDau()));
            ps.setString(5,l.maNV());ps.setString(6,l.phongKham());
            try(ResultSet rs=ps.executeQuery()) {rs.next();if(rs.getInt(1)>0)throw new SQLException("Trùng ca của nhân viên hoặc phòng khám.");}
        }
    }

    private void kiemTraLichHenBiAnhHuong(Connection con,int maLLV) throws SQLException {
        String sql="SELECT COUNT(*) FROM dbo.LichHen h WITH(UPDLOCK,HOLDLOCK) JOIN dbo.LichLamViec l WITH(UPDLOCK,HOLDLOCK) ON l.maNV=h.maNV "
                + "AND l.ngayLamViec=h.ngayHen AND l.gioBatDau<=h.gioHen "
                + "AND DATEDIFF(MINUTE,CAST('00:00' AS TIME),l.gioKetThuc)>=DATEDIFF(MINUTE,CAST('00:00' AS TIME),h.gioHen)+h.thoiLuongPhut "
                + "WHERE l.maLLV=? AND h.trangThai IN (N'Chờ xác nhận',N'Đã xác nhận')";
        try(PreparedStatement ps=con.prepareStatement(sql)){
            ps.setInt(1,maLLV);
            try(ResultSet rs=ps.executeQuery()){
                rs.next();if(rs.getInt(1)>0)throw new SQLException("Ca đã có lịch hẹn đang hiệu lực, không được sửa/hủy. Hãy xử lý lịch hẹn trước.");
            }
        }
    }

    public void huy(int maLLV) throws SQLException {
        if(maLLV<=0)throw new IllegalArgumentException("Chưa chọn ca cần hủy.");
        try(Connection con=ConnectDB.taoKetNoiMoi()){
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);
            try{
                kiemTraLichHenBiAnhHuong(con,maLLV);
                try(PreparedStatement ps=con.prepareStatement("UPDATE dbo.LichLamViec SET trangThai=N'Đã hủy' WHERE maLLV=? AND trangThai<>N'Đã hủy'")){
                    ps.setInt(1,maLLV);if(ps.executeUpdate()!=1)throw new SQLException("Ca không tồn tại hoặc đã hủy.");
                }
                con.commit();
            }catch(SQLException|RuntimeException loi){con.rollback();throw loi;}
        }
    }
}
