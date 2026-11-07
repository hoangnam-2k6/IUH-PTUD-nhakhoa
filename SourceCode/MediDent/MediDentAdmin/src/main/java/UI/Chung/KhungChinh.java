package UI.Chung;

import Class.TaiKhoan;
import Class.VaiTro;
import Enum.VaiTroHeThong;
import DAO.TaiKhoanDAO;
import UI.QuanTriVien.QuanLyPhanQuyen;
import UI.QuanTriVien.QuanLyTaiKhoan;
import UI.QuanTriVien.TrangChuQuanTriVien;
import java.awt.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

/** Khung chung: menu theo vai trò, mở trang và đánh dấu nút đang chọn. */
public final class KhungChinh extends JFrame {
    private static final Color XANH = new Color(0x0284C7);
    private static final Color XANH_DAM = new Color(0x0369A1);
    private static final Color NEN = new Color(0xF8FAFC);
    private static final Color VIEN = new Color(0xE2E8F0);
    private static final Color CHU = new Color(0x0F172A);
    private static final Color CHU_PHU = new Color(0x64748B);

    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel noiDung = new JPanel(new BorderLayout());
    private final JPanel menu = new JPanel();
    private final Map<String, JButton> nutMenu = new LinkedHashMap<>();

    // Mỗi vai trò có một Map: tên chức năng -> được hiển thị (true/false).
    // Các dấu tích được lưu trong VaiTroQuyen và đọc lại sau khi đăng nhập.
    private final Map<String, Boolean> mapQuanTriVien = new LinkedHashMap<>();
    private final Map<String, Boolean> mapQuanLy = new LinkedHashMap<>();
    private final Map<String, Boolean> mapLeTan = new LinkedHashMap<>();
    private String trangDangMo = "HOME";

    public KhungChinh(TaiKhoan taiKhoan, TaiKhoanDAO dao) {
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        setTitle("MEDIDENT - " + taiKhoan.tenVaiTro);
        setSize(1220, 790);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel khung = new JPanel(new BorderLayout());
        setContentPane(khung);
        khung.add(taoMenu(), BorderLayout.WEST);
        noiDung.setBackground(NEN);
        khung.add(noiDung, BorderLayout.CENTER);

        hienThiPanel(trangChu(), "HOME"); // Đăng nhập xong mở trang chủ.
        taiQuyen();                 // Nạp quyền và hiển thị các nút menu được tích.
    }

