package UI.LeTan;

import static UI.LeTan.BangDuLieu.boCuon;
import static UI.LeTan.BangDuLieu.datCot;
import static UI.LeTan.BangDuLieu.taoBang;
import static UI.LeTan.BangDuLieu.taoModel;
import static UI.LeTan.BangDuLieu.taoSorter;
import static UI.LeTan.GiaoDien.BACKGROUND;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY_DARK;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.datKichThuoc;
import static UI.LeTan.GiaoDien.label;
import static UI.LeTan.GiaoDien.sectionTitle;
import static UI.LeTan.GiaoDien.tinted;
import static UI.LeTan.ThanhTimKiem.cols;

import Class.HoaDon;
import Class.TaiKhoan;
import DAO.HoaDonDAO;
import UI.LeTan.ThanhTimKiem.TieuChi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Scrollable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * TRANG THANH TOÁN & HÓA ĐƠN.
 *
 * Hiển thị danh sách hóa đơn; chọn một hóa đơn (hoặc nhấp đúp) để xem trước đúng như bản in
 * rồi in ra máy in. Việc vẽ / in hóa đơn nằm ở {@link InHoaDon} và {@link XemTruocHoaDon};
 * việc đọc dữ liệu nằm ở {@link HoaDonDAO}.
 *
 * Gắn trang này vào mục "Thanh toán & Hóa đơn" của KhungChinh giống cách đã gắn TrangChuLeTan:
 * {@code new TrangThanhToanHoaDon(user)}.
 */
public final class TrangThanhToanHoaDon extends JPanel implements Scrollable {

    /** Trạng thái duy nhất được phép in (trùng giá trị QuanLyDAO ghi khi xác nhận thu). */
    private static final String DA_THANH_TOAN = "Đã thanh toán";

    private final TaiKhoan user;
    private final HoaDonDAO hoaDonDao = new HoaDonDAO();

    private final DefaultTableModel hoaDonModel;
    private final JTable hoaDonTable;
    private final TableRowSorter<DefaultTableModel> hoaDonSorter;
    private final NhanDem hoaDonCount = new NhanDem("0");
    private final JButton lamMoiButton = new NutBoGoc("Làm mới", NutBoGoc.MO);
    private final JButton inButton = new NutBoGoc("Xem & in hóa đơn", NutBoGoc.CHINH);

    private boolean loading;

