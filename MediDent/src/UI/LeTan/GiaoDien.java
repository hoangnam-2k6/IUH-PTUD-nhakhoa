package UI.LeTan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Chứa màu sắc, font và các hàm tạo thành phần giao diện dùng chung
 * để các màn hình của Lễ tân có cùng một kiểu, không phải copy code.
 */
public final class GiaoDien {
    public static final Color MAU_CHINH      = Color.decode("#0284C7");
    public static final Color MAU_CHINH_DAM  = Color.decode("#0369A1");
    public static final Color MAU_CHINH_NHAT = Color.decode("#E0F2FE");
    public static final Color NEN_APP   = Color.decode("#F8FAFC");
    public static final Color NEN_THE   = Color.decode("#FFFFFF");
    public static final Color VIEN      = Color.decode("#E2E8F0");
    public static final Color CHU_CHINH = Color.decode("#0F172A");
    public static final Color CHU_PHU   = Color.decode("#64748B");
    public static final Color CANH_BAO  = Color.decode("#F59E0B");
    public static final Color THANH_CONG = Color.decode("#10B981");

    private GiaoDien() {}

    public static Font font(int kieu, int coChu) {
        return new Font("Segoe UI", kieu, coChu);
    }

    /** Nút nền màu, chữ trắng, đổi màu khi rê chuột. */
    public static JButton taoNut(String chu, Color nen, Color nenHover) {
        JButton nut = new JButton(chu);
        nut.setFont(font(Font.BOLD, 13));
        nut.setForeground(Color.WHITE);
        nut.setBackground(nen);
        nut.setFocusPainted(false);
        nut.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nut.setBackground(nenHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nut.setBackground(nen);
            }
        });
        return nut;
    }

    /** Nút nền trắng, viền xám (dùng cho "Nhập lại", "Đăng xuất"). */
    public static JButton taoNutTrang(String chu) {
        JButton nut = new JButton(chu);
        nut.setFont(font(Font.BOLD, 13));
        nut.setForeground(CHU_CHINH);
        nut.setBackground(NEN_THE);
        nut.setFocusPainted(false);
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nut.setBackground(NEN_APP);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nut.setBackground(NEN_THE);
            }
        });
        return nut;
    }

    /** Ô nhập chữ có viền xám và đệm hai bên. */
    public static JTextField taoOText() {
        JTextField o = new JTextField();
        o.setFont(font(Font.PLAIN, 14));
        o.setForeground(CHU_CHINH);
        o.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return o;
    }

    public static JTable taoBang(DefaultTableModel model) {
        JTable bang = new JTable(model);
        bang.setRowHeight(34);
        bang.setFont(font(Font.PLAIN, 13));
        bang.setForeground(CHU_CHINH);
        bang.setGridColor(VIEN);
        bang.setShowVerticalLines(false);
        bang.setFillsViewportHeight(true);
        bang.setSelectionBackground(MAU_CHINH_NHAT);
        bang.setSelectionForeground(CHU_CHINH);
        bang.getTableHeader().setFont(font(Font.BOLD, 13));
        bang.getTableHeader().setBackground(NEN_APP);
        bang.getTableHeader().setForeground(CHU_PHU);
        bang.getTableHeader().setReorderingAllowed(false);
        return bang;
    }

    /** Trang nền xám nhạt, cách lề 16px - mỗi màn hình bắt đầu từ đây. */
    public static JPanel taoTrang() {
        JPanel trang = new JPanel(new BorderLayout());
        trang.setBackground(NEN_APP);
        trang.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return trang;
    }

    /** Thẻ trắng có viền, phía trên là tiêu đề (và mô tả nhỏ nếu có). Nội dung thêm vào CENTER. */
    public static JPanel taoThe(String tieuDe, String moTa) {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(NEN_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JPanel dau = new JPanel(new GridLayout(0, 1, 0, 2));
        dau.setOpaque(false);
        dau.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel nhanTieuDe = new JLabel(tieuDe);
        nhanTieuDe.setFont(font(Font.BOLD, 16));
        nhanTieuDe.setForeground(CHU_CHINH);
        dau.add(nhanTieuDe);

        if (moTa != null) {
            JLabel nhanMoTa = new JLabel(moTa);
            nhanMoTa.setFont(font(Font.PLAIN, 12));
            nhanMoTa.setForeground(CHU_PHU);
            dau.add(nhanMoTa);
        }
        the.add(dau, BorderLayout.NORTH);
        return the;
    }

    /** Một ô nhập gồm nhãn đậm phía trên và ô nhập phía dưới. */
    public static JPanel truong(String nhan, JComponent oNhap) {
        JLabel l = new JLabel(nhan);
        l.setFont(font(Font.BOLD, 13));
        l.setForeground(CHU_CHINH);
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        p.add(l, BorderLayout.NORTH);
        p.add(oNhap, BorderLayout.CENTER);
        return p;
    }

    /** Ô nhập nhiều dòng (tự xuống dòng) có viền giống ô nhập thường. */
    public static JTextArea taoOVanBan(int soDong) {
        JTextArea o = new JTextArea(soDong, 20);
        o.setFont(font(Font.PLAIN, 14));
        o.setForeground(CHU_CHINH);
        o.setLineWrap(true);
        o.setWrapStyleWord(true);
        o.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return o;
    }

    /** Bọc ô nhiều dòng trong khung cuộn có viền xám. */
    public static JScrollPane boc(JTextArea o) {
        JScrollPane c = new JScrollPane(o);
        c.setBorder(BorderFactory.createLineBorder(VIEN));
        return c;
    }

    /** Hiện thông báo lỗi: lỗi nhập liệu -> cảnh báo, lỗi khác -> lỗi cơ sở dữ liệu. */
    public static void baoLoi(Component cha, Exception e) {
        if (e instanceof IllegalArgumentException) {
            JOptionPane.showMessageDialog(cha, e.getMessage(), "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(cha, "Lỗi cơ sở dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
