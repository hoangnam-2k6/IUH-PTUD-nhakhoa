
package UI.Chung;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class GiaoDienHeThong {
    public static final Color PRIMARY = new Color(35, 111, 170);
    public static final Color BG = Color.WHITE, WHITE = Color.WHITE;
    public static final Color MENU = new Color(238, 245, 252);
    public static final Color BORDER = new Color(205, 218, 229);
    public static final Color TEXT = new Color(37, 55, 76);
    public static final Color MUTED = new Color(98, 112, 126);
    public static final Color GREEN = new Color(34, 139, 94);
    public static final Color HOVER = new Color(218, 236, 249);
    public static final Font FONT = new Font("Arial", Font.PLAIN, 13);
    public static final Color RED = new Color(191, 59, 59);
   

    private GiaoDienHeThong() {}

    // 1. JLabel dùng chung.
    public static JLabel label(String chu, int co, boolean dam) {
        JLabel l = new JLabel(chu);
        l.setFont(new Font("Arial", dam ? Font.BOLD : Font.PLAIN, co));
        l.setForeground(dam ? TEXT : MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // 2. Tất cả Button có cùng cách trình bày.
    public static void trangTriNut(JButton b) {
        b.setFont(new Font("Arial", Font.PLAIN, 12));
        b.setForeground(PRIMARY);
        b.setBackground(new Color(244, 249, 253));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JButton nut(String ten, boolean main) {
        JButton b = new JButton(ten);
        trangTriNut(b);
        b.setPreferredSize(new Dimension(
                Math.max(b.getPreferredSize().width, 70), 32));
        return b;
    }

    // 3. Button dành cho Sidebar.
    public static JButton nutMenu(String ten) {
        JButton b = nut(ten, false);
        b.setFont(FONT);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setBackground(WHITE);
        b.setForeground(TEXT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setPreferredSize(new Dimension(220, 44));
        return b;
    }

    // 4. Sidebar chung cho Admin, Lễ tân và Quản lý.
   public static JPanel thanhBen(JPanel menu, JButton dangXuat) {
    JPanel sidebar = new JPanel(new BorderLayout());
    sidebar.setBackground(MENU);
    sidebar.setPreferredSize(new Dimension(250, 0));
    sidebar.setBorder(BorderFactory.createMatteBorder(
            0, 0, 0, 1, BORDER));

    JLabel logo = label("MEDIDENT", 23, true);
    logo.setForeground(TEXT);
    logo.setHorizontalAlignment(SwingConstants.CENTER);
    logo.setPreferredSize(new Dimension(250, 85));
    sidebar.add(logo, BorderLayout.NORTH);

    menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
    menu.setOpaque(false);
    menu.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

    JScrollPane cuon = new JScrollPane(menu);
    cuon.setBorder(null);
    cuon.getViewport().setBackground(MENU);
    cuon.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    cuon.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
    cuon.getVerticalScrollBar().setUnitIncrement(16);

    sidebar.add(cuon, BorderLayout.CENTER);

    trangTriNut(dangXuat);

    JPanel duoi = new JPanel(new BorderLayout());
    duoi.setOpaque(false);
    duoi.setBorder(BorderFactory.createEmptyBorder(10, 12, 14, 12));
    duoi.add(dangXuat, BorderLayout.CENTER);

    sidebar.add(duoi, BorderLayout.SOUTH);
    return sidebar;
}



    // 5. Header chung: tên ứng dụng và thông tin tài khoản.
    public static JPanel dauTrang(String tenTrang, String hoTen, String vaiTro) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.setPreferredSize(new Dimension(0, 94));
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel ten = label("MEDIDENT / " + tenTrang, 21, true);
        ten.setForeground(PRIMARY);
        p.add(ten, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 4));
        info.setOpaque(false);

        JLabel nguoiDung = label(hoTen == null ? "" : hoTen, 15, true);
        JLabel role = label(vaiTro == null ? "" : vaiTro, 13, false);
        nguoiDung.setHorizontalAlignment(SwingConstants.RIGHT);
        role.setHorizontalAlignment(SwingConstants.RIGHT);

        info.add(nguoiDung);
        info.add(role);
        p.add(info, BorderLayout.EAST);
        return p;
    }

    // 6. Đổi màu menu khi chuyển trang.
    public static void chonMenu(Map<String, JButton> danhSach, String ma) {
        for (Map.Entry<String, JButton> muc : danhSach.entrySet()) {
            boolean dangChon = muc.getKey().equals(ma);
            JButton nut = muc.getValue();

            nut.setBackground(dangChon ? HOVER : WHITE);
            nut.setForeground(dangChon ? PRIMARY : TEXT);
        }
    }

    // 7. Khung giao diện.
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    // 8. JPanel hỗ trợ cuộn dọc.
    private static final class CotCuon extends JPanel implements Scrollable {
        CotCuon() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(WHITE);
        }

        @Override
        protected void addImpl(Component c, Object v, int i) {
            if (c instanceof JComponent)
                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            super.addImpl(c, v, i);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(760, 560);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(40, r.height - 50);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public static JPanel trang(String ten, String moTa) {
        JPanel p = new CotCuon();
        p.setBorder(new EmptyBorder(15, 15, 20, 15));

        JLabel tieuDe = label(ten.toUpperCase(), 20, true);
        tieuDe.setForeground(PRIMARY);

        p.add(tieuDe);
        p.add(Box.createVerticalStrut(5));
        p.add(label(moTa, 12, false));
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    public static JScrollPane cuon(JPanel p) {
        JScrollPane cuon = new JScrollPane(p);
        cuon.setBorder(null);
        cuon.getViewport().setBackground(WHITE);
        cuon.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        return cuon;
    }

    public static JPanel hang() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 1)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE,
                        getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    // 9. TextField dùng chung.
    public static JTextField text(String chu) {
        JTextField o = new JTextField(chu, 23);
        o.setFont(FONT);
        o.setBorder(new CompoundBorder(new LineBorder(BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        return o;
    }

    // 10. JTable cơ bản, giữ nguyên TableModel và dữ liệu.
    public static JComponent wrapTable(JTable table) {
        bang(table);
        table.setAutoCreateRowSorter(true);

        if (table.getColumnCount() > 8) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < table.getColumnCount(); i++)
                table.getColumnModel().getColumn(i)
                        .setPreferredWidth(i == 1 ? 155 : 115);
        }

        if (table.getColumnCount() > 0
                && "STT".equals(table.getColumnName(0))) {
            TableColumn cot = table.getColumnModel().getColumn(0);
            cot.setMinWidth(40);
            cot.setMaxWidth(65);
            cot.setPreferredWidth(50);
        }

        JScrollPane cuon = new JScrollPane(table);
        cuon.setBorder(new LineBorder(BORDER));
        return cuon;
    }

    public static void bang(JTable bang) {
        bang.setFont(FONT);
        bang.setRowHeight(30);
        bang.setGridColor(BORDER);
        bang.setFillsViewportHeight(true);
        bang.setSelectionBackground(HOVER);
        bang.setSelectionForeground(TEXT);

        JTableHeader h = bang.getTableHeader();
        if (h != null) {
            h.setFont(new Font("Arial", Font.PLAIN, 12));
            h.setBackground(new Color(234, 240, 246));
            h.setForeground(TEXT);
            h.setReorderingAllowed(false);
        }
    }

    // 11. Đồng bộ các thành phần hiện có.
    // Không đổi ActionListener, dữ liệu, DAO hoặc phân quyền.
    public static void dongBo(Component goc) {
        if (goc instanceof JButton b) {
            trangTriNut(b);
        } else if (goc instanceof JTextField t) {
            t.setFont(FONT);
            t.setBorder(new CompoundBorder(new LineBorder(BORDER),
                    new EmptyBorder(5, 7, 5, 7)));
        } else if (goc instanceof JComboBox<?> cb) {
            cb.setFont(FONT);
        } else if (goc instanceof JTable t) {
            bang(t);
        } else if (goc instanceof JLabel l) {
            Font f = l.getFont();
            l.setFont(new Font("Arial", f.getStyle(), f.getSize()));
        }

        if (goc instanceof Container p) {
            for (Component c : p.getComponents()) dongBo(c);
        }
    }

    // 12. Logo nếu có ảnh; nếu thiếu thì dùng chữ M.
    public static ImageIcon logo(int rong, int cao) {
        String[] paths = {
            "/Image/medident_logo.png",
            "/Image/logo.png"
        };

        BufferedImage out = new BufferedImage(
                rong, cao, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();

        try {
            for (String p : paths) {
                URL url = GiaoDienHeThong.class.getResource(p);
                if (url == null) continue;

                try {
                    BufferedImage image = ImageIO.read(url);
                    if (image == null) continue;

                    double scale = Math.min(
                            (double) rong / image.getWidth(),
                            (double) cao / image.getHeight());
                    int w = (int) (image.getWidth() * scale);
                    int h = (int) (image.getHeight() * scale);

                    g.drawImage(image, (rong - w) / 2,
                            (cao - h) / 2, w, h, null);
                    return new ImageIcon(out);
                } catch (IOException ignored) {}
            }

            g.setColor(PRIMARY);
            g.fillRoundRect(2, 2, rong - 4, cao - 4, 14, 14);
            g.setColor(WHITE);
            g.setFont(new Font("Arial", Font.BOLD,
                    Math.max(12, cao / 2)));

            String t = "M";
            g.drawString(t,
                    (rong - g.getFontMetrics().stringWidth(t)) / 2,
                    (cao + g.getFontMetrics().getAscent()) / 2 - 3);
            return new ImageIcon(out);
        } finally {
            g.dispose();
        }
    }

    // 13. Hiển thị lỗi, không chỉnh nội dung nghiệp vụ.
    public static void loi(Component cha, Exception ex) {
        Throwable goc = ex;
        while (goc.getCause() != null) goc = goc.getCause();

        JOptionPane.showMessageDialog(cha,
                goc.getMessage() == null ? goc.toString() : goc.getMessage(),
                "MediDent - Có lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
