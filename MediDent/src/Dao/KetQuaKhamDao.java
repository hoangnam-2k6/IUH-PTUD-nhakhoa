package Dao;

import ConnectDB.KetNoi;
import Entity.KetQuaKham;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/** DAO hồ sơ kết quả khám (quan hệ 1-1 với phiếu khám). */
public class KetQuaKhamDao {

    public KetQuaKham findByMaPK(int maPK) throws SQLException {
        String sql = "SELECT kq.MaPK, kq.MaBS, bs.HoTen AS TenBS, kq.TrieuChung, kq.KetQuaLamSang, "
                + "kq.ChanDoan, kq.HuongDieuTri, kq.DonThuoc, kq.GhiChu, kq.NgayTaiKham, kq.NgayCapNhat "
                + "FROM KetQuaKham kq LEFT JOIN BacSi bs ON bs.MaBS = kq.MaBS WHERE kq.MaPK = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    /** Lưu kết quả khám: thêm mới nếu chưa có, cập nhật nếu đã có. */
    public void luu(KetQuaKham kq) throws SQLException {
        if (kq == null || kq.getMaPK() <= 0) {
            throw new IllegalArgumentException("Kết quả khám phải gắn với một phiếu khám.");
        }
        kiemTraDoDai(kq);
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean daCo;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM KetQuaKham WHERE MaPK = ?")) {
                    ps.setInt(1, kq.getMaPK());
                    try (ResultSet rs = ps.executeQuery()) {
                        daCo = rs.next() && rs.getInt(1) > 0;
                    }
                }
                if (daCo) {
                    String sql = "UPDATE KetQuaKham SET MaBS = ?, TrieuChung = ?, KetQuaLamSang = ?, "
                            + "ChanDoan = ?, HuongDieuTri = ?, DonThuoc = ?, GhiChu = ?, NgayTaiKham = ?, "
                            + "NgayCapNhat = SYSDATETIME() WHERE MaPK = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        gan(ps, kq, false);
                        ps.executeUpdate();
                    }
                } else {
                    String sql = "INSERT INTO KetQuaKham (MaBS, TrieuChung, KetQuaLamSang, ChanDoan, "
                            + "HuongDieuTri, DonThuoc, GhiChu, NgayTaiKham, MaPK) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        gan(ps, kq, true);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void xoa(int maPK) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM KetQuaKham WHERE MaPK = ?")) {
            ps.setInt(1, maPK);
            ps.executeUpdate();
        }
    }

    private void gan(PreparedStatement ps, KetQuaKham kq, boolean insert) throws SQLException {
        int i = 1;
        if (kq.getMaBS() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, kq.getMaBS());
        ps.setString(i++, rong(kq.getTrieuChung()));
        ps.setString(i++, rong(kq.getKetQuaLamSang()));
        ps.setString(i++, rong(kq.getChanDoan()));
        ps.setString(i++, rong(kq.getHuongDieuTri()));
        ps.setString(i++, rong(kq.getDonThuoc()));
        ps.setString(i++, rong(kq.getGhiChu()));
        if (kq.getNgayTaiKham() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(kq.getNgayTaiKham()));
        ps.setInt(i, kq.getMaPK());
    }

    private void kiemTraDoDai(KetQuaKham kq) {
        kiemTra("Triệu chứng", kq.getTrieuChung(), 1000);
        kiemTra("Kết quả lâm sàng", kq.getKetQuaLamSang(), 2000);
        kiemTra("Chẩn đoán", kq.getChanDoan(), 1000);
        kiemTra("Hướng điều trị", kq.getHuongDieuTri(), 2000);
        kiemTra("Đơn thuốc", kq.getDonThuoc(), 2000);
        kiemTra("Ghi chú", kq.getGhiChu(), 1000);
        if (kq.getNgayTaiKham() != null && kq.getNgayTaiKham().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày tái khám không được ở quá khứ.");
        }
    }

    private void kiemTra(String ten, String giaTri, int toiDa) {
        if (giaTri != null && giaTri.length() > toiDa) {
            throw new IllegalArgumentException(ten + " tối đa " + toiDa + " ký tự.");
        }
    }

    private static String rong(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private KetQuaKham doc(ResultSet rs) throws SQLException {
        KetQuaKham kq = new KetQuaKham();
        kq.setMaPK(rs.getInt("MaPK"));
        int maBS = rs.getInt("MaBS");
        kq.setMaBS(rs.wasNull() ? null : maBS);
        kq.setTenBacSi(rs.getString("TenBS"));
        kq.setTrieuChung(rs.getString("TrieuChung"));
        kq.setKetQuaLamSang(rs.getString("KetQuaLamSang"));
        kq.setChanDoan(rs.getString("ChanDoan"));
        kq.setHuongDieuTri(rs.getString("HuongDieuTri"));
        kq.setDonThuoc(rs.getString("DonThuoc"));
        kq.setGhiChu(rs.getString("GhiChu"));
        Date ngay = rs.getDate("NgayTaiKham");
        kq.setNgayTaiKham(ngay == null ? null : ngay.toLocalDate());
        Timestamp capNhat = rs.getTimestamp("NgayCapNhat");
        kq.setNgayCapNhat(capNhat == null ? null : capNhat.toLocalDateTime());
        return kq;
    }
}
