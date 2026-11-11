package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import DAO.BenhNhanDAO;
import Entity.BenhNhan;
import enums.gioitinh;
import enums.trangthaidieutri;
import validation.Regex_BenhNhan;

@SuppressWarnings("serial")
public class ThemBenhNhanPanel extends JPanel implements ActionListener {

    private Runnable quayLai;
    private final BenhNhanDAO bnDAO = new BenhNhanDAO();

    private final Color BACKGROUND = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color PRIMARY = new Color(37, 99, 235);
    private final Color TEXT = new Color(30, 41, 59);
    private final Color SUBTEXT = new Color(100, 116, 139);
    private final Color BORDER = new Color(220, 227, 236);

    private JTextField txtMaBN;
    private JTextField txtHoTen;
    private DatePickerField dateNgaySinh;
    private JComboBox<gioitinh> cboGioiTinh;
    private JTextField txtSDT;
    private JTextField txtEmail;
    private JTextArea txtDiaChi;
    private JTextArea txtTienSuBenh;
    private JComboBox<trangthaidieutri> cboTrangThai;

    private JButton btnThem;
    private JButton btnHuy;

    public ThemBenhNhanPanel() {
        this(null);
    }

    public ThemBenhNhanPanel(Runnable callback) {
        this.quayLai = callback;
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        khoiTaoGiaoDien();
        taoMaBenhNhanTuDong();
    }

    private void khoiTaoGiaoDien() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Thêm bệnh nhân");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 25));
        lblTieuDe.setForeground(TEXT);

        JLabel lblMoTa = new JLabel("Nhập thông tin bệnh nhân mới vào hệ thống");
        lblMoTa.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMoTa.setForeground(SUBTEXT);
        pnlHeader.add(lblTieuDe, BorderLayout.NORTH);
        pnlHeader.add(lblMoTa, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        txtMaBN = taoTextField();
        txtMaBN.setEditable(false);
        txtMaBN.setFocusable(false);
        txtMaBN.setBackground(new Color(241, 245, 249));
        txtMaBN.setForeground(SUBTEXT);
        txtMaBN.setToolTipText("Mã bệnh nhân được tạo tự động.");

        txtHoTen = taoTextField();
        dateNgaySinh = new DatePickerField();
        txtSDT = taoTextField();
        chiChoNhapSo(txtSDT);
        txtSDT.setToolTipText("Số điện thoại chỉ gồm chữ số.");
        txtEmail = taoTextField();
        txtDiaChi = taoTextArea();
        txtTienSuBenh = taoTextArea();

        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthaidieutri.values());
        cboGioiTinh.setSelectedIndex(-1);
        cboTrangThai.setSelectedIndex(-1);
        dinhDangHopChon(cboGioiTinh);
        dinhDangHopChon(cboTrangThai);

        JPanel pnlForm = taoPanelForm();
        themTruong(pnlForm, "Mã bệnh nhân *", txtMaBN, 0, 0, 1);
        themTruong(pnlForm, "Họ và tên *", txtHoTen, 1, 0, 1);
        themTruong(pnlForm, "Ngày sinh", dateNgaySinh, 0, 1, 1);
        themTruong(pnlForm, "Giới tính", cboGioiTinh, 1, 1, 1);
        themTruong(pnlForm, "Số điện thoại", txtSDT, 0, 2, 1);
        themTruong(pnlForm, "Email", txtEmail, 1, 2, 1);
        themTruong(pnlForm, "Địa chỉ", taoScrollPane(txtDiaChi), 0, 3, 1);
        themTruong(pnlForm, "Tiền sử bệnh", taoScrollPane(txtTienSuBenh), 1, 3, 1);
        themTruong(pnlForm, "Trạng thái điều trị", cboTrangThai, 0, 4, 2);
        add(pnlForm, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnThem = new JButton("Thêm bệnh nhân");
        dinhDangButtonSecondary(btnHuy);
        dinhDangButtonPrimary(btnThem);
        btnHuy.addActionListener(this);
        btnThem.addActionListener(this);

        JPanel pnlButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlButton.setOpaque(false);
        pnlButton.add(btnHuy);
        pnlButton.add(btnThem);
        add(pnlButton, BorderLayout.SOUTH);
    }

