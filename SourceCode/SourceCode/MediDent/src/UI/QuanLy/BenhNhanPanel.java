
package UI.QuanLy;

import javax.swing.JPanel;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.GiaoDien;

public final class BenhNhanPanel extends JPanel {

    public BenhNhanPanel() {
        setBackground(GiaoDien.BG);
    }

    public BenhNhanPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        this();
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Bệnh nhân");
    }
}
