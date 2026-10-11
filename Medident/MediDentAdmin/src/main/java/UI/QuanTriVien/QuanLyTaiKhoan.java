package UI.QuanTriVien;

import Class.TaiKhoan;
import Class.VaiTro;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Quản lý tài khoản: lọc, thêm, sửa, xóa dữ liệu SQL Server.
 *
 * Cột đầu tiên là STT từ 1 đến hết thay vì hiển thị ID database.
 * Khi thao tác sửa/xóa, chương trình dùng ID nội bộ của TaiKhoan.
 */
public class QuanLyTaiKhoan extends JPanel {

    private final KhoDuLieu db;
    private final TaiKhoan actor;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Tên đăng nhập", "Họ tên", "Email", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField search = GiaoDien.text("");
    private final JComboBox<String> status = new JComboBox<>(new String[]{
        "Tất cả trạng thái", "Hoạt động", "Tạm khóa"
    });
    private final JComboBox<String> role = new JComboBox<>();

    // Dữ liệu thật tương ứng với mỗi hàng MODEL (khác chỉ số hàng đang sắp xếp).
    private List<TaiKhoan> visibleAccounts = new ArrayList<>();
    private List<VaiTro> roles = new ArrayList<>();

    public QuanLyTaiKhoan(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Quản lý tài khoản",
                "Quản lý người dùng, vai trò và trạng thái truy cập hệ thống.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        // 1. Thanh tìm kiếm và lọc co giãn theo chiều rộng cửa sổ.
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        search.setPreferredSize(new Dimension(190, 37));
        search.setMinimumSize(new Dimension(90, 37));
        role.setPreferredSize(new Dimension(148, 37));
        status.setPreferredSize(new Dimension(135, 37));

        JButton find = GiaoDien.nutIcon("Tìm kiếm", false, BieuTuong.Loai.TIM_KIEM);
        JButton add = GiaoDien.nutIcon("Thêm tài khoản", true, BieuTuong.Loai.THEM);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.weightx = 1;
        filters.add(search, constraints);
        constraints.weightx = 0;
        constraints.gridx = 1;
        filters.add(role, constraints);
        constraints.gridx = 2;
        filters.add(status, constraints);
        constraints.gridx = 3;
        filters.add(find, constraints);
        constraints.gridx = 4;
        constraints.insets = new Insets(0, 0, 0, 0);
        filters.add(add, constraints);
        page.add(filters);
        page.add(Box.createVerticalStrut(16));

        // 2. Bảng dữ liệu; STT luôn từ 1 đến số dòng sau khi lọc.
        JPanel card = GiaoDien.card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(700, 430));
        card.add(GiaoDien.wrapTable(table), BorderLayout.CENTER);
        JPanel tools = GiaoDien.hang();
        JButton edit = GiaoDien.nutIcon("Sửa tài khoản", false, BieuTuong.Loai.SUA);
        JButton remove = GiaoDien.nutIcon("Xóa tài khoản", false, BieuTuong.Loai.XOA);
        tools.add(edit);
        tools.add(remove);
        card.add(tools, BorderLayout.SOUTH);
        page.add(card);

        // 3. Chỉ đọc các vai trò từ CSDL, không bắt người dùng tự điền vai trò.
        try {
            napVaiTro();
            taiLai();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
        find.addActionListener(event -> taiLai());
        search.addActionListener(event -> taiLai());
        status.addActionListener(event -> taiLai());
        role.addActionListener(event -> taiLai());
        add.addActionListener(event -> hienForm(null));
        edit.addActionListener(event -> {
            TaiKhoan selected = taiKhoanDangChon();
            if (selected != null) {
                hienForm(selected);
            }
        });
        remove.addActionListener(event -> xoa());
    }

    private void napVaiTro() throws Exception {
        roles = db.vaiTro();
        role.removeAllItems();
        role.addItem("Tất cả vai trò");
        for (VaiTro item : roles) {
            role.addItem(item.ten);
        }
    }

