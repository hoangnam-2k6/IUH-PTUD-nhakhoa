package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Trang chủ Lễ tân (chạy trực tiếp bằng main của file này). */
public class TrangChuLeTanGUI extends BaseQuanLyGUI {
    private static final String[][] EXAMS = {
        {"A-012", "Nguyễn Minh Anh", "Khám tổng quát · Phòng 02", "Đang khám"},
        {"A-013", "Trần Hoàng Nam", "Điều trị tủy · Phòng 04", "Chờ khám"},
        {"A-014", "Lê Thu Trang", "Niềng răng · Phòng 01", "Chờ khám"},
        {"A-015", "Phạm Đức Long", "Cạo vôi răng · Chưa chỉ định", "Đã tiếp nhận"}
    };
    private static final String[][] PATIENTS = {
        {"Nguyễn Minh Anh", "BN-2084", "090 123 4567"},
        {"Trần Hoàng Nam", "BN-2083", "098 764 3112"},
        {"Lê Thu Trang", "BN-2082", "091 422 9086"},
        {"Phạm Đức Long", "BN-2081", "093 677 1284"}
    };

    private final JPanel patientList = new JPanel();
    private final ButtonGroup group = new ButtonGroup();
    private final JComboBox<String> room =
            new JComboBox<>(new String[] {"Phòng khám 01", "Phòng khám 02", "Phòng khám 03", "Phòng khám 04"});
    private String selected = PATIENTS[0][0];
    private int ticket = 16;

    /** Mở giao diện Lễ tân cho tài khoản vừa đăng nhập (gọi từ màn hình đăng nhập). */
    public static void moTrangChu(KhoDuLieu db, TaiKhoan user) {
        BaseQuanLyGUI.batDauPhien(db, user);
        new TrangChuLeTanGUI();
    }

    /** Chạy riêng để xem thử giao diện, không cần đăng nhập. */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(TrangChuLeTanGUI::new);
    }

    public TrangChuLeTanGUI() {
        super("Trang chủ Lễ tân", "Trang chủ Lễ tân");
        content.add(pageHeading("Trang chủ Lễ tân", "Xin chào, " + tenNguoiDung(),
                "Theo dõi hoạt động tiếp nhận hôm nay tại một nơi.", null, null));
        content.add(statRow(
                statCard(GuiIcons.PATIENT, "Bệnh nhân hôm nay", "24", "+12% so với hôm qua", BLUE),
                statCard(GuiIcons.CALENDAR, "Đang chờ khám", "08", "Thời gian chờ ~18 phút", ORANGE),
                statCard(GuiIcons.CHECK, "Đã hoàn thành", "11", "Cập nhật 5 phút trước", GREEN),
                statCard(GuiIcons.INVOICE, "Đã thu hôm nay", "8,42 tr", "+14,2% so với hôm qua", RED)));
        content.add(BoxGap.vertical(16));

        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(LEFT_ALIGNMENT);
        columns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));
        columns.setPreferredSize(new Dimension(900, 430));
        columns.add(buildExamPanel());
        columns.add(buildReceptionPanel());
        content.add(columns);

        showWindow();
    }

    private JPanel buildExamPanel() {
        JPanel panel = whitePanel();
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JPanel title = new JPanel(new GridLayout(2, 1));
        title.setOpaque(false);
        title.add(label("Khám & điều trị", 15, true, TEXT));
        title.add(label("Danh sách phiếu khám hôm nay", 10, false, MUTED));
        top.add(title, BorderLayout.WEST);
        JButton all = flatButton("Xem tất cả  ›", true);
        all.addActionListener(event -> openPage("Lịch hẹn"));
        top.add(all, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(EXAMS.length, 1));
        rows.setBackground(Color.WHITE);
        rows.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINE));
        for (String[] e : EXAMS) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)));
            JLabel code = badge(e[0], BLUE);
            code.setPreferredSize(new Dimension(56, 36));
            row.add(code, BorderLayout.WEST);
            JPanel info = new JPanel(new GridLayout(2, 1));
            info.setOpaque(false);
            info.add(label(e[1], 12, true, TEXT));
            info.add(label(e[2], 10, false, MUTED));
            row.add(info, BorderLayout.CENTER);
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
            chip.setOpaque(false);
            chip.add(badge(e[3], statusColor(e[3])));
            row.add(chip, BorderLayout.EAST);
            rows.add(row);
        }
        panel.add(rows, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReceptionPanel() {
        JPanel panel = whitePanel();
        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
        JPanel title = new JPanel(new GridLayout(2, 1));
        title.setOpaque(false);
        title.add(label("Tiếp nhận bệnh nhân", 15, true, TEXT));
        title.add(label("Cấp số thứ tự và chỉ định phòng khám", 10, false, MUTED));
        JButton add = outlineButton("");
        add.setIcon(GuiIcons.icon(GuiIcons.ADD, BLUE, 15));
        add.setToolTipText("Thêm bệnh nhân mới");
        add.addActionListener(event -> openPage("Bệnh nhân"));
        JTextField search = searchField("Tìm theo tên hoặc số điện thoại...", 300);
        search.getDocument().addDocumentListener(SharedListeners.document(() -> refreshPatients(search.getText())));
        top.add(title, BorderLayout.NORTH);
        top.add(search, BorderLayout.CENTER);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(top, BorderLayout.CENTER);
        head.add(add, BorderLayout.EAST);
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        panel.add(head, BorderLayout.NORTH);

        patientList.setLayout(new javax.swing.BoxLayout(patientList, javax.swing.BoxLayout.Y_AXIS));
        patientList.setBackground(Color.WHITE);
        refreshPatients("");
        JScrollPane scroll = new JScrollPane(patientList);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, LINE));
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 4));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 14, 16));
        JPanel roomBox = new JPanel(new GridLayout(2, 1, 0, 3));
        roomBox.setOpaque(false);
        roomBox.add(label("Chỉ định phòng khám", 10, false, MUTED));
        room.setSelectedIndex(1);
        roomBox.add(room);
        JButton issue = primaryButton("Cấp số khám");
        issue.addActionListener(event -> issueTicket());
        bottom.add(roomBox, BorderLayout.CENTER);
        bottom.add(issue, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPatients(String keyword) {
        patientList.removeAll();
        group.clearSelection();
        String key = normalize(keyword.trim());
        for (String[] p : PATIENTS) {
            if (!key.isEmpty() && !normalize(p[0]).contains(key) && !p[2].replace(" ", "").contains(key)) {
                continue;
            }
            JRadioButton radio = new JRadioButton("<html><b>" + p[0] + "</b><br><span style='font-size:9px;color:#70819b'>"
                    + p[1] + " · " + p[2] + "</span></html>", p[0].equals(selected));
            radio.setBackground(Color.WHITE);
            radio.setHorizontalTextPosition(SwingConstants.LEFT);
            radio.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            radio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            radio.addActionListener(event -> selected = p[0]);
            group.add(radio);
            patientList.add(radio);
        }
        patientList.revalidate();
        patientList.repaint();
    }

    private void issueTicket() {
        JOptionPane.showMessageDialog(this,
                "Đã cấp số A-0" + ticket++ + " cho " + selected + "\n" + room.getSelectedItem(),
                "Cấp số khám", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel badge(String text, Color color) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        badge.setForeground(color);
        badge.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    private Color statusColor(String status) {
        return switch (status) {
            case "Đang khám" -> BLUE;
            case "Chờ khám" -> ORANGE;
            default -> GREEN;
        };
    }
}
