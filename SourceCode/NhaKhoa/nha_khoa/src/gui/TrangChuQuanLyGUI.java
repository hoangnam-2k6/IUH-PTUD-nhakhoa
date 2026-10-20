
package gui;

import java.awt.*;
import javax.swing.*;

public class TrangChuQuanLyGUI extends BaseQuanLyGUI {

    public TrangChuQuanLyGUI() {
        super("Trang chủ Quản lý", "Trang chủ Quản lý");

        content.add(
<<<<<<< Updated upstream
            pageHeading(
                "Trang chủ Quản lý",
                "Trang chủ Quản lý",
                "Tổng quan hoạt động và hiệu quả vận hành phòng khám.",
                null,
                null
            )
        );

        content.add(
            statRow(
                statCard(
                    GuiIcons.STAFF,
                    "Tổng nhân viên", "24",
                    "+2 trong tháng này", BLUE
                ),
                statCard(
                    GuiIcons.TOOTH,
                    "Tổng dịch vụ", "36",
                    "32 dịch vụ đang áp dụng", GREEN
                ),
                statCard(
                    GuiIcons.PATIENT,
                    "Bệnh nhân hôm nay", "38",
                    "+12,5% so với hôm qua", ORANGE
                ),
                statCard(
                    GuiIcons.INVOICE,
                    "Doanh thu hôm nay", "48,65 triệu",
                    "+14,2% so với hôm qua ", RED
                )
            )
=======
                pageHeading(
                        "Trang chủ Quản lý",
                        "Trang chủ Quản lý",
                        "Tổng quan hoạt động và hiệu quả vận hành phòng khám.",
                        null,
                        null
                )
        );

        content.add(
                statRow(
                        statCard(
                                GuiIcons.STAFF,
                                "Tổng nhân viên", "24",
                                "+2 trong tháng này", BLUE
                        ),
                        statCard(
                                GuiIcons.TOOTH,
                                "Tổng dịch vụ", "36",
                                "32 dịch vụ đang áp dụng", GREEN
                        ),
                        statCard(
                                GuiIcons.PATIENT,
                                "Bệnh nhân hôm nay", "38",
                                "+12,5% so với hôm qua", ORANGE
                        ),
                        statCard(
                                GuiIcons.INVOICE,
                                "Doanh thu hôm nay", "48,65 triệu",
                                "+14,2% so với hôm qua", RED
                        )
                )
>>>>>>> Stashed changes
        );

        content.add(BoxGap.vertical(16));

        JPanel middle = new JPanel(new BorderLayout(15, 0));
        middle.setOpaque(false);
        middle.setAlignmentX(LEFT_ALIGNMENT);

        // Biểu đồ doanh thu
        JPanel chart = new JPanel(new BorderLayout());
        chart.setBackground(Color.WHITE);
        chart.setBorder(
<<<<<<< Updated upstream
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(15, 15, 12, 15)
            )
        );

