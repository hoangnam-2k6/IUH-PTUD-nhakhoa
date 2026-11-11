package UI.LeTan;

import Dao.HoaDonDao;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại thu tiền một hóa đơn: chọn phương thức, nhập tiền khách đưa,
 * tính tiền thừa trực tiếp và ghi nhận thanh toán xuống CSDL.
 */
public class ThanhToanDialog extends JDialog {
    private final HoaDonDao hoaDonDao = new HoaDonDao();
    private HoaDon hoaDon;
    private boolean daThanhToan;

    private String phuongThuc = HoaDon.TIEN_MAT;
    private final java.util.Map<String, JToggleButton> nutPhuongThuc = new java.util.LinkedHashMap<>();
    private final JTextField oKhachDua = taoOTien();
    private final JTextField oGhiChu = taoOText();
    private final JLabel lblTongTien = new JLabel();
    private final JLabel lblNhanThua = new JLabel("Tiền thừa trả khách");
    private final JLabel lblTienThua = new JLabel();
    private final JPanel theThua = new JPanel();
    private final JPanel hangGoiY = new JPanel();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);

    private ThanhToanDialog(Component cha, HoaDon hoaDon) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha),
                String.format("Thu tiền hóa đơn HD%03d", hoaDon.getMaHD()),
                ModalityType.APPLICATION_MODAL);
        this.hoaDon = hoaDon;

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);

        add(taoDau(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        napDuLieu();
        capNhatTien();

        setSize(640, 720);
        setMinimumSize(new Dimension(600, 620));
        setLocationRelativeTo(cha);
    }

    /**
     * Mở hộp thoại thu tiền. Trả về true nếu hóa đơn đã được ghi nhận thanh toán.
     */
    public static boolean hienThi(Component cha, HoaDon hoaDon) {
        if (hoaDon == null) {
            JOptionPane.showMessageDialog(cha, "Vui lòng chọn một hóa đơn cần thanh toán.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        try {
            HoaDon dayDu = new HoaDonDao().findDayDu(hoaDon.getMaHD());
            if (dayDu == null) {
                throw new IllegalArgumentException("Hóa đơn không còn tồn tại, vui lòng làm mới danh sách.");
            }
            if (!dayDu.chuaThanhToan()) {
                JOptionPane.showMessageDialog(cha,
                        "Hóa đơn " + String.format("HD%03d", dayDu.getMaHD())
                                + " đang ở trạng thái \"" + dayDu.getTrangThai() + "\", không thể thu tiền.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            ThanhToanDialog d = new ThanhToanDialog(cha, dayDu);
            d.setVisible(true);
            return d.daThanhToan;
        } catch (Exception e) {
            baoLoi(cha, e);
            return false;
        }
    }

    // ===================== Giao diện =====================

    private static final Color XANH_DAM = Color.decode("#0369A1");
    private static final Color XANH_LA_NHAT = Color.decode("#D1FAE5");
    private static final Color DO_NHAT = Color.decode("#FEE2E2");

    /** Dải tiêu đề màu: bệnh nhân, mã hóa đơn bên trái; số tiền cần thu bên phải. */
    private JComponent taoDau() {
        JPanel dau = new JPanel(new BorderLayout(16, 0));
        dau.setBackground(XANH_DAM);
        dau.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel nho = new JLabel("THU TIỀN HÓA ĐƠN");
        nho.setFont(font(Font.BOLD, 11));
        nho.setForeground(Color.decode("#BAE6FD"));

        JLabel benhNhan = new JLabel(hoaDon.getTenBenhNhan() == null ? "—" : hoaDon.getTenBenhNhan());
        benhNhan.setFont(font(Font.BOLD, 20));
        benhNhan.setForeground(Color.WHITE);

        JLabel thongTin = new JLabel(String.format("HD%03d  ·  PK%03d  ·  %s  ·  %s",
                hoaDon.getMaHD(), hoaDon.getMaPK(),
                hoaDon.getNgayKham() == null ? "—" : dinhDangNgay(hoaDon.getNgayKham()),
                hoaDon.getTenPhong() == null ? "Chưa phân phòng" : hoaDon.getTenPhong()));
        thongTin.setFont(font(Font.PLAIN, 12));
        thongTin.setForeground(Color.decode("#E0F2FE"));

        JPanel trai = new JPanel(new GridLayout(0, 1, 0, 4));
        trai.setOpaque(false);
        trai.add(nho);
        trai.add(benhNhan);
        trai.add(thongTin);
        dau.add(trai, BorderLayout.CENTER);

        JLabel nhanCanThu = new JLabel("CẦN THU", SwingConstants.RIGHT);
        nhanCanThu.setFont(font(Font.BOLD, 11));
        nhanCanThu.setForeground(Color.decode("#BAE6FD"));
        lblTongTien.setFont(font(Font.BOLD, 28));
        lblTongTien.setForeground(Color.WHITE);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongTien.setText(tien(hoaDon.getTongTien()) + " đ");
        JPanel phai = new JPanel(new GridLayout(0, 1, 0, 2));
        phai.setOpaque(false);
        phai.add(nhanCanThu);
        phai.add(lblTongTien);
        dau.add(phai, BorderLayout.EAST);
        return dau;
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

        // --- Dịch vụ ---
        noi.add(taoTieuDeNhom("Dịch vụ trong hóa đơn"));
        int[] doRong = {300, 70, 110, 120};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(1).setMaxWidth(70);
        bang.setRowHeight(30);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        cuon.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuon.setPreferredSize(new Dimension(100, 30 * 3 + 32));
        cuon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30 * 3 + 32));
        noi.add(cuon);
        noi.add(Box.createVerticalStrut(8));

        JPanel tong = new JPanel(new GridLayout(0, 1, 0, 4));
        tong.setOpaque(false);
        tong.setAlignmentX(Component.LEFT_ALIGNMENT);
        tong.add(dongTong("Tạm tính", tien(hoaDon.getTamTinh()) + " đ", CHU_CHINH));
        if (hoaDon.getGiamGia().signum() > 0) {
            String ma = hoaDon.getMaGiamCode() == null || hoaDon.getMaGiamCode().isBlank()
                    ? "" : " (" + hoaDon.getMaGiamCode() + ")";
            tong.add(dongTong("Giảm giá" + ma, "- " + tien(hoaDon.getGiamGia()) + " đ", Color.decode("#B91C1C")));
        }
        tong.setMaximumSize(new Dimension(Integer.MAX_VALUE, tong.getPreferredSize().height));
        noi.add(tong);

        // --- Phương thức: nút chọn một trong ba ---
        noi.add(taoTieuDeNhom("Phương thức thanh toán"));
        JPanel hangPT = new JPanel(new GridLayout(1, HoaDon.PHUONG_THUC.length, 8, 0));
        hangPT.setOpaque(false);
        hangPT.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangPT.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ButtonGroup nhom = new ButtonGroup();
        for (String pt : HoaDon.PHUONG_THUC) {
            JToggleButton nut = taoNutChon(pt);
            nut.addActionListener(e -> chonPhuongThuc(pt));
            nhom.add(nut);
            hangPT.add(nut);
            nutPhuongThuc.put(pt, nut);
        }
        noi.add(hangPT);

        // --- Tiền khách đưa + gợi ý nhanh ---
        noi.add(taoTieuDeNhom("Tiền khách đưa (VNĐ)"));
        oKhachDua.setFont(font(Font.BOLD, 18));
        oKhachDua.setPreferredSize(new Dimension(100, 42));
        oKhachDua.setAlignmentX(Component.LEFT_ALIGNMENT);
        oKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        oKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { capNhatTien(); }
            @Override public void removeUpdate(DocumentEvent e) { capNhatTien(); }
            @Override public void changedUpdate(DocumentEvent e) { capNhatTien(); }
        });
        noi.add(oKhachDua);
        noi.add(Box.createVerticalStrut(8));

        hangGoiY.setOpaque(false);
        hangGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangGoiY.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        for (BigDecimal soTien : goiYTien(hoaDon.getTongTien())) {
            boolean duTien = soTien.compareTo(hoaDon.getTongTien()) == 0;
            JButton nut = taoNutNho(duTien ? "Vừa đủ" : tien(soTien));
            nut.addActionListener(e -> oKhachDua.setText(tien(soTien)));
            JPanel boc = new JPanel(new BorderLayout());
            boc.setOpaque(false);
            boc.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            boc.add(nut);
            hangGoiY.add(boc);
        }
        hangGoiY.setMaximumSize(new Dimension(Integer.MAX_VALUE, hangGoiY.getPreferredSize().height));
        noi.add(hangGoiY);
        noi.add(Box.createVerticalStrut(12));

        // --- Thẻ kết quả: tiền thừa / còn thiếu ---
        lblNhanThua.setFont(font(Font.BOLD, 12));
        lblTienThua.setFont(font(Font.BOLD, 20));
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        theThua.setLayout(new BorderLayout(10, 0));
        theThua.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        theThua.setAlignmentX(Component.LEFT_ALIGNMENT);
        theThua.add(lblNhanThua, BorderLayout.WEST);
        theThua.add(lblTienThua, BorderLayout.EAST);
        theThua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        noi.add(theThua);

        // --- Ghi chú ---
        noi.add(taoTieuDeNhom("Ghi chú thanh toán (không bắt buộc)"));
        oGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        oGhiChu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        noi.add(oGhiChu);

        oKhachDua.setText(tien(hoaDon.getTongTien()));
        chonPhuongThuc(HoaDon.TIEN_MAT);

        JScrollPane cuonNoi = new JScrollPane(noi);
        cuonNoi.setBorder(null);
        cuonNoi.getVerticalScrollBar().setUnitIncrement(16);
        return cuonNoi;
    }

    private JComponent taoChan() {
        JButton huy = taoNutTrang("Hủy");
        JButton thu = taoNut("Xác nhận đã thu " + tien(hoaDon.getTongTien()) + " đ",
                THANH_CONG.darker(), Color.decode("#047857"));
        thu.setFont(font(Font.BOLD, 14));
        huy.addActionListener(e -> dispose());
        thu.addActionListener(e -> xacNhan());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        p.setBackground(NEN_APP);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(thu);
        getRootPane().setDefaultButton(thu);
        return p;
    }

    /** Một dòng tổng kết: nhãn bên trái, số tiền căn phải. */
    private static JPanel dongTong(String nhan, String giaTri, Color mau) {
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        JLabel v = new JLabel(giaTri, SwingConstants.RIGHT);
        v.setFont(font(Font.BOLD, 14));
        v.setForeground(mau);
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    /** Nút chọn dạng viên: nền xanh khi được chọn, trắng viền xám khi không. */
    private static JToggleButton taoNutChon(String chu) {
        JToggleButton nut = new JToggleButton(chu);
        nut.setFont(font(Font.BOLD, 13));
        nut.setFocusPainted(false);
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Runnable ve = () -> {
            boolean chon = nut.isSelected();
            nut.setBackground(chon ? MAU_CHINH : NEN_THE);
            nut.setForeground(chon ? Color.WHITE : CHU_CHINH);
            nut.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(chon ? MAU_CHINH_DAM : VIEN),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));
        };
        nut.addItemListener(e -> ve.run());
        ve.run();
        return nut;
    }

    /** Các mệnh giá gợi ý: đúng số tiền cần thu, rồi làm tròn lên theo tờ tiền thông dụng. */
    private static List<BigDecimal> goiYTien(BigDecimal tong) {
        List<BigDecimal> ds = new ArrayList<>();
        ds.add(tong);
        long can = tong.longValue();
        for (long menhGia : new long[]{10_000, 50_000, 100_000, 200_000, 500_000, 1_000_000}) {
            long lam = (can + menhGia - 1) / menhGia * menhGia;
            BigDecimal bd = BigDecimal.valueOf(lam);
            if (lam > can && !ds.contains(bd) && ds.size() < 5) ds.add(bd);
        }
        return ds;
    }

    private void chonPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
        JToggleButton nut = nutPhuongThuc.get(phuongThuc);
        if (nut != null) nut.setSelected(true);
        boolean tienMat = HoaDon.TIEN_MAT.equals(phuongThuc);
        oKhachDua.setEditable(tienMat);
        oKhachDua.setEnabled(tienMat);
        for (Component c : hangGoiY.getComponents()) c.setVisible(tienMat);
        if (!tienMat) {
            oKhachDua.setText(tien(hoaDon.getTongTien()));
        }
        capNhatTien();
    }

    // ===================== Dữ liệu =====================

    private void napDuLieu() {
        model.setRowCount(0);
        for (ChiTietHoaDon ct : hoaDon.getChiTiet()) {
            model.addRow(new Object[]{ct.getTenDichVu(), ct.getSoLuong(),
                    tien(ct.getDonGia()), tien(ct.getThanhTien())});
        }
    }

    private void capNhatTien() {
        BigDecimal tong = hoaDon.getTongTien();
        BigDecimal khachDua = docTien(oKhachDua.getText());
        BigDecimal thua = khachDua.subtract(tong);
        if (thua.signum() < 0) {
            lblNhanThua.setText("Còn thiếu");
            lblTienThua.setText(tien(thua.negate()) + " đ");
            lblTienThua.setForeground(Color.decode("#B91C1C"));
            lblNhanThua.setForeground(Color.decode("#B91C1C"));
            theThua.setBackground(DO_NHAT);
        } else {
            lblNhanThua.setText("Tiền thừa trả khách");
            lblTienThua.setText(tien(thua) + " đ");
            lblTienThua.setForeground(Color.decode("#047857"));
            lblNhanThua.setForeground(Color.decode("#047857"));
            theThua.setBackground(XANH_LA_NHAT);
        }
    }

    private void xacNhan() {
        BigDecimal khachDua = docTien(oKhachDua.getText());
        BigDecimal tong = hoaDon.getTongTien();

        if (khachDua.compareTo(tong) < 0) {
            JOptionPane.showMessageDialog(this,
                    "Tiền khách đưa chưa đủ. Cần tối thiểu " + tien(tong) + " đ.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            oKhachDua.requestFocusInWindow();
            return;
        }
        if (!GiaoDien.xacNhan(this, "Xác nhận đã thu " + tien(tong) + " đ của "
                        + (hoaDon.getTenBenhNhan() == null ? "bệnh nhân" : hoaDon.getTenBenhNhan())
                        + "?\nPhương thức: " + phuongThuc + ".",
                "Xác nhận thanh toán")) {
            return;
        }

        try {
            hoaDonDao.thanhToan(hoaDon.getMaHD(), phuongThuc, khachDua, oGhiChu.getText());
            daThanhToan = true;

            // Nạp lại để hộp thoại in có đủ số tiền khách đưa / tiền thừa vừa lưu.
            HoaDon sauThanhToan = hoaDonDao.findDayDu(hoaDon.getMaHD());
            if (sauThanhToan != null) hoaDon = sauThanhToan;

            Window cha = getOwner();
            int in = JOptionPane.showConfirmDialog(this,
                    "Đã ghi nhận thanh toán hóa đơn " + String.format("HD%03d", hoaDon.getMaHD()) + "."
                            + "\n\nIn hóa đơn ngay bây giờ?",
                    "Thanh toán thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (in == JOptionPane.YES_OPTION) {
                XuatHoaDon.xuat(cha, hoaDon);
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }
}
