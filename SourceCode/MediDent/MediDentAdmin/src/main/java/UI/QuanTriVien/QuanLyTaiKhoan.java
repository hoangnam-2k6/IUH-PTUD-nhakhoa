package UI.QuanTriVien;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.Box;
import javax.swing.BoxLayout;
import UI.Chung.BieuTuong;

import Class.TaiKhoan;
import Class.VaiTro;
import TienIch.EmailHopLe;
import TienIch.MatKhauHopLe;
import DAO.TaiKhoanDAO;
import UI.Chung.BieuTuong;

import UI.Chung.NgayChon;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Quản lý tài khoản: lọc, thêm, sửa, xóa dữ liệu SQL Server.
 *
 * Cột đầu tiên là STT từ 1 đến hết thay vì hiển thị ID database.
 * Khi thao tác sửa/xóa, chương trình dùng ID nội bộ của TaiKhoan.
 */
public class QuanLyTaiKhoan extends JPanel {

    private final TaiKhoanDAO db;
    private final TaiKhoan actor;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Tên đăng nhập", "Họ tên", "Email", "Vai trò", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model) {
        @Override
        public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                         int row, int column) {
            Component result = super.prepareRenderer(renderer, row, column);
            if (column == 0 && result instanceof javax.swing.JLabel) {
                ((javax.swing.JLabel) result).setText(String.valueOf(row + 1));
                ((javax.swing.JLabel) result).setHorizontalAlignment(
                        javax.swing.SwingConstants.CENTER);
            }
            return result;
        }
    };
    private final JTextField search = GiaoDienQuanLyTaiKhoan.text("");
    private final JComboBox<String> status = new JComboBox<>(new String[]{
        "Tất cả trạng thái", "Hoạt động", "Tạm khóa"
    });
    private final JComboBox<String> role = new JComboBox<>();

    // Dữ liệu thật tương ứng với mỗi hàng MODEL (khác chỉ số hàng đang sắp xếp).
    private List<TaiKhoan> visibleAccounts = new ArrayList<>();
    private List<VaiTro> roles = new ArrayList<>();

    public QuanLyTaiKhoan(TaiKhoanDAO db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDienQuanLyTaiKhoan.BG);

        JPanel page = GiaoDienQuanLyTaiKhoan.trang("Quản lý tài khoản",
                "Quản lý người dùng, vai trò và trạng thái truy cập hệ thống.");
        add(GiaoDienQuanLyTaiKhoan.cuon(page), BorderLayout.CENTER);

        // 1. Thanh tìm kiếm và lọc co giãn theo chiều rộng cửa sổ.
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        search.setPreferredSize(new Dimension(190, 37));
        search.setMinimumSize(new Dimension(90, 37));
        role.setPreferredSize(new Dimension(148, 37));
        status.setPreferredSize(new Dimension(135, 37));

        JButton find = GiaoDienQuanLyTaiKhoan.nutIcon("Tìm kiếm", false, BieuTuong.Loai.TIM_KIEM);
        JButton add = GiaoDienQuanLyTaiKhoan.nutIcon("Thêm tài khoản", true, BieuTuong.Loai.THEM);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 0, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.weightx = 1;
        filters.add(search, constraints);
        constraints.weightx = 0;
        constraints.gridx = 1;
        filters.add(role, constraints);
        constraints.gridx = 2;
        filters.add(status, constraints);
        constraints.gridx = 3;
        filters.add(find, constraints);
        constraints.gridx = 4;
        constraints.insets = new Insets(0, 0, 0, 0);
        filters.add(add, constraints);
        page.add(filters);
        page.add(Box.createVerticalStrut(16));

        // 2. Bảng dữ liệu; STT luôn từ 1 đến số dòng sau khi lọc.
        JPanel card = GiaoDienQuanLyTaiKhoan.card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(700, 550));

        // Đặt các nút ở ĐẦU bảng, luôn dễ nhìn thấy thay vì cuối bảng cao 550px.
        JButton edit = GiaoDienQuanLyTaiKhoan.nutIcon("Sửa thông tin", true, BieuTuong.Loai.SUA);
        JButton remove = GiaoDienQuanLyTaiKhoan.nutIcon("Xóa tài khoản", false, BieuTuong.Loai.XOA);
        JButton reset = GiaoDienQuanLyTaiKhoan.nutIcon("Quên mật khẩu / Reset", false,
                BieuTuong.Loai.TAI_LAI);

        JPanel tools = GiaoDienQuanLyTaiKhoan.hang();
        tools.add(edit);
        tools.add(remove);
        tools.add(reset);

        JLabel title = GiaoDienQuanLyTaiKhoan.label("Danh sách tài khoản", 14, true);
        JPanel tableHeader = new JPanel(new BorderLayout(12, 0));
        tableHeader.setOpaque(false);
        tableHeader.add(title, BorderLayout.WEST);
        tableHeader.add(tools, BorderLayout.EAST);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(GiaoDienQuanLyTaiKhoan.wrapTable(table), BorderLayout.CENTER);
        page.add(card);

        // Chỉ cho thao tác sửa/xóa/reset sau khi người dùng chọn một tài khoản.
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edit.setEnabled(false);
        remove.setEnabled(false);
        reset.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                boolean selected = table.getSelectedRow() >= 0;
                edit.setEnabled(selected);
                remove.setEnabled(selected);
                reset.setEnabled(selected);
            }
        });

        // 3. Chỉ đọc các vai trò từ CSDL, không bắt người dùng tự điền vai trò.
        try {
            napVaiTro();
            taiLai();
        } catch (Exception exception) {
            GiaoDienQuanLyTaiKhoan.loi(this, exception);
        }
        find.addActionListener(event -> taiLai());
        search.addActionListener(event -> taiLai());
        status.addActionListener(event -> taiLai());
        role.addActionListener(event -> taiLai());
        add.addActionListener(event -> hienForm(null));
        // Nút Sửa và nhấp đúp vào dòng đều dùng chung một hàm sửa.
        edit.addActionListener(event -> suaThongTinTaiKhoan());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2
                        && javax.swing.SwingUtilities.isLeftMouseButton(event)) {
                    int row = table.rowAtPoint(event.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        suaThongTinTaiKhoan();
                    }
                }
            }
        });
        remove.addActionListener(event -> xoa());
        reset.addActionListener(event -> datLaiMatKhau());
    }

    private void napVaiTro() throws Exception {
        roles = db.vaiTro();
        role.removeAllItems();
        role.addItem("Tất cả vai trò");
        for (VaiTro item : roles) {
            role.addItem(item.ten);
        }
    }

    /** 4. Đọc lại dữ liệu và đánh STT hiển thị từ 1. */
    private void taiLai() {
        try {
            int roleId = role.getSelectedIndex() <= 0
                    ? 0 : roles.get(role.getSelectedIndex() - 1).id;
            String currentStatus = status.getSelectedIndex() == 0
                    ? "" : (String) status.getSelectedItem();
            visibleAccounts = db.taiKhoan(search.getText().trim(), roleId, currentStatus);
            model.setRowCount(0);
            int number = 1;
            for (TaiKhoan account : visibleAccounts) {
                model.addRow(new Object[]{
                    number++, account.tenDangNhap, account.hoTen,
                    account.email, account.tenVaiTro, account.trangThai
                });
            }
        } catch (Exception exception) {
            GiaoDienQuanLyTaiKhoan.loi(this, exception);
        }
    }

    /** 5. Đổi hàng đang xem về hàng gốc, lấy ID từ đối tượng lưu riêng. */
    private TaiKhoan taiKhoanDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một tài khoản trong bảng.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String tenDangNhap = String.valueOf(model.getValueAt(modelRow, 1));
        for (TaiKhoan account : visibleAccounts) {
            if (tenDangNhap.equals(account.tenDangNhap)) return account;
        }
        JOptionPane.showMessageDialog(this, "Tài khoản đã thay đổi, hãy tải lại.");
        return null;
    }

    /**
     * Sửa tài khoản được chọn. Hàm này dùng chung cho nút Sửa và nhấp đúp.
     * Form sẽ tải thông tin đã lưu rồi gọi db.luuTaiKhoan() khi bấm OK.
     */
    private void suaThongTinTaiKhoan() {
        TaiKhoan account = taiKhoanDangChon();
        if (account == null) {
            return;
        }
        hienForm(account);
    }

    /**
     * 6. Form thêm/sửa tài khoản.
     *
     * Ngay khi mở form Thêm, đọc IDENTITY kế tiếp từ SQL để XEM TRƯỚC
     * cả mã tài khoản và tên đăng nhập. Không cho người dùng tự sửa hai ô.
     *
     * Ngày sinh dùng lịch chọn; các ngày không tồn tại, tương lai, quá 100
     * tuổi đều bị khóa. Khi lưu, SQL vẫn tự sinh mã thật để tránh trùng ID.
     */
    private void hienForm(TaiKhoan account) {
        boolean creating = account == null;

        // Lấy MÃ DỰ KIẾN theo bộ đếm IDENTITY, không phải MAX(maTK)+1.
        // Trường hợp đồng thời có người khác thêm, mã thực tế có thể thay đổi.
        final int maDuKien;
        try {
            maDuKien = creating ? db.duKienMaTaiKhoan(actor.id) : account.id;
        } catch (Exception ex) {
            GiaoDienQuanLyTaiKhoan.loi(this, ex);
            return;
        }
        String tenDangNhapDuKien = creating
                ? TaiKhoanDAO.tenDangNhapTheoMa(maDuKien)
                : account.tenDangNhap;

        JTextField idTuDong = GiaoDienQuanLyTaiKhoan.text(String.valueOf(maDuKien));
        idTuDong.setEditable(false);
        idTuDong.setToolTipText(creating ? "Mã dự kiến"
                : "Mã tài khoản chính thức");

        JTextField username = GiaoDienQuanLyTaiKhoan.text(tenDangNhapDuKien);
        username.setEditable(false);
        username.setToolTipText("Tên đăng nhập hệ thống tự tạo theo mã tài khoản");

        JTextField name = GiaoDienQuanLyTaiKhoan.text(creating ? "" : account.hoTen);
        JTextField email = GiaoDienQuanLyTaiKhoan.text(creating ? "" : account.email);
        JPasswordField password = new JPasswordField();
        JLabel loiTen = nhanLoi();
        JLabel loiEmail = nhanLoi();
        JLabel loiMatKhau = nhanLoi();
        JLabel loiNgaySinh = nhanLoi();
        Runnable kiemTraTen = () -> hienLoi(name, loiTen,
                name.getText().trim().isEmpty() ? "Vui lòng nhập họ tên."
                        : name.getText().trim().length() > 120 ? "Họ tên tối đa 120 ký tự." : "");
        Runnable kiemTraEmail = () -> hienLoi(email, loiEmail,
                email.getText().trim().length() > 150 ? "Email tối đa 150 ký tự."
                        : EmailHopLe.kiemTra(email.getText()) ? "" : "Email không hợp lệ (ví dụ: ten@gmail.com).");
        Runnable kiemTraMatKhau = () -> {
            String value = new String(password.getPassword());
            hienLoi(password, loiMatKhau, (!creating && value.isEmpty())
                    || MatKhauHopLe.kiemTra(value) ? "" : "Mật khẩu 10–72 ký tự, có chữ hoa và số.");
        };

        // Calendar thực: chỉ chọn ngày đã có trong tháng/năm hợp lệ.
        NgayChon ngaySinh = new NgayChon();
        Runnable kiemTraNgaySinh = () -> {
            try {
                hienLoi(ngaySinh, loiNgaySinh,
                        ngaySinh.getNgay() == null ? "Vui lòng chọn ngày sinh." : "");
            } catch (IllegalArgumentException ex) {
                hienLoi(ngaySinh, loiNgaySinh, ex.getMessage());
            }
        };
        batLoiKhiRoiO(name, kiemTraTen);
        batLoiKhiRoiO(email, kiemTraEmail);
        batLoiKhiRoiO(password, kiemTraMatKhau);
        ngaySinh.addPropertyChangeListener("ngay", event -> kiemTraNgaySinh.run());
        ngaySinh.addPropertyChangeListener("roiNgayChon", event -> kiemTraNgaySinh.run());
        try {
            if (!creating) {
                ngaySinh.setNgay(db.ngaySinhNhanVien(account.id, actor.id));
            }
        } catch (Exception ex) {
            GiaoDienQuanLyTaiKhoan.loi(this, ex);
            return;
        }

        JTextField ngayTao = GiaoDienQuanLyTaiKhoan.text(creating
                ? "Tự động" : account.ngayTao);
        ngayTao.setEditable(false);

        JComboBox<VaiTro> roleBox = new JComboBox<>(roles.toArray(new VaiTro[0]));
        JComboBox<String> statusBox = new JComboBox<>(
                new String[]{"Hoạt động", "Tạm khóa"});
        if (!creating) {
            for (VaiTro item : roles) {
                if (item.id == account.vaiTroId) {
                    roleBox.setSelectedItem(item);
                    break;
                }
            }
            statusBox.setSelectedItem(account.trangThai);
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 12));
        form.add(GiaoDienQuanLyTaiKhoan.label(creating ? "Mã tài khoản (dự kiến)" : "Mã tài khoản", 12, true));
        form.add(idTuDong);
        form.add(GiaoDienQuanLyTaiKhoan.label("Tên đăng nhập (tự động)", 12, true));
        form.add(username);
        form.add(GiaoDienQuanLyTaiKhoan.label("Ngày tạo (tự động)", 12, true));
        form.add(ngayTao);
        form.add(GiaoDienQuanLyTaiKhoan.label("Họ và tên *", 12, true));
        form.add(oVaLoi(name, loiTen));
        form.add(GiaoDienQuanLyTaiKhoan.label("Email *", 12, true));
        form.add(oVaLoi(email, loiEmail));
        form.add(GiaoDienQuanLyTaiKhoan.label("Ngày sinh (chọn từ lịch) *", 12, true));
        form.add(oVaLoi(ngaySinh, loiNgaySinh));
        form.add(GiaoDienQuanLyTaiKhoan.label(creating ? "Mật khẩu *" : "Mật khẩu mới (trống: giữ nguyên)",
                12, true));
        form.add(oVaLoi(password, loiMatKhau));
        form.add(GiaoDienQuanLyTaiKhoan.label("Vai trò", 12, true));
        form.add(roleBox);
        form.add(GiaoDienQuanLyTaiKhoan.label("Trạng thái", 12, true));
        form.add(statusBox);

        JPanel formChinh = new JPanel(new BorderLayout(0, 12));
        formChinh.add(form, BorderLayout.CENTER);
        // Click vào khoảng trắng cũng làm ô hiện tại mất focus và kiểm tra ngay.
        formChinh.setFocusable(true);
        form.setFocusable(true);
        java.awt.event.MouseAdapter boFocus = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent event) {
                ((java.awt.Component) event.getSource()).requestFocusInWindow();
            }
        };
        formChinh.addMouseListener(boFocus);
        form.addMouseListener(boFocus);
        for (Component component : form.getComponents()) {
            if (component instanceof JLabel) component.addMouseListener(boFocus);
        }
        JScrollPane formCuon = new JScrollPane(formChinh);
        formCuon.setBorder(null);
        formCuon.setPreferredSize(new Dimension(650, 490));
        formCuon.getVerticalScrollBar().setUnitIncrement(16);

        // Khi dữ liệu không hợp lệ, mở lại đúng form này, giữ nguyên nội dung.
        while (true) {
            int answer = JOptionPane.showConfirmDialog(
                    this, formCuon,
                    creating ? "Thêm tài khoản" : "Sửa tài khoản",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }

            kiemTraTen.run();
            kiemTraEmail.run();
            kiemTraMatKhau.run();
            kiemTraNgaySinh.run();
            if (!loiTen.getText().isEmpty() || !loiEmail.getText().isEmpty()
                    || !loiMatKhau.getText().isEmpty() || !loiNgaySinh.getText().isEmpty()) {
                if (!loiTen.getText().isEmpty()) name.requestFocusInWindow();
                else if (!loiEmail.getText().isEmpty()) email.requestFocusInWindow();
                else if (!loiMatKhau.getText().isEmpty()) password.requestFocusInWindow();
                continue;
            }
            String passwordValue = new String(password.getPassword());
            try {
                java.time.LocalDate ngaySinhThuc = ngaySinh.getNgay();
                VaiTro selectedRole = (VaiTro) roleBox.getSelectedItem();
                if (selectedRole == null) {
                    JOptionPane.showMessageDialog(this, "Chưa có vai trò để chọn.");
                    continue;
                }

                // Lấy mã THẬT trả về sau khi SQL Server INSERT thành công.
                int maThucTe = db.luuTaiKhoanTraVeMa(
                        creating ? 0 : account.id,
                        username.getText().trim(),
                        name.getText().trim(),
                        email.getText().trim(),
                        selectedRole.id,
                        (String) statusBox.getSelectedItem(),
                        passwordValue,
                        ngaySinhThuc,
                        actor.id);

                taiLai();
                String tenDangNhapThuc = creating
                        ? TaiKhoanDAO.tenDangNhapTheoMa(maThucTe)
                        : account.tenDangNhap;

                JOptionPane.showMessageDialog(this,
                        creating
                                ? "Đã thêm tài khoản thành công!\n"
                                    + "Mã tài khoản chính thức: " + maThucTe + "\n"
                                    + "Tên đăng nhập: " + tenDangNhapThuc
                                : "Đã cập nhật thông tin tài khoản thành công.",
                        "MediDent", JOptionPane.INFORMATION_MESSAGE);
                return;

            } catch (IllegalArgumentException dateError) {
                hienLoi(ngaySinh, loiNgaySinh, dateError.getMessage());
            } catch (Exception ex) {
                GiaoDienQuanLyTaiKhoan.loi(this, ex);
                // Form giữ dữ liệu để người quản trị có thể kiểm tra và thử lại.
            }
        }
    }

    /**
     * Admin xác minh nhân viên rồi đặt lại mật khẩu theo tên và ngày sinh.
     * Chỉ hiện mật khẩu một lần và không lưu dạng rõ trong SQL / nhật ký.
     */
    private void datLaiMatKhau() {
        TaiKhoan taiKhoan = taiKhoanDangChon();
        if (taiKhoan == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn đã xác minh danh tính của " + taiKhoan.hoTen + "?\n"
                + "Đặt lại mật khẩu cho " + taiKhoan.tenDangNhap + "?\n"
                + "Nhân viên bắt buộc đổi ngay khi đăng nhập.",
                "Xác nhận cấp lại mật khẩu", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            String matKhauTam = db.datLaiMatKhauTheoNgaySinh(taiKhoan.id, actor.id);
            JTextField hienMatKhau = GiaoDienQuanLyTaiKhoan.text(matKhauTam);
            hienMatKhau.setEditable(false);
            hienMatKhau.setPreferredSize(new Dimension(320, 40));
            JPanel noiDung = new JPanel(new GridLayout(0, 1, 6, 6));
            noiDung.add(GiaoDienQuanLyTaiKhoan.label("Mật khẩu tạm (chỉ hiển thị một lần):", 12, true));
            noiDung.add(hienMatKhau);
            noiDung.add(GiaoDienQuanLyTaiKhoan.label("Chuyển trực tiếp cho nhân viên đã được xác minh.", 11, false));
            JOptionPane.showMessageDialog(this, noiDung, "Đặt lại mật khẩu thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            hienMatKhau.setText("");
        } catch (Exception ex) {
            GiaoDienQuanLyTaiKhoan.loi(this, ex);
        }
    }

    /** 7. Xóa dùng ID thật, không dùng số thứ tự của bảng. */
    private void xoa() {
        TaiKhoan account = taiKhoanDangChon();
        if (account == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Xóa tài khoản " + account.hoTen + "?\n"
                + "Nếu nhân viên đã phát sinh nghiệp vụ, hệ thống sẽ từ chối xóa.\n"
                + "Nên dùng Tạm khóa để bảo toàn lịch sử.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            db.xoaTK(account.id, actor.id);
            taiLai();
        } catch (Exception exception) {
            GiaoDienQuanLyTaiKhoan.loi(this, exception);
        }
    }
    // Những hàm nhỏ này chỉ thuộc màn hình Quản lý tài khoản.
    private static JLabel nhanLoi() {
        JLabel label = new JLabel("");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(new Color(0xDC2626));
        return label;
    }

    private static JPanel oVaLoi(javax.swing.JComponent field, JLabel error) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.add(field, BorderLayout.CENTER);
        panel.add(error, BorderLayout.SOUTH);
        return panel;
    }

    private static void hienLoi(javax.swing.JComponent field, JLabel error, String message) {
        error.setText(message);
        field.setToolTipText(message.isEmpty() ? null : message);
    }

    private static void batLoiKhiRoiO(JTextField field, Runnable check) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent event) {
                check.run();
            }
        });
        field.addActionListener(event -> check.run());
    }

}


