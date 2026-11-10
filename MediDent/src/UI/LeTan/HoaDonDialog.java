package UI.LeTan;

import Dao.HoaDonDao;
import Dao.MaGiamGiaDao;
import Dao.PhieuKhamDao;
import Entity.ChiTietHoaDon;
import Entity.DichVu;
import Entity.HoaDon;
import Entity.MaGiamGia;
import Entity.PhieuKham;
import Entity.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại lập / sửa hóa đơn.
 *
 * Quy tắc nghiệp vụ:
 * - Lập hóa đơn mới: chỉ phiếu khám ĐÃ HOÀN THÀNH và chưa có hóa đơn mới được chọn, chọn ngay trong hộp thoại.
 *   Lúc mở chưa chọn phiếu nào nên chưa hiện thông tin; chọn phiếu xong mới hiện.
 * - Dịch vụ đã chọn khi tiếp nhận được nạp sẵn vào hóa đơn.
 * - Mỗi dịch vụ là một dòng, số lượng luôn bằng 1 (không nhập số lượng).
 * - Giảm giá chỉ được chọn từ danh mục mã giảm do quản lý tạo (combobox).
 * - Khi sửa hóa đơn chưa thanh toán, chỉ đổi được mã giảm và ghi chú;
 *   danh sách dịch vụ bị khóa để tránh làm sai lệch hóa đơn đã lập.
 */
public class HoaDonDialog extends JDialog {
    private final HoaDonDao hoaDonDao = new HoaDonDao();
    private final MaGiamGiaDao maGiamGiaDao = new MaGiamGiaDao();
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();
    private final TaiKhoan taiKhoan;
    private final List<PhieuKham> dsPhieu;      // phiếu có thể lập hóa đơn (rỗng khi sửa)
    private PhieuKham phieuKham;                // phiếu đang chọn
    private final HoaDon hoaDon;                // khác null khi sửa
    private final boolean dangSua;

    private final List<ChiTietHoaDon> chiTiet = new ArrayList<>();
    private final List<ChiTietHoaDon> dichVuGoc = new ArrayList<>();
    private int maHDMoi;
    private boolean daLuu;
    private boolean dangLoc;

