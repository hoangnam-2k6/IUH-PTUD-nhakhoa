package Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Giao dịch gắn với phiếu khám; doanh thu chỉ tính khi trạng thái Thành công. */
public record ThanhToan(int maTT, int maPK, BigDecimal soTien, String phuongThuc,
        String trangThai, LocalDateTime ngayThanhToan) { }
