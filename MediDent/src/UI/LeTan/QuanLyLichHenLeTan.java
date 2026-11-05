package UI.LeTan;

import Dao.BacSiDao;
import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.LichHen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

public class QuanLyLichHenLeTan extends JPanel {
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final LichHenDao lichHenDao = new LichHenDao();
    private final BacSiDao bacSiDao = new BacSiDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private List<LichHen> dsLich = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã LH", "Bệnh nhân", "Ngày giờ", "Bác sĩ", "Trạng thái", "Yêu cầu"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = GiaoDien.taoBang(model);

    public QuanLyLichHenLeTan() {
        setLayout(new BorderLayout());
        setBackground(NEN_APP);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel the = GiaoDien.taoThe("Lịch hẹn", "Chọn một dòng rồi bấm nút bên dưới để thao tác.");

        JButton nutTao = taoNut("Tạo lịch hẹn mới", MAU_CHINH, MAU_CHINH_DAM);
        JButton nutCapNhat = taoNut("Cập nhật trạng thái", MAU_CHINH, MAU_CHINH_DAM);
        JButton nutYeuCau = taoNut("Xử lý yêu cầu đổi/hủy", CANH_BAO, CANH_BAO.darker());
        nutTao.addActionListener(e -> taoLichHen());
        nutCapNhat.addActionListener(e -> capNhatTrangThai());
        nutYeuCau.addActionListener(e -> xuLyYeuCau());

        JPanel thanhNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        thanhNut.setOpaque(false);
        thanhNut.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        thanhNut.add(nutYeuCau);
        thanhNut.add(nutCapNhat);
        thanhNut.add(nutTao);

        the.add(new JScrollPane(bang), BorderLayout.CENTER);
        the.add(thanhNut, BorderLayout.SOUTH);
        add(the, BorderLayout.CENTER);
    }

    public void taiDuLieu() {
        try {
            dsLich = lichHenDao.findAll();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }
        model.setRowCount(0);
        for (LichHen lh : dsLich) {
            String yeuCau = "";
            if (lh.getYeuCau() != null) {
                yeuCau = lh.getYeuCau();
                if (lh.getNgayGioMoi() != null) yeuCau += " → " + lh.getNgayGioMoi().format(NGAY_GIO);
            }
            model.addRow(new Object[]{String.format("LH%03d", lh.getMaLH()), lh.getTenBenhNhan(),
                    lh.getNgayGio().format(NGAY_GIO), lh.getTenBacSi(), lh.getTrangThai(), yeuCau});
        }
    }

    private void taoLichHen() {
        List<BenhNhan> dsBenhNhan;
        List<BacSi> dsBacSi;
        try {
            dsBenhNhan = benhNhanDao.findAll();
            dsBacSi = bacSiDao.findAll();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
            return;
        }
        if (dsBenhNhan.isEmpty() || dsBacSi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cần có bệnh nhân và bác sĩ trong hệ thống để tạo lịch hẹn.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComboBox<BenhNhan> cbBenhNhan = new JComboBox<>(dsBenhNhan.toArray(new BenhNhan[0]));
        JComboBox<BacSi> cbBacSi = new JComboBox<>(dsBacSi.toArray(new BacSi[0]));
        JTextField oNgayGio = new JTextField(LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0).format(NGAY_GIO));
        JTextField oGhiChu = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Bệnh nhân:"));
        form.add(cbBenhNhan);
        form.add(new JLabel("Bác sĩ:"));
        form.add(cbBacSi);
        form.add(new JLabel("Ngày giờ (dd/MM/yyyy HH:mm):"));
        form.add(oNgayGio);
        form.add(new JLabel("Ghi chú:"));
        form.add(oGhiChu);

        int kq = JOptionPane.showConfirmDialog(this, form, "Tạo lịch hẹn mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (kq != JOptionPane.OK_OPTION) return;

        try {
            LocalDateTime ngayGio = LocalDateTime.parse(oNgayGio.getText().trim(), NGAY_GIO);
            BenhNhan bn = (BenhNhan) cbBenhNhan.getSelectedItem();
            BacSi bs = (BacSi) cbBacSi.getSelectedItem();
            lichHenDao.tao(bn.getMaBN(), bs.getMaBS(), ngayGio, oGhiChu.getText());
            JOptionPane.showMessageDialog(this, "Đã tạo lịch hẹn.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            taiDuLieu();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày giờ không đúng định dạng dd/MM/yyyy HH:mm.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void capNhatTrangThai() {
        LichHen lh = layLichDangChon();
        if (lh == null) return;
        Object chon = JOptionPane.showInputDialog(this, "Chọn trạng thái mới:", "Cập nhật trạng thái",
                JOptionPane.PLAIN_MESSAGE, null, LichHenDao.TRANG_THAI, lh.getTrangThai());
        if (chon == null) return;
        try {
            lichHenDao.capNhatTrangThai(lh.getMaLH(), (String) chon);
            taiDuLieu();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private void xuLyYeuCau() {
        LichHen lh = layLichDangChon();
        if (lh == null) return;
        if (lh.getYeuCau() == null) {
            JOptionPane.showMessageDialog(this, "Lịch hẹn này không có yêu cầu đổi/hủy.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String noiDung = lh.getTenBenhNhan() + " yêu cầu: " + lh.getYeuCau();
        if (lh.getNgayGioMoi() != null) noiDung += "\nThời gian mới: " + lh.getNgayGioMoi().format(NGAY_GIO);
        int chon = JOptionPane.showOptionDialog(this, noiDung, "Xử lý yêu cầu đổi/hủy",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Chấp nhận", "Từ chối", "Đóng"}, "Chấp nhận");
        if (chon != 0 && chon != 1) return;
        try {
            lichHenDao.xuLyYeuCau(lh.getMaLH(), chon == 0);
            taiDuLieu();
        } catch (Exception e) {
            GiaoDien.baoLoi(this, e);
        }
    }

    private LichHen layLichDangChon() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch hẹn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return dsLich.get(dong);
    }
}
