/*
 * VaiTroQuyen: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

public class VaiTroQuyen {

    private int maVaiTro;
    private String maQuyen;

    /** Tạo liên kết giữa mã vai trò và mã quyền được cấp. */
    public VaiTroQuyen() {
    }

    /** Tạo liên kết giữa mã vai trò và mã quyền được cấp. */
    public VaiTroQuyen(int maVaiTro, String maQuyen) {
        this.maVaiTro = maVaiTro;
        this.maQuyen = maQuyen;
    }

    /** Trả mã khóa chính của vai trò. */
    public int getMaVaiTro() {
        return maVaiTro;
    }

    /** Gán mã khóa chính của vai trò. */
    public void setMaVaiTro(int maVaiTro) {
        this.maVaiTro = maVaiTro;
    }

    /** Đọc mã định danh quyền của đối tượng. */
    public String getMaQuyen() {
        return maQuyen;
    }

    /** Gán mã định danh quyền cho đối tượng. */
    public void setMaQuyen(String maQuyen) {
        this.maQuyen = maQuyen;
    }
}