    /** Ô lọc phiếu khám: có chữ gợi ý mờ khi chưa nhập gì. */
    private final JTextField oLoc = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Color.decode("#94A3B8"));
                g2.setFont(font(Font.PLAIN, 13));
                Insets in = getInsets();
                g2.drawString("Lọc theo tên / mã phiếu…", in.left,
                        (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2);
                g2.dispose();
            }
        }
    };
    private final JComboBox<PhieuKham> cbPhieu = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel("—");
    private final JLabel lblThongTin = new JLabel(" ");
    private final JLabel lblCanhBao = new JLabel(" ");
    private final JComboBox<MaGiamGia> cbMaGiam = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Dịch vụ", "Đơn giá", "Thành tiền"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JTextArea oGhiChu = taoOVanBan(2);
    private final JLabel lblTamTinh = new JLabel("0 đ");
    private final JLabel lblGiamGia = new JLabel("0 đ");
    private final JLabel lblTongTien = new JLabel("0 đ");

    private JButton nutXoaDong;
    private JButton nutKhoiPhuc;
    private JButton nutLuu;

    private HoaDonDialog(Component cha, TaiKhoan taiKhoan, List<PhieuKham> dsPhieu, HoaDon hoaDon) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha),
                hoaDon != null ? String.format("Sửa hóa đơn HD%03d", hoaDon.getMaHD()) : "Lập hóa đơn",
                ModalityType.APPLICATION_MODAL);
        this.taiKhoan = taiKhoan;
        this.dsPhieu = dsPhieu == null ? new ArrayList<>() : dsPhieu;
        this.hoaDon = hoaDon;
        this.dangSua = hoaDon != null;
        if (hoaDon != null) {
            chiTiet.addAll(hoaDon.getChiTiet());
        }

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        add(taoDau(), BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(taoNoiDung());
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        add(cuon, BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        taiMaGiam();
        napDuLieu();

        setSize(900, dangSua ? 640 : 720);
        setMinimumSize(new Dimension(760, 540));
        setLocationRelativeTo(cha);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Mở hộp thoại lập hóa đơn mới; người dùng chọn phiếu khám trong hộp thoại.
     * Trả về mã hóa đơn vừa tạo, hoặc 0 nếu không tạo (hủy / không có phiếu nào để lập).
     */
    public static int lapMoi(Component cha, TaiKhoan taiKhoan) {
        List<PhieuKham> ds;
        try {
            ds = new HoaDonDao().findPhieuChuaLapHoaDon();
        } catch (Exception e) {
            baoLoi(cha, e);
            return 0;
        }
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(cha,
                    "Không có phiếu khám nào đã hoàn thành mà chưa lập hóa đơn.\n"
                            + "Chỉ lập được hóa đơn sau khi bệnh nhân hoàn thành khám.",
                    "Chưa có phiếu để lập hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return 0;
        }
        HoaDonDialog d = new HoaDonDialog(cha, taiKhoan, ds, null);
        d.setVisible(true);
        return d.daLuu ? d.maHDMoi : 0;
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

    // ===================== Giao diện =====================

    private JComponent taoDau() {
        JPanel dau = new JPanel(new BorderLayout(12, 8));
        dau.setBackground(NEN_THE);
        dau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, VIEN),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));

        JLabel tieuDe = new JLabel(dangSua ? "SỬA HÓA ĐƠN" : "LẬP HÓA ĐƠN");
        tieuDe.setFont(font(Font.BOLD, 19));
        tieuDe.setForeground(CHU_CHINH);
        lblBenhNhan.setFont(font(Font.BOLD, 14));
        lblBenhNhan.setForeground(dangSua ? MAU_CHINH_DAM : CHU_PHU);
        lblThongTin.setFont(font(Font.PLAIN, 12));
        lblThongTin.setForeground(CHU_PHU);
        lblCanhBao.setFont(font(Font.BOLD, 12));
        lblCanhBao.setForeground(Color.decode("#B45309"));

        JPanel chu = new JPanel();
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.setOpaque(false);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        chu.add(tieuDe);
        chu.add(Box.createVerticalStrut(6));

        if (dangSua) {
            lblBenhNhan.setText(hoaDon.getTenBenhNhan());
            lblThongTin.setText(String.format("HD%03d  ·  PK%03d  ·  Ngày khám %s  ·  %s",
                    hoaDon.getMaHD(), hoaDon.getMaPK(),
                    hoaDon.getNgayKham() == null ? "—" : dinhDangNgay(hoaDon.getNgayKham()),
                    hoaDon.getTenPhong() == null ? "Chưa phân phòng" : hoaDon.getTenPhong()));
        } else {
            JTextField mau = taoOText();
            oLoc.setFont(mau.getFont());
            oLoc.setForeground(mau.getForeground());
            oLoc.setBorder(mau.getBorder());
            oLoc.setToolTipText("Gõ tên bệnh nhân hoặc mã phiếu (PK012) để lọc danh sách phiếu khám");
            oLoc.setPreferredSize(new Dimension(210, 34));
            oLoc.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { locPhieu(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { locPhieu(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { locPhieu(); }
            });
            oLoc.addActionListener(e -> cbPhieu.requestFocusInWindow());   // Enter không được lưu nhầm hóa đơn
            cbPhieu.setFont(font(Font.PLAIN, 13));
            cbPhieu.setMaximumRowCount(10);
            cbPhieu.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean sel, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, sel, focus);
                    if (value == null) {
                        setText("— Chọn phiếu khám đã hoàn thành —");
                        setForeground(CHU_PHU);
                    }
                    if (value instanceof PhieuKham) {
                        PhieuKham pk = (PhieuKham) value;
                        setText(String.format("PK%03d  ·  %s  ·  %s  ·  %s  ·  %s", pk.getMaPK(),
                                pk.getTenBenhNhan() == null ? "—" : pk.getTenBenhNhan(),
                                pk.getNgayKham() == null ? "—" : dinhDangNgay(pk.getNgayKham()),
                                pk.getTenPhong() == null ? "Chưa phân phòng" : pk.getTenPhong(),
                                pk.getTrangThai()));
                    }
                    setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                    return this;
                }
            });
            cbPhieu.addActionListener(e -> {
                if (!dangLoc) chonPhieu((PhieuKham) cbPhieu.getSelectedItem());
            });

            JLabel nhanPhieu = new JLabel("Phiếu khám *");
            nhanPhieu.setFont(font(Font.BOLD, 13));
            nhanPhieu.setForeground(CHU_CHINH);
            JPanel chonPhieu = new JPanel(new BorderLayout(8, 0));
            chonPhieu.setOpaque(false);
            chonPhieu.add(nhanPhieu, BorderLayout.WEST);
            chonPhieu.add(cbPhieu, BorderLayout.CENTER);
            chonPhieu.add(oLoc, BorderLayout.EAST);
            chonPhieu.setAlignmentX(Component.LEFT_ALIGNMENT);
            chonPhieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            chu.add(chonPhieu);
            chu.add(Box.createVerticalStrut(8));
        }
        lblBenhNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblCanhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        chu.add(lblBenhNhan);
        chu.add(Box.createVerticalStrut(2));
        chu.add(lblThongTin);
        chu.add(Box.createVerticalStrut(2));
        chu.add(lblCanhBao);
        dau.add(chu, BorderLayout.CENTER);

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

        JLabel tieuDeDichVu = taoTieuDeNhom("Dịch vụ trong hóa đơn");
        tieuDeDichVu.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(tieuDeDichVu);

        JLabel ghiChuDichVu = new JLabel("Dịch vụ được lấy tự động từ phiếu khám; không thêm dịch vụ mới tại bước lập hóa đơn.");
        ghiChuDichVu.setFont(font(Font.PLAIN, 12));
        ghiChuDichVu.setForeground(CHU_PHU);
        ghiChuDichVu.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(ghiChuDichVu);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {45, 430, 115, 130};
        for (int i = 0; i < doRong.length; i++) {
            bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        }
        JScrollPane cuonBang = new JScrollPane(bang);
        cuonBang.setBorder(BorderFactory.createLineBorder(VIEN));
        cuonBang.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuonBang.setPreferredSize(new Dimension(100, 220));
        cuonBang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        noi.add(cuonBang);

        nutXoaDong = taoNutTrang("Xóa dòng");
        nutXoaDong.addActionListener(e -> xoaDong());
        JPanel hangDong = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        hangDong.setOpaque(false);
        hangDong.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangDong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        hangDong.add(nutXoaDong);
        nutKhoiPhuc = taoNutTrang("Khôi phục dịch vụ");
        nutKhoiPhuc.addActionListener(e -> khoiPhucDichVu());
        hangDong.add(nutKhoiPhuc);
        JLabel goiY = new JLabel(dangSua
                ? "Khi sửa hóa đơn, danh sách dịch vụ được giữ nguyên."
                : "Chọn dòng rồi bấm Xóa dòng nếu cần bỏ; bấm Khôi phục để lấy lại đủ dịch vụ của phiếu.");
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

        cbMaGiam.setFont(font(Font.PLAIN, 13));
        cbMaGiam.setPreferredSize(new Dimension(260, 32));
        cbMaGiam.addActionListener(e -> capNhatTong());

        soTien.add(nhanTien("Tạm tính"));
        dinhDangTien(lblTamTinh, CHU_CHINH);
        soTien.add(lblTamTinh);

        soTien.add(nhanTien("Mã giảm giá"));
        soTien.add(cbMaGiam);

        soTien.add(nhanTien("Tiền giảm"));
        dinhDangTien(lblGiamGia, Color.decode("#B45309"));
        soTien.add(lblGiamGia);

        soTien.add(nhanTien("TỔNG CỘNG"));
        dinhDangTien(lblTongTien, MAU_CHINH_DAM);
        lblTongTien.setFont(font(Font.BOLD, 18));
        soTien.add(lblTongTien);

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
        nutLuu = taoNut(dangSua ? "Lưu thay đổi" : "Lập hóa đơn", MAU_CHINH, MAU_CHINH_DAM);
        JButton luu = nutLuu;
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

    private void taiMaGiam() {
        cbMaGiam.removeAllItems();
        cbMaGiam.addItem(MaGiamGia.khongGiamGia());
        try {
            for (MaGiamGia m : maGiamGiaDao.findDangHoatDong()) cbMaGiam.addItem(m);
        } catch (Exception e) {
            baoLoi(this, e);
        }
        // Hóa đơn cũ có giảm giá nhưng chưa gắn mã: giữ nguyên khoản giảm đã lưu.
        if (hoaDon != null && hoaDon.getMaGiam() == null && hoaDon.getGiamGia().signum() > 0) {
            MaGiamGia cu = new MaGiamGia(-1, "OLD", "Giảm giá cũ (giữ nguyên)",
                    MaGiamGia.SO_TIEN, hoaDon.getGiamGia(), MaGiamGia.HOAT_DONG);
            cbMaGiam.addItem(cu);
            cbMaGiam.setSelectedItem(cu);
        }
    }

    private void napDuLieu() {
        if (hoaDon != null) {
            oGhiChu.setText(nz(hoaDon.getGhiChu()));
            if (hoaDon.getMaGiam() != null) {
                for (int i = 0; i < cbMaGiam.getItemCount(); i++) {
                    MaGiamGia m = cbMaGiam.getItemAt(i);
                    if (m.getMaGiam() == hoaDon.getMaGiam()) {
                        cbMaGiam.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        // Sửa hóa đơn: khóa danh sách dịch vụ, chỉ đổi mã giảm và ghi chú.
        if (dangSua) {
            nutXoaDong.setEnabled(false);
            nutKhoiPhuc.setEnabled(false);
            capNhatBang();
        } else {
            locPhieu();
        }
    }

    // ----- Chọn phiếu khám (chỉ khi lập mới) -----

    private void locPhieu() {
        String tuKhoa = boDau(oLoc.getText().trim());
        PhieuKham dangChon = (PhieuKham) cbPhieu.getSelectedItem();
        dangLoc = true;
        try {
            cbPhieu.removeAllItems();
            for (PhieuKham pk : dsPhieu) {
                String khop = boDau(String.format("PK%03d %s", pk.getMaPK(),
                        pk.getTenBenhNhan() == null ? "" : pk.getTenBenhNhan()));
                if (tuKhoa.isEmpty() || khop.contains(tuKhoa)) cbPhieu.addItem(pk);
            }
            cbPhieu.setSelectedIndex(-1);      // không tự chọn sẵn: người dùng phải chọn phiếu
            if (!tuKhoa.isEmpty() && cbPhieu.getItemCount() == 1) {
                cbPhieu.setSelectedIndex(0);   // lọc ra đúng một phiếu thì chọn luôn
            } else if (dangChon != null) {
                for (int i = 0; i < cbPhieu.getItemCount(); i++) {
                    if (cbPhieu.getItemAt(i).getMaPK() == dangChon.getMaPK()) {
                        cbPhieu.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } finally {
            dangLoc = false;
        }
        chonPhieu((PhieuKham) cbPhieu.getSelectedItem());
        // Nhiều phiếu khớp: mở sẵn danh sách để thấy kết quả lọc và bấm chọn ngay.
        if (!tuKhoa.isEmpty() && cbPhieu.getItemCount() > 1 && cbPhieu.isShowing()) {
            cbPhieu.showPopup();
        } else if (cbPhieu.isPopupVisible()) {
            cbPhieu.hidePopup();
        }
    }

    private static String boDau(String s) {
        return java.text.Normalizer.normalize(s == null ? "" : s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    /** Nạp thông tin và dịch vụ của phiếu khám vừa chọn vào hóa đơn. */
    private void chonPhieu(PhieuKham pk) {
        if (pk != null && phieuKham != null && pk.getMaPK() == phieuKham.getMaPK() && !chiTiet.isEmpty()) {
            return;   // chọn lại đúng phiếu đang xem: giữ nguyên các dòng người dùng đã chỉnh
        }
        phieuKham = pk;
        chiTiet.clear();
        dichVuGoc.clear();
        if (pk == null) {
            lblBenhNhan.setForeground(CHU_PHU);
            boolean dangLocChu = !oLoc.getText().isBlank();
            lblBenhNhan.setText(dangLocChu && cbPhieu.getItemCount() == 0
                    ? "Không có phiếu khám nào khớp với từ khóa"
                    : "Chưa chọn phiếu khám");
            lblThongTin.setText(dangLocChu && cbPhieu.getItemCount() == 0
                    ? "Thử gõ lại tên bệnh nhân hoặc mã phiếu."
                    : "Chọn phiếu khám ở ô phía trên để xem thông tin bệnh nhân và dịch vụ sẽ lập hóa đơn.");
            lblCanhBao.setText(" ");
        } else {
            lblBenhNhan.setForeground(MAU_CHINH_DAM);
            lblBenhNhan.setText(pk.getTenBenhNhan() == null ? "—" : pk.getTenBenhNhan());
            lblThongTin.setText(String.format("PK%03d  ·  STT %d  ·  Ngày khám %s  ·  %s  ·  %s", pk.getMaPK(),
                    pk.getSoThuTu(), pk.getNgayKham() == null ? "—" : dinhDangNgay(pk.getNgayKham()),
                    pk.getTenPhong() == null ? "Chưa phân phòng" : pk.getTenPhong(), pk.getTrangThai()));
            try {
                // Dịch vụ đã chọn khi tiếp nhận -> đưa sẵn vào hóa đơn.
                for (DichVu dv : phieuKhamDao.findDichVu(pk.getMaPK())) {
                    dichVuGoc.add(new ChiTietHoaDon(dv.getMaDV(), dv.getTenDichVu(), 1, dv.getDonGia()));
                }
            } catch (Exception e) {
                baoLoi(this, e);
            }
            chiTiet.addAll(dichVuGoc);
            if (dichVuGoc.isEmpty()) {
                lblCanhBao.setText("Phiếu này chưa có dịch vụ nào được chỉ định lúc tiếp nhận nên chưa thể lập hóa đơn.");
            } else {
                lblCanhBao.setText(" ");
            }
        }
        capNhatBang();
    }

    private void khoiPhucDichVu() {
        if (dangSua || phieuKham == null) return;
        chiTiet.clear();
        chiTiet.addAll(dichVuGoc);
        capNhatBang();
    }

    private void xoaDong() {
        if (dangSua) {
            JOptionPane.showMessageDialog(this, "Khi sửa hóa đơn không được thay đổi danh sách dịch vụ.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
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
                    tien(ct.getDonGia()),
                    tien(ct.getThanhTien())
            });
        }
        capNhatTong();
    }

    private MaGiamGia maDangChon() {
        Object chon = cbMaGiam.getSelectedItem();
        return chon instanceof MaGiamGia ? (MaGiamGia) chon : MaGiamGia.khongGiamGia();
    }

    private void capNhatTong() {
        BigDecimal tamTinh = tinhTamTinh();
        BigDecimal giam = maDangChon().tinhGiam(tamTinh);
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
        if (!dangSua && phieuKham == null) {
            sai(this, oLoc, "Vui lòng chọn phiếu khám cần lập hóa đơn.");
            return;
        }
        if (chiTiet.isEmpty()) {
            sai(this, bang, dichVuGoc.isEmpty() && !dangSua
                    ? "Phiếu khám này chưa có dịch vụ nào nên chưa thể lập hóa đơn."
                    : "Hóa đơn phải có ít nhất một dịch vụ. Bấm \"Khôi phục dịch vụ\" để lấy lại danh sách.");
            return;
        }
        MaGiamGia ma = maDangChon();
        BigDecimal tamTinh = tinhTamTinh();
        BigDecimal giam = ma.tinhGiam(tamTinh);
        Integer maGiam = ma.getMaGiam() > 0 ? ma.getMaGiam() : null;

        nutLuu.setEnabled(false);
        try {
            if (hoaDon == null) {
                maHDMoi = hoaDonDao.taoHoaDon(phieuKham.getMaPK(),
                        taiKhoan == null ? null : taiKhoan.getMaTK(),
                        chiTiet, maGiam, giam, oGhiChu.getText().trim());
            } else {
                hoaDonDao.capNhatHoaDon(hoaDon.getMaHD(), chiTiet, maGiam, giam,
                        oGhiChu.getText().trim());
            }
            daLuu = true;
            dispose();
        } catch (Exception e) {
            // Lỗi nghiệp vụ (phiếu vừa bị máy khác lập hóa đơn...) -> giữ hộp thoại để người dùng xử lý.
            baoLoi(this, e);
            nutLuu.setEnabled(true);
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
