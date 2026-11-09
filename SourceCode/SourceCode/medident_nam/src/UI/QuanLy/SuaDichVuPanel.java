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
import java.sql.SQLException;

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
public class SuaDichVuPanel extends JPanel implements ActionListener {

    private String maDV;
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
    private JButton btnLuu;
    private JButton btnHuy;

    public SuaDichVuPanel(String maDV, Runnable callback) {
        this.maDV = maDV;
        this.quayLai = callback;

        setLayout(new BorderLayout(0, 18));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        khoiTaoGiaoDien();
        taiThongTinDichVu();
    }

    private void khoiTaoGiaoDien() {
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 5));
        pnlHeader.setOpaque(false);

        JLabel lblTieuDe = new JLabel("Cập nhật dịch vụ");
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 25));
        lblTieuDe.setForeground(TEXT);

        JLabel lblMoTa = new JLabel(
            "Chỉnh sửa thông tin dịch vụ trong hệ thống"
        );
        lblMoTa.setFont(new Font("Arial", Font.PLAIN, 13));
        lblMoTa.setForeground(SUBTEXT);

        pnlHeader.add(lblTieuDe, BorderLayout.NORTH);
        pnlHeader.add(lblMoTa, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        txtMaDV = taoTextField();
        txtTenDV = taoTextField();
        txtDonGia = taoTextField();
        chiChoNhapDonGia(txtDonGia);

        txtMaDV.setEditable(false);
        txtMaDV.setBackground(new Color(241, 245, 249));

        txtMoTa = taoTextArea();

        cboTrangThai = new JComboBox<>(trangthai_dichvu.values());
        styleComboBox(cboTrangThai);

        JScrollPane scrollMoTa = taoScrollPane(txtMoTa);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(WHITE);
        pnlForm.setBorder(
            new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
            )
        );

        themTruong(pnlForm, "Mã dịch vụ", txtMaDV, 0, 0, 1);
        themTruong(pnlForm, "Tên dịch vụ *", txtTenDV, 1, 0, 1);
        themTruong(pnlForm, "Đơn giá (VNĐ) *", txtDonGia, 0, 1, 1);
        themTruong(pnlForm, "Trạng thái *", cboTrangThai, 1, 1, 1);
        themTruong(pnlForm, "Mô tả dịch vụ", scrollMoTa, 0, 2, 2);

        add(pnlForm, BorderLayout.CENTER);

        btnHuy = new JButton("Hủy");
        btnLuu = new JButton("Lưu thay đổi");

        styleButtonSecondary(btnHuy);
        styleButtonPrimary(btnLuu);

        btnHuy.addActionListener(this);
        btnLuu.addActionListener(this);

        JPanel pnlButton = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 12, 0)
        );
        pnlButton.setOpaque(false);
        pnlButton.add(btnHuy);
        pnlButton.add(btnLuu);

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

    private void styleComboBox(JComboBox<?> cbo) {
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
        btn.setBorder(
            new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(11, 20, 11, 20)
            )
        );
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Lọc dữ liệu nhập tạm thời; biểu thức cho đơn giá nằm trong Regex_DichVu.
    private void chiChoNhapDonGia(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean hopLe(String text) {
                return Regex_DichVu.isValidDonGiaInput(text);
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

    private BigDecimal docDonGia() {
        String gia = txtDonGia.getText().trim().replace(",", ".");

        if (!Regex_DichVu.isValidDonGia(gia)) {
            throw new IllegalArgumentException(
                "Đơn giá phải là số không âm, tối đa 16 chữ số nguyên "
                    + "và 2 chữ số thập phân!"
            );
        }

        return new BigDecimal(gia);
    }

    // Lấy thông tin dịch vụ theo mã
    private void taiThongTinDichVu() {
        try {
            DichVu dv = dvDAO.getDichVuByMaDV(maDV);

            if (dv == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Không tìm thấy dịch vụ: " + maDV,
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE
                );
                btnLuu.setEnabled(false);
                return;
            }

            txtMaDV.setText(dv.getMaDV());
            txtTenDV.setText(dv.getTenDV());
            txtMoTa.setText(dv.getMota());

            txtDonGia.setText(
                dv.getDongia() == null
                    ? ""
                    : dv.getDongia().toPlainString()
            );

            cboTrangThai.setSelectedItem(dv.getTrangthai());

        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Lỗi lấy thông tin dịch vụ: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
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

    // Cập nhật dịch vụ
    private void xuLySua() {
        try {
            String tenDV = txtTenDV.getText().trim();
            String mota = txtMoTa.getText().trim();
            trangthai_dichvu tt = (trangthai_dichvu) cboTrangThai.getSelectedItem();

            if (!Regex_DichVu.isValidMaDV(maDV)) {
                JOptionPane.showMessageDialog(this, "Mã dịch vụ không hợp lệ!");
                return;
            }
            if (!Regex_DichVu.isValidTenDV(tenDV)) {
                JOptionPane.showMessageDialog(this, "Tên dịch vụ không hợp lệ hoặc bị để trống!");
                txtTenDV.requestFocusInWindow();
                return;
            }
            if (!Regex_DichVu.isValidMoTa(mota)) {
                JOptionPane.showMessageDialog(this, "Mô tả dịch vụ không hợp lệ!");
                txtMoTa.requestFocusInWindow();
                return;
            }

            BigDecimal donGia = docDonGia();

            if (!Regex_DichVu.isValidTrangThai(tt == null ? null : tt.name())) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái dịch vụ hợp lệ!");
                return;
            }

            DichVu dv = new DichVu(maDV, tenDV, mota, donGia, tt);
            boolean ketQua = dvDAO.update(dv);

            if (!ketQua) {
                JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thất bại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Cập nhật dịch vụ thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            suaThanhCong();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Không thể cập nhật dịch vụ: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void huySua() {
        if (quayLai != null) {
            quayLai.run();
        }
    }

    private void suaThanhCong() {
        if (quayLai != null) {
            quayLai.run();
        }
    }

    public void moframe() {
        JFrame frame = new JFrame("Sửa dịch vụ");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1050, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SuaDichVuPanel("DV001", null).moframe();
        });
    }
}
