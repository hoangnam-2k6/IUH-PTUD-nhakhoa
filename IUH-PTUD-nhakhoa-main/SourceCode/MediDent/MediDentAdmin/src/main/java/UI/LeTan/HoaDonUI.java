package UI.LeTan;

import Class.ChiTietHoaDon;

import Class.HoaDon;


import java.awt.BasicStroke;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.FlowLayout;

import java.awt.Font;

import java.awt.FontMetrics;

import java.awt.Graphics2D;

import java.awt.Graphics;

import java.awt.Window;

import java.awt.print.PageFormat;

import java.awt.print.Paper;

import java.awt.print.Printable;

import java.awt.print.PrinterException;

import java.awt.print.PrinterJob;

import java.math.BigDecimal;

import java.math.RoundingMode;

import java.text.NumberFormat;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.List;

import java.util.Locale;


import java.util.concurrent.ExecutionException;


import javax.swing.BorderFactory;

import javax.swing.Box;

import javax.swing.BoxLayout;

import javax.swing.JButton;

import javax.swing.JComponent;

import javax.swing.JDialog;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.ScrollPaneConstants;

import javax.swing.SwingUtilities;

import javax.swing.SwingWorker;

import javax.swing.border.EmptyBorder;


import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.khuRangCua;



/**
 * Vẽ và in hóa đơn trên giấy A4 (đen trắng, in rõ cả trên máy in kim / laser đơn sắc).
 *
 * Một nơi vẽ duy nhất ({@link #vePage}) phục vụ cả hai việc: xem trước trên màn hình
 * và in thật qua {@link Printable#print}, nên bản xem trước luôn giống bản in.
 * Hóa đơn dài sẽ tự sang trang; tổng cộng và chỗ ký nằm ở trang cuối.
 */
final class InHoaDon implements Printable {

    // Thông tin phòng khám in ở đầu hóa đơn - sửa lại cho đúng thực tế.
    public static final String TEN_PHONG_KHAM = "PHÒNG KHÁM NHA KHOA MEDIDENT";
    public static final String DIA_CHI = "Địa chỉ: (cập nhật địa chỉ phòng khám)";
    public static final String LIEN_HE = "Điện thoại: (cập nhật số điện thoại)";

    private static final Color INK = Color.BLACK;
    private static final Color GREY = new Color(0x555555);
    private static final Color LINE = new Color(0xBBBBBB);
    private static final Color HEAD_BG = new Color(0xEEEEEE);

    private static final double HEADER_H = 205;      // đầu trang 1: tên phòng khám, tiêu đề, thông tin bệnh nhân
    private static final double CONT_H = 36;         // đầu các trang tiếp theo
    private static final double TABLE_HEAD_H = 26;
    private static final double ROW_H = 24;
    private static final double FOOTER_H = 175;      // tổng cộng + chỗ ký
    private static final double PAGE_NO_H = 20;      // dòng "Trang x/y" ở chân giấy

    // Tỉ lệ độ rộng các cột: STT | Dịch vụ | SL | Đơn giá | Thành tiền
    private static final double[] COL = {0.07, 0.43, 0.08, 0.21, 0.21};

    private static final Locale VI = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HoaDon hoaDon;
    private final String nguoiLap;

    public InHoaDon(HoaDon hoaDon, String nguoiLap) {
        this.hoaDon = hoaDon;
        this.nguoiLap = nguoiLap == null ? "" : nguoiLap;
    }

    public HoaDon hoaDon() {
        return hoaDon;
    }

    // ---------- In thật ----------

    @Override public int print(Graphics g, PageFormat pf, int pageIndex) {
        List<int[]> pages = chiaTrang(pf.getImageableHeight());
        if (pageIndex >= pages.size()) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        vePage(g2, pageIndex, pf.getImageableWidth(), pf.getImageableHeight());
        return PAGE_EXISTS;
    }

    /** Số trang cần để in hết hóa đơn với vùng in cao {@code height}. */
    public int soTrang(double height) {
        return chiaTrang(height).size();
    }

    /** Chia các dòng dịch vụ vào từng trang: mỗi phần tử là {dòng đầu, dòng cuối (không gồm)}. */
    private List<int[]> chiaTrang(double height) {
        double usable = height - PAGE_NO_H;
        List<int[]> pages = new ArrayList<>();
        int total = hoaDon.chiTiet.size();
        int start = 0;
        boolean first = true;
        while (true) {
            double avail = usable - (first ? HEADER_H : CONT_H) - TABLE_HEAD_H;
            int capacity = Math.max(1, (int) (avail / ROW_H));
            int end = Math.min(total, start + capacity);
            pages.add(new int[] {start, end});
            if (end == total) {
                // Hết dòng: nếu trang này không còn chỗ cho tổng cộng + chữ ký thì thêm một trang cuối.
                if (avail - (end - start) * ROW_H < FOOTER_H) pages.add(new int[] {end, end});
                return pages;
            }
            start = end;
            first = false;
        }
    }

