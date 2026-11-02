package UI.LeTan;

import Class.TaiKhoan;
import ConnectDB.KetNoi;
import DAO.KhoDuLieu;
import TienIch.EmailHopLe;

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
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * TRANG CHỦ LỄ TÂN - MEDIDENT.
 *
 * Một màn hình, chia hai khu vực:
 * 1) Khám & điều trị: chỉ xem phiếu khám có thật trong SQL Server.
 * 2) Tiếp nhận: nhập thông tin bệnh nhân bằng form, cấp số thứ tự theo ngày và chỉ định phòng.
 *
 * Khung header / sidebar nằm ở UI.Chung.KhungChinh, không tạo JFrame mới.
 * Cần chạy SQLData/06_TiepNhanLeTan_SQL2012.sql MỘT LẦN trước khi dùng.
 *
 * Quyền:
 * - BENHNHAN: xem phiếu khám và tạo / dùng lại hồ sơ bệnh nhân khi tiếp nhận.
 * - LICHHEN: tiếp nhận và cấp số khám (kiểm tra lại trong transaction SQL).
 *
 * Cấu trúc file:
 *   1. Hằng số và các biến của trang
 *   2. Hàm khởi tạo
 *   3. Dựng giao diện (banner, thống kê, hai khu vực)
 *   4. Tiếp nhận bệnh nhân (giao diện + giao dịch SQL)
 *   5. Tải dữ liệu từ SQL Server
 *   6. Bảng, tìm kiếm, lọc
 *   7. Các thành phần vẽ riêng (chỉ để làm đẹp giao diện)
 */
public final class TrangChuLeTan extends JPanel implements Scrollable {

    // ================= 1. HẰNG SỐ VÀ BIẾN CỦA TRANG =================

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

    // Mỗi phòng nhận tối đa bấy nhiêu bệnh nhân trong một ngày.
    private static final int MAX_BENH_NHAN_MOI_PHONG = 20;

    private final KhoDuLieu db;
    private final TaiKhoan user;

    private final DefaultTableModel phieuModel;
    private final JTable phieuTable;
    private final TableRowSorter<DefaultTableModel> phieuSorter;

    // Định dạng ngày sinh người dùng nhập; STRICT để chặn ngày không có thật (31/02).
    private static final DateTimeFormatter NGAY_SINH =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final JComboBox<String> phongChon = new ComboModern(ROOMS);
    private final JButton tiepNhanButton = new RoundButton("Tiếp nhận / Cấp số", RoundButton.CHINH);
    private final JButton refreshButton = new RoundButton("Làm mới", RoundButton.MO);
    private final JButton lamLaiButton = new RoundButton("Nhập lại", RoundButton.PHU);

    // Form thông tin bệnh nhân (khu vực Tiếp nhận).
    private final JTextField hoTenField = new TruongNhap("Ví dụ: Nguyễn Văn An");
    private final JTextField ngaySinhField = new TruongNhap("dd/MM/yyyy");
    private final JComboBox<String> gioiTinhChon = new ComboModern(new String[] {"Nam", "Nữ"});
    private final JTextField sdtField = new TruongNhap("Ví dụ: 0901234567");
    private final JTextField emailField = new TruongNhap("ten@example.com");
    private final JTextField diaChiField = new TruongNhap("Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành");
    private final JTextArea tienSuArea = new TextAreaNhap("Dị ứng thuốc, bệnh nền, phẫu thuật trước đây... (nếu có)");
    private final JLabel trangThai = label("", 11, false);
    private final JLabel tongBenhNhan = label("—", 25, true);
    private final JLabel choTiepNhan = label("—", 25, true);
    private final JLabel daTiepNhan = label("—", 25, true);
    private final JLabel soPhieuKham = label("—", 25, true);
    private final Pill phieuCount = new Pill("0");

    private boolean loading;      // đang tải / đang tiếp nhận thì khóa giao diện
    private boolean canReceive;   // có được phép tiếp nhận hay không

    // ================= 2. HÀM KHỞI TẠO =================

