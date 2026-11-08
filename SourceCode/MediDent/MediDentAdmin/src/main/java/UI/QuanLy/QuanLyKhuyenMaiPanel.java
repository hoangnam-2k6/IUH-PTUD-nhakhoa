package UI.QuanLy;

import DAO.ThanhToanKhuyenMaiDAO;
import DAO.ThanhToanKhuyenMaiDAO.KhuyenMai;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Quản lý khuyến mãi: danh sách và form toàn panel, không ảnh hưởng chức năng khác. */
public final class QuanLyKhuyenMaiPanel extends JPanel {
    private static final Color XANH=new Color(0x0284C7), NEN=new Color(0xF8FAFC);
    private static final Color VIEN=new Color(0xE2E8F0), DO=new Color(0xEF4444), CHU=new Color(0x0F172A);
    private static final DateTimeFormatter NGAY=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ThanhToanKhuyenMaiDAO dao;
    private final int actor;
    private final CardLayout chuyenTrang=new CardLayout();
    private final JPanel noiDung=new JPanel(chuyenTrang);
    private final DefaultTableModel model=new DefaultTableModel(
            new String[]{"Mã KM","Tên khuyến mãi","Loại","Giá trị","Bắt đầu","Kết thúc","Trạng thái"},0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable bang=new JTable(model);
    private final JTextField ma=new JTextField(),ten=new JTextField(),giaTri=new JTextField();
    private final JComboBox<String> loai=new JComboBox<>(new String[]{"PHAN_TRAM","SO_TIEN"});
    private final ChonLich batDau=new ChonLich(),ketThuc=new ChonLich();
    private final JCheckBox hoatDong=new JCheckBox("Đang áp dụng",true);
    private final JLabel baoNhap=new JLabel(" ");
    private final JButton nutLuu=nut("Lưu khuyến mãi");
    private final Border vienThuong=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
            BorderFactory.createEmptyBorder(7,9,7,9));
    private List<KhuyenMai> ds=new ArrayList<>();
    private boolean taoMoi;

