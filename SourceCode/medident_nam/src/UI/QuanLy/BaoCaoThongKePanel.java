
package UI.QuanLy;

import UI.QuanLy.*;
import util.XuatBaoCaoCSV;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BaoCaoThongKePanel extends JPanel {
    private final ChucNangThongKe chucNang = new ChucNangThongKe();
    private final ChonKhoangNgay khoangNgay = new ChonKhoangNgay();
    private final JComboBox<String> loai = new JComboBox<>(
            new String[]{"Doanh thu", "Lượt khám"});

    private final JLabel tongKet = new JLabel(" ");
    private final JLabel thongBao = new JLabel("Chọn khoảng ngày và bấm Áp dụng.");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "STT", "Kỳ báo cáo", "Số giao dịch", "Doanh thu (VNĐ)"
    }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return c == 0 || c == 2 ? Integer.class : Object.class;
        }
    };

    private final JTable bang = new JTable(model);
    private List<Object[]> ketQua = new ArrayList<>();
    private String loaiDaLoc, moTaLoc, nhomDaLoc;
    private boolean dangTai;

    public BaoCaoThongKePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel tren = new JPanel(new BorderLayout(0, 8));
        tren.setOpaque(false);
        tren.add(GiaoDienChung.tieuDe("BÁO CÁO - THỐNG KÊ"), BorderLayout.NORTH);

        JButton apDung = GiaoDienChung.nut("Áp dụng");
        JButton xuat = GiaoDienChung.nut("Xuất CSV");
        loai.setPreferredSize(new Dimension(125, 30));

        tren.add(GiaoDienChung.thanhCongCu(
                new JLabel("Thời gian:"), khoangNgay,
                new JLabel("Loại:"), loai, apDung, xuat), BorderLayout.CENTER);

        add(tren, BorderLayout.NORTH);
        GiaoDienChung.bang(bang);
        add(new JScrollPane(bang), BorderLayout.CENTER);

        JPanel duoi = new JPanel(new GridLayout(2, 1, 0, 3));
        duoi.setOpaque(false);
        duoi.add(tongKet);
        duoi.add(thongBao);
        add(duoi, BorderLayout.SOUTH);

        apDung.addActionListener(e -> taiBaoCao());
        xuat.addActionListener(e -> xuatCSV());

        khoangNgay.setKhiThayDoi(() ->
                thongBao.setText("Đã đổi ngày. Bấm Áp dụng."));
        loai.addActionListener(e ->
                thongBao.setText("Đã đổi loại. Bấm Áp dụng."));

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) taiBaoCao();
        });
    }

    // Tải báo cáo trong SwingWorker để giao diện không bị đơ.
    private void taiBaoCao() {
        if (dangTai) return;

        String l = (String) loai.getSelectedItem();
        LocalDate tu = khoangNgay.getTuNgay();
        LocalDate den = khoangNgay.getDenNgay();

        dangTai = true;
        thongBao.setText("Đang tải báo cáo...");

        new SwingWorker<ChucNangThongKe.KetQua, Void>() {
            @Override
            protected ChucNangThongKe.KetQua doInBackground() throws Exception {
                return chucNang.tai(l, tu, den);
            }

            @Override
            protected void done() {
                try {
                    ChucNangThongKe.KetQua k = get();
                    ketQua = k.dong();
                    loaiDaLoc = l;
                    nhomDaLoc = k.theo();
                    moTaLoc = moTaNgay(tu, den);

                    hienThi(k.doanhThu());
                    thongBao.setText("Nhóm theo " + k.theo() + " | " + moTaLoc);
                } catch (Exception x) {
                    loaiDaLoc = null;
                    thongBao.setText("Lỗi tải báo cáo");
                    GiaoDienChung.baoLoi(BaoCaoThongKePanel.this, x);
                } finally {
                    dangTai = false;
                }
            }
        }.execute();
    }

    private String moTaNgay(LocalDate tu, LocalDate den) {
        return tu == null ? "Tất cả thời gian" : tu + " đến " + den;
    }

    // Hiển thị dữ liệu SQL lên JTable.
    private void hienThi(boolean doanhThu) {
        model.setColumnIdentifiers(doanhThu
                ? new String[]{"STT", "Kỳ báo cáo", "Số giao dịch", "Doanh thu (VNĐ)"}
                : new String[]{"STT", "Kỳ báo cáo", "Lượt khám"});

        model.setRowCount(0);
        int tong = 0, i = 0;
        BigDecimal tien = BigDecimal.ZERO;

        for (Object[] r : ketQua) {
            int so = ((Number) r[1]).intValue();
            tong += so;

            if (doanhThu) {
                BigDecimal t = (BigDecimal) r[2];
                tien = tien.add(t);
                model.addRow(new Object[]{++i, r[0], so, t});
            } else {
                model.addRow(new Object[]{++i, r[0], so});
            }
        }

        tongKet.setText(doanhThu
                ? "Giao dịch: " + tong + " | Tổng doanh thu: " + tien.toPlainString() + " VNĐ"
                : "Tổng lượt khám hoàn thành: " + tong);
    }

    // Xuất đúng dữ liệu đang hiển thị.
    private void xuatCSV() {
        if (dangTai || loaiDaLoc == null || !loaiDaLoc.equals(loai.getSelectedItem())
                || !moTaLoc.equals(moTaNgay(khoangNgay.getTuNgay(), khoangNgay.getDenNgay()))) {
            JOptionPane.showMessageDialog(this, "Bấm Áp dụng và chờ tải xong trước khi xuất.");
            return;
        }

        JFileChooser chon = new JFileChooser();
        chon.setSelectedFile(new File("BaoCaoMediDent.csv"));
        if (chon.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File tep = chon.getSelectedFile();
        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            tep = new File(tep.getPath() + ".csv");
        }

        if (tep.exists() && JOptionPane.showConfirmDialog(this,
                "Ghi đè CSV?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            String[] cot = new String[model.getColumnCount()];
            for (int j = 0; j < cot.length; j++) {
                cot[j] = model.getColumnName(j);
            }

            List<Object[]> dong = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Object[] r = new Object[cot.length];
                for (int j = 0; j < cot.length; j++) {
                    r[j] = model.getValueAt(i, j);
                }
                dong.add(r);
            }

            XuatBaoCaoCSV.xuat(tep.toPath(), loaiDaLoc + " theo " + nhomDaLoc,
                    moTaLoc, cot, dong);

            JOptionPane.showMessageDialog(this, "Đã xuất: " + tep.getAbsolutePath());
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MediDent - Báo cáo thống kê");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new BaoCaoThongKePanel());
            frame.setSize(1150, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
