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

	public String layMaBN() {
		return maBN;
	}

	public void datMaBN(String maBN) {
		this.maBN = maBN;
	}

	public String layHoten() {
		return hoten;
	}

	public void datHoten(String hoten) {
		this.hoten = hoten;
	}

	public LocalDate layNgaysinh() {
		return ngaysinh;
	}

	public void datNgaysinh(LocalDate ngaysinh) {
		this.ngaysinh = ngaysinh;
	}

	public gioitinh layGioitinh() {
		return gioitinh;
	}

	public void datGioitinh(gioitinh gioitinh) {
		this.gioitinh = gioitinh;
	}

	public String laySdt() {
		return sdt;
	}

	public void datSdt(String sdt) {
		this.sdt = sdt;
	}

	public String layEmail() {
		return email;
	}

	public void datEmail(String email) {
		this.email = email;
	}

	public String layDiachi() {
		return diachi;
	}

	public void datDiachi(String diachi) {
		this.diachi = diachi;
	}

	public String layTiensubenh() {
		return tiensubenh;
	}

	public void datTiensubenh(String tiensubenh) {
		this.tiensubenh = tiensubenh;
	}

	public trangthaidieutri layTrangthai() {
		return trangthai;
	}

	public void datTrangthai(trangthaidieutri trangthai) {
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
    public int layMaBNSo(){
        if(maBN==null||maBN.isBlank())return 0;
        return Integer.parseInt(maBN.replaceAll("[^0-9]",""));
    }
    public void datMaBN(int v){this.maBN=String.valueOf(v);}
    public String layHoTen(){return hoten;} public void datHoTen(String v){hoten=v;}
    public LocalDate layNgaySinh(){return ngaysinh;} public void datNgaySinh(LocalDate v){ngaysinh=v;}
    public String layGioiTinh(){return gioitinh==null?null:
        (gioitinh==enums.gioitinh.NAM?"Nam":gioitinh==enums.gioitinh.NU?"Nữ":"Khác");}
    public void datGioiTinh(String v){gioitinh=v==null?null:
        (("Nam".equalsIgnoreCase(v)||"NAM".equalsIgnoreCase(v))?enums.gioitinh.NAM:
         ("Nữ".equalsIgnoreCase(v)||"NU".equalsIgnoreCase(v))?enums.gioitinh.NU:enums.gioitinh.KHAC);}
    public String laySoDienThoai(){return sdt;} public void datSoDienThoai(String v){sdt=v;}
    public String layDiaChi(){return diachi;} public void datDiaChi(String v){diachi=v;}
    public String layTienSuBenh(){return tiensubenh;} public void datTienSuBenh(String v){tiensubenh=v;}
    public String layCccd(){return cccd;} public void datCccd(String v){cccd=v;}
    public String layNgheNghiep(){return ngheNghiep;} public void datNgheNghiep(String v){ngheNghiep=v;}
    public String layTenNguoiLienHe(){return tenNguoiLienHe;} public void datTenNguoiLienHe(String v){tenNguoiLienHe=v;}
    public String laySdtNguoiLienHe(){return sdtNguoiLienHe;} public void datSdtNguoiLienHe(String v){sdtNguoiLienHe=v;}
    public String layDiUng(){return diUng;} public void datDiUng(String v){diUng=v;}
    public String layGhiChu(){return ghiChu;} public void datGhiChu(String v){ghiChu=v;}
    public String layTrangThai(){return trangThaiHoSo;} public void datTrangThai(String v){trangThaiHoSo=v;}
    public boolean dangHoatDong(){return HOAT_DONG.equals(trangThaiHoSo);}
    public java.time.LocalDateTime layNgayTao(){return ngayTao;}
    public void datNgayTao(java.time.LocalDateTime v){ngayTao=v;}
    public Integer layTuoi(){return ngaysinh==null?null:java.time.Period.between(ngaysinh,LocalDate.now()).getYears();}

}
