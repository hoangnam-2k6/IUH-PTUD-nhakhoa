package DAO;

import ConnectDB.KetNoi;
import TienIch.EmailHopLe;
import TienIch.SoDienThoaiHopLe;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nghiệp vụ vận hành phòng khám, dùng chung cho Admin / Quản lý / Lễ tân.
 * Đọc và ghi trực tiếp SQL Server 2012 bằng PreparedStatement.
 * Mọi thao tác ghi đều kiểm tra quyền tài khoản từ CSDL, không tin vào nút UI.
 */
public final class QuanLyDAO {

    /** Thời lượng mặc định cho form cũ chưa có trường chọn thời lượng. Có thể đổi qua overload. */
    public static final int THOI_LUONG_LICH_MAC_DINH_PHUT = 60;
    private static final int THOI_LUONG_LICH_TOI_THIEU_PHUT = 15;
    private static final int THOI_LUONG_LICH_TOI_DA_PHUT = 240;

    /** ID SQL được giữ tách biệt khỏi STT hiển thị trên JTable. */
    public static final class Hang {
        public final int id;
        public final String[] cot;
        public Hang(int id, String... cot) { this.id = id; this.cot = cot; }
    }

    /** ComboBox giữ ID để tránh người dùng phải nhập khóa ngoại. */
    public static final class LuaChon {
        public final int id;
        public final String ten;
        public LuaChon(int id, String ten) { this.id = id; this.ten = ten; }
        @Override public String toString() { return ten; }
    }

    /** Dữ liệu đầy đủ của một lịch hẹn để mở form dời lịch mà không dựa vào text JTable. */
    public static final class ThongTinLichHen {
        public final int maBN;
        public final Integer maNV;
        public final LocalDate ngay;
        public final LocalTime gio;
        public final int thoiLuongPhut;
        public final String phong, lyDo, trangThai;
        public ThongTinLichHen(int maBN, Integer maNV, LocalDate ngay, LocalTime gio,
                int thoiLuongPhut, String phong, String lyDo, String trangThai) {
            this.maBN = maBN; this.maNV = maNV; this.ngay = ngay; this.gio = gio;
            this.thoiLuongPhut = thoiLuongPhut; this.phong = phong;
            this.lyDo = lyDo; this.trangThai = trangThai;
        }
    }

    /** Dữ liệu đầy đủ của một ca làm việc để mở form sửa. */
    public static final class ThongTinLichLamViec {
        public final int maNV;
        public final LocalDate ngay;
        public final String ca, trangThai;
        public final LocalTime gioBatDau, gioKetThuc;
        public ThongTinLichLamViec(int maNV, LocalDate ngay, String ca,
                LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai) {
            this.maNV = maNV; this.ngay = ngay; this.ca = ca;
            this.gioBatDau = gioBatDau; this.gioKetThuc = gioKetThuc;
            this.trangThai = trangThai;
        }
    }

    private interface DocHang { Hang doc(ResultSet rs) throws SQLException; }

