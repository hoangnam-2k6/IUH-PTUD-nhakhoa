package TienIch;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/** [CH3-UC035/UC050][CH4] Hàm thuần: quy tắc trạng thái dùng chung DAO và kiểm thử offline. */
public final class QuyTacNghiepVu {
    private QuyTacNghiepVu() { }
    private static final Map<String, Set<String>> CHUYEN_LICH = Map.of(
        "Chờ xác nhận", Set.of("Đã xác nhận", "Đã hủy"),
        "Đã xác nhận", Set.of("Đã đến", "Đã hủy"),
        "Đã đến", Set.of("Chờ khám", "Đã hủy"),
        "Chờ khám", Set.of("Đang khám", "Đã hủy"),
        "Đang khám", Set.of("Hoàn tất"));

    /** [CH3-UC035] Không cho lịch đã hủy/hoàn tất quay ngược trạng thái. */
    public static boolean choPhepChuyenLich(String cu, String moi) {
        return cu != null && moi != null && CHUYEN_LICH.getOrDefault(cu, Set.of()).contains(moi);
    }

    /** [CH3-UC050] Chỉ lập hóa đơn cho hồ sơ khám đã Hoàn tất. */
    public static boolean duocLapHoaDon(String trangThaiPhieu, BigDecimal soTien) {
        return "Hoàn tất".equals(trangThaiPhieu) && soTien != null && soTien.signum() > 0;
    }

    /** [CH3-UC050] Không coi thanh toán chuyển khoản/thẻ là thực thu trước khi đối soát. */
    public static boolean duocXacNhanThucThu(String trangThai, String phuongThuc, String maGiaoDich) {
        return "Chờ xử lý".equals(trangThai)
            && ("Chuyển khoản".equals(phuongThuc) || "Thẻ ngân hàng".equals(phuongThuc))
            && maGiaoDich != null && !maGiaoDich.trim().isEmpty() && maGiaoDich.trim().length() <= 100;
    }
}
