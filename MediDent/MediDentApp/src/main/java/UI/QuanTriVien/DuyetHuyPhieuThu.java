package UI.QuanTriVien;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Yêu cầu hủy hóa đơn/phiếu thu.
 *
 * - Tất cả vai trò chỉ nhìn thấy yêu cầu cùng cấp hoặc cấp thấp hơn.
 * - Riêng Admin có quyền HOTRO mới hiện nút Duyệt / Từ chối.
 * - Vẫn kiểm tra lại quyền và trạng thái trong DAO khi thực hiện thao tác.
 */
public final class DuyetHuyPhieuThu extends JPanel {

    private final TaiKhoan user;
    private final QuanLyDAO dao;
    private final boolean adminDuocDuyet;
    private List<QuanLyDAO.Hang> duLieu = Collections.emptyList();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT", "Mã đơn", "Ngày gửi", "Mã TT", "Mã HĐ",
                "Người gửi", "Vai trò", "Lý do", "Trạng thái", "Ghi chú duyệt"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
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
    private final JButton duyet = GiaoDien.nutIcon(
            "Duyệt hủy", true, BieuTuong.Loai.KIEM_TRA);
    private final JButton tuChoi = GiaoDien.nutIcon(
            "Từ chối", false, BieuTuong.Loai.HUY);

    public DuyetHuyPhieuThu(QuanLyDAO dao, TaiKhoan user) {
        this(dao, user, false);
    }

    /**
     * @param adminDuocDuyet true khi KhungChinh đã xác nhận Admin và quyền HOTRO.
     */
    public DuyetHuyPhieuThu(QuanLyDAO dao, TaiKhoan user, boolean adminDuocDuyet) {
        this.dao = dao;
        this.user = user;
        this.adminDuocDuyet = adminDuocDuyet;

        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang("Yêu cầu hủy hóa đơn",
                "Yêu cầu từ nhân viên được lọc theo cấp bậc. Chỉ Admin phê duyệt hủy.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        JPanel toolbar = GiaoDien.hang();
        JButton xem = GiaoDien.nutIcon("Xem lý do", false, BieuTuong.Loai.TIM_KIEM);
        JButton taiLai = GiaoDien.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        toolbar.add(xem);
        toolbar.add(taiLai);

        if (adminDuocDuyet) {
            toolbar.add(duyet);
            toolbar.add(tuChoi);
        }
        page.add(toolbar);
        page.add(Box.createVerticalStrut(14));

        JPanel card = GiaoDien.card(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(900, 440));
        card.add(GiaoDien.label("Các yêu cầu hủy và kết quả xử lý", 14, true),
                BorderLayout.NORTH);

        bang.setAutoCreateRowSorter(true);
        bang.setRowHeight(34);
        bang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        bang.getColumnModel().getColumn(0).setPreferredWidth(48);
        bang.getColumnModel().getColumn(1).setPreferredWidth(75);
        bang.getColumnModel().getColumn(2).setPreferredWidth(150);
        bang.getColumnModel().getColumn(3).setPreferredWidth(65);
        bang.getColumnModel().getColumn(4).setPreferredWidth(65);
        bang.getColumnModel().getColumn(5).setPreferredWidth(165);
        bang.getColumnModel().getColumn(6).setPreferredWidth(135);
        bang.getColumnModel().getColumn(7).setPreferredWidth(260);
        bang.getColumnModel().getColumn(8).setPreferredWidth(125);
        bang.getColumnModel().getColumn(9).setPreferredWidth(220);
        card.add(GiaoDien.wrapTable(bang), BorderLayout.CENTER);
        page.add(card);

        xem.addActionListener(event -> xemChiTiet());
        taiLai.addActionListener(event -> tai());
        duyet.addActionListener(event -> xuLy(true));
        tuChoi.addActionListener(event -> xuLy(false));
        bang.getSelectionModel().addListSelectionListener(event -> capNhatNut());
        capNhatNut();
        tai();
    }

    /** Dùng ID trong Hang, STT chỉ để hiển thị. */
    private void tai() {
        try {
            duLieu = dao.danhSachYeuCauHuy(user.id);
            model.setRowCount(0);
            for (int i = 0; i < duLieu.size(); i++) {
                QuanLyDAO.Hang h = duLieu.get(i);
                model.addRow(new Object[]{i + 1, h.id, h.cot[0], h.cot[1], h.cot[2],
                        h.cot[3], h.cot[4], h.cot[5], h.cot[6], h.cot[7]});
            }
            capNhatNut();
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }

    private QuanLyDAO.Hang daChon() {
        int viewIndex = bang.getSelectedRow();
        if (viewIndex < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một yêu cầu.");
            return null;
        }
        return duLieu.get(bang.convertRowIndexToModel(viewIndex));
    }

    private void capNhatNut() {
        int viewIndex = bang.getSelectedRow();
        boolean choDuyet = false;
        if (viewIndex >= 0 && viewIndex < bang.getRowCount()) {
            QuanLyDAO.Hang h = duLieu.get(bang.convertRowIndexToModel(viewIndex));
            choDuyet = "Chờ duyệt".equals(h.cot[6]);
        }
        duyet.setEnabled(adminDuocDuyet && choDuyet);
        tuChoi.setEnabled(adminDuocDuyet && choDuyet);
    }

    private void xemChiTiet() {
        QuanLyDAO.Hang h = daChon();
        if (h == null) return;

        JTextArea text = new JTextArea(
                "Yêu cầu #" + h.id
                + "\nThời gian: " + h.cot[0]
                + "\nThanh toán: " + h.cot[1] + " — Hóa đơn: " + h.cot[2]
                + "\nNgười gửi: " + h.cot[3] + " (" + h.cot[4] + ")"
                + "\nTrạng thái: " + h.cot[6]
                + "\n\nLý do yêu cầu:\n" + h.cot[5]
                + "\n\nGhi chú duyệt:\n" + h.cot[7], 12, 52);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(text),
                "Chi tiết yêu cầu hủy hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xuLy(boolean dongY) {
        if (!adminDuocDuyet) return;
        QuanLyDAO.Hang h = daChon();
        if (h == null) return;
        if (!"Chờ duyệt".equals(h.cot[6])) {
            JOptionPane.showMessageDialog(this, "Yêu cầu đã được xử lý.");
            return;
        }
        String ghiChu = JOptionPane.showInputDialog(this,
                dongY ? "Ghi chú chấp thuận:" : "Lý do từ chối:");
        if (ghiChu == null) return;
        if (!dongY && ghiChu.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập lý do từ chối.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                (dongY ? "Duyệt hủy" : "Từ chối hủy")
                + " yêu cầu #" + h.id + "?\n"
                + "Lưu ý: phê duyệt chỉ cập nhật trạng thái, không hoàn tiền tự động.",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.duyetYeuCauHuy(h.id, dongY, ghiChu, user.id);
            JOptionPane.showMessageDialog(this, "Đã cập nhật yêu cầu thành công.");
            tai();
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }
}
