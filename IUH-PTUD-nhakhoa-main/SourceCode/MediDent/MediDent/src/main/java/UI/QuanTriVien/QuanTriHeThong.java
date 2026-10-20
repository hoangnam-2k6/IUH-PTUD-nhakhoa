/*
 * QuanTriHeThong: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
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

public class QuanTriHeThong extends JPanel {

    private final KhoDuLieu db;
    private final TaiKhoan actor;
    private final Map<String, JComponent> controls = new LinkedHashMap<>();

    /** Dựng form cấu hình hệ thống và gắn thao tác đọc/lưu từ SQL. */
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

        JPanel actions = GiaoDien.hang();
        JButton reload = GiaoDien.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        JButton save = GiaoDien.nutIcon("Lưu cấu hình", true, BieuTuong.Loai.LUU);
        actions.add(reload);
        actions.add(save);
        page.add(actions);
        reload.addActionListener(event -> taiLai());
        save.addActionListener(event -> luu());
    }

    /** Đặt trường cấu hình văn bản với nhãn vào form hiện có. */
    private void addField(JPanel panel, String title, String key, Map<String, String> config) {
        panel.add(GiaoDien.label(title, 12, true));
        JTextField field = GiaoDien.text(config.getOrDefault(key, ""));
        controls.put(key, field);
        panel.add(field);
    }

    /** Đặt tùy chọn cấu hình bật/tắt cùng giá trị đọc từ SQL. */
    private void addBoolean(JPanel panel, String title, String key, Map<String, String> config) {
        panel.add(GiaoDien.label(title, 12, true));
        JCheckBox box = new JCheckBox("Bật", Boolean.parseBoolean(
                config.getOrDefault(key, "false")));
        box.setBackground(Color.WHITE);
        controls.put(key, box);
        panel.add(box);
    }

    /** Nạp lại dữ liệu mới nhất và làm mới bảng đang hiển thị. */
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

    /** Kiểm tra và lưu thay đổi dữ liệu bằng thao tác hiện có. */
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
