
package UI.QuanLy;

import UI.QuanLy.*;
import DAO.LichHenDAO;
import Entity.LichHen;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyLichHenPanel extends JPanel {
    private final ChucNangLichHen chucNang = new ChucNangLichHen();
    private List<LichHen> ds = new ArrayList<>();

    private final CardLayout card = new CardLayout();
    private final JPanel vung = new JPanel(card);
    private final JTextField tim = new JTextField(8);
    private final JLabel thongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "STT", "Mã lịch", "Mã bệnh nhân", "Mã bác sĩ",
        "Ngày hẹn", "Giờ hẹn", "Phút", "Phòng", "Trạng thái"
    }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return c <= 1 ? Integer.class : Object.class;
        }
    };

    private final JTable bang = new JTable(model);
    private final JComboBox<GiaoDienChung.MucChon> benhNhan = new JComboBox<>();
    private final JComboBox<GiaoDienChung.MucChon> bacSi = new JComboBox<>();
    private final JSpinner ngay = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> gio = new JComboBox<>();
    private final JSpinner phut = new JSpinner(new SpinnerNumberModel(60, 15, 240, 15));
    private final JComboBox<String> phong = new JComboBox<>(
            new String[]{"Phòng 01", "Phòng 02", "Phòng 03", "Phòng 04"});
    private final JLabel tenForm = new JLabel("THÊM LỊCH HẸN");

    private LichHen dangSua;

    public QuanLyLichHenPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel danhSach = new JPanel(new BorderLayout(8, 8));
        danhSach.setBackground(Color.WHITE);

        JPanel tren = new JPanel(new BorderLayout(0, 9));
        tren.setOpaque(false);
        tren.add(GiaoDienChung.tieuDe("QUẢN LÝ LỊCH HẸN"), BorderLayout.NORTH);

        JButton timNut = GiaoDienChung.nut("Tìm");
        JButton tai = GiaoDienChung.nut("Tải lại");
        JButton them = GiaoDienChung.nut("Thêm lịch");
        JButton sua = GiaoDienChung.nut("Sửa lịch");
        JButton huy = GiaoDienChung.nut("Hủy lịch");

        JComboBox<String> trangThai = new JComboBox<>(new String[]{
            "Chuyển trạng thái...", LichHenDAO.DA_XAC_NHAN, LichHenDAO.DA_KHAM
        });
        trangThai.setPreferredSize(new Dimension(145, 30));

        tren.add(GiaoDienChung.thanhCongCu(new JLabel("Tìm:"), tim,
                timNut, them, sua, huy, trangThai, tai), BorderLayout.CENTER);

        danhSach.add(tren, BorderLayout.NORTH);
        GiaoDienChung.bang(bang);
        danhSach.add(new JScrollPane(bang), BorderLayout.CENTER);
        danhSach.add(thongBao, BorderLayout.SOUTH);

        // Hai trang cùng nằm trong một CardLayout.
        vung.add(danhSach, "danhsach");
        vung.add(taoForm(), "bieumau");
        add(vung, BorderLayout.CENTER);

        timNut.addActionListener(e -> taiDuLieu());
        tim.addActionListener(e -> taiDuLieu());
        tai.addActionListener(e -> {
            tim.setText("");
            taiDuLieu();
        });

        them.addActionListener(e -> moForm(null));
        sua.addActionListener(e -> {
            try {
                moForm(layChon());
            } catch (Exception x) {
                GiaoDienChung.baoLoi(this, x);
            }
        });

        huy.addActionListener(e -> huyLich());

        trangThai.addActionListener(e -> {
            int i = trangThai.getSelectedIndex();
            if (i <= 0) return;

            String tt = (String) trangThai.getSelectedItem();
            trangThai.setSelectedIndex(0);

            try {
                LichHen l = layChon();
                int chon = JOptionPane.showConfirmDialog(this,
                        "Đổi trạng thái lịch " + l.maLichHen() + "?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);

                if (chon == JOptionPane.YES_OPTION) {
                    chucNang.chuyenTrangThai(l.maLichHen(), tt);
                    taiDuLieu();
                }
            } catch (Exception x) {
                GiaoDienChung.baoLoi(this, x);
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) taiDuLieu();
        });
    }

    // Tạo trang thêm/sửa lịch hẹn lớn.
    private JPanel taoForm() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(Color.WHITE);

        tenForm.setFont(new Font("Arial", Font.BOLD, 20));
        tenForm.setForeground(GiaoDienChung.XANH);
        p.add(tenForm, BorderLayout.NORTH);

        JPanel o = new JPanel(new GridBagLayout());
        o.setOpaque(false);
        ngay.setEditor(new JSpinner.DateEditor(ngay, "dd/MM/yyyy"));

        for (int i = 6 * 60; i <= 20 * 60; i += 15) {
            gio.addItem(String.format("%02d:%02d", i / 60, i % 60));
        }

        phong.setEditable(true);

        Component[] cacO = {benhNhan, bacSi, ngay, gio, phut, phong};
        String[] nhan = {
            "Bệnh nhân:", "Bác sĩ:", "Ngày hẹn:",
            "Giờ hẹn:", "Thời lượng (phút):", "Phòng khám:"
        };

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 9, 9, 9);
        c.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < nhan.length; i++) {
            c.gridy = i;
            c.gridx = 0;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            o.add(new JLabel(nhan[i]), c);

            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            cacO[i].setPreferredSize(new Dimension(350, 32));
            o.add(cacO[i], c);
        }

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(o, BorderLayout.NORTH);
        p.add(giua, BorderLayout.CENTER);

        JButton luu = GiaoDienChung.nut("Lưu lịch hẹn");
        JButton quay = GiaoDienChung.nut("Quay lại");

        JPanel nut = GiaoDienChung.hang();
        nut.add(luu);
        nut.add(quay);
        p.add(nut, BorderLayout.SOUTH);

        luu.addActionListener(e -> luuLich());
        quay.addActionListener(e -> veDanhSach());
        return p;
    }

    // Hiển thị trang thêm hoặc sửa.
    private void moForm(LichHen l) {
        try {
            dangSua = l;
            tenForm.setText(l == null ? "THÊM LỊCH HẸN"
                    : "SỬA LỊCH HẸN #" + l.maLichHen());

            napMuc(benhNhan, chucNang.benhNhan(), l == null ? null : l.maBN());
            napMuc(bacSi, chucNang.bacSi(), l == null ? null : l.maNV());

            if (benhNhan.getItemCount() == 0 || bacSi.getItemCount() == 0)
                throw new IllegalStateException("Phải có bệnh nhân và bác sĩ đang làm việc.");

            ngay.setValue(java.sql.Date.valueOf(
                    l == null ? LocalDate.now().plusDays(1) : l.ngayHen()));

            gio.setSelectedItem((l == null ? LocalTime.of(9, 0) : l.gioHen())
                    .toString().substring(0, 5));

            phut.setValue(l == null ? 60 : l.thoiLuongPhut());
            phong.setSelectedItem(l == null ? "Phòng 01" : l.phongKham());

            card.show(vung, "bieumau");
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    // Đổ dữ liệu bệnh nhân và bác sĩ vào ComboBox.
    private void napMuc(JComboBox<GiaoDienChung.MucChon> box,
                        List<String[]> ds, String ma) {
        box.removeAllItems();

        for (String[] row : ds) {
            GiaoDienChung.MucChon muc = new GiaoDienChung.MucChon(row[0], row[1]);
            box.addItem(muc);
            if (row[0].equals(ma)) box.setSelectedItem(muc);
        }
    }

    // Lưu lịch qua class chức năng.
    private void luuLich() {
        try {
            GiaoDienChung.MucChon bn = (GiaoDienChung.MucChon) benhNhan.getSelectedItem();
            GiaoDienChung.MucChon bs = (GiaoDienChung.MucChon) bacSi.getSelectedItem();

            LocalDate ngayHen = new java.sql.Date(
                    ((java.util.Date) ngay.getValue()).getTime()).toLocalDate();

            LichHen moi = new LichHen(
                    dangSua == null ? 0 : dangSua.maLichHen(),
                    bn.ma, bs.ma, ngayHen,
                    LocalTime.parse((String) gio.getSelectedItem()),
                    (Integer) phut.getValue(),
                    String.valueOf(phong.getSelectedItem()),
                    dangSua == null ? LichHenDAO.CHO_XAC_NHAN : dangSua.trangThai()
            );

            chucNang.luu(moi, dangSua != null);
            veDanhSach();
            taiDuLieu();
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    private void veDanhSach() {
        card.show(vung, "danhsach");
    }

    // Hiển thị dữ liệu lên JTable.
    public void taiDuLieu() {
        try {
            ds = chucNang.tim(tim.getText().trim());
            model.setRowCount(0);
            int i = 0;

            for (LichHen l : ds) {
                model.addRow(new Object[]{
                    ++i, l.maLichHen(), l.maBN(), l.maNV(),
                    l.ngayHen(), l.gioHen(), l.thoiLuongPhut(),
                    l.phongKham(), l.trangThai()
                });
            }

            thongBao.setText("Tổng lịch hẹn: " + ds.size());
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    private LichHen layChon() {
        int i = bang.getSelectedRow();
        if (i < 0) throw new IllegalArgumentException("Hãy chọn lịch hẹn.");
        return ds.get(bang.convertRowIndexToModel(i));
    }

    private void huyLich() {
        try {
            LichHen l = layChon();

            if (JOptionPane.showConfirmDialog(this, "Hủy lịch #" + l.maLichHen() + "?","Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

            chucNang.huy(l.maLichHen());
            taiDuLieu();
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MediDent - Quản lý lịch hẹn");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new QuanLyLichHenPanel());
            frame.setSize(1150, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
