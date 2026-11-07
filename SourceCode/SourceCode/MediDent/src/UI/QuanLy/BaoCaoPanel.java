
package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import Dao.NghiepVu.BaoCaoService;
import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.QuyTacNghiepVu;
import UI.Chung.GiaoDien;
import UI.Chung.XemTruoc;

public final class BaoCaoPanel extends JPanel {

    private static final String[] CAC_LOAI_BAO_CAO = {
        "Doanh thu theo ngày", "Doanh thu theo tháng", "Doanh thu theo năm",
        "Lượt khám theo ngày", "Lượt khám theo tháng", "Lượt khám theo năm"
    };

    private final BaoCaoService dichVu;
    private final JTextField oTuNgay = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
    private final JTextField oDenNgay = new JTextField(LocalDate.now().toString(), 10);
    private final JComboBox<String> chonLoai = new JComboBox<>(CAC_LOAI_BAO_CAO);
    private final JTable bang = new JTable();
    private final JLabel nhanTong = new JLabel("Chưa có dữ liệu thống kê");
    private final JLabel nhanTrangThai = new JLabel("Chọn khoảng thời gian rồi bấm Áp dụng bộ lọc");
    private final JButton nutXuat = GiaoDien.button("Xuất CSV", false);

    private int soLanTai;
    private KetQuaBaoCao baoCaoDaLoc;

    private record KetQuaBaoCao(LocalDate tuNgay, LocalDate denNgay, String loai,
            Map<String, Object> tongHop, List<Map<String, Object>> chiTiet) {}

    public BaoCaoPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        dichVu = new BaoCaoService(phienDangNhap);
        setLayout(new BorderLayout(10, 12));
        setBackground(GiaoDien.BG);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        add(GiaoDien.label("Báo cáo và thống kê", 22, true), BorderLayout.NORTH);

        JPanel khungChinh = new JPanel(new BorderLayout(0, 10));
        khungChinh.setOpaque(false);
        khungChinh.add(taoThanhLoc(), BorderLayout.NORTH);

        JPanel vungKetQua = new JPanel(new BorderLayout(0, 10));
        vungKetQua.setOpaque(false);
        nhanTong.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        nhanTong.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        vungKetQua.add(nhanTong, BorderLayout.NORTH);

        GiaoDien.styleTable(bang);
        bang.setAutoCreateRowSorter(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        datDuLieuBang(CAC_LOAI_BAO_CAO[0], List.of());

        JScrollPane cuonBang = new JScrollPane(bang,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        GiaoDien.hỗTrợVuốtNgang(cuonBang);

        vungKetQua.add(cuonBang, BorderLayout.CENTER);
        khungChinh.add(vungKetQua, BorderLayout.CENTER);
        add(khungChinh, BorderLayout.CENTER);
        add(nhanTrangThai, BorderLayout.SOUTH);
    }

    // Tao thanh loc bao cao
    private JScrollPane taoThanhLoc() {
        JPanel thanhLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 6));
        thanhLoc.setBackground(java.awt.Color.WHITE);

        GiaoDien.styleInput(oTuNgay);
        GiaoDien.styleInput(oDenNgay);
        GiaoDien.styleInput(chonLoai);
        oTuNgay.setPreferredSize(new Dimension(119, 40));
        oDenNgay.setPreferredSize(new Dimension(119, 40));
        chonLoai.setPreferredSize(new Dimension(175, 40));

        thanhLoc.add(new JLabel("Từ ngày:"));
        thanhLoc.add(oTuNgay);
        thanhLoc.add(new JLabel("Đến ngày:"));
        thanhLoc.add(oDenNgay);
        thanhLoc.add(new JLabel("Loại báo cáo:"));
        thanhLoc.add(chonLoai);

        JButton nutApDung = GiaoDien.button("Áp dụng bộ lọc", true);
        JButton nutThangNay = GiaoDien.button("Tháng này", false);
        JButton nutNamNay = GiaoDien.button("Năm nay", false);

