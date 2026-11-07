package UI.Chung;
import UI.QuanLy.TrangChuPanel;
import UI.QuanLy.BaoCaoPanel;
import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;
/** Launcher riêng không yêu cầu đăng nhập; chỉ đọc dữ liệu SQL, không bỏ qua phân quyền CRUD. */
public final class XemTruoc {
    private XemTruoc(){
    }
    public static void open(String name,Supplier<JPanel> panel){
        SwingUtilities.invokeLater(()->{
            GiaoDien.setup();
            JFrame f=new JFrame("MEDIDENT | "+name+" • Xem độc lập");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1300,820);
            f.setMinimumSize(new Dimension(940,600));
            f.setLocationRelativeTo(null);
            JPanel page=panel.get();
            f.add(page);
            f.setVisible(true);
            if(page instanceof BangDuLieuPanel crud)crud.refresh();
            else if(page instanceof TrangChuPanel h)h.refresh();
            else if(page instanceof BaoCaoPanel b)b.refresh();
        }
        );
    }
}
