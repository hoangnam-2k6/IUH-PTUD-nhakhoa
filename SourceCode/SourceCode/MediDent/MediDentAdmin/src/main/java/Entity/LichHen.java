package Entity;

import java.time.LocalDateTime;

/** Lịch hẹn nha khoa; không chứa truy vấn SQL. */
public class LichHen {
    private int maLH,maBN,maBS;
    private String tenBenhNhan,tenBacSi,trangThai,ghiChu,yeuCau;
    private LocalDateTime ngayGio,ngayGioMoi;
    public int getMaLH(){return maLH;} public void setMaLH(int v){maLH=v;}
    public int getMaBN(){return maBN;} public void setMaBN(int v){maBN=v;}
    public int getMaBS(){return maBS;} public void setMaBS(int v){maBS=v;}
    public String getTenBenhNhan(){return tenBenhNhan;} public void setTenBenhNhan(String v){tenBenhNhan=v;}
    public String getTenBacSi(){return tenBacSi;} public void setTenBacSi(String v){tenBacSi=v;}
    public String getTrangThai(){return trangThai;} public void setTrangThai(String v){trangThai=v;}
    public String getGhiChu(){return ghiChu;} public void setGhiChu(String v){ghiChu=v;}
    public String getYeuCau(){return yeuCau;} public void setYeuCau(String v){yeuCau=v;}
    public LocalDateTime getNgayGio(){return ngayGio;} public void setNgayGio(LocalDateTime v){ngayGio=v;}
    public LocalDateTime getNgayGioMoi(){return ngayGioMoi;} public void setNgayGioMoi(LocalDateTime v){ngayGioMoi=v;}
    public boolean coYeuCau(){return yeuCau!=null && !yeuCau.isBlank();}
    public boolean daKetThuc(){return "Đã hủy".equals(trangThai)||"Hoàn thành".equals(trangThai);}
}
