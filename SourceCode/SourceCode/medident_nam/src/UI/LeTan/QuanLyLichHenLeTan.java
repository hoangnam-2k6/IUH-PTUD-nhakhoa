package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Dao.PhieuKhamDao;
import Dao.PhongKhamDao;
import Entity.BenhNhan;
import Entity.LichHen;
import Entity.PhieuKham;
import Entity.PhongKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Lịch hẹn": lọc, tạo/sửa, xác nhận, check-in và xử lý yêu cầu đổi/hủy. */
public class QuanLyLichHenLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final LichHenDao lichHenDao = new LichHenDao();
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final PhongKhamDao phongKhamDao = new PhongKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();

    private List<LichHen> dsTatCa = new ArrayList<>();
    private List<LichHen> dsHienThi = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã LH", "Bệnh nhân", "Ngày giờ", "Bác sĩ", "Trạng thái", "Yêu cầu", "Ghi chú"}, 0) {
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
    private final JTextField oTuNgay = GiaoDien.taoOText();
    private final JTextField oDenNgay = GiaoDien.taoOText();

    public QuanLyLichHenLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Quản lý lịch hẹn",
                "Xác nhận lịch, check-in khi bệnh nhân đến và xử lý yêu cầu đổi/hủy.");
        the.add(taoBoLoc(), BorderLayout.NORTH);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {70, 170, 125, 150, 110, 170, 190};
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
        JButton nutYeuCau = taoNut("Xử lý yêu cầu", CANH_BAO, Color.decode("#D97706"));

        nutLamMoi.addActionListener(e -> taiDuLieu());
        nutTao.addActionListener(e -> taoLichHen());
        nutSua.addActionListener(e -> suaLichHen());
        nutXacNhan.addActionListener(e -> xacNhanLich());
        nutCheckIn.addActionListener(e -> checkIn());
        nutHuy.addActionListener(e -> huyLich());
        nutYeuCau.addActionListener(e -> xuLyYeuCau());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        nut.add(nutTao);
        nut.add(nutSua);
        nut.add(nutXacNhan);
        nut.add(nutCheckIn);
        nut.add(nutHuy);
        nut.add(nutYeuCau);
        nut.add(nutLamMoi);

        JPanel chan = new JPanel(new BorderLayout(10, 0));
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        chan.add(soLuong, BorderLayout.WEST);
        chan.add(nut, BorderLayout.CENTER);
        JButton nutXoaLoc = taoNutTrang("Xóa bộ lọc");
        nutXoaLoc.addActionListener(e -> xoaLoc());
        chan.add(nutXoaLoc, BorderLayout.EAST);
        the.add(chan, BorderLayout.SOUTH);

        add(the, BorderLayout.CENTER);
    }

    private JPanel taoBoLoc() {
        oTimKiem.setToolTipText("Tên bệnh nhân, bác sĩ hoặc mã lịch hẹn");
        oTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apDungLoc(); }
            @Override public void removeUpdate(DocumentEvent e) { apDungLoc(); }
            @Override public void changedUpdate(DocumentEvent e) { apDungLoc(); }
        });
        cbTrangThai.setFont(font(Font.PLAIN, 13));
        cbTrangThai.addActionListener(e -> apDungLoc());
        oTuNgay.setToolTipText("dd/MM/yyyy");
        oDenNgay.setToolTipText("dd/MM/yyyy");
        oTuNgay.setPreferredSize(new Dimension(110, 32));
        oDenNgay.setPreferredSize(new Dimension(110, 32));

        JPanel dong1 = new JPanel(new BorderLayout(8, 0));
        dong1.setOpaque(false);
        JLabel nhanTim = new JLabel("Tìm kiếm:");
        nhanTim.setFont(font(Font.PLAIN, 13));
        nhanTim.setForeground(CHU_PHU);
        dong1.add(nhanTim, BorderLayout.WEST);
        dong1.add(oTimKiem, BorderLayout.CENTER);
        JPanel trangThai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        trangThai.setOpaque(false);
        JLabel nhanTT = new JLabel("Trạng thái:");
        nhanTT.setFont(font(Font.PLAIN, 13));
        nhanTT.setForeground(CHU_PHU);
        trangThai.add(nhanTT);
        trangThai.add(cbTrangThai);
        dong1.add(trangThai, BorderLayout.EAST);

        JPanel dong2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dong2.setOpaque(false);
        dong2.add(nhan("Từ ngày"));
        dong2.add(oTuNgay);
        dong2.add(nhan("Đến ngày"));
        dong2.add(oDenNgay);
        JButton nutLoc = taoNutTrang("Lọc theo ngày");
        nutLoc.addActionListener(e -> apDungLoc());
        dong2.add(nutLoc);

        JPanel boLoc = new JPanel(new GridLayout(0, 1, 0, 8));
        boLoc.setOpaque(false);
        boLoc.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        boLoc.add(dong1);
        boLoc.add(dong2);
        return boLoc;
    }

    private JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        return l;
    }

    public void taiDuLieu() {
        try {
            dsTatCa = lichHenDao.findAll();
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }
        apDungLoc();
    }

    private void apDungLoc() {
        LocalDate tu = null;
        LocalDate den = null;
        try {
            tu = docNgay(oTuNgay.getText());
            den = docNgay(oDenNgay.getText());
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
            return;
        }
        if (tu != null && den != null && tu.isAfter(den)) {
            JOptionPane.showMessageDialog(this, "Từ ngày không được sau đến ngày.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer maDangChon = lichDangChon() == null ? null : lichDangChon().getMaLH();
        String tuKhoa = oTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        String trangThai = (String) cbTrangThai.getSelectedItem();

        dsHienThi = new ArrayList<>();
        int dongChon = -1;
        for (LichHen lh : dsTatCa) {
            if (tuKhoa.isEmpty() || khopTuKhoa(lh, tuKhoa)) {
                if (trangThai == null || "Tất cả".equals(trangThai) || trangThai.equals(lh.getTrangThai())) {
                    LocalDate ngay = lh.getNgayGio() == null ? null : lh.getNgayGio().toLocalDate();
                    if ((tu == null || (ngay != null && !ngay.isBefore(tu)))
                            && (den == null || (ngay != null && !ngay.isAfter(den)))) {
                        if (maDangChon != null && maDangChon == lh.getMaLH()) {
                            dongChon = dsHienThi.size();
                        }
                        dsHienThi.add(lh);
                    }
                }
            }
        }

        model.setRowCount(0);
        for (LichHen lh : dsHienThi) {
            String yeuCau = "";
            if (lh.getYeuCau() != null) {
                yeuCau = lh.getYeuCau();
                if (lh.getNgayGioMoi() != null) yeuCau += " → " + lh.getNgayGioMoi().format(NGAY_GIO);
            }
            model.addRow(new Object[]{
                    String.format("LH%03d", lh.getMaLH()),
                    lh.getTenBenhNhan(),
                    lh.getNgayGio() == null ? "" : lh.getNgayGio().format(NGAY_GIO),
                    lh.getTenBacSi(),
                    lh.getTrangThai(),
                    yeuCau,
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

    private boolean khopTuKhoa(LichHen lh, String tuKhoa) {
        String ma = String.format("LH%03d", lh.getMaLH()).toLowerCase(Locale.ROOT);
        return ma.contains(tuKhoa)
                || (lh.getTenBenhNhan() != null && lh.getTenBenhNhan().toLowerCase(Locale.ROOT).contains(tuKhoa))
                || (lh.getTenBacSi() != null && lh.getTenBacSi().toLowerCase(Locale.ROOT).contains(tuKhoa))
                || (lh.getGhiChu() != null && lh.getGhiChu().toLowerCase(Locale.ROOT).contains(tuKhoa));
    }

    private void xoaLoc() {
        oTimKiem.setText("");
        oTuNgay.setText("");
        oDenNgay.setText("");
        cbTrangThai.setSelectedIndex(0);
        apDungLoc();
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
        try {
            List<PhongKham> dsPhong = phongKhamDao.findDangHoatDong();
            if (dsPhong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có phòng khám nào đang hoạt động.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JComboBox<PhongKham> cbPhong = new JComboBox<>(dsPhong.toArray(new PhongKham[0]));
            cbPhong.setFont(font(Font.PLAIN, 14));
            JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
            form.add(new JLabel("Bệnh nhân: " + lh.getTenBenhNhan()));
            form.add(new JLabel("Giờ hẹn: " + lh.getNgayGio().format(NGAY_GIO)
                    + " · Bác sĩ " + lh.getTenBacSi()));
            form.add(new JLabel("Chọn phòng khám để cấp số thứ tự:"));
            form.add(cbPhong);

            int kq = JOptionPane.showConfirmDialog(this, form, "Check-in bệnh nhân",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (kq != JOptionPane.OK_OPTION) return;

            PhongKham phong = (PhongKham) cbPhong.getSelectedItem();
            if (phong == null) return;
            PhieuKham pk = phieuKhamDao.capSoThuTu(lh.getMaBN(), phong.getMaPhong(), lh.getMaLH());
            BenhNhan bn = benhNhanDao.findById(lh.getMaBN());
            thongBao(this, "Đã check-in " + lh.getTenBenhNhan() + ".\nSố thứ tự: " + pk.getSoThuTu()
                    + " · " + phong.getTenPhong());
            taiDuLieu();
            if (xacNhan(this, "In phiếu khám cho " + lh.getTenBenhNhan() + "?", "In phiếu khám")) {
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

    private void xuLyYeuCau() {
        LichHen lh = yeuCauChon("Vui lòng chọn lịch hẹn có yêu cầu đổi/hủy.");
        if (lh == null) return;
        if (!lh.coYeuCau()) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này không có yêu cầu đổi/hủy.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String noiDung = lh.getTenBenhNhan() + " yêu cầu: " + lh.getYeuCau();
        if (lh.getNgayGioMoi() != null) {
            noiDung += "\nThời gian mới: " + lh.getNgayGioMoi().format(NGAY_GIO);
        }
        int chon = JOptionPane.showOptionDialog(this, noiDung, "Xử lý yêu cầu đổi/hủy",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Chấp nhận", "Từ chối", "Đóng"}, "Chấp nhận");
        if (chon != 0 && chon != 1) return;
        try {
            lichHenDao.xuLyYeuCau(lh.getMaLH(), chon == 0);
            thongBao(this, chon == 0 ? "Đã chấp nhận yêu cầu." : "Đã từ chối yêu cầu, giữ nguyên lịch hẹn.");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static final class TrangThaiRenderer extends DefaultTableCellRenderer {
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
