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
    private JTextField txtNgaySinh;
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
        txtMaBN.setText("Tự động cấp");
        txtMaBN.setEditable(false);
        txtHoTen = taoTextField();
        txtNgaySinh = taoTextField();
        txtSDT = taoTextField();
        txtEmail = taoTextField();
        txtDiaChi = taoTextArea();
        txtTienSuBenh = taoTextArea();

        cboGioiTinh = new JComboBox<>(gioitinh.values());
        cboTrangThai = new JComboBox<>(trangthaidieutri.values());
        cboGioiTinh.setSelectedIndex(-1);
        cboTrangThai.setSelectedIndex(0);
        styleComboBox(cboGioiTinh);
        styleComboBox(cboTrangThai);

        JPanel pnlForm = taoPanelForm();
        themTruong(pnlForm, "Mã bệnh nhân (tự động)", txtMaBN, 0, 0, 1);
        themTruong(pnlForm, "Họ và tên *", txtHoTen, 1, 0, 1);
        themTruong(pnlForm, "Ngày sinh (yyyy-MM-dd)", txtNgaySinh, 0, 1, 1);
        themTruong(pnlForm, "Giới tính", cboGioiTinh, 1, 1, 1);
        themTruong(pnlForm, "Số điện thoại", txtSDT, 0, 2, 1);
        themTruong(pnlForm, "Email", txtEmail, 1, 2, 1);
        themTruong(pnlForm, "Địa chỉ", taoScrollPane(txtDiaChi), 0, 3, 1);
        themTruong(pnlForm, "Tiền sử bệnh", taoScrollPane(txtTienSuBenh), 1, 3, 1);
        themTruong(pnlForm, "Trạng thái điều trị", cboTrangThai, 0, 4, 2);
        add(pnlForm, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnThem = new JButton("Thêm bệnh nhân");
        styleButtonSecondary(btnHuy);
        styleButtonPrimary(btnThem);
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

    private void styleComboBox(JComboBox<?> cbo) {
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

    private void styleButtonPrimary(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(PRIMARY);
        btn.setForeground(WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleButtonSecondary(JButton btn) {
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
            String maBN = "";
            String hoTen = txtHoTen.getText().trim();
            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Mã bệnh nhân và họ tên không được để trống!");
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

            boolean ketQua = bnDAO.create(bn);
            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Thêm bệnh nhân thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm bệnh nhân thành công!");
            themThanhCong();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Ngày sinh không hợp lệ! Định dạng: yyyy-MM-dd (ví dụ 2003-05-20)");
            txtNgaySinh.requestFocusInWindow();
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

    public void moframe() {
        JFrame frame = new JFrame("Thêm bệnh nhân");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 780);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ThemBenhNhanPanel().moframe());
    }
}
