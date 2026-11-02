package UI.LeTan;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.Font;

import java.awt.Font;


import java.awt.FontMetrics;

import java.awt.GradientPaint;

import java.awt.Graphics2D;

import java.awt.Graphics;

import java.awt.Graphics;


import java.awt.Rectangle;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;


import java.awt.geom.RoundRectangle2D;


import javax.swing.BorderFactory;

import javax.swing.JComponent;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTable;

import javax.swing.ScrollPaneConstants;

import javax.swing.SwingConstants;

import javax.swing.border.EmptyBorder;

import javax.swing.border.EmptyBorder;


import javax.swing.plaf.basic.BasicSplitPaneDivider;

import javax.swing.plaf.basic.BasicSplitPaneUI;


import javax.swing.table.DefaultTableCellRenderer;


import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableCellRenderer;

import javax.swing.table.TableCellRenderer;


import javax.swing.table.TableRowSorter;


import static UI.LeTan.GiaoDien.BACKGROUND;


import static UI.LeTan.GiaoDien.BORDER;

import static UI.LeTan.GiaoDien.MUTED;

import static UI.LeTan.GiaoDien.PRIMARY_LIGHT;

import static UI.LeTan.GiaoDien.ROW_ALT;

import static UI.LeTan.GiaoDien.ROW_HOVER;

import static UI.LeTan.GiaoDien.TEXT;

import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.font;


import static UI.LeTan.GiaoDien.khuRangCua;



/** Các hàm dựng bảng dữ liệu dùng chung: model chỉ đọc, bảng kẻ sọc, bộ sắp xếp, khung cuộn. */
final class BangDuLieu {

    private BangDuLieu() { }

    public static DefaultTableModel taoModel(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : Object.class;
            }
        };
    }

    public static JTable taoBang(DefaultTableModel model) {
        JTable table = new BangKeSoc(model);
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
        table.getTableHeader().setDefaultRenderer(new BoVeTieuDeCot());
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(48); // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(86); // Mã phiếu / Mã BN
        for (int i = 2; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(doRongCot(table.getColumnName(i)));
        }
        return table;
    }

    /** Bộ sắp xếp cho bảng; cột STT (cột 0) không cho sắp xếp. */
    public static TableRowSorter<DefaultTableModel> taoSorter(JTable table, DefaultTableModel model) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortable(0, false);
        return sorter;
    }

    public static void datCot(JTable table, int column, TableCellRenderer renderer) {
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    public static JScrollPane boCuon(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.getHorizontalScrollBar().setUnitIncrement(24);
        pane.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        pane.getHorizontalScrollBar().setUI(new ThanhCuonHienDai());
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        pane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        return pane;
    }

    /** Độ rộng mỗi cột theo tên cột. */
    private static int doRongCot(String title) {
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
}

/** Bảng kẻ sọc xen kẽ, làm nổi dòng khi rê chuột, có thông báo khi không có dữ liệu. */
final class BangKeSoc extends JTable {
    int hoverRow = -1;

    public BangKeSoc(DefaultTableModel model) {
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

    /** Màu nền dòng: chọn > rê chuột > kẻ sọc xen kẽ. */
    static Color rowBackground(JTable table, int row, boolean selected) {
        if (selected) return table.getSelectionBackground();
        if (table instanceof BangKeSoc && ((BangKeSoc) table).hoverRow == row) return ROW_HOVER;
        return row % 2 == 0 ? Color.WHITE : ROW_ALT;
    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JComponent && !(c instanceof BoVeNhan) && !(c instanceof BoVeAvatar)) {
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

/** Banner gradient xanh với vài vòng tròn trang trí mờ phía phải. */
final class BangTieuDe extends JPanel {

    public BangTieuDe() {
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

/** Tiêu đề cột: nền xám nhạt, chữ nhỏ đậm, kẻ chân. STT / Mã phiếu / Mã BN / Mã NV căn giữa. */
final class BoVeTieuDeCot extends DefaultTableCellRenderer {
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

/** Thanh chia hai khu vực: chỉ là khoảng trống cùng màu nền, không viền, không nút mũi tên. */
final class ThanhChiaUI extends BasicSplitPaneUI {
    @Override public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicSplitPaneDivider(this) {
            @Override public void paint(Graphics graphics) {
                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }
}
