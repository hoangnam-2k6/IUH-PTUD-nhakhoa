package UI.Chung;

import DAO.TaiKhoanDAO;
import DAO.ThanhToanKhuyenMaiDAO;
import Entity.TaiKhoan;
import UI.QuanLy.*;
import UI.LeTan.*;
import UI.QuanTriVien.*;
import ConnectDB.ConnectDB;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Màn hình kiểm thử RIÊNG: tất cả nút là chức năng thật, không có hộp chọn vai trò. */
public final class FullGUI extends JFrame {
    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel menu=new JPanel();
    private final JPanel noiDung=new JPanel(new BorderLayout());
    private final Map<String,JButton> nut=new LinkedHashMap<>();
    private final ThanhToanKhuyenMaiDAO tienDao=new ThanhToanKhuyenMaiDAO(ConnectDB::layConnection);
    private QuanLyPanel qlNhanVien,qlBenhNhan,qlDichVu;
    private JPanel khungNhanVien,khungBenhNhan,khungDichVu;
    private static final Color BLUE=new Color(0x0284C7);
    private static final Color BG=new Color(0xF8FAFC);

    public FullGUI(TaiKhoan taiKhoan,TaiKhoanDAO dao) {
        super("MEDIDENT - KIỂM THỬ ĐẦY ĐỦ");
        if(taiKhoan==null || !"full".equals(taiKhoan.tenDangNhap)
                || !"Quản trị viên".equals(taiKhoan.tenVaiTro)
                || !"Hoạt động".equals(taiKhoan.trangThai))
            throw new IllegalArgumentException("Chỉ tài khoản kiểm thử full đã xác thực mới được mở màn hình này.");
        this.taiKhoan=taiKhoan;this.dao=dao;
        setSize(1380,850);setMinimumSize(new Dimension(1060,650));
        setLocationRelativeTo(null);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(taoThanhChucNang(),BorderLayout.WEST);
        noiDung.setBackground(BG);getContentPane().add(noiDung,BorderLayout.CENTER);
        themNhom("QUẢN TRỊ VIÊN");
        themNut("admin_home","Tổng quan quản trị",() -> new TrangChuQuanTriVien(dao,taiKhoan));
        themNut("admin_tk","Quản lý tài khoản",() -> new QuanLyTaiKhoan(dao,taiKhoan));
        themNut("admin_quyen","Quản lý phân quyền",() -> new QuanLyPhanQuyen(dao,taiKhoan));
        themNhom("QUẢN LÝ PHÒNG KHÁM");
        themNut("ql_nv","Quản lý nhân viên",() -> nhanVien());
        themNut("ql_bn","Quản lý bệnh nhân",() -> benhNhan());
        themNut("ql_dv","Quản lý dịch vụ",() -> dichVu());
        themNut("ql_lh","Quản lý lịch hẹn",() -> new QuanLyLichHenLeTan(taiKhoan));
        themNut("ql_tt","Quản lý hóa đơn (QL)",QuanLyHoaDonPanel::new);
        themNut("ql_km","Quản lý mã khuyến mãi",() -> new QuanLyKhuyenMaiPanel(tienDao,taiKhoan.id));
        themNut("ql_bc","Báo cáo thống kê",BaoCaoThongKePanel::new);
        themNhom("LỄ TÂN");
        themNut("lt_home","Tổng quan lễ tân",() -> new TrangChuLeTan(taiKhoan));
        themNut("lt_tc","Tra cứu bệnh nhân",() -> new TraCuuThongTin(taiKhoan));
        themNut("lt_lh","Lịch hẹn và tiếp nhận",() -> new QuanLyLichHenLeTan(taiKhoan));
        themNut("lt_kham","Khám và điều trị",() -> new KhamBenhLeTan(taiKhoan));
        themNut("lt_tt","Thanh toán hóa đơn (LT)",() -> new UI.LeTan.ThanhToanPanel(tienDao,taiKhoan.id));
        themNut("help","Hướng dẫn sử dụng",() -> new HoTroPage(taiKhoan));
        chon("admin_home",new TrangChuQuanTriVien(dao,taiKhoan));
    }

