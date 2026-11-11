package Entity;

/** Hồ sơ lễ tân kế thừa thông tin nhân viên, riêng ca làm việc của lễ tân. */
public class LeTan extends NhanVien {
    private String caLamViec;
    public LeTan() { super(); }
    public String layCaLamViec() { return caLamViec; }
    public void datCaLamViec(String caLamViec) { this.caLamViec = caLamViec; }
}
