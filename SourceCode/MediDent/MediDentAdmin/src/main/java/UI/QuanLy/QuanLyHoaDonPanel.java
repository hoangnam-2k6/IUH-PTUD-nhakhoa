
package UI.QuanLy;

import UI.QuanLy.*;
import DAO.HoaDonDAO;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class QuanLyHoaDonPanel extends JPanel {
    private final ChucNangHoaDon chucNang = new ChucNangHoaDon();
    private List<HoaDonDAO.DongHoaDon> ds = new ArrayList<>();
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    private final JTextField tim = new JTextField(9);
    private final ChonKhoangNgay khoangNgay = new ChonKhoangNgay();
    private final JLabel thongBao = new JLabel("Sẵn sàng");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
        "STT", "Mã HĐ", "Mã phiếu khám", "Ngày lập",
        "Số tiền", "Phương thức", "Trạng thái", "Mã TT"
    }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return c <= 2 || c == 7 ? Integer.class : Object.class;
        }
    };

    private final JTable bang = new JTable(model);

    public QuanLyHoaDonPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel tren = new JPanel(new BorderLayout(0, 8));
        tren.setOpaque(false);
        tren.add(GiaoDienChung.tieuDe("QUẢN LÝ HÓA ĐƠN"), BorderLayout.NORTH);

        JButton timNut = GiaoDienChung.nut("Tìm");
        JButton xem = GiaoDienChung.nut("Chi tiết");
        JButton duyet = GiaoDienChung.nut("Duyệt hủy");
        JButton tai = GiaoDienChung.nut("Tải lại");

        tren.add(GiaoDienChung.thanhCongCu(new JLabel("Tìm:"), tim,
                timNut, khoangNgay, xem, duyet, tai), BorderLayout.CENTER);

        add(tren, BorderLayout.NORTH);
        GiaoDienChung.bang(bang);
        add(new JScrollPane(bang), BorderLayout.CENTER);
        add(thongBao, BorderLayout.SOUTH);

        khoangNgay.datKhoang(null, null);
        khoangNgay.datKhiThayDoi(this::taiDuLieu);

        timNut.addActionListener(e -> taiDuLieu());
        tim.addActionListener(e -> taiDuLieu());

        tai.addActionListener(e -> {
            tim.setText("");
            khoangNgay.datKhoang(null, null);
        });

        xem.addActionListener(e -> xemChiTiet());
        duyet.addActionListener(e -> duyetHuy());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) taiDuLieu();
        });
    }

    // Lấy hóa đơn từ class chức năng.
    public void taiDuLieu() {
        try {
            ds = chucNang.tim(tim.getText().trim(),
                    khoangNgay.layTuNgay(), khoangNgay.layDenNgay());

            model.setRowCount(0);
            int i = 0;

            for (HoaDonDAO.DongHoaDon h : ds) {
                model.addRow(new Object[]{
                    ++i, h.layMaHD(), h.layMaPK(),
                    h.layNgayLap(), h.laySoTien(),
                    h.layPhuongThuc() == null ? "Chưa có liên kết" : h.layPhuongThuc(),
                    h.layTrangThai(), h.layMaTT()
                });
            }

            thongBao.setText("Tổng số hóa đơn: " + ds.size());
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    private HoaDonDAO.DongHoaDon layChon() {
        int i = bang.getSelectedRow();
        if (i < 0) throw new IllegalArgumentException("Hãy chọn hóa đơn.");
        return ds.get(bang.convertRowIndexToModel(i));
    }

    // Chỉ xem thông tin, không thu tiền.
    private void xemChiTiet() {
        try {
            HoaDonDAO.DongHoaDon h = layChon();

            String noiDung = "Hóa đơn: " + h.layMaHD()
                    + "\nPhiếu khám: " + h.layMaPK()
                    + "\nThanh toán: " + h.layMaTT()
                    + "\nTổng tiền: " + h.laySoTien() + " VNĐ"
                    + "\nPhương thức: " + (h.layPhuongThuc() == null
                            ? "Chưa có liên kết" : h.layPhuongThuc())
                    + "\nTrạng thái: " + h.layTrangThai();

            JOptionPane.showMessageDialog(this, noiDung,
                    "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }

    // Duyệt hoặc từ chối yêu cầu hủy do bộ phận có thẩm quyền gửi.
    private void duyetHuy() {
        try {
            List<String> dsYeuCau = chucNang.choDuyet();

            if (dsYeuCau.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có yêu cầu hủy đang chờ duyệt.");
                return;
            }

            Object chonYC = JOptionPane.showInputDialog(this,
                    "Chọn yêu cầu:", "Duyệt hủy",
                    JOptionPane.QUESTION_MESSAGE, null,
                    dsYeuCau.toArray(), dsYeuCau.get(0));

            if (chonYC == null) return;

            int maYC = Integer.parseInt(chonYC.toString().split(" - ")[0]);
            Object[] nut = {"Duyệt hủy", "Từ chối", "Đóng"};

            int chon = JOptionPane.showOptionDialog(this,
                    "Chỉ có thể hủy chứng từ chưa thực thu.\n"
                    + "Chứng từ đã thu cần quy trình hoàn tiền riêng.",
                    "Quyết định", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, nut, nut[2]);

            if (chon != 0 && chon != 1) return;

            chucNang.duyet(maYC, chon == 0);
            JOptionPane.showMessageDialog(this, "Đã lưu quyết định.");
            taiDuLieu();
        } catch (Exception x) {
            GiaoDienChung.baoLoi(this, x);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MediDent - Quản lý hóa đơn");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new QuanLyHoaDonPanel());
            frame.setSize(1150, 750);
            frame.setMinimumSize(new Dimension(900, 600));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
