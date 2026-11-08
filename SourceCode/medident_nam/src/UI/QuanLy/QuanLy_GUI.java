package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class QuanLy_GUI extends JFrame implements ActionListener {

    private JButton trangchu;
    private JButton nhanvien;
    private JButton lichhen;
    private JButton benhnhan;
    private JButton dichvu;
    private JButton dangxuat;
    private JButton thongke;
    private JButton hoadon;
    private JButton hotro;

    private JPanel sidebar;
    private JPanel sidebar_bottom;
    private JPanel sidebar_top;
    private JPanel mainbar_top_left;
    private JPanel mainbar_top_right;
    private JPanel mainbar_center;

    private JLabel tenapp;
    private JLabel lblchucnang;
    private JLabel tenapp2;
    private JLabel hoten;
    private JLabel vaitro;

    private final Color maumacdinh = Color.WHITE;
    private final Color maukhichon = new Color(52, 152, 219);
    private JButton nutDangChon;

    // CardLayout chính
    private CardLayout cardLayout;

    // Panel danh sách
    private QuanLyNhanVienPanel quanLyNhanVienPanel;
    private QuanLyBenhNhanPanel quanLyBenhNhanPanel;

    // Manager của từng chức năng
    private QuanLyPanel nhanVienManager;
    private QuanLyPanel benhNhanManager;
    private QuanLyPanel dichVuManager;
    private QuanLyPanel lichHenManager;
    private QuanLyPanel hoaDonManager;

    // Manager đang được mở
    private QuanLyPanel managerDangMo;

    public QuanLy_GUI() {
        setTitle("MEDIDENT - Quản lý nha khoa");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // =========================
        // SIDEBAR
        // =========================
        sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(Color.WHITE);

        sidebar_top = new JPanel();
        sidebar_top.setBackground(Color.WHITE);
        tenapp = new JLabel("MEDIDENT");
        sidebar_top.add(tenapp);

        Box chucnang = Box.createVerticalBox();
        chucnang.setMaximumSize(new Dimension(300, 50));
        lblchucnang = new JLabel("chức năng hệ thống");
        lblchucnang.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        trangchu = taoNutMenu("Trang Chủ", true);
        nhanvien = taoNutMenu("Nhân Viên", false);
        benhnhan = taoNutMenu("Bệnh nhân", false);
        dichvu = taoNutMenu("Dịch vụ", false);
        lichhen = taoNutMenu("Lịch hẹn", false);
        hoadon = taoNutMenu("Hóa Đơn", false);
        thongke = taoNutMenu("Thống kê", false);

        chucnang.add(Box.createVerticalStrut(10));
        chucnang.add(lblchucnang);
        chucnang.add(Box.createVerticalStrut(10));
        chucnang.add(trangchu);
        chucnang.add(nhanvien);
        chucnang.add(benhnhan);
        chucnang.add(dichvu);
        chucnang.add(lichhen);
        chucnang.add(hoadon);
        chucnang.add(thongke);

        sidebar_bottom = new JPanel(new BorderLayout(10, 10));
        sidebar_bottom.setBackground(Color.WHITE);
        sidebar_bottom.setBorder(BorderFactory.createEmptyBorder());

        hotro = taoNutMenu("Hỗ trợ", false);
        dangxuat = taoNutMenu("Đăng xuất", false);
        sidebar_bottom.add(hotro, BorderLayout.NORTH);
        sidebar_bottom.add(dangxuat, BorderLayout.SOUTH);

        sidebar.add(sidebar_top, BorderLayout.NORTH);
        sidebar.add(chucnang, BorderLayout.CENTER);
        sidebar.add(sidebar_bottom, BorderLayout.SOUTH);

        // =========================
        // MAINBAR HEADER
        // =========================
        JPanel mainbar = new JPanel(new BorderLayout());
        JPanel mainbar_top = new JPanel(new BorderLayout());
        mainbar_top.setBackground(Color.WHITE);

        mainbar_top_left = new JPanel();
        mainbar_top_left.setBackground(Color.WHITE);
        tenapp2 = new JLabel("MEDIDENT  /  Quản Lý Nha Khoa");
        mainbar_top_left.add(tenapp2);
        mainbar_top.add(mainbar_top_left, BorderLayout.WEST);

        mainbar_top_right = new JPanel(new BorderLayout());
        mainbar_top_right.setBackground(Color.WHITE);
        hoten = new JLabel("Trịnh Hoàng Nam");
        vaitro = new JLabel("quản lý Phòng Khám");
        mainbar_top_right.add(hoten, BorderLayout.NORTH);
        mainbar_top_right.add(vaitro, BorderLayout.SOUTH);
        mainbar_top.add(mainbar_top_right, BorderLayout.EAST);
        mainbar.add(mainbar_top, BorderLayout.NORTH);

        // =========================
        // CARDLAYOUT CHÍNH
        // =========================
        cardLayout = new CardLayout();
        mainbar_center = new JPanel(cardLayout);

        mainbar_center.add(new TrangChuQuanLyPanel(), "trangchu");

        quanLyNhanVienPanel = new QuanLyNhanVienPanel(
            this::moManHinhThemNhanVien,
            this::moManHinhSuaNhanVien
        );
        nhanVienManager = themCard(
            "nhanvien",
            quanLyNhanVienPanel,
            quanLyNhanVienPanel::loadnhanvien
        );

        quanLyBenhNhanPanel = new QuanLyBenhNhanPanel(
            this::moManHinhThemBenhNhan,
            this::moManHinhSuaBenhNhan
        );
        benhNhanManager = themCard(
            "benhnhan",
            quanLyBenhNhanPanel,
            quanLyBenhNhanPanel::loadbenhnhan
        );

        QuanLyDichVuPanel quanLyDichVuPanel =
        	    new QuanLyDichVuPanel(
        	        this::moManHinhThemDichVu,
        	        this::moManHinhSuaDichVu
        	    );

        	dichVuManager = themCard(
        	    "dichvu",
        	    quanLyDichVuPanel,
        	    quanLyDichVuPanel::loaddichvu
        	);
        lichHenManager = themCard("lichhen", new QuanLyLichHenPanel(), null);
        hoaDonManager = themCard("hoadon", new QuanLyHoaDonPanel(), null);

        mainbar_center.add(new BaoCaoThongKePanel(), "thongke");
        mainbar.add(mainbar_center, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainbar, BorderLayout.CENTER);

        // =========================
        // LISTENERS
        // =========================
        trangchu.addActionListener(this);
        nhanvien.addActionListener(this);
        benhnhan.addActionListener(this);
        dichvu.addActionListener(this);
        lichhen.addActionListener(this);
        hoadon.addActionListener(this);
        thongke.addActionListener(this);
        hotro.addActionListener(this);
        dangxuat.addActionListener(this);

        doiMauNut(trangchu);
        cardLayout.show(mainbar_center, "trangchu");
    }

    private JButton taoNutMenu(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setForeground(selected ? Color.WHITE : maukhichon);
        button.setBackground(selected ? maukhichon : Color.WHITE);
        button.setPreferredSize(new Dimension(300, 50));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, maukhichon));
        return button;
    }

    private QuanLyPanel themCard(String tenCard, JPanel panelDanhSach, Runnable reload) {
        JPanel host = new JPanel(new BorderLayout());
        QuanLyPanel manager = new QuanLyPanel(host, panelDanhSach, reload);
        mainbar_center.add(host, tenCard);
        return manager;
    }

    private void doiMauNut(JButton nutMoi) {
        if (nutDangChon != null) {
            nutDangChon.setBackground(maumacdinh);
            nutDangChon.setForeground(maukhichon);
        }
        nutMoi.setBackground(maukhichon);
        nutMoi.setForeground(maumacdinh);
        nutDangChon = nutMoi;
    }

    private void chuyenTrang(String tenCard, JButton nutMoi, QuanLyPanel managerMoi) {
        if (managerDangMo != null && managerDangMo != managerMoi) {
            managerDangMo.reset();
        }
        cardLayout.show(mainbar_center, tenCard);
        managerDangMo = managerMoi;
        doiMauNut(nutMoi);
    }

    // =========================
    // NHÂN VIÊN
    // =========================
    private void moManHinhThemNhanVien() {
        nhanVienManager.show(new ThemNhanVienPanel(nhanVienManager::showList));
    }

    private void moManHinhSuaNhanVien(String maNV) {
        nhanVienManager.show(new SuaNhanVienPanel(maNV, nhanVienManager::showList));
    }

    // =========================
    // BỆNH NHÂN
    // =========================
    private void moManHinhThemBenhNhan() {
        benhNhanManager.show(new ThemBenhNhanPanel(benhNhanManager::showList));
    }

    private void moManHinhSuaBenhNhan(String maBN) {
        benhNhanManager.show(new SuaBenhNhanPanel(maBN, benhNhanManager::showList));
    }

    // Mở form thêm dịch vụ
    private void moManHinhThemDichVu() {
        dichVuManager.show(new ThemDichVuPanel(dichVuManager::showList));
    }

    // Mở form sửa dịch vụ
    private void moManHinhSuaDichVu(String maDV) {
        dichVuManager.show(new SuaDichVuPanel(maDV, dichVuManager::showList));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == trangchu) {
            chuyenTrang("trangchu", trangchu, null);
        } else if (o == nhanvien) {
            chuyenTrang("nhanvien", nhanvien, nhanVienManager);
        } else if (o == benhnhan) {
            chuyenTrang("benhnhan", benhnhan, benhNhanManager);
        } else if (o == dichvu) {
            chuyenTrang("dichvu", dichvu, dichVuManager);
        } else if (o == lichhen) {
            chuyenTrang("lichhen", lichhen, lichHenManager);
        } else if (o == hoadon) {
            chuyenTrang("hoadon", hoadon, hoaDonManager);
        } else if (o == thongke) {
            chuyenTrang("thongke", thongke, null);
        } else if (o == hotro) {
            JOptionPane.showMessageDialog(this, "Chức năng hỗ trợ chưa được triển khai.");
        } else if (o == dangxuat) {
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLy_GUI().setVisible(true));
    }
}
