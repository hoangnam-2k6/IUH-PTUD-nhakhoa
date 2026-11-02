package UI.QuanLy;

import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.*;

/** Bảng nghiệp vụ dùng chung, không tạo JFrame/Sidebar mới. */
abstract class VanHanhPage extends JPanel {
    @FunctionalInterface
    protected interface TaiDuLieu {
        List<QuanLyDAO.Hang> tai() throws Exception;
    }

    @FunctionalInterface
    protected interface HanhDong {
        void chay() throws Exception;
    }

    protected final QuanLyDAO dao;
    protected final JTable table;
    protected final DefaultTableModel model;

    private final JLabel thongBao;
    private final JPanel nut;
    private final JPanel tableCard;
    private final Map<Integer, QuanLyDAO.Hang> hangTheoMa = new HashMap<>();
    private final List<JButton> nutChucNang = new ArrayList<>();
    private final JButton nutTaiLai;

    private TaiDuLieu nguon;
    private SwingWorker<List<QuanLyDAO.Hang>, Void> workerTaiDuLieu;
    private long lanTaiDuLieu;
    private boolean dangTaiDuLieu;

    protected VanHanhPage(QuanLyDAO dao, String title, String description, String... columns) {
        this.dao = dao;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel page = GiaoDien.trang(title, description);
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        // Thanh công cụ chia thành hai hàng để không chồng ô tìm kiếm và nút.
        JPanel toolbar = GiaoDien.card(new BorderLayout(0, 10));
        toolbar.setAlignmentX(LEFT_ALIGNMENT);

        JTextField search = GiaoDien.text("");
        search.setToolTipText("Tìm trong các cột của bảng");
        search.setPreferredSize(new Dimension(250, 38));
        JPanel searchPanel = new JPanel(new BorderLayout(0, 4));
        searchPanel.setOpaque(false);
        searchPanel.add(GiaoDien.label("Tìm nhanh", 11, true), BorderLayout.NORTH);
        searchPanel.add(search, BorderLayout.CENTER);
        searchPanel.setPreferredSize(new Dimension(280, 58));
        toolbar.add(searchPanel, BorderLayout.NORTH);

        // Các nút nằm trên một hàng có thể cuộn ngang, không bị cắt ở cửa sổ hẹp.
        nut = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nut.setOpaque(false);
        JScrollPane actionScroll = new JScrollPane(
                nut,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        actionScroll.setBorder(BorderFactory.createEmptyBorder());
        actionScroll.setOpaque(false);
        actionScroll.getViewport().setOpaque(false);
        actionScroll.getHorizontalScrollBar().setUnitIncrement(24);
        actionScroll.setPreferredSize(new Dimension(760, 58));
        actionScroll.setMinimumSize(new Dimension(100, 58));
        toolbar.add(actionScroll, BorderLayout.CENTER);
        page.add(toolbar);
        page.add(Box.createVerticalStrut(14));

        String[] headers = new String[columns.length + 1];
        headers[0] = "STT";
        System.arraycopy(columns, 0, headers, 1, columns.length);
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        // STT đi theo dòng đang nhìn thấy, không trùng với ID thật trong SQL.
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component component = super.prepareRenderer(renderer, row, col);
                if (col == 0 && component instanceof JLabel) {
                    ((JLabel) component).setText(String.valueOf(row + 1));
                    ((JLabel) component).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return component;
            }
        };
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getColumnModel().getColumn(0).setPreferredWidth(54);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(165);
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        sorter.setSortable(0, false);
        table.setRowSorter(sorter);

        tableCard = GiaoDien.card(new BorderLayout(0, 10));
        tableCard.setAlignmentX(LEFT_ALIGNMENT);
        tableCard.setPreferredSize(new Dimension(750, 600));
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scroll, BorderLayout.CENTER);
        thongBao = GiaoDien.label("Chưa tải dữ liệu", 11, false);
        tableCard.add(thongBao, BorderLayout.SOUTH);
        page.add(tableCard);

        search.getDocument().addDocumentListener(SharedListeners.document(() -> {
            String query = search.getText().trim();
            // Chỉ tìm trên cột hiển thị, không lọc theo mã SQL nằm sau cột STT.
            int[] cotTim = new int[model.getColumnCount() - 1];
            for (int i = 0; i < cotTim.length; i++) cotTim[i] = i + 1;
            sorter.setRowFilter(query.isEmpty() ? null
                    : RowFilter.regexFilter("(?iu)" + Pattern.quote(query), cotTim));
            capNhatSoDong();
        }));

