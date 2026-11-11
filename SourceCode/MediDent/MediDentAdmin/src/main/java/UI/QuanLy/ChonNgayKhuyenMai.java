package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/** Bộ chọn lịch dùng riêng cho thời gian khuyến mãi, cho phép chọn cả ngày tương lai. */
public final class ChonNgayKhuyenMai extends JPanel {
    private static final DateTimeFormatter DINH_DANG = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final Color XANH = new Color(2,132,199);
    private final JTextField hienThi = new JTextField();
    private final JButton moLich = new JButton("Chọn ngày");
    private final JPopupMenu lich = new JPopupMenu();
    private LocalDate ngay;
    private YearMonth thangDangXem = YearMonth.from(LocalDate.now());

    public ChonNgayKhuyenMai() {
        super(new BorderLayout(6, 0));
        setOpaque(false);
        hienThi.setEditable(false);
        hienThi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hienThi.setBackground(Color.WHITE);
        hienThi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226,232,240)),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        moLich.setFont(new Font("Segoe UI", Font.BOLD, 13));
        moLich.setBackground(Color.WHITE);
        moLich.setForeground(XANH);
        moLich.setFocusPainted(false);
        moLich.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226,232,240)),
                BorderFactory.createEmptyBorder(8,12,8,12)));
        moLich.addActionListener(e -> {
            veLich();
            lich.show(this, 0, getHeight());
        });
        add(hienThi, BorderLayout.CENTER);
        add(moLich, BorderLayout.EAST);
        setPreferredSize(new Dimension(300, 42));
        datNgay(null);
    }

    public LocalDate layNgay() { return ngay; }

    public void datNgay(LocalDate giaTri) {
        LocalDate cu = ngay;
        ngay = giaTri;
        if (giaTri != null) thangDangXem = YearMonth.from(giaTri);
        hienThi.setText(giaTri == null ? "Chưa chọn ngày" : giaTri.format(DINH_DANG));
        firePropertyChange("ngay", cu, giaTri);
    }

    private void veLich() {
        lich.removeAll();
        JPanel khung = new JPanel(new BorderLayout(0, 8));
        khung.setBackground(Color.WHITE);
        khung.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel dau = new JPanel(new BorderLayout(6, 0));
        dau.setOpaque(false);
        JButton truoc = new JButton("‹");
        JButton sau = new JButton("›");
        JLabel tieuDe = new JLabel("Tháng " + thangDangXem.getMonthValue()
                + " / " + thangDangXem.getYear(), SwingConstants.CENTER);
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        truoc.addActionListener(e -> { thangDangXem = thangDangXem.minusMonths(1); veLich(); });
        sau.addActionListener(e -> { thangDangXem = thangDangXem.plusMonths(1); veLich(); });
        dau.add(truoc, BorderLayout.WEST);
        dau.add(tieuDe, BorderLayout.CENTER);
        dau.add(sau, BorderLayout.EAST);

        JPanel thangNam = new JPanel(new GridLayout(1, 2, 6, 0));
        thangNam.setOpaque(false);
        JComboBox<Integer> thang = new JComboBox<>();
        for (int m = 1; m <= 12; m++) thang.addItem(m);
        thang.setSelectedItem(thangDangXem.getMonthValue());
        JSpinner nam = new JSpinner(new SpinnerNumberModel(thangDangXem.getYear(), 1, 9999, 1));
        thang.addActionListener(e -> {
            thangDangXem = YearMonth.of(thangDangXem.getYear(), (Integer)thang.getSelectedItem());
            veLich();
        });
        nam.addChangeListener(e -> {
            thangDangXem = YearMonth.of((Integer)nam.getValue(), thangDangXem.getMonthValue());
            veLich();
        });
        thangNam.add(thang);
        thangNam.add(nam);
        JPanel dauLich = new JPanel(new BorderLayout(0, 6));
        dauLich.setOpaque(false);
        dauLich.add(dau, BorderLayout.NORTH);
        dauLich.add(thangNam, BorderLayout.CENTER);
        khung.add(dauLich, BorderLayout.NORTH);

        JPanel luoi = new JPanel(new GridLayout(0, 7, 3, 3));
        luoi.setOpaque(false);
        for (String thu : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
            JLabel nhan = new JLabel(thu, SwingConstants.CENTER);
            nhan.setFont(new Font("Segoe UI", Font.BOLD, 11));
            luoi.add(nhan);
        }
        for (int i = 1; i < thangDangXem.atDay(1).getDayOfWeek().getValue(); i++)
            luoi.add(new JLabel(""));
        for (int d = 1; d <= thangDangXem.lengthOfMonth(); d++) {
            LocalDate giaTri = thangDangXem.atDay(d);
            JButton nut = new JButton(String.valueOf(d));
            nut.setMargin(new Insets(3, 2, 3, 2));
            nut.setPreferredSize(new Dimension(38, 31));
            nut.setFocusPainted(false);
            nut.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            if (giaTri.equals(ngay)) {
                nut.setBackground(new Color(224,242,254));
                nut.setForeground(XANH);
            }
            nut.addActionListener(e -> { datNgay(giaTri); lich.setVisible(false); });
            luoi.add(nut);
        }
        khung.add(luoi, BorderLayout.CENTER);
        lich.add(khung);
        lich.pack();
    }
}
