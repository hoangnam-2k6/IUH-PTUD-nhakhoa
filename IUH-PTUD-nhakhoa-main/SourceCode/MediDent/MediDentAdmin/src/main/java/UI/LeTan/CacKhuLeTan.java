package UI.LeTan;

import Class.DuLieuTrangLeTan;


import Class.KetQuaTiepNhan;

import Class.ThongTinBenhNhan;

import DAO.TiepNhanDAO;

import TienIch.EmailHopLe;


import UI.LeTan.ThanhTimKiem.TieuChi;


import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Component;

import java.awt.Dimension;

import java.awt.FlowLayout;

import java.awt.Font;

import java.awt.GridBagConstraints;

import java.awt.GridBagLayout;

import java.awt.GridLayout;

import java.awt.GridLayout;


import java.awt.Insets;

import java.awt.event.FocusAdapter;

import java.awt.event.FocusEvent;

import java.awt.event.MouseAdapter;

import java.awt.event.MouseEvent;

import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;

import java.time.format.ResolverStyle;

import java.util.List;


import java.util.Locale;

import java.util.concurrent.ExecutionException;


import javax.swing.BorderFactory;

import javax.swing.Box;

import javax.swing.BoxLayout;

import javax.swing.JButton;

import javax.swing.JComboBox;

import javax.swing.JComponent;

import javax.swing.JLabel;

import javax.swing.JOptionPane;

import javax.swing.JPanel;

import javax.swing.JScrollPane;

import javax.swing.JTable;

import javax.swing.JTextArea;

import javax.swing.JTextField;

import javax.swing.ScrollPaneConstants;

import javax.swing.SwingWorker;

import javax.swing.border.EmptyBorder;

import javax.swing.border.EmptyBorder;


import javax.swing.table.DefaultTableModel;

import javax.swing.table.TableRowSorter;


import static UI.LeTan.BangDuLieu.boCuon;

import static UI.LeTan.BangDuLieu.datCot;

import static UI.LeTan.BangDuLieu.taoBang;

import static UI.LeTan.BangDuLieu.taoModel;

import static UI.LeTan.BangDuLieu.taoSorter;

import static UI.LeTan.GiaoDien.BORDER;

import static UI.LeTan.GiaoDien.GREEN;

import static UI.LeTan.GiaoDien.MUTED;

import static UI.LeTan.GiaoDien.PRIMARY;

import static UI.LeTan.GiaoDien.PRIMARY_DARK;

import static UI.LeTan.GiaoDien.ROW_ALT;

import static UI.LeTan.GiaoDien.TEXT;

import static UI.LeTan.GiaoDien.WARNING;

import static UI.LeTan.GiaoDien.card;

import static UI.LeTan.GiaoDien.datKichThuoc;

import static UI.LeTan.GiaoDien.escapeHtml;

import static UI.LeTan.GiaoDien.font;

import static UI.LeTan.GiaoDien.label;

import static UI.LeTan.GiaoDien.label;


import static UI.LeTan.GiaoDien.sectionTitle;

import static UI.LeTan.ThanhTimKiem.cols;



/**
 * Khu vực "Khám & điều trị": danh sách phiếu khám (chỉ xem), tìm kiếm / lọc,
 * và nhấp đúp để xem chi tiết. (In hóa đơn nằm ở trang Thanh toán & Hóa đơn.)
 */
final class KhuKhamDieuTri extends KhungBoGoc {

    private static final String[] TIEU_DE_CHI_TIET = {
        "Mã phiếu khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã nhân viên",
        "Bác sĩ phụ trách", "Ngày khám", "Trạng thái", "Triệu chứng",
        "Chẩn đoán", "Phương pháp điều trị", "Kết quả", "Ghi chú"
    };

    private final DefaultTableModel phieuModel;
    private final JTable phieuTable;
    private final TableRowSorter<DefaultTableModel> phieuSorter;
    private final NhanDem phieuCount = new NhanDem("0");

