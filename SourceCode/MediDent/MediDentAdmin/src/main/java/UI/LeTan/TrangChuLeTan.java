package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Dao.PhieuKhamDao;
import Dao.PhongKhamDao;
import Entity.BenhNhan;
import Entity.PhieuKham;
import Entity.PhongKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static UI.LeTan.GiaoDien.*;

public class TrangChuLeTan extends JPanel {
    private final TaiKhoan taiKhoan;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final PhongKhamDao phongKhamDao = new PhongKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final LichHenDao lichHenDao = new LichHenDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();
    private List<PhongKham> dsPhong = new ArrayList<>();
    private boolean dangNap = false;

    private final JLabel soTongBN = new JLabel("0");
    private final JLabel soChoKham = new JLabel("0");
    private final JLabel soHoanThanh = new JLabel("0");
    private final JLabel soLichHen = new JLabel("0");

    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Phòng khám", "Trạng thái", "Ghi chú"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 4 || column == 5;
        }
    };
    private final JTable bangPhieu = GiaoDien.taoBang(modelPhieu);
    private final TableRowSorter<DefaultTableModel> boLoc = new TableRowSorter<>(modelPhieu);
    private final JTextField oTimKiem = GiaoDien.taoOText();
    private final JLabel soLuongPhieu = new JLabel("0 phiếu");

    private final JTextField oTen = GiaoDien.taoOText();
    private final JTextField oNgaySinh = GiaoDien.taoOText();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField oSdt = GiaoDien.taoOText();
    private final JTextField oDiaChi = GiaoDien.taoOText();
    private final JTextField oCccd = GiaoDien.taoOText();
    private final JTextField oEmail = GiaoDien.taoOText();
    private final JTextField oNgheNghiep = GiaoDien.taoOText();
    private final JTextField oTenLienHe = GiaoDien.taoOText();
    private final JTextField oSdtLienHe = GiaoDien.taoOText();
    private final JTextArea oDiUng = GiaoDien.taoOVanBan(2);
    private final JTextArea oTienSu = GiaoDien.taoOVanBan(2);
    private final JComboBox<PhongKham> cbPhong = new JComboBox<>();

    public TrangChuLeTan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel phanTren = new JPanel(new BorderLayout(0, 12));
        phanTren.setOpaque(false);
        phanTren.add(taoBanner(), BorderLayout.NORTH);
        phanTren.add(taoHangThongKe(), BorderLayout.CENTER);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoKhamVaDieuTri(), taoTiepNhan());
        chia.setResizeWeight(0.62);
        chia.setDividerSize(14);
        chia.setBorder(null);
        chia.setOpaque(false);

        add(phanTren, BorderLayout.NORTH);
        add(chia, BorderLayout.CENTER);

        modelPhieu.addTableModelListener(e -> {
            if (dangNap || e.getType() != TableModelEvent.UPDATE) return;
            int dong = e.getFirstRow();
            if (dong < 0 || dong >= dsPhieu.size()) return;
            luuSuaTrucTiep(dong);
        });
    }

    // ===================== Xây dựng giao diện =====================

    private JPanel taoBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(MAU_CHINH);
        banner.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JLabel tieuDe = new JLabel("Tiếp nhận & Khám điều trị");
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

        JButton nutLamMoi = taoNut("Làm mới", Color.decode("#0EA5E9"), Color.decode("#38BDF8"));
        nutLamMoi.addActionListener(e -> taiDuLieu());
        JPanel goc = new JPanel(new GridBagLayout());
        goc.setOpaque(false);
        goc.add(nutLamMoi);

        banner.add(chu, BorderLayout.WEST);
        banner.add(goc, BorderLayout.EAST);
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
                "Danh sách phiếu khám hôm nay. Sửa trực tiếp ô Phòng khám / Trạng thái hoặc dùng các nút bên dưới.");

        oTimKiem.setToolTipText("Gõ tên bệnh nhân, mã phiếu, phòng hoặc trạng thái");
        oTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locBang(); }
            @Override public void removeUpdate(DocumentEvent e) { locBang(); }
            @Override public void changedUpdate(DocumentEvent e) { locBang(); }
        });
        bangPhieu.setRowSorter(boLoc);
        int[] doRong = {45, 75, 70, 175, 90, 100, 150};
        for (int i = 0; i < doRong.length; i++) {
            bangPhieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        bangPhieu.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiRenderer());

        JLabel nhanTim = new JLabel("Tìm kiếm:");
        nhanTim.setFont(font(Font.PLAIN, 13));
        nhanTim.setForeground(CHU_PHU);
        soLuongPhieu.setFont(font(Font.BOLD, 12));
        soLuongPhieu.setForeground(MAU_CHINH);

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        hangTim.add(nhanTim, BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(soLuongPhieu, BorderLayout.EAST);

        JButton nutBatDau = taoNut("Bắt đầu khám", MAU_CHINH, MAU_CHINH_DAM);
        JButton nutHoanThanh = taoNut("Hoàn thành", THANH_CONG, THANH_CONG.darker());
        JButton nutHuy = taoNut("Hủy phiếu", Color.decode("#DC2626"), Color.decode("#B91C1C"));
        JButton nutIn = taoNutTrang("In phiếu khám");
        nutBatDau.addActionListener(e -> batDauKham());
        nutHoanThanh.addActionListener(e -> hoanThanhKham());
        nutHuy.addActionListener(e -> huyPhieu());
        nutIn.addActionListener(e -> inPhieuKham());

        JPanel hangNut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangNut.setOpaque(false);
        hangNut.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        hangNut.add(nutBatDau);
        hangNut.add(nutHoanThanh);
        hangNut.add(nutHuy);
        hangNut.add(nutIn);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(hangTim, BorderLayout.NORTH);
        giua.add(new JScrollPane(bangPhieu), BorderLayout.CENTER);
        giua.add(hangNut, BorderLayout.SOUTH);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTiepNhan() {
        JPanel the = GiaoDien.taoThe("Tiếp nhận bệnh nhân", "Nhập thông tin, cấp số thứ tự và phân phòng khám");

        cbGioiTinh.setFont(font(Font.PLAIN, 14));
        JPanel hangNgaySinh = hai(truong("Ngày sinh (dd/MM/yyyy)", oNgaySinh), truong("Giới tính", cbGioiTinh));

        JPanel luoi = new JPanel();
        luoi.setLayout(new BoxLayout(luoi, BoxLayout.Y_AXIS));
        luoi.setOpaque(false);
        luoi.add(truong("Họ và tên *", oTen));
        luoi.add(hangNgaySinh);
        luoi.add(truong("Số điện thoại *", oSdt));
        luoi.add(truong("Địa chỉ", oDiaChi));
        luoi.add(hai(truong("CMND/CCCD", oCccd), truong("Nghề nghiệp", oNgheNghiep)));
        luoi.add(truong("Email", oEmail));
        luoi.add(hai(truong("Người liên hệ khẩn cấp", oTenLienHe), truong("SĐT người liên hệ", oSdtLienHe)));
        luoi.add(truong("Dị ứng (thuốc, vật liệu...)", GiaoDien.boc(oDiUng)));
        luoi.add(truong("Tiền sử bệnh", GiaoDien.boc(oTienSu)));

        JPanel boc = new JPanel(new BorderLayout());
        boc.setOpaque(false);
        boc.add(luoi, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(boc);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        the.add(cuon, BorderLayout.CENTER);

        JPanel duoi = new JPanel(new BorderLayout(0, 10));
        duoi.setOpaque(false);
        duoi.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        duoi.add(taoKhungPhong(), BorderLayout.NORTH);

        JButton nutNhapLai = taoNutTrang("Nhập lại");
        JButton nutTiepNhan = taoNut("Tiếp nhận / Cấp số", MAU_CHINH, MAU_CHINH_DAM);
        nutNhapLai.addActionListener(e -> nhapLai());
        nutTiepNhan.addActionListener(e -> tiepNhan());
        JPanel hangNut = new JPanel(new BorderLayout(10, 0));
        hangNut.setOpaque(false);
        hangNut.add(nutNhapLai, BorderLayout.WEST);
        hangNut.add(nutTiepNhan, BorderLayout.CENTER);
        duoi.add(hangNut, BorderLayout.CENTER);

        JLabel ghiChu = new JLabel("Hồ sơ trùng họ tên + SĐT sẽ được dùng lại, không tạo mới.");
        ghiChu.setFont(font(Font.PLAIN, 12));
        ghiChu.setForeground(CHU_PHU);
        duoi.add(ghiChu, BorderLayout.SOUTH);

        the.add(duoi, BorderLayout.SOUTH);
        return the;
    }

    private JPanel taoKhungPhong() {
        JPanel khung = new JPanel(new BorderLayout(10, 0));
        khung.setBackground(MAU_CHINH_NHAT);
        khung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#BAE6FD")),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel ten = new JLabel("Phòng khám *");
        ten.setFont(font(Font.BOLD, 13));
        ten.setForeground(CHU_CHINH);
        JLabel moTa = new JLabel("Chọn phòng còn chỗ trong ngày");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1));
        chu.setOpaque(false);
        chu.add(ten);
        chu.add(moTa);

        cbPhong.setFont(font(Font.PLAIN, 14));
        cbPhong.setPreferredSize(new Dimension(160, 32));
        khung.add(chu, BorderLayout.CENTER);
        khung.add(cbPhong, BorderLayout.EAST);
        return khung;
    }

    private JPanel truong(String nhan, JComponent oNhap) {
        return GiaoDien.truong(nhan, oNhap);
    }

    private JPanel hai(JComponent trai, JComponent phai) {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setOpaque(false);
        p.add(trai);
        p.add(phai);
        return p;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        int tongBN;
        int lichHenHomNay;
        try {
            dsPhong = phongKhamDao.findDangHoatDong();
            dsPhieu = phieuKhamDao.findHomNay();
            tongBN = benhNhanDao.demDangHoatDong();
            lichHenHomNay = lichHenDao.findTheoNgay(LocalDate.now()).size();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        dangNap = true;
        try {
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
            soLuongPhieu.setText(dsPhieu.size() + " phiếu");

            JComboBox<String> cbPhongTrongBang = new JComboBox<>();
            for (PhongKham p : dsPhong) cbPhongTrongBang.addItem(p.getTenPhong());
            bangPhieu.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cbPhongTrongBang));
            bangPhieu.getColumnModel().getColumn(5).setCellEditor(
                    new DefaultCellEditor(new JComboBox<>(PhieuKhamDao.TRANG_THAI)));

            PhongKham dangChon = (PhongKham) cbPhong.getSelectedItem();
            cbPhong.removeAllItems();
            for (PhongKham p : dsPhong) {
                cbPhong.addItem(p);
                if (dangChon != null && dangChon.getMaPhong() == p.getMaPhong()) cbPhong.setSelectedItem(p);
            }
        } finally {
            dangNap = false;
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

    private void nhapLai() {
        oTen.setText("");
        oNgaySinh.setText("");
        cbGioiTinh.setSelectedIndex(0);
        oSdt.setText("");
        oDiaChi.setText("");
        oCccd.setText("");
        oEmail.setText("");
        oNgheNghiep.setText("");
        oTenLienHe.setText("");
        oSdtLienHe.setText("");
        oDiUng.setText("");
        oTienSu.setText("");
        oTen.requestFocusInWindow();
    }

    /** Tìm hồ sơ cũ (trùng họ tên + SĐT) hoặc tạo mới, sau đó cấp số thứ tự và phân phòng. */
    private void tiepNhan() {
        String ten = oTen.getText().trim();
        String sdt = oSdt.getText().trim();
        if (ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên và số điện thoại.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PhongKham phong = (PhongKham) cbPhong.getSelectedItem();
        if (phong == null) {
            JOptionPane.showMessageDialog(this, "Chưa có phòng khám nào đang hoạt động.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BenhNhan bn = benhNhanDao.findByTenVaSdt(ten, sdt);
            boolean dungLaiHoSo = (bn != null);
            if (bn != null && !bn.dangHoatDong()) {
                if (!xacNhan(this, "Hồ sơ của " + bn.getHoTen() + " đang ngừng hoạt động. "
                        + "Kích hoạt lại để tiếp nhận?", "Hồ sơ ngừng hoạt động")) {
                    return;
                }
                benhNhanDao.doiTrangThai(bn.getMaBNSo(), BenhNhan.HOAT_DONG);
                bn.setTrangThai(BenhNhan.HOAT_DONG);
            }
            if (bn == null) {
                bn = new BenhNhan();
                bn.setHoTen(ten);
                bn.setSoDienThoai(sdt);
                String ngaySinh = oNgaySinh.getText().trim();
                if (!ngaySinh.isEmpty()) bn.setNgaySinh(docNgay(ngaySinh));
                bn.setGioiTinh((String) cbGioiTinh.getSelectedItem());
                bn.setDiaChi(oDiaChi.getText());
                bn.setCccd(oCccd.getText());
                bn.setEmail(oEmail.getText());
                bn.setNgheNghiep(oNgheNghiep.getText());
                bn.setTenNguoiLienHe(oTenLienHe.getText());
                bn.setSdtNguoiLienHe(oSdtLienHe.getText());
                bn.setDiUng(oDiUng.getText());
                bn.setTienSuBenh(oTienSu.getText());
                bn.setMaBN(benhNhanDao.them(bn));
            }

            PhieuKham pk = phieuKhamDao.capSoThuTu(bn.getMaBNSo(), phong.getMaPhong());
            String noiDung = "Đã cấp số thứ tự " + pk.getSoThuTu() + " cho " + bn.getHoTen()
                    + " (" + phong.getTenPhong() + ").\n"
                    + (dungLaiHoSo ? "Hồ sơ có sẵn được dùng lại (thông tin nhập mới không ghi đè hồ sơ cũ).\n"
                            + "Muốn cập nhật: vào mục Bệnh nhân → Sửa thông tin." : "Đã tạo hồ sơ bệnh nhân mới.")
                    + (bn.getDiUng() == null ? "" : "\n\n⚠ Bệnh nhân có dị ứng: " + bn.getDiUng());
            thongBao(this, noiDung);
            nhapLai();
            taiDuLieu();

            if (xacNhan(this, "In phiếu khám cho " + bn.getHoTen() + "?", "In phiếu khám")) {
                XuatPhieuKham.xuat(this, pk, bn, null);
            }
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy");
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    // ===================== Thao tác trên bảng =====================

    private void luuSuaTrucTiep(int dong) {
        PhieuKham pk = dsPhieu.get(dong);
        String tenPhong = String.valueOf(modelPhieu.getValueAt(dong, 4));
        String trangThai = String.valueOf(modelPhieu.getValueAt(dong, 5));
        String phongCu = pk.getTenPhong() == null ? "" : pk.getTenPhong();
        if (Objects.equals(tenPhong, phongCu) && Objects.equals(trangThai, pk.getTrangThai())) {
            return;
        }
        try {
            Integer maPhong = null;
            for (PhongKham p : dsPhong) {
                if (p.getTenPhong().equals(tenPhong)) maPhong = p.getMaPhong();
            }
            phieuKhamDao.capNhat(pk, maPhong, trangThai);
        } catch (Exception ex) {
            GiaoDien.baoLoi(this, ex);
        }
        SwingUtilities.invokeLater(this::taiDuLieu);
    }

    private PhieuKham phieuDangChon() {
        int dong = bangPhieu.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu khám trong bảng.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int dongModel = bangPhieu.convertRowIndexToModel(dong);
        if (dongModel < 0 || dongModel >= dsPhieu.size()) return null;
        return dsPhieu.get(dongModel);
    }

    private void batDauKham() {
        PhieuKham pk = phieuDangChon();
        if (pk == null) return;
        try {
            phieuKhamDao.batDauKham(pk.getMaPK());
            taiDuLieu();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void hoanThanhKham() {
        PhieuKham pk = phieuDangChon();
        if (pk == null) return;
        if (!xacNhan(this, "Hoàn thành phiếu khám của " + pk.getTenBenhNhan() + "?", "Hoàn thành khám")) return;
        try {
            phieuKhamDao.hoanThanh(pk.getMaPK());
            taiDuLieu();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void huyPhieu() {
        PhieuKham pk = phieuDangChon();
        if (pk == null) return;
        String lyDo = JOptionPane.showInputDialog(this, "Lý do hủy phiếu khám:", "Hủy phiếu khám",
                JOptionPane.WARNING_MESSAGE);
        if (lyDo == null) return;
        try {
            phieuKhamDao.huy(pk.getMaPK(), lyDo);
            taiDuLieu();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void inPhieuKham() {
        PhieuKham pk = phieuDangChon();
        if (pk == null) return;
        try {
            BenhNhan bn = benhNhanDao.findById(pk.getMaBN());
            XuatPhieuKham.xuat(this, pk, bn, null);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private static final class TrangThaiRenderer extends DefaultTableCellRenderer {
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
