package Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    public static final String CHUA_THANH_TOAN = "Chưa thanh toán";
    public static final String DA_THANH_TOAN = "Đã thanh toán";
    public static final String DA_HUY = "Đã hủy";

    public static final String TIEN_MAT = "Tiền mặt";
    public static final String CHUYEN_KHOAN = "Chuyển khoản";
    public static final String THE = "Thẻ";
    public static final String[] PHUONG_THUC = {TIEN_MAT, CHUYEN_KHOAN, THE};

    private int maHD;
    private int maPK;
    private Integer maTK;
    private String tenNguoiLap;
    private LocalDateTime ngayLap;
    private BigDecimal tongTien = BigDecimal.ZERO;   // số tiền phải trả (đã trừ giảm giá)
    private BigDecimal giamGia = BigDecimal.ZERO;
    private Integer maGiam;
    private String maGiamCode;
    private String phuongThucTT;
    private LocalDateTime ngayThanhToan;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThua;
    private String trangThai = CHUA_THANH_TOAN;
    private String ghiChu;
    private String lyDoHuy;
    private String tenBenhNhan;
    private String maBenhNhanHienThi;

    // Chỉ có khi lấy đầy đủ để in hóa đơn
    private BenhNhan benhNhan;
    private LocalDate ngayKham;
    private int soThuTu;
    private String tenPhong;
    private List<ChiTietHoaDon> chiTiet = new ArrayList<>();

    public HoaDon() {}

    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }
    public int getMaPK() { return maPK; }
    public void setMaPK(int maPK) { this.maPK = maPK; }
    public Integer getMaTK() { return maTK; }
    public void setMaTK(Integer maTK) { this.maTK = maTK; }
    public String getTenNguoiLap() { return tenNguoiLap; }
    public void setTenNguoiLap(String tenNguoiLap) { this.tenNguoiLap = tenNguoiLap; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public BigDecimal getTongTien() { return tongTien == null ? BigDecimal.ZERO : tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public BigDecimal getGiamGia() { return giamGia == null ? BigDecimal.ZERO : giamGia; }
    public void setGiamGia(BigDecimal giamGia) { this.giamGia = giamGia; }
    public Integer getMaGiam() { return maGiam; }
    public void setMaGiam(Integer maGiam) { this.maGiam = maGiam; }
    public String getMaGiamCode() { return maGiamCode; }
    public void setMaGiamCode(String maGiamCode) { this.maGiamCode = maGiamCode; }
    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    public BigDecimal getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(BigDecimal tienKhachDua) { this.tienKhachDua = tienKhachDua; }
    public BigDecimal getTienThua() { return tienThua; }
    public void setTienThua(BigDecimal tienThua) { this.tienThua = tienThua; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String lyDoHuy) { this.lyDoHuy = lyDoHuy; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
    public String getMaBenhNhanHienThi() { return maBenhNhanHienThi; }
    public void setMaBenhNhanHienThi(String maBenhNhanHienThi) { this.maBenhNhanHienThi = maBenhNhanHienThi; }
    public BenhNhan getBenhNhan() { return benhNhan; }
    public void setBenhNhan(BenhNhan benhNhan) { this.benhNhan = benhNhan; }
    public LocalDate getNgayKham() { return ngayKham; }
    public void setNgayKham(LocalDate ngayKham) { this.ngayKham = ngayKham; }
    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public List<ChiTietHoaDon> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietHoaDon> chiTiet) { this.chiTiet = chiTiet; }

    /** Tạm tính = tổng tiền các dịch vụ (trước giảm giá). */
    public BigDecimal getTamTinh() {
        return getTongTien().add(getGiamGia());
    }

    /** Tổng tiền dịch vụ tính trực tiếp từ các dòng chi tiết. */
    public BigDecimal tinhTongDichVu() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : chiTiet) {
            tong = tong.add(ct.getThanhTien());
        }
        return tong;
    }

    public boolean chuaThanhToan() { return CHUA_THANH_TOAN.equals(trangThai); }
    public boolean daThanhToan() { return DA_THANH_TOAN.equals(trangThai); }
    public boolean daHuy() { return DA_HUY.equals(trangThai); }

    /** Số tiền còn phải thu (0 nếu đã thanh toán hoặc đã hủy). */
    public BigDecimal getConPhaiThu() {
        return chuaThanhToan() ? getTongTien() : BigDecimal.ZERO;
    }

    /** Tiền thừa dự kiến khi khách đưa một số tiền; null nếu chưa đủ. */
    public BigDecimal tinhTienThua(BigDecimal khachDua) {
        if (khachDua == null || khachDua.compareTo(getTongTien()) < 0) return null;
        return khachDua.subtract(getTongTien());
    }

    @Override
    public String toString() {
        return String.format("HD%03d", maHD);
    }
}
