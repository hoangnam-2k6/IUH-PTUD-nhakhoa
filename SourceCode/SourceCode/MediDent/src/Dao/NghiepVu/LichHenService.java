package Dao.NghiepVu;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Dao.TruyVanDAO;

public final class LichHenService {

    private final PhanQuyen.PhienDangNhap actor;

    public LichHenService(PhanQuyen.PhienDangNhap actor) {
        this.actor = actor;
    }

    public List<Map<String, Object>> list() {
        return TruyVanDAO.list(
                "SELECT h.maLichHen AS ID,"
                        + "CONVERT(varchar(10),h.ngayHen,23) AS [Ngày hẹn],"
                        + "CONVERT(varchar(5),h.gioHen,108) AS [Giờ],"
                        + "h.thoiLuongPhut AS [Phút],"
                        + "b.hoTen AS [Bệnh nhân],"
                        + "n.hoTen AS [Bác sĩ],"
                        + "h.phongKham AS [Phòng],"
                        + "h.trangThai AS [Trạng thái],"
                        + "ISNULL(h.lyDo,N'') AS [Lý do],"
                        + "h.maBN AS [Mã bệnh nhân],"
                        + "h.maNV AS [Mã bác sĩ] "
                        + "FROM dbo.LichHen h "
                        + "JOIN dbo.BenhNhan b ON b.maBN=h.maBN "
                        + "LEFT JOIN dbo.NhanVien n ON n.maNV=h.maNV "
                        + "ORDER BY h.ngayHen DESC,h.gioHen DESC,h.maLichHen DESC");
    }

    public List<Map<String, Object>> patients() {
        return TruyVanDAO.list(
                "SELECT maBN AS ID, hoTen AS [Tên] "
                        + "FROM dbo.BenhNhan "
                        + "WHERE ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động' "
                        + "ORDER BY hoTen");
    }

    public List<Map<String, Object>> doctors() {
        return TruyVanDAO.list(
                "SELECT maNV AS ID, hoTen AS [Tên] "
                        + "FROM dbo.NhanVien "
                        + "WHERE chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc' "
                        + "ORDER BY hoTen");
    }

