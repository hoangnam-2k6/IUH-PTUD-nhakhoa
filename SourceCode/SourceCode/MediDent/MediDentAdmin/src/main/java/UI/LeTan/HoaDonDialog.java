package UI.LeTan;

import Dao.DichVuDao;
import Dao.HoaDonDao;
import Entity.ChiTietHoaDon;
import Entity.DichVu;
import Entity.HoaDon;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại lập / sửa hóa đơn cho một phiếu khám.
 * Chỉ sửa được hóa đơn chưa thanh toán; mọi thay đổi được ghi thẳng xuống CSDL qua DAO.
 */
public class HoaDonDialog extends JDialog {
    private final HoaDonDao hoaDonDao = new HoaDonDao();
    private final DichVuDao dichVuDao = new DichVuDao();
    private final TaiKhoan taiKhoan;
    private final PhieuKham phieuKham;
    private final HoaDon hoaDon;

    private final List<ChiTietHoaDon> chiTiet = new ArrayList<>();
    private final List<DichVu> dsDichVu = new ArrayList<>();
    private boolean daLuu;

    private final JComboBox<DichVu> cbDichVu = new JComboBox<>();
    private final JSpinner oSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Dịch vụ", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JTextField oGiamGia = taoOTien();
    private final JTextArea oGhiChu = taoOVanBan(2);
    private final JLabel lblTamTinh = new JLabel("0 đ");
    private final JLabel lblGiamGia = new JLabel("0 đ");
    private final JLabel lblTongTien = new JLabel("0 đ");

