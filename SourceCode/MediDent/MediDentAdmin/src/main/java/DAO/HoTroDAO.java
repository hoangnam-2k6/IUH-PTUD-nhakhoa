package DAO;
import ConnectDB.KetNoi;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Chỉ chứa nghiệp vụ hỗ trợ nội bộ. */
public final class HoTroDAO {
    public static final class Hang {
        public final int id;
        public final String[] cot;
        public Hang(int id, String... cot) { this.id = id; this.cot = cot; }
    }

    private interface DocHang { Hang doc(ResultSet rs) throws SQLException; }

    private List<Hang> truyVan(String sql, DocHang doc, Object... args) throws SQLException {
        List<Hang> ketQua = new ArrayList<>();
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql)) {
            datThamSo(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ketQua.add(doc.doc(rs));
            }
        }
        return ketQua;
    }

    private static void datThamSo(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof LocalDate) ps.setDate(i + 1, java.sql.Date.valueOf((LocalDate) arg));
            else if (arg instanceof LocalTime) ps.setTime(i + 1, Time.valueOf((LocalTime) arg));
            else ps.setObject(i + 1, arg);
        }
    }

    private static String chuoi(ResultSet rs, String key) throws SQLException {
        String value = rs.getString(key);
        return value == null ? "" : value;
    }

    private static void batBuoc(String value, String message) throws SQLException {
        if (value == null || value.trim().isEmpty()) throw new SQLException(message);
    }

    private static void ghi(Connection c, int actor, String hanhDong, String doiTuong, String moTa)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            ps.setString(1, hanhDong);
            ps.setString(2, doiTuong);
            ps.setString(3, "maTK=" + actor + " | " + moTa);
            ps.executeUpdate();
        }
    }

    private static int capBacHoTro(Connection c, int actor) throws SQLException {
        String sql = "SELECT v.capBacHoTro FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Tài khoản bị khóa hoặc không tồn tại.");
                }
                return rs.getInt(1);
            }
        }
    }

    private static boolean coQuyen(Connection c, int actor, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM dbo.TaiKhoan t "
              + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
              + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND q.maQuyen=?")) {
            ps.setInt(1, actor);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean coQuyenPhanHoiHoTro(int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            return capBacHoTro(c, actor) >= 2 && coQuyen(c, actor, "HOTRO");
        }
    }

    public void guiHoTro(int actor,String tieuDe,String noiDung) throws SQLException {
        batBuoc(tieuDe,"Nhập tiêu đề yêu cầu."); batBuoc(noiDung,"Nhập nội dung cần hỗ trợ.");
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement valid=c.prepareStatement(
                        "SELECT 1 FROM dbo.TaiKhoan WITH (UPDLOCK,HOLDLOCK) WHERE maTK=? AND trangThai=N'Hoạt động'")) {
                    valid.setInt(1,actor);
                    try(ResultSet r=valid.executeQuery()) {
                        if(!r.next()) throw new SQLException("Tài khoản đã bị khóa hoặc đăng xuất.");
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT dbo.YeuCauHoTro(maTK,tieuDe,noiDung) VALUES(?,?,?)")) {
                    datThamSo(ps,actor,tieuDe.trim(),noiDung.trim());
                    if(ps.executeUpdate()!=1) throw new SQLException("Không gửi được yêu cầu hỗ trợ.");
                }
                ghi(c,actor,"Gửi yêu cầu hỗ trợ","YeuCauHoTro",tieuDe.trim());
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<Hang> danhSachHoTro(int actor) throws SQLException {
        String sql = "SELECT y.maYC,ISNULL(n.hoTen,t.tenDangNhap) AS nguoi," 
                + "CONVERT(varchar(19),y.ngayTao,120) AS ngay," 
                + "y.tieuDe,y.trangThai,ISNULL(y.noiDung,'') AS noiDung," 
                + "ISNULL(y.phanHoi,'') AS phanHoi, v.tenVaiTro AS vaiTro "
                + "FROM dbo.YeuCauHoTro y "
                + "JOIN dbo.TaiKhoan t ON t.maTK=y.maTK "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                + "JOIN dbo.TaiKhoan me ON me.maTK=? AND me.trangThai=N'Hoạt động' "
                + "JOIN dbo.VaiTro myRole ON myRole.maVaiTro=me.maVaiTro "
                + "WHERE (myRole.tenVaiTro=N'Quản trị viên' OR y.maTK=? OR (myRole.capBacHoTro>0 "
                + "AND v.capBacHoTro>0 AND v.capBacHoTro<=myRole.capBacHoTro)) "
                + "ORDER BY y.maYC DESC";
        return truyVan(sql, r -> new Hang(r.getInt("maYC"), chuoi(r, "nguoi"),
                chuoi(r, "ngay"), chuoi(r, "tieuDe"), chuoi(r, "trangThai"),
                chuoi(r, "noiDung"), chuoi(r, "phanHoi"), chuoi(r, "vaiTro")),
                actor, actor);
    }

    public void phanHoiHoTro(int maYC, String phanHoi, int actor) throws SQLException {
        batBuoc(phanHoi, "Nhập nội dung phản hồi.");
        if (phanHoi.trim().length() > 1500) {
            throw new SQLException("Nội dung phản hồi tối đa 1500 ký tự.");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                int myRank = capBacHoTro(c, actor);
                if (myRank < 2 || !coQuyen(c, actor, "HOTRO")) {
                    throw new SQLException("Vai trò hiện tại không có quyền phản hồi hỗ trợ.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT t.maTK,v.capBacHoTro FROM dbo.YeuCauHoTro y WITH (UPDLOCK) "
                      + "JOIN dbo.TaiKhoan t ON t.maTK=y.maTK "
                      + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro WHERE y.maYC=?")) {
                    ps.setInt(1, maYC);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy yêu cầu hỗ trợ.");
                        }
                        int senderId = rs.getInt(1);
                        int senderRank = rs.getInt(2);
                        if (senderId != actor && (senderRank == 0 || senderRank > myRank)) {
                            throw new SQLException("Không được phản hồi yêu cầu cấp cao hơn.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.YeuCauHoTro SET phanHoi=?, "
                      + "trangThai=N'Đã xử lý',ngayXuLy=SYSDATETIME() WHERE maYC=?")) {
                    ps.setString(1, phanHoi.trim());
                    ps.setInt(2, maYC);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được yêu cầu hỗ trợ.");
                    }
                }
                ghi(c, actor, "Phản hồi yêu cầu hỗ trợ", "YeuCauHoTro", "maYC=" + maYC);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
