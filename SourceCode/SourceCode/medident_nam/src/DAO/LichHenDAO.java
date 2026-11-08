package DAO;



import ConnectDB.ConnectDB;
import Entity.LichHen;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LichHenDAO {

    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_HUY = "Đã hủy";

    // Kiem tra bang lich hen
    public void kiemTraBang() throws SQLException {
        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
        }
    }

    // Lay danh sach va tim kiem lich hen
    public List<LichHen> danhSach(String tuKhoa) throws SQLException {
        List<LichHen> danhSachLich = new ArrayList<>();

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
            String cotThoiLuong = coCotThoiLuong(ketNoi) ? "ISNULL(thoiLuongPhut,60) AS thoiLuongPhut" : "60 AS thoiLuongPhut";

            String sql = "SELECT maLichHen,maBN,maNV,ngayHen,gioHen," + cotThoiLuong + ",phongKham,trangThai "
                    + "FROM dbo.LichHen WHERE CAST(maLichHen AS NVARCHAR(20)) LIKE ? OR CAST(maBN AS NVARCHAR(20)) LIKE ? "
                    + "OR ISNULL(phongKham,N'') LIKE ? ORDER BY ngayHen DESC,gioHen DESC";

            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                String noiDungTim = "%" + (tuKhoa == null ? "" : tuKhoa.trim()) + "%";
                lenh.setString(1, noiDungTim);
                lenh.setString(2, noiDungTim);
                lenh.setString(3, noiDungTim);

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        danhSachLich.add(docLichHen(ketQua));
                    }
                }
            }
        }
        return danhSachLich;
    }

    // Chuyen du lieu SQL thanh doi tuong LichHen
    private LichHen docLichHen(ResultSet ketQua) throws SQLException {
        int maNhanVien = ketQua.getInt("maNV");
        Integer maBacSi = ketQua.wasNull() ? null : maNhanVien;

        return new LichHen(
            ketQua.getInt("maLichHen"), ketQua.getInt("maBN"), maBacSi,
            ketQua.getDate("ngayHen").toLocalDate(), ketQua.getTime("gioHen").toLocalTime(),
            ketQua.getInt("thoiLuongPhut"), ketQua.getString("phongKham"), ketQua.getString("trangThai")
        );
    }

    // Them hoac sua lich hen
    public void luu(LichHen lichHen, boolean laSua) throws SQLException {
        kiemTraDuLieuNhap(lichHen, laSua);

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
            ketNoi.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ketNoi.setAutoCommit(false);

            try {
                kiemTraNguoi(ketNoi, lichHen);
                kiemTraCaLamViec(ketNoi, lichHen);
                if (laSua) kiemTraTrangThai(ketNoi, lichHen.maLichHen());
                kiemTraTrungGio(ketNoi, lichHen);
                thucHienLuu(ketNoi, lichHen, laSua);
                ketNoi.commit();
            } catch (SQLException | RuntimeException loi) {
                ketNoi.rollback();
                throw loi;
            }
        }
    }

    // Kiem tra du lieu truoc khi luu
    private void kiemTraDuLieuNhap(LichHen lichHen, boolean laSua) {
        if (lichHen == null) {
            throw new IllegalArgumentException("Thông tin lịch hẹn không được để trống.");
        }
        if (lichHen.maBN() <= 0 || lichHen.maNV() == null || lichHen.maNV() <= 0) {
            throw new IllegalArgumentException("Mã bệnh nhân hoặc bác sĩ không hợp lệ.");
        }
        if (lichHen.ngayHen() == null || lichHen.gioHen() == null || lichHen.phongKham() == null || lichHen.phongKham().isBlank()) {
            throw new IllegalArgumentException("Ngày, giờ và phòng khám không được để trống.");
        }
        if (lichHen.thoiLuongPhut() < 15 || lichHen.thoiLuongPhut() > 240) {
            throw new IllegalArgumentException("Thời lượng phải từ 15 đến 240 phút.");
        }

        LocalDateTime gioBatDau = LocalDateTime.of(lichHen.ngayHen(), lichHen.gioHen());
        LocalDateTime gioKetThuc = gioBatDau.plusMinutes(lichHen.thoiLuongPhut());

        if (!gioBatDau.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lịch hẹn phải ở tương lai.");
        }
        if (!gioKetThuc.toLocalDate().equals(lichHen.ngayHen())) {
            throw new IllegalArgumentException("Lịch hẹn không được kéo dài sang ngày hôm sau.");
        }
        if (laSua && lichHen.maLichHen() <= 0) {
            throw new IllegalArgumentException("Mã lịch hẹn cần sửa không hợp lệ.");
        }
    }

    // Thuc hien INSERT hoac UPDATE
    private void thucHienLuu(Connection ketNoi, LichHen lichHen, boolean laSua) throws SQLException {
        boolean coThoiLuong = coCotThoiLuong(ketNoi);

        if (!coThoiLuong && lichHen.thoiLuongPhut() != 60) {
            throw new SQLException("SQL chưa có cột thoiLuongPhut. Hiện chỉ lưu được lịch 60 phút.");
        }

        String sql;
        if (laSua) {
            sql = "UPDATE dbo.LichHen SET maBN=?,maNV=?,ngayHen=?,gioHen=?,phongKham=?";
            if (coThoiLuong) sql += ",thoiLuongPhut=?";
            sql += " WHERE maLichHen=?";
        } else {
            sql = "INSERT INTO dbo.LichHen(maBN,maNV,ngayHen,gioHen,phongKham,trangThai";
            if (coThoiLuong) sql += ",thoiLuongPhut";
            sql += ") VALUES(?,?,?,?,?,N'Chờ xác nhận'";
            if (coThoiLuong) sql += ",?";
            sql += ")";
        }

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, lichHen.maBN());
            lenh.setInt(2, lichHen.maNV());
            lenh.setDate(3, Date.valueOf(lichHen.ngayHen()));
            lenh.setTime(4, Time.valueOf(lichHen.gioHen()));
            lenh.setString(5, lichHen.phongKham().trim());

            int viTri = 6;
            if (coThoiLuong) lenh.setInt(viTri++, lichHen.thoiLuongPhut());
            if (laSua) lenh.setInt(viTri, lichHen.maLichHen());

            if (lenh.executeUpdate() != 1) {
                throw new SQLException("Không lưu được lịch hẹn.");
            }
        }
    }

    // Huy lich hen, khong xoa khoi SQL
    public void huy(int maLichHen) throws SQLException {
        if (maLichHen <= 0) throw new IllegalArgumentException("Chưa chọn lịch hẹn cần hủy.");

        String sql = "UPDATE dbo.LichHen SET trangThai=N'Đã hủy' WHERE maLichHen=? "
                + "AND trangThai NOT IN(N'Đã hủy',N'Đã khám',N'Hoàn thành',N'Hoàn tất')";

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setInt(1, maLichHen);
                if (lenh.executeUpdate() != 1) {
                    throw new SQLException("Không thể hủy lịch đã khám hoặc đã hủy.");
                }
            }
        }
    }

    // Doi trang thai lich hen
    public void doiTrangThai(int maLichHen, String trangThaiMoi) throws SQLException {
        if (!DA_XAC_NHAN.equals(trangThaiMoi) && !"Đã khám".equals(trangThaiMoi)) {
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ.");
        }

        String sql = "UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=? AND trangThai=?";
        if ("Đã khám".equals(trangThaiMoi)) {
            sql += " AND DATEADD(MINUTE,DATEDIFF(MINUTE,CAST('00:00:00' AS TIME),CAST(gioHen AS TIME)),CAST(ngayHen AS DATETIME))<=GETDATE()";
        }

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setString(1, trangThaiMoi);
                lenh.setInt(2, maLichHen);
                lenh.setString(3, DA_XAC_NHAN.equals(trangThaiMoi) ? CHO_XAC_NHAN : DA_XAC_NHAN);

                if (lenh.executeUpdate() != 1) {
                    throw new SQLException("Lịch không ở trạng thái được phép chuyển.");
                }
            }
        }
    }

    // Thong ke lich da kham theo ngay, thang hoac nam
    public List<Object[]> thongKeLichDaKham(LocalDate tuNgay, LocalDate denNgay, String theo) throws SQLException {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày thống kê không hợp lệ.");
        }

        String ky = "CONVERT(VARCHAR(10),ngayHen,23)";
        if ("Tháng".equals(theo)) ky = "CONVERT(VARCHAR(7),ngayHen,23)";
        if ("Năm".equals(theo)) ky = "CONVERT(VARCHAR(4),YEAR(ngayHen))";

        String sql = "SELECT " + ky + " AS ky,COUNT(*) AS soLuot FROM dbo.LichHen "
                + "WHERE trangThai IN(N'Đã khám',N'Hoàn thành',N'Hoàn tất') "
                + "AND ngayHen>=? AND ngayHen<=? GROUP BY " + ky + " ORDER BY ky";

        List<Object[]> danhSachThongKe = new ArrayList<>();

        try (Connection ketNoi = ConnectDB.mo()) {
            kiemTraBang(ketNoi);
            try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
                lenh.setDate(1, Date.valueOf(tuNgay));
                lenh.setDate(2, Date.valueOf(denNgay));

                try (ResultSet ketQua = lenh.executeQuery()) {
                    while (ketQua.next()) {
                        danhSachThongKe.add(new Object[]{ketQua.getString("ky"), ketQua.getInt("soLuot")});
                    }
                }
            }
        }
        return danhSachThongKe;
    }

    // Kiem tra bang LichHen co trong database
    private void kiemTraBang(Connection ketNoi) throws SQLException {
        String sql = "SELECT OBJECT_ID(N'dbo.LichHen',N'U')";

        try (Statement lenh = ketNoi.createStatement(); ResultSet ketQua = lenh.executeQuery(sql)) {
            if (!ketQua.next() || ketQua.getObject(1) == null) {
                throw new SQLException("Chưa có bảng LichHen trong SQL Server.");
            }
        }
    }

    // Kiem tra cot thoiLuongPhut
    private boolean coCotThoiLuong(Connection ketNoi) throws SQLException {
        String sql = "SELECT COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut')";

        try (Statement lenh = ketNoi.createStatement(); ResultSet ketQua = lenh.executeQuery(sql)) {
            return ketQua.next() && ketQua.getObject(1) != null;
        }
    }

    // Kiem tra thong tin benh nhan va bac si
    private void kiemTraNguoi(Connection ketNoi, LichHen lichHen) throws SQLException {
        String sqlBenhNhan = "SELECT COUNT(*) FROM dbo.BenhNhan WHERE maBN=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlBenhNhan)) {
            lenh.setInt(1, lichHen.maBN());

            try (ResultSet ketQua = lenh.executeQuery()) {
                ketQua.next();
                if (ketQua.getInt(1) == 0) throw new SQLException("Bệnh nhân không tồn tại.");
            }
        }

        String sqlBacSi = "SELECT chucVu,trangThai FROM dbo.NhanVien WHERE maNV=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sqlBacSi)) {
            lenh.setInt(1, lichHen.maNV());

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) throw new SQLException("Bác sĩ không tồn tại.");

                String chucVu = ketQua.getString("chucVu");
                String trangThai = ketQua.getString("trangThai");

                if (chucVu == null || !chucVu.toLowerCase().contains("bác sĩ")) {
                    throw new SQLException("Nhân viên không phải bác sĩ.");
                }
                if (trangThai == null || !trangThai.equalsIgnoreCase("Đang làm việc")) {
                    throw new SQLException("Bác sĩ không còn làm việc.");
                }
            }
        }
    }

    // Kiem tra ca lam viec cua bac si
    private void kiemTraCaLamViec(Connection ketNoi, LichHen lichHen) throws SQLException {
        String sqlKiemTra = "SELECT OBJECT_ID(N'dbo.LichLamViec',N'U')";

        try (Statement lenh = ketNoi.createStatement(); ResultSet ketQua = lenh.executeQuery(sqlKiemTra)) {
            if (!ketQua.next() || ketQua.getObject(1) == null) return;
        }

        String sql = "SELECT COUNT(*) FROM dbo.LichLamViec WHERE maNV=? AND ngayLamViec=? "
                + "AND (trangThai IS NULL OR trangThai IN(N'Hoạt động',N'Đang làm việc')) "
                + "AND gioBatDau<=? AND gioKetThuc>=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, lichHen.maNV());
            lenh.setDate(2, Date.valueOf(lichHen.ngayHen()));
            lenh.setTime(3, Time.valueOf(lichHen.gioHen()));
            lenh.setTime(4, Time.valueOf(lichHen.gioHen().plusMinutes(lichHen.thoiLuongPhut())));

            try (ResultSet ketQua = lenh.executeQuery()) {
                ketQua.next();
                if (ketQua.getInt(1) == 0) {
                    throw new SQLException("Bác sĩ không có ca làm việc phù hợp.");
                }
            }
        }
    }

    // Kiem tra trang thai truoc khi sua
    private void kiemTraTrangThai(Connection ketNoi, int maLichHen) throws SQLException {
        String sql = "SELECT trangThai FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?";

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, maLichHen);

            try (ResultSet ketQua = lenh.executeQuery()) {
                if (!ketQua.next()) throw new SQLException("Không tìm thấy lịch hẹn.");

                String trangThai = ketQua.getString("trangThai");
                if (!CHO_XAC_NHAN.equals(trangThai) && !DA_XAC_NHAN.equals(trangThai)) {
                    throw new SQLException("Không thể sửa lịch đã kết thúc hoặc đã hủy.");
                }
            }
        }
    }

    // Kiem tra trung gio benh nhan, bac si va phong kham
    private void kiemTraTrungGio(Connection ketNoi, LichHen lichHen) throws SQLException {
        String doDai = coCotThoiLuong(ketNoi) ? "ISNULL(thoiLuongPhut,60)" : "60";
        String soPhut = "DATEDIFF(MINUTE,CAST('00:00:00' AS TIME),CAST(gioHen AS TIME))";

        String sql = "SELECT COUNT(*) FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE ngayHen=? AND maLichHen<>? "
                + "AND trangThai NOT IN(N'Đã hủy',N'Đã khám',N'Hoàn thành',N'Hoàn tất') "
                + "AND (maBN=? OR maNV=? OR phongKham=?) AND " + soPhut + " < ? AND " + soPhut + " + " + doDai + " > ?";

        int phutBatDau = lichHen.gioHen().getHour() * 60 + lichHen.gioHen().getMinute();

        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setDate(1, Date.valueOf(lichHen.ngayHen()));
            lenh.setInt(2, lichHen.maLichHen());
            lenh.setInt(3, lichHen.maBN());
            lenh.setInt(4, lichHen.maNV());
            lenh.setString(5, lichHen.phongKham().trim());
            lenh.setInt(6, phutBatDau + lichHen.thoiLuongPhut());
            lenh.setInt(7, phutBatDau);

            try (ResultSet ketQua = lenh.executeQuery()) {
                ketQua.next();
                if (ketQua.getInt(1) > 0) {
                    throw new SQLException("Trùng lịch bệnh nhân, bác sĩ hoặc phòng khám.");
                }
            }
        }
    }
}
