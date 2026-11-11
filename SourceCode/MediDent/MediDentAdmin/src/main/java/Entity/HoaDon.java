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
    private String phuongThucTT,tenNguoiLap,tenBacSi,ghiChu,lyDoHuy;
    private LocalDateTime ngayLap,ngayThanhToan;
    private LocalDate ngayKham;
    private BigDecimal tamTinh=BigDecimal.ZERO,giamGia=BigDecimal.ZERO,tongTien=BigDecimal.ZERO,tienKhachDua,tienThua;
    private BenhNhan benhNhan;
    private List<ChiTietHoaDon> chiTiet=new ArrayList<>();
    public int layMaHD(){return maHD;} public void datMaHD(int v){maHD=v;}
    public int layMaPK(){return maPK;} public void datMaPK(int v){maPK=v;}
    public int laySoThuTu(){return soThuTu;} public void datSoThuTu(int v){soThuTu=v;}
    public String layTenBenhNhan(){return tenBenhNhan;} public void datTenBenhNhan(String v){tenBenhNhan=v;}
    public String layMaBenhNhanHienThi(){return maBenhNhanHienThi;} public void datMaBenhNhanHienThi(String v){maBenhNhanHienThi=v;}
    public String layTenPhong(){return tenPhong;} public void datTenPhong(String v){tenPhong=v;}
    public String layTrangThai(){return trangThai;} public void datTrangThai(String v){trangThai=v;}
    public String layPhuongThucTT(){return phuongThucTT;} public void datPhuongThucTT(String v){phuongThucTT=v;}
    public String layTenNguoiLap(){return tenNguoiLap;} public void datTenNguoiLap(String v){tenNguoiLap=v;}
    public String layTenBacSi(){return tenBacSi;} public void datTenBacSi(String v){tenBacSi=v;}
    public String layGhiChu(){return ghiChu;} public void datGhiChu(String v){ghiChu=v;}
    public String layLyDoHuy(){return lyDoHuy;} public void datLyDoHuy(String v){lyDoHuy=v;}
    public LocalDateTime layNgayLap(){return ngayLap;} public void datNgayLap(LocalDateTime v){ngayLap=v;}
    public LocalDateTime layNgayThanhToan(){return ngayThanhToan;} public void datNgayThanhToan(LocalDateTime v){ngayThanhToan=v;}
    public LocalDate layNgayKham(){return ngayKham;} public void datNgayKham(LocalDate v){ngayKham=v;}
    public BigDecimal layTamTinh(){return tamTinh;} public void datTamTinh(BigDecimal v){tamTinh=v;}
    public BigDecimal layGiamGia(){return giamGia;} public void datGiamGia(BigDecimal v){giamGia=v;}
    public BigDecimal layTongTien(){return tongTien;} public void datTongTien(BigDecimal v){tongTien=v;}
    public BigDecimal layTienKhachDua(){return tienKhachDua;} public void datTienKhachDua(BigDecimal v){tienKhachDua=v;}
    public BigDecimal layTienThua(){return tienThua;} public void datTienThua(BigDecimal v){tienThua=v;}
    public BenhNhan layBenhNhan(){return benhNhan;} public void datBenhNhan(BenhNhan v){benhNhan=v;}
    public List<ChiTietHoaDon> layChiTiet(){return chiTiet;} public void datChiTiet(List<ChiTietHoaDon> v){chiTiet=v;}
    public boolean chuaThanhToan(){return CHUA_THANH_TOAN.equals(trangThai);}
    public boolean daThanhToan(){return DA_THANH_TOAN.equals(trangThai);}
    public boolean daHuy(){return DA_HUY.equals(trangThai);}
}
