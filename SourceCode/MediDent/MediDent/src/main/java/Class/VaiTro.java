package Class;

/**
 * Mô hình VaiTro và sáu nhóm quyền.
 * Danh sách quyền được đọc từ các bảng VaiTroQuyen và Quyen của SQL Server.
 */
public class VaiTro {

    public int id;
    public String ten;
    public String moTa;
    public boolean[] quyen;

    public VaiTro(int id, String ten, String moTa, boolean[] quyen) {
        this.id = id;
        this.ten = ten;
        this.moTa = moTa;
        this.quyen = quyen;
    }

    /** Hiển thị tên vai trò trong JComboBox. */
    @Override
    public String toString() {
        return ten;
    }
}
