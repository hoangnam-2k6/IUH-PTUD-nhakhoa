package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		dsnv = new ArrayList<NhanVien>();
		nv = new NhanVien();
	}
	public ArrayList<NhanVien> getallnhanvien(){
		try {
			ConnectDB.GetInstance();
			String sql = "select * from NhanVien";
			Statement statement = ConnectDB.con.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				String maNV = rs.getString(1);
				String hoten = rs.getString(2);
				LocalDate ngaysinh = rs.getDate(3) != null ? rs.getDate(3).toLocalDate() : null;
				String gt1 = rs.getString(4);
				gioitinh gt2 = (gt1 != null) ? gioitinh.valueOf(gt1) : null;
				String sdt = rs.getString(5);
				String email = rs.getString(6);
				String diachi = rs.getString(7);
				String chucvu = rs.getString(8);
				String ttnv = rs.getString(9);
				trangthai_NhanVien trangthai = (ttnv != null) ? trangthai_NhanVien.valueOf(ttnv) : null;
				String maTK = rs.getString(10);
				NhanVien s = new NhanVien(maNV, hoten, ngaysinh, gt2, sdt, email, diachi, chucvu, trangthai, maTK);
				dsnv.add(s);
			}
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return dsnv;
	}
	public boolean create(NhanVien nv) {
		ConnectDB.GetInstance();
		Connection con = ConnectDB.getConnection();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("insert into" + " NhanVien values(?,?,?,?,?,?,?,?,?,?)");
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
			// TODO: handle exception
			e.printStackTrace();
		}
		return n > 0;
	}
}
