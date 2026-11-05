package UI.LeTan;

import Dao.HoaDonDao;
import Entity.HoaDon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.LeTan.GiaoDien.*;

public class ThanhToanHoaDonLeTan extends JPanel {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final HoaDonDao hoaDonDao = new HoaDonDao();
    private List<HoaDon> dsHoaDon = new ArrayList<>();
    private File thuMucCuoi = new File(System.getProperty("user.home"));

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã HĐ", "Bệnh nhân", "Ngày lập", "Tổng tiền (VNĐ)", "Phương thức", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);
    private final JLabel thongKe = new JLabel(" ");

    public ThanhToanHoaDonLeTan() {
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Thanh toán & hóa đơn",
                "Chọn một hóa đơn rồi bấm \"Xuất hóa đơn\" (hoặc bấm đúp vào dòng) để lưu file in được.");

        thongKe.setFont(font(Font.BOLD, 12));
        thongKe.setForeground(MAU_CHINH);
        JButton nutXuat = taoNut("Xuất hóa đơn", MAU_CHINH, MAU_CHINH_DAM);
        nutXuat.addActionListener(e -> xuatHoaDon());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(nutXuat);
        JPanel hang = new JPanel(new BorderLayout());
        hang.setOpaque(false);
        hang.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        hang.add(thongKe, BorderLayout.WEST);
        hang.add(nut, BorderLayout.EAST);

        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) xuatHoaDon();
            }
        });

        JPanel giua = new JPanel(new BorderLayout());
        giua.setOpaque(false);
        giua.add(hang, BorderLayout.NORTH);
        giua.add(new JScrollPane(bang), BorderLayout.CENTER);
        the.add(giua, BorderLayout.CENTER);
        add(the, BorderLayout.CENTER);
    }

    public void taiDuLieu() {
        try {
            dsHoaDon = hoaDonDao.findAll();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }
        NumberFormat tien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        model.setRowCount(0);
        long chuaThu = 0;
        int soChua = 0;
        for (HoaDon hd : dsHoaDon) {
            model.addRow(new Object[]{String.format("HD%03d", hd.getMaHD()), hd.getTenBenhNhan(),
                    hd.getNgayLap().format(NGAY), tien.format(hd.getTongTien()),
                    hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT(), hd.getTrangThai()});
            if ("Chưa thanh toán".equals(hd.getTrangThai())) {
                soChua++;
                chuaThu += hd.getTongTien().longValue();
            }
        }
        thongKe.setText(dsHoaDon.size() + " hóa đơn  ·  " + soChua + " chưa thanh toán ("
                + tien.format(chuaThu) + " đ)");
    }

    private void xuatHoaDon() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= dsHoaDon.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong danh sách.",
                    "Chưa chọn hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            HoaDon hd = hoaDonDao.findDayDu(dsHoaDon.get(dong).getMaHD());
            if (hd == null) {
                throw new IllegalArgumentException("Hóa đơn không còn tồn tại, vui lòng làm mới danh sách.");
            }
            JFileChooser chon = new JFileChooser(thuMucCuoi);
            chon.setDialogTitle("Lưu hóa đơn");
            chon.setFileFilter(new FileNameExtensionFilter("Trang HTML (*.html)", "html"));
            chon.setSelectedFile(new File(thuMucCuoi, XuatHoaDon.tenFileGoiY(hd)));
            if (chon.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File file = chon.getSelectedFile();
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
                file = new File(file.getParentFile(), file.getName() + ".html");
            }
            if (file.exists() && JOptionPane.showConfirmDialog(this,
                    "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            XuatHoaDon.ghiFile(hd, file.toPath());
            thuMucCuoi = file.getParentFile();

            int mo = JOptionPane.showConfirmDialog(this,
                    "Đã lưu hóa đơn:\n" + file.getAbsolutePath()
                            + "\n\nMở bằng trình duyệt để in hoặc lưu PDF ngay bây giờ?",
                    "Xuất hóa đơn thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (mo == JOptionPane.YES_OPTION) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(file.toURI());
                } else {
                    JOptionPane.showMessageDialog(this, "Máy này không hỗ trợ mở trình duyệt tự động. Hãy mở file thủ công.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "Không ghi được file: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }
}