    public QuanLyKhuyenMaiPanel(ThanhToanKhuyenMaiDAO dao,int maTK) {
        this.dao=dao;actor=maTK;
        setLayout(new BorderLayout());setBackground(NEN);
        setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        noiDung.setOpaque(false);add(noiDung,BorderLayout.CENTER);
        noiDung.add(danhSach(),"DANH_SACH");
        noiDung.add(formNhap(),"FORM");
        chuyenTrang.show(noiDung,"DANH_SACH");
        ((AbstractDocument)giaTri.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet attrs)
                    throws BadLocationException {
                String moi=new StringBuilder(fb.getDocument().getText(0,fb.getDocument().getLength()))
                        .replace(offset,offset+length,text==null?"":text).toString();
                if(moi.matches("[0-9]*([.,][0-9]{0,2})?"))super.replace(fb,offset,length,text,attrs);
            }
            @Override public void insertString(FilterBypass fb,int offset,String str,AttributeSet attrs)
                    throws BadLocationException {replace(fb,offset,0,str,attrs);}
            @Override public void remove(FilterBypass fb,int offset,int length) throws BadLocationException {
                super.remove(fb,offset,length);
            }
        });
        FocusAdapter roiO=new FocusAdapter(){
            @Override public void focusLost(FocusEvent e){kiemTraNhap();}
        };
        ma.addFocusListener(roiO);ten.addFocusListener(roiO);giaTri.addFocusListener(roiO);
        loai.addActionListener(e->kiemTraNhap());
        batDau.khiChon(()->kiemTraNhap());ketThuc.khiChon(()->kiemTraNhap());
        taiDuLieu();
    }

    private JPanel danhSach() {
        JPanel p=new JPanel(new BorderLayout(0,16));p.setOpaque(false);
        p.add(tieuDe("Quản lý mã khuyến mãi","Danh sách khuyến mãi của phòng khám"),BorderLayout.NORTH);
        JPanel table=the();table.setLayout(new BorderLayout(0,12));
        bang.setRowHeight(38);bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.setAutoCreateRowSorter(true);bang.setFillsViewportHeight(true);
        bang.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        table.add(new JScrollPane(bang),BorderLayout.CENTER);
        p.add(table,BorderLayout.CENTER);
        JPanel duoi=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));duoi.setOpaque(false);
        JPanel nutDuoi=new JPanel(new GridLayout(2,1,0,9));nutDuoi.setOpaque(false);
        JButton nutThem=nut("+ Thêm mới"),nutSua=nut("Sửa mã đã chọn");
        nutThem.addActionListener(e->moForm(null));
        nutSua.addActionListener(e->{
            int row=bang.getSelectedRow();
            if(row<0){thongBao("Chọn một mã khuyến mãi cần sửa.");return;}
            moForm(ds.get(bang.convertRowIndexToModel(row)));
        });
        nutDuoi.add(nutThem);nutDuoi.add(nutSua);duoi.add(nutDuoi);
        p.add(duoi,BorderLayout.SOUTH);return p;
    }

    private JPanel formNhap() {
        JPanel p=new JPanel(new BorderLayout(0,15));p.setOpaque(false);
        p.add(tieuDe("Thông tin khuyến mãi","Các trường có lỗi được đánh dấu khi rời ô nhập hoặc chọn lịch."),BorderLayout.NORTH);
        JPanel card=the();card.setLayout(new BorderLayout());
        JPanel fields=new JPanel(new GridLayout(0,1,0,6));fields.setOpaque(false);
        themTruong(fields,"Mã khuyến mãi",ma);
        themTruong(fields,"Tên khuyến mãi",ten);
        themTruong(fields,"Loại giảm (phần trăm / số tiền)",loai);
        themTruong(fields,"Giá trị giảm (chỉ nhập số)",giaTri);
        themTruong(fields,"Ngày bắt đầu",batDau);
        themTruong(fields,"Ngày kết thúc",ketThuc);
        hoatDong.setBackground(Color.WHITE);fields.add(hoatDong);
        baoNhap.setForeground(DO);fields.add(baoNhap);
        JPanel wrap=new JPanel(new BorderLayout());wrap.setBackground(Color.WHITE);
        wrap.add(fields,BorderLayout.NORTH);card.add(new JScrollPane(wrap),BorderLayout.CENTER);
        p.add(card,BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);
        JButton quayLai=nut("Quay lại danh sách");quayLai.setBackground(new Color(0x64748B));
        quayLai.addActionListener(e->chuyenTrang.show(noiDung,"DANH_SACH"));
        nutLuu.addActionListener(e->luu());actions.add(quayLai);actions.add(nutLuu);
        p.add(actions,BorderLayout.SOUTH);return p;
    }

    private void moForm(KhuyenMai k){
        taoMoi=k==null;
        ma.setEditable(taoMoi);ma.setText(taoMoi?"":k.ma);
        ten.setText(taoMoi?"":k.ten);
        loai.setSelectedItem(taoMoi?"PHAN_TRAM":k.loai);
        giaTri.setText(taoMoi?"":k.giaTri.stripTrailingZeros().toPlainString());
        batDau.setNgay(taoMoi?LocalDate.now():k.batDau);
        ketThuc.setNgay(taoMoi?LocalDate.now().plusMonths(1):k.ketThuc);
        hoatDong.setSelected(taoMoi||k.bat);
        ma.setBorder(vienThuong);ten.setBorder(vienThuong);giaTri.setBorder(vienThuong);
        baoNhap.setText(" ");nutLuu.setEnabled(true);
        chuyenTrang.show(noiDung,"FORM");
    }

    /** Kiểm tra trên UI ngay khi rời ô, vẫn kiểm tra lại ở DAO trước khi ghi SQL. */
    private boolean kiemTraNhap(){
        if(!"FORM".equals(trangDangHien()))return true;
        boolean maDung=ma.getText().trim().matches("[A-Za-z0-9_-]{2,30}");
        boolean tenDung=!ten.getText().trim().isEmpty()&&ten.getText().trim().length()<=120;
        BigDecimal so=null;
        try {so=new BigDecimal(giaTri.getText().trim().replace(',','.'));}
        catch(NumberFormatException ignored){}
        boolean giaDung=so!=null&&so.signum()>0&&("SO_TIEN".equals(loai.getSelectedItem())
                ||so.compareTo(BigDecimal.valueOf(100))<=0);
        boolean ngayDung=batDau.getNgay()!=null&&ketThuc.getNgay()!=null
                &&!batDau.getNgay().isAfter(ketThuc.getNgay());
        ma.setBorder(maDung?vienThuong:BorderFactory.createLineBorder(DO));
        ten.setBorder(tenDung?vienThuong:BorderFactory.createLineBorder(DO));
        giaTri.setBorder(giaDung?vienThuong:BorderFactory.createLineBorder(DO));
        if(!maDung)baoNhap.setText("Mã phải có 2–30 ký tự: chữ, số, _ hoặc -.");
        else if(!tenDung)baoNhap.setText("Tên khuyến mãi không được trống (tối đa 120 ký tự).");
        else if(!giaDung)baoNhap.setText("Giá trị phải là số lớn hơn 0; phần trăm tối đa 100.");
        else if(!ngayDung)baoNhap.setText("Chọn ngày bắt đầu và kết thúc hợp lệ (bắt đầu ≤ kết thúc).");
        else baoNhap.setText(" ");
        return maDung&&tenDung&&giaDung&&ngayDung;
    }
    private String trangDangHien(){return noiDung.getComponent(1).isVisible()?"FORM":"DANH_SACH";}

    private void luu() {
        if(!kiemTraNhap()){thongBao(baoNhap.getText());return;}
        KhuyenMai k=new KhuyenMai(ma.getText().trim(),ten.getText().trim(),
                (String)loai.getSelectedItem(),new BigDecimal(giaTri.getText().trim().replace(',','.')),
                batDau.getNgay(),ketThuc.getNgay(),hoatDong.isSelected());
        boolean moi=taoMoi;nutLuu.setEnabled(false);
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground() throws Exception {dao.luuKhuyenMai(k,moi,actor);return null;}
            @Override protected void done(){
                nutLuu.setEnabled(true);
                try {get();thongBao("Đã lưu khuyến mãi " +k.ma);
                    chuyenTrang.show(noiDung,"DANH_SACH");taiDuLieu();}
                catch(Exception e){baoLoi(e);}
            }
        }.execute();
    }

    private void taiDuLieu(){
        new SwingWorker<List<KhuyenMai>,Void>(){
            @Override protected List<KhuyenMai> doInBackground() throws Exception {return dao.danhSachKhuyenMai(actor);}
            @Override protected void done(){
                try {ds=get();model.setRowCount(0);
                    for(KhuyenMai k:ds)model.addRow(new Object[]{k.ma,k.ten,k.loai,
                            k.giaTri.stripTrailingZeros().toPlainString(),
                            k.batDau.format(NGAY),k.ketThuc.format(NGAY),k.bat?"Hoạt động":"Ngừng"});
                }catch(Exception ex){baoLoi(ex);}
            }
        }.execute();
    }

    private static JButton nut(String s){JButton b=new JButton(s);b.setFont(new Font("Segoe UI",Font.BOLD,13));
        b.setBackground(XANH);b.setForeground(Color.WHITE);b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(11,18,11,18));return b;}
    private static JPanel the(){JPanel p=new JPanel();p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(18,18,18,18)));return p;}
    private static JPanel tieuDe(String ten,String moTa){JPanel p=new JPanel(new GridLayout(2,1,0,5));p.setOpaque(false);
        JLabel h=new JLabel(ten);h.setFont(new Font("Segoe UI",Font.BOLD,25));h.setForeground(CHU);
        JLabel d=new JLabel(moTa);d.setForeground(new Color(0x64748B));p.add(h);p.add(d);return p;}
    private void themTruong(JPanel p,String nhan,JComponent o){JLabel l=new JLabel(nhan);
        l.setFont(new Font("Segoe UI",Font.PLAIN,12));p.add(l);p.add(o);
        if(o instanceof JTextField)((JTextField)o).setBorder(vienThuong);}
    private void thongBao(String s){JOptionPane.showMessageDialog(this,s);}
    private void baoLoi(Exception e){Throwable cause=e.getCause()==null?e:e.getCause();
        JOptionPane.showMessageDialog(this,cause.getMessage(),"Không thể lưu/tải khuyến mãi",JOptionPane.ERROR_MESSAGE);}

    /** Lịch tháng riêng cho khuyến mãi, không áp dụng giới hạn ngày sinh của NgayChon. */
    private static final class ChonLich extends JPanel {
        private LocalDate ngay;private YearMonth thang=YearMonth.now();
        private Runnable thayDoi=()->{};
        private final JButton hienThi=new JButton("Chọn ngày");
        private final JPopupMenu lich=new JPopupMenu();
        ChonLich(){super(new BorderLayout());setOpaque(false);add(hienThi,BorderLayout.CENTER);
            hienThi.setBackground(Color.WHITE);hienThi.setFocusPainted(false);
            hienThi.addActionListener(e->{thang=YearMonth.from(ngay==null?LocalDate.now():ngay);veLich();lich.show(this,0,getHeight());});}
        void khiChon(Runnable r){thayDoi=r;}
        LocalDate getNgay(){return ngay;}
        void setNgay(LocalDate d){ngay=d;hienThi.setText(d==null?"Chọn ngày":d.format(NGAY));thayDoi.run();}
        private void veLich(){
            lich.removeAll();JPanel p=new JPanel(new BorderLayout(0,9));p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            JPanel dau=new JPanel(new BorderLayout(8,0));dau.setOpaque(false);
            JButton truoc=new JButton("‹"),sau=new JButton("›");
            JLabel tenThang=new JLabel("Tháng "+thang.getMonthValue()+" / "+thang.getYear(),SwingConstants.CENTER);
            dau.add(truoc,BorderLayout.WEST);dau.add(tenThang,BorderLayout.CENTER);dau.add(sau,BorderLayout.EAST);
            truoc.addActionListener(e->{thang=thang.minusMonths(1);veLich();});
            sau.addActionListener(e->{thang=thang.plusMonths(1);veLich();});p.add(dau,BorderLayout.NORTH);
            JPanel luoi=new JPanel(new GridLayout(0,7,3,3));luoi.setBackground(Color.WHITE);
            for(String thu:new String[]{"T2","T3","T4","T5","T6","T7","CN"})
                luoi.add(new JLabel(thu,SwingConstants.CENTER));
            for(int i=1;i<thang.atDay(1).getDayOfWeek().getValue();i++)luoi.add(new JLabel());
            for(int i=1;i<=thang.lengthOfMonth();i++){
                LocalDate d=thang.atDay(i);JButton b=new JButton(""+i);b.setMargin(new Insets(3,3,3,3));
                b.setBackground(d.equals(ngay)?new Color(0xBAE6FD):Color.WHITE);
                b.addActionListener(e->{setNgay(d);lich.setVisible(false);});luoi.add(b);
            }
            p.add(luoi,BorderLayout.CENTER);lich.add(p);lich.pack();
        }
    }
}
