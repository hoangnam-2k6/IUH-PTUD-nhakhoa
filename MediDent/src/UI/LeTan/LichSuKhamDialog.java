package UI.LeTan;

import Dao.HoaDonDao;
import Dao.KetQuaKhamDao;
import Dao.PhieuKhamDao;
import Entity.BenhNhan;
import Entity.HoaDon;
import Entity.KetQuaKham;
import Entity.PhieuKham;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.LeTan.GiaoDien.*;

/** Hộp thoại xem lịch sử khám và kết quả điều trị của một bệnh nhân. */
public class LichSuKhamDialog extends JDialog {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BenhNhan benhNhan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final KetQuaKhamDao ketQuaKhamDao = new KetQuaKhamDao();
    private final HoaDonDao hoaDonDao = new HoaDonDao();

    private final List<PhieuKham> dsPhieu = new ArrayList<>();
    private final Map<Integer, HoaDon> hoaDonTheoPhieu = new HashMap<>();
    private PhieuKham dangChon;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã phiếu", "Ngày khám", "STT", "Phòng", "Trạng thái", "Hóa đơn"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);

    private final JLabel lblTongLuot = new JLabel("0");
    private final JLabel lblNgayGanNhat = new JLabel("—");
    private final JLabel lblTongChi = new JLabel("0 đ");
    private final JLabel lblPhieuDangXem = new JLabel("Chưa chọn phiếu");

    private final JTextArea oTrieuChung = vanBan();
    private final JTextArea oKetQua = vanBan();
    private final JTextArea oChanDoan = vanBan();
    private final JTextArea oHuong = vanBan();
    private final JTextArea oDonThuoc = vanBan();
    private final JTextArea oGhiChu = vanBan();
    private final JLabel lblBacSi = giaTri();
    private final JLabel lblTaiKham = giaTri();
    private final JLabel lblCapNhat = giaTri();
    private final JLabel lblHoaDon = giaTri();
    private final JButton nutIn = taoNut("In phiếu khám", MAU_CHINH, MAU_CHINH_DAM);
    private final JButton nutWord = taoNutTrang("Xuất Word");