    // ---------- Vẽ một trang (dùng chung cho xem trước và in) ----------

    /**
     * Vẽ trang {@code page} (tính từ 0) vào vùng rộng {@code w}, cao {@code h}, gốc tọa độ ở góc trên trái.
     */
    public void vePage(Graphics2D graphics, int page, double w, double h) {
        List<int[]> pages = chiaTrang(h);
        if (page < 0 || page >= pages.size()) return;
        int[] range = pages.get(page);
        boolean first = page == 0;
        boolean last = page == pages.size() - 1;

        Graphics2D g2 = (Graphics2D) graphics.create();
        khuRangCua(g2);
        double y;
        if (first) {
            y = veDauTrang(g2, w);
        } else {
            g2.setColor(GREY);
            g2.setFont(font(Font.ITALIC, 10));
            g2.drawString(TEN_PHONG_KHAM + " - Hóa đơn " + soHoaDon() + " (tiếp theo)", 0, 14);
            y = CONT_H;
        }

        if (first || range[1] > range[0]) y = veTieuDeBang(g2, w, y);
        for (int i = range[0]; i < range[1]; i++) {
            y = veDong(g2, w, y, i + 1, hoaDon.chiTiet.get(i));
        }
        if (last) veChanTrang(g2, w, y);

        g2.setColor(GREY);
        g2.setFont(font(Font.PLAIN, 9));
        String pageText = "Trang " + (page + 1) + "/" + pages.size();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pageText, (float) (w - fm.stringWidth(pageText)) / 2f, (float) h - 4);
        g2.dispose();
    }

    private String soHoaDon() {
        return String.format("HD%06d", hoaDon.maHD);
    }

    /** Tên phòng khám, tiêu đề và khối thông tin bệnh nhân. Trả về tọa độ y ngay dưới khối này. */
    private double veDauTrang(Graphics2D g2, double w) {
        g2.setColor(INK);
        g2.setFont(font(Font.BOLD, 15));
        giuaDong(g2, TEN_PHONG_KHAM, w, 18);
        g2.setColor(GREY);
        g2.setFont(font(Font.PLAIN, 10));
        giuaDong(g2, DIA_CHI, w, 34);
        giuaDong(g2, LIEN_HE, w, 48);
        g2.setColor(LINE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, 60, (int) w, 60);

        g2.setColor(INK);
        g2.setFont(font(Font.BOLD, 20));
        giuaDong(g2, "HÓA ĐƠN THANH TOÁN", w, 92);
        g2.setFont(font(Font.PLAIN, 11));
        String ngay = hoaDon.ngayLap == null ? "—" : hoaDon.ngayLap.format(NGAY_GIO);
        giuaDong(g2, "Số: " + soHoaDon() + "     Ngày lập: " + ngay, w, 112);

        double right = w * 0.52;
        dongThongTin(g2, 0, 142, "Bệnh nhân:", hoaDon.tenBenhNhan + " (Mã BN " + hoaDon.maBN + ")", right - 10);
        dongThongTin(g2, 0, 162, "Điện thoại:", hoaDon.sdt, right - 10);
        dongThongTin(g2, 0, 182, "Trạng thái:", hoaDon.trangThai, right - 10);
        dongThongTin(g2, right, 142, "Bác sĩ:", hoaDon.tenBacSi, w - right);
        dongThongTin(g2, right, 162, "Mã phiếu khám:", String.valueOf(hoaDon.maPK), w - right);
        return HEADER_H;
    }

    /** Một dòng "nhãn: giá trị" trong khối thông tin, giá trị tự rút gọn nếu quá dài. */
    private void dongThongTin(Graphics2D g2, double x, double y, String nhan, String giaTri, double maxWidth) {
        g2.setFont(font(Font.BOLD, 11));
        g2.setColor(INK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(nhan, (float) x, (float) y);
        double labelWidth = fm.stringWidth(nhan) + 6;
        g2.setFont(font(Font.PLAIN, 11));
        g2.drawString(rutGon(giaTri == null || giaTri.isBlank() ? "—" : giaTri, g2.getFontMetrics(),
                (int) (maxWidth - labelWidth)), (float) (x + labelWidth), (float) y);
    }

    private double veTieuDeBang(Graphics2D g2, double w, double y) {
        g2.setColor(HEAD_BG);
        g2.fillRect(0, (int) y, (int) w, (int) TABLE_HEAD_H);
        g2.setColor(INK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(0, (int) y, (int) w, (int) y);
        g2.drawLine(0, (int) (y + TABLE_HEAD_H), (int) w, (int) (y + TABLE_HEAD_H));
        g2.setFont(font(Font.BOLD, 10));
        double base = y + 17;
        double[] x = cotX(w);
        giuaO(g2, "STT", x[0], x[1], base);
        g2.drawString("Dịch vụ", (float) x[1] + 6, (float) base);
        giuaO(g2, "SL", x[2], x[3], base);
        phaiO(g2, "Đơn giá", x[4] - 6, base);
        phaiO(g2, "Thành tiền", x[5] - 6, base);
        return y + TABLE_HEAD_H;
    }

    private double veDong(Graphics2D g2, double w, double y, int stt, ChiTietHoaDon line) {
        double[] x = cotX(w);
        double base = y + 16;
        g2.setColor(INK);
        g2.setFont(font(Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        giuaO(g2, String.valueOf(stt), x[0], x[1], base);
        g2.drawString(rutGon(line.tenDichVu, fm, (int) (x[2] - x[1] - 12)), (float) x[1] + 6, (float) base);
        giuaO(g2, String.valueOf(line.soLuong), x[2], x[3], base);
        phaiO(g2, tien(line.donGia), x[4] - 6, base);
        phaiO(g2, tien(line.thanhTien()), x[5] - 6, base);
        g2.setColor(LINE);
        g2.drawLine(0, (int) (y + ROW_H), (int) w, (int) (y + ROW_H));
        return y + ROW_H;
    }

    /** Dòng tổng cộng và hai ô chữ ký ở cuối trang cuối. */
    private void veChanTrang(Graphics2D g2, double w, double y) {
        y += 8;
        g2.setColor(INK);
        g2.setFont(font(Font.BOLD, 13));
        g2.drawString("TỔNG CỘNG:", (float) (w * 0.50), (float) (y + 16));
        phaiO(g2, tien(hoaDon.tongTien()), w - 6, y + 16);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawLine((int) (w * 0.50), (int) (y + 26), (int) w, (int) (y + 26));

        double sign = y + 60;
        g2.setFont(font(Font.BOLD, 11));
        giuaO(g2, "Người lập hóa đơn", 0, w / 2, sign);
        giuaO(g2, "Bệnh nhân", w / 2, w, sign);
        g2.setFont(font(Font.ITALIC, 10));
        g2.setColor(GREY);
        giuaO(g2, "(Ký, ghi rõ họ tên)", 0, w / 2, sign + 14);
        giuaO(g2, "(Ký, ghi rõ họ tên)", w / 2, w, sign + 14);
        g2.setColor(INK);
        g2.setFont(font(Font.PLAIN, 11));
        giuaO(g2, nguoiLap, 0, w / 2, sign + 80);
        g2.setColor(GREY);
        g2.setFont(font(Font.ITALIC, 9));
        giuaO(g2, "Cảm ơn quý khách đã tin tưởng sử dụng dịch vụ!", 0, w, sign + 105);
    }

    // ---------- Hàm vẽ chữ nhỏ ----------

    /** Ranh giới 6 cột: x[0]..x[5]. */
    private static double[] cotX(double w) {
        double[] x = new double[COL.length + 1];
        for (int i = 0; i < COL.length; i++) x[i + 1] = x[i] + COL[i] * w;
        return x;
    }

    private static void giuaDong(Graphics2D g2, String text, double w, double y) {
        giuaO(g2, text, 0, w, y);
    }

    private static void giuaO(Graphics2D g2, String text, double left, double right, double y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (float) (left + (right - left - fm.stringWidth(text)) / 2), (float) y);
    }

    private static void phaiO(Graphics2D g2, String text, double right, double y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (float) (right - fm.stringWidth(text)), (float) y);
    }

    /** Cắt bớt chữ và thêm "…" nếu rộng hơn {@code maxWidth}. */
    private static String rutGon(String text, FontMetrics fm, int maxWidth) {
        if (text == null) return "";
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "…";
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) end--;
        return text.substring(0, end) + ellipsis;
    }

    /** 1234567 -> "1.234.567 đ". */
    public static String tien(BigDecimal value) {
        NumberFormat format = NumberFormat.getIntegerInstance(VI);
        return format.format(value.setScale(0, RoundingMode.HALF_UP)) + " đ";
    }
}

