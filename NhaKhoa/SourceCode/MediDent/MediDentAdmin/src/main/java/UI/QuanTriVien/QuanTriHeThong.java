package UI.QuanTriVien;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import TienIch.EmailHopLe;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Màn hình quản trị cấu hình hệ thống.
 *
 * Các trường được đọc/lưu trong bảng CauHinh; tùy chọn đúng/sai dùng checkbox
 * để tránh người dùng gõ sai. Lưu cấu hình chỉ cập nhật CSDL, không tự nhận
 * rằng chức năng 2FA hoặc sao lưu nền đã được triển khai.
 */
public class QuanTriHeThong extends JPanel {

    private final KhoDuLieu db;
    private final TaiKhoan actor;
    private final Map<String, JComponent> controls = new LinkedHashMap<>();

    public QuanTriHeThong(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Quản trị hệ thống",
                "Cấu hình thông tin, bảo mật và thông báo toàn hệ thống.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        JPanel card = GiaoDien.card(new BorderLayout(0, 15));
        JPanel fields = new JPanel(new GridLayout(0, 2, 12, 15));
        fields.setOpaque(false);
        card.add(GiaoDien.label("Thông tin hệ thống và cấu hình vận hành", 15, true),
                BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);

        // 1. Cấu hình được khởi tạo từ SQL Server.
        try {
            Map<String, String> config = db.cauHinh();
            addField(fields, "Tên hệ thống", "TenHeThong", config);
            addField(fields, "Mã đơn vị", "MaDonVi", config);
            addField(fields, "Email quản trị", "EmailQuanTri", config);
            addField(fields, "Số điện thoại hỗ trợ", "SoDienThoai", config);
            addField(fields, "Địa chỉ", "DiaChi", config);
            addBoolean(fields, "Xác thực hai lớp (2FA)", "Bat2FA", config);
            addBoolean(fields, "Tự động khóa phiên", "KhoaPhien", config);
            addBoolean(fields, "Thông báo đổi phân quyền", "ThongBaoQuyen", config);
            addBoolean(fields, "Sao lưu tự động", "SaoLuuTuDong", config);
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
        page.add(card);
        page.add(Box.createVerticalStrut(16));

        // 2. Tải lại/lưu. Không dùng ký tự ✓ làm biểu tượng.
        JPanel actions = GiaoDien.hang();
        JButton reload = GiaoDien.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        JButton save = GiaoDien.nutIcon("Lưu cấu hình", true, BieuTuong.Loai.LUU);
        actions.add(reload);
        actions.add(save);
        page.add(actions);
        reload.addActionListener(event -> taiLai());
        save.addActionListener(event -> luu());
    }

    /** 3. Field có kiểu String. */
    private void addField(JPanel panel, String title, String key, Map<String, String> config) {
        panel.add(GiaoDien.label(title, 12, true));
        JTextField field = GiaoDien.text(config.getOrDefault(key, ""));
        controls.put(key, field);
        panel.add(field);
    }

    /** 4. Field dạng boolean, người dùng chỉ tích/bỏ tích. */
    private void addBoolean(JPanel panel, String title, String key, Map<String, String> config) {
        panel.add(GiaoDien.label(title, 12, true));
        JCheckBox box = new JCheckBox("Bật", Boolean.parseBoolean(
                config.getOrDefault(key, "false")));
        box.setBackground(Color.WHITE);
        controls.put(key, box);
        panel.add(box);
    }

    /** 5. Tải lại tất cả giá trị từ SQL Server mà không thay component cha. */
    private void taiLai() {
        try {
            Map<String, String> values = db.cauHinh();
            for (Map.Entry<String, JComponent> entry : controls.entrySet()) {
                String current = values.getOrDefault(entry.getKey(), "");
                if (entry.getValue() instanceof JCheckBox) {
                    ((JCheckBox) entry.getValue()).setSelected(Boolean.parseBoolean(current));
                } else if (entry.getValue() instanceof JTextField) {
                    ((JTextField) entry.getValue()).setText(current);
                }
            }
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 6. Đọc control để cập nhật bảng CauHinh. */
    private void luu() {
        Map<String, String> values = new LinkedHashMap<>();
        for (Map.Entry<String, JComponent> entry : controls.entrySet()) {
            JComponent field = entry.getValue();
            String value = field instanceof JCheckBox
                    ? String.valueOf(((JCheckBox) field).isSelected())
                    : ((JTextField) field).getText().trim();
            values.put(entry.getKey(), value);
        }
        if (!EmailHopLe.kiemTra(values.get("EmailQuanTri"))) {
            JOptionPane.showMessageDialog(this, EmailHopLe.huongDan());
            return;
        }
        try {
            db.luuCauHinh(values, actor.id);
            JOptionPane.showMessageDialog(this, "Đã lưu cấu hình vào SQL Server.");
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }
}
