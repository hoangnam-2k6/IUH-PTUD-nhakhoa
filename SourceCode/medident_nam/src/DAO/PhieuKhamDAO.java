
package DAO;

import ConnectDB.ConnectDB;
import Entity.PhieuKham;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Nghiệp vụ phiếu khám theo cấu trúc MaPK/MaBN/MaPhong/MaLH kiểu INT. */
public class PhieuKhamDAO {
    public static final String CHO_KHAM = PhieuKham.CHO_KHAM;
    public static final String DANG_KHAM = PhieuKham.DANG_KHAM;
    public static final String HOAN_THANH = PhieuKham.HOAN_THANH;
    public static final String DA_HUY = PhieuKham.DA_HUY;
    public static final String[] TRANG_THAI = {CHO_KHAM, DANG_KHAM, HOAN_THANH, DA_HUY};
    public static final int TOI_DA_MOI_PHONG = 20;

    private static final String SELECT = "SELECT pk.MaPK, pk.MaBN, pk.MaPhong, pk.MaLH, pk.NgayKham, "
            + "pk.SoThuTu, pk.TrangThai, pk.GhiChu, bn.HoTen AS TenBN, p.TenPhong "
            + "FROM dbo.PhieuKham pk JOIN dbo.BenhNhan bn ON bn.MaBN=pk.MaBN "
            + "LEFT JOIN dbo.PhongKham p ON p.MaPhong=pk.MaPhong ";

    // 1. Lấy các phiếu khám hôm nay.
    public List<PhieuKham> findHomNay() throws SQLException {
        return findTheoNgay(LocalDate.now());
    }

