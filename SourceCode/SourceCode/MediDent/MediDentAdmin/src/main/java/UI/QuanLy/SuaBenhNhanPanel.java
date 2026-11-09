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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import DAO.BenhNhanDAO;
import Entity.BenhNhan;
import enums.gioitinh;
import enums.trangthaidieutri;

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
        txtEmail = taoTextField();
        txtDiaChi = taoTextArea();
        txtTienSuBenh = taoTextArea();

        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthaidieutri.values());
        styleCombo(cboGioiTinh);
        styleCombo(cboTrangThai);

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

    private void styleButton(JButton b, boolean primary) {
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

    private void taiThongTinBenhNhan() {
        try {
            BenhNhan bn = bnDAO.getBenhNhanByMaBN(maBN);
            if (bn == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân: " + maBN,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
                btnLuu.setEnabled(false);
                return;
            }
            txtMaBN.setText(bn.getMaBN());
            txtHoTen.setText(bn.getHoten());
            txtNgaySinh.setText(bn.getNgaysinh() == null ? "" : bn.getNgaysinh().toString());
            cboGioiTinh.setSelectedItem(bn.getGioitinh());
            txtSDT.setText(bn.getSdt());
            txtEmail.setText(bn.getEmail());
            txtDiaChi.setText(bn.getDiachi());
            txtTienSuBenh.setText(bn.getTiensubenh());
            cboTrangThai.setSelectedItem(bn.getTrangthai());
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
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }

            String ngay = txtNgaySinh.getText().trim();
            LocalDate ngaySinh = ngay.isEmpty() ? null : LocalDate.parse(ngay);
            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthaidieutri tt = (trangthaidieutri) cboTrangThai.getSelectedItem();

            BenhNhan bn = new BenhNhan(
                maBN, hoTen, ngaySinh, gt,
                txtSDT.getText().trim(), txtEmail.getText().trim(),
                txtDiaChi.getText().trim(), txtTienSuBenh.getText().trim(), tt
            );

            boolean ketQua = bnDAO.update(bn);
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

    public void moframe() {
        JFrame frame = new JFrame("Sửa bệnh nhân");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SuaBenhNhanPanel("BN001", null).moframe());
    }
}
