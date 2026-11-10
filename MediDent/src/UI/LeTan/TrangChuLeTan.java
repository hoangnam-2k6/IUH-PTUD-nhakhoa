package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Dao.PhieuKhamDao;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static UI.LeTan.GiaoDien.*;

public class TrangChuLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final LichHenDao lichHenDao = new LichHenDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();

    private final JLabel soTongBN = new JLabel("0");
    private final JLabel soChoKham = new JLabel("0");
    private final JLabel soHoanThanh = new JLabel("0");
    private final JLabel soLichHen = new JLabel("0");

    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Phòng khám", "Trạng thái", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // trang chủ chỉ để xem, thao tác phiếu nằm ở màn Khám bệnh
        }
    };
    private final JTable bangPhieu = GiaoDien.taoBang(modelPhieu);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelPhieu);
    private final JTextField oTimKiem = GiaoDien.taoOText();

    // Bộ lọc nâng cao: bấm nút "Bộ lọc" để sổ khung lọc xuống phía trên bảng
    private static final String TAT_CA = "Tất cả";
    private final JTextField oMaPhieuLoc = GiaoDien.taoOText();
    private final JTextField oMaBNLoc = GiaoDien.taoOText();
    private final JTextField oBenhNhanLoc = GiaoDien.taoOText();
    private final JComboBox<String> cbPhong = new JComboBox<>(new String[]{TAT_CA});
    private final JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{TAT_CA,
            PhieuKhamDao.CHO_KHAM, PhieuKhamDao.DANG_KHAM, PhieuKhamDao.HOAN_THANH, PhieuKhamDao.DA_HUY});
    private final JButton nutLoc = taoNutTrang("Bộ lọc");
    private GiaoDien.KhungTruot khungLoc;
    private BoLoc boLoc = BoLoc.RONG;
    private boolean dangDatLai;
    private final JLabel soLuongPhieu = new JLabel("0 phiếu");

    public TrangChuLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel phanTren = new JPanel(new BorderLayout(0, 12));
        phanTren.setOpaque(false);
        phanTren.add(taoBanner(), BorderLayout.NORTH);
        phanTren.add(taoHangThongKe(), BorderLayout.CENTER);

        add(phanTren, BorderLayout.NORTH);
        add(taoKhamVaDieuTri(), BorderLayout.CENTER);
    }

    // ===================== Xây dựng giao diện =====================

    private JPanel taoBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(MAU_CHINH);
        banner.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel tieuDe = new JLabel("Tổng quan & Khám điều trị");
        tieuDe.setFont(font(Font.BOLD, 22));
        tieuDe.setForeground(Color.WHITE);

        JLabel chao = new JLabel("Xin chào, " + taiKhoan.getHoTen() + "  ·  Hôm nay "
                + LocalDate.now().format(NGAY));
        chao.setFont(font(Font.PLAIN, 13));
        chao.setForeground(MAU_CHINH_NHAT);

        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 4));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(chao);

        JButton nutLamMoi = taoNut("Làm mới", Color.decode("#0EA5E9"), Color.decode("#38BDF8"));
        nutLamMoi.addActionListener(e -> taiDuLieu());
        JPanel goc = new JPanel(new GridBagLayout());
        goc.setOpaque(false);
        goc.add(nutLamMoi);

        banner.add(chu, BorderLayout.WEST);
        banner.add(goc, BorderLayout.EAST);
        return banner;
    }

    private JPanel taoHangThongKe() {
        JPanel hang = new JPanel(new GridLayout(1, 4, 12, 0));
        hang.setOpaque(false);
        hang.add(taoTheThongKe("Tổng bệnh nhân", soTongBN, MAU_CHINH));
        hang.add(taoTheThongKe("Chờ khám hôm nay", soChoKham, CANH_BAO));
        hang.add(taoTheThongKe("Đã hoàn thành hôm nay", soHoanThanh, THANH_CONG));
        hang.add(taoTheThongKe("Lịch hẹn hôm nay", soLichHen, Color.decode("#8B5CF6")));
        return hang;
    }

    private JPanel taoTheThongKe(String nhan, JLabel so, Color mau) {
        JPanel the = new JPanel(new BorderLayout(0, 2));
        the.setBackground(NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mau),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14))));

        JLabel tenThe = new JLabel(nhan);
        tenThe.setFont(font(Font.PLAIN, 13));
        tenThe.setForeground(CHU_PHU);
        so.setFont(font(Font.BOLD, 26));
        so.setForeground(CHU_CHINH);

        the.add(tenThe, BorderLayout.NORTH);
        the.add(so, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoKhamVaDieuTri() {
        JPanel the = GiaoDien.taoThe("Khám & điều trị",
                "Danh sách phiếu khám hôm nay (chỉ xem). Thao tác bắt đầu/hoàn thành/hủy phiếu ở màn Khám bệnh. "
                        + "Bấm \"Bộ lọc\" để lọc theo mã phiếu, mã BN, bệnh nhân, phòng, trạng thái, ghi chú.");

        oTimKiem.setToolTipText("Gõ tên bệnh nhân, mã phiếu, phòng hoặc trạng thái rồi bấm Tìm kiếm hoặc Enter");
        oTimKiem.addActionListener(e -> apDungLoc());
        bangPhieu.setRowSorter(sorter);
        int[] doRong = {45, 75, 70, 175, 90, 100, 150};
        for (int i = 0; i < doRong.length; i++) {
            bangPhieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bangPhieu.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());

        JLabel nhanTim = new JLabel("Tìm kiếm:");
        nhanTim.setFont(font(Font.PLAIN, 13));
        nhanTim.setForeground(CHU_PHU);
        soLuongPhieu.setFont(font(Font.BOLD, 12));
        soLuongPhieu.setForeground(MAU_CHINH);

        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> apDungLoc());
        khoiTaoBoLoc();
        JPanel phaiTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiTim.setOpaque(false);
        phaiTim.add(nutLoc);
        phaiTim.add(nutTim);
        phaiTim.add(soLuongPhieu);

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(nhanTim, BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(phaiTim, BorderLayout.EAST);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        JPanel phanDau = new JPanel(new BorderLayout());
        phanDau.setOpaque(false);
        phanDau.add(hangTim, BorderLayout.NORTH);
        phanDau.add(khungLoc, BorderLayout.CENTER);
        giua.add(phanDau, BorderLayout.NORTH);
        giua.add(new JScrollPane(bangPhieu), BorderLayout.CENTER);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        int tongBN;
        int lichHenHomNay;
        try {
            dsPhieu = phieuKhamDao.findHomNay();
            tongBN = benhNhanDao.demDangHoatDong();
            lichHenHomNay = lichHenDao.findTheoNgay(LocalDate.now()).size();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        modelPhieu.setRowCount(0);
        int choKham = 0;
        int hoanThanh = 0;
        for (PhieuKham pk : dsPhieu) {
            modelPhieu.addRow(new Object[]{pk.getSoThuTu(), String.format("PK%03d", pk.getMaPK()),
                    String.format("BN%03d", pk.getMaBN()), pk.getTenBenhNhan(),
                    pk.getTenPhong() == null ? "" : pk.getTenPhong(), pk.getTrangThai(),
                    pk.getGhiChu() == null ? "" : pk.getGhiChu()});
            if (PhieuKhamDao.CHO_KHAM.equals(pk.getTrangThai())) choKham++;
            if (PhieuKhamDao.HOAN_THANH.equals(pk.getTrangThai())) hoanThanh++;
        }
        soTongBN.setText(String.valueOf(tongBN));
        soChoKham.setText(String.valueOf(choKham));
        soHoanThanh.setText(String.valueOf(hoanThanh));
        soLichHen.setText(String.valueOf(lichHenHomNay));
        napDanhSachPhong();
        capNhatSoLuong();
    }

    private void capNhatSoLuong() {
        int hien = bangPhieu.getRowCount();
        soLuongPhieu.setText(hien == dsPhieu.size() ? dsPhieu.size() + " phiếu"
                : hien + " / " + dsPhieu.size() + " phiếu");
    }

    /** Điền ô chọn phòng theo các phòng đang có trong phiếu hôm nay; giữ lựa chọn cũ nếu còn. */
    private void napDanhSachPhong() {
        Object dangChon = cbPhong.getSelectedItem();
        java.util.TreeSet<String> ten = new java.util.TreeSet<>();
        for (PhieuKham pk : dsPhieu) {
            if (pk.getTenPhong() != null && !pk.getTenPhong().isBlank()) ten.add(pk.getTenPhong());
        }
        dangDatLai = true;
        try {
            cbPhong.removeAllItems();
            cbPhong.addItem(TAT_CA);
            for (String t : ten) cbPhong.addItem(t);
            cbPhong.setSelectedItem(dangChon != null && ten.contains(dangChon.toString()) ? dangChon : TAT_CA);
        } finally {
            dangDatLai = false;
        }
        // Phòng đã chọn không còn trong dữ liệu thì bỏ điều kiện đó
        if (!TAT_CA.equals(boLoc.phong()) && !ten.contains(boLoc.phong())) {
            apDungLoc();
        }
    }

    // ===================== Bộ lọc =====================

    /** Dựng khung lọc (ẩn sẵn) và gắn sự kiện cho nút "Bộ lọc". */
    private void khoiTaoBoLoc() {
        for (JComboBox<String> cb : List.of(cbPhong, cbTrangThai)) {
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

        JPanel luoi = new JPanel(new GridLayout(2, 3, 16, 10));
        luoi.setOpaque(false);
        luoi.add(o("Mã phiếu", oMaPhieuLoc));
        luoi.add(o("Mã bệnh nhân", oMaBNLoc));
        luoi.add(o("Bệnh nhân", oBenhNhanLoc));
        luoi.add(o("Phòng khám", cbPhong));
        luoi.add(o("Trạng thái", cbTrangThai));

        JButton nutXoa = taoNutTrang("Xóa lọc");
        nutXoa.addActionListener(e -> xoaLoc());
        JButton nutApDung = taoNut("Áp dụng", MAU_CHINH, MAU_CHINH_DAM);
        nutApDung.addActionListener(e -> apDungLoc());
        JPanel nutHang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutHang.setOpaque(false);
        nutHang.add(nutXoa);
        nutHang.add(nutApDung);
        JPanel oNut = new JPanel(new BorderLayout());
        oNut.setOpaque(false);
        oNut.add(nutHang, BorderLayout.SOUTH);
        luoi.add(oNut);

        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(NEN_APP);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        the.add(luoi, BorderLayout.CENTER);

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
                (String) cbTrangThai.getSelectedItem());
        final BoLoc dieuKien = boLoc;
        sorter.setRowFilter(dieuKien.laRong() ? null : new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                return dieuKien.khop(e::getStringValue);
            }
        });
        capNhatNutLoc();
        capNhatSoLuong();
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
            cbTrangThai.setSelectedItem(TAT_CA);
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

    /** Điều kiện lọc đang áp dụng; các điều kiện kết hợp bằng "và". Số cột theo bảng phiếu khám. */
    private record BoLoc(String tuKhoa, String maPhieu, String maBN, String benhNhan, String phong,
                         String trangThai) {
        static final BoLoc RONG = new BoLoc("", "", "", "", TAT_CA, TAT_CA);
        private static final int COT_MA_PHIEU = 1, COT_MA_BN = 2, COT_BENH_NHAN = 3, COT_PHONG = 4,
                COT_TRANG_THAI = 5, SO_COT = 7;

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

        boolean khop(IntFunction<String> giaTri) {
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
            if (co(maBN) && !nz(giaTri.apply(COT_MA_BN)).toUpperCase().contains(maBN.trim().toUpperCase())) {
                return false;
            }
            if (co(benhNhan) && !boDau(giaTri.apply(COT_BENH_NHAN)).contains(boDau(benhNhan.trim()))) return false;
            if (phong != null && !TAT_CA.equals(phong) && !phong.equals(giaTri.apply(COT_PHONG))) return false;
            return trangThai == null || TAT_CA.equals(trangThai) || trangThai.equals(giaTri.apply(COT_TRANG_THAI));
        }

        private static String nz(String s) {
            return s == null ? "" : s;
        }
    }

    private static final class TrangThaiRenderer extends GiaoDien.OCoBanRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tt = value == null ? "" : value.toString();
            if (!isSelected) {
                if (PhieuKhamDao.CHO_KHAM.equals(tt)) c.setForeground(Color.decode("#B45309"));
                else if (PhieuKhamDao.DANG_KHAM.equals(tt)) c.setForeground(MAU_CHINH);
                else if (PhieuKhamDao.HOAN_THANH.equals(tt)) c.setForeground(Color.decode("#047857"));
                else if (PhieuKhamDao.DA_HUY.equals(tt)) c.setForeground(Color.decode("#B91C1C"));
            }
            return c;
        }
    }
}