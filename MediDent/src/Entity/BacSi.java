package Entity;

public class BacSi {
    private int maBS;
    private String hoTen;
    private String chuyenKhoa;

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

    @Override
    public String toString() {
        return hoTen;
    }
}
