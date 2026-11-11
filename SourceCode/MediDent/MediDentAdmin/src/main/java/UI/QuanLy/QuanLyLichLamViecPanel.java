
package UI.QuanLy;

import UI.QuanLy.*;
import Entity.LichLamViec;
import enums.TrangThaiLichLamViec;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyLichLamViecPanel extends JPanel {
    private final ChucNangLichLamViec chucNang = new ChucNangLichLamViec();
    private List<LichLamViec> ds = new ArrayList<>();
    private final JTextField tim = new JTextField(10);
    private final JLabel thongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "STT", "Mã ca", "Mã nhân viên", "Ngày làm việc",
        "Bắt đầu", "Kết thúc", "Phòng khám", "Trạng thái"
    }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return c <= 1 ? Integer.class : Object.class;
        }
    };

    private final JTable bang = new JTable(model);

    public QuanLyLichLamViecPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel tren = new JPanel(new BorderLayout(0, 8));
        tren.setOpaque(false);
        tren.add(GiaoDienChung.tieuDe("QUẢN LÝ LỊCH LÀM VIỆC"), BorderLayout.NORTH);

        JButton timNut = GiaoDienChung.nut("Tìm");
        JButton them = GiaoDienChung.nut("Thêm ca");
        JButton sua = GiaoDienChung.nut("Sửa ca");
        JButton huy = GiaoDienChung.nut("Hủy ca");
        JButton tai = GiaoDienChung.nut("Tải lại");

        tren.add(GiaoDienChung.thanhCongCu(new JLabel("Tìm:"), tim,
                timNut, them, sua, huy, tai), BorderLayout.CENTER);

        add(tren, BorderLayout.NORTH);
        GiaoDienChung.bang(bang);
        add(new JScrollPane(bang), BorderLayout.CENTER);
        add(thongBao, BorderLayout.SOUTH);

        timNut.addActionListener(e -> taiDuLieu());
        tim.addActionListener(e -> taiDuLieu());

        tai.addActionListener(e -> {
            tim.setText("");
            taiDuLieu();
        });

        them.addActionListener(e -> moBieuMau(null));
        sua.addActionListener(e -> {
            try {
                moBieuMau(layChon());
            } catch (Exception x) {
                GiaoDienChung.baoLoi(this, x);
            }
        });

        huy.addActionListener(e -> huyCa());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) taiDuLieu();
        });
    }

    public void taiDuLieu() {
        try {
            ds = chucNang.tim(tim.getText().trim());
            model.setRowCount(0);
            int i = 0;

            for (LichLamViec l : ds) {
                model.addRow(new Object[]{
                    ++i, l.maLLV(), l.maNV(), l.ngayLamViec(),
                    l.gioBatDau(), l.gioKetThuc(), l.phongKham(), l.trangThai()
                });
            }

            thongBao.setText("Tổng số ca: " + ds.size());
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    private LichLamViec layChon() {
        int i = bang.getSelectedRow();
        if (i < 0) throw new IllegalArgumentException("Hãy chọn một ca làm việc.");
        return ds.get(bang.convertRowIndexToModel(i));
    }

    private JComboBox<String> gioChon(LocalTime macDinh) {
        JComboBox<String> b = new JComboBox<>();

        for (int i = 6 * 60; i <= 21 * 60; i += 15) {
            b.addItem(String.format("%02d:%02d", i / 60, i % 60));
        }

        b.setSelectedItem(macDinh.toString().substring(0, 5));
        return b;
    }

    private void moBieuMau(LichLamViec cu) {
        try {
            JComboBox<GiaoDienChung.MucChon> nv = GiaoDienChung.danhSach(
                    chucNang.nhanVien(), cu == null ? null : cu.maNV());

            if (nv.getItemCount() == 0)
                throw new IllegalStateException("Không có nhân viên đang làm việc.");

            JSpinner ngay = new JSpinner(new SpinnerDateModel(
                    java.sql.Date.valueOf(cu == null
                            ? LocalDate.now().plusDays(1) : cu.ngayLamViec()),
                    null, null, Calendar.DAY_OF_MONTH));

            ngay.setEditor(new JSpinner.DateEditor(ngay, "dd/MM/yyyy"));

            JComboBox<String> batDau = gioChon(
                    cu == null ? LocalTime.of(8, 0) : cu.gioBatDau());
            JComboBox<String> ketThuc = gioChon(
                    cu == null ? LocalTime.of(12, 0) : cu.gioKetThuc());

            JTextField phong = new JTextField(cu == null ? "Phòng 01"
                    : cu.phongKham() == null ? "" : cu.phongKham());

            JComboBox<TrangThaiLichLamViec> trangThai = new JComboBox<>(
                    TrangThaiLichLamViec.values());

            if (cu != null) {
                for (TrangThaiLichLamViec t : TrangThaiLichLamViec.values()) {
                    if (t.toString().equals(cu.trangThai())) trangThai.setSelectedItem(t);
                }
            }

            JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
            form.add(new JLabel("Nhân viên:")); form.add(nv);
            form.add(new JLabel("Ngày:")); form.add(ngay);
            form.add(new JLabel("Giờ bắt đầu:")); form.add(batDau);
            form.add(new JLabel("Giờ kết thúc:")); form.add(ketThuc);
            form.add(new JLabel("Phòng:")); form.add(phong);
            form.add(new JLabel("Trạng thái:")); form.add(trangThai);

            if (JOptionPane.showConfirmDialog(this, form,
                    cu == null ? "Thêm ca" : "Sửa ca",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

            GiaoDienChung.MucChon muc = (GiaoDienChung.MucChon) nv.getSelectedItem();
            LocalDate ngayLam = new java.sql.Date(
                    ((java.util.Date) ngay.getValue()).getTime()).toLocalDate();

            LichLamViec moi = new LichLamViec(
                    cu == null ? 0 : cu.maLLV(),
                    muc.ma, ngayLam,
                    LocalTime.parse((String) batDau.getSelectedItem()),
                    LocalTime.parse((String) ketThuc.getSelectedItem()),
                    phong.getText().trim(),
                    trangThai.getSelectedItem().toString());

            chucNang.luu(moi, cu != null);
            taiDuLieu();
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    private void huyCa() {
        try {
            LichLamViec l = layChon();

            if (JOptionPane.showConfirmDialog(this,
                    "Hủy ca #" + l.maLLV() + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            chucNang.huy(l.maLLV());
            taiDuLieu();
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MediDent - Quản lý lịch làm việc");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new QuanLyLichLamViecPanel());
            frame.setSize(1150, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
