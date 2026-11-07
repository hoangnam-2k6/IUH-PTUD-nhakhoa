package UI.Chung;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import Dao.NghiepVu.QuyTacNghiepVu;

/**
 * Bảng quản lý dùng chung cho Nhân viên, Dịch vụ, Bệnh nhân, Lịch hẹn, Hóa đơn.
 * Dữ liệu luôn đọc từ SQL thông qua Supplier được truyền bởi JPanel nghiệp vụ.
 * Giao diện chỉ làm nhiệm vụ nhận/kiểm tra định dạng; kiểm tra quyền, trùng lịch,
 * trạng thái và transaction được thực hiện lại tại tầng Service và database.
 */
public class BangDuLieuPanel extends JPanel {

    public record Field(String name, String[] options, String preset) {
        public Field(String name) {
            this(name, null, "");
        }

        public Field(String name, String[] options) {
            this(name, options, "");
        }

        public Field(String name, String preset) {
            this(name, null, preset);
        }
    }

    @FunctionalInterface
    public interface Saver {
        void save(Map<String, String> values);
    }

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final JPanel listPanel = new JPanel(new BorderLayout());
    private final JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    private final JLabel status = GiaoDien.label("Chưa nạp dữ liệu", 12, false);

    private final JTable table;
    private final DefaultTableModel model;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final Supplier<List<Map<String, Object>>> fetch;
    private final String[] columns;
    private int[] minimumColumnWidths;
    private int lastViewportWidth = -1;

    protected final boolean readonly;

    private JPanel currentForm;
    private List<Map<String, Object>> rows = List.of();
    private int refreshNumber;
    private boolean saving;

    public BangDuLieuPanel(
            String title,
            String description,
            String icon,
            String[] columnNames,
            boolean readonly,
            Supplier<List<Map<String, Object>>> loader) {

        this.readonly = readonly;
        this.fetch = loader;
        this.columns = columnNames.clone();

        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Tự chia phần dư cho mọi cột, chỉ cuộn ngang khi không đủ chỗ.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        GiaoDien.styleTable(table);
        configureColumnWidths();

        sorter = new TableRowSorter<>(model);
        setupColumnSorters();
        table.setRowSorter(sorter);

        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(22, 24, 18, 24));
        main.add(heading(title, description, icon), BorderLayout.NORTH);
        main.add(dataSection(), BorderLayout.CENTER);
        main.add(footer(), BorderLayout.SOUTH);

