package UI.LeTan;

import DAO.ThanhToanKhuyenMaiDAO;
import DAO.ThanhToanKhuyenMaiDAO.ChiTiet;
import DAO.ThanhToanKhuyenMaiDAO.DuToan;
import DAO.ThanhToanKhuyenMaiDAO.HoaDon;
import DAO.ThanhToanKhuyenMaiDAO.KetQua;
import DAO.ThanhToanKhuyenMaiDAO.Phieu;
import UI.Chung.MaQRChuyenKhoan;
import DAO.ThanhToanKhuyenMaiDAO.NganHang;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.Graphics2D;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/** Một vùng nội dung, hai tab: chờ thanh toán và lịch sử hóa đơn. */
public final class ThanhToanPanel extends JPanel {
    private static final Color XANH=new Color(0x0284C7), NEN=new Color(0xF8FAFC);
    private static final Color VIEN=new Color(0xE2E8F0), CHU=new Color(0x0F172A);
    private static final NumberFormat TIEN=NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final ThanhToanKhuyenMaiDAO dao;
    private final int actor;
    private final DefaultTableModel modelCho=model("Phiếu khám","Bệnh nhân","Tổng dịch vụ","Trạng thái");
    private final DefaultTableModel modelDa=model("Mã HĐ","Bệnh nhân","Ngày lập","Tiền gốc","Giảm","Đã thu","Mã KM","Thanh toán");
    private final JTable bangCho=new JTable(modelCho),bangDa=new JTable(modelDa);
    private final JButton nutThanhToan=nut("Thanh toán"),nutHuy=nut("Hủy hóa đơn đang chờ");
    private final JButton nutXem=nut("Xem lại hóa đơn"),nutXuat=nut("Xuất hóa đơn PDF");
    private final JTabbedPane cacTab=new JTabbedPane();
    private List<Phieu> danhSachCho=new ArrayList<>();
    private List<HoaDon> danhSachDa=new ArrayList<>();
    private final JLabel lblCho = new JLabel("0 phiếu chờ"), lblDaThu = new JLabel("0 hóa đơn đã thu");
    private int lanTai=0;

