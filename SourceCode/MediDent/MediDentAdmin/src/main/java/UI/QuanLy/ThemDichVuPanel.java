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
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import DAO.DichVuDAO;
import Entity.DichVu;
import enums.trangthai_dichvu;
import validation.Regex_DichVu;

@SuppressWarnings("serial")
public class ThemDichVuPanel extends JPanel implements ActionListener {

    private Runnable quayLai;
    private final DichVuDAO dvDAO = new DichVuDAO();

    // Màu giao diện
    private final Color BACKGROUND = new Color(244, 247, 251);
    private final Color WHITE = Color.WHITE;
    private final Color PRIMARY = new Color(37, 99, 235);
    private final Color TEXT = new Color(30, 41, 59);
    private final Color SUBTEXT = new Color(100, 116, 139);
    private final Color BORDER = new Color(220, 227, 236);

    // Các ô nhập liệu
    private JTextField txtMaDV;
    private JTextField txtTenDV;
    private JTextField txtDonGia;
    private JTextArea txtMoTa;
    private JComboBox<trangthai_dichvu> cboTrangThai;

    // Nút chức năng
    private JButton btnThem;
    private JButton btnHuy;

    public ThemDichVuPanel() {
        this(null);
    }

    public ThemDichVuPanel(Runnable callback) {
        this.quayLai = callback;

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        khoiTaoGiaoDien();
        taoMaDichVuTuDong();
    }

    private void khoiTaoGiaoDien() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Thêm dịch vụ");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 25));
        lblTieuDe.setForeground(TEXT);

        JLabel lblMoTa = new JLabel(
            "Nhập thông tin dịch vụ mới vào hệ thống"
        );
        lblMoTa.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMoTa.setForeground(SUBTEXT);

        pnlHeader.add(lblTieuDe, BorderLayout.NORTH);
        pnlHeader.add(lblMoTa, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        txtMaDV = taoTextField();
        txtMaDV.setEditable(false);
        txtMaDV.setFocusable(false);
        txtMaDV.setBackground(new Color(241, 245, 249));
        txtMaDV.setForeground(SUBTEXT);
        txtMaDV.setToolTipText("Mã dịch vụ được tạo tự động.");

        txtTenDV = taoTextField();
        txtDonGia = taoTextField();
        chiChoNhapDonGia(txtDonGia);
        txtDonGia.setToolTipText(
            "Chỉ nhập chữ số; có thể dùng dấu chấm hoặc dấu phẩy cho phần thập phân."
        );
        txtMoTa = taoTextArea();

        cboTrangThai = new JComboBox<>(trangthai_dichvu.values());
        cboTrangThai.setSelectedIndex(-1);
        dinhDangHopChon(cboTrangThai);

        JScrollPane scrollMoTa = taoScrollPane(txtMoTa);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(WHITE);
        pnlForm.setBorder(
            new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
            )
        );

        themTruong(pnlForm, "Mã dịch vụ *", txtMaDV, 0, 0, 1);
        themTruong(pnlForm, "Tên dịch vụ *", txtTenDV, 1, 0, 1);
        themTruong(pnlForm, "Đơn giá (VNĐ) *", txtDonGia, 0, 1, 1);
        themTruong(pnlForm, "Trạng thái *", cboTrangThai, 1, 1, 1);
        themTruong(pnlForm, "Mô tả dịch vụ", scrollMoTa, 0, 2, 2);

