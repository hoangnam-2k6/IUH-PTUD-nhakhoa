/*
 * YeuCauHoTro: mô hình dữ liệu dùng chung.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package Class;

import java.time.LocalDateTime;

public class YeuCauHoTro {

    private int maYC;
    private int maTK;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime ngayTao;
    private String trangThai;
    private String phanHoi;
    private LocalDateTime ngayXuLy;

    /** Khởi tạo dữ liệu yêu cầu hỗ trợ: người gửi, nội dung, thời gian và phản hồi. */
    public YeuCauHoTro() {
    }

    /** Khởi tạo dữ liệu yêu cầu hỗ trợ: người gửi, nội dung, thời gian và phản hồi. */
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

    /** Trả mã yêu cầu hỗ trợ. */
    public int getMaYC() {
        return maYC;
    }

    /** Gán mã yêu cầu hỗ trợ. */
    public void setMaYC(int maYC) {
        this.maYC = maYC;
    }

    /** Trả mã tài khoản liên kết. */
    public int getMaTK() {
        return maTK;
    }

    /** Gán mã tài khoản liên kết. */
    public void setMaTK(int maTK) {
        this.maTK = maTK;
    }

    /** Trả tiêu đề yêu cầu hỗ trợ. */
    public String getTieuDe() {
        return tieuDe;
    }

    /** Đặt tiêu đề yêu cầu hỗ trợ. */
    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    /** Đọc nội dung yêu cầu hỗ trợ. */
    public String getNoiDung() {
        return noiDung;
    }

    /** Cập nhật nội dung yêu cầu hỗ trợ. */
    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    /** Trả mốc thời gian tạo đối tượng. */
    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    /** Gán mốc thời gian tạo đối tượng. */
    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    /** Trả trạng thái hiện lưu của đối tượng. */
    public String getTrangThai() {
        return trangThai;
    }

    /** Cập nhật giá trị trạng thái của đối tượng. */
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    /** Lấy phần nội dung phản hồi đã ghi. */
    public String getPhanHoi() {
        return phanHoi;
    }

    /** Gán nội dung phản hồi cần lưu. */
    public void setPhanHoi(String phanHoi) {
        this.phanHoi = phanHoi;
    }

    /** Lấy thời điểm yêu cầu được xử lý. */
    public LocalDateTime getNgayXuLy() {
        return ngayXuLy;
    }

    /** Gán thời điểm xử lý yêu cầu. */
    public void setNgayXuLy(LocalDateTime ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }
}
