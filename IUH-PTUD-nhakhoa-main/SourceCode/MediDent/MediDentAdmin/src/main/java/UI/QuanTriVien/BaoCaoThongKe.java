package UI.QuanTriVien;

import DAO.KhoDuLieu;
import UI.Chung.BaoCaoThongKePanel;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Báo cáo dành cho Quản trị viên.
 *
 * Dùng cùng giao diện và truy vấn với trang báo cáo Quản lý.
 * Riêng Quản trị viên có thêm phân bố tài khoản và nhật ký hệ thống.
 */
public class BaoCaoThongKe extends JPanel {

    /** Giữ đúng constructor mà KhungChinh.java đang gọi. */
    public BaoCaoThongKe(KhoDuLieu db) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        // Dữ liệu báo cáo được đọc từ SQL Server qua BaoCaoDuLieuDAO.
        BaoCaoThongKePanel noiDung = new BaoCaoThongKePanel(true);
        add(GiaoDien.cuon(noiDung), BorderLayout.CENTER);
    }
}
