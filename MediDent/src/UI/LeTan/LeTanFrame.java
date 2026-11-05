package UI.LeTan;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static UI.LeTan.GiaoDien.*;

public class LeTanFrame extends JFrame {
    // Sau này có màn hình đăng nhập thì lấy tên từ người đăng nhập
    private static final String TEN_NGUOI_DUNG = "Lê Thu Hương";
    private static final String VAI_TRO = "Lễ tân";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final String[] tenMenu = {"Trang chủ", "Bệnh nhân", "Lịch hẹn", "Thanh toán & hóa đơn"};
    private final JButton[] nutMenu = new JButton[tenMenu.length];
    private final JLabel duongDan = new JLabel();
    private int menuDangChon = 0;

    private final TrangChuLeTan trangChu = new TrangChuLeTan(TEN_NGUOI_DUNG);
    private final TraCuuThongTin benhNhan = new TraCuuThongTin();
    private final QuanLyLichHenLeTan lichHen = new QuanLyLichHenLeTan();
    private final ThanhToanHoaDonLeTan thanhToan = new ThanhToanHoaDonLeTan();

    public LeTanFrame() {
        setTitle("MediDent - Lễ tân");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // mở toàn màn hình; thu nhỏ lại thì về 1280x760
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_APP);

        content.setBackground(NEN_APP);
        content.add(trangChu, tenMenu[0]);
        content.add(benhNhan, tenMenu[1]);
        content.add(lichHen, tenMenu[2]);
        content.add(thanhToan, tenMenu[3]);

        add(taoHeader(), BorderLayout.NORTH);
        add(taoSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        chonMenu(0);
    }

    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NEN_THE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, VIEN),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)));

        duongDan.setFont(font(Font.BOLD, 15));
        duongDan.setForeground(CHU_CHINH);

        JLabel ten = new JLabel(TEN_NGUOI_DUNG, SwingConstants.RIGHT);
        ten.setFont(font(Font.BOLD, 13));
        ten.setForeground(CHU_CHINH);
        JLabel vaiTro = new JLabel(VAI_TRO, SwingConstants.RIGHT);
        vaiTro.setFont(font(Font.PLAIN, 12));
        vaiTro.setForeground(CHU_PHU);

        JPanel nguoiDung = new JPanel(new GridLayout(2, 1));
        nguoiDung.setOpaque(false);
        nguoiDung.add(ten);
        nguoiDung.add(vaiTro);

        header.add(duongDan, BorderLayout.WEST);
        header.add(nguoiDung, BorderLayout.EAST);
        return header;
    }

    private JPanel taoSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NEN_THE);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, VIEN),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));

        // Logo
        JLabel logo = new JLabel("MEDIDENT");
        logo.setFont(font(Font.BOLD, 20));
        logo.setForeground(MAU_CHINH);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel moTa = new JLabel("Quản lý nha khoa");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(moTa);
        sidebar.add(Box.createVerticalStrut(24));

        // Menu
        JLabel nhom = new JLabel("CHỨC NĂNG HỆ THỐNG");
        nhom.setFont(font(Font.BOLD, 11));
        nhom.setForeground(CHU_PHU);
        nhom.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(nhom);
        sidebar.add(Box.createVerticalStrut(8));

        for (int i = 0; i < tenMenu.length; i++) {
            final int vitri = i;
            JButton nut = new JButton(tenMenu[i]);
            nut.setHorizontalAlignment(SwingConstants.LEFT);
            nut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            nut.setAlignmentX(Component.LEFT_ALIGNMENT);
            nut.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            nut.setFocusPainted(false);
            nut.setContentAreaFilled(false);
            nut.setOpaque(true);
            nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nut.addActionListener(e -> chonMenu(vitri));
            nut.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (vitri != menuDangChon) nut.setBackground(NEN_APP);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (vitri != menuDangChon) nut.setBackground(NEN_THE);
                }
            });
            nutMenu[i] = nut;
            sidebar.add(nut);
            sidebar.add(Box.createVerticalStrut(4));
        }

        sidebar.add(Box.createVerticalGlue());

        // Người dùng + đăng xuất
        JLabel dangNhap = new JLabel("Đăng nhập: " + TEN_NGUOI_DUNG);
        dangNhap.setFont(font(Font.BOLD, 12));
        dangNhap.setForeground(CHU_CHINH);
        dangNhap.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel vaiTro = new JLabel(VAI_TRO);
        vaiTro.setFont(font(Font.PLAIN, 12));
        vaiTro.setForeground(CHU_PHU);
        vaiTro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton nutDangXuat = taoNutTrang("Đăng xuất");
        nutDangXuat.setAlignmentX(Component.LEFT_ALIGNMENT);
        nutDangXuat.addActionListener(e -> dangXuat());

        sidebar.add(dangNhap);
        sidebar.add(vaiTro);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(nutDangXuat);
        return sidebar;
    }

    private void chonMenu(int vitri) {
        menuDangChon = vitri;
        for (int i = 0; i < nutMenu.length; i++) {
            boolean dangChon = (i == vitri);
            nutMenu[i].setBackground(dangChon ? MAU_CHINH_NHAT : NEN_THE);
            nutMenu[i].setForeground(dangChon ? MAU_CHINH : CHU_PHU);
            nutMenu[i].setFont(font(dangChon ? Font.BOLD : Font.PLAIN, 14));
        }
        duongDan.setText("MEDIDENT   /   " + tenMenu[vitri]);
        cardLayout.show(content, tenMenu[vitri]);
        switch (vitri) {
            case 0: trangChu.taiDuLieu(); break;
            case 1: benhNhan.taiDuLieu(); break;
            case 2: lichHen.taiDuLieu(); break;
            default: thanhToan.taiDuLieu(); break;
        }
    }

    private void dangXuat() {
        int chon = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?",
                "Đăng xuất", JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION) {
            // Chưa có màn hình đăng nhập nên tạm thời thoát chương trình
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LeTanFrame().setVisible(true));
    }
}
