package Entity;

public class PhongKham {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private int maPhong;
    private String tenPhong;
    private int sucChua = 20;
    private String trangThai = HOAT_DONG;

    public PhongKham() {}

    public PhongKham(int maPhong, String tenPhong) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
    }

    public PhongKham(int maPhong, String tenPhong, int sucChua, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.sucChua = sucChua;
        this.trangThai = trangThai;
    }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public int getSucChua() { return sucChua; }
    public void setSucChua(int sucChua) { this.sucChua = sucChua; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return HOAT_DONG.equals(trangThai); }

    @Override
    public String toString() {
        return tenPhong;
    }
}
