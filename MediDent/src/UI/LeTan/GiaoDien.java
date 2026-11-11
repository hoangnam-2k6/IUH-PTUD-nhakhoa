package UI.LeTan;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * Nút theo hai kiểu chính/phụ của hệ thống:
     * nền trắng, viền xám, bo góc; xanh khi nhấn.
     * Thông số lấy theo bộ giao diện dùng chung: Segoe UI Bold 12,
     * đệm 10/14, bo góc 10, kích thước 135 x 37.
     */
    public static JButton taoNut(String chu, boolean chinh) {
        JButton nut = new JButton(chu) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g.setColor(VIEN);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension kichThuoc = super.getPreferredSize();
                return new Dimension(Math.max(135, kichThuoc.width), 37);
            }
        };
        nut.setFont(font(Font.BOLD, 12));
        nut.setBackground(chinh ? MAU_CHINH : NEN_THE);
        nut.setForeground(chinh ? Color.WHITE : CHU_CHINH);
        nut.setContentAreaFilled(false);
        nut.setOpaque(false);
        nut.setFocusPainted(false);
        nut.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nut.setBackground(chinh ? MAU_CHINH_DAM : NEN_APP);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nut.setBackground(chinh ? MAU_CHINH : NEN_THE);
                nut.setForeground(chinh ? Color.WHITE : CHU_CHINH);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                nut.setBackground(MAU_CHINH);
                nut.setForeground(Color.WHITE);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                nut.setBackground(nut.getMousePosition() != null
                        ? (chinh ? MAU_CHINH_DAM : NEN_APP)
                        : (chinh ? MAU_CHINH : NEN_THE));
                nut.setForeground(chinh || nut.getMousePosition() != null
                        ? Color.WHITE : CHU_CHINH);
            }
        });
        return nut;
    }

    /** Nút nền trắng, viền xám (dùng cho "Nhập lại", "Đăng xuất"). */
    public static JButton taoNutTrang(String chu) {
        return taoNut(chu, false);
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

        // Nội dung mọi ô dùng chung một cách căn lề: khối chữ của cột nằm giữa ô
        // và chữ ở các dòng trong cùng cột luôn bắt đầu tại cùng một vị trí.
        bang.setDefaultRenderer(Object.class, new OCoBanRenderer());

        DefaultTableCellRenderer tieuDe = new DefaultTableCellRenderer();
        tieuDe.setHorizontalAlignment(SwingConstants.CENTER);
        tieuDe.setVerticalAlignment(SwingConstants.CENTER);
        tieuDe.setFont(font(Font.BOLD, 13));
        tieuDe.setBackground(NEN_APP);
        tieuDe.setForeground(CHU_PHU);
        tieuDe.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        bang.getTableHeader().setDefaultRenderer(tieuDe);
        bang.getTableHeader().setReorderingAllowed(false);

        // Dữ liệu đổi thì tính lại độ rộng chữ lớn nhất của từng cột.
        model.addTableModelListener(e -> bang.putClientProperty(KHOA_DO_RONG_COT, null));
        return bang;
    }

    /** Khoảng đệm tối thiểu hai bên chữ khi cột quá hẹp. */
    private static final int LE_TOI_THIEU = 2;
    private static final String KHOA_DO_RONG_COT = "giaoDien.doRongChuLonNhat";

    /**
     * Renderer chung cho mọi ô dữ liệu của bảng.
     * Chữ được căn giữa theo chiều dọc; theo chiều ngang, cả khối chữ của cột
     * được đặt giữa ô nên chữ đầu hàng ở mọi dòng trong cùng cột thẳng hàng nhau.
     */
    public static class OCoBanRenderer extends DefaultTableCellRenderer {
        public OCoBanRenderer() {
            setVerticalAlignment(SwingConstants.CENTER);
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Trừ khoảng cách giữa các ô để phần chữ không bị cắt mất vài pixel.
            int doRongCot = table.getColumnModel().getColumn(column).getWidth()
                    - table.getIntercellSpacing().width * 2;
            int doRongChu = doRongChuLonNhat(table, column);
            // Khối chữ nằm giữa ô; nếu cột quá hẹp thì giảm đệm để không cắt chữ.
            int le = Math.max(LE_TOI_THIEU, (doRongCot - doRongChu) / 2);
            setBorder(BorderFactory.createEmptyBorder(0, le, 0, le));
            return c;
        }

        /** Độ rộng (pixel) của chuỗi dài nhất trong một cột, có nhớ để không đo lại mỗi lần vẽ. */
        private static int doRongChuLonNhat(JTable table, int column) {
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> boNho = (Map<Integer, Integer>) table.getClientProperty(KHOA_DO_RONG_COT);
            if (boNho == null) {
                boNho = new HashMap<>();
                table.putClientProperty(KHOA_DO_RONG_COT, boNho);
            }
            Integer daTinh = boNho.get(column);
            if (daTinh != null) return daTinh;

            FontMetrics fm = table.getFontMetrics(table.getFont());
            int rongNhat = 0;
            for (int dong = 0; dong < table.getRowCount(); dong++) {
                Object giaTri = table.getValueAt(dong, column);
                String chu = giaTri == null ? "" : giaTri.toString();
                rongNhat = Math.max(rongNhat, fm.stringWidth(chu));
            }
            boNho.put(column, rongNhat);
            return rongNhat;
        }
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

    /** Ô nhập tiền chỉ nhận chữ số và dấu phân cách, không cho nhập chữ. */
    public static JTextField taoOTien() {
        JTextField o = taoOText();
        o.setHorizontalAlignment(SwingConstants.RIGHT);
        chiNhapSoTien(o);
        return o;
    }

    /** Gắn bộ lọc chỉ cho nhập chữ số và dấu chấm/phẩy vào một ô bất kỳ. */
    public static void chiNhapSoTien(JTextField o) {
        if (o.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) o.getDocument()).setDocumentFilter(new ChiNhapChuSo());
        }
    }

    private static class ChiNhapChuSo extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            thay(fb, offset, 0, text);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                throws BadLocationException {
            thay(fb, offset, length, text);
        }

        private void thay(FilterBypass fb, int offset, int length, String text) throws BadLocationException {
            String cu = fb.getDocument().getText(0, fb.getDocument().getLength());
            String moi = cu.substring(0, offset) + (text == null ? "" : text)
                    + cu.substring(offset + length);
            if (moi.length() <= 18 && moi.matches("[0-9.,]*")) {
                fb.replace(offset, length, text, null);
            }
        }
    }

    /** Họ tên chỉ gồm chữ cái (có dấu tiếng Việt) và khoảng trắng - không số, không ký hiệu. */
    public static boolean kiemTraHoTen(String chuoi) {
        return chuoi != null && chuoi.trim().matches("[\\p{L}\\p{M} ]+");
    }

    /** Gắn bộ lọc chỉ cho nhập chữ cái và khoảng trắng (dùng cho ô họ tên). */
    public static void chiNhapChu(JTextField o) {
        if (o.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) o.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                        throws BadLocationException {
                    if (text != null && text.matches("[\\p{L}\\p{M} ]*")) fb.insertString(offset, text, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                        throws BadLocationException {
                    if (text == null || text.matches("[\\p{L}\\p{M} ]*")) fb.replace(offset, length, text, attr);
                }
            });
        }
    }

    /** Gắn bộ lọc chỉ cho nhập chữ số và dấu / vào ô ngày. */
    private static void chiNhapNgay(JTextField o) {
        if (o.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) o.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                        throws BadLocationException {
                    thay(fb, offset, 0, text);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                        throws BadLocationException {
                    thay(fb, offset, length, text);
                }

                private void thay(FilterBypass fb, int offset, int length, String text)
                        throws BadLocationException {
                    String cu = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String moi = cu.substring(0, offset) + (text == null ? "" : text)
                            + cu.substring(offset + length);
                    if (moi.length() <= 10 && moi.matches("[0-9/]*")) {
                        fb.replace(offset, length, text, null);
                    }
                }
            });
        }
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

    /** Nút nhỏ, nền xanh nhạt dùng cạnh ô nhập (ví dụ nút mở lịch). */
    public static JButton taoNutNho(String chu) {
        JButton nut = new JButton(chu);
        nut.setFont(font(Font.BOLD, 12));
        nut.setForeground(MAU_CHINH_DAM);
        nut.setBackground(MAU_CHINH_NHAT);
        nut.setFocusPainted(false);
        nut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        nut.setContentAreaFilled(false);
        nut.setOpaque(true);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                nut.setBackground(Color.decode("#BAE6FD"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nut.setBackground(MAU_CHINH_NHAT);
            }
        });
        return nut;
    }

    // ===================== Ô nhập ngày có lịch =====================

    /**
     * Ô nhập ngày dd/MM/yyyy kèm nút mở lịch tháng.
     * Dùng cho ngày sinh, ngày hẹn, ngày tái khám... để người dùng không phải
     * nhớ định dạng và tránh nhập sai ngày.
     */
    public static class ONgay extends JPanel {
        private final JTextField o = taoOText();
        private final JButton nutLich;

        public ONgay() {
            this(null);
        }

        public ONgay(LocalDate giaTriBanDau) {
            super(new BorderLayout(6, 0));
            setOpaque(false);
            o.setToolTipText("Nhập ngày theo dạng dd/MM/yyyy hoặc bấm nút để chọn trên lịch");
            chiNhapNgay(o);
            nutLich = taoNutNho("Lịch");
            nutLich.addActionListener(e -> moLich());
            add(o, BorderLayout.CENTER);
            add(nutLich, BorderLayout.EAST);
            if (giaTriBanDau != null) datNgay(giaTriBanDau);
        }

        public JTextField oNhap() {
            return o;
        }

        public String getText() {
            return o.getText();
        }

        public void setText(String chu) {
            o.setText(chu);
        }

        public void datNgay(LocalDate ngay) {
            o.setText(dinhDangNgay(ngay));
        }

        /** Đọc ngày đang nhập; ném DateTimeParseException nếu sai định dạng. */
        public LocalDate layNgay() {
            return docNgay(o.getText());
        }

        public void focus() {
            o.requestFocusInWindow();
            o.selectAll();
        }

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            o.setEnabled(b);
            nutLich.setEnabled(b);
        }

        private void moLich() {
            LocalDate dangCo;
            try {
                dangCo = layNgay();
            } catch (DateTimeParseException e) {
                dangCo = null;
            }
            JPopupMenu popup = new JPopupMenu();
            LichThang lich = new LichThang(dangCo == null ? LocalDate.now() : dangCo, ngay -> {
                datNgay(ngay);
                popup.setVisible(false);
            }, () -> popup.setVisible(false));
            popup.setBorder(BorderFactory.createLineBorder(VIEN));
            popup.setLayout(new BorderLayout());
            popup.add(lich, BorderLayout.CENTER);
            popup.setPopupSize(lich.getPreferredSize());
            // Neo lịch vào toàn bộ ô ngày để bảng xổ xuống ngay bên dưới,
            // canh mép phải với nút "Lịch" nằm bên phải ô nhập.
            int x = Math.max(0, getWidth() - lich.getPreferredSize().width);
            popup.show(this, x, getHeight());
        }
    }

    /**
     * Lịch tháng dạng popup, hiển thị ngay bên dưới nút Lịch.
     *
     * Chọn tháng / năm KHÔNG dùng JComboBox nữa: JComboBox mở thêm một popup riêng
     * nằm bên trong JPopupMenu của lịch, nên popup cha cướp sự kiện chuột và bảng
     * chỉ hiện ra mà không chọn hay cuộn được. Thay vào đó, bấm vào "Tháng"/"Năm"
     * sẽ đổi vùng giữa của lịch sang bảng chọn tháng hoặc danh sách năm (có thanh
     * cuộn + lăn chuột), tất cả nằm trong cùng một panel nên luôn nhận được chuột.
     */
    private static class LichThang extends JPanel {
        private static final int NAM_MAX = 2026; // chỉ cho chọn năm từ 2026 trở về trước
        private static final int NAM_MIN = 1900;
        private static final int NGAY = 0, THANG = 1, NAM = 2;
        private static final int RONG_HANG = 31; // chiều cao 1 hàng nút (27 + khoảng cách 4)

        private YearMonth thang;
        private LocalDate ngayDangChon;
        private final java.util.function.Consumer<LocalDate> khiChon;
        private final Runnable khiHuy;
        private int cheDo = NGAY;

        private final CardLayout the = new CardLayout();
        private final JPanel vungGiua = new JPanel(the);
        private final JPanel luoi = new JPanel(new GridLayout(0, 7, 3, 3));
        private final JPanel luoiThang = new JPanel(new GridLayout(3, 4, 4, 4));
        private final JPanel luoiNam = new JPanel(new GridLayout(0, 4, 3, 4));
        private final JScrollPane cuonNam = new JScrollPane(luoiNam,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        private final JButton nutThang = taoNutNho("");
        private final JButton nutNam = taoNutNho("");

        LichThang(LocalDate ngay, java.util.function.Consumer<LocalDate> khiChon, Runnable khiHuy) {
            super(new BorderLayout(0, 7));
            this.thang = YearMonth.from(ngay);
            this.ngayDangChon = ngay;
            this.khiChon = khiChon;
            this.khiHuy = khiHuy;
            setBackground(NEN_THE);
            setBorder(BorderFactory.createEmptyBorder(9, 9, 8, 9));
            setPreferredSize(new Dimension(286, 275));

            JButton truoc = taoNutNho("‹");
            JButton sau = taoNutNho("›");
            truoc.setPreferredSize(new Dimension(30, 28));
            sau.setPreferredSize(new Dimension(30, 28));
            truoc.addActionListener(e -> chuyen(-1));
            sau.addActionListener(e -> chuyen(1));

            nutThang.setFont(font(Font.BOLD, 11));
            nutNam.setFont(font(Font.BOLD, 11));
            nutThang.setPreferredSize(new Dimension(92, 28));
            nutNam.setPreferredSize(new Dimension(76, 28));
            nutThang.setToolTipText("Bấm để chọn tháng");
            nutNam.setToolTipText("Bấm để chọn năm");
            nutThang.addActionListener(e -> datCheDo(cheDo == THANG ? NGAY : THANG));
            nutNam.addActionListener(e -> datCheDo(cheDo == NAM ? NGAY : NAM));

            JPanel chonThangNam = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
            chonThangNam.setOpaque(false);
            chonThangNam.add(truoc);
            chonThangNam.add(nutThang);
            chonThangNam.add(nutNam);
            chonThangNam.add(sau);
            add(chonThangNam, BorderLayout.NORTH);

            // --- Thẻ 1: lưới ngày ---
            JPanel ngayPanel = new JPanel(new BorderLayout(0, 4));
            ngayPanel.setOpaque(false);
            JPanel thu = new JPanel(new GridLayout(1, 7, 3, 3));
            thu.setOpaque(false);
            for (String ten : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel l = new JLabel(ten, SwingConstants.CENTER);
                l.setFont(font(Font.BOLD, 10));
                l.setForeground(CHU_PHU);
                thu.add(l);
            }
            luoi.setOpaque(false);
            ngayPanel.add(thu, BorderLayout.NORTH);
            ngayPanel.add(luoi, BorderLayout.CENTER);

            // --- Thẻ 2: bảng 12 tháng ---
            luoiThang.setOpaque(false);
            JPanel thangPanel = new JPanel(new BorderLayout());
            thangPanel.setOpaque(false);
            thangPanel.add(luoiThang, BorderLayout.CENTER);

            // --- Thẻ 3: danh sách năm có thanh cuộn ---
            luoiNam.setOpaque(false);
            luoiNam.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
            cuonNam.setBorder(BorderFactory.createLineBorder(VIEN));
            cuonNam.getViewport().setBackground(NEN_THE);
            cuonNam.getVerticalScrollBar().setUnitIncrement(RONG_HANG);
            cuonNam.getVerticalScrollBar().setBlockIncrement(RONG_HANG * 4);

            vungGiua.setOpaque(false);
            vungGiua.add(ngayPanel, "ngay");
            vungGiua.add(thangPanel, "thang");
            vungGiua.add(cuonNam, "nam");
            add(vungGiua, BorderLayout.CENTER);

            JButton huy = taoNutNho("Hủy");
            JButton chon = taoNut("Chọn", true);
            huy.setPreferredSize(new Dimension(76, 29));
            chon.setPreferredSize(new Dimension(82, 29));
            huy.addActionListener(e -> khiHuy.run());
            chon.addActionListener(e -> khiChon.accept(ngayDangChon));
            JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            chan.setOpaque(false);
            chan.add(huy);
            chan.add(chon);
            add(chan, BorderLayout.SOUTH);

            veLai();
        }

        /** Nút ‹ › : đổi tháng (lưới ngày), đổi năm (bảng tháng) hoặc cuộn trang (danh sách năm). */
        private void chuyen(int huong) {
            if (cheDo == NGAY) {
                YearMonth moi = thang.plusMonths(huong);
                if (moi.getYear() > NAM_MAX || moi.getYear() < NAM_MIN) return;
                doiThang(moi);
            } else if (cheDo == THANG) {
                int nam = thang.getYear() + huong;
                if (nam > NAM_MAX || nam < NAM_MIN) return;
                thang = YearMonth.of(nam, thang.getMonthValue());
                veLai();
            } else {
                JScrollBar bar = cuonNam.getVerticalScrollBar();
                bar.setValue(bar.getValue() + huong * bar.getBlockIncrement());
            }
        }

        private void datCheDo(int moi) {
            cheDo = moi;
            the.show(vungGiua, moi == THANG ? "thang" : moi == NAM ? "nam" : "ngay");
            veLai();
            if (moi == NAM) cuonToiNamDangChon();
        }

        private void doiThang(YearMonth moi) {
            if (moi.getYear() > NAM_MAX || moi.getYear() < NAM_MIN) return;
            thang = moi;
            veLai();
        }

        /** Tạo một nút lưới (ngày / tháng / năm) với kiểu chọn thống nhất. */
        private JButton nutLuoi(String chu, boolean chon, int cao, Runnable khiBam) {
            JButton nut = new JButton(chu);
            nut.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            nut.setFont(font(Font.BOLD, 11));
            nut.setPreferredSize(new Dimension(30, cao));
            nut.setMinimumSize(new Dimension(30, cao));
            nut.setMargin(new Insets(1, 1, 1, 1));
            nut.setFocusPainted(false);
            nut.setOpaque(true);
            nut.setContentAreaFilled(true);
            nut.setBorderPainted(true);
            nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nut.setBorder(BorderFactory.createLineBorder(chon ? MAU_CHINH : VIEN, chon ? 2 : 1));
            nut.setBackground(chon ? MAU_CHINH : Color.WHITE);
            nut.setForeground(chon ? Color.WHITE : CHU_CHINH);
            nut.setText(chu);
            nut.addActionListener(e -> khiBam.run());
            return nut;
        }

        private void veLai() {
            nutThang.setText("Tháng " + thang.getMonthValue() + " ▾");
            nutNam.setText(thang.getYear() + " ▾");

            // Lưới ngày
            luoi.removeAll();
            int le = thang.atDay(1).getDayOfWeek().getValue() - 1;
            for (int i = 0; i < le; i++) luoi.add(new JLabel(""));
            for (int ngay = 1; ngay <= thang.lengthOfMonth(); ngay++) {
                LocalDate d = thang.atDay(ngay);
                luoi.add(nutLuoi(Integer.toString(ngay), d.equals(ngayDangChon), 27, () -> {
                    ngayDangChon = d;
                    veLai();
                }));
            }

            // Bảng 12 tháng
            luoiThang.removeAll();
            for (int m = 1; m <= 12; m++) {
                final int thangChon = m;
                luoiThang.add(nutLuoi("Tháng " + m, m == thang.getMonthValue(), 40, () -> {
                    thang = YearMonth.of(thang.getYear(), thangChon);
                    datCheDo(NGAY);
                }));
            }

            // Danh sách năm (mới nhất ở trên)
            luoiNam.removeAll();
            for (int nam = NAM_MAX; nam >= NAM_MIN; nam--) {
                final int namChon = nam;
                luoiNam.add(nutLuoi(Integer.toString(nam), nam == thang.getYear(), 27, () -> {
                    thang = YearMonth.of(namChon, thang.getMonthValue());
                    datCheDo(NGAY);
                }));
            }

            luoi.revalidate();
            luoiThang.revalidate();
            luoiNam.revalidate();
            revalidate();
            repaint();
        }

        /** Cuộn danh sách năm để năm đang chọn nằm gần giữa khung nhìn. */
        private void cuonToiNamDangChon() {
            int hang = (NAM_MAX - thang.getYear()) / 4;
            SwingUtilities.invokeLater(() -> {
                JViewport vp = cuonNam.getViewport();
                int dich = Math.max(0, hang * RONG_HANG - RONG_HANG * 2);
                int toiDa = Math.max(0, luoiNam.getPreferredSize().height - vp.getHeight());
                vp.setViewPosition(new Point(0, Math.min(dich, toiDa)));
            });
        }
    }

    // ===================== Kiểm tra nhập liệu =====================

    /** Lấy chữ đang có trong ô nhập (ô chữ, ô nhiều dòng, ô chọn, ô ngày). */
    public static String giaTri(JComponent o) {
        if (o instanceof ONgay) return ((ONgay) o).getText().trim();
        if (o instanceof JTextComponent) return ((JTextComponent) o).getText().trim();
        if (o instanceof JComboBox<?>) {
            Object v = ((JComboBox<?>) o).getSelectedItem();
            return v == null ? "" : v.toString().trim();
        }
        return "";
    }

    /** Đưa con trỏ vào đúng ô nhập và chọn sẵn nội dung để sửa ngay. */
    public static void focus(JComponent o) {
        if (o instanceof ONgay) {
            ((ONgay) o).focus();
        } else if (o instanceof JTextComponent) {
            o.requestFocusInWindow();
            ((JTextComponent) o).selectAll();
        } else {
            o.requestFocusInWindow();
        }
    }

    /**
     * Kiểm tra một ô bắt buộc nhập.
     * Thiếu dữ liệu thì hiện thông báo riêng, đưa con trỏ về đúng ô và trả về false.
     */
    public static boolean batBuoc(Component cha, JComponent o, String tenTruong) {
        if (giaTri(o).isEmpty()) {
            JOptionPane.showMessageDialog(cha, "Vui lòng nhập " + tenTruong + ".",
                    "Thiếu thông tin bắt buộc", JOptionPane.WARNING_MESSAGE);
            focus(o);
            return false;
        }
        return true;
    }

    /**
     * Báo lỗi định dạng riêng (tách khỏi thông báo thiếu thông tin),
     * đồng thời đưa con trỏ về đúng ô. Luôn trả về false để dùng cho tiện.
     */
    public static boolean sai(Component cha, JComponent o, String thongBao) {
        JOptionPane.showMessageDialog(cha, thongBao, "Dữ liệu chưa hợp lệ",
                JOptionPane.WARNING_MESSAGE);
        focus(o);
        return false;
    }

    /** Số điện thoại Việt Nam: bắt đầu bằng 0, gồm 10 hoặc 11 chữ số. */
    public static boolean kiemTraSoDienThoai(String chuoi) {
        if (chuoi == null) return false;
        String so = chuoi.replaceAll("[\\s.\\-()]", "");
        return so.matches("0\\d{9,10}");
    }

    /** CCCD 12 chữ số hoặc CMND cũ 9 chữ số. */
    public static boolean kiemTraCccd(String chuoi) {
        if (chuoi == null) return false;
        String so = chuoi.replaceAll("[\\s.\\-]", "");
        return so.matches("\\d{9}|\\d{12}");
    }

    /** Ngày sinh hợp lệ: có giá trị, không ở tương lai và không quá cũ. */
    public static boolean kiemTraNgaySinh(LocalDate ngay) {
        return ngay != null
                && !ngay.isAfter(LocalDate.now())
                && ngay.isAfter(LocalDate.of(1900, 1, 1));
    }

    // ===================== Thành phần cho khung bộ lọc =====================

    /** Ô tìm kiếm: biểu tượng kính lúp bên trái, chữ gợi ý mờ khi trống, viền đổi màu khi đang nhập. */
    public static class OTimKiem extends JTextField {
        private String goiY = "";

        public OTimKiem() {
            setFont(font(Font.PLAIN, 14));
            setForeground(CHU_CHINH);
            setCaretColor(CHU_CHINH);
            datVien(false);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    datVien(true);
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    datVien(false);
                }
            });
        }

        private void datVien(boolean dangFocus) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(dangFocus ? MAU_CHINH : VIEN),
                    BorderFactory.createEmptyBorder(7, 32, 7, 10)));
        }

        /** Chữ gợi ý hiển thị mờ khi ô trống. */
        public void datGoiY(String goiY) {
            this.goiY = goiY == null ? "" : goiY;
            setToolTipText(this.goiY.isEmpty() ? null : this.goiY);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Kính lúp
            int cy = getHeight() / 2;
            g2.setColor(CHU_PHU);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(11, cy - 7, 10, 10);
            g2.drawLine(20, cy + 2, 24, cy + 6);
            // Chữ gợi ý
            if (getText().isEmpty() && !goiY.isEmpty()) {
                g2.setFont(font(Font.PLAIN, 13));
                g2.setColor(Color.decode("#94A3B8"));
                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(fm.getStringBounds(goiY, g2).getWidth() > getWidth() - x - 8
                        ? cat(goiY, fm, getWidth() - x - 8) : goiY, x, y);
            }
            g2.dispose();
        }

        private static String cat(String s, FontMetrics fm, int rong) {
            String r = s;
            while (r.length() > 1 && fm.stringWidth(r + "…") > rong) r = r.substring(0, r.length() - 1);
            return r + "…";
        }
    }

    /** Đưa ô chọn (JComboBox) về cùng phong cách với ô nhập: nền trắng, viền xám, mũi tên gọn. */
    public static void kieuCombo(JComboBox<?> cb) {
        cb.setFont(font(Font.PLAIN, 13));
        cb.setForeground(CHU_CHINH);
        cb.setBackground(NEN_THE);
        cb.setOpaque(true);
        cb.setFocusable(true);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(BorderFactory.createLineBorder(VIEN));
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(NEN_THE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(CHU_PHU);
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                        g2.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(28, 20));
                return b;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(NEN_THE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            protected javax.swing.plaf.basic.ComboPopup createPopup() {
                javax.swing.plaf.basic.BasicComboPopup p = new javax.swing.plaf.basic.BasicComboPopup(comboBox);
                p.setBorder(BorderFactory.createLineBorder(MAU_CHINH));
                return p;
            }
        });
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
                l.setFont(font(Font.PLAIN, 13));
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                l.setForeground(CHU_CHINH);
                l.setBackground(selected ? MAU_CHINH_NHAT : NEN_THE);
                return l;
            }
        });
    }
}
