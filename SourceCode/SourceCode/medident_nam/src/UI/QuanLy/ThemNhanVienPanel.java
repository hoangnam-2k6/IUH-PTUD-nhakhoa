package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import DAO.NhanVienDAO;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Entity.NhanVien;
import enums.gioitinh;
import enums.trangthai_NhanVien;
import util.BangChonLich;
import validation.Regex_NhanVien;

@SuppressWarnings("serial")
public class ThemNhanVienPanel extends JPanel implements ActionListener {

    private Runnable quayLai;
    private final NhanVienDAO nvDAO = new NhanVienDAO();

    private final Color BACKGROUND = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color PRIMARY = new Color(37, 99, 235);
    private final Color TEXT = new Color(30, 41, 59);
    private final Color SUBTEXT = new Color(100, 116, 139);
    private final Color BORDER = new Color(220, 227, 236);

    private JTextField txtMaNV, txtHoTen, txtSDT, txtEmail, txtChucVu, txtMaTK;
    private BangChonLich dateNgaySinh;
    private JComboBox<gioitinh> cboGioiTinh;
    private JComboBox<trangthai_NhanVien> cboTrangThai;
    private JTextArea txtDiaChi;
    private JButton btnThem, btnHuy;

    public ThemNhanVienPanel() {
        this(null);
    }

    public ThemNhanVienPanel(Runnable callback) {
        this.quayLai = callback;
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        khoiTaoGiaoDien();
        taoMaNhanVienTuDong();
    }

