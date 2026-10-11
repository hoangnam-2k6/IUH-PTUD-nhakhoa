package UI.Chung;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.QuanTriVien.BaoCaoThongKe;
import UI.QuanTriVien.QuanLyPhanQuyen;
import UI.QuanTriVien.QuanLyTaiKhoan;
import UI.QuanTriVien.QuanTriHeThong;
import UI.QuanTriVien.TrangChuQuanTriVien;
import UI.QuanLy.nhanvienquanly_gui;
//import UI.LeTan.;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Khung cửa sổ dùng chung: HEADER + SIDEBAR + khu vực hiển thị trang.
 *
 * Các nút menu được cấp quyền theo SQL Server, không dựa vào ID vai trò giả định.
 * Icon của menu dùng lớp BieuTuong để không phụ thuộc vào font emoji.
 */
public class KhungChinh extends JFrame {

    private final TaiKhoan user;
    private final KhoDuLieu db;
    private final JPanel center = new JPanel(new BorderLayout());
    private final JPanel menu = new JPanel();

    private final String[] pages = {
        "Trang chủ", "Tài khoản", "Phân quyền", "Hệ thống", "Báo cáo - thống kê"
    };

    private final BieuTuong.Loai[] icons = {
        BieuTuong.Loai.TRANG_CHU,
        BieuTuong.Loai.TAI_KHOAN,
        BieuTuong.Loai.PHAN_QUYEN,
        BieuTuong.Loai.HE_THONG,
        BieuTuong.Loai.BAO_CAO
    };

    private final JButton[] buttons = new JButton[pages.length];

