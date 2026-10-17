package Class;

/**
 * Entity ánh xạ dbo.VaiTroQuyen (bảng liên kết nhiều-nhiều).
 * Khóa chính là cặp (maVaiTro, maQuyen).
 * maVaiTro tham chiếu dbo.VaiTro; maQuyen tham chiếu dbo.Quyen.
 */
public class VaiTroQuyen {

    private int maVaiTro;
    private String maQuyen;

    public VaiTroQuyen() {
    }

    public VaiTroQuyen(int maVaiTro, String maQuyen) {
        this.maVaiTro = maVaiTro;
        this.maQuyen = maQuyen;
    }

    public int getMaVaiTro() {
        return maVaiTro;
    }

    public void setMaVaiTro(int maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    public String getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(String maQuyen) {
        this.maQuyen = maQuyen;
    }
}
