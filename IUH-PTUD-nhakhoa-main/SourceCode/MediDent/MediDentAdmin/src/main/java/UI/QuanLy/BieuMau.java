package UI.QuanLy;

import DAO.QuanLyDAO;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import javax.swing.*;

/** Form điền dữ liệu có nhãn rõ, các danh mục bắt buộc dùng JComboBox. */
final class BieuMau {
    private static final DateTimeFormatter VN=DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private BieuMau() {}

    static JPanel form() {
        JPanel form=new JPanel(new GridLayout(0,1,5,4));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        return form;
    }
    static JTextField text(String s) { return GiaoDien.text(s==null?"":s); }
    static void them(JPanel form,String label,JComponent component) {
        form.add(GiaoDien.label(label,11,true));
        form.add(component);
    }
    static JComboBox<String> trangThai(String... values) {
        return new JComboBox<>(values);
    }
    static JComboBox<QuanLyDAO.LuaChon> chon(List<QuanLyDAO.LuaChon> values) {
        JComboBox<QuanLyDAO.LuaChon> combo=new JComboBox<>();
        for(QuanLyDAO.LuaChon r:values)combo.addItem(r);
        return combo;
    }
    static boolean xacNhan(Component component,String title,JPanel form) {
        JScrollPane pane=new JScrollPane(form);
        pane.setBorder(BorderFactory.createLineBorder(GiaoDien.BORDER));
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setPreferredSize(new Dimension(550,Math.min(575,form.getComponentCount()*30+30)));
        return JOptionPane.showConfirmDialog(component,pane,title,JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION;
    }
    static LocalDate ngay(String date) {
        String s=date==null?"":date.trim();
        return s.isEmpty()?null:LocalDate.parse(s,VN);
    }
    static LocalTime gio(String time) { return LocalTime.parse(time.trim()); }
}
