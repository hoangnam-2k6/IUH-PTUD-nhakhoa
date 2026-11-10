package UI.LeTan;

import Dao.BacSiDao;
import Dao.BenhNhanDao;
import Dao.KetQuaKhamDao;
import Dao.PhieuKhamDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.KetQuaKham;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.IntFunction;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Khám bệnh": danh sách phiếu khám và hồ sơ kết quả khám. */
public class KhamBenhLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final BacSiDao bacSiDao = new BacSiDao();
    private final KetQuaKhamDao ketQuaKhamDao = new KetQuaKhamDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();
    private List<BacSi> dsBacSi = new ArrayList<>();
    private PhieuKham dangChon;
    private boolean dangNap;

    private final JTextField oNgay = taoOText();
    private final JTextField oTimKiem = taoOText();
    private final JLabel soLuong = new JLabel("0 phiếu");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Bệnh nhân", "Phòng", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    // Bộ lọc nâng cao: bấm nút "Bộ lọc" để sổ khung lọc xuống phía trên bảng
    private static final String TAT_CA = "Tất cả";
    private final JTextField oMaPhieuLoc = taoOText();
    private final JTextField oMaBNLoc = taoOText();
    private final JTextField oBenhNhanLoc = taoOText();
    private final JComboBox<String> cbPhong = new JComboBox<>(new String[]{TAT_CA});
    private final JComboBox<String> cbTrangThaiLoc = new JComboBox<>(new String[]{TAT_CA,
            PhieuKhamDao.CHO_KHAM, PhieuKhamDao.DANG_KHAM, PhieuKhamDao.HOAN_THANH, PhieuKhamDao.DA_HUY});
    private final JButton nutLoc = taoNutTrang("Bộ lọc");
    private GiaoDien.KhungTruot khungLoc;
    private BoLoc boLoc = BoLoc.RONG;
    private boolean dangDatLai;

    private final JLabel lblBenhNhan = new JLabel("Chưa chọn phiếu khám");
    private final JLabel lblThongTin = new JLabel(" ");
    private final JLabel lblDiUng = new JLabel(" ");
    private final JLabel lblTrangThai = new JLabel("—");

    private final JComboBox<BacSi> cbBacSi = new JComboBox<>();
    private final JTextField oNgayTaiKham = taoOText();
    private final JTextArea oTrieuChung = taoOVanBan(3);
    private final JTextArea oKetQua = taoOVanBan(4);
    private final JTextArea oChanDoan = taoOVanBan(3);
    private final JTextArea oHuongDieuTri = taoOVanBan(3);
    private final JTextArea oDonThuoc = taoOVanBan(3);
    private final JTextArea oGhiChu = taoOVanBan(2);

    private final JButton nutBatDau = taoNut("Bắt đầu khám", Color.decode("#0EA5E9"), Color.decode("#0284C7"));
    private final JButton nutHoanThanh = taoNut("Hoàn thành", THANH_CONG, THANH_CONG.darker());
    private final JButton nutIn = taoNutTrang("In phiếu khám");
    private final JButton nutWord = taoNut("Xuất Word", Color.decode("#1D4ED8"), Color.decode("#1E40AF"));

    public KhamBenhLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(taoBanner(), BorderLayout.NORTH);
        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoDanhSach(), taoKetQua());
        chia.setResizeWeight(0.40);
        chia.setDividerSize(12);
        chia.setBorder(null);
        chia.setOpaque(false);
        add(chia, BorderLayout.CENTER);

        taiBacSi();
        taiDuLieu();
    }

    private JPanel taoBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(NEN_THE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel tieuDe = new JLabel("Khám bệnh & hồ sơ kết quả");
        tieuDe.setFont(font(Font.BOLD, 19));
        tieuDe.setForeground(CHU_CHINH);
        JLabel moTa = new JLabel("Chọn phiếu khám bên trái để xem hồ sơ kết quả và cập nhật trạng thái điều trị.");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 2));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(moTa);

        oNgay.setText(LocalDate.now().format(NGAY));
        oNgay.setPreferredSize(new Dimension(120, 32));
        oNgay.setToolTipText("dd/MM/yyyy");
        JButton nutTai = taoNutTrang("Tải danh sách");
        nutTai.addActionListener(e -> taiDuLieu());
        JPanel loc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        loc.setOpaque(false);
        loc.add(nhan("Ngày khám:"));
        loc.add(oNgay);
        loc.add(nutTai);

        banner.add(chu, BorderLayout.WEST);
        banner.add(loc, BorderLayout.EAST);
        return banner;
    }

    private JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        return l;
    }

    private JPanel taoDanhSach() {
        JPanel the = GiaoDien.taoThe("Phiếu khám trong ngày",
                "Chọn phiếu để xem hồ sơ kết quả; dùng các nút bên dưới để đổi trạng thái.");

        oTimKiem.setToolTipText("Tên bệnh nhân, mã phiếu, phòng hoặc trạng thái (bấm Tìm kiếm hoặc Enter)");
        oTimKiem.addActionListener(e -> apDungLoc());
        bang.setRowSorter(sorter);
        int[] doRong = {45, 80, 170, 100, 105};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) chonDong();
        });

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        hangTim.add(nhan("Tìm kiếm:"), BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> apDungLoc());
        JButton nutTaoPhieu = taoNut("+ Tạo phiếu khám", THANH_CONG, THANH_CONG.darker());
        nutTaoPhieu.addActionListener(e -> taoPhieuKham());
        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);
        khoiTaoBoLoc();
        JPanel phaiTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiTim.setOpaque(false);
        phaiTim.add(nutLoc);
        phaiTim.add(nutTim);
        hangTim.add(phaiTim, BorderLayout.EAST);

        JPanel hangCongCu = new JPanel(new BorderLayout());
        hangCongCu.setOpaque(false);
        hangCongCu.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        hangCongCu.add(soLuong, BorderLayout.WEST);
        hangCongCu.add(nutTaoPhieu, BorderLayout.EAST);

        JPanel phanDau = new JPanel(new BorderLayout());
        phanDau.setOpaque(false);
        phanDau.add(hangTim, BorderLayout.NORTH);
        phanDau.add(khungLoc, BorderLayout.CENTER);
        phanDau.add(hangCongCu, BorderLayout.SOUTH);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(phanDau, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        giua.add(cuon, BorderLayout.CENTER);

        nutBatDau.addActionListener(e -> batDauKham());
        nutHoanThanh.addActionListener(e -> hoanThanhKham());
        nutIn.addActionListener(e -> inPhieuKham());
        nutWord.addActionListener(e -> xuatPhieuKhamWord());
        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hangNut.add(nutBatDau);
        hangNut.add(nutHoanThanh);
        hangNut.add(nutIn);
        hangNut.add(nutWord);
        giua.add(hangNut, BorderLayout.SOUTH);

        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoKetQua() {
        JPanel the = GiaoDien.taoThe("Hồ sơ kết quả khám",
                "Thông tin do bác sĩ ghi nhận sau khi khám (chỉ xem tại phân hệ Lễ tân).");

        lblBenhNhan.setFont(font(Font.BOLD, 16));
        lblBenhNhan.setForeground(CHU_CHINH);
        lblThongTin.setFont(font(Font.PLAIN, 12));
        lblThongTin.setForeground(CHU_PHU);
        lblDiUng.setFont(font(Font.BOLD, 12));
        lblDiUng.setForeground(Color.decode("#B91C1C"));
        lblTrangThai.setFont(font(Font.BOLD, 12));

        JPanel thongTinBenh = new JPanel(new GridLayout(0, 1, 0, 3));
        thongTinBenh.setOpaque(false);
        thongTinBenh.add(lblBenhNhan);
        thongTinBenh.add(lblThongTin);
        JPanel hangTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hangTrangThai.setOpaque(false);
        hangTrangThai.add(lblTrangThai);
        hangTrangThai.add(lblDiUng);
        thongTinBenh.add(hangTrangThai);

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        dau.add(thongTinBenh, BorderLayout.CENTER);
        the.add(dau, BorderLayout.NORTH);

        cbBacSi.setFont(font(Font.PLAIN, 14));

        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setOpaque(false);
        noi.add(truong("Bác sĩ điều trị", cbBacSi));
        noi.add(truong("Triệu chứng", boc(oTrieuChung)));
        noi.add(truong("Kết quả lâm sàng", boc(oKetQua)));
        noi.add(truong("Chẩn đoán", boc(oChanDoan)));
        noi.add(truong("Hướng điều trị", boc(oHuongDieuTri)));
        noi.add(truong("Đơn thuốc", boc(oDonThuoc)));
        noi.add(truong("Ghi chú", boc(oGhiChu)));
        noi.add(truong("Ngày tái khám (dd/MM/yyyy, có thể để trống)", oNgayTaiKham));

        // Phân hệ Lễ tân chỉ xem kết quả do bác sĩ nhập, không nhập liệu tại đây.
        cbBacSi.setEnabled(false);
        oNgayTaiKham.setEditable(false);
        for (JTextArea a : new JTextArea[]{oTrieuChung, oKetQua, oChanDoan, oHuongDieuTri, oDonThuoc, oGhiChu}) {
            a.setEditable(false);
            a.setBackground(NEN_APP);
        }
        oNgayTaiKham.setBackground(NEN_APP);

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        the.add(cuon, BorderLayout.CENTER);
        return the;
    }

    // ===================== Dữ liệu =====================

    private void taiBacSi() {
        try {
            dsBacSi = bacSiDao.findDangHoatDong();
        } catch (Exception e) {
            baoLoi(this, e);
            dsBacSi = new ArrayList<>();
        }
        cbBacSi.removeAllItems();
        for (BacSi bs : dsBacSi) cbBacSi.addItem(bs);
    }

    public void taiDuLieu() {
        LocalDate ngay;
        try {
            ngay = docNgay(oNgay.getText());
            if (ngay == null) ngay = LocalDate.now();
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
            return;
        }

        int maDangChon = dangChon == null ? -1 : dangChon.getMaPK();
        try {
            dsPhieu = phieuKhamDao.findTheoNgay(ngay);
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }

        dangNap = true;
        try {
            model.setRowCount(0);
            int dongChon = -1;
            for (int i = 0; i < dsPhieu.size(); i++) {
                PhieuKham pk = dsPhieu.get(i);
                model.addRow(new Object[]{
                        pk.getSoThuTu(),
                        String.format("PK%03d", pk.getMaPK()),
                        pk.getTenBenhNhan(),
                        pk.getTenPhong() == null ? "—" : pk.getTenPhong(),
                        pk.getTrangThai()
                });
                if (pk.getMaPK() == maDangChon) dongChon = i;
            }
            napDanhSachPhong();
            capNhatSoLuong();
            // dongChon là chỉ số trong model; khi đang lọc phải đổi sang chỉ số hiển thị
            int dongHienThi = dongChon >= 0 ? bang.convertRowIndexToView(dongChon) : -1;
            if (dongHienThi >= 0) {
                bang.setRowSelectionInterval(dongHienThi, dongHienThi);
            } else if (bang.getRowCount() > 0) {
                bang.setRowSelectionInterval(0, 0);
            } else {
                xoaForm();
            }
        } finally {
            dangNap = false;
        }
        if (bang.getRowCount() > 0) {
            chonDong();
        }
    }

    private void capNhatSoLuong() {
        int hien = bang.getRowCount();
        soLuong.setText(hien == dsPhieu.size() ? dsPhieu.size() + " phiếu"
                : hien + " / " + dsPhieu.size() + " phiếu");
    }

    /** Điền ô chọn phòng theo các phòng đang có trong danh sách; giữ lựa chọn cũ nếu còn. */
    private void napDanhSachPhong() {
        Object dangChonPhong = cbPhong.getSelectedItem();
        TreeSet<String> ten = new TreeSet<>();
        for (PhieuKham pk : dsPhieu) {
            if (pk.getTenPhong() != null && !pk.getTenPhong().isBlank()) ten.add(pk.getTenPhong());
        }
        boolean conPhong = dangChonPhong != null && ten.contains(dangChonPhong.toString());
        dangDatLai = true;
        try {
            cbPhong.removeAllItems();
            cbPhong.addItem(TAT_CA);
            for (String t : ten) cbPhong.addItem(t);
            cbPhong.setSelectedItem(conPhong ? dangChonPhong : TAT_CA);
        } finally {
            dangDatLai = false;
        }
        // Phòng đã chọn không còn trong dữ liệu thì bỏ điều kiện đó
        if (!TAT_CA.equals(boLoc.phong()) && !ten.contains(boLoc.phong())) {
            apDungLoc();
        }
    }

    /** Tạo phiếu khám trực tiếp cho bệnh nhân đến khám không có lịch hẹn. */
    private void taoPhieuKham() {
        PhieuKham pk = PhieuKhamDialog.taoMoi(this);
        if (pk == null) return;
        dangChon = pk;
        oNgay.setText(LocalDate.now().format(NGAY));
        taiDuLieu();
        try {
            BenhNhan bn = benhNhanDao.findById(pk.getMaBN());
            String thongTin = "Đã tạo phiếu khám PK" + String.format("%03d", pk.getMaPK())
                    + "\nBệnh nhân: " + pk.getTenBenhNhan()
                    + "\nPhòng: " + pk.getTenPhong() + "  ·  Số thứ tự: " + pk.getSoThuTu()
                    + "\n\nIn phiếu khám ngay bây giờ?";
            if (xacNhan(this, thongTin, "Tạo phiếu khám thành công")) {
                XuatPhieuKham.xuat(this, pk, bn, null);
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    // ===================== Bộ lọc =====================

    /** Dựng khung lọc (ẩn sẵn) và gắn sự kiện cho nút "Bộ lọc". */
    private void khoiTaoBoLoc() {
        for (JComboBox<String> cb : List.of(cbPhong, cbTrangThaiLoc)) {
            cb.setFont(font(Font.PLAIN, 13));
            cb.addActionListener(e -> {
                if (!dangDatLai) apDungLoc();
            });
        }
        oMaPhieuLoc.setToolTipText("Nhập mã hoặc một phần mã, ví dụ PK012 hoặc 12");
        oMaBNLoc.setToolTipText("Nhập mã hoặc một phần mã, ví dụ BN005 hoặc 5");
        oBenhNhanLoc.setToolTipText("Nhập một phần tên bệnh nhân, không cần gõ dấu");
        for (JTextField o : new JTextField[]{oMaPhieuLoc, oMaBNLoc, oBenhNhanLoc}) {
            o.addActionListener(e -> apDungLoc());
        }

        JPanel luoi = new JPanel(new GridLayout(0, 2, 16, 10));
        luoi.setOpaque(false);
        luoi.add(o("Mã phiếu", oMaPhieuLoc));
        luoi.add(o("Mã bệnh nhân", oMaBNLoc));
        luoi.add(o("Bệnh nhân", oBenhNhanLoc));
        luoi.add(o("Phòng khám", cbPhong));
        luoi.add(o("Trạng thái", cbTrangThaiLoc));

        JButton nutXoa = taoNutTrang("Xóa lọc");
        nutXoa.addActionListener(e -> xoaLoc());
        JButton nutApDung = taoNut("Áp dụng", MAU_CHINH, MAU_CHINH_DAM);
        nutApDung.addActionListener(e -> apDungLoc());
        JPanel nutHang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutHang.setOpaque(false);
        nutHang.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        nutHang.add(nutXoa);
        nutHang.add(nutApDung);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(NEN_APP);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        the.add(luoi, BorderLayout.CENTER);
        the.add(nutHang, BorderLayout.SOUTH);

        JPanel noiDung = new JPanel(new BorderLayout());
        noiDung.setOpaque(false);
        noiDung.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        noiDung.add(the, BorderLayout.CENTER);
        khungLoc = new GiaoDien.KhungTruot(noiDung);

        nutLoc.addActionListener(e -> {
            khungLoc.datMo(!khungLoc.dangMo());
            capNhatNutLoc();
        });
        capNhatNutLoc();
    }

    /** Nhãn nhỏ màu xám phía trên, ô nhập phía dưới. */
    private static JPanel o(String nhan, JComponent oNhap) {
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(CHU_PHU);
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(oNhap, BorderLayout.CENTER);
        return p;
    }

    /** Đọc các ô lọc và lọc lại bảng. */
    private void apDungLoc() {
        boLoc = new BoLoc(oTimKiem.getText().trim(), oMaPhieuLoc.getText().trim(), oMaBNLoc.getText().trim(),
                oBenhNhanLoc.getText().trim(), (String) cbPhong.getSelectedItem(),
                (String) cbTrangThaiLoc.getSelectedItem());
        final BoLoc dieuKien = boLoc;
        sorter.setRowFilter(dieuKien.laRong() ? null : new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                int i = e.getIdentifier();
                PhieuKham pk = i >= 0 && i < dsPhieu.size() ? dsPhieu.get(i) : null;
                return dieuKien.khop(e::getStringValue, pk);
            }
        });
        capNhatNutLoc();
        capNhatSoLuong();
        // Phiếu đang chọn bị lọc mất thì chuyển sang dòng đầu tiên còn hiển thị
        if (!dangNap && bang.getSelectedRow() < 0 && bang.getRowCount() > 0) {
            bang.setRowSelectionInterval(0, 0);
        }
    }

    /** Xóa mọi điều kiện (kể cả ô tìm kiếm) và hiển thị lại toàn bộ. */
    private void xoaLoc() {
        dangDatLai = true;
        try {
            oTimKiem.setText("");
            oMaPhieuLoc.setText("");
            oMaBNLoc.setText("");
            oBenhNhanLoc.setText("");
            cbPhong.setSelectedItem(TAT_CA);
            cbTrangThaiLoc.setSelectedItem(TAT_CA);
        } finally {
            dangDatLai = false;
        }
        apDungLoc();
    }

    /** Nút hiển thị số điều kiện đang lọc và mũi tên mở/đóng; đổi màu khi đang mở hoặc đang lọc. */
    private void capNhatNutLoc() {
        int n = boLoc.soDieuKien();
        boolean mo = khungLoc != null && khungLoc.dangMo();
        nutLoc.setText("Bộ lọc" + (n > 0 ? " (" + n + ")" : "") + "  " + (mo ? "▲" : "▼"));
        boolean noiBat = mo || n > 0;
        nutLoc.setForeground(noiBat ? MAU_CHINH_DAM : CHU_CHINH);
        nutLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(noiBat ? MAU_CHINH : VIEN),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
    }

    /**
     * Điều kiện lọc đang áp dụng; các điều kiện kết hợp bằng "và".
     * Số cột theo bảng: STT, Mã phiếu, Bệnh nhân, Phòng, Trạng thái. Mã bệnh nhân
     * không có cột trên bảng nên lấy trực tiếp từ phiếu khám tương ứng.
     */
    private record BoLoc(String tuKhoa, String maPhieu, String maBN, String benhNhan, String phong,
                         String trangThai) {
        static final BoLoc RONG = new BoLoc("", "", "", "", TAT_CA, TAT_CA);
        private static final int COT_MA_PHIEU = 1, COT_BENH_NHAN = 2, COT_PHONG = 3,
                COT_TRANG_THAI = 4, SO_COT = 5;

        private static boolean co(String s) {
            return s != null && !s.isBlank();
        }

        boolean laRong() {
            return !co(tuKhoa) && soDieuKien() == 0;
        }

        /** Số điều kiện trong khung lọc (ô tìm kiếm ở hàng trên không tính). */
        int soDieuKien() {
            int n = 0;
            if (co(maPhieu)) n++;
            if (co(maBN)) n++;
            if (co(benhNhan)) n++;
            if (phong != null && !TAT_CA.equals(phong)) n++;
            if (trangThai != null && !TAT_CA.equals(trangThai)) n++;
            return n;
        }

        boolean khop(IntFunction<String> giaTri, PhieuKham pk) {
            if (co(tuKhoa)) {
                String k = boDau(tuKhoa.trim());
                boolean thay = false;
                for (int i = 0; i < SO_COT && !thay; i++) thay = boDau(giaTri.apply(i)).contains(k);
                if (!thay) return false;
            }
            // Mã: "12" hoặc "pk012" đều khớp PK012
            if (co(maPhieu) && !nz(giaTri.apply(COT_MA_PHIEU)).toUpperCase().contains(maPhieu.trim().toUpperCase())) {
                return false;
            }
            if (co(maBN)) {
                String ma = pk == null ? "" : String.format("BN%03d", pk.getMaBN());
                if (!ma.contains(maBN.trim().toUpperCase())) return false;
            }
            if (co(benhNhan) && !boDau(giaTri.apply(COT_BENH_NHAN)).contains(boDau(benhNhan.trim()))) return false;
            if (phong != null && !TAT_CA.equals(phong) && !phong.equals(giaTri.apply(COT_PHONG))) return false;
            return trangThai == null || TAT_CA.equals(trangThai) || trangThai.equals(giaTri.apply(COT_TRANG_THAI));
        }
    }

    private void chonDong() {
        if (dangNap) return;
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            dangChon = null;
            xoaForm();
            return;
        }
        int dongModel = bang.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= dsPhieu.size()) return;
        chonPhieu(dsPhieu.get(dongModel));
    }

    private void chonPhieu(PhieuKham pk) {
        dangChon = pk;
        lblBenhNhan.setText(pk.getTenBenhNhan() == null ? "—" : pk.getTenBenhNhan());
        lblThongTin.setText(String.format("PK%03d", pk.getMaPK())
                + "  ·  STT " + pk.getSoThuTu()
                + "  ·  " + (pk.getTenPhong() == null ? "Chưa phân phòng" : pk.getTenPhong())
                + "  ·  " + (pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY)));
        lblTrangThai.setText(pk.getTrangThai());
        lblTrangThai.setForeground(mauTrangThai(pk.getTrangThai()));

        try {
            BenhNhan bn = benhNhanDao.findById(pk.getMaBN());
            if (bn != null && bn.getDiUng() != null && !bn.getDiUng().isBlank()) {
                lblDiUng.setText("⚠ Dị ứng: " + bn.getDiUng());
            } else {
                lblDiUng.setText(" ");
            }
        } catch (Exception e) {
            lblDiUng.setText(" ");
        }

        KetQuaKham kq;
        try {
            kq = ketQuaKhamDao.findByMaPK(pk.getMaPK());
        } catch (Exception e) {
            baoLoi(this, e);
            kq = null;
        }
        dangNap = true;
        try {
            if (kq == null) {
                oTrieuChung.setText("");
                oKetQua.setText("");
                oChanDoan.setText("");
                oHuongDieuTri.setText("");
                oDonThuoc.setText("");
                oGhiChu.setText("");
                oNgayTaiKham.setText("");
                cbBacSi.setSelectedItem(null);
            } else {
                oTrieuChung.setText(nz(kq.getTrieuChung()));
                oKetQua.setText(nz(kq.getKetQuaLamSang()));
                oChanDoan.setText(nz(kq.getChanDoan()));
                oHuongDieuTri.setText(nz(kq.getHuongDieuTri()));
                oDonThuoc.setText(nz(kq.getDonThuoc()));
                oGhiChu.setText(nz(kq.getGhiChu()));
                oNgayTaiKham.setText(kq.getNgayTaiKham() == null ? "" : kq.getNgayTaiKham().format(NGAY));
                chonBacSi(kq.getMaBS());
            }
        } finally {
            dangNap = false;
        }
        capNhatNut();
    }

    private void chonBacSi(Integer maBS) {
        if (maBS == null) {
            cbBacSi.setSelectedItem(null);
            return;
        }
        for (int i = 0; i < cbBacSi.getItemCount(); i++) {
            if (cbBacSi.getItemAt(i).getMaBS() == maBS) {
                cbBacSi.setSelectedIndex(i);
                return;
            }
        }
        cbBacSi.setSelectedItem(null);
    }

    private void capNhatNut() {
        boolean coPhieu = dangChon != null;
        boolean daKetThuc = coPhieu && dangChon.daKetThuc();
        nutBatDau.setEnabled(coPhieu && dangChon.choKham());
        nutHoanThanh.setEnabled(coPhieu && !daKetThuc);
        nutIn.setEnabled(coPhieu);
        nutWord.setEnabled(coPhieu);
    }

    private void xoaForm() {
        dangNap = true;
        try {
            lblBenhNhan.setText("Chưa chọn phiếu khám");
            lblThongTin.setText(" ");
            lblDiUng.setText(" ");
            lblTrangThai.setText("—");
            lblTrangThai.setForeground(CHU_PHU);
            oTrieuChung.setText("");
            oKetQua.setText("");
            oChanDoan.setText("");
            oHuongDieuTri.setText("");
            oDonThuoc.setText("");
            oGhiChu.setText("");
            oNgayTaiKham.setText("");
            cbBacSi.setSelectedItem(null);
        } finally {
            dangNap = false;
        }
        dangChon = null;
        capNhatNut();
    }

    // ===================== Thao tác =====================

    private void batDauKham() {
        if (dangChon == null) return;
        try {
            phieuKhamDao.batDauKham(dangChon.getMaPK());
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void hoanThanhKham() {
        if (dangChon == null) return;
        if (!xacNhan(this, "Hoàn thành phiếu khám của " + dangChon.getTenBenhNhan() + "?",
                "Hoàn thành khám")) {
            return;
        }
        try {
            phieuKhamDao.hoanThanh(dangChon.getMaPK());
            thongBao(this, "Đã hoàn thành phiếu khám.");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void inPhieuKham() {
        if (dangChon == null) return;
        try {
            BenhNhan bn = benhNhanDao.findById(dangChon.getMaBN());
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            XuatPhieuKham.xuat(this, dangChon, bn, kq);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void xuatPhieuKhamWord() {
        if (dangChon == null) return;
        try {
            BenhNhan bn = benhNhanDao.findById(dangChon.getMaBN());
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            XuatPhieuKham.xuatWord(this, dangChon, bn, kq);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static Color mauTrangThai(String tt) {
        if (PhieuKhamDao.CHO_KHAM.equals(tt)) return Color.decode("#B45309");
        if (PhieuKhamDao.DANG_KHAM.equals(tt)) return MAU_CHINH;
        if (PhieuKhamDao.HOAN_THANH.equals(tt)) return Color.decode("#047857");
        if (PhieuKhamDao.DA_HUY.equals(tt)) return Color.decode("#B91C1C");
        return CHU_PHU;
    }

    private static final class TrangThaiRenderer extends GiaoDien.OCoBanRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(mauTrangThai(value == null ? "" : value.toString()));
            }
            return c;
        }
    }
}