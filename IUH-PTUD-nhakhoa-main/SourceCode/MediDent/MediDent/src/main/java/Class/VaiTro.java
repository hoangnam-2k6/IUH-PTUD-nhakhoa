/*
 * VaiTro: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

public class VaiTro {

    public int id;
    public String ten;
    public String moTa;
    public boolean[] quyen;

    /** Khởi tạo thông tin vai trò và mảng quyền tương ứng để hiển thị. */
    public VaiTro(int id, String ten, String moTa, boolean[] quyen) {
        this.id = id;
        this.ten = ten;
        this.moTa = moTa;
        this.quyen = quyen;
    }

    @Override
    /** Trả chuỗi tên để hiển thị đối tượng trên giao diện. */
    public String toString() {
        return ten;
    }
}
