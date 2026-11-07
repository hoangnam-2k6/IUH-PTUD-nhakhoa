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

import DAO.NhanVienDAO;
import Entity.NhanVien;
import enums.gioitinh;
import enums.trangthai_NhanVien;

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

    private JTextField txtMaNV, txtHoTen, txtNgaySinh, txtSDT, txtEmail, txtChucVu, txtMaTK;
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
        txtNgaySinh = taoTextField();
        txtSDT = taoTextField();
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
        themTruong(form, "Ngày sinh (yyyy-MM-dd)", txtNgaySinh, 0, 1);
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
            txtNgaySinh.setText(nv.getNgaysinh() == null ? "" : nv.getNgaysinh().toString());
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
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được để trống!");
                txtHoTen.requestFocusInWindow();
                return;
            }

            String ngay = txtNgaySinh.getText().trim();
            LocalDate ngaySinh = ngay.isEmpty() ? null : LocalDate.parse(ngay);
            gioitinh gt = (gioitinh) cboGioiTinh.getSelectedItem();
            trangthai_NhanVien tt = (trangthai_NhanVien) cboTrangThai.getSelectedItem();

            NhanVien nv = new NhanVien(
                maNV, hoTen, ngaySinh, gt,
                txtSDT.getText().trim(), txtEmail.getText().trim(),
                txtDiaChi.getText().trim(), txtChucVu.getText().trim(),
                tt, txtMaTK.getText().trim()
            );

            boolean ketQua = nvDAO.update(nv);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
            suaThanhCong();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Ngày sinh không hợp lệ! Định dạng yyyy-MM-dd, ví dụ 2003-05-20");
            txtNgaySinh.requestFocusInWindow();
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
}