    private void khoiTaoGiaoDien() {
        JPanel header = new JPanel(new BorderLayout(0, 5));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm nhân viên");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel desc = new JLabel("Nhập thông tin nhân viên mới vào hệ thống");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(SUBTEXT);
        header.add(title, BorderLayout.NORTH);
        header.add(desc, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        txtMaNV = taoTextField();
        txtMaNV.setEditable(false);
        txtMaNV.setFocusable(false);
        txtMaNV.setBackground(new Color(241, 245, 249));
        txtMaNV.setForeground(SUBTEXT);
        txtMaNV.setToolTipText("Mã nhân viên được tạo tự động theo mã lớn nhất hiện có.");
        txtHoTen = taoTextField();
        dateNgaySinh = new BangChonLich();
        txtSDT = taoTextField();
        chiChoNhapSo(txtSDT);
        txtSDT.setToolTipText("Số điện thoại chỉ gồm chữ số.");
        txtEmail = taoTextField();
        txtChucVu = taoTextField();
        txtMaTK = taoTextField();
        txtDiaChi = taoTextArea();
        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthai_NhanVien.values());
        cboGioiTinh.setSelectedIndex(-1);
        cboTrangThai.setSelectedIndex(-1);
        styleCombo(cboGioiTinh);
        styleCombo(cboTrangThai);

        JPanel form = taoPanelForm();
        themTruong(form, "Mã nhân viên *", txtMaNV, 0, 0);
        themTruong(form, "Họ và tên *", txtHoTen, 1, 0);
        themTruong(form, "Ngày sinh", dateNgaySinh, 0, 1);
        themTruong(form, "Giới tính", cboGioiTinh, 1, 1);
        themTruong(form, "Số điện thoại", txtSDT, 0, 2);
        themTruong(form, "Email", txtEmail, 1, 2);
        themTruong(form, "Địa chỉ", taoScroll(txtDiaChi), 0, 3);
        themTruong(form, "Chức vụ", txtChucVu, 1, 3);
        themTruong(form, "Trạng thái", cboTrangThai, 0, 4);
        themTruong(form, "Mã tài khoản", txtMaTK, 1, 4);
        add(form, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnThem = new JButton("Thêm nhân viên");
        styleButton(btnHuy, false);
        styleButton(btnThem, true);
        btnHuy.addActionListener(this);
        btnThem.addActionListener(this);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(btnHuy);
        buttons.add(btnThem);
        add(buttons, BorderLayout.SOUTH);
    }

    private JPanel taoPanelForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
            new EmptyBorder(15, 15, 15, 15)));
        return p;
    }

    private JTextField taoTextField() {
        JTextField t = new JTextField();
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.setBackground(WHITE);
        t.setForeground(TEXT);
        t.setCaretColor(PRIMARY);
        t.setPreferredSize(new Dimension(250, 40));
        t.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        return t;
    }

    private JTextArea taoTextArea() {
        JTextArea t = new JTextArea(2, 20);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setBorder(new EmptyBorder(8, 10, 8, 10));
        return t;
    }

    private JScrollPane taoScroll(JTextArea area) {
        JScrollPane s = new JScrollPane(area);
        s.setPreferredSize(new Dimension(250, 65));
        s.setBorder(new LineBorder(BORDER, 1, true));
        return s;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        c.setBackground(WHITE);
        c.setForeground(TEXT);
        c.setPreferredSize(new Dimension(250, 40));
        c.setBorder(new LineBorder(BORDER, 1, true));
    }

    private void themTruong(JPanel panel, String ten, JComponent component, int cot, int hang) {
        JPanel field = new JPanel(new BorderLayout(0, 8));
        field.setOpaque(false);
        JLabel label = new JLabel(ten);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(TEXT);
        field.add(label, BorderLayout.NORTH);
        field.add(component, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = cot;
        gbc.gridy = hang;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 12, 10, 12);
        panel.add(field, gbc);
    }

    private void styleButton(JButton b, boolean primary) {
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(primary ? PRIMARY : WHITE);
        b.setForeground(primary ? WHITE : TEXT);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(primary ? new EmptyBorder(12, 20, 12, 20)
            : new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(11, 20, 11, 20)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Chỉ cho phép nhập chữ số vào ô số điện thoại.
    // Giới hạn 20 ký tự theo độ dài cột NVARCHAR(20); kiểm tra định dạng
    // số điện thoại hợp lệ sẽ được thực hiện khi bấm Thêm.
    private void chiChoNhapSo(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                private boolean laChuoiSoHopLe(String text) {
                    return Regex_NhanVien.isValidSDTInput(text);
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

    // Tự động lấy mã NV lớn nhất trong database và tăng lên 1.
    private void taoMaNhanVienTuDong() {
        try {
            String maMoi = nvDAO.getMaNhanVienTiepTheo();
            txtMaNV.setText(maMoi);
        } catch (Exception ex) {
            txtMaNV.setText("");
            btnThem.setEnabled(false);

            JOptionPane.showMessageDialog(
                this,
                "Không thể tạo mã nhân viên tự động. "
                    + "Vui lòng kiểm tra kết nối cơ sở dữ liệu.\n"
                    + ex.getMessage(),
                "Lỗi lấy mã nhân viên",
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
            String maNV = txtMaNV.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String chucVu = txtChucVu.getText().trim();
            String maTK = txtMaTK.getText().trim();

            if (!Regex_NhanVien.isValidMaNV(maNV)) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên không hợp lệ!");
                return;
            }
            if (!Regex_NhanVien.isValidHoTen(hoTen)) {
                JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ hoặc bị để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }

            LocalDate ngaySinh = dateNgaySinh.getDate();
            if (!Regex_NhanVien.isValidNgaySinh(ngaySinh)) {
                JOptionPane.showMessageDialog(this,
                    "Ngày sinh không được lớn hơn ngày hiện tại!",
                    "Ngày sinh không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return;
            }

            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthai_NhanVien tt = (trangthai_NhanVien) cboTrangThai.getSelectedItem();

            if (!Regex_NhanVien.isValidGioiTinh(gt == null ? null : gt.name())) {
                JOptionPane.showMessageDialog(this, "Giới tính không hợp lệ!");
                return;
            }
            if (!Regex_NhanVien.isValidSDT(sdt)) {
                JOptionPane.showMessageDialog(this,
                    "Số điện thoại phải bắt đầu bằng 0 và có 10 hoặc 11 chữ số!",
                    "Số điện thoại không hợp lệ", JOptionPane.WARNING_MESSAGE);
                txtSDT.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Email không đúng định dạng!");
                txtEmail.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidDiaChi(diaChi)) {
                JOptionPane.showMessageDialog(this, "Địa chỉ không hợp lệ!");
                txtDiaChi.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidChucVu(chucVu)) {
                JOptionPane.showMessageDialog(this, "Chức vụ không hợp lệ!");
                txtChucVu.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidTrangThai(tt == null ? null : tt.name())) {
                JOptionPane.showMessageDialog(this, "Trạng thái nhân viên không hợp lệ!");
                return;
            }
            if (!Regex_NhanVien.isValidMaTK(maTK)) {
                JOptionPane.showMessageDialog(this, "Mã tài khoản không hợp lệ!");
                txtMaTK.requestFocusInWindow();
                return;
            }

            NhanVien nv = new NhanVien(
                maNV, hoTen, ngaySinh, gt,
                sdt, email, diaChi, chucVu, tt, maTK
            );

            boolean ketQua = nvDAO.create(nv);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            themThanhCong();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể thêm nhân viên: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huyThem() {
        if (quayLai != null) quayLai.run();
    }

    private void themThanhCong() {
        if (quayLai != null) quayLai.run();
    }

    public void moframe() {
        JFrame frame = new JFrame("Thêm nhân viên");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ThemNhanVienPanel().moframe());
    }

    /**
     * Bộ chọn ngày tự tạo bằng Swing, không cần cài thêm thư viện ngoài.
     * Giá trị trả về là LocalDate để truyền thẳng vào Entity NhanVien.
     */
}
