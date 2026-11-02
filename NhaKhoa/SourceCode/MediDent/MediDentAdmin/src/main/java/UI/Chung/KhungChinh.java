package UI.Chung;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.QuanLyDAO;
import UI.QuanLy.*;
import UI.LeTan.TrangChuLeTan;
import UI.LeTan.TrangThanhToanHoaDon;
import UI.QuanTriVien.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.*;

/**
 * CỬA SỔ DUY NHẤT của MEDIDENT sau đăng nhập.
 *
 * Tối ưu:
 * - Không truy vấn SQL trực tiếp trên Event Dispatch Thread (EDT).
 * - Tải quyền menu và kiểm tra quyền điều hướng bằng SwingWorker.
 * - Không truy vấn lại toàn bộ quyền menu sau mỗi lần chuyển trang.
 * - Mỗi lần mở trang vẫn kiểm tra trạng thái tài khoản/vai trò/quyền ở SQL.
 */
public final class KhungChinh extends JFrame {

    private static final Color ACTIVE = new Color(0xE0F2FE);
    private final TaiKhoan user;
    private final KhoDuLieu db;
    private final QuanLyDAO quanLyDAO = new QuanLyDAO();
    private final JPanel center = new JPanel(new BorderLayout());
    private final JPanel menu = new JPanel();
    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private final Map<String, Boolean> quyenMenu = new LinkedHashMap<>();
    private final List<Muc> danhMuc = new ArrayList<>();

    private JButton nutHoTro;
    private JButton nutDangXuat;
    private boolean dangTaiMenu;
    private boolean dangChuyenTrang;

    private static final class Muc {
        final String id, ten, quyen;
        final BieuTuong.Loai icon;
        final boolean chiAdmin;

        Muc(String id, String ten, String quyen,
            BieuTuong.Loai icon, boolean chiAdmin) {
            this.id = id;
            this.ten = ten;
            this.quyen = quyen;
            this.icon = icon;
            this.chiAdmin = chiAdmin;
        }
    }

    /** Kết quả kiểm tra truy cập được đọc ở luồng nền. */
    private static final class KetQuaTruyCap {
        final boolean taiKhoanHoatDong;
        final boolean laAdmin;
        final boolean duocTruyCap;
        final boolean coQuyenHoTro;

        KetQuaTruyCap(boolean taiKhoanHoatDong, boolean laAdmin,
                      boolean duocTruyCap, boolean coQuyenHoTro) {
            this.taiKhoanHoatDong = taiKhoanHoatDong;
            this.laAdmin = laAdmin;
            this.duocTruyCap = duocTruyCap;
            this.coQuyenHoTro = coQuyenHoTro;
        }
    }