    public KhuKhamDieuTri() {
        super(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(17, 18, 20, 18));
        setMinimumSize(new Dimension(310, 320));

        // Mã phiếu, mã bệnh nhân và mã bác sĩ đặt ở đầu để dễ nhìn trong khung chia đôi.
        // Các thuộc tính còn lại xem bằng cuộn ngang hoặc nhấp đúp để mở chi tiết.
        phieuModel = taoModel(
                "STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Mã NV", "Bác sĩ",
                "Ngày khám", "Trạng thái", "Triệu chứng", "Chẩn đoán",
                "Phương pháp điều trị", "Kết quả", "Ghi chú");
        phieuTable = taoBang(phieuModel);
        phieuSorter = taoSorter(phieuTable, phieuModel);
        caiDatCotBang();

        // TieuChi(tên hiển thị, cột mã, cột văn bản, cột SĐT) - số cột tính theo model.
        TieuChi[] criteria = {
            new TieuChi("Tất cả", cols(1, 2), cols(3, 5), cols()),
            new TieuChi("Mã phiếu", cols(1), cols(), cols()),
            new TieuChi("Mã BN", cols(2), cols(), cols()),
            new TieuChi("Tên bệnh nhân", cols(), cols(3), cols()),
            new TieuChi("Mã bác sĩ", cols(4), cols(), cols()),
            new TieuChi("Tên bác sĩ", cols(), cols(5), cols()),
            new TieuChi("Ngày khám", cols(), cols(6), cols()),
            new TieuChi("Trạng thái", cols(), cols(7), cols()),
            new TieuChi("Chẩn đoán", cols(), cols(9), cols())
        };

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Khám & điều trị",
                "Danh sách phiếu khám; nhấp đúp để xem chi tiết", phieuCount), BorderLayout.NORTH);
        top.add(ThanhTimKiem.taoThanh(phieuSorter, criteria), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(boCuon(phieuTable), BorderLayout.CENTER);
        add(label("Cuộn ngang để xem đủ thông tin · Nhấp đúp để xem phiếu khám.",
                11, false), BorderLayout.SOUTH);

        phieuTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) hienChiTiet();
            }
        });
    }

    /** Đổ danh sách phiếu khám mới tải lên bảng (STT đánh lại từ 1). */
    public void napDuLieu(List<Object[]> rows) {
        phieuModel.setRowCount(0);
        int stt = 1;
        for (Object[] values : rows) {
            Object[] copy = values.clone();
            copy[0] = stt++; // STT cố định theo thứ tự dữ liệu tải về.
            phieuModel.addRow(copy);
        }
        phieuCount.setText(String.valueOf(rows.size()));
    }

    /** Gắn cách hiển thị riêng cho từng cột của bảng. */
    private void caiDatCotBang() {
        // STT: số mờ, căn giữa.
        datCot(phieuTable, 0, new BoVeCanGiua(MUTED, false));
        // Mã phiếu: in đậm, màu chủ đạo, căn giữa.
        datCot(phieuTable, 1, new BoVeCanGiua(PRIMARY_DARK, true));
        // Mã BN và Mã NV trong bảng phiếu khám: căn giữa.
        datCot(phieuTable, 2, new BoVeCanGiua(TEXT, false));
        datCot(phieuTable, 4, new BoVeCanGiua(TEXT, false));
        // Tên kèm avatar.
        datCot(phieuTable, 3, new BoVeAvatar());
        // Nhãn màu cho trạng thái.
        datCot(phieuTable, 7, new BoVeNhan(false));
    }

    /** Hiển thị đầy đủ thuộc tính phiếu khám, không cần kéo ngang để đọc nội dung dài. */
    private void hienChiTiet() {
        int viewRow = phieuTable.getSelectedRow();
        if (viewRow < 0) return;
        HopThoaiChiTiet.hien(this, phieuModel, phieuTable.convertRowIndexToModel(viewRow),
                TIEU_DE_CHI_TIET, "Chi tiết phiếu khám");
    }
}

/**
 * Khu vực "Tiếp nhận bệnh nhân": form nhập thông tin, kiểm tra dữ liệu, hỏi xác nhận,
 * rồi gọi {@link TiepNhanDAO} (ở luồng nền) để cấp số thứ tự và chỉ định phòng.
 * Việc hiển thị trạng thái chung của trang và tải lại dữ liệu được báo ra ngoài qua {@link BoLangNghe}.
 */
final class KhuTiepNhan extends KhungBoGoc {

    /** Trang chủ lắng nghe để khóa giao diện, báo trạng thái, hiện lỗi và tải lại danh sách. */
    public interface BoLangNghe {
        void dangXuLy(boolean on);
        void trangThai(String text, Color dot);
        void loi(Throwable error);
        void canTaiLai();
    }

    // Danh sách phòng cố định để người sử dụng không nhập sai ký tự.
    private static final String[] ROOMS = {
        "Phòng 01", "Phòng 02", "Phòng 03", "Phòng 04",
        "Phòng 05", "Phòng 06", "Phòng 07", "Phòng 08"
    };

