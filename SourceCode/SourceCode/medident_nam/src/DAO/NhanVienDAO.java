package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import enums.gioitinh;
import enums.trangthai_NhanVien;

public class NhanVienDAO {

    ArrayList<NhanVien> dsnv;
    NhanVien nv;

    public NhanVienDAO() {
        dsnv = new ArrayList<>();
        nv = new NhanVien();
    }

    // 1. Lấy tất cả nhân viên
    public ArrayList<NhanVien> getallnhanvien() throws SQLException {

        dsnv = new ArrayList<>();

        ConnectDB.mo();
        Connection con = ConnectDB.mo();

        String sql = "SELECT maNV, hoten, ngaysinh, gioitinh, "
                   + "sdt, email, diachi, chucvu, trangthai, maTK "
                   + "FROM NhanVien";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String maNV = rs.getString("maNV");
                String hoten = rs.getString("hoten");

                Date ngaySQL = rs.getDate("ngaysinh");
                LocalDate ngaysinh = (ngaySQL != null)
                        ? ngaySQL.toLocalDate()
                        : null;

                String gt1 = rs.getString("gioitinh");
                gioitinh gt2 = (gt1 != null)
                        ? gioitinh.valueOf(gt1)
                        : null;

                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diachi = rs.getString("diachi");
                String chucvu = rs.getString("chucvu");

                String ttnv = rs.getString("trangthai");
                trangthai_NhanVien trangthai = (ttnv != null)
                        ? trangthai_NhanVien.valueOf(ttnv)
                        : null;

                String maTK = rs.getString("maTK");

                NhanVien s = new NhanVien(
                    maNV, hoten, ngaysinh, gt2,
                    sdt, email, diachi, chucvu,
                    trangthai, maTK
                );

                dsnv.add(s);
            }

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return dsnv;
    }

    // 2. Thêm nhân viên
    public boolean create(NhanVien nv) throws SQLException {

        ConnectDB.mo();
        Connection con = ConnectDB.mo();

        int n = 0;

        String sql = "INSERT INTO NhanVien "
                   + "(maNV, hoten, ngaysinh, gioitinh, sdt, "
                   + "email, diachi, chucvu, trangthai, maTK) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, nv.getMaNV());
            stmt.setString(2, nv.getHoten());

            if (nv.getNgaysinh() != null) {
                stmt.setDate(3, Date.valueOf(nv.getNgaysinh()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (nv.getGioitinh() != null) {
                stmt.setString(4, nv.getGioitinh().name());
            } else {
                stmt.setNull(4, Types.NVARCHAR);
            }

            stmt.setString(5, nv.getSdt());
            stmt.setString(6, nv.getEmail());
            stmt.setString(7, nv.getDiachi());
            stmt.setString(8, nv.getChucvu());

            if (nv.getTrangthai() != null) {
                stmt.setString(9, nv.getTrangthai().name());
            } else {
                stmt.setNull(9, Types.NVARCHAR);
            }

            stmt.setString(10, nv.getMaTK());

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    // 3. Cập nhật nhân viên
    public boolean update(NhanVien nv) throws SQLException {

        ConnectDB.mo();
        Connection con = ConnectDB.mo();

        int n = 0;

        String sql = "UPDATE NhanVien SET "
                   + "hoten = ?, "
                   + "ngaysinh = ?, "
                   + "gioitinh = ?, "
                   + "sdt = ?, "
                   + "email = ?, "
                   + "diachi = ?, "
                   + "chucvu = ?, "
                   + "trangthai = ?, "
                   + "maTK = ? "
                   + "WHERE maNV = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, nv.getHoten());

            if (nv.getNgaysinh() != null) {
                stmt.setDate(2, Date.valueOf(nv.getNgaysinh()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (nv.getGioitinh() != null) {
                stmt.setString(3, nv.getGioitinh().name());
            } else {
                stmt.setNull(3, Types.NVARCHAR);
            }

            stmt.setString(4, nv.getSdt());
            stmt.setString(5, nv.getEmail());
            stmt.setString(6, nv.getDiachi());
            stmt.setString(7, nv.getChucvu());

            if (nv.getTrangthai() != null) {
                stmt.setString(8, nv.getTrangthai().name());
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }

            stmt.setString(9, nv.getMaTK());
            stmt.setString(10, nv.getMaNV());

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    // 4. Xóa nhân viên
    public boolean delete(String maNV) throws SQLException {

        ConnectDB.mo();
        Connection con = ConnectDB.mo();

        int n = 0;

        String sql = "DELETE FROM NhanVien WHERE maNV = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, maNV);

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

	public ArrayList<NhanVien> timNhanVien(
	        String tuKhoa,
	        gioitinh gt,
	        String chucVu,
	        trangthai_NhanVien tt) throws SQLException {
	
	    ArrayList<NhanVien> dsnv = new ArrayList<>();
	
	    ConnectDB.mo();
	    Connection con = ConnectDB.mo();
	
	    StringBuilder sql = new StringBuilder(
	        "SELECT maNV, hoten, ngaysinh, gioitinh, "
	      + "sdt, email, diachi, chucvu, trangthai, maTK "
	      + "FROM NhanVien WHERE 1=1"
	    );
	
	    // Lọc theo mã, họ tên, số điện thoại hoặc email
	    if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
	        sql.append(" AND (CAST(maNV AS NVARCHAR(30)) LIKE ? "
	                 + "OR hoten LIKE ? OR sdt LIKE ? OR email LIKE ?)");
	    }
	
	    // Lọc theo giới tính
	    if (gt != null) {
	        sql.append(" AND gioitinh = ?");
	    }
	
	    // Lọc theo chức vụ
	    if (chucVu != null && !chucVu.trim().isEmpty()) {
	        sql.append(" AND chucvu = ?");
	    }
	
	    // Lọc theo trạng thái nhân viên
	    if (tt != null) {
	        sql.append(" AND trangthai = ?");
	    }
	
	    try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
	
	        int i = 1;
	
	        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
	            String key = "%" + tuKhoa.trim() + "%";
	
	            stmt.setString(i++, key);
	            stmt.setString(i++, key);
	            stmt.setString(i++, key);
	            stmt.setString(i++, key);
	        }
	
	        if (gt != null) {
	            stmt.setString(i++, gt.name());
	        }
	
	        if (chucVu != null && !chucVu.trim().isEmpty()) {
	            stmt.setString(i++, chucVu.trim());
	        }
	
	        if (tt != null) {
	            stmt.setString(i++, tt.name());
	        }
	
	        try (ResultSet rs = stmt.executeQuery()) {
	
	            while (rs.next()) {
	
	                String maNV = rs.getString("maNV");
	                String hoten = rs.getString("hoten");
	
	                Date ngaySQL = rs.getDate("ngaysinh");
	                LocalDate ngaysinh = ngaySQL != null
	                        ? ngaySQL.toLocalDate() : null;
	
	                String gt1 = rs.getString("gioitinh");
	                gioitinh gt2 = gt1 != null
	                        ? gioitinh.valueOf(gt1) : null;
	
	                String sdt = rs.getString("sdt");
	                String email = rs.getString("email");
	                String diachi = rs.getString("diachi");
	                String chucvu = rs.getString("chucvu");
	
	                String tt1 = rs.getString("trangthai");
	                trangthai_NhanVien trangthai = tt1 != null
	                        ? trangthai_NhanVien.valueOf(tt1) : null;
	
	                String maTK = rs.getString("maTK");
	
	                NhanVien nv = new NhanVien(
	                    maNV, hoten, ngaysinh, gt2,
	                    sdt, email, diachi, chucvu,
	                    trangthai, maTK
	                );
	
	                dsnv.add(nv);
	            }
	        }
	
	    } catch (SQLException | IllegalArgumentException e) {
	        e.printStackTrace();
	    }
	    return dsnv;
	}
	// 2. Lấy một nhân viên theo mã nhân viên
	public NhanVien getNhanVienByMaNV(String maNV) throws SQLException {

	    ConnectDB.mo();

	    Connection con = ConnectDB.mo();

	    if (con == null) {
	        throw new SQLException("Chưa kết nối SQL Server!");
	    }

	    String sql = "SELECT maNV, hoten, ngaysinh, gioitinh, "
	               + "sdt, email, diachi, chucvu, trangthai, maTK "
	               + "FROM NhanVien WHERE maNV = ?";

	    try (PreparedStatement stmt = con.prepareStatement(sql)) {

	        stmt.setString(1, maNV);

	        try (ResultSet rs = stmt.executeQuery()) {

	            if (rs.next()) {

	                String ma = rs.getString("maNV");
	                String hoten = rs.getString("hoten");

	                // Chuyển SQL Date thành LocalDate
	                Date ngaySQL = rs.getDate("ngaysinh");

	                LocalDate ngaysinh = (ngaySQL != null)
	                        ? ngaySQL.toLocalDate()
	                        : null;

	                // Lấy giới tính
	                String gt1 = rs.getString("gioitinh");

	                gioitinh gt2 = (gt1 != null && !gt1.isBlank())
	                        ? gioitinh.valueOf(gt1.trim())
	                        : null;

	                String sdt = rs.getString("sdt");
	                String email = rs.getString("email");
	                String diachi = rs.getString("diachi");
	                String chucvu = rs.getString("chucvu");

	                // Lấy trạng thái
	                String ttnv = rs.getString("trangthai");

	                trangthai_NhanVien trangthai =
	                        (ttnv != null && !ttnv.isBlank())
	                        ? trangthai_NhanVien.valueOf(ttnv.trim())
	                        : null;

	                String maTK = rs.getString("maTK");

	                // Tạo đối tượng NhanVien
	                return new NhanVien(
	                    ma,
	                    hoten,
	                    ngaysinh,
	                    gt2,
	                    sdt,
	                    email,
	                    diachi,
	                    chucvu,
	                    trangthai,
	                    maTK
	                );
	            }
	        }

	    }

	    return null;
	}
}