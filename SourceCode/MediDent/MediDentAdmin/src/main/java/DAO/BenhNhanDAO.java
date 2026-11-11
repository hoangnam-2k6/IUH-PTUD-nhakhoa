
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
import Entity.BenhNhan;
import enums.gioitinh;
import enums.trangthaidieutri;

public class BenhNhanDAO {

    ArrayList<BenhNhan> dsbn;
    BenhNhan bn;

    public BenhNhanDAO() {
        dsbn = new ArrayList<BenhNhan>();
        bn = new BenhNhan();
    }

    private static gioitinh docGioiTinh(String value) {
        if (value == null || value.isBlank()) return null;
        if ("Nam".equalsIgnoreCase(value)) return gioitinh.NAM;
        if ("Nữ".equalsIgnoreCase(value) || "Nu".equalsIgnoreCase(value)) return gioitinh.NU;
        if ("Khác".equalsIgnoreCase(value) || "Khac".equalsIgnoreCase(value)) return gioitinh.KHAC;
        return gioitinh.valueOf(value.trim());
    }

    // 1. Lấy tất cả bệnh nhân
    public ArrayList<BenhNhan> layTatCaBenhnhan() {

        ArrayList<BenhNhan> dsbn = new ArrayList<BenhNhan>();

        try (Connection con = ConnectDB.layConnection()) {

        String sql = "SELECT maBN, hoten, ngaysinh, gioitinh, "
                   + "sdt, email, diachi, tiensubenh, trangthai "
                   + "FROM BenhNhan";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String maBN = rs.getString("maBN");
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
                String tiensubenh = rs.getString("tiensubenh");

                String ttdt = rs.getString("trangthai");

                trangthaidieutri trangthai = (ttdt != null)
                        ? trangthaidieutri.valueOf(ttdt)
                        : null;

                BenhNhan s = new BenhNhan(
                    maBN, hoten, ngaysinh, gt2,
                    sdt, email, diachi, tiensubenh, trangthai
                );

                dsbn.add(s);
            }

        } catch (SQLException | IllegalArgumentException e) {
            throw new IllegalStateException("Lỗi truy vấn "+"bệnh nhân"+": " + e.getMessage(), e);
        }

