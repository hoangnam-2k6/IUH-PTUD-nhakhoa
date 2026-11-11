package UI.LeTan;

import Dao.BenhNhanDao;
import Entity.BenhNhan;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Bệnh nhân": tìm kiếm, xem hồ sơ đầy đủ, lịch sử khám, thêm/sửa hồ sơ. */
public class TraCuuThongTin extends JPanel {
    private static final String TAT_CA = "Tất cả";
    private static final String[] TEN_COT = {"Mã BN", "Họ tên", "Ngày sinh", "Tuổi", "Giới tính",
            "Số điện thoại", "Địa chỉ", "Trạng thái"};

    private final TaiKhoan taiKhoan;
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private List<BenhNhan> dsKetQua = new ArrayList<>();
    private BenhNhan dangChon;

    private final GiaoDien.OTimKiem oTimKiem = new GiaoDien.OTimKiem();
    // Bộ lọc kiểu chọn: chọn cột cần lọc, rồi nhập/chọn giá trị ở thanh tìm kiếm
    private final JComboBox<Truong> cbTruong = new JComboBox<>(Truong.values());
    private final JComboBox<String> cbGiaTri = new JComboBox<>();
    private final CardLayout cardGiaTri = new CardLayout();
    private final JPanel khungGiaTri = new JPanel(cardGiaTri);
    private boolean dangDoiTruong;
    private final DefaultTableModel model = new DefaultTableModel(TEN_COT, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JButton nutSua = taoNut("Sửa thông tin", false);
    private final JButton nutLichSu = taoNut("Lịch sử khám", false);

    // Các ô hiển thị hồ sơ chi tiết
    private final JLabel vMa = giaTri(), vTen = giaTri(), vNgaySinh = giaTri(), vGioiTinh = giaTri(),
            vCccd = giaTri(), vNghe = giaTri(), vSdt = giaTri(), vEmail = giaTri(), vDiaChi = giaTri(),
            vNguoiLh = giaTri(), vSdtLh = giaTri(), vNgayTao = giaTri(), vSoLan = giaTri(),
            vGanNhat = giaTri(), vTrangThai = giaTri(), vTongChi = giaTri();
    private final JTextArea vGhiChu = vanBan();

    public TraCuuThongTin(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần đăng nhập trước khi mở màn hình Bệnh nhân.");
        }
        this.taiKhoan = taiKhoan;

        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Bệnh nhân",
                "Chọn tiêu chí ở ô \"Lọc theo\", nhập hoặc chọn giá trị rồi bấm Tìm kiếm. "
                        + "Bấm đúp vào một dòng để xem lịch sử khám.");

        khoiTaoBoLoc();

        oTimKiem.addActionListener(e -> taiDuLieu());
        JButton nutTim = taoNut("Tìm kiếm", false);
        nutTim.addActionListener(e -> taiDuLieu());
        JButton nutXoaLoc = taoNutTrang("Xóa lọc");
        nutXoaLoc.setToolTipText("Bỏ bộ lọc và hiện lại toàn bộ bệnh nhân");
        nutXoaLoc.addActionListener(e -> {
            datLaiTimKiem();
            taiDuLieu();
            GiaoDien.focus(oTimKiem);
        });
        JButton nutThem = taoNut("+ Thêm bệnh nhân", true);
        nutThem.addActionListener(e -> themMoi());
        for (JButton n : new JButton[]{nutTim, nutXoaLoc, nutThem}) {
            n.setPreferredSize(new Dimension(n.getPreferredSize().width, CAO_O_LOC));
        }


