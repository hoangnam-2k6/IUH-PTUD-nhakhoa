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

    // ===================== Khung lọc sổ xuống =====================

    /** Bỏ dấu tiếng Việt và đưa về chữ thường để tìm kiếm không phân biệt dấu. */
    public static String boDau(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }

    /** Khung chứa nội dung, sổ xuống / thu lên mượt bằng cách đổi dần chiều cao. */
    public static final class KhungTruot extends JPanel {
        private final JComponent noiDung;
        private double tienDo = 0;      // 0 = đóng hẳn, 1 = mở hẳn
        private boolean mo;
        private javax.swing.Timer timer;

        public KhungTruot(JComponent noiDung) {
            super(new BorderLayout());
            this.noiDung = noiDung;
            setOpaque(false);
            add(noiDung, BorderLayout.NORTH);
            setVisible(false);
        }

        public boolean dangMo() {
            return mo;
        }

        public void datMo(boolean moMoi) {
            mo = moMoi;
            if (timer != null) timer.stop();
            if (mo) setVisible(true);
            timer = new javax.swing.Timer(12, e -> {
                tienDo += mo ? 0.12 : -0.12;
                boolean xong = tienDo >= 1 || tienDo <= 0;
                tienDo = Math.max(0, Math.min(1, tienDo));
                if (xong) {
                    timer.stop();
                    setVisible(tienDo > 0);
                }
                revalidate();
                repaint();
            });
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = noiDung.getPreferredSize();
            double t = tienDo * tienDo * (3 - 2 * tienDo);   // làm mượt đầu/cuối
            return new Dimension(d.width, (int) Math.round(d.height * t));
        }
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
            LocalDate chon = chonNgay(this, dangCo);
            if (chon != null) datNgay(chon);
        }
    }

    /** Mở hộp thoại lịch tháng; trả về ngày được chọn hoặc null nếu đóng. */
    public static LocalDate chonNgay(Component cha, LocalDate giaTri) {
        Window cuaSo = cha == null ? null : SwingUtilities.getWindowAncestor(cha);
        JDialog hop = new JDialog(cuaSo, "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);
        LichThang noiDung = new LichThang(giaTri == null ? LocalDate.now() : giaTri, hop);
        hop.setContentPane(noiDung);
        hop.pack();
        hop.setResizable(false);
        hop.setLocationRelativeTo(cha);
        hop.setVisible(true);
        return noiDung.daChon ? noiDung.ketQua : null;
    }

    private static class LichThang extends JPanel {
        private YearMonth thang;
        private LocalDate ketQua;
        private boolean daChon;
        private final JDialog hop;
        private final JLabel nhan = new JLabel("", SwingConstants.CENTER);
        private final JPanel luoi = new JPanel(new GridLayout(0, 7, 2, 2));

        LichThang(LocalDate ngay, JDialog hop) {
            super(new BorderLayout(0, 8));
            this.ketQua = ngay;
            this.thang = YearMonth.from(ngay);
            this.hop = hop;
            setBackground(NEN_THE);
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JButton truoc = taoNutNho("<");
            JButton sau = taoNutNho(">");
            truoc.addActionListener(e -> {
                thang = thang.minusMonths(1);
                veLai();
            });
            sau.addActionListener(e -> {
                thang = thang.plusMonths(1);
                veLai();
            });
            nhan.setFont(font(Font.BOLD, 14));
            nhan.setForeground(CHU_CHINH);

            JPanel dau = new JPanel(new BorderLayout(8, 0));
            dau.setOpaque(false);
            dau.add(truoc, BorderLayout.WEST);
            dau.add(nhan, BorderLayout.CENTER);
            dau.add(sau, BorderLayout.EAST);
            add(dau, BorderLayout.NORTH);

            JPanel giua = new JPanel(new BorderLayout(0, 4));
            giua.setOpaque(false);
            JPanel thu = new JPanel(new GridLayout(1, 7, 2, 2));
            thu.setOpaque(false);
            for (String ten : new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"}) {
                JLabel l = new JLabel(ten, SwingConstants.CENTER);
                l.setFont(font(Font.BOLD, 12));
                l.setForeground(CHU_PHU);
                thu.add(l);
            }
            giua.add(thu, BorderLayout.NORTH);
            giua.add(luoi, BorderLayout.CENTER);
            add(giua, BorderLayout.CENTER);

            JButton homNay = taoNutTrang("Hôm nay");
            homNay.addActionListener(e -> chon(LocalDate.now()));
            JPanel chan = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            chan.setOpaque(false);
            chan.add(homNay);
            add(chan, BorderLayout.SOUTH);

            veLai();
        }

        private void veLai() {
            nhan.setText("Tháng " + thang.getMonthValue() + " / " + thang.getYear());
            luoi.removeAll();
            int le = thang.atDay(1).getDayOfWeek().getValue() - 1; // tuần bắt đầu thứ Hai
            for (int i = 0; i < le; i++) luoi.add(new JLabel());
            for (int ngay = 1; ngay <= thang.lengthOfMonth(); ngay++) {
                LocalDate d = thang.atDay(ngay);
                JButton nut = new JButton(String.valueOf(ngay));
                nut.setFont(font(Font.PLAIN, 13));
                nut.setFocusPainted(false);
                nut.setMargin(new Insets(2, 2, 2, 2));
                nut.setOpaque(true);
                nut.setContentAreaFilled(true);
                nut.setBorder(BorderFactory.createLineBorder(VIEN));
                nut.setBackground(d.equals(ketQua) ? MAU_CHINH : NEN_THE);
                nut.setForeground(d.equals(ketQua) ? Color.WHITE : CHU_CHINH);
                nut.addActionListener(e -> chon(d));
                luoi.add(nut);
            }
            luoi.revalidate();
            luoi.repaint();
        }

        private void chon(LocalDate d) {
            ketQua = d;
            daChon = true;
            hop.dispose();
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
}