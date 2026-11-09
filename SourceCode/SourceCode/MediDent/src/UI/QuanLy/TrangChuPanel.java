
package UI.QuanLy;

import javax.swing.JPanel;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.GiaoDien;

public final class TrangChuPanel extends JPanel {

    public TrangChuPanel() {
        setBackground(GiaoDien.BG);
    }

    public TrangChuPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        this();
    }

    public void refresh() {
        // Chua thuc hien chuc nang trang chu
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Trang chủ");
    }
}
