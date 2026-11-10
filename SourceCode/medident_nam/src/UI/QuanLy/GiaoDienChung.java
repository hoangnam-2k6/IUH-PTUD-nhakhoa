package UI.QuanLy;

import java.awt.*;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

/** Một kiểu nút, bảng, font và màu dùng chung cho toàn bộ quản lý. */
public final class GiaoDienChung {
    public static final Color XANH = new Color(35,116,175);
    public static final Color NEN = new Color(247,250,253);
    public static final Color VIEN = new Color(207,220,231);
    public static final Color CHU = new Color(34,55,76);
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
        JButton nut = new JButton(noiDung);
        trangTriNut(nut);
        return nut;
    }


    public static void trangTriNut(JButton nut) {
    nut.setUI(new javax.swing.plaf.basic.BasicButtonUI());
    nut.setFont(new Font("Arial", Font.PLAIN, 12));
    nut.setOpaque(true);
    nut.setContentAreaFilled(true);
    nut.setFocusPainted(false);
    nut.setBackground(new Color(240, 247, 252));
    nut.setForeground(new Color(37, 99, 143));

    nut.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(VIEN),
        BorderFactory.createEmptyBorder(5, 8, 5, 8)
    ));

    nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    int rong = nut.getFontMetrics(nut.getFont()).stringWidth(nut.getText()) + 20;
    nut.setPreferredSize(new Dimension(Math.max(42, rong), 30));
    nut.setMinimumSize(new Dimension(42, 30));
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
        bang.setRowHeight(27);
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

        if (bang.getModel() instanceof DefaultTableModel model) {
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

            for (int i = 0; i < model.getColumnCount(); i++) {
                String tenCot = model.getColumnName(i).toLowerCase(Locale.ROOT);

                if (tenCot.equals("stt") || tenCot.startsWith("mã")) {
                    sorter.setComparator(i, GiaoDienChung::soSanhMa);
                }
            }

            bang.setRowSorter(sorter);
        }
    }

    private static int soSanhMa(Object a, Object b) {
        String x = String.valueOf(a);
        String y = String.valueOf(b);

        if (x.matches("\\d+") && y.matches("\\d+")) {
            return Long.compare(Long.parseLong(x), Long.parseLong(y));
        }

        String soX = x.replaceAll("\\D", "");
        String soY = y.replaceAll("\\D", "");
        String dauX = x.replaceAll("\\d", "");
        String dauY = y.replaceAll("\\d", "");

        if (!soX.isEmpty() && !soY.isEmpty() && dauX.equalsIgnoreCase(dauY)) {
            return Long.compare(Long.parseLong(soX), Long.parseLong(soY));
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
