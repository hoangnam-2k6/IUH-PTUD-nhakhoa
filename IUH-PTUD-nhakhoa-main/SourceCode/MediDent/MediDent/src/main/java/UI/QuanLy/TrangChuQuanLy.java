/*
 * TrangChuQuanLy: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class TrangChuQuanLy extends JPanel {

    /** Dựng trang tổng quan các thống kê vận hành của Quản lý. */
    public TrangChuQuanLy(KhoDuLieu db,TaiKhoan user) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel noiDung=GiaoDien.trang("Trang chủ Quản lý phòng khám",
                "Xin chào " + user.hoTen + ". Tổng quan hoạt động phòng khám.");
        add(GiaoDien.cuon(noiDung),BorderLayout.CENTER);
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
            cards.setPreferredSize(new Dimension(760,260));
            cards.setMaximumSize(new Dimension(Integer.MAX_VALUE,260));
            cards.add(the("Tổng bệnh nhân",benhNhan,GiaoDien.PRIMARY,
                    BieuTuong.Loai.TAI_KHOAN,"Hồ sơ lưu trong hệ thống"));
            cards.add(the("Lịch hẹn hôm nay",lich,GiaoDien.GREEN,
                    BieuTuong.Loai.THONG_BAO,"Theo ngày hiện tại"));
            cards.add(the("Lượt khám trong tháng",kham,new Color(0xF59E0B),
                    BieuTuong.Loai.KIEM_TRA,"Tính theo phiếu khám"));
            cards.add(the("Tổng hóa đơn",hoaDon,new Color(0x8B5CF6),
                    BieuTuong.Loai.BAO_CAO,"Tất cả trạng thái"));
            noiDung.add(cards);
            noiDung.add(Box.createVerticalStrut(18));

            JPanel history=GiaoDien.card(new BorderLayout(0,12));
            history.setAlignmentX(LEFT_ALIGNMENT);
            history.setPreferredSize(new Dimension(760,315));
            history.setMaximumSize(new Dimension(Integer.MAX_VALUE,315));
            history.add(GiaoDien.label("Lịch hẹn gần đây",14,true),BorderLayout.NORTH);
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

                System.err.println("Không tải được lịch hẹn Dashboard: " + e.getMessage());
            }
            JTable table=new JTable(model);
            table.setRowHeight(33);
            table.setFont(new Font("Segoe UI",Font.PLAIN,12));
            JComponent tableWrap=GiaoDien.wrapTable(table);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            history.add(tableWrap,BorderLayout.CENTER);
            noiDung.add(history);
            noiDung.add(Box.createVerticalStrut(16));
        }catch(Exception ex) {
            noiDung.add(GiaoDien.label("Không tải được thống kê SQL Server: "+ex.getMessage(),12,false));
        }
    }

    /** Hiển thị một chỉ số vận hành kèm mô tả và biểu tượng. */
    private JPanel the(String title,int value,Color accent,BieuTuong.Loai icon,String moTa) {
        JPanel card=GiaoDien.card(new BorderLayout(12,5));
        JPanel info=new JPanel();info.setOpaque(false);
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        JLabel label=GiaoDien.label(title,11,false);
        JLabel total=GiaoDien.label(String.format("%,d",value),25,true);
        JLabel sub=GiaoDien.label(moTa,10,false);
        info.add(label);info.add(Box.createVerticalStrut(11));
        info.add(total);info.add(Box.createVerticalStrut(9));info.add(sub);
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
