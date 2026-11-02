package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import UI.QuanTriVien.BaoCaoThongKe;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

abstract class BaseQuanLyGUI extends JPanel {
    static final Color BLUE = new Color(0, 139, 207);
    static final Color DARK_BLUE = new Color(0, 105, 163);
    static final Color TEXT = new Color(29, 41, 66);
    static final Color MUTED = new Color(112, 129, 155);
    static final Color LINE = new Color(225, 233, 242);
    static final Color BACKGROUND = new Color(246, 249, 252);
    static final Color GREEN = new Color(0, 164, 115);
    static final Color ORANGE = new Color(235, 151, 0);
    static final Color RED = new Color(224, 74, 74);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADING = new Font("Segoe UI", Font.BOLD, 25);
    private static MainWindow mainWindow;
    private final String activePage;
    private final String windowTitle;
    private final Map<String, JButton> navigationButtons = new HashMap<>();
    final JPanel content = new JPanel();

    BaseQuanLyGUI(String activePage, String windowTitle) {
        super(new BorderLayout());
        this.activePage = activePage;
        this.windowTitle = windowTitle;
        setBackground(BACKGROUND);

        content.setBackground(BACKGROUND);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(30, 34, 36, 34));
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);

        if (mainWindow == null) {
            mainWindow = new MainWindow(this);
        }
    }

    final void showWindow() {
        mainWindow.showPage(this);
    }

    protected final void openPage(String page) {
        switch (page) {
            case "Bệnh nhân" -> new BenhNhanGUI();
            case "Lịch hẹn" -> new LichHenGUI();
            case "Thanh toán & hóa đơn" -> new ThanhToanGUI();
            case "Báo cáo - thống kê" -> new BaoCaoThongKeGUI();
            default -> throw new IllegalArgumentException("Trang không được hỗ trợ: " + page);
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(22, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                BorderFactory.createEmptyBorder(11, 18, 11, 22)));
        header.setPreferredSize(new Dimension(0, 66));

        JLabel brand = new JLabel("<html><b style='font-size:17px;color:#173050'>"
                + "MEDIDENT</b><br>"
                + "<span style='font-size:10px;color:#8293a9'>Dental Management</span></html>");
        brand.setPreferredSize(new Dimension(194, 42));
        brand.setIconTextGap(9);
        brand.setIcon(new javax.swing.ImageIcon(createBrandIcon()));
        header.add(brand, BorderLayout.WEST);

        JTextField search = searchField("Tìm kiếm nhanh...", 275);
        search.setPreferredSize(new Dimension(275, 36));
        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search, BorderLayout.WEST);
        header.add(searchWrap, BorderLayout.CENTER);

        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new javax.swing.BoxLayout(profile, javax.swing.BoxLayout.X_AXIS));
        JLabel clinic = new JLabel("Quản lý phòng khám",
                GuiIcons.icon(GuiIcons.SHIELD, BLUE, 16), SwingConstants.LEFT);
        clinic.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clinic.setForeground(BLUE);
        clinic.setIconTextGap(6);
        profile.add(clinic);
        profile.add(BoxGap.horizontal(18));
        JLabel avatar = new JLabel("ND", SwingConstants.CENTER);
        avatar.setForeground(DARK_BLUE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        avatar.setPreferredSize(new Dimension(28, 32));
        profile.add(avatar);
        profile.add(BoxGap.horizontal(9));
        JLabel user = new JLabel("<html><b>Nguyễn Tấn Du</b><br>"
                + "<span style='color:#788ba3;font-size:10px'>240501 · Quản lý phòng khám</span></html>");
        user.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        profile.add(user);
        header.add(profile, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE));

        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new javax.swing.BoxLayout(items, javax.swing.BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createEmptyBorder(23, 12, 10, 12));
        addGroupLabel(items, "QUẢN LÝ PHÒNG KHÁM");
        addNavItem(items, GuiIcons.HOME, "Trang chủ Quản lý", null);
        addNavItem(items, GuiIcons.STAFF, "Nhân viên", null);
        addNavItem(items, GuiIcons.DENTAL_SERVICE, "Dịch vụ nha khoa", null);
        items.add(BoxGap.vertical(17));
        addGroupLabel(items, "VẬN HÀNH PHÒNG KHÁM");
        addNavItem(items, GuiIcons.PATIENT, "Bệnh nhân", "Bệnh nhân");
        addNavItem(items, GuiIcons.CALENDAR, "Lịch hẹn", "Lịch hẹn");
        addNavItem(items, GuiIcons.INVOICE, "Thanh toán & hóa đơn", "Thanh toán & hóa đơn");
        addNavItem(items, GuiIcons.REPORT, "Báo cáo - thống kê", "Báo cáo - thống kê");

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new javax.swing.BoxLayout(bottom, javax.swing.BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createEmptyBorder(12, 10, 14, 10));
        JPanel help = new JPanel(new BorderLayout(8, 3));
        help.setBackground(new Color(239, 248, 255));
        help.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(202, 232, 251)),
                BorderFactory.createEmptyBorder(9, 9, 9, 9)));
        JLabel helpIcon = new JLabel("?", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(BLUE);
                int diameter = Math.min(24, Math.min(getWidth(), getHeight()));
                g.fillOval((getWidth() - diameter) / 2, (getHeight() - diameter) / 2,
                        diameter, diameter);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        helpIcon.setOpaque(false);
        helpIcon.setForeground(Color.WHITE);
        helpIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        helpIcon.setPreferredSize(new Dimension(26, 26));
        JLabel helpText = new JLabel("<html><b>Cần hỗ trợ?</b><br>"
                + "<span style='font-size:11px;color:#71839b'>Liên hệ đội ngũ kỹ thuật</span>"
                + "<br><span style='font-size:11px;color:#008bcf'>Gửi yêu cầu</span></html>");
        helpText.setFont(BODY);
        help.add(helpIcon, BorderLayout.WEST);
        help.add(helpText, BorderLayout.CENTER);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(help);
        bottom.add(BoxGap.vertical(10));

        JPanel account = new JPanel(new BorderLayout(8, 0));
        account.setOpaque(false);
        account.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        account.setAlignmentX(Component.LEFT_ALIGNMENT);
        account.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        JLabel accountAvatar = new JLabel("ND", SwingConstants.CENTER);
        accountAvatar.setForeground(BLUE);
        accountAvatar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        accountAvatar.setPreferredSize(new Dimension(28, 34));
        account.add(accountAvatar, BorderLayout.WEST);
        JLabel accountDetails = new JLabel("<html><span style='font-size:9px;color:#8293a9'>"
                + "Đang đăng nhập</span><br>"
                + "<b style='font-size:11px;color:#173050'>Nguyễn Tấn Du</b><br>"
                + "<span style='font-size:9px;color:#8293a9'>240501</span><br>"
                + "<span style='font-size:10px;color:#008bcf'>Quản lý phòng khám</span></html>");
        account.add(accountDetails, BorderLayout.CENTER);
        bottom.add(account);
        bottom.add(BoxGap.vertical(7));

        JPanel clinicRow = new JPanel(new BorderLayout(8, 0));
        clinicRow.setOpaque(false);
        clinicRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        clinicRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel clinicBadge = new JLabel("AK", SwingConstants.CENTER);
        clinicBadge.setOpaque(true);
        clinicBadge.setBackground(new Color(232, 246, 255));
        clinicBadge.setForeground(DARK_BLUE);
        clinicBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        clinicBadge.setPreferredSize(new Dimension(28, 28));
        clinicRow.add(clinicBadge, BorderLayout.WEST);
        JLabel clinic = new JLabel("<html><b style='font-size:11px;color:#173050'>"
                + "Nhà khoa An Khang</b><br>"
                + "<span style='font-size:10px;color:#8293a9'>Chi nhánh Quận 1</span></html>");
        clinic.setFont(BODY);
        clinicRow.add(clinic, BorderLayout.CENTER);
        JButton clinicOptions = new JButton("···");
        clinicOptions.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clinicOptions.setForeground(MUTED);
        clinicOptions.setContentAreaFilled(false);
        clinicOptions.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        clinicOptions.setFocusPainted(false);
        clinicOptions.setToolTipText("Tùy chọn chi nhánh");
        clinicRow.add(clinicOptions, BorderLayout.EAST);
        bottom.add(clinicRow);
        bottom.add(BoxGap.vertical(5));

        JButton logout = flatButton("Đăng xuất", false);
        logout.setIcon(GuiIcons.icon(GuiIcons.LOGOUT, BLUE, 16));
        logout.setIconTextGap(9);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        bottom.add(logout);

        sidebar.add(items, BorderLayout.NORTH);
        sidebar.add(bottom, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addGroupLabel(JPanel items, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setForeground(MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 11, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        items.add(label);
    }

    private void addNavItem(JPanel items, String iconName, String label, String page) {
        boolean selected = label.equals(activePage);
        JButton button = new JButton(label, iconName == null ? null : GuiIcons.icon(iconName,
                selected ? DARK_BLUE : new Color(86, 108, 137), 17));
        button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 13));
        button.setForeground(selected ? DARK_BLUE : new Color(86, 108, 137));
        button.setBackground(selected ? new Color(225, 243, 255) : Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 9, 10, 7));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(12);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.putClientProperty("iconName", iconName);
        navigationButtons.put(label, button);
        if (page != null) {
            button.addActionListener(event -> openPage(page));
        }
        items.add(button);
        items.add(BoxGap.vertical(3));
    }

    final JPanel pageHeading(String breadcrumb, String title, String subtitle, String actionText,
            ActionListener action) {
        JPanel heading = new JPanel(new BorderLayout(12, 8));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel crumb = new JLabel("Tổng quan   ›   " + breadcrumb);
        crumb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        crumb.setForeground(MUTED);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADING);
        titleLabel.setForeground(TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 3, 0));
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED);
        text.add(crumb);
        text.add(titleLabel);
        text.add(subtitleLabel);
        heading.add(text, BorderLayout.CENTER);
        if (actionText != null) {
            boolean exportAction = "Xuất báo cáo".equals(actionText);
            JButton actionButton = primaryButton(exportAction ? actionText : "+   " + actionText);
            if (exportAction) {
                actionButton.setIcon(GuiIcons.icon(GuiIcons.DOWNLOAD, Color.WHITE, 17));
                actionButton.setIconTextGap(7);
            }
            if (action != null) {
                actionButton.addActionListener(action);
            }
            JPanel actionWrap = new JPanel(new BorderLayout());
            actionWrap.setOpaque(false);
            actionWrap.add(actionButton, BorderLayout.NORTH);
            heading.add(actionWrap, BorderLayout.EAST);
        }
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return heading;
    }

    final JPanel formHeading(String breadcrumb, String title, String subtitle, Runnable back) {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new javax.swing.BoxLayout(heading, javax.swing.BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton backButton = flatButton("‹   Quay lại", false);
        backButton.setHorizontalAlignment(SwingConstants.LEFT);
        backButton.addActionListener(event -> back.run());
        JLabel crumb = new JLabel("Tổng quan   ›   " + breadcrumb);
        crumb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        crumb.setForeground(MUTED);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(HEADING);
        titleLabel.setForeground(TEXT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(7, 0, 3, 0));
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(MUTED);
        heading.add(backButton);
        heading.add(BoxGap.vertical(7));
        heading.add(crumb);
        heading.add(titleLabel);
        heading.add(subtitleLabel);
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return heading;
    }

    final JPanel formCard(String step, String title, String subtitle, JPanel fields) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(18, 20, 20, 20)));
        JPanel sectionHeading = new JPanel(new BorderLayout(12, 0));
        sectionHeading.setOpaque(false);
        JLabel number = label(step, 12, true, BLUE);
        number.setHorizontalAlignment(SwingConstants.CENTER);
        number.setPreferredSize(new Dimension(25, 25));
        sectionHeading.add(number, BorderLayout.WEST);
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new javax.swing.BoxLayout(titleStack, javax.swing.BoxLayout.Y_AXIS));
        titleStack.add(label(title, 14, true, TEXT));
        titleStack.add(label(subtitle, 11, false, MUTED));
        sectionHeading.add(titleStack, BorderLayout.CENTER);
        card.add(sectionHeading, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    final JPanel formField(String title, JComponent input, boolean required) {
        JPanel field = new JPanel(new BorderLayout(0, 7));
        field.setOpaque(false);
        String labelText = required ? title + " *" : title;
        field.add(label(labelText, 12, true, TEXT), BorderLayout.NORTH);
        field.add(input, BorderLayout.CENTER);
        field.setMinimumSize(new Dimension(120, 64));
        return field;
    }

    final JPanel formRow(Component... fields) {
        JPanel row = new JPanel(new GridLayout(1, fields.length, 16, 0));
        row.setOpaque(false);
        for (Component field : fields) {
            row.add(field);
        }
        return row;
    }

    final JPanel verticalFields(int gap, JPanel... rows) {
        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new javax.swing.BoxLayout(fields, javax.swing.BoxLayout.Y_AXIS));
        for (int i = 0; i < rows.length; i++) {
            JPanel row = rows[i];
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    Math.max(75, row.getPreferredSize().height)));
            fields.add(row);
            if (i < rows.length - 1) {
                fields.add(BoxGap.vertical(gap));
            }
        }
        return fields;
    }

    final JScrollPane scrollableArea(JTextArea area) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.setPreferredSize(new Dimension(200, 76));
        return scroll;
    }

    final JTextField formTextField(String placeholder) {
        JTextField field = new PlaceholderTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setToolTipText(placeholder);
        field.setPreferredSize(new Dimension(180, 36));
        field.setBorder(fieldBorder());
        return field;
    }

    final JComboBox<String> formCombo(String... options) {
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setForeground(TEXT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(180, 36));
        combo.setBorder(fieldBorder());
        return combo;
    }

    final JTextArea formTextArea(String placeholder, int rows) {
        JTextArea area = new PlaceholderTextArea(placeholder, rows);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        area.setForeground(TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setToolTipText(placeholder);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return area;
    }

    final JPanel formFooter(JButton cancel, JButton submit) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(cancel);
        footer.add(submit);
        return footer;
    }

    final JPanel statCard(String iconName, String label, String value, String footnote, Color accent) {
        JPanel card = new JPanel(new BorderLayout(iconName == null ? 0 : 12, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        if (iconName != null) {
            JLabel symbol = new JLabel(GuiIcons.icon(iconName, accent, 20), SwingConstants.CENTER);
            symbol.setOpaque(true);
            symbol.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
            symbol.setForeground(accent);
            symbol.setFont(new Font("Segoe UI", Font.BOLD, 16));
            symbol.setPreferredSize(new Dimension(34, 60));
            card.add(symbol, BorderLayout.WEST);
        }
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new javax.swing.BoxLayout(labels, javax.swing.BoxLayout.Y_AXIS));
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        name.setForeground(MUTED);
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 21));
        number.setForeground(TEXT);
        number.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel foot = new JLabel(footnote);
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        foot.setForeground(accent);
        labels.add(name);
        labels.add(number);
        labels.add(foot);
        card.add(labels, BorderLayout.CENTER);
        return card;
    }

    final JPanel statRow(JPanel... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(0, 92));
        for (JPanel card : cards) {
            row.add(card);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        return row;
    }

    final JPanel toolbar(Component... components) {
        JPanel bar = new JPanel();
        bar.setBackground(Color.WHITE);
        bar.setLayout(new javax.swing.BoxLayout(bar, javax.swing.BoxLayout.X_AXIS));
        bar.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        for (int i = 0; i < components.length; i++) {
            if (i > 0) {
                bar.add(BoxGap.horizontal(9));
            }
            bar.add(components[i]);
        }
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        return bar;
    }

    final JTextField searchField(String placeholder, int width) {
        JTextField field = new SearchTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(width, 35));
        field.setMaximumSize(new Dimension(width, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(7, 32, 7, 9)));
        return field;
    }

    final JButton primaryButton(String text) {
        JButton button = new JButton(text);
        if (text.startsWith("+   ")) {
            button.setText(text.substring(4));
            button.setIcon(GuiIcons.icon(GuiIcons.ADD, Color.WHITE, 15));
            button.setIconTextGap(7);
        } else if (text.startsWith("+")) {
            button.setIcon(GuiIcons.icon(GuiIcons.ADD, Color.WHITE, 15));
            button.setIconTextGap(7);
        }
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(BLUE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return button;
    }

    final JButton outlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(MUTED);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        return button;
    }

    final JButton iconButton(String iconName, String toolTip) {
        JButton button = outlineButton("");
        button.setIcon(GuiIcons.icon(iconName, MUTED, 17));
        button.setPreferredSize(new Dimension(36, 35));
        button.setToolTipText(toolTip);
        return button;
    }

    final JButton filterButton() {
        JButton button = outlineButton("Bộ lọc");
        button.setIcon(GuiIcons.icon(GuiIcons.FILTER, MUTED, 16));
        button.setIconTextGap(7);
        return button;
    }

    final JButton flatButton(String text, boolean highlighted) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(highlighted ? DARK_BLUE : MUTED);
        button.setBackground(highlighted ? new Color(234, 246, 255) : Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return button;
    }

    final void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(48);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(LINE);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(235, 247, 255));
        table.setSelectionForeground(TEXT);
        table.setIntercellSpacing(new Dimension(0, 0));
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 10));
        header.setForeground(MUTED);
        header.setBackground(new Color(250, 252, 254));
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new WrappedTableCellRenderer(
                header.getFont(), header.getForeground(), header.getBackground(), 44));
        TableCellRenderer renderer = new WrappedTableCellRenderer(
                table.getFont(), table.getForeground(), table.getBackground(), table.getRowHeight());
        for (int column = 0; column < table.getColumnModel().getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        }
    }

    final JPanel tableCard(JTable table) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LINE));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);
        card.add(tableScroll, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    final JPanel whitePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(LINE));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    final JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size + 1));
        label.setForeground(color);
        return label;
    }

    final Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    private static final class WrappedTableCellRenderer extends JTextPane implements TableCellRenderer {
        private final int minimumRowHeight;

        private WrappedTableCellRenderer(Font font, Color foreground, Color background, int minimumRowHeight) {
            this.minimumRowHeight = minimumRowHeight;
            setEditable(false);
            setFocusable(false);
            setFont(font);
            setForeground(foreground);
            setBackground(background);
            setMargin(new Insets(3, 7, 3, 7));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            setText(value == null ? "" : value.toString());
            SimpleAttributeSet centered = new SimpleAttributeSet();
            StyleConstants.setAlignment(centered, StyleConstants.ALIGN_CENTER);
            StyledDocument document = getStyledDocument();
            document.setParagraphAttributes(0, document.getLength(), centered, false);
            Color background = selected ? table.getSelectionBackground()
                    : (row < 0 ? table.getTableHeader().getBackground() : table.getBackground());
            Color foreground = selected ? table.getSelectionForeground()
                    : (row < 0 ? table.getTableHeader().getForeground() : table.getForeground());
            setBackground(background);
            setForeground(foreground);
            if (column >= 0) {
                int availableWidth = Math.max(1, row < 0
                        ? table.getTableHeader().getColumnModel().getColumn(column).getWidth()
                        : table.getColumnModel().getColumn(column).getWidth());
                setSize(availableWidth, Short.MAX_VALUE);
                if (row >= 0) {
                    int requiredHeight = Math.max(minimumRowHeight, table.getRowHeight(row));
                    for (int cellColumn = 0; cellColumn < table.getColumnCount(); cellColumn++) {
                        setText(String.valueOf(table.getValueAt(row, cellColumn)));
                        setSize(Math.max(1, table.getColumnModel().getColumn(cellColumn).getWidth()),
                                Short.MAX_VALUE);
                        requiredHeight = Math.max(requiredHeight, getPreferredSize().height);
                    }
                    setText(value == null ? "" : value.toString());
                    setSize(availableWidth, Short.MAX_VALUE);
                    document = getStyledDocument();
                    document.setParagraphAttributes(0, document.getLength(), centered, false);
                    if (table.getRowHeight(row) != requiredHeight) {
                        table.setRowHeight(row, requiredHeight);
                    }
                }
            }
            return this;
        }
    }

    static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    static final class BoxGap {
        private BoxGap() { }

        static Component horizontal(int width) {
            return javax.swing.Box.createRigidArea(new Dimension(width, 1));
        }

        static Component vertical(int height) {
            return javax.swing.Box.createRigidArea(new Dimension(1, height));
        }
    }

    private static final class MainWindow extends javax.swing.JFrame {
        private final JPanel pageHolder = new JPanel(new BorderLayout());
        private final Map<String, JButton> navigationButtons;

        private MainWindow(BaseQuanLyGUI firstPage) {
            super(firstPage.windowTitle + " | MediDent");
            setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            setMinimumSize(new Dimension(1050, 700));
            setSize(1440, 900);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            getContentPane().setBackground(BACKGROUND);

            navigationButtons = firstPage.navigationButtons;
            add(firstPage.createHeader(), BorderLayout.NORTH);
            add(firstPage.createSidebar(), BorderLayout.WEST);
            pageHolder.setBackground(BACKGROUND);
            add(pageHolder, BorderLayout.CENTER);
        }

        private void showPage(BaseQuanLyGUI page) {
            pageHolder.removeAll();
            pageHolder.add(page, BorderLayout.CENTER);
            pageHolder.revalidate();
            pageHolder.repaint();
            setTitle(page.windowTitle + " | MediDent");
            updateNavigation(page.activePage);
            if (!isVisible()) {
                setVisible(true);
            }
        }

        private void updateNavigation(String activePage) {
            for (Map.Entry<String, JButton> entry : navigationButtons.entrySet()) {
                JButton button = entry.getValue();
                boolean selected = entry.getKey().equals(activePage);
                button.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 13));
                button.setForeground(selected ? DARK_BLUE : new Color(86, 108, 137));
                button.setBackground(selected ? new Color(225, 243, 255) : Color.WHITE);
                String iconName = (String) button.getClientProperty("iconName");
                button.setIcon(iconName == null ? null : GuiIcons.icon(iconName,
                        selected ? DARK_BLUE : new Color(86, 108, 137), 17));
            }
        }
    }

    private static BufferedImage createBrandIcon() {
        BufferedImage badge = new BufferedImage(38, 38, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = badge.createGraphics();
        graphics.setColor(BLUE);
        graphics.fillRoundRect(0, 0, 38, 38, 10, 10);
        GuiIcons.icon(GuiIcons.TOOTH_LOGO, Color.WHITE, 24).paintIcon(null, graphics, 7, 7);
        graphics.dispose();
        return badge;
    }

    private static final class SearchTextField extends JTextField {
        private final String placeholder;

        private SearchTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            GuiIcons.icon(GuiIcons.SEARCH, MUTED, 15).paintIcon(this, graphics, 9,
                    (getHeight() - 15) / 2);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D hint = (Graphics2D) graphics.create();
                hint.setColor(MUTED);
                hint.setFont(getFont());
                hint.drawString(placeholder, 31,
                        getHeight() / 2 + hint.getFontMetrics().getAscent() / 2 - 2);
                hint.dispose();
            }
        }
    }

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        private PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus() && !placeholder.isEmpty()) {
                Graphics2D hint = (Graphics2D) graphics.create();
                hint.setColor(MUTED);
                hint.setFont(getFont());
                hint.drawString(placeholder, getInsets().left + 2,
                        getHeight() / 2 + hint.getFontMetrics().getAscent() / 2 - 2);
                hint.dispose();
            }
        }
    }

    private static final class PlaceholderTextArea extends JTextArea {
        private final String placeholder;

        private PlaceholderTextArea(String placeholder, int rows) {
            super(rows, 20);
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty() && !hasFocus() && !placeholder.isEmpty()) {
                Graphics2D hint = (Graphics2D) graphics.create();
                hint.setColor(MUTED);
                hint.setFont(getFont());
                hint.drawString(placeholder, getInsets().left + 2,
                        getInsets().top + hint.getFontMetrics().getAscent());
                hint.dispose();
            }
        }
    }
}
