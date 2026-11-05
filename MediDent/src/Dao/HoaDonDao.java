package Dao;

import ConnectDB.KetNoi;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDao {

    public List<HoaDon> findAll() throws SQLException {
        String sql = "SELECT hd.MaHD, hd.MaPK, hd.NgayLap, hd.TongTien, hd.GiamGia, hd.PhuongThucTT, "
                + "hd.NgayThanhToan, hd.TrangThai, bn.HoTen AS TenBN "
                + "FROM HoaDon hd "
                + "JOIN PhieuKham pk ON pk.MaPK = hd.MaPK "
                + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
                + "ORDER BY hd.NgayLap DESC";
        List<HoaDon> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(docCoBan(rs));
            }
        }
        return ds;
    }

    /**
     * Lấy đầy đủ một hóa đơn để in: thông tin bệnh nhân, phiếu khám, phòng khám và các dòng dịch vụ.
     * Hóa đơn chưa có dòng chi tiết nào sẽ được hiển thị bằng 1 dòng "Dịch vụ khám & điều trị".
     */
    public HoaDon findDayDu(int maHD) throws SQLException {
        String sql = "SELECT hd.MaHD, hd.MaPK, hd.NgayLap, hd.TongTien, hd.GiamGia, hd.PhuongThucTT, "
                + "hd.NgayThanhToan, hd.TrangThai, bn.HoTen AS TenBN, pk.MaBN, pk.NgayKham, pk.SoThuTu, p.TenPhong "
                + "FROM HoaDon hd "
                + "JOIN PhieuKham pk ON pk.MaPK = hd.MaPK "
                + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
                + "LEFT JOIN PhongKham p ON p.MaPhong = pk.MaPhong "
                + "WHERE hd.MaHD = ?";
        HoaDon hd;
        int maBN;
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                hd = docCoBan(rs);
                maBN = rs.getInt("MaBN");
                Date ngayKham = rs.getDate("NgayKham");
                hd.setNgayKham(ngayKham == null ? null : ngayKham.toLocalDate());
                hd.setSoThuTu(rs.getInt("SoThuTu"));
                hd.setTenPhong(rs.getString("TenPhong"));
            }
            hd.setChiTiet(docChiTiet(conn, maHD));
        }
        hd.setBenhNhan(new BenhNhanDao().findById(maBN));
        if (hd.getChiTiet().isEmpty()) {
            hd.getChiTiet().add(new ChiTietHoaDon("Dịch vụ khám & điều trị", 1, hd.getTamTinh()));
        }
        return hd;
    }

    private List<ChiTietHoaDon> docChiTiet(Connection conn, int maHD) throws SQLException {
        String sql = "SELECT MaCT, MaHD, TenDichVu, SoLuong, DonGia FROM ChiTietHoaDon WHERE MaHD = ? ORDER BY MaCT";
        List<ChiTietHoaDon> ds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    ct.setMaCT(rs.getInt("MaCT"));
                    ct.setMaHD(rs.getInt("MaHD"));
                    ct.setTenDichVu(rs.getString("TenDichVu"));
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGia(rs.getBigDecimal("DonGia"));
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    private HoaDon docCoBan(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getInt("MaHD"));
        hd.setMaPK(rs.getInt("MaPK"));
        hd.setNgayLap(rs.getTimestamp("NgayLap").toLocalDateTime());
        hd.setTongTien(rs.getBigDecimal("TongTien"));
        BigDecimal giamGia = rs.getBigDecimal("GiamGia");
        hd.setGiamGia(giamGia == null ? BigDecimal.ZERO : giamGia);
        hd.setPhuongThucTT(rs.getString("PhuongThucTT"));
        Timestamp tt = rs.getTimestamp("NgayThanhToan");
        hd.setNgayThanhToan(tt == null ? null : tt.toLocalDateTime());
        hd.setTrangThai(rs.getString("TrangThai"));
        hd.setTenBenhNhan(rs.getString("TenBN"));
        return hd;
    }
}
