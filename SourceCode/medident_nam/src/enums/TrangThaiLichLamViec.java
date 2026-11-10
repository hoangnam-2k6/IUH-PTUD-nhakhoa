package enums;

public enum TrangThaiLichLamViec {
    HOAT_DONG("Hoạt động"), NGHI_PHEP("Nghỉ phép"), DA_HUY("Đã hủy");
    private final String nhan;
    TrangThaiLichLamViec(String nhan) { this.nhan = nhan; }
    @Override public String toString() { return nhan; }
}
