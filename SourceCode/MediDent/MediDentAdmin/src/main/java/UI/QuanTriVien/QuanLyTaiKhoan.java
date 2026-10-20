package UI.QuanTriVien;

import Class.TaiKhoan;
import Class.VaiTro;
import TienIch.EmailHopLe;
import TienIch.MatKhauHopLe;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
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

    private final KhoDuLieu db;
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
    private final JTextField search = GiaoDien.text("");
    private final JComboBox<String> status = new JComboBox<>(new String[]{
        "Tất cả trạng thái", "Hoạt động", "Tạm khóa"
    });
    private final JComboBox<String> role = new JComboBox<>();

    // Dữ liệu thật tương ứng với mỗi hàng MODEL (khác chỉ số hàng đang sắp xếp).
    private List<TaiKhoan> visibleAccounts = new ArrayList<>();
    private List<VaiTro> roles = new ArrayList<>();

    public QuanLyTaiKhoan(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Quản lý tài khoản",
                "Quản lý người dùng, vai trò và trạng thái truy cập hệ thống.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        // 1. Thanh tìm kiếm và lọc co giãn theo chiều rộng cửa sổ.
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);
        filters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        search.setPreferredSize(new Dimension(190, 37));
        search.setMinimumSize(new Dimension(90, 37));
        role.setPreferredSize(new Dimension(148, 37));
        status.setPreferredSize(new Dimension(135, 37));

        JButton find = GiaoDien.nutIcon("Tìm kiếm", false, BieuTuong.Loai.TIM_KIEM);
        JButton add = GiaoDien.nutIcon("Thêm tài khoản", true, BieuTuong.Loai.THEM);
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
        JPanel card = GiaoDien.card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(700, 550));

        // Đặt các nút ở ĐẦU bảng, luôn dễ nhìn thấy thay vì cuối bảng cao 550px.
        JButton edit = GiaoDien.nutIcon("Sửa thông tin", true, BieuTuong.Loai.SUA);
        JButton remove = GiaoDien.nutIcon("Xóa tài khoản", false, BieuTuong.Loai.XOA);
        JButton reset = GiaoDien.nutIcon("Quên mật khẩu / Reset", false,
                BieuTuong.Loai.TAI_LAI);

        JPanel tools = GiaoDien.hang();
        tools.add(edit);
        tools.add(remove);
        tools.add(reset);

        JLabel title = GiaoDien.label("Danh sách tài khoản", 14, true);
        JPanel tableHeader = new JPanel(new BorderLayout(12, 0));
        tableHeader.setOpaque(false);
        tableHeader.add(title, BorderLayout.WEST);
        tableHeader.add(tools, BorderLayout.EAST);

        card.add(tableHeader, BorderLayout.NORTH);
        card.add(GiaoDien.wrapTable(table), BorderLayout.CENTER);
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
            GiaoDien.loi(this, exception);
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
            GiaoDien.loi(this, exception);
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
        return modelRow >= 0 && modelRow < visibleAccounts.size()
                ? visibleAccounts.get(modelRow) : null;
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
            GiaoDien.loi(this, ex);
            return;
        }
        String tenDangNhapDuKien = creating
                ? KhoDuLieu.tenDangNhapTheoMa(maDuKien)
                : account.tenDangNhap;

        JTextField idTuDong = GiaoDien.text(String.valueOf(maDuKien));
        idTuDong.setEditable(false);
        idTuDong.setToolTipText(creating ? "Mã dự kiến (SQL có thể cấp mã khác nếu có tạo đồng thời)"
                : "Mã tài khoản chính thức");

        JTextField username = GiaoDien.text(tenDangNhapDuKien);
        username.setEditable(false);
        username.setToolTipText("Tên đăng nhập hệ thống tự tạo theo mã tài khoản");

        JTextField name = GiaoDien.text(creating ? "" : account.hoTen);
        JTextField email = GiaoDien.text(creating ? "" : account.email);
        JPasswordField password = new JPasswordField();

        // Calendar thực: chỉ chọn ngày đã có trong tháng/năm hợp lệ.
        NgayChon ngaySinh = new NgayChon();
        try {
            if (!creating) {
                ngaySinh.setNgay(db.ngaySinhNhanVien(account.id, actor.id));
            }
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
            return;
        }

        JTextField ngayTao = GiaoDien.text(creating
                ? "SQL Server ghi khi bấm Lưu" : account.ngayTao);
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
        form.add(GiaoDien.label(creating ? "Mã tài khoản (dự kiến)" : "Mã tài khoản", 12, true));
        form.add(idTuDong);
        form.add(GiaoDien.label("Tên đăng nhập (tự động)", 12, true));
        form.add(username);
        form.add(GiaoDien.label("Ngày tạo (tự động)", 12, true));
        form.add(ngayTao);
        form.add(GiaoDien.label("Họ và tên *", 12, true));
        form.add(name);
        form.add(GiaoDien.label("Email *", 12, true));
        form.add(email);
        form.add(GiaoDien.label("Ngày sinh (chọn từ lịch) *", 12, true));
        form.add(ngaySinh);
        form.add(GiaoDien.label(creating ? "Mật khẩu *" : "Mật khẩu mới (trống: giữ nguyên)",
                12, true));
        form.add(password);
        form.add(GiaoDien.label("Vai trò", 12, true));
        form.add(roleBox);
        form.add(GiaoDien.label("Trạng thái", 12, true));
        form.add(statusBox);

        JPanel formChinh = new JPanel(new BorderLayout(0, 12));
        formChinh.add(form, BorderLayout.CENTER);
        if (creating) {
            JLabel ghiChu = GiaoDien.label(
                    "<html><b>Lưu ý:</b> Mã và tên đăng nhập đang hiển thị là dự kiến. "
                    + "Nếu người khác tạo tài khoản trước, SQL Server sẽ cấp mã mới. "
                    + "Hệ thống hiển thị mã chính thức ngay sau khi lưu.</html>",
                    11, false);
            formChinh.add(ghiChu, BorderLayout.SOUTH);
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

            String passwordValue = new String(password.getPassword());
            if (name.getText().trim().isEmpty()
                    || !EmailHopLe.kiemTra(email.getText())
                    || (creating && !MatKhauHopLe.kiemTra(passwordValue))
                    || (!passwordValue.isEmpty() && !MatKhauHopLe.kiemTra(passwordValue))) {
                JOptionPane.showMessageDialog(this,
                        "Kiểm tra họ tên. " + MatKhauHopLe.huongDan() + " " + EmailHopLe.huongDan(),
                        "Kiểm tra thông tin", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            try {
                java.time.LocalDate ngaySinhThuc = ngaySinh.getNgay();
                if (ngaySinhThuc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn ngày sinh bằng nút Chọn lịch.",
                            "Thiếu ngày sinh", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
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
                        ? KhoDuLieu.tenDangNhapTheoMa(maThucTe)
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
                JOptionPane.showMessageDialog(this, dateError.getMessage(),
                        "Ngày sinh không hợp lệ", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                GiaoDien.loi(this, ex);
                // Form giữ dữ liệu để người quản trị có thể kiểm tra và thử lại.
            }
        }
    }

    /**
     * Admin xác minh danh tính ngoài ứng dụng rồi cấp mật khẩu ngẫu nhiên.
     * Chỉ hiện mật khẩu một lần và không lưu dạng rõ trong SQL / nhật ký.
     */
    private void datLaiMatKhau() {
        TaiKhoan taiKhoan = taiKhoanDangChon();
        if (taiKhoan == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn đã xác minh danh tính của " + taiKhoan.hoTen + "?\n"
                + "Cấp mật khẩu tạm mới cho " + taiKhoan.tenDangNhap + "?\n"
                + "Nhân viên bắt buộc đổi ngay khi đăng nhập.",
                "Xác nhận cấp lại mật khẩu", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        try {
            String matKhauTam = db.datLaiMatKhauAnToan(taiKhoan.id, actor.id);
            JTextField hienMatKhau = GiaoDien.text(matKhauTam);
            hienMatKhau.setEditable(false);
            hienMatKhau.setPreferredSize(new Dimension(320, 40));
            JPanel noiDung = new JPanel(new GridLayout(0, 1, 6, 6));
            noiDung.add(GiaoDien.label("Mật khẩu tạm (chỉ hiển thị một lần):", 12, true));
            noiDung.add(hienMatKhau);
            noiDung.add(GiaoDien.label("Chuyển trực tiếp cho nhân viên đã được xác minh.", 11, false));
            JOptionPane.showMessageDialog(this, noiDung, "Đặt lại mật khẩu thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            hienMatKhau.setText("");
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
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
            GiaoDien.loi(this, exception);
        }
    }
}
