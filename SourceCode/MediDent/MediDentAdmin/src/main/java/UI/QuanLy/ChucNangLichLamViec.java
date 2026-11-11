package UI.QuanLy;

import DAO.LichLamViecDAO;
import Entity.LichLamViec;
import validation.Regex_LichLamViec;
import java.sql.SQLException;
import java.util.List;

public class ChucNangLichLamViec {
    private final LichLamViecDAO dao = new LichLamViecDAO();

    public List<LichLamViec> tim(String tuKhoa) throws SQLException {
        return dao.danhSach(tuKhoa);
    }

    public List<String[]> nhanVien() throws SQLException {
        return dao.danhSachNhanVien();
    }

    public void luu(LichLamViec lich, boolean laSua) throws SQLException {
        Regex_LichLamViec.kiemTra(lich);
        dao.luu(lich, laSua);
    }

    public void huy(int ma) throws SQLException {
        dao.huy(ma);
    }
}
