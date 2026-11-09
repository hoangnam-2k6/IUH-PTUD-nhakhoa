
package DAO;

import ConnectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    // Thong tin hoa don de hien thi len JTable
    public static class DongHoaDon {
        public final int maHD, maPK, maTT;
        public final LocalDate ngayLap;
        public final BigDecimal soTien;
        public final String phuongThuc, trangThai;

        public DongHoaDon(int maHD, int maPK, int maTT, LocalDate ngayLap, BigDecimal soTien, String phuongThuc, String trangThai) {
            this.maHD = maHD;
            this.maPK = maPK;
            this.maTT = maTT;
            this.ngayLap = ngayLap;
            this.soTien = soTien;
            this.phuongThuc = phuongThuc;
            this.trangThai = trangThai;
        }
    }

    // Lay danh sach hoa don va tim kiem theo thang, nam
    public List<DongHoaDon> danhSach(String tuKhoa, Integer thang, Integer nam) throws SQLException {
        List<DongHoaDon> danhSachHoaDon = new ArrayList<>();
        boolean coLocNgay = thang != null && nam != null;

        String sql = """
                SELECT h.maHD, h.maPK, h.maTT, CAST(h.ngayLap AS DATE) AS ngayLap,
                       h.tongTien, h.trangThai, t.phuongThuc
                FROM dbo.HoaDon h
                JOIN dbo.ThanhToan t ON h.maTT = t.maTT
                WHERE (CAST(h.maHD AS NVARCHAR(20)) LIKE ? OR CAST(h.maPK AS NVARCHAR(20)) LIKE ?)
                %s
                ORDER BY h.ngayLap DESC, h.maHD DESC
                """.formatted(coLocNgay ? "AND h.ngayLap >= ? AND h.ngayLap < ?" : "");

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, false);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                String noiDungTim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
                lenh.setString(1, noiDungTim);
                lenh.setString(2, noiDungTim);

                if (coLocNgay) {
                    LocalDate ngayDau = LocalDate.of(nam, thang, 1);
                    lenh.setDate(3, Date.valueOf(ngayDau));
                    lenh.setDate(4, Date.valueOf(ngayDau.plusMonths(1)));
                }

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        danhSachHoaDon.add(new DongHoaDon(
                            ketQua.getInt("maHD"), ketQua.getInt("maPK"), ketQua.getInt("maTT"),
                            ketQua.getDate("ngayLap").toLocalDate(), ketQua.getBigDecimal("tongTien"),
                            ketQua.getString("phuongThuc"), ketQua.getString("trangThai")
                        ));
                    }
                }
            }
        }
        return danhSachHoaDon;
    }

    // Tao thanh toan va hoa don
    public void tao(int maPK, BigDecimal soTien, String phuongThuc) throws SQLException {
        if (maPK <= 0 || soTien == null || soTien.signum() <= 0) {
            throw new IllegalArgumentException("Mã phiếu khám và số tiền phải hợp lệ.");
        }
        if (!"Tiền mặt".equals(phuongThuc) && !"Chuyển khoản".equals(phuongThuc)) {
            throw new IllegalArgumentException("Chọn phương thức thanh toán hợp lệ.");
        }

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, false);
            ketNoi.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ketNoi.setAutoCommit(false);

            try {
                kiemTraPhieuKham(ketNoi, maPK);
                kiemTraTrungHoaDon(ketNoi, maPK);

                int maThanhToan = themThanhToan(ketNoi, maPK, soTien, phuongThuc);
                themHoaDon(ketNoi, maThanhToan, maPK, soTien);

                ketNoi.commit();
            } catch (SQLException | RuntimeException loi) {
                ketNoi.rollback();
                throw loi;
            }
        }
    }

    // Kiem tra phieu kham truoc khi tao hoa don
    private void kiemTraPhieuKham(Connection ketNoi, int maPK) throws SQLException {
        String sql = "SELECT trangThai FROM dbo.PhieuKham WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maPK);

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) throw new SQLException("Phiếu khám không tồn tại.");

                String trangThai = ketQua.getString("trangThai");
                if (!"Hoàn thành".equals(trangThai) && !"Đã khám".equals(trangThai)) {
                    throw new SQLException("Chỉ lập hóa đơn cho phiếu khám đã hoàn thành.");
                }
            }
        }
    }

    // Kiem tra mot phieu kham da co hoa don hay chua
    private void kiemTraTrungHoaDon(Connection ketNoi, int maPK) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE maPK=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maPK);

            try (ResultSet ketQua = lenh.executeQuery()) {
                ketQua.next();
                if (ketQua.getInt(1) > 0) throw new SQLException("Phiếu khám đã có hóa đơn.");
            }
        }
    }

    // Them du lieu vao bang ThanhToan
    private int themThanhToan(Connection ketNoi, int maPK, BigDecimal soTien, String phuongThuc) throws SQLException {
        String sql = "INSERT INTO dbo.ThanhToan(maPK,soTien,phuongThuc,trangThai) VALUES(?,?,?,N'Chờ xử lý')";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            lenh.setInt(1, maPK);
            lenh.setBigDecimal(2, soTien);
            lenh.setString(3, phuongThuc);

            if (lenh.executeUpdate() != 1) throw new SQLException("Không tạo được thanh toán.");

            try (ResultSet ketQua = lenh.getGeneratedKeys()) {
                if (ketQua.next()) return ketQua.getInt(1);
            }
        }
        throw new SQLException("Không lấy được mã thanh toán.");
    }

    // Them du lieu vao bang HoaDon
    private void themHoaDon(Connection ketNoi, int maTT, int maPK, BigDecimal soTien) throws SQLException {
        String sql = "INSERT INTO dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,N'Chờ thanh toán')";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maTT);
            lenh.setInt(2, maPK);
            lenh.setBigDecimal(3, soTien);

            if (lenh.executeUpdate() != 1) throw new SQLException("Không lập được hóa đơn.");
        }
    }

    // Xac nhan da thu tien
    public void xacNhanDaThu(int maHD) throws SQLException {
        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, false);
            ketNoi.setAutoCommit(false);

            try {
                int maTT = layMaThanhToan(ketNoi, maHD);

                String sqlThu = """
                        UPDATE dbo.ThanhToan
                        SET trangThai = N'Thành công', ngayThanhToan = SYSDATETIME()
                        WHERE maTT = ? AND trangThai = N'Chờ xử lý'
                        """;

                try (PreparedStatement lenh = ketNoi.prepareStatement(sqlThu)) {
                    lenh.setInt(1, maTT);
                    if (lenh.executeUpdate() != 1) throw new SQLException("Phiếu thu đã được xử lý.");
                }

                String sqlHoaDon = "UPDATE dbo.HoaDon SET trangThai=N'Đã thanh toán' WHERE maHD=?";
                try (PreparedStatement lenh = ketNoi.prepareStatement(sqlHoaDon)) {
                    lenh.setInt(1, maHD);
                    if (lenh.executeUpdate() != 1) throw new SQLException("Không cập nhật được hóa đơn.");
                }

                ketNoi.commit();
            } catch (SQLException | RuntimeException loi) {
                ketNoi.rollback();
                throw loi;
            }
        }
    }

    // Lay ma thanh toan cua hoa don dang cho
    private int layMaThanhToan(Connection ketNoi, int maHD) throws SQLException {
        String sql = "SELECT maTT,trangThai FROM dbo.HoaDon WITH(UPDLOCK,HOLDLOCK) WHERE maHD=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maHD);

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) throw new SQLException("Không tìm thấy hóa đơn.");
                if (!"Chờ thanh toán".equals(ketQua.getString("trangThai"))) {
                    throw new SQLException("Hóa đơn không còn chờ thanh toán.");
                }
                return ketQua.getInt("maTT");
            }
        }
    }

    // Gui yeu cau huy hoa don
    public void guiYeuCauHuy(int maHD, String lyDo) throws SQLException {
        if (lyDo == null || lyDo.isBlank() || lyDo.length() > 500) {
            throw new IllegalArgumentException("Nhập lý do hủy từ 1 đến 500 ký tự.");
        }

        String sql = """
                INSERT INTO dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai)
                SELECT h.maTT, ?, N'Chờ duyệt'
                FROM dbo.HoaDon h
                WHERE h.maHD = ? AND h.trangThai <> N'Đã hủy'
                AND NOT EXISTS (
                    SELECT 1 FROM dbo.YeuCauHuyPhieuThu y
                    WHERE y.maTT = h.maTT AND y.trangThai = N'Chờ duyệt'
                )
                """;

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, true);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setString(1, lyDo.trim());
                lenh.setInt(2, maHD);

                if (lenh.executeUpdate() != 1) {
                    throw new SQLException("Hóa đơn không hợp lệ hoặc đã có yêu cầu chờ duyệt.");
                }
            }
        }
    }

    // Lay danh sach yeu cau huy dang cho duyet
    public List<String> yeuCauChoDuyet() throws SQLException {
        List<String> danhSachYeuCau = new ArrayList<>();

        String sql = """
                SELECT y.maYC, h.maHD, y.lyDo
                FROM dbo.YeuCauHuyPhieuThu y
                JOIN dbo.HoaDon h ON h.maTT = y.maTT
                WHERE y.trangThai = N'Chờ duyệt'
                ORDER BY y.maYC DESC
                """;

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, true);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql);
                 ResultSet ketQua = lenh.executeQuery()) {

                while (ketQua.next()) {
                    String noiDung = ketQua.getInt("maYC") + " - Hóa đơn "
                            + ketQua.getInt("maHD") + ": " + ketQua.getString("lyDo");
                    danhSachYeuCau.add(noiDung);
                }
            }
        }
        return danhSachYeuCau;
    }

    // Duyet hoac tu choi yeu cau huy
    public void duyetHuy(int maYC, boolean dongY) throws SQLException {
        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, true);
            ketNoi.setAutoCommit(false);

            try {
                String sql = """
                        SELECT y.maTT, t.trangThai
                        FROM dbo.YeuCauHuyPhieuThu y WITH(UPDLOCK,HOLDLOCK)
                        JOIN dbo.ThanhToan t ON y.maTT = t.maTT
                        WHERE y.maYC = ? AND y.trangThai = N'Chờ duyệt'
                        """;

                int maTT;
                String trangThaiThu;

                try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                    lenh.setInt(1, maYC);

                    try (ResultSet ketQua = lenh.executeQuery()) {
                        if (!ketQua.next()) throw new SQLException("Yêu cầu không tồn tại hoặc đã xử lý.");
                        maTT = ketQua.getInt("maTT");
                        trangThaiThu = ketQua.getString("trangThai");
                    }
                }

                if (dongY && !"Chờ xử lý".equals(trangThaiThu)) {
                    throw new SQLException("Phiếu đã thu hoặc đã xử lý, không thể duyệt hủy khi chưa hoàn tiền.");
                }

                capNhatYeuCau(ketNoi, maYC, dongY);
                if (dongY) huyThanhToanVaHoaDon(ketNoi, maTT);
                luuPhieuDuyet(ketNoi, maYC, dongY);

                ketNoi.commit();
            } catch (SQLException | RuntimeException loi) {
                ketNoi.rollback();
                throw loi;
            }
        }
    }

    // Cap nhat trang thai yeu cau huy
    private void capNhatYeuCau(Connection ketNoi, int maYC, boolean dongY) throws SQLException {
        String sql = "UPDATE dbo.YeuCauHuyPhieuThu SET trangThai=? WHERE maYC=? AND trangThai=N'Chờ duyệt'";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setString(1, dongY ? "Đã duyệt" : "Đã từ chối");
            lenh.setInt(2, maYC);

            if (lenh.executeUpdate() != 1) throw new SQLException("Yêu cầu đã được người khác xử lý.");
        }
    }

    // Huy giao dich va hoa don chua thu tien
    private void huyThanhToanVaHoaDon(Connection ketNoi, int maTT) throws SQLException {
        String sqlThu = "UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=?";
        String sqlHoaDon = "UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maTT=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlThu)) {
            lenh.setInt(1, maTT);
            if (lenh.executeUpdate() != 1) throw new SQLException("Không hủy được thanh toán.");
        }

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlHoaDon)) {
            lenh.setInt(1, maTT);
            if (lenh.executeUpdate() != 1) throw new SQLException("Không hủy được hóa đơn.");
        }
    }

    // Luu thong tin duyet huy
    private void luuPhieuDuyet(Connection ketNoi, int maYC, boolean dongY) throws SQLException {
        String sql = """
                INSERT INTO dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet)
                VALUES(?,SYSDATETIME(),?)
                """;

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maYC);
            lenh.setString(2, dongY ? "Duyệt hủy phiếu chưa thực thu" : "Không duyệt hủy");
            if (lenh.executeUpdate() != 1) throw new SQLException("Không lưu được phiếu duyệt.");
        }
    }

    // Thong ke doanh thu theo ngay, thang, nam
    public List<Object[]> thongKeDoanhThu(LocalDate tuNgay, LocalDate denNgay, String theo) throws SQLException {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày thống kê không hợp lệ.");
        }

        String ky = switch (theo == null ? "" : theo) {
            case "Tháng" -> "CONVERT(VARCHAR(7),t.ngayThanhToan,23)";
            case "Năm" -> "CONVERT(VARCHAR(4),YEAR(t.ngayThanhToan))";
            default -> "CONVERT(VARCHAR(10),t.ngayThanhToan,23)";
        };

        String sql = """
                SELECT %s AS ky, COUNT(*) AS soGD, SUM(t.soTien) AS doanhThu
                FROM dbo.ThanhToan t
                JOIN dbo.HoaDon h ON h.maTT = t.maTT
                WHERE t.trangThai = N'Thành công' AND h.trangThai = N'Đã thanh toán'
                AND t.ngayThanhToan >= ? AND t.ngayThanhToan < DATEADD(DAY,1,?)
                GROUP BY %s
                ORDER BY ky
                """.formatted(ky, ky);

        List<Object[]> danhSachThongKe = new ArrayList<>();

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi, false);

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setDate(1, Date.valueOf(tuNgay));
                lenh.setDate(2, Date.valueOf(denNgay));

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        danhSachThongKe.add(new Object[]{
                            ketQua.getString("ky"), ketQua.getInt("soGD"),
                            ketQua.getBigDecimal("doanhThu")
                        });
                    }
                }
            }
        }
        return danhSachThongKe;
    }

    // Kiem tra cac bang can dung trong database
    private void kiemTraBang(Connection ketNoi, boolean canDuyet) throws SQLException {
        String[] cacBang = canDuyet
                ? new String[]{"HoaDon", "ThanhToan", "PhieuKham", "YeuCauHuyPhieuThu", "PhieuDuyetHuyPhieuThu"}
                : new String[]{"HoaDon", "ThanhToan", "PhieuKham"};

        String sql = "SELECT OBJECT_ID(?,N'U')";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            for (String tenBang : cacBang) {
                lenh.setString(1, "dbo." + tenBang);

                try (ResultSet ketQua = lenh.executeQuery()) {
                    if (!ketQua.next() || ketQua.getObject(1) == null) {
                        throw new SQLException("Chưa có bảng " + tenBang + " trong SQL Server. Cần đồng bộ CSDL với nhóm.");
                    }
                }
            }
        }
    }
}
