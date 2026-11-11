package UI.LeTan;

import DAO.HoaDonLeTanDAO;
import DAO.ThanhToanKhuyenMaiDAO.ChiTiet;
import DAO.ThanhToanKhuyenMaiDAO.HoaDon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/** Chỉ định dạng và xuất PDF; không can thiệp nghiệp vụ thu tiền. */
public final class XuatHoaDonPDF {
    private static final Color XANH = new Color(0x0284C7);
    private static final Color XANH_NHAT = new Color(0xE0F2FE);
    private static final Color CHU = new Color(0x0F172A);
    private static final Color VIEN = new Color(0xCBD5E1);
    private static final NumberFormat TIEN = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private static final int RONG = 1190, CAO = 1684;
    private static final int TRAI = 96, PHAI = 1094;
    private static final int[] COT = {TRAI, 622, 699, 881, PHAI};

    private XuatHoaDonPDF() {}

    public static void xuat(Component cha, HoaDon hoaDon, List<ChiTiet> dichVu) throws Exception {
        if (hoaDon == null) throw new IllegalArgumentException("Chưa chọn hóa đơn.");
        JFileChooser chon = new JFileChooser();
        chon.setDialogTitle("Lưu hóa đơn PDF");
        chon.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        chon.setSelectedFile(new File("HoaDon_HD" + hoaDon.maHD + ".pdf"));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return;
        File tep = chon.getSelectedFile();
        if (!tep.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))
            tep = new File(tep.getParentFile(), tep.getName() + ".pdf");
        if (tep.exists() && JOptionPane.showConfirmDialog(cha, "Ghi đè file đã có?", "Xuất PDF",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String bacSi = "Chưa cập nhật";
        try {
            Entity.HoaDon chiTiet = new HoaDonLeTanDAO().timDayDu(hoaDon.maHD);
            if (chiTiet != null && chiTiet.layTenBacSi() != null && !chiTiet.layTenBacSi().isBlank())
                bacSi = chiTiet.layTenBacSi();
        } catch (java.sql.SQLException ex) {
            // Dữ liệu bác sĩ có thể chưa được nhập; không làm hỏng chứng từ thanh toán.
        }
        List<BufferedImage> trang = veCacTrang(hoaDon, dichVu, bacSi);
        try (PDDocument pdf = new PDDocument()) {
            for (BufferedImage anhTrang : trang) {
                PDPage page = new PDPage(PDRectangle.A4);
                pdf.addPage(page);
                PDImageXObject anh = LosslessFactory.createFromImage(pdf, anhTrang);
                try (PDPageContentStream noiDung = new PDPageContentStream(pdf, page)) {
                    noiDung.drawImage(anh, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                }
            }
            pdf.save(tep);
        }
        JOptionPane.showMessageDialog(cha, "Đã lưu hóa đơn PDF:\n" + tep.getAbsolutePath());
    }

    private static List<BufferedImage> veCacTrang(HoaDon h, List<ChiTiet> ds, String bacSi) {
        List<ChiTiet> danhSach = ds == null ? Collections.emptyList() : ds;
        List<BufferedImage> ketQua = new ArrayList<>();
        int index = 0;
        do {
            BufferedImage trang = new BufferedImage(RONG, CAO, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = trang.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(Color.WHITE);g.fillRect(0, 0, RONG, CAO);
                g.setColor(CHU);g.setFont(new Font("Segoe UI", Font.BOLD, 36));veGiua(g,"MEDIDENT",145);
                g.setFont(new Font("Segoe UI",Font.PLAIN,20));veGiua(g,"PHÒNG KHÁM NHA KHOA",182);
                g.setColor(XANH);g.drawLine(TRAI,213,PHAI,213);
                g.setColor(CHU);g.setFont(new Font("Segoe UI",Font.BOLD,29));veGiua(g,"HÓA ĐƠN THANH TOÁN",268);
                g.setFont(new Font("Segoe UI",Font.PLAIN,22));
                g.drawString("Số hóa đơn: HD"+h.maHD,TRAI,320);
                g.drawString("Ngày lập: "+hien(h.ngayLap),650,320);
                g.drawString("Bệnh nhân: "+hien(h.benhNhan),TRAI,365);
                g.drawString("Bác sĩ khám: "+hien(bacSi),TRAI,407);
                g.drawString("Phiếu khám: PK"+h.maPK,TRAI,449);
                g.setColor(XANH_NHAT);g.fillRect(TRAI,488,PHAI-TRAI,58);
                g.setColor(VIEN);g.setStroke(new BasicStroke(2));g.drawRect(TRAI,488,PHAI-TRAI,58);
                g.setFont(new Font("Segoe UI",Font.BOLD,20));g.setColor(CHU);
                g.drawString("DỊCH VỤ",COT[0]+12,526);
                veGiuaCot(g,"SL",COT[1],COT[2],526);
                veGiuaCot(g,"ĐƠN GIÁ",COT[2],COT[3],526);
                veGiuaCot(g,"THÀNH TIỀN",COT[3],COT[4],526);
                int y=546;
                g.setFont(new Font("Segoe UI",Font.PLAIN,20));
                while(index<danhSach.size()) {
                    ChiTiet item=danhSach.get(index);
                    List<String> dong=ngatDong(g,hien(item.dichVu),COT[1]-COT[0]-27);
                    int caoDong=Math.max(60,dong.size()*27+17);
                    if(y+caoDong>1050 && y>546)break;
                    if(y+caoDong>1100)caoDong=1100-y;
                    g.setColor(index%2==0?Color.WHITE:new Color(0xF8FAFC));
                    g.fillRect(TRAI,y,PHAI-TRAI,caoDong);
                    g.setColor(VIEN);g.drawRect(TRAI,y,PHAI-TRAI,caoDong);
                    for(int k=1;k<COT.length-1;k++)g.drawLine(COT[k],y,COT[k],y+caoDong);
                    g.setColor(CHU);
                    for(int t=0;t<dong.size();t++)g.drawString(dong.get(t),TRAI+12,y+31+t*27);
                    veGiuaCot(g,String.valueOf(item.soLuong),COT[1],COT[2],y+37);
                    vePhai(g,tien(item.donGia),COT[3]-12,y+37);
                    vePhai(g,tien(item.thanhTien),COT[4]-12,y+37);
                    y+=caoDong;
                    index++;
                }
                // Khung bao bảng, kể cả khi hóa đơn chưa có dòng dịch vụ.
                g.setColor(VIEN);g.drawLine(TRAI,y,PHAI,y);
                if(index>=danhSach.size()) {
                    g.setFont(new Font("Segoe UI",Font.PLAIN,23));g.setColor(CHU);
                    g.drawString("Tổng dịch vụ",645,1155);vePhai(g,tien(h.tienGoc),PHAI-15,1155);
                    g.drawString("Khuyến mãi",645,1201);vePhai(g,"-"+tien(h.tienGiam),PHAI-15,1201);
                    g.setColor(XANH_NHAT);g.fillRoundRect(618,1221,476,67,12,12);
                    g.setColor(XANH);g.setFont(new Font("Segoe UI",Font.BOLD,27));
                    g.drawString("ĐÃ THU",645,1265);vePhai(g,tien(h.phaiThu),PHAI-15,1265);
                    g.setColor(CHU);g.setFont(new Font("Segoe UI",Font.PLAIN,20));
                    g.drawString("Phương thức: "+hien(h.phuongThuc),TRAI,1330);
                    g.drawString("Mã khuyến mãi: "+(h.maKM==null||h.maKM.isBlank()?"Không có":h.maKM),TRAI,1365);
                } else {
                    g.setColor(new Color(0x64748B));g.setFont(new Font("Segoe UI",Font.ITALIC,20));
                    g.drawString("Danh sách dịch vụ còn tiếp ở trang sau",TRAI+12,1110);
                }
                g.setColor(VIEN);g.drawLine(TRAI,1507,PHAI,1507);
                g.setColor(new Color(0x64748B));g.setFont(new Font("Segoe UI",Font.ITALIC,18));
                veGiua(g,"Cảm ơn quý khách đã sử dụng dịch vụ tại MediDent!",1551);
                g.setFont(new Font("Segoe UI",Font.PLAIN,17));
            } finally {g.dispose();}
            ketQua.add(trang);
        } while(index<danhSach.size());
        return ketQua;
    }

    private static List<String> ngatDong(Graphics2D g,String chuoi,int maxW) {
        List<String> ds=new ArrayList<>();
        if(chuoi.isBlank()){ds.add("(Không ghi tên)");return ds;}
        FontMetrics fm=g.getFontMetrics();StringBuilder dong=new StringBuilder();
        for(String tu:chuoi.split("\\s+")) {
            String thu=dong.length()==0?tu:dong+" "+tu;
            if(fm.stringWidth(thu)>maxW && dong.length()>0){ds.add(dong.toString());dong.setLength(0);}
            if(dong.length()>0)dong.append(' ');
            dong.append(tu);
        }
        if(dong.length()>0)ds.add(dong.toString());
        return ds;
    }
    private static void veGiua(Graphics2D g,String s,int y){g.drawString(s,(RONG-g.getFontMetrics().stringWidth(s))/2,y);}
    private static void veGiuaCot(Graphics2D g,String s,int x1,int x2,int y){g.drawString(s,x1+(x2-x1-g.getFontMetrics().stringWidth(s))/2,y);}
    private static void vePhai(Graphics2D g,String s,int x,int y){g.drawString(s,x-g.getFontMetrics().stringWidth(s),y);}
    private static String tien(BigDecimal n){return TIEN.format(n==null?BigDecimal.ZERO:n)+" đ";}
    private static String hien(String s){return s==null?"":s;}
}