    private JPanel taoThanhChucNang(){
        JPanel thanh=new JPanel(new BorderLayout());thanh.setPreferredSize(new Dimension(258,0));
        thanh.setBackground(Color.WHITE);
        JLabel head=new JLabel("  MEDIDENT | FULL TEST");head.setFont(new Font("Segoe UI",Font.BOLD,17));
        head.setForeground(BLUE);head.setPreferredSize(new Dimension(0,72));
        thanh.add(head,BorderLayout.NORTH);
        menu.setLayout(new BoxLayout(menu,BoxLayout.Y_AXIS));menu.setBackground(Color.WHITE);
        menu.setBorder(new EmptyBorder(12,10,10,10));
        JScrollPane cuon=new JScrollPane(menu);cuon.setBorder(null);
        cuon.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        cuon.getVerticalScrollBar().setUnitIncrement(22);
        thanh.add(cuon,BorderLayout.CENTER);
        JButton thoat=new JButton("Đăng xuất");thoat.setFont(new Font("Segoe UI",Font.BOLD,13));
        thoat.addActionListener(e->{dispose();new UI.DangNhap.DangNhap(dao).setVisible(true);});
        JPanel footer=new JPanel(new BorderLayout());footer.setBorder(new EmptyBorder(12,12,16,12));
        footer.setBackground(Color.WHITE);footer.add(thoat);thanh.add(footer,BorderLayout.SOUTH);
        return thanh;
    }
    private void themNhom(String ten){
        JLabel tenNhom=new JLabel(ten);tenNhom.setFont(new Font("Segoe UI",Font.BOLD,11));
        tenNhom.setForeground(new Color(100,116,139));tenNhom.setAlignmentX(LEFT_ALIGNMENT);
        tenNhom.setBorder(new EmptyBorder(16,6,10,2));menu.add(tenNhom);
    }
    private void themNut(String ma,String ten,java.util.function.Supplier<JPanel> panel){
        JButton b=new JButton(ten);b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(235,40));
        b.setForeground(new Color(51,65,85));b.setBackground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.PLAIN,12));b.setFocusPainted(false);
        b.addActionListener(e->{try{chon(ma,panel.get());}catch(RuntimeException ex){
            JOptionPane.showMessageDialog(this,"Không mở được chức năng: "+ex.getMessage(),"Lỗi",JOptionPane.ERROR_MESSAGE);
        }});
        nut.put(ma,b);menu.add(b);menu.add(Box.createVerticalStrut(5));
    }
    private void chon(String ma,JPanel panel){
        noiDung.removeAll();noiDung.add(panel,BorderLayout.CENTER);
        noiDung.revalidate();noiDung.repaint();
        for(Map.Entry<String,JButton> x:nut.entrySet()){
            boolean active=ma.equals(x.getKey());
            x.getValue().setBackground(active?BLUE:Color.WHITE);
            x.getValue().setForeground(active?Color.WHITE:new Color(51,65,85));
        }
    }
    private JPanel nhanVien(){
        if(qlNhanVien==null){
            khungNhanVien=new JPanel(new BorderLayout());
            QuanLyNhanVienPanel ds=new QuanLyNhanVienPanel(
                ()->qlNhanVien.hienThi(new ThemNhanVienPanel(qlNhanVien::hienThiDanhSach)),
                id->qlNhanVien.hienThi(new SuaNhanVienPanel(id,qlNhanVien::hienThiDanhSach)));
            qlNhanVien=new QuanLyPanel(khungNhanVien,ds,ds::taiNhanvien);
        }
        qlNhanVien.hienThiDanhSach();return khungNhanVien;
    }
    private JPanel benhNhan(){
        if(qlBenhNhan==null){
            khungBenhNhan=new JPanel(new BorderLayout());
            QuanLyBenhNhanPanel ds=new QuanLyBenhNhanPanel(
                ()->qlBenhNhan.hienThi(new ThemBenhNhanPanel(qlBenhNhan::hienThiDanhSach)),
                id->qlBenhNhan.hienThi(new SuaBenhNhanPanel(id,qlBenhNhan::hienThiDanhSach)));
            qlBenhNhan=new QuanLyPanel(khungBenhNhan,ds,() -> { try { ds.taiBenhnhan(); } catch (java.sql.SQLException ex) { throw new IllegalStateException(ex); } });
        }
        qlBenhNhan.hienThiDanhSach();return khungBenhNhan;
    }
    private JPanel dichVu(){
        if(qlDichVu==null){
            khungDichVu=new JPanel(new BorderLayout());
            QuanLyDichVuPanel ds=new QuanLyDichVuPanel(
                ()->qlDichVu.hienThi(new ThemDichVuPanel(qlDichVu::hienThiDanhSach)),
                id->qlDichVu.hienThi(new SuaDichVuPanel(id,qlDichVu::hienThiDanhSach)));
            qlDichVu=new QuanLyPanel(khungDichVu,ds,ds::taiDichvu);
        }
        qlDichVu.hienThiDanhSach();return khungDichVu;
    }
}
