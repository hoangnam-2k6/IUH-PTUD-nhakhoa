package Dao;

import ConnectDB.KetNoi;
import Entity.TaiKhoan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tài khoản đăng nhập. Mật khẩu được băm SHA-256 trước khi so sánh/lưu,
 * không bao giờ lưu mật khẩu gốc xuống cơ sở dữ liệu.
 */
public class TaiKhoanDao {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private static final String COT = "MaTK, TenDangNhap, HoTen, VaiTro, TrangThai, NgayTao, NgayDangNhapCuoi";

    /** Đăng nhập; trả về tài khoản nếu thành công, ném IllegalArgumentException nếu sai. */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String ten = tenDangNhap == null ? "" : tenDangNhap.trim();
        String mk = matKhau == null ? "" : matKhau;
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập.");
        }
        if (mk.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
        }

        String sql = "SELECT " + COT + ", MatKhauHash FROM TaiKhoan WHERE TenDangNhap = ?";
        TaiKhoan tk;
        String hashLuu;
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ten);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng.");
                }
                tk = doc(rs);
                hashLuu = rs.getString("MatKhauHash");
            }
            if (!HOAT_DONG.equals(tk.getTrangThai())) {
                throw new IllegalArgumentException("Tài khoản \"" + ten + "\" đã bị ngừng hoạt động. "
                        + "Liên hệ quản trị viên để được mở lại.");
            }
            if (!bamSha256(mk).equalsIgnoreCase(hashLuu)) {
                throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng.");
            }
            // Phân hệ này chỉ dành cho Lễ tân; tài khoản quản trị không đăng nhập vào đây.
            if (!"Lễ tân".equals(tk.getVaiTro())) {
                throw new IllegalArgumentException("Tài khoản này không thuộc phân hệ Lễ tân.");
            }
            capNhatLanDangNhapCuoi(conn, tk.getMaTK());
            tk.setNgayDangNhapCuoi(LocalDateTime.now());
        }
        return tk;
    }

    public List<TaiKhoan> findAll() throws SQLException {
        String sql = "SELECT " + COT + " FROM TaiKhoan ORDER BY VaiTro, HoTen";
        List<TaiKhoan> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ds.add(doc(rs));
        }
        return ds;
    }

    public TaiKhoan findById(int maTK) throws SQLException {
        String sql = "SELECT " + COT + " FROM TaiKhoan WHERE MaTK = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int them(TaiKhoan tk, String matKhau) throws SQLException {
        kiemTra(tk, matKhau, true);
        String sql = "INSERT INTO TaiKhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro, TrangThai) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tk.getTenDangNhap().trim());
            ps.setString(2, bamSha256(matKhau));
            ps.setString(3, tk.getHoTen().trim());
            ps.setString(4, tk.getVaiTro());
            ps.setString(5, tk.getTrangThai() == null ? HOAT_DONG : tk.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public void capNhat(TaiKhoan tk) throws SQLException {
        kiemTra(tk, null, false);
        String sql = "UPDATE TaiKhoan SET HoTen = ?, VaiTro = ?, TrangThai = ? WHERE MaTK = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tk.getHoTen().trim());
            ps.setString(2, tk.getVaiTro());
            ps.setString(3, tk.getTrangThai() == null ? HOAT_DONG : tk.getTrangThai());
            ps.setInt(4, tk.getMaTK());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản cần cập nhật.");
            }
        }
    }

    /** Đổi mật khẩu của chính tài khoản Quản trị viên (cần đúng mật khẩu hiện tại). */
    public void doiMatKhau(int maTK, String matKhauCu, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        String sql = "SELECT MatKhauHash, VaiTro FROM TaiKhoan WHERE MaTK = ?";
        try (Connection conn = KetNoi.getConnection()) {
            String hashLuu;
            String vaiTro;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maTK);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy tài khoản.");
                    hashLuu = rs.getString("MatKhauHash");
                    vaiTro = rs.getString("VaiTro");
                }
            }
            if (!"Quản trị viên".equals(vaiTro)) {
                throw new IllegalArgumentException("Chỉ quản trị viên mới có quyền đổi mật khẩu.");
            }
            if (!bamSha256(matKhauCu == null ? "" : matKhauCu).equalsIgnoreCase(hashLuu)) {
                throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE TaiKhoan SET MatKhauHash = ? WHERE MaTK = ?")) {
                ps.setString(1, bamSha256(matKhauMoi));
                ps.setInt(2, maTK);
                ps.executeUpdate();
            }
        }
    }

    /** Quản trị viên đặt lại mật khẩu mà không cần mật khẩu cũ. */
    public void datLaiMatKhau(int maTK, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE TaiKhoan SET MatKhauHash = ? WHERE MaTK = ?")) {
            ps.setString(1, bamSha256(matKhauMoi));
            ps.setInt(2, maTK);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản.");
            }
        }
    }

    private void capNhatLanDangNhapCuoi(Connection conn, int maTK) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE TaiKhoan SET NgayDangNhapCuoi = SYSDATETIME() WHERE MaTK = ?")) {
            ps.setInt(1, maTK);
            ps.executeUpdate();
        }
    }

    private void kiemTra(TaiKhoan tk, String matKhau, boolean batBuocMatKhau) {
        if (tk.getTenDangNhap() == null || tk.getTenDangNhap().isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (!tk.getTenDangNhap().matches("[A-Za-z0-9._-]{3,50}")) {
            throw new IllegalArgumentException("Tên đăng nhập chỉ gồm chữ, số, dấu chấm, gạch dưới, gạch ngang (3-50 ký tự).");
        }
        if (tk.getHoTen() == null || tk.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (!"Lễ tân".equals(tk.getVaiTro()) && !"Quản trị viên".equals(tk.getVaiTro())) {
            throw new IllegalArgumentException("Vai trò không hợp lệ.");
        }
        if (batBuocMatKhau && (matKhau == null || matKhau.length() < 6)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
    }

    private TaiKhoan doc(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(rs.getInt("MaTK"));
        tk.setTenDangNhap(rs.getString("TenDangNhap"));
        tk.setHoTen(rs.getString("HoTen"));
        tk.setVaiTro(rs.getString("VaiTro"));
        tk.setTrangThai(rs.getString("TrangThai"));
        Timestamp tao = rs.getTimestamp("NgayTao");
        tk.setNgayTao(tao == null ? null : tao.toLocalDateTime());
        Timestamp cuoi = rs.getTimestamp("NgayDangNhapCuoi");
        tk.setNgayDangNhapCuoi(cuoi == null ? null : cuoi.toLocalDateTime());
        return tk;
    }

    /** Băm chuỗi theo SHA-256, trả về 64 ký tự hex thường. */
    public static String bamSha256(String chuoi) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((chuoi == null ? "" : chuoi).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Máy Java không hỗ trợ SHA-256.", e);
        }
    }
}
