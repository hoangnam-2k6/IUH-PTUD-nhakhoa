package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Dao.PhieuKhamDao;
import Entity.BenhNhan;
import Entity.LichHen;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Lịch hẹn": lọc, tạo/sửa, xác nhận và check-in bệnh nhân. */
public class QuanLyLichHenLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final LichHenDao lichHenDao = new LichHenDao();
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();

    private List<LichHen> dsTatCa = new ArrayList<>();
    private List<LichHen> dsHienThi = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã LH", "Bệnh nhân", "Ngày giờ", "Bác sĩ", "Trạng thái", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JLabel soLuong = new JLabel("0 lịch hẹn");

    private final JTextField oTimKiem = GiaoDien.taoOText();
    private final JComboBox<String> cbTrangThai = new JComboBox<>(
            new String[]{"Tất cả", LichHenDao.CHO_XAC_NHAN, LichHenDao.DA_XAC_NHAN,
                    LichHenDao.DA_DEN, LichHenDao.HOAN_THANH, LichHenDao.DA_HUY});
    // Bộ lọc nâng cao: bấm nút "Bộ lọc" để sổ khung lọc xuống phía trên bảng
    private static final String TAT_CA = "Tất cả";
    private final JTextField oMaLoc = GiaoDien.taoOText();
    private final JTextField oBenhNhanLoc = GiaoDien.taoOText();
    private final JTextField oGhiChuLoc = GiaoDien.taoOText();
    private final JComboBox<String> cbBacSi = new JComboBox<>(new String[]{TAT_CA});
    private final GiaoDien.ONgay oTuNgay = new GiaoDien.ONgay();
    private final GiaoDien.ONgay oDenNgay = new GiaoDien.ONgay();
    private final JButton nutLoc = taoNutTrang("Bộ lọc");
    private GiaoDien.KhungTruot khungLoc;
    private BoLoc boLoc = BoLoc.RONG;
    private boolean dangDatLai;

    public QuanLyLichHenLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Quản lý lịch hẹn",
                "Xác nhận lịch và check-in khi bệnh nhân đến phòng khám. Bấm \"Bộ lọc\" để lọc theo "
                        + "mã lịch, bệnh nhân, bác sĩ, trạng thái, ngày hẹn, ghi chú.");
        // Giữ tiêu đề của thẻ và đặt bộ lọc ngay bên dưới (cùng một vùng NORTH)
        Component tieuDeThe = ((BorderLayout) the.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        JPanel phanDau = new JPanel(new BorderLayout());
        phanDau.setOpaque(false);
        phanDau.add(tieuDeThe, BorderLayout.NORTH);
        phanDau.add(taoBoLoc(), BorderLayout.CENTER);
        the.add(phanDau, BorderLayout.NORTH);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {70, 190, 130, 160, 120, 230};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) suaLichHen();
            }
        });
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        the.add(cuon, BorderLayout.CENTER);

        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);
        JButton nutLamMoi = taoNutTrang("Làm mới");
        JButton nutTao = taoNut("Tạo lịch hẹn", MAU_CHINH, MAU_CHINH_DAM);
        JButton nutSua = taoNut("Sửa", Color.decode("#475569"), Color.decode("#334155"));
        JButton nutXacNhan = taoNut("Xác nhận", Color.decode("#0EA5E9"), Color.decode("#0284C7"));
        JButton nutCheckIn = taoNut("Check-in / Cấp số", THANH_CONG, THANH_CONG.darker());
        JButton nutHuy = taoNut("Hủy lịch", Color.decode("#DC2626"), Color.decode("#B91C1C"));

        nutLamMoi.addActionListener(e -> taiDuLieu());
        nutTao.addActionListener(e -> taoLichHen());
        nutSua.addActionListener(e -> suaLichHen());
        nutXacNhan.addActionListener(e -> xacNhanLich());
        nutCheckIn.addActionListener(e -> checkIn());
        nutHuy.addActionListener(e -> huyLich());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        nut.add(nutTao);
        nut.add(nutSua);
        nut.add(nutXacNhan);
        nut.add(nutCheckIn);
        nut.add(nutHuy);
        nut.add(nutLamMoi);

        JPanel chan = new JPanel(new BorderLayout(10, 0));
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        chan.add(soLuong, BorderLayout.WEST);
        chan.add(nut, BorderLayout.CENTER);
        the.add(chan, BorderLayout.SOUTH);

        add(the, BorderLayout.CENTER);
    }

    private JPanel taoBoLoc() {
        oTimKiem.setToolTipText("Tên bệnh nhân, bác sĩ, mã lịch hẹn hoặc ghi chú");
        // Chỉ tìm khi bấm nút hoặc nhấn Enter, không lọc ngay theo từng ký tự.
        oTimKiem.addActionListener(e -> apDungLoc());
        for (JComboBox<String> cb : List.of(cbTrangThai, cbBacSi)) {
            cb.setFont(font(Font.PLAIN, 13));
            cb.addActionListener(e -> {
                if (!dangDatLai) apDungLoc();
            });
        }
        oMaLoc.setToolTipText("Nhập mã hoặc một phần mã, ví dụ LH012 hoặc 12");
        oBenhNhanLoc.setToolTipText("Nhập một phần tên bệnh nhân, không cần gõ dấu");
        oGhiChuLoc.setToolTipText("Nhập một phần ghi chú, không cần gõ dấu");
        for (JTextField o : new JTextField[]{oMaLoc, oBenhNhanLoc, oGhiChuLoc,
                oTuNgay.oNhap(), oDenNgay.oNhap()}) {
            o.addActionListener(e -> apDungLoc());
        }

        // Hàng tìm kiếm: ô tìm + nút Bộ lọc + nút Tìm kiếm
        JLabel nhanTim = nhan("Tìm kiếm:");
        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> apDungLoc());
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(nutLoc);
        phai.add(nutTim);
        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(nhanTim, BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(phai, BorderLayout.EAST);

        // Khung lọc sổ xuống: 2 hàng x 4 cột
        JPanel luoi = new JPanel(new GridLayout(2, 4, 16, 10));
        luoi.setOpaque(false);
        luoi.add(o("Mã lịch hẹn", oMaLoc));
        luoi.add(o("Bệnh nhân", oBenhNhanLoc));
        luoi.add(o("Bác sĩ", cbBacSi));
        luoi.add(o("Trạng thái", cbTrangThai));
        luoi.add(o("Từ ngày", oTuNgay));
        luoi.add(o("Đến ngày", oDenNgay));
        luoi.add(o("Ghi chú chứa", oGhiChuLoc));

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

        JPanel vo = new JPanel(new BorderLayout());
        vo.setOpaque(false);
        vo.add(hangTim, BorderLayout.NORTH);
        vo.add(khungLoc, BorderLayout.CENTER);
        return vo;
    }

    private JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        return l;
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

    public void taiDuLieu() {
        try {
            dsTatCa = lichHenDao.findAll();
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }
        napDanhSachBacSi();
        hienThi();
    }

    /** Điền ô chọn bác sĩ theo các bác sĩ đang có trong lịch hẹn; giữ lựa chọn cũ nếu còn. */
    private void napDanhSachBacSi() {
        Object dangChon = cbBacSi.getSelectedItem();
        java.util.TreeSet<String> ten = new java.util.TreeSet<>();
        for (LichHen lh : dsTatCa) {
            if (lh.getTenBacSi() != null && !lh.getTenBacSi().isBlank()) ten.add(lh.getTenBacSi());
        }
        dangDatLai = true;
        try {
            cbBacSi.removeAllItems();
            cbBacSi.addItem(TAT_CA);
            for (String t : ten) cbBacSi.addItem(t);
            cbBacSi.setSelectedItem(dangChon != null && ten.contains(dangChon.toString()) ? dangChon : TAT_CA);
        } finally {
            dangDatLai = false;
        }
        // Bác sĩ đã chọn không còn trong dữ liệu thì bỏ điều kiện đó
        if (!TAT_CA.equals(boLoc.bacSi()) && !ten.contains(boLoc.bacSi())) {
            boLoc = boLoc.voiBacSi(TAT_CA);
            capNhatNutLoc();
        }
    }

    /** Đọc các ô lọc, kiểm tra hợp lệ rồi hiển thị lại bảng. */
    private void apDungLoc() {
        LocalDate tu;
        LocalDate den;
        try {
            tu = oTuNgay.layNgay();
            den = oDenNgay.layNgay();
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
            return;
        }
        if (tu != null && den != null && tu.isAfter(den)) {
            JOptionPane.showMessageDialog(this, "Từ ngày không được sau đến ngày.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boLoc = new BoLoc(oTimKiem.getText().trim(), oMaLoc.getText().trim(), oBenhNhanLoc.getText().trim(),
                (String) cbBacSi.getSelectedItem(), (String) cbTrangThai.getSelectedItem(),
                tu, den, oGhiChuLoc.getText().trim());
        capNhatNutLoc();
        hienThi();
    }

    /** Lọc dsTatCa theo bộ lọc đang áp dụng và đổ vào bảng. */
    private void hienThi() {
        Integer maDangChon = lichDangChon() == null ? null : lichDangChon().getMaLH();
        dsHienThi = new ArrayList<>();
        int dongChon = -1;
        for (LichHen lh : dsTatCa) {
            if (!boLoc.khop(lh)) continue;
            if (maDangChon != null && maDangChon == lh.getMaLH()) {
                dongChon = dsHienThi.size();
            }
            dsHienThi.add(lh);
        }

        model.setRowCount(0);
        for (LichHen lh : dsHienThi) {
            model.addRow(new Object[]{
                    String.format("LH%03d", lh.getMaLH()),
                    lh.getTenBenhNhan(),
                    lh.getNgayGio() == null ? "" : lh.getNgayGio().format(NGAY_GIO),
                    lh.getTenBacSi(),
                    lh.getTrangThai(),
                    lh.getGhiChu() == null ? "" : lh.getGhiChu()
            });
        }
        soLuong.setText(dsHienThi.size() + " / " + dsTatCa.size() + " lịch hẹn");
        if (dongChon >= 0) {
            bang.setRowSelectionInterval(dongChon, dongChon);
        } else if (!dsHienThi.isEmpty()) {
            bang.setRowSelectionInterval(0, 0);
        }
    }

    /** Xóa mọi điều kiện (kể cả ô tìm kiếm) và hiển thị lại toàn bộ. */
    private void xoaLoc() {
        dangDatLai = true;
        try {
            oTimKiem.setText("");
            oMaLoc.setText("");
            oBenhNhanLoc.setText("");
            oGhiChuLoc.setText("");
            oTuNgay.setText("");
            oDenNgay.setText("");
            cbTrangThai.setSelectedIndex(0);
            cbBacSi.setSelectedItem(TAT_CA);
        } finally {
            dangDatLai = false;
        }
        boLoc = BoLoc.RONG;
        capNhatNutLoc();
        hienThi();
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

    /** Điều kiện lọc đang áp dụng; các điều kiện kết hợp bằng "và". */
    private record BoLoc(String tuKhoa, String ma, String benhNhan, String bacSi, String trangThai,
                         LocalDate tuNgay, LocalDate denNgay, String ghiChu) {
        static final BoLoc RONG = new BoLoc("", "", "", TAT_CA, TAT_CA, null, null, "");

        private static boolean co(String s) {
            return s != null && !s.isBlank();
        }

        BoLoc voiBacSi(String moi) {
            return new BoLoc(tuKhoa, ma, benhNhan, moi, trangThai, tuNgay, denNgay, ghiChu);
        }

        /** Số điều kiện trong khung lọc (ô tìm kiếm ở hàng trên không tính). */
        int soDieuKien() {
            int n = 0;
            if (co(ma)) n++;
            if (co(benhNhan)) n++;
            if (bacSi != null && !TAT_CA.equals(bacSi)) n++;
            if (trangThai != null && !TAT_CA.equals(trangThai)) n++;
            if (tuNgay != null || denNgay != null) n++;
            if (co(ghiChu)) n++;
            return n;
        }

        boolean khop(LichHen lh) {
            if (co(tuKhoa) && !khopTuKhoa(lh, tuKhoa.trim().toLowerCase(Locale.ROOT))) return false;
            // Mã: "12" hoặc "lh012" đều khớp LH012
            if (co(ma) && !String.format("LH%03d", lh.getMaLH()).contains(ma.trim().toUpperCase(Locale.ROOT))) {
                return false;
            }
            if (co(benhNhan) && !boDau(lh.getTenBenhNhan()).contains(boDau(benhNhan.trim()))) return false;
            if (bacSi != null && !TAT_CA.equals(bacSi) && !bacSi.equals(lh.getTenBacSi())) return false;
            if (trangThai != null && !TAT_CA.equals(trangThai) && !trangThai.equals(lh.getTrangThai())) {
                return false;
            }
            if (tuNgay != null || denNgay != null) {
                LocalDate ngay = lh.getNgayGio() == null ? null : lh.getNgayGio().toLocalDate();
                if (ngay == null) return false;
                if (tuNgay != null && ngay.isBefore(tuNgay)) return false;
                if (denNgay != null && ngay.isAfter(denNgay)) return false;
            }
            return !co(ghiChu) || boDau(lh.getGhiChu()).contains(boDau(ghiChu.trim()));
        }

        private static boolean khopTuKhoa(LichHen lh, String tuKhoa) {
            String ma = String.format("LH%03d", lh.getMaLH()).toLowerCase(Locale.ROOT);
            return ma.contains(tuKhoa)
                    || (lh.getTenBenhNhan() != null && lh.getTenBenhNhan().toLowerCase(Locale.ROOT).contains(tuKhoa))
                    || (lh.getTenBacSi() != null && lh.getTenBacSi().toLowerCase(Locale.ROOT).contains(tuKhoa))
                    || (lh.getGhiChu() != null && lh.getGhiChu().toLowerCase(Locale.ROOT).contains(tuKhoa));
        }
    }

    private LichHen lichDangChon() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= dsHienThi.size()) return null;
        return dsHienThi.get(dong);
    }

    private LichHen yeuCauChon(String thongBao) {
        LichHen lh = lichDangChon();
        if (lh == null) {
            JOptionPane.showMessageDialog(this, thongBao, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        return lh;
    }

    private void taoLichHen() {
        if (LichHenDialog.taoMoi(this, taiKhoan)) {
            taiDuLieu();
        }
    }

    private void suaLichHen() {
        LichHen lh = yeuCauChon("Vui lòng chọn một lịch hẹn để sửa.");
        if (lh == null) return;
        if (lh.daKetThuc()) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn đã kết thúc, không thể sửa.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (LichHenDialog.sua(this, lh)) {
            taiDuLieu();
        }
    }

    private void xacNhanLich() {
        LichHen lh = yeuCauChon("Vui lòng chọn một lịch hẹn để xác nhận.");
        if (lh == null) return;
        if (!LichHenDao.CHO_XAC_NHAN.equals(lh.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ xác nhận được lịch đang ở trạng thái \"" + LichHenDao.CHO_XAC_NHAN + "\".",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            lichHenDao.xacNhan(lh.getMaLH());
            thongBao(this, "Đã xác nhận lịch hẹn của " + lh.getTenBenhNhan() + ".");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void checkIn() {
        LichHen lh = yeuCauChon("Vui lòng chọn lịch hẹn cần check-in.");
        if (lh == null) return;
        if (lh.daKetThuc()) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn đã kết thúc, không thể check-in.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        PhieuKham pk = PhieuKhamDialog.checkIn(this, lh);
        if (pk == null) return;
        taiDuLieu();
        try {
            BenhNhan bn = benhNhanDao.findById(lh.getMaBN());
            String thongTin = "Đã check-in " + lh.getTenBenhNhan() + ".\nSố thứ tự: " + pk.getSoThuTu()
                    + "  ·  " + pk.getTenPhong() + "\n\nIn phiếu khám ngay bây giờ?";
            if (xacNhan(this, thongTin, "Check-in thành công")) {
                XuatPhieuKham.xuat(this, pk, bn, null);
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void huyLich() {
        LichHen lh = yeuCauChon("Vui lòng chọn lịch hẹn cần hủy.");
        if (lh == null) return;
        if (lh.daKetThuc()) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn đã kết thúc.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!xacNhan(this, "Hủy lịch hẹn của " + lh.getTenBenhNhan() + " lúc "
                + lh.getNgayGio().format(NGAY_GIO) + "?", "Hủy lịch hẹn")) {
            return;
        }
        String lyDo = JOptionPane.showInputDialog(this, "Lý do hủy:", "Hủy lịch hẹn",
                JOptionPane.QUESTION_MESSAGE);
        if (lyDo == null) return;
        try {
            lichHenDao.huy(lh.getMaLH(), lyDo);
            thongBao(this, "Đã hủy lịch hẹn.");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static final class TrangThaiRenderer extends GiaoDien.OCoBanRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tt = value == null ? "" : value.toString();
            if (!isSelected) {
                if (LichHenDao.CHO_XAC_NHAN.equals(tt)) c.setForeground(Color.decode("#B45309"));
                else if (LichHenDao.DA_XAC_NHAN.equals(tt)) c.setForeground(MAU_CHINH);
                else if (LichHenDao.DA_DEN.equals(tt)) c.setForeground(Color.decode("#7C3AED"));
                else if (LichHenDao.HOAN_THANH.equals(tt)) c.setForeground(Color.decode("#047857"));
                else if (LichHenDao.DA_HUY.equals(tt)) c.setForeground(Color.decode("#B91C1C"));
            }
            return c;
        }
    }
}