package UI.LeTan;

import Dao.HoaDonDao;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Thanh toán & hóa đơn": lọc, lập/sửa, thu tiền, hủy, in và xuất danh sách hóa đơn. */
public class ThanhToanHoaDonLeTan extends JPanel {
    private static final String TAT_CA = "Tất cả";

    private final TaiKhoan taiKhoan;
    private final HoaDonDao hoaDonDao = new HoaDonDao();

    private List<HoaDon> dsHoaDon = new ArrayList<>();
    private HoaDon dangChon;

    private final JTextField oTimKiem = taoOText();
    private final JComboBox<String> cbTrangThai = new JComboBox<>(
            new String[]{TAT_CA, HoaDon.CHUA_THANH_TOAN, HoaDon.DA_THANH_TOAN, HoaDon.DA_HUY});
    private final JTextField oTuNgay = taoOText();
    private final JTextField oDenNgay = taoOText();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã HĐ", "Bệnh nhân", "Ngày lập", "Tổng tiền (VNĐ)", "Phương thức", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);

    private final JLabel lblTongSo = giaTriThe("0");
    private final JLabel lblChuaThu = giaTriThe("0");
    private final JLabel lblDaThu = giaTriThe("0");
    private final JLabel lblDaHuy = giaTriThe("0");

    // Chi tiết hóa đơn đang chọn
    private final JLabel lblMaHD = new JLabel("Chưa chọn hóa đơn");
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblThongTin = new JLabel(" ");
    private final JLabel lblTrangThai = new JLabel(" ");
    private final JLabel vMaPK = giaTri(), vNgayKham = giaTri(), vPhong = giaTri(), vSoThuTu = giaTri(),
            vTamTinh = giaTri(), vGiamGia = giaTri(), vTongTien = giaTri(), vPhuongThuc = giaTri(),
            vNgayLap = giaTri(), vNguoiLap = giaTri(), vNgayThanhToan = giaTri(), vKhachDua = giaTri(),
            vTienThua = giaTri();
    private final JTextArea vGhiChu = vanBan();
    private final DefaultTableModel modelChiTiet = new DefaultTableModel(
            new String[]{"Dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bangChiTiet = GiaoDien.taoBang(modelChiTiet);

    public ThanhToanHoaDonLeTan(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần đăng nhập trước khi mở màn hình Thanh toán.");
        }
        this.taiKhoan = taiKhoan;

        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Thanh toán & hóa đơn",
                "Lọc hóa đơn, lập hóa đơn cho phiếu khám, thu tiền, hủy hoặc in/xuất danh sách.");
        the.add(taoPhanDau(), BorderLayout.NORTH);
        the.add(taoPhanGiua(), BorderLayout.CENTER);
        the.add(taoPhanNut(), BorderLayout.SOUTH);
        add(the, BorderLayout.CENTER);
    }

    // ===================== Giao diện =====================

    private JComponent taoPhanDau() {
        JPanel dau = new JPanel(new BorderLayout(0, 10));
        dau.setOpaque(false);

        oTimKiem.setToolTipText("Tên bệnh nhân, số điện thoại, mã hóa đơn, mã bệnh nhân hoặc mã phiếu khám");
        oTimKiem.addActionListener(e -> taiDuLieu());
        cbTrangThai.setFont(font(Font.PLAIN, 13));
        cbTrangThai.addActionListener(e -> taiDuLieu());
        oTuNgay.setToolTipText("dd/MM/yyyy - để trống nếu không lọc");
        oDenNgay.setToolTipText("dd/MM/yyyy - để trống nếu không lọc");
        oTuNgay.setPreferredSize(new Dimension(110, 32));
        oDenNgay.setPreferredSize(new Dimension(110, 32));

        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> taiDuLieu());
        JButton nutXoaLoc = taoNutTrang("Xóa bộ lọc");
        nutXoaLoc.addActionListener(e -> xoaLoc());

        JPanel hangLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangLoc.setOpaque(false);
        hangLoc.add(nhan("Từ khóa:"));
        hangLoc.add(oTimKiem);
        oTimKiem.setPreferredSize(new Dimension(240, 32));
        hangLoc.add(nhan("Trạng thái:"));
        hangLoc.add(cbTrangThai);
        hangLoc.add(nhan("Từ ngày:"));
        hangLoc.add(oTuNgay);
        hangLoc.add(nhan("Đến ngày:"));
        hangLoc.add(oDenNgay);
        hangLoc.add(nutTim);
        hangLoc.add(nutXoaLoc);
        dau.add(hangLoc, BorderLayout.NORTH);

