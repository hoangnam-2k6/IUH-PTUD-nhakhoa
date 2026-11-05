package UI.LeTan;

import Dao.BenhNhanDao;
import Entity.BenhNhan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/** Màn hình "Bệnh nhân": tìm kiếm, xem hồ sơ đầy đủ, thêm mới và chỉnh sửa thông tin. */
public class TraCuuThongTin extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] TEN_COT = {"Mã BN", "Họ tên", "Ngày sinh", "Tuổi", "Giới tính", "Số điện thoại", "Địa chỉ"};

    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private List<BenhNhan> dsKetQua = new ArrayList<>();
    private BenhNhan dangChon;

    private final JTextField oTimKiem = GiaoDien.taoOText();
    private final JLabel soLuong = new JLabel("0 bệnh nhân");
    private final DefaultTableModel model = new DefaultTableModel(TEN_COT, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JButton nutSua = taoNut("Sửa thông tin", MAU_CHINH, MAU_CHINH_DAM);

    private final JLabel tomTat = new JLabel(" ");

    public TraCuuThongTin() {
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Bệnh nhân", "Tìm theo mã bệnh nhân, họ tên, số điện thoại hoặc CMND/CCCD.");

        // Hàng tìm kiếm + nút thêm mới
        oTimKiem.setToolTipText("Nhập mã bệnh nhân, họ tên, số điện thoại hoặc CMND/CCCD");
        oTimKiem.addActionListener(e -> taiDuLieu());
        JButton nutTim = taoNut("Tìm kiếm", MAU_CHINH, MAU_CHINH_DAM);
        nutTim.addActionListener(e -> taiDuLieu());
        JButton nutThem = taoNutTrang("+ Thêm bệnh nhân");
        nutThem.addActionListener(e -> themMoi());
        soLuong.setFont(font(Font.BOLD, 12));
        soLuong.setForeground(MAU_CHINH);
        JPanel nutPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nutPhai.setOpaque(false);
        nutPhai.add(nutTim);
        nutPhai.add(nutThem);
        JPanel hangTim = new JPanel(new BorderLayout(8, 0));
        hangTim.setOpaque(false);
        hangTim.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hangTim.add(oTimKiem, BorderLayout.CENTER);
        hangTim.add(nutPhai, BorderLayout.EAST);

        JPanel phanBang = new JPanel(new BorderLayout(0, 6));
        phanBang.setOpaque(false);
        phanBang.add(hangTim, BorderLayout.NORTH);
        phanBang.add(new JScrollPane(bang), BorderLayout.CENTER);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] doRong = {70, 190, 95, 50, 80, 120, 220};
        for (int i = 0; i < doRong.length; i++) bang.getColumnModel().getColumn(i).setPreferredWidth(doRong[i]);
        bang.getSelectionModel().addListSelectionListener(e -> {
            int dong = bang.getSelectedRow();
            if (e.getValueIsAdjusting() || dong < 0 || dong >= dsKetQua.size()) return;
            hienChiTiet(dsKetQua.get(dong));
        });

        phanBang.add(taoKhungChiTiet(), BorderLayout.SOUTH);
        the.add(phanBang, BorderLayout.CENTER);
        add(the, BorderLayout.CENTER);
        xoaChiTiet();
    }

    // ===================== Thanh thông tin bệnh nhân đang chọn =====================

    private JComponent taoKhungChiTiet() {
        JPanel khung = new JPanel(new BorderLayout(12, 0));
        khung.setBackground(NEN_APP);
        khung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        tomTat.setFont(font(Font.BOLD, 13));
        tomTat.setForeground(CHU_CHINH);
        nutSua.setText("Xem / sửa hồ sơ");
        nutSua.addActionListener(e -> sua());
        khung.add(tomTat, BorderLayout.CENTER);
        khung.add(nutSua, BorderLayout.EAST);
        return khung;
    }

    // ===================== Dữ liệu =====================

    public void taiDuLieu() {
        taiDuLieu(dangChon == null ? -1 : dangChon.getMaBN());
    }

    /** Tải lại danh sách theo ô tìm kiếm; nếu {@code chonMa} có trong kết quả thì chọn lại dòng đó. */
    private void taiDuLieu(int chonMa) {
        try {
            dsKetQua = benhNhanDao.search(oTimKiem.getText());
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }
        model.setRowCount(0);
        int dongChon = -1;
        for (int i = 0; i < dsKetQua.size(); i++) {
            BenhNhan bn = dsKetQua.get(i);
            Integer tuoi = bn.getTuoi();
            model.addRow(new Object[]{String.format("BN%03d", bn.getMaBN()), bn.getHoTen(),
                    bn.getNgaySinh() == null ? "" : bn.getNgaySinh().format(NGAY),
                    tuoi == null ? "" : tuoi, bn.getGioiTinh(), bn.getSoDienThoai(), bn.getDiaChi()});
            if (bn.getMaBN() == chonMa) dongChon = i;
        }
        soLuong.setText(dsKetQua.size() + " bệnh nhân");
        if (dongChon >= 0) {
            bang.setRowSelectionInterval(dongChon, dongChon);
            hienChiTiet(dsKetQua.get(dongChon));
        } else {
            xoaChiTiet();
        }
    }

    private void hienChiTiet(BenhNhan bn) {
        dangChon = bn;
        nutSua.setEnabled(true);
        String t = String.format("BN%03d  ·  %s", bn.getMaBN(), bn.getHoTen());
        if (bn.getDiUng() != null) t += "   ⚠ Dị ứng: " + bn.getDiUng();
        tomTat.setText(t);
        tomTat.setForeground(bn.getDiUng() != null ? Color.decode("#B91C1C") : CHU_CHINH);
    }

    private void xoaChiTiet() {
        dangChon = null;
        nutSua.setEnabled(false);
        tomTat.setText("Chọn một bệnh nhân trong danh sách để xem hồ sơ đầy đủ.");
        tomTat.setForeground(CHU_PHU);
    }

    private void themMoi() {
        if (BenhNhanDialog.hienThi(this, null)) {
            oTimKiem.setText("");
            taiDuLieu(-1);
        }
    }

    private void sua() {
        if (dangChon == null) return;
        // Sửa trên bản sao để nếu hủy/lỗi thì dữ liệu đang hiển thị không bị lệch
        BenhNhan ban = saoChep(dangChon);
        if (BenhNhanDialog.hienThi(this, ban)) {
            taiDuLieu(ban.getMaBN());
        }
    }

    private BenhNhan saoChep(BenhNhan g) {
        BenhNhan b = new BenhNhan(g.getMaBN(), g.getHoTen(), g.getNgaySinh(), g.getGioiTinh(), g.getSoDienThoai(), g.getDiaChi());
        b.setCccd(g.getCccd());
        b.setEmail(g.getEmail());
        b.setNgheNghiep(g.getNgheNghiep());
        b.setTenNguoiLienHe(g.getTenNguoiLienHe());
        b.setSdtNguoiLienHe(g.getSdtNguoiLienHe());
        b.setDiUng(g.getDiUng());
        b.setTienSuBenh(g.getTienSuBenh());
        b.setGhiChu(g.getGhiChu());
        return b;
    }
}
