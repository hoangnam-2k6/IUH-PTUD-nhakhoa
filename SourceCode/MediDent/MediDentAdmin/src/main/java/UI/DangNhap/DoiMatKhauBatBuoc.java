package UI.DangNhap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.Box;
import javax.swing.BoxLayout;


import Entity.TaiKhoan;
import DAO.TaiKhoanDAO;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Chặn vào ứng dụng cho đến khi người dùng đổi mật khẩu tạm.
 * Mật khẩu mặc định theo ngày sinh chỉ dành cho demo, không phù hợp môi trường thật.
 */
final class DoiMatKhauBatBuoc {
    private DoiMatKhauBatBuoc() { }

    static boolean thucHien(Component cha, TaiKhoanDAO dao, TaiKhoan user) {
        JPasswordField hienTai = new JPasswordField(20);
        JPasswordField moi = new JPasswordField(20);
        JPasswordField xacNhan = new JPasswordField(20);
        JLabel loiCu = thongBao();
        JLabel loiMoi = thongBao();
        JLabel loiXacNhan = thongBao();
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 6));
        form.add(new JLabel("Đổi mật khẩu trước khi tiếp tục."));
        form.add(new JLabel("Mật khẩu hiện tại"));
        form.add(hienTai);
        form.add(loiCu);
        form.add(new JLabel("Mật khẩu mới"));
        form.add(moi);
        form.add(loiMoi);
        form.add(new JLabel("Nhập lại mật khẩu mới"));
        form.add(xacNhan);
        form.add(loiXacNhan);

        Runnable kiemTraCu = () -> loiCu.setText(hienTai.getPassword().length == 0
                ? "Vui lòng nhập mật khẩu hiện tại." : "");
        Runnable kiemTraMoi = () -> loiMoi.setText(
                Valid.MatKhauHopLe.kiemTra(new String(moi.getPassword())) ? ""
                        : Valid.MatKhauHopLe.huongDan());
        Runnable kiemTraXacNhan = () -> loiXacNhan.setText(
                xacNhan.getPassword().length > 0
                        && Arrays.equals(moi.getPassword(), xacNhan.getPassword())
                        ? "" : "Mật khẩu nhập lại không khớp.");
        batLoiKhiRoiO(hienTai, kiemTraCu);
        batLoiKhiRoiO(moi, kiemTraMoi);
        batLoiKhiRoiO(xacNhan, kiemTraXacNhan);
        form.setFocusable(true);
        java.awt.event.MouseAdapter roiO = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                form.requestFocusInWindow();
            }
        };
        form.addMouseListener(roiO);
        for (Component component : form.getComponents()) {
            if (component instanceof JLabel) component.addMouseListener(roiO);
        }

        while (true) {
            int answer = JOptionPane.showConfirmDialog(cha, form, "Đổi mật khẩu bắt buộc",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return false;
            kiemTraCu.run();
            kiemTraMoi.run();
            kiemTraXacNhan.run();
            if (!loiCu.getText().isEmpty() || !loiMoi.getText().isEmpty()
                    || !loiXacNhan.getText().isEmpty()) continue;
            char[] old = hienTai.getPassword();
            char[] next = moi.getPassword();
            try {
                dao.doiMatKhauLanDau(user.id, new String(old), new String(next));
                user.phaiDoiMatKhau = false;
                JOptionPane.showMessageDialog(cha, "Đổi mật khẩu thành công.");
                return true;
            } catch (java.sql.SQLException ex) {
                String message = ex.getMessage();
                if (message != null && message.contains("Mật khẩu hiện tại")) {
                    loiCu.setText(message);
                } else if (message != null && message.contains("Mật khẩu mới")) {
                    loiMoi.setText(message);
                } else {
                    GiaoDienDoiMatKhauBatBuoc.loi(cha, ex);
                }
            } finally {
                Arrays.fill(old, '\0');
                Arrays.fill(next, '\0');
            }
        }
    }

    private static JLabel thongBao() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(0xDC2626));
        return label;
    }

    private static void batLoiKhiRoiO(JPasswordField field, Runnable check) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { check.run(); }
        });
        field.addActionListener(e -> check.run());
    }

}


/** Thông báo lỗi ngắn, không xây dựng thêm lớp component giao diện. */
final class GiaoDienDoiMatKhauBatBuoc {
    static void loi(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, ex.getMessage(),
                "Không thể đổi mật khẩu", JOptionPane.ERROR_MESSAGE);
    }
}
