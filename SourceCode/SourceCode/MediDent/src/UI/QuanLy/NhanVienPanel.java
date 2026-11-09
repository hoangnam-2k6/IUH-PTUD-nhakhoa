
package UI.QuanLy;

import javax.swing.JPanel;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.GiaoDien;

public final class NhanVienPanel extends JPanel {

    public NhanVienPanel() {
        setBackground(GiaoDien.BG);
    }

    public NhanVienPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        this();
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Nhân viên");
    }
}
