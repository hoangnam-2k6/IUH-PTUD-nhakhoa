package UI.LeTan;

import Class.TaiKhoan;
import ConnectDB.KetNoi;
import DAO.KhoDuLieu;
import UI.Chung.GiaoDien;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * TRANG CHỦ LỄ TÂN - MEDIDENT.
 *
 * Một màn hình, chia hai khu vực:
 * 1) Khám & điều trị: chỉ xem phiếu khám có thật trong SQL Server.
 * 2) Tiếp nhận: tìm bệnh nhân, cấp số thứ tự theo ngày và chỉ định phòng.
 *
 * Khung header / sidebar nằm ở UI.Chung.KhungChinh, không tạo JFrame mới.
 * Cần chạy SQLData/06_TiepNhanLeTan_SQL2012.sql MỘT LẦN trước khi dùng.
 *
 * Quyền:
 * - BENHNHAN: xem hồ sơ bệnh nhân và phiếu khám.
 * - LICHHEN: tiếp nhận và cấp số khám (kiểm tra lại trong transaction SQL).
 *
 * Bản sửa:
 * - Mã phiếu, Mã BN, Mã NV (cả tiêu đề cột và dữ liệu) căn giữa.
 * - Trang luôn vừa khít khung nhìn (implements Scrollable), không cuộn cả trang;
 *   chỉ từng bảng có thanh cuộn riêng.
 */
public final class TrangChuLeTan extends JPanel implements Scrollable {

    private static final Color PRIMARY = new Color(0x0284C7);
    private static final Color PRIMARY_DARK = new Color(0x0369A1);
    private static final Color PRIMARY_LIGHT = new Color(0xE0F2FE);
    private static final Color TEXT = new Color(0x0F172A);
    private static final Color MUTED = new Color(0x64748B);
    private static final Color BORDER = new Color(0xE2E8F0);
    private static final Color BACKGROUND = new Color(0xF1F5F9);
    private static final Color ROW_ALT = new Color(0xF8FAFC);
    private static final Color ROW_HOVER = new Color(0xF0F9FF);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color WARNING = new Color(0xF59E0B);
    private static final Color DANGER = new Color(0xEF4444);

    // Màu avatar xoay vòng theo tên.
    private static final Color[] AVATAR_COLORS = {
        new Color(0x0284C7), new Color(0x10B981), new Color(0xF59E0B),
        new Color(0x8B5CF6), new Color(0xEC4899), new Color(0x14B8A6)
    };

    // Danh sách phòng cố định để người sử dụng không nhập sai ký tự.
    private static final String[] ROOMS = {
        "Phòng 01", "Phòng 02", "Phòng 03", "Phòng 04",
        "Phòng 05", "Phòng 06", "Phòng 07", "Phòng 08"
    };

    private final KhoDuLieu db;
    private final TaiKhoan user;
    private final DefaultTableModel phieuModel;
    private final DefaultTableModel benhNhanModel;
    private final JTable phieuTable;
    private final JTable benhNhanTable;
    private final TableRowSorter<DefaultTableModel> phieuSorter;
    private final TableRowSorter<DefaultTableModel> benhNhanSorter;

    private final JComboBox<String> phongChon = new JComboBox<>(ROOMS);
    private final JButton tiepNhanButton = button("Tiếp nhận / Cấp số", true);
    private final JButton refreshButton = new RoundButton("Làm mới", 2);
    private final JLabel chonBenhNhan = label("Chọn một bệnh nhân trong bảng", 12, false);
    private final JLabel trangThai = label("", 11, false);
    private final JLabel tongBenhNhan = bigValue("—");
    private final JLabel choTiepNhan = bigValue("—");
    private final JLabel daTiepNhan = bigValue("—");
    private final JLabel soPhieuKham = bigValue("—");
    private final Pill phieuCount = new Pill("0");
    private final Pill benhNhanCount = new Pill("0");

    private boolean loading;
    private boolean canView;
    private boolean canReceive;

