package enums;

/** Các vai trò đăng nhập được hỗ trợ bởi hệ thống. */
public enum VaiTroHeThong {
    QUAN_TRI_VIEN("Quản trị viên"),
    QUAN_LY("Quản lý phòng khám"),
    LE_TAN("Lễ tân");

    private final String ten;
    VaiTroHeThong(String ten) { this.ten = ten; }
    public String ten() { return ten; }
}