        add(pnlForm, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnThem = new JButton("Thêm dịch vụ");

        dinhDangButtonSecondary(btnHuy);
        dinhDangButtonPrimary(btnThem);

        btnHuy.addActionListener(this);
        btnThem.addActionListener(this);

        JPanel pnlButton = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 12, 0)
        );
        pnlButton.setOpaque(false);
        pnlButton.add(btnHuy);
        pnlButton.add(btnThem);

        add(pnlButton, BorderLayout.SOUTH);
    }

    private JTextField taoTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setBackground(WHITE);
        txt.setForeground(TEXT);
        txt.setCaretColor(PRIMARY);
        txt.setPreferredSize(new Dimension(250, 40));
        txt.setBorder(
            new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
            )
        );
        return txt;
    }

    private JTextArea taoTextArea() {
        JTextArea txt = new JTextArea(3, 20);
        txt.setFont(new Font("Arial", Font.PLAIN, 14));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setBackground(WHITE);
        txt.setBorder(new EmptyBorder(8, 10, 8, 10));
        return txt;
    }

    private JScrollPane taoScrollPane(JTextArea txt) {
        JScrollPane scroll = new JScrollPane(txt);
        scroll.setPreferredSize(new Dimension(250, 100));
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

    private void themTruong(
            JPanel panel,
            String ten,
            JComponent component,
            int cot,
            int hang,
            int soCot) {

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
        btn.setBorder(
            new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(11, 20, 11, 20)
            )
        );
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Chỉ cho phép nhập số vào ô đơn giá.
    // Hỗ trợ tối đa 16 chữ số phần nguyên và 2 chữ số phần thập phân.
    // Dấu chấm hoặc dấu phẩy được dùng làm dấu thập phân.
    private void chiChoNhapDonGia(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(
            new DocumentFilter() {
                private boolean laDonGiaDangNhapHopLe(String text) {
                    return Regex_DichVu.laDonGiaDangNhapHopLe(text);
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
                    if (laDonGiaDangNhapHopLe(moi)) {
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
                    if (laDonGiaDangNhapHopLe(moi)) {
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

    private BigDecimal docDonGia() {
        String gia = txtDonGia.getText().trim().replace(",", ".");

        if (!Regex_DichVu.laDonGiaHopLe(gia)) {
            throw new IllegalArgumentException(
                "Đơn giá phải là số không âm, tối đa 16 chữ số nguyên "
                    + "và 2 chữ số thập phân!"
            );
        }

        return new BigDecimal(gia);
    }

    // Tự động lấy mã dịch vụ lớn nhất trong database và tăng lên 1.
    private void taoMaDichVuTuDong() {
        try {
            String maMoi = dvDAO.layMaDichVuTiepTheo();
            txtMaDV.setText(maMoi);
        } catch (Exception ex) {
            txtMaDV.setText("");
            btnThem.setEnabled(false);

            JOptionPane.showMessageDialog(
                this,
                "Không thể tạo mã dịch vụ tự động. "
                    + "Vui lòng kiểm tra kết nối cơ sở dữ liệu.\n"
                    + ex.getMessage(),
                "Lỗi lấy mã dịch vụ",
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

    // Thêm dịch vụ vào database
    private void xuLyThem() {
        try {
            String maDV = txtMaDV.getText().trim();
            String tenDV = txtTenDV.getText().trim();
            String mota = txtMoTa.getText().trim();
            trangthai_dichvu tt = (trangthai_dichvu) cboTrangThai.getSelectedItem();

            if (!Regex_DichVu.laMaDVHopLe(maDV)) {
                JOptionPane.showMessageDialog(this, "Mã dịch vụ không hợp lệ!");
                return;
            }
            if (!Regex_DichVu.laTenDVHopLe(tenDV)) {
                JOptionPane.showMessageDialog(this, "Tên dịch vụ không hợp lệ hoặc bị để trống!");
                txtTenDV.requestFocusInWindow();
                return;
            }
            if (!Regex_DichVu.laMoTaHopLe(mota)) {
                JOptionPane.showMessageDialog(this, "Mô tả dịch vụ không hợp lệ!");
                txtMoTa.requestFocusInWindow();
                return;
            }

            BigDecimal donGia = docDonGia();

            if (!Regex_DichVu.laTrangThaiHopLe(tt == null ? null : tt.name())) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái dịch vụ hợp lệ!");
                return;
            }

            DichVu dv = new DichVu(maDV, tenDV, mota, donGia, tt);
            boolean ketQua = dvDAO.themMoi(dv);

            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            themThanhCong();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể thêm dịch vụ: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huyThem() {
        if (quayLai != null) {
            quayLai.run();
        }
    }

    private void themThanhCong() {
        if (quayLai != null) {
            quayLai.run();
        }
    }

    public void moKhung() {
        JFrame frame = new JFrame("Thêm dịch vụ");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ThemDichVuPanel().moKhung();
        });
    }
}