        listPanel.setOpaque(false);
        listPanel.add(main, BorderLayout.CENTER);
        content.setOpaque(false);
        content.add(listPanel, "list");
        add(content, BorderLayout.CENTER);
    }

    private JPanel heading(String title, String description, String icon) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLabel = GiaoDien.label(title, 25, true);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(7));

        JLabel descriptionLabel = GiaoDien.label(description, 12, false);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(descriptionLabel);

        panel.add(titles, BorderLayout.WEST);
        panel.add(new JLabel(GiaoDien.icon(icon, 36)), BorderLayout.EAST);
        return panel;
    }

    private JPanel dataSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        // Một hàng duy nhất: tìm kiếm nằm trái, tất cả nút chức năng ở bên phải.
        // Nếu cửa sổ quá hẹp, phần nút được cuộn ngang thay vì xuống dòng.
        JPanel toolbar = GiaoDien.white(new BorderLayout(16, 0));
        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        search.setOpaque(false);
        search.add(GiaoDien.label("Tìm nhanh", 12, true));

        JTextField query = new JTextField();
        GiaoDien.styleTextField(query, 19);
        query.setPreferredSize(new Dimension(205, 42));
        query.setToolTipText("Tìm theo tên, số điện thoại, trạng thái, mã... (không phân biệt chữ hoa/thường)");
        search.add(query);
        toolbar.add(search, BorderLayout.WEST);

        actionButtons.setOpaque(false);
        actionButtons.setBorder(new EmptyBorder(0, 0, 0, 0));
        JButton reload = GiaoDien.button("Tải lại", false);
        reload.setToolTipText("Đọc dữ liệu mới nhất từ SQL Server");
        reload.addActionListener(e -> refresh());
        actionButtons.add(reload);

        JScrollPane actionScroll = new JScrollPane(
                actionButtons,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        actionScroll.setBorder(null);
        actionScroll.getViewport().setBackground(GiaoDien.WHITE);
        actionScroll.setPreferredSize(new Dimension(400, 49));
        actionScroll.setMinimumSize(new Dimension(120, 49));
        actionScroll.getHorizontalScrollBar().setUnitIncrement(30);
        toolbar.add(actionScroll, BorderLayout.CENTER);
        section.add(toolbar, BorderLayout.NORTH);

        query.getDocument().addDocumentListener(new DocumentListener() {
            private void change() {
                String searchText = query.getText().trim();
                sorter.setRowFilter(searchText.isEmpty()
                        ? null
                        : RowFilter.regexFilter("(?iu)" + Pattern.quote(searchText)));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                change();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                change();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                change();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(GiaoDien.BORDER));
        tableScroll.getViewport().setBackground(GiaoDien.WHITE);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(22);
        section.add(tableScroll, BorderLayout.CENTER);

        // JScrollPane đổi kích thước sẽ tính lại độ rộng cho các cột.
        // Nếu tổng chiều rộng tối thiểu nhỏ hơn màn hình thì chia đều phần dư;
        // ngược lại giữ độ rộng tối thiểu và chỉ khi đó mới có thanh cuộn ngang.
        tableScroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                SwingUtilities.invokeLater(() -> fitColumns(tableScroll.getViewport()));
            }
        });
        return section;
    }

    private JPanel footer() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(status, BorderLayout.WEST);
        footer.add(GiaoDien.label(
                readonly
                        ? "XEM TRƯỚC • Các thao tác ghi SQL yêu cầu đăng nhập"
                        : "DỮ LIỆU SQL • Vai trò Quản lý phòng khám",
                11,
                false), BorderLayout.EAST);
        return footer;
    }

    /** Độ rộng tối thiểu theo loại nội dung, ưu tiên họ tên/ngày/mã nghiệp vụ. */
    private void configureColumnWidths() {
        minimumColumnWidths = new int[columns.length];
        for (int index = 0; index < columns.length; index++) {
            String column = columns[index];
            int width;
            if (column.contains("Họ tên") || column.contains("Bệnh nhân")
                    || column.contains("Bác sĩ") || column.contains("Tên dịch vụ")) {
                width = 165;
            } else if (column.contains("Trạng thái")) {
                width = 130;
            } else if (column.contains("Địa chỉ") || column.contains("Tiền sử")
                    || column.contains("Mô tả") || column.contains("Lý do")) {
                width = 170;
            } else if (column.contains("Số tiền") || column.contains("Đơn giá")) {
                width = 145;
            } else if (column.contains("Email")) {
                width = 160;
            } else if (column.contains("SĐT")) {
                width = 120;
            } else if (column.contains("Ngày")) {
                width = 120;
            } else if (column.equals("ID") || column.equals("Phút")
                    || column.equals("Tuổi") || column.equals("Mã TT")) {
                width = 65;
            } else {
                width = 105;
            }
            minimumColumnWidths[index] = width;
            table.getColumnModel().getColumn(index).setMinWidth(55);
            table.getColumnModel().getColumn(index).setPreferredWidth(width);
        }
    }

    /** Có ít cột thì giãn kín khung; cột nhiều mới xuất hiện thanh cuộn ngang. */
    private void fitColumns(JViewport viewport) {
        int available = viewport.getExtentSize().width;
        if (available < 120 || available == lastViewportWidth || minimumColumnWidths == null) {
            return;
        }
        lastViewportWidth = available;

        int total = 0;
        for (int width : minimumColumnWidths) {
            total += width;
        }

        int extra = Math.max(0, available - total);
        int extraUsed = 0;
        for (int i = 0; i < minimumColumnWidths.length; i++) {
            int addition = i == minimumColumnWidths.length - 1
                    ? extra - extraUsed
                    : (int) ((long) extra * minimumColumnWidths[i] / total);
            extraUsed += addition;
            int size = minimumColumnWidths[i] + addition;
            var column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(size);
            column.setWidth(size);
        }
        table.revalidate();
        table.repaint();
    }

    private void setupColumnSorters() {
        Comparator<Object> numbers = (a, b) -> {
            BigDecimal x = a == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(a));
            BigDecimal y = b == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(b));
            return x.compareTo(y);
        };

        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            if (column.equals("ID") || column.contains("Số tiền")
                    || column.contains("Đơn giá") || column.equals("Tuổi")
                    || column.equals("Phút")) {
                sorter.setComparator(i, numbers);
            }
        }
    }

    public void action(String name, boolean primary, boolean modify, Runnable command) {
        JButton button = GiaoDien.button(name, primary);
        if (readonly && modify) {
            button.setEnabled(false);
            button.setToolTipText("Chạy trang riêng chỉ được xem dữ liệu; đăng nhập để ghi dữ liệu SQL.");
        } else {
            button.addActionListener(e -> {
                if (saving) {
                    return;
                }
                try {
                    command.run();
                } catch (Exception ex) {
                    GiaoDien.error(this, ex);
                }
            });
        }

        // Thao tác chính đứng trước, nút Tải lại đặt cuối cùng.
        int index = primary ? 0 : Math.max(0, actionButtons.getComponentCount() - 1);
        actionButtons.add(button, index);
        actionButtons.revalidate();
        actionButtons.repaint();
    }

    public void refresh() {
        int request = ++refreshNumber;
        status.setText("Đang đọc dữ liệu từ SQL Server...");

        new SwingWorker<List<Map<String, Object>>, Void>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                return fetch.get();
            }

            @Override
            protected void done() {
                if (request != refreshNumber) {
                    return; // Bỏ kết quả cũ khi người dùng đã tải lại lần mới hơn.
                }
                try {
                    rows = get();
                    model.setRowCount(0);
                    for (Map<String, Object> row : rows) {
                        Object[] cellValues = new Object[columns.length];
                        for (int i = 0; i < columns.length; i++) {
                            cellValues[i] = row.get(columns[i]);
                        }
                        model.addRow(cellValues);
                    }
                    status.setText("Đã tải " + rows.size() + " bản ghi");
                    // Khi tải dữ liệu mới, luôn đưa các cột nghiệp vụ ưu tiên về đầu bảng.
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        javax.swing.JScrollPane pane = (javax.swing.JScrollPane)
                                javax.swing.SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
                        if (pane != null) {
                            pane.getViewport().setViewPosition(new java.awt.Point(0, 0));
                            lastViewportWidth = -1;
                            fitColumns(pane.getViewport());
                        }
                    });
                } catch (Exception ex) {
                    status.setText("Không tải được SQL. Kiểm tra kết nối/cấu trúc database.");
                    GiaoDien.error(BangDuLieuPanel.this, ex);
                }
            }
        }.execute();
    }

    public Map<String, Object> selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            GiaoDien.ok(this, "Vui lòng chọn một dòng trong bảng trước khi thao tác.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow >= rows.size()) {
            GiaoDien.ok(this, "Dữ liệu đã thay đổi. Vui lòng tải lại bảng.");
            return null;
        }
        return rows.get(modelRow);
    }

    public int selectedId() {
        Map<String, Object> row = selected();
        return row == null ? 0 : ((Number) row.get("ID")).intValue();
    }

    public void confirm(String question, Runnable task) {
        if (saving) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, question, "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            runAsync(task);
        }
    }

    public void runAsync(Runnable task) {
        if (saving) {
            return;
        }
        saving = true;
        status.setText("Đang xử lý giao dịch SQL...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                task.run();
                return null;
            }

            @Override
            protected void done() {
                saving = false;
                try {
                    get();
                    cardLayout.show(content, "list");
                    refresh();
                    GiaoDien.ok(BangDuLieuPanel.this, "Thao tác thành công.");
                } catch (Exception ex) {
                    status.setText("Không thể lưu thay đổi. Kiểm tra thông báo lỗi.");
                    GiaoDien.error(BangDuLieuPanel.this, ex);
                }
            }
        }.execute();
    }

    public void form(String title, List<Field> fields, Map<String, Object> previous, Saver onSave) {
        JPanel formPanel = new JPanel(new BorderLayout(0, 18));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(22, 24, 18, 24));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.add(GiaoDien.label(title, 24, true), BorderLayout.WEST);

        JButton back = GiaoDien.button("Quay về danh sách", false);
        back.addActionListener(e -> cardLayout.show(content, "list"));
        heading.add(back, BorderLayout.EAST);
        formPanel.add(heading, BorderLayout.NORTH);

        JPanel box = GiaoDien.white(new BorderLayout(0, 14));
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(GiaoDien.WHITE);

        Map<String, JComponent> inputs = new LinkedHashMap<>();
        Map<String, JLabel> errors = new LinkedHashMap<>();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(6, 11, 6, 11);

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            int col = i % 2;
            int row = i / 2;

            constraints.gridx = col;
            constraints.gridy = row * 3;
            JLabel fieldLabel = GiaoDien.label(field.name() + (isRequired(title, field.name()) ? " *" : ""), 12, true);
            formGrid.add(fieldLabel, constraints);

            String original = previous == null
                    ? field.preset()
                    : String.valueOf(previous.getOrDefault(field.name(), field.preset()));
            if ("null".equals(original) || "—".equals(original)) {
                original = "";
            }

            JComponent input;
            if (field.options() != null) {
                JComboBox<String> combo = new JComboBox<>(field.options());
                GiaoDien.styleInput(combo);
                combo.setSelectedItem(original);
                combo.setPreferredSize(new Dimension(250, 40));
                input = combo;
            } else {
                JTextField textField = new JTextField(original);
                GiaoDien.styleTextField(textField, 22);
                if (field.name().contains("Ngày")) {
                    textField.setToolTipText("Nhập ngày theo định dạng yyyy-MM-dd (ví dụ: 2026-11-04)");
                } else if (field.name().contains("Giờ")) {
                    textField.setToolTipText("Nhập thời gian theo HH:mm (ví dụ: 08:30)");
                }
                input = textField;
            }

            inputs.put(field.name(), input);
            constraints.gridy = row * 3 + 1;
            formGrid.add(input, constraints);

            JLabel validation = new JLabel(" ");
            validation.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            validation.setForeground(new java.awt.Color(0xB91C1C));
            errors.put(field.name(), validation);
            constraints.gridy = row * 3 + 2;
            formGrid.add(validation, constraints);
        }

        JPanel note = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        note.setOpaque(false);
        note.add(GiaoDien.label("Các trường * bắt buộc. Ngày: yyyy-MM-dd • Giờ: HH:mm • Giá: số VND dương.", 12, false));
        box.add(note, BorderLayout.NORTH);
        JPanel formContent = new JPanel(new BorderLayout());
        formContent.setOpaque(false);
        formContent.add(formGrid, BorderLayout.NORTH);
        box.add(formContent, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        JButton cancel = GiaoDien.button("Hủy", false);
        JButton save = GiaoDien.button("Lưu vào SQL", true);

        cancel.addActionListener(e -> cardLayout.show(content, "list"));
        save.addActionListener(e -> {
            if (saving) {
                return;
            }

            Map<String, String> values = new LinkedHashMap<>();
            for (Map.Entry<String, JComponent> entry : inputs.entrySet()) {
                JComponent input = entry.getValue();
                String value = input instanceof JComboBox<?> combo
                        ? String.valueOf(combo.getSelectedItem())
                        : ((JTextComponent) input).getText().trim();
                values.put(entry.getKey(), value);
                errors.get(entry.getKey()).setText(" ");
            }

            String problemField = validateForm(title, values, errors);
            if (problemField != null) {
                inputs.get(problemField).requestFocusInWindow();
                return;
            }

            save.setEnabled(false);
            saving = true;
            status.setText("Đang ghi dữ liệu SQL...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    onSave.save(values);
                    return null;
                }

                @Override
                protected void done() {
                    saving = false;
                    save.setEnabled(true);
                    try {
                        get();
                        cardLayout.show(content, "list");
                        refresh();
                        GiaoDien.ok(BangDuLieuPanel.this, "Đã lưu dữ liệu thành công.");
                    } catch (Exception exception) {
                        GiaoDien.error(BangDuLieuPanel.this, exception);
                        status.setText("Lưu thất bại. Dữ liệu trên form được giữ lại để sửa.");
                    }
                }
            }.execute();
        });

        buttons.add(cancel);
        buttons.add(save);
        box.add(buttons, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(box);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(GiaoDien.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        formPanel.add(scroll, BorderLayout.CENTER);

        if (currentForm != null) {
            content.remove(currentForm);
        }
        currentForm = formPanel;
        content.add(formPanel, "form");
        cardLayout.show(content, "form");
    }

    private boolean isRequired(String title, String name) {
        if (Set.of("Email", "Mô tả", "Tiền sử bệnh", "Lý do").contains(name)) {
            return name.equals("Lý do") && title.toLowerCase().contains("hủy");
        }
        return true;
    }

    private String validateForm(String title, Map<String, String> values, Map<String, JLabel> errors) {
        String first = null;
        for (Map.Entry<String, String> field : values.entrySet()) {
            String name = field.getKey();
            String value = field.getValue();
            try {
                if (isRequired(title, name)) {
                    QuyTacNghiepVu.need(value != null && !value.isBlank(), name + " không được để trống");
                }

                if (value == null || value.isBlank()) {
                    continue;
                }

                if (name.equals("SĐT")) {
                    QuyTacNghiepVu.phone(value);
                } else if (name.equals("Email")) {
                    QuyTacNghiepVu.email(value);
                } else if (name.equals("Ngày sinh")) {
                    QuyTacNghiepVu.birth(value);
                } else if (name.equals("Ngày hẹn") || name.equals("Ngày")) {
                    QuyTacNghiepVu.date(value, name);
                } else if (name.contains("Giờ")) {
                    QuyTacNghiepVu.time(value, name);
                } else if (name.equals("Phút")) {
                    QuyTacNghiepVu.durationMinutes(value, name);
                } else if (name.startsWith("Mã ") && !name.equals("Mã giao dịch")) {
                    QuyTacNghiepVu.id(value, name);
                } else if (name.equals("Đơn giá")) {
                    QuyTacNghiepVu.amount(value);
                } else {
                    int max = switch (name) {
                        case "Địa chỉ" -> 255;
                        case "Tiền sử bệnh" -> 2000;
                        case "Mô tả", "Lý do", "Lý do quyết định" -> 500;
                        case "Mã giao dịch" -> 100;
                        case "Tên dịch vụ" -> 150;
                        default -> 120;
                    };
                    QuyTacNghiepVu.need(value.length() <= max, name + " tối đa " + max + " ký tự");
                }
            } catch (IllegalArgumentException ex) {
                errors.get(name).setText(ex.getMessage());
                if (first == null) {
                    first = name;
                }
            }
        }

        // Chặn lỗi chéo giữa các trường trước khi gửi SQL.
        if (first == null && values.containsKey("Giờ bắt đầu") && values.containsKey("Giờ kết thúc")) {
            LocalTime start = QuyTacNghiepVu.time(values.get("Giờ bắt đầu"), "Giờ bắt đầu");
            LocalTime end = QuyTacNghiepVu.time(values.get("Giờ kết thúc"), "Giờ kết thúc");
            if (!start.isBefore(end)) {
                errors.get("Giờ kết thúc").setText("Giờ kết thúc phải sau giờ bắt đầu");
                first = "Giờ kết thúc";
            }
        }
        if (first == null && values.containsKey("Ngày hẹn") && values.containsKey("Giờ")) {
            LocalDate date = QuyTacNghiepVu.date(values.get("Ngày hẹn"), "Ngày hẹn");
            LocalTime time = QuyTacNghiepVu.time(values.get("Giờ"), "Giờ");
            if (!date.atTime(time).isAfter(java.time.LocalDateTime.now())) {
                errors.get("Ngày hẹn").setText("Lịch hẹn phải ở tương lai");
                first = "Ngày hẹn";
            }
        }
        return first;
    }

    public void infoRows(String title, List<Map<String, Object>> data) {
        StringBuilder output = new StringBuilder();
        for (Map<String, Object> row : data) {
            row.forEach((key, value) -> output.append(key).append(": ")
                    .append(GiaoDien.display(value)).append("     "));
            output.append('\n');
        }

        JTextArea area = new JTextArea(
                output.isEmpty() ? "Không có dữ liệu." : output.toString(), 15, 70);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), title, JOptionPane.INFORMATION_MESSAGE);
    }
}
