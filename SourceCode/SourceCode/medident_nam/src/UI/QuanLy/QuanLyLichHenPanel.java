package UI.QuanLy;

import util.BangStyle;
import Entity.LichHen;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import DAO.LichHenDAO;

public class QuanLyLichHenPanel extends JPanel {

    private LichHenDAO lichHenDAO = new LichHenDAO();
    private List<LichHen> danhSachLich = new ArrayList<>();

    private JTextField oTimKiem = new JTextField(18);
    private JLabel nhanTrangThai = new JLabel("Chưa tải dữ liệu");

    private JButton nutTim = new JButton("Tìm nhanh");
    private JButton nutTaiLai = new JButton("Tải lại");
    private JButton nutThem = new JButton("Thêm lịch");
    private JButton nutSua = new JButton("Sửa lịch");
    private JButton nutHuy = new JButton("Hủy lịch");
    private JButton nutXacNhan = new JButton("Xác nhận");
    private JButton nutDaKham = new JButton("Đã khám");

    private String[] tenCot = {
        "Mã lịch", "Mã bệnh nhân", "Mã bác sĩ", "Ngày hẹn",
        "Giờ", "Phút", "Phòng", "Trạng thái"
    };

    private DefaultTableModel moHinhBang = new DefaultTableModel(tenCot, 0) {
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return false;
        }
    };

    private JTable bangLichHen = new JTable(moHinhBang);

    public QuanLyLichHenPanel() {
        taoGiaoDien();
        ganSuKien();
    }

    // Tao giao dien quan ly lich hen
    private void taoGiaoDien() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel tieuDe = new JLabel("QUẢN LÝ LỊCH HẸN");
        tieuDe.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel phanTren = new JPanel(new BorderLayout(0, 10));
        phanTren.add(tieuDe, BorderLayout.NORTH);

        // Chinh kich thuoc o tim kiem
        oTimKiem.setPreferredSize(new Dimension(175, 38));
        oTimKiem.setMinimumSize(new Dimension(175, 38));
        oTimKiem.setMaximumSize(new Dimension(175, 38));
        oTimKiem.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Trang tri cac nut
        trangTriNut(nutTim, false, 100);
        trangTriNut(nutTaiLai, false, 80);
        trangTriNut(nutThem, false, 105);
        trangTriNut(nutSua, false, 90);
        trangTriNut(nutHuy, false, 90);
        trangTriNut(nutXacNhan, false, 105);
        trangTriNut(nutDaKham, false, 95);

        // Dua tat ca thanh phan len mot hang ngang
        JPanel thanhCongCu = new JPanel();
        thanhCongCu.setLayout(new BoxLayout(thanhCongCu, BoxLayout.X_AXIS));
        thanhCongCu.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

        JLabel nhanTim = new JLabel("Tìm nhanh:");
        nhanTim.setAlignmentY(Component.CENTER_ALIGNMENT);

        thanhCongCu.add(nhanTim);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(oTimKiem);
        thanhCongCu.add(Box.createHorizontalStrut(12));

        thanhCongCu.add(nutTim);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutTaiLai);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutThem);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutSua);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutHuy);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutXacNhan);
        thanhCongCu.add(Box.createHorizontalStrut(8));
        thanhCongCu.add(nutDaKham);
        thanhCongCu.add(Box.createHorizontalGlue());

        // Cuon ngang neu thu nho cua so
        JScrollPane cuonCongCu = new JScrollPane(thanhCongCu);
        cuonCongCu.setBorder(null);
        cuonCongCu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        cuonCongCu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cuonCongCu.setPreferredSize(new Dimension(0, 66));
        cuonCongCu.getHorizontalScrollBar().setUnitIncrement(16);

        phanTren.add(cuonCongCu, BorderLayout.CENTER);
        add(phanTren, BorderLayout.NORTH);

        // Giu nguyen JTable
        bangLichHen.setRowHeight(27);
        BangStyle.apDung(bangLichHen);
        bangLichHen.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangLichHen.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollBang = new JScrollPane(bangLichHen);
        BangStyle.apDungScrollPane(scrollBang);
        add(scrollBang, BorderLayout.CENTER);
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

        nutTaiLai.addActionListener(e -> {
            oTimKiem.setText("");
            taiDuLieu();
        });

        nutThem.addActionListener(e -> moBieuMau(false));
        nutSua.addActionListener(e -> moBieuMau(true));
        nutHuy.addActionListener(e -> huyLich());
        nutXacNhan.addActionListener(e -> doiTrangThai(LichHenDAO.DA_XAC_NHAN));
        nutDaKham.addActionListener(e -> doiTrangThai("Đã khám"));

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                taiDuLieu();
            }
        });
    }

    // =====================================================
    // TAI DU LIEU TU DAO
    // Goi truc tiep LichHenDAO tren luong giao dien (EDT) vi
    // ConnectDB dung 1 Connection Singleton dung chung, khong
    // an toan khi nhieu luong cung dung (transaction cua nut
    // Them/Sua lich doi autoCommit tren cung ket noi).
    // =====================================================
    public void taiDuLieu() {
        try {
            String tuKhoa = oTimKiem.getText().trim();

            nhanTrangThai.setText("Đang tải lịch hẹn...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                danhSachLich = layDanhSachTuDAO(tuKhoa);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }

            hienThiDanhSach();
            nhanTrangThai.setText("Đã tải " + danhSachLich.size() + " lịch hẹn.");

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Chon ham DAO phu hop: khong tu khoa -> getalllichhen(), co tu khoa -> timLichHen()
    private List<LichHen> layDanhSachTuDAO(String tuKhoa) throws Exception {
        if (tuKhoa.isEmpty()) {
            return lichHenDAO.getalllichhen();
        }

        return lichHenDAO.timLichHen(tuKhoa);
    }

    // Hien thi du lieu len JTable
    private void hienThiDanhSach() {
        moHinhBang.setRowCount(0);

        for (LichHen lich : danhSachLich) {
            moHinhBang.addRow(new Object[]{
                lich.maLichHen(), lich.maBN(), lich.maNV(), lich.ngayHen(),
                lich.gioHen(), lich.thoiLuongPhut(), lich.phongKham(), lich.trangThai()
            });
        }
    }

    // Lay lich hen dang duoc chon trong bang
    private LichHen layLichDangChon() {
        int dongDuocChon = bangLichHen.getSelectedRow();

        if (dongDuocChon < 0) {
            throw new IllegalArgumentException("Hãy chọn một lịch hẹn trong bảng.");
        }

        int dongDuLieu = bangLichHen.convertRowIndexToModel(dongDuocChon);

        if (dongDuLieu >= danhSachLich.size()) {
            throw new IllegalArgumentException("Dữ liệu đã thay đổi. Vui lòng tải lại.");
        }

        return danhSachLich.get(dongDuLieu);
    }

    // Mo form them hoac sua lich hen
    private void moBieuMau(boolean laSua) {
        try {
            LichHen lichCu = laSua ? layLichDangChon() : null;

            JTextField oBenhNhan = new JTextField(laSua ? String.valueOf(lichCu.maBN()) : "");
            JTextField oBacSi = new JTextField(laSua && lichCu.maNV() != null ? String.valueOf(lichCu.maNV()) : "");
            JTextField oNgay = new JTextField(laSua ? lichCu.ngayHen().toString() : LocalDate.now().plusDays(1).toString());
            JTextField oGio = new JTextField(laSua ? lichCu.gioHen().toString() : "09:00");
            JTextField oPhut = new JTextField(laSua ? String.valueOf(lichCu.thoiLuongPhut()) : "60");
            JTextField oPhong = new JTextField(laSua ? lichCu.phongKham() : "Phòng 01");

            JPanel bieuMau = new JPanel(new GridLayout(6, 2, 8, 8));
            bieuMau.add(new JLabel("Mã bệnh nhân *"));
            bieuMau.add(oBenhNhan);
            bieuMau.add(new JLabel("Mã bác sĩ *"));
            bieuMau.add(oBacSi);
            bieuMau.add(new JLabel("Ngày (yyyy-MM-dd) *"));
            bieuMau.add(oNgay);
            bieuMau.add(new JLabel("Giờ (HH:mm) *"));
            bieuMau.add(oGio);
            bieuMau.add(new JLabel("Thời lượng (phút) *"));
            bieuMau.add(oPhut);
            bieuMau.add(new JLabel("Phòng khám *"));
            bieuMau.add(oPhong);

            String tieuDe = laSua ? "Sửa lịch hẹn" : "Thêm lịch hẹn";
            int luaChon = JOptionPane.showConfirmDialog(this, bieuMau, tieuDe, JOptionPane.OK_CANCEL_OPTION);

            if (luaChon != JOptionPane.OK_OPTION) return;

            LichHen lichHen = new LichHen(
                laSua ? lichCu.maLichHen() : 0,
                Integer.parseInt(oBenhNhan.getText().trim()),
                Integer.parseInt(oBacSi.getText().trim()),
                LocalDate.parse(oNgay.getText().trim()),
                LocalTime.parse(oGio.getText().trim()),
                Integer.parseInt(oPhut.getText().trim()),
                oPhong.getText().trim(),
                laSua ? lichCu.trangThai() : LichHenDAO.CHO_XAC_NHAN
            );

            lichHenDAO.luu(lichHen, laSua);
            JOptionPane.showMessageDialog(this, "Đã lưu lịch hẹn thành công.");
            taiDuLieu();

        } catch (NumberFormatException loi) {
            JOptionPane.showMessageDialog(this, "Mã bệnh nhân, mã bác sĩ và số phút phải là số nguyên.");
        } catch (java.time.format.DateTimeParseException loi) {
            JOptionPane.showMessageDialog(this, "Ngày phải có dạng yyyy-MM-dd và giờ phải có dạng HH:mm.");
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Huy lich hen, khong xoa du lieu
    private void huyLich() {
        try {
            LichHen lichHen = layLichDangChon();

            int luaChon = JOptionPane.showConfirmDialog(this,
                "Hủy lịch " + lichHen.maLichHen() + "? Dữ liệu sẽ được giữ lại.",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);

            if (luaChon != JOptionPane.YES_OPTION) return;

            lichHenDAO.huy(lichHen.maLichHen());
            JOptionPane.showMessageDialog(this, "Đã hủy lịch hẹn.");
            taiDuLieu();

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Xac nhan lich hoac chuyen sang trang thai Da kham
    private void doiTrangThai(String trangThaiMoi) {
        try {
            LichHen lichHen = layLichDangChon();

            int luaChon = JOptionPane.showConfirmDialog(this,
                "Chuyển lịch " + lichHen.maLichHen() + " thành '" + trangThaiMoi + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (luaChon != JOptionPane.YES_OPTION) return;

            lichHenDAO.doiTrangThai(lichHen.maLichHen(), trangThaiMoi);
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái lịch hẹn.");
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
        JOptionPane.showMessageDialog(this, noiDung, "Lỗi lịch hẹn", JOptionPane.ERROR_MESSAGE);
    }

    // Chay rieng giao dien lich hen
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame khung = new JFrame("Kiểm tra giao diện Lịch hẹn");
            khung.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            khung.add(new QuanLyLichHenPanel());
            khung.setSize(1100, 650);
            khung.setLocationRelativeTo(null);
            khung.setVisible(true);
        });
    }
}
