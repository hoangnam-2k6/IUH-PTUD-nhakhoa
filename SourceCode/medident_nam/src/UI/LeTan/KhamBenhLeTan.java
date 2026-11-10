package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.HoaDonDao;
import Dao.KetQuaKhamDao;
import Dao.PhieuKhamDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.HoaDon;
import Entity.KetQuaKham;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import DAO.BacSiDAO;

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
    private final PhieuKhamDAO phieuKhamDao = new PhieuKhamDAO();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final BacSiDAO bacSiDao = new BacSiDAO();
    private final KetQuaKhamDAO ketQuaKhamDao = new KetQuaKhamDAO();
    private final HoaDonDao hoaDonDao = new HoaDonDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();
    private List<BacSi> dsBacSi = new ArrayList<>();
    private PhieuKham dangChon;
    private boolean dangNap;

    private final JTextField oNgay = taoOText();
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

    private final JLabel lblBenhNhan = new JLabel("Chưa chọn phiếu khám");
    private final JLabel lblThongTin = new JLabel(" ");
    private final JLabel lblDiUng = new JLabel(" ");
    private final JLabel lblTrangThai = new JLabel("—");

    private final JComboBox<BacSi> cbBacSi = new JComboBox<>();
    private final JTextField oNgayTaiKham = taoOText();
    private final JTextArea oTrieuChung = taoOVanBan(3);
    private final JTextArea oKetQua = taoOVanBan(4);
    private final JTextArea oChanDoan = taoOVanBan(3);
    private final JTextArea oHuongDieuTri = taoOVanBan(3);
    private final JTextArea oDonThuoc = taoOVanBan(3);
    private final JTextArea oGhiChu = taoOVanBan(2);

    private final JButton nutLuu = taoNut("Lưu kết quả", MAU_CHINH, MAU_CHINH_DAM);
    private final JButton nutBatDau = taoNut("Bắt đầu khám", Color.decode("#0EA5E9"), Color.decode("#0284C7"));
    private final JButton nutHoanThanh = taoNut("Hoàn thành", THANH_CONG, THANH_CONG.darker());
    private final JButton nutLapHoaDon = taoNut("Lập hóa đơn", Color.decode("#7C3AED"), Color.decode("#6D28D9"));
    private final JButton nutIn = taoNutTrang("In phiếu khám");

    public KhamBenhLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(taoBanner(), BorderLayout.NORTH);
        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoDanhSach(), taoKetQua());
        chia.setResizeWeight(0.40);
        chia.setDividerSize(12);
        chia.setBorder(null);
        chia.setOpaque(false);
        add(chia, BorderLayout.CENTER);

        taiBacSi();
        taiDuLieu();
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
        JLabel moTa = new JLabel("Chọn phiếu khám bên trái, nhập kết quả và cập nhật trạng thái điều trị.");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 2));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(moTa);

        oNgay.setText(LocalDate.now().format(NGAY));
        oNgay.setPreferredSize(new Dimension(120, 32));
        oNgay.setToolTipText("dd/MM/yyyy");
        JButton nutTai = taoNutTrang("Tải danh sách");
        nutTai.addActionListener(e -> taiDuLieu());
        JPanel loc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        loc.setOpaque(false);
        loc.add(nhan("Ngày khám:"));
        loc.add(oNgay);
        loc.add(nutTai);

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
                "Sửa trạng thái bằng các nút bên dưới. Bấm đúp để mở nhanh kết quả.");

        oTimKiem.setToolTipText("Tên bệnh nhân, mã phiếu, phòng hoặc trạng thái");
        oTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locBang(); }
            @Override public void removeUpdate(DocumentEvent e) { locBang(); }
            @Override public void changedUpdate(DocumentEvent e) { locBang(); }
        });
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
        hangTim.add(nhan("Tìm kiếm:"), BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);
        hangTim.add(soLuong, BorderLayout.EAST);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(hangTim, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(VIEN));
        giua.add(cuon, BorderLayout.CENTER);

        nutBatDau.addActionListener(e -> batDauKham());
        nutHoanThanh.addActionListener(e -> hoanThanhKham());
        nutIn.addActionListener(e -> inPhieuKham());
        nutLapHoaDon.addActionListener(e -> lapHoaDon());
        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hangNut.add(nutBatDau);
        hangNut.add(nutHoanThanh);
        hangNut.add(nutIn);
        hangNut.add(nutLapHoaDon);
        giua.add(hangNut, BorderLayout.SOUTH);

        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoKetQua() {
        JPanel the = GiaoDien.taoThe("Hồ sơ kết quả khám",
                "Thông tin do bác sĩ/lễ tân nhập sau khi khám. Lưu lại để in cùng phiếu khám.");

        lblBenhNhan.setFont(font(Font.BOLD, 16));
        lblBenhNhan.setForeground(CHU_CHINH);
        lblThongTin.setFont(font(Font.PLAIN, 12));
        lblThongTin.setForeground(CHU_PHU);
        lblDiUng.setFont(font(Font.BOLD, 12));
        lblDiUng.setForeground(Color.decode("#B91C1C"));
        lblTrangThai.setFont(font(Font.BOLD, 12));

        JPanel thongTinBenh = new JPanel(new GridLayout(0, 1, 0, 3));
        thongTinBenh.setOpaque(false);
        thongTinBenh.add(lblBenhNhan);
        thongTinBenh.add(lblThongTin);
        JPanel hangTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        hangTrangThai.setOpaque(false);
        hangTrangThai.add(lblTrangThai);
        hangTrangThai.add(lblDiUng);
        thongTinBenh.add(hangTrangThai);

        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        dau.add(thongTinBenh, BorderLayout.CENTER);
        the.add(dau, BorderLayout.NORTH);

        cbBacSi.setFont(font(Font.PLAIN, 14));

        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setOpaque(false);
        noi.add(truong("Bác sĩ điều trị", cbBacSi));
        noi.add(truong("Triệu chứng", boc(oTrieuChung)));
        noi.add(truong("Kết quả lâm sàng", boc(oKetQua)));
        noi.add(truong("Chẩn đoán", boc(oChanDoan)));
        noi.add(truong("Hướng điều trị", boc(oHuongDieuTri)));
        noi.add(truong("Đơn thuốc", boc(oDonThuoc)));
        noi.add(truong("Ghi chú", boc(oGhiChu)));
        noi.add(truong("Ngày tái khám (dd/MM/yyyy, có thể để trống)", oNgayTaiKham));

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        the.add(cuon, BorderLayout.CENTER);

        nutLuu.addActionListener(e -> {
            if (luuKetQua(true)) {
                taiDuLieu();
            }
        });
        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hangNut.add(nutLuu);
        the.add(hangNut, BorderLayout.SOUTH);
        return the;
    }

    // ===================== Dữ liệu =====================

    private void taiBacSi() {
        try {
            dsBacSi = bacSiDao.findDangHoatDong();
        } catch (Exception e) {
            baoLoi(this, e);
            dsBacSi = new ArrayList<>();
        }
        cbBacSi.removeAllItems();
        for (BacSi bs : dsBacSi) cbBacSi.addItem(bs);
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

    private void locBang() {
        String tuKhoa = oTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            boLoc.setRowFilter(null);
        } else {
            boLoc.setRowFilter(RowFilter.regexFilter("(?iu)" + Pattern.quote(tuKhoa)));
        }
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
        lblBenhNhan.setText(pk.getTenBenhNhan() == null ? "—" : pk.getTenBenhNhan());
        lblThongTin.setText(String.format("PK%03d", pk.getMaPK())
                + "  ·  STT " + pk.getSoThuTu()
                + "  ·  " + (pk.getTenPhong() == null ? "Chưa phân phòng" : pk.getTenPhong())
                + "  ·  " + (pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY)));
        lblTrangThai.setText(pk.getTrangThai());
        lblTrangThai.setForeground(mauTrangThai(pk.getTrangThai()));

        try {
            BenhNhan bn = benhNhanDao.findById(pk.getMaBN());
            if (bn != null && bn.getDiUng() != null && !bn.getDiUng().isBlank()) {
                lblDiUng.setText("⚠ Dị ứng: " + bn.getDiUng());
            } else {
                lblDiUng.setText(" ");
            }
        } catch (Exception e) {
            lblDiUng.setText(" ");
        }

        KetQuaKham kq;
        try {
            kq = ketQuaKhamDao.findByMaPK(pk.getMaPK());
        } catch (Exception e) {
            baoLoi(this, e);
            kq = null;
        }
        dangNap = true;
        try {
            if (kq == null) {
                oTrieuChung.setText("");
                oKetQua.setText("");
                oChanDoan.setText("");
                oHuongDieuTri.setText("");
                oDonThuoc.setText("");
                oGhiChu.setText("");
                oNgayTaiKham.setText("");
                cbBacSi.setSelectedItem(null);
            } else {
                oTrieuChung.setText(nz(kq.getTrieuChung()));
                oKetQua.setText(nz(kq.getKetQuaLamSang()));
                oChanDoan.setText(nz(kq.getChanDoan()));
                oHuongDieuTri.setText(nz(kq.getHuongDieuTri()));
                oDonThuoc.setText(nz(kq.getDonThuoc()));
                oGhiChu.setText(nz(kq.getGhiChu()));
                oNgayTaiKham.setText(kq.getNgayTaiKham() == null ? "" : kq.getNgayTaiKham().format(NGAY));
                chonBacSi(kq.getMaBS());
            }
        } finally {
            dangNap = false;
        }
        capNhatNut();
    }

    private void chonBacSi(Integer maBS) {
        if (maBS == null) {
            cbBacSi.setSelectedItem(null);
            return;
        }
        for (int i = 0; i < cbBacSi.getItemCount(); i++) {
            if (cbBacSi.getItemAt(i).getMaBS() == maBS) {
                cbBacSi.setSelectedIndex(i);
                return;
            }
        }
        cbBacSi.setSelectedItem(null);
    }

    private void capNhatNut() {
        boolean coPhieu = dangChon != null;
        boolean daKetThuc = coPhieu && dangChon.daKetThuc();
        nutLuu.setEnabled(coPhieu && !daKetThuc);
        nutBatDau.setEnabled(coPhieu && dangChon.choKham());
        nutHoanThanh.setEnabled(coPhieu && !daKetThuc);
        nutIn.setEnabled(coPhieu);
        nutLapHoaDon.setEnabled(coPhieu && !dangChon.daHuy());
        setFormEnabled(coPhieu && !daKetThuc);
    }

    private void setFormEnabled(boolean bat) {
        cbBacSi.setEnabled(bat);
        oNgayTaiKham.setEnabled(bat);
        for (JTextArea a : new JTextArea[]{oTrieuChung, oKetQua, oChanDoan, oHuongDieuTri, oDonThuoc, oGhiChu}) {
            a.setEnabled(bat);
        }
    }

    private void xoaForm() {
        dangNap = true;
        try {
            lblBenhNhan.setText("Chưa chọn phiếu khám");
            lblThongTin.setText(" ");
            lblDiUng.setText(" ");
            lblTrangThai.setText("—");
            lblTrangThai.setForeground(CHU_PHU);
            oTrieuChung.setText("");
            oKetQua.setText("");
            oChanDoan.setText("");
            oHuongDieuTri.setText("");
            oDonThuoc.setText("");
            oGhiChu.setText("");
            oNgayTaiKham.setText("");
            cbBacSi.setSelectedItem(null);
        } finally {
            dangNap = false;
        }
        dangChon = null;
        capNhatNut();
    }

    // ===================== Thao tác =====================

    private boolean luuKetQua(boolean hienThongBao) {
        if (dangChon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu khám.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (dangChon.daKetThuc()) {
            JOptionPane.showMessageDialog(this, "Phiếu khám đã kết thúc, không thể sửa kết quả.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        try {
            LocalDate ngayTaiKham = docNgay(oNgayTaiKham.getText());
            KetQuaKham kq = new KetQuaKham();
            kq.setMaPK(dangChon.getMaPK());
            BacSi bs = (BacSi) cbBacSi.getSelectedItem();
            kq.setMaBS(bs == null ? null : bs.getMaBS());
            kq.setTrieuChung(oTrieuChung.getText());
            kq.setKetQuaLamSang(oKetQua.getText());
            kq.setChanDoan(oChanDoan.getText());
            kq.setHuongDieuTri(oHuongDieuTri.getText());
            kq.setDonThuoc(oDonThuoc.getText());
            kq.setGhiChu(oGhiChu.getText());
            kq.setNgayTaiKham(ngayTaiKham);
            ketQuaKhamDao.luu(kq);

            if (dangChon.choKham()) {
                phieuKhamDao.batDauKham(dangChon.getMaPK());
            }
            if (hienThongBao) {
                thongBao(this, "Đã lưu kết quả khám cho " + dangChon.getTenBenhNhan() + ".");
            }
            return true;
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
        } catch (Exception e) {
            baoLoi(this, e);
        }
        return false;
    }

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
        if (!xacNhan(this, "Hoàn thành phiếu khám của " + dangChon.getTenBenhNhan()
                + "?\nKết quả đang nhập trên form sẽ được lưu trước khi hoàn thành.", "Hoàn thành khám")) {
            return;
        }
        if (!luuKetQua(false)) return;
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

    private void lapHoaDon() {
        if (dangChon == null) return;
        if (dangChon.daHuy()) {
            JOptionPane.showMessageDialog(this, "Phiếu khám đã hủy, không thể lập hóa đơn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            HoaDon daCo = hoaDonDao.findByMaPK(dangChon.getMaPK());
            if (daCo != null) {
                JOptionPane.showMessageDialog(this,
                        "Phiếu khám này đã có hóa đơn " + String.format("HD%03d", daCo.getMaHD())
                                + " (" + daCo.getTrangThai() + ").\nVào mục Thanh toán & hóa đơn để xử lý.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (Exception e) {
            baoLoi(this, e);
            return;
        }
        if (HoaDonDialog.taoMoi(this, taiKhoan, dangChon)) {
            thongBao(this, "Đã lập hóa đơn. Vào mục Thanh toán & hóa đơn để thu tiền hoặc in.");
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static Color mauTrangThai(String tt) {
        if (PhieuKhamDAO.CHO_KHAM.equals(tt)) return Color.decode("#B45309");
        if (PhieuKhamDAO.DANG_KHAM.equals(tt)) return MAU_CHINH;
        if (PhieuKhamDAO.HOAN_THANH.equals(tt)) return Color.decode("#047857");
        if (PhieuKhamDAO.DA_HUY.equals(tt)) return Color.decode("#B91C1C");
        return CHU_PHU;
    }

    private static final class TrangThaiRenderer extends DefaultTableCellRenderer {
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
