package Entity;

public class PhongKham {
    private int maPhong;
    private String tenPhong;

    public PhongKham() {}

    public PhongKham(int maPhong, String tenPhong) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
    }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    @Override
    public String toString() {
        return tenPhong;
    }
}