    public KhungChinh(TaiKhoan user, KhoDuLieu db) {
        this.user = Objects.requireNonNull(user);
        this.db = Objects.requireNonNull(db);

        taoDanhMuc();
        setTitle("MEDIDENT - " + user.tenVaiTro);
        setSize(1220, 790);
        setMinimumSize(new Dimension(950, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GiaoDien.BG);
        setContentPane(root);
        root.add(taoSidebar(), BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.add(taoHeader(), BorderLayout.NORTH);
        right.add(center, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        // Hai tác vụ SQL chạy nền; không chặn EDT khi vừa đăng nhập.
        capNhatMenu();
        moTrang("HOME");
    }

    private void taoDanhMuc() {
        danhMuc.add(new Muc("HOME", "Trang chủ", null,
                BieuTuong.Loai.TRANG_CHU, false));
        // Admin chỉ có Trang chủ, Tài khoản, Phân quyền, Hệ thống,
        // Duyệt hủy, Báo cáo hệ thống và Hỗ trợ.
        danhMuc.add(new Muc("TAIKHOAN", "Tài khoản", "TAIKHOAN",
                BieuTuong.Loai.TAI_KHOAN, true));
        danhMuc.add(new Muc("PHANQUYEN", "Phân quyền", "TAIKHOAN",
                BieuTuong.Loai.PHAN_QUYEN, true));
        danhMuc.add(new Muc("HETHONG", "Hệ thống", "HETHONG",
                BieuTuong.Loai.HE_THONG, true));
        danhMuc.add(new Muc("DUYETHUY", "Duyệt hủy hóa đơn", "HOTRO",
                BieuTuong.Loai.KIEM_TRA, true));
        // Các chức năng vận hành phụ thuộc quyền do Admin cấp.
        danhMuc.add(new Muc("NHANVIEN", "Nhân viên", "NHANVIEN",
                BieuTuong.Loai.TAI_KHOAN, false));
        danhMuc.add(new Muc("LICHLAMVIEC", "Lịch làm việc", "NHANVIEN",
                BieuTuong.Loai.THONG_BAO, false));
        danhMuc.add(new Muc("DICHVU", "Dịch vụ nha khoa", "DICHVU",
                BieuTuong.Loai.HE_THONG, false));
        danhMuc.add(new Muc("BENHNHAN", "Bệnh nhân", "BENHNHAN",
                BieuTuong.Loai.TAI_KHOAN, false));
        danhMuc.add(new Muc("LICHHEN", "Lịch hẹn", "LICHHEN",
                BieuTuong.Loai.THONG_BAO, false));
        danhMuc.add(new Muc("THANHTOAN", "Thanh toán & hóa đơn", "THANHTOAN",
                BieuTuong.Loai.BAO_CAO, false));
        danhMuc.add(new Muc("BAOCAO", "Báo cáo - thống kê", "BAOCAO",
                BieuTuong.Loai.BAO_CAO, false));
    }

    private JPanel taoSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Color.WHITE);
        side.setBorder(new MatteBorder(0, 0, 0, 1, GiaoDien.BORDER));
        side.setPreferredSize(new Dimension(250, 0));

        JPanel brand = new JPanel(new BorderLayout(10, 0));
        brand.setBackground(Color.WHITE);
        brand.setBorder(new EmptyBorder(19, 15, 20, 9));
        brand.add(new JLabel(GiaoDien.logo(45, 45)), BorderLayout.WEST);
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.add(GiaoDien.label("MEDIDENT", 16, true));
        brandText.add(Box.createVerticalStrut(3));
        brandText.add(GiaoDien.label("Quản lý nha khoa", 10, false));
        brand.add(brandText, BorderLayout.CENTER);
        side.add(brand, BorderLayout.NORTH);

        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(17, 11, 14, 11));
        JLabel header = GiaoDien.label("CHỨC NĂNG HỆ THỐNG", 11, true);
        header.setForeground(GiaoDien.MUTED);
        menu.add(header);
        menu.add(Box.createVerticalStrut(15));

        for (Muc muc : danhMuc) {
            JButton button = GiaoDien.nutIcon(muc.ten, false, muc.icon);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(225, 44));
            button.setAlignmentX(LEFT_ALIGNMENT);
            button.setBorder(new EmptyBorder(11, 11, 11, 7));
            button.setVisible(false); // Chỉ hiện sau khi tải quyền thành công.
            button.setEnabled(false);
            button.addActionListener(e -> moTrang(muc.id));
            buttons.put(muc.id, button);
            menu.add(button);
            menu.add(Box.createVerticalStrut(5));
        }