        nutXuat.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 13));
        nutXuat.setPreferredSize(new Dimension(122, 42));

        nutApDung.addActionListener(e -> taiLaiBaoCao());
        nutThangNay.addActionListener(e -> xemThangNay());
        nutNamNay.addActionListener(e -> xemNamNay());
        nutXuat.addActionListener(e -> xuatCsv());
        nutXuat.setEnabled(false);

        thanhLoc.add(nutApDung);
        thanhLoc.add(nutThangNay);
        thanhLoc.add(nutNamNay);
        thanhLoc.add(nutXuat);

        JScrollPane cuonThanhLoc = new JScrollPane(thanhLoc,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cuonThanhLoc.setPreferredSize(new Dimension(650, 59));
        cuonThanhLoc.setBorder(BorderFactory.createLineBorder(GiaoDien.BORDER));
        GiaoDien.hỗTrợVuốtNgang(cuonThanhLoc);
        return cuonThanhLoc;
    }

    // Loc bao cao theo thang hien tai
    private void xemThangNay() {
        LocalDate homNay = LocalDate.now();
        oTuNgay.setText(homNay.withDayOfMonth(1).toString());
        oDenNgay.setText(homNay.toString());
        chonLoai.setSelectedIndex(chonLoai.getSelectedIndex() >= 3 ? 3 : 0);
        taiLaiBaoCao();
    }

    // Loc bao cao theo nam hien tai
    private void xemNamNay() {
        LocalDate homNay = LocalDate.now();
        oTuNgay.setText(homNay.withDayOfYear(1).toString());
        oDenNgay.setText(homNay.toString());
        chonLoai.setSelectedIndex(chonLoai.getSelectedIndex() >= 3 ? 5 : 2);
        taiLaiBaoCao();
    }

    // Tai lai du lieu bao cao tu SQL Server
    public void taiLaiBaoCao() {
        LocalDate tuNgay, denNgay;
        String loai = String.valueOf(chonLoai.getSelectedItem());

        try {
            tuNgay = QuyTacNghiepVu.date(oTuNgay.getText(), "Từ ngày");
            denNgay = QuyTacNghiepVu.date(oDenNgay.getText(), "Đến ngày");
            BaoCaoService.validateReportRange(tuNgay, denNgay, loai);
        } catch (Exception loi) {
            GiaoDien.error(this, loi);
            return;
        }

        int maYeuCau = ++soLanTai;
        baoCaoDaLoc = null;
        nutXuat.setEnabled(false);
        nhanTrangThai.setText("Đang lấy dữ liệu từ SQL Server...");

        new SwingWorker<KetQuaBaoCao, Void>() {
            @Override
            protected KetQuaBaoCao doInBackground() {
                Map<String, Object> tongHop = dichVu.metrics(tuNgay, denNgay).get(0);
                List<Map<String, Object>> chiTiet = dichVu.report(tuNgay, denNgay, loai);
                return new KetQuaBaoCao(tuNgay, denNgay, loai, tongHop, chiTiet);
            }

            @Override
            protected void done() {
                if (maYeuCau != soLanTai) return;
                try {
                    baoCaoDaLoc = get();
                    hienThiTong(baoCaoDaLoc.tongHop());
                    datDuLieuBang(loai, baoCaoDaLoc.chiTiet());
                    nhanTrangThai.setText("Từ " + tuNgay + " đến " + denNgay + " - " + loai);
                    nutXuat.setEnabled(true);
                } catch (Exception loi) {
                    nhanTrangThai.setText("Không tải được báo cáo từ SQL Server");
                    GiaoDien.error(BaoCaoPanel.this, loi);
                }
            }
        }.execute();
    }

    // Giu tuong thich voi class cu dang goi refresh()
    public void refresh() { taiLaiBaoCao(); }

    // Hien thi thong tin tong hop
    private void hienThiTong(Map<String, Object> tongHop) {
        NumberFormat dinhDangTien = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        nhanTong.setText("Thực thu: " + dinhDangTien.format(tongHop.getOrDefault("Doanh thu thực thu", 0))
                + " đ     |     Lượt khám: " + tongHop.getOrDefault("Lượt khám", 0)
                + "     |     Bệnh nhân quay lại: " + tongHop.getOrDefault("Bệnh nhân quay lại", 0));
    }

    // Lay danh sach cot theo loai bao cao
    public static String[] layCotBaoCao(String loai) {
        return switch (loai) {
            case "Doanh thu theo ngày" -> new String[]{"Ngày", "Giao dịch", "Doanh thu"};
            case "Doanh thu theo tháng" -> new String[]{"Năm", "Tháng", "Giao dịch", "Doanh thu"};
            case "Doanh thu theo năm" -> new String[]{"Năm", "Giao dịch", "Doanh thu"};
            case "Lượt khám theo ngày" -> new String[]{"Ngày khám", "Lượt khám"};
            case "Lượt khám theo tháng" -> new String[]{"Năm", "Tháng", "Lượt khám"};
            case "Lượt khám theo năm" -> new String[]{"Năm", "Lượt khám"};
            default -> throw new IllegalArgumentException("Loại báo cáo không hợp lệ");
        };
    }

    // Giu tuong thich voi code cu
    public static String[] reportColumns(String loai) {
        return layCotBaoCao(loai);
    }

    // Dua du lieu vao JTable
    private void datDuLieuBang(String loai, List<Map<String, Object>> duLieu) {
        String[] cacCot = layCotBaoCao(loai);
        DefaultTableModel moHinh = new DefaultTableModel(cacCot, 0) {
            @Override
            public boolean isCellEditable(int dong, int chiSoCot) {
                return false;
            }
        };

        for (Map<String, Object> dong : duLieu) {
            Object[] giaTriDong = new Object[cacCot.length];
            for (int i = 0; i < cacCot.length; i++) giaTriDong[i] = dong.get(cacCot[i]);
            moHinh.addRow(giaTriDong);
        }

        bang.setModel(moHinh);
        GiaoDien.styleTable(bang);
    }

    // Tao noi dung CSV
    public static String taoNoiDungCsv(String loai, LocalDate tuNgay, LocalDate denNgay,
            Map<String, Object> tongHop, List<Map<String, Object>> chiTiet) {

        StringBuilder noiDung = new StringBuilder("\uFEFF");
        noiDung.append(taoDongCsv("MEDIDENT - BÁO CÁO THỐNG KÊ"));
        noiDung.append(taoDongCsv("Loại báo cáo", loai));
        noiDung.append(taoDongCsv("Từ ngày", tuNgay.toString()));
        noiDung.append(taoDongCsv("Đến ngày", denNgay.toString()));
        noiDung.append(taoDongCsv("Doanh thu thực thu", String.valueOf(tongHop.getOrDefault("Doanh thu thực thu", 0))));
        noiDung.append(taoDongCsv("Lượt khám", String.valueOf(tongHop.getOrDefault("Lượt khám", 0))));
        noiDung.append(taoDongCsv("Bệnh nhân quay lại", String.valueOf(tongHop.getOrDefault("Bệnh nhân quay lại", 0))));
        noiDung.append(taoDongCsv("Chi phí đã nhập", String.valueOf(tongHop.getOrDefault("Chi phí đã nhập", 0))));
        noiDung.append('\n');

        String[] cacCot = layCotBaoCao(loai);
        noiDung.append(taoDongCsv(cacCot));

        for (Map<String, Object> dong : chiTiet) {
            String[] giaTriDong = new String[cacCot.length];
            for (int i = 0; i < cacCot.length; i++)
                giaTriDong[i] = String.valueOf(dong.getOrDefault(cacCot[i], ""));
            noiDung.append(taoDongCsv(giaTriDong));
        }
        return noiDung.toString();
    }

    // Giu tuong thich voi code cu
    public static String buildCsv(String loai, LocalDate tuNgay, LocalDate denNgay,
            Map<String, Object> tongHop, List<Map<String, Object>> chiTiet) {
        return taoNoiDungCsv(loai, tuNgay, denNgay, tongHop, chiTiet);
    }

    // Tao mot dong CSV va ngan Excel hieu nham thanh cong thuc
    private static String taoDongCsv(String... cacCot) {
        StringBuilder dong = new StringBuilder();
        for (int i = 0; i < cacCot.length; i++) {
            if (i > 0) dong.append(',');
            String giaTri = cacCot[i] == null ? "" : cacCot[i];
            if (!giaTri.isEmpty() && "=+-@".indexOf(giaTri.charAt(0)) >= 0) giaTri = "'" + giaTri;
            dong.append('"').append(giaTri.replace("\"", "\"\"")).append('"');
        }
        return dong.append('\n').toString();
    }

    // Xuat bao cao CSV
    private void xuatCsv() {
        if (baoCaoDaLoc == null) {
            GiaoDien.ok(this, "Hãy áp dụng bộ lọc trước khi xuất CSV");
            return;
        }

        if (!oTuNgay.getText().trim().equals(baoCaoDaLoc.tuNgay().toString())
                || !oDenNgay.getText().trim().equals(baoCaoDaLoc.denNgay().toString())
                || !chonLoai.getSelectedItem().equals(baoCaoDaLoc.loai())) {
            GiaoDien.ok(this, "Bộ lọc vừa thay đổi. Vui lòng áp dụng lại trước khi xuất CSV");
            return;
        }

        JFileChooser hopChonFile = new JFileChooser();
        hopChonFile.setSelectedFile(new File("BaoCao_MediDent.csv"));
        if (hopChonFile.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path duongDanFile = hopChonFile.getSelectedFile().toPath();
        if (!duongDanFile.toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
            duongDanFile = Path.of(duongDanFile + ".csv");

        if (Files.exists(duongDanFile) && JOptionPane.showConfirmDialog(this,
                "Ghi đè file CSV đang có?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            String noiDungCsv = taoNoiDungCsv(baoCaoDaLoc.loai(), baoCaoDaLoc.tuNgay(),
                    baoCaoDaLoc.denNgay(), baoCaoDaLoc.tongHop(), baoCaoDaLoc.chiTiet());
            Files.writeString(duongDanFile, noiDungCsv, StandardCharsets.UTF_8);
            GiaoDien.ok(this, "Đã xuất CSV: " + duongDanFile.toAbsolutePath());
        } catch (Exception loi) {
            GiaoDien.error(this, loi);
        }
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Thống kê");
    }
}
