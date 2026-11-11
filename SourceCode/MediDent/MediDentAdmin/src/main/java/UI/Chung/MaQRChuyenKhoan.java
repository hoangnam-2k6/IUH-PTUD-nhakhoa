package UI.Chung;

import DAO.ThanhToanKhuyenMaiDAO.NganHang;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;

/** VietQR chuyển tiền tài khoản (NAPAS QRIBFTTA). In trước khi nhận tiền, không xác nhận giao dịch. */
public final class MaQRChuyenKhoan {
    private MaQRChuyenKhoan() { }

    private static String truong(String id,String giaTri) {
        if (giaTri.length()>99) throw new IllegalArgumentException("Trường QR quá dài.");
        return id+String.format(Locale.ROOT,"%02d",giaTri.length())+giaTri;
    }

    public static String noiDung(NganHang nh,BigDecimal tien,int maPK) {
        return noiDung(nh,tien,maPK,"");
    }

    public static String noiDung(NganHang nh,BigDecimal tien,int maPK,String benhNhan) {
        if (nh==null || !nh.bin.matches("[0-9]{6}") || !nh.soTaiKhoan.matches("[0-9]{6,19}"))
            throw new IllegalArgumentException("Thông tin ngân hàng không hợp lệ.");
        if(tien==null || tien.signum()<=0 || tien.stripTrailingZeros().scale()>0)
            throw new IllegalArgumentException("Số tiền QR phải là số VND nguyên dương.");
        String soTien=tien.toBigIntegerExact().toString();
        if(soTien.length()>13) throw new IllegalArgumentException("Số tiền chuyển khoản vượt giới hạn VietQR.");
        String thuHuong=truong("00",nh.bin)+truong("01",nh.soTaiKhoan);
        String napas=truong("00","A000000727")+truong("01",thuHuong)+truong("02","QRIBFTTA");
        String ten=java.text.Normalizer.normalize(benhNhan==null?"":benhNhan,
                java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","")
                .replace('đ','d').replace('Đ','D').replaceAll("[^A-Za-z0-9 ]","").trim();
        String memo="MEDIDENT PK"+maPK+(ten.isBlank()?"":" "+ten);
        if(memo.length()>55)memo=memo.substring(0,55).trim();
        String ma=truong("00","01")+truong("01","12")+truong("38",napas)
                +truong("53","704")+truong("54",soTien)+truong("58","VN")
                +truong("62",truong("08",memo))+"6304";
        int crc=0xFFFF;
        for(byte b:ma.getBytes(StandardCharsets.US_ASCII)) {
            crc^=(b&0xFF)<<8;
            for(int i=0;i<8;i++) crc=(crc&0x8000)!=0?(crc<<1)^0x1021:(crc<<1);
            crc&=0xFFFF;
        }
        return ma+String.format(Locale.ROOT,"%04X",crc);
    }

    public static BufferedImage anh(String noiDung,int kichThuoc) throws WriterException {
        BitMatrix matran=new QRCodeWriter().encode(noiDung,BarcodeFormat.QR_CODE,kichThuoc,kichThuoc);
        BufferedImage anh=new BufferedImage(matran.getWidth(),matran.getHeight(),BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<matran.getHeight();y++)
            for(int x=0;x<matran.getWidth();x++)
                anh.setRGB(x,y,matran.get(x,y)?Color.BLACK.getRGB():Color.WHITE.getRGB());
        return anh;
    }

    /** Hiển thị và cho in mã QR trên máy in đã chọn. Không tự xác nhận đã thu tiền. */
    public static void hienThi(Component cha,NganHang nh,BigDecimal tien,int maPK) throws Exception {
        BufferedImage anh=anh(noiDung(nh,tien,maPK),300);
        String soTien=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(tien)+" đ";
        JDialog dlg=new JDialog(SwingUtilities.getWindowAncestor(cha),"Mã QR chuyển khoản - PK"+maPK,
                JDialog.ModalityType.APPLICATION_MODAL);
        JPanel box=new JPanel(new BorderLayout(4,12));box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createEmptyBorder(20,25,20,25));
        JLabel title=new JLabel("QUÉT QR ĐỂ CHUYỂN KHOẢN",SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI",Font.BOLD,17));title.setForeground(new Color(2,132,199));
        box.add(title,BorderLayout.NORTH);
        JLabel image=new JLabel(new ImageIcon(anh),SwingConstants.CENTER);box.add(image,BorderLayout.CENTER);
        JPanel below=new JPanel(new java.awt.GridLayout(4,1,0,3));below.setBackground(Color.WHITE);
        for(String s:new String[]{"Ngân hàng (BIN): "+nh.bin,"Số tài khoản: "+nh.soTaiKhoan,
                "Chủ tài khoản: "+nh.chuTaiKhoan,"Số tiền: "+soTien+" • Nội dung: MEDIDENT PK"+maPK}) {
            JLabel line=new JLabel(s,SwingConstants.CENTER);line.setFont(new Font("Segoe UI",Font.PLAIN,13));below.add(line);
        }
        JPanel footer=new JPanel(new BorderLayout(0,8));footer.setBackground(Color.WHITE);
        footer.add(below,BorderLayout.CENTER);
        JPanel actions=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,10,5));actions.setOpaque(false);
        JButton print=new JButton("In mã QR");JButton close=new JButton("Đóng");
        print.addActionListener(e->{
            PrinterJob job=PrinterJob.getPrinterJob();job.setJobName("MediDent QR PK"+maPK);
            job.setPrintable((graphics,format,index)->{
                if(index>0)return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D g=(Graphics2D)graphics.create();
                try {
                    g.translate(format.getImageableX(),format.getImageableY());
                    double scale=Math.min(format.getImageableWidth()/390,format.getImageableHeight()/460);
                    g.scale(scale,scale);g.setColor(Color.BLACK);
                    g.setFont(new Font("SansSerif",Font.BOLD,18));g.drawString("MEDIDENT - QR CHUYEN KHOAN",35,32);
                    g.drawImage(anh,45,50,300,300,null);
                    g.setFont(new Font("SansSerif",Font.PLAIN,13));
                    g.drawString("BIN: "+nh.bin+" | STK: "+nh.soTaiKhoan,20,368);
                    g.drawString("Chu TK: "+nh.chuTaiKhoan,20,388);
                    g.drawString("So tien: "+soTien+" | MEDIDENT PK"+maPK,20,408);
                    g.drawString("Vui long kiem tra nguoi nhan truoc khi chuyen",20,436);
                    return java.awt.print.Printable.PAGE_EXISTS;
                }finally{g.dispose();}
            });
            if(job.printDialog())try{job.print();}catch(PrinterException ex){
                javax.swing.JOptionPane.showMessageDialog(dlg,"Lỗi in QR: "+ex.getMessage());
            }
        });
        close.addActionListener(e->dlg.dispose());actions.add(print);actions.add(close);
        footer.add(actions,BorderLayout.SOUTH);box.add(footer,BorderLayout.SOUTH);
        dlg.setContentPane(box);dlg.setSize(520,580);dlg.setLocationRelativeTo(cha);dlg.setVisible(true);
    }
}
