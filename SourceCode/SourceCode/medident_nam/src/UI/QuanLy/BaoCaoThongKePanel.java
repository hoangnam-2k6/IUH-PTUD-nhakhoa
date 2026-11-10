
package UI.QuanLy;

import util.BangStyle;
import DAO.HoaDonDAO;
import DAO.LichHenDAO;
import java.awt.*;
import java.io.File;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BaoCaoThongKePanel extends JPanel {

    private HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private LichHenDAO lichHenDAO = new LichHenDAO();

    private JTextField oTuNgay = new JTextField(10);
    private JTextField oDenNgay = new JTextField(10);
    private JComboBox<String> chonLoai = new JComboBox<>(new String[]{
        "Doanh thu theo ngày", "Doanh thu theo tháng", "Doanh thu theo năm",
        "Lịch đã khám theo ngày", "Lịch đã khám theo tháng", "Lịch đã khám theo năm"
    });

    private JLabel thongBao = new JLabel("Chọn khoảng thời gian rồi bấm Áp dụng bộ lọc.");
    private JLabel tongKet = new JLabel(" ");

    private DefaultTableModel moHinhBang = new DefaultTableModel(
            new String[]{"Kỳ báo cáo", "Giao dịch / lịch", "Doanh thu (VNĐ)"}, 0) {
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return false;
        }
    };

    private JTable bang = new JTable(moHinhBang);
    private List<Object[]> ketQua = new ArrayList<>();
    private String loaiDaLoc, tuNgayDaLoc, denNgayDaLoc;
    private boolean dangTai = false;

    private JButton nutLoc = new JButton("Áp dụng bộ lọc");
    private JButton nutThang = new JButton("Tháng này");
    private JButton nutNam = new JButton("Năm nay");
    private JButton nutCSV = new JButton("Xuất CSV");

    public BaoCaoThongKePanel() {
        taoGiaoDien();
        ganSuKien();
    }

   
   
