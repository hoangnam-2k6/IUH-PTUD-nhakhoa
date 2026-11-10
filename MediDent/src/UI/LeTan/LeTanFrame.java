package UI.LeTan;

import Entity.TaiKhoan;
import UI.DangNhap.DangNhapFrame;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static UI.LeTan.GiaoDien.*;

public class LeTanFrame extends JFrame {
    // Bảng màu sidebar theo thiết kế: nền xanh rất nhạt, thẻ menu trắng bo góc.
    private static final Color NEN_SIDEBAR   = Color.decode("#EFF7FA");
    private static final Color THE_TRANG     = Color.decode("#FFFFFF");
    private static final Color THE_HOVER     = Color.decode("#F7FCFF");
    private static final Color THE_DANG_CHON = Color.decode("#E0F2FE");
    private static final Color CHU_MEN       = Color.decode("#0F172A");
    private static final Color CHU_MEN_CHON  = Color.decode("#0369A1");
    private static final Color VIEN_THE      = Color.decode("#E2E8F0");
    private static final int BO_GOC_THE = 14;
    private static final int CAO_THE    = 58;
    private static final int KHOANG_CACH_THE = 18;

    private final TaiKhoan taiKhoan;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final String[] tenMenu = {
            "Trang Chủ", "Bệnh Nhân", "Lịch Hẹn", "Khám Bệnh", "Thanh Toán & Hóa Đơn"
    };
    private final TheMenu[] nutMenu = new TheMenu[tenMenu.length];
    private final JLabel duongDan = new JLabel();

    private final TrangChuLeTan trangChu;
    private final TraCuuThongTin benhNhan;
    private final QuanLyLichHenLeTan lichHen;
    private final KhamBenhLeTan khamBenh;
    private final ThanhToanHoaDonLeTan thanhToan;

    public LeTanFrame(TaiKhoan taiKhoan) {
        if (taiKhoan == null) {
            throw new IllegalArgumentException("Cần đăng nhập trước khi mở màn hình Lễ tân.");
        }
        this.taiKhoan = taiKhoan;
        this.trangChu = new TrangChuLeTan(taiKhoan);
        this.benhNhan = new TraCuuThongTin(taiKhoan);
        this.lichHen = new QuanLyLichHenLeTan(taiKhoan);
        this.khamBenh = new KhamBenhLeTan(taiKhoan);
        this.thanhToan = new ThanhToanHoaDonLeTan(taiKhoan);

        setTitle("MediDent - Lễ tân (" + taiKhoan.getHoTen() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_APP);

        content.setBackground(NEN_APP);
        content.add(trangChu, tenMenu[0]);
        content.add(benhNhan, tenMenu[1]);
        content.add(lichHen, tenMenu[2]);
        content.add(khamBenh, tenMenu[3]);
        content.add(thanhToan, tenMenu[4]);

        add(taoHeader(), BorderLayout.NORTH);
        add(taoSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        chonMenu(0);
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    private JPanel taoHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NEN_THE);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, VIEN),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)));

        duongDan.setFont(font(Font.BOLD, 15));
        duongDan.setForeground(CHU_CHINH);

        JLabel ten = new JLabel(taiKhoan.getHoTen(), SwingConstants.RIGHT);
        ten.setFont(font(Font.BOLD, 13));
        ten.setForeground(CHU_CHINH);
        JLabel vaiTro = new JLabel(taiKhoan.getVaiTro() + " · " + taiKhoan.getTenDangNhap(), SwingConstants.RIGHT);
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
        sidebar.setBackground(NEN_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(0, 22, 24, 22));

        JLabel logo = new JLabel("MEDIDENT");
        logo.setFont(font(Font.BOLD, 22));
        logo.setForeground(Color.decode("#0F1B27"));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        logo.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        sidebar.add(logo);

        for (int i = 0; i < tenMenu.length; i++) {
            final int vitri = i;
            TheMenu nut = new TheMenu(tenMenu[i]);
            nut.setAlignmentX(Component.CENTER_ALIGNMENT);
            nut.addActionListener(e -> chonMenu(vitri));
            nutMenu[i] = nut;
            sidebar.add(nut);
            sidebar.add(Box.createVerticalStrut(KHOANG_CACH_THE));
        }

        sidebar.add(Box.createVerticalGlue());

        TheMenu nutDangXuat = new TheMenu("Đăng Xuất");
        nutDangXuat.setAlignmentX(Component.CENTER_ALIGNMENT);
        nutDangXuat.addActionListener(e -> dangXuat());
        sidebar.add(nutDangXuat);
        return sidebar;
    }

    private void chonMenu(int vitri) {
        for (int i = 0; i < nutMenu.length; i++) {
            nutMenu[i].setDangChon(i == vitri);
        }
        duongDan.setText("MEDIDENT   /   " + tenMenu[vitri]);
        cardLayout.show(content, tenMenu[vitri]);
        switch (vitri) {
            case 0 -> trangChu.taiDuLieu();
            case 1 -> benhNhan.taiDuLieu();
            case 2 -> lichHen.taiDuLieu();
            case 3 -> khamBenh.taiDuLieu();
            default -> thanhToan.taiDuLieu();
        }
    }

    private void dangXuat() {
        if (!xacNhan(this, "Bạn có chắc muốn đăng xuất?", "Đăng xuất")) return;
        dispose();
        SwingUtilities.invokeLater(() -> new DangNhapFrame().setVisible(true));
    }

    /**
     * Thẻ menu bo góc trắng nổi trên nền xanh nhạt, chữ căn giữa.
     * Tự vẽ bóng đổ nhẹ và trạng thái rê chuột / đang chọn.
     */
    private static final class TheMenu extends JButton {
        private boolean dangChon;
        private boolean dangRe;

        private TheMenu(String chu) {
            super(chu);
            setFont(font(Font.BOLD, 15));
            setForeground(CHU_MEN);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, CAO_THE));
            setPreferredSize(new Dimension(276, CAO_THE));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    dangRe = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dangRe = false;
                    repaint();
                }
            });
        }

        private void setDangChon(boolean chon) {
            dangChon = chon;
            setForeground(chon ? CHU_MEN_CHON : CHU_MEN);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int rong = getWidth();
            int cao = getHeight();
            int le = 1;
            int rongThe = rong - le * 2;
            int caoThe = cao - le - 3;

            // Bóng đổ nhẹ phía dưới thẻ.
            g2.setColor(new Color(15, 23, 42, 20));
            g2.fillRoundRect(le + 1, le + 3, rongThe - 2, caoThe, BO_GOC_THE, BO_GOC_THE);

            // Nền thẻ.
            g2.setColor(dangChon ? THE_DANG_CHON : (dangRe ? THE_HOVER : THE_TRANG));
            g2.fillRoundRect(le, le, rongThe, caoThe, BO_GOC_THE, BO_GOC_THE);

            // Viền rất nhạt giúp thẻ tách khỏi nền.
            g2.setColor(dangChon ? Color.decode("#BAE6FD") : VIEN_THE);
            g2.drawRoundRect(le, le, rongThe, caoThe, BO_GOC_THE, BO_GOC_THE);

            // Vạch nhấn bên trái cho mục đang chọn.
            if (dangChon) {
                g2.setColor(MAU_CHINH);
                g2.fillRoundRect(12, (cao - 24) / 2, 4, 24, 4, 4);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhapFrame().setVisible(true));
    }
}
