package Class;

import java.time.LocalDateTime;

/**
 * Entity ánh xạ dbo.YeuCauHoTro.
 * maYC, ngayTao và trạng thái mặc định được SQL Server sinh khi INSERT.
 * ngayXuLy/phanHoi có thể để null trước khi Admin phản hồi.
 */
public class YeuCauHoTro {

    private int maYC;
    private int maTK;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String phanHoi;
    private LocalDateTime ngayXuLy;

    public YeuCauHoTro() {
    }

    public YeuCauHoTro(int maYC, int maTK, String tieuDe, String noiDung,
            LocalDateTime ngayTao, String trangThai, String phanHoi,
            LocalDateTime ngayXuLy) {
        this.maYC = maYC;
        this.maTK = maTK;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.phanHoi = phanHoi;
        this.ngayXuLy = ngayXuLy;
    }

    public int getMaYC() {
        return maYC;
    }

    public void setMaYC(int maYC) {
        this.maYC = maYC;
    }

    public int getMaTK() {
        return maTK;
    }

    public void setMaTK(int maTK) {
        this.maTK = maTK;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getPhanHoi() {
        return phanHoi;
    }

    public void setPhanHoi(String phanHoi) {
        this.phanHoi = phanHoi;
    }

    public LocalDateTime getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(LocalDateTime ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }
}
