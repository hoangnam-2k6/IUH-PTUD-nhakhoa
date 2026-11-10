package Entity;

import java.time.LocalDateTime;

/** Phiếu khám phát sinh khi lịch hẹn đã được xác nhận là đã khám. */
public record PhieuKham(int maPK, String maBN, String maNV, Integer maLichHen,
        LocalDateTime ngayKham, String trangThai, String ghiChu) { }
