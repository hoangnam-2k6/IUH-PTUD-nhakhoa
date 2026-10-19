/*
 * NgayChon: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.Chung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class NgayChon extends JPanel {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.forLanguageTag("vi-VN"));

    private static final String[] TEN_THANG = {
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
        "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    };

    private static final Color CHU_DAO = new Color(0x0284C7);
    private static final Color NEN_CHON = new Color(0xE0F2FE);

    private LocalDate ngayDuocChon;
    private YearMonth thangDangXem = YearMonth.from(LocalDate.now().minusYears(25));

    private final JTextField hienThi = new JTextField("Chưa chọn ngày sinh");
    private final JButton moLich = new JButton("Chọn lịch");
    private final JPopupMenu popup = new JPopupMenu();

    /** Khởi tạo bộ chọn ngày tháng bằng điều khiển Swing. */
    public NgayChon() {
        super(new BorderLayout(6, 0));
        setOpaque(false);

        hienThi.setEditable(false);
        hienThi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hienThi.setForeground(GiaoDien.MUTED);
        hienThi.setBackground(Color.WHITE);
        hienThi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDien.BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));

        moLich.setFont(new Font("Segoe UI", Font.BOLD, 11));
        moLich.setForeground(CHU_DAO);
        moLich.setBackground(Color.WHITE);
        moLich.setFocusPainted(false);
        moLich.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDien.BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        moLich.setToolTipText("Mở lịch để chọn ngày sinh hợp lệ");
        moLich.addActionListener(event -> moBoChonNgay());

        add(hienThi, BorderLayout.CENTER);
        add(moLich, BorderLayout.EAST);
        setPreferredSize(new Dimension(300, 37));
    }

    /** Kiểm tra ngày sinh đã chọn có nằm trong khoảng chấp nhận được hay không. */
    public static boolean ngaySinhHopLe(LocalDate date) {
        LocalDate homNay = LocalDate.now();
        return date != null
                && !date.isAfter(homNay)
                && !date.isBefore(homNay.minusYears(100));
    }

    /** Cập nhật thuộc tính Ngay theo giá trị được truyền vào. */
    public void setNgay(LocalDate date) {
        ngayDuocChon = date;
        if (date != null) {
            thangDangXem = YearMonth.from(date);
            hienThi.setText(date.format(FORMAT));
            hienThi.setForeground(ngaySinhHopLe(date) ? GiaoDien.TEXT : GiaoDien.RED);
            if (!ngaySinhHopLe(date)) {
                hienThi.setToolTipText("Ngày sinh cũ ngoài giới hạn; vui lòng chọn lại.");
            } else {
                hienThi.setToolTipText(null);
            }
        } else {
            thangDangXem = YearMonth.from(LocalDate.now().minusYears(25));
            hienThi.setText("Chưa chọn ngày sinh");
            hienThi.setForeground(GiaoDien.MUTED);
            hienThi.setToolTipText(null);
        }
    }

    /** Lấy giá trị thuộc tính Ngay từ đối tượng hiện tại. */
    public LocalDate getNgay() {
        if (ngayDuocChon != null && !ngaySinhHopLe(ngayDuocChon)) {
            throw new IllegalArgumentException(
                    "Ngày sinh phải từ " + LocalDate.now().minusYears(100).format(FORMAT)
                    + " đến " + LocalDate.now().format(FORMAT) + ".");
        }
        return ngayDuocChon;
    }

    /** Hiển thị trang được chọn, giữ nguyên sơ đồ điều hướng hiện có. */
    private void moBoChonNgay() {
        popup.removeAll();
        popup.setBorder(BorderFactory.createLineBorder(GiaoDien.BORDER));
        thangDangXem = YearMonth.from(ngayDuocChon != null
                ? ngayDuocChon : thangDangXem.atDay(1));
        veLich();
        popup.show(this, 0, getHeight() + 2);
    }

    /** Vẽ nội dung trong thành phần đồ họa Swing. */
    private void veLich() {
        popup.removeAll();
        JPanel khung = new JPanel(new BorderLayout(0, 10));
        khung.setBackground(Color.WHITE);
        khung.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel dau = new JPanel(new BorderLayout(5, 0));
        dau.setOpaque(false);
        JButton truoc = new JButton("<");
        JButton sau = new JButton(">");
        String tieuDe = TEN_THANG[thangDangXem.getMonthValue() - 1]
                + " / " + thangDangXem.getYear();
        JLabel thang = new JLabel(tieuDe, SwingConstants.CENTER);
        thang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        thang.setForeground(GiaoDien.TEXT);

        YearMonth min = YearMonth.from(LocalDate.now().minusYears(100));
        YearMonth max = YearMonth.from(LocalDate.now());
        truoc.setEnabled(thangDangXem.isAfter(min));
        sau.setEnabled(thangDangXem.isBefore(max));
        truoc.addActionListener(e -> {
            thangDangXem = thangDangXem.minusMonths(1);
            veLich();
        });
        sau.addActionListener(e -> {
            thangDangXem = thangDangXem.plusMonths(1);
            veLich();
        });
        dau.add(truoc, BorderLayout.WEST);
        dau.add(thang, BorderLayout.CENTER);
        dau.add(sau, BorderLayout.EAST);

        JPanel chonThangNam = new JPanel(new GridLayout(1, 2, 8, 0));
        chonThangNam.setOpaque(false);
        JComboBox<String> thangBox = new JComboBox<>(TEN_THANG);
        JComboBox<Integer> namBox = new JComboBox<>();
        for (int nam = max.getYear(); nam >= min.getYear(); nam--) {
            namBox.addItem(nam);
        }
        thangBox.setSelectedIndex(thangDangXem.getMonthValue() - 1);
        namBox.setSelectedItem(thangDangXem.getYear());
        thangBox.addActionListener(e -> doiThangNam(thangBox, namBox));
        namBox.addActionListener(e -> doiThangNam(thangBox, namBox));
        chonThangNam.add(thangBox);
        chonThangNam.add(namBox);

        JPanel thanhDieuHuong = new JPanel(new BorderLayout(0, 8));
        thanhDieuHuong.setOpaque(false);
        thanhDieuHuong.add(dau, BorderLayout.NORTH);
        thanhDieuHuong.add(chonThangNam, BorderLayout.SOUTH);
        khung.add(thanhDieuHuong, BorderLayout.NORTH);

        JPanel luoi = new JPanel(new GridLayout(0, 7, 3, 3));
        luoi.setOpaque(false);
        String[] thu = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String item : thu) {
            JLabel label = new JLabel(item, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10));
            label.setForeground(GiaoDien.MUTED);
            luoi.add(label);
        }

        int oDau = thangDangXem.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < oDau; i++) {
            luoi.add(new JLabel(""));
        }
        for (int d = 1; d <= thangDangXem.lengthOfMonth(); d++) {
            final LocalDate luaChon = thangDangXem.atDay(d);
            JButton nutNgay = new JButton(String.valueOf(d));
            nutNgay.setMargin(new Insets(3, 0, 3, 0));
            nutNgay.setPreferredSize(new Dimension(37, 32));
            nutNgay.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            nutNgay.setFocusPainted(false);
            nutNgay.setEnabled(ngaySinhHopLe(luaChon));
            nutNgay.setForeground(GiaoDien.TEXT);
            nutNgay.setBackground(Color.WHITE);
            if (luaChon.equals(ngayDuocChon)) {
                nutNgay.setBackground(NEN_CHON);
                nutNgay.setForeground(CHU_DAO);
            }
            nutNgay.addActionListener(e -> {
                setNgay(luaChon);
                popup.setVisible(false);
            });
            luoi.add(nutNgay);
        }
        khung.add(luoi, BorderLayout.CENTER);

        JLabel ghiChu = new JLabel("Không chọn ngày tương lai hoặc quá 100 tuổi.");
        ghiChu.setForeground(GiaoDien.MUTED);
        ghiChu.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        khung.add(ghiChu, BorderLayout.SOUTH);

        popup.add(khung);
        popup.revalidate();
        popup.repaint();
        popup.pack();
    }

    /** Thay đổi số ngày và ô ngày khi người dùng chọn tháng hoặc năm khác. */
    private void doiThangNam(JComboBox<String> thangBox, JComboBox<Integer> namBox) {
        Integer nam = (Integer) namBox.getSelectedItem();
        if (nam == null) return;
        YearMonth moi = YearMonth.of(nam, thangBox.getSelectedIndex() + 1);
        YearMonth min = YearMonth.from(LocalDate.now().minusYears(100));
        YearMonth max = YearMonth.from(LocalDate.now());
        if (moi.isBefore(min)) moi = min;
        if (moi.isAfter(max)) moi = max;
        if (!moi.equals(thangDangXem)) {
            thangDangXem = moi;

            veLich();
        }
    }
}
