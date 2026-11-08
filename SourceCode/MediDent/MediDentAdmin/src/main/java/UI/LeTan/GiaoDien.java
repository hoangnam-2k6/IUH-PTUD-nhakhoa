package UI.LeTan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

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

    // ===================== Định dạng ngày giờ / tiền tệ =====================

    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter NGAY_NHAP =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter NGAY_GIO_NHAP =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate docNgay(String chuoi) {
        if (chuoi == null || chuoi.isBlank()) return null;
        return LocalDate.parse(chuoi.trim(), NGAY_NHAP);
    }

    public static LocalDateTime docNgayGio(String chuoi) {
        if (chuoi == null || chuoi.isBlank()) return null;
        return LocalDateTime.parse(chuoi.trim(), NGAY_GIO_NHAP);
    }

    public static String dinhDangNgay(LocalDate ngay) {
        return ngay == null ? "" : ngay.format(NGAY);
    }

    public static String dinhDangNgayGio(LocalDateTime ngayGio) {
        return ngayGio == null ? "" : ngayGio.format(NGAY_GIO);
    }

    /** Định dạng số tiền theo kiểu Việt Nam: 1.250.000. */
    public static String tien(BigDecimal soTien) {
        if (soTien == null) return "0";
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(soTien);
    }

    public static String tien(long soTien) {
        return tien(BigDecimal.valueOf(soTien));
    }

    /** Đọc chuỗi người dùng nhập (có thể chứa dấu chấm/phẩy) thành số tiền. */
    public static BigDecimal docTien(String chuoi) {
        if (chuoi == null) return BigDecimal.ZERO;
        String s = chuoi.replaceAll("[^0-9-]", "");
        if (s.isEmpty() || "-".equals(s)) return BigDecimal.ZERO;
        return new BigDecimal(s);
    }

    /** Ô nhập tiền có định dạng sẵn (chỉ nhận chữ số và dấu phân cách). */
    public static JTextField taoOTien() {
        JTextField o = taoOText();
        o.setHorizontalAlignment(SwingConstants.RIGHT);
        return o;
    }

    /** Hộp thoại thông báo thành công. */
    public static void thongBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Hộp thoại xác nhận Có/Không. */
    public static boolean xacNhan(Component cha, String noiDung, String tieuDe) {
        return JOptionPane.showConfirmDialog(cha, noiDung, tieuDe,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Hiển thị lỗi ngày giờ nhập sai thống nhất cho các màn hình. */
    public static void baoLoiNgay(Component cha, DateTimeParseException e, String dinhDang) {
        JOptionPane.showMessageDialog(cha, "Ngày/giờ không đúng định dạng " + dinhDang + ".",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    /** Tạo nhãn tiêu đề nhóm nhỏ (dùng trong các form). */
    public static JLabel taoTieuDeNhom(String chu) {
        JLabel l = new JLabel(chu.toUpperCase());
        l.setFont(font(Font.BOLD, 11));
        l.setForeground(MAU_CHINH);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return l;
    }
}
