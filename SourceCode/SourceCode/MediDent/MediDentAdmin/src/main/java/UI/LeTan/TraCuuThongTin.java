package UI.LeTan;

import Dao.BenhNhanDao;
import Entity.BenhNhan;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Bệnh nhân": tìm kiếm, xem hồ sơ đầy đủ, lịch sử khám, thêm/sửa và ngừng hoạt động hồ sơ. */
public class TraCuuThongTin extends JPanel {
    private static final String TAT_CA = "Tất cả";
    private static final String[] TEN_COT = {"Mã BN", "Họ tên", "Ngày sinh", "Tuổi", "Giới tính",
            "Số điện thoại", "Địa chỉ", "Trạng thái"};

    private final TaiKhoan taiKhoan;
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private List<BenhNhan> dsKetQua = new ArrayList<>();
    private BenhNhan dangChon;

    private final JTextField oTimKiem = GiaoDien.taoOText();
    private final JComboBox<String> cbTrangThai = new JComboBox<>(
            new String[]{TAT_CA, BenhNhan.HOAT_DONG, BenhNhan.NGUNG_HOAT_DONG});
    private final JLabel soLuong = new JLabel("0 bệnh nhân");
    private final DefaultTableModel model = new DefaultTableModel(TEN_COT, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JButton nutSua = taoNut("Sửa thông tin", MAU_CHINH, MAU_CHINH_DAM);
    private final JButton nutLichSu = taoNut("Lịch sử khám", Color.decode("#7C3AED"), Color.decode("#6D28D9"));
    private final JButton nutTrangThai = taoNut("Ngừng hoạt động", CANH_BAO, Color.decode("#D97706"));

    // Các ô hiển thị hồ sơ chi tiết
    private final JLabel vMa = giaTri(), vTen = giaTri(), vNgaySinh = giaTri(), vGioiTinh = giaTri(),
            vCccd = giaTri(), vNghe = giaTri(), vSdt = giaTri(), vEmail = giaTri(), vDiaChi = giaTri(),
            vNguoiLh = giaTri(), vSdtLh = giaTri(), vNgayTao = giaTri(), vSoLan = giaTri(),
            vGanNhat = giaTri(), vTrangThai = giaTri(), vTongChi = giaTri();
    private final JTextArea vDiUng = vanBan(), vTienSu = vanBan(), vGhiChu = vanBan();
    private final JLabel canhBaoDiUng = new JLabel(" ");

    public TraCuuThongTin(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần đăng nhập trước khi mở màn hình Bệnh nhân.");
        }
        this.taiKhoan = taiKhoan;

        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Bệnh nhân",
                "Tìm theo mã bệnh nhân, họ tên, số điện thoại hoặc CMND/CCCD. Bấm đúp để xem lịch sử khám.");

        cbTrangThai.setFont(font(Font.PLAIN, 13));
        cbTrangThai.addActionListener(e -> taiDuLieu());

