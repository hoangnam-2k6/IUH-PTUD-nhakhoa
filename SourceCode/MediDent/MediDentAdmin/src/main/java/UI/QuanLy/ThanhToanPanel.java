package UI.QuanLy;

import DAO.ThanhToanKhuyenMaiDAO;
import DAO.ThanhToanKhuyenMaiDAO.ChiTiet;
import DAO.ThanhToanKhuyenMaiDAO.DuToan;
import DAO.ThanhToanKhuyenMaiDAO.HoaDon;
import DAO.ThanhToanKhuyenMaiDAO.KetQua;
import DAO.ThanhToanKhuyenMaiDAO.Phieu;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
    private final DefaultTableModel modelCho=model("Hóa đơn","Phiếu khám","Bệnh nhân","Tổng dịch vụ","Trạng thái");
    private final DefaultTableModel modelDa=model("Mã HĐ","Bệnh nhân","Ngày lập","Tiền gốc","Giảm","Đã thu","Mã KM","Thanh toán");
    private final JTable bangCho=new JTable(modelCho),bangDa=new JTable(modelDa);
    private final JButton nutThanhToan=nut("Thanh toán"),nutHuy=nut("Hủy hóa đơn đang chờ");
    private final JButton nutXem=nut("Xem lại hóa đơn"),nutXuat=nut("Xuất hóa đơn PDF");
    private final JTabbedPane cacTab=new JTabbedPane();
    private List<Phieu> danhSachCho=new ArrayList<>();
    private List<HoaDon> danhSachDa=new ArrayList<>();
    private int lanTai=0;

    public ThanhToanPanel(ThanhToanKhuyenMaiDAO dao,int maTK){
        this.dao=dao;actor=maTK;
        setLayout(new BorderLayout(0,16));setBackground(NEN);
        setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        JPanel tren=new JPanel(new BorderLayout(12,0));tren.setOpaque(false);
        JPanel head=new JPanel(new GridLayout(2,1,0,5));head.setOpaque(false);
        JLabel tieuDe=new JLabel("Thanh toán & hóa đơn");
        tieuDe.setFont(new Font("Segoe UI",Font.BOLD,25));tieuDe.setForeground(CHU);
        JLabel phu=new JLabel("Chọn hóa đơn cần thu hoặc xem và xuất hóa đơn đã thanh toán");
        phu.setForeground(new Color(0x64748B));head.add(tieuDe);head.add(phu);
        tren.add(head,BorderLayout.WEST);
        JButton taiLai=nut("Tải lại");taiLai.addActionListener(e->taiDuLieu());
        tren.add(taiLai,BorderLayout.EAST);add(tren,BorderLayout.NORTH);

        dinhDangBang(bangCho);dinhDangBang(bangDa);
        bangCho.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())capNhatNut();});
        bangDa.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())capNhatNut();});
        cacTab.addTab("Chờ thanh toán",the(new JScrollPane(bangCho)));
        cacTab.addTab("Lịch sử hóa đơn",the(new JScrollPane(bangDa)));
        cacTab.addChangeListener(e->capNhatNut());add(cacTab,BorderLayout.CENTER);
        JPanel thaoTac=new JPanel(new FlowLayout(FlowLayout.RIGHT,9,0));thaoTac.setOpaque(false);
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
        b.setBorder(BorderFactory.createEmptyBorder(11,16,11,16));return b;}
    private static JPanel the(JComponent content){JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(14,14,14,14)));p.add(content);return p;}
    private static void dinhDangBang(JTable t){t.setRowHeight(38);t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));}
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
                    for(Phieu p:danhSachCho)modelCho.addRow(new Object[]{p.maHD>0?"HD"+p.maHD:"Chưa lập",
                            "PK"+p.maPK,p.benhNhan,vnd(p.tongGoc),p.maHD>0?"Chưa thanh toán":"Chờ lập hóa đơn"});
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
        dialog.setSize(670,700);dialog.setMinimumSize(new Dimension(600,600));
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
        JPanel qr=new JPanel(new BorderLayout(0,5));qr.setOpaque(false);
        JLabel anhQR=new JLabel("",SwingConstants.CENTER);
        JLabel canhBao=new JLabel("QR chỉ chứa thông tin hóa đơn, không phải mã chuyển tiền ngân hàng.",SwingConstants.CENTER);
        canhBao.setForeground(new Color(0xB45309));
        qr.add(anhQR,BorderLayout.CENTER);qr.add(canhBao,BorderLayout.SOUTH);
        try {
            String noiDung="MEDIDENT\nKhách hàng: "+d.phieu.benhNhan+"\nPhiếu khám: PK"+d.phieu.maPK
                    +"\nSố tiền: "+d.phaiThu.toPlainString()+" VND";
            BitMatrix qrCode=new QRCodeWriter().encode(noiDung,BarcodeFormat.QR_CODE,220,220,
                    java.util.Map.of(com.google.zxing.EncodeHintType.CHARACTER_SET,"UTF-8"));
            BufferedImage anh=new BufferedImage(qrCode.getWidth(),qrCode.getHeight(),BufferedImage.TYPE_INT_RGB);
            for(int y=0;y<qrCode.getHeight();y++)for(int x=0;x<qrCode.getWidth();x++)
                anh.setRGB(x,y,qrCode.get(x,y)?Color.BLACK.getRGB():Color.WHITE.getRGB());
            anhQR.setIcon(new ImageIcon(anh));
        } catch(Exception ex){anhQR.setText("Không tạo được QR: "+ex.getMessage());}
        duoi.add(qr,BorderLayout.CENTER);
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
        Runnable doiPhuongThuc=()->{
            boolean chuyen="Chuyển khoản".equals(phuongThuc.getSelectedItem());
            qr.setVisible(chuyen);tienKhach.setEnabled(!chuyen);tienThua.setEnabled(!chuyen);
            xacNhan.setText(chuyen?"Mô phỏng chuyển khoản thành công":"Xác nhận đã thu tiền mặt");
            dialog.revalidate();dialog.repaint();
        };
        phuongThuc.addActionListener(e->doiPhuongThuc.run());doiPhuongThuc.run();
        quayLai.addActionListener(e->dialog.dispose());
        xacNhan.addActionListener(e->{
            boolean chuyen="Chuyển khoản".equals(phuongThuc.getSelectedItem());
            BigDecimal nhan=null;
            if(!chuyen){
                try{nhan=new BigDecimal(tienKhach.getText().trim());}
                catch(NumberFormatException ex){thongBao("Tiền khách đưa phải là số nguyên VND.");return;}
                if(nhan.compareTo(d.phaiThu)<0){thongBao("Tiền khách đưa phải đủ "+vnd(d.phaiThu));return;}
            }
            String maDemo=chuyen?"DEMO-PK"+d.phieu.maPK+"-"+System.currentTimeMillis():"";
            if(JOptionPane.showConfirmDialog(dialog,
                    chuyen?"Xác nhận MÔ PHỎNG chuyển khoản thành công? Không xác minh ngân hàng."
                          :"Đã thực sự nhận "+vnd(nhan)+" bằng tiền mặt?",
                    "Xác nhận thanh toán",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            xacNhan.setEnabled(false);
            BigDecimal soNhan=nhan;
            new SwingWorker<KetQua,Void>(){
                @Override protected KetQua doInBackground() throws Exception {
                    return dao.thuTien(d.phieu.maPK,actor,chuyen?"Chuyển khoản":"Tiền mặt",soNhan,maDemo,true);
                }
                @Override protected void done(){
                    xacNhan.setEnabled(true);
                    try {KetQua k=get();dialog.dispose();taiDuLieu();
                        int x=JOptionPane.showConfirmDialog(ThanhToanPanel.this,
                                "Đã ghi nhận hóa đơn HD"+k.maHD+" - "+vnd(k.duToan.phaiThu)
                                +(chuyen?" (CHUYỂN KHOẢN DEMO)":"")+".\nBạn có muốn xuất hóa đơn PDF?",
                                "Thanh toán thành công",JOptionPane.YES_NO_OPTION);
                        if(x==JOptionPane.YES_OPTION)xuatHoaDon(k.maHD);
                    }catch(Exception ex){baoLoi(ex);}
                }
            }.execute();
        });
        dialog.setVisible(true);
    }

    private void huyHoaDon(){
        Phieu p=phieuChon();if(p==null||p.maHD<=0)return;
        if(JOptionPane.showConfirmDialog(this,"Hủy hóa đơn HD"+p.maHD+" chưa thanh toán?",
                "Xác nhận hủy",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        nutHuy.setEnabled(false);
        new SwingWorker<Void,Void>(){
            @Override protected Void doInBackground() throws Exception {dao.huyHoaDonCho(p.maHD,actor);return null;}
            @Override protected void done(){try{get();thongBao("Đã hủy hóa đơn HD"+p.maHD);}
                catch(Exception ex){baoLoi(ex);}finally{taiDuLieu();}}
        }.execute();
    }

    private void xemHoaDon(){HoaDon h=hoaDonChon();if(h==null)return;
        new SwingWorker<List<ChiTiet>,Void>(){
            @Override protected List<ChiTiet> doInBackground() throws Exception {return dao.chiTietHoaDon(h.maHD,actor);}
            @Override protected void done(){try{List<ChiTiet> ds=get();
                DefaultTableModel m=model("Dịch vụ","Số lượng","Đơn giá","Thành tiền");
                for(ChiTiet d:ds)m.addRow(new Object[]{d.dichVu,d.soLuong,vnd(d.donGia),vnd(d.thanhTien)});
                JTable b=new JTable(m);dinhDangBang(b);
                JPanel p=new JPanel(new BorderLayout(0,12));
                JLabel l=new JLabel("HD"+h.maHD+" | "+h.benhNhan+" | "+h.ngayLap);
                l.setFont(new Font("Segoe UI",Font.BOLD,15));p.add(l,BorderLayout.NORTH);
                JScrollPane cuon=new JScrollPane(b);cuon.setPreferredSize(new Dimension(620,220));p.add(cuon,BorderLayout.CENTER);
                JLabel tong=new JLabel("Giá gốc "+vnd(h.tienGoc)+"  -  Giảm "+vnd(h.tienGiam)
                        +"  =  Đã thu "+vnd(h.phaiThu)+" ("+h.phuongThuc+")");
                p.add(tong,BorderLayout.SOUTH);
                JOptionPane.showMessageDialog(ThanhToanPanel.this,p,"Xem lại hóa đơn",JOptionPane.PLAIN_MESSAGE);
            }catch(Exception ex){baoLoi(ex);}}
        }.execute();
    }
    private void xuatHoaDon(){HoaDon h=hoaDonChon();if(h!=null)xuatHoaDon(h.maHD);}
    private void xuatHoaDon(int maHD){
        new SwingWorker<Object[],Void>(){
            @Override protected Object[] doInBackground() throws Exception {
                HoaDon hd=null;
                for(HoaDon h:dao.hoaDonGanDay(actor))if(h.maHD==maHD){hd=h;break;}
                if(hd==null)throw new IllegalArgumentException("Hóa đơn đã thanh toán không còn trong danh sách gần đây.");
                return new Object[]{hd,dao.chiTietHoaDon(maHD,actor)};
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
