
package UI.QuanLy;

import DAO.ThongKeMoRongDAO;
import UI.QuanLy.*;
import util.XuatBaoCaoCSV;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BaoCaoThongKePanel extends JPanel {
    private final ChucNangThongKe chucNang = new ChucNangThongKe();
    private final ChonKhoangNgay khoangNgay = new ChonKhoangNgay();
    private final JComboBox<String> loaiBaoCao = new JComboBox<>(new String[]{"Doanh thu", "Lượt khám"});
    private final JLabel tongKet = new JLabel("Chưa có dữ liệu thống kê.");
    private final JLabel thongBao = new JLabel("Chọn thời gian và loại báo cáo.");

    private final JButton apDung = GiaoDienChung.nut("Áp dụng");
    private final JButton xuat = GiaoDienChung.nut("Xuất CSV");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Kỳ báo cáo", "Hóa đơn đã thu", "Hóa đơn chờ", "Doanh thu (VNĐ)"}, 0) {

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            for (int i = 0; i < getRowCount(); i++) {
                Object value = getValueAt(i, c);
                if (value != null) return value.getClass();
            }
            return c == 0 ? Integer.class : Object.class;
        }
    };
   

    private final JTable bang = new JTable(model);
    private final NumberFormat dinhDang = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private String loaiDaLoc;
    private LocalDate tuDaLoc, denDaLoc;
    private boolean dangTai = false;

    public BaoCaoThongKePanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(14, 15, 14, 15));

        // Tiêu đề và thanh công cụ.
        JPanel tren = new JPanel(new BorderLayout(0, 10));
        tren.setOpaque(false);
        tren.add(GiaoDienChung.tieuDe("BÁO CÁO - THỐNG KÊ"), BorderLayout.NORTH);

        khoangNgay.setPreferredSize(new Dimension(245, 30));
        loaiBaoCao.setPreferredSize(new Dimension(135, 30));

        JPanel congCu = GiaoDienChung.hang();
        congCu.add(new JLabel("Thời gian:"));
        congCu.add(khoangNgay);
        congCu.add(new JLabel("Loại báo cáo:"));
        congCu.add(loaiBaoCao);
        congCu.add(apDung);
        congCu.add(xuat);

        tren.add(congCu, BorderLayout.CENTER);
        add(tren, BorderLayout.NORTH);

        // Bảng thống kê cơ bản.
        GiaoDienChung.bang(bang);
        bang.setRowHeight(30);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(new JScrollPane(bang), BorderLayout.CENTER);

        // Tổng kết và thông báo.
        JPanel duoi = new JPanel(new GridLayout(2, 1, 0, 5));
        duoi.setOpaque(false);

        tongKet.setFont(new Font("Arial", Font.BOLD, 13));
        thongBao.setFont(new Font("Arial", Font.PLAIN, 12));

        duoi.add(tongKet);
        duoi.add(thongBao);
        add(duoi, BorderLayout.SOUTH);

        dinhDang.setMaximumFractionDigits(0);
        dinhDang.setMinimumFractionDigits(0);
        xuat.setEnabled(false);

        // Các sự kiện chức năng.
        apDung.addActionListener(e -> taiBaoCao());
        xuat.addActionListener(e -> xuatCSV());

        khoangNgay.setKhiThayDoi(() -> {
            xuat.setEnabled(false);
            thongBao.setText("Thời gian đã thay đổi. Nhấn Áp dụng.");
        });

        loaiBaoCao.addActionListener(e -> {
            xuat.setEnabled(false);
            thongBao.setText("Loại báo cáo đã thay đổi. Nhấn Áp dụng.");
        });

        // Tự tải khi mở Panel.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) taiBaoCao();
        });
    }

    // Chức năng 1: Tải dữ liệu thống kê từ SQL Server.
    public void taiBaoCao() {
        if (dangTai) return;

        String loai = (String) loaiBaoCao.getSelectedItem();
        LocalDate tu = khoangNgay.getTuNgay();
        LocalDate den = khoangNgay.getDenNgay();

        dangTai = true;
        apDung.setEnabled(false);
        xuat.setEnabled(false);
        khoangNgay.setEnabled(false);
        loaiBaoCao.setEnabled(false);
        thongBao.setText("Đang tải dữ liệu...");

        new SwingWorker<ChucNangThongKe.BaoCao, Void>() {
            @Override
            protected ChucNangThongKe.BaoCao doInBackground() throws Exception {
                return chucNang.tai(loai, tu, den);
            }

            @Override
            protected void done() {
                try {
                    ChucNangThongKe.BaoCao ketQua = get();

                    loaiDaLoc = loai;
                    tuDaLoc = tu;
                    denDaLoc = den;

                    hienThiBaoCao(loai, ketQua.bang());

                    int tongThu = 0;
                    int tongCho = 0;
                    int tongKham = 0;
                    java.math.BigDecimal tongTien = java.math.BigDecimal.ZERO;

                    for (Object[] dong : ketQua.bang().dong()) {
                        if (loai.equals("Doanh thu")) {
                            tongThu += ((Number) dong[2]).intValue();
                            tongCho += ((Number) dong[3]).intValue();
                            tongTien = tongTien.add((java.math.BigDecimal) dong[4]);
                        } else {
                            tongKham += ((Number) dong[4]).intValue();
                        }
                    }

                    if (loai.equals("Doanh thu")) {
                        tongKet.setText("Hóa đơn đã thu: " + tongThu
                                + " | Hóa đơn chờ: " + tongCho
                                + " | Tổng doanh thu: " + dinhDang.format(tongTien) + " VNĐ");
                    } else {
                        tongKet.setText("Tổng lượt khám hoàn thành: " + tongKham);
                    }

                    String thoiGian = tu == null ? "Tất cả thời gian" : tu + " đến " + den;
                    thongBao.setText("Đã tải " + model.getRowCount() + " kỳ báo cáo"+ " | Nhóm theo " + ketQua.nhom() + " | " + thoiGian);
                    
                    if (model.getRowCount() > 0) {
                        xuat.setEnabled(true);
                    } else {
                        xuat.setEnabled(false);
                        tongKet.setText("Không có dữ liệu.");
                        thongBao.setText("Không có dữ liệu trong khoảng thời gian đã chọn.");
                    }
                    
                    
                } catch (Exception loi) {
                    loaiDaLoc = null;
                    model.setRowCount(0);
                    tongKet.setText("Không có dữ liệu.");
                    thongBao.setText("Không tải được báo cáo.");
                    GiaoDienChung.baoLoi(BaoCaoThongKePanel.this, loi);
                } finally {
                    dangTai = false;
                    apDung.setEnabled(true);
                    khoangNgay.setEnabled(true);
                    loaiBaoCao.setEnabled(true);
                }
            }
        }.execute();
    }

    // Chức năng 2: Hiển thị bảng tùy theo loại thống kê.
    private void hienThiBaoCao(String loai, ThongKeMoRongDAO.Bang duLieu) {
        // Lấy tên cột trực tiếp từ DAO để đồng bộ dữ liệu.
        model.setColumnIdentifiers(duLieu.cot());
        model.setRowCount(0);

        // Thêm dữ liệu từ SQL vào JTable.
        for (Object[] dong : duLieu.dong()) {
            model.addRow(dong);
        }

        // Cập nhật giao diện bảng.
        GiaoDienChung.bang(bang);
        dinhDangBang();
    }

    // Định dạng JTable cơ bản và số tiền Việt Nam.
    private void dinhDangBang() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected,
                    boolean focus, int row, int column) {

                super.getTableCellRendererComponent(table, value, selected, focus, row, column);

                String tenCot = table.getColumnName(column);

                if (value instanceof BigDecimal || value instanceof Number) {
                    setText(dinhDang.format(value));
                }

                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                setToolTipText(value == null ? null : value.toString());
                return this;
            }
        };

        for (int i = 0; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        bang.getColumnModel().getColumn(0).setPreferredWidth(55);
        bang.getColumnModel().getColumn(1).setPreferredWidth(170);
        bang.getTableHeader().setReorderingAllowed(false);
    }

    // Chức năng 3: Xuất báo cáo ra file CSV.
    private void xuatCSV() {
        if (dangTai || loaiDaLoc == null
                || !Objects.equals(loaiDaLoc, loaiBaoCao.getSelectedItem())
                || !Objects.equals(tuDaLoc, khoangNgay.getTuNgay())
                || !Objects.equals(denDaLoc, khoangNgay.getDenNgay())) {
            JOptionPane.showMessageDialog(this, "Hãy nhấn Áp dụng trước khi xuất CSV.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("ThongKeMediDent.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File tep = chooser.getSelectedFile();

        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            tep = new File(tep.getPath() + ".csv");
        }

        if (tep.exists() && JOptionPane.showConfirmDialog(this,
                "Ghi đè file đã có?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            String[] cot = new String[model.getColumnCount()];
            for (int i = 0; i < cot.length; i++) cot[i] = model.getColumnName(i);

            List<Object[]> danhSach = new ArrayList<>();

            for (int i = 0; i < bang.getRowCount(); i++) {
                int dongGoc = bang.convertRowIndexToModel(i);
                Object[] dong = new Object[model.getColumnCount()];

                for (int j = 0; j < dong.length; j++) {
                    dong[j] = model.getValueAt(dongGoc, j);
                }

                danhSach.add(dong);
            }

            String thoiGian = tuDaLoc == null ? "Tất cả thời gian"
                    : tuDaLoc + " đến " + denDaLoc;

            XuatBaoCaoCSV.xuat(tep.toPath(), loaiDaLoc, thoiGian, cot, danhSach);

            JOptionPane.showMessageDialog(this, "Xuất CSV thành công.");
        } catch (Exception loi) {
            GiaoDienChung.baoLoi(this, loi);
        }
    }

    // Chạy Panel độc lập để kiểm tra giao diện.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MediDent - Báo cáo thống kê");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new BaoCaoThongKePanel());
            frame.setSize(1150, 720);
            frame.setMinimumSize(new Dimension(950, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
