package Dao;

import ConnectDB.KetNoi;
import Entity.PhieuKham;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhieuKhamDao {
    public static final String CHO_KHAM = "Chờ khám";
    public static final String DANG_KHAM = "Đang khám";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String DA_HUY = "Đã hủy";
    public static final String[] TRANG_THAI = {CHO_KHAM, DANG_KHAM, HOAN_THANH, DA_HUY};
    public static final int TOI_DA_MOI_PHONG = 20; // số bệnh nhân tối đa / phòng / ngày

    public List<PhieuKham> findHomNay() throws SQLException {
        String sql = "SELECT pk.MaPK, pk.MaBN, pk.MaPhong, pk.NgayKham, pk.SoThuTu, pk.TrangThai, "
                + "bn.HoTen AS TenBN, p.TenPhong "
                + "FROM PhieuKham pk "
                + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
                + "LEFT JOIN PhongKham p ON p.MaPhong = pk.MaPhong "
                + "WHERE pk.NgayKham = ? ORDER BY pk.SoThuTu";
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuKham pk = new PhieuKham();
                    pk.setMaPK(rs.getInt("MaPK"));
                    pk.setMaBN(rs.getInt("MaBN"));
                    int maPhong = rs.getInt("MaPhong");
                    pk.setMaPhong(rs.wasNull() ? null : maPhong);
                    pk.setNgayKham(rs.getDate("NgayKham").toLocalDate());
                    pk.setSoThuTu(rs.getInt("SoThuTu"));
                    pk.setTrangThai(rs.getString("TrangThai"));
                    pk.setTenBenhNhan(rs.getString("TenBN"));
                    pk.setTenPhong(rs.getString("TenPhong"));
                    ds.add(pk);
                }
            }
        }
        return ds;
    }

    public PhieuKham capSoThuTu(int maBN) throws SQLException {
        return capSoThuTu(maBN, null);
    }

    /** Cấp số thứ tự hôm nay cho bệnh nhân và (nếu có) phân luôn phòng khám. */
    public PhieuKham capSoThuTu(int maBN, Integer maPhong) throws SQLException {
        LocalDate homNay = LocalDate.now();
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (demPhieuKhacTrangThai(conn, maBN, homNay, DA_HUY) > 0) {
                    throw new IllegalArgumentException("Bệnh nhân này đã được cấp số thứ tự trong hôm nay.");
                }
                int soThuTu = layMaxSoThuTu(conn, homNay) + 1;
                if (maPhong != null && demPhieuTheoPhong(conn, maPhong, homNay) >= TOI_DA_MOI_PHONG) {
                    throw new IllegalArgumentException("Phòng này đã đủ " + TOI_DA_MOI_PHONG + " bệnh nhân trong hôm nay.");
                }
                PhieuKham pk = new PhieuKham();
                pk.setMaBN(maBN);
                pk.setMaPhong(maPhong);
                pk.setNgayKham(homNay);
                pk.setSoThuTu(soThuTu);
                pk.setTrangThai(CHO_KHAM);
                pk.setMaPK(insert(conn, pk));
                conn.commit();
                return pk;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void chiDinhPhong(PhieuKham pk, int maPhong) throws SQLException {
        if (HOAN_THANH.equals(pk.getTrangThai()) || DA_HUY.equals(pk.getTrangThai())) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể chỉ định phòng.");
        }
        update(pk.getMaPK(), maPhong, pk.getTrangThai());
    }

    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai) throws SQLException {
        if (!Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phiếu khám không hợp lệ.");
        }
        update(pk.getMaPK(), maPhong, trangThai);
    }

    private int layMaxSoThuTu(Connection conn, LocalDate ngay) throws SQLException {
        String sql = "SELECT ISNULL(MAX(SoThuTu), 0) FROM PhieuKham WITH (UPDLOCK, HOLDLOCK) WHERE NgayKham = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int demPhieuTheoPhong(Connection conn, int maPhong, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuKham WHERE MaPhong = ? AND NgayKham = ? AND TrangThai <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhong);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int demPhieuKhacTrangThai(Connection conn, int maBN, LocalDate ngay, String trangThaiLoaiTru) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuKham WHERE MaBN = ? AND NgayKham = ? AND TrangThai <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, trangThaiLoaiTru);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insert(Connection conn, PhieuKham pk) throws SQLException {
        String sql = "INSERT INTO PhieuKham (MaBN, MaPhong, NgayKham, SoThuTu, TrangThai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pk.getMaBN());
            if (pk.getMaPhong() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, pk.getMaPhong());
            }
            ps.setDate(3, Date.valueOf(pk.getNgayKham()));
            ps.setInt(4, pk.getSoThuTu());
            ps.setString(5, pk.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int update(int maPK, Integer maPhong, String trangThai) throws SQLException {
        String sql = "UPDATE PhieuKham SET MaPhong = ?, TrangThai = ? WHERE MaPK = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maPhong == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, maPhong);
            }
            ps.setString(2, trangThai);
            ps.setInt(3, maPK);
            return ps.executeUpdate();
        }
    }
}