    // Định dạng ngày sinh người dùng nhập; STRICT để chặn ngày không có thật (31/02).
    private static final DateTimeFormatter NGAY_SINH =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final TiepNhanDAO dao;
    private final BoLangNghe listener;

    private final JComboBox<String> phongChon = new HopChonHienDai(ROOMS);
    private final JButton tiepNhanButton = new NutBoGoc("Tiếp nhận / Cấp số", NutBoGoc.CHINH);
    private final JButton lamLaiButton = new NutBoGoc("Nhập lại", NutBoGoc.PHU);

    private final JTextField hoTenField = new TruongNhap("Ví dụ: Nguyễn Văn An");
    private final JTextField ngaySinhField = new TruongNhap("dd/MM/yyyy");
    private final JComboBox<String> gioiTinhChon = new HopChonHienDai(new String[] {"Nam", "Nữ"});
    private final JTextField sdtField = new TruongNhap("Ví dụ: 0901234567");
    private final JTextField emailField = new TruongNhap("ten@example.com");
    private final JTextField diaChiField = new TruongNhap("Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành");
    private final JTextArea tienSuArea = new VungVanBanNhap("Dị ứng thuốc, bệnh nền, phẫu thuật trước đây... (nếu có)");
    private final JLabel trangThai = label("", 11, false);

    public KhuTiepNhan(TiepNhanDAO dao, BoLangNghe listener) {
        super(new BorderLayout(0, 14));
        this.dao = dao;
        this.listener = listener;
        setBorder(new EmptyBorder(17, 18, 20, 18));
        setMinimumSize(new Dimension(330, 320));

        add(tieuDeKhu(), BorderLayout.NORTH);
        add(dungForm(), BorderLayout.CENTER);
        add(dungPhanDuoi(), BorderLayout.SOUTH);

        tiepNhanButton.addActionListener(event -> tiepNhanTuForm());
        lamLaiButton.addActionListener(event -> lamLaiForm());
    }

    // ---------- Các hàm cho trang chủ gọi ----------

    /** Bật / tắt toàn bộ form tiếp nhận (khóa khi đang tải hoặc khi không có quyền). */
    public void datChoPhepNhap(boolean on) {
        JComponent[] controls = {hoTenField, ngaySinhField, gioiTinhChon, sdtField, emailField,
            diaChiField, tienSuArea, phongChon, tiepNhanButton, lamLaiButton};
        for (JComponent control : controls) control.setEnabled(on);
    }

    /** Dòng trạng thái có chấm màu: vàng = đang xử lý, xanh = ổn, đỏ = lỗi / bị chặn. */
    public void datTrangThai(String text, Color dot) {
        trangThai.setText(text);
        trangThai.setIcon(new BieuTuongCham(dot));
        trangThai.setIconTextGap(8);
    }

    // ---------- Dựng giao diện ----------

    /** Tiêu đề khu vực: biểu tượng người + dấu cộng, tên, mô tả và nhãn ngày hôm nay. */
    private JPanel tieuDeKhu() {
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(label("Tiếp nhận bệnh nhân", 16, true));
        texts.add(Box.createVerticalStrut(4));
        texts.add(label("Nhập thông tin, cấp số thứ tự và phân phòng khám", 11, false));

        JPanel badge = new JPanel(new GridBagLayout());
        badge.setOpaque(false);
        badge.add(new HuyHieuBieuTuong(4, PRIMARY, 42));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);
        left.add(badge, BorderLayout.WEST);
        left.add(texts, BorderLayout.CENTER);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        east.setOpaque(false);
        east.add(new NhanDem("Hôm nay · " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM"))));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        header.add(left, BorderLayout.CENTER);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    /** Phần form có cuộn dọc: ba nhóm có tiêu đề, các ô ngắn xếp hai cột. */
    private JScrollPane dungForm() {
        // Ô tiền sử bệnh: khung bo góc, viền đổi màu khi focus giống các ô nhập khác.
        JScrollPane tienSuPane = new JScrollPane(tienSuArea);
        tienSuPane.setBorder(new VienNhap(BORDER, 1.2f));
        tienSuPane.setBackground(Color.WHITE);
        tienSuPane.getViewport().setBackground(Color.WHITE);
        tienSuPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tienSuPane.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        tienSuPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        tienSuPane.setPreferredSize(new Dimension(200, 88));
        tienSuArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent event) {
                tienSuPane.setBorder(new VienNhap(PRIMARY, 1.8f));
                tienSuPane.repaint();
            }
            @Override public void focusLost(FocusEvent event) {
                tienSuPane.setBorder(new VienNhap(BORDER, 1.2f));
                tienSuPane.repaint();
            }
        });

