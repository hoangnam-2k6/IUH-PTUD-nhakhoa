package UI.QuanLy;

import UI.Chung.BaoCaoThongKePanel;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/** Báo cáo Quản lý dùng chung biểu đồ + DAO với Admin, chỉ không xem nhật ký/tài khoản. */
public final class BaoCaoThongKeGUI extends JPanel {
    public BaoCaoThongKeGUI() {
        setLayout(new BorderLayout());
        // Báo cáo dài được cuộn theo chiều dọc của vùng làm việc chung.
        add(GiaoDien.cuon(new BaoCaoThongKePanel(false)), BorderLayout.CENTER);
    }
}
