package Dao;

import ConnectDB.KetNoi;
import Entity.BenhNhan;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BenhNhanDao {
    private static final String COT = "MaBN, HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, "
            + "CCCD, Email, NgheNghiep, TenNguoiLienHe, SdtNguoiLienHe, DiUng, TienSuBenh, GhiChu, "
            + "TrangThaiHoSo, NgayTao";

    public List<BenhNhan> findAll() throws SQLException {
        String sql = "SELECT " + COT + " FROM BenhNhan "
                + "ORDER BY CASE WHEN TrangThaiHoSo = N'Hoạt động' THEN 0 ELSE 1 END, MaBN DESC";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return docDanhSach(rs);
        }
    }

    public List<BenhNhan> search(String tuKhoaGoc) throws SQLException {
        String tuKhoa = tuKhoaGoc == null ? "" : tuKhoaGoc.trim();
        if (tuKhoa.isEmpty()) {
            return findAll();
        }
        String sql = "SELECT " + COT + " FROM BenhNhan "
                + "WHERE HoTen COLLATE Vietnamese_CI_AI LIKE ? OR SoDienThoai LIKE ? OR CCCD LIKE ? "
                + "OR Email COLLATE Vietnamese_CI_AI LIKE ? "
                + "OR CAST(MaBN AS VARCHAR(20)) = ? OR 'BN' + RIGHT('000' + CAST(MaBN AS VARCHAR(20)), 3) = ? "
                + "ORDER BY CASE WHEN TrangThaiHoSo = N'Hoạt động' THEN 0 ELSE 1 END, MaBN DESC";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String mau = "%" + tuKhoa + "%";
            ps.setString(1, mau);
            ps.setString(2, mau);
            ps.setString(3, mau);
            ps.setString(4, mau);
            ps.setString(5, tuKhoa);
            ps.setString(6, tuKhoa.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return docDanhSach(rs);
            }
        }
    }

    public BenhNhan findById(int maBN) throws SQLException {
        String sql = "SELECT " + COT + " FROM BenhNhan WHERE MaBN = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public BenhNhan findByCccd(String cccd) throws SQLException {
        if (cccd == null || cccd.isBlank()) return null;
        String sql = "SELECT " + COT + " FROM BenhNhan WHERE CCCD = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cccd.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int demTatCa() throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM BenhNhan");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int demDangHoatDong() throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM BenhNhan WHERE TrangThaiHoSo = N'Hoạt động'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Tìm hồ sơ trùng họ tên + số điện thoại (không phân biệt hoa/thường). Không có thì trả về null. */
    public BenhNhan findByTenVaSdt(String hoTen, String sdt) throws SQLException {
        if (hoTen == null || sdt == null || hoTen.isBlank() || sdt.isBlank()) return null;
        String sql = "SELECT TOP 1 " + COT + " FROM BenhNhan "
                + "WHERE HoTen COLLATE Vietnamese_CI_AI = ? AND SoDienThoai = ? ORDER BY MaBN";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hoTen.trim());
            ps.setString(2, sdt.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public int them(BenhNhan bn) throws SQLException {
        chuanHoaVaKiemTra(bn);
        kiemTraTrungCccd(bn);
        return insert(bn);
    }

    /** Cập nhật toàn bộ thông tin hồ sơ (không đổi mã bệnh nhân, không đổi trạng thái). */
    public void capNhat(BenhNhan bn) throws SQLException {
        chuanHoaVaKiemTra(bn);
        kiemTraTrungCccd(bn);
        String sql = "UPDATE BenhNhan SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, SoDienThoai = ?, DiaChi = ?, "
                + "CCCD = ?, Email = ?, NgheNghiep = ?, TenNguoiLienHe = ?, SdtNguoiLienHe = ?, "
                + "DiUng = ?, TienSuBenh = ?, GhiChu = ? WHERE MaBN = ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = gan(ps, bn);
            ps.setInt(i, bn.getMaBN());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy bệnh nhân BN" + String.format("%03d", bn.getMaBN()) + ".");
            }
        }
    }

    /** Ngừng hoạt động / kích hoạt lại hồ sơ (không xóa dữ liệu lịch sử). */
    public void doiTrangThai(int maBN, String trangThai) throws SQLException {
        if (!BenhNhan.HOAT_DONG.equals(trangThai) && !BenhNhan.NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new IllegalArgumentException("Trạng thái hồ sơ không hợp lệ.");
        }
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE BenhNhan SET TrangThaiHoSo = ? WHERE MaBN = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maBN);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
            }
        }
    }

    /** Số lần đã khám (không tính phiếu hủy) và ngày khám gần nhất; ngày null nếu chưa khám lần nào. */
    public Object[] thongKeKham(int maBN) throws SQLException {
        String sql = "SELECT COUNT(*), MAX(NgayKham) FROM PhieuKham WHERE MaBN = ? AND TrangThai <> N'Đã hủy'";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Date ngay = rs.getDate(2);
                return new Object[]{rs.getInt(1), ngay == null ? null : ngay.toLocalDate()};
            }
        }
    }

    /** Tổng tiền đã thanh toán của bệnh nhân (dùng cho phần lịch sử). */
    public java.math.BigDecimal tongDaThanhToan(int maBN) throws SQLException {
        String sql = "SELECT ISNULL(SUM(hd.TongTien), 0) FROM HoaDon hd "
                + "JOIN PhieuKham pk ON pk.MaPK = hd.MaPK "
                + "WHERE pk.MaBN = ? AND hd.TrangThai = N'Đã thanh toán'";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
            }
        }
    }

    private static String rong(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void chuanHoaVaKiemTra(BenhNhan bn) {
        String hoTen = rong(bn.getHoTen());
        if (hoTen == null) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        String sdt = rong(bn.getSoDienThoai());
        if (sdt != null && !sdt.matches("0\\d{9,10}")) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.");
        }
        String sdtLh = rong(bn.getSdtNguoiLienHe());
        if (sdtLh != null && !sdtLh.matches("0\\d{9,10}")) {
            throw new IllegalArgumentException("SĐT người liên hệ phải bắt đầu bằng 0 và có 10-11 chữ số.");
        }
        String cccd = rong(bn.getCccd());
        if (cccd != null && !cccd.matches("\\d{9}|\\d{12}")) {
            throw new IllegalArgumentException("CMND/CCCD phải gồm 9 hoặc 12 chữ số.");
        }
        String email = rong(bn.getEmail());
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        if (bn.getNgaySinh() != null && bn.getNgaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        }
        String gioiTinh = rong(bn.getGioiTinh());
        if (gioiTinh != null && !gioiTinh.equals("Nam") && !gioiTinh.equals("Nữ") && !gioiTinh.equals("Khác")) {
            gioiTinh = null;
        }
        bn.setHoTen(hoTen);
        bn.setSoDienThoai(sdt);
        bn.setSdtNguoiLienHe(sdtLh);
        bn.setCccd(cccd);
        bn.setEmail(email);
        bn.setGioiTinh(gioiTinh);
        bn.setDiaChi(rong(bn.getDiaChi()));
        bn.setNgheNghiep(rong(bn.getNgheNghiep()));
        bn.setTenNguoiLienHe(rong(bn.getTenNguoiLienHe()));
        bn.setDiUng(rong(bn.getDiUng()));
        bn.setTienSuBenh(rong(bn.getTienSuBenh()));
        bn.setGhiChu(rong(bn.getGhiChu()));
        if (bn.getTrangThai() == null) {
            bn.setTrangThai(BenhNhan.HOAT_DONG);
        }
    }

    private void kiemTraTrungCccd(BenhNhan bn) throws SQLException {
        if (bn.getCccd() == null) return;
        String sql = "SELECT TOP 1 MaBN FROM BenhNhan WHERE CCCD = ? AND MaBN <> ?";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bn.getCccd());
            ps.setInt(2, bn.getMaBN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("CMND/CCCD này đã thuộc hồ sơ BN"
                            + String.format("%03d", rs.getInt(1)) + ".");
                }
            }
        }
    }

    /** Gán 13 cột thông tin (theo đúng thứ tự INSERT/UPDATE); trả về vị trí tham số kế tiếp. */
    private int gan(PreparedStatement ps, BenhNhan bn) throws SQLException {
        ps.setString(1, bn.getHoTen());
        if (bn.getNgaySinh() == null) {
            ps.setNull(2, Types.DATE);
        } else {
            ps.setDate(2, Date.valueOf(bn.getNgaySinh()));
        }
        ps.setString(3, bn.getGioiTinh());
        ps.setString(4, bn.getSoDienThoai());
        ps.setString(5, bn.getDiaChi());
        ps.setString(6, bn.getCccd());
        ps.setString(7, bn.getEmail());
        ps.setString(8, bn.getNgheNghiep());
        ps.setString(9, bn.getTenNguoiLienHe());
        ps.setString(10, bn.getSdtNguoiLienHe());
        ps.setString(11, bn.getDiUng());
        ps.setString(12, bn.getTienSuBenh());
        ps.setString(13, bn.getGhiChu());
        return 14;
    }

    private int insert(BenhNhan bn) throws SQLException {
        String sql = "INSERT INTO BenhNhan (HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, CCCD, Email, NgheNghiep, "
                + "TenNguoiLienHe, SdtNguoiLienHe, DiUng, TienSuBenh, GhiChu, TrangThaiHoSo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            gan(ps, bn);
            ps.setString(14, bn.getTrangThai());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private List<BenhNhan> docDanhSach(ResultSet rs) throws SQLException {
        List<BenhNhan> ds = new ArrayList<>();
        while (rs.next()) {
            ds.add(doc(rs));
        }
        return ds;
    }

    private BenhNhan doc(ResultSet rs) throws SQLException {
        BenhNhan bn = new BenhNhan();
        bn.setMaBN(rs.getInt("MaBN"));
        bn.setHoTen(rs.getString("HoTen"));
        Date ngaySinh = rs.getDate("NgaySinh");
        bn.setNgaySinh(ngaySinh == null ? null : ngaySinh.toLocalDate());
        bn.setGioiTinh(rs.getString("GioiTinh"));
        bn.setSoDienThoai(rs.getString("SoDienThoai"));
        bn.setDiaChi(rs.getString("DiaChi"));
        bn.setCccd(rs.getString("CCCD"));
        bn.setEmail(rs.getString("Email"));
        bn.setNgheNghiep(rs.getString("NgheNghiep"));
        bn.setTenNguoiLienHe(rs.getString("TenNguoiLienHe"));
        bn.setSdtNguoiLienHe(rs.getString("SdtNguoiLienHe"));
        bn.setDiUng(rs.getString("DiUng"));
        bn.setTienSuBenh(rs.getString("TienSuBenh"));
        bn.setGhiChu(rs.getString("GhiChu"));
        bn.setTrangThai(rs.getString("TrangThaiHoSo"));
        java.sql.Timestamp tao = rs.getTimestamp("NgayTao");
        bn.setNgayTao(tao == null ? null : tao.toLocalDateTime());
        return bn;
    }
}
