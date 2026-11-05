package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.PhieuKhamDao;
import Dao.PhongKhamDao;
import Entity.BenhNhan;
import Entity.PhieuKham;
import Entity.PhongKham;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static UI.LeTan.GiaoDien.*;

public class TrangChuLeTan extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final String tenNguoiDung;
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final PhongKhamDao phongKhamDao = new PhongKhamDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();

    private List<PhieuKham> dsPhieu = new ArrayList<>();
    private List<PhongKham> dsPhong = new ArrayList<>();
    private boolean dangNap = false;

    // 4 con số thống kê
    private final JLabel soTongBN = new JLabel("0");
    private final JLabel soChoKham = new JLabel("0");
    private final JLabel soHoanThanh = new JLabel("0");
    private final JLabel soPhieu = new JLabel("0");

    // Bảng phiếu khám (bên trái)
    private final DefaultTableModel modelPhieu = new DefaultTableModel(
            new String[]{"STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Phòng khám", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= 4; // chỉ sửa được Phòng khám và Trạng thái
        }
    };
    private final JTable bangPhieu = GiaoDien.taoBang(modelPhieu);
    private final TableRowSorter<DefaultTableModel> boLoc = new TableRowSorter<>(modelPhieu);
    private final JTextField oTimKiem = GiaoDien.taoOText();
    private final JLabel soLuongPhieu = new JLabel("0 phiếu");

    // Form tiếp nhận (bên phải)
    private final JTextField oTen = GiaoDien.taoOText();
    private final JTextField oNgaySinh = GiaoDien.taoOText();
    private final JComboBox<String> cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JTextField oSdt = GiaoDien.taoOText();
    private final JTextField oDiaChi = GiaoDien.taoOText();
    private final JComboBox<PhongKham> cbPhong = new JComboBox<>();

    public TrangChuLeTan(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
        setLayout(new BorderLayout(0, 12));
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel phanTren = new JPanel(new BorderLayout(0, 12));
        phanTren.setOpaque(false);
        phanTren.add(taoBanner(), BorderLayout.NORTH);
        phanTren.add(taoHangThongKe(), BorderLayout.CENTER);

        JSplitPane chia = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoKhamVaDieuTri(), taoTiepNhan());
        chia.setResizeWeight(0.6);
        chia.setDividerSize(14);
        chia.setBorder(null);
        chia.setOpaque(false);

        add(phanTren, BorderLayout.NORTH);
        add(chia, BorderLayout.CENTER);

        // Sửa Phòng khám / Trạng thái ngay trên bảng thì lưu vào CSDL
        modelPhieu.addTableModelListener(e -> {
            if (dangNap || e.getType() != TableModelEvent.UPDATE) return;
            int dong = e.getFirstRow();
            if (dong < 0 || dong >= dsPhieu.size()) return;
            PhieuKham pk = dsPhieu.get(dong);
            String tenPhong = String.valueOf(modelPhieu.getValueAt(dong, 4));
            String trangThai = String.valueOf(modelPhieu.getValueAt(dong, 5));
            String phongCu = pk.getTenPhong() == null ? "" : pk.getTenPhong();
            if (Objects.equals(tenPhong, phongCu) && Objects.equals(trangThai, pk.getTrangThai())) return;
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

        String homNay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel chao = new JLabel("Xin chào, " + tenNguoiDung + "  ·  Hôm nay " + homNay);
        chao.setFont(font(Font.PLAIN, 13));
        chao.setForeground(MAU_CHINH_NHAT);

        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 4));
        chu.setOpaque(false);
        chu.add(tieuDe);
        chu.add(chao);

        JButton nutLamMoi = taoNut("Làm mới", Color.decode("#0EA5E9"), Color.decode("#38BDF8"));
        nutLamMoi.addActionListener(e -> taiDuLieu());
        JPanel goc = new JPanel(new GridBagLayout()); // để nút nằm giữa theo chiều dọc
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
        hang.add(taoTheThongKe("Phiếu khám hôm nay", soPhieu, Color.decode("#8B5CF6")));
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
                "Danh sách phiếu khám hôm nay. Bấm vào ô Phòng khám / Trạng thái để sửa trực tiếp.");

        // Ô tìm kiếm lọc ngay trên bảng
        oTimKiem.setToolTipText("Gõ tên bệnh nhân, mã phiếu, phòng hoặc trạng thái");
        oTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locBang(); }
            @Override public void removeUpdate(DocumentEvent e) { locBang(); }
            @Override public void changedUpdate(DocumentEvent e) { locBang(); }
        });
        bangPhieu.setRowSorter(boLoc);
        int[] doRong = {45, 80, 75, 180, 90, 100};
        for (int i = 0; i < doRong.length; i++) {
            bangPhieu.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }

        JLabel nhanTim = new JLabel("Tìm kiếm:");
        nhanTim.setFont(font(Font.PLAIN, 13));
        nhanTim.setForeground(CHU_PHU);
        soLuongPhieu.setFont(font(Font.BOLD, 12));
        soLuongPhieu.setForeground(MAU_CHINH);

        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(nhanTim, BorderLayout.WEST);
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(soLuongPhieu, BorderLayout.EAST);

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(hangTim, BorderLayout.NORTH);
        giua.add(new JScrollPane(bangPhieu), BorderLayout.CENTER);
        the.add(giua, BorderLayout.CENTER);
        return the;
    }

    private JPanel taoTiepNhan() {
        JPanel the = GiaoDien.taoThe("Tiếp nhận bệnh nhân", "Nhập thông tin, cấp số thứ tự và phân phòng khám");

        // Các ô nhập (có thanh cuộn phòng khi cửa sổ thấp)
        cbGioiTinh.setFont(font(Font.PLAIN, 14));
        JPanel hangNgaySinh = hai(truong("Ngày sinh (dd/MM/yyyy)", oNgaySinh), truong("Giới tính", cbGioiTinh));

        JPanel luoi = new JPanel();
        luoi.setLayout(new BoxLayout(luoi, BoxLayout.Y_AXIS));
        luoi.setOpaque(false);
        luoi.add(truong("Họ và tên *", oTen));
        luoi.add(hangNgaySinh);
        luoi.add(truong("Số điện thoại *", oSdt));
        luoi.add(truong("Địa chỉ", oDiaChi));

        JPanel boc = new JPanel(new BorderLayout());
        boc.setOpaque(false);
        boc.add(luoi, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(boc);
        cuon.setBorder(null);
        cuon.setOpaque(false);
        cuon.getViewport().setOpaque(false);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        the.add(cuon, BorderLayout.CENTER);

        // Phần cố định phía dưới: chọn phòng + nút + ghi chú
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
        JLabel moTa = new JLabel("Tối đa " + PhieuKhamDao.TOI_DA_MOI_PHONG + " bệnh nhân / phòng / ngày");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new GridLayout(2, 1));
        chu.setOpaque(false);
        chu.add(ten);
        chu.add(moTa);

        cbPhong.setFont(font(Font.PLAIN, 14));
        cbPhong.setPreferredSize(new Dimension(130, 32));
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

    // ===================== Xử lý dữ liệu =====================

    public void taiDuLieu() {
        int tongBN;
        try {
            dsPhong = phongKhamDao.findAll();
            dsPhieu = phieuKhamDao.findHomNay();
            tongBN = benhNhanDao.demTatCa();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }

        // Đổ dữ liệu vào bảng
        dangNap = true;
        try {
            modelPhieu.setRowCount(0);
            int choKham = 0;
            int hoanThanh = 0;
            for (PhieuKham pk : dsPhieu) {
                modelPhieu.addRow(new Object[]{pk.getSoThuTu(), String.format("PK%03d", pk.getMaPK()),
                        String.format("BN%03d", pk.getMaBN()), pk.getTenBenhNhan(),
                        pk.getTenPhong() == null ? "" : pk.getTenPhong(), pk.getTrangThai()});
                if (PhieuKhamDao.CHO_KHAM.equals(pk.getTrangThai())) choKham++;
                if (PhieuKhamDao.HOAN_THANH.equals(pk.getTrangThai())) hoanThanh++;
            }
            soTongBN.setText(String.valueOf(tongBN));
            soChoKham.setText(String.valueOf(choKham));
            soHoanThanh.setText(String.valueOf(hoanThanh));
            soPhieu.setText(String.valueOf(dsPhieu.size()));
            soLuongPhieu.setText(dsPhieu.size() + " phiếu");
        } finally {
            dangNap = false;
        }

        // Danh sách phòng ở bảng và ở form (giữ lại phòng đang chọn)
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
            JOptionPane.showMessageDialog(this, "Chưa có phòng khám nào trong hệ thống.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BenhNhan bn = benhNhanDao.findByTenVaSdt(ten, sdt);
            boolean dungLaiHoSo = (bn != null);
            if (bn == null) {
                bn = new BenhNhan();
                bn.setHoTen(ten);
                bn.setSoDienThoai(sdt);
                String ngaySinh = oNgaySinh.getText().trim();
                if (!ngaySinh.isEmpty()) bn.setNgaySinh(LocalDate.parse(ngaySinh, NGAY));
                bn.setGioiTinh((String) cbGioiTinh.getSelectedItem());
                bn.setDiaChi(oDiaChi.getText());
                bn.setMaBN(benhNhanDao.them(bn));
            }

            PhieuKham pk = phieuKhamDao.capSoThuTu(bn.getMaBN(), phong.getMaPhong());
            JOptionPane.showMessageDialog(this,
                    "Đã cấp số thứ tự " + pk.getSoThuTu() + " cho " + bn.getHoTen() + " (" + phong.getTenPhong() + ").\n"
                            + (dungLaiHoSo ? "Hồ sơ có sẵn được dùng lại." : "Đã tạo hồ sơ bệnh nhân mới.")
                            + (bn.getDiUng() == null ? "" : "\n\n⚠ Bệnh nhân có dị ứng: " + bn.getDiUng()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            nhapLai();
            taiDuLieu();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không đúng định dạng dd/MM/yyyy.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }
}