    private LichSuKhamDialog(Component cha, BenhNhan bn) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha),
                "Lịch sử khám - " + String.format("BN%03d", bn.getMaBN()) + " " + bn.getHoTen(),
                ModalityType.APPLICATION_MODAL);
        this.benhNhan = bn;
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_APP);

        add(taoDau(), BorderLayout.NORTH);
        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, taoBang(), taoChiTiet());
        chia.setResizeWeight(0.38);
        chia.setDividerSize(10);
        chia.setBorder(BorderFactory.createEmptyBorder(0, 14, 8, 14));
        add(chia, BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        setSize(980, 720);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(cha);
        taiDuLieu();
    }

    public static void hienThi(Component cha, BenhNhan bn) {
        if (bn == null) {
            JOptionPane.showMessageDialog(cha, "Vui lòng chọn một bệnh nhân.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        new LichSuKhamDialog(cha, bn).setVisible(true);
    }

    private JPanel taoDau() {
        JPanel dau = new JPanel(new BorderLayout(10, 8));
        dau.setBackground(NEN_THE);
        dau.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));

        JLabel ten = new JLabel(benhNhan.getHoTen());
        ten.setFont(font(Font.BOLD, 20));
        ten.setForeground(CHU_CHINH);
        JLabel moTa = new JLabel(String.format("BN%03d", benhNhan.getMaBN())
                + (benhNhan.getNgaySinh() == null ? "" : "  ·  " + benhNhan.getNgaySinh().format(NGAY)
                + " (" + benhNhan.getTuoi() + " tuổi)")
                + (benhNhan.getSoDienThoai() == null ? "" : "  ·  " + benhNhan.getSoDienThoai()));
        moTa.setFont(font(Font.PLAIN, 13));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 3));
        chu.setOpaque(false);
        chu.add(ten);
        chu.add(moTa);

        JPanel thongKe = new JPanel(new GridLayout(1, 3, 10, 0));
        thongKe.setOpaque(false);
        thongKe.add(theNho("Số lượt khám", lblTongLuot));
        thongKe.add(theNho("Lần khám gần nhất", lblNgayGanNhat));
        thongKe.add(theNho("Đã thanh toán", lblTongChi));

        dau.add(chu, BorderLayout.WEST);
        dau.add(thongKe, BorderLayout.EAST);
        return dau;
    }

    private JPanel theNho(String nhan, JLabel giaTri) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(MAU_CHINH_NHAT);
        p.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 11));
        l.setForeground(CHU_PHU);
        giaTri.setFont(font(Font.BOLD, 14));
        giaTri.setForeground(MAU_CHINH_DAM);
        p.add(l);
        p.add(giaTri);
        return p;
    }

    private JComponent taoBang() {
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {90, 110, 55, 130, 110, 140};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            hienChiTiet();
        });
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        p.add(cuon, BorderLayout.CENTER);
        return p;
    }

    private JComponent taoChiTiet() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        lblPhieuDangXem.setFont(font(Font.BOLD, 15));
        lblPhieuDangXem.setForeground(CHU_CHINH);
        dau.add(lblPhieuDangXem, BorderLayout.WEST);
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        phai.setOpaque(false);
        phai.add(nho("Bác sĩ", lblBacSi));
        phai.add(nho("Tái khám", lblTaiKham));
        phai.add(nho("Cập nhật", lblCapNhat));
        phai.add(nho("Hóa đơn", lblHoaDon));
        dau.add(phai, BorderLayout.EAST);
        dau.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(dau);
        noi.add(Box.createVerticalStrut(10));

        JPanel luoi = new JPanel(new GridLayout(0, 2, 12, 8));
        luoi.setOpaque(false);
        luoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        luoi.add(o("Triệu chứng", boc(oTrieuChung)));
        luoi.add(o("Kết quả lâm sàng", boc(oKetQua)));
        luoi.add(o("Chẩn đoán", boc(oChanDoan)));
        luoi.add(o("Hướng điều trị", boc(oHuong)));
        luoi.add(o("Đơn thuốc", boc(oDonThuoc)));
        luoi.add(o("Ghi chú", boc(oGhiChu)));
        noi.add(luoi);

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        return cuon;
    }

    private JPanel nho(String nhan, JLabel giaTri) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 1));
        p.setOpaque(false);
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.PLAIN, 10));
        l.setForeground(CHU_PHU);
        giaTri.setFont(font(Font.BOLD, 12));
        p.add(l);
        p.add(giaTri);
        return p;
    }

    private JPanel o(String nhan, JComponent oNhap) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(CHU_CHINH);
        p.add(l, BorderLayout.NORTH);
        p.add(oNhap, BorderLayout.CENTER);
        return p;
    }

    private static JTextArea vanBan() {
        JTextArea a = new JTextArea(3, 20);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(font(Font.PLAIN, 13));
        a.setForeground(CHU_CHINH);
        a.setBackground(NEN_APP);
        a.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return a;
    }

    private static JLabel giaTri() {
        JLabel l = new JLabel("—");
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        return l;
    }

    private JPanel taoChan() {
        JButton dong = taoNutTrang("Đóng");
        nutIn.addActionListener(e -> inPhieu());
        nutIn.setEnabled(false);
        nutWord.addActionListener(e -> xuatPhieuWord());
        nutWord.setEnabled(false);
        dong.addActionListener(e -> dispose());
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(nutWord);
        phai.add(nutIn);
        phai.add(dong);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    private void taiDuLieu() {
        try {
            dsPhieu.clear();
            dsPhieu.addAll(phieuKhamDao.findByBenhNhan(benhNhan.getMaBN()));
            hoaDonTheoPhieu.clear();
            for (HoaDon hd : hoaDonDao.findAll()) {
                hoaDonTheoPhieu.putIfAbsent(hd.getMaPK(), hd);
            }
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }

        model.setRowCount(0);
        for (PhieuKham pk : dsPhieu) {
            HoaDon hd = hoaDonTheoPhieu.get(pk.getMaPK());
            model.addRow(new Object[]{
                    String.format("PK%03d", pk.getMaPK()),
                    pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY),
                    pk.getSoThuTu(),
                    pk.getTenPhong() == null ? "—" : pk.getTenPhong(),
                    pk.getTrangThai(),
                    hd == null ? "Chưa lập" : hd.getTrangThai()
            });
        }
        if (!dsPhieu.isEmpty()) {
            bang.setRowSelectionInterval(0, 0);
        } else {
            xoaChiTiet();
        }

        lblTongLuot.setText(String.valueOf(dsPhieu.size()));
        lblNgayGanNhat.setText(dsPhieu.isEmpty() || dsPhieu.get(0).getNgayKham() == null
                ? "—" : dsPhieu.get(0).getNgayKham().format(NGAY));
        try {
            lblTongChi.setText(tien(new Dao.BenhNhanDao().tongDaThanhToan(benhNhan.getMaBN())) + " đ");
        } catch (Exception e) {
            lblTongChi.setText("—");
        }
    }

    private void hienChiTiet() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= dsPhieu.size()) {
            xoaChiTiet();
            return;
        }
        dangChon = dsPhieu.get(dong);
        nutIn.setEnabled(true);
        nutWord.setEnabled(true);
        lblPhieuDangXem.setText(String.format("Phiếu PK%03d", dangChon.getMaPK())
                + "  ·  " + (dangChon.getNgayKham() == null ? "" : dangChon.getNgayKham().format(NGAY))
                + "  ·  STT " + dangChon.getSoThuTu()
                + "  ·  " + dangChon.getTrangThai());

        HoaDon hd = hoaDonTheoPhieu.get(dangChon.getMaPK());
        lblHoaDon.setText(hd == null ? "Chưa lập"
                : String.format("HD%03d", hd.getMaHD()) + " · " + hd.getTrangThai());

        try {
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            if (kq == null) {
                oTrieuChung.setText("Chưa nhập kết quả khám.");
                oKetQua.setText("");
                oChanDoan.setText("");
                oHuong.setText("");
                oDonThuoc.setText("");
                oGhiChu.setText("");
                lblBacSi.setText("—");
                lblTaiKham.setText("—");
                lblCapNhat.setText("—");
            } else {
                oTrieuChung.setText(nz(kq.getTrieuChung()));
                oKetQua.setText(nz(kq.getKetQuaLamSang()));
                oChanDoan.setText(nz(kq.getChanDoan()));
                oHuong.setText(nz(kq.getHuongDieuTri()));
                oDonThuoc.setText(nz(kq.getDonThuoc()));
                oGhiChu.setText(nz(kq.getGhiChu()));
                lblBacSi.setText(kq.getTenBacSi() == null ? "—" : kq.getTenBacSi());
                lblTaiKham.setText(kq.getNgayTaiKham() == null ? "—" : kq.getNgayTaiKham().format(NGAY));
                lblCapNhat.setText(kq.getNgayCapNhat() == null ? "—" : kq.getNgayCapNhat().format(NGAY_GIO));
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }
        for (JTextArea a : new JTextArea[]{oTrieuChung, oKetQua, oChanDoan, oHuong, oDonThuoc, oGhiChu}) {
            a.setCaretPosition(0);
        }
    }

    private void xoaChiTiet() {
        dangChon = null;
        nutIn.setEnabled(false);
        nutWord.setEnabled(false);
        lblPhieuDangXem.setText("Chưa chọn phiếu");
        lblBacSi.setText("—");
        lblTaiKham.setText("—");
        lblCapNhat.setText("—");
        lblHoaDon.setText("—");
        for (JTextArea a : new JTextArea[]{oTrieuChung, oKetQua, oChanDoan, oHuong, oDonThuoc, oGhiChu}) {
            a.setText("");
        }
    }

    private void inPhieu() {
        if (dangChon == null) return;
        try {
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            XuatPhieuKham.xuat(this, dangChon, benhNhan, kq);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void xuatPhieuWord() {
        if (dangChon == null) return;
        try {
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            XuatPhieuKham.xuatWord(this, dangChon, benhNhan, kq);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