    public ThanhToanPanel(ThanhToanKhuyenMaiDAO dao,int maTK){
        this.dao=dao;actor=maTK;
        setLayout(new BorderLayout(0,16));setBackground(NEN);
        setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        JPanel tren=new JPanel(new BorderLayout(12,0));tren.setOpaque(false);
        JPanel head=new JPanel(new GridLayout(2,1,0,5));head.setOpaque(false);
        JLabel tieuDe=new JLabel("Thanh toán & hóa đơn");
        tieuDe.setFont(new Font("Segoe UI",Font.BOLD,25));tieuDe.setForeground(CHU);
        JLabel phu=new JLabel("Thu tiền theo phiếu khám, quản lý hóa đơn chờ và tra cứu lịch sử đã thu");
        phu.setFont(new Font("Segoe UI",Font.PLAIN,13));
        phu.setForeground(new Color(0x64748B));head.add(tieuDe);head.add(phu);
        tren.add(head,BorderLayout.WEST);
        JButton taiLai=nut("Tải lại dữ liệu");taiLai.addActionListener(e->taiDuLieu());
        JPanel benPhai=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));benPhai.setOpaque(false);
        for(JLabel nhan:new JLabel[]{lblCho,lblDaThu}){
            nhan.setFont(new Font("Segoe UI",Font.BOLD,12));nhan.setForeground(XANH);
            nhan.setOpaque(true);nhan.setBackground(new Color(0xE0F2FE));
            nhan.setBorder(BorderFactory.createEmptyBorder(9,12,9,12));benPhai.add(nhan);
        }
        benPhai.add(taiLai);tren.add(benPhai,BorderLayout.EAST);
        JPanel tieuDeNgoai=new JPanel(new BorderLayout());tieuDeNgoai.setBackground(Color.WHITE);
        tieuDeNgoai.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(17,20,17,20)));
        tieuDeNgoai.add(tren);add(tieuDeNgoai,BorderLayout.NORTH);

        dinhDangBang(bangCho);dinhDangBang(bangDa);
        bangCho.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())capNhatNut();});
        bangDa.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())capNhatNut();});
        cacTab.addTab("Chờ thanh toán",the(new JScrollPane(bangCho)));
        cacTab.addTab("Lịch sử hóa đơn",the(new JScrollPane(bangDa)));
        cacTab.setFont(new Font("Segoe UI",Font.BOLD,13));
        cacTab.setBackground(Color.WHITE);cacTab.setForeground(CHU);
        cacTab.addChangeListener(e->capNhatNut());add(cacTab,BorderLayout.CENTER);
        JPanel thaoTac=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,12));
        thaoTac.setBackground(Color.WHITE);
        thaoTac.setBorder(BorderFactory.createMatteBorder(1,0,0,0,VIEN));
        nutHuy.setBackground(new Color(0xEF4444));
        thaoTac.add(nutThanhToan);thaoTac.add(nutHuy);thaoTac.add(nutXem);thaoTac.add(nutXuat);
        add(thaoTac,BorderLayout.SOUTH);
        nutThanhToan.addActionListener(e->moThanhToan());
        nutHuy.addActionListener(e->huyHoaDon());
        nutXem.addActionListener(e->xemHoaDon());
        nutXuat.addActionListener(e->xuatHoaDon());
        capNhatNut();taiDuLieu();
    }
    private static DefaultTableModel model(String... cot){return new DefaultTableModel(cot,0){
        @Override public boolean isCellEditable(int r,int c){return false;}
    };}
    private static JButton nut(String s){JButton b=new JButton(s);b.setFont(new Font("Segoe UI",Font.BOLD,12));
        b.setBackground(XANH);b.setForeground(Color.WHITE);b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));return b;}
    private static JPanel the(JComponent content){JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(14,14,14,14)));p.add(content);return p;}
    private static void dinhDangBang(JTable t){
        t.setFont(new Font("Segoe UI",Font.PLAIN,13));t.setForeground(CHU);
        t.setRowHeight(46);t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);t.setShowHorizontalLines(true);
        t.setGridColor(VIEN);t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(new Color(0xDBEAFE));t.setSelectionForeground(CHU);
        t.setAutoCreateRowSorter(true);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        t.getTableHeader().setBackground(new Color(0xE0F2FE));
        t.getTableHeader().setForeground(CHU);
        t.getTableHeader().setPreferredSize(new Dimension(0,42));
        t.getTableHeader().setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable bang,Object giaTri,
                    boolean chon,boolean focus,int hang,int cot){
                super.getTableCellRendererComponent(bang,giaTri,chon,focus,hang,cot);
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                setBackground(chon?new Color(0xDBEAFE):hang%2==0?Color.WHITE:new Color(0xF8FAFC));
                setForeground(CHU);setHorizontalAlignment(SwingConstants.LEFT);
                String tenCot=bang.getColumnName(cot);
                if(tenCot.equals("Tổng dịch vụ")||tenCot.equals("Tiền gốc")||tenCot.equals("Giảm")
                        ||tenCot.equals("Đã thu"))setHorizontalAlignment(SwingConstants.RIGHT);
                if(tenCot.equals("Trạng thái")){
                    setFont(getFont().deriveFont(Font.BOLD));
                    setForeground("Chờ thanh toán".equals(giaTri)?new Color(0xB45309):new Color(0x64748B));
                }
                return this;
            }
        });
    }
    private static String vnd(BigDecimal n){return TIEN.format(n==null?BigDecimal.ZERO:n)+" đ";}
    private Phieu phieuChon(){int r=bangCho.getSelectedRow();
        int i=r<0?-1:bangCho.convertRowIndexToModel(r);
        return i>=0&&i<danhSachCho.size()?danhSachCho.get(i):null;
    }
    private HoaDon hoaDonChon(){int r=bangDa.getSelectedRow();
        int i=r<0?-1:bangDa.convertRowIndexToModel(r);
        return i>=0&&i<danhSachDa.size()?danhSachDa.get(i):null;
    }
    private void capNhatNut(){
        boolean dangCho=cacTab.getSelectedIndex()==0;
        nutThanhToan.setVisible(dangCho);nutHuy.setVisible(dangCho);
        nutXem.setVisible(!dangCho);nutXuat.setVisible(!dangCho);
        nutThanhToan.setEnabled(dangCho&&phieuChon()!=null);
        nutHuy.setEnabled(dangCho&&phieuChon()!=null&&phieuChon().maHD>0);
        nutXem.setEnabled(!dangCho&&hoaDonChon()!=null);
        nutXuat.setEnabled(!dangCho&&hoaDonChon()!=null);
    }
    private void taiDuLieu(){
        int phien=++lanTai;
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.phieuChoThu(actor),dao.hoaDonGanDay(actor)};
            }
            @SuppressWarnings("unchecked") @Override protected void done(){
                if(phien!=lanTai)return;
                try {
                    Object[] k=get();danhSachCho=(List<Phieu>)k[0];danhSachDa=(List<HoaDon>)k[1];
                    modelCho.setRowCount(0);modelDa.setRowCount(0);
                    for(Phieu p:danhSachCho)modelCho.addRow(new Object[]{"PK"+p.maPK,
                            p.benhNhan,vnd(p.tongGoc),"Chờ thanh toán"});
                    lblCho.setText(danhSachCho.size()+" phiếu chờ");
                    lblDaThu.setText(danhSachDa.size()+" hóa đơn đã thu");
                    for(HoaDon h:danhSachDa)modelDa.addRow(new Object[]{"HD"+h.maHD,h.benhNhan,h.ngayLap,
                            vnd(h.tienGoc),vnd(h.tienGiam),vnd(h.phaiThu),h.maKM,h.phuongThuc});
                    bangCho.clearSelection();bangDa.clearSelection();capNhatNut();
                } catch(Exception ex){baoLoi(ex);}
            }
        }.execute();
    }

    private void moThanhToan(){
        Phieu p=phieuChon();if(p==null)return;
        nutThanhToan.setEnabled(false);
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.xemDuToan(p.maPK,actor),dao.chiTietDieuTri(p.maPK,actor)};
            }
            @SuppressWarnings("unchecked") @Override protected void done(){
                capNhatNut();
                try {Object[] data=get();hienThiThanhToan((DuToan)data[0],(List<ChiTiet>)data[1]);}
                catch(Exception ex){baoLoi(ex);taiDuLieu();}
            }
        }.execute();
    }

    /** Mở giao diện thu tiền riêng với chi tiết, nhập tiền mặt hoặc QR demo. */
    private void hienThiThanhToan(DuToan d,List<ChiTiet> chiTiet){
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),"Thanh toán PK"+d.phieu.maPK,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(750,640);dialog.setMinimumSize(new Dimension(650,580));
        dialog.setLocationRelativeTo(this);
        JPanel khung=new JPanel(new BorderLayout(0,13));khung.setBackground(NEN);
        khung.setBorder(BorderFactory.createEmptyBorder(16,18,16,18));
        JLabel t=new JLabel("Thông tin thanh toán - "+d.phieu.benhNhan);
        t.setFont(new Font("Segoe UI",Font.BOLD,19));t.setForeground(CHU);khung.add(t,BorderLayout.NORTH);
        DefaultTableModel c=model("Dịch vụ","Số lượng","Đơn giá","Thành tiền");
        for(ChiTiet x:chiTiet)c.addRow(new Object[]{x.dichVu,x.soLuong,vnd(x.donGia),vnd(x.thanhTien)});
        JTable bang=new JTable(c);dinhDangBang(bang);
        JPanel gia=the(new JScrollPane(bang));
        JPanel tong=new JPanel(new GridLayout(3,2,5,5));tong.setBackground(Color.WHITE);
        tong.add(new JLabel("Tiền gốc"));tong.add(new JLabel(vnd(d.phieu.tongGoc),SwingConstants.RIGHT));
        tong.add(new JLabel("Mã ưu đãi / giảm"));tong.add(new JLabel(
                (d.khuyenMai==null?"Không":d.khuyenMai.ma)+" / -"+vnd(d.giam),SwingConstants.RIGHT));
        JLabel soThu=new JLabel(vnd(d.phaiThu),SwingConstants.RIGHT);
        soThu.setFont(new Font("Segoe UI",Font.BOLD,21));soThu.setForeground(XANH);
        tong.add(new JLabel("PHẢI THU"));tong.add(soThu);
        gia.add(tong,BorderLayout.SOUTH);khung.add(gia,BorderLayout.CENTER);

        JPanel duoi=new JPanel(new BorderLayout(0,9));duoi.setOpaque(false);
        JPanel luu=new JPanel(new GridLayout(0,2,8,8));luu.setOpaque(false);
        JComboBox<String> phuongThuc=new JComboBox<>(new String[]{"Tiền mặt","Chuyển khoản"});
        JTextField tienKhach=new JTextField();
        JLabel tienThua=new JLabel("Tiền thừa: 0 đ");tienThua.setForeground(CHU);
        luu.add(new JLabel("Phương thức thanh toán"));luu.add(phuongThuc);
        luu.add(new JLabel("Khách đưa (VND)"));luu.add(tienKhach);
        luu.add(new JLabel("Tiền thừa"));luu.add(tienThua);
        duoi.add(luu,BorderLayout.NORTH);
        JLabel ghiChu=new JLabel("Chỉ xác nhận sau khi phòng khám đã nhận đủ tiền.");
        ghiChu.setForeground(new Color(0x64748B));
        duoi.add(ghiChu,BorderLayout.CENTER);
        JButton xacNhan=nut("Xác nhận đã thu tiền mặt");
        JButton quayLai=nut("Đóng");quayLai.setBackground(new Color(0x64748B));
        JPanel cacNut=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));cacNut.setOpaque(false);
        cacNut.add(quayLai);cacNut.add(xacNhan);duoi.add(cacNut,BorderLayout.SOUTH);
        khung.add(duoi,BorderLayout.SOUTH);
        dialog.setContentPane(khung);

        ((AbstractDocument)tienKhach.getDocument()).setDocumentFilter(new DocumentFilter(){
            @Override public void replace(FilterBypass fb,int offset,int length,String text,AttributeSet a)
                    throws BadLocationException {
                String k=new StringBuilder(fb.getDocument().getText(0,fb.getDocument().getLength()))
                        .replace(offset,offset+length,text==null?"":text).toString();
                if(k.matches("[0-9]*"))super.replace(fb,offset,length,text,a);
            }
            @Override public void insertString(FilterBypass fb,int offset,String str,AttributeSet a)
                    throws BadLocationException {replace(fb,offset,0,str,a);}
        });
        tienKhach.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            private void doi(){try{BigDecimal n=new BigDecimal(tienKhach.getText());
                tienThua.setText(n.compareTo(d.phaiThu)<0?"Chưa đủ tiền":"Tiền thừa: "+vnd(n.subtract(d.phaiThu)));}
                catch(NumberFormatException ex){tienThua.setText("Nhập số tiền khách đưa");}}
            public void insertUpdate(javax.swing.event.DocumentEvent e){doi();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){doi();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){doi();}
        });
        quayLai.addActionListener(e->dialog.dispose());
        phuongThuc.addActionListener(e->{
            boolean chuyen="Chuyển khoản".equals(phuongThuc.getSelectedItem());
            tienKhach.setEnabled(!chuyen);tienThua.setEnabled(!chuyen);
            xacNhan.setEnabled(!chuyen);
            if(chuyen)moMaQR(dialog,d,phuongThuc);
        });
        xacNhan.addActionListener(e->{
            BigDecimal nhan;
            try{nhan=new BigDecimal(tienKhach.getText().trim());}
            catch(NumberFormatException ex){thongBao("Tiền khách đưa phải là số nguyên VND.");return;}
            if(nhan.compareTo(d.phaiThu)<0){thongBao("Tiền khách đưa phải đủ "+vnd(d.phaiThu));return;}
            if(JOptionPane.showConfirmDialog(dialog,"Đã nhận "+vnd(nhan)+" bằng tiền mặt?",
                    "Xác nhận thu tiền",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            xacNhan.setEnabled(false);
            luuThanhToan(d,"Tiền mặt",nhan,null,xacNhan,dialog,null);
        });
        dialog.setVisible(true);
    }

    /** Hiển thị VietQR thật; nút thành công chỉ xác nhận thủ công sau khi kiểm tra ngân hàng. */
    private void moMaQR(JDialog thanhToan,DuToan duToan,JComboBox<String> phuongThuc){
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.xemDuToan(duToan.phieu.maPK,actor),dao.nganHang(actor)};
            }
            @Override protected void done(){
                try{
                    if(!thanhToan.isDisplayable() || !"Chuyển khoản".equals(phuongThuc.getSelectedItem()))return;
                    Object[] k=get();DuToan moi=(DuToan)k[0];NganHang nh=(NganHang)k[1];
                    if(moi.phaiThu.compareTo(duToan.phaiThu)!=0)
                        throw new IllegalArgumentException("Số tiền đã thay đổi. Hãy đóng và tải lại phiếu khám.");
                    if(moi.phaiThu.signum()<=0)
                        throw new IllegalArgumentException("Hóa đơn 0 đồng không cần tạo mã QR ngân hàng.");
                    String noiDung=MaQRChuyenKhoan.noiDung(nh,moi.phaiThu,moi.phieu.maPK,moi.phieu.benhNhan);
                    BufferedImage anh=MaQRChuyenKhoan.anh(noiDung,280);
                    JDialog qr=new JDialog(thanhToan,"Quét mã QR thanh toán",Dialog.ModalityType.APPLICATION_MODAL);
                    JPanel vung=new JPanel(new BorderLayout(0,14));vung.setBackground(Color.WHITE);
                    vung.setBorder(BorderFactory.createEmptyBorder(18,24,18,24));
                    JLabel t=new JLabel("QUÉT MÃ QR ĐỂ THANH TOÁN",SwingConstants.CENTER);
                    t.setForeground(XANH);t.setFont(new Font("Segoe UI",Font.BOLD,19));
                    vung.add(t,BorderLayout.NORTH);
                    JPanel noiDungQR=new JPanel(new BorderLayout(0,10));noiDungQR.setBackground(Color.WHITE);
                    noiDungQR.add(new JLabel(new ImageIcon(anh),SwingConstants.CENTER),BorderLayout.CENTER);
                    JPanel chiTiet=new JPanel(new GridLayout(0,1,0,6));chiTiet.setOpaque(false);
                    for(String text:new String[]{"Bệnh nhân: "+moi.phieu.benhNhan,
                            "Phiếu khám: PK"+moi.phieu.maPK,"Phòng khám: MEDIDENT",
                            "Đơn vị nhận tiền: "+nh.chuTaiKhoan,
                            "Ngân hàng (BIN): "+nh.bin+"    Số tài khoản: "+nh.soTaiKhoan,
                            "Số tiền phải thu: "+vnd(moi.phaiThu)}){
                        JLabel l=new JLabel(text,SwingConstants.CENTER);
                        l.setForeground(CHU);l.setFont(new Font("Segoe UI",Font.PLAIN,13));chiTiet.add(l);
                    }
                    noiDungQR.add(chiTiet,BorderLayout.SOUTH);vung.add(noiDungQR,BorderLayout.CENTER);
                    JPanel cuoi=new JPanel(new BorderLayout(0,8));cuoi.setOpaque(false);
                    JLabel canhBao=new JLabel("Chỉ xác nhận sau khi đã kiểm tra tiền vào tài khoản phòng khám.");
                    canhBao.setForeground(new Color(0xB45309));cuoi.add(canhBao,BorderLayout.NORTH);
                    JPanel dongNhap=new JPanel(new BorderLayout(8,0));dongNhap.setOpaque(false);
                    dongNhap.add(new JLabel("Mã giao dịch ngân hàng:"),BorderLayout.WEST);
                    JTextField maGD=new JTextField();dongNhap.add(maGD,BorderLayout.CENTER);
                    cuoi.add(dongNhap,BorderLayout.CENTER);
                    JPanel hanhDong=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));hanhDong.setOpaque(false);
                    JButton in=nut("In mã QR"),huy=nut("Hủy"),thanhCong=nut("Xác nhận thành công");
                    huy.setBackground(new Color(0x64748B));
                    hanhDong.add(in);hanhDong.add(huy);hanhDong.add(thanhCong);
                    in.addActionListener(e->inMaQR(anh,moi,nh));
                    huy.addActionListener(e->{qr.dispose();phuongThuc.setSelectedItem("Tiền mặt");});
                    thanhCong.addActionListener(e->{
                        String ma=maGD.getText().trim();
                        if(ma.isEmpty()||ma.length()>100){
                            JOptionPane.showMessageDialog(qr,"Nhập mã giao dịch thực tế (tối đa 100 ký tự).");return;
                        }
                        thanhCong.setEnabled(false);
                        luuThanhToan(moi,"Chuyển khoản",null,ma,thanhCong,qr,thanhToan);
                    });
                    cuoi.add(hanhDong,BorderLayout.SOUTH);vung.add(cuoi,BorderLayout.SOUTH);
                    qr.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    qr.addWindowListener(new java.awt.event.WindowAdapter(){
                        @Override public void windowClosing(java.awt.event.WindowEvent e){
                            qr.dispose();phuongThuc.setSelectedItem("Tiền mặt");
                        }
                    });
                    qr.setContentPane(vung);qr.setSize(570,720);qr.setMinimumSize(new Dimension(530,660));
                    qr.setLocationRelativeTo(thanhToan);qr.setVisible(true);
                }catch(Exception ex){
                    phuongThuc.setSelectedItem("Tiền mặt");baoLoi(ex);
                }
            }
        }.execute();
    }

    private void inMaQR(BufferedImage anh,DuToan d,NganHang nh){
        PrinterJob job=PrinterJob.getPrinterJob();job.setJobName("MediDent QR PK"+d.phieu.maPK);
        job.setPrintable((graphics,page,index)->{
            if(index>0)return Printable.NO_SUCH_PAGE;
            Graphics2D g=(Graphics2D)graphics.create();
            try{
                g.translate(page.getImageableX(),page.getImageableY());
                double scale=Math.min(page.getImageableWidth()/390,page.getImageableHeight()/490);
                g.scale(scale,scale);g.setColor(Color.BLACK);
                g.setFont(new Font("SansSerif",Font.BOLD,17));g.drawString("MEDIDENT - THANH TOAN",34,30);
                g.drawImage(anh,55,45,280,280,null);
                g.setFont(new Font("SansSerif",Font.PLAIN,12));
                g.drawString("Benh nhan: "+d.phieu.benhNhan,15,351);
                g.drawString("Ma phieu: PK"+d.phieu.maPK,15,374);
                g.drawString("Chu TK: "+nh.chuTaiKhoan,15,397);
                g.drawString("BIN: "+nh.bin+" | STK: "+nh.soTaiKhoan,15,420);
                g.drawString("So tien: "+vnd(d.phaiThu),15,443);
                g.drawString("Vui long kiem tra chu tai khoan truoc khi quet.",15,472);
                return Printable.PAGE_EXISTS;
            }finally{g.dispose();}
        });
        if(job.printDialog())try{job.print();}
        catch(java.awt.print.PrinterException ex){baoLoi(ex);}
    }

    private void luuThanhToan(DuToan d,String phuongThuc,BigDecimal tienNhan,String maGiaoDich,
            JButton nut,JDialog cuaSo,JDialog cuaSoCha){
        new SwingWorker<KetQua,Void>(){
            @Override protected KetQua doInBackground() throws Exception {
                return dao.thuTien(d.phieu.maPK,actor,phuongThuc,tienNhan,maGiaoDich,true,null,d.phaiThu);
            }
            @Override protected void done(){
                try{
                    KetQua k=get();cuaSo.dispose();if(cuaSoCha!=null)cuaSoCha.dispose();taiDuLieu();
                    int chon=JOptionPane.showConfirmDialog(ThanhToanPanel.this,
                            "Bạn có muốn xuất hóa đơn HD"+k.maHD+" ra PDF?",
                            "Xuất hóa đơn",JOptionPane.YES_NO_OPTION);
                    if(chon==JOptionPane.YES_OPTION)xuatHoaDon(k.maHD);
                }catch(Exception ex){nut.setEnabled(true);baoLoi(ex);}
            }
        }.execute();
    }

    private void huyHoaDon(){
        Phieu p=phieuChon();if(p==null)return;
        if(p.maHD<=0)return; // Chỉ hủy hóa đơn đã lập và đang chờ thu.
        if(JOptionPane.showConfirmDialog(this,"Hủy hóa đơn HD"+p.maHD+" chưa thanh toán?",
                "Xác nhận hủy",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        nutHuy.setEnabled(false);
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground() throws Exception {dao.huyHoaDonCho(p.maHD,actor);return null;}
            @Override protected void done(){try{get();thongBao("Đã hủy hóa đơn HD"+p.maHD);}
                catch(Exception ex){baoLoi(ex);}finally{taiDuLieu();}}
        }.execute();
    }

    private void xemHoaDon(){
        HoaDon h=hoaDonChon();if(h==null)return;
        nutXem.setEnabled(false);
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                List<ChiTiet> ds;
                try{ds=dao.chiTietHoaDon(h.maHD,actor);}
                catch(java.sql.SQLException ex){
                    if(ex.getErrorCode()!=208)throw ex;
                    ds=new ArrayList<>(); // DB bản cũ chưa có bảng chi tiết hóa đơn.
                }
                boolean duLieuCu=ds.isEmpty();
                if(duLieuCu)ds=dao.chiTietDieuTri(h.maPK,actor);
                return new Object[]{ds,duLieuCu};
            }
            @SuppressWarnings("unchecked") @Override protected void done(){
                capNhatNut();
                try{
                    Object[] k=get();List<ChiTiet> ds=(List<ChiTiet>)k[0];
                    DefaultTableModel m=model("Dịch vụ","Số lượng","Đơn giá","Thành tiền");
                    for(ChiTiet d:ds)m.addRow(new Object[]{d.dichVu,d.soLuong,vnd(d.donGia),vnd(d.thanhTien)});
                    JTable b=new JTable(m);dinhDangBang(b);
                    JPanel p=new JPanel(new BorderLayout(0,12));p.setBackground(Color.WHITE);
                    p.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
                    JLabel l=new JLabel("HÓA ĐƠN HD"+h.maHD+"  |  "+h.benhNhan+"  |  "+h.ngayLap);
                    l.setFont(new Font("Segoe UI",Font.BOLD,16));l.setForeground(XANH);
                    p.add(l,BorderLayout.NORTH);
                    JScrollPane cuon=new JScrollPane(b);cuon.setPreferredSize(new Dimension(740,260));
                    p.add(cuon,BorderLayout.CENTER);
                    JPanel duoi=new JPanel(new GridLayout(0,1,0,7));duoi.setOpaque(false);
                    duoi.add(new JLabel("Tiền gốc: "+vnd(h.tienGoc)+"    |    Giảm: "+vnd(h.tienGiam)
                            +"    |    Đã thu: "+vnd(h.phaiThu)));
                    duoi.add(new JLabel("Phương thức: "+h.phuongThuc+"    |    Khuyến mãi: "
                            +(h.maKM==null||h.maKM.isBlank()?"Không có":h.maKM)));
                    if((boolean)k[1]){
                        JLabel luuY=new JLabel("Hóa đơn cũ không có bản ghi chi tiết; đang xem dịch vụ theo phiếu khám.");
                        luuY.setForeground(new Color(0xB45309));duoi.add(luuY);
                    }
                    p.add(duoi,BorderLayout.SOUTH);
                    JOptionPane.showMessageDialog(ThanhToanPanel.this,p,"Xem lại hóa đơn",JOptionPane.PLAIN_MESSAGE);
                }catch(Exception ex){baoLoi(ex);}
            }
        }.execute();
    }
    private void xuatHoaDon(){HoaDon h=hoaDonChon();if(h!=null)xuatHoaDon(h.maHD);}
    private void xuatHoaDon(int maHD){
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                HoaDon hd=null;
                for(HoaDon h:dao.hoaDonGanDay(actor))if(h.maHD==maHD){hd=h;break;}
                if(hd==null)throw new IllegalArgumentException("Hóa đơn đã thanh toán không còn trong danh sách gần đây.");
                List<ChiTiet> ds;
                try{ds=dao.chiTietHoaDon(maHD,actor);}
                catch(java.sql.SQLException ex){
                    if(ex.getErrorCode()!=208)throw ex;
                    ds=new ArrayList<>();
                }
                if(ds.isEmpty())ds=dao.chiTietDieuTri(hd.maPK,actor);
                return new Object[]{hd,ds};
            }
            @SuppressWarnings("unchecked") @Override protected void done(){
                try {Object[] r=get();XuatHoaDonPDF.xuat(ThanhToanPanel.this,
                        (HoaDon)r[0],(List<ChiTiet>)r[1]);}
                catch(Exception ex){baoLoi(ex);}
            }
        }.execute();
    }
    private void thongBao(String s){JOptionPane.showMessageDialog(this,s);}
    private void baoLoi(Exception ex){Throwable e=ex.getCause()==null?ex:ex.getCause();
        JOptionPane.showMessageDialog(this,e.getMessage(),"Không thể thực hiện",JOptionPane.ERROR_MESSAGE);}
}
