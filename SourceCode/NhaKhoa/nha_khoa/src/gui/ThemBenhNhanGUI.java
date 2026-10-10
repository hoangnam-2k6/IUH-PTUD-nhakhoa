package gui;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ThemBenhNhanGUI extends BaseQuanLyGUI {
    private final JTextField name = formTextField("Nhập họ và tên bệnh nhân");
    private final JTextField birthDate = formTextField("dd/mm/yyyy");
    private final JTextField phone = formTextField("Nhập số điện thoại");
    private final JTextField email = formTextField("name@email.com");

    public ThemBenhNhanGUI() {
        super("Bệnh nhân", "Thêm bệnh nhân mới");
        content.add(formHeading("Bệnh nhân   ›   Thêm bệnh nhân", "Thêm bệnh nhân mới",
                "Tạo hồ sơ bệnh nhân mới trong hệ thống phòng khám.", this::goBack));

        JPanel personalFields = verticalFields(13,
                formRow(formField("Họ và tên", name, true)),
                formRow(
                        formField("Mã bệnh nhân", readOnlyField("Tự động tạo sau khi lưu"), false),
                        formField("Ngày sinh", birthDate, false)),
                formRow(
                        formField("Giới tính", formCombo("Chọn giới tính", "Nữ", "Nam", "Khác"), false),
                        formField("Số điện thoại", phone, true)),
                formRow(formField("Email", email, false)),
                formRow(formField("Địa chỉ", formTextField("Nhập địa chỉ hiện tại"), false)));
        content.add(formCard("1", "Thông tin cá nhân",
                "Thông tin định danh và liên hệ của bệnh nhân.", personalFields));
        content.add(BoxGap.vertical(14));

        JPanel medicalFields = verticalFields(13,
                formRow(
                        formField("Nhóm máu", formCombo("Chưa xác định", "A", "B", "AB", "O"), false),
                        formField("Nguồn giới thiệu", formCombo("Khách hàng giới thiệu",
                                "Khách hàng giới thiệu", "Tìm kiếm trực tuyến", "Khác"), false)),
                formRow(formField("Tên sử dụng", formTextField("Thuốc, thực phẩm hoặc vật liệu nha khoa..."), false)),
                formRow(formField("Ghi chú y tế", scrollableArea(
                        formTextArea("Nhập bệnh nền hoặc thông tin cần lưu ý...", 3)), false)));
        content.add(formCard("2", "Thông tin y tế ban đầu",
                "Ghi nhận thông tin y tế quan trọng trước lần khám đầu tiên.", medicalFields));
        content.add(BoxGap.vertical(14));

        JButton cancel = flatButton("Hủy bỏ", false);
        cancel.addActionListener(event -> goBack());
        JButton save = primaryButton("+   Tạo hồ sơ");
        save.addActionListener(event -> savePatient());
        content.add(formFooter(cancel, save));
        showWindow();
    }

    private JTextField readOnlyField(String value) {
        JTextField field = formTextField("");
        field.setText(value);
        field.setEditable(false);
        field.setBackground(new Color(248, 250, 252));
        field.setForeground(MUTED);
        return field;
    }

    private void savePatient() {
        if (name.getText().isBlank() || phone.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên và số điện thoại.",
                    "Thiếu thông tin bắt buộc", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Thông tin hồ sơ đã được kiểm tra.",
                "Tạo hồ sơ", JOptionPane.INFORMATION_MESSAGE);
        goBack();
    }

    private void goBack() {
        openPage("Bệnh nhân");
    }
}