        nutTaiLai = GiaoDien.nutIcon("Tải lại", false, BieuTuong.Loai.TAI_LAI);
        nutTaiLai.addActionListener(e -> taiLai());
        nut.add(nutTaiLai);
    }

    protected void themNut(String text, boolean primary,
                           BieuTuong.Loai icon, HanhDong action) {
        JButton button = GiaoDien.nutIcon(text, primary, icon);
        button.addActionListener(e -> thucHien(action));
        button.setEnabled(!dangTaiDuLieu);
        nutChucNang.add(button);
        nut.add(button);
        nut.revalidate();
    }

    /**
     * Dùng cho các thao tác giao diện/biểu mẫu chạy đồng bộ.
     * Không chuyển toàn bộ action sang luồng nền vì action có thể mở Swing dialog
     * hoặc đọc/ghi Swing component; riêng các lần tải dữ liệu đã dùng taiLai().
     */
    protected void thucHien(HanhDong action) {
        try {
            action.chay();
        } catch (Exception e) {
            GiaoDien.loi(this, e);
        }
    }

    protected void nap(TaiDuLieu data) {
        nguon = data;
        taiLai();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Khi quay lại màn hình, tải lại từ SQL thay vì giữ bảng cũ.
        if (nguon != null) taiLai();
    }

    /**
     * Nạp dữ liệu SQL ở luồng nền để không khóa Event Dispatch Thread (EDT).
     * Toàn bộ cập nhật JTable, model và trạng thái nút được thực hiện trong done()
     * trên EDT theo quy tắc của Swing.
     */
    protected void taiLai() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::taiLai);
            return;
        }
        if (nguon == null) {
            return;
        }

        // Tránh chạy đồng thời nhiều lần tải dữ liệu cho cùng một màn hình.
        if (workerTaiDuLieu != null && !workerTaiDuLieu.isDone()) {
            return;
        }

        final TaiDuLieu nguonHienTai = nguon;
        final long idLanTai = ++lanTaiDuLieu;
        setDangTaiDuLieu(true);
        thongBao.setText("Đang tải dữ liệu từ máy chủ...");

        workerTaiDuLieu = new SwingWorker<List<QuanLyDAO.Hang>, Void>() {
            @Override
            protected List<QuanLyDAO.Hang> doInBackground() throws Exception {
                List<QuanLyDAO.Hang> rows = nguonHienTai.tai();
                return rows == null ? Collections.emptyList() : rows;
            }

            @Override
            protected void done() {
                // Bảo vệ trước kết quả cũ nếu sau này có cơ chế tải lại nối tiếp.
                if (idLanTai != lanTaiDuLieu || workerTaiDuLieu != this) {
                    return;
                }

                workerTaiDuLieu = null;
                try {
                    List<QuanLyDAO.Hang> rows = get();
                    capNhatBang(rows);
                    thongBao.setText("Tải thành công " + rows.size() + " dòng");
                } catch (CancellationException e) {
                    thongBao.setText("Đã hủy tải dữ liệu");
                } catch (InterruptedException e) {
                    // done() chạy trên EDT; không đặt cờ interrupt lên luồng giao diện.
                    thongBao.setText("Quá trình tải dữ liệu bị gián đoạn");
                    GiaoDien.loi(VanHanhPage.this, e);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    thongBao.setText("Không thể tải dữ liệu. Vui lòng thử tải lại.");
                    GiaoDien.loi(VanHanhPage.this,
                            cause instanceof Exception
                                    ? (Exception) cause
                                    : new Exception("Lỗi khi tải dữ liệu", cause));
                } finally {
                    setDangTaiDuLieu(false);
                }
            }
        };

        workerTaiDuLieu.execute();
    }

    /** Cập nhật model/table hoàn toàn trên EDT sau khi truy vấn nền hoàn tất. */
    private void capNhatBang(List<QuanLyDAO.Hang> rows) {
        hangTheoMa.clear();
        model.setRowCount(0);

        for (QuanLyDAO.Hang row : rows) {
            Object[] values = new Object[row.cot.length + 1];
            // Cột STT trong model giữ mã thật (PK); renderer vẫn hiện 1, 2, 3...
            values[0] = row.id;
            hangTheoMa.put(row.id, row);
            System.arraycopy(row.cot, 0, values, 1, row.cot.length);
            model.addRow(values);
        }

        capNhatSoDong();

        // Nhiều bản ghi -> bảng cao hơn, trang bên ngoài có thể cuộn dọc.
        int height = Math.min(880, Math.max(470, 95 + model.getRowCount() * 42));
        tableCard.setPreferredSize(new Dimension(1000, height));
        tableCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        tableCard.revalidate();
        tableCard.repaint();
    }

    private void setDangTaiDuLieu(boolean loading) {
        dangTaiDuLieu = loading;
        nutTaiLai.setEnabled(!loading);
        for (JButton button : nutChucNang) {
            button.setEnabled(!loading);
        }
        setCursor(loading
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
        revalidate();
        repaint();
    }

    private void capNhatSoDong() {
        thongBao.setText("Đang hiển thị " + table.getRowCount()
                + " / " + model.getRowCount() + " dòng");
    }


    /** Chỉ gọi cho bảng ít cột: trải đều các cột để sử dụng hết chiều rộng khung. */
    protected void bangVuaKhung() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(i == 1 ? 240 : 175);
        }
        tableCard.setPreferredSize(new Dimension(1000, 600));
    }

    protected QuanLyDAO.Hang daChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng.");
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= model.getRowCount()) return null;
        Object ma = model.getValueAt(modelRow, 0);
        return ma instanceof Number ? hangTheoMa.get(((Number) ma).intValue()) : null;
    }
}
