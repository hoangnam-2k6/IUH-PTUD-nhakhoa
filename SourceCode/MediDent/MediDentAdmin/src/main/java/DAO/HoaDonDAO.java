package DAO;

import ConnectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    // =====================================================
    // DỮ LIỆU HÓA ĐƠN DÙNG ĐỂ HIỂN THỊ LÊN JTable
    // =====================================================
    public static class DongHoaDon {

        public final int maHD;
        public final int maPK;
        public final int maTT;
        public final LocalDate ngayLap;
        public final BigDecimal soTien;
        public final String phuongThuc;
        public final String trangThai;

        public DongHoaDon(
                int maHD,
                int maPK,
                int maTT,
                LocalDate ngayLap,
                BigDecimal soTien,
                String phuongThuc,
                String trangThai) {

            this.maHD = maHD;
            this.maPK = maPK;
            this.maTT = maTT;
            this.ngayLap = ngayLap;
            this.soTien = soTien;
            this.phuongThuc = phuongThuc;
            this.trangThai = trangThai;
        }

        public int layMaHD() {
            return maHD;
        }

        public int layMaPK() {
            return maPK;
        }

        public int layMaTT() {
            return maTT;
        }

        public LocalDate layNgayLap() {
            return ngayLap;
        }

        public BigDecimal laySoTien() {
            return soTien;
        }

        public String layPhuongThuc() {
            return phuongThuc;
        }

        public String layTrangThai() {
            return trangThai;
        }
    }

    // =====================================================
    // HÀM KẾT NỐI
    // Sử dụng đúng ConnectDB.GetInstance() và getConnection().
    // Không đóng Connection vì ConnectDB đang dùng kết nối Singleton.
    // =====================================================
    private Connection moKetNoi() throws SQLException {
        return ConnectDB.taoKetNoiMoi();
    }

    // Khôi phục cấu hình kết nối dùng chung sau khi hoàn tất transaction.
    private void khoiPhucKetNoi(
            Connection ketNoi,
            boolean autoCommitCu,
            int isolationCu) {

        try {
            if (ketNoi.getAutoCommit() != autoCommitCu) {
                ketNoi.setAutoCommit(autoCommitCu);
            }

            if (ketNoi.getTransactionIsolation() != isolationCu) {
                ketNoi.setTransactionIsolation(isolationCu);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // =====================================================
    // 1. LẤY TẤT CẢ HÓA ĐƠN
    // Tương tự getalldichvu() trong DichVuDAO.
    // =====================================================
    public ArrayList<DongHoaDon> layTatCaHoadon() throws SQLException {
        return timHoaDon(null, null, null);
    }

    // Giữ tên phương thức cũ để không làm hỏng code UI hiện tại.
    public List<DongHoaDon> danhSach(
            String tuKhoa,
            Integer thang,
            Integer nam) throws SQLException {

        return timHoaDon(tuKhoa, thang, nam);
    }

    // =====================================================
    // 2. TÌM KIẾM / LỌC HÓA ĐƠN
    // Tìm theo mã hóa đơn, mã phiếu khám, tháng và năm.
    // =====================================================
    public ArrayList<DongHoaDon> timHoaDon(
            String tuKhoa,
            Integer thang,
            Integer nam) throws SQLException {

        ArrayList<DongHoaDon> danhSachHoaDon = new ArrayList<>();

        if ((thang == null) != (nam == null)) {
            throw new IllegalArgumentException(
                "Phải chọn đồng thời cả tháng và năm."
            );
        }

        boolean coLocNgay = thang != null && nam != null;

        if (coLocNgay && (thang < 1 || thang > 12 || nam < 1)) {
            throw new IllegalArgumentException("Tháng hoặc năm không hợp lệ.");
        }

        String sql = """
                SELECT h.maHD,
                       h.maPK,
                       h.maTT,
                       CAST(h.ngayLap AS DATE) AS ngayLap,
                       h.tongTien,
                       h.trangThai,
                       t.phuongThuc
                FROM dbo.HoaDon h
                LEFT JOIN dbo.ThanhToan t ON h.maTT = t.maTT
                WHERE (
                    CAST(h.maHD AS NVARCHAR(20)) LIKE ?
                    OR CAST(h.maPK AS NVARCHAR(20)) LIKE ?
                )
                %s
                ORDER BY h.ngayLap DESC, h.maHD DESC
                """.formatted(
                    coLocNgay
                        ? "AND h.ngayLap >= ? AND h.ngayLap < ?"
                        : ""
                );

        try (Connection ketNoi = moKetNoi()) {
            kiemTraBang(ketNoi, false);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {

                String noiDungTim = "%"
                        + (tuKhoa == null ? "" : tuKhoa.trim())
                        + "%";

                lenh.setString(1, noiDungTim);
                lenh.setString(2, noiDungTim);

                if (coLocNgay) {
                    LocalDate ngayDau = LocalDate.of(nam, thang, 1);

                    lenh.setDate(3, Date.valueOf(ngayDau));
                    lenh.setDate(4, Date.valueOf(ngayDau.plusMonths(1)));
                }

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        Date ngayLapSql = ketQua.getDate("ngayLap");

                        LocalDate ngayLap = ngayLapSql == null
                                ? null
                                : ngayLapSql.toLocalDate();

                        danhSachHoaDon.add(new DongHoaDon(
                            ketQua.getInt("maHD"),
                            ketQua.getInt("maPK"),
                            ketQua.getInt("maTT"),
                            ngayLap,
                            ketQua.getBigDecimal("tongTien"),
                            ketQua.getString("phuongThuc"),
                            ketQua.getString("trangThai")
                        ));
                    }
                }
            }
        }

        return danhSachHoaDon;
    }

    // Bộ lọc khoảng ngày dùng chung với Thống kê, cho phép null = tất cả.
    public List<DongHoaDon> timHoaDonTheoNgay(String tuKhoa, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        if ((tuNgay == null) != (denNgay == null) || (tuNgay != null && tuNgay.isAfter(denNgay)))
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        String sql = "SELECT h.maHD,h.maPK,h.maTT,CAST(h.ngayLap AS DATE) ngayLap,h.tongTien,h.trangThai,t.phuongThuc "
                + "FROM dbo.HoaDon h LEFT JOIN dbo.ThanhToan t ON t.maTT=h.maTT "
                + "WHERE (CAST(h.maHD AS NVARCHAR(20)) LIKE ? OR CAST(h.maPK AS NVARCHAR(20)) LIKE ?) "
                + (tuNgay == null ? "" : "AND h.ngayLap>=? AND h.ngayLap<DATEADD(DAY,1,?) ")
                + "ORDER BY h.ngayLap DESC,h.maHD DESC";
        List<DongHoaDon> ds = new ArrayList<>();
        try (Connection con = moKetNoi(); PreparedStatement ps = con.prepareStatement(sql)) {
            String tim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
            ps.setString(1,tim); ps.setString(2,tim);
            if (tuNgay != null) { ps.setDate(3,Date.valueOf(tuNgay)); ps.setDate(4,Date.valueOf(denNgay)); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(new DongHoaDon(rs.getInt("maHD"),rs.getInt("maPK"),rs.getInt("maTT"),
                        rs.getDate("ngayLap").toLocalDate(),rs.getBigDecimal("tongTien"),
                        rs.getString("phuongThuc"),rs.getString("trangThai")));
            }
        }
        return ds;
    }

    // Các phiếu khám đã hoàn thành và chưa lập hóa đơn, để chọn bằng JComboBox.
    public List<String[]> danhSachPhieuKhamChuaLap() throws SQLException {
        String sql = "SELECT p.maPK,p.maBN FROM dbo.PhieuKham p "
                + "WHERE p.trangThai IN (N'Hoàn thành',N'Đã khám') "
                + "AND EXISTS (SELECT 1 FROM dbo.ChiTietPhieuKham c "
                + "WHERE c.maPK=p.maPK GROUP BY c.maPK HAVING SUM(c.thanhTien)>0) "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon h WHERE h.maPK=p.maPK) "
                + "ORDER BY p.maPK DESC";

        List<String[]> ds = new ArrayList<>();

        try (Connection con = moKetNoi();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(new String[]{
                    String.valueOf(rs.getInt("maPK")),
                    "Bệnh nhân " + rs.getString("maBN")
                });
            }
        }
        return ds;
    }

    public BigDecimal tongTienDichVu(int maPK) throws SQLException {
        String sql="SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietPhieuKham WHERE maPK=?";
        try (Connection con=moKetNoi();PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setInt(1,maPK);
            try(ResultSet rs=ps.executeQuery()) {rs.next();return rs.getBigDecimal(1);}
        }
    }

    // =====================================================
    // 3. LẤY MỘT HÓA ĐƠN THEO MÃ
    // Tương tự getDichVuByMaDV() trong DichVuDAO.
    // =====================================================
    public DongHoaDon layHoaDonByMaHD(int maHD) throws SQLException {

        if (maHD <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }

        String sql = """
                SELECT h.maHD,
                       h.maPK,
                       h.maTT,
                       CAST(h.ngayLap AS DATE) AS ngayLap,
                       h.tongTien,
                       h.trangThai,
                       t.phuongThuc
                FROM dbo.HoaDon h
                LEFT JOIN dbo.ThanhToan t ON h.maTT = t.maTT
                WHERE h.maHD = ?
                """;

        try (Connection ketNoi = moKetNoi()) {
            kiemTraBang(ketNoi, false);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setInt(1, maHD);

                try (ResultSet ketQua = lenh.executeQuery()) {
                    if (ketQua.next()) {
                        Date ngayLapSql = ketQua.getDate("ngayLap");

                        LocalDate ngayLap = ngayLapSql == null
                                ? null
                                : ngayLapSql.toLocalDate();

                        return new DongHoaDon(
                            ketQua.getInt("maHD"),
                            ketQua.getInt("maPK"),
                            ketQua.getInt("maTT"),
                            ngayLap,
                            ketQua.getBigDecimal("tongTien"),
                            ketQua.getString("phuongThuc"),
                            ketQua.getString("trangThai")
                        );
                    }
                }
            }
        }

        return null;
    }

    // =====================================================
    // 4. TẠO HÓA ĐƠN VÀ THANH TOÁN
    // Một phiếu khám chỉ được lập một hóa đơn.
    // Giữ lại tên tao() để tương thích code cũ.
    // =====================================================
	    
    public void tao(int maPK, BigDecimal soTien, String phuongThuc) throws SQLException {
    if (maPK <= 0 || soTien == null || soTien.signum() <= 0) {
        throw new IllegalArgumentException("Mã phiếu khám và số tiền phải hợp lệ.");
    }

    if (!"Tiền mặt".equals(phuongThuc) && !"Chuyển khoản".equals(phuongThuc)) {
        throw new IllegalArgumentException("Chọn phương thức thanh toán hợp lệ.");
    }

    Connection ketNoi = moKetNoi();
    boolean autoCommitCu = ketNoi.getAutoCommit();
    int isolationCu = ketNoi.getTransactionIsolation();

    try {
        kiemTraBang(ketNoi, false);
        ketNoi.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        ketNoi.setAutoCommit(false);

        try {
            // Kiểm tra phiếu khám đã hoàn thành.
            kiemTraPhieuKham(ketNoi, maPK);

            // Không cho một phiếu khám lập nhiều hóa đơn.
            kiemTraTrungHoaDon(ketNoi, maPK);

            // Kiểm tra chi tiết dịch vụ và tổng tiền.
            String sqlTong = "SELECT COUNT(*), COALESCE(SUM(thanhTien),0) "
                    + "FROM dbo.ChiTietPhieuKham WITH(HOLDLOCK) WHERE maPK=?";

            try (PreparedStatement ps = ketNoi.prepareStatement(sqlTong)) {
                ps.setInt(1, maPK);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Không đọc được chi tiết phiếu khám.");
                    }

                    if (rs.getInt(1) == 0) {
                        throw new SQLException("Phiếu khám chưa có chi tiết dịch vụ.");
                    }

                    BigDecimal tongDichVu = rs.getBigDecimal(2);

                    if (tongDichVu == null || tongDichVu.signum() <= 0) {
                        throw new SQLException("Tổng tiền dịch vụ phải lớn hơn 0.");
                    }

                    if (tongDichVu.compareTo(soTien) != 0) {
                        throw new SQLException("Số tiền phải bằng tổng dịch vụ: " + tongDichVu);
                    }
                }
            }

            // Tạo thanh toán ở trạng thái chờ xử lý.
            int maTT = themThanhToan(ketNoi, maPK, soTien, phuongThuc);

            // Tạo hóa đơn liên kết đúng mã thanh toán, mã phiếu khám.
            themHoaDon(ketNoi, maTT, maPK, soTien);

            // Chỉ lưu khi toàn bộ thao tác thành công.
            ketNoi.commit();

        } catch (SQLException | RuntimeException loi) {
            try {
                ketNoi.rollback();
            } catch (SQLException loiRollback) {
                loi.addSuppressed(loiRollback);
            }
            throw loi;
        }
    } finally {
        try {
            khoiPhucKetNoi(ketNoi, autoCommitCu, isolationCu);
        } finally {
            ketNoi.close();
        }
    }
}



        // Kiểm tra phiếu khám trước khi tạo hóa đơn.
    private void kiemTraPhieuKham(
            Connection ketNoi,
            int maPK) throws SQLException {

        String sql = """
                SELECT trangThai
                FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK)
                WHERE maPK = ?
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maPK);

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) {
                    throw new SQLException("Phiếu khám không tồn tại.");
                }

                String trangThai = ketQua.getString("trangThai");

                if (!"Hoàn thành".equals(trangThai)
                        && !"Đã khám".equals(trangThai)) {

                    throw new SQLException(
                        "Chỉ lập hóa đơn cho phiếu khám đã hoàn thành."
                    );
                }
            }
        }
    }

    // Kiểm tra phiếu khám đã có hóa đơn hay chưa.
    private void kiemTraTrungHoaDon(
            Connection ketNoi,
            int maPK) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK)
                WHERE maPK = ?
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maPK);

            try (ResultSet ketQua = lenh.executeQuery()) {
                ketQua.next();

                if (ketQua.getInt(1) > 0) {
                    throw new SQLException("Phiếu khám đã có hóa đơn.");
                }
            }
        }
    }

    // Thêm dữ liệu vào bảng ThanhToan.
    private int themThanhToan(
            Connection ketNoi,
            int maPK,
            BigDecimal soTien,
            String phuongThuc) throws SQLException {

        String sql = """
                INSERT INTO dbo.ThanhToan
                    (maPK, soTien, phuongThuc, trangThai)
                VALUES (?, ?, ?, N'Chờ xử lý')
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            lenh.setInt(1, maPK);
            lenh.setBigDecimal(2, soTien);
            lenh.setString(3, phuongThuc);

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không tạo được thanh toán.");
            }

            try (ResultSet ketQua = lenh.getGeneratedKeys()) {
                if (ketQua.next()) {
                    return ketQua.getInt(1);
                }
            }
        }

        throw new SQLException("Không lấy được mã thanh toán.");
    }

    // Thêm dữ liệu vào bảng HoaDon.
    private void themHoaDon(
            Connection ketNoi,
            int maTT,
            int maPK,
            BigDecimal soTien) throws SQLException {

        String sql = """
                INSERT INTO dbo.HoaDon
                    (maTT, maPK, tongTien, trangThai)
                VALUES (?, ?, ?, N'Chờ thanh toán')
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maTT);
            lenh.setInt(2, maPK);
            lenh.setBigDecimal(3, soTien);

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không lập được hóa đơn.");
            }
        }
    }

    // =====================================================
    // 5. XÁC NHẬN ĐÃ THU TIỀN
    // Cập nhật cả ThanhToan và HoaDon trong một transaction.
    // =====================================================
    public void xacNhanDaThu(int maHD) throws SQLException {

        if (maHD <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }

        Connection ketNoi = moKetNoi();
        boolean autoCommitCu = ketNoi.getAutoCommit();
        int isolationCu = ketNoi.getTransactionIsolation();

        try {
            kiemTraBang(ketNoi, false);
            ketNoi.setAutoCommit(false);

            try {
                int maTT = layMaThanhToan(ketNoi, maHD);

                String sqlThu = """
                        UPDATE dbo.ThanhToan
                        SET trangThai = N'Thành công',
                            ngayThanhToan = SYSDATETIME()
                        WHERE maTT = ?
                          AND trangThai = N'Chờ xử lý'
                        """;

                try (PreparedStatement lenh = ketNoi.prepareStatement(sqlThu)) {
                    lenh.setInt(1, maTT);

                    if (lenh.executeUpdate() != 1) {
                        throw new SQLException("Phiếu thu đã được xử lý.");
                    }
                }

                String sqlHoaDon = """
                        UPDATE dbo.HoaDon
                        SET trangThai = N'Đã thanh toán'
                        WHERE maHD = ?
                        """;

                try (PreparedStatement lenh = ketNoi.prepareStatement(sqlHoaDon)) {
                    lenh.setInt(1, maHD);

                    if (lenh.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được hóa đơn.");
                    }
                }

                ketNoi.commit();

            } catch (SQLException | RuntimeException loi) {
                try {
                    ketNoi.rollback();
                } catch (SQLException loiRollback) {
                    loi.addSuppressed(loiRollback);
                }
                throw loi;
            }
        } finally {
            khoiPhucKetNoi(ketNoi, autoCommitCu, isolationCu);
            ketNoi.close();
        }
    }


        // Lấy mã thanh toán của hóa đơn đang chờ thanh toán.
    private int layMaThanhToan(
            Connection ketNoi,
            int maHD) throws SQLException {

        String sql = """
                SELECT maTT, trangThai
                FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK)
                WHERE maHD = ?
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maHD);

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) {
                    throw new SQLException("Không tìm thấy hóa đơn.");
                }

                if (!"Chờ thanh toán".equals(
                        ketQua.getString("trangThai"))) {

                    throw new SQLException(
                        "Hóa đơn không còn chờ thanh toán."
                    );
                }

                return ketQua.getInt("maTT");
            }
        }
    }

    // =====================================================
    // 6. GỬI YÊU CẦU HỦY HÓA ĐƠN
    // Không xóa trực tiếp hóa đơn.
    // =====================================================
    public void guiYeuCauHuy(
            int maHD,
            String lyDo) throws SQLException {

        if (maHD <= 0) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        }

        if (lyDo == null || lyDo.isBlank() || lyDo.length() > 500) {
            throw new IllegalArgumentException(
                "Nhập lý do hủy từ 1 đến 500 ký tự."
            );
        }

        String sql = """
        	    INSERT INTO dbo.YeuCauHuyPhieuThu (maTT, lyDo, trangThai)
        	    SELECT h.maTT, ?, N'Chờ duyệt'
        	    FROM dbo.HoaDon h
        	    JOIN dbo.ThanhToan t ON t.maTT = h.maTT AND t.maPK = h.maPK
        	    WHERE h.maHD = ?
        	      AND h.trangThai = N'Chờ thanh toán'
        	      AND t.trangThai = N'Chờ xử lý'
        	      AND NOT EXISTS (
        	          SELECT 1 FROM dbo.YeuCauHuyPhieuThu y
        	          WHERE y.maTT = h.maTT AND y.trangThai = N'Chờ duyệt'
        	      )
        	    """;

        try (Connection ketNoi = moKetNoi()) {
            kiemTraBang(ketNoi, true);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setString(1, lyDo.trim());
                lenh.setInt(2, maHD);

                if (lenh.executeUpdate() != 1) {
                    throw new SQLException(
                        "Hóa đơn không hợp lệ hoặc đã có yêu cầu chờ duyệt."
                    );
                }
            }
        }
    }

    // =====================================================
    // 7. LẤY DANH SÁCH YÊU CẦU HỦY ĐANG CHỜ DUYỆT
    // =====================================================
    public List<String> yeuCauChoDuyet() throws SQLException {

        List<String> danhSachYeuCau = new ArrayList<>();

        String sql = """
                SELECT y.maYC, h.maHD, y.lyDo
                FROM dbo.YeuCauHuyPhieuThu y
                JOIN dbo.HoaDon h ON h.maTT = y.maTT
                WHERE y.trangThai = N'Chờ duyệt'
                ORDER BY y.maYC DESC
                """;

        try (Connection ketNoi = moKetNoi()) {
            kiemTraBang(ketNoi, true);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
                 ResultSet ketQua = lenh.executeQuery()) {

                while (ketQua.next()) {
                    String noiDung = ketQua.getInt("maYC")
                            + " - Hóa đơn "
                            + ketQua.getInt("maHD")
                            + ": "
                            + ketQua.getString("lyDo");

                    danhSachYeuCau.add(noiDung);
                }
            }
        }

        return danhSachYeuCau;
    }

    // =====================================================
    // 8. DUYỆT HOẶC TỪ CHỐI YÊU CẦU HỦY
    // =====================================================
    public void duyetHuy(
            int maYC,
            boolean dongY) throws SQLException {

        if (maYC <= 0) {
            throw new IllegalArgumentException("Mã yêu cầu hủy không hợp lệ.");
        }

        Connection ketNoi = moKetNoi();
        boolean autoCommitCu = ketNoi.getAutoCommit();
        int isolationCu = ketNoi.getTransactionIsolation();

        try {
            kiemTraBang(ketNoi, true);
            ketNoi.setAutoCommit(false);

            try {
                String sql = """
                        SELECT y.maTT, t.trangThai
                        FROM dbo.YeuCauHuyPhieuThu y WITH(UPDLOCK,HOLDLOCK)
                        JOIN dbo.ThanhToan t ON y.maTT = t.maTT
                        WHERE y.maYC = ?
                          AND y.trangThai = N'Chờ duyệt'
                        """;

                int maTT;
                String trangThaiThu;

                try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                    lenh.setInt(1, maYC);

                    try (ResultSet ketQua = lenh.executeQuery()) {
                        if (!ketQua.next()) {
                            throw new SQLException(
                                "Yêu cầu không tồn tại hoặc đã xử lý."
                            );
                        }

                        maTT = ketQua.getInt("maTT");
                        trangThaiThu = ketQua.getString("trangThai");
                    }
                }

                if (dongY && !"Chờ xử lý".equals(trangThaiThu)) {
                    throw new SQLException(
                        "Phiếu đã thu hoặc đã xử lý, không thể duyệt hủy khi chưa hoàn tiền."
                    );
                }

                capNhatYeuCau(ketNoi, maYC, dongY);

                if (dongY) {
                    huyThanhToanVaHoaDon(ketNoi, maTT);
                }

                luuPhieuDuyet(ketNoi, maYC, dongY);
                ketNoi.commit();

            } catch (SQLException | RuntimeException loi) {
                try {
                    ketNoi.rollback();
                } catch (SQLException loiRollback) {
                    loi.addSuppressed(loiRollback);
                }
                throw loi;
            }
        } finally {
            khoiPhucKetNoi(ketNoi, autoCommitCu, isolationCu);
            ketNoi.close();
        }
    }


        // Cập nhật trạng thái yêu cầu hủy.
    private void capNhatYeuCau(
            Connection ketNoi,
            int maYC,
            boolean dongY) throws SQLException {

        String sql = """
                UPDATE dbo.YeuCauHuyPhieuThu
                SET trangThai = ?
                WHERE maYC = ?
                  AND trangThai = N'Chờ duyệt'
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, dongY ? "Đã duyệt" : "Đã từ chối");
            lenh.setInt(2, maYC);

            if (lenh.executeUpdate() != 1) {
                throw new SQLException(
                    "Yêu cầu đã được người khác xử lý."
                );
            }
        }
    }

    // Hủy thanh toán và hóa đơn khi yêu cầu được duyệt.
    private void huyThanhToanVaHoaDon(
            Connection ketNoi,
            int maTT) throws SQLException {

        String sqlThu = """
                UPDATE dbo.ThanhToan
                SET trangThai = N'Đã hủy'
                WHERE maTT = ?
                """;

        String sqlHoaDon = """
                UPDATE dbo.HoaDon
                SET trangThai = N'Đã hủy'
                WHERE maTT = ?
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlThu)) {
            lenh.setInt(1, maTT);

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không hủy được thanh toán.");
            }
        }

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlHoaDon)) {
            lenh.setInt(1, maTT);

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không hủy được hóa đơn.");
            }
        }
    }

    // Lưu thông tin duyệt hủy.
    private void luuPhieuDuyet(
            Connection ketNoi,
            int maYC,
            boolean dongY) throws SQLException {

        String sql = """
                INSERT INTO dbo.PhieuDuyetHuyPhieuThu
                    (maYC, ngayPheDuyet, lyDoPheDuyet)
                VALUES (?, SYSDATETIME(), ?)
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maYC);
            lenh.setString(
                2,
                dongY
                    ? "Duyệt hủy phiếu chưa thực thu"
                    : "Không duyệt hủy"
            );

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không lưu được phiếu duyệt.");
            }
        }
    }

    // =====================================================
    // 9. THỐNG KÊ DOANH THU THEO NGÀY, THÁNG, NĂM
    // =====================================================
    public List<Object[]> thongKeDoanhThu(
            LocalDate tuNgay,
            LocalDate denNgay,
            String theo) throws SQLException {

        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException(
                "Khoảng ngày thống kê không hợp lệ."
            );
        }

        String ky;

        switch (theo == null ? "" : theo) {
            case "Tháng":
                ky = "CONVERT(VARCHAR(7), t.ngayThanhToan, 23)";
                break;

            case "Năm":
                ky = "CONVERT(VARCHAR(4), YEAR(t.ngayThanhToan))";
                break;

            case "Ngày":
            case "":
                ky = "CONVERT(VARCHAR(10), t.ngayThanhToan, 23)";
                break;

            default:
                throw new IllegalArgumentException(
                    "Kiểu thống kê phải là Ngày, Tháng hoặc Năm."
                );
        }

        String sql = """
                SELECT %s AS ky,
                       COUNT(*) AS soGD,
                       SUM(t.soTien) AS doanhThu
                FROM dbo.ThanhToan t
                JOIN dbo.HoaDon h ON h.maTT = t.maTT
                WHERE t.trangThai = N'Thành công'
                  AND h.trangThai = N'Đã thanh toán'
                  AND t.ngayThanhToan >= ?
                  AND t.ngayThanhToan < DATEADD(DAY, 1, ?)
                GROUP BY %s
                ORDER BY ky
                """.formatted(ky, ky);

        List<Object[]> danhSachThongKe = new ArrayList<>();

        try (Connection ketNoi = moKetNoi()) {
            kiemTraBang(ketNoi, false);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setDate(1, Date.valueOf(tuNgay));
                lenh.setDate(2, Date.valueOf(denNgay));

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        danhSachThongKe.add(new Object[] {
                            ketQua.getString("ky"),
                            ketQua.getInt("soGD"),
                            ketQua.getBigDecimal("doanhThu")
                        });
                    }
                }
            }
        }

        return danhSachThongKe;
    }

    // =====================================================
    // 10. KIỂM TRA CÁC BẢNG CẦN DÙNG TRONG DATABASE
    // =====================================================
    private void kiemTraBang(
            Connection ketNoi,
            boolean canDuyet) throws SQLException {

        String[] cacBang = canDuyet
                ? new String[] {
                    "HoaDon",
                    "ThanhToan",
                    "PhieuKham",
                    "YeuCauHuyPhieuThu",
                    "PhieuDuyetHuyPhieuThu"
                }
                : new String[] {
                    "HoaDon",
                    "ThanhToan",
                    "PhieuKham"
                };

        String sql = "SELECT OBJECT_ID(?, N'U')";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            for (String tenBang : cacBang) {
                lenh.setString(1, "dbo." + tenBang);

                try (ResultSet ketQua = lenh.executeQuery()) {
                    if (!ketQua.next() || ketQua.getObject(1) == null) {
                        throw new SQLException(
                            "Chưa có bảng " + tenBang
                            + " trong SQL Server. Cần đồng bộ CSDL với nhóm."
                        );
                    }
                }
            }
        }
    }
}