/** Hộp thoại xem trước hóa đơn (đúng như bản in) kèm nút In. */
final class XemTruocHoaDon {

    private static final double RONG = 595.2;   // A4 theo điểm (1/72 inch)
    private static final double CAO = 841.8;
    private static final double LE = 36;        // lề 0,5 inch

    private XemTruocHoaDon() { }

    /** Khổ giấy A4 dọc, lề 0,5 inch - dùng cho cả xem trước lẫn in thật. */
    public static PageFormat trangA4() {
        Paper paper = new Paper();
        paper.setSize(RONG, CAO);
        paper.setImageableArea(LE, LE, RONG - 2 * LE, CAO - 2 * LE);
        PageFormat format = new PageFormat();
        format.setPaper(paper);
        format.setOrientation(PageFormat.PORTRAIT);
        return format;
    }

    /** Mở hộp thoại xem trước; nút "In hóa đơn" mở hộp thoại chọn máy in của hệ điều hành. */
    public static void hien(Component parent, HoaDon hoaDon, String nguoiLap) {
        InHoaDon printable = new InHoaDon(hoaDon, nguoiLap);
        PageFormat a4 = trangA4();
        int pageCount = printable.soTrang(a4.getImageableHeight());

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, String.format("Xem trước hóa đơn HD%06d", hoaDon.maHD),
                JDialog.ModalityType.APPLICATION_MODAL);

