/*
 * TrangChuQuanTriVien: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanTriVien;

import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Calendar;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class TrangChuQuanTriVien extends JPanel {

    private final KhoDuLieu db;

    /** Khởi tạo Dashboard của Quản trị viên theo bố cục hiện tại. */
    public TrangChuQuanTriVien(KhoDuLieu db) {
        this.db = db;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel page = GiaoDien.trang("Trang chủ Quản trị viên",
                "Theo dõi tổng quan tài khoản, phân quyền và trạng thái hệ thống.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);
        napDuLieu(page);
    }

    /** Tải số liệu tài khoản, quyền và nhật ký từ SQL lên Dashboard. */
    private void napDuLieu(JPanel page) {
        try {
            int tong = db.dem("SELECT COUNT(*) FROM dbo.TaiKhoan");
            int roles = db.dem("SELECT COUNT(*) FROM dbo.VaiTro");
            int active = db.dem("SELECT COUNT(*) FROM dbo.TaiKhoan "
                    + "WHERE trangThai=N'Hoạt động'");
            int alerts = db.dem("SELECT COUNT(*) FROM dbo.NhatKyHeThong "
                    + "WHERE (hanhDong LIKE N'%Lỗi%' OR hanhDong LIKE N'%Thất bại%') "
                    + "AND thoiGian >= DATEADD(day, -7, GETDATE())");

            JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
            grid.setOpaque(false);
            grid.setAlignmentX(LEFT_ALIGNMENT);
            grid.setPreferredSize(new Dimension(700, 255));
            grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 255));
            grid.add(stat("Tổng tài khoản", tong, BieuTuong.Loai.TAI_KHOAN,
                    GiaoDien.PRIMARY));
            grid.add(stat("Vai trò hệ thống", roles, BieuTuong.Loai.PHAN_QUYEN,
                    GiaoDien.GREEN));
            grid.add(stat("Đang hoạt động", active, BieuTuong.Loai.KIEM_TRA,
                    GiaoDien.WARN));
            grid.add(stat("Cảnh báo bảo mật (7 ngày)", alerts, BieuTuong.Loai.THONG_BAO,
                    GiaoDien.RED));
            page.add(grid);
            page.add(Box.createVerticalStrut(20));
            page.add(taoBieuDo());
            page.add(Box.createVerticalStrut(18));
            page.add(taoThongTinThem(tong));
        } catch (Exception exception) {
            page.add(GiaoDien.label("Không thể tải Dashboard: " + exception.getMessage(),
                    12, false));
        }
    }

    /** Tạo thẻ thống kê có tiêu đề, số liệu và biểu tượng. */
    private JPanel stat(String title, int value, BieuTuong.Loai icon, Color color) {
        JPanel card = GiaoDien.card(new BorderLayout(12, 0));
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 5));
        text.setOpaque(false);
        text.add(GiaoDien.label(title, 12, false));
        text.add(GiaoDien.label(String.valueOf(value), 27, true));
        card.add(text, BorderLayout.CENTER);

        JPanel iconBox = new JPanel(new GridLayout(1, 1));
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(44, 44));
        JLabel iconLabel = new JLabel(new BieuTuong(icon, color, 26));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.EAST);
        return card;
    }

    /** Tạo khu vực đồ thị thống kê tài khoản theo thời gian. */
    private JPanel taoBieuDo() {
        JPanel chart = GiaoDien.card(new BorderLayout(0, 12));
        chart.setAlignmentX(LEFT_ALIGNMENT);
        chart.setPreferredSize(new Dimension(700, 290));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(GiaoDien.label("Tăng trưởng tài khoản (lũy kế theo tháng)",
                15, true), BorderLayout.WEST);

        JComboBox<Integer> years = new JComboBox<>();
        int yearNow = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = yearNow - 4; year <= yearNow; year++) {
            years.addItem(year);
        }
        years.setSelectedItem(yearNow);
        head.add(years, BorderLayout.EAST);
        chart.add(head, BorderLayout.NORTH);

        JPanel bars = new JPanel(new GridLayout(1, 12, 9, 0));
        bars.setOpaque(false);
        bars.setPreferredSize(new Dimension(600, 190));
        chart.add(bars, BorderLayout.CENTER);
        years.addActionListener(event -> {
            Integer selected = (Integer) years.getSelectedItem();
            if (selected != null) {
                veCot(bars, selected);
            }
        });
        veCot(bars, yearNow);
        return chart;
    }

    /** Vẽ nội dung trong thành phần đồ họa Swing. */
    private void veCot(JPanel target, int year) {
        target.removeAll();
        try {
            int[] values = db.tangTruong(year);
            int max = 1;
            for (int value : values) {
                max = Math.max(max, value);
            }

            for (int index = 0; index < values.length; index++) {
                final int value = values[index];
                final int maximum = max;
                JPanel month = new JPanel(new BorderLayout());
                month.setOpaque(false);

                JComponent bar = new JComponent() {
                    @Override
                    /** Vẽ nội dung đồ họa của thành phần Swing. */
                    protected void paintComponent(Graphics graphics) {
                        Graphics2D g = (Graphics2D) graphics.create();
                        try {
                            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                            int height = (getHeight() - 9) * value / maximum;
                            if (value > 0) {
                                g.setColor(GiaoDien.PRIMARY);
                                g.fillRoundRect(2, getHeight() - height,
                                        Math.max(3, getWidth() - 4), height, 6, 6);
                            }
                        } finally {
                            g.dispose();
                        }
                    }
                };
                bar.setToolTipText(value + " tài khoản");
                month.add(bar, BorderLayout.CENTER);

                JLabel label = GiaoDien.label("T" + (index + 1), 10, false);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                month.add(label, BorderLayout.SOUTH);
                target.add(month);
            }
        } catch (Exception exception) {
            target.add(GiaoDien.label(exception.getMessage(), 11, false));
        }
        target.revalidate();
        target.repaint();
    }

    /** Dựng hai khu vực thông tin phụ ở cuối Dashboard quản trị. */
    private JPanel taoThongTinThem(int tongTaiKhoan) throws Exception {
        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);
        bottom.setAlignmentX(LEFT_ALIGNMENT);
        bottom.setPreferredSize(new Dimension(700, 320));
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel rolesCard = GiaoDien.card(new BorderLayout(0, 12));
        rolesCard.add(GiaoDien.label("Phân bố vai trò", 15, true), BorderLayout.NORTH);
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new javax.swing.BoxLayout(list, javax.swing.BoxLayout.Y_AXIS));
        for (String[] row : db.phanBo()) {
            int count = Integer.parseInt(row[1]);
            list.add(GiaoDien.label(row[0] + "  (" + count + ")", 11, false));
            JProgressBar progress = new JProgressBar(0, Math.max(1, tongTaiKhoan));
            progress.setValue(count);
            progress.setForeground(GiaoDien.PRIMARY);
            list.add(progress);
            list.add(Box.createVerticalStrut(12));
        }
        rolesCard.add(list, BorderLayout.CENTER);
        bottom.add(rolesCard);

        JPanel logCard = GiaoDien.card(new BorderLayout(0, 12));
        logCard.add(GiaoDien.label("Hoạt động quản trị gần đây", 15, true),
                BorderLayout.NORTH);
        JPanel logs = new JPanel();
        logs.setOpaque(false);
        logs.setLayout(new javax.swing.BoxLayout(logs, javax.swing.BoxLayout.Y_AXIS));
        int count = 0;
        for (String[] log : db.nhatKy()) {
            if (count++ >= 7) {
                break;
            }
            logs.add(GiaoDien.label(log[1] + " · " + log[0], 11, false));
            logs.add(Box.createVerticalStrut(13));
        }
        logCard.add(logs, BorderLayout.CENTER);
        bottom.add(logCard);
        return bottom;
    }
}
