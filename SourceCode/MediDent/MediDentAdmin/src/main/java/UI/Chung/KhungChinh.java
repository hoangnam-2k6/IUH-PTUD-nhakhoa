package UI.Chung;

import Class.TaiKhoan;
import Class.VaiTro;
import DAO.HoTroDAO;
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
        JPanel benPhai = new JPanel(new BorderLayout());
        benPhai.add(taoHeader(), BorderLayout.NORTH);
        noiDung.setBackground(NEN);
        benPhai.add(noiDung, BorderLayout.CENTER);
        khung.add(benPhai, BorderLayout.CENTER);

        moTrang("HOME");            // Đăng nhập xong luôn mở trang chủ / Dashboard.
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
        JLabel ten = new JLabel("<html><b style='font-size:16px'>MEDIDENT</b><br>Quản lý nha khoa</html>");
        ten.setForeground(CHU);
        logo.add(ten);
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

    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, VIEN), new EmptyBorder(20, 25, 20, 25)));
        JLabel tieuDe = new JLabel("MEDIDENT     /     Quản lý nha khoa");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.add(tieuDe, BorderLayout.WEST);
        JLabel nguoiDung = new JLabel("<html><div style='text-align:right'><b>" + taiKhoan.hoTen
                + "</b><br>" + taiKhoan.tenVaiTro + "</div></html>");
        header.add(nguoiDung, BorderLayout.EAST);
        return header;
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
        btnTrangChu.addActionListener(e -> moTrang("HOME"));

        if (quyen.getOrDefault("Quản lý tài khoản", false)) {
            JButton btnTaiKhoan = themNut("TAIKHOAN", "Quản lý tài khoản");
            btnTaiKhoan.addActionListener(e -> moTrang("TAIKHOAN"));
        }
        if (quyen.getOrDefault("Phân quyền tài khoản", false)) {
            JButton btnPhanQuyen = themNut("PHANQUYEN", "Phân quyền tài khoản");
            btnPhanQuyen.addActionListener(e -> moTrang("PHANQUYEN"));
        }
        if (quyen.getOrDefault("Hỗ trợ", false)) {
            JButton btnHoTro = themNut("HOTRO", "Hỗ trợ");
            btnHoTro.addActionListener(e -> moTrang("HOTRO"));
        }

        // Các quyền nghiệp vụ đã có trong SQL nhưng chưa có màn hình Java.
        // Tạo nút theo checkbox; khi bấm, khung sẽ giải thích rõ trạng thái.
        if (quyen.getOrDefault("Quản lý nhân viên", false)) {
            JButton btnNhanVien = themNut("QL_NHANVIEN", "Quản lý nhân viên");
            btnNhanVien.addActionListener(e -> moTrang("QL_NHANVIEN"));
        }
        if (quyen.getOrDefault("Quản lý dịch vụ", false)) {
            JButton btnDichVu = themNut("QL_DICHVU", "Quản lý dịch vụ");
            btnDichVu.addActionListener(e -> moTrang("QL_DICHVU"));
        }
        if (quyen.getOrDefault("Quản lý bệnh nhân", false)) {
            JButton btnBenhNhan = themNut("QL_BENHNHAN", "Quản lý bệnh nhân");
            btnBenhNhan.addActionListener(e -> moTrang("QL_BENHNHAN"));
        }
        if (quyen.getOrDefault("Lịch hẹn (Quản lý)", false)) {
            JButton btnLichHen = themNut("QL_LICHHEN", "Quản lý lịch hẹn");
            btnLichHen.addActionListener(e -> moTrang("QL_LICHHEN"));
        }
        if (quyen.getOrDefault("Thanh toán (Quản lý)", false)) {
            JButton btnThanhToan = themNut("QL_THANHTOAN", "Thanh toán và hóa đơn");
            btnThanhToan.addActionListener(e -> moTrang("QL_THANHTOAN"));
        }
        if (quyen.getOrDefault("Báo cáo (Quản lý)", false)) {
            JButton btnBaoCao = themNut("QL_BAOCAO", "Báo cáo - thống kê");
            btnBaoCao.addActionListener(e -> moTrang("QL_BAOCAO"));
        }
        if (quyen.getOrDefault("Khám và điều trị", false)) {
            JButton btnKham = themNut("LT_KHAM", "Khám và điều trị");
            btnKham.addActionListener(e -> moTrang("LT_KHAM"));
        }
        if (quyen.getOrDefault("Tiếp nhận bệnh nhân", false)) {
            JButton btnTiepNhan = themNut("LT_TIEPNHAN", "Tiếp nhận bệnh nhân");
            btnTiepNhan.addActionListener(e -> moTrang("LT_TIEPNHAN"));
        }
        if (quyen.getOrDefault("Lịch hẹn (Lễ tân)", false)) {
            JButton btnLichHenLeTan = themNut("LT_LICHHEN", "Quản lý lịch hẹn");
            btnLichHenLeTan.addActionListener(e -> moTrang("LT_LICHHEN"));
        }
        if (quyen.getOrDefault("Thanh toán (Lễ tân)", false)) {
            JButton btnThuNgan = themNut("LT_THANHTOAN", "Thanh toán và hóa đơn");
            btnThuNgan.addActionListener(e -> moTrang("LT_THANHTOAN"));
        }
        if (quyen.getOrDefault("Tra cứu thông tin", false)) {
            JButton btnTraCuu = themNut("LT_TRACUU", "Tra cứu thông tin");
            btnTraCuu.addActionListener(e -> moTrang("LT_TRACUU"));
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
        nut.setIcon(new BieuTuong(bieuTuong(ma), CHU_PHU, 17));
        nut.setIconTextGap(12);
        nutMenu.put(ma, nut);
        menu.add(nut);
        menu.add(Box.createVerticalStrut(7));
        return nut;
    }

    private void moTrang(String ma) {
        JPanel trang;
        switch (ma) {
            case "TAIKHOAN": trang = new QuanLyTaiKhoan(dao, taiKhoan); break;
            case "PHANQUYEN": trang = new QuanLyPhanQuyen(dao, taiKhoan); break;
            case "HOTRO": trang = new HoTroPage(new HoTroDAO(), taiKhoan,
                    "Quản trị viên".equals(taiKhoan.tenVaiTro)); break;
            case "HOME":
                if ("Quản trị viên".equals(taiKhoan.tenVaiTro)) {
                    trang = new TrangChuQuanTriVien(dao, taiKhoan);
                } else {
                    trang = new JPanel(new BorderLayout());
                    trang.setBackground(NEN);
                    JLabel chao = new JLabel("Trang chủ - " + taiKhoan.tenVaiTro);
                    chao.setFont(new Font("Segoe UI", Font.BOLD, 25));
                    chao.setBorder(new EmptyBorder(32, 36, 0, 0));
                    trang.add(chao, BorderLayout.NORTH);
                }
                break;
            default:
                // Chưa khôi phục màn hình nghiệp vụ của Quản lý và Lễ tân.
                trang = new JPanel(new BorderLayout());
                trang.setBackground(NEN);
                JLabel ghiChu = new JLabel("Chức năng chưa được xây dựng lại: " + tenTrang(ma));
                ghiChu.setFont(new Font("Segoe UI", Font.BOLD, 20));
                ghiChu.setForeground(CHU);
                ghiChu.setBorder(new EmptyBorder(32, 36, 0, 0));
                trang.add(ghiChu, BorderLayout.NORTH);
        }
        noiDung.removeAll();
        noiDung.add(trang, BorderLayout.CENTER);
        noiDung.revalidate();
        noiDung.repaint();
        trangDangMo = ma;
        chonNut();
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

    private BieuTuong.Loai bieuTuong(String ma) {
        switch (ma) {
            case "TAIKHOAN": return BieuTuong.Loai.TAI_KHOAN;
            case "PHANQUYEN": return BieuTuong.Loai.PHAN_QUYEN;
            case "HOTRO": return BieuTuong.Loai.THONG_BAO;
            case "QL_BAOCAO": return BieuTuong.Loai.BAO_CAO;
            case "QL_NHANVIEN": case "QL_BENHNHAN": case "LT_TIEPNHAN":
                return BieuTuong.Loai.TAI_KHOAN;
            case "QL_DICHVU": case "QL_THANHTOAN": case "LT_THANHTOAN":
                return BieuTuong.Loai.HE_THONG;
            case "QL_LICHHEN": case "LT_LICHHEN": case "LT_KHAM":
                return BieuTuong.Loai.KIEM_TRA;
            case "LT_TRACUU": return BieuTuong.Loai.TIM_KIEM;
            default: return BieuTuong.Loai.TRANG_CHU;
        }
    }

    /** Chỉ nút đang mở có nền xanh đậm; nút còn lại trở về màu trắng. */
    private void chonNut() {
        for (Map.Entry<String, JButton> entry : nutMenu.entrySet()) {
            boolean dangChon = entry.getKey().equals(trangDangMo);
            entry.getValue().setBackground(dangChon ? XANH_DAM : Color.WHITE);
            entry.getValue().setForeground(dangChon ? Color.WHITE : CHU_PHU);
            entry.getValue().setIcon(new BieuTuong(bieuTuong(entry.getKey()),
                    dangChon ? Color.WHITE : CHU_PHU, 17));
        }
    }
}
