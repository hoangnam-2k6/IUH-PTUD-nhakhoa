/*
 * QuanLyDAO: truy cập dữ liệu và kiểm soát nghiệp vụ SQL Server.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package DAO;

import ConnectDB.KetNoi;
import TienIch.EmailHopLe;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class QuanLyDAO {

    public static final class Hang {
        public final int id;
        public final String[] cot;
        /** Khởi tạo hàng dữ liệu gồm khóa chính SQL và các cột hiển thị. */
        public Hang(int id, String... cot) { this.id = id; this.cot = cot; }
    }

    public static final class LuaChon {
        public final int id;
        public final String ten;
        /** Tạo giá trị lựa chọn gồm mã thực và tên hiển thị cho ComboBox. */
        public LuaChon(int id, String ten) { this.id = id; this.ten = ten; }
        @Override public String toString() { return ten; }
    }

    private interface DocHang { Hang doc(ResultSet rs) throws SQLException; }

    /** Chạy SELECT bằng PreparedStatement và tự đóng Connection, ResultSet. */
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

    /** Gắn từng giá trị vào PreparedStatement để tránh ghép chuỗi SQL không an toàn. */
    private static void datThamSo(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof LocalDate) ps.setDate(i + 1, Date.valueOf((LocalDate) arg));
            else if (arg instanceof LocalTime) ps.setTime(i + 1, Time.valueOf((LocalTime) arg));
            else ps.setObject(i + 1, arg);
        }
    }

    /** Lấy nội dung cột từ ResultSet, chuyển NULL thành chuỗi rỗng. */
    private static String chuoi(ResultSet rs, String key) throws SQLException {
        String value = rs.getString(key);
        return value == null ? "" : value;
    }

    /** Kiểm tra trường văn bản bắt buộc và trả lỗi SQL nếu rỗng. */
    private static void batBuoc(String value, String message) throws SQLException {
        if (value == null || value.trim().isEmpty()) throw new SQLException(message);
    }

    /** Kiểm tra quyền từ cơ sở dữ liệu tại thời điểm thao tác; không dùng quyền hiển thị của UI. */
    private static void canQuyen(Connection c, int actor, String maQuyen) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON t.maVaiTro=q.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro<>N'Quản trị viên' AND q.maQuyen=? "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.NhanVien n "
                + "WHERE n.maTK=t.maTK AND (n.trangThai IS NULL OR n.trangThai<>N'Đang làm việc'))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, actor);
            ps.setString(2, maQuyen);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Bạn không có quyền " + maQuyen + ".");
            }
        }
    }

    /** Ghi sự kiện nghiệp vụ vào nhật ký trên chính Connection của transaction. */
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

    /** Đếm dữ liệu của lớp; kết quả được chuyển cho nơi gọi. */
    public int dem(String table) throws SQLException {

        if (!java.util.Arrays.asList("BenhNhan", "LichHen", "NhanVien", "DichVu", "HoaDon",
                "PhieuKham", "YeuCauHoTro").contains(table)) {
            throw new IllegalArgumentException("Bảng không được cho phép.");
        }
        try (Connection c = KetNoi.mo(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT COUNT(*) FROM dbo." + table)) {
            return r.next() ? r.getInt(1) : 0;
        }
    }

    /** Nạp danh sách bệnh nhân để hiển thị bảng quản lý. */
    public List<Hang> benhNhan() throws SQLException {
        return truyVan("SELECT maBN,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(gioiTinh,'') AS gioiTinh, "
                + "CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,ISNULL(email,'') AS email, "
                + "ISNULL(trangThai,'') AS trangThai FROM dbo.BenhNhan ORDER BY maBN DESC",
                r -> new Hang(r.getInt("maBN"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"gioiTinh"), chuoi(r,"ngaySinh"), chuoi(r,"email"),
                        chuoi(r,"trangThai")));
    }

    /** Xác thực dữ liệu bệnh nhân, đối chiếu hồ sơ trùng chính xác và ghi nhật ký cùng transaction. */
    public void luuBenhNhan(int id, String hoTen, String sdt, LocalDate ngaySinh,
            String gioiTinh, String email, String diaChi, String tienSu, String trangThai,
            int actor) throws SQLException {
        batBuoc(hoTen, "Họ tên bệnh nhân không được trống.");
        if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now()))
            throw new SQLException("Ngày sinh bệnh nhân không được ở tương lai.");
        if (gioiTinh == null || !java.util.Set.of("Nam", "Nữ", "Khác", "Chưa xác định").contains(gioiTinh))
            throw new SQLException("Giới tính bệnh nhân không hợp lệ.");
        if (trangThai == null || !java.util.Set.of("Mới", "Đang điều trị", "Hoàn tất", "Ngừng hoạt động").contains(trangThai))
            throw new SQLException("Trạng thái bệnh nhân không hợp lệ.");
        if (sdt == null || !sdt.matches("[0-9+ -]{9,16}"))
            throw new SQLException("Số điện thoại phải từ 9-16 ký tự hợp lệ.");
        if (email != null && !email.trim().isEmpty() && !EmailHopLe.kiemTra(email))
            throw new SQLException(EmailHopLe.huongDan());
        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "BENHNHAN");
            String sql = id == 0
                ? "INSERT dbo.BenhNhan(hoTen,sdt,ngaySinh,gioiTinh,email,diaChi,tienSuBenh,trangThai) VALUES(?,?,?,?,?,?,?,?)"
                : "UPDATE dbo.BenhNhan SET hoTen=?,sdt=?,ngaySinh=?,gioiTinh=?,email=?,diaChi=?,tienSuBenh=?,trangThai=? WHERE maBN=?";
            c.setAutoCommit(false);
            try {
                if (id == 0 && ngaySinh != null) {

                    try (PreparedStatement duplicate = c.prepareStatement(
                            "SELECT 1 FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                          + "WHERE LOWER(LTRIM(RTRIM(hoTen)))=LOWER(?) AND ngaySinh=? AND sdt=?")) {
                        datThamSo(duplicate, hoTen.trim(), ngaySinh, sdt.trim());
                        try (ResultSet found = duplicate.executeQuery()) {
                            if (found.next()) throw new SQLException(
                                    "Hồ sơ có cùng họ tên, ngày sinh và số điện thoại đã tồn tại; hãy kiểm tra trước khi tạo.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    datThamSo(ps, hoTen.trim(), sdt.trim(), ngaySinh, gioiTinh, email, diaChi, tienSu, trangThai);
                    if (ngaySinh == null) ps.setNull(3, java.sql.Types.DATE);
                    if (id != 0) ps.setInt(9, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không thể lưu bệnh nhân.");
                    // Lưu thành công và nhật ký phải cùng COMMIT; lỗi ở bước nào cũng ROLLBACK.
                    ghi(c, actor, id == 0 ? "Thêm bệnh nhân" : "Sửa bệnh nhân", "BenhNhan", hoTen);
                }
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Lấy toàn bộ thuộc tính bệnh nhân để sửa mà không mất tiền sử bệnh. */
    public String[] chiTietBenhNhan(int maBN) throws SQLException {
        try (Connection c=KetNoi.mo(); PreparedStatement ps=c.prepareStatement(
                "SELECT ISNULL(hoTen,'') AS hoTen,ISNULL(sdt,'') AS sdt,"
              + "ISNULL(gioiTinh,'') AS gioiTinh,CONVERT(varchar(10),ngaySinh,103) AS ngaySinh,"
              + "ISNULL(email,'') AS email,ISNULL(diaChi,'') AS diaChi,"
              + "ISNULL(tienSuBenh,'') AS tienSuBenh,ISNULL(trangThai,'') AS trangThai "
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

    /** Chuyển bệnh nhân sang ngừng hoạt động; giữ khóa chính và lịch sử khám. */
    public void xoaBenhNhan(int id, int actor) throws SQLException {
        xoaAnToan("BENHNHAN", "dbo.BenhNhan", "maBN", id, actor, "Bệnh nhân");
    }

    /** Nạp danh sách nhân viên cùng trạng thái và chức vụ hiện có. */
    public List<Hang> nhanVien() throws SQLException {
        return truyVan("SELECT maNV,hoTen,ISNULL(sdt,'') AS sdt,ISNULL(email,'') AS email, "
                + "ISNULL(chucVu,'') AS chucVu,ISNULL(trangThai,'') AS trangThai "
                + "FROM dbo.NhanVien ORDER BY maNV DESC",
                r -> new Hang(r.getInt("maNV"), chuoi(r,"hoTen"), chuoi(r,"sdt"),
                        chuoi(r,"email"), chuoi(r,"chucVu"), chuoi(r,"trangThai")));
    }

    /** Lưu nhân sự, kiểm tra liên hệ và đồng bộ khóa tài khoản khi nhân viên ngừng việc. */
    public void luuNhanVien(int id, String ten, String sdt, String email, String chucVu,
            String trangThai, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên nhân viên.");
        if (trangThai == null || !java.util.Set.of("Đang làm việc", "Tạm nghỉ", "Nghỉ việc").contains(trangThai))
            throw new SQLException("Trạng thái nhân viên không hợp lệ.");

        String tenChuan = ten.trim();
        String sdtChuan = sdt == null ? "" : sdt.trim();
        String emailChuan = email == null ? "" : email.trim();
        String mauSDT = "^(?:03[2-9]|05[25689]|070|07[6-9]|08[1-9]|09[0-4]|09[6-9])[0-9]{7}$";
        if (!sdtChuan.matches(mauSDT))
            throw new SQLException("Số điện thoại nhân viên phải đủ 10 chữ số và có đầu số Việt Nam hợp lệ.");
        if (!EmailHopLe.kiemTra(emailChuan))
            throw new SQLException(EmailHopLe.huongDan());

        try (Connection c = KetNoi.mo()) {
            canQuyen(c, actor, "NHANVIEN");
            c.setAutoCommit(false);
            try {

                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM dbo.NhanVien WITH (UPDLOCK, HOLDLOCK) WHERE sdt=? AND maNV<>?")) {
                    ps.setString(1, sdtChuan);
                    ps.setInt(2, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("Số điện thoại đã tồn tại ở nhân viên khác.");
                    }
                }
                if (id != 0) {
                    String check = "SELECT v.tenVaiTro FROM dbo.NhanVien n "
                            + "JOIN dbo.TaiKhoan t ON n.maTK=t.maTK "
                            + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE n.maNV=?";
                    try(PreparedStatement ps=c.prepareStatement(check)) {
                        ps.setInt(1,id);
                        try(ResultSet r=ps.executeQuery()) {
                            if(r.next()) {
                                String vaiTroTaiKhoan = r.getString(1);
                                if (!vaiTroTaiKhoan.equals(chucVu))
                                    throw new SQLException("Nhân viên đã có tài khoản. Vai trò phải đổi tại trang Tài khoản của Admin.");
                                if ("Quản trị viên".equals(vaiTroTaiKhoan) && !"Đang làm việc".equals(trangThai))
                                    throw new SQLException("Không thể ngừng tài khoản Admin từ màn hình Nhân viên.");
                            }
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
                if (id != 0 && !"Đang làm việc".equals(trangThai)) {

                    try (PreparedStatement lock = c.prepareStatement(
                            "UPDATE t SET t.trangThai=N'Tạm khóa' FROM dbo.TaiKhoan t "
                          + "JOIN dbo.NhanVien n ON n.maTK=t.maTK "
                          + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                          + "WHERE n.maNV=? AND v.tenVaiTro<>N'Quản trị viên'")) {
                        lock.setInt(1, id);
                        lock.executeUpdate();
                    }
                }
                ghi(c, actor, id == 0 ? "Thêm nhân viên" : "Sửa nhân viên", "NhanVien", ten);
                c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Ngừng hoạt động nhân viên; giữ lịch sử và khóa tài khoản liên kết nếu có. */
    public void xoaNhanVien(int id, int actor) throws SQLException {
        xoaAnToan("NHANVIEN", "dbo.NhanVien", "maNV", id, actor, "Nhân viên");
    }

    /** Nạp bảng danh mục dịch vụ và đơn giá đang lưu trong SQL. */
    public List<Hang> dichVu() throws SQLException {
        return truyVan("SELECT maDV,tenDV,CAST(donGia AS DECIMAL(18,0)) AS donGia, "
                + "ISNULL(trangThai,'') AS trangThai,ISNULL(moTa,'') AS moTa "
                + "FROM dbo.DichVu ORDER BY maDV DESC",
                r -> new Hang(r.getInt("maDV"), chuoi(r,"tenDV"),
                        chuoi(r,"donGia"), chuoi(r,"trangThai"), chuoi(r,"moTa")));
    }

    /** Ghi thay đổi dịch vụ; lịch sử bảng giá chỉ cập nhật khi giá thực sự thay đổi. */
    public void luuDichVu(int id, String ten, BigDecimal gia, String trangThai,
            String moTa, int actor) throws SQLException {
        batBuoc(ten, "Thiếu tên dịch vụ.");
        if (trangThai == null || !java.util.Set.of("Đang áp dụng", "Ngừng áp dụng").contains(trangThai))
            throw new SQLException("Trạng thái dịch vụ không hợp lệ.");
        if(gia==null || gia.signum()<0)throw new SQLException("Giá dịch vụ không hợp lệ.");
        try(Connection c=KetNoi.mo()) {
            canQuyen(c,actor,"DICHVU");
            c.setAutoCommit(false);
            try {
                int maDV=id;
                boolean giaThayDoi = id == 0;
                if (id != 0) {
                    try (PreparedStatement old = c.prepareStatement(
                            "SELECT donGia FROM dbo.DichVu WITH (UPDLOCK,HOLDLOCK) WHERE maDV=?")) {
                        old.setInt(1, id);
                        try (ResultSet rs = old.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Không tìm thấy dịch vụ.");
                            giaThayDoi = rs.getBigDecimal(1).compareTo(gia) != 0;
                        }
                    }
                }
                if(id==0) {
                    try(PreparedStatement ps=c.prepareStatement(
                            "INSERT dbo.DichVu(tenDV,donGia,trangThai,moTa) VALUES(?,?,?,?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        datThamSo(ps,ten,gia,trangThai,moTa);
                        ps.executeUpdate();
                        try(ResultSet keys=ps.getGeneratedKeys()) {
                            if(!keys.next())throw new SQLException("Không có mã dịch vụ mới.");
                            maDV=keys.getInt(1);
                        }
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement(
                            "UPDATE dbo.DichVu SET tenDV=?,donGia=?,trangThai=?,moTa=? WHERE maDV=?")) {
                        datThamSo(ps,ten,gia,trangThai,moTa,id);
                        if(ps.executeUpdate()!=1)throw new SQLException("Không tìm thấy dịch vụ.");
                    }
                }
                if (giaThayDoi) {

                    int updated;
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.BangGiaDichVu SET donGia=?,trangThai=? "
                          + "WHERE maDV=? AND ngayApDung=CONVERT(date,GETDATE())")) {
                        datThamSo(ps, gia, trangThai, maDV);
                        updated = ps.executeUpdate();
                    }
                    if (updated == 0) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai) "
                              + "VALUES(?,CONVERT(date,GETDATE()),?,?)")) {
                            datThamSo(ps, maDV, gia, trangThai);
                            ps.executeUpdate();
                        }
                    }
                }
                ghi(c,actor,id==0?"Thêm dịch vụ":"Sửa dịch vụ","DichVu",ten);
                c.commit();
            }catch(SQLException ex){c.rollback();throw ex;}
            finally{c.setAutoCommit(true);}
        }
    }

    /** Ngừng áp dụng dịch vụ thay vì xóa cứng dữ liệu đã phát sinh. */
    public void xoaDichVu(int id, int actor) throws SQLException {
        xoaAnToan("DICHVU", "dbo.DichVu", "maDV", id, actor, "Dịch vụ");
    }

    /** Dùng tập bảng được phép cố định; chỉ cập nhật trạng thái và lưu audit, không DELETE. */
    private void xoaAnToan(String quyen, String bang, String cot, int id,
                           int actor, String doiTuong) throws SQLException {
        final String trangThai;
        switch (bang) {
            case "dbo.BenhNhan": trangThai = "Ngừng hoạt động"; break;
            case "dbo.NhanVien": trangThai = "Nghỉ việc"; break;
            case "dbo.DichVu": trangThai = "Ngừng áp dụng"; break;
            default: throw new SQLException("Không cho phép xóa dữ liệu ngoài danh mục.");
        }
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                canQuyen(c, actor, quyen);

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE " + bang + " SET trangThai=? WHERE " + cot + "=?")) {
                    datThamSo(ps, trangThai, id);
                    if (ps.executeUpdate() != 1) throw new SQLException("Bản ghi không tồn tại.");
                }
                if ("dbo.NhanVien".equals(bang)) {

                    try (PreparedStatement check = c.prepareStatement(
                            "SELECT v.tenVaiTro FROM dbo.NhanVien n "
                          + "JOIN dbo.TaiKhoan t ON n.maTK=t.maTK "
                          + "JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro WHERE n.maNV=?")) {
                        check.setInt(1, id);
                        try (ResultSet rs = check.executeQuery()) {
                            if (rs.next() && "Quản trị viên".equals(rs.getString(1)))
                                throw new SQLException("Không được ngừng tài khoản Admin tại màn hình Nhân viên.");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE t SET t.trangThai=N'Tạm khóa' FROM dbo.TaiKhoan t "
                          + "JOIN dbo.NhanVien n ON n.maTK=t.maTK WHERE n.maNV=?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                ghi(c, actor, "Ngừng hoạt động " + doiTuong, bang, cot + "=" + id);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Lọc các danh mục còn đủ điều kiện để tạo lịch hoặc thanh toán mới. */
    public List<LuaChon> luaChon(String bang) throws SQLException {

        String sql;
        switch (bang) {
            case "BenhNhan": sql = "SELECT maBN,hoTen FROM dbo.BenhNhan "
                    + "WHERE ISNULL(trangThai,N'')<>N'Ngừng hoạt động' ORDER BY hoTen"; break;
            case "NhanVien": sql = "SELECT maNV,hoTen FROM dbo.NhanVien "
                    + "WHERE trangThai=N'Đang làm việc' AND chucVu=N'Bác sĩ' ORDER BY hoTen"; break;
            case "DichVu": sql = "SELECT maDV,tenDV FROM dbo.DichVu WHERE ISNULL(trangThai,N'')<>N'Ngừng áp dụng' ORDER BY tenDV"; break;
            case "PhieuKham": sql = "SELECT p.maPK, N'PK #'+CONVERT(nvarchar(20),p.maPK)+N' - '+b.hoTen "
                    + "FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                    + "WHERE NOT EXISTS (SELECT 1 FROM dbo.ThanhToan t "
                    + "WHERE t.maPK=p.maPK AND t.trangThai<>N'Đã hủy') "
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

    /** Lấy danh sách lịch hẹn kèm tên bệnh nhân và bác sĩ từ SQL. */
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

    /** Lấy tám lịch gần nhất cho các số liệu ở Dashboard. */
    public List<Hang> lichHenGanDay() throws SQLException {
        return truyVan("SELECT TOP 8 l.maLichHen,CONVERT(varchar(10),l.ngayHen,103) AS ngay, "
                + "CONVERT(varchar(5),l.gioHen,108) AS gio,b.hoTen,l.trangThai "
                + "FROM dbo.LichHen l JOIN dbo.BenhNhan b ON l.maBN=b.maBN "
                + "ORDER BY l.ngayHen DESC,l.gioHen DESC",
                r->new Hang(r.getInt(1),chuoi(r,"ngay"),chuoi(r,"gio"),
                        r.getString(4),chuoi(r,"trangThai")));
    }

    /** Kiểm tra bác sĩ, lịch trùng giờ và phòng trước khi thêm lịch trong cùng transaction. */
    public void taoLichHen(int benhNhan, Integer nhanVien, LocalDate ngay, LocalTime gio,
            String phong, String lyDo, int actor) throws SQLException {
        if (benhNhan <= 0 || ngay == null || gio == null) throw new SQLException("Thiếu thông tin lịch hẹn.");
        if (ngay.isBefore(LocalDate.now()) ||
                (ngay.equals(LocalDate.now()) && gio.isBefore(LocalTime.now())))
            throw new SQLException("Không thể đặt lịch tại thời điểm đã qua.");
        try (Connection c = KetNoi.mo()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try {
                canQuyen(c,actor,"LICHHEN");
                // Bệnh nhân đã ngừng hoạt động không thể được đặt lịch mới.
                try (PreparedStatement patient = c.prepareStatement(
                        "SELECT 1 FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maBN=? AND ISNULL(trangThai,N'')<>N'Ngừng hoạt động'")) {
                    patient.setInt(1, benhNhan);
                    try (ResultSet rs = patient.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Bệnh nhân không tồn tại hoặc đã ngừng hoạt động.");
                    }
                }
                // Một bệnh nhân không thể có hai lịch ở cùng thời điểm bắt đầu.
                try (PreparedStatement samePatient = c.prepareStatement(
                        "SELECT 1 FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maBN=? AND ngayHen=? AND gioHen=? "
                      + "AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất',N'Đã khám')")) {
                    datThamSo(samePatient, benhNhan, ngay, gio);
                    try (ResultSet rs = samePatient.executeQuery()) {
                        if (rs.next()) throw new SQLException("Bệnh nhân đã có lịch hẹn tại giờ này.");
                    }
                }
                if (nhanVien != null) {
                    try (PreparedStatement staff = c.prepareStatement(
                            "SELECT 1 FROM dbo.NhanVien WITH (UPDLOCK,HOLDLOCK) "
                          + "WHERE maNV=? AND chucVu=N'Bác sĩ' AND trangThai=N'Đang làm việc'")) {
                        staff.setInt(1, nhanVien);
                        try (ResultSet rs = staff.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Bác sĩ không còn trong trạng thái đang làm việc.");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                            + "WHERE maNV=? AND ngayHen=? AND gioHen=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất',N'Đã khám')")) {
                        datThamSo(ps,nhanVien,ngay,gio);
                        try (ResultSet r = ps.executeQuery()) {
                            if (r.next() && r.getInt(1) > 0) throw new SQLException("Bác sĩ đã có lịch ở giờ này.");
                        }
                    }
                }

                if (phong != null && !phong.isBlank()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT COUNT(*) FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                          + "WHERE phongKham=? AND ngayHen=? AND gioHen=? "
                          + "AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất',N'Đã khám')")) {
                        datThamSo(ps, phong, ngay, gio);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0)
                                throw new SQLException("Phòng khám đã có lịch tại giờ được chọn.");
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT dbo.LichHen(" 
                        + "maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai) VALUES(?,?,?,?,?,?,N'Chờ xác nhận')")) {
                    ps.setInt(1,benhNhan);
                    if (nhanVien == null) ps.setNull(2,java.sql.Types.INTEGER);
                    else ps.setInt(2,nhanVien);
                    datThamSoTail(ps,3,phong,ngay,gio,lyDo);
                    ps.executeUpdate();
                }
                ghi(c,actor,"Tạo lịch hẹn","LichHen","maBN="+benhNhan);
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Gắn tham số JDBC bắt đầu từ vị trí được chỉ định, gồm ngày và giờ. */
    private static void datThamSoTail(PreparedStatement ps, int from, Object... args) throws SQLException {
        for (int i=0;i<args.length;i++) {
            Object arg=args[i];
            if (arg instanceof LocalDate) ps.setDate(i+from,Date.valueOf((LocalDate)arg));
            else if(arg instanceof LocalTime) ps.setTime(i+from,Time.valueOf((LocalTime)arg));
            else ps.setObject(i+from,arg);
        }
    }

    /** Áp dụng state machine; khóa bản ghi và lưu nhật ký nguyên tử khi đổi trạng thái. */
    public void trangThaiLichHen(int id, String status, int actor) throws SQLException {
        if (!java.util.Set.of("Chờ xác nhận", "Đã xác nhận", "Đang khám", "Hoàn tất", "Đã hủy").contains(status))
            throw new SQLException("Trạng thái lịch hẹn không hợp lệ.");
        try (Connection c = KetNoi.mo()) {
            c.setAutoCommit(false);
            try {
                canQuyen(c, actor, "LICHHEN");
                String old;

                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT trangThai, maNV FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) WHERE maLichHen=?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy lịch hẹn.");
                        old = rs.getString(1);
                        if ("Đang khám".equals(status) && rs.getObject(2) == null)
                            throw new SQLException("Phải phân công bác sĩ trước khi bắt đầu khám.");
                    }
                }
                boolean hopLe = ("Chờ xác nhận".equals(old)
                            && ("Đã xác nhận".equals(status) || "Đã hủy".equals(status)))
                        || ("Đã xác nhận".equals(old)
                            && ("Đang khám".equals(status) || "Đã hủy".equals(status)))
                        || ("Đang khám".equals(old) && "Hoàn tất".equals(status));
                if (!hopLe) throw new SQLException(
                        "Không thể chuyển lịch từ '" + old + "' sang '" + status + "'.");
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE dbo.LichHen SET trangThai=? WHERE maLichHen=? AND trangThai=?")) {
                    datThamSo(ps, status, id, old);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Lịch hẹn vừa được thay đổi tại máy khác.");
                }

                // Ghi audit ngay trong transaction: lỗi ghi nhật ký thì trạng thái không đổi.
                ghi(c, actor, "Cập nhật lịch hẹn", "LichHen",
                        "maLichHen=" + id + ", " + old + " -> " + status);
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Lấy mã hóa đơn, mã thanh toán, số tiền và trạng thái để đối soát. */
    public List<Hang> hoaDon() throws SQLException {

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

    /** Xác định mã giao dịch từ mã hóa đơn thật và chống gửi đơn hủy nhầm. */
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
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT h.maTT, h.trangThai FROM dbo.HoaDon h WITH (UPDLOCK, HOLDLOCK) "
                      + "WHERE h.maHD=?")) {
                    ps.setInt(1, maHD);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy hóa đơn #" + maHD);
                        if ("Đã hủy".equalsIgnoreCase(rs.getString("trangThai"))) {
                            throw new SQLException("Hóa đơn #" + maHD + " đã hủy.");
                        }
                        maTT = rs.getInt("maTT");
                    }
                }

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

    /** Tính tổng các chi tiết điều trị của một phiếu khám. */
    public BigDecimal soTienPhieuKham(int maPK) throws SQLException {
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(
                "SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WHERE maPK=?")) {
            ps.setInt(1,maPK);
            try (ResultSet r = ps.executeQuery()) { return r.next()?r.getBigDecimal(1):BigDecimal.ZERO; }
        }
    }

    /** Chốt đúng một lần thu cho mỗi phiếu khám; mọi bản ghi và audit cùng commit hoặc rollback. */
    public void taoThanhToan(int maPK, String phuongThuc, int actor) throws SQLException {
        if (phuongThuc == null || !java.util.Set.of("Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng").contains(phuongThuc))
            throw new SQLException("Phương thức thanh toán không hợp lệ.");
        try (Connection c = KetNoi.mo()) {
            c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            c.setAutoCommit(false);
            try {
                canQuyen(c,actor,"THANHTOAN");
                // Khóa phiếu khám để hai máy không thể đồng thời tạo thu tiền của cùng một phiếu.
                try (PreparedStatement lock = c.prepareStatement(
                        "SELECT 1 FROM dbo.PhieuKham WITH (UPDLOCK,HOLDLOCK) WHERE maPK=?")) {
                    lock.setInt(1,maPK);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy phiếu khám.");
                    }
                }
                BigDecimal soTien;
                try (PreparedStatement ps=c.prepareStatement("SELECT COALESCE(SUM(thanhTien),0) FROM dbo.ChiTietDieuTri WHERE maPK=?")) {
                    ps.setInt(1,maPK);
                    try(ResultSet rs=ps.executeQuery()) { if(!rs.next()) throw new SQLException("Không có phiếu khám."); soTien=rs.getBigDecimal(1); }
                }
                if(soTien.signum()<=0) throw new SQLException("Phiếu khám chưa có chi tiết điều trị hoặc số tiền bằng 0.");
                // Phần mềm hiện chỉ thu trọn một lần: kể cả phiếu Chờ xử lý cũng không được nhân đôi.
                try (PreparedStatement ps=c.prepareStatement(
                        "SELECT 1 FROM dbo.ThanhToan WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maPK=? AND trangThai<>N'Đã hủy'")) {
                    ps.setInt(1,maPK);
                    try(ResultSet rs=ps.executeQuery()) {
                        if(rs.next()) throw new SQLException("Phiếu khám đã có thanh toán hoặc giao dịch đang chờ xử lý.");
                    }
                }
                int maTT;
                try (PreparedStatement ps=c.prepareStatement("INSERT dbo.ThanhToan(maPK,soTien,phuongThuc,trangThai) "
                        + "VALUES(?,?,?,N'Đã thanh toán')",Statement.RETURN_GENERATED_KEYS)) {
                    datThamSo(ps,maPK,soTien,phuongThuc);
                    ps.executeUpdate();
                    try(ResultSet k=ps.getGeneratedKeys()) { if(!k.next())throw new SQLException("Không lấy được mã thanh toán.");maTT=k.getInt(1); }
                }
                try (PreparedStatement ps=c.prepareStatement("INSERT dbo.HoaDon(maTT,maPK,tongTien,trangThai) VALUES(?,?,?,N'Đã thanh toán')")) {
                    datThamSo(ps,maTT,maPK,soTien);
                    ps.executeUpdate();
                }
                ghi(c,actor,"Thanh toán","ThanhToan","maPK="+maPK+", maTT="+maTT);
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Kiểm tra lý do, trạng thái và tạo yêu cầu hủy giao dịch trong một transaction. */
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

    /** Tạo bản ghi nghiệp vụ của lớp; kết quả được chuyển cho nơi gọi. */
    private static void taoYeuCauHuyTrongGiaoDich(
            Connection c, int maTT, String lyDo, int actor) throws SQLException {

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
                if ("Đã hủy".equalsIgnoreCase(rs.getString(1))) {
                    throw new SQLException("Thanh toán đã hủy, không thể gửi lại yêu cầu.");
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

    /** Cập nhật thông tin của lớp; kết quả được chuyển cho nơi gọi. */
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

    /** Kiểm tra điều kiện của lớp; kết quả được chuyển cho nơi gọi. */
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

    /** Kiểm tra điều kiện của lớp; kết quả được chuyển cho nơi gọi. */
    public boolean coQuyenPhanHoiHoTro(int actor) throws SQLException {
        try (Connection c = KetNoi.mo()) {
            return capBacHoTro(c, actor) >= 2 && coQuyen(c, actor, "HOTRO");
        }
    }

    /** Truy vấn danh sách của lớp; kết quả được chuyển cho nơi gọi. */
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

    /** Khóa yêu cầu chờ duyệt và đồng bộ chứng từ khi Admin duyệt hoặc từ chối. */
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
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT maTT FROM dbo.YeuCauHuyPhieuThu WITH (UPDLOCK, HOLDLOCK) "
                      + "WHERE maYC=? AND trangThai=N'Chờ duyệt'")) {
                    ps.setInt(1, maYC);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Yêu cầu không còn ở trạng thái Chờ duyệt.");
                        }
                        maTT = rs.getInt(1);
                    }
                }

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
                    ps.executeUpdate();
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
                            "UPDATE dbo.ThanhToan SET trangThai=N'Đã hủy' WHERE maTT=?")) {
                        ps.setInt(1, maTT);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("Thanh toán không tồn tại, không thể duyệt.");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE dbo.HoaDon SET trangThai=N'Đã hủy' WHERE maTT=?")) {
                        ps.setInt(1, maTT);
                        ps.executeUpdate();
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

    /** Kiểm tra người thao tác có phải tài khoản Quản trị viên đang hoạt động không. */
    private boolean laAdmin(Connection c,int actor) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' AND v.tenVaiTro=N'Quản trị viên'")) {
            ps.setInt(1,actor);
            try(ResultSet r=ps.executeQuery()) { return r.next(); }
        }
    }

    /** Gửi hỗ trợ đồng thời ghi nhật ký cùng transaction, không lưu nửa chừng. */
    public void guiHoTro(int actor,String tieuDe,String noiDung) throws SQLException {
        batBuoc(tieuDe,"Nhập tiêu đề yêu cầu."); batBuoc(noiDung,"Nhập nội dung cần hỗ trợ.");
        try(Connection c=KetNoi.mo()) {
            try(PreparedStatement valid=c.prepareStatement(
                    "SELECT 1 FROM dbo.TaiKhoan WHERE maTK=? AND trangThai=N'Hoạt động'")) {
                valid.setInt(1,actor);
                try(ResultSet r=valid.executeQuery()) {
                    if(!r.next()) throw new SQLException("Tài khoản đã bị khóa hoặc đăng xuất.");
                }
            }
            c.setAutoCommit(false);
            try {
                try(PreparedStatement ps=c.prepareStatement("INSERT dbo.YeuCauHoTro(maTK,tieuDe,noiDung) VALUES(?,?,?)")) {
                    datThamSo(ps,actor,tieuDe,noiDung);ps.executeUpdate();
                }
                ghi(c,actor,"Gửi yêu cầu hỗ trợ","YeuCauHoTro",tieuDe);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    /** Truy vấn danh sách của lớp; kết quả được chuyển cho nơi gọi. */
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

    /** Kiểm soát quyền hỗ trợ và ghi phản hồi có nhật ký trong transaction. */
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
