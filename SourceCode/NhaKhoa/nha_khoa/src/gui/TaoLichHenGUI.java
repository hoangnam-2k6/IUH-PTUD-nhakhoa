package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TaoLichHenGUI extends BaseQuanLyGUI {
    private final javax.swing.JComboBox<String> patient = formCombo(
            "Chọn bệnh nhân", "Nguyễn Minh Anh · BN-00241", "Trần Quốc Huy · BN-00240",
            "Lê Thị Thu Hà · BN-00239", "Phạm Hoàng Nam · BN-00238", "Võ Ngọc Linh · BN-00237");
    private final javax.swing.JComboBox<String> service = formCombo(
            "Chỉnh nha định kỳ", "Khám tổng quát", "Điều trị tủy", "Tẩy trắng răng", "Lấy cao răng");
    private final JTextField date = filledField("19/06/2026");
    private final JTextField startTime = filledField("09:30");
    private final javax.swing.JComboBox<String> duration = formCombo(
            "30 phút", "45 phút", "60 phút", "90 phút");
    private final javax.swing.JComboBox<String> dentist = formCombo(
            "BS. Hoàng Việt", "BS. Minh Thắng", "BS. Tuấn Anh");
    private final javax.swing.JComboBox<String> room = formCombo("Phòng 02", "Phòng 01", "Phòng 03");
    private final JLabel summaryDate = label("09:30 – 10:00", 11, true, TEXT);
    private final JLabel summaryService = label("Chỉnh nha định kỳ", 11, true, TEXT);
    private final JLabel summaryDentist = label("BS. Hoàng Việt", 11, true, TEXT);
    private final JLabel summaryRoom = label("Phòng 02", 11, true, TEXT);
    private final JLabel previewText = label("", 11, false, TEXT);
    private final JLabel dateBadgeDay = label("19", 30, true, Color.WHITE);
    private final JLabel dateBadgeMonth = label("THÁNG 06, 2026", 9, false, Color.WHITE);

    public TaoLichHenGUI() {
        super("Lịch hẹn", "Tạo lịch hẹn mới");
        patient.setSelectedIndex(1);
        content.add(formHeading("Lịch hẹn   ›   Tạo lịch hẹn", "Tạo lịch hẹn mới",
                "Sắp xếp thời gian khám, dịch vụ và bác sĩ phụ trách.", this::goBack));

        JPanel patientFields = new JPanel(new BorderLayout(0, 12));
        patientFields.setOpaque(false);
        patientFields.add(formField("Bệnh nhân", patient, true), BorderLayout.NORTH);
        patientFields.add(patientPreview(), BorderLayout.CENTER);
        JPanel appointmentFields = verticalFields(12,
                formRow(formField("Dịch vụ", service, true)),
                formRow(
                        formField("Ngày hẹn", date, true),
                        formField("Giờ bắt đầu", startTime, true)),
                formRow(
                        formField("Thời lượng", duration, false),
                        formField("Bác sĩ phụ trách", dentist, true)),
                formRow(formField("Phòng khám", room, false)),
                formRow(formField("Ghi chú lịch hẹn", scrollableArea(
                        formTextArea("Nội dung cần chuẩn bị hoặc lưu ý cho bác sĩ...", 3)), false)));

        JPanel mainColumn = new JPanel();
        mainColumn.setOpaque(false);
        mainColumn.setLayout(new javax.swing.BoxLayout(mainColumn, javax.swing.BoxLayout.Y_AXIS));
        mainColumn.add(formCard("1", "Chọn bệnh nhân",
                "Tìm hồ sơ bệnh nhân có hoặc tạo hồ sơ mới.", patientFields));
        mainColumn.add(BoxGap.vertical(14));
        mainColumn.add(formCard("2", "Thông tin lịch hẹn",
                "Chọn dịch vụ, thời gian và nhân sự phụ trách.", appointmentFields));
        mainColumn.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel columns = new JPanel(new BorderLayout(18, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);
        columns.add(mainColumn, BorderLayout.CENTER);
        columns.add(summaryCard(), BorderLayout.EAST);
        content.add(columns);
        content.add(BoxGap.vertical(14));
        javax.swing.JButton cancel = flatButton("Hủy bỏ", false);
        cancel.addActionListener(event -> goBack());
        javax.swing.JButton save = primaryButton("+   Tạo lịch hẹn");
        save.addActionListener(event -> saveAppointment());
        content.add(formFooter(cancel, save));

        patient.addActionListener(event -> updatePatientPreview());
        service.addActionListener(event -> summaryService.setText((String) service.getSelectedItem()));
        dentist.addActionListener(event -> summaryDentist.setText((String) dentist.getSelectedItem()));
        room.addActionListener(event -> summaryRoom.setText((String) room.getSelectedItem()));
        date.getDocument().addDocumentListener(SharedListeners.document(this::updateTimeSummary));
        date.getDocument().addDocumentListener(SharedListeners.document(this::updateDateSummary));
        startTime.getDocument().addDocumentListener(SharedListeners.document(this::updateTimeSummary));
        duration.addActionListener(event -> updateTimeSummary());
        updatePatientPreview();
        showWindow();
    }

    private JPanel patientPreview() {
        JPanel preview = new JPanel(new BorderLayout(10, 0));
        preview.setBackground(new Color(248, 251, 254));
        preview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(11, 12, 11, 12)));
        JLabel initials = label("NA", 11, true, DARK_BLUE);
        initials.setHorizontalAlignment(JLabel.CENTER);
        initials.setOpaque(true);
        initials.setBackground(new Color(220, 241, 255));
        initials.setPreferredSize(new Dimension(34, 34));
        preview.add(initials, BorderLayout.WEST);
        previewText.setText("<html><b>Nguyễn Minh Anh</b><br>"
                + "<span style='color:#71839b'>BN-00241 · 090 123 4567</span></html>");
        preview.add(previewText, BorderLayout.CENTER);
        return preview;
    }

    private JPanel summaryCard() {
        JPanel card = new JPanel(new BorderLayout(0, 13));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setPreferredSize(new Dimension(250, 270));
        card.setMaximumSize(new Dimension(280, Integer.MAX_VALUE));
        card.setAlignmentY(Component.TOP_ALIGNMENT);
        card.add(label("Tóm tắt lịch hẹn", 14, true, TEXT), BorderLayout.NORTH);
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new javax.swing.BoxLayout(details, javax.swing.BoxLayout.Y_AXIS));
        JPanel dateBadge = new JPanel();
        dateBadge.setLayout(new javax.swing.BoxLayout(dateBadge, javax.swing.BoxLayout.Y_AXIS));
        dateBadge.setBackground(BLUE);
        dateBadge.setBorder(BorderFactory.createEmptyBorder(17, 12, 17, 12));
        dateBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel today = label("THỨ SÁU", 9, true, Color.WHITE);
        today.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateBadgeDay.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateBadgeMonth.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateBadge.add(today);
        dateBadge.add(dateBadgeDay);
        dateBadge.add(dateBadgeMonth);
        details.add(dateBadge);
        details.add(BoxGap.vertical(18));
        details.add(summaryLine("Thời gian", summaryDate));
        details.add(BoxGap.vertical(13));
        details.add(summaryLine("Dịch vụ", summaryService));
        details.add(BoxGap.vertical(13));
        details.add(summaryLine("Bác sĩ", summaryDentist));
        details.add(BoxGap.vertical(13));
        details.add(summaryLine("Phòng", summaryRoom));
        card.add(details, BorderLayout.CENTER);
        JLabel availability = label("Không trùng lịch đã đặt", 10, true, GREEN);
        availability.setIcon(GuiIcons.icon(GuiIcons.CHECK, GREEN, 13));
        availability.setIconTextGap(6);
        card.add(availability, BorderLayout.SOUTH);
        return card;
    }

    private JPanel summaryLine(String caption, JLabel value) {
        JPanel line = new JPanel(new BorderLayout(4, 4));
        line.setOpaque(false);
        line.add(label(caption, 10, false, MUTED), BorderLayout.NORTH);
        line.add(value, BorderLayout.CENTER);
        return line;
    }

    private JTextField filledField(String value) {
        JTextField field = formTextField("");
        field.setText(value);
        return field;
    }

    private void updatePatientPreview() {
        String selected = (String) patient.getSelectedItem();
        if (selected != null && patient.getSelectedIndex() > 0) {
            String[] phones = {"", "090 123 4567", "091 856 2440", "093 435 6712",
                "093 728 4105", "097 512 3068"};
            previewText.setText("<html><b>" + selected.substring(0, selected.indexOf(" · "))
                    + "</b><br><span style='color:#71839b'>" + selected.substring(selected.indexOf(" · ") + 3)
                    + " · " + phones[patient.getSelectedIndex()] + "</span></html>");
        }
    }

    private void updateTimeSummary() {
        String time = startTime.getText().trim();
        String length = (String) duration.getSelectedItem();
        int minutes = Integer.parseInt(length.substring(0, length.indexOf(' ')));
        String[] parts = time.split(":");
        try {
            int totalMinutes = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]) + minutes;
            summaryDate.setText(time + " – " + String.format("%02d:%02d",
                    (totalMinutes / 60) % 24, totalMinutes % 60));
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            summaryDate.setText(time);
        }
    }

    private void updateDateSummary() {
        String[] parts = date.getText().trim().split("/");
        if (parts.length == 3) {
            try {
                dateBadgeDay.setText(String.valueOf(Integer.parseInt(parts[0])));
                dateBadgeMonth.setText("THÁNG " + parts[1] + ", " + parts[2]);
            } catch (NumberFormatException exception) {
                dateBadgeDay.setText("--");
                dateBadgeMonth.setText("NGÀY HẸN");
            }
        }
    }

    private void saveAppointment() {
        if (patient.getSelectedIndex() == 0 || date.getText().isBlank()
                || startTime.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân và nhập ngày, giờ hẹn.",
                    "Thiếu thông tin bắt buộc", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Thông tin lịch hẹn đã được kiểm tra.",
                "Tạo lịch hẹn", JOptionPane.INFORMATION_MESSAGE);
        goBack();
    }

    private void goBack() {
        openPage("Lịch hẹn");
    }
}
