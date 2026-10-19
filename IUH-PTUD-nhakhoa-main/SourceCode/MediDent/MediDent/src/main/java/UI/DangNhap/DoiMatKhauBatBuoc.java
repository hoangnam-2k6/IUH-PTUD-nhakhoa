/*
 * DoiMatKhauBatBuoc: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.DangNhap;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.Chung.GiaoDien;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

final class DoiMatKhauBatBuoc {
    /** Dựng màn hình buộc thay mật khẩu tạm trước khi tiếp tục đăng nhập. */
    private DoiMatKhauBatBuoc() { }

    static boolean thucHien(Component cha, KhoDuLieu dao, TaiKhoan user) {
        while (true) {
            JPasswordField hienTai = new JPasswordField(20);
            JPasswordField moi = new JPasswordField(20);
            JPasswordField xacNhan = new JPasswordField(20);
            JPanel f = new JPanel(new GridLayout(0, 1, 5, 7));
            f.add(new JLabel("Tài khoản cần đổi mật khẩu trước khi tiếp tục."));
            f.add(GiaoDien.label("Mật khẩu hiện tại (mật khẩu tạm)", 12, true));
            f.add(hienTai);
            f.add(GiaoDien.label("Mật khẩu mới (ít nhất 10 ký tự, chữ hoa và số)", 12, true));
            f.add(moi);
            f.add(GiaoDien.label("Nhập lại mật khẩu mới", 12, true));
            f.add(xacNhan);
            int answer = JOptionPane.showConfirmDialog(cha, f, "Đổi mật khẩu bắt buộc",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return false;
            char[] old = hienTai.getPassword();
            char[] next = moi.getPassword();
            char[] confirm = xacNhan.getPassword();
            try {
                if (!Arrays.equals(next, confirm)) {
                    JOptionPane.showMessageDialog(cha, "Mật khẩu nhập lại không khớp.");
                    continue;
                }
                dao.doiMatKhauLanDau(user.id, new String(old), new String(next));
                user.phaiDoiMatKhau = false;
                JOptionPane.showMessageDialog(cha, "Đã đổi mật khẩu. Bạn có thể tiếp tục.");
                return true;
            } catch (Exception ex) {
                GiaoDien.loi(cha, ex);
            } finally {
                Arrays.fill(old, '\0');
                Arrays.fill(next, '\0');
                Arrays.fill(confirm, '\0');
                hienTai.setText("");
                moi.setText("");
                xacNhan.setText("");
            }
        }
    }
}