        JPanel pages = new JPanel();
        pages.setLayout(new BoxLayout(pages, BoxLayout.Y_AXIS));
        pages.setBackground(new Color(0xCBD5E1));
        pages.setBorder(new EmptyBorder(16, 16, 16, 16));
        for (int i = 0; i < pageCount; i++) {
            TrangXem page = new TrangXem(printable, i, a4);
            page.setAlignmentX(Component.CENTER_ALIGNMENT);
            pages.add(page);
            pages.add(Box.createVerticalStrut(16));
        }

        JScrollPane scroll = new JScrollPane(pages);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        JButton closeButton = new NutBoGoc("Đóng", NutBoGoc.PHU);
        JButton printButton = new NutBoGoc("In hóa đơn", NutBoGoc.CHINH);
        closeButton.setPreferredSize(new Dimension(90, 38));
        printButton.setPreferredSize(new Dimension(130, 38));
        JLabel hint = new JLabel("Khổ A4 · " + pageCount + " trang");
        hint.setFont(font(Font.PLAIN, 11));
        hint.setForeground(new Color(0x64748B));

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE2E8F0)),
                new EmptyBorder(10, 16, 10, 16)));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(closeButton);
        right.add(printButton);
        buttons.add(hint, BorderLayout.WEST);
        buttons.add(right, BorderLayout.EAST);

        closeButton.addActionListener(event -> dialog.dispose());
        printButton.addActionListener(event -> in(dialog, printButton, printable, a4));

        dialog.setLayout(new BorderLayout());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setSize(680, 780);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    /** Hộp thoại chọn máy in chạy ở luồng giao diện; lệnh in chạy nền để không đứng màn hình. */
    private static void in(JDialog dialog, JButton printButton, InHoaDon printable, PageFormat a4) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(String.format("Hoa don HD%06d", printable.hoaDon().maHD));
        job.setPrintable(printable, a4);
        if (!job.printDialog()) return;

        printButton.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws PrinterException {
                job.print();
                return null;
            }
            @Override protected void done() {
                printButton.setEnabled(true);
                try {
                    get();
                    JOptionPane.showMessageDialog(dialog, "Đã gửi hóa đơn tới máy in.",
                            "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    JOptionPane.showMessageDialog(dialog, "Không in được: " + cause.getMessage(),
                            "In hóa đơn", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Một tờ A4 trắng trên nền xám, vẽ lại đúng nội dung sẽ in. */
    private static final class TrangXem extends JComponent {
        private final InHoaDon printable;
        private final int page;
        private final PageFormat format;

        TrangXem(InHoaDon printable, int page, PageFormat format) {
            this.printable = printable;
            this.page = page;
            this.format = format;
            Dimension size = new Dimension((int) format.getWidth(), (int) format.getHeight());
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            khuRangCua(g2);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0x94A3B8));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g2.translate(format.getImageableX(), format.getImageableY());
            printable.vePage(g2, page, format.getImageableWidth(), format.getImageableHeight());
            g2.dispose();
        }
    }
}
