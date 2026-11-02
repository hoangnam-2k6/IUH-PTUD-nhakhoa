package Class;

/** Kết quả giao dịch tiếp nhận để báo lại cho lễ tân. */
public final class KetQuaTiepNhan {
    public final int maBenhNhan;
    public final int soKham;
    public final boolean hoSoMoi;

    public KetQuaTiepNhan(int maBenhNhan, int soKham, boolean hoSoMoi) {
        this.maBenhNhan = maBenhNhan;
        this.soKham = soKham;
        this.hoSoMoi = hoSoMoi;
    }
}
