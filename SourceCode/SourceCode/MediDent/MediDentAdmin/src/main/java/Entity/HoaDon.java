package Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Chứng từ và thông tin snapshot thanh toán; không chứa SQL. */
public class HoaDon {
    public static final String TIEN_MAT="Tiền mặt";
    public static final String[] PHUONG_THUC={TIEN_MAT,"Chuyển khoản"};
    public static final String CHUA_THANH_TOAN="Chưa thanh toán", DA_THANH_TOAN="Đã thanh toán", DA_HUY="Đã hủy";
    private int maHD,maPK,soThuTu;
    private String tenBenhNhan,maBenhNhanHienThi,tenPhong,trangThai=CHUA_THANH_TOAN;
    private String phuongThucTT,tenNguoiLap,ghiChu,lyDoHuy;
    private LocalDateTime ngayLap,ngayThanhToan;
    private LocalDate ngayKham;
    private BigDecimal tamTinh=BigDecimal.ZERO,giamGia=BigDecimal.ZERO,tongTien=BigDecimal.ZERO,tienKhachDua,tienThua;
    private BenhNhan benhNhan;
    private List<ChiTietHoaDon> chiTiet=new ArrayList<>();
    public int getMaHD(){return maHD;} public void setMaHD(int v){maHD=v;}
    public int getMaPK(){return maPK;} public void setMaPK(int v){maPK=v;}
    public int getSoThuTu(){return soThuTu;} public void setSoThuTu(int v){soThuTu=v;}
    public String getTenBenhNhan(){return tenBenhNhan;} public void setTenBenhNhan(String v){tenBenhNhan=v;}
    public String getMaBenhNhanHienThi(){return maBenhNhanHienThi;} public void setMaBenhNhanHienThi(String v){maBenhNhanHienThi=v;}
    public String getTenPhong(){return tenPhong;} public void setTenPhong(String v){tenPhong=v;}
    public String getTrangThai(){return trangThai;} public void setTrangThai(String v){trangThai=v;}
    public String getPhuongThucTT(){return phuongThucTT;} public void setPhuongThucTT(String v){phuongThucTT=v;}
    public String getTenNguoiLap(){return tenNguoiLap;} public void setTenNguoiLap(String v){tenNguoiLap=v;}
    public String getGhiChu(){return ghiChu;} public void setGhiChu(String v){ghiChu=v;}
    public String getLyDoHuy(){return lyDoHuy;} public void setLyDoHuy(String v){lyDoHuy=v;}
    public LocalDateTime getNgayLap(){return ngayLap;} public void setNgayLap(LocalDateTime v){ngayLap=v;}
    public LocalDateTime getNgayThanhToan(){return ngayThanhToan;} public void setNgayThanhToan(LocalDateTime v){ngayThanhToan=v;}
    public LocalDate getNgayKham(){return ngayKham;} public void setNgayKham(LocalDate v){ngayKham=v;}
    public BigDecimal getTamTinh(){return tamTinh;} public void setTamTinh(BigDecimal v){tamTinh=v;}
    public BigDecimal getGiamGia(){return giamGia;} public void setGiamGia(BigDecimal v){giamGia=v;}
    public BigDecimal getTongTien(){return tongTien;} public void setTongTien(BigDecimal v){tongTien=v;}
    public BigDecimal getTienKhachDua(){return tienKhachDua;} public void setTienKhachDua(BigDecimal v){tienKhachDua=v;}
    public BigDecimal getTienThua(){return tienThua;} public void setTienThua(BigDecimal v){tienThua=v;}
    public BenhNhan getBenhNhan(){return benhNhan;} public void setBenhNhan(BenhNhan v){benhNhan=v;}
    public List<ChiTietHoaDon> getChiTiet(){return chiTiet;} public void setChiTiet(List<ChiTietHoaDon> v){chiTiet=v;}
    public boolean chuaThanhToan(){return CHUA_THANH_TOAN.equals(trangThai);}
    public boolean daThanhToan(){return DA_THANH_TOAN.equals(trangThai);}
    public boolean daHuy(){return DA_HUY.equals(trangThai);}
}
