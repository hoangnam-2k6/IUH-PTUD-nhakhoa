package Entity;

import java.time.LocalDateTime;

/** Lịch hẹn nha khoa; không chứa truy vấn SQL. */
public class LichHen {
    private int maLH,maBN,maBS;
    private String tenBenhNhan,tenBacSi,trangThai,ghiChu,yeuCau;
    private LocalDateTime ngayGio,ngayGioMoi;
    public int layMaLH(){return maLH;} public void datMaLH(int v){maLH=v;}
    public int layMaBN(){return maBN;} public void datMaBN(int v){maBN=v;}
    public int layMaBS(){return maBS;} public void datMaBS(int v){maBS=v;}
    public String layTenBenhNhan(){return tenBenhNhan;} public void datTenBenhNhan(String v){tenBenhNhan=v;}
    public String layTenBacSi(){return tenBacSi;} public void datTenBacSi(String v){tenBacSi=v;}
    public String layTrangThai(){return trangThai;} public void datTrangThai(String v){trangThai=v;}
    public String layGhiChu(){return ghiChu;} public void datGhiChu(String v){ghiChu=v;}
    public String layYeuCau(){return yeuCau;} public void datYeuCau(String v){yeuCau=v;}
    public LocalDateTime layNgayGio(){return ngayGio;} public void datNgayGio(LocalDateTime v){ngayGio=v;}
    public LocalDateTime layNgayGioMoi(){return ngayGioMoi;} public void datNgayGioMoi(LocalDateTime v){ngayGioMoi=v;}
    // Thuộc tính cho giao diện quản lý lịch hẹn cũ (giữ nguyên API Lễ tân ở trên).
    private int maLichHen, thoiLuongPhut;
    private String maBenhNhanChuoi, maNhanVienChuoi, phongKham;
    private java.time.LocalDate ngayHen;
    private java.time.LocalTime gioHen;
    public LichHen() {}
    public LichHen(int maLichHen, String maBN, String maNV, java.time.LocalDate ngayHen,
                   java.time.LocalTime gioHen, int thoiLuongPhut, String phongKham, String trangThai) {
        this.maLichHen = maLichHen; this.maBenhNhanChuoi = maBN; this.maNhanVienChuoi = maNV;
        this.ngayHen = ngayHen; this.gioHen = gioHen; this.thoiLuongPhut = thoiLuongPhut;
        this.phongKham = phongKham; this.trangThai = trangThai;
    }
    public int maLichHen() { return maLichHen; }
    public String maBN() { return maBenhNhanChuoi; }
    public String maNV() { return maNhanVienChuoi; }
    public java.time.LocalDate ngayHen() { return ngayHen; }
    public java.time.LocalTime gioHen() { return gioHen; }
    public int thoiLuongPhut() { return thoiLuongPhut; }
    public String phongKham() { return phongKham; }
    public String trangThai() { return trangThai; }
    public boolean coYeuCau(){return yeuCau!=null && !yeuCau.isBlank();}
    public boolean daKetThuc(){return "Đã hủy".equals(trangThai)||"Hoàn thành".equals(trangThai);}
}
