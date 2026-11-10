
package UI.Chung;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import UI.QuanTriVien.*;
import UI.QuanLy.*;
import UI.LeTan.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.swing.*;
import DAO.ThongKeDAO;
import UI.QuanLy.BaoCaoThongKePanel;

/** Màn hình demo đủ ba nhóm nghiệp vụ; chỉ dành cho tài khoản full. */
public final class FullGUI extends JFrame {
    private final TaiKhoan taiKhoan;
    private final TaiKhoanDAO dao;
    private final JPanel menu = new JPanel();
    private final JPanel noiDung = new JPanel(new BorderLayout());
    private final Map<String, JButton> cacNut = new LinkedHashMap<>();

    private QuanLyPanel quanLyNV, quanLyBN, quanLyDV;
    private JPanel khungNV, khungBN, khungDV;

    public FullGUI(TaiKhoan taiKhoan, TaiKhoanDAO dao) {
        super("MediDent - Trình bày toàn hệ thống");

        if (taiKhoan == null || dao == null
                || !"full".equals(taiKhoan.tenDangNhap)
                || !"Quản trị viên".equals(taiKhoan.tenVaiTro)
                || !"Hoạt động".equals(taiKhoan.trangThai)) {
            throw new IllegalArgumentException(
                    "Chỉ tài khoản demo full đã đăng nhập mới được mở màn hình này.");
        }

        this.taiKhoan = taiKhoan;
        this.dao = dao;

        setSize(1400, 850);
        setMinimumSize(new Dimension(1050, 650));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel chinh = new JPanel(new BorderLayout());
        chinh.setBackground(Color.WHITE);
        setContentPane(chinh);

        JButton dangXuat = GiaoDienHeThong.nut("Đăng xuất", false);
        dangXuat.addActionListener(e -> {
            dispose();
            new UI.DangNhap.DangNhap(dao).setVisible(true);
        });

        chinh.add(GiaoDienHeThong.thanhBen(menu, dangXuat), BorderLayout.WEST);

        JPanel benPhai = new JPanel(new BorderLayout());
        benPhai.setBackground(Color.WHITE);
        benPhai.add(GiaoDienHeThong.dauTrang(
                "Trình bày toàn hệ thống",
                taiKhoan.hoTen, "Quản trị viên - Demo"), BorderLayout.NORTH);

        noiDung.setBackground(Color.WHITE);
        benPhai.add(noiDung, BorderLayout.CENTER);
        chinh.add(benPhai, BorderLayout.CENTER);

        taoMenu();
        hienThi("admin_home", new TrangChuQuanTriVien(dao, taiKhoan));
    }

    // Tạo sidebar cho cả ba vai trò.
    private void taoMenu() {
        menu.removeAll();
        cacNut.clear();

        // Nhóm Quản trị viên.
        themNhom("QUẢN TRỊ VIÊN");
        themNut("admin_home", "Trang chủ Admin",
                () -> new TrangChuQuanTriVien(dao, taiKhoan));

        themNut("admin_tk", "Quản lý tài khoản",
                () -> new QuanLyTaiKhoan(dao, taiKhoan));

        themNut("admin_pq", "Phân quyền",
                () -> new QuanLyPhanQuyen(dao, taiKhoan));

        // Nhóm Quản lý phòng khám.
        themNhom("QUẢN LÝ PHÒNG KHÁM");
        themNut("ql_home", "Trang chủ quản lý",
                TrangChuQuanLyPanel::new);

        themNut("ql_nv", "Nhân viên", this::moNhanVien);
        themNut("ql_bn", "Bệnh nhân", this::moBenhNhan);
        themNut("ql_dv", "Dịch vụ", this::moDichVu);

        themNut("ql_llv", "Lịch làm việc",
                QuanLyLichLamViecPanel::new);

        themNut("ql_lh", "Lịch hẹn",
                QuanLyLichHenPanel::new);

        themNut("ql_hd", "Hóa đơn",
                QuanLyHoaDonPanel::new);

        themNut("ql_bc", "Thống kê",() -> new BaoCaoThongKePanel());
        // Nhóm Lễ tân.
        themNhom("LỄ TÂN");
        themNut("lt_home", "Trang chủ Lễ tân",
                () -> new TrangChuLeTan(taiKhoan));

        themNut("lt_tc", "Tra cứu bệnh nhân",
                () -> new TraCuuThongTin(taiKhoan));

        themNut("lt_lh", "Lịch hẹn - Tiếp nhận",
                () -> new QuanLyLichHenLeTan(taiKhoan));

        themNut("lt_kham", "Khám và điều trị",
                () -> new KhamBenhLeTan(taiKhoan));

        themNut("lt_tt", "Thanh toán - Hóa đơn",
                () -> new ThanhToanHoaDonLeTan(taiKhoan));

        // Nhóm chức năng chung.
        themNhom("CHỨC NĂNG CHUNG");
        themNut("help", "Hỗ trợ",
                () -> new HoTroPage(taiKhoan));

        menu.revalidate();
        menu.repaint();
    }

