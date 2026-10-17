package UI.Chung;

import DAO.BaoCaoDuLieuDAO;
import DAO.BaoCaoDuLieuDAO.DichVu;
import DAO.BaoCaoDuLieuDAO.DuLieu;
import DAO.BaoCaoDuLieuDAO.Thang;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.Scrollable;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * GIAO DIỆN BÁO CÁO DÙNG CHUNG cho Quản lý và Quản trị viên.
 *
 * - Cùng bộ màu, KPI, biểu đồ, bảng, bộ lọc và xuất CSV.
 * - Dùng DAO đọc dữ liệu SQL Server thật, không có số liệu mẫu cứng.
 * - Admin có thêm báo cáo Vai trò và Nhật ký; Quản lý không thấy hai mục đó.
 * - Không có bộ lọc chi nhánh vì ERD hiện chưa có mã chi nhánh.
 */
public class BaoCaoThongKePanel extends JPanel implements Scrollable {

    // 1. Màu sắc hệ thống MediDent (tương thích GiaoDien.java).
    private static final Color XANH = new Color(0x0284C7);
    private static final Color XANH_NHAT = new Color(0xE0F2FE);
    private static final Color XANH_LA = new Color(0x10B981);
    private static final Color CAM = new Color(0xF59E0B);
    private static final Color TIM = new Color(0x8B5CF6);
    private static final Color CHU = new Color(0x0F172A);
    private static final Color PHU = new Color(0x64748B);
    private static final Color VIEN = new Color(0xE2E8F0);
    private static final Color NEN = new Color(0xF8FAFC);
    private static final Color[] BIEU_DO_MAU = {
        XANH, new Color(0x38BDF8), XANH_LA, CAM, TIM, new Color(0x94A3B8)
    };
    private static final NumberFormat SO = NumberFormat.getIntegerInstance(
            Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // 2. Dữ liệu và quyền truy cập màn hình.
    private final BaoCaoDuLieuDAO dao;
    private final boolean laQuanTriVien;
    private final int maTaiKhoan;
    private DuLieu duLieu;

    // 3. Các control bộ lọc: người dùng chọn thay vì nhập ngày/năm tùy ý.
    private final JComboBox<Integer> cbNam = new JComboBox<>();
    private final JComboBox<String> cbKy = new JComboBox<>();
    private final JComboBox<String> cbLoai = new JComboBox<>();
    private final JButton nutApDung = GiaoDien.nutIcon(
            "Áp dụng", true, BieuTuong.Loai.TIM_KIEM);
    private final JButton nutXuat = GiaoDien.nutIcon(
            "Xuất CSV", false, BieuTuong.Loai.TAI_XUONG);

    // 4. Những thành phần thay đổi khi truy vấn xong.
    private final JLabel nhanPhamVi = nhan("Chưa tải dữ liệu", 11, PHU, false);
    private final JLabel nhanTrangThai = nhan("Đang chuẩn bị...", 11, PHU, false);
    private final JLabel kpiDoanhThu = nhan("--", 23, CHU, true);
    private final JLabel kpiLuotKham = nhan("--", 23, CHU, true);
    private final JLabel kpiTrungBinh = nhan("--", 23, CHU, true);
    private final JLabel kpiQuayLai = nhan("--", 23, CHU, true);
    private final JLabel kpiChiPhi = nhan("--", 23, CHU, true);
    private final JLabel kpiLaiLo = nhan("--", 23, CHU, true);
    private final JLabel ghiChuLaiLo = nhan("Chi phí phải nhập đủ để tính lãi/lỗ.", 11, PHU, false);
    private final JLabel moTaBieuDo = nhan("", 11, PHU, false);
    private final JLabel moTaDichVu = nhan("", 11, PHU, false);
    private final JLabel moTaBang = nhan("", 11, PHU, false);
    private final JLabel nhanKetLuan = nhan("", 12, CHU, false);

    private final CotThang bieuDoThang = new CotThang();
    private final VongDichVu vongDichVu = new VongDichVu();
    private final JPanel danhSachDichVu = new JPanel();

    // 5. Bảng luôn đánh STT tự động 1..N, kể cả khi sắp xếp.
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);

    public BaoCaoThongKePanel(boolean laQuanTriVien) {
        this(new BaoCaoDuLieuDAO(), laQuanTriVien, -1);
    }

    public BaoCaoThongKePanel(boolean laQuanTriVien, int maTaiKhoan) {
        this(new BaoCaoDuLieuDAO(), laQuanTriVien, maTaiKhoan);
    }

    public BaoCaoThongKePanel(BaoCaoDuLieuDAO dao, boolean laQuanTriVien) {
        this(dao, laQuanTriVien, -1);
    }

    public BaoCaoThongKePanel(BaoCaoDuLieuDAO dao, boolean laQuanTriVien, int maTaiKhoan) {
        if (dao == null) throw new IllegalArgumentException("DAO không được null.");
        this.dao = dao;
        this.laQuanTriVien = laQuanTriVien;
        this.maTaiKhoan = maTaiKhoan;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(NEN);
        setBorder(new EmptyBorder(25, 24, 28, 24));
        khoiTaoBoLoc();
        taoGiaoDien();
        ganSuKien();
        taiLai();
    }

