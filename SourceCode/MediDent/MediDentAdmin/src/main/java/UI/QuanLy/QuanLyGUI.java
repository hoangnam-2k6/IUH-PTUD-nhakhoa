package UI.QuanLy;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import Entity.VaiTro;
import java.awt.*;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

import UI.Chung.HoTroPage;

/** Frame riêng cho vai trò Quản lý phòng khám; menu lấy từ map quyền. */
public final class QuanLyGUI extends JFrame {

    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel menu = new JPanel();
    private final JPanel noiDung = new JPanel(new BorderLayout());
    private final Map<String,Boolean> quyenMenu = new LinkedHashMap<>();
    private final Map<String,JButton> nutMenu = new LinkedHashMap<>();
    private String maDangChon = "HOME";
    private static final Color XANH = new Color(0x0284C7);
    private static final Color XANH_DAM = new Color(0x0369A1);
    private static final Color NEN = new Color(0xF8FAFC);
    private static final Color VIEN = new Color(0xE2E8F0);
    private static final Color CHU_PHU = new Color(0x64748B);

    public QuanLyGUI(TaiKhoan taiKhoan, TaiKhoanDAO dao) {
        if (taiKhoan == null || (!"Quản lý phòng khám".equals(taiKhoan.tenVaiTro)
                && !("full".equals(taiKhoan.tenDangNhap) && "Quản trị viên".equals(taiKhoan.tenVaiTro))))
            throw new IllegalArgumentException("Tài khoản không thuộc vai trò Quản lý phòng khám.");
        this.taiKhoan = taiKhoan;
        this.dao = dao;
        setTitle("MEDIDENT - " + taiKhoan.tenVaiTro);
        setSize(1220,790);
        setMinimumSize(new Dimension(950,620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel khung = new JPanel(new BorderLayout());
        setContentPane(khung);
        khung.add(taoMenu(), BorderLayout.WEST);
        noiDung.setBackground(NEN);
        khung.add(noiDung, BorderLayout.CENTER);
        hienThiPanel(new TrangChuQuanLyPanel(), "HOME");
        taiQuyen();
    }

    private JPanel taoMenu() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250,0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new MatteBorder(0,0,0,1,VIEN));
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT,12,20));
        logo.setBackground(Color.WHITE);
        URL anh = getClass().getResource("/Image/medident_logo.png");
        if (anh != null) {
            Image icon = new ImageIcon(anh).getImage().getScaledInstance(46,46,Image.SCALE_SMOOTH);
            logo.add(new JLabel(new ImageIcon(icon)));
        }
        JPanel nhanHieu = new JPanel();
        nhanHieu.setLayout(new BoxLayout(nhanHieu,BoxLayout.Y_AXIS));
        nhanHieu.setOpaque(false);
        JLabel ten = new JLabel("MEDIDENT");
        ten.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ten.setForeground(XANH);
        JLabel moTa = new JLabel("Quản lý nha khoa");
        moTa.setFont(new Font("Segoe UI",Font.PLAIN,11));
        nhanHieu.add(ten); nhanHieu.add(moTa); logo.add(nhanHieu);
        sidebar.add(logo,BorderLayout.NORTH);
        menu.setBackground(Color.WHITE);
        menu.setLayout(new BoxLayout(menu,BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(18,10,10,10));
        JLabel nhan = new JLabel("CHỨC NĂNG HỆ THỐNG");
        nhan.setFont(new Font("Segoe UI",Font.BOLD,11));
        nhan.setForeground(CHU_PHU); nhan.setAlignmentX(LEFT_ALIGNMENT);
        menu.add(nhan); menu.add(Box.createVerticalStrut(16));
        JScrollPane cuon = new JScrollPane(menu); cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(14);
        sidebar.add(cuon,BorderLayout.CENTER);
        JPanel duoi = new JPanel();
        duoi.setLayout(new BoxLayout(duoi,BoxLayout.Y_AXIS));
        duoi.setBackground(Color.WHITE); duoi.setBorder(new EmptyBorder(10,13,18,13));
        JLabel tenNguoiDung = new JLabel(taiKhoan.hoTen);
        tenNguoiDung.setFont(new Font("Segoe UI",Font.BOLD,12));
        JLabel vaiTro = new JLabel(taiKhoan.tenVaiTro);
        vaiTro.setForeground(CHU_PHU);
        duoi.add(tenNguoiDung); duoi.add(vaiTro);
        JButton dangXuat = new JButton("Đăng xuất");
        dangXuat.setAlignmentX(LEFT_ALIGNMENT);
        dangXuat.addActionListener(e -> {
            dispose();
            new UI.DangNhap.DangNhap(dao).setVisible(true);
        });
        duoi.add(Box.createVerticalStrut(12)); duoi.add(dangXuat);
        sidebar.add(duoi,BorderLayout.SOUTH);
        return sidebar;
    }

    // Read permissions from SQL. The account 'full' has a demo-only menu exception.
    private void taiQuyen() {
        new SwingWorker<List<VaiTro>,Void>() {
            @Override protected List<VaiTro> doInBackground() throws Exception { return dao.vaiTro(); }
            @Override protected void done() {
                try {
                    String[] dsMa = TaiKhoanDAO.cacQuyen();
                    for (VaiTro vt : get()) {
                        if (!"Quản lý phòng khám".equals(vt.ten)) continue;
                        for (int i=0; i<dsMa.length; i++)
                            quyenMenu.put(dsMa[i], i<vt.quyen.length && vt.quyen[i]);
                    }
                    if ("full".equals(taiKhoan.tenDangNhap)) {
                        for (String ma : dsMa) quyenMenu.put(ma,true);
                    }
                    hienThiMenu();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(QuanLyGUI.this,
                        "Không tải được phân quyền: " + ex.getMessage(), "Kết nối", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JButton themNut(String ma, String ten) {
        JButton nut = new JButton(ten);
        nut.setHorizontalAlignment(SwingConstants.LEFT);
        nut.setFont(new Font("Segoe UI",Font.BOLD,12));
        nut.setBackground(Color.WHITE); nut.setForeground(CHU_PHU);
        nut.setFocusPainted(false);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.setBorder(new CompoundBorder(new LineBorder(VIEN,1,true),new EmptyBorder(12,13,12,8)));
        nut.setAlignmentX(LEFT_ALIGNMENT);
        nut.setMaximumSize(new Dimension(225,46));
        nutMenu.put(ma,nut); menu.add(nut); menu.add(Box.createVerticalStrut(7));
        return nut;
    }

    private void hienThiPanel(JPanel panel, String ma) {
        noiDung.removeAll(); noiDung.add(panel,BorderLayout.CENTER);
        noiDung.revalidate(); noiDung.repaint();
        maDangChon = ma;
        for (Map.Entry<String,JButton> nut : nutMenu.entrySet()) {
            boolean chon = nut.getKey().equals(ma);
            nut.getValue().setBackground(chon ? XANH_DAM : Color.WHITE);
            nut.getValue().setForeground(chon ? Color.WHITE : CHU_PHU);
        }
    }

    private QuanLyPanel nhanVienManager, benhNhanManager, dichVuManager;
    private JPanel khungNhanVien,khungBenhNhan,khungDichVu;

    private void hienThiMenu() {
        while (menu.getComponentCount()>2) menu.remove(2);
        nutMenu.clear();
        themNut("HOME","Trang chủ").addActionListener(e ->
            hienThiPanel(new TrangChuQuanLyPanel(),"HOME"));
        if (quyenMenu.getOrDefault("QL_NHANVIEN",false))
            themNut("QL_NHANVIEN","Quản lý nhân viên").addActionListener(e -> {
                if (nhanVienManager==null) {
                    khungNhanVien=new JPanel(new BorderLayout());
                    QuanLyNhanVienPanel ds = new QuanLyNhanVienPanel(
                        () -> nhanVienManager.show(new ThemNhanVienPanel(nhanVienManager::showList)),
                        id -> nhanVienManager.show(new SuaNhanVienPanel(id,nhanVienManager::showList)));
                    nhanVienManager=new QuanLyPanel(khungNhanVien,ds,ds::loadnhanvien);
                }
                nhanVienManager.showList(); hienThiPanel(khungNhanVien,"QL_NHANVIEN");
            });
        if (quyenMenu.getOrDefault("QL_BENHNHAN",false))
            themNut("QL_BENHNHAN","Quản lý bệnh nhân").addActionListener(e -> {
                if (benhNhanManager==null) {
                    khungBenhNhan=new JPanel(new BorderLayout());
                    QuanLyBenhNhanPanel ds=new QuanLyBenhNhanPanel(
                        () -> benhNhanManager.show(new ThemBenhNhanPanel(benhNhanManager::showList)),
                        id -> benhNhanManager.show(new SuaBenhNhanPanel(id,benhNhanManager::showList)));
                    benhNhanManager=new QuanLyPanel(khungBenhNhan,ds,ds::loadbenhnhan);
                }
                benhNhanManager.showList(); hienThiPanel(khungBenhNhan,"QL_BENHNHAN");
            });
        if (quyenMenu.getOrDefault("QL_DICHVU",false))
            themNut("QL_DICHVU","Quản lý dịch vụ").addActionListener(e -> {
                if (dichVuManager==null) {
                    khungDichVu=new JPanel(new BorderLayout());
                    QuanLyDichVuPanel ds=new QuanLyDichVuPanel(
                        () -> dichVuManager.show(new ThemDichVuPanel(dichVuManager::showList)),
                        id -> dichVuManager.show(new SuaDichVuPanel(id,dichVuManager::showList)));
                    dichVuManager=new QuanLyPanel(khungDichVu,ds,ds::loaddichvu);
                }
                dichVuManager.showList(); hienThiPanel(khungDichVu,"QL_DICHVU");
            });
        if (quyenMenu.getOrDefault("QL_LICHHEN",false))
            themNut("QL_LICHHEN","Quản lý lịch hẹn").addActionListener(e ->
                hienThiPanel(new UI.LeTan.QuanLyLichHenLeTan(taiKhoan),"QL_LICHHEN"));
        if (quyenMenu.getOrDefault("QL_THANHTOAN",false))
            themNut("QL_THANHTOAN","Thanh toán và hóa đơn").addActionListener(e ->
                hienThiPanel(new ThanhToanPanel(new DAO.ThanhToanKhuyenMaiDAO(ConnectDB.ConnectDB::getConnection),taiKhoan.id),"QL_THANHTOAN"));
        if (quyenMenu.getOrDefault("QL_THANHTOAN",false))
            themNut("QL_LAP_HOADON","Lập hóa đơn theo dịch vụ").addActionListener(e ->
                hienThiPanel(new UI.LeTan.ThanhToanHoaDonLeTan(taiKhoan),"QL_LAP_HOADON"));
        if (quyenMenu.getOrDefault("QL_KHUYENMAI",false))
            themNut("QL_KHUYENMAI","Quản lý khuyến mãi").addActionListener(e ->
                hienThiPanel(new QuanLyKhuyenMaiPanel(new DAO.ThanhToanKhuyenMaiDAO(ConnectDB.ConnectDB::getConnection),taiKhoan.id),"QL_KHUYENMAI"));
        if (quyenMenu.getOrDefault("QL_BAOCAO",false))
            themNut("QL_BAOCAO","Báo cáo thống kê").addActionListener(e ->
                hienThiPanel(new BaoCaoThongKePanel(),"QL_BAOCAO"));
        if (quyenMenu.getOrDefault("HOTRO",false))
            themNut("HOTRO","Hỗ trợ").addActionListener(e ->
                hienThiPanel(new HoTroPage(taiKhoan),"HOTRO"));
        menu.revalidate(); menu.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyGUI(
            new TaiKhoan(0,"xemtruoc","Xem trước giao diện","",0,"Quản lý phòng khám","Hoạt động"),
            new TaiKhoanDAO()).setVisible(true)); // Run GUI independently for preview; DAO still checks real rights.
    }
}