        oTimKiem.setToolTipText("Nhập mã bệnh nhân, họ tên, số điện thoại hoặc CMND/CCCD");
        oTimKiem.addActionListener(e -> taiDuLieu());
        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> taiDuLieu());
        JButton nutThem = taoNutTrang("+ Thêm bệnh nhân");
        nutThem.addActionListener(e -> themMoi());

        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);

        JPanel nutPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutPhai.setOpaque(false);
        nutPhai.add(new JLabel("Trạng thái:"));
        nutPhai.add(cbTrangThai);
        nutPhai.add(nutTim);
        nutPhai.add(nutThem);

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(nutPhai, BorderLayout.EAST);

        JPanel phanBang = new JPanel(new BorderLayout(0, 6));
        phanBang.setOpaque(false);
        phanBang.add(hangTim, BorderLayout.NORTH);
        JScrollPane cuonBang = new JScrollPane(bang);
        cuonBang.setBorder(BorderFactory.createLineBorder(VIEN));
        phanBang.add(cuonBang, BorderLayout.CENTER);
        phanBang.add(soLuong, BorderLayout.SOUTH);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {70, 185, 95, 50, 80, 115, 210, 120};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(7).setCellRenderer(new TrangThaiRenderer());
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) xemLichSu();
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int dong = bang.getSelectedRow();
            if (dong < 0 || dong >= dsKetQua.size()) {
                xoaChiTiet();
                return;
            }
            hienChiTiet(dsKetQua.get(dong));
        });

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, phanBang, taoKhungChiTiet());
        chia.setResizeWeight(0.38);
        chia.setDividerSize(10);
        chia.setBorder(null);
        chia.setOpaque(false);

        the.add(chia, BorderLayout.CENTER);
        add(the, BorderLayout.CENTER);
        xoaChiTiet();
    }

    // ===================== Giao diện hồ sơ chi tiết =====================

    private JComponent taoKhungChiTiet() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_APP);
        noi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 14, 8, 14)));

        JLabel tieuDe = new JLabel("Hồ sơ bệnh nhân");
        tieuDe.setFont(font(Font.BOLD, 14));
        tieuDe.setForeground(CHU_CHINH);
        canhBaoDiUng.setFont(font(Font.BOLD, 12));
        canhBaoDiUng.setForeground(Color.decode("#B91C1C"));
        canhBaoDiUng.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        nutSua.addActionListener(e -> sua());
        nutLichSu.addActionListener(e -> xemLichSu());
        nutTrangThai.addActionListener(e -> doiTrangThai());

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.setOpaque(false);
        trai.add(tieuDe);
        trai.add(canhBaoDiUng);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(nutLichSu);
        phai.add(nutSua);
        phai.add(nutTrangThai);

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        dau.add(trai, BorderLayout.CENTER);
        dau.add(phai, BorderLayout.EAST);
        dau.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(dau);

        JPanel luoi = new JPanel(new GridLayout(0, 4, 16, 8));
        luoi.setOpaque(false);
        luoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        luoi.add(o("Mã bệnh nhân", vMa));
        luoi.add(o("Họ tên", vTen));
        luoi.add(o("Ngày sinh (tuổi)", vNgaySinh));
        luoi.add(o("Giới tính", vGioiTinh));
        luoi.add(o("CMND/CCCD", vCccd));
        luoi.add(o("Nghề nghiệp", vNghe));
        luoi.add(o("Số điện thoại", vSdt));
        luoi.add(o("Email", vEmail));
        luoi.add(o("Địa chỉ", vDiaChi));
        luoi.add(o("Người liên hệ khẩn cấp", vNguoiLh));
        luoi.add(o("SĐT người liên hệ", vSdtLh));
        luoi.add(o("Trạng thái hồ sơ", vTrangThai));
        luoi.add(o("Ngày tạo hồ sơ", vNgayTao));
        luoi.add(o("Số lần khám", vSoLan));
        luoi.add(o("Lần khám gần nhất", vGanNhat));
        luoi.add(o("Tổng đã thanh toán", vTongChi));
        noi.add(luoi);
        noi.add(Box.createVerticalStrut(10));

        JPanel yTe = new JPanel(new GridLayout(1, 3, 16, 0));
        yTe.setOpaque(false);
        yTe.setAlignmentX(Component.LEFT_ALIGNMENT);
        yTe.add(o("Dị ứng", vanBanCuon(vDiUng)));
        yTe.add(o("Tiền sử bệnh", vanBanCuon(vTienSu)));
        yTe.add(o("Ghi chú", vanBanCuon(vGhiChu)));
        noi.add(yTe);

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        return cuon;
    }

    private static JLabel giaTri() {
        JLabel l = new JLabel("—");
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        return l;
    }

    private static JTextArea vanBan() {
        JTextArea a = new JTextArea(3, 14);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(font(Font.PLAIN, 13));
        a.setForeground(CHU_CHINH);
        a.setBackground(NEN_THE);
        a.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return a;
    }

    private static JScrollPane vanBanCuon(JTextArea a) {
        JScrollPane c = new JScrollPane(a);
        c.setBorder(BorderFactory.createLineBorder(VIEN));
        return c;
    }

    /** Nhãn nhỏ màu xám phía trên, giá trị phía dưới. */
    private static JPanel o(String nhan, JComponent giaTri) {
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(CHU_PHU);
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(giaTri, BorderLayout.CENTER);
        return p;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        taiDuLieu(dangChon == null ? -1 : dangChon.getMaBNSo());
    }

    /** Tải lại danh sách theo ô tìm kiếm; nếu {@code chonMa} có trong kết quả thì chọn lại dòng đó. */
    private void taiDuLieu(int chonMa) {
        List<BenhNhan> tatCa;
        try {
            tatCa = benhNhanDao.search(oTimKiem.getText());
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        String locTrangThai = (String) cbTrangThai.getSelectedItem();
        dsKetQua = new ArrayList<>();
        for (BenhNhan bn : tatCa) {
            if (locTrangThai == null || TAT_CA.equals(locTrangThai) || locTrangThai.equals(bn.getTrangThai())) {
                dsKetQua.add(bn);
            }
        }

        model.setRowCount(0);
        int dongChon = -1;
        for (int i = 0; i < dsKetQua.size(); i++) {
            BenhNhan bn = dsKetQua.get(i);
            Integer tuoi = bn.getTuoi();
            model.addRow(new Object[]{String.format("BN%03d", bn.getMaBNSo()), bn.getHoTen(),
                    bn.getNgaySinh() == null ? "" : dinhDangNgay(bn.getNgaySinh()),
                    tuoi == null ? "" : tuoi, bn.getGioiTinh(), bn.getSoDienThoai(), bn.getDiaChi(),
                    bn.getTrangThai()});
            if (bn.getMaBNSo() == chonMa) dongChon = i;
        }
        soLuong.setText(dsKetQua.size() + " bệnh nhân"
                + (TAT_CA.equals(locTrangThai) || locTrangThai == null ? "" : " (" + locTrangThai.toLowerCase() + ")"));
        if (dongChon >= 0) {
            bang.setRowSelectionInterval(dongChon, dongChon);
            hienChiTiet(dsKetQua.get(dongChon));
        } else {
            xoaChiTiet();
        }
    }

    private void hienChiTiet(BenhNhan bn) {
        dangChon = bn;
        boolean hoatDong = bn.getTrangThai() == null || bn.dangHoatDong();
        nutSua.setEnabled(true);
        nutLichSu.setEnabled(true);
        nutTrangThai.setEnabled(true);
        nutTrangThai.setText(hoatDong ? "Ngừng hoạt động" : "Kích hoạt lại");
        nutTrangThai.setBackground(hoatDong ? CANH_BAO : THANH_CONG);

        vMa.setText(String.format("BN%03d", bn.getMaBNSo()));
        vTen.setText(bn.getHoTen());
        Integer tuoi = bn.getTuoi();
        vNgaySinh.setText(bn.getNgaySinh() == null ? "—" : dinhDangNgay(bn.getNgaySinh())
                + (tuoi == null ? "" : " (" + tuoi + " tuổi)"));
        vGioiTinh.setText(hoac(bn.getGioiTinh()));
        vCccd.setText(hoac(bn.getCccd()));
        vNghe.setText(hoac(bn.getNgheNghiep()));
        vSdt.setText(hoac(bn.getSoDienThoai()));
        vEmail.setText(hoac(bn.getEmail()));
        vDiaChi.setText(hoac(bn.getDiaChi()));
        vNguoiLh.setText(hoac(bn.getTenNguoiLienHe()));
        vSdtLh.setText(hoac(bn.getSdtNguoiLienHe()));
        vTrangThai.setText(hoac(bn.getTrangThai()));
        vTrangThai.setForeground(hoatDong ? Color.decode("#047857") : Color.decode("#B91C1C"));
        vDiUng.setText(bn.getDiUng() == null ? "Không ghi nhận" : bn.getDiUng());
        vTienSu.setText(bn.getTienSuBenh() == null ? "Không ghi nhận" : bn.getTienSuBenh());
        vGhiChu.setText(bn.getGhiChu() == null ? "" : bn.getGhiChu());
        vDiUng.setCaretPosition(0);
        vTienSu.setCaretPosition(0);
        canhBaoDiUng.setText(bn.getDiUng() == null || bn.getDiUng().isBlank()
                ? " " : "⚠ Có dị ứng: " + bn.getDiUng());

        try {
            Object[] tk = benhNhanDao.thongKeKham(bn.getMaBNSo());
            vSoLan.setText(String.valueOf(tk[0]));
            vGanNhat.setText(tk[1] == null ? "Chưa khám" : dinhDangNgay((LocalDate) tk[1]));
            vTongChi.setText(tien(benhNhanDao.tongDaThanhToan(bn.getMaBNSo())) + " đ");
        } catch (Exception e) {
            vSoLan.setText("—");
            vGanNhat.setText("—");
            vTongChi.setText("—");
        }
        vNgayTao.setText(bn.getNgayTao() == null ? "—" : bn.getNgayTao().toLocalDate().format(NGAY));
    }

    private void xoaChiTiet() {
        dangChon = null;
        nutSua.setEnabled(false);
        nutLichSu.setEnabled(false);
        nutTrangThai.setEnabled(false);
        nutTrangThai.setText("Ngừng hoạt động");
        for (JLabel l : new JLabel[]{vMa, vTen, vNgaySinh, vGioiTinh, vCccd, vNghe, vSdt, vEmail, vDiaChi,
                vNguoiLh, vSdtLh, vNgayTao, vSoLan, vGanNhat, vTrangThai, vTongChi}) {
            l.setText("—");
            l.setForeground(CHU_CHINH);
        }
        vDiUng.setText("");
        vTienSu.setText("");
        vGhiChu.setText("");
        canhBaoDiUng.setText(" ");
    }

    private static String hoac(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    // ===================== Thao tác =====================

    private void themMoi() {
        if (BenhNhanDialog.hienThi(this, null)) {
            oTimKiem.setText("");
            cbTrangThai.setSelectedItem(TAT_CA);
            taiDuLieu(-1);
        }
    }

    private void sua() {
        if (dangChon == null) return;
        // Sửa trên bản sao để nếu hủy/lỗi thì dữ liệu đang hiển thị không bị lệch
        BenhNhan ban = saoChep(dangChon);
        if (BenhNhanDialog.hienThi(this, ban)) {
            taiDuLieu(ban.getMaBNSo());
        }
    }

    private void doiTrangThai() {
        if (dangChon == null) return;
        boolean hoatDong = dangChon.dangHoatDong();
        String moi = hoatDong ? BenhNhan.NGUNG_HOAT_DONG : BenhNhan.HOAT_DONG;
        String cauHoi = hoatDong
                ? "Ngừng hoạt động hồ sơ " + dangChon.getHoTen() + "?\n"
                + "Hồ sơ và lịch sử khám vẫn được giữ lại, chỉ không hiện trong danh sách đang hoạt động."
                : "Kích hoạt lại hồ sơ " + dangChon.getHoTen() + "?";
        if (!xacNhan(this, cauHoi, hoatDong ? "Ngừng hoạt động hồ sơ" : "Kích hoạt hồ sơ")) return;
        try {
            benhNhanDao.doiTrangThai(dangChon.getMaBNSo(), moi);
            thongBao(this, "Đã chuyển hồ sơ " + String.format("BN%03d", dangChon.getMaBNSo())
                    + " sang trạng thái \"" + moi + "\".");
            taiDuLieu(dangChon.getMaBNSo());
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void xemLichSu() {
        if (dangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bệnh nhân để xem lịch sử khám.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LichSuKhamDialog.hienThi(this, dangChon);
    }

    private BenhNhan saoChep(BenhNhan g) {
        BenhNhan b = new BenhNhan(g.getMaBNSo(), g.getHoTen(), g.getNgaySinh(), g.getGioiTinh(),
                g.getSoDienThoai(), g.getDiaChi());
        b.setCccd(g.getCccd());
        b.setEmail(g.getEmail());
        b.setNgheNghiep(g.getNgheNghiep());
        b.setTenNguoiLienHe(g.getTenNguoiLienHe());
        b.setSdtNguoiLienHe(g.getSdtNguoiLienHe());
        b.setDiUng(g.getDiUng());
        b.setTienSuBenh(g.getTienSuBenh());
        b.setGhiChu(g.getGhiChu());
        b.setTrangThai(g.getTrangThai());
        return b;
    }

    private static final class TrangThaiRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(BenhNhan.HOAT_DONG.equals(value) ? Color.decode("#047857")
                        : Color.decode("#B45309"));
            }
            return c;
        }
    }
}
