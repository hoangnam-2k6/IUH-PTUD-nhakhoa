package UI.Chung;

import Entity.TaiKhoan;
import UI.QuanLy.*;
import UI.LeTan.*;
import UI.QuanTriVien.*;
import ConnectDB.ConnectDB;
import DAO.TaiKhoanDAO;
import DAO.ThanhToanKhuyenMaiDAO;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** Màn hình kiểm thử RIÊNG: tất cả nút là chức năng thật, không có hộp chọn vai trò. */
public final class FullGUI extends JFrame {
    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel menu=new JPanel();
    private final JPanel noiDung=new JPanel(new BorderLayout());
    private final Map<String,JButton> nut=new LinkedHashMap<>();
    private final ThanhToanKhuyenMaiDAO tienDao=new ThanhToanKhuyenMaiDAO(ConnectDB::getConnection);
    private QuanLyPanel qlNhanVien,qlBenhNhan,qlDichVu;
    private JPanel khungNhanVien,khungBenhNhan,khungDichVu;
    private static final Color BLUE=new Color(0x0284C7);
    private static final Color BG=new Color(0xF8FAFC);
    private static final Color XANH = new Color(0x0284C7);
    private static final Color XANH_DAM = new Color(0x0369A1);
    private static final Color NEN = new Color(0xF8FAFC);
    private static final Color VIEN = new Color(0xE2E8F0);
    private static final Color CHU_PHU = new Color(0x64748B);
    public FullGUI(TaiKhoan taiKhoan,TaiKhoanDAO dao) {
        super("MEDIDENT - KIỂM THỬ ĐẦY ĐỦ");
        if(taiKhoan==null || !"full".equals(taiKhoan.tenDangNhap)
                || !"Quản trị viên".equals(taiKhoan.tenVaiTro)
                || !"Hoạt động".equals(taiKhoan.trangThai))
            throw new IllegalArgumentException("Chỉ tài khoản kiểm thử full đã xác thực mới được mở màn hình này.");
        this.taiKhoan=taiKhoan;this.dao=dao;
        setSize(1030, 660);;setMinimumSize(new Dimension(1060,650));
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
        themNut("ql_tt","Thanh toán hóa đơn (QL)",() -> new ThanhToanPanel(tienDao,taiKhoan.id));
        themNut("ql_draft","Lập hóa đơn theo dịch vụ",() -> new ThanhToanHoaDonLeTan(taiKhoan));
        themNut("ql_km","Quản lý mã khuyến mãi",() -> new QuanLyKhuyenMaiPanel(tienDao,taiKhoan.id));
        themNut("ql_bc","Báo cáo thống kê",BaoCaoThongKePanel::new);
        themNhom("LỄ TÂN");
        themNut("lt_home","Tổng quan lễ tân",() -> new TrangChuLeTan(taiKhoan));
        themNut("lt_tc","Tra cứu bệnh nhân",() -> new TraCuuThongTin(taiKhoan));
        themNut("lt_lh","Lịch hẹn và tiếp nhận",() -> new QuanLyLichHenLeTan(taiKhoan));
        themNut("lt_kham","Khám và điều trị",() -> new KhamBenhLeTan(taiKhoan));
        themNut("lt_tt","Thanh toán hóa đơn (LT)",() -> new ThanhToanPanel(tienDao,taiKhoan.id));
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
        cuon.getVerticalScrollBar().setUnitIncrement(22);
        thanh.add(cuon,BorderLayout.CENTER);
        JButton thoat=new JButton("Đăng xuất");
        thoat.setHorizontalAlignment(SwingConstants.LEFT);
        thoat.setFont(new Font("Segoe UI",Font.BOLD,12));
        thoat.setBackground(Color.WHITE); 
        thoat.setForeground(CHU_PHU);
        thoat.setFocusPainted(false);
        thoat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        thoat.setBorder(new CompoundBorder(new LineBorder(VIEN,1,true),new EmptyBorder(12,13,12,8)));
        thoat.setAlignmentX(LEFT_ALIGNMENT);
        thoat.setMaximumSize(new Dimension(225,46));
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
        JButton b=new JButton(ten);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI",Font.BOLD,12));
        b.setBackground(Color.WHITE); 
        b.setForeground(CHU_PHU);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(new LineBorder(VIEN,1,true),new EmptyBorder(12,13,12,8)));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(225,46));
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
                ()->qlNhanVien.show(new ThemNhanVienPanel(qlNhanVien::showList)),
                id->qlNhanVien.show(new SuaNhanVienPanel(id,qlNhanVien::showList)));
            qlNhanVien=new QuanLyPanel(khungNhanVien,ds,ds::loadnhanvien);
        }
        qlNhanVien.showList();return khungNhanVien;
    }
    private JPanel benhNhan(){
        if(qlBenhNhan==null){
            khungBenhNhan=new JPanel(new BorderLayout());
            QuanLyBenhNhanPanel ds=new QuanLyBenhNhanPanel(
                ()->qlBenhNhan.show(new ThemBenhNhanPanel(qlBenhNhan::showList)),
                id->qlBenhNhan.show(new SuaBenhNhanPanel(id,qlBenhNhan::showList)));
            qlBenhNhan=new QuanLyPanel(khungBenhNhan,ds,ds::loadbenhnhan);
        }
        qlBenhNhan.showList();return khungBenhNhan;
    }
    private JPanel dichVu(){
        if(qlDichVu==null){
            khungDichVu=new JPanel(new BorderLayout());
            QuanLyDichVuPanel ds=new QuanLyDichVuPanel(
                ()->qlDichVu.show(new ThemDichVuPanel(qlDichVu::showList)),
                id->qlDichVu.show(new SuaDichVuPanel(id,qlDichVu::showList)));
            qlDichVu=new QuanLyPanel(khungDichVu,ds,ds::loaddichvu);
        }
        qlDichVu.showList();return khungDichVu;
    }
}
