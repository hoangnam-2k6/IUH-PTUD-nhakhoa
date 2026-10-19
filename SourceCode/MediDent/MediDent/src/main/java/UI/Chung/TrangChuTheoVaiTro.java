/*
 * TrangChuTheoVaiTro: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.Chung;

import Class.TaiKhoan;
import java.awt.BorderLayout;
import javax.swing.JPanel;

public class TrangChuTheoVaiTro extends JPanel {

    /** Chọn trang chủ thích hợp cho vai trò trong cấu trúc điều hướng hiện tại. */
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
