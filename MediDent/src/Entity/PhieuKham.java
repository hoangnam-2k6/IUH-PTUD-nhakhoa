package Entity;

import java.time.LocalDate;

public class PhieuKham {
    private int maPK;
    private int maBN;
    private Integer maPhong;
    private LocalDate ngayKham;
    private int soThuTu;
    private String trangThai;
    private String tenBenhNhan;
    private String tenPhong;

    public PhieuKham() {}

    public int getMaPK() { return maPK; }
    public void setMaPK(int maPK) { this.maPK = maPK; }
    public int getMaBN() { return maBN; }
    public void setMaBN(int maBN) { this.maBN = maBN; }
    public Integer getMaPhong() { return maPhong; }
    public void setMaPhong(Integer maPhong) { this.maPhong = maPhong; }
    public LocalDate getNgayKham() { return ngayKham; }
    public void setNgayKham(LocalDate ngayKham) { this.ngayKham = ngayKham; }
    public int getSoThuTu() { return soThuTu; }
    public void setSoThuTu(int soThuTu) { this.soThuTu = soThuTu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenBenhNhan() { return tenBenhNhan; }
    public void setTenBenhNhan(String tenBenhNhan) { this.tenBenhNhan = tenBenhNhan; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
}
