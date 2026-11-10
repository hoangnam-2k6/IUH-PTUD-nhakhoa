
package UI.QuanTriVien;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import Entity.VaiTro;
import UI.Chung.GiaoDienHeThong;
import UI.Chung.NgayChon;
import validation.EmailHopLe;
import validation.MatKhauHopLe;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyTaiKhoan extends JPanel {
    private final TaiKhoanDAO db;
    private final TaiKhoan actor;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Tên đăng nhập", "Họ tên",
                "Email", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return false;
        }
    };

    private final JTable bang = new JTable(model) {
        @Override
        public Component prepareRenderer(
                javax.swing.table.TableCellRenderer renderer,
                int dong, int cot) {

            Component ketQua = super.prepareRenderer(renderer, dong, cot);

            if (cot == 0 && ketQua instanceof JLabel nhan) {
                nhan.setText(String.valueOf(dong + 1));
                nhan.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return ketQua;
        }
    };

    private final JTextField timKiem = GiaoDienHeThong.text("");
    private final JComboBox<String> locTrangThai = new JComboBox<>(
            new String[]{"Tất cả trạng thái", "Hoạt động", "Tạm khóa"});
    private final JComboBox<String> locVaiTro = new JComboBox<>();

    private List<TaiKhoan> danhSach = new ArrayList<>();
    private List<VaiTro> cacVaiTro = new ArrayList<>();
    private JScrollPane trangDanhSach;

    public QuanLyTaiKhoan(TaiKhoanDAO db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel trang = GiaoDienHeThong.trang(
                "Quản lý tài khoản",
                "Quản lý tài khoản, vai trò và trạng thái người dùng.");

        trangDanhSach = GiaoDienHeThong.cuon(trang);
        add(trangDanhSach, BorderLayout.CENTER);

        // Các nút sử dụng cùng một kiểu giao diện.
        JButton tim = GiaoDienHeThong.nut("Tìm", false);
        JButton them = GiaoDienHeThong.nut("Thêm tài khoản", false);
        JButton sua = GiaoDienHeThong.nut("Sửa", false);
        JButton xoa = GiaoDienHeThong.nut("Xóa", false);
        JButton reset = GiaoDienHeThong.nut("Reset mật khẩu", false);

        // Tất cả thanh công cụ nằm trên cùng một hàng.
        JPanel congCu = new JPanel(new GridBagLayout());
        congCu.setOpaque(false);
        congCu.setAlignmentX(Component.LEFT_ALIGNMENT);
        congCu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        timKiem.setPreferredSize(new Dimension(160, 32));
        locVaiTro.setPreferredSize(new Dimension(120, 32));
        locTrangThai.setPreferredSize(new Dimension(125, 32));

        Component[] cacThanhPhan = {
            timKiem, locVaiTro, locTrangThai,
            tim, them, sua, xoa, reset
        };

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < cacThanhPhan.length; i++) {
            c.gridx = i;
            c.weightx = i == 0 ? 1 : 0;
            c.insets = new Insets(0, 0, 0, 5);
            congCu.add(cacThanhPhan[i], c);
        }

        trang.add(congCu);
        trang.add(Box.createVerticalStrut(12));

        // Bảng tài khoản.
        JPanel khung = GiaoDienHeThong.card(new BorderLayout(0, 8));
        khung.setPreferredSize(new Dimension(900, 530));
        khung.add(GiaoDienHeThong.label(
                "Danh sách tài khoản", 14, true), BorderLayout.NORTH);
        khung.add(GiaoDienHeThong.wrapTable(bang), BorderLayout.CENTER);

        trang.add(khung);

        // Khóa các thao tác khi chưa chọn dòng.
        sua.setEnabled(false);
        xoa.setEnabled(false);
        reset.setEnabled(false);

        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean daChon = bang.getSelectedRow() >= 0;
                sua.setEnabled(daChon);
                xoa.setEnabled(daChon);
                reset.setEnabled(daChon);
            }
        });

        try {
            napVaiTro();
            taiLai();
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
        }

        // Gắn chức năng cho từng nút.
        tim.addActionListener(e -> taiLai());
        timKiem.addActionListener(e -> taiLai());
        locVaiTro.addActionListener(e -> taiLai());
        locTrangThai.addActionListener(e -> taiLai());

        them.addActionListener(e -> hienForm(null));
        sua.addActionListener(e -> suaTaiKhoan());
        xoa.addActionListener(e -> xoaTaiKhoan());
        reset.addActionListener(e -> datLaiMatKhau());

        // Nhấn đúp để sửa tài khoản.
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2
                        && SwingUtilities.isLeftMouseButton(e)) {
                    int dong = bang.rowAtPoint(e.getPoint());

                    if (dong >= 0) {
                        bang.setRowSelectionInterval(dong, dong);
                        suaTaiKhoan();
                    }
                }
            }
        });

        GiaoDienHeThong.dongBo(this);
    }

    // Lấy các vai trò từ DAO.
    private void napVaiTro() throws Exception {
        cacVaiTro = db.vaiTro();
        locVaiTro.removeAllItems();
        locVaiTro.addItem("Tất cả vai trò");

        for (VaiTro vt : cacVaiTro) {
            locVaiTro.addItem(vt.ten);
        }
    }

    // Tìm kiếm, lọc và tải danh sách tài khoản.
    private void taiLai() {
        try {
            int maVaiTro = locVaiTro.getSelectedIndex() <= 0
                    ? 0 : cacVaiTro.get(locVaiTro.getSelectedIndex() - 1).id;

            String trangThai = locTrangThai.getSelectedIndex() == 0
                    ? "" : (String) locTrangThai.getSelectedItem();

            danhSach = db.taiKhoan(
                    timKiem.getText().trim(), maVaiTro, trangThai, actor.id);

            model.setRowCount(0);
            int stt = 1;

            for (TaiKhoan tk : danhSach) {
                model.addRow(new Object[]{
                    stt++, tk.tenDangNhap, tk.hoTen,
                    tk.email, tk.tenVaiTro, tk.trangThai
                });
            }
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
        }
    }

    // Lấy tài khoản được chọn theo tên đăng nhập.
    private TaiKhoan taiKhoanDangChon() {
        int dong = bang.getSelectedRow();

        if (dong < 0) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một tài khoản trong bảng.");
            return null;
        }

        int dongModel = bang.convertRowIndexToModel(dong);
        String tenDangNhap = String.valueOf(
                model.getValueAt(dongModel, 1));

        for (TaiKhoan tk : danhSach) {
            if (tenDangNhap.equals(tk.tenDangNhap)) return tk;
        }

        JOptionPane.showMessageDialog(this,
                "Không tìm thấy tài khoản. Hãy tải lại danh sách.");
        return null;
    }

    // Sửa tài khoản đã chọn.
    private void suaTaiKhoan() {
        TaiKhoan tk = taiKhoanDangChon();
        if (tk != null) hienForm(tk);
    }

    // Tạo biểu mẫu Thêm hoặc Sửa tài khoản.
    private void hienForm(TaiKhoan cu) {
        boolean themMoi = cu == null;
        final int maDuKien;

        try {
            maDuKien = themMoi ? db.duKienMaTaiKhoan(actor.id) : cu.id;
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
            return;
        }

        String tenDangNhap = themMoi
                ? TaiKhoanDAO.tenDangNhapTheoMa(maDuKien)
                : cu.tenDangNhap;

        JTextField maTK = GiaoDienHeThong.text(String.valueOf(maDuKien));
        JTextField tenTK = GiaoDienHeThong.text(tenDangNhap);
        JTextField hoTen = GiaoDienHeThong.text(themMoi ? "" : cu.hoTen);
        JTextField email = GiaoDienHeThong.text(themMoi ? "" : cu.email);
        JPasswordField matKhau = themMoi ? new JPasswordField() : null;

        maTK.setEditable(false);
        tenTK.setEditable(false);
        maTK.setToolTipText(themMoi
                ? "Mã dự kiến, mã thật được cấp khi lưu."
                : "Mã tài khoản.");

        JLabel loiTen = nhanLoi();
        JLabel loiEmail = nhanLoi();
        JLabel loiMatKhau = nhanLoi();
        JLabel loiNgaySinh = nhanLoi();

        // Kiểm tra họ tên.
        Runnable kiemTraTen = () -> {
            String ten = hoTen.getText().trim();
            String loi = ten.isEmpty() ? "Vui lòng nhập họ tên."
                    : ten.length() > 120 ? "Họ tên tối đa 120 ký tự." : "";
            hienLoi(hoTen, loiTen, loi);
        };

        // Kiểm tra email.
        Runnable kiemTraEmail = () -> {
            String value = email.getText().trim();
            String loi = value.length() > 150 ? "Email tối đa 150 ký tự."
                    : EmailHopLe.kiemTra(value) ? ""
                    : "Email không hợp lệ.";
            hienLoi(email, loiEmail, loi);
        };

        // Kiểm tra mật khẩu khi thêm tài khoản.
        Runnable kiemTraMatKhau = () -> {
            if (!themMoi) return;

            String value = new String(matKhau.getPassword());
            String loi = MatKhauHopLe.kiemTra(value) ? ""
                    : "Mật khẩu 10-72 ký tự, có chữ hoa và số.";
            hienLoi(matKhau, loiMatKhau, loi);
        };

        // Chọn ngày sinh bằng lịch.
        NgayChon ngaySinh = new NgayChon();

        Runnable kiemTraNgaySinh = () -> {
            try {
                String loi = ngaySinh.getNgay() == null
                        ? "Vui lòng chọn ngày sinh." : "";
                hienLoi(ngaySinh, loiNgaySinh, loi);
            } catch (IllegalArgumentException e) {
                hienLoi(ngaySinh, loiNgaySinh, e.getMessage());
            }
        };

        batLoiKhiRoiO(hoTen, kiemTraTen);
        batLoiKhiRoiO(email, kiemTraEmail);
        if (themMoi) batLoiKhiRoiO(matKhau, kiemTraMatKhau);

        ngaySinh.addPropertyChangeListener("ngay",
                e -> kiemTraNgaySinh.run());
        ngaySinh.addPropertyChangeListener("roiNgayChon",
                e -> kiemTraNgaySinh.run());

        try {
            if (!themMoi) {
                ngaySinh.setNgay(db.ngaySinhNhanVien(cu.id, actor.id));
            }
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
            return;
        }

        JTextField ngayTao = themMoi ? null
                : GiaoDienHeThong.text(cu.ngayTao);

        if (ngayTao != null) ngayTao.setEditable(false);

        JComboBox<VaiTro> vaiTro = new JComboBox<>(
                cacVaiTro.toArray(new VaiTro[0]));
        JComboBox<String> trangThai = new JComboBox<>(
                new String[]{"Hoạt động", "Tạm khóa"});

        if (!themMoi) {
            for (VaiTro item : cacVaiTro) {
                if (item.id == cu.vaiTroId) {
                    vaiTro.setSelectedItem(item);
                    break;
                }
            }
            trangThai.setSelectedItem(cu.trangThai);
        }

        // Giao diện form hai cột.
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.setOpaque(false);

        form.add(GiaoDienHeThong.label("Mã tài khoản", 12, true));
        form.add(maTK);

        form.add(GiaoDienHeThong.label(
                "Tên đăng nhập (tự động)", 12, true));
        form.add(tenTK);

        if (!themMoi) {
            form.add(GiaoDienHeThong.label("Ngày tạo", 12, true));
            form.add(ngayTao);
        }

        form.add(GiaoDienHeThong.label("Họ và tên *", 12, true));
        form.add(oVaLoi(hoTen, loiTen));

        form.add(GiaoDienHeThong.label("Email *", 12, true));
        form.add(oVaLoi(email, loiEmail));

        form.add(GiaoDienHeThong.label("Ngày sinh *", 12, true));
        form.add(oVaLoi(ngaySinh, loiNgaySinh));

        if (themMoi) {
            form.add(GiaoDienHeThong.label("Mật khẩu *", 12, true));
            form.add(oVaLoi(matKhau, loiMatKhau));
        }

        form.add(GiaoDienHeThong.label("Vai trò", 12, true));
        form.add(vaiTro);

        form.add(GiaoDienHeThong.label("Trạng thái", 12, true));
        form.add(trangThai);

        JPanel trang = GiaoDienHeThong.trang(
                themMoi ? "Thêm tài khoản" : "Sửa tài khoản",
                themMoi ? "Nhập thông tin tài khoản mới."
                        : "Cập nhật tài khoản hiện có.");

        JPanel khungForm = GiaoDienHeThong.card(new BorderLayout());
        khungForm.add(form, BorderLayout.CENTER);

        trang.add(khungForm);
        trang.add(Box.createVerticalStrut(12));

        JPanel hangNut = GiaoDienHeThong.hang();
        JButton luu = GiaoDienHeThong.nut("Lưu thông tin", false);
        JButton quayLai = GiaoDienHeThong.nut("Quay lại", false);

        hangNut.add(luu);
        hangNut.add(quayLai);
        trang.add(hangNut);

        quayLai.addActionListener(e -> hienDanhSach());

        // Lưu tài khoản bằng hàm DAO đang sử dụng.
        luu.addActionListener(e -> {
            kiemTraTen.run();
            kiemTraEmail.run();
            kiemTraMatKhau.run();
            kiemTraNgaySinh.run();

            if (!loiTen.getText().isEmpty()
                    || !loiEmail.getText().isEmpty()
                    || !loiMatKhau.getText().isEmpty()
                    || !loiNgaySinh.getText().isEmpty()) {

                if (!loiTen.getText().isEmpty()) {
                    hoTen.requestFocusInWindow();
                } else if (!loiEmail.getText().isEmpty()) {
                    email.requestFocusInWindow();
                } else if (themMoi && !loiMatKhau.getText().isEmpty()) {
                    matKhau.requestFocusInWindow();
                } else {
                    ngaySinh.requestFocusInWindow();
                }
                return;
            }

            VaiTro vt = (VaiTro) vaiTro.getSelectedItem();

            if (vt == null) {
                JOptionPane.showMessageDialog(this, "Hãy chọn vai trò.");
                return;
            }

            try {
                int maMoi = db.luuTaiKhoanTraVeMa(
                        themMoi ? 0 : cu.id,
                        hoTen.getText().trim(),
                        email.getText().trim(),
                        vt.id,
                        (String) trangThai.getSelectedItem(),
                        themMoi ? new String(matKhau.getPassword()) : null,
                        ngaySinh.getNgay(), actor.id);

                taiLai();
                hienDanhSach();

                JOptionPane.showMessageDialog(this, themMoi
                        ? "Đã thêm tài khoản "
                            + TaiKhoanDAO.tenDangNhapTheoMa(maMoi)
                        : "Đã cập nhật thông tin tài khoản.");

            } catch (IllegalArgumentException loi) {
                hienLoi(ngaySinh, loiNgaySinh, loi.getMessage());
            } catch (Exception loi) {
                GiaoDienHeThong.loi(this, loi);
            }
        });

        GiaoDienHeThong.dongBo(trang);
        removeAll();
        add(GiaoDienHeThong.cuon(trang), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Trở lại trang danh sách.
    private void hienDanhSach() {
        removeAll();
        add(trangDanhSach, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Đặt lại mật khẩu thông qua DAO.
    private void datLaiMatKhau() {
        TaiKhoan tk = taiKhoanDangChon();
        if (tk == null) return;

        int chon = JOptionPane.showConfirmDialog(this,
                "Bạn đã xác minh danh tính của " + tk.hoTen + "?\n"
                + "Đặt lại mật khẩu cho " + tk.tenDangNhap + "?\n"
                + "Người dùng sẽ phải đổi mật khẩu khi đăng nhập.",
                "Xác nhận đặt lại mật khẩu",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (chon != JOptionPane.YES_OPTION) return;

        try {
            String matKhauTam = db.datLaiMatKhauTheoNgaySinh(
                    tk.id, actor.id);

            JTextField oHienMatKhau = GiaoDienHeThong.text(matKhauTam);
            oHienMatKhau.setEditable(false);

            JPanel thongBao = new JPanel(new GridLayout(0, 1, 5, 5));
            thongBao.add(GiaoDienHeThong.label(
                    "Mật khẩu tạm:", 12, true));
            thongBao.add(oHienMatKhau);
            thongBao.add(GiaoDienHeThong.label(
                    "Chỉ cung cấp cho người đã xác minh danh tính.",
                    11, false));

            JOptionPane.showMessageDialog(this, thongBao,
                    "Đặt lại mật khẩu thành công",
                    JOptionPane.INFORMATION_MESSAGE);

            oHienMatKhau.setText("");
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
        }
    }

    // Xóa tài khoản bằng ID thật.
    private void xoaTaiKhoan() {
        TaiKhoan tk = taiKhoanDangChon();
        if (tk == null) return;

        int chon = JOptionPane.showConfirmDialog(this,
                "Xóa tài khoản " + tk.hoTen + "?\n"
                + "Nếu đã phát sinh nghiệp vụ, hệ thống có thể từ chối xóa.\n"
                + "Bạn nên sử dụng Tạm khóa để bảo toàn lịch sử.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);

        if (chon != JOptionPane.YES_OPTION) return;

        try {
            db.xoaTK(tk.id, actor.id);
            taiLai();
        } catch (Exception loi) {
            GiaoDienHeThong.loi(this, loi);
        }
    }

    // Hiển thị lỗi validation.
    private static JLabel nhanLoi() {
        JLabel nhan = new JLabel("");
        nhan.setFont(new Font("Arial", Font.PLAIN, 10));
        nhan.setForeground(new Color(191, 59, 59));
        return nhan;
    }

    private static JPanel oVaLoi(JComponent oNhap, JLabel nhanLoi) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(oNhap, BorderLayout.CENTER);
        p.add(nhanLoi, BorderLayout.SOUTH);
        return p;
    }

    private static void hienLoi(JComponent oNhap, JLabel nhan, String loi) {
        nhan.setText(loi);
        oNhap.setToolTipText(loi.isEmpty() ? null : loi);
    }

    // Kiểm tra ô nhập khi mất focus hoặc nhấn Enter.
    private static void batLoiKhiRoiO(JTextField oNhap, Runnable kiemTra) {
        oNhap.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                kiemTra.run();
            }
        });

        oNhap.addActionListener(e -> kiemTra.run());
    }
}
