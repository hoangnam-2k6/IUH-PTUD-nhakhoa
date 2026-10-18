package UI.QuanLy;

import Class.TaiKhoan;
import UI.Chung.BaoCaoThongKePanel;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/** Báo cáo Quản lý dùng chung biểu đồ + DAO với Admin, chỉ không xem nhật ký/tài khoản. */
public final class BaoCaoThongKeGUI extends JPanel {
    /** Mở đúng báo cáo Quản lý với ID tài khoản đang đăng nhập. */
    public BaoCaoThongKeGUI(TaiKhoan user) {
        setLayout(new BorderLayout());
        add(GiaoDien.cuon(new BaoCaoThongKePanel(false, user.id)), BorderLayout.CENTER);
    }

    /** Cho phép các lớp cũ vẫn khởi tạo màn hình (chỉ xem, không ghi chi phí). */
    public BaoCaoThongKeGUI() {
        setLayout(new BorderLayout());
        add(GiaoDien.cuon(new BaoCaoThongKePanel(false)), BorderLayout.CENTER);
    }
}
