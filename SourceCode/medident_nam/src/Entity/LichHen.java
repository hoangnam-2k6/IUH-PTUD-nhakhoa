package Entity;

import java.time.LocalDate;
import java.time.LocalTime;

/** Mã bệnh nhân và nhân viên dùng NVARCHAR(20) như BN001, NV001. */
public record LichHen(int maLichHen, String maBN, String maNV, LocalDate ngayHen,
        LocalTime gioHen, int thoiLuongPhut, String phongKham, String trangThai) { }
