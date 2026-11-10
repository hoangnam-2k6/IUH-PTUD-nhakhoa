package UI.LeTan;

import Dao.BenhNhanDao;
import Dao.DichVuDao;
import Dao.PhieuKhamDao;
import Dao.PhongKhamDao;
import Entity.BenhNhan;
import Entity.DichVu;
import Entity.LichHen;
import Entity.PhieuKham;
import Entity.PhongKham;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại tạo phiếu khám (bệnh nhân vãng lai hoặc check-in từ lịch hẹn).
 * <ul>
 *   <li>Tìm bệnh nhân theo tên / mã / số điện thoại, xem ngay dị ứng và tuổi.</li>
 *   <li>Phòng khám hiển thị số chỗ còn lại trong ngày.</li>
 *   <li>Dịch vụ chọn bằng ô tích, tổng tiền tạm tính cập nhật ngay.</li>
 *   <li>Báo lỗi riêng từng ô, giữ nguyên hộp thoại để sửa; lưu phiếu + dịch vụ trong một giao dịch.</li>
 * </ul>
 */
public class PhieuKhamDialog extends JDialog {
    private final PhieuKhamDao phieuKhamDao = new PhieuKhamDao();

    private final BenhNhan benhNhanCoDinh;   // khác null khi check-in từ lịch hẹn
    private final LichHen lichHen;           // null khi tạo phiếu vãng lai
    private final List<BenhNhan> dsBenhNhan;
    private final Set<Integer> daCoPhieuHomNay;
    private final Map<Integer, Integer> daDungTheoPhong;
    private final List<DichVu> dsDichVu;

    private PhieuKham ketQua;
    private BenhNhan benhNhanDaChon;

    private final JTextField oTim = taoOText();
    private final DefaultListModel<BenhNhan> modelBenhNhan = new DefaultListModel<>();
    private final JList<BenhNhan> listBenhNhan = new JList<>(modelBenhNhan);
    private final JLabel lblThongTinBN = new JLabel(" ");
    private final JComboBox<PhongKham> cbPhong = new JComboBox<>();
    private final JLabel lblPhong = new JLabel(" ");
    private final DefaultTableModel modelDichVu = new DefaultTableModel(
            new Object[]{"", "Dịch vụ", "Đơn giá (đ)"}, 0) {
        @Override
        public Class<?> getColumnClass(int c) {
            return c == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return c == 0;
        }
    };
    private final JTable bangDichVu = GiaoDien.taoBang(modelDichVu);
    private final JLabel lblTong = new JLabel();
    private final JTextArea oGhiChu = taoOVanBan(2);
    private final JButton nutLuu;