/**
 * Giao diện dùng chung giữa các vai trò.
 *
 * Chỉ chứa những hàm tạo giao diện / bảng màu; các màn hình riêng của quản trị
 * viên nằm trong UI.QuanTriVien và không chỉnh sửa ManHinhTrangChu cũ.
 */
final class GiaoDienQuanLyTaiKhoan {

    // 1. Màu sắc chuẩn của MediDent.
    public static final Color PRIMARY = new Color(0x0284C7);
    public static final Color HOVER = new Color(0x0369A1);
    public static final Color BG = new Color(0xF8FAFC);
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color GREEN = new Color(0x10B981);
    public static final Color WARN = new Color(0xF59E0B);
    public static final Color RED = new Color(0xEF4444);

    private GiaoDienQuanLyTaiKhoan() {
        // Không tạo đối tượng từ lớp tiện ích.
    }

    /** 2. Tạo nhãn có font và màu nhất quán. */
    public static JLabel label(String text, int fontSize, boolean bold) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, fontSize));
        result.setForeground(bold ? TEXT : MUTED);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        return result;
    }

    /** 3. Tạo nút theo hai kiểu chính/phụ của hệ thống. */
    public static JButton nut(String title, boolean primary) {
        // Nút trắng, viền xám, bo góc; xanh khi nhấn.
        JButton button = new JButton(title) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(WHITE);
        button.setForeground(TEXT);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    /** 4. Nút có icon Java2D, không còn các ký tự Unicode gây ô vuông. */
    public static JButton nutIcon(String title, boolean primary, BieuTuong.Loai loai) {
        JButton button = nut(title, primary);
        button.setIcon(new BieuTuong(loai, MUTED, 17));
        button.setIconTextGap(9);
        return button;
    }

    /** 5. Khung trắng và viền nhạt dùng cho form, bảng và biểu đồ. */
    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));
        return panel;
    }

    /**
     * 6. Nội dung co theo chiều ngang viewport để không trôi sang bên phải.
     * Khi dài, người dùng chỉ cuộn dọc ở toàn trang.
     */
    private static final class CotNoiDung extends JPanel implements Scrollable {
        CotNoiDung() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        protected void addImpl(Component child, Object constraints, int index) {
            if (child instanceof JComponent) {
                ((JComponent) child).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            super.addImpl(child, constraints, index);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(750, 560);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle rect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle rect, int orientation, int direction) {
            return Math.max(40, rect.height - 50);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** 7. Đầu trang thống nhất gồm breadcrumb, tiêu đề và mô tả. */
    public static JPanel trang(String title, String description) {
        JPanel page = new CotNoiDung();
        page.setBorder(new EmptyBorder(26, 28, 44, 28));
        page.add(label("Tổng quan   ›   " + title, 11, false));
        page.add(Box.createVerticalStrut(18));
        page.add(label(title, 24, true));
        page.add(Box.createVerticalStrut(7));
        page.add(label(description, 12, false));
        page.add(Box.createVerticalStrut(23));
        return page;
    }

    /** 8. Trang cuộn dọc, bảng riêng có thể cuộn ngang. */
    public static JScrollPane cuon(JPanel page) {
        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(22);
        scroll.getVerticalScrollBar().setBlockIncrement(90);
        return scroll;
    }

    /**
     * 9. Đọc logo từ tài nguyên: /Image/logo(1024x884).png.
     *
     * Không bóp méo ảnh: luôn giữ tỷ lệ gốc và căn giữa khung hiển thị.
     * Nếu thiếu tài nguyên, dùng logo dự phòng và không phát sinh lỗi URL null.
     */
    public static ImageIcon logo(int kichThuoc) {
        return logo(kichThuoc, kichThuoc);
    }

    public static ImageIcon logo(int rong, int cao) {
        if (rong < 1 || cao < 1) {
            throw new IllegalArgumentException("Kích thước logo phải lớn hơn 0.");
        }

        BufferedImage canvas = new BufferedImage(rong, cao, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            String[] tenAnh = {
                "/Image/medident_logo.png",
                "/Image/logo(1024x884).png",
                "/Image/logo.png",
                "/Image/logo_ai.png"
            };

            for (String path : tenAnh) {
                URL url = GiaoDienQuanLyTaiKhoan.class.getResource(path);
                if (url == null) {
                    continue;
                }
                try {
                    BufferedImage source = ImageIO.read(url);
                    if (source == null) {
                        continue;
                    }

                    // Scale theo cạnh nhỏ hơn để ảnh không bị méo.
                    double tyLe = Math.min(rong / (double) source.getWidth(),
                            cao / (double) source.getHeight());
                    int displayWidth = Math.max(1, (int) Math.round(source.getWidth() * tyLe));
                    int displayHeight = Math.max(1, (int) Math.round(source.getHeight() * tyLe));
                    int left = (rong - displayWidth) / 2;
                    int top = (cao - displayHeight) / 2;

                    graphics.drawImage(source, left, top, displayWidth, displayHeight, null);
                    return new ImageIcon(canvas);
                } catch (IOException ex) {
                    System.err.println("Không đọc được ảnh " + path + ": " + ex.getMessage());
                }
            }

            // Logo dự phòng: hình tròn xanh và chữ M; không cần font biểu tượng.
            graphics.setColor(PRIMARY);
            graphics.fillRoundRect(1, 1, rong - 2, cao - 2, 14, 14);
            graphics.setColor(WHITE);
            graphics.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, cao / 2)));
            String text = "M";
            int textX = (rong - graphics.getFontMetrics().stringWidth(text)) / 2;
            int textY = (cao + graphics.getFontMetrics().getAscent()) / 2 - 3;
            graphics.drawString(text, textX, textY);
            return new ImageIcon(canvas);
        } finally {
            graphics.dispose();
        }
    }

    /** 10. Hộp lỗi phân biệt SQL Server với lỗi giao diện. */
    public static void loi(Component owner, Exception exception) {
        exception.printStackTrace();
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.toString();
        }
        if (message.contains("Invalid column name") || message.contains("Invalid object name")) {
            message = "Dữ liệu hệ thống chưa được cập nhật.";
        }
        JOptionPane.showMessageDialog(owner, message,
                "Không thể thực hiện",
                JOptionPane.ERROR_MESSAGE);
    }

    /** 11. Ô nhập liệu có padding và viền đồng nhất. */
    public static JTextField text(String value) {
        JTextField field = new JTextField(value, 23);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(9, 10, 9, 10)));
        return field;
    }

    /** 12. Hàng chứa bộ lọc, nút hành động căn trái. */
    public static JPanel hang() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 13. Cột STT hiển thị 1..N theo vị trí thật trên màn hình.
     * Nếu người dùng sắp xếp lại bằng header, STT vẫn liên tục 1..N.
     */
    public static void cotSoThuTu(JTable table) {
        if (table.getColumnCount() == 0 || !"STT".equals(table.getColumnName(0))) {
            return;
        }

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(45);
        column.setMaxWidth(65);
        column.setPreferredWidth(52);
        column.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value,
                    boolean selected, boolean focused, int row, int columnIndex) {
                JLabel result = (JLabel) super.getTableCellRendererComponent(
                        source, row + 1, selected, focused, row, columnIndex);
                result.setHorizontalAlignment(SwingConstants.CENTER);
                return result;
            }
        });
    }

    /** 14. Trang trí bảng và cho phép cuộn ngang ở bảng nhiều cột. */
    public static JComponent wrapTable(JTable table) {
        table.setRowHeight(39);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        // Không chọn nhầm dòng khi người dùng lọc/sắp xếp JTable.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) table.setRowSelectionInterval(row, row);
                }
            }
        });
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(BG);
        table.setSelectionBackground(new Color(0xE0F2FE));

        if (table.getColumnCount() >= 5) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < table.getColumnCount(); i++) {
                String column = table.getColumnName(i).toLowerCase();
                int width;
                if (column.equals("stt")) {
                    width = 52;
                } else if (column.contains("email")) {
                    width = 190;
                } else if (column.contains("họ tên")) {
                    width = 165;
                } else if (column.contains("đăng nhập")) {
                    width = 130;
                } else if (column.contains("vai trò")) {
                    width = 170;
                } else if (column.contains("trạng thái")) {
                    width = 120;
                } else {
                    width = 115;
                }
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
        cotSoThuTu(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        return scroll;
    }
}
