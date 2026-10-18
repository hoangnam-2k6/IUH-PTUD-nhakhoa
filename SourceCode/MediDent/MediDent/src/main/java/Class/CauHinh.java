package Class;

/**
 * Entity ánh xạ dbo.CauHinh.
 * Cột SQL "Khoa" (VARCHAR(60)) là khóa chính; "GiaTri" là NVARCHAR(500).
 */
public class CauHinh {

    private String khoa;
    private String giaTri;

    public CauHinh() {
    }

    public CauHinh(String khoa, String giaTri) {
        this.khoa = khoa;
        this.giaTri = giaTri;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }
}