    public TrangThanhToanHoaDon(TaiKhoan user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        // Cột: STT | Mã HĐ | Mã phiếu | Mã BN | Bệnh nhân | Ngày lập | Tổng tiền | Trạng thái
        hoaDonModel = taoModel("STT", "Mã HĐ", "Mã phiếu", "Mã BN", "Bệnh nhân",
                "Ngày lập", "Tổng tiền", "Trạng thái");
        hoaDonTable = taoBang(hoaDonModel);
        hoaDonSorter = taoSorter(hoaDonTable, hoaDonModel);
        caiDatCotBang();

        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 20, 24));
        page.add(banner(), BorderLayout.NORTH);
        page.add(khuHoaDon(), BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);

        lamMoiButton.addActionListener(event -> taiLaiAsync());
        inButton.addActionListener(event -> xemVaIn());
        hoaDonTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) xemVaIn();
            }
        });

        // Tránh kết nối SQL trên Event Dispatch Thread khi mở trang.
        taiLaiAsync();
    }

    // ================= Dựng giao diện =================

    private JPanel banner() {
        BangTieuDe outer = new BangTieuDe();

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(tinted(label("Tổng quan  ›  Thanh toán & Hóa đơn", 11, false), new Color(0xBAE6FD)));
        texts.add(Box.createVerticalStrut(8));
        texts.add(tinted(label("Thanh toán & Hóa đơn", 26, true), Color.WHITE));
        texts.add(Box.createVerticalStrut(6));
        texts.add(tinted(label("Xin chào, " + user.hoTen
                + "  ·  Chọn hóa đơn để xem trước và in", 12, false), new Color(0xE0F2FE)));
        outer.add(texts, BorderLayout.WEST);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(lamMoiButton);
        outer.add(actions, BorderLayout.EAST);
        return outer;
    }

    private KhungBoGoc khuHoaDon() {
        KhungBoGoc card = new KhungBoGoc(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(17, 18, 20, 18));

        // TieuChi(tên hiển thị, cột mã, cột văn bản, cột SĐT) - số cột tính theo model.
        TieuChi[] criteria = {
            new TieuChi("Tất cả", cols(1, 2, 3), cols(4, 5, 7), cols()),
            new TieuChi("Mã hóa đơn", cols(1), cols(), cols()),
            new TieuChi("Mã phiếu", cols(2), cols(), cols()),
            new TieuChi("Mã BN", cols(3), cols(), cols()),
            new TieuChi("Tên bệnh nhân", cols(), cols(4), cols()),
            new TieuChi("Ngày lập", cols(), cols(5), cols()),
            new TieuChi("Trạng thái", cols(), cols(7), cols())
        };

        datKichThuoc(inButton, 150, 28);
        inButton.setToolTipText("Xem trước và in hóa đơn đang chọn");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(inButton);
        actions.add(hoaDonCount);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Danh sách hóa đơn",
                "Chỉ in được hóa đơn đã thanh toán · Nhấp đúp để xem trước", actions), BorderLayout.NORTH);
        top.add(ThanhTimKiem.taoThanh(hoaDonSorter, criteria), BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(boCuon(hoaDonTable), BorderLayout.CENTER);
        card.add(label("Chọn hóa đơn đã thanh toán rồi bấm Xem & in hóa đơn · Bản xem trước giống hệt bản in.",
                11, false), BorderLayout.SOUTH);
        return card;
    }

    private void caiDatCotBang() {
        datCot(hoaDonTable, 0, new BoVeCanGiua(MUTED, false));
        datCot(hoaDonTable, 1, new BoVeCanGiua(PRIMARY_DARK, true));
        datCot(hoaDonTable, 2, new BoVeCanGiua(TEXT, false));
        datCot(hoaDonTable, 3, new BoVeCanGiua(TEXT, false));
        datCot(hoaDonTable, 4, new BoVeAvatar());
        datCot(hoaDonTable, 6, new BoVeTien(TEXT));
        datCot(hoaDonTable, 7, new BoVeNhan(false));
    }

    // ================= Tải danh sách =================

    private void taiLaiAsync() {
        if (loading) return;
        datDangTai(true);
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() throws Exception {
                return hoaDonDao.danhSach();
            }
            @Override protected void done() {
                try {
                    napDuLieu(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    baoLoi("Không tải được danh sách hóa đơn", e);
                } finally {
                    datDangTai(false);
                }
            }
        }.execute();
    }

    /** Đổ danh sách hóa đơn lên bảng (STT đánh lại từ 1). */
    private void napDuLieu(List<Object[]> rows) {
        hoaDonModel.setRowCount(0);
        int stt = 1;
        for (Object[] values : rows) {
            Object[] copy = values.clone();
            copy[0] = stt++;
            hoaDonModel.addRow(copy);
        }
        hoaDonCount.setText(String.valueOf(rows.size()));
    }

    // ================= Xem trước & in =================

    /** Đọc đầy đủ hóa đơn đang chọn ở luồng nền rồi mở hộp thoại xem trước / in. */
    private void xemVaIn() {
        int viewRow = hoaDonTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một hóa đơn trong bảng trước khi in.",
                    "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = hoaDonTable.convertRowIndexToModel(viewRow);
        int maHD = (Integer) hoaDonModel.getValueAt(modelRow, 1);
        String trangThai = String.valueOf(hoaDonModel.getValueAt(modelRow, 7));
        if (!DA_THANH_TOAN.equalsIgnoreCase(trangThai.trim())) {
            baoChuaThanhToan(maHD, trangThai);
            return;
        }

        inButton.setEnabled(false);
        new SwingWorker<HoaDon, Void>() {
            @Override protected HoaDon doInBackground() throws Exception {
                return hoaDonDao.timTheoMa(maHD);
            }
            @Override protected void done() {
                inButton.setEnabled(true);
                try {
                    HoaDon invoice = get();
                    if (invoice == null) {
                        JOptionPane.showMessageDialog(TrangThanhToanHoaDon.this,
                                "Không tìm thấy hóa đơn " + maHD + " (có thể đã bị xóa). Hãy bấm Làm mới.",
                                "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    if (!DA_THANH_TOAN.equalsIgnoreCase(invoice.trangThai == null ? "" : invoice.trangThai.trim())) {
                        // Trạng thái vừa đổi trong SQL sau khi danh sách được tải.
                        baoChuaThanhToan(maHD, invoice.trangThai);
                        taiLaiAsync();
                        return;
                    }
                    XemTruocHoaDon.hien(TrangThanhToanHoaDon.this, invoice, user.hoTen);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    baoLoi("Không đọc được hóa đơn", e);
                }
            }
        }.execute();
    }

    private void baoChuaThanhToan(int maHD, String trangThai) {
        JOptionPane.showMessageDialog(this, String.format("Hóa đơn HD%06d đang ở trạng thái \"%s\".\n"
                + "Chỉ in được hóa đơn đã thanh toán.", maHD, trangThai == null ? "" : trangThai),
                "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
    }

    private void datDangTai(boolean value) {
        loading = value;
        lamMoiButton.setEnabled(!value);
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void baoLoi(String tieuDe, ExecutionException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        JOptionPane.showMessageDialog(this, cause.getMessage(),
                "MediDent - " + tieuDe, JOptionPane.ERROR_MESSAGE);
    }

    // Trang luôn vừa khít khung nhìn (giống TrangChuLeTan), chỉ bảng có thanh cuộn riêng.

    @Override public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override public boolean getScrollableTracksViewportHeight() {
        return true;
    }
}
