package UI.LeTan;

import Dao.HoaDonDao;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

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

    // Bộ lọc nâng cao: bấm nút "Bộ lọc" để sổ khung lọc xuống phía trên bảng.
    // Trạng thái, từ ngày, đến ngày và từ khóa được lọc ở CSDL; các điều kiện còn lại lọc ngay trên bảng.
    private final JTextField oMaHDLoc = taoOText();
    private final JTextField oMaPKLoc = taoOText();
    private final JTextField oBenhNhanLoc = taoOText();
    private final JTextField oTienTu = taoOTien();
    private final JTextField oTienDen = taoOTien();
    private final JComboBox<String> cbPhuongThuc = new JComboBox<>(
            new String[]{TAT_CA, HoaDon.TIEN_MAT, HoaDon.CHUYEN_KHOAN, HoaDon.THE});
    private final JButton nutLoc = taoNutTrang("Bộ lọc");
    private GiaoDien.KhungTruot khungLoc;
    private BoLoc boLoc = BoLoc.RONG;
    private boolean dangDatLai;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã HĐ", "Bệnh nhân", "Ngày lập", "Tổng tiền (VNĐ)", "Phương thức", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

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
            new String[]{"Dịch vụ", "Đơn giá", "Thành tiền"}, 0) {
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

        oTimKiem.setToolTipText("Tên bệnh nhân, số điện thoại, mã hóa đơn, mã bệnh nhân hoặc mã phiếu khám (bấm Tìm kiếm hoặc Enter)");
        oTimKiem.addActionListener(e -> taiDuLieu());

        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> taiDuLieu());
        khoiTaoBoLoc();

        JPanel phaiTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiTim.setOpaque(false);
        phaiTim.add(nutLoc);
        phaiTim.add(nutTim);

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.add(nhan("Từ khóa:"), BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(phaiTim, BorderLayout.EAST);

        JPanel phanTim = new JPanel(new BorderLayout());
        phanTim.setOpaque(false);
        phanTim.add(hangTim, BorderLayout.NORTH);
        phanTim.add(khungLoc, BorderLayout.CENTER);
        dau.add(phanTim, BorderLayout.NORTH);

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
        bang.setRowSorter(sorter);
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
        int[] doRong = {215, 95, 110};
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
        JButton nutWord = taoNut("Xuất Word", Color.decode("#1D4ED8"), Color.decode("#1E40AF"));
        JButton nutXuat = taoNut("Xuất danh sách (Word)", Color.decode("#7C3AED"), Color.decode("#6D28D9"));

        nutLamMoi.addActionListener(e -> taiDuLieu());
        nutLap.addActionListener(e -> lapHoaDon());
        nutSua.addActionListener(e -> suaHoaDon());
        nutThu.addActionListener(e -> thuTien());
        nutHuy.addActionListener(e -> huyHoaDon());
        nutIn.addActionListener(e -> inHoaDon());
        nutWord.addActionListener(e -> xuatHoaDonWord());
        nutXuat.addActionListener(e -> XuatHoaDon.xuatDanhSachWord(this, danhSachHienThi()));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        p.add(nutLap);
        p.add(nutSua);
        p.add(nutThu);
        p.add(nutHuy);
        p.add(nutIn);
        p.add(nutWord);
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
        BigDecimal tienTu = docTien(oTienTu.getText());
        BigDecimal tienDen = docTien(oTienDen.getText());
        if (tienTu != null && tienDen != null && tienTu.compareTo(tienDen) > 0) {
            JOptionPane.showMessageDialog(this, "Số tiền bắt đầu không được lớn hơn số tiền kết thúc.",
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
        for (int i = 0; i < dsHoaDon.size(); i++) {
            HoaDon hd = dsHoaDon.get(i);
            model.addRow(new Object[]{String.format("HD%03d", hd.getMaHD()), hd.getTenBenhNhan(),
                    hd.getNgayLap() == null ? "" : dinhDangNgayGio(hd.getNgayLap()),
                    tien(hd.getTongTien()), hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT(),
                    hd.getTrangThai()});
            if (hd.getMaHD() == maDangChon) dongChon = i;
        }

        apDungLoc();

        // dongChon là chỉ số trong model; khi đang lọc phải đổi sang chỉ số hiển thị
        int dongHienThi = dongChon >= 0 ? bang.convertRowIndexToView(dongChon) : -1;
        if (dongHienThi >= 0) {
            bang.setRowSelectionInterval(dongHienThi, dongHienThi);
        } else {
            xoaChiTiet();
        }
    }

    /** Hóa đơn đang hiển thị trên bảng (sau khi lọc), theo thứ tự hiển thị. */
    private List<HoaDon> danhSachHienThi() {
        List<HoaDon> ds = new ArrayList<>();
        for (int i = 0; i < bang.getRowCount(); i++) {
            int dongModel = bang.convertRowIndexToModel(i);
            if (dongModel >= 0 && dongModel < dsHoaDon.size()) ds.add(dsHoaDon.get(dongModel));
        }
        return ds;
    }

    /** Thống kê theo các hóa đơn đang hiển thị. */
    private void capNhatThongKe() {
        int soChua = 0, soDa = 0, soHuy = 0;
        BigDecimal tienChua = BigDecimal.ZERO, tienDa = BigDecimal.ZERO;
        List<HoaDon> hien = danhSachHienThi();
        for (HoaDon hd : hien) {
            if (hd.chuaThanhToan()) {
                soChua++;
                tienChua = tienChua.add(hd.getTongTien());
            } else if (hd.daThanhToan()) {
                soDa++;
                tienDa = tienDa.add(hd.getTongTien());
            } else {
                soHuy++;
            }
        }
        lblTongSo.setText(hien.size() == dsHoaDon.size() ? hien.size() + " hóa đơn"
                : hien.size() + " / " + dsHoaDon.size() + " hóa đơn");
        lblChuaThu.setText(soChua + "  ·  " + tien(tienChua) + " đ");
        lblDaThu.setText(soDa + "  ·  " + tien(tienDa) + " đ");
        lblDaHuy.setText(soHuy + " hóa đơn");
    }

    // ===================== Bộ lọc =====================

    /** Dựng khung lọc (ẩn sẵn) và gắn sự kiện cho nút "Bộ lọc". */
    private void khoiTaoBoLoc() {
        for (JComboBox<String> cb : List.of(cbTrangThai, cbPhuongThuc)) {
            cb.setFont(font(Font.PLAIN, 13));
        }
        // Trạng thái đọc từ CSDL nên phải tải lại; phương thức chỉ lọc trên bảng
        cbTrangThai.addActionListener(e -> {
            if (!dangDatLai) taiDuLieu();
        });
        cbPhuongThuc.addActionListener(e -> {
            if (!dangDatLai) apDungLoc();
        });
        oMaHDLoc.setToolTipText("Nhập mã hoặc một phần mã, ví dụ HD012 hoặc 12");
        oMaPKLoc.setToolTipText("Nhập mã hoặc một phần mã, ví dụ PK012 hoặc 12");
        oBenhNhanLoc.setToolTipText("Nhập một phần tên bệnh nhân, không cần gõ dấu");
        oTienTu.setToolTipText("Tổng tiền tối thiểu (VNĐ) - để trống nếu không lọc");
        oTienDen.setToolTipText("Tổng tiền tối đa (VNĐ) - để trống nếu không lọc");
        oTuNgay.setToolTipText("Ngày lập từ dd/MM/yyyy - để trống nếu không lọc");
        oDenNgay.setToolTipText("Ngày lập đến dd/MM/yyyy - để trống nếu không lọc");
        for (JTextField o : new JTextField[]{oMaHDLoc, oMaPKLoc, oBenhNhanLoc, oTienTu, oTienDen}) {
            o.addActionListener(e -> taiDuLieu());
        }
        for (JTextField o : new JTextField[]{oTuNgay, oDenNgay}) {
            o.addActionListener(e -> taiDuLieu());
        }

        JPanel luoi = new JPanel(new GridLayout(0, 3, 16, 10));
        luoi.setOpaque(false);
        luoi.add(o("Mã hóa đơn", oMaHDLoc));
        luoi.add(o("Mã phiếu khám", oMaPKLoc));
        luoi.add(o("Bệnh nhân", oBenhNhanLoc));
        luoi.add(o("Trạng thái", cbTrangThai));
        luoi.add(o("Phương thức thanh toán", cbPhuongThuc));
        luoi.add(new JLabel());
        luoi.add(o("Tổng tiền từ (VNĐ)", oTienTu));
        luoi.add(o("Tổng tiền đến (VNĐ)", oTienDen));
        luoi.add(new JLabel());
        luoi.add(o("Ngày lập từ (dd/MM/yyyy)", oTuNgay));
        luoi.add(o("Ngày lập đến (dd/MM/yyyy)", oDenNgay));

        JButton nutXoa = taoNutTrang("Xóa lọc");
        nutXoa.addActionListener(e -> xoaLoc());
        JButton nutApDung = taoNut("Áp dụng", MAU_CHINH, MAU_CHINH_DAM);
        nutApDung.addActionListener(e -> taiDuLieu());
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
        noiDung.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        noiDung.add(the, BorderLayout.CENTER);
        khungLoc = new GiaoDien.KhungTruot(noiDung);

        nutLoc.addActionListener(e -> {
            khungLoc.datMo(!khungLoc.dangMo());
            capNhatNutLoc();
        });
        capNhatNutLoc();
    }

    /** Đọc các ô lọc trên bảng rồi lọc lại bảng và cập nhật thống kê. */
    private void apDungLoc() {
        boLoc = new BoLoc(oMaHDLoc.getText().trim(), oMaPKLoc.getText().trim(), oBenhNhanLoc.getText().trim(),
                (String) cbPhuongThuc.getSelectedItem(), docTien(oTienTu.getText()), docTien(oTienDen.getText()));
        final BoLoc dieuKien = boLoc;
        sorter.setRowFilter(dieuKien.laRong() ? null : new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                int i = e.getIdentifier();
                HoaDon hd = i >= 0 && i < dsHoaDon.size() ? dsHoaDon.get(i) : null;
                return dieuKien.khop(e::getStringValue, hd);
            }
        });
        capNhatNutLoc();
        capNhatThongKe();
    }

    /** Đọc số tiền từ ô nhập (bỏ dấu chấm/phẩy ngăn cách); rỗng hoặc không hợp lệ thì trả về null. */
    private static BigDecimal docTien(String chu) {
        if (chu == null) return null;
        String so = chu.replaceAll("[^0-9]", "");
        return so.isEmpty() ? null : new BigDecimal(so);
    }

    /** Nút hiển thị số điều kiện đang lọc và mũi tên mở/đóng; đổi màu khi đang mở hoặc đang lọc. */
    private void capNhatNutLoc() {
        int n = boLoc.soDieuKien();
        if (cbTrangThai.getSelectedItem() != null && !TAT_CA.equals(cbTrangThai.getSelectedItem())) n++;
        if (!oTuNgay.getText().isBlank()) n++;
        if (!oDenNgay.getText().isBlank()) n++;
        boolean mo = khungLoc != null && khungLoc.dangMo();
        nutLoc.setText("Bộ lọc" + (n > 0 ? " (" + n + ")" : "") + "  " + (mo ? "▲" : "▼"));
        boolean noiBat = mo || n > 0;
        nutLoc.setForeground(noiBat ? MAU_CHINH_DAM : CHU_CHINH);
        nutLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(noiBat ? MAU_CHINH : VIEN),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
    }

    /**
     * Điều kiện lọc trên bảng; các điều kiện kết hợp bằng "và".
     * Số cột theo bảng: Mã HĐ, Bệnh nhân, Ngày lập, Tổng tiền, Phương thức, Trạng thái.
     * Mã phiếu khám và tổng tiền không hiển thị đúng dạng số trên bảng nên lấy từ hóa đơn tương ứng.
     */
    private record BoLoc(String maHD, String maPK, String benhNhan, String phuongThuc,
                         BigDecimal tienTu, BigDecimal tienDen) {
        static final BoLoc RONG = new BoLoc("", "", "", TAT_CA, null, null);
        private static final int COT_MA_HD = 0, COT_BENH_NHAN = 1, COT_PHUONG_THUC = 4;

        private static boolean co(String s) {
            return s != null && !s.isBlank();
        }

        boolean laRong() {
            return soDieuKien() == 0;
        }

        int soDieuKien() {
            int n = 0;
            if (co(maHD)) n++;
            if (co(maPK)) n++;
            if (co(benhNhan)) n++;
            if (phuongThuc != null && !TAT_CA.equals(phuongThuc)) n++;
            if (tienTu != null) n++;
            if (tienDen != null) n++;
            return n;
        }

        boolean khop(IntFunction<String> giaTri, HoaDon hd) {
            // Mã: "12" hoặc "hd012" đều khớp HD012
            if (co(maHD) && !nz(giaTri.apply(COT_MA_HD)).toUpperCase().contains(maHD.trim().toUpperCase())) {
                return false;
            }
            if (co(maPK)) {
                String ma = hd == null ? "" : String.format("PK%03d", hd.getMaPK());
                if (!ma.contains(maPK.trim().toUpperCase())) return false;
            }
            if (co(benhNhan) && !boDau(giaTri.apply(COT_BENH_NHAN)).contains(boDau(benhNhan.trim()))) return false;
            if (phuongThuc != null && !TAT_CA.equals(phuongThuc) && !phuongThuc.equals(giaTri.apply(COT_PHUONG_THUC))) {
                return false;
            }
            if (tienTu != null && (hd == null || hd.getTongTien().compareTo(tienTu) < 0)) return false;
            return tienDen == null || (hd != null && hd.getTongTien().compareTo(tienDen) <= 0);
        }

        private static String nz(String s) {
            return s == null ? "" : s;
        }
    }

    /** Xóa mọi điều kiện (kể cả ô từ khóa) và hiển thị lại toàn bộ. */
    private void xoaLoc() {
        dangDatLai = true;
        try {
            oTimKiem.setText("");
            oMaHDLoc.setText("");
            oMaPKLoc.setText("");
            oBenhNhanLoc.setText("");
            oTienTu.setText("");
            oTienDen.setText("");
            cbTrangThai.setSelectedItem(TAT_CA);
            cbPhuongThuc.setSelectedItem(TAT_CA);
            oTuNgay.setText("");
            oDenNgay.setText("");
        } finally {
            dangDatLai = false;
        }
        taiDuLieu();
    }

    private void chonDong() {
        int dongHienThi = bang.getSelectedRow();
        int dong = dongHienThi < 0 ? -1 : bang.convertRowIndexToModel(dongHienThi);
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
            modelChiTiet.addRow(new Object[]{ct.getTenDichVu(),
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
        int maHD = HoaDonDialog.lapMoi(this, taiKhoan);
        if (maHD <= 0) return;

        // Tải lại danh sách và chọn sẵn hóa đơn vừa lập; nếu bộ lọc đang ẩn nó thì xóa lọc.
        HoaDon tam = new HoaDon();
        tam.setMaHD(maHD);
        dangChon = tam;
        taiDuLieu();
        if (dangChon == null || dangChon.getMaHD() != maHD) {
            HoaDon tam2 = new HoaDon();
            tam2.setMaHD(maHD);
            dangChon = tam2;
            xoaLoc();
        }
        if (dangChon == null || dangChon.getMaHD() != maHD || !dangChon.chuaThanhToan()) return;

        if (xacNhan(this, "Đã lập hóa đơn " + String.format("HD%03d", maHD) + " cho " + dangChon.getTenBenhNhan()
                + ".\nTổng cộng: " + tien(dangChon.getTongTien()) + " đ\n\nThu tiền ngay bây giờ?",
                "Lập hóa đơn thành công")) {
            thuTien();
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

    private void xuatHoaDonWord() {
        if (!yeuCauChonHoaDon()) return;
        try {
            HoaDon dayDu = hoaDonDao.findDayDu(dangChon.getMaHD());
            if (dayDu == null) {
                throw new IllegalArgumentException("Hóa đơn không còn tồn tại, vui lòng làm mới danh sách.");
            }
            XuatHoaDon.xuatWord(this, dayDu);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static Color mauTrangThai(String trangThai) {
        if (HoaDon.DA_THANH_TOAN.equals(trangThai)) return Color.decode("#047857");
        if (HoaDon.DA_HUY.equals(trangThai)) return Color.decode("#B91C1C");
        return Color.decode("#B45309");
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