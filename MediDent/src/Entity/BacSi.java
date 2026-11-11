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

    public int getMaBS() { return maBS; }
    public void setMaBS(int maBS) { this.maBS = maBS; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getChuyenKhoa() { return chuyenKhoa; }
    public void setChuyenKhoa(String chuyenKhoa) { this.chuyenKhoa = chuyenKhoa; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean dangHoatDong() { return HOAT_DONG.equals(trangThai); }

    @Override
    public String toString() {
        return hoTen;
    }
}
