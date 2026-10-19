/*
 * TrangChuLeTan: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.LeTan;

import Class.TaiKhoan;
import ConnectDB.KetNoi;
import DAO.KhoDuLieu;
import UI.Chung.GiaoDien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class TrangChuLeTan extends JPanel {

    private static final Color PRIMARY = new Color(0x0284C7);
    private static final Color TEXT = new Color(0x0F172A);
    private static final Color MUTED = new Color(0x64748B);
    private static final Color BORDER = new Color(0xE2E8F0);
    private static final Color BACKGROUND = new Color(0xF8FAFC);
    private static final Color GREEN = new Color(0x10B981);
    private static final Color WARNING = new Color(0xF59E0B);

    private static final String[] ROOMS = {
        "Phòng 01", "Phòng 02", "Phòng 03", "Phòng 04",
        "Phòng 05", "Phòng 06", "Phòng 07", "Phòng 08"
    };

    private final KhoDuLieu db;
    private final TaiKhoan user;
    private final DefaultTableModel phieuModel;
    private final DefaultTableModel benhNhanModel;
    private final JTable phieuTable;
    private final JTable benhNhanTable;
    private final TableRowSorter<DefaultTableModel> phieuSorter;
    private final TableRowSorter<DefaultTableModel> benhNhanSorter;

    private final JComboBox<String> phongChon = new JComboBox<>(ROOMS);
    private final JButton tiepNhanButton = button("Tiếp nhận / Cấp số", true);
    private final JButton refreshButton = button("Làm mới", false);
    private final JLabel chonBenhNhan = label("Chọn một bệnh nhân trong bảng", 12, false);
    private final JLabel trangThai = label("", 11, false);
    private final JLabel tongBenhNhan = bigValue("—");
    private final JLabel choTiepNhan = bigValue("—");
    private final JLabel daTiepNhan = bigValue("—");
    private final JLabel soPhieuKham = bigValue("—");

    private boolean loading;
    private boolean canView;
    private boolean canReceive;

    /** Khởi tạo trang tiếp nhận, hai bảng dữ liệu và các nhãn thống kê. */
    public TrangChuLeTan(KhoDuLieu db, TaiKhoan user) {
        this.db = db;
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        phieuModel = model(
                "STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Mã NV", "Bác sĩ",
                "Ngày khám", "Trạng thái", "Triệu chứng", "Chẩn đoán",
                "Phương pháp điều trị", "Kết quả", "Ghi chú");
        benhNhanModel = model(
                "STT", "Mã BN", "Họ tên", "Ngày sinh", "Giới tính", "SĐT",
                "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái", "Số khám", "Phòng");
        phieuTable = table(phieuModel);
        benhNhanTable = table(benhNhanModel);
        phieuSorter = new TableRowSorter<>(phieuModel);
        benhNhanSorter = new TableRowSorter<>(benhNhanModel);
        phieuTable.setRowSorter(phieuSorter);
        benhNhanTable.setRowSorter(benhNhanSorter);
        phieuSorter.setSortable(0, false);
        benhNhanSorter.setSortable(0, false);
        setupStt(phieuTable);
        setupStt(benhNhanTable);

        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(22, 22, 18, 22));
        add(page, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 16));
        upper.setOpaque(false);
        upper.add(titlePanel(), BorderLayout.NORTH);
        upper.add(statsPanel(), BorderLayout.CENTER);
        page.add(upper, BorderLayout.NORTH);

        JPanel reception = receptionPanel();
        JPanel treatment = treatmentPanel();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treatment, reception);
        split.setResizeWeight(0.50);
        split.setContinuousLayout(true);
        split.setDividerSize(9);
        split.setBorder(null);
        split.setBackground(BACKGROUND);
        split.setOneTouchExpandable(true);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.50));
        page.add(split, BorderLayout.CENTER);

        benhNhanTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) updateSelection();
        });
        tiepNhanButton.addActionListener(event -> receiveSelected());
        refreshButton.addActionListener(event -> refreshAsync());

        refreshAsync();
    }

    /** Dựng tiêu đề, lời chào và nút làm mới trên trang Lễ tân. */
    private JPanel titlePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(label("Tổng quan  ›  Trang chủ Lễ tân", 11, false));
        texts.add(Box.createVerticalStrut(9));
        texts.add(label("Tiếp nhận & Khám điều trị", 23, true));
        texts.add(Box.createVerticalStrut(5));
        texts.add(label("Chào " + user.hoTen + " · Dữ liệu cập nhật từ SQL Server", 12, false));
        outer.add(texts, BorderLayout.WEST);
        outer.add(refreshButton, BorderLayout.EAST);
        return outer;
    }

    /** Đặt bốn chỉ số tiếp nhận theo bố cục hàng ngang hiện tại. */
    private JPanel statsPanel() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.setPreferredSize(new Dimension(700, 88));
        grid.add(statCard("Tổng bệnh nhân", tongBenhNhan, PRIMARY));
        grid.add(statCard("Chưa nhận hôm nay", choTiepNhan, WARNING));
        grid.add(statCard("Đã tiếp nhận hôm nay", daTiepNhan, GREEN));
        grid.add(statCard("Phiếu khám hôm nay", soPhieuKham, PRIMARY));
        return grid;
    }

    /** Dựng một thẻ chỉ số tiếp nhận với tên và giá trị cập nhật. */
    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel card = card(new BorderLayout());
        JLabel titleLabel = label(title, 11, false);
        value.setForeground(accent);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    /** Dựng khu vực bảng phiếu khám và tìm kiếm theo thông tin khám. */
    private JPanel treatmentPanel() {
        JPanel panel = card(new BorderLayout(0, 11));
        panel.setMinimumSize(new Dimension(310, 320));
        JPanel top = new JPanel(new BorderLayout(0, 9));
        top.setOpaque(false);
        top.add(sectionTitle("Khám & điều trị", "Danh sách phiếu khám; nhấp đúp để xem chi tiết"),
                BorderLayout.NORTH);
        JTextField search = search("Tìm mã phiếu, bệnh nhân, bác sĩ...");
        search.getDocument().addDocumentListener(new SearchListener(() ->
                filter(phieuSorter, search.getText())));
        top.add(search, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollTable(phieuTable), BorderLayout.CENTER);
        phieuTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) showPhieuDetail();
            }
        });
        panel.add(label("Cuộn ngang để xem đủ thông tin · Nhấp đúp để xem phiếu khám.",
                11, false), BorderLayout.SOUTH);
        return panel;
    }

    /** Hiển thị thông tin phiếu khám hiện được chọn trong bảng. */
    private void showPhieuDetail() {
        int viewRow = phieuTable.getSelectedRow();
        if (viewRow < 0) return;

        int row = phieuTable.convertRowIndexToModel(viewRow);
        String[] properties = {
            "Mã phiếu khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã nhân viên",
            "Bác sĩ phụ trách", "Ngày khám", "Trạng thái", "Triệu chứng",
            "Chẩn đoán", "Phương pháp điều trị", "Kết quả", "Ghi chú"
        };
        showDetail(phieuModel, row, properties, "Chi tiết phiếu khám");
    }

    /** Hiển thị hồ sơ bệnh nhân được chọn trong danh sách. */
    private void showPatientDetail() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0) return;

        int row = benhNhanTable.convertRowIndexToModel(viewRow);
        String[] properties = {
            "Mã bệnh nhân", "Họ và tên", "Ngày sinh", "Giới tính", "Số điện thoại",
            "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái", "Số khám hôm nay",
            "Phòng khám hôm nay"
        };
        showDetail(benhNhanModel, row, properties, "Chi tiết bệnh nhân");
    }

    /** Hiển thị hộp thoại chỉ đọc với các trường thông tin chi tiết. */
    private void showDetail(DefaultTableModel model, int row, String[] titles, String caption) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < titles.length; i++) {
            Object raw = model.getValueAt(row, i + 1);               
            result.append(titles[i]).append(":\n")
                  .append(raw == null || String.valueOf(raw).isBlank() ? "Chưa cập nhật" : raw)
                  .append("\n\n");
        }

        JTextArea detail = new JTextArea(result.toString(), 19, 50);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detail.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane view = new JScrollPane(detail);
        view.setPreferredSize(new Dimension(550, 410));
        JOptionPane.showMessageDialog(this, view, caption, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Dựng khu vực danh sách bệnh nhân và công cụ cấp số tiếp nhận. */
    private JPanel receptionPanel() {
        JPanel panel = card(new BorderLayout(0, 11));
        panel.setMinimumSize(new Dimension(330, 320));
        JPanel top = new JPanel(new BorderLayout(0, 9));
        top.setOpaque(false);
        top.add(sectionTitle("Tiếp nhận bệnh nhân", "Chọn bệnh nhân, cấp số và phân phòng khám"),
                BorderLayout.NORTH);
        JTextField search = search("Tìm họ tên, mã bệnh nhân, số điện thoại...");
        search.getDocument().addDocumentListener(new SearchListener(() ->
                filter(benhNhanSorter, search.getText())));
        top.add(search, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollTable(benhNhanTable), BorderLayout.CENTER);
        benhNhanTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) showPatientDetail();
            }
        });

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        chonBenhNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(chonBenhNhan);
        controls.add(Box.createVerticalStrut(8));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.add(label("Phòng khám", 12, true));
        phongChon.setPreferredSize(new Dimension(118, 35));
        actionRow.add(phongChon);
        actionRow.add(tiepNhanButton);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(actionRow);
        controls.add(Box.createVerticalStrut(8));
        trangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(trangThai);
        JPanel bottom = new JPanel(new BorderLayout(0, 5));
        bottom.setOpaque(false);
        bottom.add(controls, BorderLayout.CENTER);
        bottom.add(label("Mã BN hiển thị trực tiếp · Cuộn ngang / nhấp đúp xem đầy đủ hồ sơ.",
                11, false), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    /** Cập nhật bệnh nhân đang chọn và khả năng bấm nút Tiếp nhận. */
    private void updateSelection() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0) {
            chonBenhNhan.setText("Chọn một bệnh nhân trong bảng");
        } else {
            int row = benhNhanTable.convertRowIndexToModel(viewRow);
            chonBenhNhan.setText("Đã chọn: BN " + benhNhanModel.getValueAt(row, 1)
                    + " - " + benhNhanModel.getValueAt(row, 2));
        }
        tiepNhanButton.setEnabled(!loading && canReceive && viewRow >= 0);
    }

    /** Thực hiện tiếp nhận ở luồng nền và tải lại danh sách sau khi hoàn tất. */
    private void receiveSelected() {
        int viewRow = benhNhanTable.getSelectedRow();
        if (viewRow < 0 || loading || !canReceive) return;
        int modelRow = benhNhanTable.convertRowIndexToModel(viewRow);
        int patientId = (Integer) benhNhanModel.getValueAt(modelRow, 1);
        String patientName = String.valueOf(benhNhanModel.getValueAt(modelRow, 2));
        String room = String.valueOf(phongChon.getSelectedItem());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tiếp nhận " + patientName + " vào " + room + "?\n"
                + "Số thứ tự sẽ được SQL Server tự cấp theo ngày.",
                "Xác nhận tiếp nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        setLoading(true);
        trangThai.setText("Đang tiếp nhận...");
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception {
                return receiveInSql(patientId, room);
            }
            @Override protected void done() {
                try {
                    int queue = get();
                    JOptionPane.showMessageDialog(TrangChuLeTan.this,
                            "Tiếp nhận thành công!\nBệnh nhân: " + patientName
                            + "\nSố khám: " + String.format("%03d", queue)
                            + "\nPhòng: " + room,
                            "Phiếu tiếp nhận", JOptionPane.INFORMATION_MESSAGE);
                    trangThai.setText("Tiếp nhận thành công · Số "
                            + String.format("%03d", queue));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError(e);
                } catch (ExecutionException e) {
                    showError(e.getCause() == null ? e : e.getCause());
                } finally {
                    setLoading(false);
                    refreshAsync();
                }
            }
        }.execute();
    }

    /** Tiếp nhận và cấp số thứ tự trong giao dịch đã khóa để tránh số trùng. */
    private int receiveInSql(int patientId, String room) throws SQLException {
        try (Connection connection = KetNoi.mo()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try {
                requirePermission(connection, "LICHHEN");
                // Khi dùng danh sách đã tải trước đó, phải kiểm tra lại trạng thái BN trên SQL.
                try (PreparedStatement patient = connection.prepareStatement(
                        "SELECT 1 FROM dbo.BenhNhan WITH (UPDLOCK,HOLDLOCK) "
                      + "WHERE maBN=? AND ISNULL(trangThai,N'')<>N'Ngừng hoạt động'")) {
                    patient.setInt(1, patientId);
                    try (ResultSet result = patient.executeQuery()) {
                        if (!result.next()) throw new SQLException(
                                "Bệnh nhân không tồn tại hoặc đã ngừng hoạt động; không thể tiếp nhận.");
                    }
                }
                if (!columnExists(connection)) {
                    throw new SQLException("Thiếu cột soThuTuKham. Hãy chạy SQLData/06_TiepNhanLeTan_SQL2012.sql.");
                }

                Date today;
                try (Statement statement = connection.createStatement();
                        ResultSet result = statement.executeQuery("SELECT CAST(GETDATE() AS date)")) {
                    result.next();
                    today = result.getDate(1);
                }
                String lockName = "MEDIDENT_TIEPNHAN_" + today.toString();
                try (PreparedStatement statement = connection.prepareStatement(
                        "DECLARE @result int; "
                        + "EXEC @result = sp_getapplock @Resource=?, @LockMode='Exclusive', "
                        + "@LockOwner='Transaction', @LockTimeout=10000; "
                        + "SELECT @result")) {
                    statement.setString(1, lockName);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next() || result.getInt(1) < 0) {
                            throw new SQLException("Không khóa được bộ cấp số. Hãy thử lại.");
                        }
                    }
                }

                int appointmentId = -1;
                Integer previouslyAssigned = null;
                String previouslyRoom = null;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT TOP 1 maLichHen,soThuTuKham,phongKham FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE maBN=? AND ngayHen=? AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất') "
                        + "ORDER BY CASE WHEN soThuTuKham IS NULL THEN 1 ELSE 0 END, maLichHen")) {
                    statement.setInt(1, patientId);
                    statement.setDate(2, today);
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            appointmentId = result.getInt(1);
                            int value = result.getInt(2);
                            if (!result.wasNull()) previouslyAssigned = value;
                            previouslyRoom = result.getString("phongKham");
                        }
                    }
                }
                if (previouslyAssigned != null) {
                    // Bấm lại sau timeout phải trả về số cũ, không sinh thêm lượt.
                    // Nếu chọn phòng khác thì không được âm thầm chuyển phòng.
                    if (!java.util.Objects.equals(room, previouslyRoom))
                        throw new SQLException("Bệnh nhân đã được tiếp nhận ở "
                                + previouslyRoom + ", số " + String.format("%03d", previouslyAssigned));
                    connection.commit();
                    return previouslyAssigned;
                }

                int queue = 1;
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT ISNULL(MAX(soThuTuKham),0)+1 FROM dbo.LichHen WITH (UPDLOCK,HOLDLOCK) "
                        + "WHERE ngayHen=?")) {
                    statement.setDate(1, today);
                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        queue = result.getInt(1);
                    }
                }
                if (appointmentId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE dbo.LichHen SET soThuTuKham=?,phongKham=?, "
                            + "trangThai=N'Đã xác nhận' WHERE maLichHen=? AND soThuTuKham IS NULL")) {
                        statement.setInt(1, queue);
                        statement.setString(2, room);
                        statement.setInt(3, appointmentId);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Lịch hẹn vừa được thay đổi, hãy tải lại.");
                        }
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT dbo.LichHen (maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai,soThuTuKham) "
                            + "VALUES (?,NULL,?,CAST(GETDATE() AS date),CAST(GETDATE() AS time), "
                            + "N'Tiếp nhận trực tiếp',N'Đã xác nhận',?)")) {
                        statement.setInt(1, patientId);
                        statement.setString(2, room);
                        statement.setInt(3, queue);
                        statement.executeUpdate();
                    }
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT dbo.NhatKyHeThong(hanhDong,doiTuong,moTa) VALUES(?,?,?)")) {
                    statement.setString(1, "Tiếp nhận bệnh nhân");
                    statement.setString(2, "LichHen");
                    statement.setString(3, "maTK=" + user.id + " | maBN=" + patientId
                            + " | STT=" + queue + " | " + room);
                    statement.executeUpdate();
                }
                connection.commit();
                return queue;
            } catch (Exception e) {
                connection.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException("Tiếp nhận không thành công.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /** Chỉ Lễ tân còn làm việc và còn quyền Lịch hẹn mới được cấp số tiếp nhận. */
    private void requirePermission(Connection connection, String code) throws SQLException {
        String sql = "SELECT 1 FROM dbo.TaiKhoan t "
                + "JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro "
                + "JOIN dbo.VaiTroQuyen q ON q.maVaiTro=t.maVaiTro "
                + "WHERE t.maTK=? AND t.trangThai=N'Hoạt động' "
                + "AND v.tenVaiTro=N'Lễ tân' AND q.maQuyen=? "
                + "AND NOT EXISTS(SELECT 1 FROM dbo.NhanVien n WHERE n.maTK=t.maTK "
                + "AND (n.trangThai IS NULL OR n.trangThai<>N'Đang làm việc'))";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.id);
            statement.setString(2, code);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("Bạn không có quyền tiếp nhận bệnh nhân.");
            }
        }
    }

    /** Kiểm tra migration SQL đã bổ sung cột số thứ tự khám hay chưa. */
    private boolean columnExists(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "SELECT COL_LENGTH('dbo.LichHen','soThuTuKham')")) {
            return result.next() && result.getObject(1) != null;
        }
    }

    /** Tải lại bảng và thống kê bằng SwingWorker để không khóa giao diện. */
    private void refreshAsync() {
        if (loading) return;
        setLoading(true);
        trangThai.setText("Đang tải dữ liệu...");
        new SwingWorker<ReportData, Void>() {
            @Override protected ReportData doInBackground() throws Exception {
                return loadFromSql();
            }
            @Override protected void done() {
                try {
                    ReportData data = get();
                    canView = data.viewAllowed;
                    canReceive = data.receiveAllowed;
                    fillTable(phieuModel, data.examRows);
                    fillTable(benhNhanModel, data.patientRows);
                    tongBenhNhan.setText(String.valueOf(data.totalPatients));
                    choTiepNhan.setText(String.valueOf(data.waitingToday));
                    daTiepNhan.setText(String.valueOf(data.receivedToday));
                    soPhieuKham.setText(String.valueOf(data.examsToday));
                    trangThai.setText(data.viewAllowed
                            ? "Đã đồng bộ SQL Server · Hôm nay: " + LocalDate.now()
                            : "Không có quyền BENHNHAN — danh sách đã được ẩn");
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

    /** Truy vấn bệnh nhân, phiếu khám và hàng đợi theo quyền tài khoản. */
    private ReportData loadFromSql() throws SQLException {
        ReportData data = new ReportData();
        data.viewAllowed = db.coQuyenTaiKhoan(user.id, "BENHNHAN");
        data.receiveAllowed = data.viewAllowed && db.coQuyenTaiKhoan(user.id, "LICHHEN")
                && "Lễ tân".equalsIgnoreCase(user.tenVaiTro);
        if (!data.viewAllowed) return data;

        try (Connection connection = KetNoi.mo()) {
            boolean queueInstalled = columnExists(connection);
            String queueField = queueInstalled ? "l.soThuTuKham" : "CAST(NULL AS INT)";

            String examSql =
                    "SELECT p.maPK, p.maBN, p.maNV, "
                    + "ISNULL(b.hoTen,N'') AS benhNhan, "
                    + "ISNULL(n.hoTen,N'') AS bacSi, "
                    + "CONVERT(varchar(16), p.ngayKham, 120) AS ngayKham, "
                    + "ISNULL(p.trangThai,N'Chưa cập nhật') AS trangThai, "
                    + "ISNULL(p.trieuChung,N'') AS trieuChung, "
                    + "ISNULL(p.chanDoan,N'') AS chanDoan, "
                    + "ISNULL(p.phuongPhapDieuTri,N'') AS phuongPhapDieuTri, "
                    + "ISNULL(p.ketQua,N'') AS ketQua, "
                    + "ISNULL(p.ghiChu,N'') AS ghiChu "
                    + "FROM dbo.PhieuKham p "
                    + "LEFT JOIN dbo.BenhNhan b ON p.maBN=b.maBN "
                    + "LEFT JOIN dbo.NhanVien n ON p.maNV=n.maNV "
                    + "ORDER BY p.ngayKham DESC, p.maPK DESC";

            try (PreparedStatement statement = connection.prepareStatement(examSql);
                    ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    data.examRows.add(new Object[]{
                        0,
                        result.getInt("maPK"),
                        result.getInt("maBN"),
                        result.getString("benhNhan"),
                        result.getInt("maNV"),
                        result.getString("bacSi"),
                        result.getString("ngayKham"),
                        result.getString("trangThai"),
                        result.getString("trieuChung"),
                        result.getString("chanDoan"),
                        result.getString("phuongPhapDieuTri"),
                        result.getString("ketQua"),
                        result.getString("ghiChu")
                    });
                }
            }

            String patientSql =
                    "SELECT b.maBN, b.hoTen, "
                    + "ISNULL(CONVERT(varchar(10),b.ngaySinh,103),'') AS ngaySinh, "
                    + "ISNULL(b.gioiTinh,N'') AS gioiTinh, "
                    + "ISNULL(b.sdt,'') AS sdt, ISNULL(b.email,'') AS email, "
                    + "ISNULL(b.diaChi,N'') AS diaChi, "
                    + "ISNULL(b.tienSuBenh,N'') AS tienSuBenh, "
                    + "ISNULL(b.trangThai,N'') AS trangThai, "
                    + queueField.replace("l.", "a.") + " AS soThuTuKham, "
                    + "ISNULL(a.phongKham,N'') AS phongKham "
                    + "FROM dbo.BenhNhan b "
                    + "OUTER APPLY (SELECT TOP 1 l.maLichHen, l.phongKham"
                    + (queueInstalled ? ", l.soThuTuKham" : "")
                    + " FROM dbo.LichHen l WHERE l.maBN=b.maBN "
                    + "AND l.ngayHen=CAST(GETDATE() AS date) AND l.trangThai<>N'Đã hủy' "
                    + "ORDER BY " + (queueInstalled
                            ? "CASE WHEN l.soThuTuKham IS NULL THEN 1 ELSE 0 END, " : "")
                    + "l.maLichHen) a ORDER BY b.maBN DESC";

            try (PreparedStatement statement = connection.prepareStatement(patientSql);
                    ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Object number = result.getObject("soThuTuKham");
                    data.patientRows.add(new Object[]{
                        0,
                        result.getInt("maBN"),
                        result.getString("hoTen"),
                        result.getString("ngaySinh"),
                        result.getString("gioiTinh"),
                        result.getString("sdt"),
                        result.getString("email"),
                        result.getString("diaChi"),
                        result.getString("tienSuBenh"),
                        result.getString("trangThai"),
                        number == null ? "—" : String.format("%03d", result.getInt("soThuTuKham")),
                        result.getString("phongKham")
                    });
                }
            }

            data.totalPatients = scalar(connection, "SELECT COUNT(*) FROM dbo.BenhNhan");
            data.examsToday = scalar(connection,
                    "SELECT COUNT(*) FROM dbo.PhieuKham "
                    + "WHERE ngayKham>=CAST(GETDATE() AS date) "
                    + "AND ngayKham<DATEADD(day,1,CAST(GETDATE() AS date))");
            if (queueInstalled) {
                data.receivedToday = scalar(connection,
                        "SELECT COUNT(*) FROM dbo.LichHen "
                        + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NOT NULL");
                data.waitingToday = scalar(connection,
                        "SELECT COUNT(*) FROM dbo.LichHen "
                        + "WHERE ngayHen=CAST(GETDATE() AS date) AND soThuTuKham IS NULL "
                        + "AND trangThai<>N'Đã hủy'");
            }
        }
        return data;
    }

    /** Chạy câu truy vấn số nguyên để tính một chỉ số của trang Lễ tân. */
    private int scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            return result.next() ? result.getInt(1) : 0;
        }
    }

    /** Nạp lại danh sách hàng cho TableModel sau khi truy vấn SQL xong. */
    private void fillTable(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] values : rows) model.addRow(values);
    }

    /** Cập nhật thuộc tính Loading theo giá trị được truyền vào. */
    private void setLoading(boolean value) {
        loading = value;
        refreshButton.setEnabled(!value);
        phongChon.setEnabled(!value);
        tiepNhanButton.setEnabled(!value && canReceive && benhNhanTable.getSelectedRow() >= 0);
    }

    /** Thông báo lỗi truy vấn hoặc lưu dữ liệu cho người dùng tại Lễ tân. */
    private void showError(Throwable error) {
        trangThai.setText("Không tải/lưu được dữ liệu SQL Server.");
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "MediDent - Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private static final class ReportData {
        boolean viewAllowed;
        boolean receiveAllowed;
        int totalPatients;
        int waitingToday;
        int receivedToday;
        int examsToday;
        final List<Object[]> examRows = new ArrayList<>();
        final List<Object[]> patientRows = new ArrayList<>();
    }

    /** Hiển thị trang được chọn, giữ nguyên sơ đồ điều hướng hiện có. */
    private static DefaultTableModel model(String... headers) {
        return new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Integer.class : Object.class;
            }
        };
    }

    /** Khởi tạo JTable với cột và thuộc tính hiển thị theo theme hiện tại. */
    private static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(37);
        table.setSelectionBackground(new Color(0xE0F2FE));
        table.setSelectionForeground(TEXT);
        table.setGridColor(new Color(0xEEF2F7));
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(0xF1F5F9));
        table.getTableHeader().setForeground(MUTED);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(43);       
        table.getColumnModel().getColumn(1).setPreferredWidth(80);                    
        for (int i = 2; i < table.getColumnCount(); i++) {
            String title = table.getColumnName(i);
            int width;
            switch (title) {
                case "Mã BN":
                case "Mã NV": width = 75; break;
                case "Bệnh nhân":
                case "Họ tên":
                case "Bác sĩ": width = 160; break;
                case "Ngày khám":
                case "Ngày sinh": width = 126; break;
                case "Triệu chứng":
                case "Chẩn đoán":
                case "Phương pháp điều trị":
                case "Kết quả":
                case "Tiền sử bệnh":
                case "Địa chỉ":
                case "Ghi chú": width = 180; break;
                default: width = 125;
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
        return table;
    }

    /** Cập nhật thuộc tính upStt theo giá trị được truyền vào. */
    private static void setupStt(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, row + 1, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(renderer);
    }

    /** Bọc JTable trong JScrollPane và thiết lập viền hiển thị. */
    private static JScrollPane scrollTable(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(Color.WHITE);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.getHorizontalScrollBar().setUnitIncrement(24);
        return pane;
    }

    /** Tạo bảng nền trắng có viền cho các khối thông tin Lễ tân. */
    private static JPanel card(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(16, 17, 16, 17)));
        return panel;
    }

    /** Tạo tiêu đề và chú thích cho từng khu vực trang Lễ tân. */
    private static JPanel sectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(label(title, 16, true));
        panel.add(Box.createVerticalStrut(4));
        panel.add(label(subtitle, 11, false));
        return panel;
    }

    /** Tạo nhãn Swing theo nội dung và định dạng chữ, không phụ thuộc dữ liệu SQL. */
    private static JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setForeground(bold ? TEXT : MUTED);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return label;
    }

    /** Tạo nhãn số lớn cho các chỉ số tổng quan. */
    private static JLabel bigValue(String text) {
        return label(text, 25, true);
    }

    /** Tạo nút chính hoặc phụ với trạng thái hiển thị hiện hành. */
    private static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(primary ? PRIMARY : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? PRIMARY : BORDER),
                new EmptyBorder(9, 13, 9, 13)));
        return button;
    }

    /** Tạo ô tìm kiếm với chữ gợi ý trên bảng dữ liệu. */
    private static JTextField search(String prompt) {

        JTextField field = new JTextField() {
            @Override protected void paintComponent(java.awt.Graphics graphics) {
                super.paintComponent(graphics);
                if (getText().isEmpty() && !hasFocus()) {
                    graphics.setColor(MUTED);
                    graphics.setFont(getFont());
                    int baseline = (getHeight() + graphics.getFontMetrics().getAscent()
                            - graphics.getFontMetrics().getDescent()) / 2;
                    graphics.drawString(prompt, getInsets().left + 2, baseline);
                }
            }
        };
        field.setToolTipText(prompt);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 10, 7, 10)));
        return field;
    }

    /** Cập nhật bộ lọc dòng JTable theo từ khóa người dùng nhập. */
    private static void filter(TableRowSorter<DefaultTableModel> sorter, String text) {
        String query = text == null ? "" : text.trim();
        sorter.setRowFilter(query.isEmpty() ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(query)));
    }

    private static final class SearchListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        SearchListener(Runnable action) { this.action = action; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }
}
