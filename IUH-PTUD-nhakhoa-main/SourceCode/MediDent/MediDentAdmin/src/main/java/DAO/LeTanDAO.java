package DAO;

import Class.DuLieuTrangLeTan;
import Class.TaiKhoan;
import ConnectDB.KetNoi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Đọc dữ liệu cho trang chủ Lễ tân: quyền, danh sách phiếu khám và các số thống kê. */
public final class LeTanDAO {
    private final KhoDuLieu db;
    private final TaiKhoan user;

    public LeTanDAO(KhoDuLieu db, TaiKhoan user) {
        this.db = db;
        this.user = user;
    }

    /** Kiểm tra quyền rồi đọc phiếu khám, bệnh nhân và các số thống kê. */
    public DuLieuTrangLeTan taiDuLieu() throws SQLException {
        DuLieuTrangLeTan data = new DuLieuTrangLeTan();
        data.viewAllowed = db.coQuyenTaiKhoan(user.id, "BENHNHAN");
        data.receiveAllowed = data.viewAllowed && db.coQuyenTaiKhoan(user.id, "LICHHEN")
                && "Lễ tân".equalsIgnoreCase(user.tenVaiTro);
        if (!data.viewAllowed) return data;

        try (Connection connection = KetNoi.mo()) {
            boolean queueInstalled = TiepNhanDAO.daCaiCotSoThuTu(connection);
            loadPhieuKham(connection, data);
            loadThongKe(connection, data, queueInstalled);
        }
        return data;
    }

    /** Lấy toàn bộ thuộc tính của PhieuKham và cả hai mã khóa ngoại (LEFT JOIN để phiếu luôn hiện). */
    private void loadPhieuKham(Connection connection, DuLieuTrangLeTan data) throws SQLException {
        String sql =
                "SELECT p.maPK, p.maBN, p.maNV, "
                + "ISNULL(b.hoTen,N'') AS benhNhan, "
                + "ISNULL(n.hoTen,N'') AS bacSi, "
                + "CONVERT(varchar(16), p.ngayKham, 120) AS ngayKham, "
                + "ISNULL(p.trangThai,N'Chưa cập nhật') AS trangThai, "
                + "ISNULL(p.trieuChung,N'') AS trieuChung, "
                + "ISNULL(p.chanDoan,N'') AS chanDoan, "
                + "ISNULL(p.phuongPhapDieuTri,N'') AS phuongPhapDieuTri, "
                + "ISNULL(p.ketQua,N'') AS ketQua, "
                + "ISNULL(p.ghiChu,N'') AS ghiChu "
                + "FROM dbo.PhieuKham p "
                + "LEFT JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                + "LEFT JOIN dbo.NhanVien n ON p.maNV=n.maNV "
                + "ORDER BY p.ngayKham DESC, p.maPK DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                data.examRows.add(new Object[]{
                    0, // STT, được đánh số lại khi đổ lên bảng
                    result.getInt("maPK"),
                    result.getInt("maBN"),
                    result.getString("benhNhan"),
                    result.getInt("maNV"),
                    result.getString("bacSi"),
                    result.getString("ngayKham"),
                    result.getString("trangThai"),
                    result.getString("trieuChung"),
                    result.getString("chanDoan"),
                    result.getString("phuongPhapDieuTri"),
                    result.getString("ketQua"),
                    result.getString("ghiChu")
                });
            }
        }
    }

    /** Bốn số trên các thẻ thống kê. Hai số "chưa nhận / đã tiếp nhận" cần cột soThuTuKham. */
    private void loadThongKe(Connection connection, DuLieuTrangLeTan data, boolean queueInstalled)
            throws SQLException {
        data.totalPatients = scalar(connection, "SELECT COUNT(*) FROM dbo.BenhNhan");
        data.examsToday = scalar(connection,
                "SELECT COUNT(*) FROM dbo.PhieuKham "
                + "WHERE ngayKham>=CAST(GETDATE() AS date) "
                + "AND ngayKham<DATEADD(day,1,CAST(GETDATE() AS date))");
        if (queueInstalled) {
            data.receivedToday = scalar(connection,
                    "SELECT COUNT(*) FROM dbo.LichHen "
                    + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NOT NULL");
            data.waitingToday = scalar(connection,
                    "SELECT COUNT(*) FROM dbo.LichHen "
                    + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NULL "
                    + "AND trangThai<>N'Đã hủy'");
        }
    }

    /** Chạy câu SQL trả về một số nguyên (ví dụ COUNT). */
    private int scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }
}
