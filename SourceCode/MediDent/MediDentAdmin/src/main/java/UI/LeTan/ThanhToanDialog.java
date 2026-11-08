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

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại thu tiền một hóa đơn: chọn phương thức, nhập tiền khách đưa,
 * tính tiền thừa trực tiếp và ghi nhận thanh toán xuống CSDL.
 */
public class ThanhToanDialog extends JDialog {
    private final HoaDonDao hoaDonDao = new HoaDonDao();
    private HoaDon hoaDon;
    private boolean daThanhToan;

    private final JComboBox<String> cbPhuongThuc = new JComboBox<>(HoaDon.PHUONG_THUC);
    private final JTextField oKhachDua = taoOTien();
    private final JTextField oGhiChu = taoOText();
    private final JLabel lblTongTien = new JLabel();
    private final JLabel lblTienThua = new JLabel();
    private final JLabel lblTrangThai = new JLabel();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);

    private ThanhToanDialog(Component cha, HoaDon hoaDon) {
        super(SwingUtilities.getWindowAncestor(cha),
                String.format("Thanh toán hóa đơn HD%03d", hoaDon.getMaHD()),
                ModalityType.APPLICATION_MODAL);
        this.hoaDon = hoaDon;

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);

        add(taoDau(), BorderLayout.NORTH);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        napDuLieu();
        capNhatTien();

        setSize(620, 690);
        setMinimumSize(new Dimension(560, 560));
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

    private JComponent taoDau() {
        JPanel dau = new JPanel(new BorderLayout(12, 6));
        dau.setBackground(NEN_THE);
        dau.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JLabel tieuDe = new JLabel("THU TIỀN HÓA ĐƠN");
        tieuDe.setFont(font(Font.BOLD, 19));
        tieuDe.setForeground(CHU_CHINH);

        JLabel benhNhan = new JLabel(hoaDon.getTenBenhNhan() == null ? "—" : hoaDon.getTenBenhNhan());
        benhNhan.setFont(font(Font.BOLD, 14));
        benhNhan.setForeground(MAU_CHINH_DAM);

        JLabel thongTin = new JLabel(String.format("HD%03d  ·  PK%03d  ·  Ngày khám %s  ·  %s",
                hoaDon.getMaHD(), hoaDon.getMaPK(),
                hoaDon.getNgayKham() == null ? "—" : dinhDangNgay(hoaDon.getNgayKham()),
                hoaDon.getTenPhong() == null ? "Chưa phân phòng" : hoaDon.getTenPhong()));
        thongTin.setFont(font(Font.PLAIN, 12));
        thongTin.setForeground(CHU_PHU);

        JPanel chu = new JPanel(new GridLayout(0, 1, 0, 3));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(benhNhan);
        chu.add(thongTin);
        dau.add(chu, BorderLayout.WEST);

        lblTrangThai.setFont(font(Font.BOLD, 13));
        lblTrangThai.setForeground(Color.decode("#B45309"));
        lblTrangThai.setText(hoaDon.getTrangThai());
        dau.add(lblTrangThai, BorderLayout.EAST);
        return dau;
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(0, 20, 8, 20));

        noi.add(taoTieuDeNhom("Chi tiết dịch vụ"));
        int[] doRong = {300, 50, 100, 120};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        cuon.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuon.setPreferredSize(new Dimension(100, 190));
        cuon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        noi.add(cuon);
        noi.add(Box.createVerticalStrut(8));

        JPanel tong = new JPanel(new GridLayout(0, 2, 12, 6));
        tong.setBackground(MAU_CHINH_NHAT);
        tong.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        tong.setAlignmentX(Component.LEFT_ALIGNMENT);
        tong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        tong.add(nhan("Tạm tính"));
        tong.add(taoGiaTri(tien(hoaDon.getTamTinh()) + " đ", CHU_CHINH));
        tong.add(nhan("Giảm giá"));
        tong.add(taoGiaTri("- " + tien(hoaDon.getGiamGia()) + " đ", Color.decode("#B45309")));
        tong.add(nhan("CẦN THU"));
        lblTongTien.setFont(font(Font.BOLD, 20));
        lblTongTien.setForeground(MAU_CHINH_DAM);
        lblTongTien.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTongTien.setText(tien(hoaDon.getTongTien()) + " đ");
        tong.add(lblTongTien);
        noi.add(tong);
        noi.add(Box.createVerticalStrut(10));

        noi.add(taoTieuDeNhom("Thanh toán"));
        cbPhuongThuc.setFont(font(Font.PLAIN, 14));
        cbPhuongThuc.addActionListener(e -> {
            boolean tienMat = HoaDon.TIEN_MAT.equals(cbPhuongThuc.getSelectedItem());
            oKhachDua.setEditable(tienMat);
            oKhachDua.setEnabled(tienMat);
            if (!tienMat) {
                oKhachDua.setText(tien(hoaDon.getTongTien()));
            }
            capNhatTien();
        });
        noi.add(truong("Phương thức thanh toán", cbPhuongThuc));

        oKhachDua.setText(tien(hoaDon.getTongTien()));
        oKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { capNhatTien(); }
            @Override public void removeUpdate(DocumentEvent e) { capNhatTien(); }
            @Override public void changedUpdate(DocumentEvent e) { capNhatTien(); }
        });
        noi.add(truong("Tiền khách đưa (VNĐ)", oKhachDua));

        lblTienThua.setFont(font(Font.BOLD, 16));
        lblTienThua.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel hangThua = new JPanel(new BorderLayout(10, 0));
        hangThua.setOpaque(false);
        hangThua.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangThua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        hangThua.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangThua.add(nhan("Tiền thừa trả khách"), BorderLayout.WEST);
        hangThua.add(lblTienThua, BorderLayout.EAST);
        noi.add(hangThua);

        noi.add(truong("Ghi chú thanh toán (không bắt buộc)", oGhiChu));

        JScrollPane cuonNoi = new JScrollPane(noi);
        cuonNoi.setBorder(null);
        cuonNoi.getVerticalScrollBar().setUnitIncrement(16);
        return cuonNoi;
    }

    private JComponent taoChan() {
        JButton huy = taoNutTrang("Hủy");
        JButton thu = taoNut("Xác nhận thanh toán", THANH_CONG, THANH_CONG.darker());
        huy.addActionListener(e -> dispose());
        thu.addActionListener(e -> xacNhan());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(thu);
        getRootPane().setDefaultButton(thu);
        return p;
    }

    private static JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        return l;
    }

    private static JLabel taoGiaTri(String chu, Color mau) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 14));
        l.setForeground(mau);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
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
            lblTienThua.setText("Còn thiếu " + tien(thua.negate()) + " đ");
            lblTienThua.setForeground(Color.decode("#B91C1C"));
        } else {
            lblTienThua.setText(tien(thua) + " đ");
            lblTienThua.setForeground(Color.decode("#047857"));
        }
    }

    private void xacNhan() {
        String phuongThuc = (String) cbPhuongThuc.getSelectedItem();
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
                            + "\n\nMở hộp thoại in / lưu hóa đơn ngay bây giờ?",
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
