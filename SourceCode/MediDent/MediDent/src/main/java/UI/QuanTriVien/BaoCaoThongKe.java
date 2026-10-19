/*
 * BaoCaoThongKe: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanTriVien;

import DAO.KhoDuLieu;
import UI.Chung.BaoCaoThongKePanel;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public class BaoCaoThongKe extends JPanel {

    /** Gắn bảng thống kê vào trang báo cáo thuộc khu vực Quản trị. */
    public BaoCaoThongKe(KhoDuLieu db) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        BaoCaoThongKePanel noiDung = new BaoCaoThongKePanel(true);
        add(GiaoDien.cuon(noiDung), BorderLayout.CENTER);
    }
}
