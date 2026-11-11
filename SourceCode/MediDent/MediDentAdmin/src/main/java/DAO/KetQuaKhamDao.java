package DAO;

import ConnectDB.ConnectDB;
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

    public KetQuaKham timTheoMaPK(int maPK) throws SQLException {
        String sql = "SELECT kq.MaPK, kq.MaBS, bs.HoTen AS TenBS, kq.TrieuChung, kq.KetQuaLamSang, "
                + "kq.ChanDoan, kq.HuongDieuTri, kq.DonThuoc, kq.GhiChu, kq.NgayTaiKham, kq.NgayCapNhat "
                + "FROM KetQuaKham kq LEFT JOIN BacSi bs ON bs.MaBS = kq.MaBS WHERE kq.MaPK = ?";
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    /** Lưu kết quả khám: thêm mới nếu chưa có, cập nhật nếu đã có. */
    public void luu(KetQuaKham kq) throws SQLException {
        if (kq == null || kq.layMaPK() <= 0) {
            throw new IllegalArgumentException("Kết quả khám phải gắn với một phiếu khám.");
        }
        kiemTraDoDai(kq);
        try (Connection conn = ConnectDB.layConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean daCo;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM KetQuaKham WHERE MaPK = ?")) {
                    ps.setInt(1, kq.layMaPK());
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
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM KetQuaKham WHERE MaPK = ?")) {
            ps.setInt(1, maPK);
            ps.executeUpdate();
        }
    }

    private void gan(PreparedStatement ps, KetQuaKham kq, boolean insert) throws SQLException {
        int i = 1;
        if (kq.layMaBS() == null) ps.setNull(i++, Types.INTEGER);
        else ps.setInt(i++, kq.layMaBS());
        ps.setString(i++, rong(kq.layTrieuChung()));
        ps.setString(i++, rong(kq.layKetQuaLamSang()));
        ps.setString(i++, rong(kq.layChanDoan()));
        ps.setString(i++, rong(kq.layHuongDieuTri()));
        ps.setString(i++, rong(kq.layDonThuoc()));
        ps.setString(i++, rong(kq.layGhiChu()));
        if (kq.layNgayTaiKham() == null) ps.setNull(i++, Types.DATE);
        else ps.setDate(i++, Date.valueOf(kq.layNgayTaiKham()));
        ps.setInt(i, kq.layMaPK());
    }

    private void kiemTraDoDai(KetQuaKham kq) {
        kiemTra("Triệu chứng", kq.layTrieuChung(), 1000);
        kiemTra("Kết quả lâm sàng", kq.layKetQuaLamSang(), 2000);
        kiemTra("Chẩn đoán", kq.layChanDoan(), 1000);
        kiemTra("Hướng điều trị", kq.layHuongDieuTri(), 2000);
        kiemTra("Đơn thuốc", kq.layDonThuoc(), 2000);
        kiemTra("Ghi chú", kq.layGhiChu(), 1000);
        if (kq.layNgayTaiKham() != null && kq.layNgayTaiKham().isBefore(java.time.LocalDate.now())) {
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
        kq.datMaPK(rs.getInt("MaPK"));
        int maBS = rs.getInt("MaBS");
        kq.datMaBS(rs.wasNull() ? null : maBS);
        kq.datTenBacSi(rs.getString("TenBS"));
        kq.datTrieuChung(rs.getString("TrieuChung"));
        kq.datKetQuaLamSang(rs.getString("KetQuaLamSang"));
        kq.datChanDoan(rs.getString("ChanDoan"));
        kq.datHuongDieuTri(rs.getString("HuongDieuTri"));
        kq.datDonThuoc(rs.getString("DonThuoc"));
        kq.datGhiChu(rs.getString("GhiChu"));
        Date ngay = rs.getDate("NgayTaiKham");
        kq.datNgayTaiKham(ngay == null ? null : ngay.toLocalDate());
        Timestamp capNhat = rs.getTimestamp("NgayCapNhat");
        kq.datNgayCapNhat(capNhat == null ? null : capNhat.toLocalDateTime());
        return kq;
    }
}
