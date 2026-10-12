package UI.QuanTriVien;

import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Báo cáo số liệu thật từ SQL Server: ThanhToan, PhieuKham, TaiKhoan và nhật ký.
 *
 * Bảng kết quả có cột STT 1..N; CSV xuất STT theo thứ tự đang nhìn thấy.
 * Không tự tạo doanh thu hay số lượt khám giả.
 */
public class BaoCaoThongKe extends JPanel {

    private final KhoDuLieu db;
    private final JComboBox<String> kind = new JComboBox<>(new String[]{
        "Doanh thu theo tháng", "Lượt khám theo tháng",
        "Tài khoản theo vai trò", "Nhật ký quản trị"
    });
    private final JComboBox<Integer> year = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    private final JLabel doanhThu = GiaoDien.label("Đang tải...", 24, true);
    private final JLabel luotKham = GiaoDien.label("Đang tải...", 24, true);
    private final JLabel trungBinh = GiaoDien.label("Đang tải...", 24, true);
    private final JLabel quayLai = GiaoDien.label("Đang tải...", 24, true);

    public BaoCaoThongKe(KhoDuLieu db) {
        this.db = db;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel page = GiaoDien.trang("Báo cáo - thống kê",
                "Theo dõi hiệu quả vận hành và doanh thu thực tế của phòng khám.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        // 1. Bộ lọc là combo để người dùng không nhập sai năm/loại báo cáo.
        JPanel filters = GiaoDien.hang();
        filters.add(GiaoDien.label("Loại báo cáo", 12, true));
        filters.add(kind);
        filters.add(GiaoDien.label("Năm", 12, true));
        int currentYear = LocalDate.now().getYear();
        for (int value = currentYear - 4; value <= currentYear + 1; value++) {
            year.addItem(value);
        }
        year.setSelectedItem(currentYear);
        filters.add(year);
        JButton apply = GiaoDien.nutIcon("Áp dụng bộ lọc", true,
                BieuTuong.Loai.TIM_KIEM);
        JButton export = GiaoDien.nutIcon("Xuất CSV", false,
                BieuTuong.Loai.TAI_XUONG);
        filters.add(apply);
        filters.add(export);
        page.add(filters);
        page.add(Box.createVerticalStrut(18));

        // 2. Thẻ thống kê tính từ các bảng nghiệp vụ, không hard-code.
        JPanel kpis = new JPanel(new GridLayout(2, 2, 13, 13));
        kpis.setOpaque(false);
        kpis.setPreferredSize(new Dimension(700, 215));
        kpis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 215));
        kpis.setAlignmentX(LEFT_ALIGNMENT);
        kpis.add(taoThe("Tổng doanh thu (đã thanh toán)", doanhThu));
        kpis.add(taoThe("Tổng lượt khám", luotKham));
        kpis.add(taoThe("Doanh thu / lượt khám", trungBinh));
        kpis.add(taoThe("Bệnh nhân khám từ 2 lần", quayLai));
        page.add(kpis);
        page.add(Box.createVerticalStrut(18));

        // 3. Bảng tự chọn header theo báo cáo, cột đầu là STT.
        JPanel tableCard = GiaoDien.card(new BorderLayout(0, 12));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        tableCard.setPreferredSize(new Dimension(720, 440));
        tableCard.add(GiaoDien.label("Kết quả báo cáo từ SQL Server", 14, true),
                BorderLayout.NORTH);
        tableCard.add(GiaoDien.wrapTable(table), BorderLayout.CENTER);
        page.add(tableCard);

        apply.addActionListener(event -> taiLai());
        year.addActionListener(event -> taiLai());
        kind.addActionListener(event -> taiLai());
        export.addActionListener(event -> xuatCsv());
        taiLai();
    }

    private JPanel taoThe(String title, JLabel value) {
        JPanel card = GiaoDien.card(new BorderLayout());
        card.add(GiaoDien.label(title, 11, false), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private String tien(BigDecimal amount) {
        return String.format("%,.0f đ", amount.doubleValue());
    }

    /** 4. Tải dữ liệu theo năm và đánh số 1..N. */
    private void taiLai() {
        try {
            Integer selectedYear = (Integer) year.getSelectedItem();
            if (selectedYear == null) {
                return;
            }
            LocalDate from = LocalDate.of(selectedYear, 1, 1);
            LocalDate to = LocalDate.of(selectedYear, 12, 31);
            BigDecimal revenue = db.tongDoanhThu(from, to);
            int visits = db.tongLuotKham(from, to);
            doanhThu.setText(tien(revenue));
            luotKham.setText(String.format("%,d", visits));
            trungBinh.setText(tien(visits == 0 ? BigDecimal.ZERO
                    : revenue.divide(BigDecimal.valueOf(visits), 0, RoundingMode.HALF_UP)));
            quayLai.setText(String.format("%.1f%%", db.tyLeQuayLai(from, to)));

            int selectedKind = kind.getSelectedIndex();
            List<String[]> rows;
            String[] headers;
            if (selectedKind == 0 || selectedKind == 1) {
                headers = new String[]{"STT", "Tháng " + selectedYear, "Giá trị"};
                rows = db.baoCaoTheoThang(selectedYear, selectedKind == 0);
            } else if (selectedKind == 2) {
                headers = new String[]{"STT", "Vai trò", "Số tài khoản hiện tại"};
                rows = db.phanBo();
            } else {
                headers = new String[]{"STT", "Thời điểm", "Hành động", "Chi tiết"};
                rows = db.nhatKy();
            }

            model.setRowCount(0);
            model.setColumnIdentifiers(headers);
            int number = 1;
            for (String[] values : rows) {
                Object[] data = new Object[values.length + 1];
                data[0] = number++;
                System.arraycopy(values, 0, data, 1, values.length);
                model.addRow(data);
            }
            // Header thay đổi theo báo cáo, nên cần gán renderer STT lại.
            GiaoDien.cotSoThuTu(table);
            if (table.getColumnCount() > 0) {
                table.getColumnModel().getColumn(0).setPreferredWidth(52);
            }
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 5. Xuất CSV UTF-8 có BOM để Excel đọc đúng dấu tiếng Việt. */
    private void xuatCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(
                "MediDent_BaoCao_" + year.getSelectedItem() + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        StringBuilder csv = new StringBuilder("\uFEFF");
        for (int column = 0; column < model.getColumnCount(); column++) {
            if (column > 0) {
                csv.append(',');
            }
            csv.append(escape(model.getColumnName(column)));
        }
        csv.append('\n');

        // Dùng chỉ số hàng đang hiển thị, kể cả khi người dùng sắp xếp header.
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            for (int column = 0; column < table.getColumnCount(); column++) {
                if (column > 0) {
                    csv.append(',');
                }
                Object value = column == 0 ? viewRow + 1
                        : table.getValueAt(viewRow, column);
                csv.append(escape(String.valueOf(value)));
            }
            csv.append('\n');
        }

        try {
            Files.write(chooser.getSelectedFile().toPath(),
                    csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất báo cáo CSV.");
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }

    /** 6. Escape dữ liệu để Excel không coi chuỗi người dùng là công thức. */
    private String escape(String value) {
        if (!value.isEmpty() && "=+-@".indexOf(value.charAt(0)) >= 0) {
            value = "'" + value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
