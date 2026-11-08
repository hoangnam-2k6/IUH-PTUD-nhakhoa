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

    private static trangthai_NhanVien docTrangThai(String value) {
        if (value == null) return null;
        if ("Đang làm việc".equalsIgnoreCase(value)) return trangthai_NhanVien.DANGLAMVIEC;
        if ("Tạm nghỉ".equalsIgnoreCase(value)) return trangthai_NhanVien.TAMNGHI;
        if ("Ngừng làm việc".equalsIgnoreCase(value)) return trangthai_NhanVien.NGHIVIEC;
        return trangthai_NhanVien.valueOf(value);
    }
    private static String luuTrangThai(trangthai_NhanVien value) {
        return value == trangthai_NhanVien.DANGLAMVIEC ? "Đang làm việc" :
               value == trangthai_NhanVien.TAMNGHI ? "Tạm nghỉ" : "Ngừng làm việc";
    }

    private static gioitinh docGioiTinh(String value) {
        if (value == null || value.isBlank()) return null;
        if ("Nam".equalsIgnoreCase(value)) return gioitinh.NAM;
        if ("Nữ".equalsIgnoreCase(value) || "Nu".equalsIgnoreCase(value)) return gioitinh.NU;
        if ("Khác".equalsIgnoreCase(value) || "Khac".equalsIgnoreCase(value)) return gioitinh.KHAC;
        return gioitinh.valueOf(value.trim());
    }

    // 1. Lấy tất cả nhân viên
    public ArrayList<NhanVien> getallnhanvien() {

        ArrayList<NhanVien> dsnv = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection()) {

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
                        ? docGioiTinh(gt1)
                        : null;

                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String diachi = rs.getString("diachi");
                String chucvu = rs.getString("chucvu");

                String ttnv = rs.getString("trangthai");
                trangthai_NhanVien trangthai = (ttnv != null)
                        ? docTrangThai(ttnv)
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
            throw new IllegalStateException("Lỗi truy vấn "+"nhân viên"+": " + e.getMessage(), e);
        }

        return dsnv;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn nhân viên thất bại: " + e.getMessage(), e);
        }
}

    // 2. Thêm nhân viên
    public boolean create(NhanVien nv) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "INSERT INTO NhanVien "
                   + "(hoten, ngaysinh, gioitinh, sdt, "
                   + "email, diachi, chucvu, trangthai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

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
                stmt.setString(8, luuTrangThai(nv.getTrangthai()));
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }


            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn nhân viên thất bại: " + e.getMessage(), e);
        }
}

    // 3. Cập nhật nhân viên
    public boolean update(NhanVien nv) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "UPDATE NhanVien SET "
                   + "hoten = ?, "
                   + "ngaysinh = ?, "
                   + "gioitinh = ?, "
                   + "sdt = ?, "
                   + "email = ?, "
                   + "diachi = ?, "
                   + "chucvu = ?, "
                   + "trangthai = ? "
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
                stmt.setString(8, luuTrangThai(nv.getTrangthai()));
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }

            stmt.setString(9, nv.getMaNV());

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn nhân viên thất bại: " + e.getMessage(), e);
        }
}

    // 4. Xóa nhân viên
    public boolean delete(String maNV) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "DELETE FROM NhanVien WHERE maNV = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, maNV);

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn nhân viên thất bại: " + e.getMessage(), e);
        }
}

	public ArrayList<NhanVien> timNhanVien(
	        String tuKhoa,
	        gioitinh gt,
	        String chucVu,
	        trangthai_NhanVien tt) {
	
	    ArrayList<NhanVien> dsnv = new ArrayList<>();
	
		    try (Connection con = ConnectDB.getConnection()) {
	
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
	            stmt.setString(i++, luuTrangThai(tt));
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
	                        ? docGioiTinh(gt1) : null;
	
	                String sdt = rs.getString("sdt");
	                String email = rs.getString("email");
	                String diachi = rs.getString("diachi");
	                String chucvu = rs.getString("chucvu");
	
	                String tt1 = rs.getString("trangthai");
	                trangthai_NhanVien trangthai = tt1 != null
	                        ? docTrangThai(tt1) : null;
	
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
	
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn nhân viên thất bại: " + e.getMessage(), e);
        }
}
	// 2. Lấy một nhân viên theo mã nhân viên
	public NhanVien getNhanVienByMaNV(String maNV) throws SQLException {

	
	    try (Connection con = ConnectDB.getConnection()) {

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
	                        ? docGioiTinh(gt1)
	                        : null;

	                String sdt = rs.getString("sdt");
	                String email = rs.getString("email");
	                String diachi = rs.getString("diachi");
	                String chucvu = rs.getString("chucvu");

	                // Lấy trạng thái
	                String ttnv = rs.getString("trangthai");

	                trangthai_NhanVien trangthai =
	                        (ttnv != null && !ttnv.isBlank())
	                        ? docTrangThai(ttnv)
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
	
        } // đóng handle JDBC sau mỗi thao tác
}
}