package UI.LeTan;

import Dao.BenhNhanDao;
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

    private final BenhNhanDao dao = new BenhNhanDao();
    private final BenhNhan bn;          // đang sửa (maBN > 0) hoặc hồ sơ mới
    private boolean daLuu = false;

    private final JTextField oTen = taoOText();
    private final ONgay oNgaySinh = new ONgay();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField oSdt = taoOText();
    private final JTextField oCccd = taoOText();
    private final JTextField oEmail = taoOText();
    private final JTextField oNgheNghiep = taoOText();
    private final JTextField oDiaChi = taoOText();
    private final JTextField oTenLienHe = taoOText();
    private final JTextField oSdtLienHe = taoOText();
    private final JTextArea oDiUng = taoOVanBan(3);
    private final JTextArea oTienSu = taoOVanBan(6);
    private final JTextArea oGhiChu = taoOVanBan(3);

    private BenhNhanDialog(Component cha, BenhNhan bn) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha), bn.getMaBN() > 0
                ? String.format("Sửa hồ sơ BN%03d", bn.getMaBN()) : "Thêm bệnh nhân mới", ModalityType.APPLICATION_MODAL);
        this.bn = bn;
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        cbGioiTinh.setFont(font(Font.PLAIN, 14));
        oTen.setToolTipText("Bắt buộc");
        oNgaySinh.oNhap().setToolTipText("Bắt buộc - chọn trên lịch hoặc nhập dd/MM/yyyy");
        oSdt.setToolTipText("Bắt buộc - 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09");
        oSdtLienHe.setToolTipText("Không bắt buộc - nếu nhập phải gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09");
        oCccd.setToolTipText("Bắt buộc - 9 chữ số (CMND cũ) hoặc 12 chữ số (CCCD)");
        oNgheNghiep.setToolTipText("Bắt buộc");
        oDiaChi.setToolTipText("Bắt buộc");
        oDiUng.setToolTipText("Thuốc / vật liệu / thức ăn gây dị ứng (vd: Penicillin, latex)");
        oTienSu.setToolTipText("Tim mạch, huyết áp, tiểu đường, đang mang thai, đang dùng thuốc...");

        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoHangNut(), BorderLayout.SOUTH);
        napDuLieu();

        setSize(680, 760);
        setMinimumSize(new Dimension(600, 460));
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
        noi.add(hai(truong("Họ và tên *", oTen), truong("Ngày sinh * (dd/MM/yyyy)", oNgaySinh)));
        noi.add(hai(truong("Giới tính *", cbGioiTinh), truong("CMND/CCCD *", oCccd)));
        noi.add(truong("Nghề nghiệp *", oNgheNghiep));

        noi.add(tieuDeNhom("Liên hệ"));
        noi.add(hai(truong("Số điện thoại *", oSdt), truong("Email", oEmail)));
        noi.add(truong("Địa chỉ *", oDiaChi));
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
        l.setFont(font(Font.BOLD, 11));
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
        oTen.setText(nz(bn.getHoTen()));
        oNgaySinh.datNgay(bn.getNgaySinh());
        if (bn.getGioiTinh() != null) cbGioiTinh.setSelectedItem(bn.getGioiTinh());
        oSdt.setText(nz(bn.getSoDienThoai()));
        oCccd.setText(nz(bn.getCccd()));
        oEmail.setText(nz(bn.getEmail()));
        oNgheNghiep.setText(nz(bn.getNgheNghiep()));
        oDiaChi.setText(nz(bn.getDiaChi()));
        oTenLienHe.setText(nz(bn.getTenNguoiLienHe()));
        oSdtLienHe.setText(nz(bn.getSdtNguoiLienHe()));
        oDiUng.setText(nz(bn.getDiUng()));
        oTienSu.setText(nz(bn.getTienSuBenh()));
        oGhiChu.setText(nz(bn.getGhiChu()));
    }

    private static final String MA_LOI_SDT =
            "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09.";

    /** SĐT di động Việt Nam: 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09 (bỏ qua khoảng trắng, dấu chấm, gạch nối). */
    private static boolean kiemTraSdtDiDong(String chuoi) {
        if (chuoi == null) return false;
        return chuoi.replaceAll("[\\s.\\-()]", "").matches("0[35789]\\d{8}");
    }

    /** Lưu SĐT dạng chỉ gồm chữ số để tìm kiếm khớp. */
    private static String chiLaySo(String sdt) {
        return sdt.replaceAll("[\\s.\\-()]", "");
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private void luu() {
        // 1) Các ô bắt buộc: thiếu thì báo riêng và đưa con trỏ về đúng ô.
        if (!batBuoc(this, oTen, "họ và tên bệnh nhân")) return;
        if (!batBuoc(this, oNgaySinh, "ngày sinh")) return;
        if (cbGioiTinh.getSelectedItem() == null) {
            sai(this, cbGioiTinh, "Vui lòng chọn giới tính.");
            return;
        }
        if (!batBuoc(this, oCccd, "số CMND/CCCD")) return;
        if (!batBuoc(this, oNgheNghiep, "nghề nghiệp")) return;
        if (!batBuoc(this, oSdt, "số điện thoại")) return;
        if (!batBuoc(this, oDiaChi, "địa chỉ")) return;

        // 2) Lỗi định dạng: thông báo riêng, khác với thông báo thiếu thông tin.
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
        if (!kiemTraSdtDiDong(sdt)) {
            sai(this, oSdt, MA_LOI_SDT);
            return;
        }
        String cccd = oCccd.getText().trim();
        if (!kiemTraCccd(cccd)) {
            sai(this, oCccd, "CMND/CCCD phải gồm 9 chữ số (CMND cũ) hoặc 12 chữ số (CCCD).");
            return;
        }
        String sdtLh = oSdtLienHe.getText().trim();
        if (!sdtLh.isEmpty() && !kiemTraSdtDiDong(sdtLh)) {
            sai(this, oSdtLienHe, "Số điện thoại người liên hệ phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09.");
            return;
        }

        try {
            bn.setNgaySinh(ngaySinh);
            bn.setHoTen(oTen.getText().trim());
            bn.setGioiTinh((String) cbGioiTinh.getSelectedItem());
            bn.setSoDienThoai(chiLaySo(sdt));
            bn.setCccd(cccd);
            bn.setEmail(oEmail.getText().trim());
            bn.setNgheNghiep(oNgheNghiep.getText().trim());
            bn.setDiaChi(oDiaChi.getText().trim());
            bn.setTenNguoiLienHe(oTenLienHe.getText().trim());
            bn.setSdtNguoiLienHe(chiLaySo(sdtLh));
            bn.setDiUng(oDiUng.getText().trim());
            bn.setTienSuBenh(oTienSu.getText().trim());
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