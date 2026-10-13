package UI.QuanTriVien;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Báo cáo GIỚI HẠN cho Admin: chỉ tài khoản, vai trò và nhật ký đăng nhập.
 * Không gọi DAO doanh thu, khám bệnh hay thanh toán.
 * Thao tác và xuất CSV đều kiểm tra lại vai trò Admin đang hoạt động trong SQL.
 */
public final class BaoCaoHeThongAdmin extends JPanel {
    private final KhoDuLieu db;
    private final TaiKhoan actor;
    private final JComboBox<String> loai = new JComboBox<>(new String[]{
        "Phân bố tài khoản theo vai trò",
        "Tăng trưởng tài khoản theo tháng",
        "Nhật ký đăng nhập (20 gần nhất)"
    });
    private final JComboBox<Integer> nam = new JComboBox<>();
    private final JLabel tong = GiaoDien.label("--", 25, true);
    private final JLabel dangHoatDong = GiaoDien.label("--", 25, true);
    private final JLabel soVaiTro = GiaoDien.label("--", 25, true);
    private final JLabel canhBao = GiaoDien.label("--", 25, true);
    private final JLabel moTa = GiaoDien.label("Dữ liệu lấy từ SQL Server", 11, false);
    private final BieuDoCot bieuDo = new BieuDoCot();
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable bang = new JTable(model) {
        @Override public java.awt.Component prepareRenderer(
                javax.swing.table.TableCellRenderer renderer, int row, int col) {
            java.awt.Component c = super.prepareRenderer(renderer, row, col);
            if (col == 0 && c instanceof JLabel) {
                ((JLabel)c).setText(String.valueOf(row + 1));
                ((JLabel)c).setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            }
            return c;
        }
    };

