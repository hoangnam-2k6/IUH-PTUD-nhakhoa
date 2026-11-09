package Entity;
/** Thông tin dịch vụ nha khoa; giá sử dụng BigDecimal. */
public record DichVu(int maDV, String tenDV, java.math.BigDecimal donGia, String trangThai, String moTa) {
}