        JScrollPane scroll = new JScrollPane(menu);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        side.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(10, 13, 15, 13));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        nutHoTro = GiaoDien.nutIcon("Cần hỗ trợ?  Gửi yêu cầu",
                false, BieuTuong.Loai.THONG_BAO);
        nutHoTro.setAlignmentX(LEFT_ALIGNMENT);
        nutHoTro.setMaximumSize(new Dimension(230, 45));
        nutHoTro.addActionListener(e -> moTrang("HOTRO"));
        bottom.add(nutHoTro);
        bottom.add(Box.createVerticalStrut(13));

        JLabel info = GiaoDien.label("Đăng nhập: " + user.hoTen, 11, true);
        info.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(info);
        JLabel role = GiaoDien.label(user.tenVaiTro, 10, false);
        role.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(role);
        bottom.add(Box.createVerticalStrut(10));

        nutDangXuat = GiaoDien.nutIcon("Đăng xuất", false, BieuTuong.Loai.DANG_XUAT);
        nutDangXuat.setAlignmentX(LEFT_ALIGNMENT);
        nutDangXuat.addActionListener(e -> {
            dispose();
            new UI.DangNhap.DangNhap(db).setVisible(true);
        });
        bottom.add(nutDangXuat);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    /** Header gồm 2 hàng tên và vai trò. */
    private JPanel taoHeader() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, GiaoDien.BORDER),
                new EmptyBorder(16, 23, 16, 25)));
        top.add(GiaoDien.label("MEDIDENT    /    Quản lý nha khoa", 13, true),
                BorderLayout.WEST);
        JPanel east = new JPanel(new GridLayout(2, 1, 0, 4));
        east.setOpaque(false);
        east.add(GiaoDien.label(user.hoTen, 12, true));
        east.add(GiaoDien.label(user.tenVaiTro, 11, false));
        top.add(east, BorderLayout.EAST);
        return top;
    }

    /** Admin và nhóm nghiệp vụ được tách biệt; HOME/BAOCAO dùng chung. */
    private boolean dungNhomVaiTro(Muc muc, boolean laAdmin) {
        if (muc.id.equals("HOME") || muc.id.equals("BAOCAO")) {
            return true;
        }
        return muc.chiAdmin == laAdmin;
    }

    /**
     * Tải quyền hiển thị menu ở luồng nền.
     * Mỗi lần dựng menu chỉ xác định vai trò Admin một lần; các quyền được kiểm
     * tra qua KhoDuLieu như trước và lỗi luôn được xử lý theo hướng từ chối quyền.
     */
    private void capNhatMenu() {
        if (dangTaiMenu) {
            return;
        }

        dangTaiMenu = true;
        capNhatTrangThaiNut();

        new SwingWorker<Map<String, Boolean>, Void>() {
            @Override
            protected Map<String, Boolean> doInBackground() throws Exception {
                boolean adminHienTai = db.laQuanTriVienTaiKhoan(user.id);
                Map<String, Boolean> ketQua = new LinkedHashMap<>();

                for (Muc muc : danhMuc) {
                    boolean allowed = dungNhomVaiTro(muc, adminHienTai);
                    if (allowed && muc.quyen != null) {
                        allowed = db.coQuyenTaiKhoan(user.id, muc.quyen);
                    }
                    ketQua.put(muc.id, allowed);
                }
                return ketQua;
            }

            @Override
            protected void done() {
                dangTaiMenu = false;
                try {
                    quyenMenu.clear();
                    quyenMenu.putAll(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    quyenMenu.clear();
                } catch (ExecutionException ex) {
                    // Không tải được quyền => ẩn toàn bộ mục, không cấp quyền mặc định.
                    quyenMenu.clear();
                }
                capNhatTrangThaiNut();
            }
        }.execute();
    }

    /** Cập nhật trạng thái nút theo quyền đã tải và trạng thái đang bận. */
    private void capNhatTrangThaiNut() {
        boolean menuSanSang = !dangTaiMenu && !dangChuyenTrang;
        for (Muc muc : danhMuc) {
            JButton button = buttons.get(muc.id);
            boolean allowed = quyenMenu.getOrDefault(muc.id, false);
            button.setVisible(allowed);
            button.setEnabled(allowed && menuSanSang);
        }

        if (nutHoTro != null) {
            nutHoTro.setEnabled(!dangChuyenTrang);
        }
        if (nutDangXuat != null) {
            nutDangXuat.setEnabled(!dangChuyenTrang);
        }
        setCursor(dangChuyenTrang
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private Muc timMuc(String page) {
        for (Muc muc : danhMuc) {
            if (muc.id.equals(page)) {
                return muc;
            }
        }
        return null;
    }

    /**
     * Kiểm tra tài khoản và quyền ở nền trước khi tạo màn hình.
     * Không gọi capNhatMenu() sau mỗi lần mở trang vì việc đó gây hàng loạt
     * truy vấn lặp không cần thiết. Quyền của trang đang mở vẫn luôn được xác minh.
     */
    private void moTrang(String page) {
        if (dangChuyenTrang) {
            return;
        }

        Muc muc = timMuc(page);
        if (muc == null && !"HOTRO".equals(page)) {
            GiaoDien.loi(this,
                    new IllegalArgumentException("Màn hình chưa hỗ trợ: " + page));
            return;
        }

        dangChuyenTrang = true;
        capNhatTrangThaiNut();

        new SwingWorker<KetQuaTruyCap, Void>() {
            @Override
            protected KetQuaTruyCap doInBackground() throws Exception {
                if (!db.taiKhoanDangHoatDong(user.id)) {
                    return new KetQuaTruyCap(false, false, false, false);
                }

                boolean currentAdmin = db.laQuanTriVienTaiKhoan(user.id);
                boolean allowed = muc == null
                        || (dungNhomVaiTro(muc, currentAdmin)
                        && (muc.quyen == null
                        || db.coQuyenTaiKhoan(user.id, muc.quyen)));

                if (!allowed) {
                    return new KetQuaTruyCap(true, currentAdmin, false, false);
                }

                // DUYETHUY đã qua kiểm tra quyền HOTRO ở trên, không truy vấn lần hai.
                boolean coQuyenHoTro = currentAdmin && "DUYETHUY".equals(page);
                if (currentAdmin && "HOTRO".equals(page)) {
                    coQuyenHoTro = db.coQuyenTaiKhoan(user.id, "HOTRO");
                }

                return new KetQuaTruyCap(true, currentAdmin, true, coQuyenHoTro);
            }

            @Override
            protected void done() {
                try {
                    KetQuaTruyCap access = get();

                    if (!access.taiKhoanHoatDong) {
                        JOptionPane.showMessageDialog(KhungChinh.this,
                                "Phiên đăng nhập không còn hiệu lực.");
                        dispose();
                        new UI.DangNhap.DangNhap(db).setVisible(true);
                        return;
                    }

                    if (!access.duocTruyCap) {
                        JOptionPane.showMessageDialog(KhungChinh.this,
                                "Không có quyền truy cập chức năng này.");
                        dangChuyenTrang = false;
                        capNhatMenu(); // Cập nhật lại nếu quyền vừa bị thay đổi trong SQL.
                        capNhatTrangThaiNut();
                        return;
                    }

                    JPanel view = taoTrang(page, access);
                    capNhatGiaoDienTrang(page, view);

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    GiaoDien.loi(KhungChinh.this, ex);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    GiaoDien.loi(KhungChinh.this,
                            cause instanceof Exception
                                    ? (Exception) cause : new Exception(cause));
                } catch (Exception ex) {
                    GiaoDien.loi(KhungChinh.this, ex);
                } finally {
                    // Nếu phiên bị đóng/đăng xuất, không cập nhật trạng thái frame cũ.
                    if (isDisplayable()) {
                        dangChuyenTrang = false;
                        capNhatTrangThaiNut();
                    }
                }
            }
        }.execute();
    }

    /** Chỉ tạo JPanel sau khi việc kiểm tra quyền đã hoàn tất. */
    private JPanel taoTrang(String page, KetQuaTruyCap access) {
        switch (page) {
            case "TAIKHOAN":
                return new QuanLyTaiKhoan(db, user);
            case "PHANQUYEN":
                return new QuanLyPhanQuyen(db, user);
            case "HETHONG":
                return new QuanTriHeThong(db, user);
            case "DUYETHUY":
                return new DuyetHuyPhieuThu(quanLyDAO, user,
                        access.coQuyenHoTro);
            case "NHANVIEN":
                return new NhanVienGUI(quanLyDAO, user);
            case "LICHLAMVIEC":
                return new LichLamViecGUI(quanLyDAO, user);
            case "DICHVU":
                return new DichVuGUI(quanLyDAO, user);
            case "BENHNHAN":
                return new BenhNhanGUI(quanLyDAO, user);
            case "LICHHEN":
                return new LichHenGUI(quanLyDAO, user);
            case "THANHTOAN":
                return taoTrangThanhToan();
            case "BAOCAO":
                return access.laAdmin
                        ? new BaoCaoHeThongAdmin(db, user)
                        : new BaoCaoThongKeGUI(user);
            case "HOTRO":
                return new HoTroPage(quanLyDAO, user, access.coQuyenHoTro);
            case "HOME":
                if (access.laAdmin) {
                    return new TrangChuQuanTriVien(db);
                }
                if ("Lễ tân".equalsIgnoreCase(user.tenVaiTro)) {
                    return new TrangChuLeTan(db, user);
                }
                if ("Quản lý".equalsIgnoreCase(user.tenVaiTro)
                        || "Quản lý phòng khám".equalsIgnoreCase(user.tenVaiTro)) {
                    return new nhanvienquanly_gui(db, user);
                }
                return new TrangChuTheoVaiTro(user);
            default:
                throw new IllegalArgumentException("Màn hình chưa hỗ trợ: " + page);
        }
    }

    /**
     * Mục "Thanh toán & hóa đơn": giữ nguyên màn hình thanh toán cũ (ThanhToanGUI) ở tab đầu,
     * thêm tab "In hóa đơn" (xem trước + in). Tab in hóa đơn chỉ tải dữ liệu khi được mở lần đầu.
     */
    private JPanel taoTrangThanhToan() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Thanh toán", new ThanhToanGUI(quanLyDAO, user));

        JPanel hoaDonHolder = new JPanel(new BorderLayout());
        tabs.addTab("In hóa đơn", hoaDonHolder);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == hoaDonHolder && hoaDonHolder.getComponentCount() == 0) {
                hoaDonHolder.add(new TrangThanhToanHoaDon(user), BorderLayout.CENTER);
                hoaDonHolder.revalidate();
                hoaDonHolder.repaint();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private void capNhatGiaoDienTrang(String page, JPanel view) {
        for (Muc muc : danhMuc) {
            JButton button = buttons.get(muc.id);
            boolean active = muc.id.equals(page);
            button.setBackground(active ? ACTIVE : Color.WHITE);
            button.setForeground(active ? GiaoDien.PRIMARY : GiaoDien.MUTED);
            button.setIcon(new BieuTuong(muc.icon,
                    active ? GiaoDien.PRIMARY : GiaoDien.MUTED, 17));
        }

        center.removeAll();
        center.add(view, BorderLayout.CENTER);
        center.revalidate();
        center.repaint();
    }
}
