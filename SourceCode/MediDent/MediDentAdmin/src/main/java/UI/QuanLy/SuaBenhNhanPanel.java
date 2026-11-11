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
import java.time.format.DateTimeParseException;

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
public class SuaBenhNhanPanel extends JPanel implements ActionListener {

    private final String maBN;
    private final Runnable quayLai;
    private final BenhNhanDAO bnDAO = new BenhNhanDAO();

    private final Color BACKGROUND = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color PRIMARY = new Color(37, 99, 235);
    private final Color TEXT = new Color(30, 41, 59);
    private final Color SUBTEXT = new Color(100, 116, 139);
    private final Color BORDER = new Color(220, 227, 236);

    private JTextField txtMaBN;
    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JComboBox<gioitinh> cboGioiTinh;
    private JTextField txtSDT;
    private JTextField txtEmail;
    private JTextArea txtDiaChi;
    private JTextArea txtTienSuBenh;
    private JComboBox<trangthaidieutri> cboTrangThai;
    private JButton btnLuu;
    private JButton btnHuy;

    public SuaBenhNhanPanel(String maBN, Runnable callback) {
        this.maBN = maBN;
        this.quayLai = callback;
        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));
        khoiTaoGiaoDien();
        taiThongTinBenhNhan();
    }

    private void khoiTaoGiaoDien() {
        JPanel header = new JPanel(new BorderLayout(0, 5));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật bệnh nhân");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(TEXT);
        JLabel desc = new JLabel("Chỉnh sửa thông tin bệnh nhân trong hệ thống");
        desc.setFont(new Font("Arial", Font.PLAIN, 13));
        desc.setForeground(SUBTEXT);
        header.add(title, BorderLayout.NORTH);
        header.add(desc, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        txtMaBN = taoTextField();
        txtMaBN.setEditable(false);
        txtMaBN.setBackground(new Color(241, 245, 249));
        txtHoTen = taoTextField();
        txtNgaySinh = taoTextField();
        txtSDT = taoTextField();
        chiChoNhapSo(txtSDT);
        txtEmail = taoTextField();
        txtDiaChi = taoTextArea();
        txtTienSuBenh = taoTextArea();

        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthaidieutri.values());
        dinhDangHopChon(cboGioiTinh);
        dinhDangHopChon(cboTrangThai);

        JPanel form = taoPanelForm();
        themTruong(form, "Mã bệnh nhân", txtMaBN, 0, 0, 1);
        themTruong(form, "Họ và tên *", txtHoTen, 1, 0, 1);
        themTruong(form, "Ngày sinh (yyyy-MM-dd)", txtNgaySinh, 0, 1, 1);
        themTruong(form, "Giới tính", cboGioiTinh, 1, 1, 1);
        themTruong(form, "Số điện thoại", txtSDT, 0, 2, 1);
        themTruong(form, "Email", txtEmail, 1, 2, 1);
        themTruong(form, "Địa chỉ", taoScroll(txtDiaChi), 0, 3, 1);
        themTruong(form, "Tiền sử bệnh", taoScroll(txtTienSuBenh), 1, 3, 1);
        themTruong(form, "Trạng thái điều trị", cboTrangThai, 0, 4, 2);
        add(form, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnLuu = new JButton("Lưu thay đổi");
        dinhDangButton(btnHuy, false);
        dinhDangButton(btnLuu, true);
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

    private void dinhDangHopChon(JComboBox<?> c) {
        c.setFont(new Font("Arial", Font.PLAIN, 14));
        c.setBackground(WHITE);
        c.setForeground(TEXT);
        c.setPreferredSize(new Dimension(250, 40));
        c.setBorder(new LineBorder(BORDER, 1, true));
    }

    private void themTruong(JPanel panel, String ten, JComponent component,
                            int cot, int hang, int soCot) {
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
        gbc.gridwidth = soCot;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 12, 10, 12);
        panel.add(field, gbc);
    }

    private void dinhDangButton(JButton b, boolean primary) {
        b.setFont(new Font("Arial", Font.BOLD, 14));
        b.setBackground(primary ? PRIMARY : WHITE);
        b.setForeground(primary ? WHITE : TEXT);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setBorder(primary ? new EmptyBorder(12, 20, 12, 20)
            : new CompoundBorder(new LineBorder(BORDER, 1, true),
                new EmptyBorder(11, 20, 11, 20)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Lọc ký tự nhập vào; quy tắc chữ số được lấy từ validation.Regex_BenhNhan.
    private void chiChoNhapSo(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean hopLe(String text) {
                return Regex_BenhNhan.laSDTDangNhapHopLe(text);
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

    private void taiThongTinBenhNhan() {
        try {
            BenhNhan bn = bnDAO.layBenhNhanByMaBN(maBN);
            if (bn == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân: " + maBN,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                btnLuu.setEnabled(false);
                return;
            }
            txtMaBN.setText(bn.layMaBN());
            txtHoTen.setText(bn.layHoten());
            txtNgaySinh.setText(bn.layNgaysinh() == null ? "" : bn.layNgaysinh().toString());
            cboGioiTinh.setSelectedItem(bn.layGioitinh());
            txtSDT.setText(bn.laySdt());
            txtEmail.setText(bn.layEmail());
            txtDiaChi.setText(bn.layDiachi());
            txtTienSuBenh.setText(bn.layTiensubenh());
            cboTrangThai.setSelectedItem(bn.layTrangthai());
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi lấy thông tin bệnh nhân: " + ex.getMessage(),
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
            String ngay = txtNgaySinh.getText().trim();
            LocalDate ngaySinh = ngay.isEmpty() ? null : LocalDate.parse(ngay);
            String sdt = txtSDT.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String tienSuBenh = txtTienSuBenh.getText().trim();

            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthaidieutri tt = (trangthaidieutri) cboTrangThai.getSelectedItem();

            if (!Regex_BenhNhan.laMaBNHopLe(maBN)) {
                JOptionPane.showMessageDialog(this, "Mã bệnh nhân không hợp lệ!");
                return;
            }
            if (!Regex_BenhNhan.laHoTenHopLe(hoTen)) {
                JOptionPane.showMessageDialog(this, "Họ tên không hợp lệ hoặc bị để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laNgaySinhHopLe(ngaySinh)) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không được lớn hơn ngày hiện tại!");
                txtNgaySinh.requestFocusInWindow();
                return;
            }
            if (!Regex_BenhNhan.laGioiTinhHopLe(gt == null ? null : gt.name())) {
                JOptionPane.showMessageDialog(this, "Giới tính không hợp lệ!");
                return;
            }
            if (!Regex_BenhNhan.laSDTHopLe(sdt)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không đúng định dạng!");
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

            boolean ketQua = bnDAO.capNhat(bn);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Cập nhật bệnh nhân thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Cập nhật bệnh nhân thành công!");
            suaThanhCong();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Ngày sinh không hợp lệ! Định dạng yyyy-MM-dd, ví dụ 2003-05-20");
            txtNgaySinh.requestFocusInWindow();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể cập nhật bệnh nhân: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huySua() {
        if (quayLai != null) quayLai.run();
    }

    private void suaThanhCong() {
        if (quayLai != null) quayLai.run();
    }

    public void moKhung() {
        JFrame frame = new JFrame("Sửa bệnh nhân");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SuaBenhNhanPanel("BN001", null).moKhung());
    }
}
