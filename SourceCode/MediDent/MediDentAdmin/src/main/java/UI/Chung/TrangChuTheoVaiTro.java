package UI.Chung;

import Class.TaiKhoan;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Trang chủ đơn giản dành cho những vai trò không phải Quản trị viên.
 * Không hiển thị số liệu quản trị nội bộ cho tài khoản không có quyền.
 */
public class TrangChuTheoVaiTro extends JPanel {

    public TrangChuTheoVaiTro(TaiKhoan user) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Trang chủ " + user.tenVaiTro,
                "Chào mừng " + user.hoTen
                + ". Các chức năng được mở theo quyền cấp trong cơ sở dữ liệu.");
        JPanel card = GiaoDien.card(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.add(GiaoDien.label("Vai trò: " + user.tenVaiTro, 16, true),
                BorderLayout.NORTH);
        card.add(GiaoDien.label("Các module khám bệnh, lịch hẹn và thanh toán "
                + "của từng vai trò cần tích hợp thêm với CSDL nghiệp vụ.", 12, false),
                BorderLayout.CENTER);
        page.add(card);
        add(GiaoDien.cuon(page), BorderLayout.CENTER);
    }
}
