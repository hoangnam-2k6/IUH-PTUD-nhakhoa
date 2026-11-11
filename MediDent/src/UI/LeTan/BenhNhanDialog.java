package UI.LeTan;

import Dao.BenhNhanDao;
import Entity.BenhNhan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.text.JTextComponent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import static UI.LeTan.GiaoDien.*;

/** Hộp thoại thêm mới / chỉnh sửa hồ sơ bệnh nhân đầy đủ thông tin. */
public class BenhNhanDialog extends JDialog {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final BenhNhanDao dao = new BenhNhanDao();
    private final BenhNhan bn;          // đang sửa (maBN > 0) hoặc hồ sơ mới
    private boolean daLuu = false;

    private final JTextField oTen = taoOText();
    private final ONgay oNgaySinh = new ONgay();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});
    private final JTextField oSdt = taoOText();
    private final JTextField oCccd = taoOText();
    private final JTextField oEmail = taoOText();
    private final JTextField oDiaChi = taoOText();
    private final JTextField oTenLienHe = taoOText();
    private final JTextField oSdtLienHe = taoOText();
    private final JTextArea oGhiChu = taoOVanBan(8);

    private BenhNhanDialog(Component cha, BenhNhan bn) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha), bn.getMaBN() > 0
                ? String.format("Sửa hồ sơ BN%03d", bn.getMaBN()) : "Thêm bệnh nhân mới", ModalityType.APPLICATION_MODAL);
        this.bn = bn;
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        cbGioiTinh.setFont(font(Font.PLAIN, 14));
        oTen.setToolTipText("Bắt buộc - chỉ nhập chữ, không nhập số");
        chiNhapChu(oTen);
        chiNhapChu(oTenLienHe);
        oNgaySinh.oNhap().setToolTipText("Bắt buộc - chọn trên lịch hoặc nhập dd/MM/yyyy");
        oSdt.setToolTipText("Bắt buộc - số điện thoại 10 hoặc 11 chữ số");
        oGhiChu.setToolTipText("Ghi chú y tế, dị ứng thuốc, tiền sử bệnh và thông tin cần lưu ý...");

        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoHangNut(), BorderLayout.SOUTH);
        napDuLieu();
        caiDatHieuUngFocus();

        setSize(780, 850);
        setMinimumSize(new Dimension(680, 600));
        setLocationRelativeTo(cha);
    }

    /** Mở hộp thoại. {@code bnCu == null}: thêm mới. Trả về true nếu đã lưu thành công. */
    public static boolean hienThi(Component cha, BenhNhan bnCu) {
        BenhNhanDialog d = new BenhNhanDialog(cha, bnCu == null ? new BenhNhan() : bnCu);
        d.setVisible(true);
        return d.daLuu;
    }

    /** Viền chuyển sang màu chủ đạo khi người dùng đang nhập/chọn trường. */
    private void caiDatHieuUngFocus() {
        ganFocus(oTen, oTen);
        ganFocus(oNgaySinh.oNhap(), oNgaySinh.oNhap());
        ganFocus(cbGioiTinh, cbGioiTinh);
        ganFocus(oSdt, oSdt);
        ganFocus(oCccd, oCccd);
        ganFocus(oEmail, oEmail);
        ganFocus(oDiaChi, oDiaChi);
        ganFocus(oTenLienHe, oTenLienHe);
        ganFocus(oSdtLienHe, oSdtLienHe);
        JScrollPane khungGhiChu = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, oGhiChu);
        if (khungGhiChu != null) ganFocus(oGhiChu, khungGhiChu);
    }

    private void ganFocus(JComponent oNhap, JComponent khung) {
        Color vienMacDinh = VIEN;
        FocusAdapter xuLy = new FocusAdapter() {
            private void capNhat(boolean dangFocus) {
                Color mauVien = dangFocus ? MAU_CHINH : vienMacDinh;
                if (khung instanceof JTextField) {
                    khung.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(mauVien, dangFocus ? 2 : 1),
                            BorderFactory.createEmptyBorder(7, 10, 7, 10)));
                } else if (khung instanceof JTextArea) {
                    khung.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                    JComponent cha = (JComponent) khung.getParent().getParent();
                    cha.setBorder(BorderFactory.createLineBorder(mauVien, dangFocus ? 2 : 1));
                } else {
                    khung.setBorder(BorderFactory.createLineBorder(mauVien, dangFocus ? 2 : 1));
                }
            }
            @Override public void focusGained(FocusEvent e) { capNhat(true); }
            @Override public void focusLost(FocusEvent e) { capNhat(false); }
        };
        oNhap.addFocusListener(xuLy);
        if (oNhap instanceof JComboBox<?>) {
            ((JComboBox<?>) oNhap).addFocusListener(xuLy);
        }
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_APP);
        noi.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));

        JPanel caNhan = new JPanel(new GridLayout(0, 1, 0, 8));
        caNhan.setOpaque(false);
        caNhan.add(hai(truong("Họ và tên *", oTen), truong("Ngày sinh * (dd/MM/yyyy)", oNgaySinh)));
        caNhan.add(hai(truong("Giới tính", cbGioiTinh), truong("CMND/CCCD", oCccd)));
        noi.add(taoThe("Thông tin cá nhân", "Thông tin cơ bản của bệnh nhân", caNhan));
        noi.add(Box.createVerticalStrut(12));

        JPanel lienHe = new JPanel(new GridLayout(0, 1, 0, 8));
        lienHe.setOpaque(false);
        lienHe.add(hai(truong("Số điện thoại *", oSdt), truong("Email", oEmail)));
        lienHe.add(truong("Địa chỉ", oDiaChi));
        JLabel thongTinLienHe = new JLabel("THÔNG TIN NGƯỜI LIÊN HỆ");
        thongTinLienHe.setFont(font(Font.BOLD, 12));
        thongTinLienHe.setForeground(new Color(22, 163, 74));
        thongTinLienHe.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        lienHe.add(thongTinLienHe);
        lienHe.add(hai(truong("Người liên hệ khẩn cấp", oTenLienHe), truong("SĐT người liên hệ", oSdtLienHe)));
        noi.add(taoThe("Thông tin liên hệ", "Thông tin để phòng khám liên lạc với bệnh nhân", lienHe));
        noi.add(Box.createVerticalStrut(12));

        JPanel yTe = new JPanel(new BorderLayout());
        yTe.setOpaque(false);
        yTe.add(truong("Ghi chú", boc(oGhiChu)), BorderLayout.CENTER);
        noi.add(taoThe("Ghi chú", "Dị ứng thuốc, tiền sử bệnh và các lưu ý liên quan", yTe));

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(18);
        return cuon;
    }

    private JPanel taoThe(String tieuDe, String moTa, JComponent noiDung) {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(14, 16, 10, 16)));
        JLabel tieuDeLabel = new JLabel(tieuDe);
        tieuDeLabel.setFont(font(Font.BOLD, 16));
        tieuDeLabel.setForeground(MAU_CHINH);
        JLabel moTaLabel = new JLabel(moTa);
        moTaLabel.setFont(font(Font.PLAIN, 12));
        moTaLabel.setForeground(CHU_PHU);
        JPanel dau = new JPanel(new GridLayout(0, 1, 0, 3));
        dau.setOpaque(false);
        dau.add(tieuDeLabel);
        dau.add(moTaLabel);
        the.add(dau, BorderLayout.NORTH);
        the.add(noiDung, BorderLayout.CENTER);
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        return the;
    }

    private JPanel hai(JComponent trai, JComponent phai) {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(trai);
        p.add(phai);
        return p;
    }

    private JPanel taoHangNut() {
        JButton huy = taoNutTrang("Hủy");
        JButton luu = taoNut("Lưu hồ sơ", MAU_CHINH, MAU_CHINH_DAM);
        huy.addActionListener(e -> dispose());
        luu.addActionListener(e -> luu());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(luu);
        getRootPane().setDefaultButton(luu);
        return p;
    }

    private void napDuLieu() {
        oTen.setText(nz(bn.getHoTen()));
        oNgaySinh.datNgay(bn.getNgaySinh());
        if (bn.getGioiTinh() != null) cbGioiTinh.setSelectedItem(bn.getGioiTinh());
        oSdt.setText(nz(bn.getSoDienThoai()));
        oCccd.setText(nz(bn.getCccd()));
        oEmail.setText(nz(bn.getEmail()));
        oDiaChi.setText(nz(bn.getDiaChi()));
        oTenLienHe.setText(nz(bn.getTenNguoiLienHe()));
        oSdtLienHe.setText(nz(bn.getSdtNguoiLienHe()));
        oGhiChu.setText(gopGhiChu(bn.getDiUng(), bn.getTienSuBenh(), bn.getGhiChu()));
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String gopGhiChu(String diUng, String tienSu, String ghiChu) {
        if ((diUng == null || diUng.isBlank()) && (tienSu == null || tienSu.isBlank())) {
            return nz(ghiChu);
        }
        StringBuilder sb = new StringBuilder();
        themMucGhiChu(sb, "Dị ứng thuốc / vật liệu", diUng);
        themMucGhiChu(sb, "Tiền sử bệnh", tienSu);
        themMucGhiChu(sb, "Ghi chú khác", ghiChu);
        return sb.toString();
    }

    private static void themMucGhiChu(StringBuilder sb, String tieuDe, String noiDung) {
        if (noiDung == null || noiDung.isBlank()) return;
        if (sb.length() > 0) sb.append("\n\n");
        sb.append(tieuDe).append(":\n").append(noiDung.trim());
    }

    private void luu() {
        // 1) Các ô bắt buộc: thiếu thì báo riêng và đưa con trỏ về đúng ô.
        if (!batBuoc(this, oTen, "họ và tên bệnh nhân")) return;
        if (!batBuoc(this, oNgaySinh, "ngày sinh")) return;
        if (!batBuoc(this, oSdt, "số điện thoại")) return;

        // 2) Lỗi định dạng: thông báo riêng, khác với thông báo thiếu thông tin.
        if (!kiemTraHoTen(oTen.getText())) {
            sai(this, oTen, "Họ và tên chỉ được gồm chữ cái, không được chứa số hoặc ký hiệu.");
            return;
        }
        LocalDate ngaySinh;
        try {
            ngaySinh = oNgaySinh.layNgay();
        } catch (DateTimeParseException e) {
            sai(this, oNgaySinh, "Ngày sinh không đúng định dạng dd/MM/yyyy.");
            return;
        }
        if (!kiemTraNgaySinh(ngaySinh)) {
            sai(this, oNgaySinh, "Ngày sinh phải là một ngày trong quá khứ (sau 01/01/1900).");
            return;
        }
        String sdt = oSdt.getText().trim();
        if (!kiemTraSoDienThoai(sdt)) {
            sai(this, oSdt, "Số điện thoại phải bắt đầu bằng 0 và gồm 10 hoặc 11 chữ số.");
            return;
        }
        String cccd = oCccd.getText().trim();
        if (!cccd.isEmpty() && !kiemTraCccd(cccd)) {
            sai(this, oCccd, "CMND/CCCD phải gồm 9 chữ số (CMND cũ) hoặc 12 chữ số (CCCD).");
            return;
        }
        String sdtLh = oSdtLienHe.getText().trim();
        if (!sdtLh.isEmpty() && !kiemTraSoDienThoai(sdtLh)) {
            sai(this, oSdtLienHe, "Số điện thoại người liên hệ phải bắt đầu bằng 0 và gồm 10 hoặc 11 chữ số.");
            return;
        }

        try {
            bn.setNgaySinh(ngaySinh);
            bn.setHoTen(oTen.getText().trim());
            bn.setGioiTinh((String) cbGioiTinh.getSelectedItem());
            bn.setSoDienThoai(sdt);
            bn.setCccd(cccd);
            bn.setEmail(oEmail.getText().trim());
            bn.setDiaChi(oDiaChi.getText().trim());
            bn.setTenNguoiLienHe(oTenLienHe.getText().trim());
            bn.setSdtNguoiLienHe(sdtLh);
            // Thông tin dị ứng, tiền sử và ghi chú được quản lý chung tại một ô Ghi chú.
            bn.setDiUng("");
            bn.setTienSuBenh("");
            bn.setGhiChu(oGhiChu.getText().trim());
            if (bn.getMaBN() > 0) {
                dao.capNhat(bn);
            } else {
                bn.setMaBN(dao.them(bn));
            }
            daLuu = true;
            dispose();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }
}
