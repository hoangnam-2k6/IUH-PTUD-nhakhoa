package Entity;

import java.text.DecimalFormat;
import java.util.Objects;

import enums.trangthai_dichvu;

public class DichVu {
	private String maDV;
	private String tenDV;
	private String mota;
	private DecimalFormat dongia;
	private trangthai_dichvu trangthai;
	
	public DichVu() {
		super();
	}

	public DichVu(String maDV, String tenDV, String mota, DecimalFormat dongia, trangthai_dichvu trangthai) {
		super();
		this.maDV = maDV;
		this.tenDV = tenDV;
		this.mota = mota;
		this.dongia = dongia;
		this.trangthai = trangthai;
	}

	public String getMaDV() {
		return maDV;
	}

	public void setMaDV(String maDV) {
		this.maDV = maDV;
	}

	public String getTenDV() {
		return tenDV;
	}

	public void setTenDV(String tenDV) {
		this.tenDV = tenDV;
	}

	public String getMota() {
		return mota;
	}

	public void setMota(String mota) {
		this.mota = mota;
	}

	public DecimalFormat getDongia() {
		return dongia;
	}

	public void setDongia(DecimalFormat dongia) {
		this.dongia = dongia;
	}

	public trangthai_dichvu getTrangthai() {
		return trangthai;
	}

	public void setTrangthai(trangthai_dichvu trangthai) {
		this.trangthai = trangthai;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dongia, maDV, mota, tenDV, trangthai);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DichVu other = (DichVu) obj;
		return Objects.equals(dongia, other.dongia) && Objects.equals(maDV, other.maDV)
				&& Objects.equals(mota, other.mota) && Objects.equals(tenDV, other.tenDV)
				&& trangthai == other.trangthai;
	}

	@Override
	public String toString() {
		return "DichVu [maDV=" + maDV + ", tenDV=" + tenDV + ", mota=" + mota + ", dongia=" + dongia + ", trangthai="
				+ trangthai + "]";
	}
}
