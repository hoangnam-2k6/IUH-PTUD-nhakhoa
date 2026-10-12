package UI.QuanTriVien;

import Class.TaiKhoan;
import Class.VaiTro;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Quản lý phân quyền: bảng ma trận quyền theo các VaiTro / VaiTroQuyen trong SQL.
 *
 * STT tăng từ 1 đến N; quyền có/không được vẽ bằng icon Java2D xanh/xám
 * (không dùng dấu ✓ hoặc × Unicode nên không còn ô vuông).
 */
public class QuanLyPhanQuyen extends JPanel {

    private final KhoDuLieu db;
    private final TaiKhoan actor;
    private List<VaiTro> roles = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Vai trò", "Tài khoản", "Bệnh nhân", "Lịch hẹn",
                "Thanh toán", "Báo cáo", "Hệ thống"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // Cột quyền lưu Boolean để phân biệt có quyền / không có quyền.
            return columnIndex >= 2 ? Boolean.class : super.getColumnClass(columnIndex);
        }
    };
    private final JTable table = new JTable(model);

    public QuanLyPhanQuyen(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Quản lý phân quyền",
                "Thiết lập vai trò và quyền truy cập cho từng nhóm người dùng.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        // 1. Các nút thao tác với VaiTro.
        JPanel actions = GiaoDien.hang();
        JButton add = GiaoDien.nutIcon("Thêm vai trò", true, BieuTuong.Loai.THEM);
        JButton edit = GiaoDien.nutIcon("Sửa vai trò", false, BieuTuong.Loai.SUA);
        JButton remove = GiaoDien.nutIcon("Xóa vai trò", false, BieuTuong.Loai.XOA);
        actions.add(add);
        actions.add(edit);
        actions.add(remove);
        page.add(actions);
        page.add(Box.createVerticalStrut(17));

        // 2. Tạo bảng có STT, vẽ icon cho từng ô quyền.
        JPanel card = GiaoDien.card(new BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(740, 440));
        card.add(GiaoDien.wrapTable(table), BorderLayout.CENTER);
        trangTriCotQuyen();
        page.add(card);

        taiLai();
        add.addActionListener(event -> hienForm(null));
        edit.addActionListener(event -> {
            VaiTro selected = vaiTroDangChon();
            if (selected != null) {
                hienForm(selected);
            }
        });
        remove.addActionListener(event -> xoa());
    }

    /** 3. Renderer chỉ hiển thị icon, không cho JTable dùng checkbox mặc định. */
    private void trangTriCotQuyen() {
        for (int column = 2; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(
                    new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable source, Object value,
                        boolean selected, boolean focused, int row, int column) {
                    JLabel result = (JLabel) super.getTableCellRendererComponent(
                            source, "", selected, focused, row, column);
                    boolean allowed = Boolean.TRUE.equals(value);
                    result.setHorizontalAlignment(SwingConstants.CENTER);
                    result.setIcon(new BieuTuong(
                            allowed ? BieuTuong.Loai.KIEM_TRA : BieuTuong.Loai.HUY,
                            allowed ? GiaoDien.GREEN : GiaoDien.MUTED, 17));
                    return result;
                }
            });
        }
    }

    /** 4. Nạp vai trò từ CSDL; không ghi ID thật vào cột STT. */
    private void taiLai() {
        try {
            roles = db.vaiTro();
            model.setRowCount(0);
            int number = 1;
            for (VaiTro item : roles) {
                Object[] row = new Object[8];
                row[0] = number++;
                row[1] = item.ten;
                for (int index = 0; index < 6; index++) {
                    row[index + 2] = item.quyen[index];
                }
                model.addRow(row);
            }
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 5. Lấy vai trò đang chọn qua model index, an toàn khi sắp xếp bảng. */
    private VaiTro vaiTroDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một vai trò.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < roles.size() ? roles.get(modelRow) : null;
    }

    /** 6. Form checkbox giúp tránh gõ sai tên quyền. */
    private void hienForm(VaiTro old) {
        if (old != null && "Quản trị viên".equals(old.ten)) {
            JOptionPane.showMessageDialog(this,
                    "Không chỉnh sửa vai trò Quản trị viên gốc.");
            return;
        }
        JTextField name = GiaoDien.text(old == null ? "" : old.ten);
        JTextField description = GiaoDien.text(old == null ? "" : old.moTa);
        String[] permissionNames = {
            "Quản lý tài khoản / phân quyền", "Quản lý bệnh nhân",
            "Quản lý lịch hẹn", "Quản lý thanh toán", "Xem báo cáo",
            "Cấu hình hệ thống"
        };
        JCheckBox[] permissions = new JCheckBox[permissionNames.length];
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(GiaoDien.label("Tên vai trò *", 12, true));
        form.add(name);
        form.add(GiaoDien.label("Mô tả", 12, true));
        form.add(description);
        form.add(GiaoDien.label("Quyền truy cập", 12, true));
        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = new JCheckBox(permissionNames[i],
                    old != null && old.quyen[i]);
            form.add(permissions[i]);
        }

        int answer = JOptionPane.showConfirmDialog(this, form,
                old == null ? "Thêm vai trò" : "Sửa vai trò",
                JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }
        if (name.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên vai trò không được trống.");
            return;
        }

        boolean[] values = new boolean[permissions.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = permissions[i].isSelected();
        }
        try {
            db.luuVaiTro(old == null ? 0 : old.id, name.getText().trim(),
                    description.getText().trim(), values, actor.id);
            taiLai();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 7. Xóa theo ID VaiTro, không theo STT hiển thị. */
    private void xoa() {
        VaiTro selected = vaiTroDangChon();
        if (selected == null) {
            return;
        }
        if ("Quản trị viên".equals(selected.ten)) {
            JOptionPane.showMessageDialog(this, "Không xóa Quản trị viên gốc.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa vai trò " + selected.ten + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            db.xoaVaiTro(selected.id, actor.id);
            taiLai();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }
}
