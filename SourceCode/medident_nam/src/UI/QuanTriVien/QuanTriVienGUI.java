
package UI.QuanTriVien;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import Entity.VaiTro;
import UI.Chung.FullGUI;
import UI.Chung.GiaoDienHeThong;
import UI.Chung.HoTroPage;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public final class QuanTriVienGUI extends JFrame {
    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel menu = new JPanel();
    private final JPanel noiDung = new JPanel(new BorderLayout());
    private final Map<String, Boolean> quyenMenu = new LinkedHashMap<>();
    private final Map<String, JButton> nutMenu = new LinkedHashMap<>();
    private String maDangChon = "HOME";

    public QuanTriVienGUI(TaiKhoan taiKhoan, TaiKhoanDAO dao) {
        if (taiKhoan == null || dao == null
                || !"Quản trị viên".equals(taiKhoan.tenVaiTro)) {
            throw new IllegalArgumentException(
                    "Tài khoản không thuộc vai trò Quản trị viên.");
        }

        this.taiKhoan = taiKhoan;
        this.dao = dao;

        setTitle("MediDent - Quản trị viên");
        setSize(1350, 850);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel khung = new JPanel(new BorderLayout());
        khung.setBackground(Color.WHITE);
        setContentPane(khung);

        JButton dangXuat = GiaoDienHeThong.nut("Đăng xuất", false);
        dangXuat.addActionListener(e -> {
            dispose();
            new UI.DangNhap.DangNhap(dao).setVisible(true);
        });

        khung.add(GiaoDienHeThong.thanhBen(menu, dangXuat),
                BorderLayout.WEST);

        JPanel benPhai = new JPanel(new BorderLayout());
        benPhai.setBackground(Color.WHITE);

        benPhai.add(GiaoDienHeThong.dauTrang(
                "Quản trị hệ thống", taiKhoan.hoTen,
                taiKhoan.tenVaiTro), BorderLayout.NORTH);

        benPhai.add(noiDung, BorderLayout.CENTER);
        khung.add(benPhai, BorderLayout.CENTER);

        hienThiPanel(new TrangChuQuanTriVien(dao, taiKhoan), "HOME");
        taiQuyen();
    }

    // Đọc quyền của quản trị viên.
    private void taiQuyen() {
        new SwingWorker<List<VaiTro>, Void>() {
            @Override
            protected List<VaiTro> doInBackground() throws Exception {
                return dao.vaiTro();
            }

            @Override
            protected void done() {
                try {
                    quyenMenu.clear();
                    String[] dsMa = TaiKhoanDAO.cacQuyen();

                    for (VaiTro vt : get()) {
                        if (!"Quản trị viên".equals(vt.ten)) continue;

                        for (int i = 0; i < dsMa.length; i++) {
                            quyenMenu.put(dsMa[i],
                                    i < vt.quyen.length && vt.quyen[i]);
                        }
                    }

                    // Giữ ngoại lệ của tài khoản kiểm thử.
                    if ("full".equals(taiKhoan.tenDangNhap)) {
                        for (String ma : dsMa) quyenMenu.put(ma, true);
                    }

                    hienThiMenu();
                } catch (Exception loi) {
                    GiaoDienHeThong.loi(QuanTriVienGUI.this, loi);
                }
            }
        }.execute();
    }

    // Tạo nút menu bằng class giao diện chung.
    private JButton themNut(String ma, String ten) {
        JButton nut = GiaoDienHeThong.nutMenu(ten);
        nut.setAlignmentX(Component.LEFT_ALIGNMENT);

        nutMenu.put(ma, nut);
        menu.add(nut);
        menu.add(Box.createVerticalStrut(7));
        return nut;
    }

    // Hiển thị một JPanel của quản trị viên.
    private void hienThiPanel(JPanel panel, String ma) {
        GiaoDienHeThong.dongBo(panel);

        noiDung.removeAll();
        noiDung.add(panel, BorderLayout.CENTER);
        noiDung.revalidate();
        noiDung.repaint();

        maDangChon = ma;
        GiaoDienHeThong.chonMenu(nutMenu, maDangChon);
    }

    // Xây dựng sidebar theo quyền trong database.
    private void hienThiMenu() {
        menu.removeAll();
        nutMenu.clear();

        JLabel nhan = GiaoDienHeThong.label(
                "CHỨC NĂNG HỆ THỐNG", 12, true);
        nhan.setForeground(GiaoDienHeThong.PRIMARY);
        menu.add(nhan);
        menu.add(Box.createVerticalStrut(12));

        themNut("HOME", "Trang chủ").addActionListener(e ->
                hienThiPanel(new TrangChuQuanTriVien(
                        dao, taiKhoan), "HOME"));

        if (quyenMenu.getOrDefault("TAIKHOAN", false)) {
            themNut("TAIKHOAN", "Quản lý tài khoản").addActionListener(e ->
                    hienThiPanel(new QuanLyTaiKhoan(
                            dao, taiKhoan), "TAIKHOAN"));
        }

        if (quyenMenu.getOrDefault("PHANQUYEN", false)) {
            themNut("PHANQUYEN", "Quản lý phân quyền").addActionListener(e ->
                    hienThiPanel(new QuanLyPhanQuyen(
                            dao, taiKhoan), "PHANQUYEN"));
        }

        if (quyenMenu.getOrDefault("HOTRO", false)) {
            themNut("HOTRO", "Hỗ trợ").addActionListener(e ->
                    hienThiPanel(new HoTroPage(taiKhoan), "HOTRO"));
        }

        // Chỉ tài khoản full mới có nút trình bày toàn hệ thống.
        if ("full".equals(taiKhoan.tenDangNhap)) {
            menu.add(Box.createVerticalStrut(12));

            JLabel nhanDemo = GiaoDienHeThong.label(
                    "CHẾ ĐỘ TRÌNH BÀY", 12, true);
            nhanDemo.setForeground(GiaoDienHeThong.PRIMARY);
            menu.add(nhanDemo);
            menu.add(Box.createVerticalStrut(8));

            themNut("FULL", "Toàn bộ chức năng").addActionListener(e -> {
                try {
                    FullGUI fullGUI = new FullGUI(taiKhoan, dao);
                    fullGUI.setVisible(true);
                    dispose();
                } catch (Exception loi) {
                    GiaoDienHeThong.loi(this, loi);
                }
            });
        }

        GiaoDienHeThong.chonMenu(nutMenu, maDangChon);
        menu.revalidate();
        menu.repaint();
    }
}
