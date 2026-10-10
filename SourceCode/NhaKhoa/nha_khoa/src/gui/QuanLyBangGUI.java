
package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.*;

abstract class QuanLyBangGUI extends BaseQuanLyGUI {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final String[] columns;
    private final int statusCol;
    private final int categoryCol;
    private final String heading;

    private final JTextField search;
    private final JComboBox<String> category;
    private final JComboBox<String> status;

    private final JLabel info = new JLabel();
    private final JLabel count = new JLabel();

    private static final Map<String, List<String[]>> DATA =
            new HashMap<>();

    QuanLyBangGUI(
            String tab,
            String heading,
            String description,
            String[] columns,
            String[][] initial,
            int categoryCol,
            int statusCol,
            String[] choices) {

        super(tab, heading);

        this.heading = heading;
        this.columns = columns;
        this.categoryCol = categoryCol;
        this.statusCol = statusCol;

        List<String[]> data = DATA.computeIfAbsent(tab, key -> {
            List<String[]> a = new ArrayList<>();

            for (String[] line : initial) {
                a.add(line.clone());
            }

            return a;
        });

        // Tiêu đề và nút thêm mới
        content.add(
            pageHeading(
                heading,
                heading,
                description,
                tab.equals("Nhân viên")
                    ? "Thêm nhân viên"
                    : "Thêm dịch vụ",
                e -> edit(-1)
            )
        );

        // Thống kê
        JPanel summary = new JPanel(new GridLayout(1, 3, 0, 0));

        summary.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 58)
        );
        summary.setAlignmentX(LEFT_ALIGNMENT);
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createLineBorder(LINE));

        count.setFont(new Font("Segoe UI", Font.BOLD, 17));
        count.setForeground(BLUE);

        summary.add(summaryCell(count, "Tổng số"));

        summary.add(
            summaryCell(
                new JLabel(tab.equals("Nhân viên") ? "4" : "4"),
                "Đang hoạt động"
            )
        );

        summary.add(
            summaryCell(
                new JLabel(tab.equals("Nhân viên") ? "1" : "1"),
                "Tạm nghỉ / Tạm ngưng"
            )
        );

        content.add(summary);
        content.add(BoxGap.vertical(16));

        // Tạo JTable
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);
        table.setRowHeight(53);

        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Tìm kiếm
        search = searchField(
            "Tìm theo mã, tên hoặc thông tin...", 300
        );

        search.getDocument().addDocumentListener(
            SharedListeners.document(this::filter)
        );

        // Lọc danh mục
        category = new JComboBox<>(choices);
        category.setBackground(Color.WHITE);
        category.addActionListener(e -> filter());

        // Lọc trạng thái
        status = new JComboBox<>(
            new String[] {
                "Tất cả trạng thái",
                tab.equals("Nhân viên")
                    ? "Đang làm việc"
                    : "Đang áp dụng",
                tab.equals("Nhân viên")
                    ? "Đang nghỉ phép"
                    : "Tạm ngưng",
                "Tạm ngưng"
            }
        );

        status.setBackground(Color.WHITE);
        status.addActionListener(e -> filter());

        // Xuất CSV
        JButton export = outlineButton("Xuất CSV");
        export.addActionListener(e -> export());

        JPanel bar = toolbar(
            search,
            Box.createHorizontalGlue(),
            category,
            status,
            export
        );

        // Khu vực bảng
        JPanel area = new JPanel(new BorderLayout());
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setOpaque(false);

        area.add(bar, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
            BorderFactory.createLineBorder(LINE)
        );

        scroll.setPreferredSize(
            new Dimension(
                900,
                Math.min(480, 48 + data.size() * 53)
            )
        );

        area.add(scroll, BorderLayout.CENTER);

        // Các nút chức năng
        JPanel actions = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 8, 5)
        );

        actions.setBackground(Color.WHITE);

        JButton add = primaryButton("+ Thêm");
        add.addActionListener(e -> edit(-1));
        actions.add(add);

        JButton edit = outlineButton("Sửa dòng chọn");

        edit.addActionListener(e -> {
            int r = table.getSelectedRow();

            if (r >= 0) {
                edit(table.convertRowIndexToModel(r));
            } else {
                warn();
            }
        });

        actions.add(edit);

        JButton remove = outlineButton("Xóa dòng chọn");

        remove.addActionListener(e -> {
            int r = table.getSelectedRow();

            if (r >= 0) {
                delete(table.convertRowIndexToModel(r));
            } else {
                warn();
            }
        });

        actions.add(remove);

        area.add(actions, BorderLayout.SOUTH);
        content.add(area);

        info.setForeground(MUTED);
        info.setBorder(
            BorderFactory.createEmptyBorder(13, 0, 0, 0)
        );

        content.add(info);

        // Nhấn Enter để sửa
        table.getInputMap(JComponent.WHEN_FOCUSED).put(
            KeyStroke.getKeyStroke("ENTER"),
            "edit"
        );

        table.getActionMap().put(
            "edit",
            new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int r = table.getSelectedRow();

                    if (r >= 0) {
                        edit(table.convertRowIndexToModel(r));
                    }
                }
            }
        );

        // Nhấp đúp vào dòng để sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();

                    if (r >= 0) {
                        edit(table.convertRowIndexToModel(r));
                    }
                }
            }
        });

        reload();
        showWindow();
    }

    // Tạo ô thống kê
    private JPanel summaryCell(JLabel value, String title) {
        JPanel p = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 12, 16)
        );

        p.setOpaque(false);

        value.setFont(
            new Font("Segoe UI", Font.BOLD, 16)
        );

        p.add(value);
        p.add(new JLabel(title));

        return p;
    }

    // Lấy danh sách dữ liệu
    private List<String[]> rows() {
        return DATA.get(
            heading.equals("Quản lý nhân viên")
                ? "Nhân viên"
                : "Dịch vụ nha khoa"
        );
    }

    // Nạp dữ liệu vào JTable
    private void reload() {
        model.setRowCount(0);

        for (String[] r : rows()) {
            model.addRow(r);
        }

        count.setText("" + rows().size());
        filter();
    }

    // Tìm kiếm và lọc dữ liệu
    private void filter() {
        if (sorter == null) {
            return;
        }

        String q = search.getText().trim();
        String cat = (String) category.getSelectedItem();
        String st = (String) status.getSelectedItem();

        List<RowFilter<Object, Object>> fs = new ArrayList<>();

        if (!q.isEmpty()) {
            fs.add(
                RowFilter.regexFilter(
                    "(?iu)" + Pattern.quote(q)
                )
            );
        }

        if (cat != null && !cat.startsWith("Tất cả")) {
            fs.add(
                RowFilter.regexFilter(
                    "^" + Pattern.quote(cat) + "$",
                    categoryCol
                )
            );
        }

        if (st != null && !st.startsWith("Tất cả")) {
            fs.add(
                RowFilter.regexFilter(
                    "^" + Pattern.quote(st) + "$",
                    statusCol
                )
            );
        }

        sorter.setRowFilter(
            fs.isEmpty()
                ? null
                : RowFilter.andFilter(fs)
        );

        info.setText(
            "Hiển thị " + table.getRowCount()
            + " / " + model.getRowCount()
            + " kết quả"
        );
    }

    // Thông báo chưa chọn dòng
    private void warn() {
        JOptionPane.showMessageDialog(
            this,
            "Hãy chọn một dòng trong bảng.",
            heading,
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Thêm hoặc sửa thông tin
    private void edit(int index) {
        boolean updating = index >= 0;

        String[] old = updating
            ? rows().get(index)
            : null;

        JPanel form = new JPanel(
            new GridLayout(columns.length, 2, 8, 10)
        );

        JTextField[] fields =
            new JTextField[columns.length];

        for (int i = 0; i < columns.length; i++) {
            form.add(new JLabel(columns[i]));

            fields[i] = new JTextField(
                updating ? old[i] : "",
                25
            );

            form.add(fields[i]);
        }

        int decision = JOptionPane.showConfirmDialog(
            this,
            form,
            updating ? "Sửa thông tin" : "Thêm mới",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (decision != JOptionPane.OK_OPTION) {
            return;
        }

        String[] values =
            new String[columns.length];

        for (int i = 0; i < columns.length; i++) {
            values[i] = fields[i].getText().trim();
        }

        // Kiểm tra dữ liệu bắt buộc
        if (values[0].isBlank() || values[1].isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "Mã và tên không được bỏ trống."
            );
            return;
        }

        // Kiểm tra trùng mã
        for (int i = 0; i < rows().size(); i++) {
            if (
                i != index &&
                rows().get(i)[0].equalsIgnoreCase(values[0])
            ) {
                JOptionPane.showMessageDialog(
                    this,
                    "Mã đã tồn tại."
                );
                return;
            }
        }

        if (updating) {
            rows().set(index, values);
        } else {
            rows().add(values);
        }

        reload();
    }

    // Xóa một dòng
    private void delete(int i) {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Xóa " + rows().get(i)[1] + "?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            rows().remove(i);
            reload();
        }
    }

    // Xuất dữ liệu CSV
    private void export() {
        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
            new File("medident.csv")
        );

        if (
            chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION
        ) {
            return;
        }

        try (
            Writer w = new OutputStreamWriter(
                new FileOutputStream(
                    chooser.getSelectedFile()
                ),
                StandardCharsets.UTF_8
            )
        ) {
            // UTF-8 BOM
            w.write('\uFEFF');

            for (int i = 0; i < columns.length; i++) {
                if (i > 0) {
                    w.write(',');
                }

                writeCell(w, columns[i]);
            }

            w.write("\n");

            for (String[] r : rows()) {
                for (int i = 0; i < r.length; i++) {
                    if (i > 0) {
                        w.write(',');
                    }

                    writeCell(w, r[i]);
                }

                w.write("\n");
            }

            JOptionPane.showMessageDialog(
                this,
                "Đã xuất dữ liệu CSV. "
            );

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Ghi một ô CSV, xử lý dấu ngoặc kép
    private void writeCell(Writer w, String v)
            throws IOException {

        w.write('"');
        w.write(v.replace("\"", "\"\""));
        w.write('"');
    }
}
