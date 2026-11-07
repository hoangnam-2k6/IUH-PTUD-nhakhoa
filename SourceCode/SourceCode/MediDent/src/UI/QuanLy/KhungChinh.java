
package UI.QuanLy;

import java.awt.*;
import javax.swing.*;

import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.GiaoDien;
import UI.DangNhap.DangNhap;

public class KhungChinh extends JFrame {

    private final CardLayout boChuyenTrang = new CardLayout();
    private final JPanel vungChucNang = new JPanel(boChuyenTrang);
    private final JLabel nhanTieuDe = new JLabel("Trang chủ");
    private final boolean cheDoXemTruoc;

    private final String[] tenTrang = {
        "Trang chủ", "Nhân viên", "Lịch làm việc",
        "Dịch vụ nha khoa", "Bệnh nhân",
        "Lịch hẹn", "Hóa đơn", "Thống kê"
    };

    private final JButton[] cacNut = new JButton[tenTrang.length];

    // Constructor su dung khi dang nhap binh thuong
    public KhungChinh(PhanQuyen.PhienDangNhap phienDangNhap) {
        this(phienDangNhap, "Trang chủ");
    }

    // Constructor su dung cho dang nhap va xem truoc
    public KhungChinh(PhanQuyen.PhienDangNhap phienDangNhap, String trangBanDau) {
        super("MediDent - Quản lý phòng khám");
        cheDoXemTruoc = phienDangNhap == null;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1420, 830);
        setMinimumSize(new Dimension(1040, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Tao cac man hinh chua thuc hien
        vungChucNang.add(new TrangChuPanel(), "Trang chủ");
        vungChucNang.add(new NhanVienPanel(), "Nhân viên");
        vungChucNang.add(taoTrangTrong(), "Lịch làm việc");
        vungChucNang.add(new DichVuPanel(), "Dịch vụ nha khoa");
        vungChucNang.add(new BenhNhanPanel(), "Bệnh nhân");
        vungChucNang.add(new LichHenPanel(phienDangNhap), "Lịch hẹn");
        vungChucNang.add(new ThanhToanPanel(phienDangNhap), "Hóa đơn");
        vungChucNang.add(new BaoCaoPanel(phienDangNhap), "Thống kê");

        // 3. Tao thanh menu ben trai
        add(taoThanhMenu(), BorderLayout.WEST);

        // 4. Tao khu vuc hien thi ben phai
        JPanel benPhai = new JPanel(new BorderLayout());
        JPanel dauTrang = new JPanel(new BorderLayout());

        dauTrang.setBackground(Color.WHITE);
        dauTrang.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        nhanTieuDe.setFont(new Font("Arial", Font.BOLD, 20));
        dauTrang.add(nhanTieuDe, BorderLayout.WEST);

        // Hien thi ten nguoi dung, tranh loi null khi xem truoc
        String tenNguoiDung = cheDoXemTruoc
                ? "Xem trước" : phienDangNhap.fullName();

        JLabel nhanNguoiDung = new JLabel(tenNguoiDung + " - Quản lý phòng khám");
        dauTrang.add(nhanNguoiDung, BorderLayout.EAST);

        benPhai.add(dauTrang, BorderLayout.NORTH);
        benPhai.add(vungChucNang, BorderLayout.CENTER);
        add(benPhai, BorderLayout.CENTER);

        // 5. Hien thi trang duoc chon khi mo chuong trinh
        boolean trangHopLe = false;
        for (String ten : tenTrang) {
            if (ten.equals(trangBanDau)) {
                trangHopLe = true;
                break;
            }
        }
        chuyenTrang(trangHopLe ? trangBanDau : "Trang chủ");
    }

    // Tao thanh menu ben trai
    private JPanel taoThanhMenu() {
        JPanel thanhMenu = new JPanel(new BorderLayout(0, 18));
        thanhMenu.setPreferredSize(new Dimension(254, 0));
        thanhMenu.setBorder(BorderFactory.createEmptyBorder(23, 14, 17, 14));
        thanhMenu.setBackground(new Color(235, 245, 250));

        // Ten ung dung
        JLabel tenUngDung = new JLabel("MEDIDENT", SwingConstants.CENTER);
        tenUngDung.setFont(new Font("Arial", Font.BOLD, 22));
        thanhMenu.add(tenUngDung, BorderLayout.NORTH);

        // Tao danh sach cac nut chuc nang
        JPanel danhSachNut = new JPanel(new GridLayout(0, 1, 0, 9));
        danhSachNut.setOpaque(false);

        for (int i = 0; i < tenTrang.length; i++) {
            String trang = tenTrang[i];

            JButton nut = GiaoDien.button(trang, false);
            nut.setFont(new Font("Arial", Font.BOLD, 14));
            nut.setPreferredSize(new Dimension(222, 51));
            nut.setMinimumSize(new Dimension(190, 48));

            // Chuyen trang khi bam nut
            nut.addActionListener(e -> chuyenTrang(trang));

            danhSachNut.add(nut);
            cacNut[i] = nut;
        }

        // Tao JPanel chua menu
        JPanel nhomMenu = new JPanel(new BorderLayout());
        nhomMenu.setOpaque(false);
        nhomMenu.add(danhSachNut, BorderLayout.NORTH);

        // An thanh cuon nhung van cho phep cuon
        JScrollPane vungMenu = new JScrollPane(
            nhomMenu,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        vungMenu.setBorder(null);
        vungMenu.setOpaque(false);
        vungMenu.getViewport().setOpaque(false);
        vungMenu.getVerticalScrollBar().setUnitIncrement(26);

        thanhMenu.add(vungMenu, BorderLayout.CENTER);

        // Nut dang xuat
        JButton nutDangXuat = GiaoDien.button("Đăng xuất", false);
        nutDangXuat.setFont(new Font("Arial", Font.BOLD, 14));
        nutDangXuat.setPreferredSize(new Dimension(222, 47));

        nutDangXuat.addActionListener(e -> {
            dispose();
            new DangNhap().setVisible(true);
        });

        thanhMenu.add(nutDangXuat, BorderLayout.SOUTH);
        return thanhMenu;
    }

    // Tao JPanel trong cho chuc nang chua thuc hien
    private JPanel taoTrangTrong() {
        JPanel trangTrong = new JPanel();
        trangTrong.setBackground(GiaoDien.BG);
        return trangTrong;
    }

    // Chuyen trang khi nguoi dung bam nut Sidebar
   
    private void chuyenTrang(String trang) {
    	
    boChuyenTrang.show(vungChucNang, trang);
    nhanTieuDe.setText("Quản lý phòng khám / " + trang);

    // Doi mau nut dang duoc chon
    for (int i = 0; i < tenTrang.length; i++) {
        boolean dangChon = tenTrang[i].equals(trang);
        cacNut[i].setBackground(dangChon ? new Color(205, 230, 244) : Color.WHITE);
        cacNut[i].setForeground(dangChon ? new Color(0, 94, 145) : Color.BLACK);
    }

    // Tai du lieu cua man hinh dang hien thi
    for (Component manHinh : vungChucNang.getComponents()) {
        if (!manHinh.isVisible()) continue;

        if (manHinh instanceof BangDuLieuPanel bangDuLieu) {
            bangDuLieu.refresh();
        } else if (manHinh instanceof BaoCaoPanel baoCao) {
            baoCao.refresh();
        }
    }
}


    // Mo toan bo form quan ly khi chay doc lap tung class
    public static void xemTruoc(String tenTrang) {
        SwingUtilities.invokeLater(() -> {
            KhungChinh khung = new KhungChinh(null, tenTrang);
            khung.setVisible(true);
        });
    }

    // Chay doc lap KhungChinh
    public static void main(String[] args) {
        xemTruoc("Trang chủ");
    }
}
