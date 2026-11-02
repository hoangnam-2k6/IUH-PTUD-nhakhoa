package DAO;

import Class.KetQuaTiepNhan;
import Class.TaiKhoan;
import Class.ThongTinBenhNhan;
import ConnectDB.KetNoi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Giao dịch SQL tiếp nhận bệnh nhân (UC037 - UC039).
 * Cần chạy SQLData/06_TiepNhanLeTan_SQL2012.sql MỘT LẦN trước khi dùng.
 */
public final class TiepNhanDAO {

    /** Mỗi phòng nhận tối đa bấy nhiêu bệnh nhân trong một ngày. */
    public static final int MAX_BENH_NHAN_MOI_PHONG = 20;

    private final TaiKhoan user;

    public TiepNhanDAO(TaiKhoan user) {
        this.user = user;
    }

    /**
     * Một giao dịch SQL Server 2012, làm lần lượt:
     * 1. Kiểm tra lại vai trò và quyền được Admin cấp.
     * 2. Khóa cấp số theo ngày qua sp_getapplock để không trùng khi nhiều lễ tân thao tác.
     * 3. Tìm hồ sơ bệnh nhân theo SĐT + họ tên; chưa có thì tạo hồ sơ mới từ form.
     * 4. Tìm lịch hẹn hôm nay của bệnh nhân (chưa có thì lát nữa tạo lịch hẹn trực tiếp).
     * 5. Kiểm tra phòng chưa đủ người, cấp số thứ tự (MAX + 1).
     * 6. Cập nhật hoặc tạo lịch hẹn, ghi nhật ký rồi COMMIT. Có lỗi thì ROLLBACK
     *    (hồ sơ mới vừa tạo cũng được hoàn tác).
     */
    public KetQuaTiepNhan tiepNhan(ThongTinBenhNhan info, String room) throws SQLException {
        try (Connection connection = KetNoi.mo()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                requirePermission(connection, "LICHHEN");
                requirePermission(connection, "BENHNHAN");
                if (!daCaiCotSoThuTu(connection)) {
                    throw new SQLException("Thiếu cột soThuTuKham. Hãy chạy SQLData/06_TiepNhanLeTan_SQL2012.sql.");
                }

                Date today = layNgayHienTai(connection);
                khoaBoCapSo(connection, today);
                int[] patient = timHoacTaoBenhNhan(connection, info);
                int patientId = patient[0];
                boolean created = patient[1] == 1;
                int appointmentId = timLichHenHomNay(connection, patientId, today);
                kiemTraGioiHanPhong(connection, today, room);
                int queue = capSoMoi(connection, today);

                if (appointmentId > 0) {
                    capNhatLichHen(connection, appointmentId, queue, room);
                } else {
                    taoLichHenTrucTiep(connection, patientId, queue, room);
                }
                ghiNhatKy(connection, patientId, queue, room, created);
                connection.commit();
                return new KetQuaTiepNhan(patientId, queue, created);
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException("Tiếp nhận không thành công.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Đã chạy script 06 (có cột soThuTuKham trong bảng LichHen) chưa? */
    public static boolean daCaiCotSoThuTu(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SELECT COL_LENGTH('dbo.LichHen','soThuTuKham')")) {
            return result.next() && result.getObject(1) != null;
        }
    }

    /**
     * Dùng lại hồ sơ nếu đã có bệnh nhân cùng SĐT và họ tên; ngược lại thêm hồ sơ mới.
     * (Cùng SĐT nhưng khác họ tên vẫn là người khác, ví dụ người thân dùng chung số.)
     *
     * @return {mã bệnh nhân, 1 nếu vừa tạo mới / 0 nếu dùng lại hồ sơ cũ}
     */
    private int[] timHoacTaoBenhNhan(Connection connection, ThongTinBenhNhan info) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT TOP 1 maBN FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE sdt=? AND hoTen=? ORDER BY maBN")) {
            statement.setString(1, info.sdt);
            statement.setString(2, info.hoTen);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return new int[] {result.getInt(1), 0};
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.BenhNhan (hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,tienSuBenh,trangThai) "
                + "VALUES (?,?,?,?,?,?,?,N'Hoạt động')", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, info.hoTen);
            statement.setDate(2, Date.valueOf(info.ngaySinh));
            statement.setString(3, info.gioiTinh);
            statement.setString(4, info.sdt);
            datChuoiTuyChon(statement, 5, info.email);
            datChuoiTuyChon(statement, 6, info.diaChi);
            datChuoiTuyChon(statement, 7, info.tienSu);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return new int[] {keys.getInt(1), 1};
            }
        }
        throw new SQLException("Không lấy được mã bệnh nhân vừa tạo.");
    }

    /** Ô tùy chọn để trống thì lưu NULL thay vì chuỗi rỗng. */
    private static void datChuoiTuyChon(PreparedStatement statement, int index, String value)
            throws SQLException {
        if (value == null || value.isEmpty()) statement.setNull(index, Types.NVARCHAR);
        else statement.setString(index, value);
    }

    /** Kiểm tra ngay trong transaction để chặn ghi khi Admin vừa thu hồi quyền. */
    private void requirePermission(Connection connection, String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro=N'Lễ tân' AND q.maQuyen=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id);
            statement.setString(2, code);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Bạn không có quyền tiếp nhận bệnh nhân.");
            }
        }
    }

    /** Ngày hiện tại theo đồng hồ của SQL Server. */
    private Date layNgayHienTai(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT CAST(GETDATE() AS date)")) {
            result.next();
            return result.getDate(1);
        }
    }

    /** Khóa bộ cấp số của ngày hôm nay (tối đa chờ 10 giây). */
    private void khoaBoCapSo(Connection connection, Date today) throws SQLException {
        String lockName = "MEDIDENT_TIEPNHAN_" + today.toString();
        try (PreparedStatement statement = connection.prepareStatement(
                "DECLARE @result int; "
                + "EXEC @result = sp_getapplock @Resource=?, @LockMode='Exclusive', "
                + "@LockOwner='Transaction', @LockTimeout=10000; "
                + "SELECT @result")) {
            statement.setString(1, lockName);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) < 0) {
                    throw new SQLException("Không khóa được bộ cấp số. Hãy thử lại.");
                }
            }
        }
    }

    /**
     * Tìm lịch hẹn hôm nay của bệnh nhân.
     * @return mã lịch hẹn, hoặc -1 nếu bệnh nhân chưa có lịch hẹn
     * @throws SQLException nếu bệnh nhân đã được cấp số hôm nay
     */
    private int timLichHenHomNay(Connection connection, int patientId, Date today) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT TOP 1 maLichHen,soThuTuKham FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE maBN=? AND ngayHen=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất') "
                + "ORDER BY CASE WHEN soThuTuKham IS NULL THEN 1 ELSE 0 END, maLichHen")) {
            statement.setInt(1, patientId);
            statement.setDate(2, today);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return -1;
                int appointmentId = result.getInt(1);
                int oldQueue = result.getInt(2);
                if (!result.wasNull()) {
                    throw new SQLException("Bệnh nhân đã được tiếp nhận hôm nay, số khám: "
                            + String.format("%03d", oldQueue));
                }
                return appointmentId;
            }
        }
    }

    /** Mỗi phòng nhận tối đa MAX_BENH_NHAN_MOI_PHONG bệnh nhân trong ngày. */
    private void kiemTraGioiHanPhong(Connection connection, Date today, String room) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ngayHen=? AND phongKham=? AND soThuTuKham IS NOT NULL "
                + "AND trangThai NOT IN (N'Đã hủy')")) {
            statement.setDate(1, today);
            statement.setString(2, room);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) >= MAX_BENH_NHAN_MOI_PHONG) {
                    throw new SQLException("Phòng " + room + " đã đủ " + MAX_BENH_NHAN_MOI_PHONG
                            + " bệnh nhân trong hôm nay. Vui lòng chọn phòng khác.");
                }
            }
        }
    }

    /** Số thứ tự mới = số lớn nhất của hôm nay + 1. */
    private int capSoMoi(Connection connection, Date today) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ISNULL(MAX(soThuTuKham),0)+1 FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ngayHen=?")) {
            statement.setDate(1, today);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /** Bệnh nhân đã có lịch hẹn: ghi số khám, phòng và đổi trạng thái. */
    private void capNhatLichHen(Connection connection, int appointmentId, int queue, String room)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE dbo.LichHen SET soThuTuKham=?,phongKham=?, "
                + "trangThai=N'Đã xác nhận' WHERE maLichHen=? AND soThuTuKham IS NULL")) {
            statement.setInt(1, queue);
            statement.setString(2, room);
            statement.setInt(3, appointmentId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Lịch hẹn vừa được thay đổi, hãy tải lại.");
            }
        }
    }

    /** Bệnh nhân chưa có lịch hẹn: tạo lịch hẹn "Tiếp nhận trực tiếp". */
    private void taoLichHenTrucTiep(Connection connection, int patientId, int queue, String room)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.LichHen (maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai,soThuTuKham) "
                + "VALUES (?,NULL,?,CAST(GETDATE() AS date),CAST(GETDATE() AS time), "
                + "N'Tiếp nhận trực tiếp',N'Đã xác nhận',?)")) {
            statement.setInt(1, patientId);
            statement.setString(2, room);
            statement.setInt(3, queue);
            statement.executeUpdate();
        }
    }

    private void ghiNhatKy(Connection connection, int patientId, int queue, String room,
            boolean created) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            statement.setString(1, "Tiếp nhận bệnh nhân");
            statement.setString(2, "LichHen");
            statement.setString(3, "maTK=" + user.id + " | maBN=" + patientId
                    + (created ? " (hồ sơ mới)" : "") + " | STT=" + queue + " | " + room);
            statement.executeUpdate();
        }
    }
}
