package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Component chọn ngày dùng chung cho các form Thêm/Sửa Nhân viên và Bệnh nhân.
 * Cho phép chọn trực tiếp tháng và nhập/chọn năm, không phải bấm lùi từng tuần.
 * Giá trị nhận/trả là LocalDate.
 */
@SuppressWarnings("serial")
public class BangChonLich extends JPanel {

    private static final int MIN_YEAR = 1900;
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale VI_LOCALE = new Locale("vi", "VN");

    private static final Color WHITE = Color.WHITE;
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color SUBTEXT = new Color(100, 116, 139);
    private static final Color BORDER = new Color(220, 227, 236);
    private static final Color DISABLED_BG = new Color(241, 245, 249);

    private final JTextField txtHienThi;
    private LocalDate selectedDate;

    public BangChonLich() {
        setLayout(new BorderLayout(6, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(250, 40));

        txtHienThi = new JTextField();
        txtHienThi.setEditable(false);
        txtHienThi.setFocusable(false);
        txtHienThi.setFont(new Font("Arial", Font.PLAIN, 14));
        txtHienThi.setBackground(WHITE);
        txtHienThi.setForeground(TEXT);
        txtHienThi.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtHienThi.setText("Chưa chọn ngày sinh");

        JButton btnChonNgay = new JButton("Chọn lịch");
        btnChonNgay.setFont(new Font("Arial", Font.PLAIN, 12));
        btnChonNgay.setBackground(WHITE);
        btnChonNgay.setForeground(PRIMARY);
        btnChonNgay.setFocusPainted(false);
        btnChonNgay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnChonNgay.addActionListener(e -> moLich());

        add(txtHienThi, BorderLayout.CENTER);
        add(btnChonNgay, BorderLayout.EAST);
    }

    /** Trả về ngày đã chọn; trả về null nếu chưa chọn hoặc đã xóa ngày. */
    public LocalDate getDate() {
        return selectedDate;
    }

    /** Đặt ngày từ dữ liệu có sẵn, thường dùng khi mở form Sửa. */
    public void setDate(LocalDate date) {
        selectedDate = date;
        txtHienThi.setText(date == null
            ? "Chưa chọn ngày sinh"
            : FORMATTER.format(date));
    }

    private void moLich() {
        LocalDate today = LocalDate.now();
        LocalDate initialDate = selectedDate == null ? today : selectedDate;

        int initialYear = Math.max(MIN_YEAR, Math.min(today.getYear(), initialDate.getYear()));
        int initialMonth = initialDate.getMonthValue();
        if (initialYear == today.getYear() && initialMonth > today.getMonthValue()) {
            initialMonth = today.getMonthValue();
        }

        final int[] thangDangXem = { initialMonth };
        final int[] namDangXem = { initialYear };

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(
            owner,
            "Chọn ngày sinh",
            Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.setResizable(false);

        // Khu vực chọn tháng/năm trực tiếp.
        JButton btnThangTruoc = new JButton("‹");
        JButton btnThangSau = new JButton("›");
        btnThangTruoc.setToolTipText("Tháng trước");
        btnThangSau.setToolTipText("Tháng sau");

        String[] tenThang = new String[12];
        for (int i = 0; i < 12; i++) {
            String ten = Month.of(i + 1).getDisplayName(TextStyle.FULL, VI_LOCALE);
            tenThang[i] = ten.substring(0, 1).toUpperCase(VI_LOCALE) + ten.substring(1);
        }

        JComboBox<String> cboThang = new JComboBox<>(tenThang);
        cboThang.setSelectedIndex(initialMonth - 1);
        cboThang.setPreferredSize(new Dimension(135, 30));

        JSpinner spnNam = new JSpinner(
            new SpinnerNumberModel(initialYear, MIN_YEAR, today.getYear(), 1)
        );
        spnNam.setEditor(new JSpinner.NumberEditor(spnNam, "####"));
        spnNam.setPreferredSize(new Dimension(85, 30));
        spnNam.setToolTipText("Có thể nhập trực tiếp năm, từ " + MIN_YEAR + " đến " + today.getYear());

        JPanel pnlThangNam = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pnlThangNam.setOpaque(false);
        pnlThangNam.add(new JLabel("Tháng:"));
        pnlThangNam.add(cboThang);
        pnlThangNam.add(new JLabel("Năm:"));
        pnlThangNam.add(spnNam);

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBorder(new EmptyBorder(12, 12, 4, 12));
        header.add(btnThangTruoc, BorderLayout.WEST);
        header.add(pnlThangNam, BorderLayout.CENTER);
        header.add(btnThangSau, BorderLayout.EAST);

        JPanel lich = new JPanel(new GridLayout(7, 7, 4, 4));
        lich.setBorder(new EmptyBorder(6, 12, 8, 12));

        Runnable capNhatLich = () -> {
            lich.removeAll();

            String[] thuTrongTuan = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
            for (String thu : thuTrongTuan) {
                JLabel lblThu = new JLabel(thu, SwingConstants.CENTER);
                lblThu.setFont(new Font("Arial", Font.BOLD, 12));
                lblThu.setForeground(SUBTEXT);
                lich.add(lblThu);
            }

            YearMonth yearMonth = YearMonth.of(namDangXem[0], thangDangXem[0]);
            int lech = yearMonth.atDay(1).getDayOfWeek().getValue() - 1;
            int soNgay = yearMonth.lengthOfMonth();

            for (int i = 0; i < lech; i++) {
                lich.add(new JLabel(""));
            }

            for (int ngay = 1; ngay <= soNgay; ngay++) {
                LocalDate ngayChon = yearMonth.atDay(ngay);
                JButton btnNgay = new JButton(String.valueOf(ngay));
                btnNgay.setFont(new Font("Arial", Font.PLAIN, 12));
                btnNgay.setMargin(new java.awt.Insets(3, 3, 3, 3));
                btnNgay.setFocusPainted(false);

                if (ngayChon.isAfter(today)) {
                    btnNgay.setEnabled(false);
                    btnNgay.setBackground(DISABLED_BG);
                    btnNgay.setForeground(new Color(160, 170, 185));
                } else if (ngayChon.equals(selectedDate)) {
                    btnNgay.setBackground(PRIMARY);
                    btnNgay.setForeground(WHITE);
                } else {
                    btnNgay.setBackground(WHITE);
                    btnNgay.setForeground(TEXT);
                }

                btnNgay.addActionListener(e -> {
                    setDate(ngayChon);
                    dialog.dispose();
                });
                lich.add(btnNgay);
            }

            int soOConThieu = 42 - (lech + soNgay);
            for (int i = 0; i < soOConThieu; i++) {
                lich.add(new JLabel(""));
            }

            // Vô hiệu hóa nút tháng sau nếu đang ở tháng hiện tại.
            btnThangSau.setEnabled(yearMonth.isBefore(YearMonth.now()));
            btnThangTruoc.setEnabled(yearMonth.isAfter(YearMonth.of(MIN_YEAR, 1)));

            lich.revalidate();
            lich.repaint();
        };

        cboThang.addActionListener(e -> {
            int thangMoi = cboThang.getSelectedIndex() + 1;
            YearMonth luaChon = YearMonth.of(namDangXem[0], thangMoi);
            if (luaChon.isAfter(YearMonth.now())) {
                cboThang.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
                return;
            }
            thangDangXem[0] = thangMoi;
            capNhatLich.run();
        });

        spnNam.addChangeListener(e -> {
            namDangXem[0] = (Integer) spnNam.getValue();
            if (namDangXem[0] == today.getYear()
                    && thangDangXem[0] > today.getMonthValue()) {
                thangDangXem[0] = today.getMonthValue();
                cboThang.setSelectedIndex(thangDangXem[0] - 1);
            }
            capNhatLich.run();
        });

        btnThangTruoc.addActionListener(e -> {
            YearMonth current = YearMonth.of(namDangXem[0], thangDangXem[0]).minusMonths(1);
            namDangXem[0] = current.getYear();
            thangDangXem[0] = current.getMonthValue();
            spnNam.setValue(namDangXem[0]);
            cboThang.setSelectedIndex(thangDangXem[0] - 1);
            capNhatLich.run();
        });

        btnThangSau.addActionListener(e -> {
            YearMonth current = YearMonth.of(namDangXem[0], thangDangXem[0]);
            if (current.isBefore(YearMonth.now())) {
                current = current.plusMonths(1);
                namDangXem[0] = current.getYear();
                thangDangXem[0] = current.getMonthValue();
                spnNam.setValue(namDangXem[0]);
                cboThang.setSelectedIndex(thangDangXem[0] - 1);
                capNhatLich.run();
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBorder(new EmptyBorder(0, 8, 4, 8));

        JButton btnXoaNgay = new JButton("Xóa ngày");
        JButton btnHomNay = new JButton("Hôm nay");
        JButton btnDong = new JButton("Đóng");

        btnXoaNgay.addActionListener(e -> {
            setDate(null);
            dialog.dispose();
        });
        btnHomNay.addActionListener(e -> {
            setDate(today);
            dialog.dispose();
        });
        btnDong.addActionListener(e -> dialog.dispose());

        footer.add(btnXoaNgay);
        footer.add(btnHomNay);
        footer.add(btnDong);

        capNhatLich.run();
        dialog.add(header, BorderLayout.NORTH);
        dialog.add(lich, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(430, 390));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
