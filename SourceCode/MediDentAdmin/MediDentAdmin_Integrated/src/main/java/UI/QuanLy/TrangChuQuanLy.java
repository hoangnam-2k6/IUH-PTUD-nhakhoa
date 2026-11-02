package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Dashboard Quản lý - nạp số liệu SQL Server ở luồng nền để không khóa Swing EDT. */
public final class TrangChuQuanLy extends JPanel {

    private final JLabel trangThai;
    private final JLabel tongBenhNhan;
    private final JLabel lichHenHomNay;
    private final JLabel luotKhamTrongThang;
    private final JLabel tongHoaDon;
    private final DefaultTableModel modelLichHen;

    public TrangChuQuanLy(KhoDuLieu db, TaiKhoan user) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel noiDung = GiaoDien.trang("Trang chủ Quản lý phòng khám",
                "Xin chào " + user.hoTen + ". Tổng quan hoạt động phòng khám.");
        add(GiaoDien.cuon(noiDung), BorderLayout.CENTER);

        JPanel cards = new JPanel(new GridLayout(2, 2, 14, 14));
        cards.setOpaque(false);
        cards.setAlignmentX(LEFT_ALIGNMENT);
        cards.setPreferredSize(new Dimension(760, 260));
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        StatCard cardBenhNhan = the("Tổng bệnh nhân", GiaoDien.PRIMARY,
                BieuTuong.Loai.TAI_KHOAN, "Hồ sơ lưu trong hệ thống");
        StatCard cardLich = the("Lịch hẹn hôm nay", GiaoDien.GREEN,
                BieuTuong.Loai.THONG_BAO, "Theo ngày hiện tại");
        StatCard cardKham = the("Lượt khám trong tháng", new Color(0xF59E0B),
                BieuTuong.Loai.KIEM_TRA, "Tính theo phiếu khám");
        StatCard cardHoaDon = the("Tổng hóa đơn", new Color(0x8B5CF6),
                BieuTuong.Loai.BAO_CAO, "Tất cả trạng thái");

        tongBenhNhan = cardBenhNhan.value;
        lichHenHomNay = cardLich.value;
        luotKhamTrongThang = cardKham.value;
        tongHoaDon = cardHoaDon.value;
        cards.add(cardBenhNhan.panel);
        cards.add(cardLich.panel);
        cards.add(cardKham.panel);
        cards.add(cardHoaDon.panel);
        noiDung.add(cards);
        noiDung.add(Box.createVerticalStrut(18));

        JPanel history = GiaoDien.card(new BorderLayout(0, 12));
        history.setAlignmentX(LEFT_ALIGNMENT);
        history.setPreferredSize(new Dimension(760, 315));
        history.setMaximumSize(new Dimension(Integer.MAX_VALUE, 315));
        history.add(GiaoDien.label("Lịch hẹn gần đây", 14, true), BorderLayout.NORTH);

        modelLichHen = new DefaultTableModel(
                new String[]{"STT", "Ngày", "Giờ", "Bệnh nhân", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelLichHen);
        table.setRowHeight(33);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // wrapTable() đặt AUTO_RESIZE_OFF cho bảng từ 5 cột trở lên.
        // Dashboard cần bảng luôn giãn theo chiều rộng viewport để không còn
        // vùng trống bên phải, vì vậy đặt lại chế độ sau khi tạo JScrollPane.
        JComponent bangLichHen = GiaoDien.wrapTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        history.add(bangLichHen, BorderLayout.CENTER);
        noiDung.add(history);
        noiDung.add(Box.createVerticalStrut(8));

        trangThai = GiaoDien.label("Đang tải dữ liệu từ máy chủ...", 11, false);
        trangThai.setAlignmentX(LEFT_ALIGNMENT);
        noiDung.add(trangThai);

        taiDuLieuNen();
    }

    private void taiDuLieuNen() {
        new SwingWorker<QuanLyDAO.DuLieuDashboard, Void>() {
            @Override
            protected QuanLyDAO.DuLieuDashboard doInBackground() throws Exception {
                return new QuanLyDAO().duLieuDashboard();
            }

            @Override
            protected void done() {
                try {
                    QuanLyDAO.DuLieuDashboard data = get();
                    tongBenhNhan.setText(String.format("%,d", data.tongBenhNhan));
                    lichHenHomNay.setText(String.format("%,d", data.lichHenHomNay));
                    luotKhamTrongThang.setText(String.format("%,d", data.luotKhamTrongThang));
                    tongHoaDon.setText(String.format("%,d", data.tongHoaDon));

                    modelLichHen.setRowCount(0);
                    int stt = 1;
                    for (QuanLyDAO.Hang row : data.lichHenGanDay) {
                        modelLichHen.addRow(new Object[]{stt++, row.cot[0], row.cot[1],
                                row.cot[2], row.cot[3]});
                    }
                    trangThai.setText("Đã cập nhật số liệu từ SQL Server.");
                } catch (InterruptedException ex) {
                    // done() chạy trên EDT; không đặt cờ interrupt lên luồng giao diện.
                    hienThiLoi("Quá trình tải Dashboard bị gián đoạn.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    String message = cause.getMessage();
                    hienThiLoi("Không tải được dữ liệu Dashboard: "
                            + (message == null || message.isBlank() ? cause.getClass().getSimpleName() : message));
                }
            }
        }.execute();
    }

    private void hienThiLoi(String message) {
        tongBenhNhan.setText("—");
        lichHenHomNay.setText("—");
        luotKhamTrongThang.setText("—");
        tongHoaDon.setText("—");
        modelLichHen.setRowCount(0);
        trangThai.setText(message);
    }

    private static final class StatCard {
        final JPanel panel;
        final JLabel value;
        StatCard(JPanel panel, JLabel value) {
            this.panel = panel;
            this.value = value;
        }
    }

    private StatCard the(String title, Color accent, BieuTuong.Loai icon, String moTa) {
        JPanel card = GiaoDien.card(new BorderLayout(12, 5));
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel label = GiaoDien.label(title, 11, false);
        JLabel total = GiaoDien.label("Đang tải...", 25, true);
        JLabel sub = GiaoDien.label(moTa, 10, false);
        info.add(label);
        info.add(Box.createVerticalStrut(11));
        info.add(total);
        info.add(Box.createVerticalStrut(9));
        info.add(sub);
        card.add(info, BorderLayout.CENTER);

        JLabel glyph = new JLabel(new BieuTuong(icon, accent, 24));
        glyph.setPreferredSize(new Dimension(39, 39));
        glyph.setHorizontalAlignment(SwingConstants.CENTER);
        glyph.setOpaque(true);
        glyph.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
        card.add(glyph, BorderLayout.EAST);
        return new StatCard(card, total);
    }
}