    private HoaDonDialog(Component cha, TaiKhoan taiKhoan, PhieuKham phieuKham, HoaDon hoaDon) {
        super(SwingUtilities.getWindowAncestor(cha), tieuDe(phieuKham, hoaDon), ModalityType.APPLICATION_MODAL);
        this.taiKhoan = taiKhoan;
        this.phieuKham = phieuKham;
        this.hoaDon = hoaDon;
        if (hoaDon != null) {
            chiTiet.addAll(hoaDon.getChiTiet());
        }

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        add(taoDau(), BorderLayout.NORTH);
        add(new JScrollPane(taoNoiDung()), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        taiDichVu();
        napDuLieu();

        setSize(900, 760);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(cha);
    }

    /** Mở hộp thoại lập hóa đơn mới cho phiếu khám. Trả về true nếu đã lưu. */
    public static boolean taoMoi(Component cha, TaiKhoan taiKhoan, PhieuKham phieuKham) {
        if (phieuKham == null) {
            JOptionPane.showMessageDialog(cha, "Vui lòng chọn một phiếu khám để lập hóa đơn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        HoaDonDialog d = new HoaDonDialog(cha, taiKhoan, phieuKham, null);
        d.setVisible(true);
        return d.daLuu;
    }

    /** Mở hộp thoại sửa hóa đơn chưa thanh toán. Trả về true nếu đã lưu. */
    public static boolean sua(Component cha, HoaDon hoaDon) {
        if (hoaDon == null) {
            JOptionPane.showMessageDialog(cha, "Vui lòng chọn một hóa đơn để sửa.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        try {
            HoaDon dayDu = new HoaDonDao().findDayDu(hoaDon.getMaHD());
            if (dayDu == null) {
                throw new IllegalArgumentException("Hóa đơn không còn tồn tại, vui lòng làm mới danh sách.");
            }
            if (!dayDu.chuaThanhToan()) {
                throw new IllegalArgumentException("Chỉ sửa được hóa đơn chưa thanh toán.");
            }
            HoaDonDialog d = new HoaDonDialog(cha, null, null, dayDu);
            d.setVisible(true);
            return d.daLuu;
        } catch (Exception e) {
            baoLoi(cha, e);
            return false;
        }
    }

    private static String tieuDe(PhieuKham pk, HoaDon hd) {
        if (hd != null) {
            return String.format("Sửa hóa đơn HD%03d", hd.getMaHD());
        }
        if (pk == null) return "Lập hóa đơn";
        return String.format("Lập hóa đơn cho PK%03d - %s", pk.getMaPK(),
                pk.getTenBenhNhan() == null ? "bệnh nhân" : pk.getTenBenhNhan());
    }

    // ===================== Giao diện =====================

    private JComponent taoDau() {
        JPanel dau = new JPanel(new BorderLayout(12, 8));
        dau.setBackground(NEN_THE);
        dau.setBorder(BorderFactory.createEmptyBorder(14, 18, 10, 18));

        JLabel tieuDe = new JLabel(hoaDon == null ? "LẬP HÓA ĐƠN" : "SỬA HÓA ĐƠN");
        tieuDe.setFont(font(Font.BOLD, 19));
        tieuDe.setForeground(CHU_CHINH);

        String benhNhan;
        String thongTin;
        if (hoaDon != null) {
            benhNhan = hoaDon.getTenBenhNhan();
            thongTin = String.format("HD%03d  ·  PK%03d  ·  Ngày khám %s  ·  %s",
                    hoaDon.getMaHD(), hoaDon.getMaPK(),
                    hoaDon.getNgayKham() == null ? "—" : dinhDangNgay(hoaDon.getNgayKham()),
                    hoaDon.getTenPhong() == null ? "Chưa phân phòng" : hoaDon.getTenPhong());
        } else {
            benhNhan = phieuKham == null ? "—" : phieuKham.getTenBenhNhan();
            thongTin = phieuKham == null ? "—" : String.format("PK%03d  ·  Ngày khám %s  ·  %s",
                    phieuKham.getMaPK(),
                    phieuKham.getNgayKham() == null ? "—" : dinhDangNgay(phieuKham.getNgayKham()),
                    phieuKham.getTenPhong() == null ? "Chưa phân phòng" : phieuKham.getTenPhong());
        }

        JLabel lblBenhNhan = new JLabel(benhNhan);
        lblBenhNhan.setFont(font(Font.BOLD, 14));
        lblBenhNhan.setForeground(MAU_CHINH_DAM);
        JLabel lblThongTin = new JLabel(thongTin);
        lblThongTin.setFont(font(Font.PLAIN, 12));
        lblThongTin.setForeground(CHU_PHU);

        JPanel chu = new JPanel(new GridLayout(0, 1, 0, 3));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(lblBenhNhan);
        chu.add(lblThongTin);
        dau.add(chu, BorderLayout.WEST);

        if (hoaDon != null) {
            JLabel trangThai = new JLabel(hoaDon.getTrangThai());
            trangThai.setFont(font(Font.BOLD, 13));
            trangThai.setForeground(hoaDon.chuaThanhToan()
                    ? Color.decode("#B45309") : Color.decode("#047857"));
            dau.add(trangThai, BorderLayout.EAST);
        }
        return dau;
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(4, 18, 12, 18));

        JLabel tieuDeDichVu = taoTieuDeNhom("Dịch vụ & vật tư");
        tieuDeDichVu.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(tieuDeDichVu);

        cbDichVu.setFont(font(Font.PLAIN, 14));
        cbDichVu.setPreferredSize(new Dimension(360, 34));
        oSoLuong.setFont(font(Font.PLAIN, 14));
        oSoLuong.setPreferredSize(new Dimension(70, 34));
        JButton nutThem = taoNut("+ Thêm dòng", MAU_CHINH, MAU_CHINH_DAM);
        nutThem.addActionListener(e -> themDong());

        JPanel hangThem = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangThem.setOpaque(false);
        hangThem.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangThem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        hangThem.add(cbDichVu);
        hangThem.add(new JLabel("SL:"));
        hangThem.add(oSoLuong);
        hangThem.add(nutThem);
        noi.add(hangThem);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {45, 370, 55, 115, 130};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) suaSoLuong();
            }
        });
        JScrollPane cuonBang = new JScrollPane(bang);
        cuonBang.setBorder(BorderFactory.createLineBorder(VIEN));
        cuonBang.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuonBang.setPreferredSize(new Dimension(100, 210));
        cuonBang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        noi.add(cuonBang);

