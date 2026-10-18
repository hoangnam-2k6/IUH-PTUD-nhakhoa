package UI.Chung;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.QuanLyDAO;
import UI.QuanLy.*;
import UI.LeTan.TrangChuLeTan;
import UI.QuanTriVien.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * CỬA SỔ DUY NHẤT của MEDIDENT sau đăng nhập.
 * - Header, logo, nav, nút hỗ trợ và đăng xuất dùng chung mọi vai trò.
 * - Chọn trang theo vai trò + quyền đang lưu trong SQL Server.
 * - Không tạo JFrame/MainWindow khác trong các màn hình Quản lý/Lễ tân.
 */
public final class KhungChinh extends JFrame {

    private static final Color ACTIVE = new Color(0xE0F2FE);
    private final TaiKhoan user;
    private final KhoDuLieu db;
    private final QuanLyDAO quanLyDAO = new QuanLyDAO();
    private final JPanel center = new JPanel(new BorderLayout());
    private final JPanel menu = new JPanel();
    private final Map<String,JButton> buttons = new LinkedHashMap<>();
    private final List<Muc> danhMuc = new ArrayList<>();

    private static final class Muc {
        final String id, ten, quyen;
        final BieuTuong.Loai icon;
        final boolean chiAdmin;
        Muc(String id,String ten,String quyen,BieuTuong.Loai icon,boolean chiAdmin) {
            this.id=id;this.ten=ten;this.quyen=quyen;this.icon=icon;this.chiAdmin=chiAdmin;
        }
    }

    public KhungChinh(TaiKhoan user,KhoDuLieu db) {
        this.user=Objects.requireNonNull(user);
        this.db=Objects.requireNonNull(db);
        taoDanhMuc();
        setTitle("MEDIDENT - "+user.tenVaiTro);
        Dimension kichThuocManHinh = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().getSize();
        setSize(Math.min(1400, kichThuocManHinh.width),
                Math.min(900, kichThuocManHinh.height));
        setMinimumSize(new Dimension(950,620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root=new JPanel(new BorderLayout());
        root.setBackground(GiaoDien.BG);
        setContentPane(root);
        root.add(taoSidebar(),BorderLayout.WEST);
        JPanel right=new JPanel(new BorderLayout());
        right.add(taoHeader(),BorderLayout.NORTH);
        right.add(center,BorderLayout.CENTER);
        root.add(right,BorderLayout.CENTER);
        capNhatMenu();
        moTrang("HOME");
    }

    private void taoDanhMuc() {
        danhMuc.add(new Muc("HOME","Trang chủ",null,BieuTuong.Loai.TRANG_CHU,false));
        // Admin chỉ có Trang chủ, Tài khoản, Phân quyền, Hệ thống, Báo cáo hệ thống và Hỗ trợ.
        danhMuc.add(new Muc("TAIKHOAN","Tài khoản","TAIKHOAN",BieuTuong.Loai.TAI_KHOAN,true));
        danhMuc.add(new Muc("PHANQUYEN","Phân quyền","TAIKHOAN",BieuTuong.Loai.PHAN_QUYEN,true));
        danhMuc.add(new Muc("HETHONG","Hệ thống","HETHONG",BieuTuong.Loai.HE_THONG,true));
        // Admin: nơi xem và phê duyệt yêu cầu hủy hóa đơn/phiếu thu.
        danhMuc.add(new Muc("DUYETHUY","Duyệt hủy hóa đơn","HOTRO",BieuTuong.Loai.KIEM_TRA,true));
        // Vận hành phòng khám: theo quyền do Admin cấp cho mỗi vai trò.
        danhMuc.add(new Muc("NHANVIEN","Nhân viên","NHANVIEN",BieuTuong.Loai.TAI_KHOAN,false));
        danhMuc.add(new Muc("DICHVU","Dịch vụ nha khoa","DICHVU",BieuTuong.Loai.HE_THONG,false));
        danhMuc.add(new Muc("BENHNHAN","Bệnh nhân","BENHNHAN",BieuTuong.Loai.TAI_KHOAN,false));
        danhMuc.add(new Muc("LICHHEN","Lịch hẹn","LICHHEN",BieuTuong.Loai.THONG_BAO,false));
        danhMuc.add(new Muc("THANHTOAN","Thanh toán & hóa đơn","THANHTOAN",BieuTuong.Loai.BAO_CAO,false));
        danhMuc.add(new Muc("BAOCAO","Báo cáo - thống kê","BAOCAO",BieuTuong.Loai.BAO_CAO,false));
    }

    private JPanel taoSidebar() {
        JPanel side=new JPanel(new BorderLayout());
        side.setBackground(Color.WHITE);
        side.setBorder(new MatteBorder(0,0,0,1,GiaoDien.BORDER));
        side.setPreferredSize(new Dimension(250,0));
        JPanel brand=new JPanel(new BorderLayout(10,0));
        brand.setBackground(Color.WHITE);
        brand.setBorder(new EmptyBorder(19,15,20,9));
        brand.add(new JLabel(GiaoDien.logo(45,45)),BorderLayout.WEST);
        JPanel brandText=new JPanel();brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText,BoxLayout.Y_AXIS));
        brandText.add(GiaoDien.label("MEDIDENT",16,true));
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(GiaoDien.label("Quản lý nha khoa",10,false));
        brand.add(brandText,BorderLayout.CENTER);
        side.add(brand,BorderLayout.NORTH);

        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu,BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(17,11,14,11));
        JLabel header=GiaoDien.label("CHỨC NĂNG HỆ THỐNG",11,true);
        header.setForeground(GiaoDien.MUTED);
        menu.add(header);
        menu.add(Box.createVerticalStrut(15));
        for (Muc muc:danhMuc) {
            JButton b=GiaoDien.nutIcon(muc.ten,false,muc.icon);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setMaximumSize(new Dimension(225,44));
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.setBorder(new EmptyBorder(11,11,11,7));
            b.addActionListener(e->moTrang(muc.id));
            buttons.put(muc.id,b);
            menu.add(b);
            menu.add(Box.createVerticalStrut(5));
        }
        JScrollPane scroll=new JScrollPane(menu);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        side.add(scroll,BorderLayout.CENTER);

