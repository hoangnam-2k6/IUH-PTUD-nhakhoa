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

    public int layMaPhong() { return maPhong; }
    public void datMaPhong(int maPhong) { this.maPhong = maPhong; }
    public String layTenPhong() { return tenPhong; }
    public void datTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public int laySucChua() { return sucChua; }
    public void datSucChua(int sucChua) { this.sucChua = sucChua; }
    public String layTrangThai() { return trangThai; }
    public void datTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return HOAT_DONG.equals(trangThai); }

    @Override
    public String toString() {
        return tenPhong;
    }
}
