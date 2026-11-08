package UI.QuanLy;

import DAO.ThanhToanKhuyenMaiDAO.ChiTiet;
import DAO.ThanhToanKhuyenMaiDAO.HoaDon;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/** Xuất hóa đơn đã thu thành PDF thật từ dữ liệu SQL, không sử dụng mẫu HTML giả. */
public final class XuatHoaDonPDF {
    private XuatHoaDonPDF(){}
    private static final NumberFormat TIEN=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public static void xuat(Component cha,HoaDon hd,List<ChiTiet> chiTiet){
        JFileChooser chon=new JFileChooser();chon.setDialogTitle("Xuất hóa đơn PDF");
        chon.setFileFilter(new FileNameExtensionFilter("Hóa đơn PDF (*.pdf)","pdf"));
        chon.setSelectedFile(new File("HoaDon_HD"+hd.maHD+".pdf"));
        if(chon.showSaveDialog(cha)!=JFileChooser.APPROVE_OPTION)return;
        File f=chon.getSelectedFile();
        if(!f.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))f=new File(f.getParentFile(),f.getName()+".pdf");
        if(f.exists()&&JOptionPane.showConfirmDialog(cha,"Ghi đè file "+f.getName()+"?",
                "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try {ghiPDF(f,hd,chiTiet);
            JOptionPane.showMessageDialog(cha,"Đã xuất hóa đơn PDF:\n"+f.getAbsolutePath());
        }catch(Exception e){JOptionPane.showMessageDialog(cha,"Không thể xuất PDF: "+e.getMessage(),
                "Lỗi",JOptionPane.ERROR_MESSAGE);}
    }

    /** Đọc font hệ thống để in dấu tiếng Việt; không đóng gói hay xuất file font. */
    private static File fontHeThong() throws IOException {
        String[] paths={"C:/Windows/Fonts/arial.ttf","C:/Windows/Fonts/tahoma.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf"};
        for(String path:paths){File f=new File(path);if(f.isFile())return f;}
        throw new IOException("Không tìm thấy font hỗ trợ tiếng Việt trên máy.");
    }
    private static String tien(BigDecimal so){return TIEN.format(so==null?BigDecimal.ZERO:so)+" đ";}
    private static void dong(PDPageContentStream out,PDType0Font font,float x,float y,
                             int co,String s) throws IOException {
        out.beginText();out.setFont(font,co);out.newLineAtOffset(x,y);out.showText(s);out.endText();
    }
    private static String ngan(String s,int max){
        if(s==null)return "";return s.length()>max?s.substring(0,max-3)+"...":s;
    }
    private static void ghiPDF(File f,HoaDon h,List<ChiTiet> ds) throws IOException {
        try(PDDocument doc=new PDDocument()){
            PDType0Font font=PDType0Font.load(doc,fontHeThong());
            PDPage page=new PDPage(PDRectangle.A4);doc.addPage(page);
            PDPageContentStream out=new PDPageContentStream(doc,page);
            float y=790;
            try {
                dong(out,font,46,y,19,"MEDIDENT - PHÒNG KHÁM NHA KHOA");y-=38;
                dong(out,font,46,y,16,"HÓA ĐƠN THANH TOÁN");y-=33;
                dong(out,font,46,y,11,"Mã hóa đơn: HD"+h.maHD+"   |   Phiếu khám: PK"+h.maPK);y-=23;
                dong(out,font,46,y,11,"Ngày lập: "+h.ngayLap);y-=23;
                dong(out,font,46,y,11,"Bệnh nhân: "+ngan(h.benhNhan,55));y-=23;
                dong(out,font,46,y,11,"Phương thức: "+h.phuongThuc);y-=23;
                dong(out,font,46,y,11,"Trạng thái: "+h.trangThai);y-=33;
                dong(out,font,46,y,12,"CHI TIẾT DỊCH VỤ");y-=26;
                dong(out,font,46,y,10,"Dịch vụ");
                dong(out,font,302,y,10,"SL");
                dong(out,font,350,y,10,"Đơn giá");
                dong(out,font,467,y,10,"Thành tiền");y-=22;
                for(ChiTiet ct:ds){
                    if(y<110){
                        out.close();page=new PDPage(PDRectangle.A4);doc.addPage(page);
                        out=new PDPageContentStream(doc,page);y=790;
                    }
                    dong(out,font,46,y,10,ngan(ct.dichVu,38));
                    dong(out,font,302,y,10,String.valueOf(ct.soLuong));
                    dong(out,font,350,y,10,TIEN.format(ct.donGia));
                    dong(out,font,467,y,10,TIEN.format(ct.thanhTien));y-=22;
                }
                if(y<160){out.close();page=new PDPage(PDRectangle.A4);doc.addPage(page);
                    out=new PDPageContentStream(doc,page);y=790;}
                y-=19;
                dong(out,font,330,y,11,"Tổng gốc: "+tien(h.tienGoc));y-=25;
                dong(out,font,330,y,11,"Mã giảm: "+(h.maKM==null||h.maKM.isBlank()?"Không":h.maKM));y-=25;
                dong(out,font,330,y,11,"Giảm: -"+tien(h.tienGiam));y-=27;
                dong(out,font,330,y,14,"ĐÃ THU: "+tien(h.phaiThu));y-=38;
                dong(out,font,46,y,10,"Hóa đơn được ghi nhận từ giao dịch đã thanh toán trên MEDIDENT.");
            }finally {out.close();}
            doc.save(f);
        }
    }
}
