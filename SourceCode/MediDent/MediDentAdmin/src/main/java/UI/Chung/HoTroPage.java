package UI.Chung;

import Class.TaiKhoan;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Trang hướng dẫn sử dụng, không gửi yêu cầu hỗ trợ. */
public final class HoTroPage extends JPanel {
    private static final Color NEN = new Color(0xF8FAFC);
    private static final Color CHU = new Color(0x0F172A);
    private static final Color CHU_PHU = new Color(0x475569);

    public HoTroPage(TaiKhoan taiKhoan) {
        setLayout(new BorderLayout());
        setBackground(NEN);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBackground(NEN);
        noiDung.setBorder(BorderFactory.createEmptyBorder(30, 34, 35, 34));
        themDong(noiDung, "Hướng dẫn sử dụng MediDent", 24, true);
        themDong(noiDung, "Thông tin nghiệp vụ và cách thao tác trong ứng dụng", 13, false);
        noiDung.add(Box.createVerticalStrut(20));

        themMuc(noiDung, "Đăng nhập và tài khoản",
                "Dùng tên đăng nhập và mật khẩu đã được cấp. Sau khi đăng nhập, "
              + "chương trình mở Trang chủ và hiển thị các chức năng theo vai trò.");
        themMuc(noiDung, "Quên mật khẩu",
                "Tại màn hình đăng nhập, chọn Quên mật khẩu, nhập tên đăng nhập "
              + "và ngày sinh để đặt lại mật khẩu. Đăng nhập bằng mật khẩu tạm "
              + "và đổi sang mật khẩu mới khi được yêu cầu.");
        themMuc(noiDung, "Menu chức năng",
                "Chọn chức năng ở thanh menu bên trái để mở trang. "
              + "Mục đang xem sẽ có nền xanh; các mục không được phân quyền sẽ không hiển thị.");

        if ("Quản trị viên".equals(taiKhoan.tenVaiTro)) {
            themMuc(noiDung, "Quản lý tài khoản",
                    "Chọn Quản lý tài khoản để tìm kiếm, thêm, sửa hoặc khóa tài khoản. "
                  + "Khi bấm Thêm hoặc Sửa, nhập thông tin trên trang riêng, "
                  + "chọn Lưu thông tin hoặc Quay lại.");
            themMuc(noiDung, "Phân quyền tài khoản",
                    "Chọn vai trò cần chỉnh sửa, đánh dấu các quyền được phép sử dụng "
                  + "bằng ô chọn và lưu. Khi đăng nhập lại, menu sẽ áp dụng quyền mới.");
            themMuc(noiDung, "Trang chủ quản trị viên",
                    "Xem tổng số tài khoản, số tài khoản hoạt động, tạm khóa "
                  + "và lịch sử đăng nhập.");
        } else if ("Quản lý phòng khám".equals(taiKhoan.tenVaiTro)) {
            themMuc(noiDung, "Nghiệp vụ Quản lý",
                    "Các nhóm nghiệp vụ gồm nhân viên, dịch vụ, bệnh nhân, "
                  + "lịch hẹn, thanh toán và báo cáo. Những màn hình chưa hoàn thiện "
                  + "sẽ được bổ sung vào dự án sau.");
        } else if ("Lễ tân".equals(taiKhoan.tenVaiTro)) {
            themMuc(noiDung, "Nghiệp vụ Lễ tân",
                    "Các nhóm nghiệp vụ gồm tiếp nhận, khám và điều trị, "
                  + "lịch hẹn, thanh toán và tra cứu bệnh nhân. Những màn hình "
                  + "chưa hoàn thiện sẽ được bổ sung vào dự án sau.");
        }
        themMuc(noiDung, "Đăng xuất", "Chọn Đăng xuất ở cuối menu khi kết thúc làm việc.");

        JScrollPane cuon = new JScrollPane(noiDung);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(NEN);
        cuon.getVerticalScrollBar().setUnitIncrement(18);
        add(cuon, BorderLayout.CENTER);
    }

    private void themMuc(JPanel noiDung, String tieuDe, String huongDan) {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(16, 20, 17, 20)));
        the.setAlignmentX(LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        themDong(the, tieuDe, 15, true);
        the.add(Box.createVerticalStrut(8));
        javax.swing.JTextArea moTa = new javax.swing.JTextArea(huongDan);
        moTa.setLineWrap(true);
        moTa.setWrapStyleWord(true);
        moTa.setEditable(false);
        moTa.setFocusable(false);
        moTa.setOpaque(false);
        moTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        moTa.setForeground(CHU_PHU);
        moTa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        the.add(moTa);
        noiDung.add(the);
        noiDung.add(Box.createVerticalStrut(12));
    }

    private void themDong(JPanel noiDung, String chu, int co, boolean dam) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, co));
        nhan.setForeground(dam ? CHU : CHU_PHU);
        nhan.setAlignmentX(LEFT_ALIGNMENT);
        noiDung.add(nhan);
    }
}