    // Hiển thị tên nhóm trong sidebar.
    private void themNhom(String tieuDe) {
        JLabel nhan = GiaoDienHeThong.label(tieuDe, 12, true);
        nhan.setForeground(GiaoDienHeThong.PRIMARY);
        nhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        nhan.setBorder(BorderFactory.createEmptyBorder(15, 5, 8, 0));
        menu.add(nhan);
    }

    // Tạo JButton dùng chung cho tất cả nhóm.
    private void themNut(String ma, String ten, Supplier<JPanel> nguonPanel) {
        JButton nut = GiaoDienHeThong.nutMenu(ten);
        nut.setAlignmentX(Component.LEFT_ALIGNMENT);

        nut.addActionListener(e -> {
            try {
                JPanel panel = nguonPanel.get();
                hienThi(ma, panel);
            } catch (Exception loi) {
                GiaoDienHeThong.loi(this, loi);
            }
        });

        cacNut.put(ma, nut);
        menu.add(nut);
        menu.add(Box.createVerticalStrut(6));
    }

    // Chuyển nội dung và đánh dấu menu đang chọn.
    private void hienThi(String ma, JPanel panel) {
        GiaoDienHeThong.dongBo(panel);

        noiDung.removeAll();
        noiDung.add(panel, BorderLayout.CENTER);
        noiDung.revalidate();
        noiDung.repaint();

        GiaoDienHeThong.chonMenu(cacNut, ma);
    }

    // Nhân viên: giữ luồng Danh sách -> Thêm/Sửa -> Danh sách.
    private JPanel moNhanVien() {
        if (quanLyNV == null) {
            khungNV = new JPanel(new BorderLayout());

            QuanLyNhanVienPanel ds = new QuanLyNhanVienPanel(
                () -> quanLyNV.show(
                        new ThemNhanVienPanel(quanLyNV::showList)),
                id -> quanLyNV.show(
                        new SuaNhanVienPanel(id, quanLyNV::showList))
            );

            quanLyNV = new QuanLyPanel(
                    khungNV, ds, ds::loadnhanvien);
        }

        quanLyNV.showList();
        return khungNV;
    }

    // Bệnh nhân: giữ luồng Danh sách -> Thêm/Sửa -> Danh sách.
    private JPanel moBenhNhan() {
        if (quanLyBN == null) {
            khungBN = new JPanel(new BorderLayout());

            QuanLyBenhNhanPanel ds = new QuanLyBenhNhanPanel(
                () -> quanLyBN.show(
                        new ThemBenhNhanPanel(quanLyBN::showList)),
                id -> quanLyBN.show(
                        new SuaBenhNhanPanel(id, quanLyBN::showList))
            );

            // Hàm loadbenhnhan có khai báo throws SQLException.
            Runnable taiLai = () -> {
                try {
                    ds.loadbenhnhan();
                } catch (SQLException loi) {
                    GiaoDienHeThong.loi(this, loi);
                }
            };

            quanLyBN = new QuanLyPanel(khungBN, ds, taiLai);
        }

        quanLyBN.showList();
        return khungBN;
    }

    // Dịch vụ: giữ luồng Danh sách -> Thêm/Sửa -> Danh sách.
    private JPanel moDichVu() {
        if (quanLyDV == null) {
            khungDV = new JPanel(new BorderLayout());

            QuanLyDichVuPanel ds = new QuanLyDichVuPanel(
                () -> quanLyDV.show(
                        new ThemDichVuPanel(quanLyDV::showList)),
                id -> quanLyDV.show(
                        new SuaDichVuPanel(id, quanLyDV::showList))
            );

            quanLyDV = new QuanLyPanel(
                    khungDV, ds, ds::loaddichvu);
        }

        quanLyDV.showList();
        return khungDV;
    }
}
