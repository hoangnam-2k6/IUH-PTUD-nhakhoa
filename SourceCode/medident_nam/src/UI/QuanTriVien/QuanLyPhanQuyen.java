
package UI.QuanTriVien;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import Entity.VaiTro;
import UI.Chung.GiaoDienHeThong;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QuanLyPhanQuyen extends JPanel {
    private final TaiKhoanDAO db;
    private final TaiKhoan actor;
    private List<VaiTro> danhSach = new ArrayList<>();
    private JScrollPane trangDanhSach;

    private static final String[] TEN_QUYEN = TaiKhoanDAO.tenCacQuyen();

    private final DefaultTableModel model = new DefaultTableModel(taoCot(), 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c >= 2 ? Boolean.class : super.getColumnClass(c);
        }
    };

    private final JTable bang = new JTable(model);

    public QuanLyPhanQuyen(TaiKhoanDAO db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel page = GiaoDienHeThong.trang(
                "Quản lý phân quyền",
                "Quản lý quyền truy cập cho từng vai trò.");

        trangDanhSach = GiaoDienHeThong.cuon(page);
        add(trangDanhSach, BorderLayout.CENTER);

        // Các nút thao tác.
        JPanel hangNut = GiaoDienHeThong.hang();
        JButton sua = GiaoDienHeThong.nut("Chỉnh sửa quyền", false);
        JButton taiLai = GiaoDienHeThong.nut("Tải lại", false);

        hangNut.add(sua);
        hangNut.add(taiLai);
        page.add(hangNut);
        page.add(Box.createVerticalStrut(12));

        // Khung danh sách phân quyền.
        JPanel khung = GiaoDienHeThong.card(new BorderLayout(0, 8));
        khung.setPreferredSize(new Dimension(900, 490));
        khung.add(GiaoDienHeThong.label(
                "Danh sách vai trò và quyền", 14, true), BorderLayout.NORTH);

        khung.add(GiaoDienHeThong.wrapTable(bang), BorderLayout.CENTER);
        page.add(khung);

        // Cột đầu và cột vai trò.
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.getColumnModel().getColumn(0).setPreferredWidth(48);
        bang.getColumnModel().getColumn(1).setPreferredWidth(165);

        for (int i = 2; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(135);
        }

        trangTriCotQuyen();
        taiLai();

        sua.addActionListener(e -> {
            VaiTro vaiTro = vaiTroDangChon();
            if (vaiTro != null) hienForm(vaiTro);
        });

        taiLai.addActionListener(e -> taiLai());
        GiaoDienHeThong.dongBo(this);
    }

    // Tên cột được lấy trực tiếp từ DAO.
    private static String[] taoCot() {
        String[] cot = new String[TEN_QUYEN.length + 2];
        cot[0] = "STT";
        cot[1] = "Vai trò";

        System.arraycopy(TEN_QUYEN, 0, cot, 2, TEN_QUYEN.length);
        return cot;
    }

    // Hiển thị Có hoặc Không thay vì các checkbox trên bảng.
    private void trangTriCotQuyen() {
        for (int i = 2; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i)
                    .setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean selected,
                        boolean focused, int row, int column) {

                    JLabel label = (JLabel) super.getTableCellRendererComponent(
                            table, "", selected, focused, row, column);

                    boolean coQuyen = Boolean.TRUE.equals(value);

                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setIcon(null);
                    label.setText(coQuyen ? "Có" : "Không");
                    label.setForeground(coQuyen
                            ? GiaoDienHeThong.GREEN : GiaoDienHeThong.MUTED);

                    return label;
                }
            });
        }
    }

    // Lấy các vai trò và quyền từ database.
    private void taiLai() {
        try {
            danhSach = db.vaiTro();
            model.setRowCount(0);

            int stt = 1;
            for (VaiTro item : danhSach) {
                Object[] dong = new Object[TEN_QUYEN.length + 2];
                dong[0] = stt++;
                dong[1] = item.ten;

                for (int i = 0; i < TEN_QUYEN.length; i++) {
                    dong[i + 2] = i < item.quyen.length && item.quyen[i];
                }

                model.addRow(dong);
            }
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
        }
    }

    // Lấy vai trò được chọn, kể cả khi bảng được sắp xếp.
    private VaiTro vaiTroDangChon() {
        int dong = bang.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một vai trò.");
            return null;
        }

        int dongModel = bang.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= danhSach.size()) return null;

        return danhSach.get(dongModel);
    }

    // Mở trang chỉnh sửa quyền.
    private void hienForm(VaiTro cu) {
        if ("Quản trị viên".equals(cu.ten)) {
            JOptionPane.showMessageDialog(this,
                    "Không chỉnh sửa vai trò Quản trị viên gốc.");
            return;
        }

        JTextField ten = GiaoDienHeThong.text(cu.ten);
        JTextField moTa = GiaoDienHeThong.text(cu.moTa);

        ten.setEditable(false);
        moTa.setEditable(false);

        JCheckBox[] cacQuyen = new JCheckBox[TEN_QUYEN.length];

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.setOpaque(false);

        form.add(GiaoDienHeThong.label("Tên vai trò", 12, true));
        form.add(ten);
        form.add(GiaoDienHeThong.label("Mô tả", 12, true));
        form.add(moTa);
        form.add(GiaoDienHeThong.label("Quyền truy cập", 12, true));

        for (int i = 0; i < cacQuyen.length; i++) {
            boolean daCo = i < cu.quyen.length && cu.quyen[i];
            JCheckBox check = new JCheckBox(TEN_QUYEN[i], daCo);

            // Giữ nguyên hai quyền không được chỉnh sửa.
            if (i < 2) check.setEnabled(false);

            check.setOpaque(false);
            cacQuyen[i] = check;
            form.add(check);
        }

        JPanel trang = GiaoDienHeThong.trang(
                "Chỉnh sửa phân quyền",
                "Chọn các quyền truy cập dành cho vai trò.");

        JPanel khung = GiaoDienHeThong.card(new BorderLayout());
        khung.add(form, BorderLayout.CENTER);

        trang.add(khung);
        trang.add(Box.createVerticalStrut(12));

        JPanel hangNut = GiaoDienHeThong.hang();
        JButton luu = GiaoDienHeThong.nut("Lưu quyền", false);
        JButton quayLai = GiaoDienHeThong.nut("Quay lại", false);

        hangNut.add(luu);
        hangNut.add(quayLai);
        trang.add(hangNut);

        quayLai.addActionListener(e -> hienDanhSach());

        // Chức năng lưu vẫn sử dụng DAO hiện tại.
        luu.addActionListener(e -> {
            boolean[] quyenMoi = new boolean[cacQuyen.length];

            for (int i = 0; i < cacQuyen.length; i++) {
                quyenMoi[i] = cacQuyen[i].isSelected();
            }

            try {
                db.luuVaiTro(cu.id, ten.getText().trim(),
                        moTa.getText().trim(), quyenMoi, actor.id);

                taiLai();
                hienDanhSach();
                JOptionPane.showMessageDialog(this,
                        "Đã cập nhật phân quyền.");
            } catch (Exception loi) {
                GiaoDienHeThong.loi(this, loi);
            }
        });

        GiaoDienHeThong.dongBo(trang);

        removeAll();
        add(GiaoDienHeThong.cuon(trang), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Trở lại danh sách.
    private void hienDanhSach() {
        removeAll();
        add(trangDanhSach, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