    private JPanel taoPanelForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(WHITE);
        panel.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    private JTextField taoTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setBackground(WHITE);
        txt.setForeground(TEXT);
        txt.setCaretColor(PRIMARY);
        txt.setPreferredSize(new Dimension(250, 40));
        txt.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(8, 10, 8, 10)
        ));
        return txt;
    }

    private JTextArea taoTextArea() {
        JTextArea txt = new JTextArea(2, 20);
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBackground(WHITE);
        txt.setBorder(new EmptyBorder(8, 10, 8, 10));
        return txt;
    }

    private JScrollPane taoScrollPane(JTextArea txt) {
        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(250, 65));
        scroll.setBorder(new LineBorder(BORDER, 1, true));
        return scroll;
    }

    private void dinhDangHopChon(JComboBox<?> cbo) {
        cbo.setFont(new Font("Arial", Font.PLAIN, 14));
        cbo.setBackground(WHITE);
        cbo.setForeground(TEXT);
        cbo.setPreferredSize(new Dimension(250, 40));
        cbo.setBorder(new LineBorder(BORDER, 1, true));
    }

    private void themTruong(JPanel panel, String ten, JComponent component,
                            int cot, int hang, int soCot) {
        JPanel pnlTruong = new JPanel(new BorderLayout(0, 8));
        pnlTruong.setOpaque(false);
        JLabel lbl = new JLabel(ten);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(TEXT);
        pnlTruong.add(lbl, BorderLayout.NORTH);
        pnlTruong.add(component, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = cot;
        gbc.gridy = hang;
        gbc.gridwidth = soCot;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 12, 10, 12);
        panel.add(pnlTruong, gbc);
    }

    private void dinhDangButtonPrimary(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(PRIMARY);
        btn.setForeground(WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void dinhDangButtonSecondary(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(WHITE);
        btn.setForeground(TEXT);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(BORDER, 1, true), new EmptyBorder(11, 20, 11, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Chỉ cho phép nhập chữ số vào ô số điện thoại.
    // Định dạng điện thoại sẽ được kiểm tra khi bấm Thêm.
    private void chiChoNhapSo(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                private boolean laChuoiSoHopLe(String text) {
                    return Regex_BenhNhan.laSDTDangNhapHopLe(text);
                }

                @Override
                public void insertString(FilterBypass fb, int offset,
                        String text, AttributeSet attr) throws BadLocationException {
                    if (text == null) {
                        return;
                    }
                    String hienTai = fb.getDocument().getText(
                        0, fb.getDocument().getLength()
                    );
                    String moi = hienTai.substring(0, offset)
                        + text + hienTai.substring(offset);
                    if (laChuoiSoHopLe(moi)) {
                        super.insertString(fb, offset, text, attr);
                    }
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length,
                        String text, AttributeSet attrs) throws BadLocationException {
                    String chen = text == null ? "" : text;
                    String hienTai = fb.getDocument().getText(
                        0, fb.getDocument().getLength()
                    );
                    String moi = hienTai.substring(0, offset)
                        + chen + hienTai.substring(offset + length);
                    if (laChuoiSoHopLe(moi)) {
                        super.replace(fb, offset, length, chen, attrs);
                    }
                }

                @Override
                public void remove(FilterBypass fb, int offset, int length)
                        throws BadLocationException {
                    super.remove(fb, offset, length);
                }
            }
        );
    }

    // Tự động lấy mã bệnh nhân lớn nhất trong database và tăng lên 1.
    private void taoMaBenhNhanTuDong() {
        try {
            String maMoi = bnDAO.layMaBenhNhanTiepTheo();
            txtMaBN.setText(maMoi);
        } catch (Exception ex) {
            txtMaBN.setText("");
            btnThem.setEnabled(false);

            JOptionPane.showMessageDialog(
                this,
                "Không thể tạo mã bệnh nhân tự động. "
                    + "Vui lòng kiểm tra kết nối cơ sở dữ liệu.\n"
                    + ex.getMessage(),
                "Lỗi lấy mã bệnh nhân",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnHuy) {
            huyThem();
        } else if (e.getSource() == btnThem) {
            xuLyThem();
        }
    }

    private void xuLyThem() {
        try {
            String maBN = txtMaBN.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String tienSuBenh = txtTienSuBenh.getText().trim();

            if (!Regex_BenhNhan.laMaBNHopLe(maBN)) {
                JOptionPane.showMessageDialog(this, "Mã bệnh nhân không hợp lệ!");
                return;
            }
            if (!Regex_BenhNhan.laHoTenHopLe(hoTen)) {
                JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ hoặc bị để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }

            LocalDate ngaySinh = dateNgaySinh.layDate();
            if (!Regex_BenhNhan.laNgaySinhHopLe(ngaySinh)) {
                JOptionPane.showMessageDialog(this,
                    "Ngày sinh không được lớn hơn ngày hiện tại!",
                    "Ngày sinh không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthaidieutri tt = (trangthaidieutri) cboTrangThai.getSelectedItem();

            if (!Regex_BenhNhan.laGioiTinhHopLe(gt == null ? null : gt.name())) {
                JOptionPane.showMessageDialog(this, "Giới tính không hợp lệ!");
                return;
            }
            if (!Regex_BenhNhan.laSDTHopLe(sdt)) {
                JOptionPane.showMessageDialog(this,
                    "Số điện thoại phải bắt đầu bằng 0 và có 10 hoặc 11 chữ số!",
                    "Số điện thoại không hợp lệ", JOptionPane.WARNING_MESSAGE);
                txtSDT.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laEmailHopLe(email)) {
                JOptionPane.showMessageDialog(this, "Email không đúng định dạng!");
                txtEmail.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laDiaChiHopLe(diaChi)) {
                JOptionPane.showMessageDialog(this, "Địa chỉ không hợp lệ!");
                txtDiaChi.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laTienSuBenhHopLe(tienSuBenh)) {
                JOptionPane.showMessageDialog(this, "Tiền sử bệnh không hợp lệ!");
                txtTienSuBenh.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laTrangThaiHopLe(tt == null ? null : tt.name())) {
                JOptionPane.showMessageDialog(this, "Trạng thái điều trị không hợp lệ!");
                return;
            }

            BenhNhan bn = new BenhNhan(
                maBN, hoTen, ngaySinh, gt, sdt, email,
                diaChi, tienSuBenh, tt
            );

            boolean ketQua = bnDAO.themMoi(bn);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Thêm bệnh nhân thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm bệnh nhân thành công!");
            themThanhCong();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể thêm bệnh nhân: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huyThem() {
        if (quayLai != null) quayLai.run();
    }

    private void themThanhCong() {
        if (quayLai != null) quayLai.run();
    }

    public void moKhung() {
        JFrame frame = new JFrame("Thêm bệnh nhân");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ThemBenhNhanPanel().moKhung());
    }

    /**
     * Bộ chọn ngày tự tạo bằng Swing, không cần thư viện ngoài.
     * Giá trị trả về là LocalDate để truyền trực tiếp vào Entity BenhNhan.
     */
    private class DatePickerField extends JPanel {
        private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private final JTextField txtHienThi;
        private LocalDate selectedDate;

        DatePickerField() {
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
            btnChonNgay.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChonNgay.addActionListener(e -> moLich());

            add(txtHienThi, BorderLayout.CENTER);
            add(btnChonNgay, BorderLayout.EAST);
        }

        LocalDate layDate() {
            return selectedDate;
        }

        void datDate(LocalDate date) {
            selectedDate = date;
            txtHienThi.setText(date == null
                ? "Chưa chọn ngày sinh"
                : FORMATTER.format(date));
        }

        private void moLich() {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(
                owner,
                "Chọn ngày sinh",
                Dialog.ModalityType.APPLICATION_MODAL
            );
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout(8, 8));

            YearMonth thangDangXem = selectedDate == null
                ? YearMonth.now() : YearMonth.from(selectedDate);
            final YearMonth[] thang = { thangDangXem };

            JButton btnThangTruoc = new JButton("<");
            JButton btnThangSau = new JButton(">");
            JLabel lblThangNam = new JLabel("", SwingConstants.CENTER);
            lblThangNam.setFont(new Font("Arial", Font.BOLD, 16));

            JPanel header = new JPanel(new BorderLayout(8, 0));
            header.setBorder(new EmptyBorder(10, 10, 0, 10));
            header.add(btnThangTruoc, BorderLayout.WEST);
            header.add(lblThangNam, BorderLayout.CENTER);
            header.add(btnThangSau, BorderLayout.EAST);

            JPanel lich = new JPanel(new GridLayout(0, 7, 4, 4));
            lich.setBorder(new EmptyBorder(8, 10, 8, 10));

            Runnable capNhatLich = () -> {
                lich.removeAll();

                String[] tenNgay = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
                for (String ten : tenNgay) {
                    JLabel lbl = new JLabel(ten, SwingConstants.CENTER);
                    lbl.setFont(new Font("Arial", Font.BOLD, 12));
                    lich.add(lbl);
                }

                Locale locale = new Locale("vi", "VN");
                String tenThang = thang[0].getMonth()
                    .getDisplayName(TextStyle.FULL, locale);
                tenThang = tenThang.substring(0, 1).toUpperCase(locale)
                    + tenThang.substring(1);
                lblThangNam.setText(tenThang + " " + thang[0].getYear());

                int lech = thang[0].atDay(1).getDayOfWeek().getValue() - 1;
                int soNgay = thang[0].lengthOfMonth();

                for (int i = 0; i < lech; i++) {
                    lich.add(new JLabel(""));
                }

                for (int ngay = 1; ngay <= soNgay; ngay++) {
                    LocalDate ngayChon = thang[0].atDay(ngay);
                    JButton btnNgay = new JButton(String.valueOf(ngay));
                    btnNgay.setMargin(new Insets(4, 4, 4, 4));
                    btnNgay.setFocusPainted(false);
                    btnNgay.setFont(new Font("Arial", Font.PLAIN, 12));

                    if (ngayChon.isAfter(LocalDate.now())) {
                        btnNgay.setEnabled(false);
                    }
                    if (ngayChon.equals(selectedDate)) {
                        btnNgay.setBackground(PRIMARY);
                        btnNgay.setForeground(WHITE);
                    }

                    btnNgay.addActionListener(e -> {
                        datDate(ngayChon);
                        dialog.dispose();
                    });
                    lich.add(btnNgay);
                }

                int soOConThieu = 42 - (lech + soNgay);
                for (int i = 0; i < soOConThieu; i++) {
                    lich.add(new JLabel(""));
                }

                lich.revalidate();
                lich.repaint();
            };

            btnThangTruoc.addActionListener(e -> {
                thang[0] = thang[0].minusMonths(1);
                capNhatLich.run();
            });
            btnThangSau.addActionListener(e -> {
                if (thang[0].isBefore(YearMonth.now())) {
                    thang[0] = thang[0].plusMonths(1);
                    capNhatLich.run();
                }
            });

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton btnHomNay = new JButton("Hôm nay");
            JButton btnXoaNgay = new JButton("Xóa ngày");
            JButton btnDong = new JButton("Đóng");

            btnHomNay.addActionListener(e -> {
                datDate(LocalDate.now());
                dialog.dispose();
            });
            btnXoaNgay.addActionListener(e -> {
                datDate(null);
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
            dialog.setSize(360, 390);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }

}
