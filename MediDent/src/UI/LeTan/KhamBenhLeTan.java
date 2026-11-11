package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.KetQuaKhamDao;
import Dao.PhieuKhamDao;
import Entity.BenhNhan;
import Entity.KetQuaKham;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Khám bệnh": danh sách phiếu khám và hồ sơ kết quả khám. */
public class KhamBenhLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final KetQuaKhamDao ketQuaKhamDao = new KetQuaKhamDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();
    private PhieuKham dangChon;
    private boolean dangNap;

    private final GiaoDien.ONgay oNgay = new GiaoDien.ONgay(LocalDate.now());
    private final JComboBox<String> cbTruongLoc = new JComboBox<>(new String[]{
            "Tất cả", "Mã phiếu", "Bệnh nhân", "Phòng", "Trạng thái"
    });
    private final JTextField oTimKiem = taoOText();
    private final JLabel soLuong = new JLabel("0 phiếu");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Bệnh nhân", "Phòng", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final TableRowSorter<DefaultTableModel> boLoc = new TableRowSorter<>(model);

    private final JButton nutBatDau = taoNut("Bắt đầu khám", true);
    private final JButton nutHoanThanh = taoNut("Hoàn thành", true);
    private final JButton nutIn = taoNut("In phiếu khám", true);

    public KhamBenhLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(taoBanner(), BorderLayout.NORTH);
        add(taoDanhSach(), BorderLayout.CENTER);

        taiDuLieu();
        // Đổi ngày khám (gõ tay hoặc chọn trên lịch) thì danh sách tự tải lại.
        oNgay.oNhap().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { khiDoiNgay(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { khiDoiNgay(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { khiDoiNgay(); }
        });
    }

    private JPanel taoBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(NEN_THE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel tieuDe = new JLabel("Khám bệnh & hồ sơ kết quả");
        tieuDe.setFont(font(Font.BOLD, 19));
        tieuDe.setForeground(CHU_CHINH);
        JLabel moTa = new JLabel("Chọn phiếu khám trong danh sách, rồi bắt đầu khám, hoàn thành hoặc in phiếu.");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 2));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(moTa);

        oNgay.setPreferredSize(new Dimension(190, 34));
        JPanel loc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        loc.setOpaque(false);
        loc.add(nhan("Ngày khám:"));
        loc.add(oNgay);

        banner.add(chu, BorderLayout.WEST);
        banner.add(loc, BorderLayout.EAST);
        return banner;
    }

    private JLabel nhan(String chu) {
        JLabel l = new JLabel(chu);
        l.setFont(font(Font.PLAIN, 13));
        l.setForeground(CHU_PHU);
        return l;
    }

    private JPanel taoDanhSach() {
        JPanel the = GiaoDien.taoThe("Phiếu khám trong ngày",
                "Chọn phiếu khám rồi dùng các nút bên dưới để bắt đầu khám, hoàn thành hoặc in phiếu.");

        cbTruongLoc.setToolTipText("Chọn tiêu chí cần tìm");
        cbTruongLoc.setFont(font(Font.PLAIN, 13));
        cbTruongLoc.setPreferredSize(new Dimension(145, 34));
        oTimKiem.setToolTipText("Nhập nội dung cần tìm theo tiêu chí đã chọn");
        oTimKiem.setPreferredSize(new Dimension(200, 34));
        oTimKiem.addActionListener(e -> locBang());
        bang.setRowSorter(boLoc);
        int[] doRong = {45, 80, 170, 100, 105};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bang.getColumnModel().getColumn(4).setCellRenderer(new TrangThaiRenderer());
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) chonDong();
        });

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel traiLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        traiLoc.setOpaque(false);
        traiLoc.add(nhan("Lọc theo:"));
        traiLoc.add(cbTruongLoc);
        hangTim.add(traiLoc, BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        JButton nutTim = taoNut("Tìm kiếm", false);
        nutTim.addActionListener(e -> locBang());
        JButton nutXoaLoc = taoNutTrang("Xóa lọc");
        nutXoaLoc.addActionListener(e -> {
            cbTruongLoc.setSelectedIndex(0);
            oTimKiem.setText("");
            locBang();
        });
        JButton nutTaoPhieu = taoNut("+ Tạo phiếu khám", true);
        nutTaoPhieu.addActionListener(e -> taoPhieuKham());
        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);
        JPanel phaiTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phaiTim.setOpaque(false);
        phaiTim.add(nutTim);
        phaiTim.add(nutXoaLoc);
        phaiTim.add(nutTaoPhieu);
        phaiTim.add(soLuong);
        hangTim.add(phaiTim, BorderLayout.EAST);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(hangTim, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        giua.add(cuon, BorderLayout.CENTER);

        nutBatDau.addActionListener(e -> batDauKham());
        nutHoanThanh.addActionListener(e -> hoanThanhKham());
        nutIn.addActionListener(e -> inPhieuKham());
        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hangNut.add(nutBatDau);
        hangNut.add(nutHoanThanh);
        hangNut.add(nutIn);
        giua.add(hangNut, BorderLayout.SOUTH);

        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    // ===================== Dữ liệu =====================

    private void khiDoiNgay() {
        String chu = oNgay.getText() == null ? "" : oNgay.getText().trim();
        if (chu.length() != 10) return;
        try {
            if (docNgay(chu) == null) return;
        } catch (DateTimeParseException e) {
            return;
        }
        SwingUtilities.invokeLater(this::taiDuLieu);
    }

    public void taiDuLieu() {
        LocalDate ngay;
        try {
            ngay = docNgay(oNgay.getText());
            if (ngay == null) ngay = LocalDate.now();
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
            return;
        }

        int maDangChon = dangChon == null ? -1 : dangChon.getMaPK();
        try {
            dsPhieu = phieuKhamDao.findTheoNgay(ngay);
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }

        dangNap = true;
        try {
            model.setRowCount(0);
            int dongChon = -1;
            for (int i = 0; i < dsPhieu.size(); i++) {
                PhieuKham pk = dsPhieu.get(i);
                model.addRow(new Object[]{
                        pk.getSoThuTu(),
                        String.format("PK%03d", pk.getMaPK()),
                        pk.getTenBenhNhan(),
                        pk.getTenPhong() == null ? "—" : pk.getTenPhong(),
                        pk.getTrangThai()
                });
                if (pk.getMaPK() == maDangChon) dongChon = i;
            }
            soLuong.setText(dsPhieu.size() + " phiếu");
            if (dongChon >= 0) {
                bang.setRowSelectionInterval(dongChon, dongChon);
            } else if (!dsPhieu.isEmpty()) {
                bang.setRowSelectionInterval(0, 0);
            } else {
                xoaForm();
            }
        } finally {
            dangNap = false;
        }
        if (!dsPhieu.isEmpty()) {
            chonDong();
        }
    }

    /** Tạo phiếu khám trực tiếp cho bệnh nhân đến khám không có lịch hẹn. */
    private void taoPhieuKham() {
        PhieuKham pk = PhieuKhamDialog.taoMoi(this);
        if (pk == null) return;
        dangChon = pk;
        oNgay.setText(LocalDate.now().format(NGAY));
        taiDuLieu();
        try {
            BenhNhan bn = benhNhanDao.findById(pk.getMaBN());
            String thongTin = "Đã tạo phiếu khám PK" + String.format("%03d", pk.getMaPK())
                    + "\nBệnh nhân: " + pk.getTenBenhNhan()
                    + "\nPhòng: " + pk.getTenPhong() + "  ·  Số thứ tự: " + pk.getSoThuTu()
                    + "\n\nIn phiếu khám ngay bây giờ?";
            if (xacNhan(this, thongTin, "Tạo phiếu khám thành công")) {
                XuatPhieuKham.xuat(this, pk, bn, null);
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void locBang() {
        String truong = (String) cbTruongLoc.getSelectedItem();
        String tuKhoa = oTimKiem.getText() == null ? "" : oTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            boLoc.setRowFilter(null);
            soLuong.setText(bang.getRowCount() + " phiếu");
            return;
        }
        int cot;
        if ("Mã phiếu".equals(truong)) cot = 1;
        else if ("Bệnh nhân".equals(truong)) cot = 2;
        else if ("Phòng".equals(truong)) cot = 3;
        else if ("Trạng thái".equals(truong)) cot = 4;
        else cot = -1;
        if (cot < 0) {
            boLoc.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        } else {
            boLoc.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa), cot));
        }
        soLuong.setText(bang.getRowCount() + " phiếu");
    }

    private void chonDong() {
        if (dangNap) return;
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            dangChon = null;
            xoaForm();
            return;
        }
        int dongModel = bang.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= dsPhieu.size()) return;
        chonPhieu(dsPhieu.get(dongModel));
    }

    private void chonPhieu(PhieuKham pk) {
        dangChon = pk;
        capNhatNut();
    }

    private void capNhatNut() {
        boolean coPhieu = dangChon != null;
        boolean daKetThuc = coPhieu && dangChon.daKetThuc();
        nutBatDau.setEnabled(coPhieu && dangChon.choKham());
        nutHoanThanh.setEnabled(coPhieu && !daKetThuc);
        nutIn.setEnabled(coPhieu);
    }

    private void xoaForm() {
        dangChon = null;
        capNhatNut();
    }

    // ===================== Thao tác =====================

    private void batDauKham() {
        if (dangChon == null) return;
        try {
            phieuKhamDao.batDauKham(dangChon.getMaPK());
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void hoanThanhKham() {
        if (dangChon == null) return;
        if (!xacNhan(this, "Hoàn thành phiếu khám của " + dangChon.getTenBenhNhan() + "?",
                "Hoàn thành khám")) {
            return;
        }
        try {
            phieuKhamDao.hoanThanh(dangChon.getMaPK());
            thongBao(this, "Đã hoàn thành phiếu khám.");
            taiDuLieu();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private void inPhieuKham() {
        if (dangChon == null) return;
        try {
            BenhNhan bn = benhNhanDao.findById(dangChon.getMaBN());
            KetQuaKham kq = ketQuaKhamDao.findByMaPK(dangChon.getMaPK());
            XuatPhieuKham.xuat(this, dangChon, bn, kq);
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }

    private static Color mauTrangThai(String tt) {
        if (PhieuKhamDao.CHO_KHAM.equals(tt)) return Color.decode("#B45309");
        if (PhieuKhamDao.DANG_KHAM.equals(tt)) return MAU_CHINH;
        if (PhieuKhamDao.HOAN_THANH.equals(tt)) return Color.decode("#047857");
        if (PhieuKhamDao.DA_HUY.equals(tt)) return Color.decode("#B91C1C");
        return CHU_PHU;
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
