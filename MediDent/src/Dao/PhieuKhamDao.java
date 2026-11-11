package Dao;

import ConnectDB.KetNoi;
import Entity.DichVu;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<PhieuKham> findHomNay() throws SQLException {
        return findTheoNgay(LocalDate.now());
    }

    public List<PhieuKham> findTheoNgay(LocalDate ngay) throws SQLException {
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE pk.NgayKham = ? ORDER BY pk.SoThuTu")) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public List<PhieuKham> findByBenhNhan(int maBN) throws SQLException {
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT + "WHERE pk.MaBN = ? ORDER BY pk.NgayKham DESC, pk.SoThuTu DESC")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    public PhieuKham findById(int maPK) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE pk.MaPK = ?")) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    // ===================== Dịch vụ chỉ định khi tiếp nhận =====================

    /** Lưu danh sách dịch vụ đã chọn (mỗi dịch vụ số lượng 1) cho một phiếu khám (thay toàn bộ danh sách cũ). */
    public void luuDichVu(int maPK, List<Integer> maDichVu) throws SQLException {
        luuDichVu(maPK, motDonVi(maDichVu));
    }

    /** Lưu dịch vụ + số lượng đã chọn cho một phiếu khám (thay toàn bộ danh sách cũ). Khóa: MaDV, giá trị: số lượng. */
    public void luuDichVu(int maPK, Map<Integer, Integer> dichVuSoLuong) throws SQLException {
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM PhieuKhamDichVu WHERE MaPK = ?")) {
                    ps.setInt(1, maPK);
                    ps.executeUpdate();
                }
                themDichVu(conn, maPK, dichVuSoLuong);
                try (PreparedStatement ps = conn.prepareStatement("UPDATE PhieuKham SET GhiChu = ? WHERE MaPK = ?")) {
                    PhieuKham hienTai = layVoiKhoa(conn, maPK);
                    String ghiChu = hienTai == null ? null : hienTai.getGhiChu();
                    ps.setString(1, ghepSoLuong(ghiChu, dichVuSoLuong));
                    ps.setInt(2, maPK);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // ===================== Số lượng dịch vụ lưu kèm ghi chú phiếu =====================
    // Bảng PhieuKhamDichVu chỉ có (MaPK, MaDV) nên số lượng > 1 được lưu thành một thẻ ở cuối cột GhiChu,
    // dạng "[SL:5=2;8=3]" (MaDV=số lượng). Thẻ được tách ra khi đọc và ghép lại khi ghi, nên phần còn lại
    // của ứng dụng chỉ thấy ghi chú gốc.

    private static final int GHI_CHU_TOI_DA = 300;
    private static final java.util.regex.Pattern THE_SO_LUONG =
            java.util.regex.Pattern.compile("\\s*\\[SL:([0-9=;]*)\\]\\s*$");

    /** Chỉ giữ các dịch vụ có số lượng lớn hơn 1 (mặc định là 1 nên không cần lưu). */
    static Map<Integer, Integer> soLuongLonHonMot(Map<Integer, Integer> m) {
        Map<Integer, Integer> kq = new LinkedHashMap<>();
        if (m != null) {
            for (Map.Entry<Integer, Integer> e : m.entrySet()) {
                if (e.getKey() != null && e.getValue() != null && e.getValue() > 1) kq.put(e.getKey(), e.getValue());
            }
        }
        return kq;
    }

    static String ghepSoLuong(String ghiChu, Map<Integer, Integer> soLuong) {
        String goc = ghiChu == null ? "" : ghiChu.trim();
        Map<Integer, Integer> can = soLuongLonHonMot(soLuong);
        if (can.isEmpty()) return goc.isEmpty() ? null : goc;
        StringBuilder the = new StringBuilder("[SL:");
        boolean dau = true;
        for (Map.Entry<Integer, Integer> e : can.entrySet()) {
            if (!dau) the.append(';');
            the.append(e.getKey()).append('=').append(e.getValue());
            dau = false;
        }
        the.append(']');
        String kq = goc.isEmpty() ? the.toString() : goc + "\n" + the;
        if (kq.length() > GHI_CHU_TOI_DA) {
            throw new IllegalArgumentException("Ghi chú quá dài: khi có dịch vụ chọn nhiều số lượng, ghi chú tối đa "
                    + (GHI_CHU_TOI_DA - the.length() - 1) + " ký tự.");
        }
        return kq;
    }

    public static String boTheSoLuong(String raw) {
        if (raw == null) return null;
        String s = THE_SO_LUONG.matcher(raw).replaceFirst("").trim();
        return s.isEmpty() ? null : s;
    }

    static Map<Integer, Integer> tachSoLuong(String raw) {
        Map<Integer, Integer> kq = new LinkedHashMap<>();
        if (raw == null) return kq;
        java.util.regex.Matcher m = THE_SO_LUONG.matcher(raw);
        if (!m.find()) return kq;
        for (String cap : m.group(1).split(";")) {
            String[] kv = cap.split("=");
            if (kv.length != 2) continue;
            try {
                kq.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
            } catch (NumberFormatException ignored) {
            }
        }
        return kq;
    }

    private static Map<Integer, Integer> motDonVi(List<Integer> maDichVu) {
        Map<Integer, Integer> m = new LinkedHashMap<>();
        if (maDichVu != null) for (Integer ma : maDichVu) if (ma != null) m.put(ma, 1);
        return m;
    }

    private void themDichVu(Connection conn, int maPK, Map<Integer, Integer> dichVuSoLuong) throws SQLException {
        if (dichVuSoLuong == null || dichVuSoLuong.isEmpty()) return;
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO PhieuKhamDichVu (MaPK, MaDV) VALUES (?, ?)")) {
            for (Map.Entry<Integer, Integer> e : dichVuSoLuong.entrySet()) {
                if (e.getKey() == null) continue;
                if (e.getValue() != null && e.getValue() < 1) {
                    throw new IllegalArgumentException("Số lượng dịch vụ phải từ 1 trở lên.");
                }
                ps.setInt(1, maPK);
                ps.setInt(2, e.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Danh sách dịch vụ đã chỉ định cho phiếu khám (dùng để nạp sẵn vào hóa đơn). */
    public List<DichVu> findDichVu(int maPK) throws SQLException {
        String sql = "SELECT dv.MaDV, dv.TenDichVu, dv.DonGia, dv.TrangThai "
                + "FROM PhieuKhamDichVu pkdv "
                + "JOIN DichVu dv ON dv.MaDV = pkdv.MaDV "
                + "WHERE pkdv.MaPK = ? ORDER BY dv.TenDichVu";
        List<DichVu> ds = new ArrayList<>();
        PhieuKham phieu = findById(maPK);
        Map<Integer, Integer> soLuong = phieu == null ? new LinkedHashMap<>() : phieu.getSoLuongDichVu();
        try (Connection conn = KetNoi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu(rs.getInt("MaDV"), rs.getString("TenDichVu"),
                            rs.getBigDecimal("DonGia"));
                    dv.setTrangThai(rs.getString("TrangThai"));
                    dv.setSoLuong(soLuong.getOrDefault(dv.getMaDV(), 1));
                    ds.add(dv);
                }
            }
        }
        return ds;
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
        return capSoThuTu(maBN, maPhong, maLH, null, (Map<Integer, Integer>) null);
    }

    /**
     * Như trên, nhưng lưu luôn ghi chú và danh sách dịch vụ chỉ định trong cùng một giao dịch:
     * hoặc có đủ phiếu + dịch vụ, hoặc không có gì (không bị phiếu "mồ côi" thiếu dịch vụ).
     */
    public PhieuKham capSoThuTu(int maBN, Integer maPhong, Integer maLH, String ghiChu,
                                List<Integer> maDichVu) throws SQLException {
        return capSoThuTu(maBN, maPhong, maLH, ghiChu, motDonVi(maDichVu));
    }

    /** Như trên, nhưng mỗi dịch vụ chỉ định kèm số lượng (khóa: MaDV, giá trị: số lượng). */
    public PhieuKham capSoThuTu(int maBN, Integer maPhong, Integer maLH, String ghiChu,
                                Map<Integer, Integer> dichVuSoLuong) throws SQLException {
        LocalDate homNay = LocalDate.now();
        try (Connection conn = KetNoi.getConnection()) {
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
                pk.setMaBN(maBN);
                pk.setMaPhong(maPhong);
                pk.setMaLH(maLH);
                pk.setNgayKham(homNay);
                pk.setSoThuTu(soThuTu);
                pk.setTrangThai(CHO_KHAM);
                pk.setTenBenhNhan(tenBN);
                pk.setGhiChu(ghiChu == null || ghiChu.isBlank() ? null : ghiChu.trim());
                pk.setSoLuongDichVu(soLuongLonHonMot(dichVuSoLuong));
                pk.setMaPK(insert(conn, pk));
                if (dichVuSoLuong != null && !dichVuSoLuong.isEmpty()) {
                    themDichVu(conn, pk.getMaPK(), dichVuSoLuong);
                }

                if (maLH != null) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE LichHen SET TrangThai = ? WHERE MaLH = ?")) {
                        ps.setString(1, LichHenDao.DA_DEN);
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
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraSucChuaPhong(conn, maPhong, pk.getNgayKham() == null ? LocalDate.now() : pk.getNgayKham());
                update(conn, pk.getMaPK(), maPhong, pk.getTrangThai(), pk.getGhiChu(), false);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai) throws SQLException {
        capNhat(pk, maPhong, trangThai, pk.getGhiChu());
    }

    /**
     * Cập nhật phòng / trạng thái / ghi chú. Khi phiếu hoàn thành mà được tạo
     * từ lịch hẹn thì lịch hẹn cũng chuyển sang "Hoàn thành".
     */
    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai, String ghiChu) throws SQLException {
        if (!Arrays.asList(TRANG_THAI).contains(trangThai)) {
            throw new IllegalArgumentException("Trạng thái phiếu khám không hợp lệ.");
        }
        try (Connection conn = KetNoi.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PhieuKham hienTai = layVoiKhoa(conn, pk.getMaPK());
                if (hienTai == null) {
                    throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
                }
                if (hienTai.daKetThuc() && !hienTai.getTrangThai().equals(trangThai)) {
                    throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể đổi trạng thái.");
                }
                if (maPhong != null && !maPhong.equals(hienTai.getMaPhong())) {
                    kiemTraSucChuaPhong(conn, maPhong, hienTai.getNgayKham());
                }
                update(conn, pk.getMaPK(), maPhong, trangThai,
                        ghepSoLuong(ghiChu, hienTai.getSoLuongDichVu()), true);

                if (hienTai.getMaLH() != null) {
                    if (HOAN_THANH.equals(trangThai)) {
                        capNhatLichHen(conn, hienTai.getMaLH(), LichHenDao.HOAN_THANH);
                    } else if (DA_HUY.equals(trangThai)) {
                        // Trả lịch hẹn về trạng thái có thể check-in lại.
                        capNhatLichHenNeuDang(conn, hienTai.getMaLH(), LichHenDao.DA_DEN, LichHenDao.DA_XAC_NHAN);
                    }
                }
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
        pk.setMaPhong(maPhong);
        pk.setTrangThai(trangThai);
        pk.setGhiChu(ghiChu);
    }

    public void batDauKham(int maPK) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (!pk.choKham()) {
            throw new IllegalArgumentException("Chỉ có thể bắt đầu khám phiếu đang ở trạng thái \"Chờ khám\".");
        }
        capNhat(pk, pk.getMaPhong(), DANG_KHAM, pk.getGhiChu());
    }

    public void hoanThanh(int maPK) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc.");
        }
        capNhat(pk, pk.getMaPhong(), HOAN_THANH, pk.getGhiChu());
    }

    public void huy(int maPK, String lyDo) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) {
            throw new IllegalArgumentException("Phiếu khám đã kết thúc.");
        }
        String ghiChu = lyDo == null || lyDo.isBlank() ? pk.getGhiChu() : lyDo.trim();
        capNhat(pk, pk.getMaPhong(), DA_HUY, ghiChu);
    }

    public int demTheoPhong(int maPhong, LocalDate ngay) throws SQLException {
        try (Connection conn = KetNoi.getConnection()) {
            return demPhieuTheoPhong(conn, maPhong, ngay);
        }
    }

    public int demTheoNgay(LocalDate ngay) throws SQLException {
        try (Connection conn = KetNoi.getConnection();
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
                if (!LichHenDao.CHO_XAC_NHAN.equals(tt) && !LichHenDao.DA_XAC_NHAN.equals(tt)) {
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

    private int insert(Connection conn, PhieuKham pk) throws SQLException {
        String sql = "INSERT INTO PhieuKham (MaBN, MaPhong, MaLH, NgayKham, SoThuTu, TrangThai, GhiChu) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pk.getMaBN());
            if (pk.getMaPhong() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, pk.getMaPhong());
            if (pk.getMaLH() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, pk.getMaLH());
            ps.setDate(4, Date.valueOf(pk.getNgayKham()));
            ps.setInt(5, pk.getSoThuTu());
            ps.setString(6, pk.getTrangThai());
            ps.setString(7, ghepSoLuong(pk.getGhiChu(), pk.getSoLuongDichVu()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    private int update(Connection conn, int maPK, Integer maPhong, String trangThai,
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
        pk.setMaPK(rs.getInt("MaPK"));
        pk.setMaBN(rs.getInt("MaBN"));
        int maPhong = rs.getInt("MaPhong");
        pk.setMaPhong(rs.wasNull() ? null : maPhong);
        int maLH = rs.getInt("MaLH");
        pk.setMaLH(rs.wasNull() ? null : maLH);
        Date ngay = rs.getDate("NgayKham");
        pk.setNgayKham(ngay == null ? null : ngay.toLocalDate());
        pk.setSoThuTu(rs.getInt("SoThuTu"));
        pk.setTrangThai(rs.getString("TrangThai"));
        String ghiChuTho = rs.getString("GhiChu");
        pk.setGhiChu(boTheSoLuong(ghiChuTho));
        pk.setSoLuongDichVu(tachSoLuong(ghiChuTho));
        pk.setTenBenhNhan(rs.getString("TenBN"));
        pk.setMaBenhNhanHienThi(String.format("BN%03d", pk.getMaBN()));
        pk.setTenPhong(rs.getString("TenPhong"));
        return pk;
    }
}
