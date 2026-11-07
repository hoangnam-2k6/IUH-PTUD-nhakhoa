
package UI.DangNhap;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.XacThucService;
import UI.Chung.GiaoDien;
import UI.QuanLy.KhungChinh;

public final class DangNhap extends JFrame {

    private final XacThucService xacThuc = new XacThucService();

    private final JTextField tenDangNhap = new JTextField(24);
    private final JPasswordField matKhauDangNhap = new JPasswordField(24);
    private final JTextField tenDangKy = new JTextField(24);
    private final JTextField hoTen = new JTextField(24);
    private final JTextField soDienThoai = new JTextField(24);
    private final JTextField diaChi = new JTextField(24);
    private final JTextField ngaySinh = new JTextField(24);
    private final JTextField maMoi = new JTextField(24);
    private final JComboBox<String> gioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
    private final JPasswordField matKhauDangKy = new JPasswordField(24);
    private final JPasswordField xacNhanMatKhau = new JPasswordField(24);

    private final CardLayout boChuyenTrang = new CardLayout();
    private final JPanel vungNoiDung = new JPanel(boChuyenTrang);
    private final JLabel nhanTrangThai = GiaoDien.label(" ", 12, false);

    public DangNhap() {
        super("MEDIDENT – Đăng nhập / Đăng ký Quản lý");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 740);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(GiaoDien.BG);

        JPanel vungAnh = taoAnhPhongKham();
        vungAnh.setPreferredSize(new Dimension(410, 0));
        vungAnh.setBorder(new EmptyBorder(50, 30, 30, 30));

        JLabel nhanLogo = new JLabel(GiaoDien.logo(120));
        nhanLogo.setHorizontalAlignment(SwingConstants.CENTER);
        vungAnh.add(nhanLogo, BorderLayout.NORTH);

        JLabel khauHieu = new JLabel("<html><center>MEDIDENT<br><br>Quản lý phòng khám"
                + "<br><small>Một hệ thống · Một cơ sở dữ liệu</small></center></html>");
        khauHieu.setForeground(Color.WHITE);
        khauHieu.setHorizontalAlignment(SwingConstants.CENTER);
        khauHieu.setFont(new Font("Segoe UI", Font.BOLD, 26));
        vungAnh.add(khauHieu, BorderLayout.CENTER);
        add(vungAnh, BorderLayout.WEST);

        JPanel khungBieuMau = new JPanel(new BorderLayout());
        khungBieuMau.setBackground(GiaoDien.WHITE);
        khungBieuMau.setBorder(new EmptyBorder(25, 36, 20, 36));
        khungBieuMau.add(GiaoDien.label("Chào mừng đến MediDent", 25, true), BorderLayout.NORTH);

        vungNoiDung.setBackground(GiaoDien.WHITE);
        vungNoiDung.add(taoTrangDangNhap(), "login");

        JScrollPane cuonDangKy = new JScrollPane(taoTrangDangKy());
        cuonDangKy.setBorder(null);
        cuonDangKy.getVerticalScrollBar().setUnitIncrement(18);
        cuonDangKy.getViewport().setBackground(Color.WHITE);
        vungNoiDung.add(cuonDangKy, "register");

