package UI.QuanLy;

import DAO.LichHenDAO;
import Entity.LichHen;
import validation.Regex_LichHen;
import java.sql.SQLException;
import java.util.List;

public class ChucNangLichHen {
    private final LichHenDAO dao = new LichHenDAO();

    public List<LichHen> tim(String tuKhoa) throws SQLException {
        return dao.timLichHen(tuKhoa);
    }

    public List<String[]> benhNhan() throws SQLException {
        return dao.danhSachBenhNhan();
    }

    public List<String[]> bacSi() throws SQLException {
        return dao.danhSachBacSi();
    }

    public void luu(LichHen lich, boolean laSua) throws SQLException {
        Regex_LichHen.kiemTra(lich);
        dao.luu(lich, laSua);
    }

    public void huy(int ma) throws SQLException {
        dao.huy(ma);
    }

    public void chuyenTrangThai(int ma, String trangThai) throws SQLException {
        dao.doiTrangThai(ma, trangThai);
    }
}