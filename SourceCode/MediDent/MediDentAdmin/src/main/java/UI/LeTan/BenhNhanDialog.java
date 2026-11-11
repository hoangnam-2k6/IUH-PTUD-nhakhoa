package UI.LeTan;

import DAO.BenhNhanLeTanDAO;
import Entity.BenhNhan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import static UI.LeTan.GiaoDien.*;

/** Hộp thoại thêm mới / chỉnh sửa hồ sơ bệnh nhân đầy đủ thông tin. */
public class BenhNhanDialog extends JDialog {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final BenhNhanLeTanDAO dao = new BenhNhanLeTanDAO();
    private final BenhNhan bn;          // đang sửa (maBN > 0) hoặc hồ sơ mới
    private boolean daLuu = false;

    private final JTextField oTen = taoOText();
    private final JTextField oNgaySinh = taoOText();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField oSdt = taoOText();
    private final JTextField oCccd = taoOText();
    private final JTextField oEmail = taoOText();
    private final JTextField oNgheNghiep = taoOText();
    private final JTextField oDiaChi = taoOText();
    private final JTextField oTenLienHe = taoOText();
    private final JTextField oSdtLienHe = taoOText();
    private final JTextArea oDiUng = taoOVanBan(2);
    private final JTextArea oTienSu = taoOVanBan(3);
    private final JTextArea oGhiChu = taoOVanBan(2);

    private BenhNhanDialog(Component cha, BenhNhan bn) {
        super(SwingUtilities.getWindowAncestor(cha), bn.layMaBNSo() > 0
                ? String.format("Sửa hồ sơ BN%03d", bn.layMaBNSo()) : "Thêm bệnh nhân mới", ModalityType.APPLICATION_MODAL);
        this.bn = bn;
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        cbGioiTinh.setFont(taoPhongChu(Font.PLAIN, 14));
        oTen.setToolTipText("Bắt buộc");
        oNgaySinh.setToolTipText("dd/MM/yyyy");
        oDiUng.setToolTipText("Thuốc / vật liệu / thức ăn gây dị ứng (vd: Penicillin, latex)");
        oTienSu.setToolTipText("Tim mạch, huyết áp, tiểu đường, đang mang thai, đang dùng thuốc...");

        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoHangNut(), BorderLayout.SOUTH);
        napDuLieu();

        setSize(640, 640);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(cha);
    }

    /** Mở hộp thoại. {@code bnCu == null}: thêm mới. Trả về true nếu đã lưu thành công. */
    public static boolean hienThi(Component cha, BenhNhan bnCu) {
        BenhNhanDialog d = new BenhNhanDialog(cha, bnCu == null ? new BenhNhan() : bnCu);
        d.setVisible(true);
        return d.daLuu;
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));

        noi.add(tieuDeNhom("Thông tin cá nhân"));
        noi.add(hai(truong("Họ và tên *", oTen), truong("Ngày sinh (dd/MM/yyyy)", oNgaySinh)));
        noi.add(hai(truong("Giới tính", cbGioiTinh), truong("CMND/CCCD", oCccd)));
        noi.add(truong("Nghề nghiệp", oNgheNghiep));

        noi.add(tieuDeNhom("Liên hệ"));
        noi.add(hai(truong("Số điện thoại", oSdt), truong("Email", oEmail)));
        noi.add(truong("Địa chỉ", oDiaChi));
        noi.add(hai(truong("Người liên hệ khẩn cấp", oTenLienHe), truong("SĐT người liên hệ", oSdtLienHe)));

        noi.add(tieuDeNhom("Thông tin y tế"));
        noi.add(truong("Dị ứng (thuốc, vật liệu...)", boc(oDiUng)));
        noi.add(truong("Tiền sử bệnh", boc(oTienSu)));
        noi.add(truong("Ghi chú", boc(oGhiChu)));

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        return cuon;
    }

    private JLabel tieuDeNhom(String chu) {
        JLabel l = new JLabel(chu.toUpperCase());
        l.setFont(taoPhongChu(Font.BOLD, 11));
        l.setForeground(MAU_CHINH);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return l;
    }

    private JPanel hai(JComponent trai, JComponent phai) {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
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
        oTen.setText(chuoiKhongNull(bn.layHoTen()));
        oNgaySinh.setText(bn.layNgaySinh() == null ? "" : bn.layNgaySinh().format(NGAY));
        if (bn.layGioiTinh() != null) cbGioiTinh.setSelectedItem(bn.layGioiTinh());
        oSdt.setText(chuoiKhongNull(bn.laySoDienThoai()));
        oCccd.setText(chuoiKhongNull(bn.layCccd()));
        oEmail.setText(chuoiKhongNull(bn.layEmail()));
        oNgheNghiep.setText(chuoiKhongNull(bn.layNgheNghiep()));
        oDiaChi.setText(chuoiKhongNull(bn.layDiaChi()));
        oTenLienHe.setText(chuoiKhongNull(bn.layTenNguoiLienHe()));
        oSdtLienHe.setText(chuoiKhongNull(bn.laySdtNguoiLienHe()));
        oDiUng.setText(chuoiKhongNull(bn.layDiUng()));
        oTienSu.setText(chuoiKhongNull(bn.layTienSuBenh()));
        oGhiChu.setText(chuoiKhongNull(bn.layGhiChu()));
    }

    private static String chuoiKhongNull(String s) {
        return s == null ? "" : s;
    }

    private void luu() {
        try {
            String ngaySinh = oNgaySinh.getText().trim();
            bn.datNgaySinh(ngaySinh.isEmpty() ? null : LocalDate.parse(ngaySinh, NGAY));
            bn.datHoTen(oTen.getText());
            bn.datGioiTinh((String) cbGioiTinh.getSelectedItem());
            bn.datSoDienThoai(oSdt.getText());
            bn.datCccd(oCccd.getText());
            bn.datEmail(oEmail.getText());
            bn.datNgheNghiep(oNgheNghiep.getText());
            bn.datDiaChi(oDiaChi.getText());
            bn.datTenNguoiLienHe(oTenLienHe.getText());
            bn.datSdtNguoiLienHe(oSdtLienHe.getText());
            bn.datDiUng(oDiUng.getText());
            bn.datTienSuBenh(oTienSu.getText());
            bn.datGhiChu(oGhiChu.getText());
            if (bn.layMaBNSo() > 0) {
                dao.capNhat(bn);
            } else {
                bn.datMaBN(dao.themMoi(bn));
            }
            daLuu = true;
            dispose();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/MM/yyyy.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }
}