    public KhungChinh(TaiKhoan user, KhoDuLieu db) {
        this.user = user;
        this.db = db;

        setTitle("MEDIDENT - " + user.tenVaiTro);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 750);
        setMinimumSize(new Dimension(920, 610));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GiaoDien.BG);
        setContentPane(root);

        // Sidebar cố định 242px, phần chính tự co giãn theo cửa sổ.
        root.add(sidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout());
        root.add(main, BorderLayout.CENTER);
        main.add(header(), BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        apDungMenu();
        hien(0);
    }

    /** Sidebar: logo, danh mục chức năng và thông tin tài khoản. */
    private JPanel sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(242, 0));
        side.setBorder(new MatteBorder(0, 0, 0, 1, GiaoDien.BORDER));

        JPanel brand = new JPanel(new BorderLayout(9, 0));
        brand.setBackground(Color.WHITE);
        brand.setBorder(new EmptyBorder(18, 15, 21, 12));

        // Logo gốc 1024x884, hiển thị khoảng 46x46 và giữ nguyên tỷ lệ.
        JLabel logo = new JLabel(GiaoDien.logo(46, 46));
        brand.add(logo, BorderLayout.WEST);
        JPanel text = new JPanel();
        text.setBackground(Color.WHITE);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel title = GiaoDien.label("MEDIDENT", 16, true);
        JLabel sub = GiaoDien.label("Quản lý nha khoa", 10, false);
        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(sub);
        brand.add(text, BorderLayout.CENTER);
        side.add(brand, BorderLayout.NORTH);

        menu.setBackground(Color.WHITE);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(13, 12, 12, 12));
        JLabel section = GiaoDien.label("QUẢN TRỊ HỆ THỐNG", 11, true);
        section.setForeground(new Color(0x94A3B8));
        menu.add(section);
        menu.add(Box.createVerticalStrut(13));

        for (int i = 0; i < pages.length; i++) {
            final int pageIndex = i;
            JButton button = GiaoDien.nutIcon(pages[i], false, icons[i]);
            button.setMaximumSize(new Dimension(225, 43));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setAlignmentX(0);
            button.setBorder(new EmptyBorder(12, 13, 12, 8));
            button.addActionListener(event -> hien(pageIndex));
            buttons[i] = button;
            menu.add(button);
            menu.add(Box.createVerticalStrut(5));
        }

        JPanel middle = new JPanel(new BorderLayout());
        middle.setBackground(Color.WHITE);
        middle.add(menu, BorderLayout.NORTH);
        side.add(middle, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(10, 14, 19, 14));
        footer.add(GiaoDien.label("Đang đăng nhập: " + user.hoTen, 11, true));
        footer.add(Box.createVerticalStrut(5));
        footer.add(GiaoDien.label(user.tenDangNhap + " · " + user.tenVaiTro, 10, false));
        footer.add(Box.createVerticalStrut(16));

        JButton logout = GiaoDien.nutIcon("Đăng xuất", false, BieuTuong.Loai.DANG_XUAT);
        logout.addActionListener(event -> {
            dispose();
            new UI.DangNhap.DangNhap(db).setVisible(true);
        });
        footer.add(logout);
        side.add(footer, BorderLayout.SOUTH);
        return side;
    }

    /** Header chung, tự hiển thị tên người đăng nhập lấy từ CSDL. */
    private JPanel header() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, GiaoDien.BORDER),
                new EmptyBorder(15, 25, 15, 22)));
        top.add(GiaoDien.label("MEDIDENT   /   Quản lý nha khoa", 13, true), BorderLayout.WEST);
        top.add(GiaoDien.label(user.hoTen + "   ·   " + user.tenVaiTro, 12, false),
                BorderLayout.EAST);
        return top;
    }

    /** Ẩn những mục chưa được cấp quyền trong SQL Server. */
    private void apDungMenu() {
        String[] codes = {"", "TAIKHOAN", "TAIKHOAN", "HETHONG", "BAOCAO"};
        for (int i = 1; i < buttons.length; i++) {
            try {
                buttons[i].setVisible(db.coQuyenTaiKhoan(user.id, codes[i]));
            } catch (Exception exception) {
                buttons[i].setVisible(false);
            }
        }
    }

    /** Mở màn hình tương ứng; mỗi lần mở đều kiểm tra lại quyền ở database. */
    private void hien(int index) {
        try {
            String[] codes = {"", "TAIKHOAN", "TAIKHOAN", "HETHONG", "BAOCAO"};
            if (index != 0 && !db.coQuyenTaiKhoan(user.id, codes[index])) {
                JOptionPane.showMessageDialog(this, "Bạn không có quyền truy cập.");
                return;
            }

            for (int i = 0; i < buttons.length; i++) {
                boolean selected = (i == index);
                buttons[i].setBackground(selected ? new Color(0xE0F2FE) : Color.WHITE);
                buttons[i].setForeground(selected ? GiaoDien.PRIMARY : GiaoDien.MUTED);
                buttons[i].setIcon(new BieuTuong(icons[i],
                        selected ? GiaoDien.PRIMARY : GiaoDien.MUTED, 17));
            }

            JPanel page;
            switch (index) {
                case 1:
                    page = new QuanLyTaiKhoan(db, user);
                    break;
                case 2:
                    page = new QuanLyPhanQuyen(db, user);
                    break;
                case 3:
                    page = new QuanTriHeThong(db, user);
                    break;
                case 4:
                    page = new BaoCaoThongKe(db);
                    break;
                default:
                	// 1. Kiểm tra tài khoản Quản trị viên
                    if (db.laQuanTriVienTaiKhoan(user.id)) {

                        page = new TrangChuQuanTriVien(db);

                    }

                    // 2. Kiểm tra tài khoản Quản lý
                    else if ("Quản lý phòng khám".equalsIgnoreCase(user.tenVaiTro)) {

                        page = new nhanvienquanly_gui(db, user);

                    }

//                    // 3. Kiểm tra tài khoản Lễ tân
//                    else if ("Lễ tân".equalsIgnoreCase(user.tenVaiTro)) {
//
//                        page = new TrangChuLeTan(db, user);
//
//                    }

                    // 4. Các vai trò chưa có Dashboard riêng
                    else {

                        page = new TrangChuTheoVaiTro(user);

                    }
                    break;
            }
            center.removeAll();
            center.add(page, BorderLayout.CENTER);
            center.revalidate();
            center.repaint();
        } catch (Exception exception) {
            GiaoDien.loi(this, exception);
        }
    }
}
