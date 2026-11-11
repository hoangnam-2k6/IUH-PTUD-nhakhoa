package UI.QuanLy;

import DAO.ThanhToanKhuyenMaiDAO;
import DAO.ThanhToanKhuyenMaiDAO.KhuyenMai;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Quản lý mã khuyến mãi: mã được SQL tự cấp, lọc danh sách tại giao diện. */
public final class QuanLyKhuyenMaiPanel extends JPanel {
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    private static final Color BLUE=new Color(2,132,199), BG=new Color(248,250,252),LINE=new Color(226,232,240);
    private static final Color CHU=new Color(15,23,42), PHU=new Color(100,116,139);
    private final ThanhToanKhuyenMaiDAO dao;
    private final int actor;
    private final DefaultTableModel model=new DefaultTableModel(
            new String[]{"Mã KM","Tên khuyến mãi","Kiểu","Giá trị","Bắt đầu","Kết thúc","Hiệu lực"},0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable bang=new JTable(model);
    private final TableRowSorter<DefaultTableModel> sapXep=new TableRowSorter<>(model);
    private final JTextField tim=new JTextField(23),ma=new JTextField(),ten=new JTextField(),giaTri=new JTextField();
    private final ChonNgayKhuyenMai batDau=new ChonNgayKhuyenMai(),ketThuc=new ChonNgayKhuyenMai();
    private final JComboBox<String> boLoc=new JComboBox<>(new String[]{"Tất cả","Đang áp dụng","Tạm dừng","Chưa bắt đầu","Đã hết hạn"});
    private final JComboBox<String> loai=new JComboBox<>(new String[]{"PHAN_TRAM","SO_TIEN"});
    private final JCheckBox bat=new JCheckBox("Bật áp dụng",true);
    private final JButton luu=taoNut("Lưu khuyến mãi");
    private final JLabel loiTen=new JLabel(" "),loiGiaTri=new JLabel(" "),
            loiBatDau=new JLabel(" "),loiKetThuc=new JLabel(" ");
    private List<KhuyenMai> ds=new ArrayList<>();
    private boolean taoMoi=true;
    private final CardLayout trang = new CardLayout();
    private final JPanel cacTrang = new JPanel(trang);
    private static final String DANH_SACH = "danhSach", NHAP = "nhap";

    public QuanLyKhuyenMaiPanel(ThanhToanKhuyenMaiDAO dao,int maTK){
        this.dao=dao;this.actor=maTK;
        setLayout(new BorderLayout(12,14));setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
        JLabel title=new JLabel("Quản lý mã khuyến mãi");title.setFont(new Font("Segoe UI",Font.BOLD,25));
        JLabel note=new JLabel("Mã được cấp tự động · ưu đãi chỉ áp dụng khi được bật và còn thời hạn");
        note.setForeground(new Color(71,85,105));
        JPanel titles=new JPanel(new java.awt.GridLayout(2,1,0,5));titles.setOpaque(false);
        titles.add(title);titles.add(note);
        JPanel top=new JPanel(new BorderLayout(0,14));top.setOpaque(false);top.add(titles,BorderLayout.NORTH);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));toolbar.setOpaque(false);
        JButton moi=taoNut("Thêm mới");
        JButton sua=taoNut("Sửa");
        JButton timKiem=taoNut("Tìm kiếm");
        JButton lamMoi=taoNut("Làm mới");
        moi.addActionListener(e->{
        	lamMoiForm();trang.show(cacTrang,NHAP);});
        sua.addActionListener(e->{
            if(bang.getSelectedRow()<0){JOptionPane.showMessageDialog(this,"Hãy chọn mã khuyến mãi cần sửa.");return;}
            chon();trang.show(cacTrang,NHAP);
        });
        timKiem.addActionListener(e->loc());
        lamMoi.addActionListener(e->{tim.setText("");boLoc.setSelectedIndex(0);loc();taiDuLieu();});
        toolbar.add(moi);toolbar.add(sua);toolbar.add(new JLabel("Tìm kiếm:"));
        tim.setPreferredSize(new Dimension(220,34));toolbar.add(tim);
        toolbar.add(boLoc);toolbar.add(timKiem);toolbar.add(lamMoi);
        top.add(toolbar,BorderLayout.SOUTH);
        JPanel danhSach=new JPanel(new BorderLayout(0,12));danhSach.setOpaque(false);
        danhSach.add(top,BorderLayout.NORTH);
        bang.setRowHeight(34);bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.setRowSorter(sapXep);
        JPanel bangWrap=taoNhom();bangWrap.add(new JScrollPane(bang),BorderLayout.CENTER);
        danhSach.add(bangWrap,BorderLayout.CENTER);
        JPanel form=taoNhom();
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(24,26,20,26)));
        JPanel fields=new JPanel();fields.setBackground(Color.WHITE);
        fields.setBorder(BorderFactory.createEmptyBorder(4,6,8,6));
        fields.setLayout(new BoxLayout(fields,BoxLayout.Y_AXIS));
        themTruong(fields,"Mã khuyến mãi (SQL tự tạo)",ma);
        themTruong(fields,"Tên khuyến mãi",ten,loiTen);
        themTruong(fields,"Loại giảm: PHAN_TRAM / SO_TIEN",loai);
        themTruong(fields,"Giá trị (%, hoặc VND)",giaTri,loiGiaTri);
        themTruong(fields,"Ngày bắt đầu",batDau,loiBatDau);
        themTruong(fields,"Ngày kết thúc",ketThuc,loiKetThuc);
        ma.setBackground(new Color(241,245,249));
        bat.setBackground(Color.WHITE);
        bat.setFont(new Font("Segoe UI",Font.BOLD,13));
        bat.setForeground(CHU);fields.add(bat);
        JLabel hint=new JLabel("Ngừng áp dụng thay vì xóa để giữ lịch sử hóa đơn.");
        hint.setForeground(new Color(100,116,139));fields.add(hint);
        JScrollPane cuonNhap=new JScrollPane(fields);
        cuonNhap.setBorder(null);
        cuonNhap.getViewport().setBackground(Color.WHITE);
        cuonNhap.getVerticalScrollBar().setUnitIncrement(16);
        form.add(cuonNhap,BorderLayout.CENTER);
        JButton quayLai=taoNut("Quay lại danh sách");
        quayLai.setBackground(Color.WHITE);quayLai.setForeground(BLUE);
        quayLai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(11,18,11,18)));
        quayLai.addActionListener(e->trang.show(cacTrang,DANH_SACH));
        luu.addActionListener(e->luu());
        JPanel nutForm=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));nutForm.setOpaque(false);
        nutForm.setBorder(BorderFactory.createMatteBorder(1,0,0,0,LINE));
        nutForm.add(quayLai);nutForm.add(luu);
        form.add(nutForm,BorderLayout.SOUTH);
        JPanel nhap=new JPanel(new BorderLayout(0,12));nhap.setOpaque(false);
        JLabel tieuDeForm=new JLabel("Thông tin khuyến mãi");
        tieuDeForm.setFont(new Font("Segoe UI",Font.BOLD,24));
        tieuDeForm.setForeground(CHU);
        tieuDeForm.setBorder(BorderFactory.createEmptyBorder(0,2,4,0));
        nhap.add(tieuDeForm,BorderLayout.NORTH);nhap.add(form,BorderLayout.CENTER);
        cacTrang.setOpaque(false);
        cacTrang.add(danhSach,DANH_SACH);cacTrang.add(nhap,NHAP);
        add(cacTrang,BorderLayout.CENTER);
        for(JTextField f:new JTextField[]{ten,giaTri}) {
            f.getDocument().addDocumentListener(new DocumentListener(){
                public void insertUpdate(DocumentEvent e){kiemTraForm(false);}
                public void removeUpdate(DocumentEvent e){kiemTraForm(false);}
                public void changedUpdate(DocumentEvent e){kiemTraForm(false);}
            });
            f.addFocusListener(new java.awt.event.FocusAdapter(){
                @Override public void focusLost(java.awt.event.FocusEvent e){kiemTraForm(false);}
            });
        }
        batDau.addPropertyChangeListener("ngay", e->kiemTraForm(false));
        ketThuc.addPropertyChangeListener("ngay", e->kiemTraForm(false));
        loai.addActionListener(e->kiemTraForm(false));
        lamMoiForm();taiDuLieu();
    }
    private static JButton taoNut(String text){
    	JButton b = new JButton(text) {
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
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(Color.white);
        b.setForeground(TEXT);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
        }
    private static JPanel taoNhom(){
    	JPanel p=new JPanel(new BorderLayout(8,8));
    		p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(16,16,16,16)));
        return p;
        }
    private static void themTruong(JPanel p,String label,javax.swing.JComponent field){
        JLabel l=new JLabel(label);
        l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(CHU);
        l.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(600,42));
        field.setMaximumSize(new Dimension(999,42));
        field.setFont(new Font("Segoe UI",Font.PLAIN,13));
        if(field instanceof JTextField){
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                    BorderFactory.createEmptyBorder(8,12,8,12)));
        } else if(field instanceof JComboBox){
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(LINE));
        }
        p.add(l);p.add(Box.createVerticalStrut(6));p.add(field);p.add(Box.createVerticalStrut(18));
    }
    private static void themTruong(JPanel p,String label,javax.swing.JComponent field,JLabel loi){
        JLabel l=new JLabel(label);
        l.setFont(new Font("Segoe UI",Font.BOLD,13));
        l.setForeground(CHU);
        l.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(600,42));
        field.setMaximumSize(new Dimension(999,42));
        field.setFont(new Font("Segoe UI",Font.PLAIN,13));
        if(field instanceof JTextField){
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                    BorderFactory.createEmptyBorder(8,12,8,12)));
        } else if(field instanceof JComboBox){
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(LINE));
        }
        loi.setForeground(new Color(220,38,38));
        loi.setFont(new Font("Segoe UI",Font.PLAIN,11));
        loi.setAlignmentX(LEFT_ALIGNMENT);
        loi.setMaximumSize(new Dimension(999,25));
        p.add(l);p.add(Box.createVerticalStrut(6));
        p.add(field);
        p.add(Box.createVerticalStrut(3));
        p.add(loi);
        p.add(Box.createVerticalStrut(11));
    }
    private static void hienLoi(JLabel label,String message){
        label.setText(message==null?" ":message);
    }
    /** Thông báo lỗi trực tiếp ngay dưới từng ô, không hiện lỗi nhập liệu ở cuối form. */
    private boolean kiemTraForm(boolean khiLuu){
        boolean hopLe=true;
        String t=ten.getText().trim();
        boolean saiTen=t.isEmpty()||t.length()>120;
        hienLoi(loiTen,saiTen&&(khiLuu||!t.isEmpty())?
                "Tên phải có từ 1 đến 120 ký tự.":null);
        hopLe &=!saiTen;
        String g=giaTri.getText().trim();
        String msgGia=null;
        if(!g.isEmpty()||khiLuu){
            try{
                BigDecimal v=new BigDecimal(g);
                if(v.signum()<=0)msgGia="Giá trị phải lớn hơn 0.";
                else if("PHAN_TRAM".equals(loai.getSelectedItem())&&v.compareTo(new BigDecimal("100"))>0)
                    msgGia="Phần trăm không được vượt quá 100.";
                else if(v.scale()>2)msgGia="Tối đa 2 chữ số sau dấu thập phân.";
            }catch(NumberFormatException e){msgGia="Chỉ nhập số, ví dụ 10 hoặc 300000.";}
        }
        hienLoi(loiGiaTri,msgGia);hopLe &=!g.isEmpty()&&msgGia==null;
        LocalDate tu=null,den=null;
        String loiTu=null,loiDen=null;
        tu=batDau.layNgay();
        den=ketThuc.layNgay();
        if(tu!=null&&den!=null&&den.isBefore(tu))loiDen="Ngày kết thúc phải từ ngày bắt đầu trở đi.";
        hienLoi(loiBatDau,loiTu);hienLoi(loiKetThuc,loiDen);
        hopLe &=tu!=null&&den!=null&&loiDen==null;
        return hopLe;
    }
    private static String hieuLuc(KhuyenMai k){
        if(!k.bat)return "Tạm dừng";
        LocalDate today=LocalDate.now();
        if(today.isBefore(k.batDau))
        	return "Chưa bắt đầu";
        if(today.isAfter(k.ketThuc))
        	return "Đã hết hạn";
        return "Đang áp dụng";
    }
    private void loc(){
        String q=tim.getText().trim();
        String trangThai=(String)boLoc.getSelectedItem();
        List<RowFilter<Object,Object>> filters=new ArrayList<>();
        if(!q.isEmpty())
        	filters.add(RowFilter.regexFilter("(?iu)"+Pattern.quote(q),0,1));
        if(!"Tất cả".equals(trangThai))
        	filters.add(RowFilter.regexFilter("^"+Pattern.quote(trangThai)+"$",6));
        sapXep.setRowFilter(filters.isEmpty()?null:RowFilter.andFilter(filters));
    }
    private void lamMoiForm(){
        taoMoi=true;ma.setEditable(false);
        ma.setText("(Tự sinh khi lưu)");
        ten.setText("");giaTri.setText("");
        loai.setSelectedIndex(0);
        batDau.datNgay(LocalDate.now());
        ketThuc.datNgay(LocalDate.now().plusMonths(1));
        bat.setSelected(true);
        bang.clearSelection();kiemTraForm(false);
    }
    private void taiDuLieu(){
        new SwingWorker<List<KhuyenMai>,Void>() {
            @Override protected List<KhuyenMai> doInBackground() throws Exception {return dao.danhSachKhuyenMai(actor);}
            @Override protected void done(){
                try{ds=get();model.setRowCount(0);
                    for(KhuyenMai k:ds)model.addRow(new Object[]{k.ma,k.ten,k.loai,k.giaTri.toPlainString(),
                        k.batDau,k.ketThuc,hieuLuc(k)});
                }catch(Exception ex){JOptionPane.showMessageDialog(QuanLyKhuyenMaiPanel.this,
                    ex.getCause()==null?ex.getMessage():ex.getCause().getMessage(),
                    "Không thể tải khuyến mãi",JOptionPane.ERROR_MESSAGE);}
            }
        }.execute();
    }
    private void chon(){
        int row=bang.getSelectedRow();if(row<0)return;
        int index=bang.convertRowIndexToModel(row);
        if(index>=ds.size())return;
        KhuyenMai k=ds.get(index);
        taoMoi=false;
        ma.setText(k.ma);
        ma.setEditable(false);
        ten.setText(k.ten);
        loai.setSelectedItem(k.loai);
        giaTri.setText(k.giaTri.toPlainString());
        batDau.datNgay(k.batDau);
        ketThuc.datNgay(k.ketThuc);
        bat.setSelected(k.bat);
        kiemTraForm(false);
    }
    private void luu(){
        if(!kiemTraForm(true))return;
        final KhuyenMai k;
        try{k=new KhuyenMai(taoMoi?"":ma.getText().trim(),ten.getText().trim(),
                (String)loai.getSelectedItem(),new BigDecimal(giaTri.getText().trim()),
                batDau.layNgay(),ketThuc.layNgay(),bat.isSelected());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this,"Kiểm tra giá trị giảm.",
                "Dữ liệu không hợp lệ",JOptionPane.ERROR_MESSAGE);return;
        }
        final boolean moi=taoMoi;luu.setEnabled(false);
        new SwingWorker<String,Void>(){
            @Override protected String doInBackground() throws Exception {return dao.luuKhuyenMai(k,moi,actor);}
            @Override protected void done(){
                luu.setEnabled(true);
                try{String maMoi=get();JOptionPane.showMessageDialog(QuanLyKhuyenMaiPanel.this,
                    "Đã lưu mã khuyến mãi: "+maMoi);
                taiDuLieu();
                lamMoiForm();
                trang.show(cacTrang,DANH_SACH);
                }catch(Exception ex){JOptionPane.showMessageDialog(QuanLyKhuyenMaiPanel.this,
                    ex.getCause()==null?ex.getMessage():ex.getCause().getMessage(),
                    "Không lưu được khuyến mãi",JOptionPane.ERROR_MESSAGE);}
            }
        }.execute();
    }
}
