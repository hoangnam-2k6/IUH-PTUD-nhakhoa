package UI.QuanLy;

import DAO.TrangChuDAO;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class TrangChuQuanLyPanel extends JPanel {

    private final TrangChuDAO dao = new TrangChuDAO();
    private final JLabel soBenhNhan = new JLabel("--");
    private final JLabel soLichHen = new JLabel("--");
    private final JLabel soCa = new JLabel("--");
    private final JLabel doanhThu = new JLabel("--");
    private final JLabel trangThai = new JLabel("Đang chuẩn bị dữ liệu...");

    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Mã lịch", "Bệnh nhân", "Ngày hẹn", "Giờ", "Bác sĩ", "Trạng thái"
    }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable bang = new JTable(model);
    private boolean dangTai;

    public TrangChuQuanLyPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel tren = new JPanel(new BorderLayout(0, 14));
        tren.setOpaque(false);

        JPanel tieuDe = new JPanel(new BorderLayout());
        tieuDe.setOpaque(false);
        tieuDe.add(GiaoDienChung.tieuDe("TỔNG QUAN PHÒNG KHÁM"), BorderLayout.WEST);

        JButton tai = GiaoDienChung.nut("Làm mới");
        tieuDe.add(tai, BorderLayout.EAST);

        JPanel cacThe = new JPanel(new GridLayout(1, 4, 10, 0));
        cacThe.setOpaque(false);

        cacThe.add(theThongKe("Bệnh nhân", soBenhNhan));
        cacThe.add(theThongKe("Lịch hẹn hôm nay", soLichHen));
        cacThe.add(theThongKe("Ca làm việc hôm nay", soCa));
        cacThe.add(theThongKe("Doanh thu tháng (VNĐ)", doanhThu));

        tren.add(tieuDe, BorderLayout.NORTH);
        tren.add(cacThe, BorderLayout.CENTER);
        add(tren, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 9));
        giua.setOpaque(false);

        JLabel tenBang = new JLabel("Lịch hẹn sắp tới");
        tenBang.setFont(new Font("Arial", Font.BOLD, 15));
        tenBang.setForeground(GiaoDienChung.CHU);
        giua.add(tenBang, BorderLayout.NORTH);

        GiaoDienChung.bang(bang);

        JScrollPane cuon = new JScrollPane(bang);
        cuon.setBorder(BorderFactory.createLineBorder(GiaoDienChung.VIEN));

        giua.add(cuon, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        trangThai.setFont(new Font("Arial", Font.PLAIN, 12));
        add(trangThai, BorderLayout.SOUTH);

        tai.addActionListener(e -> taiDuLieu());

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                taiDuLieu();
            }
        });
    }

    private JPanel theThongKe(String ten, JLabel so) {
        JPanel the = new JPanel(new BorderLayout(0, 12));
        the.setBackground(new Color(246, 250, 253));
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDienChung.VIEN),
                BorderFactory.createEmptyBorder(13, 12, 13, 12)
        ));

        JLabel nhan = new JLabel(ten);
        nhan.setFont(new Font("Arial", Font.PLAIN, 12));

        so.setFont(new Font("Arial", Font.BOLD, 19));
        so.setForeground(GiaoDienChung.XANH);

        the.add(nhan, BorderLayout.NORTH);
        the.add(so, BorderLayout.CENTER);
        return the;
    }

    public void taiDuLieu() {
        if (dangTai) return;

        dangTai = true;
        trangThai.setText("Đang tải dữ liệu...");

        new SwingWorker<Object[], Void>() {

            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{dao.layTongQuan(), dao.layLichSapToi()};
            }

            @Override
            protected void done() {
                try {
                    Object[] duLieu = get();
                    TrangChuDAO.TongQuan t = (TrangChuDAO.TongQuan) duLieu[0];

                    soBenhNhan.setText(String.valueOf(t.benhNhan()));
                    soLichHen.setText(String.valueOf(t.lichHen()));
                    soCa.setText(String.valueOf(t.caLamViec()));

                    NumberFormat dinhDang = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                    doanhThu.setText(dinhDang.format(t.doanhThu()));

                    model.setRowCount(0);

                    @SuppressWarnings("unchecked")
                    List<TrangChuDAO.LichSapToi> ds = (List<TrangChuDAO.LichSapToi>) duLieu[1];

                    for (TrangChuDAO.LichSapToi l : ds) {
                        model.addRow(new Object[]{
                                l.maLich(), l.benhNhan(), l.ngay(),
                                l.gio(), l.bacSi(), l.trangThai()
                        });
                    }

                    trangThai.setText("Hóa đơn chờ thanh toán: " + t.choThanhToan()
                            + "   |   Lịch sắp tới: " + ds.size());

                } catch (Exception x) {
                    trangThai.setText("Không tải được Trang chủ.");
                    GiaoDienChung.baoLoi(TrangChuQuanLyPanel.this, x);
                } finally {
                    dangTai = false;
                }
            }
        }.execute();
    }
}