private void taoGiaoDien() {
    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JLabel tieuDe = new JLabel("BÁO CÁO - THỐNG KÊ");
    tieuDe.setFont(new Font("Arial", Font.BOLD, 20));

    JPanel phanTren = new JPanel(new BorderLayout(0, 10));
    phanTren.add(tieuDe, BorderLayout.NORTH);

    // Chinh kich thuoc cac o nhap
    oTuNgay.setPreferredSize(new Dimension(110, 38));
    oTuNgay.setMaximumSize(new Dimension(110, 38));

    oDenNgay.setPreferredSize(new Dimension(110, 38));
    oDenNgay.setMaximumSize(new Dimension(110, 38));

    chonLoai.setPreferredSize(new Dimension(190, 38));
    chonLoai.setMaximumSize(new Dimension(190, 38));

    // Trang tri cac nut
    trangTriNut(nutLoc, false, 145);
    trangTriNut(nutThang, false, 105);
    trangTriNut(nutNam, false, 100);
    trangTriNut(nutCSV, false, 105);

    // Dua tat ca thanh phan len mot hang
    JPanel thanhCongCu = new JPanel();
    thanhCongCu.setLayout(new BoxLayout(thanhCongCu, BoxLayout.X_AXIS));

    thanhCongCu.add(new JLabel("Từ ngày:"));
    thanhCongCu.add(Box.createHorizontalStrut(6));
    thanhCongCu.add(oTuNgay);
    thanhCongCu.add(Box.createHorizontalStrut(12));

    thanhCongCu.add(new JLabel("Đến ngày:"));
    thanhCongCu.add(Box.createHorizontalStrut(6));
    thanhCongCu.add(oDenNgay);
    thanhCongCu.add(Box.createHorizontalStrut(12));

    thanhCongCu.add(new JLabel("Loại báo cáo:"));
    thanhCongCu.add(Box.createHorizontalStrut(6));
    thanhCongCu.add(chonLoai);
    thanhCongCu.add(Box.createHorizontalStrut(15));

    thanhCongCu.add(nutLoc);
    thanhCongCu.add(Box.createHorizontalStrut(8));
    thanhCongCu.add(nutThang);
    thanhCongCu.add(Box.createHorizontalStrut(8));
    thanhCongCu.add(nutNam);
    thanhCongCu.add(Box.createHorizontalStrut(8));
    thanhCongCu.add(nutCSV);
    thanhCongCu.add(Box.createHorizontalGlue());

    // Giu thanh cong cu tren mot hang ke ca khi cua so hep
    JScrollPane cuonCongCu = new JScrollPane(thanhCongCu);
    cuonCongCu.setBorder(null);
    cuonCongCu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    cuonCongCu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    cuonCongCu.setPreferredSize(new Dimension(0, 55));

    phanTren.add(cuonCongCu, BorderLayout.CENTER);
    add(phanTren, BorderLayout.NORTH);

    // Giu nguyen JTable
    bang.setRowHeight(27);
        BangStyle.apDung(bang);
    bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    JScrollPane scrollBang = new JScrollPane(bang);
        BangStyle.apDungScrollPane(scrollBang);
        add(scrollBang, BorderLayout.CENTER);

    JPanel chanTrang = new JPanel(new GridLayout(2, 1));
    chanTrang.add(tongKet);
    chanTrang.add(thongBao);
    add(chanTrang, BorderLayout.SOUTH);

    oTuNgay.setText(LocalDate.now().withDayOfMonth(1).toString());
    oDenNgay.setText(LocalDate.now().toString());
}


    // Trang tri cac nut bam
    private void trangTriNut(JButton nut, boolean nutChinh, int chieuRong) {
        Color mauXanh = new Color(35, 116, 175);
        Color mauVien = new Color(195, 213, 228);

        nut.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        nut.setFont(new Font("Arial", Font.BOLD, 13));
        nut.setPreferredSize(new Dimension(chieuRong, 38));
        nut.setMinimumSize(new Dimension(chieuRong, 38));
        nut.setMaximumSize(new Dimension(chieuRong, 38));

        nut.setBackground(nutChinh ? mauXanh : Color.WHITE);
        nut.setForeground(nutChinh ? Color.WHITE : mauXanh);
        nut.setOpaque(true);
        nut.setContentAreaFilled(true);
        nut.setFocusPainted(false);
        nut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(nutChinh ? mauXanh : mauVien, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        nut.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Gan su kien cho cac nut
    private void ganSuKien() {
        nutLoc.addActionListener(e -> taiBaoCao());
        nutThang.addActionListener(e -> xemThangNay());
        nutNam.addActionListener(e -> xemNamNay());
        nutCSV.addActionListener(e -> xuatCSV());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                taiBaoCao();
            }
        });
    }

    // Xem bao cao thang hien tai
    private void xemThangNay() {
        LocalDate homNay = LocalDate.now();
        oTuNgay.setText(homNay.withDayOfMonth(1).toString());
        oDenNgay.setText(homNay.toString());

        int loai = chonLoai.getSelectedIndex();
        chonLoai.setSelectedIndex(loai < 3 ? 0 : 3);
        taiBaoCao();
    }

    // Xem bao cao nam hien tai
    private void xemNamNay() {
        LocalDate homNay = LocalDate.now();
        oTuNgay.setText(homNay.withDayOfYear(1).toString());
        oDenNgay.setText(homNay.toString());

        int loai = chonLoai.getSelectedIndex();
        chonLoai.setSelectedIndex(loai < 3 ? 2 : 5);
        taiBaoCao();
    }

    // Lay du lieu thong ke tu DAO
    private void taiBaoCao() {
        if (dangTai) return;

        try {
            LocalDate tuNgay = LocalDate.parse(oTuNgay.getText().trim());
            LocalDate denNgay = LocalDate.parse(oDenNgay.getText().trim());

            if (tuNgay.isAfter(denNgay)) {
                throw new IllegalArgumentException("Từ ngày không được lớn hơn đến ngày.");
            }
            if (tuNgay.getYear() < 2000 || denNgay.getYear() > 2100) {
                throw new IllegalArgumentException("Chọn năm từ 2000 đến 2100.");
            }

            String loai = (String) chonLoai.getSelectedItem();
            boolean doanhThu = loai.startsWith("Doanh thu");
            String theo = loai.endsWith("ngày") ? "Ngày" : loai.endsWith("tháng") ? "Tháng" : "Năm";

            dangTai = true;
            thongBao.setText("Đang tải báo cáo...");

            new SwingWorker<List<Object[]>, Void>() {
                @Override
                protected List<Object[]> doInBackground() throws Exception {
                    if (doanhThu) return hoaDonDAO.thongKeDoanhThu(tuNgay, denNgay, theo);
                    return lichHenDAO.thongKeLichDaKham(tuNgay, denNgay, theo);
                }

                @Override
                protected void done() {
                    try {
                        List<Object[]> duLieu = get();
                        hienThiBaoCao(duLieu, doanhThu);

                        loaiDaLoc = loai;
                        tuNgayDaLoc = tuNgay.toString();
                        denNgayDaLoc = denNgay.toString();

                        thongBao.setText("Từ " + tuNgay + " đến " + denNgay + " - " + loai);

                        if (!doanhThu) {
                            thongBao.setText(thongBao.getText() + " (chỉ tính lịch đã khám, không phải toàn bộ phiếu khám)");
                        }
                    } catch (Exception loi) {
                        baoLoi(loi);
                    } finally {
                        dangTai = false;
                    }
                }
            }.execute();

        } catch (Exception loi) {
            dangTai = false;
            baoLoi(loi);
        }
    }

    // Dua du lieu thong ke len JTable
    private void hienThiBaoCao(List<Object[]> duLieu, boolean doanhThu) {
        ketQua = duLieu;
        moHinhBang.setRowCount(0);

        int tongSo = 0;
        BigDecimal tongTien = BigDecimal.ZERO;

        for (Object[] dong : ketQua) {
            int soLuong = ((Number) dong[1]).intValue();
            BigDecimal soTien = doanhThu ? (BigDecimal) dong[2] : BigDecimal.ZERO;

            tongSo += soLuong;
            tongTien = tongTien.add(soTien);
            moHinhBang.addRow(new Object[]{dong[0], soLuong, doanhThu ? soTien : "-"});
        }

        if (ketQua.isEmpty()) {
            moHinhBang.addRow(new Object[]{"Không phát sinh trong kỳ", 0, doanhThu ? BigDecimal.ZERO : "-"});
        }

        String noiDung = doanhThu ? "Giao dịch: " : "Lịch đã khám: ";
        tongKet.setText(noiDung + tongSo + "    |    Doanh thu: " + tongTien + " VNĐ");
    }

    // Xuat bao cao ra file CSV
    private void xuatCSV() {
        if (dangTai || loaiDaLoc == null) {
            JOptionPane.showMessageDialog(this, "Hãy áp dụng bộ lọc và tải xong báo cáo trước khi xuất CSV.");
            return;
        }

        if (!loaiDaLoc.equals(chonLoai.getSelectedItem()) || !tuNgayDaLoc.equals(oTuNgay.getText().trim())
                || !denNgayDaLoc.equals(oDenNgay.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Bộ lọc đã thay đổi. Hãy bấm Áp dụng bộ lọc trước khi xuất CSV.");
            return;
        }

        JFileChooser chonFile = new JFileChooser();
        chonFile.setSelectedFile(new File("BaoCaoMediDent.csv"));

        if (chonFile.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File tep = chonFile.getSelectedFile();
        if (!tep.getName().toLowerCase().endsWith(".csv")) {
            tep = new File(tep.getPath() + ".csv");
        }

        if (tep.exists()) {
            int luaChon = JOptionPane.showConfirmDialog(this, "Ghi đè file CSV đã tồn tại?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (luaChon != JOptionPane.YES_OPTION) return;
        }

        try (Writer ghiFile = Files.newBufferedWriter(tep.toPath(), StandardCharsets.UTF_8)) {
            ghiFile.write('\uFEFF');
            ghiFile.write("Loại báo cáo," + dinhDangCSV(loaiDaLoc) + "\n");
            ghiFile.write("Từ ngày," + dinhDangCSV(tuNgayDaLoc) + ",Đến ngày," + dinhDangCSV(denNgayDaLoc) + "\n");

            // Ghi ten cac cot
            for (int cot = 0; cot < moHinhBang.getColumnCount(); cot++) {
                if (cot > 0) ghiFile.write(',');
                ghiFile.write(dinhDangCSV(moHinhBang.getColumnName(cot)));
            }
            ghiFile.write('\n');

            // Ghi du lieu tung dong
            for (int dong = 0; dong < moHinhBang.getRowCount(); dong++) {
                for (int cot = 0; cot < moHinhBang.getColumnCount(); cot++) {
                    if (cot > 0) ghiFile.write(',');
                    ghiFile.write(dinhDangCSV(moHinhBang.getValueAt(dong, cot)));
                }
                ghiFile.write('\n');
            }

            JOptionPane.showMessageDialog(this, "Đã xuất CSV: " + tep.getAbsolutePath());
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Dinh dang du lieu truoc khi ghi CSV
    private String dinhDangCSV(Object giaTri) {
        String noiDung = giaTri == null ? "" : giaTri.toString();

        if (!noiDung.isEmpty() && "=+-@".indexOf(noiDung.charAt(0)) >= 0) {
            noiDung = "'" + noiDung;
        }
        return "\"" + noiDung.replace("\"", "\"\"") + "\"";
    }

    // Hien thi thong bao khi xay ra loi
    private void baoLoi(Exception loi) {
        Throwable nguyenNhan = loi.getCause() == null ? loi : loi.getCause();
        String noiDung = nguyenNhan.getMessage();

        thongBao.setText("Lỗi: " + noiDung);
        JOptionPane.showMessageDialog(this, noiDung, "Lỗi thống kê", JOptionPane.ERROR_MESSAGE);
    }

    // Chay giao dien thong ke doc lap
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame khung = new JFrame("Kiểm tra giao diện Thống kê");
            khung.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            khung.add(new BaoCaoThongKePanel());
            khung.setSize(1300, 650);
            khung.setLocationRelativeTo(null);
            khung.setVisible(true);
        });
    }
}
