package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Dao.PhieuKhamDao;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static UI.LeTan.GiaoDien.*;

public class TrangChuLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final LichHenDao lichHenDao = new LichHenDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();

    private final JLabel soTongBN = new JLabel("0");
    private final JLabel soChoKham = new JLabel("0");
    private final JLabel soHoanThanh = new JLabel("0");
    private final JLabel soLichHen = new JLabel("0");

    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Phòng khám", "Trạng thái", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // trang chủ chỉ để xem, thao tác phiếu nằm ở màn Khám bệnh
        }
    };
    private final JTable bangPhieu = GiaoDien.taoBang(modelPhieu);
    private final TableRowSorter<DefaultTableModel> boLoc = new TableRowSorter<>(modelPhieu);
    private final JComboBox<String> cbTruongLoc = new JComboBox<>(new String[]{
            "Tất cả", "Mã phiếu", "Mã BN", "Bệnh nhân", "Phòng khám", "Trạng thái"
    });
    private final JTextField oTimKiem = new JTextField();

    public TrangChuLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel phanTren = new JPanel(new BorderLayout(0, 12));
        phanTren.setOpaque(false);
        phanTren.add(taoBanner(), BorderLayout.NORTH);
        phanTren.add(taoHangThongKe(), BorderLayout.CENTER);

        add(phanTren, BorderLayout.NORTH);
        add(taoKhamVaDieuTri(), BorderLayout.CENTER);
    }

    // ===================== Xây dựng giao diện =====================

    private JPanel taoBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(MAU_CHINH);
        banner.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel tieuDe = new JLabel("Tổng quan & Khám điều trị");
        tieuDe.setFont(font(Font.BOLD, 22));
        tieuDe.setForeground(Color.WHITE);

        JLabel chao = new JLabel("Xin chào, " + taiKhoan.getHoTen() + "  ·  Hôm nay "
                + LocalDate.now().format(NGAY));
        chao.setFont(font(Font.PLAIN, 13));
        chao.setForeground(MAU_CHINH_NHAT);

        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 4));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(chao);

        banner.add(chu, BorderLayout.WEST);
        return banner;
    }

    private JPanel taoHangThongKe() {
        JPanel hang = new JPanel(new GridLayout(1, 4, 12, 0));
        hang.setOpaque(false);
        hang.add(taoTheThongKe("Tổng bệnh nhân", soTongBN, MAU_CHINH));
        hang.add(taoTheThongKe("Chờ khám hôm nay", soChoKham, CANH_BAO));
        hang.add(taoTheThongKe("Đã hoàn thành hôm nay", soHoanThanh, THANH_CONG));
        hang.add(taoTheThongKe("Lịch hẹn hôm nay", soLichHen, Color.decode("#8B5CF6")));
        return hang;
    }

    private JPanel taoTheThongKe(String nhan, JLabel so, Color mau) {
        JPanel the = new JPanel(new BorderLayout(0, 2));
        the.setBackground(NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mau),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14))));

        JLabel tenThe = new JLabel(nhan);
        tenThe.setFont(font(Font.PLAIN, 13));
        tenThe.setForeground(CHU_PHU);
        so.setFont(font(Font.BOLD, 26));
        so.setForeground(CHU_CHINH);

        the.add(tenThe, BorderLayout.NORTH);
        the.add(so, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoKhamVaDieuTri() {
        JPanel the = GiaoDien.taoThe("Khám & điều trị",
                "Danh sách phiếu khám hôm nay (chỉ xem). Thao tác bắt đầu/hoàn thành/hủy phiếu ở màn Khám bệnh.");

        cbTruongLoc.setToolTipText("Chọn tiêu chí cần lọc");
        oTimKiem.setToolTipText("Nhập nội dung cần tìm theo tiêu chí đã chọn");
        oTimKiem.setFont(font(Font.PLAIN, 13));
        oTimKiem.setPreferredSize(new Dimension(220, 38));
        oTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        GiaoDien.kieuCombo(cbTruongLoc);
        cbTruongLoc.setPreferredSize(new Dimension(170, 38));
        oTimKiem.addActionListener(e -> locBang());
        bangPhieu.setRowSorter(boLoc);
        int[] doRong = {45, 75, 70, 175, 90, 100, 150};
        for (int i = 0; i < doRong.length; i++) {
            bangPhieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bangPhieu.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());

        JLabel nhanTruong = new JLabel("Lọc theo:");
        nhanTruong.setFont(font(Font.BOLD, 12));
        nhanTruong.setForeground(CHU_PHU);
        JLabel nhanGiaTri = new JLabel("Tìm kiếm:");
        nhanGiaTri.setFont(font(Font.BOLD, 12));
        nhanGiaTri.setForeground(CHU_PHU);

        JButton nutTim = taoNut("Tìm kiếm", false);
        nutTim.addActionListener(e -> locBang());
        JButton nutXoaLoc = GiaoDien.taoNutTrang("Xóa lọc");
        nutXoaLoc.addActionListener(e -> {
            cbTruongLoc.setSelectedIndex(0);
            oTimKiem.setText("");
            locBang();
        });

        JButton nutLamMoi = GiaoDien.taoNutTrang("Làm mới");
        nutLamMoi.setToolTipText("Tải lại danh sách từ cơ sở dữ liệu");
        nutLamMoi.addActionListener(e -> taiDuLieu());

        JPanel khungLoc = new JPanel(new GridBagLayout());
        khungLoc.setBackground(NEN_APP);
        khungLoc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.insets = new Insets(0, 0, 4, 10); khungLoc.add(nhanTruong, c);
        c.gridx = 1; khungLoc.add(nhanGiaTri, c);
        c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 0; c.insets = new Insets(0, 0, 0, 10); khungLoc.add(cbTruongLoc, c);
        c.gridx = 1; c.weightx = 1; khungLoc.add(oTimKiem, c);
        c.gridx = 2; c.weightx = 0; c.insets = new Insets(0, 0, 0, 8); khungLoc.add(nutTim, c);
        c.gridx = 3; c.insets = new Insets(0, 0, 0, 8); khungLoc.add(nutXoaLoc, c);
        c.gridx = 4; c.insets = new Insets(0, 0, 0, 0); khungLoc.add(nutLamMoi, c);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        JPanel phanDau = new JPanel(new BorderLayout(0, 8));
        phanDau.setOpaque(false);
        phanDau.add(khungLoc, BorderLayout.NORTH);
        giua.add(phanDau, BorderLayout.NORTH);
        giua.add(new JScrollPane(bangPhieu), BorderLayout.CENTER);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        int tongBN;
        int lichHenHomNay;
        try {
            dsPhieu = phieuKhamDao.findHomNay();
            tongBN = benhNhanDao.demDangHoatDong();
            lichHenHomNay = lichHenDao.findTheoNgay(LocalDate.now()).size();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        modelPhieu.setRowCount(0);
        int choKham = 0;
        int hoanThanh = 0;
        for (PhieuKham pk : dsPhieu) {
            modelPhieu.addRow(new Object[]{pk.getSoThuTu(), String.format("PK%03d", pk.getMaPK()),
                    String.format("BN%03d", pk.getMaBN()), pk.getTenBenhNhan(),
                    pk.getTenPhong() == null ? "" : pk.getTenPhong(), pk.getTrangThai(),
                    pk.getGhiChu() == null ? "" : pk.getGhiChu()});
            if (PhieuKhamDao.CHO_KHAM.equals(pk.getTrangThai())) choKham++;
            if (PhieuKhamDao.HOAN_THANH.equals(pk.getTrangThai())) hoanThanh++;
        }
        soTongBN.setText(String.valueOf(tongBN));
        soChoKham.setText(String.valueOf(choKham));
        soHoanThanh.setText(String.valueOf(hoanThanh));
        soLichHen.setText(String.valueOf(lichHenHomNay));
        locBang();
    }

    private int cotTheoTruong(String truong) {
        if ("Mã phiếu".equals(truong)) return 1;
        if ("Mã BN".equals(truong)) return 2;
        if ("Bệnh nhân".equals(truong)) return 3;
        if ("Phòng khám".equals(truong)) return 4;
        if ("Trạng thái".equals(truong)) return 5;
        return -1;
    }

    private void locBang() {
        String truong = (String) cbTruongLoc.getSelectedItem();
        String tuKhoa = oTimKiem.getText() == null ? "" : oTimKiem.getText().trim();
        int cot = cotTheoTruong(truong);
        if (tuKhoa.isEmpty()) {
            boLoc.setRowFilter(null);
        } else if (cot < 0) {
            boLoc.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa)));
        } else {
            boLoc.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), cot));
        }
    }

    private static final class TrangThaiRenderer extends GiaoDien.OCoBanRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String tt = value == null ? "" : value.toString();
            if (!isSelected) {
                if (PhieuKhamDao.CHO_KHAM.equals(tt)) c.setForeground(Color.decode("#B45309"));
                else if (PhieuKhamDao.DANG_KHAM.equals(tt)) c.setForeground(MAU_CHINH);
                else if (PhieuKhamDao.HOAN_THANH.equals(tt)) c.setForeground(Color.decode("#047857"));
                else if (PhieuKhamDao.DA_HUY.equals(tt)) c.setForeground(Color.decode("#B91C1C"));
            }
            return c;
        }
    }
}