        JPanel body = new KhungCuon();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0, 0, 6, 4));
        body.add(nhomTieuDe("Thông tin cá nhân"));
        body.add(Box.createVerticalStrut(10));
        body.add(khoi("Họ và tên *", hoTenField));
        body.add(Box.createVerticalStrut(12));
        body.add(hangDoi(khoi("Ngày sinh *", ngaySinhField),
                khoi("Giới tính *", gioiTinhChon)));
        body.add(Box.createVerticalStrut(20));
        body.add(nhomTieuDe("Liên hệ"));
        body.add(Box.createVerticalStrut(10));
        body.add(hangDoi(khoi("Số điện thoại *", sdtField), khoi("Email", emailField)));
        body.add(Box.createVerticalStrut(12));
        body.add(khoi("Địa chỉ", diaChiField));
        body.add(Box.createVerticalStrut(20));
        body.add(nhomTieuDe("Thông tin y tế"));
        body.add(Box.createVerticalStrut(10));
        body.add(khoi("Tiền sử bệnh", tienSuPane));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new ThanhCuonHienDai());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        return scroll;
    }

    /** Phần cố định phía dưới (có đường kẻ phân cách): phòng khám, nút, ghi chú, trạng thái. */
    private JPanel dungPhanDuoi() {
        // Ghi chú nhỏ trong khung xám nhạt có biểu tượng "i".
        JLabel note = label("Hồ sơ trùng họ tên + SĐT sẽ được dùng lại, không tạo mới.", 11, false);
        note.setIcon(new BieuTuongThongTin());
        note.setIconTextGap(8);
        HopTo noteBox = new HopTo(new BorderLayout(), ROW_ALT, BORDER);
        noteBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        noteBox.add(note, BorderLayout.CENTER);
        noteBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, noteBox.getPreferredSize().height));
        trangThai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 0, 0, 0)));
        bottom.add(hangPhongKham());
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(hangNut());
        bottom.add(Box.createVerticalStrut(12));
        bottom.add(noteBox);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(trangThai);
        return bottom;
    }

    /** Tiêu đề nhóm trường: chấm màu + chữ in hoa nhỏ + đường kẻ mảnh kéo dài hết hàng. */
    private static JPanel nhomTieuDe(String text) {
        JLabel name = new JLabel(text.toUpperCase(Locale.ROOT));
        name.setFont(font(java.awt.Font.BOLD, 11));
        name.setForeground(PRIMARY_DARK);
        name.setIcon(new BieuTuongCham(PRIMARY));
        name.setIconTextGap(6);

        JPanel line = new JPanel();
        line.setBackground(BORDER);
        line.setPreferredSize(new Dimension(10, 1));
        JPanel lineWrap = new JPanel(new GridBagLayout());
        lineWrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        lineWrap.add(line, gc);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(name, BorderLayout.WEST);
        row.add(lineWrap, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Một khối: nhãn nhỏ phía trên (dấu * bắt buộc màu đỏ), ô nhập phía dưới. */
    private static JPanel khoi(String caption, JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 6));
        block.setOpaque(false);
        JLabel name = label(caption, 12, true);
        name.setFont(font(java.awt.Font.BOLD, 12));
        if (caption.endsWith("*")) {
            name.setText("<html>" + escapeHtml(caption.substring(0, caption.length() - 1).trim())
                    + " <font color='#EF4444'>*</font></html>");
        }
        block.add(name, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, block.getPreferredSize().height));
        return block;
    }

    /** Hai khối cạnh nhau, chia đều chiều rộng. */
    private static JPanel hangDoi(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(left);
        row.add(right);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /** Khung "Phòng khám" nền xanh nhạt: biểu tượng cửa phòng, mô tả giới hạn, ô chọn phòng bên phải. */
    private JPanel hangPhongKham() {
        phongChon.setFont(font(java.awt.Font.PLAIN, 13));
        datKichThuoc(phongChon, 132, 38);

        JLabel name = label("", 12, true);
        name.setText("<html>Phòng khám <font color='#EF4444'>*</font></html>");
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(name);
        texts.add(Box.createVerticalStrut(2));
        texts.add(label("Tối đa " + TiepNhanDAO.MAX_BENH_NHAN_MOI_PHONG
                + " bệnh nhân / phòng / ngày", 11, false));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new HuyHieuBieuTuong(5, PRIMARY, 38));
        JPanel comboWrap = new JPanel(new GridBagLayout());
        comboWrap.setOpaque(false);
        comboWrap.add(phongChon);

        HopTo box = new HopTo(new BorderLayout(12, 0), new Color(0xF0F9FF), new Color(0xBAE6FD));
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        box.add(iconWrap, BorderLayout.WEST);
        box.add(texts, BorderLayout.CENTER);
        box.add(comboWrap, BorderLayout.EAST);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, box.getPreferredSize().height));
        return box;
    }

    /** Hàng nút: "Nhập lại" (nút phụ) và "Tiếp nhận / Cấp số" (nút chính, chiếm phần còn lại). */
    private JPanel hangNut() {
        datKichThuoc(lamLaiButton, 100, 42);
        tiepNhanButton.setFont(font(java.awt.Font.BOLD, 13));
        tiepNhanButton.setPreferredSize(new Dimension(150, 42));
        tiepNhanButton.setMinimumSize(new Dimension(130, 42));

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        GridBagConstraints clear = cot(0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE);
        clear.insets = new Insets(0, 0, 0, 10);
        row.add(lamLaiButton, clear);
        row.add(tiepNhanButton, cot(1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL));
        return row;
    }

    private static GridBagConstraints cot(int gridx, double weightx, int anchor, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = gridx;
        c.gridy = 0;
        c.weightx = weightx;
        c.anchor = anchor;
        c.fill = fill;
        return c;
    }

    // ---------- Đọc và kiểm tra form ----------

    /** Xóa trắng form để nhập bệnh nhân kế tiếp (giữ nguyên phòng khám đang chọn). */
    private void lamLaiForm() {
        for (JTextField field : new JTextField[] {hoTenField, ngaySinhField, sdtField, emailField, diaChiField}) {
            if (field instanceof TruongNhap) ((TruongNhap) field).datLoi(false);
        }
        hoTenField.setText("");
        ngaySinhField.setText("");
        gioiTinhChon.setSelectedIndex(0);
        sdtField.setText("");
        emailField.setText("");
        diaChiField.setText("");
        tienSuArea.setText("");
        hoTenField.requestFocusInWindow();
    }

    /** Đọc và kiểm tra form; trả về null (kèm thông báo, đặt con trỏ vào ô sai) nếu chưa hợp lệ. */
    private ThongTinBenhNhan docForm() {
        String hoTen = hoTenField.getText().trim().replaceAll("\\s+", " ");
        if (hoTen.isEmpty()) return loiForm("Vui lòng nhập họ và tên bệnh nhân.", hoTenField);
        if (hoTen.chars().anyMatch(Character::isDigit)) {
            return loiForm("Họ và tên không được chứa chữ số.", hoTenField);
        }

        String ngayText = ngaySinhField.getText().trim();
        if (ngayText.isEmpty()) return loiForm("Vui lòng nhập ngày sinh (dd/MM/yyyy).", ngaySinhField);
        LocalDate ngaySinh;
        try {
            ngaySinh = LocalDate.parse(ngayText, NGAY_SINH);
        } catch (DateTimeParseException e) {
            return loiForm("Ngày sinh không hợp lệ. Nhập theo dạng dd/MM/yyyy, ví dụ 15/08/1995.",
                    ngaySinhField);
        }
        if (ngaySinh.isAfter(LocalDate.now()) || ngaySinh.getYear() < 1900) {
            return loiForm("Ngày sinh phải từ năm 1900 đến hôm nay.", ngaySinhField);
        }

        String sdt = sdtField.getText().replaceAll("[\\s.\\-]", "");
        if (sdt.startsWith("+84")) sdt = "0" + sdt.substring(3);
        if (sdt.isEmpty()) return loiForm("Vui lòng nhập số điện thoại.", sdtField);
        if (!sdt.matches("0\\d{9}")) {
            return loiForm("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.", sdtField);
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EmailHopLe.kiemTra(email)) {
            return loiForm(EmailHopLe.huongDan(), emailField);
        }

        return new ThongTinBenhNhan(hoTen, ngaySinh, String.valueOf(gioiTinhChon.getSelectedItem()),
                sdt, email, diaChiField.getText().trim(), tienSuArea.getText().trim());
    }

    private ThongTinBenhNhan loiForm(String message, JComponent field) {
        if (field instanceof TruongNhap) ((TruongNhap) field).datLoi(true); // viền đỏ cho tới khi sửa
        JOptionPane.showMessageDialog(this, message, "Thiếu hoặc sai thông tin",
                JOptionPane.WARNING_MESSAGE);
        field.requestFocusInWindow();
        return null;
    }

    // ---------- Xử lý khi bấm nút Tiếp nhận / Cấp số ----------

    /** Kiểm tra form, hỏi xác nhận, rồi tạo tiếp nhận trong SQL ở luồng nền để không đứng giao diện. */
    private void tiepNhanTuForm() {
        if (!tiepNhanButton.isEnabled()) return;   // đang tải hoặc không có quyền
        ThongTinBenhNhan info = docForm();
        if (info == null) return;
        String room = String.valueOf(phongChon.getSelectedItem());

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tiếp nhận " + info.hoTen + " (SĐT " + info.sdt + ") vào " + room + "?\n"
                + "Nếu chưa có hồ sơ, hệ thống tạo hồ sơ bệnh nhân mới.\n"
                + "Số thứ tự sẽ được SQL Server tự cấp theo ngày.",
                "Xác nhận tiếp nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        listener.dangXuLy(true);
        listener.trangThai("Đang tiếp nhận...", WARNING);
        new SwingWorker<KetQuaTiepNhan, Void>() {
            @Override protected KetQuaTiepNhan doInBackground() throws Exception {
                return dao.tiepNhan(info, room);
            }
            @Override protected void done() {
                try {
                    KetQuaTiepNhan result = get();
                    String queue = String.format("%03d", result.soKham);
                    JOptionPane.showMessageDialog(KhuTiepNhan.this,
                            "Tiếp nhận thành công!\nBệnh nhân: " + info.hoTen
                            + " (Mã BN " + result.maBenhNhan
                            + (result.hoSoMoi ? " - hồ sơ mới" : " - hồ sơ đã có") + ")"
                            + "\nSố khám: " + queue
                            + "\nPhòng: " + room,
                            "Phiếu tiếp nhận", JOptionPane.INFORMATION_MESSAGE);
                    listener.trangThai("Tiếp nhận thành công · Số " + queue, GREEN);
                    lamLaiForm();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    listener.loi(e);
                } catch (ExecutionException e) {
                    listener.loi(e.getCause() == null ? e : e.getCause());
                } finally {
                    listener.dangXuLy(false);
                    listener.canTaiLai();
                }
            }
        }.execute();
    }
}

