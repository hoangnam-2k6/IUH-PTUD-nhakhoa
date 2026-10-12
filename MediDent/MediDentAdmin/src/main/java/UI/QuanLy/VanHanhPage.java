package UI.QuanLy;

import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.*;

/** Bảng nghiệp vụ dùng chung, không tạo JFrame/Sidebar mới. */
abstract class VanHanhPage extends JPanel {
    @FunctionalInterface protected interface TaiDuLieu { List<QuanLyDAO.Hang> tai() throws Exception; }
    @FunctionalInterface protected interface HanhDong { void chay() throws Exception; }

    protected final QuanLyDAO dao;
    protected final JTable table;
    protected final DefaultTableModel model;
    private final JLabel thongBao;
    private final JPanel nut;
    private final JPanel tableCard;
    private final List<QuanLyDAO.Hang> hang = new ArrayList<>();
    private TaiDuLieu nguon;

    protected VanHanhPage(QuanLyDAO dao, String title, String description, String... columns) {
        this.dao=dao;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel page=GiaoDien.trang(title,description);
        add(GiaoDien.cuon(page),BorderLayout.CENTER);

        JPanel toolbar=GiaoDien.card(new BorderLayout(10,0));
        toolbar.setAlignmentX(LEFT_ALIGNMENT);
        JTextField search=GiaoDien.text("");
        search.setToolTipText("Tìm trong các cột của bảng");
        search.setPreferredSize(new Dimension(250,38));
        JPanel searchPanel = new JPanel(new BorderLayout(0, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(GiaoDien.label("Tìm nhanh", 11, true), BorderLayout.NORTH);
        searchPanel.add(search, BorderLayout.CENTER);
        toolbar.add(searchPanel, BorderLayout.WEST);
        nut=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        nut.setOpaque(false);
        toolbar.add(nut,BorderLayout.EAST);
        page.add(toolbar);
        page.add(Box.createVerticalStrut(14));

        String[] headers=new String[columns.length+1];
        headers[0]="STT";
        System.arraycopy(columns,0,headers,1,columns.length);
        model=new DefaultTableModel(headers,0) {
            @Override public boolean isCellEditable(int row,int col) {return false;}
        };
        // STT đi theo dòng đang nhìn thấy, không trùng với ID thật trong SQL.
        table=new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r,int row,int col) {
                Component c=super.prepareRenderer(r,row,col);
                if (col==0 && c instanceof JLabel) {
                    ((JLabel)c).setText(String.valueOf(row+1));
                    ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        };
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI",Font.PLAIN,12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,11));
        table.getColumnModel().getColumn(0).setPreferredWidth(54);
        for(int i=1;i<table.getColumnCount();i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(165);
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(model);
        sorter.setSortable(0,false);
        table.setRowSorter(sorter);
        tableCard=GiaoDien.card(new BorderLayout(0,10));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        tableCard.setPreferredSize(new Dimension(750,600));
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE,600));
        JScrollPane scroll=new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll,BorderLayout.CENTER);
        thongBao=GiaoDien.label("Chưa tải dữ liệu",11,false);
        tableCard.add(thongBao,BorderLayout.SOUTH);
        page.add(tableCard);
        search.getDocument().addDocumentListener(SharedListeners.document(() -> {
            String q=search.getText().trim();
            sorter.setRowFilter(q.isEmpty()?null:RowFilter.regexFilter("(?iu)"+Pattern.quote(q)));
            capNhatSoDong();
        }));
        themNut("Tải lại",false,BieuTuong.Loai.TAI_LAI,this::taiLai);
    }

    protected void themNut(String text,boolean primary,BieuTuong.Loai icon,HanhDong action) {
        JButton b=GiaoDien.nutIcon(text,primary,icon);
        b.addActionListener(e->thucHien(action));
        nut.add(b);
        nut.revalidate();
    }

    protected void thucHien(HanhDong action) {
        try { action.chay(); }
        catch(Exception e) { GiaoDien.loi(this,e); }
    }

    protected void nap(TaiDuLieu data) { nguon=data; taiLai(); }
    protected void taiLai() {
        if(nguon==null)return;
        thucHien(() -> {
            List<QuanLyDAO.Hang> rows=nguon.tai();
            hang.clear();hang.addAll(rows);
            model.setRowCount(0);
            for(QuanLyDAO.Hang r:rows) {
                Object[] a=new Object[r.cot.length+1];
                a[0]=model.getRowCount()+1;
                System.arraycopy(r.cot,0,a,1,r.cot.length);
                model.addRow(a);
            }
            capNhatSoDong();
            // Nhiều bản ghi -> bảng cao hơn, trang bên ngoài có thể cuộn dọc.
            int height = Math.min(880, Math.max(470, 95 + model.getRowCount() * 42));
            tableCard.setPreferredSize(new Dimension(750, height));
            tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
            tableCard.revalidate();
        });
    }
    private void capNhatSoDong() {
        thongBao.setText("Đang hiển thị "+table.getRowCount()+" / "+model.getRowCount()+" dòng");
    }
    protected QuanLyDAO.Hang daChon() {
        int v=table.getSelectedRow();
        if(v<0) {
            JOptionPane.showMessageDialog(this,"Vui lòng chọn một dòng.");
            return null;
        }
        int m=table.convertRowIndexToModel(v);
        return m>=0&&m<hang.size()?hang.get(m):null;
    }
}
