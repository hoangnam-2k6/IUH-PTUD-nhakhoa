package DAO;

import ConnectDB.ConnectDB;
import Entity.PhieuKham;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PhieuKhamDao {
    public static final String CHO_KHAM = PhieuKham.CHO_KHAM;
    public static final String DANG_KHAM = PhieuKham.DANG_KHAM;
    public static final String HOAN_THANH = PhieuKham.HOAN_THANH;
    public static final String DA_HUY = PhieuKham.DA_HUY;
    public static final String[] TRANG_THAI = {CHO_KHAM, DANG_KHAM, HOAN_THANH, DA_HUY};

    /** Giới hạn dự phòng khi phòng khám không khai báo sức chứa. */
    public static final int TOI_DA_MOI_PHONG = 20;

    private static final String SELECT = "SELECT pk.MaPK, pk.MaBN, pk.MaPhong, pk.MaLH, pk.NgayKham, pk.SoThuTu, "
            + "pk.TrangThai, pk.GhiChu, bn.HoTen AS TenBN, p.TenPhong "
            + "FROM PhieuKham pk "
            + "JOIN BenhNhan bn ON bn.MaBN = pk.MaBN "
            + "LEFT JOIN PhongKham p ON p.MaPhong = pk.MaPhong ";

    public List<PhieuKham> timHomNay() throws SQLException {
        return timTheoNgay(LocalDate.now());
    }

    public List<PhieuKham> timTheoNgay(LocalDate ngay) throws SQLException {
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE pk.NgayKham = ? ORDER BY pk.SoThuTu")) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public List<PhieuKham> timTheoBenhNhan(int maBN) throws SQLException {
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT + "WHERE pk.MaBN = ? ORDER BY pk.NgayKham DESC, pk.SoThuTu DESC")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public PhieuKham timTheoMa(int maPK) throws SQLException {
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE pk.MaPK = ?")) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    public PhieuKham capSoThuTu(int maBN) throws SQLException {
        return capSoThuTu(maBN, null, null);
    }

    public PhieuKham capSoThuTu(int maBN, Integer maPhong) throws SQLException {
        return capSoThuTu(maBN, maPhong, null);
    }

    /**
     * Cấp số thứ tự hôm nay cho bệnh nhân và (nếu có) phân luôn phòng khám.
     * Nếu {@code maLH} khác null thì phiếu được tạo từ lịch hẹn và lịch hẹn
     * được đánh dấu "Đã đến" trong cùng một giao dịch.
     */
    public PhieuKham capSoThuTu(int maBN, Integer maPhong, Integer maLH) throws SQLException {
        LocalDate homNay = LocalDate.now();
        try (Connection conn = ConnectDB.layConnection()) {
            conn.setAutoCommit(false);
            try {
                String tenBN = layTenBenhNhan(conn, maBN);
                kiemTraLichHen(conn, maBN, maLH);

                if (demPhieuChuaHuy(conn, maBN, homNay) > 0) {
                    throw new IllegalArgumentException("Bệnh nhân " + tenBN + " đã được cấp số thứ tự trong hôm nay.");
                }
                if (maPhong != null) {
                    kiemTraSucChuaPhong(conn, maPhong, homNay);
                }
                int soThuTu = layMaxSoThuTu(conn, homNay) + 1;

                PhieuKham pk = new PhieuKham();
                pk.datMaBN(maBN);
                pk.datMaPhong(maPhong);
                pk.datMaLH(maLH);
                pk.datNgayKham(homNay);
                pk.datSoThuTu(soThuTu);
                pk.datTrangThai(CHO_KHAM);
                pk.datTenBenhNhan(tenBN);
                pk.datMaPK(themMoi(conn, pk));

                if (maLH != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE LichHen SET TrangThai = ? WHERE MaLH = ?")) {
                        ps.setString(1, LichHenLeTanDAO.DA_DEN);
                        ps.setInt(2, maLH);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return pk;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void chiDinhPhong(PhieuKham pk, int maPhong) throws SQLException {
        if (pk.daKetThuc()) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể chỉ định phòng.");
        }
        try (Connection conn = ConnectDB.layConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraSucChuaPhong(conn, maPhong, pk.layNgayKham() == null ? LocalDate.now() : pk.layNgayKham());
                capNhat(conn, pk.layMaPK(), maPhong, pk.layTrangThai(), pk.layGhiChu(), false);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai) throws SQLException {
        capNhat(pk, maPhong, trangThai, pk.layGhiChu());
    }

    /**
     * Cập nhật phòng / trạng thái / ghi chú. Khi phiếu hoàn thành mà được tạo
     * từ lịch hẹn thì lịch hẹn cũng chuyển sang "Hoàn thành".
     */
    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai, String ghiChu) throws SQLException {
        if (!Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phiếu khám không hợp lệ.");
        }
        try (Connection conn = ConnectDB.layConnection()) {
            conn.setAutoCommit(false);
            try {
                PhieuKham hienTai = layVoiKhoa(conn, pk.layMaPK());
                if (hienTai == null) {
                    throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
                }
                if (hienTai.daKetThuc() && !hienTai.layTrangThai().equals(trangThai)) {
                    throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể đổi trạng thái.");
                }
                if (maPhong != null && !maPhong.equals(hienTai.layMaPhong())) {
                    kiemTraSucChuaPhong(conn, maPhong, hienTai.layNgayKham());
                }
                capNhat(conn, pk.layMaPK(), maPhong, trangThai, ghiChu, true);

                if (hienTai.layMaLH() != null) {
                    if (HOAN_THANH.equals(trangThai)) {
                        capNhatLichHen(conn, hienTai.layMaLH(), LichHenLeTanDAO.HOAN_THANH);
                    } else if (DA_HUY.equals(trangThai)) {
                        // Trả lịch hẹn về trạng thái có thể check-in lại.
                        capNhatLichHenNeuDang(conn, hienTai.layMaLH(), LichHenLeTanDAO.DA_DEN, LichHenLeTanDAO.DA_XAC_NHAN);
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
        pk.datMaPhong(maPhong);
        pk.datTrangThai(trangThai);
        pk.datGhiChu(ghiChu);
    }

    public void batDauKham(int maPK) throws SQLException {
        PhieuKham pk = timTheoMa(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (!pk.choKham()) {
            throw new IllegalArgumentException("Chỉ có thể bắt đầu khám phiếu đang ở trạng thái \"Chờ khám\".");
        }
        capNhat(pk, pk.layMaPhong(), DANG_KHAM, pk.layGhiChu());
    }

    public void hoanThanh(int maPK) throws SQLException {
        PhieuKham pk = timTheoMa(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc.");
        }
        capNhat(pk, pk.layMaPhong(), HOAN_THANH, pk.layGhiChu());
    }

    public void huy(int maPK, String lyDo) throws SQLException {
        PhieuKham pk = timTheoMa(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc.");
        }
        String ghiChu = lyDo == null || lyDo.isBlank() ? pk.layGhiChu() : lyDo.trim();
        capNhat(pk, pk.layMaPhong(), DA_HUY, ghiChu);
    }

    public int demTheoPhong(int maPhong, LocalDate ngay) throws SQLException {
        try (Connection conn = ConnectDB.layConnection()) {
            return demPhieuTheoPhong(conn, maPhong, ngay);
        }
    }

    public int demTheoNgay(LocalDate ngay) throws SQLException {
        try (Connection conn = ConnectDB.layConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM PhieuKham WHERE NgayKham = ? AND TrangThai <> ?")) {
            ps.setDate(1, Date.valueOf(ngay));
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ===================== Hỗ trợ giao dịch =====================

    private void kiemTraLichHen(Connection conn, int maBN, Integer maLH) throws SQLException {
        if (maLH == null) return;
        String sql = "SELECT MaBN, TrangThai FROM LichHen WITH (UPDLOCK, HOLDLOCK) WHERE MaLH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Không tìm thấy lịch hẹn cần check-in.");
                }
                if (rs.getInt("MaBN") != maBN) {
                    throw new IllegalArgumentException("Lịch hẹn không thuộc bệnh nhân đang chọn.");
                }
                String tt = rs.getString("TrangThai");
                if (!LichHenLeTanDAO.CHO_XAC_NHAN.equals(tt) && !LichHenLeTanDAO.DA_XAC_NHAN.equals(tt)) {
                    throw new IllegalArgumentException("Lịch hẹn đang ở trạng thái \"" + tt
                            + "\", không thể check-in.");
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM PhieuKham WHERE MaLH = ? AND TrangThai <> ?")) {
            ps.setInt(1, maLH);
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalArgumentException("Lịch hẹn này đã được check-in trước đó.");
                }
            }
        }
    }

    private String layTenBenhNhan(Connection conn, int maBN) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT HoTen FROM BenhNhan WHERE MaBN = ?")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
                return rs.getString(1);
            }
        }
    }

    private void kiemTraSucChuaPhong(Connection conn, int maPhong, LocalDate ngay) throws SQLException {
        int sucChua;
        String trangThai;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SucChua, TrangThai FROM PhongKham WHERE MaPhong = ?")) {
            ps.setInt(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy phòng khám.");
                sucChua = rs.getInt("SucChua");
                trangThai = rs.getString("TrangThai");
            }
        }
        if (PhongKhamDao.NGUNG_HOAT_DONG.equals(trangThai)) {
            throw new IllegalArgumentException("Phòng khám này đang ngừng hoạt động.");
        }
        int daCo = demPhieuTheoPhong(conn, maPhong, ngay);
        if (daCo >= sucChua) {
            throw new IllegalArgumentException("Phòng này đã đủ " + sucChua + " bệnh nhân trong ngày.");
        }
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

    private int demPhieuChuaHuy(Connection conn, int maBN, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PhieuKham WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE MaBN = ? AND NgayKham = ? AND TrangThai <> ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private PhieuKham layVoiKhoa(Connection conn, int maPK) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE pk.MaPK = ?")) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    private int themMoi(Connection conn, PhieuKham pk) throws SQLException {
        String sql = "INSERT INTO PhieuKham (MaBN, MaPhong, MaLH, NgayKham, SoThuTu, TrangThai, GhiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pk.layMaBN());
            if (pk.layMaPhong() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, pk.layMaPhong());
            if (pk.layMaLH() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, pk.layMaLH());
            ps.setDate(4, Date.valueOf(pk.layNgayKham()));
            ps.setInt(5, pk.laySoThuTu());
            ps.setString(6, pk.layTrangThai());
            ps.setString(7, pk.layGhiChu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int capNhat(Connection conn, int maPK, Integer maPhong, String trangThai,
                       String ghiChu, boolean capNhatGhiChu) throws SQLException {
        String sql = "UPDATE PhieuKham SET MaPhong = ?, TrangThai = ?"
                + (capNhatGhiChu ? ", GhiChu = ?" : "") + " WHERE MaPK = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (maPhong == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, maPhong);
            ps.setString(2, trangThai);
            int i = 3;
            if (capNhatGhiChu) ps.setString(i++, ghiChu);
            ps.setInt(i, maPK);
            return ps.executeUpdate();
        }
    }

    private void capNhatLichHen(Connection conn, int maLH, String trangThai) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE LichHen SET TrangThai = ? WHERE MaLH = ?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maLH);
            ps.executeUpdate();
        }
    }

    private void capNhatLichHenNeuDang(Connection conn, int maLH, String dangLa, String trangThaiMoi)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE LichHen SET TrangThai = ? WHERE MaLH = ? AND TrangThai = ?")) {
            ps.setString(1, trangThaiMoi);
            ps.setInt(2, maLH);
            ps.setString(3, dangLa);
            ps.executeUpdate();
        }
    }

    private PhieuKham doc(ResultSet rs) throws SQLException {
        PhieuKham pk = new PhieuKham();
        pk.datMaPK(rs.getInt("MaPK"));
        pk.datMaBN(rs.getInt("MaBN"));
        int maPhong = rs.getInt("MaPhong");
        pk.datMaPhong(rs.wasNull() ? null : maPhong);
        int maLH = rs.getInt("MaLH");
        pk.datMaLH(rs.wasNull() ? null : maLH);
        Date ngay = rs.getDate("NgayKham");
        pk.datNgayKham(ngay == null ? null : ngay.toLocalDate());
        pk.datSoThuTu(rs.getInt("SoThuTu"));
        pk.datTrangThai(rs.getString("TrangThai"));
        pk.datGhiChu(rs.getString("GhiChu"));
        pk.datTenBenhNhan(rs.getString("TenBN"));
        pk.datMaBenhNhanHienThi(String.format("BN%03d", pk.layMaBN()));
        pk.datTenPhong(rs.getString("TenPhong"));
        return pk;
    }
}
