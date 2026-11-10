package DAO;

import ConnectDB.ConnectDB;
import Entity.LichHen;
import enums.TrangThaiLichHen;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Nghiệp vụ Lịch hẹn, không chứa thành phần Swing. */
public class LichHenDAO {
    public static final String CHO_XAC_NHAN = TrangThaiLichHen.CHO_XAC_NHAN.toString();
    public static final String DA_XAC_NHAN = TrangThaiLichHen.DA_XAC_NHAN.toString();
    public static final String DA_HUY = TrangThaiLichHen.DA_HUY.toString();
    public static final String DA_KHAM = TrangThaiLichHen.DA_KHAM.toString();

    public ArrayList<LichHen> getalllichhen() throws SQLException { return timLichHen(""); }
    public List<LichHen> danhSach(String tuKhoa) throws SQLException { return timLichHen(tuKhoa); }
    public void kiemTraBang() throws SQLException {
        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            try (ResultSet rs = con.createStatement().executeQuery("SELECT OBJECT_ID(N'dbo.LichHen',N'U')")) {
                if (!rs.next() || rs.getObject(1) == null) throw new SQLException("Chưa có bảng LichHen. Hãy đồng bộ SQL.");
            }
        }
    }

    public ArrayList<LichHen> timLichHen(String tuKhoa) throws SQLException {
        String sql = "SELECT l.maLichHen,l.maBN,l.maNV,l.ngayHen,l.gioHen,l.thoiLuongPhut,l.phongKham,l.trangThai "
                + "FROM dbo.LichHen l LEFT JOIN dbo.BenhNhan b ON b.maBN=l.maBN "
                + "LEFT JOIN dbo.NhanVien n ON n.maNV=l.maNV "
                + "WHERE CAST(l.maLichHen AS NVARCHAR(20)) LIKE ? OR l.maBN LIKE ? OR ISNULL(l.maNV,N'') LIKE ? "
                + "OR ISNULL(l.phongKham,N'') LIKE ? OR ISNULL(b.hoten,N'') LIKE ? OR ISNULL(n.hoten,N'') LIKE ? "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC,l.maLichHen DESC";
        ArrayList<LichHen> ds = new ArrayList<>();
        try (Connection con = ConnectDB.taoKetNoiMoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            String tim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
            for (int i = 1; i <= 6; i++) ps.setString(i, tim);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) ds.add(doc(rs)); }
        }
        return ds;
    }

    public LichHen getLichHenByMaLichHen(int ma) throws SQLException {
        String sql = "SELECT maLichHen,maBN,maNV,ngayHen,gioHen,thoiLuongPhut,phongKham,trangThai FROM dbo.LichHen WHERE maLichHen=?";
        try (Connection con = ConnectDB.taoKetNoiMoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ma);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? doc(rs) : null; }
        }
    }

    private LichHen doc(ResultSet rs) throws SQLException {
        return new LichHen(rs.getInt("maLichHen"), rs.getString("maBN"), rs.getString("maNV"),
                rs.getDate("ngayHen").toLocalDate(), rs.getTime("gioHen").toLocalTime(),
                rs.getInt("thoiLuongPhut"), rs.getString("phongKham"), rs.getString("trangThai"));
    }

    public List<String[]> danhSachBenhNhan() throws SQLException {
        return docDanhMuc("SELECT maBN,hoten FROM dbo.BenhNhan ORDER BY maBN");
    }
    public List<String[]> danhSachBacSi() throws SQLException {
        return docDanhMuc("SELECT maNV,hoten FROM dbo.NhanVien WHERE chucvu LIKE N'%Bác sĩ%' AND trangthai=N'DANGLAMVIEC' ORDER BY maNV");
    }
    private List<String[]> docDanhMuc(String sql) throws SQLException {
        List<String[]> ds = new ArrayList<>();
        try (Connection con = ConnectDB.taoKetNoiMoi(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ds.add(new String[]{rs.getString(1),rs.getString(2)});
        }
        return ds;
    }

    public void luu(LichHen lich, boolean laSua) throws SQLException {
        kiemTraNhap(lich, laSua);
        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);
            try {
                kiemTraBenhNhanBacSi(con, lich);
                kiemTraCa(con, lich);
                if (laSua) kiemTraCoTheSua(con, lich.maLichHen());
                kiemTraTrung(con, lich);
                String sql = laSua
                        ? "UPDATE dbo.LichHen SET maBN=?,maNV=?,ngayHen=?,gioHen=?,thoiLuongPhut=?,phongKham=? WHERE maLichHen=?"
                        : "INSERT INTO dbo.LichHen(maBN,maNV,ngayHen,gioHen,thoiLuongPhut,phongKham,trangThai) VALUES(?,?,?,?,?,?,N'Chờ xác nhận')";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, lich.maBN()); ps.setString(2, lich.maNV());
                    ps.setDate(3, java.sql.Date.valueOf(lich.ngayHen())); ps.setTime(4, Time.valueOf(lich.gioHen()));
                    ps.setInt(5, lich.thoiLuongPhut()); ps.setString(6, lich.phongKham().trim());
                    if (laSua) ps.setInt(7, lich.maLichHen());
                    if (ps.executeUpdate() != 1) throw new SQLException("Không lưu được lịch hẹn.");
                }
                con.commit();
            } catch (SQLException | RuntimeException loi) { con.rollback(); throw loi; }
        }
    }

    private void kiemTraNhap(LichHen l, boolean laSua) {
        if (l == null || l.maBN() == null || l.maBN().isBlank() || l.maNV() == null || l.maNV().isBlank())
            throw new IllegalArgumentException("Phải chọn bệnh nhân và bác sĩ.");
        if (l.ngayHen() == null || l.gioHen() == null || l.phongKham() == null || l.phongKham().isBlank())
            throw new IllegalArgumentException("Ngày, giờ và phòng khám không được để trống.");
        if (l.thoiLuongPhut() < 15 || l.thoiLuongPhut() > 240) throw new IllegalArgumentException("Thời lượng phải từ 15 đến 240 phút.");
        LocalDateTime dau = LocalDateTime.of(l.ngayHen(),l.gioHen());
        if (!dau.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Lịch hẹn mới phải ở tương lai.");
        if (!dau.plusMinutes(l.thoiLuongPhut()).toLocalDate().equals(l.ngayHen()))
            throw new IllegalArgumentException("Lịch hẹn không thể kéo dài sang ngày tiếp theo.");
        if (laSua && l.maLichHen() <= 0) throw new IllegalArgumentException("Mã lịch hẹn không hợp lệ.");
    }

    private void kiemTraBenhNhanBacSi(Connection con, LichHen l) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM dbo.BenhNhan WHERE maBN=?")) {
            ps.setString(1,l.maBN());
            try (ResultSet rs=ps.executeQuery()) { if (!rs.next()) throw new SQLException("Mã bệnh nhân không tồn tại: " + l.maBN()); }
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT chucvu,trangthai FROM dbo.NhanVien WHERE maNV=?")) {
            ps.setString(1,l.maNV());
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Bác sĩ không tồn tại: " + l.maNV());
                if (rs.getString(1) == null || !rs.getString(1).toLowerCase(Locale.ROOT).contains("bác sĩ"))
                    throw new SQLException("Nhân viên được chọn không phải bác sĩ.");
                if (!"DANGLAMVIEC".equalsIgnoreCase(rs.getString(2)))
                    throw new SQLException("Bác sĩ không ở trạng thái đang làm việc.");
            }
        }
    }

    private void kiemTraCa(Connection con,LichHen l) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.LichLamViec WITH(UPDLOCK,HOLDLOCK) WHERE maNV=? AND ngayLamViec=? AND trangThai=N'Hoạt động' "
                + "AND gioBatDau<=? AND gioKetThuc>=? AND (phongKham IS NULL OR phongKham=?)";
        try (PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setString(1,l.maNV()); ps.setDate(2,java.sql.Date.valueOf(l.ngayHen()));
            ps.setTime(3,Time.valueOf(l.gioHen())); ps.setTime(4,Time.valueOf(l.gioHen().plusMinutes(l.thoiLuongPhut())));
            ps.setString(5,l.phongKham());
            try (ResultSet rs=ps.executeQuery()) {
                rs.next(); if (rs.getInt(1)==0) throw new SQLException("Chưa có ca làm việc phù hợp cho bác sĩ. Hãy tạo ca tại Lịch làm việc trước.");
            }
        }
    }

    private void kiemTraCoTheSua(Connection con,int ma) throws SQLException {
        try (PreparedStatement ps=con.prepareStatement("SELECT trangThai FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?")) {
            ps.setInt(1,ma);
            try (ResultSet rs=ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                String tt=rs.getString(1);
                if (!CHO_XAC_NHAN.equals(tt) && !DA_XAC_NHAN.equals(tt))
                    throw new SQLException("Không thể sửa lịch đã khám hoặc đã hủy.");
            }
        }
    }

    private void kiemTraTrung(Connection con,LichHen l) throws SQLException {
        String phut="DATEDIFF(MINUTE,CAST('00:00' AS TIME),gioHen)";
        String sql="SELECT COUNT(*) FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE ngayHen=? AND maLichHen<>? "
                + "AND trangThai IN (N'Chờ xác nhận',N'Đã xác nhận') AND (maBN=? OR maNV=? OR phongKham=?) "
                + "AND " + phut + "<? AND " + phut + "+thoiLuongPhut>?";
        int dau=l.gioHen().getHour()*60+l.gioHen().getMinute();
        try (PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setDate(1,java.sql.Date.valueOf(l.ngayHen())); ps.setInt(2,l.maLichHen());
            ps.setString(3,l.maBN()); ps.setString(4,l.maNV()); ps.setString(5,l.phongKham());
            ps.setInt(6,dau+l.thoiLuongPhut()); ps.setInt(7,dau);
            try (ResultSet rs=ps.executeQuery()) {
                rs.next(); if (rs.getInt(1)>0) throw new SQLException("Trùng giờ bác sĩ, bệnh nhân hoặc phòng khám.");
            }
        }
    }

    public void huy(int ma) throws SQLException {
        String sql="UPDATE dbo.LichHen SET trangThai=N'Đã hủy' WHERE maLichHen=? AND trangThai IN (N'Chờ xác nhận',N'Đã xác nhận')";
        try (Connection con=ConnectDB.taoKetNoiMoi();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setInt(1,ma); if (ps.executeUpdate()!=1) throw new SQLException("Không thể hủy lịch đã khám hoặc đã hủy.");
        }
    }

    public void doiTrangThai(int ma,String trangThaiMoi) throws SQLException {
        if (!DA_XAC_NHAN.equals(trangThaiMoi) && !DA_KHAM.equals(trangThaiMoi))
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        try (Connection con=ConnectDB.taoKetNoiMoi()) {
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);
            try {
                LichHen lich;
                String sqlChon="SELECT maLichHen,maBN,maNV,ngayHen,gioHen,thoiLuongPhut,phongKham,trangThai FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?";
                try (PreparedStatement ps=con.prepareStatement(sqlChon)) {
                    ps.setInt(1,ma);
                    try (ResultSet rs=ps.executeQuery()) { lich=rs.next()?doc(rs):null; }
                }
                if (lich==null) throw new SQLException("Không tìm thấy lịch hẹn.");
                String cu=DA_XAC_NHAN.equals(trangThaiMoi)?CHO_XAC_NHAN:DA_XAC_NHAN;
                if (!cu.equals(lich.trangThai())) throw new SQLException("Phải chuyển lần lượt: Chờ xác nhận → Đã xác nhận → Đã khám.");
                if (DA_KHAM.equals(trangThaiMoi)) {
                    if (LocalDateTime.of(lich.ngayHen(),lich.gioHen()).isAfter(LocalDateTime.now()))
                        throw new SQLException("Lịch chưa đến giờ khám.");
                    String themPhieu="INSERT INTO dbo.PhieuKham(maBN,maNV,maLichHen,ngayKham,trangThai) "
                            + "SELECT ?,?,?,SYSDATETIME(),N'Hoàn thành' WHERE NOT EXISTS "
                            + "(SELECT 1 FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?)";
                    try (PreparedStatement ps=con.prepareStatement(themPhieu)) {
                        ps.setString(1,lich.maBN()); ps.setString(2,lich.maNV());ps.setInt(3,ma);ps.setInt(4,ma);
                        if (ps.executeUpdate()!=1) throw new SQLException("Lịch đã có phiếu khám; không tạo trùng.");
                    }
                }
                try (PreparedStatement ps=con.prepareStatement("UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=? AND trangThai=?")) {
                    ps.setString(1,trangThaiMoi);ps.setInt(2,ma);ps.setString(3,cu);
                    if (ps.executeUpdate()!=1) throw new SQLException("Lịch hẹn đã thay đổi trạng thái.");
                }
                con.commit();
            } catch (SQLException | RuntimeException loi) { con.rollback();throw loi; }
        }
    }

    /** Giữ API thống kê cũ cho tương thích; báo cáo mới sử dụng ThongKeDAO và PhieuKham. */
    public List<Object[]> thongKeLichDaKham(LocalDate tu,LocalDate den,String theo) throws SQLException {
        String ky= switch (theo) {
            case "Tháng" -> "CONVERT(VARCHAR(7),ngayHen,23)";
            case "Năm" -> "CONVERT(VARCHAR(4),YEAR(ngayHen))";
            default -> "CONVERT(VARCHAR(10),ngayHen,23)";
        };
        List<Object[]> ds=new ArrayList<>();
        String sql="SELECT "+ky+" AS ky,COUNT(*) AS soLuot FROM dbo.LichHen WHERE trangThai=N'Đã khám' "
                + "AND ngayHen>=? AND ngayHen<=? GROUP BY "+ky+" ORDER BY ky";
        try (Connection con=ConnectDB.taoKetNoiMoi(); PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setDate(1,java.sql.Date.valueOf(tu));ps.setDate(2,java.sql.Date.valueOf(den));
            try (ResultSet rs=ps.executeQuery()) { while (rs.next()) ds.add(new Object[]{rs.getString(1),rs.getInt(2)}); }
        }
        return ds;
    }
}
