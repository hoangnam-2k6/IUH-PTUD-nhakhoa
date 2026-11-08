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

    // Trường hồ sơ lễ tân; trạng thái hồ sơ khác trạng thái điều trị.
    public static final String HOAT_DONG="Hoạt động", NGUNG_HOAT_DONG="Ngừng hoạt động";
    private String cccd, ngheNghiep, tenNguoiLienHe, sdtNguoiLienHe, diUng, ghiChu;
    private String trangThaiHoSo=HOAT_DONG;
    private java.time.LocalDateTime ngayTao;

    public BenhNhan(int id,String ten,LocalDate ngay,String gioiTinh,String sdt,String diaChi) {
        this.maBN=String.valueOf(id);this.hoten=ten;this.ngaysinh=ngay;
        this.gioitinh=gioiTinh==null?null:(("Nam".equalsIgnoreCase(gioiTinh)||"NAM".equalsIgnoreCase(gioiTinh))?enums.gioitinh.NAM:
              ("Nữ".equalsIgnoreCase(gioiTinh)||"NU".equalsIgnoreCase(gioiTinh))?enums.gioitinh.NU:enums.gioitinh.KHAC);
        this.sdt=sdt;this.diachi=diaChi;
    }
    public int getMaBNSo(){
        if(maBN==null||maBN.isBlank())return 0;
        return Integer.parseInt(maBN.replaceAll("[^0-9]",""));
    }
    public void setMaBN(int v){this.maBN=String.valueOf(v);}
    public String getHoTen(){return hoten;} public void setHoTen(String v){hoten=v;}
    public LocalDate getNgaySinh(){return ngaysinh;} public void setNgaySinh(LocalDate v){ngaysinh=v;}
    public String getGioiTinh(){return gioitinh==null?null:
        (gioitinh==enums.gioitinh.NAM?"Nam":gioitinh==enums.gioitinh.NU?"Nữ":"Khác");}
    public void setGioiTinh(String v){gioitinh=v==null?null:
        (("Nam".equalsIgnoreCase(v)||"NAM".equalsIgnoreCase(v))?enums.gioitinh.NAM:
         ("Nữ".equalsIgnoreCase(v)||"NU".equalsIgnoreCase(v))?enums.gioitinh.NU:enums.gioitinh.KHAC);}
    public String getSoDienThoai(){return sdt;} public void setSoDienThoai(String v){sdt=v;}
    public String getDiaChi(){return diachi;} public void setDiaChi(String v){diachi=v;}
    public String getTienSuBenh(){return tiensubenh;} public void setTienSuBenh(String v){tiensubenh=v;}
    public String getCccd(){return cccd;} public void setCccd(String v){cccd=v;}
    public String getNgheNghiep(){return ngheNghiep;} public void setNgheNghiep(String v){ngheNghiep=v;}
    public String getTenNguoiLienHe(){return tenNguoiLienHe;} public void setTenNguoiLienHe(String v){tenNguoiLienHe=v;}
    public String getSdtNguoiLienHe(){return sdtNguoiLienHe;} public void setSdtNguoiLienHe(String v){sdtNguoiLienHe=v;}
    public String getDiUng(){return diUng;} public void setDiUng(String v){diUng=v;}
    public String getGhiChu(){return ghiChu;} public void setGhiChu(String v){ghiChu=v;}
    public String getTrangThai(){return trangThaiHoSo;} public void setTrangThai(String v){trangThaiHoSo=v;}
    public boolean dangHoatDong(){return HOAT_DONG.equals(trangThaiHoSo);}
    public java.time.LocalDateTime getNgayTao(){return ngayTao;}
    public void setNgayTao(java.time.LocalDateTime v){ngayTao=v;}
    public Integer getTuoi(){return ngaysinh==null?null:java.time.Period.between(ngaysinh,LocalDate.now()).getYears();}

}
