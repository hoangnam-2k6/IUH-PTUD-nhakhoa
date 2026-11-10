
package UI.QuanLy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class QuanLy_GUI extends JFrame implements ActionListener {

    private JButton trangchu, nhanvien, lichlamviec, dichvu;
    private JButton benhnhan, lichhen, hoadon, thongke, dangxuat;
    private JButton nutDangChon;

    private JPanel sidebar, mainbar_center;
    private CardLayout cardLayout;

    private final Color mauNen = new Color(238, 245, 252);
    private final Color mauNut = Color.WHITE;
    private final Color mauChu = new Color(25, 25, 25);
    private final Color mauChon = new Color(218, 236, 249);
    private final Color mauVien = new Color(205, 215, 225);

    private QuanLyNhanVienPanel quanLyNhanVienPanel;
    private QuanLyBenhNhanPanel quanLyBenhNhanPanel;

    private QuanLyPanel nhanVienManager;
    private QuanLyPanel benhNhanManager;
    private QuanLyPanel dichVuManager;
    private QuanLyPanel lichHenManager;
    private QuanLyPanel hoaDonManager;
    private QuanLyPanel managerDangMo;

    public QuanLy_GUI() {
        setTitle("MediDent - Quản lý phòng khám");
        setSize(1350, 850);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        taoSidebar();
        taoMainbar();
        taoCacManHinh();
        ganSuKien();

        doiMauNut(trangchu);
        cardLayout.show(mainbar_center, "trangchu");
    }

    // Tạo sidebar bên trái.
    private void taoSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(mauNen);

        JPanel sidebarTop = new JPanel(new GridBagLayout());
        sidebarTop.setBackground(mauNen);
        sidebarTop.setPreferredSize(new Dimension(240, 78));

        JLabel tenApp = new JLabel("MEDIDENT");
        tenApp.setFont(new Font("Arial", Font.BOLD, 23));
        tenApp.setForeground(new Color(35, 55, 76));
        sidebarTop.add(tenApp);

        // Tạo các nút điều hướng.
        trangchu = taoNutMenu("Trang chủ");
        nhanvien = taoNutMenu("Nhân viên");
        lichlamviec = taoNutMenu("Lịch làm việc");
        dichvu = taoNutMenu("Dịch vụ nha khoa");
        benhnhan = taoNutMenu("Bệnh nhân");
        lichhen = taoNutMenu("Lịch hẹn");
        hoadon = taoNutMenu("Hóa đơn");
        thongke = taoNutMenu("Thống kê");
        dangxuat = taoNutMenu("Đăng xuất");

        // Đưa các nút vào Box theo chiều dọc.
        Box danhSachNut = Box.createVerticalBox();
        danhSachNut.setOpaque(true);
        danhSachNut.setBackground(mauNen);
        danhSachNut.setBorder(BorderFactory.createEmptyBorder(4, 14, 5, 14));

        danhSachNut.add(trangchu);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(nhanvien);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(lichlamviec);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(dichvu);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(benhnhan);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(lichhen);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(hoadon);
        danhSachNut.add(Box.createVerticalStrut(7));
        danhSachNut.add(thongke);

        // Nút đăng xuất ở dưới cùng.
        JPanel sidebarBottom = new JPanel(new BorderLayout());
        sidebarBottom.setBackground(mauNen);
        sidebarBottom.setPreferredSize(new Dimension(240, 65));
        sidebarBottom.setBorder(BorderFactory.createEmptyBorder(7, 14, 10, 14));
        sidebarBottom.add(dangxuat, BorderLayout.CENTER);

        // Cuộn sidebar bằng chuột nhưng ẩn thanh cuộn.
        JScrollPane cuonMenu = new JScrollPane(danhSachNut,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        cuonMenu.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        cuonMenu.getVerticalScrollBar().setUnitIncrement(16);
        cuonMenu.setBorder(null);

        sidebar.add(sidebarTop, BorderLayout.NORTH);
        sidebar.add(cuonMenu, BorderLayout.CENTER);
        sidebar.add(sidebarBottom, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    // Tạo nút menu thống nhất kích thước và màu sắc.
    private JButton taoNutMenu(String tenNut) {
        JButton nut = new JButton(tenNut);
        nut.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        nut.setFont(new Font("Arial", Font.PLAIN, 13));
        nut.setForeground(mauChu);
        nut.setBackground(mauNut);
        nut.setPreferredSize(new Dimension(212, 42));
        nut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nut.setAlignmentX(Component.CENTER_ALIGNMENT);
        nut.setFocusPainted(false);
        nut.setOpaque(true);
        nut.setContentAreaFilled(true);
        nut.setBorder(BorderFactory.createLineBorder(mauVien));
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return nut;
    }

    // Tạo header và khu vực hiển thị nội dung.
    private void taoMainbar() {
        JPanel mainbar = new JPanel(new BorderLayout());
        mainbar.setBackground(Color.WHITE);

        JPanel mainbarTop = new JPanel(new BorderLayout());
        mainbarTop.setBackground(Color.WHITE);
        mainbarTop.setPreferredSize(new Dimension(0, 90));
        mainbarTop.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, mauVien),
                BorderFactory.createEmptyBorder(15, 20, 15, 25)));

        JLabel tenApp = new JLabel("MEDIDENT / Quản lý nha khoa");
        tenApp.setFont(new Font("Arial", Font.BOLD, 21));
        tenApp.setForeground(new Color(35, 110, 165));

        JPanel thongTin = new JPanel(new GridLayout(2, 1));
        thongTin.setBackground(Color.WHITE);

        JLabel hoTen = new JLabel("Trịnh Hoàng Nam", SwingConstants.RIGHT);
        JLabel vaiTro = new JLabel("Quản lý phòng khám", SwingConstants.RIGHT);

        hoTen.setFont(new Font("Arial", Font.BOLD, 16));
        vaiTro.setFont(new Font("Arial", Font.PLAIN, 14));
        hoTen.setForeground(new Color(40, 40, 40));
        vaiTro.setForeground(new Color(90, 90, 90));

        thongTin.add(hoTen);
        thongTin.add(vaiTro);

        mainbarTop.add(tenApp, BorderLayout.WEST);
        mainbarTop.add(thongTin, BorderLayout.EAST);

        // CardLayout quản lý các trang.
        cardLayout = new CardLayout();
        mainbar_center = new JPanel(cardLayout);
        mainbar_center.setBackground(Color.WHITE);

        mainbar.add(mainbarTop, BorderLayout.NORTH);
        mainbar.add(mainbar_center, BorderLayout.CENTER);
        add(mainbar, BorderLayout.CENTER);
    }

    // Đăng ký tất cả màn hình.
    private void taoCacManHinh() {
        // Trang chủ.
        mainbar_center.add(new TrangChuQuanLyPanel(), "trangchu");

        // Quản lý nhân viên.
        quanLyNhanVienPanel = new QuanLyNhanVienPanel(
                this::moManHinhThemNhanVien, this::moManHinhSuaNhanVien);
        nhanVienManager = themCard("nhanvien", quanLyNhanVienPanel, this::taiLaiNhanVien);

        // Lịch làm việc.
        mainbar_center.add(new QuanLyLichLamViecPanel(), "lichlamviec");

        // Dịch vụ nha khoa.
        QuanLyDichVuPanel quanLyDichVuPanel = new QuanLyDichVuPanel(
                this::moManHinhThemDichVu, this::moManHinhSuaDichVu);
        dichVuManager = themCard("dichvu", quanLyDichVuPanel,
                () -> taiLaiDichVu(quanLyDichVuPanel));

        // Quản lý bệnh nhân.
        quanLyBenhNhanPanel = new QuanLyBenhNhanPanel(
                this::moManHinhThemBenhNhan, this::moManHinhSuaBenhNhan);
        benhNhanManager = themCard("benhnhan", quanLyBenhNhanPanel, this::taiLaiBenhNhan);

        // Quản lý lịch hẹn.
        lichHenManager = themCard("lichhen", new QuanLyLichHenPanel(), null);

        // Quản lý hóa đơn.
        hoaDonManager = themCard("hoadon", new QuanLyHoaDonPanel(), null);

        // Thống kê.
        mainbar_center.add(new BaoCaoThongKePanel(), "thongke");

        // Đồng bộ giao diện các Panel.
        for (Component thanhPhan : mainbar_center.getComponents()) {
            GiaoDienChung.dongBo(thanhPhan);
        }
    }

    // Đưa Panel vào vùng CardLayout.
    private QuanLyPanel themCard(String tenCard, JPanel panelDanhSach, Runnable taiLai) {
        JPanel khung = new JPanel(new BorderLayout());
        QuanLyPanel manager = new QuanLyPanel(khung, panelDanhSach, taiLai);
        mainbar_center.add(khung, tenCard);
        return manager;
    }

    // Tải lại dữ liệu nhân viên.
    private void taiLaiNhanVien() {
        try {
            quanLyNhanVienPanel.loadnhanvien();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tải lại dữ liệu bệnh nhân.
    private void taiLaiBenhNhan() {
        try {
            quanLyBenhNhanPanel.loadbenhnhan();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tải lại dữ liệu dịch vụ.
    private void taiLaiDichVu(QuanLyDichVuPanel panel) {
        try {
            panel.loaddichvu();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Gắn sự kiện cho sidebar.
    private void ganSuKien() {
        trangchu.addActionListener(this);
        nhanvien.addActionListener(this);
        lichlamviec.addActionListener(this);
        dichvu.addActionListener(this);
        benhnhan.addActionListener(this);
        lichhen.addActionListener(this);
        hoadon.addActionListener(this);
        thongke.addActionListener(this);
        dangxuat.addActionListener(this);
    }

    // Đổi màu nút đang được chọn.
    private void doiMauNut(JButton nutMoi) {
        if (nutDangChon != null) {
            nutDangChon.setBackground(mauNut);
            nutDangChon.setForeground(mauChu);
        }

        nutMoi.setBackground(mauChon);
        nutMoi.setForeground(new Color(28, 105, 150));
        nutDangChon = nutMoi;
    }

    // Chuyển màn hình bằng CardLayout.
    private void chuyenTrang(String tenCard, JButton nutMoi, QuanLyPanel managerMoi) {
        if (managerDangMo != null && managerDangMo != managerMoi) {
            managerDangMo.reset();
        }

        cardLayout.show(mainbar_center, tenCard);
        managerDangMo = managerMoi;
        doiMauNut(nutMoi);
    }

    // Mở form thêm nhân viên.
    private void moManHinhThemNhanVien() {
        nhanVienManager.show(new ThemNhanVienPanel(nhanVienManager::showList));
    }

    // Mở form sửa nhân viên.
    private void moManHinhSuaNhanVien(String maNV) {
        nhanVienManager.show(new SuaNhanVienPanel(maNV, nhanVienManager::showList));
    }

    // Mở form thêm bệnh nhân.
    private void moManHinhThemBenhNhan() {
        benhNhanManager.show(new ThemBenhNhanPanel(benhNhanManager::showList));
    }

    // Mở form sửa bệnh nhân.
    private void moManHinhSuaBenhNhan(String maBN) {
        benhNhanManager.show(new SuaBenhNhanPanel(maBN, benhNhanManager::showList));
    }

    // Mở form thêm dịch vụ.
    private void moManHinhThemDichVu() {
        dichVuManager.show(new ThemDichVuPanel(dichVuManager::showList));
    }

    // Mở form sửa dịch vụ.
    private void moManHinhSuaDichVu(String maDV) {
        dichVuManager.show(new SuaDichVuPanel(maDV, dichVuManager::showList));
    }

    // Thông báo lỗi.
    private void baoLoi(Exception loi) {
        GiaoDienChung.baoLoi(this, loi);
    }

    // Xử lý sự kiện khi nhấn sidebar.
    @Override
    public void actionPerformed(ActionEvent e) {
        Object nut = e.getSource();

        if (nut == trangchu) {
            chuyenTrang("trangchu", trangchu, null);
        } else if (nut == nhanvien) {
            chuyenTrang("nhanvien", nhanvien, nhanVienManager);
        } else if (nut == lichlamviec) {
            chuyenTrang("lichlamviec", lichlamviec, null);
        } else if (nut == dichvu) {
            chuyenTrang("dichvu", dichvu, dichVuManager);
        } else if (nut == benhnhan) {
            chuyenTrang("benhnhan", benhnhan, benhNhanManager);
        } else if (nut == lichhen) {
            chuyenTrang("lichhen", lichhen, lichHenManager);
        } else if (nut == hoadon) {
            chuyenTrang("hoadon", hoadon, hoaDonManager);
        } else if (nut == thongke) {
            chuyenTrang("thongke", thongke, null);
        } else if (nut == dangxuat) {
            dangXuat();
        }
    }

    // Xác nhận đăng xuất.
    private void dangXuat() {
        int luaChon = JOptionPane.showConfirmDialog(this,
                "Bạn muốn đăng xuất khỏi MediDent?",
                "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION);

        if (luaChon == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    // Chạy ứng dụng, không tự đồng bộ SQL.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLy_GUI().setVisible(true));
    }
}
