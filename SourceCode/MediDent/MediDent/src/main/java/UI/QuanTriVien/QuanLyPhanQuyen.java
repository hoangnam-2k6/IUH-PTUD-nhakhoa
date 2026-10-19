/*
 * QuanLyPhanQuyen: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
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

public class QuanLyPhanQuyen extends JPanel {

    private final KhoDuLieu db;
    private final TaiKhoan actor;
    private List<VaiTro> roles = new ArrayList<>();

    private static final String[] TEN_QUYEN = {
        "Tài khoản", "Bệnh nhân", "Lịch hẹn", "Thanh toán", "Báo cáo",
        "Hệ thống", "Nhân viên", "Dịch vụ", "Hỗ trợ"
    };
    private final DefaultTableModel model = new DefaultTableModel(taoTieuDe(), 0) {
        @Override
        /** Quyết định có cho nhập trực tiếp trên ô bảng hay không. */
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        /** Lấy giá trị thuộc tính ColumnClass từ đối tượng hiện tại. */
        public Class<?> getColumnClass(int columnIndex) {

            return columnIndex >= 2 ? Boolean.class : super.getColumnClass(columnIndex);
        }
    };
    private final JTable table = new JTable(model) {
        @Override
        /** Thiết lập cách hiển thị từng dòng của bảng. */
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                         int row, int column) {
            Component result = super.prepareRenderer(renderer, row, column);
            if (column == 0 && result instanceof JLabel) {
                ((JLabel) result).setText(String.valueOf(row + 1));
                ((JLabel) result).setHorizontalAlignment(SwingConstants.CENTER);
            }
            return result;
        }
    };

    /** Tạo mảng tiêu đề gồm STT, vai trò và các mã quyền hiển thị. */
    private static String[] taoTieuDe() {
        String[] tieuDe = new String[TEN_QUYEN.length + 2];
        tieuDe[0] = "STT";
        tieuDe[1] = "Vai trò";
        System.arraycopy(TEN_QUYEN, 0, tieuDe, 2, TEN_QUYEN.length);
        return tieuDe;
    }

    /** Khởi tạo bảng ma trận phân quyền và nút chỉnh sửa vai trò. */
    public QuanLyPhanQuyen(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Quản lý phân quyền",
                "Thiết lập vai trò và quyền truy cập cho từng nhóm người dùng.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        JPanel actions = GiaoDien.hang();
        JButton edit = GiaoDien.nutIcon("Chỉnh sửa quyền", true, BieuTuong.Loai.SUA);
        actions.add(edit);
        page.add(actions);
        page.add(Box.createVerticalStrut(17));

        JPanel card = GiaoDien.card(new BorderLayout());
        card.setPreferredSize(new java.awt.Dimension(740, 440));
        card.add(GiaoDien.wrapTable(table), BorderLayout.CENTER);
        trangTriCotQuyen();
        page.add(card);

        taiLai();
        edit.addActionListener(event -> {
            VaiTro selected = vaiTroDangChon();
            if (selected != null) {
                hienForm(selected);
            }
        });
    }

    /** Dùng biểu tượng đúng/sai để hiển thị các quyền trong JTable. */
    private void trangTriCotQuyen() {
        for (int column = 2; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(
                    new DefaultTableCellRenderer() {
                @Override
                /** Tạo ô hiển thị bảng theo giá trị và trạng thái chọn. */
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

    /** Nạp lại dữ liệu mới nhất và làm mới bảng đang hiển thị. */
    private void taiLai() {
        try {
            roles = db.vaiTro();
            model.setRowCount(0);
            int number = 1;
            for (VaiTro item : roles) {
                Object[] row = new Object[TEN_QUYEN.length + 2];
                row[0] = number++;
                row[1] = item.ten;
                for (int index = 0; index < TEN_QUYEN.length; index++) {
                    row[index + 2] = item.quyen[index];
                }
                model.addRow(row);
            }
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** Xác định mã vai trò thực của dòng đang chọn trên bảng. */
    private VaiTro vaiTroDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một vai trò.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < roles.size() ? roles.get(modelRow) : null;
    }

    /** Mở hộp thoại chỉnh sửa danh sách quyền và lưu theo vai trò. */
    private void hienForm(VaiTro old) {
        if (old != null && "Quản trị viên".equals(old.ten)) {
            JOptionPane.showMessageDialog(this,
                    "Không chỉnh sửa vai trò Quản trị viên gốc.");
            return;
        }
        JTextField name = GiaoDien.text(old == null ? "" : old.ten);
        name.setEditable(false);
        JTextField description = GiaoDien.text(old == null ? "" : old.moTa);
        description.setEditable(false);

        String[] permissionNames = TEN_QUYEN;
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

    /** Lấy mã bản ghi đang chọn và gọi DAO để xử lý nghiệp vụ tương ứng. */
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
