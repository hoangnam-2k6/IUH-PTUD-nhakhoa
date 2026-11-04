package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import Dao.NghiepVu.BaoCaoService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.GiaoDien;
import UI.DangNhap.DangNhap;

/**
 * Khung chính tích hợp đúng 7 màn hình nghiệp vụ của quản lý phòng khám.
 * Không chuyển sang JFrame khác khi bấm menu; chỉ thay panel ở vùng nội dung.
 */
public final class KhungChinh extends JFrame {

    private final java.awt.CardLayout layout = new java.awt.CardLayout();
    private final JPanel screens = new JPanel(layout);
    private final Map<String, JButton> nav = new LinkedHashMap<>();
    private final Map<String, JPanel> opened = new LinkedHashMap<>();

    private final JLabel breadcrumb = GiaoDien.label("MEDIDENT / Quản lý phòng khám", 14, true);
    

    private TrangChuPanel home;

    public KhungChinh(PhanQuyen.PhienDangNhap session) {
        super("MEDIDENT – Quản lý phòng khám");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1480, 850);
        setMinimumSize(new Dimension(1180, 700));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(session), BorderLayout.WEST);
        add(buildRight(session), BorderLayout.CENTER);

        showScreen("Trang chủ Quản lý");
    }

    private JPanel buildSidebar(PhanQuyen.PhienDangNhap session) {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(GiaoDien.NAV_BG);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(310, 0));
        sidebar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, GiaoDien.NAV_DIVIDER),
                new EmptyBorder(26, 16, 16, 16)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(new JLabel(GiaoDien.logo(46)));
        brand.add(GiaoDien.label("MEDIDENT", 22, true));

        JLabel caption = GiaoDien.label("QUẢN LÝ PHÒNG KHÁM", 11, true);
        caption.setAlignmentX(LEFT_ALIGNMENT);

        JLabel section = GiaoDien.label("CHỨC NĂNG HỆ THỐNG", 12, true);
        section.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(caption);
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(section);
        sidebar.add(Box.createVerticalStrut(14));

        String[] names = {
                "Trang chủ Quản lý",
                "Quản lý nhân viên",
                "Dịch vụ nha khoa",
                "Quản lý bệnh nhân",
                "Quản lý lịch hẹn",
                "Thanh toán & hóa đơn",
                "Báo cáo - thống kê"
        };

        String[] icons = { "home", "staff", "tooth", "patient", "calendar", "bill", "chart" };

        home = new TrangChuPanel(session);
        screens.add(home, names[0]);
        screens.add(new NhanVienPanel(session), names[1]);
        screens.add(new DichVuPanel(session), names[2]);
        screens.add(new BenhNhanPanel(session), names[3]);
        screens.add(new LichHenPanel(session), names[4]);
        screens.add(new ThanhToanPanel(session), names[5]);
        screens.add(new BaoCaoPanel(session), names[6]);

        for (int i = 0; i < names.length; i++) {
            opened.put(names[i], (JPanel) screens.getComponent(i));
            JButton item = menuButton(names[i], icons[i]);
            String screenName = names[i];
            item.addActionListener(e -> showScreen(screenName));
            sidebar.add(item);
            sidebar.add(Box.createVerticalStrut(8));
            nav.put(screenName, item);
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel userLabel = GiaoDien.label(session.fullName() + " • Quản lý", 12, true);
        userLabel.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(14));

        JButton logout = GiaoDien.button("Đăng xuất", false);
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            dispose();
            new DangNhap().setVisible(true);
        });
        sidebar.add(logout);

        return sidebar;
    }

    private JButton menuButton(String text, String icon) {
        JButton button = GiaoDien.menuButton("  " + text, GiaoDien.icon(icon, 20));
        button.setAlignmentX(LEFT_ALIGNMENT);
        return button;
    }

    private JPanel buildRight(PhanQuyen.PhienDangNhap session) {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(GiaoDien.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GiaoDien.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, GiaoDien.BORDER),
                new EmptyBorder(20, 26, 20, 26)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(breadcrumb);

        header.add(left, BorderLayout.WEST);
        header.add(GiaoDien.label(session.username() + " • SQL MediDent", 12, false), BorderLayout.EAST);

        screens.setBackground(GiaoDien.BG);
        right.add(header, BorderLayout.NORTH);
        right.add(screens, BorderLayout.CENTER);
        return right;
    }

    private void showScreen(String name) {
        JPanel selected = opened.get(name);

        if (selected instanceof BangDuLieuPanel crud) {
            crud.refresh();
        } else if (selected instanceof BaoCaoPanel report) {
            report.refresh();
        } else if (selected instanceof TrangChuPanel homePanel) {
            homePanel.refresh();
        }

        breadcrumb.setText("MEDIDENT  /  Quản lý phòng khám  /  " + name);
        layout.show(screens, name);

        nav.forEach((key, button) -> {
            boolean active = key.equals(name);
            button.setBackground(active ? GiaoDien.NAV_ACTIVE : GiaoDien.NAV_ITEM);
            button.setForeground(active ? GiaoDien.BLUE_DARK : GiaoDien.MUTED);

        });
    }
}
