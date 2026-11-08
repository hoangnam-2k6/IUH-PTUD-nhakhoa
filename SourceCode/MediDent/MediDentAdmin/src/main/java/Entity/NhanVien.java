package Entity;

import java.time.LocalDate;
import java.util.Objects;

import enums.gioitinh;
import enums.trangthai_NhanVien;

public class NhanVien {
	private String maNV;
	private String hoten;
	private LocalDate ngaysinh;
	private gioitinh gioitinh;
	private String sdt;
	private String email;
	private String diachi;
	private String chucvu;
	private trangthai_NhanVien trangthai;
	private String maTK;
	
	public NhanVien() {
		super();
	}

	public NhanVien(String maNV, String hoten, LocalDate ngaysinh, enums.gioitinh gioitinh, String sdt, String email,
			String diachi, String chucvu, trangthai_NhanVien trangthai, String maTK) {
		super();
		this.maNV = maNV;
		this.hoten = hoten;
		this.ngaysinh = ngaysinh;
		this.gioitinh = gioitinh;
		this.sdt = sdt;
		this.email = email;
		this.diachi = diachi;
		this.chucvu = chucvu;
		this.trangthai = trangthai;
		this.maTK = maTK;
	}

	public String getMaNV() {
		return maNV;
	}

	public void setMaNV(String maNV) {
		this.maNV = maNV;
	}

	public String getHoten() {
		return hoten;
	}

	public void setHoten(String hoten) {
		this.hoten = hoten;
	}

	public LocalDate getNgaysinh() {
		return ngaysinh;
	}

	public void setNgaysinh(LocalDate ngaysinh) {
		this.ngaysinh = ngaysinh;
	}

	public gioitinh getGioitinh() {
		return gioitinh;
	}

	public void setGioitinh(gioitinh gioitinh) {
		this.gioitinh = gioitinh;
	}

	public String getSdt() {
		return sdt;
	}

	public void setSdt(String sdt) {
		this.sdt = sdt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDiachi() {
		return diachi;
	}

	public void setDiachi(String diachi) {
		this.diachi = diachi;
	}

	public String getChucvu() {
		return chucvu;
	}

	public void setChucvu(String chucvu) {
		this.chucvu = chucvu;
	}

	public trangthai_NhanVien getTrangthai() {
		return trangthai;
	}

	public void setTrangthai(trangthai_NhanVien trangthai) {
		this.trangthai = trangthai;
	}

	public String getMaTK() {
		return maTK;
	}

	public void setMaTK(String maTK) {
		this.maTK = maTK;
	}

	@Override
	public int hashCode() {
		return Objects.hash(chucvu, diachi, email, gioitinh, hoten, maNV, maTK, ngaysinh, sdt, trangthai);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NhanVien other = (NhanVien) obj;
		return Objects.equals(chucvu, other.chucvu) && Objects.equals(diachi, other.diachi)
				&& Objects.equals(email, other.email) && gioitinh == other.gioitinh
				&& Objects.equals(hoten, other.hoten) && Objects.equals(maNV, other.maNV)
				&& Objects.equals(maTK, other.maTK) && Objects.equals(ngaysinh, other.ngaysinh)
				&& Objects.equals(sdt, other.sdt) && trangthai == other.trangthai;
	}

	@Override
	public String toString() {
		return "NhanVien [maNV=" + maNV + ", hoten=" + hoten + ", ngaysinh=" + ngaysinh + ", gioitinh=" + gioitinh
				+ ", sdt=" + sdt + ", email=" + email + ", diachi=" + diachi + ", chucvu=" + chucvu + ", trangthai="
				+ trangthai + ", maTK=" + maTK + "]";
	}
}
