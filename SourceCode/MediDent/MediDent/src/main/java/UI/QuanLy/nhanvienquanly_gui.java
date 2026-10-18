package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Tương thích tên class của nhóm Quản lý.
 * Lưu ý: class này KHÔNG tạo JFrame; KhungChinh chứa sidebar/header dùng chung.
 */
public final class nhanvienquanly_gui extends JPanel {
    public nhanvienquanly_gui(KhoDuLieu db,TaiKhoan user) {
        setLayout(new BorderLayout());
        add(new TrangChuQuanLy(db,user),BorderLayout.CENTER);
    }
}
