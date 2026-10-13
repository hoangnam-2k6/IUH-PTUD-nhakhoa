package UI.Chung;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.QuanTriVien.DuyetHuyPhieuThu;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Trung tâm yêu cầu chung cho Admin / Quản lý / Lễ tân.
 *
 * Dữ liệu được lọc THEO CẤP BẬC TRONG SQL SERVER, không lọc ở mỗi JTable.
 * Gồm 2 nhóm yêu cầu đã có trong MediDent:
 * 1) YeuCauHoTro - phản ánh, quên mật khẩu, hỗ trợ nghiệp vụ.
 * 2) YeuCauHuyPhieuThu - yêu cầu hủy hóa đơn/phiếu thu.
 */
public final class HoTroPage extends JPanel {

    private final QuanLyDAO dao;
    private final TaiKhoan user;
    private final boolean duocPhanHoi;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Mã YC", "Người gửi", "Vai trò", "Thời điểm",
                "Tiêu đề", "Trạng thái", "Phản hồi"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // STT theo hàng đang hiển thị, vẫn đúng khi người dùng sắp xếp bảng.
    private final JTable bang = new JTable(model) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
            Component component = super.prepareRenderer(renderer, row, col);
            if (col == 0 && component instanceof javax.swing.JLabel) {
                javax.swing.JLabel label = (javax.swing.JLabel) component;
                label.setText(String.valueOf(row + 1));
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            }
            return component;
        }
    };
    private List<QuanLyDAO.Hang> duLieu = Collections.emptyList();

    /** @param admin true khi đăng nhập là Admin có quyền HOTRO. */
    public HoTroPage(QuanLyDAO dao, TaiKhoan user, boolean admin) {
        this.dao = dao;
        this.user = user;
        boolean coThePhanHoi = false;
        try {
            coThePhanHoi = dao.coQuyenPhanHoiHoTro(user.id);
        } catch (Exception ex) {
            // Không truy vấn được quyền: ẩn chức năng phản hồi.
        }
        this.duocPhanHoi = coThePhanHoi;

        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        tabs.addTab("Yêu cầu hỗ trợ", taoHoTro());
        tabs.addTab("Yêu cầu hủy hóa đơn",
                new DuyetHuyPhieuThu(dao, user, admin));
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel taoHoTro() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(GiaoDien.BG);
        JPanel page = GiaoDien.trang("Trung tâm hỗ trợ - yêu cầu",
                "Hiển thị các yêu cầu của vai trò ngang cấp và cấp thấp hơn."
                + " Mọi yêu cầu vẫn lưu tại SQL Server.");
        container.add(GiaoDien.cuon(page), BorderLayout.CENTER);

        JPanel toolbar = GiaoDien.hang();
        JButton gui = GiaoDien.nutIcon("Gửi yêu cầu", true, BieuTuong.Loai.THEM);
        JButton xem = GiaoDien.nutIcon("Xem nội dung", false, BieuTuong.Loai.TIM_KIEM);
        JButton taiLai = GiaoDien.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        toolbar.add(gui);
        toolbar.add(xem);
        toolbar.add(taiLai);

        if (duocPhanHoi) {
            JButton phanHoi = GiaoDien.nutIcon("Phản hồi yêu cầu", false,
                    BieuTuong.Loai.SUA);
            phanHoi.addActionListener(event -> phanHoi());
            toolbar.add(phanHoi);
        }
        page.add(toolbar);
        page.add(Box.createVerticalStrut(14));

        JPanel card = GiaoDien.card(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(900, 450));
        card.add(GiaoDien.label("Danh sách yêu cầu theo cấp bậc", 14, true),
                BorderLayout.NORTH);

        bang.setAutoCreateRowSorter(true);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.setRowHeight(34);
        int[] widths = {50, 65, 170, 130, 145, 265, 115, 240};
        for (int col = 0; col < widths.length; col++) {
            bang.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        }
        card.add(GiaoDien.wrapTable(bang), BorderLayout.CENTER);
        page.add(card);

        gui.addActionListener(event -> guiYeuCau());
        xem.addActionListener(event -> xemNoiDung());
        taiLai.addActionListener(event -> tai());
        tai();
        return container;
    }

    private void tai() {
        try {
            duLieu = dao.danhSachHoTro(user.id);
            model.setRowCount(0);
            for (int i = 0; i < duLieu.size(); i++) {
                QuanLyDAO.Hang h = duLieu.get(i);
                model.addRow(new Object[]{i + 1, h.id, h.cot[0], h.cot[6],
                        h.cot[1], h.cot[2], h.cot[3], h.cot[5]});
            }
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }

    private QuanLyDAO.Hang daChon() {
        int viewIndex = bang.getSelectedRow();
        if (viewIndex < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một yêu cầu.");
            return null;
        }
        return duLieu.get(bang.convertRowIndexToModel(viewIndex));
    }

    private void guiYeuCau() {
        JTextField tieuDe = GiaoDien.text("");
        JTextArea noiDung = new JTextArea(6, 36);
        noiDung.setLineWrap(true);
        noiDung.setWrapStyleWord(true);
        JPanel form = new JPanel(new BorderLayout(0, 8));
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(new JLabel("Tiêu đề yêu cầu"), BorderLayout.NORTH);
        top.add(tieuDe, BorderLayout.CENTER);
        form.add(top, BorderLayout.NORTH);
        form.add(new JScrollPane(noiDung), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, form, "Gửi yêu cầu hỗ trợ",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            dao.guiHoTro(user.id, tieuDe.getText(), noiDung.getText());
            JOptionPane.showMessageDialog(this, "Đã ghi nhận yêu cầu.");
            tai();
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }

    private void xemNoiDung() {
        QuanLyDAO.Hang h = daChon();
        if (h == null) return;
        JTextArea details = new JTextArea("Người gửi: " + h.cot[0]
                + "\nVai trò: " + h.cot[6]
                + "\nNgày tạo: " + h.cot[1]
                + "\nTrạng thái: " + h.cot[3]
                + "\n\nNội dung:\n" + h.cot[4]
                + "\n\nPhản hồi:\n" + h.cot[5], 12, 48);
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(details),
                "Yêu cầu #" + h.id + " — " + h.cot[2], JOptionPane.INFORMATION_MESSAGE);
    }

    private void phanHoi() {
        if (!duocPhanHoi) return;
        QuanLyDAO.Hang h = daChon();
        if (h == null) return;
        JTextArea text = new JTextArea(6, 35);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        int option = JOptionPane.showConfirmDialog(this, new JScrollPane(text),
                "Phản hồi: " + h.cot[2], JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;
        try {
            dao.phanHoiHoTro(h.id, text.getText(), user.id);
            JOptionPane.showMessageDialog(this, "Đã lưu phản hồi.");
            tai();
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }
}
