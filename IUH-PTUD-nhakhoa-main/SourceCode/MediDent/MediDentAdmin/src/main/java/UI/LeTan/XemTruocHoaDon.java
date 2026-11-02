package UI.LeTan;

import static UI.LeTan.GiaoDien.font;
import static UI.LeTan.GiaoDien.khuRangCua;

import Class.HoaDon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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

/** Hộp thoại xem trước hóa đơn (đúng như bản in) kèm nút In. */
public final class XemTruocHoaDon {

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