    // Khi nằm trong JScrollPane của Admin, chỉ cuộn dọc toàn trang;
    // tránh lỗi các khung bị cắt ngang hoặc bị đẩy lệch lề.
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(860, 620);
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) {
        return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
        return Math.max(80, rect.height - 50);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /** Mỗi nội dung rộng theo khu vực hiển thị, không lệch phải do BoxLayout. */
    private void themDayDu(Component component) {
        if (component instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        add(component);
    }

    private void khoiTaoBoLoc() {
        int namHienTai = LocalDate.now().getYear();
        for (int nam = namHienTai - 5; nam <= namHienTai + 1; nam++) cbNam.addItem(nam);
        cbNam.setSelectedItem(namHienTai);

        cbKy.addItem("Cả năm");
        for (int quy = 1; quy <= 4; quy++) cbKy.addItem("Quý " + quy);
        for (int thang = 1; thang <= 12; thang++) cbKy.addItem(String.format("Tháng %02d", thang));

        cbLoai.addItem("Doanh thu theo tháng");
        cbLoai.addItem("Lượt khám theo tháng");
        cbLoai.addItem("Dịch vụ điều trị");
        if (laQuanTriVien) {
            cbLoai.addItem("Tài khoản theo vai trò");
            cbLoai.addItem("Nhật ký hệ thống");
        } else {
            // Chỉ báo cáo Quản lý mới có chi phí / lãi-lỗ.
            cbLoai.addItem("Lãi/lỗ theo tháng");
            cbLoai.addItem("Chi phí theo tháng");
        }
        for (JComboBox<?> combo : new JComboBox<?>[] {cbNam, cbKy, cbLoai}) {
            combo.setBackground(Color.WHITE);
            combo.setForeground(CHU);
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            combo.setBorder(new LineBorder(VIEN, 1, true));
        }
    }

    private void taoGiaoDien() {
        // PHẦN A: Breadcrumb / tiêu đề / xuất báo cáo.
        JPanel dauTrang = trongSuot(new BorderLayout(12, 0));
        JPanel cotTieuDe = trongSuot();
        cotTieuDe.setLayout(new BoxLayout(cotTieuDe, BoxLayout.Y_AXIS));
        cotTieuDe.add(nhan("Tổng quan   ›   Báo cáo - thống kê", 11, PHU, false));
        cotTieuDe.add(Box.createVerticalStrut(12));
        cotTieuDe.add(nhan("Báo cáo - thống kê", 25, CHU, true));
        cotTieuDe.add(Box.createVerticalStrut(6));
        cotTieuDe.add(nhan("Doanh thu, lượt khám và hiệu quả hoạt động theo dữ liệu SQL Server.",
                12, PHU, false));
        dauTrang.add(cotTieuDe, BorderLayout.CENTER);
        dauTrang.add(nutXuat, BorderLayout.EAST);
        themDayDu(dauTrang);
        themDayDu(Box.createVerticalStrut(21));

        // PHẦN B: Bộ lọc và phạm vi áp dụng.
        JPanel khoangLoc = the(new BorderLayout(0, 13), 16);
        khoangLoc.add(nhan("Bộ lọc báo cáo", 14, CHU, true), BorderLayout.NORTH);
        JPanel hangLoc = trongSuot(new FlowLayout(FlowLayout.LEFT, 10, 3));
        hangLoc.add(tieuChi("Năm", cbNam, 96));
        hangLoc.add(tieuChi("Khoảng thời gian", cbKy, 148));
        hangLoc.add(tieuChi("Loại báo cáo", cbLoai, 218));
        hangLoc.add(nutApDung);
        if (!laQuanTriVien && maTaiKhoan > 0) {
            JButton ghiChi = GiaoDien.nutIcon(
                    "Ghi chi phí", false, BieuTuong.Loai.THEM);
            ghiChi.addActionListener(e -> moFormChiPhi());
            hangLoc.add(ghiChi);
        }
        khoangLoc.add(hangLoc, BorderLayout.CENTER);
        JPanel phamVi = trongSuot(new BorderLayout());
        phamVi.add(nhanPhamVi, BorderLayout.WEST);
        phamVi.add(nhanTrangThai, BorderLayout.EAST);
        khoangLoc.add(phamVi, BorderLayout.SOUTH);
        themDayDu(khoangLoc);
        themDayDu(Box.createVerticalStrut(15));

        // PHẦN C: Quản lý có thêm 2 thẻ Chi phí - Lãi/lỗ (Admin bị giới hạn).
        int soHangKpi = laQuanTriVien ? 2 : 3;
        int caoKpi = laQuanTriVien ? 242 : 355;
        JPanel cacKpi = trongSuot(new GridLayout(soHangKpi, 2, 13, 13));
        cacKpi.setPreferredSize(new Dimension(760, caoKpi));
        cacKpi.setMaximumSize(new Dimension(Integer.MAX_VALUE, caoKpi));
        cacKpi.add(theThongKe("Doanh thu đã thanh toán", kpiDoanhThu,
                "Từ các giao dịch thành công", XANH, BieuTuong.Loai.BAO_CAO));
        cacKpi.add(theThongKe("Tổng lượt khám", kpiLuotKham,
                "Theo các phiếu khám", XANH_LA, BieuTuong.Loai.TAI_KHOAN));
        cacKpi.add(theThongKe("Doanh thu / lượt khám", kpiTrungBinh,
                "Bình quân trong kỳ", CAM, BieuTuong.Loai.KIEM_TRA));
        cacKpi.add(theThongKe("Bệnh nhân khám từ 2 lần", kpiQuayLai,
                "Tỷ lệ theo bệnh nhân duy nhất", TIM, BieuTuong.Loai.PHAN_QUYEN));
        if (!laQuanTriVien) {
            cacKpi.add(theThongKe("Chi phí đã ghi nhận", kpiChiPhi,
                    "Chỉ những khoản chi đã nhập SQL", CAM, BieuTuong.Loai.BAO_CAO));
            cacKpi.add(theThongKe("Lãi/lỗ tạm tính", kpiLaiLo,
                    "Tiền thực thu - chi phí đã ghi", XANH_LA, BieuTuong.Loai.KIEM_TRA));
        }
        themDayDu(cacKpi);
        if (!laQuanTriVien) {
            themDayDu(Box.createVerticalStrut(7));
            themDayDu(ghiChuLaiLo);
        }
        themDayDu(Box.createVerticalStrut(16));

        // PHẦN D: Biểu đồ tháng và cơ cấu dịch vụ trên cùng một hàng.
        JPanel haiBieuDo = trongSuot(new GridLayout(1, 2, 14, 0));
        haiBieuDo.setPreferredSize(new Dimension(760, 358));
        haiBieuDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 358));

        JPanel khungCot = the(new BorderLayout(0, 10), 14);
        JPanel dauCot = trongSuot();
        dauCot.setLayout(new BoxLayout(dauCot, BoxLayout.Y_AXIS));
        dauCot.add(nhan("Xu hướng theo tháng", 14, CHU, true));
        dauCot.add(Box.createVerticalStrut(4));
        dauCot.add(moTaBieuDo);
        khungCot.add(dauCot, BorderLayout.NORTH);
        khungCot.add(bieuDoThang, BorderLayout.CENTER);
        haiBieuDo.add(khungCot);

        JPanel khungDichVu = the(new BorderLayout(0, 8), 14);
        JPanel dauDichVu = trongSuot();
        dauDichVu.setLayout(new BoxLayout(dauDichVu, BoxLayout.Y_AXIS));
        dauDichVu.add(nhan("Cơ cấu dịch vụ điều trị", 14, CHU, true));
        dauDichVu.add(Box.createVerticalStrut(4));
        dauDichVu.add(moTaDichVu);
        khungDichVu.add(dauDichVu, BorderLayout.NORTH);
        vongDichVu.setPreferredSize(new Dimension(150, 150));
        khungDichVu.add(vongDichVu, BorderLayout.CENTER);
        danhSachDichVu.setOpaque(false);
        danhSachDichVu.setLayout(new BoxLayout(danhSachDichVu, BoxLayout.Y_AXIS));
        khungDichVu.add(danhSachDichVu, BorderLayout.SOUTH);
        haiBieuDo.add(khungDichVu);
        themDayDu(haiBieuDo);
        themDayDu(Box.createVerticalStrut(16));

        // PHẦN E: Bảng báo cáo chi tiết, STT 1..N và sắp xếp theo tiêu đề.
        JPanel bangCard = the(new BorderLayout(0, 10), 15);
        JPanel dauBang = trongSuot(new BorderLayout());
        dauBang.add(nhan("Chi tiết báo cáo", 14, CHU, true), BorderLayout.WEST);
        dauBang.add(moTaBang, BorderLayout.EAST);
        bangCard.add(dauBang, BorderLayout.NORTH);
        bang.setRowHeight(37);
        bang.setAutoCreateRowSorter(true);
        bang.setFillsViewportHeight(true);
        bang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bang.setSelectionBackground(XANH_NHAT);
        bang.setGridColor(VIEN);
        bang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        bang.getTableHeader().setBackground(new Color(0xF1F5F9));
        bang.getTableHeader().setReorderingAllowed(false);
        bangCard.add(new JScrollPane(bang), BorderLayout.CENTER);
        bangCard.setPreferredSize(new Dimension(760, 330));
        bangCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        themDayDu(bangCard);
        themDayDu(Box.createVerticalStrut(16));

        // PHẦN F: Diễn giải có căn cứ vào số liệu thật.
        JPanel tongKet = the(new BorderLayout(0, 9), 15);
        tongKet.setBackground(new Color(0xEFF8FF));
        tongKet.add(nhan("Nhận xét từ số liệu hiện tại", 14, CHU, true), BorderLayout.NORTH);
        tongKet.add(nhanKetLuan, BorderLayout.CENTER);
        themDayDu(tongKet);
    }

    /** Tạo nhóm tên control bên trên, JComboBox bên dưới. */
    private JPanel tieuChi(String tieuDe, JComboBox<?> combo, int chieuRong) {
        JPanel nhom = trongSuot();
        nhom.setLayout(new BoxLayout(nhom, BoxLayout.Y_AXIS));
        nhom.add(nhan(tieuDe, 10, PHU, true));
        nhom.add(Box.createVerticalStrut(4));
        combo.setPreferredSize(new Dimension(chieuRong, 35));
        combo.setMaximumSize(new Dimension(chieuRong, 35));
        nhom.add(combo);
        return nhom;
    }

    private JPanel theThongKe(String tieuDe, JLabel so, String ghiChu,
                              Color mau, BieuTuong.Loai icon) {
        JPanel card = the(new BorderLayout(10, 5), 15);
        JPanel text = trongSuot();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(nhan(tieuDe, 11, PHU, false));
        text.add(Box.createVerticalStrut(11));
        text.add(so);
        text.add(Box.createVerticalStrut(8));
        text.add(nhan(ghiChu, 10, mau, false));
        card.add(text, BorderLayout.CENTER);
        JLabel hinh = new JLabel(new BieuTuong(icon, mau, 22), SwingConstants.CENTER);
        hinh.setOpaque(true);
        hinh.setBackground(new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 22));
        hinh.setPreferredSize(new Dimension(43, 43));
        card.add(hinh, BorderLayout.EAST);
        return card;
    }

    private static JPanel the(java.awt.LayoutManager layout, int padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(VIEN, 1, true),
                new EmptyBorder(padding, padding, padding, padding)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static JPanel trongSuot() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private static JPanel trongSuot(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    private static JLabel nhan(String text, int coChu, Color mau, boolean dam) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", dam ? Font.BOLD : Font.PLAIN, coChu));
        label.setForeground(mau);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Nhấn Áp dụng để đồng bộ mọi thống kê và biểu đồ với một kỳ. */
    private void ganSuKien() {
        nutApDung.addActionListener(e -> taiLai());
        nutXuat.addActionListener(e -> xuatCsv());

        // Đã đổi bộ lọc nhưng chưa nhấn Áp dụng: không xuất dữ liệu của kỳ cũ.
        java.awt.event.ActionListener chuaApDung = e -> {
            if (duLieu != null) {
                nhanTrangThai.setText("Bộ lọc chưa áp dụng");
                nutXuat.setEnabled(false);
            }
        };
        cbNam.addActionListener(chuaApDung);
        cbKy.addActionListener(chuaApDung);
        cbLoai.addActionListener(e -> {
            if (duLieu != null) {
                capNhatBieuDo();
                capNhatBang();
            }
        });
    }

    private LocalDate[] layKhoangNgay() {
        Integer nam = (Integer) cbNam.getSelectedItem();
        int namChon = nam == null ? LocalDate.now().getYear() : nam;
        int ky = cbKy.getSelectedIndex();
        LocalDate tuNgay;
        LocalDate denNgay;

        if (ky >= 1 && ky <= 4) {
            int thangBatDau = (ky - 1) * 3 + 1;
            tuNgay = LocalDate.of(namChon, thangBatDau, 1);
            denNgay = YearMonth.of(namChon, thangBatDau + 2).atEndOfMonth();
        } else if (ky >= 5) {
            int thang = ky - 4;
            tuNgay = LocalDate.of(namChon, thang, 1);
            denNgay = YearMonth.of(namChon, thang).atEndOfMonth();
        } else {
            tuNgay = LocalDate.of(namChon, 1, 1);
            denNgay = LocalDate.of(namChon, 12, 31);
        }
        return new LocalDate[] {tuNgay, denNgay};
    }

    /**
     * SwingWorker chạy SQL Server ngoài EDT để giao diện không bị treo.
     * Khi thất bại, không dùng lại số cũ như dữ liệu mới.
     */
    private void taiLai() {
        LocalDate[] khoang = layKhoangNgay();
        final LocalDate tuNgay = khoang[0];
        final LocalDate denNgay = khoang[1];
        nutApDung.setEnabled(false);
        nutXuat.setEnabled(false);
        nhanTrangThai.setText("Đang tải dữ liệu...");
        nhanPhamVi.setText("Từ " + tuNgay.format(NGAY) + " đến " + denNgay.format(NGAY));
        duLieu = null;
        xoaGiaTriCu();

        new SwingWorker<DuLieu, Void>() {
            @Override
            protected DuLieu doInBackground() throws Exception {
                return dao.layBaoCao(tuNgay, denNgay, laQuanTriVien);
            }

            @Override
            protected void done() {
                nutApDung.setEnabled(true);
                try {
                    duLieu = get();
                    napGiaoDien(duLieu);
                    nutXuat.setEnabled(true);
                    nhanTrangThai.setText("Đã đồng bộ SQL Server");
                } catch (Exception ex) {
                    nhanTrangThai.setText("Không thể tải báo cáo");
                    Throwable goc = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(BaoCaoThongKePanel.this,
                            goc.getMessage(), "Lỗi tải dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void xoaGiaTriCu() {
        kpiDoanhThu.setText("--");
        kpiLuotKham.setText("--");
        kpiTrungBinh.setText("--");
        kpiQuayLai.setText("--");
        kpiChiPhi.setText("--");
        kpiLaiLo.setText("--");
        ghiChuLaiLo.setText("Đang kiểm tra khoản chi...");
        moTaBang.setText("");
        model.setRowCount(0);
        bieuDoThang.gan(new ArrayList<Thang>(), false);
        vongDichVu.gan(new ArrayList<DichVu>());
        danhSachDichVu.removeAll();
        nhanKetLuan.setText("Đang tổng hợp dữ liệu...");
    }

    private void napGiaoDien(DuLieu dl) {
        kpiDoanhThu.setText(tien(dl.doanhThu));
        kpiLuotKham.setText(SO.format(dl.luotKham));
        BigDecimal trungBinh = dl.luotKham == 0 ? BigDecimal.ZERO
                : dl.doanhThu.divide(BigDecimal.valueOf(dl.luotKham), 0, RoundingMode.HALF_UP);
        kpiTrungBinh.setText(tien(trungBinh));
        kpiQuayLai.setText(String.format(Locale.forLanguageTag("vi-VN"),
                "%.1f%%", dl.tyLeQuayLai()));
        if (!laQuanTriVien) {
            if (dl.soKhoanChi == 0) {
                // Chưa nhập chi phí: KHÔNG báo lợi nhuận = doanh thu.
                kpiChiPhi.setText("Chưa ghi");
                kpiLaiLo.setText("Chưa đủ dữ liệu");
                kpiLaiLo.setFont(new Font("Segoe UI", Font.BOLD, 15));
                ghiChuLaiLo.setText("Chưa có khoản chi trong kỳ. Không thể xác định lãi/lỗ thực tế.");
            } else {
                kpiChiPhi.setText(tien(dl.chiPhi));
                kpiLaiLo.setFont(new Font("Segoe UI", Font.BOLD, 23));
                kpiLaiLo.setText(tien(dl.laiLoTamTinh()));
                kpiLaiLo.setForeground(dl.laiLoTamTinh().signum() < 0
                        ? new Color(0xEF4444) : XANH_LA);
                ghiChuLaiLo.setText("Lãi/lỗ tạm tính từ " + dl.soKhoanChi
                        + " khoản chi đã ghi; chưa bao gồm chi phí chưa nhập.");
            }
        }

        moTaDichVu.setText("TOP 6 dịch vụ theo lượt thực hiện, không phải lượt khám");
        vongDichVu.gan(dl.dichVu);
        capNhatChuThichDichVu(dl.dichVu);
        capNhatBieuDo();
        capNhatBang();
        nhanKetLuan.setText(String.format(
                "<html>Trong kỳ có <b>%s lượt khám</b> của <b>%s bệnh nhân</b>; " +
                "<b>%s bệnh nhân</b> khám từ 2 lần trở lên (%.1f%%). " +
                "Doanh thu đã thu đạt <b>%s</b>.</html>",
                SO.format(dl.luotKham), SO.format(dl.soBenhNhan),
                SO.format(dl.benhNhanQuayLai), dl.tyLeQuayLai(), tien(dl.doanhThu)));
        if (!laQuanTriVien) {
            String loiNhuanText = dl.soKhoanChi == 0
                    ? "Chưa đủ chi phí để tính lãi/lỗ."
                    : "Chênh lệch thu - chi đã ghi: <b>" + tien(dl.laiLoTamTinh()) + "</b>.";
            nhanKetLuan.setText(String.format(
                    "<html>Có <b>%s lượt khám</b>, doanh thu đã thu <b>%s</b>, "
                  + "chi phí đã ghi <b>%s</b>. %s</html>",
                    SO.format(dl.luotKham), tien(dl.doanhThu),
                    dl.soKhoanChi == 0 ? "chưa có" : tien(dl.chiPhi), loiNhuanText));
        }
    }

    private void capNhatBieuDo() {
        if (duLieu == null) return;
        boolean xemLuotKham = cbLoai.getSelectedIndex() == 1;
        moTaBieuDo.setText(xemLuotKham ? "Số lượt khám mỗi tháng trong kỳ"
                : (!laQuanTriVien && cbLoai.getSelectedIndex() >= 3
                    ? "Biểu đồ doanh thu; chi phí/lãi-lỗ xem tại KPI và bảng"
                    : "Doanh thu đã thanh toán theo tháng (đơn vị: triệu đồng)"));
        bieuDoThang.gan(duLieu.theoThang, xemLuotKham);
    }

    private void capNhatChuThichDichVu(List<DichVu> danhSach) {
        danhSachDichVu.removeAll();
        int tong = 0;
        for (DichVu dv : danhSach) tong += dv.soLuot;
        if (danhSach.isEmpty()) {
            danhSachDichVu.add(nhan("Chưa có chi tiết dịch vụ trong kỳ.", 11, PHU, false));
        } else {
            for (int i = 0; i < danhSach.size(); i++) {
                DichVu dichVu = danhSach.get(i);
                Color mau = BIEU_DO_MAU[i % BIEU_DO_MAU.length];
                JPanel dong = trongSuot(new BorderLayout(7, 0));
                JLabel ten = nhan(dichVu.ten, 10, PHU, false);
                ten.setIcon(new BieuTuong(BieuTuong.Loai.KIEM_TRA, mau, 12));
                dong.add(ten, BorderLayout.CENTER);
                dong.add(nhan(SO.format(dichVu.soLuot), 10, CHU, true), BorderLayout.EAST);
                dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
                danhSachDichVu.add(dong);
            }
        }
        danhSachDichVu.revalidate();
        danhSachDichVu.repaint();
    }

    private void capNhatBang() {
        if (duLieu == null) return;
        int luaChon = cbLoai.getSelectedIndex();
        List<String[]> dong = new ArrayList<>();
        String[] cot;

        if (luaChon == 0 || luaChon == 1) {
            cot = new String[] {"STT", "Tháng", luaChon == 0 ? "Doanh thu (đ)" : "Lượt khám"};
            for (Thang thang : duLieu.theoThang) {
                dong.add(new String[] {
                    String.format("%02d/%d", thang.thang.getMonthValue(), thang.thang.getYear()),
                    luaChon == 0 ? SO.format(thang.doanhThu) : SO.format(thang.luotKham)
                });
            }
        } else if (luaChon == 2) {
            cot = new String[] {"STT", "Dịch vụ điều trị", "Lượt thực hiện"};
            for (DichVu dv : duLieu.dichVu) {
                dong.add(new String[] {dv.ten, SO.format(dv.soLuot)});
            }
        } else if (!laQuanTriVien && luaChon == 3) {
            cot = new String[] {"STT", "Tháng", "Doanh thu (đ)", "Chi phí đã ghi (đ)",
                    "Lãi/lỗ tạm tính (đ)"};
            for (Thang thang : duLieu.theoThang) {
                dong.add(new String[] {
                    String.format("%02d/%d", thang.thang.getMonthValue(), thang.thang.getYear()),
                    SO.format(thang.doanhThu),
                    duLieu.soKhoanChi == 0 ? "Chưa ghi" : SO.format(thang.chiPhi),
                    duLieu.soKhoanChi == 0 ? "Chưa đủ dữ liệu" : SO.format(thang.laiLoTamTinh())
                });
            }
        } else if (!laQuanTriVien && luaChon == 4) {
            cot = new String[] {"STT", "Tháng", "Chi phí đã ghi (đ)"};
            for (Thang thang : duLieu.theoThang) {
                dong.add(new String[] {
                    String.format("%02d/%d", thang.thang.getMonthValue(), thang.thang.getYear()),
                    duLieu.soKhoanChi == 0 ? "Chưa ghi" : SO.format(thang.chiPhi)
                });
            }
        } else if (laQuanTriVien && luaChon == 3) {
            // Đây là snapshot hiện tại, không phải số tài khoản tạo trong kỳ.
            cot = new String[] {"STT", "Vai trò", "Số tài khoản hiện tại"};
            dong.addAll(duLieu.vaiTro);
        } else if (laQuanTriVien && luaChon == 4) {
            cot = new String[] {"STT", "Thời điểm", "Hành động", "Chi tiết"};
            dong.addAll(duLieu.nhatKy);
        } else {
            return;
        }

        model.setRowCount(0);
        model.setColumnIdentifiers(cot);
        int stt = 1;
        for (String[] duLieuDong : dong) {
            Object[] mang = new Object[duLieuDong.length + 1];
            mang[0] = stt++;
            System.arraycopy(duLieuDong, 0, mang, 1, duLieuDong.length);
            model.addRow(mang);
        }
        GiaoDien.cotSoThuTu(bang);
        bang.setAutoResizeMode(cot.length >= 4
                ? JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_ALL_COLUMNS);
        if (cot.length >= 4) {
            bang.getColumnModel().getColumn(1).setPreferredWidth(140);
            bang.getColumnModel().getColumn(2).setPreferredWidth(190);
            bang.getColumnModel().getColumn(3).setPreferredWidth(190);
            if (cot.length >= 5) {
                bang.getColumnModel().getColumn(4).setPreferredWidth(195);
            }
        }
        moTaBang.setText(dong.size() + " dòng  |  " + cbLoai.getSelectedItem());
    }

    /**
     * Quản lý ghi chi phí thực tế. Mã và thời điểm tạo tự sinh ở SQL Server.
     * Người nhập không được phép tự chọn tài khoản/ID hay sửa ngày ghi hệ thống.
     */
    private void moFormChiPhi() {
        if (laQuanTriVien || maTaiKhoan <= 0) return;
        JComboBox<String> nhom = new JComboBox<>(new String[] {
                "Vật tư", "Nhân sự", "Vận hành", "Thuê mặt bằng", "Khác"});
        JSpinner ngay = new JSpinner(new javax.swing.SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(ngay, "dd/MM/yyyy");
        dateEditor.getFormat().setLenient(false); // 30/02 không được tự sửa thành tháng 03.
        ngay.setEditor(dateEditor);
        JSpinner soTien = new JSpinner(new javax.swing.SpinnerNumberModel(
                100_000L, 1L, 1_000_000_000_000L, 10_000L));
        javax.swing.JTextField noiDung = new javax.swing.JTextField(26);

        JPanel form = new JPanel(new java.awt.GridLayout(0, 1, 5, 5));
        form.add(nhan("Ngày chi (mặc định hôm nay)", 11, CHU, true));
        form.add(ngay);
        form.add(nhan("Nhóm chi phí", 11, CHU, true));
        form.add(nhom);
        form.add(nhan("Số tiền (VND)", 11, CHU, true));
        form.add(soTien);
        form.add(nhan("Mô tả khoản chi *", 11, CHU, true));
        form.add(noiDung);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (JOptionPane.showConfirmDialog(this, form,
                "Ghi chi phí thực tế", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            // Đồng bộ chữ người dùng vừa gõ với model; chặn ngày/số tiền sai trước khi lưu.
            ngay.commitEdit();
            soTien.commitEdit();
            java.util.Date selected = (java.util.Date) ngay.getValue();
            LocalDate date = selected.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            BigDecimal value = BigDecimal.valueOf(((Number) soTien.getValue()).longValue());
            dao.ghiChiPhi(date, String.valueOf(nhom.getSelectedItem()),
                    noiDung.getText(), value, maTaiKhoan);
            JOptionPane.showMessageDialog(this,
                    "Đã lưu chi phí thực tế vào SQL Server. Báo cáo sẽ được cập nhật.");
            taiLai();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Không thể ghi chi phí", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Xuất phạm vi + KPI + bảng theo đúng thứ tự đang hiển thị. */
    private void xuatCsv() {
        if (duLieu == null) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("MediDent_BaoCao_" +
                duLieu.tuNgay + "_" + duLieu.denNgay + ".csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (file.exists() && JOptionPane.showConfirmDialog(this,
                "Ghi đè file hiện có?", "Xác nhận", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        StringBuilder csv = new StringBuilder("\uFEFF");
        dongCsv(csv, "Báo cáo", String.valueOf(cbLoai.getSelectedItem()));
        dongCsv(csv, "Từ ngày", duLieu.tuNgay.format(NGAY));
        dongCsv(csv, "Đến ngày", duLieu.denNgay.format(NGAY));
        dongCsv(csv, "Doanh thu đã thu (VND)", duLieu.doanhThu.toPlainString());
        if (!laQuanTriVien) {
            dongCsv(csv, "Số khoản chi đã nhập", String.valueOf(duLieu.soKhoanChi));
            dongCsv(csv, "Chi phí đã ghi nhận (VND)", duLieu.soKhoanChi == 0
                    ? "Chưa có dữ liệu" : duLieu.chiPhi.toPlainString());
            dongCsv(csv, "Lãi/lỗ tạm tính (VND)", duLieu.soKhoanChi == 0
                    ? "Chưa đủ dữ liệu" : duLieu.laiLoTamTinh().toPlainString());
            dongCsv(csv, "Ghi chú", "Lãi/lỗ tạm tính = thực thu - chi phí đã nhập, không phải lợi nhuận kế toán đầy đủ.");
        }
        dongCsv(csv, "Lượt khám", String.valueOf(duLieu.luotKham));
        dongCsv(csv, "Bệnh nhân quay lại", String.valueOf(duLieu.benhNhanQuayLai));
        if (cbLoai.getSelectedIndex() == 3 && laQuanTriVien) {
            dongCsv(csv, "Ghi chú", "Phân bố vai trò là số liệu hiện tại, không lọc theo ngày");
        }
        csv.append('\n');

        for (int col = 0; col < bang.getColumnCount(); col++) {
            if (col > 0) csv.append(',');
            csv.append(oCsv(bang.getColumnName(col)));
        }
        csv.append('\n');

        // STT luôn tính từ vị trí sau khi sort, không dùng ID khóa chính.
        for (int row = 0; row < bang.getRowCount(); row++) {
            for (int col = 0; col < bang.getColumnCount(); col++) {
                if (col > 0) csv.append(',');
                Object value = col == 0 ? row + 1 : bang.getValueAt(row, col);
                csv.append(oCsv(String.valueOf(value == null ? "" : value)));
            }
            csv.append('\n');
        }

        try {
            Files.write(file.toPath(), csv.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất CSV: " + file.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Lỗi xuất CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void dongCsv(StringBuilder csv, String cot1, String cot2) {
        csv.append(oCsv(cot1)).append(',').append(oCsv(cot2)).append('\n');
    }

    /** Chặn Excel diễn giải nội dung nhập từ DB thành công thức CSV. */
    private static String oCsv(String text) {
        String value = text == null ? "" : text;
        String check = value.stripLeading();
        if (!check.isEmpty() && "=+-@".indexOf(check.charAt(0)) >= 0) {
            value = "'" + value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String tien(BigDecimal tien) {
        return SO.format(tien) + " đ";
    }

    /** Biểu đồ Java2D không cần ảnh/icon/font ngoài. */
    private static final class CotThang extends JPanel {
        private List<Thang> duLieu = new ArrayList<>();
        private boolean laLuotKham;

        CotThang() { setOpaque(false); }

        void gan(List<Thang> duLieu, boolean laLuotKham) {
            this.duLieu = new ArrayList<>(duLieu);
            this.laLuotKham = laLuotKham;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics original) {
            super.paintComponent(original);
            Graphics2D g = (Graphics2D) original.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int left = 45;
                int top = 12;
                int right = getWidth() - 14;
                int bottom = getHeight() - 30;
                if (right - left <= 0 || bottom - top <= 0) return;
                if (duLieu.isEmpty()) {
                    g.setColor(PHU);
                    g.drawString("Đang tải biểu đồ...", left, 32);
                    return;
                }
                double max = 1;
                for (Thang thang : duLieu) {
                    double value = laLuotKham ? thang.luotKham
                            : thang.doanhThu.doubleValue() / 1_000_000.0;
                    max = Math.max(max, value);
                }
                max *= 1.15;
                int h = bottom - top;
                for (int i = 0; i <= 4; i++) {
                    int y = bottom - (i * h / 4);
                    g.setColor(new Color(0xE2E8F0));
                    g.setStroke(new BasicStroke(1f));
                    g.drawLine(left, y, right, y);
                    g.setColor(PHU);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    String mark = String.format("%.0f", max * i / 4.0);
                    g.drawString(mark, Math.max(2, left - 7 - g.getFontMetrics().stringWidth(mark)), y + 4);
                }
                int count = duLieu.size();
                double slot = (right - left) / (double) count;
                int barWidth = Math.max(5, (int) Math.min(35, slot * .58));
                for (int i = 0; i < count; i++) {
                    Thang thang = duLieu.get(i);
                    double value = laLuotKham ? thang.luotKham
                            : thang.doanhThu.doubleValue() / 1_000_000.0;
                    int barHeight = (int) (value * h / max);
                    int x = left + (int) (slot * (i + .5)) - barWidth / 2;
                    int y = bottom - barHeight;
                    g.setColor(new Color(0x0EA5E9));
                    g.fillRoundRect(x, y, barWidth, barHeight, 7, 7);
                    if (count <= 12 || i % 2 == 0) {
                        String label = String.format("%02d", thang.thang.getMonthValue());
                        g.setColor(PHU);
                        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                        g.drawString(label, x + (barWidth - g.getFontMetrics().stringWidth(label)) / 2,
                                bottom + 17);
                    }
                }
            } finally {
                g.dispose();
            }
        }
    }

    /** Vòng tròn thể hiện TOP dịch vụ điều trị trong kỳ. */
    private static final class VongDichVu extends JPanel {
        private List<DichVu> duLieu = new ArrayList<>();

        VongDichVu() { setOpaque(false); }

        void gan(List<DichVu> duLieu) {
            this.duLieu = new ArrayList<>(duLieu);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics original) {
            super.paintComponent(original);
            Graphics2D g = (Graphics2D) original.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int tong = 0;
                for (DichVu dv : duLieu) tong += dv.soLuot;
                int size = Math.max(0, Math.min(getWidth() - 20, getHeight() - 4));
                if (size < 30) return;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                double batDau = 90;
                if (tong == 0) {
                    g.setColor(VIEN);
                    g.fill(new Ellipse2D.Double(x, y, size, size));
                } else {
                    for (int i = 0; i < duLieu.size(); i++) {
                        double goc = 360.0 * duLieu.get(i).soLuot / tong;
                        g.setColor(BIEU_DO_MAU[i % BIEU_DO_MAU.length]);
                        g.fill(new Arc2D.Double(x, y, size, size, batDau, -goc, Arc2D.PIE));
                        batDau -= goc;
                    }
                }
                int inset = size / 4;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x + inset, y + inset,
                        size - inset * 2, size - inset * 2));
                String so = SO.format(tong);
                g.setColor(CHU);
                g.setFont(new Font("Segoe UI", Font.BOLD, 17));
                g.drawString(so, (getWidth() - g.getFontMetrics().stringWidth(so)) / 2,
                        getHeight() / 2 + 1);
                g.setColor(PHU);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String caption = "lượt điều trị";
                g.drawString(caption, (getWidth() - g.getFontMetrics().stringWidth(caption)) / 2,
                        getHeight() / 2 + 18);
            } finally {
                g.dispose();
            }
        }
    }
}
