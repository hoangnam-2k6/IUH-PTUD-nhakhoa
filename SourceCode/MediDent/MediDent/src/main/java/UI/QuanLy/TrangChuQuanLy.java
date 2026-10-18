package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Dashboard Quản lý - thẻ thống kê và lịch hẹn gần đây lấy trực tiếp SQL Server. */
public final class TrangChuQuanLy extends JPanel {

    public TrangChuQuanLy(KhoDuLieu db,TaiKhoan user) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel noiDung=GiaoDien.trang("Trang chủ Quản lý phòng khám",
                "Xin chào " + user.hoTen + ". Tổng quan hoạt động phòng khám.");
        noiDung.setBorder(new EmptyBorder(10,20,10,20));
        add(noiDung,BorderLayout.CENTER);
        try {
            int benhNhan=db.dem("SELECT COUNT(*) FROM dbo.BenhNhan");
            int lich=db.dem("SELECT COUNT(*) FROM dbo.LichHen WHERE ngayHen=CAST(GETDATE() AS date)");
            int kham=db.dem("SELECT COUNT(*) FROM dbo.PhieuKham WHERE "
                    + "ngayKham>=DATEADD(month,DATEDIFF(month,0,GETDATE()),0) "
                    + "AND ngayKham<DATEADD(month,DATEDIFF(month,0,GETDATE())+1,0)");
            int hoaDon=db.dem("SELECT COUNT(*) FROM dbo.HoaDon");
            JPanel cards=new JPanel(new GridLayout(2,2,14,14));
            cards.setOpaque(false);
            cards.setAlignmentX(LEFT_ALIGNMENT);
            cards.setPreferredSize(new Dimension(760,220));
            cards.setMaximumSize(new Dimension(Integer.MAX_VALUE,220));
            cards.add(the("Tổng bệnh nhân",benhNhan,GiaoDien.PRIMARY,
                    BieuTuong.Loai.TAI_KHOAN,"Hồ sơ lưu trong hệ thống"));
            cards.add(the("Lịch hẹn hôm nay",lich,GiaoDien.GREEN,
                    BieuTuong.Loai.THONG_BAO,"Theo ngày hiện tại"));
            cards.add(the("Lượt khám trong tháng",kham,new Color(0xF59E0B),
                    BieuTuong.Loai.KIEM_TRA,"Tính theo phiếu khám"));
            cards.add(the("Tổng hóa đơn",hoaDon,new Color(0x8B5CF6),
                    BieuTuong.Loai.BAO_CAO,"Tất cả trạng thái"));

            JPanel noiDungLich=new JPanel(new BorderLayout(0,10));
            noiDungLich.setOpaque(false);
            noiDungLich.add(GiaoDien.label("Lịch hẹn gần đây",14,true),BorderLayout.NORTH);
            DefaultTableModel model=new DefaultTableModel(
                    new String[]{"STT","Ngày","Giờ","Bệnh nhân","Trạng thái"},0) {
                @Override public boolean isCellEditable(int r,int c){return false;}
            };
            try {
                List<QuanLyDAO.Hang> rows=new QuanLyDAO().lichHenGanDay();
                int stt=1;
                for(QuanLyDAO.Hang row:rows)
                    model.addRow(new Object[]{stt++,row.cot[0],row.cot[1],row.cot[2],row.cot[3]});
            }catch(Exception e) {
                // 4 thẻ KPI vẫn hiển thị nếu phần danh sách lịch bị lỗi.
                // Để bảng trống khi lỗi; không tạo dòng giả như bản ghi SQL thật.
                System.err.println("Không tải được lịch hẹn Dashboard: " + e.getMessage());
            }
            JTable table=new JTable(model);
            table.setFont(new Font("Segoe UI",Font.PLAIN,12));
            JScrollPane tableWrap=(JScrollPane)GiaoDien.wrapTable(table);
            table.setRowHeight(25);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            tableWrap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            tableWrap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            tableWrap.setPreferredSize(new Dimension(760,330));
            noiDungLich.add(tableWrap,BorderLayout.CENTER);

            JPanel khungTongQuan=GiaoDien.card(new BorderLayout(0,14));
            khungTongQuan.setPreferredSize(new Dimension(760,610));
            khungTongQuan.setMaximumSize(new Dimension(Integer.MAX_VALUE,610));
            khungTongQuan.add(cards,BorderLayout.NORTH);
            khungTongQuan.add(noiDungLich,BorderLayout.CENTER);
            noiDung.add(khungTongQuan);
        }catch(Exception ex) {
            noiDung.add(GiaoDien.label("Không tải được thống kê SQL Server: "+ex.getMessage(),12,false));
        }
    }

    private JPanel the(String title,int value,Color accent,BieuTuong.Loai icon,String moTa) {
        JPanel card=GiaoDien.card(new BorderLayout(12,5));
        card.setBorder(new EmptyBorder(10,16,10,16));
        JPanel info=new JPanel();info.setOpaque(false);
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JLabel label=GiaoDien.label(title,11,false);
        JLabel total=GiaoDien.label(String.format("%,d",value),25,true);
        JLabel sub=GiaoDien.label(moTa,10,false);
        info.add(label);info.add(Box.createVerticalStrut(5));
        info.add(total);info.add(Box.createVerticalStrut(4));info.add(sub);
        card.add(info,BorderLayout.CENTER);
        JLabel glyph=new JLabel(new BieuTuong(icon,accent,24));
        glyph.setPreferredSize(new Dimension(39,39));
        glyph.setHorizontalAlignment(SwingConstants.CENTER);
        glyph.setOpaque(true);
        glyph.setBackground(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),35));
        card.add(glyph,BorderLayout.EAST);
        return card;
    }
}
