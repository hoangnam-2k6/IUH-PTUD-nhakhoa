package UI.QuanLy;

import ConnectDB.ConnectDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Báo cáo hoạt động đọc dữ liệu SQL, không dùng số giả hoặc kết nối riêng cho mỗi ô. */
public class BaoCaoThongKePanel extends JPanel {
    private static final Color XANH=new Color(0x0284C7);
    private final JLabel doanhThu=new JLabel("—"),daThu=new JLabel("—"),choThu=new JLabel("—"),benhNhan=new JLabel("—");
    private final JLabel tinhTrang=new JLabel("Đang tải dữ liệu...");
    private final JComboBox<Integer> nam=new JComboBox<>();
    private final DefaultTableModel model=new DefaultTableModel(
        new String[]{"Tháng", "Giao dịch thành công", "Doanh thu", "Số bệnh nhân khám"},0){
        @Override public boolean isCellEditable(int row,int col){return false;}
    };
    private final JButton taiLai=new JButton("Tải lại");
    private int phienBaoCao;
    private final NumberFormat tien=NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public BaoCaoThongKePanel(){
        setLayout(new BorderLayout(12,16));setBackground(new Color(0xF8FAFC));
        setBorder(new EmptyBorder(22,25,22,25));
        JPanel dau=new JPanel(new BorderLayout());dau.setOpaque(false);
        JLabel title=new JLabel("Báo cáo hoạt động phòng khám");
        title.setFont(new Font("Segoe UI",Font.BOLD,23));title.setForeground(new Color(0x0F172A));
        dau.add(title,BorderLayout.WEST);
        JPanel chon=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));chon.setOpaque(false);
        chon.add(new JLabel("Năm"));
        int now=LocalDate.now().getYear();for(int y=now-3;y<=now+1;y++) nam.addItem(y);
        nam.setSelectedItem(now);chon.add(nam);chon.add(taiLai);dau.add(chon,BorderLayout.EAST);
        add(dau,BorderLayout.NORTH);
        JPanel giua=new JPanel(new BorderLayout(0,14));giua.setOpaque(false);
        JPanel chiSo=new JPanel(new java.awt.GridLayout(1,4,12,0));chiSo.setOpaque(false);
        chiSo.add(o("Doanh thu năm",doanhThu));chiSo.add(o("Phiếu đã thu",daThu));
        chiSo.add(o("Hóa đơn chờ thu",choThu));chiSo.add(o("Tổng bệnh nhân",benhNhan));
        giua.add(chiSo,BorderLayout.NORTH);
        JTable bang=new JTable(model);bang.setRowHeight(35);bang.setFillsViewportHeight(true);
        bang.setAutoCreateRowSorter(true);
        JPanel bao=new JPanel(new BorderLayout());bao.setBackground(Color.WHITE);
        bao.setBorder(new EmptyBorder(10,10,10,10));bao.add(new JScrollPane(bang),BorderLayout.CENTER);
        giua.add(bao,BorderLayout.CENTER);add(giua,BorderLayout.CENTER);
        tinhTrang.setForeground(new Color(0x64748B));add(tinhTrang,BorderLayout.SOUTH);
        taiLai.addActionListener(e->napDuLieu());nam.addActionListener(e->napDuLieu());
        napDuLieu();
    }
    private JPanel o(String title,JLabel value){
        JPanel p=new JPanel(new BorderLayout(0,7));p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,17,15,17));
        JLabel l=new JLabel(title);l.setForeground(new Color(0x64748B));
        value.setForeground(XANH);value.setFont(new Font("Segoe UI",Font.BOLD,18));
        p.add(l,BorderLayout.NORTH);p.add(value,BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(170,90));return p;
    }
    private static final class DuLieu {
        BigDecimal doanhThu=BigDecimal.ZERO;
        int daThu,choThu,benhNhan;
        final int[] soGiaoDich=new int[13],luotKham=new int[13];
        final BigDecimal[] theoThang=new BigDecimal[13];
        DuLieu(){for(int i=0;i<13;i++)theoThang[i]=BigDecimal.ZERO;}
    }
    private void napDuLieu(){
        if(nam.getSelectedItem()==null)return;
        final int year=(Integer)nam.getSelectedItem();
        final int phien=++phienBaoCao;
        taiLai.setEnabled(false);
        tinhTrang.setText("Đang đọc thống kê SQL năm "+year+"...");
        new SwingWorker<DuLieu,Void>(){
            @Override protected DuLieu doInBackground() throws SQLException {
                DuLieu a=new DuLieu();
                try(Connection c=ConnectDB.getConnection()){
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*),ISNULL(SUM(SoTien),0) FROM dbo.ThanhToan "
                            +"WHERE TrangThai IN(N'Thành công',N'Đã thanh toán') AND YEAR(NgayThanhToan)=?")){
                        p.setInt(1,year);try(ResultSet r=p.executeQuery()){
                            if(r.next()){a.daThu=r.getInt(1);a.doanhThu=r.getBigDecimal(2);}
                        }
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM dbo.HoaDon WHERE TrangThai=N'Chưa thanh toán'")){
                        try(ResultSet r=p.executeQuery()){if(r.next())a.choThu=r.getInt(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM dbo.BenhNhan")){
                        try(ResultSet r=p.executeQuery()){if(r.next())a.benhNhan=r.getInt(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT MONTH(NgayThanhToan),COUNT(*),SUM(SoTien) FROM dbo.ThanhToan "
                            +"WHERE TrangThai IN(N'Thành công',N'Đã thanh toán') AND YEAR(NgayThanhToan)=? "
                            +"GROUP BY MONTH(NgayThanhToan)")){
                        p.setInt(1,year);try(ResultSet r=p.executeQuery()){
                            while(r.next()){int i=r.getInt(1);a.soGiaoDich[i]=r.getInt(2);a.theoThang[i]=r.getBigDecimal(3);}
                        }
                    }
                    try(PreparedStatement p=c.prepareStatement("SELECT MONTH(NgayKham),COUNT(*) FROM dbo.PhieuKham "
                            +"WHERE TrangThai<>N'Đã hủy' AND YEAR(NgayKham)=? GROUP BY MONTH(NgayKham)")){
                        p.setInt(1,year);try(ResultSet r=p.executeQuery()){
                            while(r.next())a.luotKham[r.getInt(1)]=r.getInt(2);
                        }
                    }
                }return a;
            }
            @Override protected void done(){
                if(phien!=phienBaoCao)return;
                taiLai.setEnabled(true);
                try{
                    DuLieu a=get();doanhThu.setText(tien.format(a.doanhThu)+" đ");
                    daThu.setText(String.valueOf(a.daThu));choThu.setText(String.valueOf(a.choThu));
                    benhNhan.setText(String.valueOf(a.benhNhan));model.setRowCount(0);
                    for(int i=1;i<=12;i++)model.addRow(new Object[]{i+"/"+year,
                        a.soGiaoDich[i],tien.format(a.theoThang[i])+" đ",a.luotKham[i]});
                    tinhTrang.setText("Số liệu trực tiếp từ SQL Server, tính theo trạng thái thanh toán thành công.");
                }catch(Exception ex){
                    tinhTrang.setText("Không tải được báo cáo: "+(ex.getCause()==null?ex.getMessage():ex.getCause().getMessage()));
                }
            }
        }.execute();
    }
}
