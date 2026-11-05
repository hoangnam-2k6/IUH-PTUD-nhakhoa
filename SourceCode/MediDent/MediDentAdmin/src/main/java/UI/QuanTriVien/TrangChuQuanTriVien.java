package UI.QuanTriVien;

import Class.TaiKhoan;
import DAO.TaiKhoanDAO;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Trang chủ Admin: thống kê tài khoản và lịch sử đăng nhập. */
public final class TrangChuQuanTriVien extends JPanel {
    private final TaiKhoanDAO dao;
    private final TaiKhoan admin;
    private final JLabel trangThai = new JLabel(" ");
    private final JLabel tongTaiKhoan = new JLabel("...");
    private final JLabel dangHoatDong = new JLabel("...");
    private final JLabel tamKhoa = new JLabel("...");
    private final JLabel soVaiTro = new JLabel("...");
    private final DefaultTableModel duLieu = new DefaultTableModel(
            new String[]{"STT", "Thời gian", "Tên đăng nhập", "Vai trò", "Kết quả"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public TrangChuQuanTriVien(TaiKhoanDAO dao, TaiKhoan admin) {
        this.dao = dao;
        this.admin = admin;
        setLayout(new BorderLayout(0, 18));
        setBackground(new Color(0xF8FAFC));
        setBorder(new EmptyBorder(30, 34, 28, 34));

        JPanel dauTrang = new JPanel(new BorderLayout());
        dauTrang.setOpaque(false);
        JLabel tieuDe = new JLabel("Trang chủ quản trị viên");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 25));
        tieuDe.setForeground(new Color(0x0F172A));
        JLabel chao = new JLabel("Xin chào, " + admin.hoTen);
        chao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chao.setForeground(new Color(0x64748B));
        JPanel tieuDeVaChao = new JPanel(new GridLayout(2, 1, 0, 6));
        tieuDeVaChao.setOpaque(false);
        tieuDeVaChao.add(tieuDe);
        tieuDeVaChao.add(chao);
        dauTrang.add(tieuDeVaChao, BorderLayout.WEST);
        JButton taiLai = new JButton("Tải lại");
        taiLai.setBackground(Color.WHITE);
        taiLai.setForeground(new Color(0x0F172A));
        taiLai.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0), 1, true),
                new EmptyBorder(10, 17, 10, 17)));
        taiLai.setFocusPainted(false);
        taiLai.addActionListener(e -> {
            taiThongKe();
            taiLichSu();
        });
        dauTrang.add(taiLai, BorderLayout.EAST);
        add(dauTrang, BorderLayout.NORTH);

        JPanel khung = new JPanel(new BorderLayout(0, 14));
        khung.setBackground(Color.WHITE);
        khung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(20, 20, 18, 20)));
        JLabel nhan = new JLabel("Lịch sử đăng nhập của các vai trò");
        nhan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        khung.add(nhan, BorderLayout.NORTH);
        JTable bang = new JTable(duLieu) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer ren, int row, int col) {
                Component c = super.prepareRenderer(ren, row, col);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8FAFC));
                if (col == 0 && c instanceof JLabel) ((JLabel) c).setText(String.valueOf(row + 1));
                return c;
            }
        };
        bang.setRowHeight(37);
        bang.setAutoCreateRowSorter(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getColumnModel().getColumn(0).setMaxWidth(60);
        bang.getColumnModel().getColumn(1).setPreferredWidth(160);
        bang.getColumnModel().getColumn(2).setPreferredWidth(150);
        bang.getColumnModel().getColumn(3).setPreferredWidth(180);
        bang.getColumnModel().getColumn(4).setPreferredWidth(110);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        cuon.getViewport().setBackground(Color.WHITE);
        bang.setFillsViewportHeight(true);
        khung.add(cuon, BorderLayout.CENTER);
        trangThai.setForeground(new Color(0x64748B));
        khung.add(trangThai, BorderLayout.SOUTH);
        JPanel thongKe = new JPanel(new GridLayout(1, 4, 14, 0));
        thongKe.setOpaque(false);
        thongKe.add(theThongKe("Tổng tài khoản", tongTaiKhoan));
        thongKe.add(theThongKe("Đang hoạt động", dangHoatDong));
        thongKe.add(theThongKe("Tạm khóa", tamKhoa));
        thongKe.add(theThongKe("Vai trò", soVaiTro));
        JPanel thanTrang = new JPanel(new BorderLayout(0, 18));
        thanTrang.setOpaque(false);
        thanTrang.add(thongKe, BorderLayout.NORTH);
        thanTrang.add(khung, BorderLayout.CENTER);
        add(thanTrang, BorderLayout.CENTER);
        taiThongKe();
        taiLichSu();
    }

    private JPanel theThongKe(String ten, JLabel so) {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(18, 20, 18, 20)));
        JLabel nhan = new JLabel(ten);
        nhan.setForeground(new Color(0x64748B));
        nhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        so.setFont(new Font("Segoe UI", Font.BOLD, 28));
        so.setForeground(new Color(0x0284C7));
        the.add(nhan, BorderLayout.NORTH);
        the.add(so, BorderLayout.CENTER);
        return the;
    }

    private void taiThongKe() {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() throws Exception {
                return dao.thongKeTaiKhoan(admin.id);
            }
            @Override protected void done() {
                try {
                    int[] so = get();
                    tongTaiKhoan.setText(String.valueOf(so[0]));
                    dangHoatDong.setText(String.valueOf(so[1]));
                    tamKhoa.setText(String.valueOf(so[2]));
                    soVaiTro.setText(String.valueOf(so[3]));
                } catch (Exception e) {
                    tongTaiKhoan.setText("—");
                    dangHoatDong.setText("—");
                    tamKhoa.setText("—");
                    soVaiTro.setText("—");
                }
            }
        }.execute();
    }

    private void taiLichSu() {
        trangThai.setText("Đang tải lịch sử...");
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() throws Exception {
                return dao.lichSuDangNhap(admin.id);
            }
            @Override protected void done() {
                try {
                    duLieu.setRowCount(0);
                    List<String[]> danhSach = get();
                    for (int i = 0; i < danhSach.size(); i++) {
                        String[] hang = danhSach.get(i);
                        duLieu.addRow(new Object[]{i + 1, hang[0], hang[1], hang[2], hang[3]});
                    }
                } catch (Exception e) {
                    trangThai.setText("Không tải được lịch sử đăng nhập.");
                }
            }
        }.execute();
    }
}
