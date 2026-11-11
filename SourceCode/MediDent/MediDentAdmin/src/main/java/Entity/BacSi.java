package Entity;

public class BacSi {
    public static final String HOAT_DONG = "Hoạt động";
    public static final String NGUNG_HOAT_DONG = "Ngừng hoạt động";

    private int maBS;
    private String hoTen;
    private String chuyenKhoa;
    private String trangThai = HOAT_DONG;

    public BacSi() {}

    public BacSi(int maBS, String hoTen, String chuyenKhoa) {
        this.maBS = maBS;
        this.hoTen = hoTen;
        this.chuyenKhoa = chuyenKhoa;
    }

    public int layMaBS() { return maBS; }
    public void datMaBS(int maBS) { this.maBS = maBS; }
    public String layHoTen() { return hoTen; }
    public void datHoTen(String hoTen) { this.hoTen = hoTen; }
    public String layChuyenKhoa() { return chuyenKhoa; }
    public void datChuyenKhoa(String chuyenKhoa) { this.chuyenKhoa = chuyenKhoa; }
    public String layTrangThai() { return trangThai; }
    public void datTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return HOAT_DONG.equals(trangThai); }

    @Override
    public String toString() {
        return hoTen;
    }
}
