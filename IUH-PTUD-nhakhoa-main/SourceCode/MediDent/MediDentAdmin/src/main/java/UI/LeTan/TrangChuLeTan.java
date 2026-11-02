package UI.LeTan;

import static UI.LeTan.GiaoDien.BACKGROUND;
import static UI.LeTan.GiaoDien.DANGER;
import static UI.LeTan.GiaoDien.GREEN;
import static UI.LeTan.GiaoDien.WARNING;
import static UI.LeTan.GiaoDien.label;
import static UI.LeTan.GiaoDien.tinted;

import Class.DuLieuTrangLeTan;
import Class.TaiKhoan;
import DAO.KhoDuLieu;
import DAO.LeTanDAO;
import DAO.TiepNhanDAO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * TRANG CHỦ LỄ TÂN - MEDIDENT.
 *
 * Trang chỉ ghép các thành phần và điều phối việc tải dữ liệu; mỗi phần việc nằm ở một class:
 * - {@link ThongKeLeTan}   : bốn thẻ thống kê.
 * - {@link KhuKhamDieuTri} : bảng phiếu khám, tìm kiếm, xem chi tiết.
 * - {@link KhuTiepNhan}    : form tiếp nhận, cấp số, chỉ định phòng.
 * - {@link LeTanDAO}       : đọc dữ liệu / kiểm tra quyền.
 * - {@link TiepNhanDAO}    : giao dịch SQL tiếp nhận.
 * - các class còn lại cùng package : các thành phần vẽ riêng (nút, ô nhập, bảng, thanh cuộn...).
 *
 * Khung header / sidebar nằm ở UI.Chung.KhungChinh, không tạo JFrame mới.
 * Cần chạy SQLData/06_TiepNhanLeTan_SQL2012.sql MỘT LẦN trước khi dùng.
 *
 * Quyền:
 * - BENHNHAN: xem phiếu khám và tạo / dùng lại hồ sơ bệnh nhân khi tiếp nhận.
 * - LICHHEN: tiếp nhận và cấp số khám (kiểm tra lại trong transaction SQL).
 */
public final class TrangChuLeTan extends JPanel implements Scrollable {

    private final TaiKhoan user;
    private final LeTanDAO leTanDao;

    private final ThongKeLeTan thongKe = new ThongKeLeTan();
    private final KhuKhamDieuTri khuKham;
    private final KhuTiepNhan khuTiepNhan;
    private final JButton refreshButton = new NutBoGoc("Làm mới", NutBoGoc.MO);

    private boolean loading;      // đang tải / đang tiếp nhận thì khóa giao diện
    private boolean canReceive;   // có được phép tiếp nhận hay không

    public TrangChuLeTan(KhoDuLieu db, TaiKhoan user) {
        this.user = user;
        this.leTanDao = new LeTanDAO(db, user);
        this.khuKham = new KhuKhamDieuTri();
        this.khuTiepNhan = new KhuTiepNhan(new TiepNhanDAO(user), new KhuTiepNhan.BoLangNghe() {
            @Override public void dangXuLy(boolean on) { setLoading(on); }
            @Override public void trangThai(String text, Color dot) { setStatus(text, dot); }
            @Override public void loi(Throwable error) { showError(error); }
            @Override public void canTaiLai() { refreshAsync(); }
        });

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 24, 20, 24));
        add(page, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 16));
        upper.setOpaque(false);
        upper.add(banner(), BorderLayout.NORTH);
        upper.add(thongKe, BorderLayout.CENTER);
        page.add(upper, BorderLayout.NORTH);
        page.add(splitPanel(), BorderLayout.CENTER);

        refreshButton.addActionListener(event -> refreshAsync());

        // Tránh kết nối SQL trên Event Dispatch Thread khi mở trang.
        refreshAsync();
    }

    // Trang luôn vừa khít khung nhìn, không cuộn cả trang (chỉ từng bảng có thanh cuộn riêng).
    // Nếu KhungChinh bọc trang trong JScrollPane thì các hàm dưới đây buộc trang
    // co giãn đúng bằng khung nhìn.

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

    // ================= Dựng giao diện =================

    /** Banner đầu trang: đường dẫn, tiêu đề, lời chào và nút Làm mới. */
    private JPanel banner() {
        BangTieuDe outer = new BangTieuDe();

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(tinted(label("Tổng quan  ›  Trang chủ Lễ tân", 11, false), new Color(0xBAE6FD)));
        texts.add(Box.createVerticalStrut(8));
        texts.add(tinted(label("Tiếp nhận & Khám điều trị", 26, true), Color.WHITE));
        texts.add(Box.createVerticalStrut(6));
        texts.add(tinted(label("Xin chào, " + user.hoTen + "  ·  Hôm nay "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + "  ·  Dữ liệu cập nhật từ SQL Server", 12, false), new Color(0xE0F2FE)));
        outer.add(texts, BorderLayout.WEST);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(refreshButton);
        outer.add(actions, BorderLayout.EAST);
        return outer;
    }

    /** Khung chia đôi: Khám & điều trị | Tiếp nhận. */
    private JSplitPane splitPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, khuKham, khuTiepNhan);
        // Thanh chia chỉ là khoảng trống cùng màu nền, bỏ viền và nút mũi tên mặc định.
        split.setUI(new ThanhChiaUI());
        split.setResizeWeight(0.50);
        split.setContinuousLayout(true);
        split.setDividerSize(14);
        split.setBorder(null);
        split.setBackground(BACKGROUND);
        split.setOneTouchExpandable(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.50));
        return split;
    }

    // ================= Tải dữ liệu =================

    /** Tải dữ liệu ở luồng nền (SwingWorker), xong thì đổ lên giao diện. */
    private void refreshAsync() {
        if (loading) return;
        setLoading(true);
        setStatus("Đang tải dữ liệu...", WARNING);
        new SwingWorker<DuLieuTrangLeTan, Void>() {
            @Override protected DuLieuTrangLeTan doInBackground() throws Exception {
                return leTanDao.taiDuLieu();
            }
            @Override protected void done() {
                try {
                    hienThiDuLieu(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError(e);
                } catch (ExecutionException e) {
                    showError(e.getCause() == null ? e : e.getCause());
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private void hienThiDuLieu(DuLieuTrangLeTan data) {
        canReceive = data.receiveAllowed;
        khuKham.napDuLieu(data.examRows);
        thongKe.capNhat(data);
        if (!data.viewAllowed) {
            setStatus("Không có quyền BENHNHAN — danh sách đã được ẩn", DANGER);
        } else if (!data.receiveAllowed) {
            setStatus("Tài khoản không có quyền tiếp nhận (LICHHEN) — form đã bị khóa", WARNING);
        } else {
            setStatus("Đã đồng bộ SQL Server · Hôm nay: " + LocalDate.now(), GREEN);
        }
    }

    /** Khóa / mở giao diện khi đang tải. */
    private void setLoading(boolean value) {
        loading = value;
        refreshButton.setEnabled(!value);
        khuTiepNhan.datChoPhepNhap(!value && canReceive);
        setCursor(value ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void setStatus(String text, Color dot) {
        khuTiepNhan.datTrangThai(text, dot);
    }

    private void showError(Throwable error) {
        setStatus("Không tải/lưu được dữ liệu SQL Server.", DANGER);
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "MediDent - Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }
}
