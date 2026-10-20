/*
 * Quyen: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

public class Quyen {

    private String maQuyen;
    private String tenQuyen;

    /** Khởi tạo mã quyền và tên quyền dùng trong ma trận phân quyền. */
    public Quyen() {
    }

    /** Khởi tạo mã quyền và tên quyền dùng trong ma trận phân quyền. */
    public Quyen(String maQuyen, String tenQuyen) {
        this.maQuyen = maQuyen;
        this.tenQuyen = tenQuyen;
    }

    /** Đọc mã định danh quyền của đối tượng. */
    public String getMaQuyen() {
        return maQuyen;
    }

    /** Gán mã định danh quyền cho đối tượng. */
    public void setMaQuyen(String maQuyen) {
        this.maQuyen = maQuyen;
    }

    /** Lấy tên hiển thị của quyền. */
    public String getTenQuyen() {
        return tenQuyen;
    }

    /** Gán tên hiển thị của quyền. */
    public void setTenQuyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }
}
