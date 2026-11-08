
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
    private final Color mauChon = new Color(25, 95, 120);
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

    // Tao sidebar ben trai
   
    private void taoSidebar() {
	    sidebar = new JPanel(new BorderLayout());
	    sidebar.setPreferredSize(new Dimension(240, 0));
	    sidebar.setBackground(mauNen);
	
	    // Phan logo
	    JPanel sidebar_top = new JPanel(new GridBagLayout());
	    sidebar_top.setBackground(mauNen);
	    sidebar_top.setPreferredSize(new Dimension(240, 85));
	
	    JLabel tenapp = new JLabel("MEDIDENT");
	    tenapp.setFont(new Font("Arial", Font.BOLD, 23));
	    tenapp.setForeground(new Color(35, 35, 35));
	    sidebar_top.add(tenapp);
	
	    // Tao cac nut
	    trangchu = taoNutMenu("Trang chủ");
	    nhanvien = taoNutMenu("Nhân viên");
	    lichlamviec = taoNutMenu("Lịch làm việc");
	    dichvu = taoNutMenu("Dịch vụ nha khoa");
	    benhnhan = taoNutMenu("Bệnh nhân");
	    lichhen = taoNutMenu("Lịch hẹn");
	    hoadon = taoNutMenu("Hóa đơn");
	    thongke = taoNutMenu("Thống kê");
	    dangxuat = taoNutMenu("Đăng xuất");
	
	    // Sap xep menu
	    Box danhSachNut = Box.createVerticalBox();
	    danhSachNut.setOpaque(true);
	    danhSachNut.setBackground(mauNen);
	    danhSachNut.setBorder(BorderFactory.createEmptyBorder(4, 14, 5, 14));
	
	    danhSachNut.add(trangchu);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(nhanvien);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(lichlamviec);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(dichvu);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(benhnhan);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(lichhen);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(hoadon);
	    danhSachNut.add(Box.createVerticalStrut(9));
	    danhSachNut.add(thongke);
	
	    // Nut dang xuat
	    JPanel sidebar_bottom = new JPanel(new BorderLayout());
	    sidebar_bottom.setBackground(mauNen);
	    sidebar_bottom.setPreferredSize(new Dimension(240, 70));
	    sidebar_bottom.setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
	    sidebar_bottom.add(dangxuat, BorderLayout.CENTER);
	
	    sidebar.add(sidebar_top, BorderLayout.NORTH);
	    sidebar.add(danhSachNut, BorderLayout.CENTER);
	    sidebar.add(sidebar_bottom, BorderLayout.SOUTH);
	
	    add(sidebar, BorderLayout.WEST);
}


    // Tao kieu nut menu

    private JButton taoNutMenu(String tenNut) {
	    JButton nut = new JButton(tenNut);
	
	    nut.setFont(new Font("Arial", Font.BOLD, 15));
	    nut.setForeground(mauChu);
	    nut.setBackground(Color.WHITE);
	
	    nut.setPreferredSize(new Dimension(212, 48));
	    nut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
	
	    nut.setAlignmentX(Component.CENTER_ALIGNMENT);
	    nut.setFocusPainted(false);
	    nut.setOpaque(true);
	    nut.setContentAreaFilled(true);
	    nut.setBorder(BorderFactory.createLineBorder(mauVien, 1, true));
	    nut.setCursor(new Cursor(Cursor.HAND_CURSOR));

    return nut;
    }


    // Tao phan noi dung ben phai
    private void taoMainbar() {
        JPanel mainbar = new JPanel(new BorderLayout());
        mainbar.setBackground(Color.WHITE);

        // Thanh thong tin phia tren
        JPanel mainbar_top = new JPanel(new BorderLayout());
        mainbar_top.setBackground(Color.WHITE);
        mainbar_top.setPreferredSize(new Dimension(0, 90));
        mainbar_top.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, mauVien),
            BorderFactory.createEmptyBorder(15, 20, 15, 25)
        ));

        JLabel tenapp2 = new JLabel("MEDIDENT / Quản lý nha khoa");
        tenapp2.setFont(new Font("Arial", Font.BOLD, 21));
        tenapp2.setForeground(new Color(35, 110, 165));

        JPanel mainbar_top_right = new JPanel(new GridLayout(2, 1));
        mainbar_top_right.setBackground(Color.WHITE);

        JLabel hoten = new JLabel("Trịnh Hoàng Nam", SwingConstants.RIGHT);
        JLabel vaitro = new JLabel("Quản lý phòng khám", SwingConstants.RIGHT);

        hoten.setFont(new Font("Arial", Font.BOLD, 16));
        vaitro.setFont(new Font("Arial", Font.PLAIN, 14));
        hoten.setForeground(new Color(40, 40, 40));
        vaitro.setForeground(new Color(90, 90, 90));

        mainbar_top_right.add(hoten);
        mainbar_top_right.add(vaitro);

        mainbar_top.add(tenapp2, BorderLayout.WEST);
        mainbar_top.add(mainbar_top_right, BorderLayout.EAST);

        // Vung chuyen doi cac man hinh
        cardLayout = new CardLayout();
        mainbar_center = new JPanel(cardLayout);
        mainbar_center.setBackground(Color.WHITE);

        mainbar.add(mainbar_top, BorderLayout.NORTH);
        mainbar.add(mainbar_center, BorderLayout.CENTER);

        add(mainbar, BorderLayout.CENTER);
    }

    // Dang ky cac giao dien chuc nang
    private void taoCacManHinh() {

        // Trang chu
        mainbar_center.add(new TrangChuQuanLyPanel(), "trangchu");

        // Nhan vien
        quanLyNhanVienPanel = new QuanLyNhanVienPanel(
            this::moManHinhThemNhanVien, this::moManHinhSuaNhanVien
        );
        nhanVienManager = themCard("nhanvien", quanLyNhanVienPanel, this::taiLaiNhanVien);

        // Lich lam viec
        JPanel manHinhLichLamViec = new JPanel(new GridBagLayout());
        manHinhLichLamViec.setBackground(Color.WHITE);

        JLabel thongBaoLich = new JLabel("Chức năng Lịch làm việc đang được xây dựng");
        thongBaoLich.setFont(new Font("Arial", Font.PLAIN, 18));
        manHinhLichLamViec.add(thongBaoLich);

        mainbar_center.add(manHinhLichLamViec, "lichlamviec");

        // Dich vu nha khoa
        QuanLyDichVuPanel quanLyDichVuPanel = new QuanLyDichVuPanel(
            this::moManHinhThemDichVu, this::moManHinhSuaDichVu
        );
        dichVuManager = themCard("dichvu", quanLyDichVuPanel, () -> taiLaiDichVu(quanLyDichVuPanel));

        // Benh nhan
        quanLyBenhNhanPanel = new QuanLyBenhNhanPanel(
            this::moManHinhThemBenhNhan, this::moManHinhSuaBenhNhan
        );
        benhNhanManager = themCard("benhnhan", quanLyBenhNhanPanel, this::taiLaiBenhNhan);

        // Lich hen
        lichHenManager = themCard("lichhen", new QuanLyLichHenPanel(), null);

        // Hoa don
        hoaDonManager = themCard("hoadon", new QuanLyHoaDonPanel(), null);

        // Thong ke
        mainbar_center.add(new BaoCaoThongKePanel(), "thongke");
    }

    // Them JPanel vao CardLayout thong qua QuanLyPanel
    private QuanLyPanel themCard(String tenCard, JPanel panelDanhSach, Runnable taiLai) {
        JPanel khung = new JPanel(new BorderLayout());
        QuanLyPanel manager = new QuanLyPanel(khung, panelDanhSach, taiLai);
        mainbar_center.add(khung, tenCard);
        return manager;
    }

    // Tai lai nhan vien
    private void taiLaiNhanVien() {
        try {
            quanLyNhanVienPanel.loadnhanvien();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tai lai benh nhan
    private void taiLaiBenhNhan() {
        try {
            quanLyBenhNhanPanel.loadbenhnhan();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tai lai dich vu
    private void taiLaiDichVu(QuanLyDichVuPanel panel) {
        try {
            panel.loaddichvu();
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Gan su kien cho menu
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

    // Doi mau nut khi chon
    private void doiMauNut(JButton nutMoi) {
        if (nutDangChon != null) {
            nutDangChon.setBackground(mauNut);
            nutDangChon.setForeground(mauChu);
        }

        nutMoi.setBackground(mauNut);
        nutMoi.setForeground(mauChon);
        nutDangChon = nutMoi;
    }

    // Chuyen giua cac man hinh
    private void chuyenTrang(String tenCard, JButton nutMoi, QuanLyPanel managerMoi) {
        if (managerDangMo != null && managerDangMo != managerMoi) {
            managerDangMo.reset();
        }

        cardLayout.show(mainbar_center, tenCard);
        managerDangMo = managerMoi;
        doiMauNut(nutMoi);
    }

    // Mo form them nhan vien
    private void moManHinhThemNhanVien() {
        nhanVienManager.show(new ThemNhanVienPanel(nhanVienManager::showList));
    }

    // Mo form sua nhan vien
    private void moManHinhSuaNhanVien(String maNV) {
        nhanVienManager.show(new SuaNhanVienPanel(maNV, nhanVienManager::showList));
    }

    // Mo form them benh nhan
    private void moManHinhThemBenhNhan() {
        benhNhanManager.show(new ThemBenhNhanPanel(benhNhanManager::showList));
    }

    // Mo form sua benh nhan
    private void moManHinhSuaBenhNhan(String maBN) {
        benhNhanManager.show(new SuaBenhNhanPanel(maBN, benhNhanManager::showList));
    }

    // Mo form them dich vu
    private void moManHinhThemDichVu() {
        dichVuManager.show(new ThemDichVuPanel(dichVuManager::showList));
    }

    // Mo form sua dich vu
    private void moManHinhSuaDichVu(String maDV) {
        dichVuManager.show(new SuaDichVuPanel(maDV, dichVuManager::showList));
    }

    // Hien thi loi
    private void baoLoi(Exception loi) {
        JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Xu ly nut menu duoc nhan
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

    // Dang xuat khoi chuong trinh
    private void dangXuat() {
        int luaChon = JOptionPane.showConfirmDialog(
            this, "Bạn muốn đăng xuất khỏi MediDent?",
            "Xác nhận đăng xuất", JOptionPane.YES_NO_OPTION
        );

        if (luaChon == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLy_GUI().setVisible(true));
    }
}
