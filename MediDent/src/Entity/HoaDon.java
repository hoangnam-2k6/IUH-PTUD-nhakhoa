package Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private int maHD;
    private int maPK;
    private LocalDateTime ngayLap;
    private BigDecimal tongTien = BigDecimal.ZERO;   // số tiền phải trả (đã trừ giảm giá)
    private BigDecimal giamGia = BigDecimal.ZERO;
    private String phuongThucTT;
    private LocalDateTime ngayThanhToan;
    private String trangThai;
    private String tenBenhNhan;

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
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public BigDecimal getGiamGia() { return giamGia; }
    public void setGiamGia(BigDecimal giamGia) { this.giamGia = giamGia; }
    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
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
        return tongTien.add(giamGia);
    }
}