    /** Gom các lệnh SELECT về một chỗ và luôn đóng Connection/ResultSet. */
    private List<Hang> truyVan(String sql, DocHang doc, Object... args) throws SQLException {
        List<Hang> ketQua = new ArrayList<>();
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql)) {
            datThamSo(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ketQua.add(doc.doc(rs));
            }
        }
        return ketQua;
    }

    private static void datThamSo(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof LocalDate) ps.setDate(i + 1, Date.valueOf((LocalDate) arg));
            else if (arg instanceof LocalTime) ps.setTime(i + 1, Time.valueOf((LocalTime) arg));
            else ps.setObject(i + 1, arg);
        }
    }

    private static String chuoi(ResultSet rs, String key) throws SQLException {
        String value = rs.getString(key);
        return value == null ? "" : value;
    }

    private static void batBuoc(String value, String message) throws SQLException {
        if (value == null || value.trim().isEmpty()) throw new SQLException(message);
    }

    private static int kiemTraThoiLuong(int phut) throws SQLException {
        if (phut < THOI_LUONG_LICH_TOI_THIEU_PHUT
                || phut > THOI_LUONG_LICH_TOI_DA_PHUT) {
            throw new SQLException("Thời lượng lịch hẹn phải từ 15 đến 240 phút.");
        }
        return phut;
    }

    private static LocalDateTime ketThucLich(LocalDate ngay, LocalTime gio, int phut)
            throws SQLException {
        LocalDateTime batDau = LocalDateTime.of(ngay, gio);
        LocalDateTime ketThuc = batDau.plusMinutes(kiemTraThoiLuong(phut));
        if (!ketThuc.toLocalDate().equals(ngay)) {
            throw new SQLException("Lịch hẹn không được kéo dài sang ngày hôm sau.");
        }
        return ketThuc;
    }

    /** Lấy khóa ứng dụng theo ngày/nguồn lực để tuần tự hóa các lượt đặt cùng bác sĩ, BN hoặc phòng. */
    private static void khoaNguonLucLich(Connection c, LocalDate ngay, Integer maNV,
            int maBN, String phong) throws SQLException {
        List<String> resources = new ArrayList<>();
        String dateKey = ngay.toString();
        if (maNV != null) resources.add("MEDIDENT_LICH_NV_" + dateKey + "_" + maNV);
        resources.add("MEDIDENT_LICH_BN_" + dateKey + "_" + maBN);
        if (phong != null && !phong.trim().isEmpty()) {
            resources.add("MEDIDENT_LICH_PHONG_" + dateKey + "_" + phong.trim().toUpperCase());
        }
        Collections.sort(resources);
        for (String resource : resources) {
            try (PreparedStatement ps = c.prepareStatement(
                    "DECLARE @r INT; EXEC @r=sys.sp_getapplock "
                  + "@Resource=?,@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000; "
                  + "SELECT @r")) {
                ps.setString(1, resource);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) < 0) {
                        throw new SQLException("Không khóa được lịch hẹn đồng thời. Hãy tải lại và thử lại.");
                    }
                }
            }
        }
    }

    private static void kiemTraBenhNhanHoatDong(Connection c, int maBN) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT ISNULL(trangThaiHoSo,N'Hoạt động') FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) WHERE maBN=?")) {
            ps.setInt(1, maBN);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy bệnh nhân #" + maBN + ".");
                if ("Ngừng hồ sơ".equalsIgnoreCase(rs.getString(1))) {
                    throw new SQLException("Hồ sơ bệnh nhân đã ngừng, không thể tạo hoặc dời lịch.");
                }
            }
        }
    }

    /** Bác sĩ phải đang làm việc và có ca bao trọn toàn bộ khoảng hẹn. */
    private static void kiemTraBacSiTrongCa(Connection c, int maNV, LocalDate ngay,
            LocalTime gio, int thoiLuongPhut) throws SQLException {
        LocalDateTime end = ketThucLich(ngay, gio, thoiLuongPhut);
        String sql = "SELECT TOP 1 n.maNV FROM dbo.NhanVien n WITH (UPDLOCK,HOLDLOCK) "
                + "JOIN dbo.LichLamViec l WITH (UPDLOCK,HOLDLOCK) ON l.maNV=n.maNV "
                + "WHERE n.maNV=? AND n.chucVu=N'Bác sĩ' AND n.trangThai=N'Đang làm việc' "
                + "AND l.ngayLamViec=? AND l.gioBatDau<=? AND l.gioKetThuc>=? "
                + "AND l.trangThai=N'Đã xếp lịch'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            ps.setDate(2, Date.valueOf(ngay));
            ps.setTime(3, Time.valueOf(gio));
            ps.setTime(4, Time.valueOf(end.toLocalTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Bác sĩ không có ca làm việc phù hợp hoặc đã ngừng làm việc.");
                }
            }
        }
    }

    /** Phát hiện giao nhau theo khoảng thời gian, không chỉ so giờ bắt đầu. */
    private static void kiemTraKhongChongLich(Connection c, int maLichHen, int maBN,
            Integer maNV, String phong, LocalDate ngay, LocalTime gio, int thoiLuongPhut)
            throws SQLException {
        ketThucLich(ngay, gio, thoiLuongPhut);
        String sql = "DECLARE @start DATETIME = DATEADD(SECOND, "
                + "DATEDIFF(SECOND,CAST('00:00:00' AS time),CAST(? AS time)),CAST(? AS datetime)); "
                + "DECLARE @finish DATETIME = DATEADD(MINUTE,?,@start); "
                + "SELECT TOP 1 l.maLichHen, l.maNV, l.maBN, ISNULL(l.phongKham,N'') AS phongKham "
                + "FROM dbo.LichHen l WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ISNULL(l.trangThai,N'') NOT IN (N'Đã hủy',N'Hoàn tất') "
                + "AND (?=0 OR l.maLichHen<>?) "
                + "AND @start < DATEADD(MINUTE,ISNULL(l.thoiLuongPhut,60),"
                + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime))) "
                + "AND @finish > DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime)) "
                + "AND (l.maBN=? OR (? IS NOT NULL AND l.maNV=?) "
                + "OR (NULLIF(LTRIM(RTRIM(?)),N'') IS NOT NULL "
                + "AND UPPER(LTRIM(RTRIM(ISNULL(l.phongKham,N''))))=UPPER(LTRIM(RTRIM(?)))))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(gio));
            ps.setDate(2, Date.valueOf(ngay));
            ps.setInt(3, thoiLuongPhut);
            ps.setInt(4, maLichHen);
            ps.setInt(5, maLichHen);
            ps.setInt(6, maBN);
            if (maNV == null) {
                ps.setNull(7, Types.INTEGER);
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(7, maNV);
                ps.setInt(8, maNV);
            }
            ps.setString(9, phong == null ? "" : phong.trim());
            ps.setString(10, phong == null ? "" : phong.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Lịch bị chồng với lịch #" + rs.getInt("maLichHen")
                            + " của cùng bệnh nhân, bác sĩ hoặc phòng. Hãy chọn khung giờ khác.");
                }
            }
        }
    }

    private static String thuTrongTuan(LocalDate ngay) {
        DayOfWeek day = ngay.getDayOfWeek();
        switch (day) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            default: return "Chủ nhật";
        }
    }

    private static void kiemTraNhanVienKhongConLichTuongLai(Connection c, int maNV)
            throws SQLException {
        // Lịch chưa ở trạng thái kết thúc phải được chuyển giao/giải quyết,
        // kể cả khi giờ hẹn đã qua nhưng trạng thái vẫn còn mở.
        String sql = "SELECT TOP 1 maLichHen FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE maNV=? AND ISNULL(trangThai,N'') NOT IN (N'Đã hủy',N'Hoàn tất')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new SQLException("Nhân viên còn lịch hẹn chưa kết thúc #"
                        + rs.getInt(1) + ". Hãy chuyển lịch trước khi cho nghỉ việc.");
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP 1 maLichLV FROM dbo.LichLamViec WITH (UPDLOCK,HOLDLOCK) "
              + "WHERE maNV=? AND ISNULL(trangThai,N'') NOT IN (N'Đã hủy',N'Ngừng xếp lịch') "
              + "AND (ngayLamViec>CONVERT(date,GETDATE()) OR "
              + "(ngayLamViec=CONVERT(date,GETDATE()) AND gioKetThuc>CONVERT(time,GETDATE())))")) {
            ps.setInt(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new SQLException("Nhân viên còn ca làm việc chưa kết thúc #"
                        + rs.getInt(1) + ". Hãy hủy hoặc chuyển ca trước khi cho nghỉ việc.");
            }
        }
    }

    /** Đọc quyền hiện hành từ VaiTroQuyen mỗi lần trước khi ghi. */
    private static void canQuyen(Connection c, int actor, String maQuyen) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON t.maVaiTro=q.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro<>N'Quản trị viên' AND q.maQuyen=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);
            ps.setString(2, maQuyen);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Bạn không có quyền " + maQuyen + ".");
            }
        }
    }

    /** Lưu hành động và ID người thực hiện vào nhật ký hệ thống. */
    private static void ghi(Connection c, int actor, String hanhDong, String doiTuong, String moTa)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            ps.setString(1, hanhDong);
            ps.setString(2, doiTuong);
            ps.setString(3, "maTK=" + actor + " | " + moTa);
            ps.executeUpdate();
        }
    }

    public int dem(String table) throws SQLException {
        // Không chấp nhận tên bảng tùy ý từ người dùng: chỉ tên cố định.
        if (!java.util.Arrays.asList("BenhNhan", "LichHen", "NhanVien", "DichVu", "HoaDon",
                "PhieuKham", "YeuCauHoTro").contains(table)) {
            throw new IllegalArgumentException("Bảng không được cho phép.");
        }
        try (Connection c = KetNoi.mo(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM dbo." + table)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    public List<Hang> benhNhan() throws SQLException {
        return truyVan("SELECT maBN,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(gioiTinh,'') AS gioiTinh, "
                + "CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,ISNULL(email,'') AS email, "
                + "CASE WHEN ISNULL(trangThaiHoSo,N'Hoạt động')=N'Ngừng hồ sơ' "
                + "THEN N'Ngừng hồ sơ' ELSE ISNULL(trangThai,'') END AS trangThai "
                + "FROM dbo.BenhNhan ORDER BY maBN DESC",
                r -> new Hang(r.getInt("maBN"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"gioiTinh"), chuoi(r,"ngaySinh"), chuoi(r,"email"),
                        chuoi(r,"trangThai")));
    }

    public void luuBenhNhan(int id, String hoTen, String sdt, LocalDate ngaySinh,
            String gioiTinh, String email, String diaChi, String tienSu, String trangThai,
            int actor) throws SQLException {
        batBuoc(hoTen, "Họ tên bệnh nhân không được trống.");
        // Chặn số giả ở tầng DAO ngay cả khi UI bị bỏ qua.
        try { sdt = SoDienThoaiHopLe.chuanHoa(sdt); }
        catch (IllegalArgumentException ex) { throw new SQLException(ex.getMessage(), ex); }
        if (ngaySinh != null && (ngaySinh.isAfter(LocalDate.now())
                || ngaySinh.isBefore(LocalDate.now().minusYears(100))))
            throw new SQLException("Ngày sinh bệnh nhân không được ở tương lai hoặc quá 100 năm.");
        if (email != null && !email.trim().isEmpty() && !EmailHopLe.kiemTra(email))
            throw new SQLException(EmailHopLe.huongDan());
        if (!java.util.Arrays.asList("Mới", "Đang điều trị", "Hoàn tất").contains(trangThai)) {
            throw new SQLException("Trạng thái bệnh nhân không hợp lệ.");
        }
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "BENHNHAN");
            c.setAutoCommit(false);
            try {
                if (id != 0) {
                    try (PreparedStatement check = c.prepareStatement(
                            "SELECT trangThaiHoSo FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) WHERE maBN=?")) {
                        check.setInt(1, id);
                        try (ResultSet rs = check.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Không tìm thấy bệnh nhân.");
                            if ("Ngừng hồ sơ".equalsIgnoreCase(rs.getString(1))) {
                                throw new SQLException("Hồ sơ đã ngừng. Hãy dùng chức năng khôi phục riêng.");
                            }
                        }
                    }
                }
                // Chỉ chặn bản ghi trùng hoàn toàn; không UNIQUE số điện thoại vì gia đình có thể dùng chung.
                String duplicateSql = "SELECT TOP 1 maBN FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE LOWER(LTRIM(RTRIM(hoTen)))=LOWER(?) AND sdt=? "
                        + "AND ((ngaySinh=?) OR (ngaySinh IS NULL AND ? IS NULL)) "
                        + "AND maBN<>? AND ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động'";
                try (PreparedStatement ps = c.prepareStatement(duplicateSql)) {
                    ps.setString(1, hoTen.trim());
                    ps.setString(2, sdt.trim());
                    if (ngaySinh == null) {
                        ps.setNull(3, Types.DATE);
                        ps.setNull(4, Types.DATE);
                    } else {
                        ps.setDate(3, Date.valueOf(ngaySinh));
                        ps.setDate(4, Date.valueOf(ngaySinh));
                    }
                    ps.setInt(5, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Có hồ sơ bệnh nhân trùng thông tin (mã #"
                                + rs.getInt(1) + "). Hãy kiểm tra trước khi lưu.");
                    }
                }
                String sql = id == 0
                    ? "INSERT dbo.BenhNhan(hoTen,sdt,ngaySinh,gioiTinh,email,diaChi,tienSuBenh,trangThai,trangThaiHoSo) "
                        + "VALUES(?,?,?,?,?,?,?,?,N'Hoạt động')"
                    : "UPDATE dbo.BenhNhan SET hoTen=?,sdt=?,ngaySinh=?,gioiTinh=?,email=?,diaChi=?,tienSuBenh=?,trangThai=? WHERE maBN=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    datThamSo(ps, hoTen.trim(), sdt.trim(), ngaySinh, gioiTinh, email, diaChi, tienSu, trangThai);
                    if (ngaySinh == null) ps.setNull(3, Types.DATE);
                    if (id != 0) ps.setInt(9, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không thể lưu bệnh nhân.");
                }
                ghi(c, actor, id == 0 ? "Thêm bệnh nhân" : "Sửa bệnh nhân", "BenhNhan", hoTen);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Khi sửa, nạp đầy đủ thông tin để không xóa nhầm địa chỉ / tiền sử bệnh. */
    public String[] chiTietBenhNhan(int maBN) throws SQLException {
        try (Connection c=KetNoi.mo(); PreparedStatement ps=c.prepareStatement(
                "SELECT ISNULL(hoTen,'') AS hoTen,ISNULL(sdt,'') AS sdt,"
              + "ISNULL(gioiTinh,'') AS gioiTinh,CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,"
              + "ISNULL(email,'') AS email,ISNULL(diaChi,'') AS diaChi,"
              + "ISNULL(tienSuBenh,'') AS tienSuBenh,"
              + "CASE WHEN ISNULL(trangThaiHoSo,N'Hoạt động')=N'Ngừng hồ sơ' "
              + "THEN N'Ngừng hồ sơ' ELSE ISNULL(trangThai,'') END AS trangThai "
              + "FROM dbo.BenhNhan WHERE maBN=?")) {
            ps.setInt(1,maBN);
            try(ResultSet r=ps.executeQuery()) {
                if(!r.next()) throw new SQLException("Không tìm thấy bệnh nhân.");
                return new String[]{chuoi(r,"hoTen"),chuoi(r,"sdt"),chuoi(r,"gioiTinh"),
                        chuoi(r,"ngaySinh"),chuoi(r,"email"),chuoi(r,"diaChi"),
                        chuoi(r,"tienSuBenh"),chuoi(r,"trangThai")};
            }
        }
    }

    /** Ngừng hồ sơ mềm; giữ nguyên toàn bộ lịch sử khám, điều trị và hóa đơn. */
    public void xoaBenhNhan(int id, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "BENHNHAN");
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT trangThaiHoSo FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) WHERE maBN=?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy bệnh nhân.");
                        if ("Ngừng hồ sơ".equalsIgnoreCase(rs.getString(1))) {
                            throw new SQLException("Hồ sơ bệnh nhân đã được ngừng trước đó.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT TOP 1 maLichHen FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maBN=? AND ISNULL(trangThai,N'') NOT IN (N'Đã hủy',N'Hoàn tất')")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Bệnh nhân còn lịch hẹn chưa kết thúc (#"
                                + rs.getInt(1) + "). Hãy xử lý lịch trước khi ngừng hồ sơ.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.BenhNhan SET trangThaiHoSo=N'Ngừng hồ sơ' WHERE maBN=?")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không thể ngừng hồ sơ bệnh nhân.");
                }
                ghi(c, actor, "Ngừng hồ sơ bệnh nhân", "BenhNhan", "maBN=" + id);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Khôi phục hồ sơ mềm; không tự khôi phục các lịch đã hủy hoặc trạng thái điều trị. */
    public void khoiPhucBenhNhan(int id, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "BENHNHAN");
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE dbo.BenhNhan SET trangThaiHoSo=N'Hoạt động' "
                  + "WHERE maBN=? AND trangThaiHoSo=N'Ngừng hồ sơ'")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() != 1) throw new SQLException("Hồ sơ không tồn tại hoặc chưa bị ngừng.");
                ghi(c, actor, "Khôi phục hồ sơ bệnh nhân", "BenhNhan", "maBN=" + id);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<Hang> nhanVien() throws SQLException {
        return truyVan("SELECT maNV,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(email,'') AS email, "
                + "ISNULL(chucVu,'') AS chucVu,ISNULL(trangThai,'') AS trangThai "
                + "FROM dbo.NhanVien ORDER BY maNV DESC",
                r -> new Hang(r.getInt("maNV"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"email"), chuoi(r,"chucVu"), chuoi(r,"trangThai")));
    }

    /** Nhân viên mới không tự tạo tài khoản: Admin cấp tài khoản ở phần Tài khoản. */
    /**
     * Nhân viên liên kết tài khoản không được đổi vai trò qua form vận hành:
     * Admin cần cấp quyền tại Quản lý tài khoản / Phân quyền.
     */
    public void luuNhanVien(int id, String ten, String sdt, String email, String chucVu,
            String trangThai, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên nhân viên.");
        // Kiểm tra ở DAO để không thể bỏ qua bằng cách gọi trực tiếp từ UI khác.
        String tenChuan = ten.trim();
        String sdtChuan = sdt == null ? "" : sdt.trim();
        String emailChuan = email == null ? "" : email.trim();
        try { sdtChuan = SoDienThoaiHopLe.chuanHoa(sdtChuan); }
        catch (IllegalArgumentException ex) { throw new SQLException(ex.getMessage(), ex); }
        if (!EmailHopLe.kiemTra(emailChuan))
            throw new SQLException(EmailHopLe.huongDan());
        if (!java.util.Arrays.asList("Quản lý phòng khám", "Bác sĩ", "Lễ tân", "Kế toán", "Nhân viên")
                .contains(chucVu)) {
            throw new SQLException("Chức vụ nhân viên không hợp lệ.");
        }
        if (!java.util.Arrays.asList("Đang làm việc", "Tạm nghỉ", "Nghỉ việc").contains(trangThai)) {
            throw new SQLException("Trạng thái nhân viên không hợp lệ.");
        }

        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {
                // Khi sửa loại trừ chính nhân viên hiện tại khỏi phép kiểm tra trùng SĐT.
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM dbo.NhanVien WITH (UPDLOCK, HOLDLOCK) WHERE sdt=? AND maNV<>?")) {
                    ps.setString(1, sdtChuan);
                    ps.setInt(2, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Số điện thoại đã tồn tại ở nhân viên khác.");
                    }
                }
                if (id != 0 && (!"Đang làm việc".equalsIgnoreCase(trangThai)
                        || !"Bác sĩ".equalsIgnoreCase(chucVu))) {
                    kiemTraNhanVienKhongConLichTuongLai(c, id);
                }
                if (id != 0) {
                    String check = "SELECT v.tenVaiTro FROM dbo.NhanVien n "
                            + "JOIN dbo.TaiKhoan t ON n.maTK=t.maTK "
                            + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE n.maNV=?";
                    try(PreparedStatement ps=c.prepareStatement(check)) {
                        ps.setInt(1,id);
                        try(ResultSet r=ps.executeQuery()) {
                            if(r.next() && !r.getString(1).equals(chucVu))
                                throw new SQLException("Nhân viên đã có tài khoản. Vai trò phải đổi tại trang Tài khoản của Admin.");
                        }
                    }
                }
                String sql = id == 0
                    ? "INSERT dbo.NhanVien(hoTen,sdt,email,chucVu,trangThai) VALUES(?,?,?,?,?)"
                    : "UPDATE dbo.NhanVien SET hoTen=?,sdt=?,email=?,chucVu=?,trangThai=? WHERE maNV=?";
                try (PreparedStatement ps=c.prepareStatement(sql)) {
                    datThamSo(ps,tenChuan,sdtChuan,emailChuan,chucVu,trangThai);
                    if(id!=0)ps.setInt(6,id);
                    if(ps.executeUpdate()!=1)throw new SQLException("Không thể lưu nhân viên.");
                }
                ghi(c, actor, id == 0 ? "Thêm nhân viên" : "Sửa nhân viên", "NhanVien", ten);
                c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Nghỉ việc mềm; không xóa hồ sơ nhân viên hay các liên kết lịch sử. */
    public void xoaNhanVien(int id, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {
                kiemTraNhanVienKhongConLichTuongLai(c, id);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.NhanVien SET trangThai=N'Nghỉ việc' "
                      + "WHERE maNV=? AND trangThai<>N'Nghỉ việc'")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Nhân viên không tồn tại hoặc đã nghỉ việc.");
                }
                ghi(c, actor, "Cho nhân viên nghỉ việc", "NhanVien", "maNV=" + id);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<Hang> dichVu() throws SQLException {
        return truyVan("SELECT maDV,tenDV,CAST(donGia AS DECIMAL(18,0)) AS donGia, "
                + "ISNULL(trangThai,'') AS trangThai,ISNULL(moTa,'') AS moTa "
                + "FROM dbo.DichVu ORDER BY maDV DESC",
                r -> new Hang(r.getInt("maDV"), chuoi(r,"tenDV"),
                        chuoi(r,"donGia"), chuoi(r,"trangThai"), chuoi(r,"moTa")));
    }

    /** Chỉ ghi thêm lịch sử giá khi đơn giá thực sự thay đổi; lịch sử cũ không bị ghi đè. */
    public void luuDichVu(int id, String ten, BigDecimal gia, String trangThai,
            String moTa, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên dịch vụ.");
        if (gia == null || gia.signum() < 0 || gia.scale() > 2 || gia.precision() > 18) {
            throw new SQLException("Giá dịch vụ phải từ 0 trở lên và tối đa 2 chữ số thập phân.");
        }
        batBuoc(trangThai, "Thiếu trạng thái dịch vụ.");
        if (!java.util.Arrays.asList("Đang áp dụng", "Ngừng áp dụng").contains(trangThai)) {
            throw new SQLException("Trạng thái dịch vụ không hợp lệ.");
        }
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "DICHVU");
            c.setAutoCommit(false);
            try {
                int maDV = id;
                boolean canGhiLichSuGia = id == 0;
                if (id == 0) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT dbo.DichVu(tenDV,donGia,trangThai,moTa) VALUES(?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        datThamSo(ps, ten.trim(), gia, trangThai, moTa);
                        if (ps.executeUpdate() != 1) throw new SQLException("Không thể tạo dịch vụ.");
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không lấy được mã dịch vụ mới.");
                            maDV = keys.getInt(1);
                        }
                    }
                } else {
                    BigDecimal giaCu;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT donGia FROM dbo.DichVu WITH (UPDLOCK,HOLDLOCK) WHERE maDV=?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Không tìm thấy dịch vụ.");
                            giaCu = rs.getBigDecimal(1);
                        }
                    }
                    canGhiLichSuGia = giaCu == null || giaCu.compareTo(gia) != 0;
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.DichVu SET tenDV=?,donGia=?,trangThai=?,moTa=? WHERE maDV=?")) {
                        datThamSo(ps, ten.trim(), gia, trangThai, moTa, id);
                        if (ps.executeUpdate() != 1) throw new SQLException("Không thể cập nhật dịch vụ.");
                    }
                }
                if (canGhiLichSuGia) {
                    // BangGiaDichVu chỉ có DATE; vẫn giữ từng lần đổi bằng maBG tăng dần.
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai) "
                          + "VALUES(?,CAST(GETDATE() AS date),?,?)")) {
                        datThamSo(ps, maDV, gia, trangThai);
                        ps.executeUpdate();
                    }
                }
                ghi(c, actor, id == 0 ? "Thêm dịch vụ" : "Sửa dịch vụ", "DichVu",
                        "maDV=" + maDV + ", ten=" + ten.trim() + ", gia=" + gia.toPlainString()
                        + (canGhiLichSuGia ? ", đã ghi lịch sử giá" : ", không thay đổi giá"));
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Ngừng áp dụng dịch vụ thay vì DELETE để giữ liên kết lịch sử. */
    public void xoaDichVu(int id, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "DICHVU");
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE dbo.DichVu SET trangThai=N'Ngừng áp dụng' "
                  + "WHERE maDV=? AND ISNULL(trangThai,N'')<>N'Ngừng áp dụng'")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() != 1) throw new SQLException("Dịch vụ không tồn tại hoặc đã ngừng áp dụng.");
                ghi(c, actor, "Ngừng áp dụng dịch vụ", "DichVu", "maDV=" + id);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<LuaChon> luaChon(String bang) throws SQLException {
        // Toàn bộ tên bảng/cột đến từ whitelist; dữ liệu người dùng chỉ là lựa chọn ID.
        String sql;
        switch (bang) {
            case "BenhNhan": sql = "SELECT maBN,hoTen FROM dbo.BenhNhan WHERE ISNULL(trangThaiHoSo,N'Hoạt động')=N'Hoạt động' ORDER BY hoTen"; break;
            case "NhanVien": sql = "SELECT maNV,hoTen FROM dbo.NhanVien "
                    + "WHERE trangThai=N'Đang làm việc' AND chucVu=N'Bác sĩ' ORDER BY hoTen"; break;
            case "DichVu": sql = "SELECT maDV,tenDV FROM dbo.DichVu WHERE trangThai<>N'Ngừng áp dụng' ORDER BY tenDV"; break;
            case "PhieuKham": sql = "SELECT p.maPK, N'PK #'+CONVERT(nvarchar(20),p.maPK)+N' - '+b.hoTen "
                    + "FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                    + "ORDER BY p.maPK DESC"; break;
            default: throw new IllegalArgumentException("Danh mục không hợp lệ: " + bang);
        }
        List<LuaChon> out = new ArrayList<>();
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) out.add(new LuaChon(r.getInt(1),r.getString(2)));
        }
        return out;
    }

    public List<Hang> lichHen() throws SQLException {
        return truyVan("SELECT l.maLichHen,CONVERT(varchar(10),l.ngayHen,103) AS ngay, "
                + "CONVERT(varchar(5),l.gioHen,108) AS gio,b.hoTen AS benhNhan, "
                + "ISNULL(n.hoTen,'Chưa phân công') AS bacSi,ISNULL(l.phongKham,'') AS phongKham, "
                + "ISNULL(l.lyDo,'') AS lyDo,l.trangThai FROM dbo.LichHen l "
                + "JOIN dbo.BenhNhan b ON l.maBN=b.maBN "
                + "LEFT JOIN dbo.NhanVien n ON l.maNV=n.maNV "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC",
                r -> new Hang(r.getInt("maLichHen"),chuoi(r,"ngay"),chuoi(r,"gio"),
                        chuoi(r,"benhNhan"),chuoi(r,"bacSi"),chuoi(r,"phongKham"),
                        chuoi(r,"lyDo"),chuoi(r,"trangThai")));
    }

    /** Đọc chi tiết lịch theo khóa chính, phục vụ form dời lịch. */
    public ThongTinLichHen chiTietLichHen(int maLichHen) throws SQLException {
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(
                "SELECT maBN,maNV,ngayHen,gioHen,ISNULL(thoiLuongPhut,60) AS thoiLuongPhut, "
              + "ISNULL(phongKham,N'') AS phongKham,ISNULL(lyDo,N'') AS lyDo,trangThai "
              + "FROM dbo.LichHen WHERE maLichHen=?")) {
            ps.setInt(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                int staff = rs.getInt("maNV");
                Integer maNV = rs.wasNull() ? null : staff;
                return new ThongTinLichHen(rs.getInt("maBN"), maNV,
                        rs.getDate("ngayHen").toLocalDate(), rs.getTime("gioHen").toLocalTime(),
                        rs.getInt("thoiLuongPhut"), rs.getString("phongKham"),
                        rs.getString("lyDo"), rs.getString("trangThai"));
            }
        }
    }

    /** 8 lịch gần đây cho Dashboard Quản lý, dữ liệu SQL thực. */
    public List<Hang> lichHenGanDay() throws SQLException {
        return truyVan("SELECT TOP 8 l.maLichHen,CONVERT(varchar(10),l.ngayHen,103) AS ngay, "
                + "CONVERT(varchar(5),l.gioHen,108) AS gio,b.hoTen,l.trangThai "
                + "FROM dbo.LichHen l JOIN dbo.BenhNhan b ON l.maBN=b.maBN "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC",
                r->new Hang(r.getInt(1),chuoi(r,"ngay"),chuoi(r,"gio"),
                        r.getString(4),chuoi(r,"trangThai")));
    }

    /** Tương thích form cũ: dùng thời lượng mặc định 60 phút đã ghi trong migration. */
    public void taoLichHen(int benhNhan, Integer nhanVien, LocalDate ngay, LocalTime gio,
            String phong, String lyDo, int actor) throws SQLException {
        taoLichHen(benhNhan, nhanVien, ngay, gio, THOI_LUONG_LICH_MAC_DINH_PHUT,
                phong, lyDo, actor);
    }

    /** Tạo lịch có thời lượng; kiểm tra bệnh nhân, bác sĩ/ca, phòng và mọi khoảng thời gian giao nhau. */
    public void taoLichHen(int benhNhan, Integer nhanVien, LocalDate ngay, LocalTime gio,
            int thoiLuongPhut, String phong, String lyDo, int actor) throws SQLException {
        if (benhNhan <= 0 || nhanVien == null || nhanVien <= 0 || ngay == null || gio == null) {
            throw new SQLException("Phải chọn bệnh nhân, bác sĩ, ngày và giờ hẹn.");
        }
        batBuoc(phong, "Phải chọn phòng khám.");
        if (lyDo != null && lyDo.trim().length() > 500) throw new SQLException("Lý do khám tối đa 500 ký tự.");
        kiemTraThoiLuong(thoiLuongPhut);
        LocalDateTime end = ketThucLich(ngay, gio, thoiLuongPhut);
        if (ngay.isBefore(LocalDate.now()) || (ngay.equals(LocalDate.now())
                && !end.isAfter(LocalDateTime.now()))) {
            throw new SQLException("Ngày và giờ hẹn phải nằm trong tương lai.");
        }
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "LICHHEN");
            c.setAutoCommit(false);
            try {
                khoaNguonLucLich(c, ngay, nhanVien, benhNhan, phong);
                kiemTraBenhNhanHoatDong(c, benhNhan);
                kiemTraBacSiTrongCa(c, nhanVien, ngay, gio, thoiLuongPhut);
                kiemTraKhongChongLich(c, 0, benhNhan, nhanVien, phong, ngay, gio, thoiLuongPhut);
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT dbo.LichHen(maBN,maNV,phongKham,ngayHen,gioHen,thoiLuongPhut,lyDo,trangThai) "
                      + "VALUES(?,?,?,?,?,?,?,N'Chờ xác nhận')")) {
                    ps.setInt(1, benhNhan);
                    ps.setInt(2, nhanVien);
                    ps.setString(3, phong.trim());
                    ps.setDate(4, Date.valueOf(ngay));
                    ps.setTime(5, Time.valueOf(gio));
                    ps.setInt(6, thoiLuongPhut);
                    ps.setString(7, lyDo == null ? "" : lyDo.trim());
                    if (ps.executeUpdate() != 1) throw new SQLException("Không tạo được lịch hẹn.");
                }
                ghi(c, actor, "Tạo lịch hẹn", "LichHen", "maBN=" + benhNhan + ", maNV=" + nhanVien
                        + ", ngay=" + ngay + ", gio=" + gio + ", thoiLuong=" + thoiLuongPhut
                        + ", phong=" + phong.trim());
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Danh sách ca làm của nhân viên; hiện chưa có màn hình UI gọi trực tiếp phương thức này. */
    public List<Hang> lichLamViec() throws SQLException {
        return truyVan("SELECT l.maLichLV,n.hoTen,CONVERT(varchar(10),l.ngayLamViec,103) AS ngay, "
                + "ISNULL(l.caLamViec,'') AS ca,CONVERT(varchar(5),l.gioBatDau,108) AS batDau, "
                + "CONVERT(varchar(5),l.gioKetThuc,108) AS ketThuc,ISNULL(l.trangThai,'') AS trangThai "
                + "FROM dbo.LichLamViec l JOIN dbo.NhanVien n ON n.maNV=l.maNV "
                + "ORDER BY l.ngayLamViec DESC,l.gioBatDau", r -> new Hang(r.getInt("maLichLV"),
                chuoi(r,"hoTen"),chuoi(r,"ngay"),chuoi(r,"ca"),chuoi(r,"batDau"),
                chuoi(r,"ketThuc"),chuoi(r,"trangThai")));
    }

    /** Đọc chi tiết ca làm việc theo khóa chính để mở form sửa. */
    public ThongTinLichLamViec chiTietLichLamViec(int maLichLV) throws SQLException {
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(
                "SELECT maNV,ngayLamViec,ISNULL(caLamViec,N'') AS caLamViec, "
              + "gioBatDau,gioKetThuc,ISNULL(trangThai,N'') AS trangThai "
              + "FROM dbo.LichLamViec WHERE maLichLV=?")) {
            ps.setInt(1, maLichLV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy ca làm việc.");
                java.sql.Time start = rs.getTime("gioBatDau");
                java.sql.Time end = rs.getTime("gioKetThuc");
                java.sql.Date workDate = rs.getDate("ngayLamViec");
                if (workDate == null) {
                    throw new SQLException("Ca làm việc chưa có ngày làm việc hợp lệ.");
                }
                return new ThongTinLichLamViec(rs.getInt("maNV"),
                        workDate.toLocalDate(), rs.getString("caLamViec"),
                        start == null ? null : start.toLocalTime(),
                        end == null ? null : end.toLocalTime(),
                        rs.getString("trangThai"));
            }
        }
    }

    /** CRUD ca làm việc dùng quyền NHANVIEN; chống trùng ca và kiểm tra lịch đã đặt. */
    public void luuLichLamViec(int id, int maNV, LocalDate ngay, String ca,
            LocalTime gioBatDau, LocalTime gioKetThuc, String trangThai, int actor)
            throws SQLException {
        if (maNV <= 0 || ngay == null || gioBatDau == null || gioKetThuc == null
                || !gioBatDau.isBefore(gioKetThuc)) {
            throw new SQLException("Thông tin ca làm việc không hợp lệ.");
        }
        if (ngay.isBefore(LocalDate.now())) throw new SQLException("Không thể xếp ca trong quá khứ.");
        batBuoc(ca, "Thiếu tên ca làm việc.");
        if (!java.util.Arrays.asList("Đã xếp lịch", "Ngừng xếp lịch", "Đã hủy").contains(trangThai)) {
            throw new SQLException("Trạng thái ca làm việc không hợp lệ.");
        }
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM dbo.NhanVien WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maNV=? AND chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc'")) {
                    ps.setInt(1, maNV);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Chỉ được xếp ca cho bác sĩ đang làm việc.");
                    }
                }
                // Nếu sửa ca hiện có, không cho thu hẹp/di chuyển ca khiến lịch đã đặt nằm ngoài ca.
                if (id != 0) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT TOP 1 l.maLichHen FROM dbo.LichHen l WITH (UPDLOCK,HOLDLOCK) "
                          + "WHERE l.maNV=? AND l.ngayHen=? "
                          + "AND ISNULL(l.trangThai,N'') NOT IN (N'Đã hủy',N'Hoàn tất') "
                          + "AND NOT (l.ngayHen=? AND "
                          + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime)) >= "
                          + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),CAST(? AS time)),CAST(? AS datetime)) "
                          + "AND DATEADD(MINUTE,ISNULL(l.thoiLuongPhut,60),"
                          + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime))) <= "
                          + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),CAST(? AS time)),CAST(? AS datetime)))")) {
                        // Chỉ cần kiểm tra ca cũ khi đang sửa; dữ liệu maNV/ngay cũ được khóa riêng bên dưới.
                        int oldStaff = maNV;
                        LocalDate oldDate = ngay;
                        try (PreparedStatement old = c.prepareStatement(
                                "SELECT maNV,ngayLamViec FROM dbo.LichLamViec WITH (UPDLOCK,HOLDLOCK) WHERE maLichLV=?")) {
                            old.setInt(1, id);
                            try (ResultSet rs = old.executeQuery()) {
                                if (!rs.next()) throw new SQLException("Không tìm thấy ca làm việc.");
                                oldStaff = rs.getInt(1);
                                oldDate = rs.getDate(2).toLocalDate();
                            }
                        }
                        ps.setInt(1, oldStaff);
                        ps.setDate(2, Date.valueOf(oldDate));
                        ps.setDate(3, Date.valueOf(ngay));
                        ps.setTime(4, Time.valueOf(gioBatDau));
                        ps.setDate(5, Date.valueOf(ngay));
                        ps.setTime(6, Time.valueOf(gioKetThuc));
                        ps.setDate(7, Date.valueOf(ngay));
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) throw new SQLException("Ca đang có lịch hẹn #" + rs.getInt(1)
                                    + ". Hãy chuyển lịch trước khi thay đổi ca.");
                        }
                    }
                }
                if ("Đã xếp lịch".equals(trangThai)) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT TOP 1 maLichLV FROM dbo.LichLamViec WITH (UPDLOCK,HOLDLOCK) "
                          + "WHERE maNV=? AND ngayLamViec=? AND maLichLV<>? "
                          + "AND ISNULL(trangThai,N'') NOT IN (N'Đã hủy',N'Ngừng xếp lịch') "
                          + "AND gioBatDau<? AND gioKetThuc>?")) {
                        ps.setInt(1, maNV);
                        ps.setDate(2, Date.valueOf(ngay));
                        ps.setInt(3, id);
                        ps.setTime(4, Time.valueOf(gioKetThuc));
                        ps.setTime(5, Time.valueOf(gioBatDau));
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) throw new SQLException("Ca làm việc bị chồng với ca #" + rs.getInt(1) + ".");
                        }
                    }
                }
                if (id == 0) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT dbo.LichLamViec(maNV,thu,ngayLamViec,caLamViec,gioBatDau,gioKetThuc,trangThai) "
                          + "VALUES(?,?,?,?,?,?,?)")) {
                        datThamSo(ps, maNV, thuTrongTuan(ngay), ngay, ca.trim(), gioBatDau, gioKetThuc, trangThai);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.LichLamViec SET maNV=?,thu=?,ngayLamViec=?,caLamViec=?, "
                          + "gioBatDau=?,gioKetThuc=?,trangThai=? WHERE maLichLV=?")) {
                        datThamSo(ps, maNV, thuTrongTuan(ngay), ngay, ca.trim(), gioBatDau,
                                gioKetThuc, trangThai, id);
                        if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy ca làm việc.");
                    }
                }
                ghi(c, actor, id == 0 ? "Thêm ca làm việc" : "Sửa ca làm việc", "LichLamViec",
                        "maNV=" + maNV + ", ngay=" + ngay + ", ca=" + ca.trim());
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Hủy ca mềm; không được hủy nếu còn lịch hẹn chưa kết thúc của bác sĩ trong ca đó. */
    public void xoaLichLamViec(int id, int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {
                int maNV;
                LocalDate ngay;
                LocalTime start;
                LocalTime end;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT maNV,ngayLamViec,gioBatDau,gioKetThuc FROM dbo.LichLamViec WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maLichLV=? AND ISNULL(trangThai,N'') NOT IN (N'Đã hủy',N'Ngừng xếp lịch')")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy ca đang xếp.");
                        maNV = rs.getInt(1);
                        ngay = rs.getDate(2).toLocalDate();
                        start = rs.getTime(3).toLocalTime();
                        end = rs.getTime(4).toLocalTime();
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "DECLARE @shiftStart DATETIME = DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),CAST(? AS time)),CAST(? AS datetime)); "
                      + "DECLARE @shiftEnd DATETIME = DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),CAST(? AS time)),CAST(? AS datetime)); "
                      + "SELECT TOP 1 l.maLichHen FROM dbo.LichHen l WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE l.maNV=? AND l.ngayHen=? AND ISNULL(l.trangThai,N'') NOT IN (N'Đã hủy',N'Hoàn tất') "
                      + "AND @shiftStart < DATEADD(MINUTE,ISNULL(l.thoiLuongPhut,60),"
                      + "DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime))) "
                      + "AND @shiftEnd > DATEADD(SECOND,DATEDIFF(SECOND,CAST('00:00:00' AS time),l.gioHen),CAST(l.ngayHen AS datetime))")) {
                    ps.setTime(1, Time.valueOf(start));
                    ps.setDate(2, Date.valueOf(ngay));
                    ps.setTime(3, Time.valueOf(end));
                    ps.setDate(4, Date.valueOf(ngay));
                    ps.setInt(5, maNV);
                    ps.setDate(6, Date.valueOf(ngay));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Ca đang có lịch hẹn #" + rs.getInt(1)
                                + ". Hãy chuyển lịch trước khi hủy ca.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.LichLamViec SET trangThai=N'Ngừng xếp lịch' WHERE maLichLV=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                ghi(c, actor, "Hủy ca làm việc", "LichLamViec", "maLichLV=" + id);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Dời lịch: cập nhật ngày/giờ/bác sĩ/phòng/thời lượng trong một transaction. */
    public void doiLichHen(int id, int benhNhan, Integer nhanVien, LocalDate ngay,
            LocalTime gio, int thoiLuongPhut, String phong, String lyDo, int actor)
            throws SQLException {
        if (id <= 0 || benhNhan <= 0 || nhanVien == null || nhanVien <= 0 || ngay == null || gio == null) {
            throw new SQLException("Thiếu thông tin để dời lịch.");
        }
        batBuoc(phong, "Phải chọn phòng khám.");
        if (lyDo != null && lyDo.trim().length() > 500) throw new SQLException("Lý do khám tối đa 500 ký tự.");
        LocalDateTime end = ketThucLich(ngay, gio, thoiLuongPhut);
        if (ngay.isBefore(LocalDate.now()) || (ngay.equals(LocalDate.now()) && !end.isAfter(LocalDateTime.now()))) {
            throw new SQLException("Ngày và giờ mới phải nằm trong tương lai.");
        }
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "LICHHEN");
            c.setAutoCommit(false);
            try {
                String currentStatus;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT trangThai FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) WHERE maLichHen=?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                        currentStatus = rs.getString(1);
                    }
                }
                if (!java.util.Arrays.asList("Chờ xác nhận", "Đã xác nhận", "Đã đến", "Chờ khám")
                        .contains(currentStatus)) {
                    throw new SQLException("Chỉ được dời lịch chưa bắt đầu khám và chưa kết thúc/hủy.");
                }
                khoaNguonLucLich(c, ngay, nhanVien, benhNhan, phong);
                kiemTraBenhNhanHoatDong(c, benhNhan);
                kiemTraBacSiTrongCa(c, nhanVien, ngay, gio, thoiLuongPhut);
                kiemTraKhongChongLich(c, id, benhNhan, nhanVien, phong, ngay, gio, thoiLuongPhut);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.LichHen SET maBN=?,maNV=?,ngayHen=?,gioHen=?,thoiLuongPhut=?, "
                      + "phongKham=?,lyDo=? WHERE maLichHen=? AND trangThai=?")) {
                    datThamSo(ps, benhNhan, nhanVien, ngay, gio, thoiLuongPhut,
                            phong.trim(), lyDo == null ? "" : lyDo.trim(), id, currentStatus);
                    if (ps.executeUpdate() != 1) throw new SQLException("Lịch vừa được thay đổi. Hãy tải lại.");
                }
                ghi(c, actor, "Dời lịch hẹn", "LichHen", "maLichHen=" + id + ", ngày=" + ngay
                        + ", giờ=" + gio + ", bác sĩ=" + nhanVien + ", phòng=" + phong.trim());
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Gán bác sĩ cho lịch do Lễ tân tiếp nhận trực tiếp (maNV ban đầu có thể NULL). */
    public void phanCongBacSi(int maLichHen, int maNV, int actor) throws SQLException {
        if (maLichHen <= 0 || maNV <= 0) throw new SQLException("Thiếu lịch hẹn hoặc bác sĩ.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "LICHHEN");
            c.setAutoCommit(false);
            try {
                int maBN;
                String phong;
                LocalDate ngay;
                LocalTime gio;
                int duration;
                String status;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT maBN,ISNULL(phongKham,N'') AS phongKham,ngayHen,gioHen, "
                      + "ISNULL(thoiLuongPhut,60) AS thoiLuongPhut,trangThai "
                      + "FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) WHERE maLichHen=?")) {
                    ps.setInt(1, maLichHen);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                        maBN = rs.getInt("maBN");
                        phong = rs.getString("phongKham");
                        ngay = rs.getDate("ngayHen").toLocalDate();
                        gio = rs.getTime("gioHen").toLocalTime();
                        duration = rs.getInt("thoiLuongPhut");
                        status = rs.getString("trangThai");
                    }
                }
                if (!java.util.Arrays.asList("Đã xác nhận", "Đã đến", "Chờ khám").contains(status)) {
                    throw new SQLException("Chỉ phân công bác sĩ cho lịch đã đến bước tiếp nhận.");
                }
                khoaNguonLucLich(c, ngay, maNV, maBN, phong);
                kiemTraBenhNhanHoatDong(c, maBN);
                kiemTraBacSiTrongCa(c, maNV, ngay, gio, duration);
                kiemTraKhongChongLich(c, maLichHen, maBN, maNV, phong, ngay, gio, duration);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.LichHen SET maNV=?,trangThai=CASE WHEN trangThai=N'Đã xác nhận' "
                      + "THEN N'Chờ khám' ELSE trangThai END WHERE maLichHen=?")) {
                    ps.setInt(1, maNV);
                    ps.setInt(2, maLichHen);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không thể phân công bác sĩ.");
                }
                ghi(c, actor, "Phân công bác sĩ", "LichHen", "maLichHen=" + maLichHen + ", maNV=" + maNV);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** State machine cho lịch hẹn; chống chuyển lùi/bỏ qua bước và gói cập nhật + audit trong transaction. */
    public void trangThaiLichHen(int id, String status, int actor) throws SQLException {
        List<String> allowed = java.util.Arrays.asList(
                "Chờ xác nhận", "Đã xác nhận", "Đã đến", "Chờ khám", "Đang khám", "Hoàn tất", "Đã hủy");
        if (!allowed.contains(status)) throw new SQLException("Trạng thái lịch hẹn không hợp lệ.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "LICHHEN");
            c.setAutoCommit(false);
            try {
                String current;
                Integer maNV;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT trangThai,maNV FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) WHERE maLichHen=?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                        current = rs.getString("trangThai");
                        int staffId = rs.getInt("maNV");
                        maNV = rs.wasNull() ? null : staffId;
                    }
                }
                if (status.equals(current)) throw new SQLException("Lịch hẹn đang ở trạng thái này.");
                boolean valid = false;
                if ("Chờ xác nhận".equals(current)) valid = "Đã xác nhận".equals(status) || "Đã hủy".equals(status);
                else if ("Đã xác nhận".equals(current)) valid = java.util.Arrays.asList("Đã đến", "Đã hủy").contains(status);
                else if ("Đã đến".equals(current)) valid = java.util.Arrays.asList("Chờ khám", "Đã hủy").contains(status);
                else if ("Chờ khám".equals(current)) valid = "Đang khám".equals(status) || "Đã hủy".equals(status);
                else if ("Đang khám".equals(current)) valid = "Hoàn tất".equals(status);
                if (!valid) throw new SQLException("Không được chuyển lịch từ '" + current + "' sang '" + status + "'.");
                if ("Đang khám".equals(status)) {
                    if (maNV == null) throw new SQLException("Lịch chưa được phân công bác sĩ.");
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT 1 FROM dbo.NhanVien WHERE maNV=? AND chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc'")) {
                        ps.setInt(1, maNV);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Bác sĩ đã ngừng làm việc hoặc không còn hợp lệ.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=? AND trangThai=?")) {
                    datThamSo(ps, status, id, current);
                    if (ps.executeUpdate() != 1) throw new SQLException("Lịch vừa được cập nhật ở nơi khác. Hãy tải lại.");
                }
                ghi(c, actor, "Cập nhật lịch hẹn", "LichHen", "maLichHen=" + id + ": " + current + " -> " + status);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    private static void datThamSoTail(PreparedStatement ps, int from, Object... args) throws SQLException {
        for (int i=0;i<args.length;i++) {
            Object arg=args[i];
            if (arg instanceof LocalDate) ps.setDate(i+from,Date.valueOf((LocalDate)arg));
            else if(arg instanceof LocalTime) ps.setTime(i+from,Time.valueOf((LocalTime)arg));
            else ps.setObject(i+from,arg);
        }
    }

    public List<Hang> hoaDon() throws SQLException {
        // maHD là KHÓA CHÍNH. Hiện riêng mã HĐ và mã thanh toán; STT chỉ là thứ tự bảng.
        // Sắp theo maHD tăng dần để người xem dễ đối chiếu mã khi mới mở danh sách.
        return truyVan(
                "SELECT h.maHD, b.hoTen, CONVERT(varchar(19), h.ngayLap, 120) AS ngay, "
              + "CONVERT(varchar(35), CAST(h.tongTien AS DECIMAL(18,0))) AS tien, "
              + "ISNULL(t.phuongThuc, '') AS phuongThuc, h.trangThai, "
              + "CONVERT(varchar(20), t.maTT) AS maTT "
              + "FROM dbo.HoaDon h "
              + "JOIN dbo.PhieuKham p ON h.maPK=p.maPK "
              + "JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
              + "JOIN dbo.ThanhToan t ON h.maTT=t.maTT "
              + "ORDER BY h.maHD ASC",
                r -> new Hang(
                        r.getInt("maHD"),
                        String.valueOf(r.getInt("maHD")),
                        chuoi(r, "hoTen"), chuoi(r, "ngay"),
                        chuoi(r, "tien"), chuoi(r, "phuongThuc"),
                        chuoi(r, "trangThai"), chuoi(r, "maTT")));
    }

    /**
     * Yêu cầu hủy bằng MÃ HÓA ĐƠN thật, không dùng STT hoặc vị trí dòng.
     * Khóa hóa đơn và lấy đúng maTT ngay trong cùng giao dịch để chống nhầm bản ghi.
     */
    public void yeuCauHuyHoaDon(int maHD, String lyDo, int actor) throws SQLException {
        batBuoc(lyDo, "Phải nhập lý do yêu cầu hủy.");
        if (lyDo.trim().length() > 500) {
            throw new SQLException("Lý do tối đa 500 ký tự.");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                canQuyen(c, actor, "THANHTOAN");
                int maTT;
                String invoiceStatus;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT h.maTT, h.trangThai FROM dbo.HoaDon h WITH (UPDLOCK, HOLDLOCK) "
                      + "WHERE h.maHD=?")) {
                    ps.setInt(1, maHD);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy hóa đơn #" + maHD);
                        invoiceStatus = rs.getString("trangThai");
                        if (!"Chờ thanh toán".equalsIgnoreCase(invoiceStatus)) {
                            throw new SQLException("Chỉ gửi yêu cầu hủy hóa đơn chưa thực thu đang Chờ thanh toán.");
                        }
                        maTT = rs.getInt("maTT");
                    }
                }
                // Nếu nhiều hóa đơn dùng chung maTT, duyệt sẽ cập nhật tất cả: từ chối để an toàn.
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM dbo.HoaDon WITH (UPDLOCK, HOLDLOCK) WHERE maTT=?")) {
                    ps.setInt(1, maTT);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) != 1) {
                            throw new SQLException("Giao dịch #" + maTT
                                    + " gắn nhiều hóa đơn, cần kiểm tra trước khi hủy.");
                        }
                    }
                }
                taoYeuCauHuyTrongGiaoDich(c, maTT, lyDo, actor);
                ghi(c, actor, "Gửi yêu cầu hủy hóa đơn", "HoaDon",
                        "maHD=" + maHD + ", maTT=" + maTT);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Giá trị từ ChiTietDieuTri để không nhập tùy ý số tiền thanh toán. */
    public BigDecimal soTienPhieuKham(int maPK) throws SQLException {
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WHERE maPK=?")) {
            ps.setInt(1,maPK);
            try (ResultSet r = ps.executeQuery()) { return r.next()?r.getBigDecimal(1):BigDecimal.ZERO; }
        }
    }

    /** Tiền mặt chỉ được ghi nhận sau xác nhận thu; chuyển khoản/thẻ ở trạng thái chờ xác minh. */
    public void taoThanhToan(int maPK, String phuongThuc, int actor) throws SQLException {
        batBuoc(phuongThuc, "Chọn phương thức thanh toán.");
        if (!java.util.Arrays.asList("Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng").contains(phuongThuc)) {
            throw new SQLException("Phương thức thanh toán không hợp lệ.");
        }
        boolean tienMat = "Tiền mặt".equals(phuongThuc);
        String trangThaiTT = tienMat ? "Đã thanh toán" : "Chờ xử lý";
        String trangThaiHD = tienMat ? "Đã thanh toán" : "Chờ thanh toán";
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "THANHTOAN");
            c.setAutoCommit(false);
            try {
                // App lock bảo đảm hai quầy không cùng tạo khoản thu cho một phiếu khám.
                try (PreparedStatement lock = c.prepareStatement(
                        "DECLARE @r INT; EXEC @r=sys.sp_getapplock @Resource=?, "
                      + "@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000; SELECT @r")) {
                    lock.setString(1, "MEDIDENT_THANHTOAN_PK_" + maPK);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) < 0) throw new SQLException("Không khóa được phiếu thanh toán. Hãy thử lại.");
                    }
                }
                BigDecimal soTien;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WITH (UPDLOCK,HOLDLOCK) WHERE maPK=?")) {
                    ps.setInt(1, maPK);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy phiếu khám.");
                        soTien = rs.getBigDecimal(1);
                    }
                }
                if (soTien == null || soTien.signum() <= 0) {
                    throw new SQLException("Phiếu khám chưa có chi tiết điều trị hoặc số tiền bằng 0.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT TOP 1 maTT,trangThai FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maPK=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tiền',N'Thất bại') ORDER BY maTT DESC")) {
                    ps.setInt(1, maPK);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new SQLException("Phiếu khám đã có khoản thanh toán #" + rs.getInt("maTT")
                                    + " ở trạng thái '" + rs.getString("trangThai") + "'. Không tạo khoản trùng.");
                        }
                    }
                }
                int maTT;
                String insertPayment = tienMat
                        ? "INSERT dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai) VALUES(?,SYSDATETIME(),?,?,?)"
                        : "INSERT dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai) VALUES(?,NULL,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(insertPayment, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, maPK);
                    ps.setBigDecimal(2, soTien);
                    ps.setString(3, phuongThuc);
                    ps.setString(4, trangThaiTT);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được mã thanh toán.");
                        maTT = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,?)")) {
                    datThamSo(ps, maTT, maPK, soTien, trangThaiHD);
                    ps.executeUpdate();
                }
                ghi(c, actor, tienMat ? "Thu tiền mặt" : "Tạo khoản chờ xác minh",
                        "ThanhToan", "maPK=" + maPK + ", maTT=" + maTT + ", soTien=" + soTien
                        + ", phuongThuc=" + phuongThuc + ", trangThai=" + trangThaiTT);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Xác nhận tiền chuyển khoản/thẻ đã thực nhận; bắt buộc mã giao dịch để đối soát. */
    public void xacNhanThucThu(int maTT, String maGiaoDich, int actor) throws SQLException {
        if (maTT <= 0) throw new SQLException("Mã thanh toán không hợp lệ.");
        batBuoc(maGiaoDich, "Phải nhập mã giao dịch/xác nhận thực nhận.");
        if (maGiaoDich.trim().length() > 100) throw new SQLException("Mã giao dịch tối đa 100 ký tự.");
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "THANHTOAN");
            c.setAutoCommit(false);
            try {
                // Khóa theo mã giao dịch để hai người không thể đồng thời gán cùng một reference.
                try (PreparedStatement lock = c.prepareStatement(
                        "DECLARE @r INT; EXEC @r=sys.sp_getapplock @Resource=?, "
                      + "@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000; SELECT @r")) {
                    lock.setString(1, "MEDIDENT_GIAODICH_" + maGiaoDich.trim().toUpperCase(java.util.Locale.ROOT));
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next() || rs.getInt(1) < 0) {
                            throw new SQLException("Không khóa được mã giao dịch để đối soát. Hãy thử lại.");
                        }
                    }
                }
                int maPK;
                String status;
                String method;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT maPK,trangThai,phuongThuc FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) WHERE maTT=?")) {
                    ps.setInt(1, maTT);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy thanh toán #" + maTT + ".");
                        maPK = rs.getInt("maPK");
                        status = rs.getString("trangThai");
                        method = rs.getString("phuongThuc");
                    }
                }
                if (!"Chờ xử lý".equals(status)) {
                    throw new SQLException("Chỉ xác nhận khoản đang Chờ xử lý; trạng thái hiện tại: " + status);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) WHERE maGiaoDich=? AND maTT<>?")) {
                    ps.setString(1, maGiaoDich.trim());
                    ps.setInt(2, maTT);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Mã giao dịch đã được ghi nhận cho khoản thanh toán khác.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.ThanhToan SET trangThai=N'Đã thanh toán',ngayThanhToan=SYSDATETIME(),maGiaoDich=? "
                      + "WHERE maTT=? AND trangThai=N'Chờ xử lý'")) {
                    ps.setString(1, maGiaoDich.trim());
                    ps.setInt(2, maTT);
                    if (ps.executeUpdate() != 1) throw new SQLException("Khoản thanh toán vừa thay đổi. Hãy tải lại.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.HoaDon SET trangThai=N'Đã thanh toán' WHERE maTT=? AND trangThai=N'Chờ thanh toán'")) {
                    ps.setInt(1, maTT);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không tìm thấy hóa đơn chờ thanh toán tương ứng.");
                }
                ghi(c, actor, "Xác nhận thực thu", "ThanhToan", "maTT=" + maTT + ", maPK=" + maPK
                        + ", phuongThuc=" + method + ", maGiaoDich=" + maGiaoDich.trim());
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /**
     * Gửi yêu cầu hủy: bắt buộc lưu người gửi để lọc theo cấp bậc.
     * Transaction + TABLOCKX ngăn 2 người gửi đơn chờ duyệt trùng maTT.
     */
    public void yeuCauHuy(int maTT, String lyDo, int actor) throws SQLException {
        batBuoc(lyDo, "Phải nhập lý do yêu cầu hủy.");
        if (lyDo.trim().length() > 500) {
            throw new SQLException("Lý do tối đa 500 ký tự.");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                canQuyen(c, actor, "THANHTOAN");
                taoYeuCauHuyTrongGiaoDich(c, maTT, lyDo, actor);
                ghi(c, actor, "Gửi yêu cầu hủy thanh toán", "ThanhToan", "maTT=" + maTT);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Chạy dưới cùng một transaction của hóa đơn được chọn.
     * Không tự COMMIT tại đây để tránh lệch dữ liệu khi thao tác đồng thời.
     */
    private static void taoYeuCauHuyTrongGiaoDich(
            Connection c, int maTT, String lyDo, int actor) throws SQLException {

        // Khóa tập yêu cầu để không gửi hai đơn đang chờ cho cùng một thanh toán.
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM dbo.YeuCauHuyPhieuThu WITH (TABLOCKX, HOLDLOCK) "
              + "WHERE maTT=? AND trangThai=N'Chờ duyệt'")) {
            ps.setInt(1, maTT);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) != 0) {
                    throw new SQLException("Thanh toán đã có yêu cầu hủy chờ duyệt.");
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT trangThai FROM dbo.ThanhToan WITH (UPDLOCK, HOLDLOCK) WHERE maTT=?")) {
            ps.setInt(1, maTT);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy thanh toán #" + maTT);
                String paymentStatus = rs.getString(1);
                if ("Đã thanh toán".equalsIgnoreCase(paymentStatus)
                        || "Thành công".equalsIgnoreCase(paymentStatus)) {
                    throw new SQLException("Khoản thanh toán đã thực thu. Hủy hóa đơn phải đi kèm quy trình hoàn tiền riêng.");
                }
                if (!"Chờ xử lý".equalsIgnoreCase(paymentStatus)) {
                    throw new SQLException("Chỉ có thể gửi yêu cầu hủy cho khoản thanh toán đang Chờ xử lý.");
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai,maTKNguoiGui) "
              + "VALUES(?,?,N'Chờ duyệt',?)")) {
            ps.setInt(1, maTT);
            ps.setString(2, lyDo.trim());
            ps.setInt(3, actor);
            ps.executeUpdate();
        }
    }

    /**
     * Cấp xem yêu cầu lấy từ SQL, không lấy tên vai trò do giao diện truyền lên.
     * Cấp 3 Admin > cấp 2 Quản lý > cấp 1 Lễ tân.
     * Các vai trò chưa phân cấp (0) chỉ được xem đơn của bản thân.
     */
    private static int capBacHoTro(Connection c, int actor) throws SQLException {
        String sql = "SELECT v.capBacHoTro FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Tài khoản bị khóa hoặc không tồn tại.");
                }
                return rs.getInt(1);
            }
        }
    }

    private static boolean coQuyen(Connection c, int actor, String code) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM dbo.TaiKhoan t "
              + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
              + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND q.maQuyen=?")) {
            ps.setInt(1, actor);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Chỉ vai trò có cấp 2+ VÀ quyền HOTRO mới được phản hồi yêu cầu. */
    public boolean coQuyenPhanHoiHoTro(int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            return capBacHoTro(c, actor) >= 2 && coQuyen(c, actor, "HOTRO");
        }
    }

    /**
     * Liệt kê yêu cầu hủy thuộc phạm vi cấp bậc người đăng nhập.
     * Yêu cầu cũ không ghi maTKNguoiGui chỉ Admin được xem.
     * Hang.cot: ngày, mã TT, mã HĐ, người gửi, vai trò, lý do, trạng thái, ghi chú duyệt.
     */
    public List<Hang> danhSachYeuCauHuy(int actor) throws SQLException {
        String sql = "SELECT y.maYC,CONVERT(varchar(19),y.ngayYeuCau,120) AS ngay," 
                + "CONVERT(varchar(20),y.maTT) AS maTT," 
                + "ISNULL(CONVERT(varchar(20),h.maHD),'—') AS maHD," 
                + "ISNULL(n.hoTen,t.tenDangNhap) AS nguoiGui," 
                + "ISNULL(v.tenVaiTro,N'Không rõ') AS vaiTro," 
                + "y.lyDo,y.trangThai,ISNULL(pd.lyDoPheDuyet,'') AS ghiChu "
                + "FROM dbo.YeuCauHuyPhieuThu y "
                + "LEFT JOIN dbo.HoaDon h ON h.maHD=(SELECT TOP(1) h2.maHD "
                + " FROM dbo.HoaDon h2 WHERE h2.maTT=y.maTT ORDER BY h2.maHD DESC) "
                + "LEFT JOIN dbo.TaiKhoan t ON t.maTK=y.maTKNguoiGui "
                + "LEFT JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                + "OUTER APPLY (SELECT TOP(1) p.lyDoPheDuyet "
                + " FROM dbo.PhieuDuyetHuyPhieuThu p WHERE p.maYC=y.maYC "
                + " ORDER BY p.maPD DESC) pd "
                + "JOIN dbo.TaiKhoan me ON me.maTK=? AND me.trangThai=N'Hoạt động' "
                + "JOIN dbo.VaiTro myRole ON myRole.maVaiTro=me.maVaiTro "
                + "WHERE (myRole.tenVaiTro=N'Quản trị viên' OR y.maTKNguoiGui=? OR "
                + "(myRole.capBacHoTro > 0 AND v.capBacHoTro > 0 "
                + " AND v.capBacHoTro <= myRole.capBacHoTro) OR "
                + "(y.maTKNguoiGui IS NULL AND myRole.tenVaiTro=N'Quản trị viên')) "
                + "ORDER BY y.maYC DESC";
        return truyVan(sql, r -> new Hang(r.getInt("maYC"),
                chuoi(r, "ngay"), chuoi(r, "maTT"), chuoi(r, "maHD"),
                chuoi(r, "nguoiGui"), chuoi(r, "vaiTro"), chuoi(r, "lyDo"),
                chuoi(r, "trangThai"), chuoi(r, "ghiChu")), actor, actor);
    }

    /** Admin duyệt hoặc từ chối; dữ liệu đơn và hóa đơn cập nhật cùng transaction. */
    public void duyetYeuCauHuy(int maYC, boolean chapThuan, String note, int actor)
            throws SQLException {
        if (!chapThuan) {
            batBuoc(note, "Từ chối phải nhập lý do.");
        }
        if (note != null && note.length() > 500) {
            throw new SQLException("Ghi chú duyệt tối đa 500 ký tự.");
        }
        try (Connection c = KetNoi.mo()) {
            if (!laAdmin(c, actor) || !coQuyen(c, actor, "HOTRO")) {
                throw new SQLException("Chỉ Admin có quyền Hỗ trợ được duyệt hủy hóa đơn.");
            }
            c.setAutoCommit(false);
            try {
                int maTT;
                String trangThaiThanhToan;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT y.maTT,t.trangThai AS trangThaiThanhToan "
                      + "FROM dbo.YeuCauHuyPhieuThu y WITH (UPDLOCK,HOLDLOCK) "
                      + "JOIN dbo.ThanhToan t WITH (UPDLOCK,HOLDLOCK) ON t.maTT=y.maTT "
                      + "WHERE y.maYC=? AND y.trangThai=N'Chờ duyệt'")) {
                    ps.setInt(1, maYC);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Yêu cầu không còn ở trạng thái Chờ duyệt.");
                        }
                        maTT = rs.getInt("maTT");
                        trangThaiThanhToan = rs.getString("trangThaiThanhToan");
                    }
                }
                // Hủy chứng từ không đồng nghĩa hoàn tiền. Chỉ cho duyệt hủy khoản chưa thực thu.
                if (chapThuan && ("Đã thanh toán".equalsIgnoreCase(trangThaiThanhToan)
                        || "Thành công".equalsIgnoreCase(trangThaiThanhToan))) {
                    throw new SQLException("Thanh toán đã thực thu. Không được hủy trực tiếp; cần quy trình hoàn tiền và đối soát riêng.");
                }
                if (chapThuan && !"Chờ xử lý".equalsIgnoreCase(trangThaiThanhToan)) {
                    throw new SQLException("Chỉ được duyệt hủy khoản thanh toán đang Chờ xử lý.");
                }
                // Không duyệt tự động nếu một thanh toán có nhiều hóa đơn liên kết.
                if (chapThuan) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM dbo.HoaDon WITH (UPDLOCK, HOLDLOCK) WHERE maTT=?")) {
                        ps.setInt(1, maTT);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) != 1) {
                                throw new SQLException("Mã thanh toán gắn nhiều hoặc không có hóa đơn; "
                                        + "hãy đối chiếu thủ công trước khi duyệt.");
                            }
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.YeuCauHuyPhieuThu SET trangThai=? WHERE maYC=?")) {
                    ps.setString(1, chapThuan ? "Đã duyệt" : "Từ chối");
                    ps.setInt(2, maYC);
                    if (ps.executeUpdate() != 1) throw new SQLException("Yêu cầu vừa được xử lý ở nơi khác.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet) "
                      + "VALUES(?,SYSDATETIME(),?)")) {
                    ps.setInt(1, maYC);
                    ps.setString(2, note == null ? "" : note.trim());
                    ps.executeUpdate();
                }
                if (chapThuan) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=? AND trangThai=N'Chờ xử lý'")) {
                        ps.setInt(1, maTT);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Thanh toán không tồn tại, không thể duyệt.");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maTT=? AND trangThai=N'Chờ thanh toán'")) {
                        ps.setInt(1, maTT);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Hóa đơn không ở trạng thái Chờ thanh toán; cần đối chiếu trước khi duyệt.");
                        }
                    }
                }
                ghi(c, actor, chapThuan ? "Duyệt hủy hóa đơn" : "Từ chối hủy hóa đơn",
                        "YeuCauHuyPhieuThu", "maYC=" + maYC + ", maTT=" + maTT);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private boolean laAdmin(Connection c,int actor) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'")) {
            ps.setInt(1,actor);
            try(ResultSet r=ps.executeQuery()) { return r.next(); }
        }
    }

    public void guiHoTro(int actor,String tieuDe,String noiDung) throws SQLException {
        batBuoc(tieuDe,"Nhập tiêu đề yêu cầu."); batBuoc(noiDung,"Nhập nội dung cần hỗ trợ.");
        try(Connection c=KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement valid=c.prepareStatement(
                        "SELECT 1 FROM dbo.TaiKhoan WITH (UPDLOCK,HOLDLOCK) WHERE maTK=? AND trangThai=N'Hoạt động'")) {
                    valid.setInt(1,actor);
                    try(ResultSet r=valid.executeQuery()) {
                        if(!r.next()) throw new SQLException("Tài khoản đã bị khóa hoặc đăng xuất.");
                    }
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT dbo.YeuCauHoTro(maTK,tieuDe,noiDung) VALUES(?,?,?)")) {
                    datThamSo(ps,actor,tieuDe.trim(),noiDung.trim());
                    if(ps.executeUpdate()!=1) throw new SQLException("Không gửi được yêu cầu hỗ trợ.");
                }
                ghi(c,actor,"Gửi yêu cầu hỗ trợ","YeuCauHoTro",tieuDe.trim());
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /**
     * Xem hỗ trợ theo cấp bậc thực tế từ SQL, KHÔNG chỉ lọc y.maTK=actor.
     * Cấp 3: xem cấp 3/2/1, cấp 2: xem 2/1, cấp 1: xem 1.
     * Các role chưa gán cấp vẫn xem được yêu cầu của chính họ.
     */
    public List<Hang> danhSachHoTro(int actor) throws SQLException {
        String sql = "SELECT y.maYC,ISNULL(n.hoTen,t.tenDangNhap) AS nguoi," 
                + "CONVERT(varchar(19),y.ngayTao,120) AS ngay," 
                + "y.tieuDe,y.trangThai,ISNULL(y.noiDung,'') AS noiDung," 
                + "ISNULL(y.phanHoi,'') AS phanHoi, v.tenVaiTro AS vaiTro "
                + "FROM dbo.YeuCauHoTro y "
                + "JOIN dbo.TaiKhoan t ON t.maTK=y.maTK "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                + "JOIN dbo.TaiKhoan me ON me.maTK=? AND me.trangThai=N'Hoạt động' "
                + "JOIN dbo.VaiTro myRole ON myRole.maVaiTro=me.maVaiTro "
                + "WHERE (myRole.tenVaiTro=N'Quản trị viên' OR y.maTK=? OR (myRole.capBacHoTro>0 "
                + "AND v.capBacHoTro>0 AND v.capBacHoTro<=myRole.capBacHoTro)) "
                + "ORDER BY y.maYC DESC";
        return truyVan(sql, r -> new Hang(r.getInt("maYC"), chuoi(r, "nguoi"),
                chuoi(r, "ngay"), chuoi(r, "tieuDe"), chuoi(r, "trangThai"),
                chuoi(r, "noiDung"), chuoi(r, "phanHoi"), chuoi(r, "vaiTro")),
                actor, actor);
    }

    /**
     * Phản hồi chỉ khi có quyền HOTRO, cấp >= 2, và người gửi không cao cấp hơn.
     * Kiểm tra ngay ở DAO kể cả khi UI vô tình bật nút.
     */
    public void phanHoiHoTro(int maYC, String phanHoi, int actor) throws SQLException {
        batBuoc(phanHoi, "Nhập nội dung phản hồi.");
        if (phanHoi.trim().length() > 1500) {
            throw new SQLException("Nội dung phản hồi tối đa 1500 ký tự.");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                int myRank = capBacHoTro(c, actor);
                if (myRank < 2 || !coQuyen(c, actor, "HOTRO")) {
                    throw new SQLException("Vai trò hiện tại không có quyền phản hồi hỗ trợ.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT t.maTK,v.capBacHoTro FROM dbo.YeuCauHoTro y WITH (UPDLOCK) "
                      + "JOIN dbo.TaiKhoan t ON t.maTK=y.maTK "
                      + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro WHERE y.maYC=?")) {
                    ps.setInt(1, maYC);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy yêu cầu hỗ trợ.");
                        }
                        int senderId = rs.getInt(1);
                        int senderRank = rs.getInt(2);
                        if (senderId != actor && (senderRank == 0 || senderRank > myRank)) {
                            throw new SQLException("Không được phản hồi yêu cầu cấp cao hơn.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.YeuCauHoTro SET phanHoi=?, "
                      + "trangThai=N'Đã xử lý',ngayXuLy=SYSDATETIME() WHERE maYC=?")) {
                    ps.setString(1, phanHoi.trim());
                    ps.setInt(2, maYC);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Không cập nhật được yêu cầu hỗ trợ.");
                    }
                }
                ghi(c, actor, "Phản hồi yêu cầu hỗ trợ", "YeuCauHoTro", "maYC=" + maYC);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

}