        JPanel bottom=new JPanel();bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(10,13,15,13));
        bottom.setLayout(new BoxLayout(bottom,BoxLayout.Y_AXIS));
        JButton help=GiaoDien.nutIcon("Cần hỗ trợ?  Gửi yêu cầu",false,BieuTuong.Loai.THONG_BAO);
        help.setAlignmentX(LEFT_ALIGNMENT);
        help.setMaximumSize(new Dimension(230,45));
        help.addActionListener(e->moTrang("HOTRO"));
        bottom.add(help);
        bottom.add(Box.createVerticalStrut(13));
        JLabel info=GiaoDien.label("Đăng nhập: "+user.hoTen,11,true);
        info.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(info);
        JLabel role=GiaoDien.label(user.tenVaiTro,10,false);
        role.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(role);
        bottom.add(Box.createVerticalStrut(10));
        JButton logout=GiaoDien.nutIcon("Đăng xuất",false,BieuTuong.Loai.DANG_XUAT);
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.addActionListener(e->{dispose();new UI.DangNhap.DangNhap(db).setVisible(true);});
        bottom.add(logout);
        side.add(bottom,BorderLayout.SOUTH);
        return side;
    }

    /** Header gồm 2 hàng tên và vai trò, tự lấy từ TaiKhoan / NhanVien. */
    private JPanel taoHeader() {
        JPanel top=new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,GiaoDien.BORDER),
                new EmptyBorder(16,23,16,25)));
        top.add(GiaoDien.label("MEDIDENT    /    Quản lý nha khoa",13,true),BorderLayout.WEST);
        JPanel east=new JPanel(new GridLayout(2,1,0,4));
        east.setOpaque(false);
        east.add(GiaoDien.label(user.hoTen,12,true));
        east.add(GiaoDien.label(user.tenVaiTro,11,false));
        top.add(east,BorderLayout.EAST);
        return top;
    }

    /**
     * Phân định tuyệt đối menu Admin và menu nghiệp vụ.
     * Quyền SQL tiếp tục quyết định từng mục trong phạm vi vai trò tương ứng.
     */
    private boolean dungNhomVaiTro(Muc muc, boolean laAdmin) {
        if (muc.id.equals("HOME") || muc.id.equals("BAOCAO")) return true;
        return muc.chiAdmin == laAdmin;
    }

    private void capNhatMenu() {
        for(Muc muc:danhMuc) {
            boolean allowed=false;
            try {
                // Không tin vai trò đã cache từ lúc đăng nhập: luôn đối chiếu SQL.
                boolean adminHienTai = db.laQuanTriVienTaiKhoan(user.id);
                allowed=dungNhomVaiTro(muc,adminHienTai)
                        && (muc.quyen==null || db.coQuyenTaiKhoan(user.id,muc.quyen));
            } catch(Exception ignored) {
                // Không xác định được quyền => ẩn, tránh hiển thị nhầm chức năng.
            }
            buttons.get(muc.id).setVisible(allowed);
        }
    }

    private void moTrang(String page) {
        try {
            if(!db.taiKhoanDangHoatDong(user.id)) {
                JOptionPane.showMessageDialog(this,"Phiên đăng nhập không còn hiệu lực.");
                dispose();new UI.DangNhap.DangNhap(db).setVisible(true);return;
            }
            boolean currentAdmin=db.laQuanTriVienTaiKhoan(user.id);
            Muc muc=null;
            for(Muc m:danhMuc)if(m.id.equals(page)){muc=m;break;}
            if(muc!=null && (!dungNhomVaiTro(muc,currentAdmin)
                    ||(muc.quyen!=null&&!db.coQuyenTaiKhoan(user.id,muc.quyen)))) {
                JOptionPane.showMessageDialog(this,"Không có quyền truy cập chức năng này.");
                capNhatMenu();return;
            }
            JPanel view;
            switch(page) {
                case "TAIKHOAN": view=new QuanLyTaiKhoan(db,user);break;
                case "PHANQUYEN": view=new QuanLyPhanQuyen(db,user);break;
                case "HETHONG": view=new QuanTriHeThong(db,user);break;
                case "DUYETHUY": view=new DuyetHuyPhieuThu(quanLyDAO,user,currentAdmin && db.coQuyenTaiKhoan(user.id,"HOTRO"));break;
                case "NHANVIEN": view=new NhanVienGUI(quanLyDAO,user);break;
                case "DICHVU": view=new DichVuGUI(quanLyDAO,user);break;
                case "BENHNHAN": view=new BenhNhanGUI(quanLyDAO,user);break;
                case "LICHHEN": view=new LichHenGUI(quanLyDAO,user);break;
                case "THANHTOAN": view=new ThanhToanGUI(quanLyDAO,user);break;
                case "BAOCAO": view=currentAdmin?new BaoCaoHeThongAdmin(db,user):new BaoCaoThongKeGUI(user);break;
                case "HOTRO":
                    view=new HoTroPage(quanLyDAO,user,currentAdmin && db.coQuyenTaiKhoan(user.id,"HOTRO"));
                    break;
                case "HOME":
                    if(currentAdmin)view=new TrangChuQuanTriVien(db);
                    else if("Lễ tân".equalsIgnoreCase(user.tenVaiTro))view=new TrangChuLeTan(db,user);
                    else if("Quản lý".equalsIgnoreCase(user.tenVaiTro)
                            || "Quản lý phòng khám".equalsIgnoreCase(user.tenVaiTro))
                        view=new nhanvienquanly_gui(db,user);
                    else view=new TrangChuTheoVaiTro(user);
                    break;
                default:throw new IllegalArgumentException("Màn hình chưa hỗ trợ: "+page);
            }
            for(Muc m:danhMuc) {
                JButton button=buttons.get(m.id);
                boolean active=m.id.equals(page);
                button.setBackground(active?ACTIVE:Color.WHITE);
                button.setForeground(active?GiaoDien.PRIMARY:GiaoDien.MUTED);
                button.setIcon(new BieuTuong(m.icon,active?GiaoDien.PRIMARY:GiaoDien.MUTED,17));
            }
            center.removeAll();
            center.add(view,BorderLayout.CENTER);
            center.revalidate();center.repaint();
            capNhatMenu();
        }catch(Exception e){GiaoDien.loi(this,e);}
    }
}
