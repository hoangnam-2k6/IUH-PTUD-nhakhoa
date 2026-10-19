/*
 * BaoCaoThongKeGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import UI.Chung.BaoCaoThongKePanel;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public final class BaoCaoThongKeGUI extends JPanel {

    /** Gắn trang thống kê vào màn hình báo cáo của Quản lý. */
    public BaoCaoThongKeGUI(TaiKhoan user) {
        setLayout(new BorderLayout());
        add(GiaoDien.cuon(new BaoCaoThongKePanel(false, user.id)), BorderLayout.CENTER);
    }

    /** Gắn trang thống kê vào màn hình báo cáo của Quản lý. */
    public BaoCaoThongKeGUI() {
        setLayout(new BorderLayout());
        add(GiaoDien.cuon(new BaoCaoThongKePanel(false)), BorderLayout.CENTER);
    }
}
