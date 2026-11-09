package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.math.BigDecimal;
import java.util.ArrayList;

import ConnectDB.ConnectDB;
import Entity.DichVu;
import enums.trangthai_dichvu;

public class DichVuDAO {

    ArrayList<DichVu> dsdv;
    DichVu dv;

    public DichVuDAO() {
        dsdv = new ArrayList<>();
        dv = new DichVu();
    }

    // 1. Lấy tất cả dịch vụ
    public ArrayList<DichVu> getalldichvu() {

        ArrayList<DichVu> dsdv = new ArrayList<>();

        try (Connection con = ConnectDB.getConnection()) {

        String sql = "SELECT maDV, tenDV, moTa, donGia, trangThai "
                   + "FROM DichVu";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String maDV = rs.getString("maDV");
                String tenDV = rs.getString("tenDV");
                String mota = rs.getString("moTa");
                BigDecimal dongia = rs.getBigDecimal("donGia");

                String tt1 = rs.getString("trangThai");

                trangthai_dichvu tt2 = (tt1 != null)
                        ? trangthai_dichvu.valueOf(tt1)
                        : null;

                DichVu s = new DichVu(
                    maDV, tenDV, mota, dongia, tt2
                );

                dsdv.add(s);
            }

        } catch (SQLException | IllegalArgumentException e) {
            throw new IllegalStateException("Không đọc được bảng DichVu: "+e.getMessage(),e);
        }

        return dsdv;
    
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}

    // 2. Thêm dịch vụ
    public boolean create(DichVu dv) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "INSERT INTO DichVu "
                   + "(tenDV, moTa, donGia, trangThai) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, dv.getTenDV());
            stmt.setString(2, dv.getMota());
            stmt.setBigDecimal(3, dv.getDongia());

            if (dv.getTrangthai() != null) {
                stmt.setString(4, dv.getTrangthai().name());
            } else {
                stmt.setNull(4, Types.NVARCHAR);
            }

            n = stmt.executeUpdate();

            if (n > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        dv.setMaDV(rs.getString(1));
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}

    // 3. Cập nhật dịch vụ
    public boolean update(DichVu dv) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "UPDATE DichVu SET "
                   + "tenDV = ?, "
                   + "moTa = ?, "
                   + "donGia = ?, "
                   + "trangThai = ? "
                   + "WHERE maDV = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, dv.getTenDV());
            stmt.setString(2, dv.getMota());
            stmt.setBigDecimal(3, dv.getDongia());

            if (dv.getTrangthai() != null) {
                stmt.setString(4, dv.getTrangthai().name());
            } else {
                stmt.setNull(4, Types.NVARCHAR);
            }

            stmt.setInt(5, Integer.parseInt(dv.getMaDV()));

            n = stmt.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}

    // 4. Ngừng áp dụng dịch vụ
    public boolean delete(String maDV) {

        try (Connection con = ConnectDB.getConnection()) {

        int n = 0;

        String sql = "UPDATE DichVu SET trangThai = ? "
                   + "WHERE maDV = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, trangthai_dichvu.NGUNGAPDUNG.name());
            stmt.setInt(2, Integer.parseInt(maDV));

            n = stmt.executeUpdate();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }

        return n > 0;
    
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}

	public ArrayList<DichVu> timDichVu(
	        String tuKhoa,
	        BigDecimal giaTu,
	        BigDecimal giaDen,
	        trangthai_dichvu tt) {
	
	    ArrayList<DichVu> dsdv = new ArrayList<>();
	
	    // Kiểm tra khoảng giá
	    if (giaTu != null && giaDen != null
	            && giaTu.compareTo(giaDen) > 0) {
	        throw new IllegalArgumentException(
	            "Giá từ không được lớn hơn giá đến."
	        );
	    }
	
		    try (Connection con = ConnectDB.getConnection()) {
	
	    StringBuilder sql = new StringBuilder(
	        "SELECT maDV, tenDV, moTa, donGia, trangThai "
	      + "FROM DichVu WHERE 1=1"
	    );
	
	    // Lọc theo mã, tên hoặc mô tả
	    if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
	        sql.append(" AND (CAST(maDV AS NVARCHAR(30)) LIKE ? "
	                 + "OR tenDV LIKE ? OR moTa LIKE ?)");
	    }
	
	    // Lọc giá tối thiểu
	    if (giaTu != null) {
	        sql.append(" AND donGia >= ?");
	    }
	
	    // Lọc giá tối đa
	    if (giaDen != null) {
	        sql.append(" AND donGia <= ?");
	    }
	
	    // Lọc trạng thái dịch vụ
	    if (tt != null) {
	        sql.append(" AND trangThai = ?");
	    }
	
	    sql.append(" ORDER BY maDV DESC");
	
	    try (PreparedStatement stmt = con.prepareStatement(sql.toString())) {
	
	        int i = 1;
	
	        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
	            String key = "%" + tuKhoa.trim() + "%";
	
	            stmt.setString(i++, key);
	            stmt.setString(i++, key);
	            stmt.setString(i++, key);
	        }
	
	        if (giaTu != null) {
	            stmt.setBigDecimal(i++, giaTu);
	        }
	
	        if (giaDen != null) {
	            stmt.setBigDecimal(i++, giaDen);
	        }
	
	        if (tt != null) {
	            stmt.setString(i++, tt.name());
	        }
	
	        try (ResultSet rs = stmt.executeQuery()) {
	
	            while (rs.next()) {
	
	                String maDV = rs.getString("maDV");
	                String tenDV = rs.getString("tenDV");
	                String mota = rs.getString("moTa");
	
	                BigDecimal dongia = rs.getBigDecimal("donGia");
	
	                String tt1 = rs.getString("trangThai");
	
	                trangthai_dichvu tt2 = tt1 != null
	                        ? trangthai_dichvu.valueOf(tt1) : null;
	
	                DichVu dv = new DichVu(
	                    maDV, tenDV, mota, dongia, tt2
	                );
	
	                dsdv.add(dv);
	            }
	        }
	
	    } catch (SQLException | IllegalArgumentException e) {
	        e.printStackTrace();
	    }
	
	    return dsdv;
	
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}
	public DichVu getDichVuByMaDV(String maDV) throws SQLException {

	
	    try (Connection con = ConnectDB.getConnection()) {

	    if (con == null) {
	        throw new SQLException("Chưa kết nối SQL Server!");
	    }

	    String sql = "SELECT maDV, tenDV, mota, dongia, trangthai "
	               + "FROM DichVu WHERE maDV = ?";

	    try (PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setString(1, maDV);

	        try (ResultSet rs = ps.executeQuery()) {

	            if (rs.next()) {

	                String ma = rs.getString("maDV");
	                String ten = rs.getString("tenDV");
	                String mota = rs.getString("mota");

	                BigDecimal dongia = rs.getBigDecimal("dongia");

	                String tt1 = rs.getString("trangthai");

	                trangthai_dichvu tt =
	                    (tt1 != null && !tt1.isBlank())
	                    ? trangthai_dichvu.valueOf(tt1.trim())
	                    : null;

	                return new DichVu(
	                    ma,
	                    ten,
	                    mota,
	                    dongia,
	                    tt
	                );
	            }
	        }
	    }

	    return null;
	
        } catch (SQLException loiDongKetNoi) { throw new IllegalStateException("Lỗi trả kết nối JDBC", loiDongKetNoi); }
}
}