package UI.Chung;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Hỗ trợ chung: mọi nhân viên gửi yêu cầu, chỉ Admin có quyền HOTRO phản hồi.
 * Sử dụng bảng YeuCauHoTro tạo trong SQLData/04_NangCap_Quyen_HoTro_SQL2012.sql.
 */
public final class HoTroPage extends JPanel {
    private final QuanLyDAO dao;
    private final TaiKhoan user;
    private final boolean admin;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"STT","Người gửi","Thời điểm","Tiêu đề","Trạng thái","Phản hồi"},0) {
        @Override public boolean isCellEditable(int row,int column) { return false; }
    };
    private final JTable bang = new JTable(model);
    private List<QuanLyDAO.Hang> hang;

    public HoTroPage(QuanLyDAO dao,TaiKhoan user,boolean admin) {
        this.dao=dao;this.user=user;this.admin=admin;
        setLayout(new BorderLayout());setBackground(GiaoDien.BG);
        JPanel page=GiaoDien.trang("Trung tâm hỗ trợ",
                admin?"Tiếp nhận và phản hồi yêu cầu hỗ trợ từ các vai trò.":"Gửi yêu cầu hỗ trợ và xem phản hồi của Admin.");
        add(GiaoDien.cuon(page),BorderLayout.CENTER);
        JPanel actions=GiaoDien.hang();
        JButton send=GiaoDien.nutIcon("Gửi yêu cầu",true,BieuTuong.Loai.THEM);
        JButton refresh=GiaoDien.nutIcon("Tải lại",false,BieuTuong.Loai.TAI_LAI);
        send.addActionListener(e->gui());refresh.addActionListener(e->tai());
        actions.add(send);actions.add(refresh);
        JButton detail=GiaoDien.nutIcon("Xem nội dung",false,BieuTuong.Loai.TIM_KIEM);
        detail.addActionListener(e->xemNoiDung());
        actions.add(detail);
        if(admin) {
            JButton reply=GiaoDien.nutIcon("Phản hồi",false,BieuTuong.Loai.SUA);
            reply.addActionListener(e->phanHoi());actions.add(reply);
        }
        page.add(actions);
        page.add(Box.createVerticalStrut(14));
        JPanel card=GiaoDien.card(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(760,430));
        card.add(GiaoDien.wrapTable(bang),BorderLayout.CENTER);
        page.add(card);
        tai();
    }
    private void tai() {
        try {
            hang=dao.danhSachHoTro(user.id);
            model.setRowCount(0);
            int stt=1;
            for(QuanLyDAO.Hang h:hang) {
                Object[] row={stt++,h.cot[0],h.cot[1],h.cot[2],h.cot[3],h.cot[5]};
                model.addRow(row);
            }
        }catch(Exception ex){GiaoDien.loi(this,ex);}
    }
    private void gui() {
        JTextField title=GiaoDien.text("");
        JTextArea body=new JTextArea(5,28);
        JPanel form=new JPanel(new BorderLayout(0,8));
        JPanel top=new JPanel(new BorderLayout());top.add(new JLabel("Tiêu đề"),BorderLayout.NORTH);
        top.add(title,BorderLayout.CENTER);
        form.add(top,BorderLayout.NORTH);
        form.add(new JScrollPane(body),BorderLayout.CENTER);
        if(JOptionPane.showConfirmDialog(this,form,"Gửi yêu cầu hỗ trợ",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try {dao.guiHoTro(user.id,title.getText(),body.getText());tai();}
        catch(Exception ex){GiaoDien.loi(this,ex);}
    }
    private void xemNoiDung() {
        int v=bang.getSelectedRow();
        if(v<0){JOptionPane.showMessageDialog(this,"Hãy chọn một yêu cầu.");return;}
        QuanLyDAO.Hang h=hang.get(bang.convertRowIndexToModel(v));
        JTextArea text=new JTextArea(h.cot[4],8,35);
        text.setEditable(false); text.setLineWrap(true);text.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this,new JScrollPane(text),"Nội dung: "+h.cot[2],
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void phanHoi() {
        int v=bang.getSelectedRow();
        if(v<0){JOptionPane.showMessageDialog(this,"Hãy chọn yêu cầu.");return;}
        QuanLyDAO.Hang h=hang.get(bang.convertRowIndexToModel(v));
        JTextArea text=new JTextArea(5,30);
        int result=JOptionPane.showConfirmDialog(this,new JScrollPane(text),
                "Phản hồi: "+h.cot[2],JOptionPane.OK_CANCEL_OPTION);
        if(result!=JOptionPane.OK_OPTION)return;
        try{dao.phanHoiHoTro(h.id,text.getText(),user.id);tai();}
        catch(Exception ex){GiaoDien.loi(this,ex);}
    }
}
