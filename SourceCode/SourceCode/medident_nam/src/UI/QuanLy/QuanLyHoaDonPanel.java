package UI.QuanLy;

import DAO.HoaDonDAO;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyHoaDonPanel extends JPanel {

    private HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private List<HoaDonDAO.DongHoaDon> danhSachHoaDon = new ArrayList<>();

    private JTextField oTimKiem = new JTextField(13);
    private JTextField oNam = new JTextField(String.valueOf(LocalDate.now().getYear()), 5);
    private JComboBox<String> chonThang = new JComboBox<>();
    private JLabel nhanTrangThai = new JLabel("Chưa tải dữ liệu");

    private JButton nutTim = new JButton("Tìm nhanh");
    private JButton nutLoc = new JButton("Lọc tháng");
    private JButton nutTatCa = new JButton("Tất cả");
    private JButton nutThem = new JButton("Lập hóa đơn");
    private JButton nutDaThu = new JButton("Xác nhận thu");
    private JButton nutYeuCau = new JButton("Yêu cầu hủy");
    private JButton nutDuyet = new JButton("Duyệt / Từ chối");

    private String[] tenCot = {
        "Mã HĐ", "Mã phiếu khám", "Ngày lập", "Số tiền",
        "Phương thức", "Trạng thái", "Mã TT"
    };

    private DefaultTableModel moHinhBang = new DefaultTableModel(tenCot, 0) {
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return false;
        }
    };

    private JTable bangHoaDon = new JTable(moHinhBang);

    public QuanLyHoaDonPanel() {
        taoGiaoDien();
        ganSuKien();
    }

    // Tao giao dien quan ly hoa don
    private void taoGiaoDien() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tieuDe = new JLabel("QUẢN LÝ HÓA ĐƠN");
        tieuDe.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel phanTren = new JPanel(new BorderLayout(0, 10));
        phanTren.add(tieuDe, BorderLayout.NORTH);

        chonThang.addItem("Tất cả tháng");
        for (int i = 1; i <= 12; i++) {
            chonThang.addItem("Tháng " + i);
        }

        // Chinh kich thuoc o tim kiem va bo loc
        oTimKiem.setPreferredSize(new Dimension(140, 38));
        oTimKiem.setMinimumSize(new Dimension(140, 38));
        oTimKiem.setMaximumSize(new Dimension(140, 38));

        chonThang.setPreferredSize(new Dimension(115, 38));
        chonThang.setMinimumSize(new Dimension(115, 38));
        chonThang.setMaximumSize(new Dimension(115, 38));

        oNam.setPreferredSize(new Dimension(65, 38));
        oNam.setMinimumSize(new Dimension(65, 38));
        oNam.setMaximumSize(new Dimension(65, 38));

        oTimKiem.setAlignmentY(Component.CENTER_ALIGNMENT);
        chonThang.setAlignmentY(Component.CENTER_ALIGNMENT);
        oNam.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Trang tri cac nut
        trangTriNut(nutTim, false, 95);
        trangTriNut(nutLoc, false, 95);
        trangTriNut(nutTatCa, false, 75);
        trangTriNut(nutThem, false, 120);
        trangTriNut(nutDaThu, false, 125);
        trangTriNut(nutYeuCau, false, 115);
        trangTriNut(nutDuyet, false, 140);

        // Dua tat ca thanh phan len mot hang ngang
        JPanel thanhCongCu = new JPanel();
        thanhCongCu.setLayout(new BoxLayout(thanhCongCu, BoxLayout.X_AXIS));
        thanhCongCu.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

        JLabel nhanTim = new JLabel("Tìm nhanh:");
        JLabel nhanThang = new JLabel("Tháng:");
        JLabel nhanNam = new JLabel("Năm:");

        nhanTim.setAlignmentY(Component.CENTER_ALIGNMENT);
        nhanThang.setAlignmentY(Component.CENTER_ALIGNMENT);
        nhanNam.setAlignmentY(Component.CENTER_ALIGNMENT);

        thanhCongCu.add(nhanTim);
        thanhCongCu.add(Box.createHorizontalStrut(6));
        thanhCongCu.add(oTimKiem);
        thanhCongCu.add(Box.createHorizontalStrut(10));

        thanhCongCu.add(nhanThang);
        thanhCongCu.add(Box.createHorizontalStrut(6));
        thanhCongCu.add(chonThang);
        thanhCongCu.add(Box.createHorizontalStrut(10));

        thanhCongCu.add(nhanNam);
        thanhCongCu.add(Box.createHorizontalStrut(6));
        thanhCongCu.add(oNam);
        thanhCongCu.add(Box.createHorizontalStrut(12));

        thanhCongCu.add(nutTim);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutLoc);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutTatCa);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutThem);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutDaThu);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutYeuCau);
        thanhCongCu.add(Box.createHorizontalStrut(7));
        thanhCongCu.add(nutDuyet);
        thanhCongCu.add(Box.createHorizontalGlue());

        // Cuon ngang neu cua so nho
        JScrollPane cuonCongCu = new JScrollPane(thanhCongCu);
        cuonCongCu.setBorder(null);
        cuonCongCu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        cuonCongCu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cuonCongCu.setPreferredSize(new Dimension(0, 66));
        cuonCongCu.getHorizontalScrollBar().setUnitIncrement(16);

        phanTren.add(cuonCongCu, BorderLayout.CENTER);
        add(phanTren, BorderLayout.NORTH);

        // Giu nguyen JTable
        bangHoaDon.setRowHeight(27);
        bangHoaDon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangHoaDon.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        add(new JScrollPane(bangHoaDon), BorderLayout.CENTER);
        add(nhanTrangThai, BorderLayout.SOUTH);
    }

    // Trang tri cac nut chuc nang
    private void trangTriNut(JButton nut, boolean nutChinh, int chieuRong) {
        Color mauXanh = new Color(35, 116, 175);
        Color mauVien = new Color(195, 213, 228);

        nut.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        nut.setFont(new Font("Arial", Font.BOLD, 13));
        nut.setPreferredSize(new Dimension(chieuRong, 38));
        nut.setMinimumSize(new Dimension(chieuRong, 38));
        nut.setMaximumSize(new Dimension(chieuRong, 38));

        nut.setBackground(nutChinh ? mauXanh : Color.WHITE);
        nut.setForeground(nutChinh ? Color.WHITE : mauXanh);
        nut.setAlignmentY(Component.CENTER_ALIGNMENT);
        nut.setOpaque(true);
        nut.setContentAreaFilled(true);
        nut.setFocusPainted(false);

        nut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(nutChinh ? mauXanh : mauVien),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        nut.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Gan su kien cho cac nut chuc nang
    private void ganSuKien() {
        nutTim.addActionListener(e -> taiDuLieu());
        oTimKiem.addActionListener(e -> taiDuLieu());
        nutLoc.addActionListener(e -> taiDuLieu());

        nutTatCa.addActionListener(e -> {
            oTimKiem.setText("");
            chonThang.setSelectedIndex(0);
            taiDuLieu();
        });

        nutThem.addActionListener(e -> lapHoaDon());
        nutDaThu.addActionListener(e -> xacNhanThu());
        nutYeuCau.addActionListener(e -> guiYeuCauHuy());
        nutDuyet.addActionListener(e -> duyetYeuCau());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                taiDuLieu();
            }
        });
    }

    // =====================================================
    // TAI DU LIEU TU DAO
    // Goi truc tiep HoaDonDAO tren luong giao dien (EDT) vi
    // ConnectDB dung 1 Connection Singleton dung chung, khong
    // an toan khi nhieu luong cung dung (transaction cua nut
    // Lap hoa don / Xac nhan thu doi autoCommit tren cung ket noi).
    // =====================================================
    public void taiDuLieu() {
        try {
            int thang = chonThang.getSelectedIndex();
            Integer nam = null;

            if (thang > 0) {
                nam = Integer.parseInt(oNam.getText().trim());

                if (nam < 2000 || nam > 2100) {
                    throw new IllegalArgumentException("Năm phải từ 2000 đến 2100.");
                }
            }

            String tuKhoa = oTimKiem.getText().trim();
            Integer thangLoc = thang == 0 ? null : thang;

            nhanTrangThai.setText("Đang tải hóa đơn...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                danhSachHoaDon = layDanhSachTuDAO(tuKhoa, thangLoc, nam);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }

            hienThiDanhSach();
            nhanTrangThai.setText("Đã tải " + danhSachHoaDon.size() + " hóa đơn.");

        } catch (NumberFormatException loi) {
            JOptionPane.showMessageDialog(this, "Năm phải là số nguyên.");
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Chon ham DAO phu hop: khong loc -> getallhoadon(), co loc -> timHoaDon()
    private List<HoaDonDAO.DongHoaDon> layDanhSachTuDAO(
            String tuKhoa,
            Integer thang,
            Integer nam) throws Exception {

        boolean khongLoc = tuKhoa.isEmpty() && thang == null && nam == null;

        if (khongLoc) {
            return hoaDonDAO.getallhoadon();
        }

        return hoaDonDAO.timHoaDon(tuKhoa, thang, nam);
    }

    // Hien thi danh sach hoa don len JTable
    private void hienThiDanhSach() {
        moHinhBang.setRowCount(0);

        for (HoaDonDAO.DongHoaDon hoaDon : danhSachHoaDon) {
            moHinhBang.addRow(new Object[]{
                hoaDon.getMaHD(),
                hoaDon.getMaPK(),
                hoaDon.getNgayLap(),
                hoaDon.getSoTien(),
                hoaDon.getPhuongThuc(),
                hoaDon.getTrangThai(),
                hoaDon.getMaTT()
            });
        }
    }

    // Lay hoa don dang duoc chon trong bang
    private HoaDonDAO.DongHoaDon layHoaDonDangChon() {
        int dongDuocChon = bangHoaDon.getSelectedRow();

        if (dongDuocChon < 0) {
            throw new IllegalArgumentException("Hãy chọn một hóa đơn trên bảng.");
        }

        int dongDuLieu = bangHoaDon.convertRowIndexToModel(dongDuocChon);

        if (dongDuLieu >= danhSachHoaDon.size()) {
            throw new IllegalArgumentException("Dữ liệu đã thay đổi. Vui lòng tải lại.");
        }

        return danhSachHoaDon.get(dongDuLieu);
    }

    // Lap hoa don cho phieu kham da hoan thanh
    private void lapHoaDon() {
        try {
            JTextField oMaPhieu = new JTextField();
            JTextField oSoTien = new JTextField();
            JComboBox<String> chonPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});

            JPanel bieuMau = new JPanel(new GridLayout(3, 2, 8, 8));
            bieuMau.add(new JLabel("Mã phiếu khám đã hoàn thành:"));
            bieuMau.add(oMaPhieu);
            bieuMau.add(new JLabel("Tổng tiền (VNĐ):"));
            bieuMau.add(oSoTien);
            bieuMau.add(new JLabel("Phương thức:"));
            bieuMau.add(chonPhuongThuc);

            int luaChon = JOptionPane.showConfirmDialog(this, bieuMau, "Lập hóa đơn", JOptionPane.OK_CANCEL_OPTION);
            if (luaChon != JOptionPane.OK_OPTION) return;

            int maPhieu = Integer.parseInt(oMaPhieu.getText().trim());
            BigDecimal soTien = new BigDecimal(oSoTien.getText().trim().replace(",", ""));
            String phuongThuc = (String) chonPhuongThuc.getSelectedItem();

            hoaDonDAO.tao(maPhieu, soTien, phuongThuc);
            JOptionPane.showMessageDialog(this, "Đã lập hóa đơn. Chưa được tính là đã thực thu.");
            taiDuLieu();

        } catch (NumberFormatException loi) {
            JOptionPane.showMessageDialog(this, "Mã phiếu hoặc số tiền không hợp lệ.");
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Xac nhan da nhan tien thanh toan
    private void xacNhanThu() {
        try {
            HoaDonDAO.DongHoaDon hoaDon = layHoaDonDangChon();

            String noiDung = "Đã nhận " + hoaDon.getSoTien() + " VNĐ cho hóa đơn " + hoaDon.getMaHD() + "?";
            int luaChon = JOptionPane.showConfirmDialog(this, noiDung, "Xác nhận thực thu", JOptionPane.YES_NO_OPTION);

            if (luaChon != JOptionPane.YES_OPTION) return;

            hoaDonDAO.xacNhanDaThu(hoaDon.getMaHD());
            JOptionPane.showMessageDialog(this, "Đã xác nhận thanh toán thành công.");
            taiDuLieu();

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Gui yeu cau huy hoa don
    private void guiYeuCauHuy() {
        try {
            HoaDonDAO.DongHoaDon hoaDon = layHoaDonDangChon();
            String lyDo = JOptionPane.showInputDialog(this, "Lý do muốn hủy hóa đơn " + hoaDon.getMaHD() + ":");

            if (lyDo == null) return;

            hoaDonDAO.guiYeuCauHuy(hoaDon.getMaHD(), lyDo.trim());
            JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu hủy để quản lý duyệt.");

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Lay danh sach yeu cau va xu ly duyet huy
    private void duyetYeuCau() {
        try {
            List<String> danhSachYeuCau = hoaDonDAO.yeuCauChoDuyet();

            if (danhSachYeuCau.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có yêu cầu chờ duyệt.");
                return;
            }

            Object yeuCauDuocChon = JOptionPane.showInputDialog(this, "Chọn yêu cầu:", "Duyệt hủy phiếu thu",
                    JOptionPane.QUESTION_MESSAGE, null, danhSachYeuCau.toArray(), danhSachYeuCau.get(0));

            if (yeuCauDuocChon == null) return;

            int maYeuCau = Integer.parseInt(yeuCauDuocChon.toString().split(" - ")[0]);
            Object[] cacNut = {"Đồng ý hủy", "Từ chối", "Thoát"};

            int luaChon = JOptionPane.showOptionDialog(this, "Chỉ được duyệt hủy phiếu CHƯA thực thu.",
                    "Xử lý yêu cầu", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, cacNut, cacNut[2]);

            if (luaChon != 0 && luaChon != 1) return;

            hoaDonDAO.duyetHuy(maYeuCau, luaChon == 0);
            JOptionPane.showMessageDialog(this, "Đã xử lý yêu cầu hủy.");
            taiDuLieu();

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Hien thi thong bao loi
    private void baoLoi(Exception loi) {
        setCursor(Cursor.getDefaultCursor());

        Throwable nguyenNhan = loi.getCause() == null ? loi : loi.getCause();
        String noiDung = nguyenNhan.getMessage();

        if (noiDung == null || noiDung.isBlank()) {
            noiDung = nguyenNhan.getClass().getSimpleName();
        }

        nhanTrangThai.setText("Lỗi: " + noiDung);
        JOptionPane.showMessageDialog(this, noiDung, "Lỗi hóa đơn", JOptionPane.ERROR_MESSAGE);
    }

    // Chay rieng giao dien hoa don
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame khung = new JFrame("Kiểm tra giao diện Hóa đơn");
            khung.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            khung.add(new QuanLyHoaDonPanel());
            khung.setSize(1350, 650);
            khung.setLocationRelativeTo(null);
            khung.setVisible(true);
        });
    }
}