    // 2. Tìm phiếu khám theo ngày.
    public List<PhieuKham> findTheoNgay(LocalDate ngay) throws SQLException {
        if (ngay == null) throw new IllegalArgumentException("Ngày khám không hợp lệ.");

        List<PhieuKham> ds = new ArrayList<>();
        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE pk.NgayKham=? ORDER BY pk.SoThuTu")) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    // 3. Lấy lịch sử phiếu khám của bệnh nhân.
    public List<PhieuKham> findByBenhNhan(int maBN) throws SQLException {
        List<PhieuKham> ds = new ArrayList<>();
        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE pk.MaBN=? ORDER BY pk.NgayKham DESC, pk.SoThuTu DESC")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(doc(rs));
            }
        }
        return ds;
    }

    // 4. Tìm phiếu khám theo mã.
    public PhieuKham findById(int maPK) throws SQLException {
        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE pk.MaPK=?")) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    // 5. Cấp số thứ tự không chỉ định phòng và lịch hẹn.
    public PhieuKham capSoThuTu(int maBN) throws SQLException {
        return capSoThuTu(maBN, null, null);
    }

    // 6. Cấp số thứ tự và chỉ định phòng.
    public PhieuKham capSoThuTu(int maBN, Integer maPhong) throws SQLException {
        return capSoThuTu(maBN, maPhong, null);
    }

    // 7. Cấp số thứ tự, kiểm tra phòng và check-in lịch hẹn.
    public PhieuKham capSoThuTu(int maBN, Integer maPhong, Integer maLH) throws SQLException {
        if (maBN <= 0 || (maPhong != null && maPhong <= 0) || (maLH != null && maLH <= 0))
            throw new IllegalArgumentException("Mã bệnh nhân, phòng khám hoặc lịch hẹn không hợp lệ.");

        LocalDate ngay = LocalDate.now();
        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            con.setAutoCommit(false);

            try {
                String tenBN = layTenBenhNhan(con, maBN);
                kiemTraLichHen(con, maBN, maLH);

                if (demPhieuChuaHuy(con, maBN, ngay) > 0)
                    throw new IllegalArgumentException("Bệnh nhân " + tenBN + " đã được cấp số thứ tự hôm nay.");

                if (maPhong != null) kiemTraSucChuaPhong(con, maPhong, ngay);

                PhieuKham pk = new PhieuKham();
                pk.setMaBN(maBN);
                pk.setMaPhong(maPhong);
                pk.setMaLH(maLH);
                pk.setNgayKham(ngay);
                pk.setSoThuTu(layMaxSoThuTu(con, ngay) + 1);
                pk.setTrangThai(CHO_KHAM);
                pk.setTenBenhNhan(tenBN);
                pk.setMaBenhNhanHienThi(String.format("BN%03d", maBN));
                pk.setMaPK(insert(con, pk));

                if (maLH != null) capNhatLichHen(con, maLH, "Đã đến");

                con.commit();
                return pk;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // 8. Chỉ định phòng khám cho phiếu.
    public void chiDinhPhong(PhieuKham pk, int maPhong) throws SQLException {
        if (pk == null || pk.getMaPK() <= 0 || maPhong <= 0)
            throw new IllegalArgumentException("Phiếu khám hoặc phòng khám không hợp lệ.");

        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            con.setAutoCommit(false);
            try {
                PhieuKham hienTai = layVoiKhoa(con, pk.getMaPK());
                if (hienTai == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");

                if (hienTai.daKetThuc())
                    throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể chỉ định phòng.");

                if (!Integer.valueOf(maPhong).equals(hienTai.getMaPhong()))
                    kiemTraSucChuaPhong(con, maPhong, hienTai.getNgayKham());

                if (update(con, pk.getMaPK(), maPhong, hienTai.getTrangThai(), hienTai.getGhiChu(), false) != 1)
                    throw new SQLException("Không cập nhật được phòng khám.");

                con.commit();
                pk.setMaPhong(maPhong);

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // 9. Cập nhật phòng và trạng thái, giữ ghi chú cũ.
    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai) throws SQLException {
        capNhat(pk, maPhong, trangThai, pk == null ? null : pk.getGhiChu());
    }

    // 10. Cập nhật phòng, trạng thái, ghi chú và lịch hẹn liên quan.
    public void capNhat(PhieuKham pk, Integer maPhong, String trangThai, String ghiChu) throws SQLException {
        if (pk == null || pk.getMaPK() <= 0)
            throw new IllegalArgumentException("Phiếu khám không hợp lệ.");

        if (!Arrays.asList(TRANG_THAI).contains(trangThai))
            throw new IllegalArgumentException("Trạng thái phiếu khám không hợp lệ.");

        if (maPhong != null && maPhong <= 0)
            throw new IllegalArgumentException("Phòng khám không hợp lệ.");

        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            con.setAutoCommit(false);
            try {
                PhieuKham hienTai = layVoiKhoa(con, pk.getMaPK());
                if (hienTai == null)
                    throw new IllegalArgumentException("Không tìm thấy phiếu khám.");

                if (hienTai.daKetThuc() && !hienTai.getTrangThai().equals(trangThai))
                    throw new IllegalArgumentException("Phiếu khám đã kết thúc, không thể đổi trạng thái.");

                if (maPhong != null && !maPhong.equals(hienTai.getMaPhong()))
                    kiemTraSucChuaPhong(con, maPhong, hienTai.getNgayKham());

                if (update(con, pk.getMaPK(), maPhong, trangThai, ghiChu, true) != 1)
                    throw new SQLException("Không cập nhật được phiếu khám.");

                if (hienTai.getMaLH() != null) {
                    if (HOAN_THANH.equals(trangThai)) {
                        capNhatLichHen(con, hienTai.getMaLH(), "Hoàn thành");
                    } else if (DA_HUY.equals(trangThai)) {
                        capNhatLichHenNeuDang(con, hienTai.getMaLH(), "Đã đến", "Đã xác nhận");
                    }
                }

                con.commit();
                pk.setMaPhong(maPhong);
                pk.setTrangThai(trangThai);
                pk.setGhiChu(ghiChu);

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // 11. Bắt đầu khám.
    public void batDauKham(int maPK) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (!pk.choKham()) throw new IllegalArgumentException("Chỉ bắt đầu khám với phiếu Chờ khám.");

        capNhat(pk, pk.getMaPhong(), DANG_KHAM, pk.getGhiChu());
    }

    // 12. Hoàn thành phiếu khám.
    public void hoanThanh(int maPK) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) throw new IllegalArgumentException("Phiếu khám đã kết thúc.");

        capNhat(pk, pk.getMaPhong(), HOAN_THANH, pk.getGhiChu());
    }

    // 13. Hủy phiếu khám.
    public void huy(int maPK, String lyDo) throws SQLException {
        PhieuKham pk = findById(maPK);
        if (pk == null) throw new IllegalArgumentException("Không tìm thấy phiếu khám.");
        if (pk.daKetThuc()) throw new IllegalArgumentException("Phiếu khám đã kết thúc.");

        String ghiChu = lyDo == null || lyDo.isBlank() ? pk.getGhiChu() : lyDo.trim();
        capNhat(pk, pk.getMaPhong(), DA_HUY, ghiChu);
    }

    // 14. Thống kê số phiếu trong phòng.
    public int demTheoPhong(int maPhong, LocalDate ngay) throws SQLException {
        try (Connection con = ConnectDB.taoKetNoiMoi()) {
            return demPhieuTheoPhong(con, maPhong, ngay);
        }
    }

    // 15. Thống kê số phiếu theo ngày.
    public int demTheoNgay(LocalDate ngay) throws SQLException {
        try (Connection con = ConnectDB.taoKetNoiMoi();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dbo.PhieuKham WHERE NgayKham=? AND TrangThai<>?")) {
            ps.setDate(1, Date.valueOf(ngay));
            ps.setString(2, DA_HUY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // 16. Kiểm tra lịch hẹn có hợp lệ để check-in.
    private void kiemTraLichHen(Connection con, int maBN, Integer maLH) throws SQLException {
        if (maLH == null) return;

        String sql = "SELECT MaBN,TrangThai FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE MaLH=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maLH);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy lịch hẹn cần check-in.");
                if (rs.getInt("MaBN") != maBN)
                    throw new IllegalArgumentException("Lịch hẹn không thuộc bệnh nhân này.");

                String tt = rs.getString("TrangThai");
                if (!"Chờ xác nhận".equals(tt) && !"Đã xác nhận".equals(tt))
                    throw new IllegalArgumentException("Lịch hẹn có trạng thái '" + tt + "', không thể check-in.");
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM dbo.PhieuKham WHERE MaLH=? AND TrangThai<>?")) {
            ps.setInt(1, maLH);
            ps.setString(2, DA_HUY);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0)
                    throw new IllegalArgumentException("Lịch hẹn này đã được check-in.");
            }
        }
    }

    // 17. Lấy tên bệnh nhân.
    private String layTenBenhNhan(Connection con, int maBN) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT HoTen FROM dbo.BenhNhan WHERE MaBN=?")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy bệnh nhân.");
                return rs.getString(1);
            }
        }
    }

    // 18. Kiểm tra phòng còn hoạt động và còn sức chứa.
    private void kiemTraSucChuaPhong(Connection con, int maPhong, LocalDate ngay) throws SQLException {
        int sucChua;
        String trangThai;

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT SucChua,TrangThai FROM dbo.PhongKham WHERE MaPhong=?")) {
            ps.setInt(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy phòng khám.");
                sucChua = rs.getInt("SucChua");
                trangThai = rs.getString("TrangThai");
            }
        }

        if ("Ngừng hoạt động".equals(trangThai))
            throw new IllegalArgumentException("Phòng khám đang ngừng hoạt động.");

        if (demPhieuTheoPhong(con, maPhong, ngay) >= sucChua)
            throw new IllegalArgumentException("Phòng đã đủ " + sucChua + " bệnh nhân trong ngày.");
    }

    // 19. Lấy số thứ tự lớn nhất của ngày.
    private int layMaxSoThuTu(Connection con, LocalDate ngay) throws SQLException {
        String sql = "SELECT ISNULL(MAX(SoThuTu),0) FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE NgayKham=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // 20. Đếm phiếu theo phòng, không tính phiếu hủy.
    private int demPhieuTheoPhong(Connection con, int maPhong, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.PhieuKham WHERE MaPhong=? AND NgayKham=? AND TrangThai<>?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPhong);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, DA_HUY);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // 21. Kiểm tra bệnh nhân đã có phiếu khám hôm nay.
    private int demPhieuChuaHuy(Connection con, int maBN, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE MaBN=? AND NgayKham=? AND TrangThai<>?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBN);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setString(3, DA_HUY);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // 22. Đọc phiếu khám có khóa trong transaction.
    private PhieuKham layVoiKhoa(Connection con, int maPK) throws SQLException {
        String sql = "SELECT pk.MaPK,pk.MaBN,pk.MaPhong,pk.MaLH,pk.NgayKham,pk.SoThuTu,pk.TrangThai,"
                + "pk.GhiChu,bn.HoTen AS TenBN,p.TenPhong FROM dbo.PhieuKham pk WITH(UPDLOCK,HOLDLOCK) "
                + "JOIN dbo.BenhNhan bn ON bn.MaBN=pk.MaBN "
                + "LEFT JOIN dbo.PhongKham p ON p.MaPhong=pk.MaPhong WHERE pk.MaPK=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maPK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? doc(rs) : null;
            }
        }
    }

    // 23. Thêm phiếu khám.
    private int insert(Connection con, PhieuKham pk) throws SQLException {
        String sql = "INSERT INTO dbo.PhieuKham(MaBN,MaPhong,MaLH,NgayKham,SoThuTu,TrangThai,GhiChu) VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pk.getMaBN());
            if (pk.getMaPhong() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, pk.getMaPhong());

            if (pk.getMaLH() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, pk.getMaLH());

            ps.setDate(4, Date.valueOf(pk.getNgayKham()));
            ps.setInt(5, pk.getSoThuTu());
            ps.setString(6, pk.getTrangThai());
            ps.setString(7, pk.getGhiChu());

            if (ps.executeUpdate() != 1) throw new SQLException("Không tạo được phiếu khám.");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Không lấy được mã phiếu khám mới.");
            }
        }
    }

    // 24. Cập nhật phiếu khám.
    private int update(Connection con, int maPK, Integer maPhong, String trangThai,
                       String ghiChu, boolean capNhatGhiChu) throws SQLException {
        String sql = "UPDATE dbo.PhieuKham SET MaPhong=?,TrangThai=?"
                + (capNhatGhiChu ? ",GhiChu=?" : "") + " WHERE MaPK=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (maPhong == null) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, maPhong);

            ps.setString(2, trangThai);
            int i = 3;

            if (capNhatGhiChu) ps.setString(i++, ghiChu);
            ps.setInt(i, maPK);
            return ps.executeUpdate();
        }
    }

    // 25. Cập nhật trạng thái lịch hẹn.
    private void capNhatLichHen(Connection con, int maLH, String trangThai) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE dbo.LichHen SET TrangThai=? WHERE MaLH=?")) {
            ps.setString(1, trangThai);
            ps.setInt(2, maLH);
            if (ps.executeUpdate() != 1) throw new SQLException("Không cập nhật được lịch hẹn.");
        }
    }

    // 26. Chỉ chuyển lịch hẹn nếu đúng trạng thái yêu cầu.
    private void capNhatLichHenNeuDang(Connection con, int maLH, String dangLa, String trangThaiMoi) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE dbo.LichHen SET TrangThai=? WHERE MaLH=? AND TrangThai=?")) {
            ps.setString(1, trangThaiMoi);
            ps.setInt(2, maLH);
            ps.setString(3, dangLa);
            ps.executeUpdate();
        }
    }

    // 27. Chuyển ResultSet thành Entity.PhieuKham.
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
        pk.setGhiChu(rs.getString("GhiChu"));
        pk.setTenBenhNhan(rs.getString("TenBN"));
        pk.setMaBenhNhanHienThi(String.format("BN%03d", pk.getMaBN()));
        pk.setTenPhong(rs.getString("TenPhong"));
        return pk;
    }
}
