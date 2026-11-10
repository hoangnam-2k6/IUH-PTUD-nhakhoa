package UI.QuanLy;



import DAO.ThongKeDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ChucNangThongKe {
    private final ThongKeDAO dao = new ThongKeDAO();

    public record KetQua(boolean doanhThu, String theo, List<Object[]> dong) {}

    public KetQua tai(String loai, LocalDate tu, LocalDate den) throws SQLException {
        if (loai == null || (!loai.equals("Doanh thu") && !loai.equals("Lượt khám")))
            throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");

        if ((tu == null) != (den == null) || (tu != null && tu.isAfter(den)))
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");

        String theo = tu == null ? "Tháng"
                : ChronoUnit.DAYS.between(tu, den) <= 31 ? "Ngày"
                : ChronoUnit.DAYS.between(tu, den) <= 730 ? "Tháng" : "Năm";

        boolean doanhThu = loai.equals("Doanh thu");
        return new KetQua(doanhThu, theo, dao.layBaoCao(doanhThu, theo, tu, den));
    }
}