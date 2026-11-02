package UI.LeTan;

import java.awt.BasicStroke;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Cursor;

import java.awt.Dimension;

import java.awt.Font;

import java.awt.FontMetrics;

import java.awt.Graphics2D;

import java.awt.Graphics2D;


import java.awt.Graphics;

import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.Rectangle;

import java.awt.event.FocusAdapter;

import java.awt.event.FocusEvent;

import java.awt.event.FocusEvent;


import java.awt.geom.Path2D;


import java.awt.geom.RoundRectangle2D;


import java.text.Normalizer;

import java.util.Locale;


import javax.swing.BorderFactory;

import javax.swing.ButtonGroup;

import javax.swing.DefaultListCellRenderer;

import javax.swing.JButton;

import javax.swing.JButton;


import javax.swing.JComboBox;

import javax.swing.JList;

import javax.swing.JPanel;

import javax.swing.JPopupMenu;

import javax.swing.JRadioButtonMenuItem;

import javax.swing.JTextArea;


import javax.swing.JTextField;


import javax.swing.RowFilter;

import javax.swing.border.AbstractBorder;


import javax.swing.border.EmptyBorder;

import javax.swing.event.DocumentEvent;

import javax.swing.event.DocumentListener;


import javax.swing.plaf.basic.BasicComboBoxUI;

import javax.swing.plaf.basic.BasicComboPopup;

import javax.swing.plaf.basic.ComboPopup;


import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableRowSorter;


import static UI.LeTan.GiaoDien.BORDER;

import static UI.LeTan.GiaoDien.DANGER;

import static UI.LeTan.GiaoDien.MUTED;

import static UI.LeTan.GiaoDien.PRIMARY;

import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;

import static UI.LeTan.GiaoDien.ROW_ALT;

import static UI.LeTan.GiaoDien.TEXT;

import static UI.LeTan.GiaoDien.datKichThuoc;

import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.font;


import static UI.LeTan.GiaoDien.khuRangCua;



/** Gọi lại một hành động mỗi khi nội dung ô nhập thay đổi. */
final class BoLangNgheTimKiem implements DocumentListener {
    private final Runnable action;

    public BoLangNgheTimKiem(Runnable action) {
        this.action = action;
    }

    @Override public void insertUpdate(DocumentEvent e) { action.run(); }
    @Override public void removeUpdate(DocumentEvent e) { action.run(); }
    @Override public void changedUpdate(DocumentEvent e) { action.run(); }
}

/** Ô chọn (Giới tính, Phòng khám): bo tròn, viền đổi màu khi focus, mũi tên nhỏ, danh sách thả gọn. */
final class HopChonHienDai extends JComboBox<String> {

    public HopChonHienDai(String[] items) {
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
                    setForeground(HopChonHienDai.this.isEnabled() ? TEXT : MUTED);
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
            // Nền do HopChonHienDai tự vẽ (bo góc) nên ở đây không tô gì.
        }

        @Override protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), new EmptyBorder(2, 2, 2, 2)));
            return popup;
        }
    }
}

/** Nút lọc nhỏ: biểu tượng phễu, sáng lên khi đang lọc theo một trường cụ thể. */
final class NutLoc extends JButton {
    private boolean active;

    public NutLoc() {
        datKichThuoc(this, 32, 32);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setActive(boolean value) {
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

/** Ô tìm kiếm bo tròn có nền xám nhạt, biểu tượng kính lúp và chữ gợi ý; viền đổi màu khi focus. */
final class OTimKiem extends JTextField {
    private String prompt;

    public OTimKiem(String prompt) {
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

    public void setPrompt(String value) {
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

/**
 * Thanh tìm kiếm có nút chọn tiêu chí lọc (biểu tượng phễu) và ô nhập từ khóa.
 *
 * Lọc nhanh có kiểm soát từng cột:
 * - Nhập toàn chữ số: khớp CHÍNH XÁC cột mã; cột SĐT chỉ xét khi nhập từ 6 số.
 * - Còn lại: tìm chứa (không phân biệt hoa thường và dấu) trong các cột văn bản.
 * Không quét toàn bộ cột nên "6" không khớp nhầm mã NV, ngày khám hay STT.
 */
final class ThanhTimKiem {

    private ThanhTimKiem() { }

    /** Một tiêu chí lọc: tên hiển thị trong menu và các cột (theo chỉ số model) được xét. */
    public static final class TieuChi {
        final String label;
        final int[] ids;     // cột mã: khớp chính xác
        final int[] texts;   // cột văn bản: tìm chứa, không phân biệt hoa thường/dấu
        final int[] phones;  // cột SĐT: chỉ xét khi nhập từ 6 số (chế độ Tất cả)

        public TieuChi(String label, int[] ids, int[] texts, int[] phones) {
            this.label = label;
            this.ids = ids;
            this.texts = texts;
            this.phones = phones;
        }
    }

    /** Viết tắt để tạo mảng chỉ số cột: cols(1, 2) tương đương new int[] {1, 2}. */
    public static int[] cols(int... columns) {
        return columns;
    }

    /** Thanh tìm kiếm: nút lọc bên trái, ô nhập từ khóa bên phải. Tiêu chí đầu tiên là mặc định ("Tất cả"). */
    public static JPanel taoThanh(TableRowSorter<DefaultTableModel> sorter, TieuChi[] criteria) {
        OTimKiem field = new OTimKiem("Tìm trong tất cả các trường...");
        NutLoc filterButton = new NutLoc();
        filterButton.setToolTipText("Lọc theo: " + criteria[0].label);

        // Mảng một phần tử để hàm lambda bên dưới đọc và đổi được tiêu chí đang chọn.
        TieuChi[] chosen = {criteria[0]};
        Runnable apply = () -> loc(sorter, field.getText(), chosen[0]);
        field.getDocument().addDocumentListener(new BoLangNgheTimKiem(apply));

        JPopupMenu menu = new JPopupMenu();
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < criteria.length; i++) {
            TieuChi criterion = criteria[i];
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

    private static void loc(TableRowSorter<DefaultTableModel> sorter, String text, TieuChi criterion) {
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
}

/**
 * Ô nhập bo tròn của form tiếp nhận:
 * chữ gợi ý mờ khi trống, vòng sáng xanh khi focus, viền + nền đỏ nhạt khi nhập sai.
 */
final class TruongNhap extends JTextField {
    private final String prompt;
    private boolean focused;
    private boolean loi;

    public TruongNhap() {
        this("");
    }

    public TruongNhap(String prompt) {
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
        getDocument().addDocumentListener(new BoLangNgheTimKiem(() -> {
            if (loi) datLoi(false);
        }));
    }

    /** Bật / tắt trạng thái nhập sai (viền đỏ). */
    public void datLoi(boolean value) {
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
final class VienNhap extends AbstractBorder {
    private final Color color;
    private final float thickness;

    public VienNhap(Color color, float thickness) {
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
final class VungVanBanNhap extends JTextArea {
    private final String prompt;

    public VungVanBanNhap(String prompt) {
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