        chart.add(
            label("Doanh thu 12 tháng — Năm 2026 ", 14, true, TEXT),
            BorderLayout.NORTH
=======
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        BorderFactory.createEmptyBorder(15, 15, 12, 15)
                )
        );

        chart.add(
                label("Doanh thu 12 tháng — Năm 2026", 14, true, TEXT),
                BorderLayout.NORTH
>>>>>>> Stashed changes
        );

        JComponent bars = new JComponent() {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D d = (Graphics2D) g.create();

                d.setRenderingHint(
<<<<<<< Updated upstream
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                int[] vals = {
                    300, 420, 330, 560, 460, 610,
                    490, 670, 540, 730, 630, 770
=======
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int[] vals = {
                        300, 420, 330, 560, 460, 610,
                        490, 670, 540, 730, 630, 770
>>>>>>> Stashed changes
                };

                int left = 40;
                int bottom = getHeight() - 30;
                int top = 32;

                // Vẽ các đường lưới và trục doanh thu
                for (int k = 0; k <= 4; k++) {
                    int y = bottom - (bottom - top) * k / 4;

                    d.setColor(LINE);
                    d.drawLine(left, y, getWidth() - 10, y);

                    d.setColor(MUTED);
                    d.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    d.drawString((k * 200) + "M", 4, y + 3);
                }

                // Vẽ 12 cột doanh thu
                int avail = getWidth() - left - 25;
                int slot = avail / 12;

                for (int i = 0; i < 12; i++) {
                    int h = vals[i] * (bottom - top) / 800;

                    d.setColor(new Color(17, 158, 224));

                    d.fillRoundRect(
<<<<<<< Updated upstream
                        left + i * slot + slot / 4,
                        bottom - h,
                        Math.max(8, slot / 2),
                        h,
                        5,
                        5
=======
                            left + i * slot + slot / 4,
                            bottom - h,
                            Math.max(8, slot / 2),
                            h,
                            5,
                            5
>>>>>>> Stashed changes
                    );

                    d.setColor(MUTED);

                    d.drawString(
<<<<<<< Updated upstream
                        "T" + (i + 1),
                        left + i * slot + slot / 3,
                        bottom + 17
=======
                            "T" + (i + 1),
                            left + i * slot + slot / 3,
                            bottom + 17
>>>>>>> Stashed changes
                    );
                }

                d.dispose();
            }
        };

        bars.setPreferredSize(new Dimension(650, 310));
        chart.add(bars, BorderLayout.CENTER);
        middle.add(chart, BorderLayout.CENTER);

        // Hoạt động gần đây
        JPanel feed = new JPanel();
        feed.setBackground(Color.WHITE);
        feed.setLayout(new BoxLayout(feed, BoxLayout.Y_AXIS));

        feed.setBorder(
<<<<<<< Updated upstream
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            )
=======
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                )
>>>>>>> Stashed changes
        );

        feed.add(label("Hoạt động gần đây", 14, true, TEXT));
        feed.add(BoxGap.vertical(16));

        String[] activities = {
<<<<<<< Updated upstream
            "✓   Nguyễn Minh Anh đã hoàn tất thanh toán",
            "◷   Lịch hẹn mới được tạo — Trần Quốc Huy",
            "✓   Nhân viên cập nhật trạng thái nghỉ phép",
            "◷   Dịch vụ mới được cập nhật"
=======
                "✓   Nguyễn Minh Anh đã hoàn tất thanh toán",
                "◷   Lịch hẹn mới được tạo — Trần Quốc Huy",
                "✓   Nhân viên cập nhật trạng thái nghỉ phép",
                "◷   Dịch vụ mới được cập nhật"
>>>>>>> Stashed changes
        };

        for (String line : activities) {
            JLabel item = label(line, 11, false, TEXT);

            item.setBorder(
<<<<<<< Updated upstream
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    BorderFactory.createEmptyBorder(17, 0, 17, 0)
                )
=======
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                            BorderFactory.createEmptyBorder(17, 0, 17, 0)
                    )
>>>>>>> Stashed changes
            );

            feed.add(item);
        }

        feed.setPreferredSize(new Dimension(330, 350));
        middle.add(feed, BorderLayout.EAST);

        content.add(middle);
        content.add(BoxGap.vertical(16));

        // Lịch khám hôm nay
        JPanel agenda = new JPanel(new BorderLayout());
        agenda.setAlignmentX(LEFT_ALIGNMENT);
        agenda.setBackground(Color.WHITE);

        agenda.setBorder(
<<<<<<< Updated upstream
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        );

        agenda.add(
            label("Lịch khám hôm nay", 14, true, TEXT),
            BorderLayout.NORTH
=======
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LINE),
                        BorderFactory.createEmptyBorder(16, 16, 16, 16)
                )
        );

        agenda.add(
                label("Lịch khám hôm nay", 14, true, TEXT),
                BorderLayout.NORTH
>>>>>>> Stashed changes
        );

        JPanel appointments = new JPanel(new GridLayout(1, 4, 12, 0));
        appointments.setOpaque(false);

        String[] bookings = {
<<<<<<< Updated upstream
            "08:00|Nguyễn Minh Anh|Đã xác nhận",
            "08:30|Trần Quốc Huy|Đang chờ",
            "09:15|Lê Thị Thu Hà|Đang khám",
            "10:30|Phạm Hoàng Nam| Chưa xác nhận"
=======
                "08:00|Nguyễn Minh Anh|Đã xác nhận",
                "08:30|Trần Quốc Huy|Đang chờ",
                "09:15|Lê Thị Thu Hà|Đang khám",
                "10:30|Phạm Hoàng Nam|Chưa xác nhận"
>>>>>>> Stashed changes
        };

        for (String entry : bookings) {
            String[] a = entry.split("\\|");

            JPanel cell = new JPanel(new GridLayout(3, 1));
            cell.setBackground(Color.WHITE);
            cell.setBorder(
<<<<<<< Updated upstream
                BorderFactory.createEmptyBorder(12, 8, 8, 8)
=======
                    BorderFactory.createEmptyBorder(12, 8, 8, 8)
>>>>>>> Stashed changes
            );

            cell.add(label(a[0], 13, true, BLUE));
            cell.add(label(a[1], 11, true, TEXT));
            cell.add(label(a[2], 10, false, MUTED));

            appointments.add(cell);
        }

        agenda.add(appointments, BorderLayout.CENTER);
        content.add(agenda);

        showWindow();
    }
}
