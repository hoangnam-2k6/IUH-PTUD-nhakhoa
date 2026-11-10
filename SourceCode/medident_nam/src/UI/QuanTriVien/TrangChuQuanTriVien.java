
package UI.QuanTriVien;

import DAO.TaiKhoanDAO;
import Entity.TaiKhoan;
import UI.Chung.GiaoDienHeThong;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
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
        @Override
        public boolean isCellEditable(int dong, int cot) {
            return false;
        }
    };

    private final JTable bang = new JTable(duLieu);

    public TrangChuQuanTriVien(TaiKhoanDAO dao, TaiKhoan admin) {
        this.dao = dao;
        this.admin = admin;

        setLayout(new BorderLayout(0, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel phanTren = new JPanel(new BorderLayout());
        phanTren.setOpaque(false);

        JPanel tieuDe = new JPanel(new GridLayout(2, 1, 0, 4));
        tieuDe.setOpaque(false);

        JLabel tenTrang = GiaoDienHeThong.label(
                "TRANG CHỦ QUẢN TRỊ VIÊN", 20, true);
        tenTrang.setForeground(GiaoDienHeThong.PRIMARY);

        tieuDe.add(tenTrang);
        tieuDe.add(GiaoDienHeThong.label(
                "Xin chào, " + admin.hoTen, 12, false));

        phanTren.add(tieuDe, BorderLayout.WEST);

        JButton taiLai = GiaoDienHeThong.nut("Tải lại", false);
        taiLai.addActionListener(e -> {
            taiThongKe();
            taiLichSu();
        });

        phanTren.add(taiLai, BorderLayout.EAST);
        add(phanTren, BorderLayout.NORTH);

        // Bốn thẻ tổng quan.
        JPanel theThongKe = new JPanel(new GridLayout(1, 4, 8, 0));
        theThongKe.setOpaque(false);

        theThongKe.add(taoThe("Tổng tài khoản", tongTaiKhoan));
        theThongKe.add(taoThe("Đang hoạt động", dangHoatDong));
        theThongKe.add(taoThe("Tạm khóa", tamKhoa));
        theThongKe.add(taoThe("Số vai trò", soVaiTro));

        // Bảng lịch sử đăng nhập.
        JPanel khungBang = GiaoDienHeThong.card(new BorderLayout(0, 9));

        JLabel nhanBang = GiaoDienHeThong.label(
                "Lịch sử đăng nhập", 14, true);
        khungBang.add(nhanBang, BorderLayout.NORTH);

        khungBang.add(GiaoDienHeThong.wrapTable(bang),
                BorderLayout.CENTER);

        trangThai.setFont(GiaoDienHeThong.FONT);
        trangThai.setForeground(GiaoDienHeThong.MUTED);
        khungBang.add(trangThai, BorderLayout.SOUTH);

        JPanel noiDung = new JPanel(new BorderLayout(0, 12));
        noiDung.setOpaque(false);
        noiDung.add(theThongKe, BorderLayout.NORTH);
        noiDung.add(khungBang, BorderLayout.CENTER);

        add(noiDung, BorderLayout.CENTER);

        taiThongKe();
        taiLichSu();
    }

    // Tạo một thẻ thống kê.
    private JPanel taoThe(String ten, JLabel so) {
        JPanel p = GiaoDienHeThong.card(new BorderLayout(0, 6));
        p.add(GiaoDienHeThong.label(ten, 12, false), BorderLayout.NORTH);

        so.setFont(new Font("Arial", Font.BOLD, 19));
        so.setForeground(GiaoDienHeThong.PRIMARY);

        p.add(so, BorderLayout.CENTER);
        return p;
    }

    // Tải thống kê tài khoản từ DAO.
    private void taiThongKe() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return dao.thongKeTaiKhoan(admin.id);
            }

            @Override
            protected void done() {
                try {
                    int[] so = get();

                    tongTaiKhoan.setText(String.valueOf(so[0]));
                    dangHoatDong.setText(String.valueOf(so[1]));
                    tamKhoa.setText(String.valueOf(so[2]));
                    soVaiTro.setText(String.valueOf(so[3]));
                } catch (Exception loi) {
                    tongTaiKhoan.setText("—");
                    dangHoatDong.setText("—");
                    tamKhoa.setText("—");
                    soVaiTro.setText("—");
                    trangThai.setText("Không tải được thống kê tài khoản.");
                }
            }
        }.execute();
    }

    // Tải lịch sử đăng nhập từ SQL Server.
    private void taiLichSu() {
        trangThai.setText("Đang tải lịch sử đăng nhập...");

        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return dao.lichSuDangNhap(admin.id);
            }

            @Override
            protected void done() {
                try {
                    List<String[]> danhSach = get();
                    duLieu.setRowCount(0);

                    for (int i = 0; i < danhSach.size(); i++) {
                        String[] dong = danhSach.get(i);

                        duLieu.addRow(new Object[]{
                            i + 1, dong[0], dong[1], dong[2], dong[3]
                        });
                    }

                    trangThai.setText(danhSach.isEmpty()
                            ? "Chưa có lịch sử đăng nhập."
                            : "Số lượt đăng nhập hiển thị: " + danhSach.size());
                } catch (Exception loi) {
                    trangThai.setText("Không tải được lịch sử đăng nhập.");
                }
            }
        }.execute();
    }
}
