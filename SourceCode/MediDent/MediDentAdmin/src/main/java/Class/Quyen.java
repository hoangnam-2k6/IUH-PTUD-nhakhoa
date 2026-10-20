package Class;

/**
 * Entity ánh xạ bảng dbo.Quyen.
 * maQuyen là khóa chính VARCHAR(30), ví dụ BENHNHAN, LICHHEN.
 */
public class Quyen {

    private String maQuyen;
    private String tenQuyen;

    public Quyen() {
    }

    public Quyen(String maQuyen, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen;
    }

    public String getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(String maQuyen) {
        this.maQuyen = maQuyen;
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }
}