    private JPanel taoMenu() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, VIEN));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 20));
        logo.setBackground(Color.WHITE);
        URL url = getClass().getResource("/Image/medident_logo.png");
        if (url != null) {
            Image anh = new ImageIcon(url).getImage().getScaledInstance(46, 46, Image.SCALE_SMOOTH);
            logo.add(new JLabel(new ImageIcon(anh)));
        }
        JPanel nhanHieu = new JPanel();
        nhanHieu.setLayout(new BoxLayout(nhanHieu, BoxLayout.Y_AXIS));
        nhanHieu.setOpaque(false);
        JLabel ten = new JLabel("MEDIDENT");
        ten.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ten.setForeground(XANH);
        JLabel moTa = new JLabel("Quản lý nha khoa");
        moTa.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nhanHieu.add(ten);
        nhanHieu.add(moTa);
        logo.add(nhanHieu);
        sidebar.add(logo, BorderLayout.NORTH);

        menu.setBackground(Color.WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(18, 10, 10, 10));
        JLabel nhan = new JLabel("CHỨC NĂNG HỆ THỐNG");
        nhan.setForeground(CHU_PHU);
        nhan.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nhan.setAlignmentX(LEFT_ALIGNMENT);
        menu.add(nhan);
        menu.add(Box.createVerticalStrut(16));
        JScrollPane cuon = new JScrollPane(menu);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(14);
        sidebar.add(cuon, BorderLayout.CENTER);

        JPanel duoi = new JPanel();
        duoi.setLayout(new BoxLayout(duoi, BoxLayout.Y_AXIS));
        duoi.setBackground(Color.WHITE);
        duoi.setBorder(new EmptyBorder(10, 13, 18, 13));
        JLabel nguoiDung = new JLabel("Đăng nhập: " + taiKhoan.hoTen);
        nguoiDung.setFont(new Font("Segoe UI", Font.BOLD, 11));
        JLabel vaiTro = new JLabel(taiKhoan.tenVaiTro);
        vaiTro.setForeground(CHU_PHU);
        for (JLabel label : new JLabel[]{nguoiDung, vaiTro}) {
            label.setAlignmentX(LEFT_ALIGNMENT);
            duoi.add(label);
            duoi.add(Box.createVerticalStrut(6));
        }
        JButton dangXuat = new JButton("Đăng xuất");
        dangXuat.setAlignmentX(LEFT_ALIGNMENT);
        dangXuat.setBackground(Color.WHITE);
        dangXuat.setBorder(new CompoundBorder(new LineBorder(VIEN, 1, true), new EmptyBorder(10, 14, 10, 14)));
        dangXuat.addActionListener(e -> {
            dispose();
            new UI.DangNhap.DangNhap(dao).setVisible(true);
        });
        duoi.add(dangXuat);
        sidebar.add(duoi, BorderLayout.SOUTH);
        return sidebar;
    }

    /** Đọc checkbox từ SQL và tạo ba Map quyền có cùng tên vai trò. */
    private void taiQuyen() {
        new SwingWorker<List<VaiTro>, Void>() {
            @Override protected List<VaiTro> doInBackground() throws Exception {
                return dao.vaiTro();
            }
            @Override protected void done() {
                try {
                    String[] tenQuyen = TaiKhoanDAO.tenCacQuyen();
                    for (VaiTro vaiTro : get()) {
                        Map<String, Boolean> map;
                        if ("Quản trị viên".equals(vaiTro.ten)) map = mapQuanTriVien;
                        else if ("Quản lý phòng khám".equals(vaiTro.ten)) map = mapQuanLy;
                        else if ("Lễ tân".equals(vaiTro.ten)) map = mapLeTan;
                        else continue;
                        map.clear();
                        for (int i = 0; i < tenQuyen.length; i++) {
                            map.put(tenQuyen[i], i < vaiTro.quyen.length && vaiTro.quyen[i]);
                        }
                    }
                    hienThiMenu();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(KhungChinh.this,
                            "Không tải được quyền truy cập. Hãy đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Hiển thị tất cả mục đã được tích quyền theo vai trò. */
    private void hienThiMenu() {
        Map<String, Boolean> quyen;
        if ("Quản trị viên".equals(taiKhoan.tenVaiTro)) quyen = mapQuanTriVien;
        else if ("Quản lý phòng khám".equals(taiKhoan.tenVaiTro)) quyen = mapQuanLy;
        else quyen = mapLeTan;

        // Giữ tiêu đề menu ở hai vị trí đầu.
        while (menu.getComponentCount() > 2) menu.remove(2);
        nutMenu.clear();

        JButton btnTrangChu = themNut("HOME", "Trang chủ");
        btnTrangChu.addActionListener(e -> hienThiPanel(trangChu(), "HOME"));

        if (VaiTroHeThong.QUAN_TRI_VIEN.ten().equals(taiKhoan.tenVaiTro)
                && quyen.getOrDefault("Quản lý tài khoản", false)) {
            JButton btnTaiKhoan = themNut("TAIKHOAN", "Quản lý tài khoản");
            btnTaiKhoan.addActionListener(e -> hienThiPanel(new QuanLyTaiKhoan(dao, taiKhoan), "TAIKHOAN"));
        }
        if (VaiTroHeThong.QUAN_TRI_VIEN.ten().equals(taiKhoan.tenVaiTro)
                && quyen.getOrDefault("Phân quyền tài khoản", false)) {
            JButton btnPhanQuyen = themNut("PHANQUYEN", "Phân quyền tài khoản");
            btnPhanQuyen.addActionListener(e -> hienThiPanel(new QuanLyPhanQuyen(dao, taiKhoan), "PHANQUYEN"));
        }
        if (quyen.getOrDefault("Hỗ trợ", false)) {
            JButton btnHoTro = themNut("HOTRO", "Hỗ trợ");
            btnHoTro.addActionListener(e -> hienThiPanel(new HoTroPage(taiKhoan), "HOTRO"));
        }

        // Các quyền nghiệp vụ đã có trong SQL nhưng chưa có màn hình Java.
        // Tạo nút theo checkbox; khi bấm, khung sẽ giải thích rõ trạng thái.
        if (quyen.getOrDefault("Quản lý nhân viên", false)) {
            JButton btnNhanVien = themNut("QL_NHANVIEN", "Quản lý nhân viên");
            btnNhanVien.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_NHANVIEN"), "QL_NHANVIEN"));
        }
        if (quyen.getOrDefault("Quản lý dịch vụ", false)) {
            JButton btnDichVu = themNut("QL_DICHVU", "Quản lý dịch vụ");
            btnDichVu.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_DICHVU"), "QL_DICHVU"));
        }
        if (quyen.getOrDefault("Quản lý bệnh nhân", false)) {
            JButton btnBenhNhan = themNut("QL_BENHNHAN", "Quản lý bệnh nhân");
            btnBenhNhan.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_BENHNHAN"), "QL_BENHNHAN"));
        }
        if (quyen.getOrDefault("Lịch hẹn (Quản lý)", false)) {
            JButton btnLichHen = themNut("QL_LICHHEN", "Quản lý lịch hẹn");
            btnLichHen.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_LICHHEN"), "QL_LICHHEN"));
        }
        if (quyen.getOrDefault("Thanh toán (Quản lý)", false)) {
            JButton btnThanhToan = themNut("QL_THANHTOAN", "Thanh toán và hóa đơn");
            btnThanhToan.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_THANHTOAN"), "QL_THANHTOAN"));
        }
        if (quyen.getOrDefault("Báo cáo (Quản lý)", false)) {
            JButton btnBaoCao = themNut("QL_BAOCAO", "Báo cáo - thống kê");
            btnBaoCao.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("QL_BAOCAO"), "QL_BAOCAO"));
        }
        if (quyen.getOrDefault("Khám và điều trị", false)) {
            JButton btnKham = themNut("LT_KHAM", "Khám và điều trị");
            btnKham.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("LT_KHAM"), "LT_KHAM"));
        }
        if (quyen.getOrDefault("Tiếp nhận bệnh nhân", false)) {
            JButton btnTiepNhan = themNut("LT_TIEPNHAN", "Tiếp nhận bệnh nhân");
            btnTiepNhan.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("LT_TIEPNHAN"), "LT_TIEPNHAN"));
        }
        if (quyen.getOrDefault("Lịch hẹn (Lễ tân)", false)) {
            JButton btnLichHenLeTan = themNut("LT_LICHHEN", "Quản lý lịch hẹn");
            btnLichHenLeTan.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("LT_LICHHEN"), "LT_LICHHEN"));
        }
        if (quyen.getOrDefault("Thanh toán (Lễ tân)", false)) {
            JButton btnThuNgan = themNut("LT_THANHTOAN", "Thanh toán và hóa đơn");
            btnThuNgan.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("LT_THANHTOAN"), "LT_THANHTOAN"));
        }
        if (quyen.getOrDefault("Tra cứu thông tin", false)) {
            JButton btnTraCuu = themNut("LT_TRACUU", "Tra cứu thông tin");
            btnTraCuu.addActionListener(e -> hienThiPanel(trangChuaHoanThanh("LT_TRACUU"), "LT_TRACUU"));
        }

        chonNut();
        menu.revalidate();
        menu.repaint();
    }

    /** Chỉ tạo kiểu dáng nút; sự kiện click đặt ngay dưới tên nút để dễ đọc. */
    private JButton themNut(String ma, String ten) {
        JButton nut = new JButton(ten) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D ve = (Graphics2D) g.create();
                ve.setColor(getBackground());
                ve.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                ve.dispose();
                super.paintComponent(g);
            }
        };
        nut.setContentAreaFilled(false);
        nut.setOpaque(false);
        nut.setHorizontalAlignment(SwingConstants.LEFT);
        nut.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nut.setBackground(Color.WHITE);
        nut.setFocusPainted(false);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.setBorder(new CompoundBorder(new LineBorder(VIEN, 1, true), new EmptyBorder(12, 13, 12, 8)));
        nut.setAlignmentX(LEFT_ALIGNMENT);
        nut.setMaximumSize(new Dimension(225, 46));
        nutMenu.put(ma, nut);
        menu.add(nut);
        menu.add(Box.createVerticalStrut(7));
        return nut;
    }

    /** Chỉ thay panel trong vùng nội dung; màn hình được new trực tiếp ở nút. */
    private void hienThiPanel(JPanel trang, String ma) {
        noiDung.removeAll();
        noiDung.add(trang, BorderLayout.CENTER);
        noiDung.revalidate();
        noiDung.repaint();
        trangDangMo = ma;
        chonNut();
    }

    private JPanel trangChu() {
        if (VaiTroHeThong.QUAN_TRI_VIEN.ten().equals(taiKhoan.tenVaiTro)) {
            return new TrangChuQuanTriVien(dao, taiKhoan);
        }
        JPanel trang = new JPanel(new BorderLayout());
        trang.setBackground(NEN);
        JLabel chao = new JLabel("Trang chủ - " + taiKhoan.tenVaiTro);
        chao.setFont(new Font("Segoe UI", Font.BOLD, 25));
        chao.setBorder(new EmptyBorder(32, 36, 0, 0));
        trang.add(chao, BorderLayout.NORTH);
        return trang;
    }

    /** Nghiệp vụ chưa có trong ZIP gốc: giữ thông báo, không tạo chức năng giả. */
    private JPanel trangChuaHoanThanh(String ma) {
        JPanel trang = new JPanel(new BorderLayout());
        trang.setBackground(NEN);
        JLabel ghiChu = new JLabel("Chức năng chưa được xây dựng lại: " + tenTrang(ma));
        ghiChu.setFont(new Font("Segoe UI", Font.BOLD, 20));
        ghiChu.setForeground(CHU);
        ghiChu.setBorder(new EmptyBorder(32, 36, 0, 0));
        trang.add(ghiChu, BorderLayout.NORTH);
        return trang;
    }

    private String tenTrang(String ma) {
        switch (ma) {
            case "QL_NHANVIEN": return "Quản lý nhân viên";
            case "QL_DICHVU": return "Quản lý dịch vụ";
            case "QL_BENHNHAN": return "Quản lý bệnh nhân";
            case "QL_LICHHEN": case "LT_LICHHEN": return "Quản lý lịch hẹn";
            case "QL_THANHTOAN": case "LT_THANHTOAN": return "Thanh toán và hóa đơn";
            case "QL_BAOCAO": return "Báo cáo - thống kê";
            case "LT_KHAM": return "Khám và điều trị";
            case "LT_TIEPNHAN": return "Tiếp nhận bệnh nhân";
            case "LT_TRACUU": return "Tra cứu thông tin";
            default: return ma;
        }
    }

    /** Chỉ nút đang mở có nền xanh đậm; nút còn lại trở về màu trắng. */
    private void chonNut() {
        for (Map.Entry<String, JButton> entry : nutMenu.entrySet()) {
            boolean dangChon = entry.getKey().equals(trangDangMo);
            entry.getValue().setBackground(dangChon ? XANH_DAM : Color.WHITE);
            entry.getValue().setForeground(dangChon ? Color.WHITE : CHU_PHU);

        }
    }
}
