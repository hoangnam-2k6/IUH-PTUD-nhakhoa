package Entity;

import java.time.LocalDate;
import java.util.Objects;

import enums.gioitinh;
import enums.trangthaidieutri;

public class BenhNhan {
	private String maBN;
	private String hoten;
	private LocalDate ngaysinh;
	private gioitinh gioitinh;
	private String sdt;
	private String email;
	private String diachi;
	private String tiensubenh;
	private trangthaidieutri trangthai;
	
	public BenhNhan() {
		super();
	}

	public BenhNhan(String maBN, String hoten, LocalDate ngaysinh, enums.gioitinh gioitinh, String sdt, String email,
			String diachi, String tiensubenh, trangthaidieutri trangthai) {
		super();
		this.maBN = maBN;
		this.hoten = hoten;
		this.ngaysinh = ngaysinh;
		this.gioitinh = gioitinh;
		this.sdt = sdt;
		this.email = email;
		this.diachi = diachi;
		this.tiensubenh = tiensubenh;
		this.trangthai = trangthai;
	}

	public String getMaBN() {
		return maBN;
	}

	public void setMaBN(String maBN) {
		this.maBN = maBN;
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

	public String getTiensubenh() {
		return tiensubenh;
	}

	public void setTiensubenh(String tiensubenh) {
		this.tiensubenh = tiensubenh;
	}

	public trangthaidieutri getTrangthai() {
		return trangthai;
	}

	public void setTrangthai(trangthaidieutri trangthai) {
		this.trangthai = trangthai;
	}

	@Override
	public int hashCode() {
		return Objects.hash(diachi, email, gioitinh, hoten, maBN, ngaysinh, sdt, tiensubenh, trangthai);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BenhNhan other = (BenhNhan) obj;
		return Objects.equals(diachi, other.diachi) && Objects.equals(email, other.email) && gioitinh == other.gioitinh
				&& Objects.equals(hoten, other.hoten) && Objects.equals(maBN, other.maBN)
				&& Objects.equals(ngaysinh, other.ngaysinh) && Objects.equals(sdt, other.sdt)
				&& Objects.equals(tiensubenh, other.tiensubenh) && trangthai == other.trangthai;
	}

	@Override
	public String toString() {
		return "BenhNhan [maBN=" + maBN + ", hoten=" + hoten + ", ngaysinh=" + ngaysinh + ", gioitinh=" + gioitinh
				+ ", sdt=" + sdt + ", email=" + email + ", diachi=" + diachi + ", tiensubenh=" + tiensubenh
				+ ", trangthai=" + trangthai + "]";
	}
}