    // Kiểm tra nghiệp vụ ở Service; GUI chỉ nhận dữ liệu từ các ô nhập.
    public void save(Integer id, Map<String, String> values) {
        int patientId = QuyTacNghiepVu.id(values.get("Mã bệnh nhân"), "Mã bệnh nhân");
        int doctorId = QuyTacNghiepVu.id(values.get("Mã bác sĩ"), "Mã bác sĩ");
        LocalDate date = QuyTacNghiepVu.date(values.get("Ngày hẹn"), "Ngày hẹn");
        LocalTime time = QuyTacNghiepVu.time(values.get("Giờ"), "Giờ");
        int minutes = QuyTacNghiepVu.durationMinutes(values.get("Phút"), "Thời lượng");
        String room = QuyTacNghiepVu.text(values.get("Phòng"), "Phòng", 80);
        String reason = QuyTacNghiepVu.optionalText(values.get("Lý do"), 500, "Lý do");

        LocalDateTime startTime = LocalDateTime.of(date, time);
        QuyTacNghiepVu.need(startTime.isAfter(LocalDateTime.now()), "Giờ bắt đầu phải ở tương lai");
        QuyTacNghiepVu.need(time.toSecondOfDay() / 60 + minutes <= 1439, "Lịch không được kéo dài sang ngày mới");

        TruyVanDAO.tx(connection -> {
            PhanQuyen.requireManager(connection, actor);
            PhanQuyen.lock("LICH_" + date, connection);

            if (TruyVanDAO.one(connection,
                    "SELECT maBN FROM dbo.BenhNhan WHERE maBN=? AND ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động'",
                    patientId) == null) {
                throw new SQLException("Bệnh nhân không tồn tại hoặc đã ngừng hồ sơ");
            }

            if (TruyVanDAO.one(connection,
                    "SELECT maNV FROM dbo.NhanVien WHERE maNV=? AND chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc'",
                    doctorId) == null) {
                throw new SQLException("Bác sĩ không còn hoạt động");
            }

            if (TruyVanDAO.one(connection,
                    "SELECT maPhong FROM dbo.PhongKham WHERE tenPhong=? AND trangThai=N'Hoạt động'",
                    room) == null) {
                throw new SQLException("Phòng khám chưa hoạt động trong bảng PhongKham");
            }

            if (id != null) {
                Map<String, Object> old = TruyVanDAO.one(connection,
                        "SELECT trangThai FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?",
                        id);
                if (old == null) {
                    throw new SQLException("Không tìm thấy lịch hẹn");
                }

                String oldStatus = TruyVanDAO.s(old.get("trangThai"));
                if (!Set.of("Chờ xác nhận", "Đã xác nhận").contains(oldStatus)) {
                    throw new SQLException("Chỉ được dời lịch Chờ xác nhận / Đã xác nhận");
                }
            }

            if (TruyVanDAO.one(connection,
                    "SELECT TOP 1 maLichLV FROM dbo.LichLamViec "
                            + "WHERE maNV=? AND ngayLamViec=? "
                            + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioBatDau)<=? "
                            + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioKetThuc)>=? "
                            + "AND ISNULL(trangThai,N'') NOT IN(N'Ngừng xếp lịch',N'Đã hủy')",
                    doctorId,
                    date,
                    time.toSecondOfDay() / 60,
                    time.toSecondOfDay() / 60 + minutes) == null) {
                throw new SQLException("Bác sĩ chưa có ca làm việc bao trọn lịch. Hãy xếp ca trước.");
            }

            String overlapSql = "SELECT TOP 1 maLichHen "
                    + "FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) "
                    + "WHERE ngayHen=? AND maLichHen<>? "
                    + "AND trangThai NOT IN(N'Đã hủy',N'Hoàn tất',N'Đã khám') "
                    + "AND (maBN=? OR maNV=? OR phongKham=?) "
                    + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioHen) < ? "
                    + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioHen)+ISNULL(thoiLuongPhut,60)>?";

            if (TruyVanDAO.one(connection,
                    overlapSql,
                    date,
                    id == null ? 0 : id,
                    patientId,
                    doctorId,
                    room,
                    time.toSecondOfDay() / 60 + minutes,
                    time.toSecondOfDay() / 60) != null) {
                throw new SQLException("Trùng khoảng thời gian giữa bệnh nhân, bác sĩ hoặc phòng khám");
            }

            if (id == null) {
                TruyVanDAO.update(connection,
                        "INSERT dbo.LichHen(maBN,maNV,phongKham,ngayHen,gioHen,thoiLuongPhut,lyDo,trangThai) "
                                + "VALUES(?,?,?,?,?,?,?,N'Chờ xác nhận')",
                        patientId,
                        doctorId,
                        room,
                        date,
                        time,
                        minutes,
                        reason);
            } else {
                TruyVanDAO.exactlyOne(
                        TruyVanDAO.update(connection,
                                "UPDATE dbo.LichHen "
                                        + "SET maBN=?,maNV=?,phongKham=?,ngayHen=?,gioHen=?,thoiLuongPhut=?,lyDo=? "
                                        + "WHERE maLichHen=?",
                                patientId,
                                doctorId,
                                room,
                                date,
                                time,
                                minutes,
                                reason,
                                id),
                        "Không sửa được lịch hẹn");
            }

            TruyVanDAO.audit(
                    connection,
                    actor.accountId(),
                    id == null ? "Tạo lịch" : "Dời lịch",
                    "LichHen",
                    "maBN=" + patientId + ", maNV=" + doctorId + ", ngay=" + date + ", gio=" + time);
            return null;
        });
    }

    public void addShift(int doctorId, LocalDate day, LocalTime from, LocalTime to, String shiftName) {
        QuyTacNghiepVu.need(day != null && !day.isBefore(LocalDate.now()), "Ngày ca không được ở quá khứ");
        QuyTacNghiepVu.need(from != null && to != null && from.isBefore(to), "Giờ ca không hợp lệ");
        QuyTacNghiepVu.need(!to.isAfter(LocalTime.of(23, 59)), "Ca làm việc không được qua ngày mới");
        String label = QuyTacNghiepVu.text(shiftName, "Tên ca", 50);

        TruyVanDAO.tx(connection -> {
            PhanQuyen.requireManager(connection, actor);
            PhanQuyen.lock("CA_" + day + "_" + doctorId, connection);

            if (TruyVanDAO.one(connection,
                    "SELECT maNV FROM dbo.NhanVien WHERE maNV=? AND chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc'",
                    doctorId) == null) {
                throw new SQLException("Chỉ được xếp ca cho bác sĩ đang làm việc");
            }

            if (TruyVanDAO.one(connection,
                    "SELECT TOP 1 maLichLV FROM dbo.LichLamViec WITH(UPDLOCK,HOLDLOCK) "
                            + "WHERE maNV=? AND ngayLamViec=? "
                            + "AND ISNULL(trangThai,N'') NOT IN(N'Đã hủy',N'Ngừng xếp lịch') "
                            + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioBatDau)<? "
                            + "AND DATEDIFF(MINUTE,CAST('00:00' AS time),gioKetThuc)>?",
                    doctorId,
                    day,
                    to.toSecondOfDay() / 60,
                    from.toSecondOfDay() / 60) != null) {
                throw new SQLException("Ca làm việc bị trùng giờ");
            }

            TruyVanDAO.update(connection,
                    "INSERT dbo.LichLamViec(maNV,thu,ngayLamViec,caLamViec,gioBatDau,gioKetThuc,trangThai) "
                            + "VALUES(?,N'',?,?,?,?,N'Đang xếp lịch')",
                    doctorId,
                    day,
                    label,
                    from,
                    to);

            TruyVanDAO.audit(connection, actor.accountId(), "Xếp ca bác sĩ", "LichLamViec", "maNV=" + doctorId + " ngày=" + day);
            return null;
        });
    }

    public void change(int id, String status) {
        QuyTacNghiepVu.oneOf(status, "Trạng thái", Set.of("Đã xác nhận", "Đã đến", "Chờ khám", "Đang khám", "Hoàn tất", "Đã hủy"));

        TruyVanDAO.tx(connection -> {
            PhanQuyen.requireManager(connection, actor);

            Map<String, Object> old = TruyVanDAO.one(connection,
                    "SELECT trangThai,maNV FROM dbo.LichHen WITH(UPDLOCK,HOLDLOCK) WHERE maLichHen=?",
                    id);
            if (old == null) {
                throw new SQLException("Lịch không tồn tại");
            }

            String before = TruyVanDAO.s(old.get("trangThai"));
            QuyTacNghiepVu.need(QuyTacNghiepVu.move(before, status), "Không thể chuyển từ '" + before + "' sang '" + status + "'");
            if ("Đang khám".equals(status) && old.get("maNV") == null) {
                throw new SQLException("Lịch chưa phân công bác sĩ");
            }

            TruyVanDAO.exactlyOne(
                    TruyVanDAO.update(connection,
                            "UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=? AND trangThai=?",
                            status,
                            id,
                            before),
                    "Lịch vừa thay đổi ở máy khác");

            TruyVanDAO.audit(connection, actor.accountId(), "Đổi trạng thái lịch", "LichHen", id + ": " + before + " → " + status);
            return null;
        });
    }
}
