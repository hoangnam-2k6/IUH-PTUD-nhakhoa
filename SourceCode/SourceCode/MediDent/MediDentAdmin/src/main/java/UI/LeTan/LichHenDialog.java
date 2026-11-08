package UI.LeTan;

import DAO.BacSiDao;
import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.LichHen;
import Entity.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/** Hộp thoại tạo mới / chỉnh sửa lịch hẹn. */
public class LichHenDialog extends JDialog {
    private final LichHenDao lichHenDao = new LichHenDao();
    private final BenhNhanDao benhNhanDao = new BenhNhanDao();
    private final BacSiDao bacSiDao = new BacSiDao();

    private final LichHen lichHen;      // null = tạo mới
    private final Integer maTK;
    private boolean daLuu;

    private final JComboBox<BenhNhan> cbBenhNhan = new JComboBox<>();
    private final JComboBox<BacSi> cbBacSi = new JComboBox<>();
    private final JTextField oNgayGio = taoOText();
    private final JTextArea oGhiChu = taoOVanBan(3);

    private LichHenDialog(Component cha, LichHen lichHen, TaiKhoan taiKhoan) {
        super(SwingUtilities.getWindowAncestor(cha),
                lichHen == null ? "Tạo lịch hẹn mới" : "Sửa lịch hẹn " + String.format("LH%03d", lichHen.getMaLH()),
                ModalityType.APPLICATION_MODAL);
        this.lichHen = lichHen;
        this.maTK = taiKhoan == null ? null : taiKhoan.getMaTK();

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);
        napDuLieu();
        setSize(560, 470);
        setMinimumSize(new Dimension(500, 420));
        setLocationRelativeTo(cha);
    }

    public static boolean taoMoi(Component cha, TaiKhoan taiKhoan) {
        LichHenDialog d = new LichHenDialog(cha, null, taiKhoan);
        d.setVisible(true);
        return d.daLuu;
    }

    public static boolean sua(Component cha, LichHen lichHen) {
        if (lichHen == null) return false;
        LichHenDialog d = new LichHenDialog(cha, lichHen, null);
        d.setVisible(true);
        return d.daLuu;
    }

    private JComponent taoNoiDung() {
        JPanel noi = new JPanel();
        noi.setLayout(new BoxLayout(noi, BoxLayout.Y_AXIS));
        noi.setBackground(NEN_THE);
        noi.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));

        cbBenhNhan.setFont(font(Font.PLAIN, 14));
        cbBacSi.setFont(font(Font.PLAIN, 14));
        oNgayGio.setToolTipText("dd/MM/yyyy HH:mm");

        noi.add(taoTieuDeNhom("Thông tin lịch hẹn"));
        noi.add(truong("Bệnh nhân *", cbBenhNhan));
        noi.add(truong("Bác sĩ *", cbBacSi));
        noi.add(truong("Ngày giờ hẹn * (dd/MM/yyyy HH:mm)", oNgayGio));
        noi.add(truong("Ghi chú", boc(oGhiChu)));

        JLabel goiY = new JLabel("<html><span style='color:#64748B'>Bác sĩ cần cách lịch hẹn trước đó ít nhất 30 phút. "
                + "Lịch mới tạo ở trạng thái \"Chờ xác nhận\".</span></html>");
        goiY.setFont(font(Font.PLAIN, 11));
        goiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        noi.add(Box.createVerticalStrut(6));
        noi.add(goiY);

        JScrollPane cuon = new JScrollPane(noi);
        cuon.setBorder(null);
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        return cuon;
    }

    private JPanel taoChan() {
        JButton huy = taoNutTrang("Hủy");
        JButton luu = taoNut(lichHen == null ? "Tạo lịch hẹn" : "Lưu thay đổi", MAU_CHINH, MAU_CHINH_DAM);
        huy.addActionListener(e -> dispose());
        luu.addActionListener(e -> luu());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(NEN_THE);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, VIEN));
        p.add(huy);
        p.add(luu);
        getRootPane().setDefaultButton(luu);
        return p;
    }

    private void napDuLieu() {
        try {
            List<BenhNhan> dsBenhNhan = new ArrayList<>();
            for (BenhNhan bn : benhNhanDao.findAll()) {
                if (bn.dangHoatDong() || (lichHen != null && bn.getMaBNSo() == lichHen.getMaBN())) {
                    dsBenhNhan.add(bn);
                }
            }
            for (BenhNhan bn : dsBenhNhan) cbBenhNhan.addItem(bn);

            List<BacSi> dsBacSi = new ArrayList<>();
            for (BacSi bs : bacSiDao.findAll()) {
                if (bs.dangHoatDong() || (lichHen != null && bs.getMaBS() == lichHen.getMaBS())) {
                    dsBacSi.add(bs);
                }
            }
            for (BacSi bs : dsBacSi) cbBacSi.addItem(bs);

            if (dsBenhNhan.isEmpty() || dsBacSi.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Cần có ít nhất một bệnh nhân đang hoạt động và một bác sĩ đang hoạt động.",
                        "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            baoLoi(this, e);
        }

        if (lichHen == null) {
            LocalDateTime macDinh = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
            oNgayGio.setText(macDinh.format(NGAY_GIO_NHAP));
            return;
        }
        oNgayGio.setText(lichHen.getNgayGio() == null ? "" : lichHen.getNgayGio().format(NGAY_GIO_NHAP));
        oGhiChu.setText(lichHen.getGhiChu() == null ? "" : lichHen.getGhiChu());
        for (int i = 0; i < cbBenhNhan.getItemCount(); i++) {
            if (cbBenhNhan.getItemAt(i).getMaBNSo() == lichHen.getMaBN()) {
                cbBenhNhan.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cbBacSi.getItemCount(); i++) {
            if (cbBacSi.getItemAt(i).getMaBS() == lichHen.getMaBS()) {
                cbBacSi.setSelectedIndex(i);
                break;
            }
        }
    }

    private void luu() {
        BenhNhan bn = (BenhNhan) cbBenhNhan.getSelectedItem();
        BacSi bs = (BacSi) cbBacSi.getSelectedItem();
        if (bn == null || bs == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân và bác sĩ.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDateTime ngayGio = docNgayGio(oNgayGio.getText());
            if (ngayGio == null) {
                throw new IllegalArgumentException("Vui lòng nhập ngày giờ hẹn.");
            }
            if (lichHen == null) {
                lichHenDao.tao(bn.getMaBNSo(), bs.getMaBS(), ngayGio, oGhiChu.getText(), maTK);
                thongBao(this, "Đã tạo lịch hẹn cho " + bn.getHoTen()
                        + " lúc " + ngayGio.format(NGAY_GIO) + ".");
            } else {
                lichHenDao.capNhat(lichHen.getMaLH(), bn.getMaBNSo(), bs.getMaBS(), ngayGio, oGhiChu.getText());
                thongBao(this, "Đã cập nhật lịch hẹn.");
            }
            daLuu = true;
            dispose();
        } catch (DateTimeParseException e) {
            baoLoiNgay(this, e, "dd/MM/yyyy HH:mm");
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }
}
