package UI.QuanLy;

import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;

/** Một nút mở lịch tháng: nhấn ngày bắt đầu, ngày kết thúc, rồi Chọn. */
public class ChonKhoangNgay extends JButton {
    private static final DateTimeFormatter DINH_DANG=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private LocalDate tuNgay,denNgay;
    private Runnable khiThayDoi=()->{};

    public ChonKhoangNgay() {
        super();
        LocalDate nay=LocalDate.now();
        tuNgay=nay.withDayOfMonth(1);denNgay=nay;
        capNhatNhan();
        GiaoDienChung.trangTriNut(this);
        addActionListener(e->moLich());
    }

    public LocalDate getTuNgay(){return tuNgay;}
    public LocalDate getDenNgay(){return denNgay;}
    public void setKhiThayDoi(Runnable listener){khiThayDoi=listener==null?()->{}:listener;}
    public void datKhoang(LocalDate tu,LocalDate den){
        if((tu==null)!=(den==null)||(tu!=null&&tu.isAfter(den)))throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        tuNgay=tu;denNgay=den;capNhatNhan();khiThayDoi.run();
    }

    private void capNhatNhan() {
    DateTimeFormatter ngan = DateTimeFormatter.ofPattern("dd/MM");

    setText(tuNgay == null ? "Tất cả ngày"
            : tuNgay.format(ngan) + " - " + denNgay.format(ngan));

    setToolTipText(tuNgay == null ? "Không giới hạn thời gian"
            : tuNgay.format(DINH_DANG) + " đến " + denNgay.format(DINH_DANG));
}


    private void moLich(){
        Window cha=SwingUtilities.getWindowAncestor(this);
        JDialog hopThoai=new JDialog(cha,"Chọn khoảng ngày",Dialog.ModalityType.APPLICATION_MODAL);
        hopThoai.setLayout(new BorderLayout(8,8));
        hopThoai.getRootPane().setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        LichThang lich=new LichThang(tuNgay==null?LocalDate.now():tuNgay,tuNgay,denNgay);
        hopThoai.add(lich,BorderLayout.CENTER);
        JPanel duoi=GiaoDienChung.hang();
        JButton thang=GiaoDienChung.nut("Tháng này");
        JButton nam=GiaoDienChung.nut("Năm nay");
        JButton tatCa=GiaoDienChung.nut("Tất cả");
        JButton chon=GiaoDienChung.nut("Chọn");
        JButton huy=GiaoDienChung.nut("Đóng");
        thang.addActionListener(e->{LocalDate n=LocalDate.now();datKhoang(n.withDayOfMonth(1),n);hopThoai.dispose();});
        nam.addActionListener(e->{LocalDate n=LocalDate.now();datKhoang(n.withDayOfYear(1),n);hopThoai.dispose();});
        tatCa.addActionListener(e->{datKhoang(null,null);hopThoai.dispose();});
        chon.addActionListener(e->{
            if(lich.chonTu==null||lich.chonDen==null){
                JOptionPane.showMessageDialog(hopThoai,"Chọn lần lượt ngày bắt đầu và ngày kết thúc trên lịch.");return;
            }
            datKhoang(lich.chonTu,lich.chonDen);hopThoai.dispose();
        });
        huy.addActionListener(e->hopThoai.dispose());
        duoi.add(thang);duoi.add(nam);duoi.add(tatCa);duoi.add(chon);duoi.add(huy);
        hopThoai.add(duoi,BorderLayout.SOUTH);
        hopThoai.pack();
        hopThoai.setMinimumSize(new Dimension(520,430));
        hopThoai.setLocationRelativeTo(this);
        hopThoai.setVisible(true);
    }

    private static class LichThang extends JPanel {
        LocalDate chonTu,chonDen,thang;
        JPanel ngayTrongThang=new JPanel(new GridLayout(0,7,4,4));
        JLabel tieuDe=new JLabel("",SwingConstants.CENTER);
        JLabel huongDan=new JLabel("Nhấn ngày bắt đầu, sau đó nhấn ngày kết thúc.",SwingConstants.CENTER);
        LichThang(LocalDate thang,LocalDate tu,LocalDate den){
            this.thang=thang.withDayOfMonth(1);chonTu=tu;chonDen=den;
            setLayout(new BorderLayout(6,8));
            JButton lui=GiaoDienChung.nut("<");
            JButton tien=GiaoDienChung.nut(">");
            JPanel tren=new JPanel(new BorderLayout(8,0));
            tieuDe.setFont(new Font("Arial",Font.BOLD,16));
            tren.add(lui,BorderLayout.WEST);tren.add(tieuDe,BorderLayout.CENTER);tren.add(tien,BorderLayout.EAST);
            lui.addActionListener(e->{this.thang=this.thang.minusMonths(1);veLich();});
            tien.addActionListener(e->{this.thang=this.thang.plusMonths(1);veLich();});
            ngayTrongThang.setBorder(BorderFactory.createEmptyBorder(8,4,8,4));
            add(tren,BorderLayout.NORTH);add(ngayTrongThang,BorderLayout.CENTER);add(huongDan,BorderLayout.SOUTH);
            veLich();
        }
        private void veLich(){
            ngayTrongThang.removeAll();
            tieuDe.setText("Tháng "+thang.getMonthValue()+" / "+thang.getYear());
            for(String ten:new String[]{"T2","T3","T4","T5","T6","T7","CN"}){
                JLabel l=new JLabel(ten,SwingConstants.CENTER);
                l.setFont(new Font("Arial",Font.BOLD,13));l.setForeground(GiaoDienChung.XANH);
                ngayTrongThang.add(l);
            }
            int truoc=thang.getDayOfWeek().getValue()-1;
            for(int i=0;i<truoc;i++)ngayTrongThang.add(new JLabel());
            for(int i=1;i<=thang.lengthOfMonth();i++){
                LocalDate ngay=thang.withDayOfMonth(i);
                JButton b=new JButton(String.valueOf(i));
                b.setFont(new Font("Arial",Font.PLAIN,13));
                b.setFocusPainted(false);
                b.setOpaque(true);
                boolean duocChon=chonTu!=null&&chonDen!=null&&!ngay.isBefore(chonTu)&&!ngay.isAfter(chonDen);
                b.setBackground(duocChon?new Color(198,229,250):Color.WHITE);
                b.setForeground(GiaoDienChung.CHU);
                b.setBorder(BorderFactory.createLineBorder(ngay.equals(chonTu)||ngay.equals(chonDen)
                        ? GiaoDienChung.XANH : GiaoDienChung.VIEN));
                b.addActionListener(e->{
                    if(chonTu==null||chonDen!=null){chonTu=ngay;chonDen=null;}
                    else if(ngay.isBefore(chonTu)){chonTu=ngay;}
                    else {chonDen=ngay;}
                    huongDan.setText(chonDen==null?"Đã chọn từ "+chonTu.format(DINH_DANG)+". Chọn ngày kết thúc.":
                            "Đã chọn "+chonTu.format(DINH_DANG)+" đến "+chonDen.format(DINH_DANG)+". Nhấn Chọn.");
                    veLich();
                });
                ngayTrongThang.add(b);
            }
            int them=(7-(truoc+thang.lengthOfMonth())%7)%7;
            for(int i=0;i<them;i++)ngayTrongThang.add(new JLabel());
            ngayTrongThang.revalidate();ngayTrongThang.repaint();
        }
    }
}
