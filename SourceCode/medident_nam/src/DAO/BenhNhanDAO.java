
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

    // 1. Lấy tất cả bệnh nhân
    public ArrayList<BenhNhan> getallbenhnhan() {

        dsbn = new ArrayList<BenhNhan>();

        ConnectDB.GetInstance();
        Connection con = ConnectDB.getConnection();

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
                        ? gioitinh.valueOf(gt1)
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
            e.printStackTrace();
        }

        return dsbn;
    }

    // 2. Thêm bệnh nhân
    public boolean create(BenhNhan bn) {

        ConnectDB.GetInstance();
        Connection con = ConnectDB.getConnection();

        int n = 0;

        String sql = "INSERT INTO BenhNhan "
                   + "(maBN, hoten, ngaysinh, gioitinh, sdt, "
                   + "email, diachi, tiensubenh, trangthai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, bn.getMaBN());
            stmt.setString(2, bn.getHoten());

            if (bn.getNgaysinh() != null) {
                stmt.setDate(3, Date.valueOf(bn.getNgaysinh()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (bn.getGioitinh() != null) {
                stmt.setString(4, bn.getGioitinh().name());
            } else {
                stmt.setNull(4, Types.NVARCHAR);
            }

            stmt.setString(5, bn.getSdt());
            stmt.setString(6, bn.getEmail());
            stmt.setString(7, bn.getDiachi());
            stmt.setString(8, bn.getTiensubenh());

            if (bn.getTrangthai() != null) {
                stmt.setString(9, bn.getTrangthai().name());
            } else {
                stmt.setNull(9, Types.NVARCHAR);
            }

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    // 3. Cập nhật bệnh nhân
    public boolean update(BenhNhan bn) {

        ConnectDB.GetInstance();
        Connection con = ConnectDB.getConnection();

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

            stmt.setString(1, bn.getHoten());

            if (bn.getNgaysinh() != null) {
                stmt.setDate(2, Date.valueOf(bn.getNgaysinh()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (bn.getGioitinh() != null) {
                stmt.setString(3, bn.getGioitinh().name());
            } else {
                stmt.setNull(3, Types.NVARCHAR);
            }

            stmt.setString(4, bn.getSdt());
            stmt.setString(5, bn.getEmail());
            stmt.setString(6, bn.getDiachi());
            stmt.setString(7, bn.getTiensubenh());

            if (bn.getTrangthai() != null) {
                stmt.setString(8, bn.getTrangthai().name());
            } else {
                stmt.setNull(8, Types.NVARCHAR);
            }

            stmt.setString(9, bn.getMaBN());

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    // 4. Xóa bệnh nhân
    public boolean delete(String maBN) {

        ConnectDB.GetInstance();
        Connection con = ConnectDB.getConnection();

        int n = 0;

        String sql = "DELETE FROM BenhNhan WHERE maBN = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, maBN);

            n = stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

	public ArrayList<BenhNhan> timBenhNhan(
	        String tuKhoa,
	        gioitinh gt,
	        trangthaidieutri tt) {
	
	    ArrayList<BenhNhan> dsbn = new ArrayList<>();
	
	    ConnectDB.GetInstance();
	    Connection con = ConnectDB.getConnection();
	
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
	                        ? gioitinh.valueOf(gt1) : null;
	
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
	}
	// Lấy một bệnh nhân theo mã
	public BenhNhan getBenhNhanByMaBN(String maBN) throws SQLException {

	    ConnectDB.GetInstance();

	    Connection con = ConnectDB.getConnection();

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
	                        ? gioitinh.valueOf(gt1.trim())
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
	}
}
