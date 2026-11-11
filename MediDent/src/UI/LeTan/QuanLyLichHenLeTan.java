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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;

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

    private final JComboBox<String> cbTruongLoc = new JComboBox<>(new String[]{
            "Tất cả", "Mã lịch hẹn", "Bệnh nhân", "Bác sĩ", "Ngày hẹn", "Trạng thái", "Ghi chú"
    });
    private final JTextField oTimKiem = new JTextField();

    public QuanLyLichHenLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Quản lý lịch hẹn",
                "Xác nhận lịch và check-in khi bệnh nhân đến phòng khám.");
        the.add(taoBoLoc(), BorderLayout.NORTH);

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
        JButton nutTao = taoNut("Tạo lịch hẹn", true);
        JButton nutSua = taoNut("Sửa", false);
        JButton nutXacNhan = taoNut("Xác nhận", true);
        JButton nutCheckIn = taoNut("Check-in / Cấp số", true);
        JButton nutHuy = taoNut("Hủy lịch", false);

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
        cbTruongLoc.setToolTipText("Chọn tiêu chí cần lọc");
        oTimKiem.setToolTipText("Nhập nội dung cần tìm theo tiêu chí đã chọn");
        oTimKiem.setFont(font(Font.PLAIN, 13));
        oTimKiem.setPreferredSize(new Dimension(240, 38));
        oTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        cbTruongLoc.setPreferredSize(new Dimension(180, 38));

        JLabel nhanTruong = nhan("Lọc theo:");
        JLabel nhanTimKiem = nhan("Tìm kiếm:");
        nhanTruong.setFont(font(Font.BOLD, 12));
        nhanTimKiem.setFont(font(Font.BOLD, 12));

        JButton nutTim = taoNut("Tìm kiếm", false);
        nutTim.addActionListener(e -> apDungLoc());
        oTimKiem.addActionListener(e -> apDungLoc());
        JButton nutXoaLoc = GiaoDien.taoNutTrang("Xóa lọc");
        nutXoaLoc.addActionListener(e -> xoaLoc());

        JPanel khungLoc = new JPanel(new GridBagLayout());
        khungLoc.setBackground(NEN_APP);
        khungLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.insets = new Insets(0, 0, 4, 10); khungLoc.add(nhanTruong, c);
        c.gridx = 1; khungLoc.add(nhanTimKiem, c);
        c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 0; c.insets = new Insets(0, 0, 0, 10); khungLoc.add(cbTruongLoc, c);
        c.gridx = 1; c.weightx = 1; khungLoc.add(oTimKiem, c);
        c.gridx = 2; c.weightx = 0; c.insets = new Insets(0, 0, 0, 8); khungLoc.add(nutTim, c);
        c.gridx = 3; c.insets = new Insets(0, 0, 0, 0); khungLoc.add(nutXoaLoc, c);

        JPanel boLoc = new JPanel(new BorderLayout());
        boLoc.setOpaque(false);
        boLoc.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        boLoc.add(khungLoc, BorderLayout.CENTER);
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

    private String giaTriTheoTruong(LichHen lh, String truong) {
        if ("Mã lịch hẹn".equals(truong)) return String.format("LH%03d", lh.getMaLH());
        if ("Bệnh nhân".equals(truong)) return lh.getTenBenhNhan();
        if ("Bác sĩ".equals(truong)) return lh.getTenBacSi();
        if ("Ngày hẹn".equals(truong)) return lh.getNgayGio() == null ? "" : lh.getNgayGio().toLocalDate().format(NGAY);
        if ("Trạng thái".equals(truong)) return lh.getTrangThai();
        if ("Ghi chú".equals(truong)) return lh.getGhiChu();
        return null;
    }

    private void apDungLoc() {
        Integer maDangChon = lichDangChon() == null ? null : lichDangChon().getMaLH();
        String truong = (String) cbTruongLoc.getSelectedItem();
        String tuKhoa = oTimKiem.getText() == null ? "" : oTimKiem.getText().trim().toLowerCase(Locale.ROOT);
        dsHienThi = new ArrayList<>();
        int dongChon = -1;
        for (LichHen lh : dsTatCa) {
            String value = giaTriTheoTruong(lh, truong);
            boolean hopLe;
            if (tuKhoa.isEmpty()) {
                hopLe = true;
            } else if ("Tất cả".equals(truong)) {
                String noiDung = String.join(" ",
                        String.format("LH%03d", lh.getMaLH()),
                        lh.getTenBenhNhan() == null ? "" : lh.getTenBenhNhan(),
                        lh.getTenBacSi() == null ? "" : lh.getTenBacSi(),
                        lh.getNgayGio() == null ? "" : lh.getNgayGio().format(NGAY_GIO),
                        lh.getTrangThai() == null ? "" : lh.getTrangThai(),
                        lh.getGhiChu() == null ? "" : lh.getGhiChu());
                hopLe = noiDung.toLowerCase(Locale.ROOT).contains(tuKhoa);
            } else {
                hopLe = value != null && value.toLowerCase(Locale.ROOT).contains(tuKhoa);
            }
            if (hopLe) {
                if (maDangChon != null && maDangChon == lh.getMaLH()) dongChon = dsHienThi.size();
                dsHienThi.add(lh);
            }
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
        if (dongChon >= 0) bang.setRowSelectionInterval(dongChon, dongChon);
        else if (!dsHienThi.isEmpty()) bang.setRowSelectionInterval(0, 0);
    }

    private void xoaLoc() {
        cbTruongLoc.setSelectedIndex(0);
        oTimKiem.setText("");
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