        return dsbn;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn bệnh nhân thất bại: " + e.getMessage(), e);
        }
}

    // 2. Thêm bệnh nhân
    public boolean themMoi(BenhNhan bn) {

        try (Connection con = ConnectDB.layConnection()) {

        int n = 0;

        String sql = "INSERT INTO BenhNhan "
                   + "(hoten, ngaysinh, gioitinh, sdt, "
                   + "email, diachi, tiensubenh, trangthai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, bn.layHoten());

            if (bn.layNgaysinh() != null) {
                stmt.setDate(2, Date.valueOf(bn.layNgaysinh()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (bn.layGioitinh() != null) {
                stmt.setString(3, bn.layGioitinh().name());
            } else {
                stmt.setNull(3, Types.NVARCHAR);
            }

            stmt.setString(4, bn.laySdt());
            stmt.setString(5, bn.layEmail());
            stmt.setString(6, bn.layDiachi());
            stmt.setString(7, bn.layTiensubenh());

            if (bn.layTrangthai() != null) {
                stmt.setString(8, bn.layTrangthai().name());
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn bệnh nhân thất bại: " + e.getMessage(), e);
        }
}

    // 3. Cập nhật bệnh nhân
    public boolean capNhat(BenhNhan bn) {

        try (Connection con = ConnectDB.layConnection()) {

        int n = 0;

        String sql = "UPDATE BenhNhan SET "
                   + "hoten = ?, "
                   + "ngaysinh = ?, "
                   + "gioitinh = ?, "
                   + "sdt = ?, "
                   + "email = ?, "
                   + "diachi = ?, "
                   + "tiensubenh = ?, "
                   + "trangthai = ? "
                   + "WHERE maBN = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, bn.layHoten());

            if (bn.layNgaysinh() != null) {
                stmt.setDate(2, Date.valueOf(bn.layNgaysinh()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (bn.layGioitinh() != null) {
                stmt.setString(3, bn.layGioitinh().name());
            } else {
                stmt.setNull(3, Types.NVARCHAR);
            }

            stmt.setString(4, bn.laySdt());
            stmt.setString(5, bn.layEmail());
            stmt.setString(6, bn.layDiachi());
            stmt.setString(7, bn.layTiensubenh());

            if (bn.layTrangthai() != null) {
                stmt.setString(8, bn.layTrangthai().name());
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }

            stmt.setString(9, bn.layMaBN());

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn bệnh nhân thất bại: " + e.getMessage(), e);
        }
}

    // 4. Xóa bệnh nhân
    public boolean xoa(String maBN) {

        try (Connection con = ConnectDB.layConnection()) {

        int n = 0;

        String sql = "DELETE FROM BenhNhan WHERE maBN = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, maBN);

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn bệnh nhân thất bại: " + e.getMessage(), e);
        }
}

	public ArrayList<BenhNhan> timBenhNhan(
	        String tuKhoa,
	        gioitinh gt,
	        trangthaidieutri tt) {
	
	    ArrayList<BenhNhan> dsbn = new ArrayList<>();
	
		    try (Connection con = ConnectDB.layConnection()) {
	
	    StringBuilder sql = new StringBuilder(
	        "SELECT maBN, hoten, ngaysinh, gioitinh, "
	      + "sdt, email, diachi, tiensubenh, trangthai "
	      + "FROM BenhNhan WHERE 1=1"
	    );
	
	    // Lọc theo mã, họ tên, số điện thoại hoặc email
	    if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
	        sql.append(" AND (CAST(maBN AS NVARCHAR(30)) LIKE ? "
	                 + "OR hoten LIKE ? OR sdt LIKE ? OR email LIKE ?)");
	    }
	
	    // Lọc theo giới tính
	    if (gt != null) {
	        sql.append(" AND gioitinh = ?");
	    }
	
	    // Lọc theo trạng thái điều trị
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
	
	        if (tt != null) {
	            stmt.setString(i++, tt.name());
	        }
	
	        try (ResultSet rs = stmt.executeQuery()) {
	
	            while (rs.next()) {
	
	                String maBN = rs.getString("maBN");
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
	                String tiensubenh = rs.getString("tiensubenh");
	
	                String tt1 = rs.getString("trangthai");
	                trangthaidieutri trangthai = tt1 != null
	                        ? trangthaidieutri.valueOf(tt1) : null;
	
	                BenhNhan bn = new BenhNhan(
	                    maBN, hoten, ngaysinh, gt2,
	                    sdt, email, diachi, tiensubenh, trangthai
	                );
	
	                dsbn.add(bn);
	            }
	        }
	
	    } catch (SQLException | IllegalArgumentException e) {
	        e.printStackTrace();
	    }
	
	    return dsbn;
	
        } catch (SQLException e) {
            throw new IllegalStateException("Truy vấn bệnh nhân thất bại: " + e.getMessage(), e);
        }
}
	// Lấy một bệnh nhân theo mã
	public BenhNhan layBenhNhanByMaBN(String maBN) throws SQLException {

	
	    try (Connection con = ConnectDB.layConnection()) {

	    if (con == null) {
	        throw new SQLException("Chưa kết nối SQL Server!");
	    }

	    String sql = "SELECT maBN, hoten, ngaysinh, gioitinh, "
	               + "sdt, email, diachi, tiensubenh, trangthai "
	               + "FROM BenhNhan WHERE maBN = ?";

	    try (PreparedStatement stmt = con.prepareStatement(sql)) {

	        stmt.setString(1, maBN);

	        try (ResultSet rs = stmt.executeQuery()) {

	            if (rs.next()) {

	                String ma = rs.getString("maBN");
	                String hoten = rs.getString("hoten");

	                // Chuyển SQL Date thành LocalDate
	                Date ngaySQL = rs.getDate("ngaysinh");

	                LocalDate ngaysinh = (ngaySQL != null)
	                        ? ngaySQL.toLocalDate()
	                        : null;

	                // Lấy giới tính
	                String gt1 = rs.getString("gioitinh");

	                gioitinh gt = (gt1 != null && !gt1.isBlank())
	                        ? docGioiTinh(gt1)
	                        : null;

	                String sdt = rs.getString("sdt");
	                String email = rs.getString("email");
	                String diachi = rs.getString("diachi");
	                String tiensubenh = rs.getString("tiensubenh");

	                // Lấy trạng thái điều trị
	                String tt1 = rs.getString("trangthai");

	                trangthaidieutri tt =
	                        (tt1 != null && !tt1.isBlank())
	                        ? trangthaidieutri.valueOf(tt1.trim())
	                        : null;

	                return new BenhNhan(
	                    ma,
	                    hoten,
	                    ngaysinh,
	                    gt,
	                    sdt,
	                    email,
	                    diachi,
	                    tiensubenh,
	                    tt
	                );
	            }
	        }
	    }
	    return null;
	
        } // đóng handle JDBC sau mỗi thao tác
}

    /** Mã dự kiến để hiển thị form; ID chính thức do SQL Server IDENTITY cấp. */
    public String layMaBenhNhanTiepTheo() throws java.sql.SQLException {
        try (java.sql.Connection c=ConnectDB.taoKetNoiMoi();
             java.sql.PreparedStatement q=c.prepareStatement("SELECT ISNULL(MAX(MaBN),0)+1 FROM dbo.BenhNhan");
             java.sql.ResultSet r=q.executeQuery()) {
            return "BN" + String.format("%06d",r.next()?r.getInt(1):1);
        }
    }
}
