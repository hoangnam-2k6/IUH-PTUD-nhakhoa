package Entity;

import java.time.LocalDate;

/** Phiếu khám cho nghiệp vụ tiếp nhận và điều trị. */
public class PhieuKham {
    public static final String CHO_KHAM="Chờ khám", DANG_KHAM="Đang khám", HOAN_THANH="Hoàn thành", DA_HUY="Đã hủy";
    private int maPK, maBN, soThuTu;
    private Integer maPhong, maLH;
    private LocalDate ngayKham;
    private String tenBenhNhan,tenPhong,maBenhNhanHienThi,trangThai=CHO_KHAM,ghiChu;
    public int layMaPK(){return maPK;} public void datMaPK(int v){maPK=v;}
    public int layMaBN(){return maBN;} public void datMaBN(int v){maBN=v;}
    public int laySoThuTu(){return soThuTu;} public void datSoThuTu(int v){soThuTu=v;}
    public Integer layMaPhong(){return maPhong;} public void datMaPhong(Integer v){maPhong=v;}
    public Integer layMaLH(){return maLH;} public void datMaLH(Integer v){maLH=v;}
    public LocalDate layNgayKham(){return ngayKham;} public void datNgayKham(LocalDate v){ngayKham=v;}
    public String layTenBenhNhan(){return tenBenhNhan;} public void datTenBenhNhan(String v){tenBenhNhan=v;}
    public String layTenPhong(){return tenPhong;} public void datTenPhong(String v){tenPhong=v;}
    public String layMaBenhNhanHienThi(){return maBenhNhanHienThi;} public void datMaBenhNhanHienThi(String v){maBenhNhanHienThi=v;}
    public String layTrangThai(){return trangThai;} public void datTrangThai(String v){trangThai=v;}
    public String layGhiChu(){return ghiChu;} public void datGhiChu(String v){ghiChu=v;}
    public boolean daHuy(){return DA_HUY.equals(trangThai);}
    public boolean choKham(){return CHO_KHAM.equals(trangThai);}
    public boolean daKetThuc(){return HOAN_THANH.equals(trangThai)||DA_HUY.equals(trangThai);}
    @Override public String toString(){return String.format("PK%03d - %s",maPK,tenBenhNhan);}
}
