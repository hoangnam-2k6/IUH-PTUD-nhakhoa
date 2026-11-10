package enums;

public enum TrangThaiLichHen {
    CHO_XAC_NHAN("Chờ xác nhận"), DA_XAC_NHAN("Đã xác nhận"), DA_KHAM("Đã khám"), DA_HUY("Đã hủy");
    private final String nhan;
    TrangThaiLichHen(String nhan) { this.nhan = nhan; }
    @Override public String toString() { return nhan; }
}