        khungBieuMau.add(vungNoiDung, BorderLayout.CENTER);
        khungBieuMau.add(nhanTrangThai, BorderLayout.SOUTH);
        add(khungBieuMau, BorderLayout.CENTER);
    }

    // Tao anh nen phong kham
    private JPanel taoAnhPhongKham() {
        return new JPanel(new BorderLayout()) {
            private java.awt.Image anhNen;
            {
                java.net.URL duongDanAnh = DangNhap.class.getResource("/Image/phong_kham.jpg");
                if (duongDanAnh != null) anhNen = new ImageIcon(duongDanAnh).getImage();
                setBackground(GiaoDien.BLUE_DARK);
            }

            @Override
            protected void paintComponent(Graphics doHoa) {
                super.paintComponent(doHoa);
                if (anhNen != null) {
                    Graphics2D doHoaSao = (Graphics2D) doHoa.create();
                    doHoaSao.drawImage(anhNen, 0, 0, getWidth(), getHeight(), this);
                    doHoaSao.setColor(new Color(2, 90, 150, 180));
                    doHoaSao.fillRect(0, 0, getWidth(), getHeight());
                    doHoaSao.dispose();
                }
            }
        };
    }

    // Tao giao dien dang nhap
    private JPanel taoTrangDangNhap() {
        JPanel khungNgoai = new JPanel(new GridBagLayout());
        khungNgoai.setBackground(Color.WHITE);
        JPanel khungNhap = taoKhungNhapDoc();

        themTruongNhap(khungNhap, "Tên đăng nhập", tenDangNhap);
        themTruongNhap(khungNhap, "Mật khẩu", matKhauDangNhap);

        JButton nutDangNhap = GiaoDien.button("Đăng nhập Quản lý", true);
        nutDangNhap.addActionListener(e -> xuLyDangNhap());
        themNut(khungNhap, nutDangNhap);
        khungNhap.add(Box.createVerticalStrut(15));

        JButton nutDangKy = GiaoDien.button("Đăng ký tài khoản Quản lý", false);
        nutDangKy.addActionListener(e -> {
            matKhauDangKy.setText("");
            boChuyenTrang.show(vungNoiDung, "register");
        });
        themNut(khungNhap, nutDangKy);
        khungNgoai.add(khungNhap);
        return khungNgoai;
    }

    // Tao giao dien dang ky
    private JPanel taoTrangDangKy() {
        JPanel khungNgoai = new JPanel(new GridBagLayout());
        khungNgoai.setBackground(Color.WHITE);
        JPanel khungNhap = taoKhungNhapDoc();

        themTruongNhap(khungNhap, "Tên đăng nhập (ít nhất 4 ký tự)", tenDangKy);
        themTruongNhap(khungNhap, "Họ tên quản lý", hoTen);
        themTruongNhap(khungNhap, "Số điện thoại", soDienThoai);
        themTruongNhap(khungNhap, "Địa chỉ", diaChi);
        themTruongNhap(khungNhap, "Ngày sinh (yyyy-MM-dd)", ngaySinh);
        themTruongNhap(khungNhap, "Giới tính", gioiTinh);
        themTruongNhap(khungNhap, "Mật khẩu mạnh", matKhauDangKy);
        themTruongNhap(khungNhap, "Nhập lại mật khẩu", xacNhanMatKhau);
        themTruongNhap(khungNhap, "Mã mời (theo cấu hình hoặc đăng ký quản lý đầu tiên)", maMoi);

        JButton nutLuu = GiaoDien.button("Tạo tài khoản trong SQL", true);
        nutLuu.addActionListener(e -> xuLyDangKy());
        themNut(khungNhap, nutLuu);
        khungNhap.add(Box.createVerticalStrut(12));

        JButton nutQuayLai = GiaoDien.button("Trở về đăng nhập", false);
        nutQuayLai.addActionListener(e -> boChuyenTrang.show(vungNoiDung, "login"));
        themNut(khungNhap, nutQuayLai);

        khungNgoai.add(khungNhap);
        return khungNgoai;
    }

    // Tao JPanel chua cac truong nhap theo chieu doc
    private JPanel taoKhungNhapDoc() {
        JPanel khungNhap = new JPanel();
        khungNhap.setLayout(new BoxLayout(khungNhap, BoxLayout.Y_AXIS));
        khungNhap.setBackground(Color.WHITE);
        return khungNhap;
    }

    // Them JLabel va o nhap vao bieu mau
    private void themTruongNhap(JPanel khungNhap, String tieuDe, JComponent oNhap) {
        JLabel nhan = GiaoDien.label(tieuDe, 12, true);
        nhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        GiaoDien.styleInput(oNhap);
        oNhap.setMaximumSize(new Dimension(335, 42));
        oNhap.setPreferredSize(new Dimension(335, 42));
        oNhap.setAlignmentX(Component.LEFT_ALIGNMENT);

        khungNhap.add(nhan);
        khungNhap.add(Box.createVerticalStrut(5));
        khungNhap.add(oNhap);
        khungNhap.add(Box.createVerticalStrut(11));
    }

    // Them JButton vao bieu mau
    private void themNut(JPanel khungNhap, JButton nut) {
        nut.setAlignmentX(Component.LEFT_ALIGNMENT);
        nut.setMaximumSize(new Dimension(335, 44));
        nut.setPreferredSize(new Dimension(335, 44));
        khungNhap.add(nut);
    }

    // Xu ly tac vu SQL tren luong rieng
    private void xuLyCoSoDuLieu(Runnable congViec) {
        nhanTrangThai.setText("Đang xử lý SQL Server...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                congViec.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    nhanTrangThai.setText("Kết nối SQL thành công");
                } catch (Exception loi) {
                    GiaoDien.error(DangNhap.this, loi);
                }
            }
        }.execute();
    }

    // Xu ly dang nhap
    private void xuLyDangNhap() {
        String tenTaiKhoan = tenDangNhap.getText().trim();
        String matKhau = new String(matKhauDangNhap.getPassword());

        if (tenTaiKhoan.isBlank() || matKhau.isEmpty()) {
            GiaoDien.ok(this, "Nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        xuLyCoSoDuLieu(() -> {
            PhanQuyen.PhienDangNhap phienDangNhap = xacThuc.login(tenTaiKhoan, matKhau);
            SwingUtilities.invokeLater(() -> {
                dispose();
                new KhungChinh(phienDangNhap).setVisible(true);
            });
        });
    }

    // Xu ly dang ky
    private void xuLyDangKy() {
        String tenTaiKhoan = tenDangKy.getText().trim();
        String hoTenNhap = hoTen.getText().trim();
        String soDienThoaiNhap = soDienThoai.getText().trim();
        String diaChiNhap = diaChi.getText().trim();
        String ngaySinhNhap = ngaySinh.getText().trim();
        String gioiTinhNhap = String.valueOf(gioiTinh.getSelectedItem());
        String matKhau = new String(matKhauDangKy.getPassword());
        String matKhauXacNhan = new String(xacNhanMatKhau.getPassword());
        String maMoiNhap = maMoi.getText().trim();

        xuLyCoSoDuLieu(() -> {
            xacThuc.register(tenTaiKhoan, matKhau, matKhauXacNhan, hoTenNhap,
                    soDienThoaiNhap, diaChiNhap, ngaySinhNhap, gioiTinhNhap, maMoiNhap);

            SwingUtilities.invokeLater(() -> {
                matKhauDangKy.setText("");
                xacNhanMatKhau.setText("");
                tenDangNhap.setText(tenTaiKhoan);
                boChuyenTrang.show(vungNoiDung, "login");
                GiaoDien.ok(this, "Đăng ký thành công. Đăng nhập bằng tài khoản mới.");
            });
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
    }
}