    /** 4. Đọc lại dữ liệu và đánh STT hiển thị từ 1. */
    private void taiLai() {
        try {
            int roleId = role.getSelectedIndex() <= 0
                    ? 0 : roles.get(role.getSelectedIndex() - 1).id;
            String currentStatus = status.getSelectedIndex() == 0
                    ? "" : (String) status.getSelectedItem();
            visibleAccounts = db.taiKhoan(search.getText().trim(), roleId, currentStatus);
            model.setRowCount(0);
            int number = 1;
            for (TaiKhoan account : visibleAccounts) {
                model.addRow(new Object[]{
                    number++, account.tenDangNhap, account.hoTen,
                    account.email, account.tenVaiTro, account.trangThai
                });
            }
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 5. Đổi hàng đang xem về hàng gốc, lấy ID từ đối tượng lưu riêng. */
    private TaiKhoan taiKhoanDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một tài khoản trong bảng.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < visibleAccounts.size()
                ? visibleAccounts.get(modelRow) : null;
    }

    /** 6. Form thêm/sửa: tên đăng nhập tự sinh, vai trò/trạng thái dùng combo. */
    private void hienForm(TaiKhoan account) {
        boolean creating = (account == null);
        JTextField username = GiaoDien.text(creating
                ? "Tự tạo: NV + mã tài khoản" : account.tenDangNhap);
        username.setEditable(false);
        JTextField name = GiaoDien.text(creating ? "" : account.hoTen);
        JTextField email = GiaoDien.text(creating ? "" : account.email);
        JPasswordField password = new JPasswordField();
        JComboBox<VaiTro> roleBox = new JComboBox<>(roles.toArray(new VaiTro[0]));
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});

        if (!creating) {
            for (VaiTro item : roles) {
                if (item.id == account.vaiTroId) {
                    roleBox.setSelectedItem(item);
                }
            }
            statusBox.setSelectedItem(account.trangThai);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.add(GiaoDien.label("Tên đăng nhập", 12, true));
        form.add(username);
        form.add(GiaoDien.label("Họ và tên", 12, true));
        form.add(name);
        form.add(GiaoDien.label("Email", 12, true));
        form.add(email);
        form.add(GiaoDien.label(creating ? "Mật khẩu" : "Mật khẩu mới (trống: giữ nguyên)",
                12, true));
        form.add(password);
        form.add(GiaoDien.label("Vai trò", 12, true));
        form.add(roleBox);
        form.add(GiaoDien.label("Trạng thái", 12, true));
        form.add(statusBox);

        int answer = JOptionPane.showConfirmDialog(this, form,
                creating ? "Thêm tài khoản" : "Sửa tài khoản",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        String passwordValue = new String(password.getPassword());
        if (name.getText().trim().isEmpty()
                || !email.getText().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || (creating && passwordValue.length() < 8)
                || (!passwordValue.isEmpty() && passwordValue.length() < 8)) {
            JOptionPane.showMessageDialog(this,
                    "Kiểm tra tên, email và mật khẩu (tối thiểu 8 ký tự).");
            return;
        }

        try {
            db.luuTaiKhoan(creating ? 0 : account.id, username.getText().trim(),
                    name.getText().trim(), email.getText().trim(),
                    ((VaiTro) roleBox.getSelectedItem()).id,
                    (String) statusBox.getSelectedItem(), passwordValue, actor.id);
            taiLai();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 7. Xóa dùng ID thật, không dùng số thứ tự của bảng. */
    private void xoa() {
        TaiKhoan account = taiKhoanDangChon();
        if (account == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa tài khoản " + account.hoTen + "?\n"
                + "Nếu nhân viên đã phát sinh nghiệp vụ, hệ thống sẽ từ chối xóa.\n"
                + "Nên dùng Tạm khóa để bảo toàn lịch sử.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            db.xoaTK(account.id, actor.id);
            taiLai();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }
}
