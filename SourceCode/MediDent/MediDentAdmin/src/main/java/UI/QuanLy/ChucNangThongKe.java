package UI.QuanLy;


import DAO.ThongKeMoRongDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ChucNangThongKe {
    private final ThongKeMoRongDAO dao = new ThongKeMoRongDAO();

    public record BaoCao(ThongKeMoRongDAO.Bang bang, String nhom) {}

    // Kiểm tra ngày, xác định kiểu nhóm và lấy báo cáo.
    public BaoCao tai(String loai, LocalDate tu, LocalDate den) throws SQLException {
        if ((tu == null) != (den == null) || (tu != null && tu.isAfter(den))) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        }

        String nhom = "Tháng";

        if (tu != null) {
            long soNgay = ChronoUnit.DAYS.between(tu, den);
            nhom = soNgay <= 31 ? "Ngày" : soNgay <= 730 ? "Tháng" : "Năm";
        }

        return new BaoCao(dao.baoCao(loai, nhom, tu, den), nhom);
    }
}
