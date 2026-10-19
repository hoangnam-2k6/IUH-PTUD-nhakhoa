/*
 * CauHinh: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

public class CauHinh {

    private String khoa;
    private String giaTri;

    /** Khởi tạo đối tượng cấu hình gồm khóa và giá trị tương ứng trong bảng CauHinh. */
    public CauHinh() {
    }

    /** Khởi tạo đối tượng cấu hình gồm khóa và giá trị tương ứng trong bảng CauHinh. */
    public CauHinh(String khoa, String giaTri) {
        this.khoa = khoa;
        this.giaTri = giaTri;
    }

    /** Trả tên khóa cấu hình. */
    public String getKhoa() {
        return khoa;
    }

    /** Đặt tên khóa cấu hình. */
    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    /** Lấy giá trị cấu hình đang lưu. */
    public String getGiaTri() {
        return giaTri;
    }

    /** Gán giá trị cấu hình. */
    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }
}
