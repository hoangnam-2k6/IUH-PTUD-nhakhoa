package Entity;

import java.time.LocalDate;

/** Phiếu khám cho nghiệp vụ tiếp nhận và điều trị. */
public class PhieuKham {
    public static final String CHO_KHAM="Chờ khám", DANG_KHAM="Đang khám", HOAN_THANH="Hoàn thành", DA_HUY="Đã hủy";
    private int maPK, maBN, soThuTu;
    private Integer maPhong, maLH;
    private LocalDate ngayKham;
    private String tenBenhNhan,tenPhong,maBenhNhanHienThi,trangThai=CHO_KHAM,ghiChu;
    public int getMaPK(){return maPK;} public void setMaPK(int v){maPK=v;}
    public int getMaBN(){return maBN;} public void setMaBN(int v){maBN=v;}
    public int getSoThuTu(){return soThuTu;} public void setSoThuTu(int v){soThuTu=v;}
    public Integer getMaPhong(){return maPhong;} public void setMaPhong(Integer v){maPhong=v;}
    public Integer getMaLH(){return maLH;} public void setMaLH(Integer v){maLH=v;}
    public LocalDate getNgayKham(){return ngayKham;} public void setNgayKham(LocalDate v){ngayKham=v;}
    public String getTenBenhNhan(){return tenBenhNhan;} public void setTenBenhNhan(String v){tenBenhNhan=v;}
    public String getTenPhong(){return tenPhong;} public void setTenPhong(String v){tenPhong=v;}
    public String getMaBenhNhanHienThi(){return maBenhNhanHienThi;} public void setMaBenhNhanHienThi(String v){maBenhNhanHienThi=v;}
    public String getTrangThai(){return trangThai;} public void setTrangThai(String v){trangThai=v;}
    public String getGhiChu(){return ghiChu;} public void setGhiChu(String v){ghiChu=v;}
    public boolean daHuy(){return DA_HUY.equals(trangThai);}
    public boolean choKham(){return CHO_KHAM.equals(trangThai);}
    public boolean daKetThuc(){return HOAN_THANH.equals(trangThai)||DA_HUY.equals(trangThai);}
    @Override public String toString(){return String.format("PK%03d - %s",maPK,tenBenhNhan);}
}