    public TrangChuLeTan(KhoDuLieu db, TaiKhoan user) {
        this.db = db;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Mã phiếu, mã bệnh nhân và mã bác sĩ đặt ở đầu để dễ nhìn trong khung chia đôi.
        // Các thuộc tính còn lại xem bằng cuộn ngang hoặc nhấp đúp để mở chi tiết.
        phieuModel = model(
                "STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Mã NV", "Bác sĩ",
                "Ngày khám", "Trạng thái", "Triệu chứng", "Chẩn đoán",
                "Phương pháp điều trị", "Kết quả", "Ghi chú");
        phieuTable = table(phieuModel);
        phieuSorter = taoSorter(phieuTable, phieuModel);
        caiDatCotBang();

        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 20, 24));
        add(page, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 16));
        upper.setOpaque(false);
        upper.add(titlePanel(), BorderLayout.NORTH);
        upper.add(statsPanel(), BorderLayout.CENTER);
        page.add(upper, BorderLayout.NORTH);
        page.add(splitPanel(), BorderLayout.CENTER);

        tiepNhanButton.addActionListener(event -> receiveFromForm());
        lamLaiButton.addActionListener(event -> lamLaiForm());
        refreshButton.addActionListener(event -> refreshAsync());

        // Tránh kết nối SQL trên Event Dispatch Thread khi mở trang.
        refreshAsync();
    }

    /** Bộ sắp xếp cho bảng; cột STT (cột 0) không cho sắp xếp. */
    private static TableRowSorter<DefaultTableModel> taoSorter(JTable table, DefaultTableModel model) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(0, false);
        return sorter;
    }

    /** Gắn cách hiển thị riêng cho từng cột của hai bảng. */
    private void caiDatCotBang() {
        // STT: số mờ, căn giữa.
        datCot(phieuTable, 0, new CenterRenderer(MUTED, false));
        // Mã phiếu: in đậm, màu chủ đạo, căn giữa.
        datCot(phieuTable, 1, new CenterRenderer(PRIMARY_DARK, true));
        // Mã BN và Mã NV trong bảng phiếu khám: căn giữa.
        datCot(phieuTable, 2, new CenterRenderer(TEXT, false));
        datCot(phieuTable, 4, new CenterRenderer(TEXT, false));
        // Tên kèm avatar.
        datCot(phieuTable, 3, new AvatarRenderer());
        // Nhãn màu cho trạng thái và số khám.
        datCot(phieuTable, 7, new BadgeRenderer(false));
    }

    private static void datCot(JTable table, int column, TableCellRenderer renderer) {
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    // Trang luôn vừa khít khung nhìn, không cuộn cả trang (chỉ từng bảng có thanh cuộn riêng).
    // Nếu KhungChinh bọc trang trong JScrollPane thì các hàm dưới đây buộc trang
    // co giãn đúng bằng khung nhìn.

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

    // ================= 3. DỰNG GIAO DIỆN =================

    // ---------- 3.1 Banner đầu trang và các thẻ thống kê ----------

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
        value.setFont(font(Font.BOLD, 28));
        texts.add(label(title, 12, false), BorderLayout.NORTH);
        texts.add(value, BorderLayout.CENTER);
        card.add(iconWrap, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        return card;
    }

    // ---------- 3.2 Khung chia đôi: Khám & điều trị | Tiếp nhận ----------

    private JSplitPane splitPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treatmentPanel(), receptionPanel());
        // Thanh chia chỉ là khoảng trống cùng màu nền, bỏ viền và nút mũi tên mặc định.
        split.setUI(new ThanhChiaUI());
        split.setResizeWeight(0.50);
        split.setContinuousLayout(true);
        split.setDividerSize(14);
        split.setBorder(null);
        split.setBackground(BACKGROUND);
        split.setOneTouchExpandable(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.50));
        return split;
    }

    // ---------- 3.3 Khu vực Khám & điều trị ----------

    private JPanel treatmentPanel() {
        JPanel panel = card(new BorderLayout(0, 12));
        panel.setMinimumSize(new Dimension(310, 320));

        // Criterion(tên hiển thị, cột mã, cột văn bản, cột SĐT) - số cột tính theo model.
        Criterion[] criteria = {
            new Criterion("Tất cả", cols(1, 2), cols(3, 5), cols()),
            new Criterion("Mã phiếu", cols(1), cols(), cols()),
            new Criterion("Mã BN", cols(2), cols(), cols()),
            new Criterion("Tên bệnh nhân", cols(), cols(3), cols()),
            new Criterion("Mã bác sĩ", cols(4), cols(), cols()),
            new Criterion("Tên bác sĩ", cols(), cols(5), cols()),
            new Criterion("Ngày khám", cols(), cols(6), cols()),
            new Criterion("Trạng thái", cols(), cols(7), cols()),
            new Criterion("Chẩn đoán", cols(), cols(9), cols())
        };
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Khám & điều trị", "Danh sách phiếu khám; nhấp đúp để xem chi tiết",
                phieuCount), BorderLayout.NORTH);
        top.add(searchBar(phieuSorter, criteria), BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollTable(phieuTable), BorderLayout.CENTER);
        panel.add(label("Cuộn ngang để xem đủ thông tin · Nhấp đúp để xem phiếu khám.",
                11, false), BorderLayout.SOUTH);

        phieuTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) showPhieuDetail();
            }
        });
        return panel;
    }

    /** Hiển thị đầy đủ thuộc tính phiếu khám, không cần kéo ngang để đọc nội dung dài. */
    private void showPhieuDetail() {
        int viewRow = phieuTable.getSelectedRow();
        if (viewRow < 0) return;
        String[] properties = {
            "Mã phiếu khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã nhân viên",
            "Bác sĩ phụ trách", "Ngày khám", "Trạng thái", "Triệu chứng",
            "Chẩn đoán", "Phương pháp điều trị", "Kết quả", "Ghi chú"
        };
        showDetail(phieuModel, phieuTable.convertRowIndexToModel(viewRow),
                properties, "Chi tiết phiếu khám");
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
            name.setFont(font(Font.BOLD, 10));
            JLabel value = new JLabel("<html><body style='width:430px'>"
                    + escapeHtml(text).replace("\n", "<br>") + "</body></html>");
            value.setFont(font(empty ? Font.ITALIC : Font.PLAIN, 13));
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
        // Khối trống ở cuối để các thuộc tính dồn lên trên.
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

    // ================= 4. TIẾP NHẬN BỆNH NHÂN =================

    // ---------- 4.1 Giao diện khu vực Tiếp nhận (form nhập thông tin bệnh nhân) ----------

    private JPanel receptionPanel() {
        JPanel panel = card(new BorderLayout(0, 14));
        panel.setMinimumSize(new Dimension(330, 320));

        JPanel title = receptionHeader();

        // Ô tiền sử bệnh: khung bo góc, viền đổi màu khi focus giống các ô nhập khác.
        JScrollPane tienSuPane = new JScrollPane(tienSuArea);
        tienSuPane.setBorder(new VienNhap(BORDER, 1.2f));
        tienSuPane.setBackground(Color.WHITE);
        tienSuPane.getViewport().setBackground(Color.WHITE);
        tienSuPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tienSuPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        tienSuPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        tienSuPane.setPreferredSize(new Dimension(200, 88));
        tienSuArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                tienSuPane.setBorder(new VienNhap(PRIMARY, 1.8f));
                tienSuPane.repaint();
            }
            @Override public void focusLost(FocusEvent event) {
                tienSuPane.setBorder(new VienNhap(BORDER, 1.2f));
                tienSuPane.repaint();
            }
        });

        // Chia form thành ba nhóm có tiêu đề; các ô ngắn xếp hai cột.
        JPanel body = new KhungCuon();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0, 0, 6, 4));
        body.add(nhomTieuDe("Thông tin cá nhân"));
        body.add(Box.createVerticalStrut(10));
        body.add(khoi("Họ và tên *", hoTenField));
        body.add(Box.createVerticalStrut(12));
        body.add(hangDoi(khoi("Ngày sinh *", ngaySinhField),
                khoi("Giới tính *", gioiTinhChon)));
        body.add(Box.createVerticalStrut(20));
        body.add(nhomTieuDe("Liên hệ"));
        body.add(Box.createVerticalStrut(10));
        body.add(hangDoi(khoi("Số điện thoại *", sdtField), khoi("Email", emailField)));
        body.add(Box.createVerticalStrut(12));
        body.add(khoi("Địa chỉ", diaChiField));
        body.add(Box.createVerticalStrut(20));
        body.add(nhomTieuDe("Thông tin y tế"));
        body.add(Box.createVerticalStrut(10));
        body.add(khoi("Tiền sử bệnh", tienSuPane));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        // Ghi chú nhỏ trong khung xám nhạt có biểu tượng "i".
        JLabel note = label("Hồ sơ trùng họ tên + SĐT sẽ được dùng lại, không tạo mới.", 11, false);
        note.setIcon(new InfoIcon());
        note.setIconTextGap(8);
        HopTo noteBox = new HopTo(new BorderLayout(), ROW_ALT, BORDER);
        noteBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        noteBox.add(note, BorderLayout.CENTER);
        noteBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, noteBox.getPreferredSize().height));
        trangThai.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Phần cố định phía dưới (có đường kẻ phân cách): phòng khám, nút, ghi chú, trạng thái.
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 0, 0, 0)));
        bottom.add(roomRow());
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(buttonRow());
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(noteBox);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(trangThai);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    /** Tiêu đề khu vực Tiếp nhận: biểu tượng người + dấu cộng, tên, mô tả và nhãn ngày hôm nay. */
    private JPanel receptionHeader() {
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(label("Tiếp nhận bệnh nhân", 16, true));
        texts.add(Box.createVerticalStrut(4));
        texts.add(label("Nhập thông tin, cấp số thứ tự và phân phòng khám", 11, false));

        JPanel badge = new JPanel(new GridBagLayout());
        badge.setOpaque(false);
        badge.add(new IconBadge(4, PRIMARY, 42));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(badge, BorderLayout.WEST);
        left.add(texts, BorderLayout.CENTER);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        east.setOpaque(false);
        east.add(new Pill("Hôm nay · " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.add(left, BorderLayout.CENTER);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    /** Tiêu đề nhóm trường: chấm màu + chữ in hoa nhỏ + đường kẻ mảnh kéo dài hết hàng. */
    private static JPanel nhomTieuDe(String text) {
        JLabel name = new JLabel(text.toUpperCase(Locale.ROOT));
        name.setFont(font(Font.BOLD, 11));
        name.setForeground(PRIMARY_DARK);
        name.setIcon(new DotIcon(PRIMARY));
        name.setIconTextGap(6);

        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setPreferredSize(new Dimension(10, 1));
        JPanel lineWrap = new JPanel(new GridBagLayout());
        lineWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        lineWrap.add(line, gc);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(name, BorderLayout.WEST);
        row.add(lineWrap, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Một khối: nhãn nhỏ phía trên (dấu * bắt buộc màu đỏ), ô nhập phía dưới. */
    private static JPanel khoi(String caption, JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 6));
        block.setOpaque(false);
        JLabel name = label(caption, 12, true);
        name.setFont(font(Font.BOLD, 12));
        if (caption.endsWith("*")) {
            name.setText("<html>" + escapeHtml(caption.substring(0, caption.length() - 1).trim())
                    + " <font color='#EF4444'>*</font></html>");
        }
        block.add(name, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
        return block;
    }

    /** Hai khối cạnh nhau, chia đều chiều rộng. */
    private static JPanel hangDoi(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(left);
        row.add(right);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Khung "Phòng khám" nền xanh nhạt: biểu tượng cửa phòng, mô tả giới hạn, ô chọn phòng bên phải. */
    private JPanel roomRow() {
        phongChon.setFont(font(Font.PLAIN, 13));
        datKichThuoc(phongChon, 132, 38);

        JLabel name = label("", 12, true);
        name.setText("<html>Phòng khám <font color='#EF4444'>*</font></html>");
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(Box.createVerticalStrut(2));
        texts.add(label("Tối đa " + MAX_BENH_NHAN_MOI_PHONG + " bệnh nhân / phòng / ngày", 11, false));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new IconBadge(5, PRIMARY, 38));
        JPanel comboWrap = new JPanel(new GridBagLayout());
        comboWrap.setOpaque(false);
        comboWrap.add(phongChon);

        HopTo box = new HopTo(new BorderLayout(12, 0), new Color(0xF0F9FF), new Color(0xBAE6FD));
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        box.add(iconWrap, BorderLayout.WEST);
        box.add(texts, BorderLayout.CENTER);
        box.add(comboWrap, BorderLayout.EAST);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getPreferredSize().height));
        return box;
    }

    /** Hàng nút: "Nhập lại" (nút phụ) và "Tiếp nhận / Cấp số" (nút chính, chiếm phần còn lại). */
    private JPanel buttonRow() {
        datKichThuoc(lamLaiButton, 100, 42);
        tiepNhanButton.setFont(font(Font.BOLD, 13));
        tiepNhanButton.setPreferredSize(new Dimension(150, 42));
        tiepNhanButton.setMinimumSize(new Dimension(130, 42));

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        GridBagConstraints clear = cot(0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE);
        clear.insets = new Insets(0, 0, 0, 10);
        row.add(lamLaiButton, clear);
        row.add(tiepNhanButton, cot(1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
        return row;
    }

    private static GridBagConstraints cot(int gridx, double weightx, int anchor, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = 0;
        c.weightx = weightx;
        c.anchor = anchor;
        c.fill = fill;
        return c;
    }

    /** Xóa trắng form để nhập bệnh nhân kế tiếp (giữ nguyên phòng khám đang chọn). */
    private void lamLaiForm() {
        for (JTextField field : new JTextField[] {hoTenField, ngaySinhField, sdtField, emailField, diaChiField}) {
            if (field instanceof TruongNhap) ((TruongNhap) field).datLoi(false);
        }
        hoTenField.setText("");
        ngaySinhField.setText("");
        gioiTinhChon.setSelectedIndex(0);
        sdtField.setText("");
        emailField.setText("");
        diaChiField.setText("");
        tienSuArea.setText("");
        hoTenField.requestFocusInWindow();
    }

    /** Thông tin bệnh nhân đã kiểm tra hợp lệ từ form. */
    private static final class ThongTinBenhNhan {
        final String hoTen;
        final LocalDate ngaySinh;
        final String gioiTinh;
        final String sdt;
        final String email;     // rỗng nếu không nhập
        final String diaChi;    // rỗng nếu không nhập
        final String tienSu;    // rỗng nếu không nhập

        ThongTinBenhNhan(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                String email, String diaChi, String tienSu) {
            this.hoTen = hoTen;
            this.ngaySinh = ngaySinh;
            this.gioiTinh = gioiTinh;
            this.sdt = sdt;
            this.email = email;
            this.diaChi = diaChi;
            this.tienSu = tienSu;
        }
    }

    /** Kết quả giao dịch tiếp nhận để báo lại cho lễ tân. */
    private static final class KetQuaTiepNhan {
        final int maBenhNhan;
        final int soKham;
        final boolean hoSoMoi;

        KetQuaTiepNhan(int maBenhNhan, int soKham, boolean hoSoMoi) {
            this.maBenhNhan = maBenhNhan;
            this.soKham = soKham;
            this.hoSoMoi = hoSoMoi;
        }
    }

    /** Đọc và kiểm tra form; trả về null (kèm thông báo, đặt con trỏ vào ô sai) nếu chưa hợp lệ. */
    private ThongTinBenhNhan docForm() {
        String hoTen = hoTenField.getText().trim().replaceAll("\\s+", " ");
        if (hoTen.isEmpty()) return loiForm("Vui lòng nhập họ và tên bệnh nhân.", hoTenField);
        if (hoTen.chars().anyMatch(Character::isDigit)) {
            return loiForm("Họ và tên không được chứa chữ số.", hoTenField);
        }

        String ngayText = ngaySinhField.getText().trim();
        if (ngayText.isEmpty()) return loiForm("Vui lòng nhập ngày sinh (dd/MM/yyyy).", ngaySinhField);
        LocalDate ngaySinh;
        try {
            ngaySinh = LocalDate.parse(ngayText, NGAY_SINH);
        } catch (DateTimeParseException e) {
            return loiForm("Ngày sinh không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 15/08/1995.",
                    ngaySinhField);
        }
        if (ngaySinh.isAfter(LocalDate.now()) || ngaySinh.getYear() < 1900) {
            return loiForm("Ngày sinh phải từ năm 1900 đến hôm nay.", ngaySinhField);
        }

        String sdt = sdtField.getText().replaceAll("[\\s.\\-]", "");
        if (sdt.startsWith("+84")) sdt = "0" + sdt.substring(3);
        if (sdt.isEmpty()) return loiForm("Vui lòng nhập số điện thoại.", sdtField);
        if (!sdt.matches("0\\d{9}")) {
            return loiForm("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", sdtField);
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EmailHopLe.kiemTra(email)) {
            return loiForm(EmailHopLe.huongDan(), emailField);
        }

        return new ThongTinBenhNhan(hoTen, ngaySinh, String.valueOf(gioiTinhChon.getSelectedItem()),
                sdt, email, diaChiField.getText().trim(), tienSuArea.getText().trim());
    }

    private ThongTinBenhNhan loiForm(String message, JComponent field) {
        if (field instanceof TruongNhap) ((TruongNhap) field).datLoi(true); // viền đỏ cho tới khi sửa
        JOptionPane.showMessageDialog(this, message, "Thiếu hoặc sai thông tin",
                JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
        return null;
    }

    // ---------- 4.2 Xử lý khi bấm nút Tiếp nhận / Cấp số ----------

    /** Kiểm tra form, hỏi xác nhận, rồi tạo tiếp nhận trong SQL ở luồng nền để không đứng giao diện. */
    private void receiveFromForm() {
        if (loading || !canReceive) return;
        ThongTinBenhNhan info = docForm();
        if (info == null) return;
        String room = String.valueOf(phongChon.getSelectedItem());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tiếp nhận " + info.hoTen + " (SĐT " + info.sdt + ") vào " + room + "?\n"
                + "Nếu chưa có hồ sơ, hệ thống tạo hồ sơ bệnh nhân mới.\n"
                + "Số thứ tự sẽ được SQL Server tự cấp theo ngày.",
                "Xác nhận tiếp nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        setLoading(true);
        setStatus("Đang tiếp nhận...", WARNING);
        new SwingWorker<KetQuaTiepNhan, Void>() {
            @Override protected KetQuaTiepNhan doInBackground() throws Exception {
                return receiveInSql(info, room);
            }
            @Override protected void done() {
                try {
                    KetQuaTiepNhan result = get();
                    String queue = String.format("%03d", result.soKham);
                    JOptionPane.showMessageDialog(TrangChuLeTan.this,
                            "Tiếp nhận thành công!\nBệnh nhân: " + info.hoTen
                            + " (Mã BN " + result.maBenhNhan
                            + (result.hoSoMoi ? " - hồ sơ mới" : " - hồ sơ đã có") + ")"
                            + "\nSố khám: " + queue
                            + "\nPhòng: " + room,
                            "Phiếu tiếp nhận", JOptionPane.INFORMATION_MESSAGE);
                    setStatus("Tiếp nhận thành công · Số " + queue, GREEN);
                    lamLaiForm();
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

    // ---------- 4.3 Giao dịch SQL: cấp số và tiếp nhận ----------

    /**
     * Một giao dịch SQL Server 2012, làm lần lượt:
     * 1. Kiểm tra lại vai trò và quyền được Admin cấp.
     * 2. Khóa cấp số theo ngày qua sp_getapplock để không trùng khi nhiều lễ tân thao tác.
     * 3. Tìm hồ sơ bệnh nhân theo SĐT + họ tên; chưa có thì tạo hồ sơ mới từ form.
     * 4. Tìm lịch hẹn hôm nay của bệnh nhân (chưa có thì lát nữa tạo lịch hẹn trực tiếp).
     * 5. Kiểm tra phòng chưa đủ người, cấp số thứ tự (MAX + 1).
     * 6. Cập nhật hoặc tạo lịch hẹn, ghi nhật ký rồi COMMIT. Có lỗi thì ROLLBACK
     *    (hồ sơ mới vừa tạo cũng được hoàn tác).
     */
    private KetQuaTiepNhan receiveInSql(ThongTinBenhNhan info, String room) throws SQLException {
        try (Connection connection = KetNoi.mo()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                requirePermission(connection, "LICHHEN");
                requirePermission(connection, "BENHNHAN");
                if (!columnExists(connection)) {
                    throw new SQLException("Thiếu cột soThuTuKham. Hãy chạy SQLData/06_TiepNhanLeTan_SQL2012.sql.");
                }

                Date today = layNgayHienTai(connection);
                khoaBoCapSo(connection, today);
                int[] patient = timHoacTaoBenhNhan(connection, info);
                int patientId = patient[0];
                boolean created = patient[1] == 1;
                int appointmentId = timLichHenHomNay(connection, patientId, today);
                kiemTraGioiHanPhong(connection, today, room);
                int queue = capSoMoi(connection, today);

                if (appointmentId > 0) {
                    capNhatLichHen(connection, appointmentId, queue, room);
                } else {
                    taoLichHenTrucTiep(connection, patientId, queue, room);
                }
                ghiNhatKy(connection, patientId, queue, room, created);
                connection.commit();
                return new KetQuaTiepNhan(patientId, queue, created);
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException("Tiếp nhận không thành công.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * Dùng lại hồ sơ nếu đã có bệnh nhân cùng SĐT và họ tên; ngược lại thêm hồ sơ mới.
     * (Cùng SĐT nhưng khác họ tên vẫn là người khác, ví dụ người thân dùng chung số.)
     *
     * @return {mã bệnh nhân, 1 nếu vừa tạo mới / 0 nếu dùng lại hồ sơ cũ}
     */
    private int[] timHoacTaoBenhNhan(Connection connection, ThongTinBenhNhan info) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT TOP 1 maBN FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE sdt=? AND hoTen=? ORDER BY maBN")) {
            statement.setString(1, info.sdt);
            statement.setString(2, info.hoTen);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return new int[] {result.getInt(1), 0};
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.BenhNhan (hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,tienSuBenh,trangThai) "
                + "VALUES (?,?,?,?,?,?,?,N'Hoạt động')", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, info.hoTen);
            statement.setDate(2, Date.valueOf(info.ngaySinh));
            statement.setString(3, info.gioiTinh);
            statement.setString(4, info.sdt);
            datChuoiTuyChon(statement, 5, info.email);
            datChuoiTuyChon(statement, 6, info.diaChi);
            datChuoiTuyChon(statement, 7, info.tienSu);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return new int[] {keys.getInt(1), 1};
            }
        }
        throw new SQLException("Không lấy được mã bệnh nhân vừa tạo.");
    }

    /** Ô tùy chọn để trống thì lưu NULL thay vì chuỗi rỗng. */
    private static void datChuoiTuyChon(PreparedStatement statement, int index, String value)
            throws SQLException {
        if (value == null || value.isEmpty()) statement.setNull(index, Types.NVARCHAR);
        else statement.setString(index, value);
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

    /** Đã chạy script 06 (có cột soThuTuKham trong bảng LichHen) chưa? */
    private boolean columnExists(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SELECT COL_LENGTH('dbo.LichHen','soThuTuKham')")) {
            return result.next() && result.getObject(1) != null;
        }
    }

    /** Ngày hiện tại theo đồng hồ của SQL Server. */
    private Date layNgayHienTai(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT CAST(GETDATE() AS date)")) {
            result.next();
            return result.getDate(1);
        }
    }

    /** Khóa bộ cấp số của ngày hôm nay (tối đa chờ 10 giây). */
    private void khoaBoCapSo(Connection connection, Date today) throws SQLException {
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
    }

    /**
     * Tìm lịch hẹn hôm nay của bệnh nhân.
     * @return mã lịch hẹn, hoặc -1 nếu bệnh nhân chưa có lịch hẹn
     * @throws SQLException nếu bệnh nhân đã được cấp số hôm nay
     */
    private int timLichHenHomNay(Connection connection, int patientId, Date today) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT TOP 1 maLichHen,soThuTuKham FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE maBN=? AND ngayHen=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất') "
                + "ORDER BY CASE WHEN soThuTuKham IS NULL THEN 1 ELSE 0 END, maLichHen")) {
            statement.setInt(1, patientId);
            statement.setDate(2, today);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) return -1;
                int appointmentId = result.getInt(1);
                int oldQueue = result.getInt(2);
                if (!result.wasNull()) {
                    throw new SQLException("Bệnh nhân đã được tiếp nhận hôm nay, số khám: "
                            + String.format("%03d", oldQueue));
                }
                return appointmentId;
            }
        }
    }

    /** Mỗi phòng nhận tối đa MAX_BENH_NHAN_MOI_PHONG bệnh nhân trong ngày. */
    private void kiemTraGioiHanPhong(Connection connection, Date today, String room) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ngayHen=? AND phongKham=? AND soThuTuKham IS NOT NULL "
                + "AND trangThai NOT IN (N'Đã hủy')")) {
            statement.setDate(1, today);
            statement.setString(2, room);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) >= MAX_BENH_NHAN_MOI_PHONG) {
                    throw new SQLException("Phòng " + room + " đã đủ " + MAX_BENH_NHAN_MOI_PHONG
                            + " bệnh nhân trong hôm nay. Vui lòng chọn phòng khác.");
                }
            }
        }
    }

    /** Số thứ tự mới = số lớn nhất của hôm nay + 1. */
    private int capSoMoi(Connection connection, Date today) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT ISNULL(MAX(soThuTuKham),0)+1 FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                + "WHERE ngayHen=?")) {
            statement.setDate(1, today);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    /** Bệnh nhân đã có lịch hẹn: ghi số khám, phòng và đổi trạng thái. */
    private void capNhatLichHen(Connection connection, int appointmentId, int queue, String room)
            throws SQLException {
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
    }

    /** Bệnh nhân chưa có lịch hẹn: tạo lịch hẹn "Tiếp nhận trực tiếp". */
    private void taoLichHenTrucTiep(Connection connection, int patientId, int queue, String room)
            throws SQLException {
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

    private void ghiNhatKy(Connection connection, int patientId, int queue, String room,
            boolean created) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
            statement.setString(1, "Tiếp nhận bệnh nhân");
            statement.setString(2, "LichHen");
            statement.setString(3, "maTK=" + user.id + " | maBN=" + patientId
                    + (created ? " (hồ sơ mới)" : "") + " | STT=" + queue + " | " + room);
            statement.executeUpdate();
        }
    }

    // ================= 5. TẢI DỮ LIỆU TỪ SQL SERVER =================

    /** Tải dữ liệu ở luồng nền (SwingWorker), xong thì đổ lên giao diện. */
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
                    hienThiDuLieu(get());
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

    private void hienThiDuLieu(ReportData data) {
        canReceive = data.receiveAllowed;
        fillTable(phieuModel, data.examRows);
        phieuCount.setText(String.valueOf(data.examRows.size()));
        tongBenhNhan.setText(String.valueOf(data.totalPatients));
        choTiepNhan.setText(String.valueOf(data.waitingToday));
        daTiepNhan.setText(String.valueOf(data.receivedToday));
        soPhieuKham.setText(String.valueOf(data.examsToday));
        if (!data.viewAllowed) {
            setStatus("Không có quyền BENHNHAN — danh sách đã được ẩn", DANGER);
        } else if (!data.receiveAllowed) {
            setStatus("Tài khoản không có quyền tiếp nhận (LICHHEN) — form đã bị khóa", WARNING);
        } else {
            setStatus("Đã đồng bộ SQL Server · Hôm nay: " + LocalDate.now(), GREEN);
        }
    }

    /** Kiểm tra quyền rồi đọc phiếu khám, bệnh nhân và các số thống kê. */
    private ReportData loadFromSql() throws SQLException {
        ReportData data = new ReportData();
        data.viewAllowed = db.coQuyenTaiKhoan(user.id, "BENHNHAN");
        data.receiveAllowed = data.viewAllowed && db.coQuyenTaiKhoan(user.id, "LICHHEN")
                && "Lễ tân".equalsIgnoreCase(user.tenVaiTro);
        if (!data.viewAllowed) return data;

        try (Connection connection = KetNoi.mo()) {
            boolean queueInstalled = columnExists(connection);
            loadPhieuKham(connection, data);
            loadThongKe(connection, data, queueInstalled);
        }
        return data;
    }

    /** Lấy toàn bộ thuộc tính của PhieuKham và cả hai mã khóa ngoại (LEFT JOIN để phiếu luôn hiện). */
    private void loadPhieuKham(Connection connection, ReportData data) throws SQLException {
        String sql =
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
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                data.examRows.add(new Object[]{
                    0, // STT, được đánh số lại trong fillTable
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
    }

    /** Bốn số trên các thẻ thống kê. Hai số "chưa nhận / đã tiếp nhận" cần cột soThuTuKham. */
    private void loadThongKe(Connection connection, ReportData data, boolean queueInstalled)
            throws SQLException {
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

    /** Chạy câu SQL trả về một số nguyên (ví dụ COUNT). */
    private int scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    private void fillTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        int stt = 1;
        for (Object[] values : rows) {
            Object[] copy = values.clone();
            copy[0] = stt++; // STT cố định theo thứ tự dữ liệu tải về.
            model.addRow(copy);
        }
    }

    /** Khóa / mở giao diện khi đang tải. */
    private void setLoading(boolean value) {
        loading = value;
        refreshButton.setEnabled(!value);
        datChoPhepNhap(!value && canReceive);
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    /** Bật / tắt toàn bộ form tiếp nhận (khóa khi đang tải hoặc khi không có quyền). */
    private void datChoPhepNhap(boolean on) {
        JComponent[] controls = {hoTenField, ngaySinhField, gioiTinhChon, sdtField, emailField,
            diaChiField, tienSuArea, phongChon, tiepNhanButton, lamLaiButton};
        for (JComponent control : controls) control.setEnabled(on);
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

    /** Gói kết quả tải từ SQL để chuyển từ luồng nền sang giao diện. */
    private static final class ReportData {
        boolean viewAllowed;
        boolean receiveAllowed;
        int totalPatients;
        int waitingToday;
        int receivedToday;
        int examsToday;
        final List<Object[]> examRows = new ArrayList<>();
    }

    // ================= 6. BẢNG, TÌM KIẾM, LỌC =================

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
        table.setFont(font(Font.PLAIN, 12));
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
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidth(table.getColumnName(i)));
        }
        return table;
    }

    /** Độ rộng mỗi cột theo tên cột. */
    private static int columnWidth(String title) {
        switch (title) {
            case "Mã BN":
            case "Mã NV":
                return 78;
            case "Bệnh nhân":
            case "Họ tên":
            case "Bác sĩ":
                return 190;
            case "Ngày khám":
            case "Ngày sinh":
                return 126;
            case "Triệu chứng":
            case "Chẩn đoán":
            case "Phương pháp điều trị":
            case "Kết quả":
            case "Tiền sử bệnh":
            case "Địa chỉ":
            case "Ghi chú":
                return 180;
            case "Trạng thái":
                return 140;
            default:
                return 125;
        }
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

    // ---------- Các hàm dựng giao diện nhỏ dùng chung ----------

    private static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(bold ? TEXT : MUTED);
        label.setFont(font(bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    private static void datKichThuoc(JComponent component, int width, int height) {
        Dimension size = new Dimension(width, height);
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }

    private static RoundedPanel card(LayoutManager layout) {
        RoundedPanel panel = new RoundedPanel(layout);
        panel.setBorder(new EmptyBorder(17, 18, 20, 18));
        return panel;
    }

    /** Tiêu đề khu vực: tên + mô tả nhỏ bên trái, nhãn đếm số dòng bên phải. */
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

    // ---------- Tìm kiếm và lọc ----------

    /** Viết tắt để tạo mảng chỉ số cột: cols(1, 2) tương đương new int[] {1, 2}. */
    private static int[] cols(int... columns) {
        return columns;
    }

    /** Một tiêu chí lọc: tên hiển thị trong ô chọn và các cột (theo chỉ số model) được xét. */
    private static final class Criterion {
        final String label;
        final int[] ids;     // cột mã: khớp chính xác
        final int[] texts;   // cột văn bản: tìm chứa, không phân biệt hoa thường/dấu
        final int[] phones;  // cột SĐT: chỉ xét khi nhập từ 6 số (chế độ Tất cả)

        Criterion(String label, int[] ids, int[] texts, int[] phones) {
            this.label = label;
            this.ids = ids;
            this.texts = texts;
            this.phones = phones;
        }
    }

    /** Thanh tìm kiếm: nút lọc (biểu tượng phễu) bên trái, ô nhập từ khóa bên phải. */
    private static JPanel searchBar(TableRowSorter<DefaultTableModel> sorter, Criterion[] criteria) {
        SearchField field = new SearchField("Tìm trong tất cả các trường...");
        FilterButton filterButton = new FilterButton();
        filterButton.setToolTipText("Lọc theo: " + criteria[0].label);

        // Mảng một phần tử để hàm lambda bên dưới đọc và đổi được tiêu chí đang chọn.
        Criterion[] chosen = {criteria[0]};
        Runnable apply = () -> filter(sorter, field.getText(), chosen[0]);
        field.getDocument().addDocumentListener(new SearchListener(apply));

        // Menu chọn tiêu chí lọc, mục đầu tiên ("Tất cả") là mặc định.
        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < criteria.length; i++) {
            Criterion criterion = criteria[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(criterion.label, i == 0);
            item.setFont(font(Font.PLAIN, 12));
            item.addActionListener(event -> {
                chosen[0] = criterion;
                boolean all = criterion == criteria[0];
                filterButton.setActive(!all);
                filterButton.setToolTipText("Lọc theo: " + criterion.label);
                field.setPrompt(all ? "Tìm trong tất cả các trường..."
                        : "Nhập " + criterion.label + " cần lọc...");
                apply.run();
            });
            group.add(item);
            menu.add(item);
            if (i == 0) menu.addSeparator();
        }
        filterButton.addActionListener(event ->
                menu.show(filterButton, 0, filterButton.getHeight() + 4));

        // Bọc nút trong GridBagLayout để nút nhỏ nằm giữa theo chiều dọc, không bị kéo cao bằng ô tìm kiếm.
        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);
        holder.add(filterButton);

        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bar.add(holder, BorderLayout.WEST);
        bar.add(field, BorderLayout.CENTER);
        return bar;
    }

    /**
     * Lọc nhanh có kiểm soát từng cột:
     * - Nhập toàn chữ số: khớp CHÍNH XÁC cột mã; cột SĐT chỉ xét khi nhập từ 6 số.
     * - Còn lại: tìm chứa (không phân biệt hoa thường và dấu) trong các cột văn bản.
     * Không quét toàn bộ cột nên "6" không khớp nhầm mã NV, ngày khám hay STT.
     */
    private static void filter(TableRowSorter<DefaultTableModel> sorter, String text, Criterion criterion) {
        String query = text == null ? "" : text.trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        boolean digits = query.chars().allMatch(Character::isDigit);
        String id = query.replaceFirst("^0+(?!$)", ""); // bỏ số 0 đứng đầu: "007" -> "7"
        String folded = fold(query);

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (digits) {
                    if (khopMa(entry, criterion.ids, id)) return true;
                    if (query.length() >= 6 && khopSdt(entry, criterion.phones, query)) return true;
                }
                return khopChu(entry, criterion.texts, folded);
            }
        });
    }

    private static boolean khopMa(RowFilter.Entry<?, ?> entry, int[] columns, String id) {
        for (int column : columns) {
            if (String.valueOf(entry.getValue(column)).trim().equals(id)) return true;
        }
        return false;
    }

    private static boolean khopSdt(RowFilter.Entry<?, ?> entry, int[] columns, String query) {
        for (int column : columns) {
            String phone = String.valueOf(entry.getValue(column)).replaceAll("\\D", "");
            if (phone.contains(query)) return true;
        }
        return false;
    }

    private static boolean khopChu(RowFilter.Entry<?, ?> entry, int[] columns, String folded) {
        for (int column : columns) {
            if (fold(String.valueOf(entry.getValue(column))).contains(folded)) return true;
        }
        return false;
    }

    /** Bỏ dấu tiếng Việt và hạ chữ thường để tìm kiếm không phụ thuộc dấu. */
    private static String fold(String value) {
        String plain = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return plain.toLowerCase(Locale.ROOT);
    }

    /** Gọi lại hàm lọc mỗi khi nội dung ô tìm kiếm thay đổi. */
    private static final class SearchListener implements DocumentListener {
        private final Runnable action;
        SearchListener(Runnable action) { this.action = action; }
        @Override public void insertUpdate(DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(DocumentEvent e) { action.run(); }
    }

    // ================= 7. THÀNH PHẦN VẼ RIÊNG (chỉ để làm đẹp giao diện) =================

    /** Bật khử răng cưa cho nét vẽ và chữ. */
    private static void khuRangCua(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Thanh chia hai khu vực: chỉ là khoảng trống cùng màu nền, không viền, không nút mũi tên. */
    private static final class ThanhChiaUI extends BasicSplitPaneUI {
        @Override public BasicSplitPaneDivider createDefaultDivider() {
            return new BasicSplitPaneDivider(this) {
                @Override public void paint(Graphics graphics) {
                    graphics.setColor(BACKGROUND);
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                }
            };
        }
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
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, 20, 20));
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillOval(w - 190, -70, 230, 230);
            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(w - 310, h - 70, 170, 170);
            g2.dispose();
        }
    }

    /**
     * Ô vuông bo góc màu nhạt chứa biểu tượng vẽ bằng nét:
     * 0 người, 1 đồng hồ, 2 dấu tick, 3 phiếu, 4 thêm người, 5 cửa phòng.
     * Biểu tượng vẽ trên lưới 48x48 rồi co giãn theo cỡ yêu cầu.
     */
    private static final class IconBadge extends JComponent {
        private final int kind;
        private final Color accent;
        private final int size;

        IconBadge(int kind, Color accent) {
            this(kind, accent, 48);
        }

        IconBadge(int kind, Color accent, int size) {
            this.kind = kind;
            this.accent = accent;
            this.size = size;
            setPreferredSize(new Dimension(size, size));
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            double scale = size / 48.0;
            g2.scale(scale, scale);
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
                case 4:
                    g2.drawOval(12, 12, 12, 12);
                    g2.drawArc(7, 28, 22, 16, 0, 180);
                    g2.drawLine(35, 15, 35, 25);
                    g2.drawLine(30, 20, 40, 20);
                    break;
                case 5:
                    g2.drawRoundRect(14, 10, 20, 28, 5, 5);
                    g2.drawLine(10, 38, 38, 38);
                    g2.fillOval(27, 23, 3, 3);
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
        RoundedPanel(LayoutManager layout) {
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
     * Nút bo góc, có trạng thái rê chuột / nhấn / vô hiệu.
     * Kiểu nút: PHU = nền trắng, CHINH = xanh, MO = mờ (dùng trên nền gradient).
     */
    private static final class RoundButton extends JButton {
        static final int PHU = 0;
        static final int CHINH = 1;
        static final int MO = 2;

        private final int kind;

        RoundButton(String text, int kind) {
            super(text);
            this.kind = kind;
            setFont(font(Font.BOLD, 12));
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
            if (kind == CHINH) {
                fill = !on ? new Color(0x94A3B8) : pressed ? new Color(0x075985)
                        : hover ? PRIMARY_DARK : PRIMARY;
                edge = fill;
                textColor = Color.WHITE;
            } else if (kind == MO) {
                fill = new Color(255, 255, 255, !on ? 25 : pressed ? 95 : hover ? 70 : 42);
                edge = new Color(255, 255, 255, on ? 130 : 60);
                textColor = on ? Color.WHITE : new Color(255, 255, 255, 140);
            } else {
                fill = !on ? new Color(0xF1F5F9) : pressed ? new Color(0xE2E8F0)
                        : hover ? new Color(0xF1F5F9) : Color.WHITE;
                edge = hover || pressed ? new Color(0xCBD5E1) : BORDER;
                textColor = on ? TEXT : new Color(0x94A3B8);
            }
            if (kind == CHINH && on) {
                Color top = pressed ? new Color(0x075985) : hover ? new Color(0x0284C7) : new Color(0x0EA5E9);
                Color bottom = pressed ? new Color(0x075985) : hover ? new Color(0x0369A1) : new Color(0x0284C7);
                g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            } else {
                g2.setColor(fill);
            }
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
            datKichThuoc(button, 0, 0);
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

    /** Nút lọc nhỏ: biểu tượng phễu, sáng lên khi đang lọc theo một trường cụ thể. */
    private static final class FilterButton extends JButton {
        private boolean active;

        FilterButton() {
            datKichThuoc(this, 32, 32);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void setActive(boolean value) {
            active = value;
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            boolean hover = getModel().isRollover() || getModel().isPressed();
            g2.setColor(active ? PRIMARY_LIGHT : hover ? new Color(0xF1F5F9) : ROW_ALT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
            g2.setColor(active ? PRIMARY : hover ? new Color(0xCBD5E1) : BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            // Vẽ hình cái phễu ở giữa nút.
            float cx = w / 2f;
            float cy = h / 2f;
            Path2D funnel = new Path2D.Float();
            funnel.moveTo(cx - 6.5f, cy - 5.5f);
            funnel.lineTo(cx + 6.5f, cy - 5.5f);
            funnel.lineTo(cx + 2f, cy);
            funnel.lineTo(cx + 2f, cy + 5.5f);
            funnel.lineTo(cx - 2f, cy + 3.5f);
            funnel.lineTo(cx - 2f, cy);
            funnel.closePath();
            g2.setColor(active ? PRIMARY : MUTED);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(funnel);
            g2.dispose();
        }
    }

    /** Viền bo góc cho ô tìm kiếm. */
    private static final class VienBo extends AbstractBorder {
        private final Color color;
        private final float thickness;

        VienBo(Color color, float thickness) {
            this.color = color;
            this.thickness = thickness;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            float o = thickness / 2f;
            g2.draw(new RoundRectangle2D.Float(x + o, y + o, w - thickness, h - thickness, 12, 12));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(7, 36, 7, 12);
        }
    }

    /** Ô tìm kiếm bo tròn có nền xám nhạt, biểu tượng kính lúp và chữ gợi ý; viền đổi màu khi focus. */
    private static final class SearchField extends JTextField {
        private String prompt;

        SearchField(String prompt) {
            this.prompt = prompt;
            setToolTipText(prompt);
            setFont(font(Font.PLAIN, 12));
            setForeground(TEXT);
            setOpaque(false);
            setPreferredSize(new Dimension(200, 40));
            setBorder(new VienBo(BORDER, 1.2f));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent event) {
                    setBorder(new VienBo(PRIMARY, 1.8f));
                    repaint();
                }
                @Override public void focusLost(FocusEvent event) {
                    setBorder(new VienBo(BORDER, 1.2f));
                    repaint();
                }
            });
        }

        void setPrompt(String value) {
            prompt = value;
            setToolTipText(value);
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            // Nền bo tròn.
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(hasFocus() ? Color.WHITE : ROW_ALT);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(graphics);

            // Kính lúp và chữ gợi ý (chỉ hiện khi ô trống và chưa focus).
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
            setFont(font(Font.BOLD, 11));
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

    // ---------- Bảng và các bộ hiển thị ô ----------

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
                g2.setFont(font(Font.PLAIN, 13));
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
            setFont(font(Font.BOLD, 11));
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

    /** Ô căn giữa với màu chữ riêng (dùng cho cột STT, cột mã, Mã BN, Mã NV). */
    private static final class CenterRenderer extends DefaultTableCellRenderer {
        private final Color color;
        private final boolean bold;

        CenterRenderer(Color color, boolean bold) {
            this.color = color;
            this.bold = bold;
        }

        @Override public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (bold) setFont(font(Font.BOLD, 12));
            if (!selected) setForeground(color);
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
            setFont(font(Font.PLAIN, 12));
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
                g2.setFont(font(Font.BOLD, 12));
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
            setFont(font(Font.BOLD, 11));
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

    /**
     * Ô nhập bo tròn của form tiếp nhận:
     * chữ gợi ý mờ khi trống, vòng sáng xanh khi focus, viền + nền đỏ nhạt khi nhập sai.
     */
    private static final class TruongNhap extends JTextField {
        private final String prompt;
        private boolean focused;
        private boolean loi;

        TruongNhap() {
            this("");
        }

        TruongNhap(String prompt) {
            this.prompt = prompt;
            setFont(font(Font.PLAIN, 13));
            setForeground(TEXT);
            setDisabledTextColor(MUTED);
            setCaretColor(PRIMARY);
            setSelectionColor(new Color(0xBAE6FD));
            setOpaque(false);
            setPreferredSize(new Dimension(120, 38));
            setBorder(new VienNhap(BORDER, 1.2f));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent event) {
                    focused = true;
                    capNhatVien();
                }
                @Override public void focusLost(FocusEvent event) {
                    focused = false;
                    capNhatVien();
                }
            });
            // Bắt đầu gõ lại thì bỏ trạng thái lỗi.
            getDocument().addDocumentListener(new SearchListener(() -> {
                if (loi) datLoi(false);
            }));
        }

        /** Bật / tắt trạng thái nhập sai (viền đỏ). */
        void datLoi(boolean value) {
            loi = value;
            capNhatVien();
        }

        private void capNhatVien() {
            if (loi) setBorder(new VienNhap(DANGER, 1.8f));
            else if (focused) setBorder(new VienNhap(PRIMARY, 1.8f));
            else setBorder(new VienNhap(BORDER, 1.2f));
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(loi ? new Color(0xFEF2F2) : isEnabled() ? Color.WHITE : ROW_ALT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            if (focused && !loi) {
                g2.setColor(new Color(2, 132, 199, 38));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Float(2.5f, 2.5f, w - 6, h - 6, 10, 10));
            }
            g2.dispose();
            super.paintComponent(graphics);

            if (getText().isEmpty() && !prompt.isEmpty()) {
                g2 = (Graphics2D) graphics.create();
                khuRangCua(g2);
                g2.setFont(getFont());
                g2.setColor(new Color(0x94A3B8));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(prompt, getInsets().left, (h - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Viền bo góc cho ô nhập form (lề trong gọn hơn viền ô tìm kiếm vì không có biểu tượng). */
    private static final class VienNhap extends AbstractBorder {
        private final Color color;
        private final float thickness;

        VienNhap(Color color, float thickness) {
            this.color = color;
            this.thickness = thickness;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            float o = thickness / 2f;
            g2.draw(new RoundRectangle2D.Float(x + o, y + o, w - thickness, h - thickness, 12, 12));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) {
            return new Insets(6, 12, 6, 12);
        }
    }

    /** Vùng nhập nhiều dòng (tiền sử bệnh) có chữ gợi ý mờ khi trống. */
    private static final class TextAreaNhap extends JTextArea {
        private final String prompt;

        TextAreaNhap(String prompt) {
            this.prompt = prompt;
            setFont(font(Font.PLAIN, 13));
            setForeground(TEXT);
            setDisabledTextColor(MUTED);
            setCaretColor(PRIMARY);
            setSelectionColor(new Color(0xBAE6FD));
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(null);
        }

        @Override public void setEnabled(boolean on) {
            super.setEnabled(on);
            setBackground(on ? Color.WHITE : ROW_ALT);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                khuRangCua(g2);
                g2.setFont(getFont());
                g2.setColor(new Color(0x94A3B8));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(prompt, getInsets().left, getInsets().top + fm.getAscent());
                g2.dispose();
            }
        }
    }

    /** Ô chọn (Giới tính, Phòng khám): bo tròn, viền đổi màu khi focus, mũi tên nhỏ, danh sách thả gọn. */
    private static final class ComboModern extends JComboBox<String> {
        ComboModern(String[] items) {
            super(items);
            setFont(font(Font.PLAIN, 13));
            setForeground(TEXT);
            setBackground(Color.WHITE);
            setOpaque(false);
            setBorder(new EmptyBorder(0, 0, 0, 0));
            setPreferredSize(new Dimension(120, 38));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, false);
                    setFont(font(Font.PLAIN, 13));
                    if (index < 0) {                       // phần đang hiển thị trong ô
                        setOpaque(false);
                        setForeground(ComboModern.this.isEnabled() ? TEXT : MUTED);
                        setBorder(new EmptyBorder(0, 12, 0, 4));
                    } else {                               // các dòng trong danh sách thả
                        setOpaque(true);
                        setBackground(selected ? PRIMARY_LIGHT : Color.WHITE);
                        setForeground(TEXT);
                        setBorder(new EmptyBorder(8, 12, 8, 12));
                    }
                    return this;
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent event) { repaint(); }
                @Override public void focusLost(FocusEvent event) { repaint(); }
            });
        }

        @Override public void updateUI() {
            setUI(new ComboUI());
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(isEnabled() ? Color.WHITE : ROW_ALT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.dispose();

            super.paintComponent(graphics);   // giá trị đang chọn + mũi tên

            g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            boolean active = isEnabled() && (hasFocus() || isPopupVisible());
            g2.setColor(active ? PRIMARY : BORDER);
            g2.setStroke(new BasicStroke(active ? 1.8f : 1.2f));
            float o = active ? 0.9f : 0.6f;
            g2.draw(new RoundRectangle2D.Float(o, o, w - 2 * o - 1, h - 2 * o - 1, 12, 12));
            g2.dispose();
        }
    }

    /** Giao diện ô chọn: không tự tô nền, mũi tên chữ V mảnh, danh sách thả viền mảnh. */
    private static final class ComboUI extends BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g2 = (Graphics2D) graphics.create();
                    khuRangCua(g2);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.setColor(comboBox.isEnabled() ? MUTED : new Color(0xCBD5E1));
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawPolyline(new int[] {cx - 4, cx, cx + 4}, new int[] {cy - 2, cy + 2, cy - 2}, 3);
                    g2.dispose();
                }
            };
            button.setBorder(null);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            return button;
        }

        @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Nền do ComboModern tự vẽ (bo góc) nên ở đây không tô gì.
        }

        @Override protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(2, 2, 2, 2)));
            return popup;
        }
    }

    /** Khung bo góc có màu nền và màu viền tùy chọn (khung Phòng khám, khung ghi chú). */
    private static final class HopTo extends JPanel {
        private final Color fill;
        private final Color edge;

        HopTo(LayoutManager layout, Color fill, Color edge) {
            super(layout);
            this.fill = fill;
            this.edge = edge;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(edge);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
        }
    }

    /** Biểu tượng chữ "i" trong vòng tròn, đứng trước dòng ghi chú. */
    private static final class InfoIcon implements Icon {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x, y, 13, 13);
            g2.drawLine(x + 7, y + 6, x + 7, y + 10);
            g2.fillOval(x + 6, y + 3, 2, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 14; }
        @Override public int getIconHeight() { return 14; }
    }

    /** Khung chứa form: rộng đúng bằng khung nhìn (không cuộn ngang), cao theo nội dung để cuộn dọc. */
    private static final class KhungCuon extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 18; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) { return 72; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}