    PhieuKhamDialog(Component cha, BenhNhan benhNhanCoDinh, LichHen lichHen, List<BenhNhan> dsBenhNhan,
                    Set<Integer> daCoPhieuHomNay, List<PhongKham> dsPhong, Map<Integer, Integer> daDungTheoPhong,
                    List<DichVu> dsDichVu) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha),
                lichHen == null ? "Tạo phiếu khám mới" : "Check-in & tạo phiếu khám",
                ModalityType.APPLICATION_MODAL);
        this.benhNhanCoDinh = benhNhanCoDinh;
        this.lichHen = lichHen;
        this.dsBenhNhan = dsBenhNhan;
        this.daCoPhieuHomNay = daCoPhieuHomNay;
        this.daDungTheoPhong = daDungTheoPhong;
        this.dsDichVu = dsDichVu;
        this.benhNhanDaChon = benhNhanCoDinh;

        nutLuu = taoNut(lichHen == null ? "Tạo phiếu khám" : "Check-in", MAU_CHINH, MAU_CHINH_DAM);

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        add(taoTieuDe(), BorderLayout.NORTH);
        add(taoNoiDung(dsPhong), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);

        setSize(680, benhNhanCoDinh == null ? 790 : 640);
        setMinimumSize(new Dimension(600, 520));
        setLocationRelativeTo(cha);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        capNhatThongTinBenhNhan();
        capNhatGoiYPhong();
        capNhatTong();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                if (PhieuKhamDialog.this.benhNhanCoDinh == null) oTim.requestFocusInWindow();
                else cbPhong.requestFocusInWindow();
            }
        });
    }

    /** Tạo phiếu khám cho bệnh nhân đến khám không hẹn trước. Trả về phiếu đã tạo, hoặc null nếu hủy. */
    public static PhieuKham taoMoi(Component cha) {
        return mo(cha, null, null);
    }

    /** Check-in từ lịch hẹn: bệnh nhân đã xác định sẵn. Trả về phiếu đã tạo, hoặc null nếu hủy. */
    public static PhieuKham checkIn(Component cha, LichHen lichHen) {
        return mo(cha, lichHen, lichHen == null ? null : lichHen.getMaBN());
    }

    private static PhieuKham mo(Component cha, LichHen lichHen, Integer maBNCoDinh) {
        try {
            PhieuKhamDao pkDao = new PhieuKhamDao();
            PhongKhamDao phongDao = new PhongKhamDao();
            BenhNhanDao bnDao = new BenhNhanDao();

            List<PhongKham> dsPhong = phongDao.findDangHoatDong();
            if (dsPhong.isEmpty()) {
                canhBao(cha, "Chưa có phòng khám nào đang hoạt động.");
                return null;
            }
            List<DichVu> dsDichVu = new DichVuDao().findDangHoatDong();
            if (dsDichVu.isEmpty()) {
                canhBao(cha, "Hiện chưa có dịch vụ nào đang hoạt động. Vui lòng kiểm tra danh mục dịch vụ.");
                return null;
            }

            LocalDate homNay = LocalDate.now();
            Set<Integer> daCoPhieu = new HashSet<>();
            for (PhieuKham pk : pkDao.findTheoNgay(homNay)) {
                if (!pk.daHuy()) daCoPhieu.add(pk.getMaBN());
            }
            Map<Integer, Integer> daDung = new HashMap<>();
            for (PhongKham p : dsPhong) daDung.put(p.getMaPhong(), pkDao.demTheoPhong(p.getMaPhong(), homNay));

            BenhNhan codinh = null;
            List<BenhNhan> dsBN = new ArrayList<>();
            if (maBNCoDinh != null) {
                codinh = bnDao.findById(maBNCoDinh);
                if (codinh == null) {
                    canhBao(cha, "Không tìm thấy hồ sơ bệnh nhân của lịch hẹn.");
                    return null;
                }
            } else {
                for (BenhNhan bn : bnDao.findAll()) if (bn.dangHoatDong()) dsBN.add(bn);
                if (dsBN.isEmpty()) {
                    canhBao(cha, "Chưa có bệnh nhân đang hoạt động. Vui lòng tạo hồ sơ bệnh nhân trước.");
                    return null;
                }
            }

            PhieuKhamDialog d = new PhieuKhamDialog(cha, codinh, lichHen, dsBN, daCoPhieu, dsPhong, daDung, dsDichVu);
            d.setVisible(true);
            return d.ketQua;
        } catch (Exception e) {
            baoLoi(cha, e);
            return null;
        }
    }

    private static void canhBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Không thể tạo phiếu", JOptionPane.WARNING_MESSAGE);
    }

    // ===================== Giao diện =====================

    private JComponent taoTieuDe() {
        JLabel tieuDe = new JLabel(lichHen == null ? "Tạo phiếu khám cho bệnh nhân vãng lai" : "Check-in bệnh nhân có lịch hẹn");
        tieuDe.setFont(font(Font.BOLD, 17));
        tieuDe.setForeground(CHU_CHINH);
        String mo = "Ngày khám " + LocalDate.now().format(NGAY) + "  ·  số thứ tự do hệ thống tự cấp";
        if (lichHen != null && lichHen.getNgayGio() != null) {
            mo = "Lịch hẹn " + lichHen.getNgayGio().format(NGAY_GIO)
                    + (lichHen.getTenBacSi() == null ? "" : "  ·  Bác sĩ " + lichHen.getTenBacSi())
                    + "  ·  số thứ tự do hệ thống tự cấp";
        }
        JLabel moTa = new JLabel(mo);
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, VIEN),
                BorderFactory.createEmptyBorder(14, 20, 12, 20)));
        p.add(tieuDe);
        p.add(moTa);
        return p;
    }

    private JComponent taoNoiDung(List<PhongKham> dsPhong) {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        // --- Bệnh nhân ---
        noi.add(taoTieuDeNhom("Bệnh nhân"));
        if (benhNhanCoDinh == null) {
            oTim.setToolTipText("Gõ tên, mã (BN001) hoặc số điện thoại để lọc danh sách");
            oTim.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { locBenhNhan(); }
                public void removeUpdate(DocumentEvent e) { locBenhNhan(); }
                public void changedUpdate(DocumentEvent e) { locBenhNhan(); }
            });
            // Mũi tên lên/xuống ngay trong ô tìm kiếm để chọn nhanh, không cần chuột.
            oTim.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "xuong");
            oTim.getActionMap().put("xuong", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { dichChon(1); }
            });
            oTim.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "len");
            oTim.getActionMap().put("len", new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) { dichChon(-1); }
            });

            listBenhNhan.setFont(font(Font.PLAIN, 13));
            listBenhNhan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listBenhNhan.setFixedCellHeight(40);
            listBenhNhan.setCellRenderer(new BenhNhanRenderer());
            listBenhNhan.addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                benhNhanDaChon = listBenhNhan.getSelectedValue();
                capNhatThongTinBenhNhan();
            });
            JScrollPane cuonBN = new JScrollPane(listBenhNhan);
            cuonBN.setBorder(BorderFactory.createLineBorder(VIEN));
            cuonBN.setPreferredSize(new Dimension(100, 120));

            JPanel khoiBN = new JPanel(new BorderLayout(0, 6));
            khoiBN.setOpaque(false);
            khoiBN.setAlignmentX(Component.LEFT_ALIGNMENT);
            khoiBN.add(oTim, BorderLayout.NORTH);
            khoiBN.add(cuonBN, BorderLayout.CENTER);
            khoiBN.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
            noi.add(khoiBN);
            noi.add(Box.createVerticalStrut(6));
            locBenhNhan();
        }
        lblThongTinBN.setFont(font(Font.PLAIN, 12));
        lblThongTinBN.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(NEN_APP);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        the.add(lblThongTinBN, BorderLayout.CENTER);
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        noi.add(the);

        // --- Phòng khám ---
        noi.add(taoTieuDeNhom("Phòng khám"));
        for (PhongKham p : dsPhong) cbPhong.addItem(p);
        cbPhong.setFont(font(Font.PLAIN, 14));
        cbPhong.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof PhongKham) {
                    PhongKham p = (PhongKham) value;
                    int con = conLai(p);
                    setText(p.getTenPhong() + (con > 0 ? "  —  còn " + con + "/" + sucChua(p) + " chỗ"
                            : "  —  đã đủ " + sucChua(p) + " bệnh nhân"));
                    if (con <= 0 && !sel) setForeground(CHU_PHU);
                }
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        cbPhong.addActionListener(e -> capNhatGoiYPhong());
        for (int i = 0; i < cbPhong.getItemCount(); i++) {   // mặc định: phòng còn chỗ đầu tiên
            if (conLai(cbPhong.getItemAt(i)) > 0) {
                cbPhong.setSelectedIndex(i);
                break;
            }
        }
        lblPhong.setFont(font(Font.PLAIN, 11));
        lblPhong.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbPhong.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbPhong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        noi.add(cbPhong);
        noi.add(Box.createVerticalStrut(4));
        noi.add(lblPhong);

        // --- Dịch vụ ---
        noi.add(taoTieuDeNhom("Dịch vụ khám (tích chọn một hoặc nhiều dịch vụ)"));
        for (DichVu dv : dsDichVu) {
            modelDichVu.addRow(new Object[]{Boolean.FALSE, dv.getTenDichVu(), tien(dv.getDonGia())});
        }
        bangDichVu.setRowHeight(30);
        bangDichVu.getColumnModel().getColumn(0).setMaxWidth(40);
        bangDichVu.getColumnModel().getColumn(0).setMinWidth(40);
        bangDichVu.getColumnModel().getColumn(2).setPreferredWidth(120);
        bangDichVu.getColumnModel().getColumn(2).setMaxWidth(150);
        bangDichVu.getColumnModel().getColumn(1).setCellRenderer(new GiaoDien.OCoBanRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return x;
            }
        });
        bangDichVu.getColumnModel().getColumn(2).setCellRenderer(new GiaoDien.OCoBanRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component x = super.getTableCellRendererComponent(t, v, s, f, r, c);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
                return x;
            }
        });
        bangDichVu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Bấm vào bất kỳ chỗ nào trên dòng cũng tích / bỏ tích, không bắt người dùng nhắm đúng ô vuông nhỏ.
        bangDichVu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = bangDichVu.rowAtPoint(e.getPoint());
                int c = bangDichVu.columnAtPoint(e.getPoint());
                if (r >= 0 && c != 0) {
                    modelDichVu.setValueAt(!(Boolean) modelDichVu.getValueAt(r, 0), r, 0);
                }
            }
        });
        bangDichVu.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "tich");
        bangDichVu.getActionMap().put("tich", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int r = bangDichVu.getSelectedRow();
                if (r >= 0) modelDichVu.setValueAt(!(Boolean) modelDichVu.getValueAt(r, 0), r, 0);
            }
        });
        modelDichVu.addTableModelListener(e -> capNhatTong());

        JScrollPane cuonDV = new JScrollPane(bangDichVu);
        cuonDV.setBorder(BorderFactory.createLineBorder(VIEN));
        cuonDV.setPreferredSize(new Dimension(100, 30 * Math.min(5, Math.max(3, dsDichVu.size())) + 30));
        cuonDV.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuonDV.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        noi.add(cuonDV);

        lblTong.setFont(font(Font.BOLD, 13));
        lblTong.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JPanel hangTong = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        hangTong.setOpaque(false);
        hangTong.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangTong.add(lblTong);
        hangTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        noi.add(hangTong);

        // --- Ghi chú ---
        noi.add(taoTieuDeNhom("Ghi chú tiếp nhận (không bắt buộc)"));
        JScrollPane cuonGhiChu = boc(oGhiChu);
        cuonGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        cuonGhiChu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        oGhiChu.setToolTipText("Ví dụ: đau răng hàm dưới, khám lần đầu, mang theo phim X-quang...");
        noi.add(cuonGhiChu);

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        cuon.getViewport().setBackground(NEN_THE);
        return cuon;
    }

    private JPanel taoChan() {
        JButton huy = taoNutTrang("Hủy");
        huy.addActionListener(e -> dispose());
        nutLuu.addActionListener(e -> luu());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(nutLuu);
        getRootPane().setDefaultButton(nutLuu);
        return p;
    }

    // ===================== Hành vi =====================

    private void locBenhNhan() {
        String tuKhoa = boDau(oTim.getText().trim());
        BenhNhan dangChon = listBenhNhan.getSelectedValue();
        modelBenhNhan.clear();
        for (BenhNhan bn : dsBenhNhan) {
            String khop = boDau(String.format("BN%03d %s %s", bn.getMaBN(), bn.getHoTen(),
                    bn.getSoDienThoai() == null ? "" : bn.getSoDienThoai()));
            if (tuKhoa.isEmpty() || khop.contains(tuKhoa)) modelBenhNhan.addElement(bn);
        }
        if (modelBenhNhan.isEmpty()) {
            listBenhNhan.clearSelection();
        } else if (dangChon != null && modelBenhNhan.contains(dangChon)) {
            listBenhNhan.setSelectedValue(dangChon, true);
        } else if (!tuKhoa.isEmpty()) {
            listBenhNhan.setSelectedIndex(0);      // gõ xong là có sẵn kết quả đầu tiên để Enter
        } else {
            listBenhNhan.clearSelection();
        }
        if (listBenhNhan.getSelectedValue() == null) {
            benhNhanDaChon = null;
            capNhatThongTinBenhNhan();
        }
    }

    private void dichChon(int buoc) {
        int n = modelBenhNhan.size();
        if (n == 0) return;
        int i = Math.max(0, Math.min(n - 1, listBenhNhan.getSelectedIndex() + buoc));
        listBenhNhan.setSelectedIndex(i);
        listBenhNhan.ensureIndexIsVisible(i);
    }

    private static String boDau(String s) {
        String d = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D');
        return d.toLowerCase();
    }

    private void capNhatThongTinBenhNhan() {
        BenhNhan bn = benhNhanDaChon;
        if (bn == null) {
            lblThongTinBN.setText("<html><span style='color:#64748B'>Chưa chọn bệnh nhân. "
                    + "Gõ tên, mã hoặc số điện thoại ở ô tìm kiếm rồi chọn trong danh sách.</span></html>");
            return;
        }
        StringBuilder sb = new StringBuilder("<html><b style='font-size:13px'>")
                .append(esc(bn.getHoTen())).append("</b> &nbsp;<span style='color:#64748B'>")
                .append(String.format("BN%03d", bn.getMaBN()));
        if (bn.getGioiTinh() != null) sb.append(" · ").append(esc(bn.getGioiTinh()));
        if (bn.getTuoi() != null) sb.append(" · ").append(bn.getTuoi()).append(" tuổi");
        if (bn.getSoDienThoai() != null && !bn.getSoDienThoai().isBlank()) {
            sb.append(" · ").append(esc(bn.getSoDienThoai()));
        }
        sb.append("</span>");
        if (bn.getDiUng() != null && !bn.getDiUng().isBlank()) {
            sb.append("<br><b style='color:#B91C1C'>⚠ Dị ứng: ").append(esc(bn.getDiUng())).append("</b>");
        }
        if (daCoPhieuHomNay.contains(bn.getMaBN())) {
            sb.append("<br><b style='color:#B45309'>Bệnh nhân đã có phiếu khám hôm nay.</b>");
        }
        lblThongTinBN.setText(sb.append("</html>").toString());
    }

    private void capNhatGoiYPhong() {
        PhongKham p = (PhongKham) cbPhong.getSelectedItem();
        if (p == null) {
            lblPhong.setText(" ");
            return;
        }
        int con = conLai(p);
        lblPhong.setForeground(con > 0 ? CHU_PHU : Color.decode("#B91C1C"));
        lblPhong.setText(con > 0 ? "Phòng đang có " + (sucChua(p) - con) + " bệnh nhân hôm nay."
                : "Phòng này đã đủ số bệnh nhân trong ngày, vui lòng chọn phòng khác.");
    }

    private int sucChua(PhongKham p) {
        return p.getSucChua() > 0 ? p.getSucChua() : PhieuKhamDao.TOI_DA_MOI_PHONG;
    }

    private int conLai(PhongKham p) {
        return sucChua(p) - daDungTheoPhong.getOrDefault(p.getMaPhong(), 0);
    }

    private List<DichVu> dichVuDaTich() {
        List<DichVu> ds = new ArrayList<>();
        for (int i = 0; i < modelDichVu.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelDichVu.getValueAt(i, 0))) ds.add(dsDichVu.get(i));
        }
        return ds;
    }

    private void capNhatTong() {
        BigDecimal tong = BigDecimal.ZERO;
        List<DichVu> ds = dichVuDaTich();
        for (DichVu dv : ds) if (dv.getDonGia() != null) tong = tong.add(dv.getDonGia());
        lblTong.setForeground(ds.isEmpty() ? CHU_PHU : CHU_CHINH);
        lblTong.setText(ds.isEmpty() ? "Chưa chọn dịch vụ"
                : "Đã chọn " + ds.size() + " dịch vụ  ·  Tạm tính: " + tien(tong) + " đ");
    }

    private void luu() {
        if (benhNhanDaChon == null) {
            sai(this, benhNhanCoDinh == null ? oTim : cbPhong, "Vui lòng chọn bệnh nhân cần tạo phiếu khám.");
            return;
        }
        if (daCoPhieuHomNay.contains(benhNhanDaChon.getMaBN())) {
            sai(this, benhNhanCoDinh == null ? oTim : cbPhong, "Bệnh nhân " + benhNhanDaChon.getHoTen()
                    + " đã được cấp số thứ tự trong hôm nay.");
            return;
        }
        PhongKham phong = (PhongKham) cbPhong.getSelectedItem();
        if (phong == null) {
            sai(this, cbPhong, "Vui lòng chọn phòng khám.");
            return;
        }
        if (conLai(phong) <= 0) {
            sai(this, cbPhong, phong.getTenPhong() + " đã đủ bệnh nhân trong ngày, vui lòng chọn phòng khác.");
            return;
        }
        List<DichVu> dv = dichVuDaTich();
        if (dv.isEmpty()) {
            sai(this, bangDichVu, "Vui lòng chọn ít nhất một dịch vụ khám.");
            return;
        }
        List<Integer> maDV = new ArrayList<>();
        for (DichVu d : dv) maDV.add(d.getMaDV());

        nutLuu.setEnabled(false);
        try {
            ketQua = phieuKhamDao.capSoThuTu(benhNhanDaChon.getMaBN(), phong.getMaPhong(),
                    lichHen == null ? null : lichHen.getMaLH(), oGhiChu.getText(), maDV);
            ketQua.setTenPhong(phong.getTenPhong());
            dispose();
        } catch (Exception e) {
            // Lỗi nghiệp vụ (hết chỗ, trùng phiếu do máy khác vừa tạo...) -> giữ hộp thoại để sửa.
            baoLoi(this, e);
            nutLuu.setEnabled(true);
        }
    }

    /** Phiếu vừa tạo (null nếu người dùng hủy). */
    PhieuKham ketQua() {
        return ketQua;
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ===================== Hiển thị danh sách bệnh nhân =====================

    private final class BenhNhanRenderer extends JPanel implements ListCellRenderer<BenhNhan> {
        private final JLabel dong1 = new JLabel();
        private final JLabel dong2 = new JLabel();

        BenhNhanRenderer() {
            super(new GridLayout(2, 1, 0, 0));
            setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
            dong1.setFont(font(Font.BOLD, 13));
            dong2.setFont(font(Font.PLAIN, 11));
            add(dong1);
            add(dong2);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends BenhNhan> list, BenhNhan bn, int index,
                                                      boolean sel, boolean focus) {
            boolean coPhieu = daCoPhieuHomNay.contains(bn.getMaBN());
            dong1.setText(bn.getHoTen());
            StringBuilder sb = new StringBuilder(String.format("BN%03d", bn.getMaBN()));
            if (bn.getTuoi() != null) sb.append(" · ").append(bn.getTuoi()).append(" tuổi");
            if (bn.getSoDienThoai() != null && !bn.getSoDienThoai().isBlank()) sb.append(" · ").append(bn.getSoDienThoai());
            if (coPhieu) sb.append(" · đã có phiếu hôm nay");
            dong2.setText(sb.toString());
            setBackground(sel ? MAU_CHINH_NHAT : NEN_THE);
            dong1.setForeground(coPhieu ? CHU_PHU : CHU_CHINH);
            dong2.setForeground(coPhieu ? Color.decode("#B45309") : CHU_PHU);
            return this;
        }
    }
}
