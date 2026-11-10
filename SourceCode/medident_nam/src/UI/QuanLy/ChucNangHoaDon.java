package UI.QuanLy;

import DAO.HoaDonDAO;
import validation.Regex_HoaDon;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ChucNangHoaDon {
    private final HoaDonDAO dao = new HoaDonDAO();

    public List<HoaDonDAO.DongHoaDon> tim(String tuKhoa, LocalDate tu, LocalDate den)
            throws SQLException {
        return dao.timHoaDonTheoNgay(tuKhoa, tu, den);
    }

    public List<String> choDuyet() throws SQLException {
        return dao.yeuCauChoDuyet();
    }

    public void duyet(int maYC, boolean chapNhan) throws SQLException {
        Regex_HoaDon.kiemTraMa(maYC);
        dao.duyetHuy(maYC, chapNhan);
    }

    // Dành cho màn hình Lễ tân gửi yêu cầu hủy, không gọi ở Quản lý.
    public void guiYeuCauHuy(int maHD, String lyDo) throws SQLException {
        Regex_HoaDon.kiemTraMa(maHD);
        Regex_HoaDon.kiemTraLyDo(lyDo);
        dao.guiYeuCauHuy(maHD, lyDo);
    }
}