        JButton nutSuaSl = taoNutTrang("Sửa số lượng");
        JButton nutXoaDong = taoNutTrang("Xóa dòng");
        nutSuaSl.addActionListener(e -> suaSoLuong());
        nutXoaDong.addActionListener(e -> xoaDong());
        JPanel hangDong = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        hangDong.setOpaque(false);
        hangDong.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        hangDong.add(nutSuaSl);
        hangDong.add(nutXoaDong);
        JLabel goiY = new JLabel("Bấm đúp vào một dòng để đổi số lượng.");
        goiY.setFont(font(Font.PLAIN, 12));
        goiY.setForeground(CHU_PHU);
        hangDong.add(goiY);
        noi.add(hangDong);

        noi.add(Box.createVerticalStrut(6));
        noi.add(taoPhanTong());
        return noi;
    }

    private JComponent taoPhanTong() {
        JPanel tong = new JPanel(new BorderLayout(20, 0));
        tong.setOpaque(false);
        tong.setAlignmentX(Component.LEFT_ALIGNMENT);
        tong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JPanel ghiChu = new JPanel(new BorderLayout(0, 4));
        ghiChu.setOpaque(false);
        JLabel lblGhiChu = new JLabel("Ghi chú hóa đơn");
        lblGhiChu.setFont(font(Font.BOLD, 13));
        lblGhiChu.setForeground(CHU_CHINH);
        ghiChu.add(lblGhiChu, BorderLayout.NORTH);
        ghiChu.add(boc(oGhiChu), BorderLayout.CENTER);

        JPanel soTien = new JPanel(new GridLayout(0, 2, 12, 8));
        soTien.setBackground(MAU_CHINH_NHAT);
        soTien.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        soTien.add(nhanTien("Tạm tính"));
        dinhDangTien(lblTamTinh, CHU_CHINH);
        soTien.add(lblTamTinh);

        soTien.add(nhanTien("Giảm giá (VNĐ)"));
        soTien.add(oGiamGia);

        soTien.add(nhanTien("Tiền giảm"));
        dinhDangTien(lblGiamGia, Color.decode("#B45309"));
        soTien.add(lblGiamGia);

        soTien.add(nhanTien("TỔNG CỘNG"));
        dinhDangTien(lblTongTien, MAU_CHINH_DAM);
        lblTongTien.setFont(font(Font.BOLD, 18));
        soTien.add(lblTongTien);

        oGiamGia.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { capNhatTong(); }
            @Override public void removeUpdate(DocumentEvent e) { capNhatTong(); }
            @Override public void changedUpdate(DocumentEvent e) { capNhatTong(); }
        });

        tong.add(ghiChu, BorderLayout.CENTER);
        tong.add(soTien, BorderLayout.EAST);
        return tong;
    }

    private static JLabel nhanTien(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        return l;
    }

    private static void dinhDangTien(JLabel l, Color mau) {
        l.setFont(font(Font.BOLD, 14));
        l.setForeground(mau);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private JComponent taoChan() {
        JButton huy = taoNutTrang("Hủy");
        JButton luu = taoNut("Lưu hóa đơn", MAU_CHINH, MAU_CHINH_DAM);
        huy.addActionListener(e -> dispose());
        luu.addActionListener(e -> luu());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(luu);
        getRootPane().setDefaultButton(luu);
        return p;
    }

    // ===================== Dữ liệu =====================

    private void taiDichVu() {
        try {
            dsDichVu.addAll(dichVuDao.findDangHoatDong());
        } catch (Exception e) {
            baoLoi(this, e);
        }
        cbDichVu.removeAllItems();
        for (DichVu dv : dsDichVu) cbDichVu.addItem(dv);
        if (cbDichVu.getItemCount() == 0) {
            cbDichVu.setEnabled(false);
        }
    }

    private void napDuLieu() {
        if (hoaDon != null) {
            if (hoaDon.getGiamGia().signum() > 0) {
                oGiamGia.setText(tien(hoaDon.getGiamGia()));
            }
            oGhiChu.setText(nz(hoaDon.getGhiChu()));
        }
        capNhatBang();
    }

    private void themDong() {
        DichVu dv = (DichVu) cbDichVu.getSelectedItem();
        if (dv == null) {
            JOptionPane.showMessageDialog(this, "Chưa có dịch vụ nào đang hoạt động để thêm.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int soLuong = (Integer) oSoLuong.getValue();
        for (ChiTietHoaDon ct : chiTiet) {
            if (Objects.equals(ct.getMaDV(), dv.getMaDVSo())) {
                ct.setSoLuong(ct.getSoLuong() + soLuong);
                capNhatBang();
                return;
            }
        }
        chiTiet.add(new ChiTietHoaDon(dv.getMaDVSo(), dv.getTenDichVu(), soLuong, dv.getDonGia()));
        capNhatBang();
        oSoLuong.setValue(1);
    }

    private void suaSoLuong() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= chiTiet.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng dịch vụ cần sửa.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ChiTietHoaDon ct = chiTiet.get(dong);
        String nhap = JOptionPane.showInputDialog(this,
                "Số lượng cho \"" + ct.getTenDichVu() + "\":", String.valueOf(ct.getSoLuong()));
        if (nhap == null) return;
        try {
            int soLuong = Integer.parseInt(nhap.trim());
            if (soLuong <= 0) {
                throw new NumberFormatException();
            }
            ct.setSoLuong(soLuong);
            capNhatBang();
            bang.setRowSelectionInterval(dong, dong);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên lớn hơn 0.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaDong() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= chiTiet.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng dịch vụ cần xóa.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        chiTiet.remove(dong);
        capNhatBang();
    }

    private void capNhatBang() {
        model.setRowCount(0);
        int stt = 1;
        for (ChiTietHoaDon ct : chiTiet) {
            model.addRow(new Object[]{
                    stt++,
                    ct.getTenDichVu(),
                    ct.getSoLuong(),
                    tien(ct.getDonGia()),
                    tien(ct.getThanhTien())
            });
        }
        capNhatTong();
    }

    private void capNhatTong() {
        BigDecimal tamTinh = tinhTamTinh();
        BigDecimal giam = docTien(oGiamGia.getText());
        BigDecimal tong = tamTinh.subtract(giam);
        lblTamTinh.setText(tien(tamTinh) + " đ");
        lblGiamGia.setText("- " + tien(giam) + " đ");
        lblTongTien.setText(tien(tong) + " đ");
        lblTongTien.setForeground(tong.signum() < 0 ? Color.decode("#B91C1C") : MAU_CHINH_DAM);
    }

    private BigDecimal tinhTamTinh() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : chiTiet) {
            tong = tong.add(ct.getThanhTien());
        }
        return tong;
    }

    private void luu() {
        if (chiTiet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn phải có ít nhất một dịch vụ.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal tamTinh = tinhTamTinh();
        BigDecimal giam = docTien(oGiamGia.getText());
        if (giam.signum() < 0) {
            JOptionPane.showMessageDialog(this, "Giảm giá không được âm.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (giam.compareTo(tamTinh) > 0) {
            JOptionPane.showMessageDialog(this, "Giảm giá không được lớn hơn tổng tiền dịch vụ ("
                    + tien(tamTinh) + " đ).", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (hoaDon == null) {
                hoaDonDao.taoHoaDon(phieuKham.getMaPK(),
                        taiKhoan == null ? null : taiKhoan.getMaTK(),
                        chiTiet, giam, oGhiChu.getText());
            } else {
                hoaDonDao.capNhatHoaDon(hoaDon.getMaHD(), chiTiet, giam, oGhiChu.getText());
            }
            daLuu = true;
            dispose();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