/** Hàng bốn thẻ thống kê ở đầu trang Lễ tân. */
final class ThongKeLeTan extends JPanel {

    private final JLabel tongBenhNhan = label("—", 25, true);
    private final JLabel choTiepNhan = label("—", 25, true);
    private final JLabel daTiepNhan = label("—", 25, true);
    private final JLabel soPhieuKham = label("—", 25, true);

    public ThongKeLeTan() {
        super(new GridLayout(1, 4, 14, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(700, 92));
        add(theThongKe("Tổng bệnh nhân", tongBenhNhan, PRIMARY, 0));
        add(theThongKe("Chưa nhận hôm nay", choTiepNhan, WARNING, 1));
        add(theThongKe("Đã tiếp nhận hôm nay", daTiepNhan, GREEN, 2));
        add(theThongKe("Phiếu khám hôm nay", soPhieuKham, new Color(0x8B5CF6), 3));
    }

    public void capNhat(DuLieuTrangLeTan data) {
        tongBenhNhan.setText(String.valueOf(data.totalPatients));
        choTiepNhan.setText(String.valueOf(data.waitingToday));
        daTiepNhan.setText(String.valueOf(data.receivedToday));
        soPhieuKham.setText(String.valueOf(data.examsToday));
    }

    /** Thẻ thống kê: biểu tượng trong ô màu nhạt bên trái, tiêu đề mờ và số liệu lớn. */
    private static JPanel theThongKe(String title, JLabel value, Color accent, int iconKind) {
        KhungBoGoc card = card(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(14, 16, 18, 14));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new HuyHieuBieuTuong(iconKind, accent));

        JPanel texts = new JPanel(new BorderLayout(0, 0));
        texts.setOpaque(false);
        value.setForeground(TEXT);
        value.setFont(font(Font.BOLD, 28));
        texts.add(label(title, 12, false), BorderLayout.NORTH);
        texts.add(value, BorderLayout.CENTER);
        card.add(iconWrap, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        return card;
    }
}
