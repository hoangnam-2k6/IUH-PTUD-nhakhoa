
package UI.QuanLy;

import javax.swing.JPanel;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.GiaoDien;

public final class DichVuPanel extends JPanel {

    public DichVuPanel() {
        setBackground(GiaoDien.BG);
    }

    public DichVuPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        this();
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Dịch vụ nha khoa");
    }
}
