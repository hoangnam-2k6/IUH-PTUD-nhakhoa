/*
 * nhanvienquanly_gui: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public final class nhanvienquanly_gui extends JPanel {
    /** Gắn trang chủ Quản lý vào panel trung gian cho khung chính. */
    public nhanvienquanly_gui(KhoDuLieu db,TaiKhoan user) {
        setLayout(new BorderLayout());
        add(new TrangChuQuanLy(db,user),BorderLayout.CENTER);
    }
}
