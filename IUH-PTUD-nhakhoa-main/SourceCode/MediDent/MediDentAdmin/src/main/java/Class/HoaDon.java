package Class;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Hóa đơn của một phiếu khám, dùng để xem trước và in. */
public final class HoaDon {
    public final int maHD;
    public final int maPK;
    public final int maBN;
    public final String tenBenhNhan;
    public final String sdt;
    public final String tenBacSi;
    public final LocalDateTime ngayLap;   // có thể null nếu CSDL chưa ghi
    public final String trangThai;
    public final List<ChiTietHoaDon> chiTiet = new ArrayList<>();

    public HoaDon(int maHD, int maPK, int maBN, String tenBenhNhan, String sdt,
            String tenBacSi, LocalDateTime ngayLap, String trangThai) {
        this.maHD = maHD;
        this.maPK = maPK;
        this.maBN = maBN;
        this.tenBenhNhan = tenBenhNhan;
        this.sdt = sdt;
        this.tenBacSi = tenBacSi;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
    }

    /** Tổng tiền tính từ các dòng chi tiết. */
    public BigDecimal tongTien() {
        BigDecimal sum = BigDecimal.ZERO;
        for (ChiTietHoaDon line : chiTiet) sum = sum.add(line.thanhTien());
        return sum;
    }
}
