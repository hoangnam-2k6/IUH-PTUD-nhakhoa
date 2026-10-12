package UI.QuanTriVien;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Chỉ Admin duyệt/từ chối yêu cầu hủy phiếu thu, có ghi nhật ký SQL. */
public final class DuyetHuyPhieuThu extends JPanel {
    private final TaiKhoan user;
    private final QuanLyDAO dao;
    private List<QuanLyDAO.Hang> rows;
    private final DefaultTableModel model=new DefaultTableModel(
            new String[]{"STT","Thời điểm","Mã thanh toán","Lý do","Trạng thái"},0) {
        @Override public boolean isCellEditable(int row,int col) {return false;}
    };
    private final JTable table=new JTable(model);

    public DuyetHuyPhieuThu(QuanLyDAO dao,TaiKhoan user) {
        this.dao=dao;this.user=user;
        setLayout(new BorderLayout());setBackground(GiaoDien.BG);
        JPanel page=GiaoDien.trang("Duyệt hủy phiếu thu", "Kiểm tra yêu cầu do nhân viên gửi trước khi duyệt.");
        add(GiaoDien.cuon(page),BorderLayout.CENTER);
        JPanel tools=GiaoDien.hang();
        JButton approve=GiaoDien.nutIcon("Duyệt hủy",true,BieuTuong.Loai.KIEM_TRA);
        JButton reject=GiaoDien.nutIcon("Từ chối",false,BieuTuong.Loai.HUY);
        JButton reload=GiaoDien.nutIcon("Tải lại",false,BieuTuong.Loai.TAI_LAI);
        approve.addActionListener(e->xuLy(true));reject.addActionListener(e->xuLy(false));
        reload.addActionListener(e->tai());
        tools.add(approve);tools.add(reject);tools.add(reload);
        page.add(tools);page.add(Box.createVerticalStrut(15));
        JPanel card=GiaoDien.card(new BorderLayout());
        card.setAlignmentX(LEFT_ALIGNMENT);card.setPreferredSize(new Dimension(740,420));
        card.add(GiaoDien.wrapTable(table),BorderLayout.CENTER);
        page.add(card);
        tai();
    }
    private void tai() {
        try {
            rows=dao.yeuCauHuyDangCho();model.setRowCount(0);
            int i=1;
            for(QuanLyDAO.Hang r:rows)model.addRow(new Object[]{i++,r.cot[0],r.cot[1],r.cot[2],r.cot[3]});
        }catch(Exception ex){GiaoDien.loi(this,ex);}
    }
    private void xuLy(boolean approve) {
        int v=table.getSelectedRow();
        if(v<0){JOptionPane.showMessageDialog(this,"Hãy chọn yêu cầu hủy.");return;}
        QuanLyDAO.Hang r=rows.get(table.convertRowIndexToModel(v));
        String note=JOptionPane.showInputDialog(this,approve?"Lý do duyệt:":"Lý do từ chối:");
        if(note==null)return;
        if(JOptionPane.showConfirmDialog(this,"Xác nhận "+(approve?"duyệt":"từ chối")
                +" yêu cầu #"+r.id+"?", "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.duyetYeuCauHuy(r.id,approve,note,user.id);tai();}
        catch(Exception ex){GiaoDien.loi(this,ex);}
    }
}