    public TrangChuLeTan(KhoDuLieu db, TaiKhoan user) {
        this.db = db;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Đặt mã phiếu, mã bệnh nhân và mã bác sĩ ở đầu để dễ nhìn trong khung chia đôi.
        // Các thuộc tính còn lại xem bằng cuộn ngang hoặc nhấp đúp để mở chi tiết.
        phieuModel = model(
                "STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Mã NV", "Bác sĩ",
                "Ngày khám", "Trạng thái", "Triệu chứng", "Chẩn đoán",
                "Phương pháp điều trị", "Kết quả", "Ghi chú");
        benhNhanModel = model(
                "STT", "Mã BN", "Họ tên", "Ngày sinh", "Giới tính", "SĐT",
                "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái", "Số khám", "Phòng");
        phieuTable = table(phieuModel);
        benhNhanTable = table(benhNhanModel);
        phieuSorter = new TableRowSorter<>(phieuModel);
        benhNhanSorter = new TableRowSorter<>(benhNhanModel);
        phieuTable.setRowSorter(phieuSorter);
        benhNhanTable.setRowSorter(benhNhanSorter);
        phieuSorter.setSortable(0, false);
        benhNhanSorter.setSortable(0, false);
        setupStt(phieuTable);
        setupStt(benhNhanTable);
        // Mã phiếu / Mã BN: in đậm, màu chủ đạo, căn giữa.
        idColumn(phieuTable, 1);
        idColumn(benhNhanTable, 1);
        // Mã BN và Mã NV trong bảng phiếu khám: căn giữa.
        centerColumn(phieuTable, 2);
        centerColumn(phieuTable, 4);
        avatarColumn(phieuTable, 3);
        avatarColumn(benhNhanTable, 2);
        badgeColumn(phieuTable, 7, false);
        badgeColumn(benhNhanTable, 9, false);
        badgeColumn(benhNhanTable, 10, true);

        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 20, 24));
        add(page, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 16));
        upper.setOpaque(false);
        upper.add(titlePanel(), BorderLayout.NORTH);
        upper.add(statsPanel(), BorderLayout.CENTER);
        page.add(upper, BorderLayout.NORTH);

        JPanel reception = receptionPanel();
        JPanel treatment = treatmentPanel();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treatment, reception);
        // Thanh chia chỉ là khoảng trống cùng màu nền, bỏ viền và nút mũi tên mặc định.
        split.setUI(new BasicSplitPaneUI() {
            @Override public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics graphics) {
                        graphics.setColor(BACKGROUND);
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        split.setResizeWeight(0.50);
        split.setContinuousLayout(true);
        split.setDividerSize(14);
        split.setBorder(null);
        split.setBackground(BACKGROUND);
        split.setOneTouchExpandable(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.50));
        page.add(split, BorderLayout.CENTER);

        benhNhanTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) updateSelection();
        });
        tiepNhanButton.addActionListener(event -> receiveSelected());
        refreshButton.addActionListener(event -> refreshAsync());

        // Tránh kết nối SQL trên Event Dispatch Thread khi mở trang.
        refreshAsync();
    }

    // ---------- 0. Luôn vừa khít khung nhìn, không cuộn cả trang ----------
    // Nếu KhungChinh bọc trang trong JScrollPane, các hàm dưới đây buộc trang
    // co giãn đúng bằng khung nhìn (cả rộng lẫn cao) nên không xuất hiện thanh cuộn trang.
    // Việc cuộn chỉ còn nằm trong từng bảng.

    @Override public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    // ---------- 1. Banner đầu trang và các thẻ thống kê ----------

    private JPanel titlePanel() {
        HeroPanel outer = new HeroPanel();

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(tinted(label("Tổng quan  ›  Trang chủ Lễ tân", 11, false), new Color(0xBAE6FD)));
        texts.add(Box.createVerticalStrut(8));
        texts.add(tinted(label("Tiếp nhận & Khám điều trị", 26, true), Color.WHITE));
        texts.add(Box.createVerticalStrut(6));
        texts.add(tinted(label("Xin chào, " + user.hoTen + "  ·  Hôm nay "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + "  ·  Dữ liệu cập nhật từ SQL Server", 12, false), new Color(0xE0F2FE)));
        outer.add(texts, BorderLayout.WEST);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(refreshButton);
        outer.add(actions, BorderLayout.EAST);
        return outer;
    }

    private static JLabel tinted(JLabel label, Color color) {
        label.setForeground(color);
        return label;
    }

    private JPanel statsPanel() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(700, 92));
        grid.add(statCard("Tổng bệnh nhân", tongBenhNhan, PRIMARY, 0));
        grid.add(statCard("Chưa nhận hôm nay", choTiepNhan, WARNING, 1));
        grid.add(statCard("Đã tiếp nhận hôm nay", daTiepNhan, GREEN, 2));
        grid.add(statCard("Phiếu khám hôm nay", soPhieuKham, new Color(0x8B5CF6), 3));
        return grid;
    }

    /** Thẻ thống kê: biểu tượng trong ô màu nhạt bên trái, tiêu đề mờ và số liệu lớn. */
    private JPanel statCard(String title, JLabel value, Color accent, int iconKind) {
        RoundedPanel card = card(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 18, 14));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new IconBadge(iconKind, accent));

        JPanel texts = new JPanel(new BorderLayout(0, 0));
        texts.setOpaque(false);
        value.setForeground(TEXT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 28));
        texts.add(label(title, 12, false), BorderLayout.NORTH);
        texts.add(value, BorderLayout.CENTER);
        card.add(iconWrap, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        return card;
    }

    // ---------- 2. Khu vực Khám & điều trị ----------

    private JPanel treatmentPanel() {
        JPanel panel = card(new BorderLayout(0, 12));
        panel.setMinimumSize(new Dimension(310, 320));
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Khám & điều trị", "Danh sách phiếu khám; nhấp đúp để xem chi tiết",
                phieuCount), BorderLayout.NORTH);
        JTextField search = search("Tìm mã phiếu, bệnh nhân, bác sĩ...");
        search.getDocument().addDocumentListener(new SearchListener(() ->
                filter(phieuSorter, search.getText())));
        top.add(search, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollTable(phieuTable), BorderLayout.CENTER);
        phieuTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) showPhieuDetail();
            }
        });
        panel.add(label("Cuộn ngang để xem đủ thông tin · Nhấp đúp để xem phiếu khám.",
                11, false), BorderLayout.SOUTH);
        return panel;
    }

    /** Hiển thị đầy đủ thuộc tính phiếu khám, không cần kéo ngang để đọc nội dung dài. */
    private void showPhieuDetail() {
        int viewRow = phieuTable.getSelectedRow();
        if (viewRow < 0) return;

        int row = phieuTable.convertRowIndexToModel(viewRow);
        String[] properties = {
            "Mã phiếu khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã nhân viên",
            "Bác sĩ phụ trách", "Ngày khám", "Trạng thái", "Triệu chứng",
            "Chẩn đoán", "Phương pháp điều trị", "Kết quả", "Ghi chú"
        };
        showDetail(phieuModel, row, properties, "Chi tiết phiếu khám");
    }

    /** Hiển thị đầy đủ thuộc tính bệnh nhân. */
    private void showPatientDetail() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0) return;

        int row = benhNhanTable.convertRowIndexToModel(viewRow);
        String[] properties = {
            "Mã bệnh nhân", "Họ và tên", "Ngày sinh", "Giới tính", "Số điện thoại",
            "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái", "Số khám hôm nay",
            "Phòng khám hôm nay"
        };
        showDetail(benhNhanModel, row, properties, "Chi tiết bệnh nhân");
    }

    /** Hộp thoại chi tiết: mỗi thuộc tính là một khối (nhãn nhỏ + giá trị), có cuộn dọc. */
    private void showDetail(DefaultTableModel model, int row, String[] titles, String caption) {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        for (int i = 0; i < titles.length; i++) {
            Object raw = model.getValueAt(row, i + 1); // Bỏ cột STT.
            boolean empty = raw == null || String.valueOf(raw).isBlank();
            String text = empty ? "Chưa cập nhật" : String.valueOf(raw);

            JLabel name = label(titles[i].toUpperCase(), 10, false);
            name.setFont(new Font("Segoe UI", Font.BOLD, 10));
            JLabel value = new JLabel("<html><body style='width:430px'>"
                    + escapeHtml(text).replace("\n", "<br>") + "</body></html>");
            value.setFont(new Font("Segoe UI", empty ? Font.ITALIC : Font.PLAIN, 13));
            value.setForeground(empty ? new Color(0x94A3B8) : TEXT);

            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setBackground(i % 2 == 0 ? Color.WHITE : ROW_ALT);
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(10, 18, 11, 18)));
            name.setAlignmentX(Component.LEFT_ALIGNMENT);
            value.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.add(name);
            item.add(Box.createVerticalStrut(4));
            item.add(value);
            gc.gridy = i;
            body.add(item, gc);
        }
        gc.gridy = titles.length;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        body.add(filler, gc);

        JScrollPane view = new JScrollPane(body);
        view.setBorder(BorderFactory.createLineBorder(BORDER));
        view.getVerticalScrollBar().setUnitIncrement(18);
        view.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        view.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        view.setPreferredSize(new Dimension(520, 420));
        JOptionPane.showMessageDialog(this, view, caption, JOptionPane.PLAIN_MESSAGE);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ---------- 3. Khu vực Tiếp nhận bệnh nhân ----------

    private JPanel receptionPanel() {
        JPanel panel = card(new BorderLayout(0, 12));
        panel.setMinimumSize(new Dimension(330, 320));
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Tiếp nhận bệnh nhân", "Chọn bệnh nhân, cấp số và phân phòng khám",
                benhNhanCount), BorderLayout.NORTH);
        JTextField search = search("Tìm họ tên, mã bệnh nhân, số điện thoại...");
        search.getDocument().addDocumentListener(new SearchListener(() ->
                filter(benhNhanSorter, search.getText())));
        top.add(search, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollTable(benhNhanTable), BorderLayout.CENTER);
        benhNhanTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) showPatientDetail();
            }
        });

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        // Dải thông tin bệnh nhân đang chọn, có vạch nhấn bên trái.
        JPanel info = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                khuRangCua(g2);
                g2.setColor(new Color(0xF0F9FF));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(0xBAE6FD));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 9, 4, getHeight() - 19, 4, 4);
                g2.dispose();
            }
        };
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(10, 16, 10, 13));
        info.add(chonBenhNhan, BorderLayout.CENTER);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        controls.add(info);
        controls.add(Box.createVerticalStrut(10));

        phongChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phongChon.setBackground(Color.WHITE);
        phongChon.setPreferredSize(new Dimension(122, 36));
        JPanel roomBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        roomBox.setOpaque(false);
        roomBox.add(label("Phòng khám", 12, true));
        roomBox.add(phongChon);
        JPanel actionRow = new JPanel(new BorderLayout(10, 0));
        actionRow.setOpaque(false);
        actionRow.add(roomBox, BorderLayout.WEST);
        actionRow.add(tiepNhanButton, BorderLayout.EAST);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        controls.add(actionRow);
        controls.add(Box.createVerticalStrut(9));
        trangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(trangThai);
        JPanel bottom = new JPanel(new BorderLayout(0, 5));
        bottom.setOpaque(false);
        bottom.add(controls, BorderLayout.CENTER);
        bottom.add(label("Mã BN hiển thị trực tiếp · Cuộn ngang / nhấp đúp xem đầy đủ hồ sơ.",
                11, false), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void updateSelection() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0) {
            chonBenhNhan.setText("Chọn một bệnh nhân trong bảng");
            chonBenhNhan.setForeground(MUTED);
        } else {
            int row = benhNhanTable.convertRowIndexToModel(viewRow);
            chonBenhNhan.setText("Đã chọn: BN " + benhNhanModel.getValueAt(row, 1)
                    + " - " + benhNhanModel.getValueAt(row, 2));
            chonBenhNhan.setForeground(PRIMARY_DARK);
        }
        tiepNhanButton.setEnabled(!loading && canReceive && viewRow >= 0);
    }

    /** Tạo tiếp nhận trong SQL ở luồng nền, tránh đứng giao diện. */
    private void receiveSelected() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0 || loading || !canReceive) return;
        int modelRow = benhNhanTable.convertRowIndexToModel(viewRow);
        int patientId = (Integer) benhNhanModel.getValueAt(modelRow, 1);
        String patientName = String.valueOf(benhNhanModel.getValueAt(modelRow, 2));
        String room = String.valueOf(phongChon.getSelectedItem());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tiếp nhận " + patientName + " vào " + room + "?\n"
                + "Số thứ tự sẽ được SQL Server tự cấp theo ngày.",
                "Xác nhận tiếp nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        setLoading(true);
        setStatus("Đang tiếp nhận...", WARNING);
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                return receiveInSql(patientId, room);
            }
            @Override protected void done() {
                try {
                    int queue = get();
                    JOptionPane.showMessageDialog(TrangChuLeTan.this,
                            "Tiếp nhận thành công!\nBệnh nhân: " + patientName
                            + "\nSố khám: " + String.format("%03d", queue)
                            + "\nPhòng: " + room,
                            "Phiếu tiếp nhận", JOptionPane.INFORMATION_MESSAGE);
                    setStatus("Tiếp nhận thành công · Số "
                            + String.format("%03d", queue), GREEN);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError(e);
                } catch (ExecutionException e) {
                    showError(e.getCause() == null ? e : e.getCause());
                } finally {
                    setLoading(false);
                    refreshAsync();
                }
            }
        }.execute();
    }

    /**
     * Một giao dịch SQL Server 2012:
     * 1. Kiểm tra lại vai trò và quyền được Admin cấp.
     * 2. Khóa cấp số theo ngày qua sp_getapplock để không trùng khi nhiều lễ tân thao tác.
     * 3. Tìm lịch hẹn hôm nay, hoặc tạo lịch hẹn trực tiếp.
     * 4. Cấp số thứ tự (MAX + 1), cập nhật phòng và ghi nhật ký.
     */
    private int receiveInSql(int patientId, String room) throws SQLException {
        try (Connection connection = KetNoi.mo()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                requirePermission(connection, "LICHHEN");
                if (!columnExists(connection)) {
                    throw new SQLException("Thiếu cột soThuTuKham. Hãy chạy SQLData/06_TiepNhanLeTan_SQL2012.sql.");
                }

                Date today;
                try (Statement statement = connection.createStatement();
                        ResultSet result = statement.executeQuery("SELECT CAST(GETDATE() AS date)")) {
                    result.next();
                    today = result.getDate(1);
                }
                String lockName = "MEDIDENT_TIEPNHAN_" + today.toString();
                try (PreparedStatement statement = connection.prepareStatement(
                        "DECLARE @result int; "
                        + "EXEC @result = sp_getapplock @Resource=?, @LockMode='Exclusive', "
                        + "@LockOwner='Transaction', @LockTimeout=10000; "
                        + "SELECT @result")) {
                    statement.setString(1, lockName);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getInt(1) < 0) {
                            throw new SQLException("Không khóa được bộ cấp số. Hãy thử lại.");
                        }
                    }
                }

                int appointmentId = -1;
                Integer previouslyAssigned = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT TOP 1 maLichHen,soThuTuKham FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE maBN=? AND ngayHen=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất') "
                        + "ORDER BY CASE WHEN soThuTuKham IS NULL THEN 1 ELSE 0 END, maLichHen")) {
                    statement.setInt(1, patientId);
                    statement.setDate(2, today);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            appointmentId = result.getInt(1);
                            int value = result.getInt(2);
                            if (!result.wasNull()) previouslyAssigned = value;
                        }
                    }
                }
                if (previouslyAssigned != null) {
                    throw new SQLException("Bệnh nhân đã được tiếp nhận hôm nay, số khám: "
                            + String.format("%03d", previouslyAssigned));
                }

                int queue = 1;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT ISNULL(MAX(soThuTuKham),0)+1 FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE ngayHen=?")) {
                    statement.setDate(1, today);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        queue = result.getInt(1);
                    }
                }
                if (appointmentId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE dbo.LichHen SET soThuTuKham=?,phongKham=?, "
                            + "trangThai=N'Đã xác nhận' WHERE maLichHen=? AND soThuTuKham IS NULL")) {
                        statement.setInt(1, queue);
                        statement.setString(2, room);
                        statement.setInt(3, appointmentId);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Lịch hẹn vừa được thay đổi, hãy tải lại.");
                        }
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT dbo.LichHen (maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai,soThuTuKham) "
                            + "VALUES (?,NULL,?,CAST(GETDATE() AS date),CAST(GETDATE() AS time), "
                            + "N'Tiếp nhận trực tiếp',N'Đã xác nhận',?)")) {
                        statement.setInt(1, patientId);
                        statement.setString(2, room);
                        statement.setInt(3, queue);
                        statement.executeUpdate();
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    statement.setString(1, "Tiếp nhận bệnh nhân");
                    statement.setString(2, "LichHen");
                    statement.setString(3, "maTK=" + user.id + " | maBN=" + patientId
                            + " | STT=" + queue + " | " + room);
                    statement.executeUpdate();
                }
                connection.commit();
                return queue;
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException("Tiếp nhận không thành công.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Kiểm tra ngay trong transaction để chặn ghi khi Admin vừa thu hồi quyền. */
    private void requirePermission(Connection connection, String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro=N'Lễ tân' AND q.maQuyen=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id);
            statement.setString(2, code);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Bạn không có quyền tiếp nhận bệnh nhân.");
            }
        }
    }

    private boolean columnExists(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SELECT COL_LENGTH('dbo.LichHen','soThuTuKham')")) {
            return result.next() && result.getObject(1) != null;
        }
    }

    // ---------- 4. Truy vấn SQL thật, thực hiện ngoài luồng EDT ----------

    private void refreshAsync() {
        if (loading) return;
        setLoading(true);
        setStatus("Đang tải dữ liệu...", WARNING);
        new SwingWorker<ReportData, Void>() {
            @Override protected ReportData doInBackground() throws Exception {
                return loadFromSql();
            }
            @Override protected void done() {
                try {
                    ReportData data = get();
                    canView = data.viewAllowed;
                    canReceive = data.receiveAllowed;
                    fillTable(phieuModel, data.examRows);
                    fillTable(benhNhanModel, data.patientRows);
                    phieuCount.setText(String.valueOf(data.examRows.size()));
                    benhNhanCount.setText(String.valueOf(data.patientRows.size()));
                    tongBenhNhan.setText(String.valueOf(data.totalPatients));
                    choTiepNhan.setText(String.valueOf(data.waitingToday));
                    daTiepNhan.setText(String.valueOf(data.receivedToday));
                    soPhieuKham.setText(String.valueOf(data.examsToday));
                    if (data.viewAllowed) {
                        setStatus("Đã đồng bộ SQL Server · Hôm nay: " + LocalDate.now(), GREEN);
                    } else {
                        setStatus("Không có quyền BENHNHAN — danh sách đã được ẩn", DANGER);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError(e);
                } catch (ExecutionException e) {
                    showError(e.getCause() == null ? e : e.getCause());
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private ReportData loadFromSql() throws SQLException {
        ReportData data = new ReportData();
        data.viewAllowed = db.coQuyenTaiKhoan(user.id, "BENHNHAN");
        data.receiveAllowed = data.viewAllowed && db.coQuyenTaiKhoan(user.id, "LICHHEN")
                && "Lễ tân".equalsIgnoreCase(user.tenVaiTro);
        if (!data.viewAllowed) return data;

        try (Connection connection = KetNoi.mo()) {
            boolean queueInstalled = columnExists(connection);
            String queueField = queueInstalled ? "l.soThuTuKham" : "CAST(NULL AS INT)";

            // Lấy toàn bộ thuộc tính của PhieuKham và cả hai mã khóa ngoại.
            // LEFT JOIN giúp phiếu vẫn hiển thị nếu hồ sơ nhân viên không tra cứu được.
            String examSql =
                    "SELECT p.maPK, p.maBN, p.maNV, "
                    + "ISNULL(b.hoTen,N'') AS benhNhan, "
                    + "ISNULL(n.hoTen,N'') AS bacSi, "
                    + "CONVERT(varchar(16), p.ngayKham, 120) AS ngayKham, "
                    + "ISNULL(p.trangThai,N'Chưa cập nhật') AS trangThai, "
                    + "ISNULL(p.trieuChung,N'') AS trieuChung, "
                    + "ISNULL(p.chanDoan,N'') AS chanDoan, "
                    + "ISNULL(p.phuongPhapDieuTri,N'') AS phuongPhapDieuTri, "
                    + "ISNULL(p.ketQua,N'') AS ketQua, "
                    + "ISNULL(p.ghiChu,N'') AS ghiChu "
                    + "FROM dbo.PhieuKham p "
                    + "LEFT JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                    + "LEFT JOIN dbo.NhanVien n ON p.maNV=n.maNV "
                    + "ORDER BY p.ngayKham DESC, p.maPK DESC";

            try (PreparedStatement statement = connection.prepareStatement(examSql);
                    ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    data.examRows.add(new Object[]{
                        0,
                        result.getInt("maPK"),
                        result.getInt("maBN"),
                        result.getString("benhNhan"),
                        result.getInt("maNV"),
                        result.getString("bacSi"),
                        result.getString("ngayKham"),
                        result.getString("trangThai"),
                        result.getString("trieuChung"),
                        result.getString("chanDoan"),
                        result.getString("phuongPhapDieuTri"),
                        result.getString("ketQua"),
                        result.getString("ghiChu")
                    });
                }
            }

            // Bệnh nhân: đầy đủ thuộc tính theo ERD, cộng số khám/phòng của lịch hôm nay.
            // OUTER APPLY lấy lịch hẹn tương ứng. Không sinh số khám trong lúc xem.
            String patientSql =
                    "SELECT b.maBN, b.hoTen, "
                    + "ISNULL(CONVERT(varchar(10),b.ngaySinh,103),'') AS ngaySinh, "
                    + "ISNULL(b.gioiTinh,N'') AS gioiTinh, "
                    + "ISNULL(b.sdt,'') AS sdt, ISNULL(b.email,'') AS email, "
                    + "ISNULL(b.diaChi,N'') AS diaChi, "
                    + "ISNULL(b.tienSuBenh,N'') AS tienSuBenh, "
                    + "ISNULL(b.trangThai,N'') AS trangThai, "
                    + queueField.replace("l.", "a.") + " AS soThuTuKham, "
                    + "ISNULL(a.phongKham,N'') AS phongKham "
                    + "FROM dbo.BenhNhan b "
                    + "OUTER APPLY (SELECT TOP 1 l.maLichHen, l.phongKham"
                    + (queueInstalled ? ", l.soThuTuKham" : "")
                    + " FROM dbo.LichHen l WHERE l.maBN=b.maBN "
                    + "AND l.ngayHen=CAST(GETDATE() AS date) AND l.trangThai<>N'Đã hủy' "
                    + "ORDER BY " + (queueInstalled
                            ? "CASE WHEN l.soThuTuKham IS NULL THEN 1 ELSE 0 END, " : "")
                    + "l.maLichHen) a ORDER BY b.maBN DESC";

            try (PreparedStatement statement = connection.prepareStatement(patientSql);
                    ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Object number = result.getObject("soThuTuKham");
                    data.patientRows.add(new Object[]{
                        0,
                        result.getInt("maBN"),
                        result.getString("hoTen"),
                        result.getString("ngaySinh"),
                        result.getString("gioiTinh"),
                        result.getString("sdt"),
                        result.getString("email"),
                        result.getString("diaChi"),
                        result.getString("tienSuBenh"),
                        result.getString("trangThai"),
                        number == null ? "—" : String.format("%03d", result.getInt("soThuTuKham")),
                        result.getString("phongKham")
                    });
                }
            }

            data.totalPatients = scalar(connection, "SELECT COUNT(*) FROM dbo.BenhNhan");
            data.examsToday = scalar(connection,
                    "SELECT COUNT(*) FROM dbo.PhieuKham "
                    + "WHERE ngayKham>=CAST(GETDATE() AS date) "
                    + "AND ngayKham<DATEADD(day,1,CAST(GETDATE() AS date))");
            if (queueInstalled) {
                data.receivedToday = scalar(connection,
                        "SELECT COUNT(*) FROM dbo.LichHen "
                        + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NOT NULL");
                data.waitingToday = scalar(connection,
                        "SELECT COUNT(*) FROM dbo.LichHen "
                        + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NULL "
                        + "AND trangThai<>N'Đã hủy'");
            }
        }
        return data;
    }

    private int scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private void fillTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] values : rows) model.addRow(values);
    }

    private void setLoading(boolean value) {
        loading = value;
        refreshButton.setEnabled(!value);
        phongChon.setEnabled(!value);
        tiepNhanButton.setEnabled(!value && canReceive && benhNhanTable.getSelectedRow() >= 0);
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    /** Dòng trạng thái có chấm màu: vàng = đang xử lý, xanh = ổn, đỏ = lỗi / bị chặn. */
    private void setStatus(String text, Color dot) {
        trangThai.setText(text);
        trangThai.setIcon(new DotIcon(dot));
        trangThai.setIconTextGap(8);
    }

    private void showError(Throwable error) {
        setStatus("Không tải/lưu được dữ liệu SQL Server.", DANGER);
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "MediDent - Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static final class ReportData {
        boolean viewAllowed;
        boolean receiveAllowed;
        int totalPatients;
        int waitingToday;
        int receivedToday;
        int examsToday;
        final List<Object[]> examRows = new ArrayList<>();
        final List<Object[]> patientRows = new ArrayList<>();
    }

    // ---------- 5. Các hàm dựng giao diện dùng chung ----------

    private static DefaultTableModel model(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : Object.class;
            }
        };
    }

    private static JTable table(DefaultTableModel model) {
        JTable table = new StripedTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(44);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(0xEEF2F7));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        // Chiều cao ưu tiên nhỏ để bảng không ép trang dài hơn khung nhìn;
        // bảng tự co theo khoảng trống và có thanh cuộn riêng.
        table.setPreferredScrollableViewportSize(new Dimension(300, 160));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(48); // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(86); // Mã phiếu / Mã BN
        for (int i = 2; i < table.getColumnCount(); i++) {
            String title = table.getColumnName(i);
            int width;
            switch (title) {
                case "Mã BN":
                case "Mã NV": width = 78; break;
                case "Bệnh nhân":
                case "Họ tên":
                case "Bác sĩ": width = 190; break;
                case "Ngày khám":
                case "Ngày sinh": width = 126; break;
                case "Triệu chứng":
                case "Chẩn đoán":
                case "Phương pháp điều trị":
                case "Kết quả":
                case "Tiền sử bệnh":
                case "Địa chỉ":
                case "Ghi chú": width = 180; break;
                case "Trạng thái": width = 140; break;
                default: width = 125;
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        return table;
    }

    /** STT tính theo thứ tự đang xem, nên lọc và sắp xếp vẫn bắt đầu từ 1. */
    private static void setupStt(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, row + 1, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) setForeground(MUTED);
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(renderer);
    }

    /** Cột mã (mã phiếu / mã BN): in đậm, màu chủ đạo, căn giữa. */
    private static void idColumn(JTable table, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!selected) setForeground(PRIMARY_DARK);
                return this;
            }
        };
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    /** Cột số thường (Mã BN, Mã NV trong bảng phiếu khám): căn giữa. */
    private static void centerColumn(JTable table, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) setForeground(TEXT);
                return this;
            }
        };
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    private static void badgeColumn(JTable table, int column, boolean queueNumber) {
        table.getColumnModel().getColumn(column).setCellRenderer(new BadgeRenderer(queueNumber));
    }

    private static void avatarColumn(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new AvatarRenderer());
    }

    /** Màu nền dòng: chọn > rê chuột > kẻ sọc xen kẽ. */
    private static Color rowBackground(JTable table, int row, boolean selected) {
        if (selected) return table.getSelectionBackground();
        if (table instanceof StripedTable && ((StripedTable) table).hoverRow == row) return ROW_HOVER;
        return row % 2 == 0 ? Color.WHITE : ROW_ALT;
    }

    private static JScrollPane scrollTable(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.getHorizontalScrollBar().setUnitIncrement(24);
        pane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        pane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        pane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return pane;
    }

    private static RoundedPanel card(java.awt.LayoutManager layout) {
        RoundedPanel panel = new RoundedPanel(layout);
        panel.setBorder(new EmptyBorder(17, 18, 20, 18));
        return panel;
    }

    private static JPanel sectionTitle(String title, String subtitle, JComponent count) {
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(label(title, 16, true));
        texts.add(Box.createVerticalStrut(4));
        texts.add(label(subtitle, 11, false));

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        east.setOpaque(false);
        east.add(count);

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.add(texts, BorderLayout.CENTER);
        panel.add(east, BorderLayout.EAST);
        return panel;
    }

    private static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(bold ? TEXT : MUTED);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    private static JLabel bigValue(String text) {
        return label(text, 25, true);
    }

    private static JButton button(String text, boolean primary) {
        return new RoundButton(text, primary ? 1 : 0);
    }

    private static JTextField search(String prompt) {
        return new SearchField(prompt);
    }

    private static void filter(TableRowSorter<DefaultTableModel> sorter, String text) {
        String query = text == null ? "" : text.trim();
        sorter.setRowFilter(query.isEmpty() ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(query)));
    }

    private static final class SearchListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        SearchListener(Runnable action) { this.action = action; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }

    // ---------- 6. Thành phần vẽ riêng ----------

    private static void khuRangCua(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Chấm tròn nhỏ đứng trước dòng trạng thái. */
    private static final class DotIcon implements Icon {
        private final Color color;
        DotIcon(Color color) { this.color = color; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g2.fillOval(x, y, 12, 12);
            g2.setColor(color);
            g2.fillOval(x + 3, y + 3, 6, 6);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 12; }
        @Override public int getIconHeight() { return 12; }
    }

    /** Banner gradient xanh với vài vòng tròn trang trí mờ phía phải. */
    private static final class HeroPanel extends JPanel {
        HeroPanel() {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(20, 26, 20, 26));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(0x0369A1), w, h, new Color(0x0EA5E9)));
            g2.fillRoundRect(0, 0, w, h, 20, 20);
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 20, 20));
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(w - 190, -70, 230, 230);
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(w - 310, h - 70, 170, 170);
            g2.dispose();
        }
    }

    /** Ô vuông bo góc màu nhạt chứa biểu tượng vẽ bằng nét: 0 người, 1 đồng hồ, 2 dấu tick, 3 phiếu. */
    private static final class IconBadge extends JComponent {
        private final int kind;
        private final Color accent;

        IconBadge(int kind, Color accent) {
            this.kind = kind;
            this.accent = accent;
            setPreferredSize(new Dimension(48, 48));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
            g2.fillRoundRect(0, 0, 47, 47, 16, 16);
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (kind) {
                case 0:
                    g2.drawOval(18, 11, 12, 12);
                    g2.drawArc(12, 27, 24, 18, 0, 180);
                    break;
                case 1:
                    g2.drawOval(12, 12, 24, 24);
                    g2.drawLine(24, 24, 24, 17);
                    g2.drawLine(24, 24, 30, 27);
                    break;
                case 2:
                    g2.drawOval(12, 12, 24, 24);
                    g2.drawPolyline(new int[] {18, 22, 30}, new int[] {24, 28, 20}, 3);
                    break;
                default:
                    g2.drawRoundRect(15, 11, 18, 26, 5, 5);
                    g2.drawLine(19, 19, 29, 19);
                    g2.drawLine(19, 24, 29, 24);
                    g2.drawLine(19, 29, 25, 29);
            }
            g2.dispose();
        }
    }

    /** Thẻ trắng bo góc, viền mảnh và bóng đổ mềm nhiều lớp. */
    private static final class RoundedPanel extends JPanel {
        RoundedPanel(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            for (int k = 3; k >= 1; k--) {
                g2.setColor(new Color(15, 23, 42, 8));
                g2.fillRoundRect(3 - k, k, w - 7 + 2 * k, h - 7 + k, 20, 20);
            }
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(3, 0, w - 7, h - 7, 18, 18);
            g2.setColor(BORDER);
            g2.drawRoundRect(3, 0, w - 7, h - 7, 18, 18);
            g2.dispose();
        }
    }

    /**
     * Nút bo góc. kind: 0 = phụ (nền trắng), 1 = chính (xanh), 2 = ghost (mờ, dùng trên nền gradient).
     * Có trạng thái rê chuột / nhấn / vô hiệu.
     */
    private static final class RoundButton extends JButton {
        private final int kind;

        RoundButton(String text, int kind) {
            super(text);
            this.kind = kind;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            boolean on = isEnabled();
            boolean hover = on && getModel().isRollover();
            boolean pressed = on && getModel().isPressed();

            Color fill;
            Color edge;
            Color textColor;
            if (kind == 1) {
                fill = !on ? new Color(0x94A3B8) : pressed ? new Color(0x075985)
                        : hover ? PRIMARY_DARK : PRIMARY;
                edge = fill;
                textColor = Color.WHITE;
            } else if (kind == 2) {
                fill = new Color(255, 255, 255, !on ? 25 : pressed ? 95 : hover ? 70 : 42);
                edge = new Color(255, 255, 255, on ? 130 : 60);
                textColor = on ? Color.WHITE : new Color(255, 255, 255, 140);
            } else {
                fill = !on ? new Color(0xF1F5F9) : pressed ? new Color(0xE2E8F0)
                        : hover ? new Color(0xF1F5F9) : Color.WHITE;
                edge = hover || pressed ? new Color(0xCBD5E1) : BORDER;
                textColor = on ? TEXT : new Color(0x94A3B8);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(edge);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(textColor);
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                    (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Thanh cuộn mảnh, bo tròn, không có nút mũi tên. */
    private static final class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private static JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
            if (bounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(isThumbRollover() || isDragging ? new Color(0x94A3B8) : new Color(0xCBD5E1));
            g2.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Viền bo góc cho ô tìm kiếm; đổi màu khi focus. */
    private static final class VienBo extends AbstractBorder {
        private final Color color;
        private final float thickness;
        private final Insets insets;

        VienBo(Color color, float thickness, Insets insets) {
            this.color = color;
            this.thickness = thickness;
            this.insets = insets;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            float o = thickness / 2f;
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(x + o, y + o, w - thickness, h - thickness, 12, 12));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return insets;
        }
    }

    /** Ô tìm kiếm bo tròn có nền xám nhạt, biểu tượng kính lúp và chữ gợi ý. */
    private static final class SearchField extends JTextField {
        private final String prompt;

        SearchField(String prompt) {
            this.prompt = prompt;
            setToolTipText(prompt);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setForeground(TEXT);
            setOpaque(false);
            setPreferredSize(new Dimension(200, 40));
            setBorder(new VienBo(BORDER, 1.2f, new Insets(7, 36, 7, 12)));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent event) {
                    setBorder(new VienBo(PRIMARY, 1.8f, new Insets(7, 36, 7, 12)));
                    repaint();
                }
                @Override public void focusLost(FocusEvent event) {
                    setBorder(new VienBo(BORDER, 1.2f, new Insets(7, 36, 7, 12)));
                    repaint();
                }
            });
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(hasFocus() ? Color.WHITE : ROW_ALT);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(graphics);

            g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int cy = getHeight() / 2;
            g2.setColor(hasFocus() ? PRIMARY : MUTED);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(13, cy - 7, 9, 9);
            g2.drawLine(21, cy + 1, 25, cy + 5);
            if (getText().isEmpty() && !hasFocus()) {
                g2.setColor(new Color(0x94A3B8));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(prompt, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    /** Nhãn đếm số dòng ở tiêu đề mỗi khu vực. */
    private static final class Pill extends JLabel {
        Pill(String text) {
            super(text, SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(PRIMARY_DARK);
            setBorder(new EmptyBorder(3, 10, 3, 10));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(PRIMARY_LIGHT);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Bảng kẻ sọc xen kẽ, làm nổi dòng khi rê chuột, có thông báo khi không có dữ liệu. */
    private static final class StripedTable extends JTable {
        int hoverRow = -1;

        StripedTable(DefaultTableModel model) {
            super(model);
            MouseAdapter hover = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) { setHover(rowAtPoint(e.getPoint())); }
                @Override public void mouseExited(MouseEvent e) { setHover(-1); }
            };
            addMouseMotionListener(hover);
            addMouseListener(hover);
        }

        private void setHover(int row) {
            if (row != hoverRow) {
                hoverRow = row;
                repaint();
            }
        }

        @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (c instanceof JComponent && !(c instanceof BadgeRenderer) && !(c instanceof AvatarRenderer)) {
                ((JComponent) c).setBorder(new EmptyBorder(0, 10, 0, 10));
            }
            if (!isRowSelected(row)) {
                c.setBackground(rowBackground(this, row, false));
            }
            return c;
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                khuRangCua(g2);
                Rectangle visible = getVisibleRect();
                String message = "Không có dữ liệu để hiển thị";
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(new Color(0x94A3B8));
                g2.drawString(message, visible.x + (visible.width - fm.stringWidth(message)) / 2,
                        visible.y + visible.height / 2);
                g2.dispose();
            }
        }
    }

    /** Tiêu đề cột: nền xám nhạt, chữ nhỏ đậm, kẻ chân. STT / Mã phiếu / Mã BN / Mã NV căn giữa. */
    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setBackground(new Color(0xF1F5F9));
            setForeground(MUTED);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            String name = table.getColumnName(column);
            boolean center = column == 0 || "Mã phiếu".equals(name)
                    || "Mã BN".equals(name) || "Mã NV".equals(name);
            setHorizontalAlignment(center ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(0, 10, 0, 10)));
            setPreferredSize(null);
            setPreferredSize(new Dimension(getPreferredSize().width, 40));
            return this;
        }
    }

    /** Tên kèm avatar tròn có chữ cái đầu, màu theo tên. */
    private static final class AvatarRenderer extends DefaultTableCellRenderer {
        private Color cellBackground = Color.WHITE;
        private Color avatarColor = PRIMARY;
        private String initial = "";

        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(new EmptyBorder(0, 46, 0, 10));
            if (!selected) setForeground(TEXT);
            String name = value == null ? "" : String.valueOf(value).trim();
            cellBackground = rowBackground(table, row, selected);
            if (name.isEmpty()) {
                initial = "";
            } else {
                String[] parts = name.split("\\s+");
                String last = parts[parts.length - 1];
                initial = last.substring(0, 1).toUpperCase();
                avatarColor = AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.length];
            }
            return this;
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(cellBackground);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (!initial.isEmpty()) {
                int size = 28;
                int y = (getHeight() - size) / 2;
                g2.setColor(new Color(avatarColor.getRed(), avatarColor.getGreen(), avatarColor.getBlue(), 36));
                g2.fillOval(10, y, size, size);
                g2.setColor(avatarColor);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, 10 + (size - fm.stringWidth(initial)) / 2,
                        y + (size - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Hiển thị trạng thái / số khám dưới dạng nhãn bo tròn có màu theo ý nghĩa. */
    private static final class BadgeRenderer extends DefaultTableCellRenderer {
        private final boolean queueNumber;
        private Color cellBackground = Color.WHITE;
        private Color pillBackground = new Color(0xE2E8F0);
        private boolean pill;

        BadgeRenderer(boolean queueNumber) {
            this.queueNumber = queueNumber;
        }

        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(new EmptyBorder(0, 20, 0, 10));
            String text = value == null ? "" : String.valueOf(value).trim();
            cellBackground = rowBackground(table, row, selected);
            pill = !text.isEmpty() && !(queueNumber && "—".equals(text));
            Color[] colors = pill ? badgeColors(text) : new Color[] {pillBackground, MUTED};
            pillBackground = colors[0];
            setForeground(pill ? colors[1] : MUTED);
            return this;
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(cellBackground);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (pill) {
                FontMetrics fm = getFontMetrics(getFont());
                int pillHeight = 24;
                g2.setColor(pillBackground);
                g2.fillRoundRect(10, (getHeight() - pillHeight) / 2,
                        fm.stringWidth(getText()) + 20, pillHeight, pillHeight, pillHeight);
            }
            g2.dispose();
            super.paintComponent(graphics);
        }

        /** Màu nhãn: xanh lá = hoàn tất/hoạt động, vàng = đang chờ, đỏ = hủy/khóa, xanh dương = còn lại. */
        private Color[] badgeColors(String text) {
            String t = text.toLowerCase();
            if (queueNumber) return new Color[] {PRIMARY_LIGHT, PRIMARY_DARK};
            if (t.contains("hủy") || t.contains("khóa") || t.contains("ngừng")) {
                return new Color[] {new Color(0xFEE2E2), new Color(0xB91C1C)};
            }
            if (t.contains("hoàn tất") || t.contains("hoạt động") || t.contains("đã xác nhận")
                    || t.contains("đã khám") || t.contains("hoàn thành")) {
                return new Color[] {new Color(0xD1FAE5), new Color(0x047857)};
            }
            if (t.contains("chờ") || t.contains("đang") || t.contains("chưa")) {
                return new Color[] {new Color(0xFEF3C7), new Color(0xB45309)};
            }
            return new Color[] {new Color(0xE2E8F0), new Color(0x334155)};
        }
    }
}