        JPanel hangTim = new JPanel(new BorderLayout(12, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(taoKhungLoc(nutTim, nutXoaLoc), BorderLayout.CENTER);
        hangTim.add(khungNutThem(nutThem), BorderLayout.EAST);

        JPanel phanBang = new JPanel(new BorderLayout(0, 6));
        phanBang.setOpaque(false);
        phanBang.add(hangTim, BorderLayout.NORTH);
        JScrollPane cuonBang = new JScrollPane(bang);
        cuonBang.setBorder(BorderFactory.createLineBorder(VIEN));
        phanBang.add(cuonBang, BorderLayout.CENTER);

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

        nutSua.addActionListener(e -> sua());
        nutLichSu.addActionListener(e -> xemLichSu());

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.setOpaque(false);
        trai.add(tieuDe);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(nutLichSu);
        phai.add(nutSua);

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

        JPanel ghiChu = o("Ghi chú", vanBanCuon(vGhiChu));
        ghiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(ghiChu);

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
        return vanBan(3);
    }

    private static JTextArea vanBan(int soDong) {
        JTextArea a = new JTextArea(soDong, 14);
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
        taiDuLieu(dangChon == null ? -1 : dangChon.getMaBN());
    }

    /** Tải lại danh sách theo cột + giá trị đang chọn; nếu {@code chonMa} có trong kết quả thì chọn lại dòng đó. */
    private void taiDuLieu(int chonMa) {
        Truong truong = (Truong) cbTruong.getSelectedItem();
        String tuKhoa = layTuKhoa();

        Predicate<BenhNhan> dieuKien;
        try {
            dieuKien = taoDieuKien(truong, tuKhoa);
        } catch (IllegalArgumentException e) {
            canhBaoLoc(e.getMessage());
            GiaoDien.focus(oTimKiem);
            return;
        }

        List<BenhNhan> tatCa;
        try {
            // "Tất cả" dùng tìm kiếm của CSDL; các cột còn lại lọc đúng cột đã chọn
            tatCa = truong == Truong.TATCA ? benhNhanDao.search(tuKhoa) : benhNhanDao.findAll();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        dsKetQua = new ArrayList<>();
        for (BenhNhan bn : tatCa) {
            if (dieuKien.test(bn)) dsKetQua.add(bn);
        }

        model.setRowCount(0);
        int dongChon = -1;
        for (int i = 0; i < dsKetQua.size(); i++) {
            BenhNhan bn = dsKetQua.get(i);
            Integer tuoi = bn.getTuoi();
            model.addRow(new Object[]{String.format("BN%03d", bn.getMaBN()), bn.getHoTen(),
                    bn.getNgaySinh() == null ? "" : dinhDangNgay(bn.getNgaySinh()),
                    tuoi == null ? "" : tuoi, bn.getGioiTinh(), bn.getSoDienThoai(), bn.getDiaChi(),
                    bn.getTrangThai()});
            if (bn.getMaBN() == chonMa) dongChon = i;
        }
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

        vMa.setText(String.format("BN%03d", bn.getMaBN()));
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
        vGhiChu.setText(bn.getGhiChu() == null ? "" : bn.getGhiChu());

        try {
            Object[] tk = benhNhanDao.thongKeKham(bn.getMaBN());
            vSoLan.setText(String.valueOf(tk[0]));
            vGanNhat.setText(tk[1] == null ? "Chưa khám" : dinhDangNgay((LocalDate) tk[1]));
            vTongChi.setText(tien(benhNhanDao.tongDaThanhToan(bn.getMaBN())) + " đ");
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
        for (JLabel l : new JLabel[]{vMa, vTen, vNgaySinh, vGioiTinh, vCccd, vNghe, vSdt, vEmail, vDiaChi,
                vNguoiLh, vSdtLh, vNgayTao, vSoLan, vGanNhat, vTrangThai, vTongChi}) {
            l.setText("—");
            l.setForeground(CHU_CHINH);
        }
        vGhiChu.setText("");
    }

    private static String hoac(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    // ===================== Bộ lọc kiểu chọn =====================

    /** Cột có thể chọn để lọc. */
    private enum Truong {
        TATCA("Tất cả", "Nhập mã bệnh nhân, họ tên, số điện thoại, CMND/CCCD hoặc email"),
        MA("Mã BN", "Nhập mã bệnh nhân, ví dụ BN003 hoặc 3"),
        HO_TEN("Họ tên", "Nhập một phần họ tên, không cần gõ dấu"),
        SDT("Số điện thoại", "Nhập một phần số điện thoại"),
        CCCD("CMND/CCCD", "Nhập một phần số CMND/CCCD"),
        NGAY_SINH("Ngày sinh", "Nhập ngày sinh dạng dd/MM/yyyy (hoặc một phần, ví dụ 03/1990)"),
        TUOI("Tuổi", "Nhập một số tuổi (25) hoặc khoảng tuổi (20-30)"),
        GIOI_TINH("Giới tính", "Chọn giới tính cần lọc"),
        DIA_CHI("Địa chỉ", "Nhập một phần địa chỉ, không cần gõ dấu"),
        TRANG_THAI("Trạng thái", "Chọn trạng thái hồ sơ cần lọc");

        final String nhan;
        final String goiY;

        Truong(String nhan, String goiY) {
            this.nhan = nhan;
            this.goiY = goiY;
        }

        /** Cột lọc bằng cách chọn trong danh sách thay vì gõ chữ. */
        boolean laDanhSachChon() {
            return this == GIOI_TINH || this == TRANG_THAI;
        }

        @Override
        public String toString() {
            return nhan;
        }
    }

    private static final int CAO_O_LOC = 36;

    /** Dựng ô "Lọc theo" và ô giá trị (ô gõ chữ hoặc danh sách chọn tùy cột). */
    private void khoiTaoBoLoc() {
        GiaoDien.kieuCombo(cbTruong);
        GiaoDien.kieuCombo(cbGiaTri);
        cbTruong.setToolTipText("Chọn tiêu chí cần lọc");
        cbTruong.setMaximumRowCount(Truong.values().length);
        cbGiaTri.setModel(new DefaultComboBoxModel<>(new String[]{TAT_CA}));

        cbTruong.setPreferredSize(new Dimension(170, CAO_O_LOC));
        cbGiaTri.setPreferredSize(new Dimension(200, CAO_O_LOC));
        oTimKiem.setPreferredSize(new Dimension(200, CAO_O_LOC));

        khungGiaTri.setOpaque(false);
        khungGiaTri.add(oTimKiem, "chu");
        khungGiaTri.add(cbGiaTri, "chon");

        oTimKiem.datGoiY(Truong.TATCA.goiY);
        cbTruong.addActionListener(e -> {
            if (!dangDoiTruong) doiTruong();
        });
        cbGiaTri.addActionListener(e -> {
            if (!dangDoiTruong) taiDuLieu();
        });
    }

    /** Khung lọc: nền xám nhạt, viền mảnh; nhãn nhỏ phía trên từng ô, nút Tìm kiếm / Xóa lọc bên phải. */
    private JComponent taoKhungLoc(JButton nutTim, JButton nutXoaLoc) {
        JPanel khung = new JPanel(new GridBagLayout());
        khung.setBackground(NEN_APP);
        khung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0;
        c.insets = new Insets(0, 0, 4, 10);
        khung.add(nhanLoc("Lọc theo"), c);
        c.gridx = 1;
        c.insets = new Insets(0, 0, 4, 10);
        khung.add(nhanLoc("Tìm kiếm"), c);

        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 10);
        khung.add(cbTruong, c);
        c.gridx = 1;
        c.weightx = 1;
        khung.add(khungGiaTri, c);
        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 8);
        khung.add(nutTim, c);
        c.gridx = 3;
        c.insets = new Insets(0, 0, 0, 0);
        khung.add(nutXoaLoc, c);
        return khung;
    }

    private static JLabel nhanLoc(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(CHU_PHU);
        return l;
    }

    /** Nút thêm mới nằm ngoài khung lọc, canh theo hàng ô nhập. */
    private static JComponent khungNutThem(JButton nutThem) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        p.add(nutThem, BorderLayout.SOUTH);
        return p;
    }

    /** Đổi cột lọc: đưa ô giá trị về trống, đổi kiểu nhập (gõ chữ / chọn) rồi tải lại danh sách. */
    private void doiTruong() {
        Truong truong = (Truong) cbTruong.getSelectedItem();
        dangDoiTruong = true;
        try {
            oTimKiem.setText("");
            oTimKiem.datGoiY(truong.goiY);
            oTimKiem.setToolTipText(truong.goiY);
            cardGiaTri.show(khungGiaTri, "chu");
        } finally {
            dangDoiTruong = false;
        }
        taiDuLieu();
    }

    /** Giá trị đang nhập (hoặc đang chọn); rỗng nghĩa là không lọc. */
    private String layTuKhoa() {
        return oTimKiem.getText() == null ? "" : oTimKiem.getText().trim();
    }

    /** Đưa bộ lọc về mặc định: cột "Tất cả" và ô tìm kiếm trống (chưa tải lại danh sách). */
    private void datLaiTimKiem() {
        dangDoiTruong = true;
        try {
            cbTruong.setSelectedItem(Truong.TATCA);
            oTimKiem.setText("");
            oTimKiem.datGoiY(Truong.TATCA.goiY);
            cardGiaTri.show(khungGiaTri, "chu");
        } finally {
            dangDoiTruong = false;
        }
    }

    /** Tạo điều kiện lọc cho cột đã chọn; ném IllegalArgumentException nếu giá trị nhập không hợp lệ. */
    private static Predicate<BenhNhan> taoDieuKien(Truong truong, String tuKhoa) {
        if (tuKhoa.isEmpty() || truong == Truong.TATCA) return bn -> true;
        String khongDau = boDau(tuKhoa);
        switch (truong) {
            case MA:
                if (tuKhoa.matches("[0-9]{1,9}")) {
                    int ma = Integer.parseInt(tuKhoa);
                    return bn -> bn.getMaBN() == ma;
                }
                return bn -> String.format("BN%03d", bn.getMaBN()).toLowerCase().contains(tuKhoa.toLowerCase());
            case HO_TEN:
                return bn -> boDau(bn.getHoTen()).contains(khongDau);
            case SDT:
                return bn -> bn.getSoDienThoai() != null && bn.getSoDienThoai().contains(tuKhoa);
            case CCCD:
                return bn -> bn.getCccd() != null && bn.getCccd().contains(tuKhoa);
            case NGAY_SINH:
                return bn -> bn.getNgaySinh() != null && dinhDangNgay(bn.getNgaySinh()).contains(tuKhoa);
            case TUOI: {
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("(\\d{1,3})\\s*(?:[-–]\\s*(\\d{1,3}))?").matcher(tuKhoa);
                if (!m.matches()) {
                    throw new IllegalArgumentException("Tuổi nhập một số (ví dụ 25) hoặc một khoảng (ví dụ 20-30).");
                }
                int tu = Integer.parseInt(m.group(1));
                int den = m.group(2) == null ? tu : Integer.parseInt(m.group(2));
                if (tu > den) {
                    throw new IllegalArgumentException("Khoảng tuổi không hợp lệ: số đầu không được lớn hơn số sau.");
                }
                return bn -> bn.getTuoi() != null && bn.getTuoi() >= tu && bn.getTuoi() <= den;
            }
            case GIOI_TINH:
                return bn -> bn.getGioiTinh() != null
                        && boDau(bn.getGioiTinh()).contains(khongDau);
            case DIA_CHI:
                return bn -> boDau(bn.getDiaChi()).contains(khongDau);
            case TRANG_THAI:
                return bn -> bn.getTrangThai() != null
                        && boDau(bn.getTrangThai()).contains(khongDau);
            default:
                return bn -> true;
        }
    }

    private void canhBaoLoc(String noiDung) {
        JOptionPane.showMessageDialog(this, noiDung, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private static String boDau(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }

    // ===================== Thao tác =====================

    private void themMoi() {
        if (BenhNhanDialog.hienThi(this, null)) {
            oTimKiem.setText("");
            datLaiTimKiem();
            taiDuLieu(-1);
        }
    }

    private void sua() {
        if (dangChon == null) return;
        // Sửa trên bản sao để nếu hủy/lỗi thì dữ liệu đang hiển thị không bị lệch
        BenhNhan ban = saoChep(dangChon);
        if (BenhNhanDialog.hienThi(this, ban)) {
            taiDuLieu(ban.getMaBN());
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
        BenhNhan b = new BenhNhan(g.getMaBN(), g.getHoTen(), g.getNgaySinh(), g.getGioiTinh(),
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

    private static final class TrangThaiRenderer extends GiaoDien.OCoBanRenderer {
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
