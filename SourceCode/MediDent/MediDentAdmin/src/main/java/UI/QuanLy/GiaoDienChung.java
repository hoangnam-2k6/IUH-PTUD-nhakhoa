package UI.QuanLy;

import java.awt.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/** Một kiểu nút, bảng, font và màu dùng chung cho toàn bộ quản lý. */
public final class GiaoDienChung {
    public static final Color XANH = new Color(35,116,175);
    public static final Color NEN = new Color(247,250,253);
    public static final Color VIEN = new Color(207,220,231);
    public static final Color CHU = new Color(34,55,76);
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    private static final Font FONT = new Font("Arial",Font.PLAIN,13);
    private GiaoDienChung() { }

    public static final class MucChon {
        public final String ma, ten;
        public MucChon(String ma,String ten){this.ma=ma;this.ten=ten;}
        @Override public String toString(){return ma+" - "+ten;}
    }

    public static JComboBox<MucChon> danhSach(java.util.List<String[]> ds,String maDaChon) {
        JComboBox<MucChon> box=new JComboBox<>();
        for(String[] dong:ds){
            MucChon muc=new MucChon(dong[0],dong[1]);
            box.addItem(muc);
            if(dong[0].equals(maDaChon))box.setSelectedItem(muc);
        }
        return box;
    }

    public static JButton nut(String noiDung) {
    	JButton nut = new JButton(noiDung) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        trangTriNut(nut);
        return nut;
    }


    public static void trangTriNut(JButton nut) {
    	nut.setFont(new Font("Segoe UI", Font.BOLD, 12));
    	nut.setBackground(Color.white);
    	nut.setForeground(TEXT);
    	nut.setContentAreaFilled(false);
    	nut.setOpaque(false);
    	nut.setFocusPainted(false);
    	nut.setBorder(new EmptyBorder(10, 14, 10, 14));
    	nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
}


    public static JPanel hang() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        p.setOpaque(false);
        return p;
    }


    public static JPanel khung(String tieuDe) {
        JPanel p=new JPanel(new BorderLayout(8,8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(14,15,14,15));
        return p;
    }

    public static JLabel tieuDe(String ten) {
        JLabel label = new JLabel(ten);
        label.setForeground(XANH);
        label.setFont(new Font("Arial",Font.BOLD,21));
        return label;
    }

  
    
  public static void bang(JTable bang) {
    bang.setFont(new Font("Arial", Font.PLAIN, 13));
    bang.setRowHeight(30);
    bang.setShowGrid(true);
    bang.setGridColor(new Color(225, 225, 225));
    bang.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    bang.setFillsViewportHeight(true);

    bang.setSelectionBackground(UIManager.getColor("Table.selectionBackground"));
    bang.setSelectionForeground(UIManager.getColor("Table.selectionForeground"));

    JTableHeader header = bang.getTableHeader();
    header.setFont(new Font("Arial", Font.PLAIN, 12));
    header.setBackground(UIManager.getColor("TableHeader.background"));
    header.setForeground(UIManager.getColor("TableHeader.foreground"));
    header.setReorderingAllowed(false);

    // Căn lề và hiển thị đầy đủ nội dung khi rê chuột.
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {

            super.getTableCellRendererComponent(table, value, selected, focus, row, column);

            String tenCot = table.getColumnName(column).toLowerCase();
            setToolTipText(value == null ? null : value.toString());

            if (tenCot.equals("stt") || tenCot.startsWith("mã")) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else if (value instanceof Number) {
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    };

    for (int i = 0; i < bang.getColumnCount(); i++) {
        bang.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }

    // Giữ sorter cũ để không làm mất chức năng tìm kiếm.
    TableRowSorter<?> sorter = null;

    if (bang.getRowSorter() instanceof TableRowSorter<?>) {
        sorter = (TableRowSorter<?>) bang.getRowSorter();
    } else if (bang.getRowSorter() == null) {
        TableRowSorter<TableModel> moi = new TableRowSorter<>(bang.getModel());
        bang.setRowSorter(moi);
        sorter = moi;
    }

    if (sorter != null) {
        for (int i = 0; i < bang.getColumnCount(); i++) {
            String ten = bang.getColumnName(i).toLowerCase();

            if (ten.equals("stt") || ten.startsWith("mã")) {
                sorter.setComparator(i, GiaoDienChung::soSanhMa);
            }
        }
    }
}

    
private static int soSanhMa(Object a, Object b) {
    if (a == null) return b == null ? 0 : -1;
    if (b == null) return 1;

    if (a instanceof Number && b instanceof Number) {
        return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()));
    }

    String x = a.toString().trim();
    String y = b.toString().trim();

    Pattern p = Pattern.compile("^(.*?)(\\d+)$");
    Matcher mx = p.matcher(x);
    Matcher my = p.matcher(y);

    if (mx.matches() && my.matches()
            && mx.group(1).equalsIgnoreCase(my.group(1))) {
        return new BigInteger(mx.group(2)).compareTo(new BigInteger(my.group(2)));
    }

    return x.compareToIgnoreCase(y);
}
    public static JPanel thanhCongCu(Component... thanhPhan) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 5);

        for (int i = 0; i < thanhPhan.length; i++) {
            c.gridx = i;
            c.weightx = thanhPhan[i] instanceof JTextField ? 1 : 0;
            c.fill = thanhPhan[i] instanceof JTextField
                    ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;

            p.add(thanhPhan[i], c);
        }

        return p;
    }

    public static void dongBo(Component goc) {
        if(goc instanceof JButton) trangTriNut((JButton)goc);
        if(goc instanceof JTextField) {
            JTextField o=(JTextField)goc;
            o.setFont(FONT);
            o.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                    BorderFactory.createEmptyBorder(5,7,5,7)));
        }
        if(goc instanceof JComboBox<?>) goc.setFont(FONT);
        if(goc instanceof JTable) bang((JTable)goc);
        if(goc instanceof Container) {
            for(Component con:((Container)goc).getComponents()) dongBo(con);
        }
    }

    public static void baoLoi(Component cha,Exception loi) {
        Throwable goc = loi;
        while(goc.getCause()!=null)goc=goc.getCause();
        String thongBao=goc.getMessage()==null?goc.getClass().getSimpleName():goc.getMessage();
        JOptionPane.showMessageDialog(cha,thongBao,"MediDent - Có lỗi",JOptionPane.ERROR_MESSAGE);
    }
}