        JPanel thongKe = new JPanel(new GridLayout(1, 4, 10, 0));
        thongKe.setOpaque(false);
        thongKe.add(theThongKe("Tổng hóa đơn", lblTongSo, MAU_CHINH_DAM, MAU_CHINH_NHAT));
        thongKe.add(theThongKe("Chưa thanh toán", lblChuaThu, Color.decode("#B45309"), Color.decode("#FEF3C7")));
        thongKe.add(theThongKe("Đã thanh toán", lblDaThu, Color.decode("#047857"), Color.decode("#D1FAE5")));
        thongKe.add(theThongKe("Đã hủy", lblDaHuy, Color.decode("#B91C1C"), Color.decode("#FEE2E2")));
        dau.add(thongKe, BorderLayout.CENTER);
        return dau;
    }

    private static JPanel theThongKe(String tieuDe, JLabel giaTri, Color mauChu, Color nen) {
        JPanel the = new JPanel(new GridLayout(0, 1, 0, 2));
        the.setBackground(nen);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JLabel l = new JLabel(tieuDe);
        l.setFont(font(Font.BOLD, 11));
        l.setForeground(CHU_PHU);
        giaTri.setFont(font(Font.BOLD, 17));
        giaTri.setForeground(mauChu);
        the.add(l);
        the.add(giaTri);
        return the;
    }

    private static JLabel giaTriThe(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 17));
        return l;
    }

    private JComponent taoPhanGiua() {
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {70, 170, 125, 120, 110, 120};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            chonDong();
        });
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) inHoaDon();
            }
        });

        JScrollPane cuonBang = new JScrollPane(bang);
        cuonBang.setBorder(BorderFactory.createLineBorder(VIEN));

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cuonBang, taoChiTiet());
        chia.setResizeWeight(0.58);
        chia.setDividerSize(10);
        chia.setBorder(null);
        chia.setOpaque(false);
        return chia;
    }

    private JComponent taoChiTiet() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_APP);
        noi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 14, 10, 14)));

        lblMaHD.setFont(font(Font.BOLD, 16));
        lblMaHD.setForeground(CHU_CHINH);
        lblBenhNhan.setFont(font(Font.BOLD, 14));
        lblBenhNhan.setForeground(MAU_CHINH_DAM);
        lblThongTin.setFont(font(Font.PLAIN, 12));
        lblThongTin.setForeground(CHU_PHU);
        lblTrangThai.setFont(font(Font.BOLD, 13));

        JPanel dau = new JPanel(new BorderLayout(8, 0));
        dau.setOpaque(false);
        dau.setAlignmentX(Component.LEFT_ALIGNMENT);
        dau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JPanel chu = new JPanel(new GridLayout(0, 1, 0, 2));
        chu.setOpaque(false);
        chu.add(lblMaHD);
        chu.add(lblBenhNhan);
        chu.add(lblThongTin);
        dau.add(chu, BorderLayout.CENTER);
        dau.add(lblTrangThai, BorderLayout.EAST);
        noi.add(dau);
        noi.add(Box.createVerticalStrut(8));

        JPanel luoi = new JPanel(new GridLayout(0, 2, 12, 6));
        luoi.setOpaque(false);
        luoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        luoi.add(o("Phiếu khám", vMaPK));
        luoi.add(o("Ngày khám", vNgayKham));
        luoi.add(o("Phòng", vPhong));
        luoi.add(o("Số thứ tự", vSoThuTu));
        luoi.add(o("Tạm tính", vTamTinh));
        luoi.add(o("Giảm giá", vGiamGia));
        luoi.add(o("Tổng cộng", vTongTien));
        luoi.add(o("Phương thức", vPhuongThuc));
        luoi.add(o("Ngày lập", vNgayLap));
        luoi.add(o("Người lập", vNguoiLap));
        luoi.add(o("Ngày thanh toán", vNgayThanhToan));
        luoi.add(o("Khách đưa", vKhachDua));
        luoi.add(o("Tiền thừa", vTienThua));
        noi.add(luoi);
        noi.add(Box.createVerticalStrut(6));

        JPanel ghiChu = new JPanel(new BorderLayout(0, 3));
        ghiChu.setOpaque(false);
        ghiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghiChu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        JLabel l = new JLabel("Ghi chú / lý do hủy");
        l.setFont(font(Font.PLAIN, 12));
        l.setForeground(CHU_PHU);
        vGhiChu.setRows(2);
        ghiChu.add(l, BorderLayout.NORTH);
        ghiChu.add(boc(vGhiChu), BorderLayout.CENTER);
        noi.add(ghiChu);
        noi.add(Box.createVerticalStrut(8));

        JLabel tieuDe = taoTieuDeNhom("Dịch vụ trong hóa đơn");
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(tieuDe);
        int[] doRong = {190, 45, 90, 105};
        for (int i = 0; i < doRong.length; i++) {
            bangChiTiet.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        JScrollPane cuon = new JScrollPane(bangChiTiet);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        cuon.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(cuon);

        JScrollPane cuonNoi = new JScrollPane(noi);
        cuonNoi.setBorder(null);
        cuonNoi.getVerticalScrollBar().setUnitIncrement(16);
        return cuonNoi;
    }

    private JComponent taoPhanNut() {
        JButton nutLamMoi = taoNutTrang("Làm mới");
        JButton nutLap = taoNut("Lập hóa đơn", MAU_CHINH, MAU_CHINH_DAM);
        JButton nutSua = taoNut("Sửa hóa đơn", Color.decode("#475569"), Color.decode("#334155"));
        JButton nutThu = taoNut("Thu tiền", THANH_CONG, THANH_CONG.darker());
        JButton nutHuy = taoNut("Hủy hóa đơn", Color.decode("#DC2626"), Color.decode("#B91C1C"));
        JButton nutIn = taoNutTrang("In hóa đơn");
        JButton nutXuat = taoNut("Xuất danh sách", Color.decode("#7C3AED"), Color.decode("#6D28D9"));

        nutLamMoi.addActionListener(e -> taiDuLieu());
        nutLap.addActionListener(e -> lapHoaDon());
        nutSua.addActionListener(e -> suaHoaDon());
        nutThu.addActionListener(e -> thuTien());
        nutHuy.addActionListener(e -> huyHoaDon());
        nutIn.addActionListener(e -> inHoaDon());
        nutXuat.addActionListener(e -> XuatHoaDon.xuatDanhSach(this, dsHoaDon));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        p.add(nutLap);
        p.add(nutSua);
        p.add(nutThu);
        p.add(nutHuy);
        p.add(nutIn);
        p.add(nutXuat);
        p.add(nutLamMoi);
        return p;
    }

    private static JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        return l;
    }

    private static JLabel giaTri() {
        JLabel l = new JLabel("—");
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        return l;
    }

    private static JTextArea vanBan() {
        JTextArea a = new JTextArea(2, 16);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(font(Font.PLAIN, 13));
        a.setForeground(CHU_CHINH);
        a.setBackground(NEN_THE);
        a.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return a;
    }

    private static JPanel o(String nhan, JComponent giaTri) {
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 11));
        l.setForeground(CHU_PHU);
        JPanel p = new JPanel(new BorderLayout(0, 1));
        p.setOpaque(false);
        p.add(l, BorderLayout.NORTH);
        p.add(giaTri, BorderLayout.CENTER);
        return p;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        LocalDate tuNgay;
        LocalDate denNgay;
        try {
            tuNgay = docNgay(oTuNgay.getText());
            denNgay = docNgay(oDenNgay.getText());
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
            return;
        }
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            JOptionPane.showMessageDialog(this, "Ngày bắt đầu không được lớn hơn ngày kết thúc.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maDangChon = dangChon == null ? -1 : dangChon.getMaHD();
        String trangThai = (String) cbTrangThai.getSelectedItem();
        try {
            dsHoaDon = hoaDonDao.tim(oTimKiem.getText(), trangThai, tuNgay, denNgay);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        model.setRowCount(0);
        int dongChon = -1;
        int soChua = 0, soDa = 0, soHuy = 0;
        BigDecimal tienChua = BigDecimal.ZERO, tienDa = BigDecimal.ZERO;
        for (int i = 0; i < dsHoaDon.size(); i++) {
            HoaDon hd = dsHoaDon.get(i);
            model.addRow(new Object[]{String.format("HD%03d", hd.getMaHD()), hd.getTenBenhNhan(),
                    hd.getNgayLap() == null ? "" : dinhDangNgayGio(hd.getNgayLap()),
                    tien(hd.getTongTien()), hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT(),
                    hd.getTrangThai()});
            if (hd.chuaThanhToan()) {
                soChua++;
                tienChua = tienChua.add(hd.getTongTien());
            } else if (hd.daThanhToan()) {
                soDa++;
                tienDa = tienDa.add(hd.getTongTien());
            } else {
                soHuy++;
            }
            if (hd.getMaHD() == maDangChon) dongChon = i;
        }

        lblTongSo.setText(dsHoaDon.size() + " hóa đơn");
        lblChuaThu.setText(soChua + "  ·  " + tien(tienChua) + " đ");
        lblDaThu.setText(soDa + "  ·  " + tien(tienDa) + " đ");
        lblDaHuy.setText(soHuy + " hóa đơn");

        if (dongChon >= 0) {
            bang.setRowSelectionInterval(dongChon, dongChon);
        } else {
            xoaChiTiet();
        }
    }

    private void xoaLoc() {
        oTimKiem.setText("");
        cbTrangThai.setSelectedItem(TAT_CA);
        oTuNgay.setText("");
        oDenNgay.setText("");
        taiDuLieu();
    }

    private void chonDong() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= dsHoaDon.size()) {
            xoaChiTiet();
            return;
        }
        try {
            HoaDon dayDu = hoaDonDao.findDayDu(dsHoaDon.get(dong).getMaHD());
            if (dayDu == null) {
                xoaChiTiet();
                return;
            }
            dangChon = dayDu;
            hienChiTiet(dayDu);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void hienChiTiet(HoaDon hd) {
        lblMaHD.setText(String.format("HD%03d", hd.getMaHD()));
        lblBenhNhan.setText(hd.getTenBenhNhan() == null ? "—" : hd.getTenBenhNhan());
        lblThongTin.setText((hd.getMaBenhNhanHienThi() == null ? "" : hd.getMaBenhNhanHienThi() + "  ·  ")
                + (hd.getNgayLap() == null ? "" : dinhDangNgayGio(hd.getNgayLap())));
        lblTrangThai.setText(hd.getTrangThai());
        lblTrangThai.setForeground(mauTrangThai(hd.getTrangThai()));

        vMaPK.setText(String.format("PK%03d", hd.getMaPK()));
        vNgayKham.setText(hd.getNgayKham() == null ? "—" : dinhDangNgay(hd.getNgayKham()));
        vPhong.setText(hd.getTenPhong() == null ? "—" : hd.getTenPhong());
        vSoThuTu.setText(String.valueOf(hd.getSoThuTu()));
        vTamTinh.setText(tien(hd.getTamTinh()) + " đ");
        vGiamGia.setText("- " + tien(hd.getGiamGia()) + " đ");
        vTongTien.setText(tien(hd.getTongTien()) + " đ");
        vTongTien.setForeground(hd.chuaThanhToan() ? Color.decode("#B45309") : MAU_CHINH_DAM);
        vPhuongThuc.setText(hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT());
        vNgayLap.setText(hd.getNgayLap() == null ? "—" : dinhDangNgayGio(hd.getNgayLap()));
        vNguoiLap.setText(hd.getTenNguoiLap() == null ? "—" : hd.getTenNguoiLap());
        vNgayThanhToan.setText(hd.getNgayThanhToan() == null ? "—" : dinhDangNgayGio(hd.getNgayThanhToan()));
        vKhachDua.setText(hd.getTienKhachDua() == null ? "—" : tien(hd.getTienKhachDua()) + " đ");
        vTienThua.setText(hd.getTienThua() == null ? "—" : tien(hd.getTienThua()) + " đ");

        StringBuilder ghiChu = new StringBuilder();
        if (hd.getGhiChu() != null && !hd.getGhiChu().isBlank()) ghiChu.append(hd.getGhiChu());
        if (hd.getLyDoHuy() != null && !hd.getLyDoHuy().isBlank()) {
            if (ghiChu.length() > 0) ghiChu.append("\n");
            ghiChu.append("Lý do hủy: ").append(hd.getLyDoHuy());
        }
        vGhiChu.setText(ghiChu.toString());
        vGhiChu.setCaretPosition(0);

        modelChiTiet.setRowCount(0);
        for (ChiTietHoaDon ct : hd.getChiTiet()) {
            modelChiTiet.addRow(new Object[]{ct.getTenDichVu(), ct.getSoLuong(),
                    tien(ct.getDonGia()), tien(ct.getThanhTien())});
        }
    }

    private void xoaChiTiet() {
        dangChon = null;
        lblMaHD.setText("Chưa chọn hóa đơn");
        lblBenhNhan.setText(" ");
        lblThongTin.setText(" ");
        lblTrangThai.setText(" ");
        for (JLabel l : new JLabel[]{vMaPK, vNgayKham, vPhong, vSoThuTu, vTamTinh, vGiamGia, vTongTien,
                vPhuongThuc, vNgayLap, vNguoiLap, vNgayThanhToan, vKhachDua, vTienThua}) {
            l.setText("—");
        }
        vTongTien.setForeground(CHU_CHINH);
        vGhiChu.setText("");
        modelChiTiet.setRowCount(0);
    }

    // ===================== Thao tác =====================

    private void lapHoaDon() {
        List<PhieuKham> dsPhieu;
        try {
            dsPhieu = hoaDonDao.findPhieuChuaLapHoaDon();
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }
        if (dsPhieu.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không còn phiếu khám nào chưa lập hóa đơn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] luaChon = new Object[dsPhieu.size()];
        for (int i = 0; i < dsPhieu.size(); i++) {
            PhieuKham pk = dsPhieu.get(i);
            luaChon[i] = String.format("PK%03d  ·  %s  ·  %s  ·  %s", pk.getMaPK(),
                    pk.getTenBenhNhan() == null ? "—" : pk.getTenBenhNhan(),
                    pk.getNgayKham() == null ? "—" : dinhDangNgay(pk.getNgayKham()),
                    pk.getTenPhong() == null ? "Chưa phân phòng" : pk.getTenPhong());
        }
        Object chon = JOptionPane.showInputDialog(this,
                "Chọn phiếu khám cần lập hóa đơn:", "Lập hóa đơn",
                JOptionPane.PLAIN_MESSAGE, null, luaChon, luaChon[0]);
        if (chon == null) return;
        int viTri = java.util.Arrays.asList(luaChon).indexOf(chon);
        if (viTri < 0) return;
        if (HoaDonDialog.taoMoi(this, taiKhoan, dsPhieu.get(viTri))) {
            taiDuLieu();
        }
    }

    private boolean yeuCauChonHoaDon() {
        if (dangChon != null) return true;
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong danh sách.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        return false;
    }

    private void suaHoaDon() {
        if (!yeuCauChonHoaDon()) return;
        if (!dangChon.chuaThanhToan()) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ sửa được hóa đơn chưa thanh toán. Hóa đơn này đang ở trạng thái \""
                            + dangChon.getTrangThai() + "\".",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (HoaDonDialog.sua(this, dangChon)) {
            taiDuLieu();
        }
    }

    private void thuTien() {
        if (!yeuCauChonHoaDon()) return;
        if (!dangChon.chuaThanhToan()) {
            JOptionPane.showMessageDialog(this,
                    "Hóa đơn này không còn ở trạng thái chưa thanh toán.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (ThanhToanDialog.hienThi(this, dangChon)) {
            taiDuLieu();
        }
    }

    private void huyHoaDon() {
        if (!yeuCauChonHoaDon()) return;
        if (!dangChon.chuaThanhToan()) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ hủy được hóa đơn chưa thanh toán.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String lyDo = JOptionPane.showInputDialog(this,
                "Lý do hủy hóa đơn " + String.format("HD%03d", dangChon.getMaHD()) + ":",
                "Hủy hóa đơn", JOptionPane.WARNING_MESSAGE);
        if (lyDo == null) return;
        if (lyDo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập lý do hủy hóa đơn.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            hoaDonDao.huy(dangChon.getMaHD(), lyDo);
            thongBao(this, "Đã hủy hóa đơn " + String.format("HD%03d", dangChon.getMaHD()) + ".");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void inHoaDon() {
        if (!yeuCauChonHoaDon()) return;
        try {
            HoaDon dayDu = hoaDonDao.findDayDu(dangChon.getMaHD());
            if (dayDu == null) {
                throw new IllegalArgumentException("Hóa đơn không còn tồn tại, vui lòng làm mới danh sách.");
            }
            XuatHoaDon.xuat(this, dayDu);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static Color mauTrangThai(String trangThai) {
        if (HoaDon.DA_THANH_TOAN.equals(trangThai)) return Color.decode("#047857");
        if (HoaDon.DA_HUY.equals(trangThai)) return Color.decode("#B91C1C");
        return Color.decode("#B45309");
    }

    private static final class TrangThaiRenderer extends DefaultTableCellRenderer {
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
