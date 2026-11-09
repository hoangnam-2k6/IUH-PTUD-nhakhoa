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
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import DAO.NhanVienDAO;
import Entity.NhanVien;
import enums.gioitinh;
import enums.trangthai_NhanVien;
import util.BangChonLich;
import validation.Regex_NhanVien;

@SuppressWarnings("serial")
public class SuaNhanVienPanel extends JPanel implements ActionListener {

    private final String maNV;
    private final Runnable quayLai;
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
    private JButton btnLuu, btnHuy;

    public SuaNhanVienPanel(String maNV, Runnable callback) {
        this.maNV = maNV;
        this.quayLai = callback;
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        khoiTaoGiaoDien();
        taiThongTinNhanVien();
    }

    private void khoiTaoGiaoDien() {
        JPanel header = new JPanel(new BorderLayout(0, 5));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật nhân viên");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel desc = new JLabel("Chỉnh sửa thông tin nhân viên trong hệ thống");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(SUBTEXT);
        header.add(title, BorderLayout.NORTH);
        header.add(desc, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        txtMaNV = taoTextField();
        txtMaNV.setEditable(false);
        txtMaNV.setBackground(new Color(241, 245, 249));
        txtHoTen = taoTextField();
        dateNgaySinh = new BangChonLich();
        txtSDT = taoTextField();
        chiChoNhapSo(txtSDT);
        txtEmail = taoTextField();
        txtChucVu = taoTextField();
        txtMaTK = taoTextField();
        txtDiaChi = taoTextArea();
        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthai_NhanVien.values());
        styleCombo(cboGioiTinh);
        styleCombo(cboTrangThai);

        JPanel form = taoPanelForm();
        themTruong(form, "Mã nhân viên", txtMaNV, 0, 0);
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
        btnLuu = new JButton("Lưu thay đổi");
        styleButton(btnHuy, false);
        styleButton(btnLuu, true);
        btnHuy.addActionListener(this);
        btnLuu.addActionListener(this);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);
        buttons.add(btnHuy);
        buttons.add(btnLuu);
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

    // Lọc ký tự nhập vào; quy tắc chữ số được lấy từ validation.Regex_NhanVien.
    private void chiChoNhapSo(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean hopLe(String text) {
                return Regex_NhanVien.isValidSDTInput(text);
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text,
                    AttributeSet attr) throws BadLocationException {
                if (text == null) return;
                String old = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = old.substring(0, offset) + text + old.substring(offset);
                if (hopLe(next)) super.insertString(fb, offset, text, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                    AttributeSet attrs) throws BadLocationException {
                String insert = text == null ? "" : text;
                String old = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = old.substring(0, offset) + insert + old.substring(offset + length);
                if (hopLe(next)) super.replace(fb, offset, length, insert, attrs);
            }
        });
    }

    private void taiThongTinNhanVien() {
        try {
            NhanVien nv = nvDAO.getNhanVienByMaNV(maNV);
            if (nv == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên có mã: " + maNV,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                btnLuu.setEnabled(false);
                return;
            }
            txtMaNV.setText(nv.getMaNV());
            txtHoTen.setText(nv.getHoten());
            dateNgaySinh.setDate(nv.getNgaysinh());
            cboGioiTinh.setSelectedItem(nv.getGioitinh());
            txtSDT.setText(nv.getSdt());
            txtEmail.setText(nv.getEmail());
            txtDiaChi.setText(nv.getDiachi());
            txtChucVu.setText(nv.getChucvu());
            cboTrangThai.setSelectedItem(nv.getTrangthai());
            txtMaTK.setText(nv.getMaTK());
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi lấy thông tin nhân viên: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            btnLuu.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnHuy) {
            huySua();
        } else if (e.getSource() == btnLuu) {
            xuLySua();
        }
    }

    private void xuLySua() {
        try {
            String hoTen = txtHoTen.getText().trim();
            LocalDate ngaySinh = dateNgaySinh.getDate();
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String chucVu = txtChucVu.getText().trim();
            String maTK = txtMaTK.getText().trim();

            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthai_NhanVien tt = (trangthai_NhanVien) cboTrangThai.getSelectedItem();

            if (!Regex_NhanVien.isValidMaNV(maNV)) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên không hợp lệ!");
                return;
            }
            if (!Regex_NhanVien.isValidHoTen(hoTen)) {
                JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ hoặc bị để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidNgaySinh(ngaySinh)) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không được lớn hơn ngày hiện tại!");
                dateNgaySinh.requestFocusInWindow();
                return;
            }
            if (!Regex_NhanVien.isValidGioiTinh(gt == null ? null : gt.name())) {
                JOptionPane.showMessageDialog(this, "Giới tính không hợp lệ!");
                return;
            }
            if (!Regex_NhanVien.isValidSDT(sdt)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không đúng định dạng!");
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
                maNV, hoTen, ngaySinh, gt, sdt, email,
                diaChi, chucVu, tt, maTK
            );

            boolean ketQua = nvDAO.update(nv);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
            suaThanhCong();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể cập nhật nhân viên: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huySua() {
        if (quayLai != null) quayLai.run();
    }

    private void suaThanhCong() {
        if (quayLai != null) quayLai.run();
    }

    public void moframe() {
        JFrame frame = new JFrame("Sửa nhân viên");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SuaNhanVienPanel("NV001", null).moframe());
    }

    // Bộ chọn ngày sinh dùng giống form Thêm nhân viên.
}