    public BaoCaoHeThongAdmin(KhoDuLieu db, TaiKhoan actor) {
        this.db = db;
        this.actor = actor;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        JPanel page = GiaoDien.trang("Báo cáo hệ thống - Quản trị viên",
                "Giới hạn trong tài khoản, vai trò và nhật ký bảo mật. Không xem số liệu tài chính hay bệnh nhân.");
        add(GiaoDien.cuon(page), BorderLayout.CENTER);

        JPanel filter = GiaoDien.card(new BorderLayout(10, 10));
        JPanel fields = GiaoDien.hang();
        fields.add(GiaoDien.label("Loại báo cáo", 12, true));
        fields.add(loai);
        fields.add(GiaoDien.label("Năm", 12, true));
        int year = LocalDate.now().getYear();
        for(int i = year - 4; i <= year + 1; i++) nam.addItem(i);
        nam.setSelectedItem(year);
        fields.add(nam);
        JButton apply = GiaoDien.nutIcon("Áp dụng", true, BieuTuong.Loai.TIM_KIEM);
        JButton export = GiaoDien.nutIcon("Xuất CSV", false, BieuTuong.Loai.TAI_XUONG);
        fields.add(apply);
        fields.add(export);
        filter.add(fields, BorderLayout.NORTH);
        filter.add(moTa, BorderLayout.SOUTH);
        page.add(filter);
        page.add(Box.createVerticalStrut(15));

        JPanel kpi = new JPanel(new GridLayout(2, 2, 13, 13));
        kpi.setOpaque(false);
        kpi.setAlignmentX(LEFT_ALIGNMENT);
        kpi.setPreferredSize(new Dimension(720, 235));
        kpi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));
        kpi.add(the("Tổng tài khoản", tong));
        kpi.add(the("Tài khoản hoạt động", dangHoatDong));
        kpi.add(the("Số vai trò", soVaiTro));
        kpi.add(the("Đăng nhập lỗi 7 ngày", canhBao));
        page.add(kpi);
        page.add(Box.createVerticalStrut(15));

        JPanel chartCard = GiaoDien.card(new BorderLayout(0, 10));
        chartCard.setAlignmentX(LEFT_ALIGNMENT);
        chartCard.add(GiaoDien.label("Tăng trưởng tài khoản trong năm", 15, true), BorderLayout.NORTH);
        chartCard.add(bieuDo, BorderLayout.CENTER);
        chartCard.setPreferredSize(new Dimension(720, 275));
        page.add(chartCard);
        page.add(Box.createVerticalStrut(15));

        JPanel card = GiaoDien.card(new BorderLayout(0, 10));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(720, 410));
        card.add(GiaoDien.label("Chi tiết giới hạn theo quyền Admin", 15, true), BorderLayout.NORTH);
        card.add(GiaoDien.wrapTable(bang), BorderLayout.CENTER);
        page.add(card);
        page.add(Box.createVerticalStrut(20));

        apply.addActionListener(e -> taiLai());
        loai.addActionListener(e -> taiLai());
        nam.addActionListener(e -> taiLai());
        export.addActionListener(e -> xuat());
        taiLai();
    }

    private JPanel the(String caption, JLabel value) {
        JPanel c = GiaoDien.card(new BorderLayout(0, 12));
        c.add(GiaoDien.label(caption, 12, false), BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    private void kiemTra() throws Exception {
        if (!db.laQuanTriVienTaiKhoan(actor.id) ||
                !db.coQuyenTaiKhoan(actor.id, "BAOCAO")) {
            throw new SecurityException("Không còn quyền truy cập báo cáo hệ thống.");
        }
    }

    private void taiLai() {
        try {
            kiemTra();
            tong.setText(String.valueOf(db.dem("SELECT COUNT(*) FROM dbo.TaiKhoan")));
            dangHoatDong.setText(String.valueOf(db.dem(
                    "SELECT COUNT(*) FROM dbo.TaiKhoan WHERE trangThai=N'Hoạt động'")));
            soVaiTro.setText(String.valueOf(db.dem("SELECT COUNT(*) FROM dbo.VaiTro")));
            canhBao.setText(String.valueOf(db.dem(
                    "SELECT COUNT(*) FROM dbo.NhatKyHeThong WHERE hanhDong LIKE N'%thất bại%' " +
                    "AND thoiGian>=DATEADD(day,-7,GETDATE())")));

            Integer year = (Integer) nam.getSelectedItem();
            if (year == null) return;
            int[] monthly = db.tangTruong(year);
            bieuDo.setDuLieu(monthly);
            List<String[]> rows = new ArrayList<>();
            String[] head;
            switch (loai.getSelectedIndex()) {
                case 1:
                    head = new String[]{"STT", "Tháng", "Tài khoản lũy kế"};
                    for (int i = 0; i < 12; i++) {
                        rows.add(new String[]{"Tháng " + (i + 1), String.valueOf(monthly[i])});
                    }
                    break;
                case 2:
                    head = new String[]{"STT", "Thời điểm", "Hành động", "Đối tượng"};
                    for (String[] r : db.nhatKy()) {
                        if (!r[1].toLowerCase(java.util.Locale.ROOT).contains("đăng nhập")
                                || !r[0].startsWith(String.valueOf(year))) continue;
                        rows.add(r);
                        if (rows.size() == 20) break;
                    }
                    break;
                default:
                    head = new String[]{"STT", "Vai trò", "Số tài khoản"};
                    rows.addAll(db.phanBo());
                    break;
            }
            model.setColumnIdentifiers(head);
            model.setRowCount(0);
            for (String[] values : rows) {
                Object[] data = new Object[values.length + 1];
                data[0] = model.getRowCount() + 1;
                System.arraycopy(values, 0, data, 1, values.length);
                model.addRow(data);
            }
            moTa.setText("Năm " + year + " · " + rows.size()
                    + " dòng · Chỉ dữ liệu hệ thống được phép hiển thị");
        } catch (Exception ex) {
            GiaoDien.loi(this, ex);
        }
    }

    /** Xuất những cột được phép hiển thị; chặn công thức nguy hiểm trong CSV. */
    private void xuat() {
        try {
            kiemTra();
            JFileChooser choose = new JFileChooser();
            choose.setSelectedFile(new java.io.File("MediDent_BaoCaoHeThong.csv"));
            if (choose.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            StringBuilder sb = new StringBuilder("\uFEFF");
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) sb.append(',');
                sb.append(csv(model.getColumnName(c)));
            }
            sb.append('\n');
            for (int r = 0; r < bang.getRowCount(); r++) {
                for (int c = 0; c < bang.getColumnCount(); c++) {
                    if (c > 0) sb.append(',');
                    sb.append(csv(String.valueOf(c == 0 ? r + 1 : bang.getValueAt(r,c))));
                }
                sb.append('\n');
            }
            Files.write(choose.getSelectedFile().toPath(),
                    sb.toString().getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu được phép xem.");
        } catch (Exception ex) { GiaoDien.loi(this, ex); }
    }

    private static String csv(String s) {
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    /** Biểu đồ Java2D: theo 12 tháng, không dùng ký tự icon Unicode. */
    private static final class BieuDoCot extends JPanel {
        private int[] values = new int[12];
        BieuDoCot() { setOpaque(false); setPreferredSize(new Dimension(600, 205)); }
        void setDuLieu(int[] data) { values = data.clone(); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = Math.max(1,getWidth() - 42), h = Math.max(1,getHeight() - 37);
            int max = 1;
            for (int a : values) max = Math.max(max,a);
            g2.setColor(new Color(0xE2E8F0));
            g2.setStroke(new BasicStroke(1));
            for (int n=0;n<=4;n++) g2.drawLine(36,8+n*h/4,36+w,8+n*h/4);
            int dx = Math.max(1,w/12);
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            for(int n=0;n<12;n++){
                int bar = Math.max(1,(values[n] * (h-9) / max));
                int x = 36+n*dx+dx/5;
                int y = 8+h-bar;
                g2.setColor(new Color(0x0284C7));
                g2.fillRoundRect(x,y,Math.max(5,3*dx/5),bar,6,6);
                g2.setColor(new Color(0x64748B));
                g2.drawString("T"+(n+1),x,19+h);
            }
            g2.dispose();
        }
    }
}
