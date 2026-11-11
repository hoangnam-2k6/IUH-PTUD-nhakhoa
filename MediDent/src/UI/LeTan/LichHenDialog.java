package UI.LeTan;

import Dao.BacSiDao;
import Dao.BenhNhanDao;
import Dao.LichHenDao;
import Entity.BacSi;
import Entity.BenhNhan;
import Entity.LichHen;
import Entity.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private final JComboBox<BenhNhan> cbBenhNhan = new JComboBox<>();
    private final JComboBox<BacSi> cbBacSi = new JComboBox<>();
    private final ONgay oNgay = new ONgay();
    private final JComboBox<String> cbGio = new JComboBox<>();
    private final JTextArea oGhiChu = taoOVanBan(3);

    private LichHenDialog(Component cha, LichHen lichHen, TaiKhoan taiKhoan) {
        super(cha == null ? null : SwingUtilities.getWindowAncestor(cha),
                lichHen == null ? "Tạo lịch hẹn mới" : "Sửa lịch hẹn " + String.format("LH%03d", lichHen.getMaLH()),
                ModalityType.APPLICATION_MODAL);
        this.lichHen = lichHen;
        this.maTK = taiKhoan == null ? null : taiKhoan.getMaTK();

        setLayout(new BorderLayout());
        getContentPane().setBackground(NEN_THE);
        add(taoNoiDung(), BorderLayout.CENTER);
        add(taoChan(), BorderLayout.SOUTH);
        napDuLieu();
        setSize(600, 500);
        setMinimumSize(new Dimension(520, 440));
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
        oNgay.oNhap().setToolTipText("Bắt buộc - chọn trên lịch hoặc nhập dd/MM/yyyy");
        cbGio.setFont(font(Font.PLAIN, 14));
        for (int phut = 7 * 60; phut <= 19 * 60 + 30; phut += 30) {
            cbGio.addItem(String.format("%02d:%02d", phut / 60, phut % 60));
        }

        noi.add(taoTieuDeNhom("Thông tin lịch hẹn"));
        noi.add(truong("Bệnh nhân *" + (lichHen == null ? "" : " (không đổi khi sửa)"), cbBenhNhan));
        noi.add(truong("Bác sĩ *", cbBacSi));
        noi.add(hai(truong("Ngày hẹn *", oNgay), truong("Giờ hẹn *", cbGio)));
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

    private JPanel hai(JComponent trai, JComponent phai) {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setOpaque(false);
        p.add(trai);
        p.add(phai);
        return p;
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
                if (bn.dangHoatDong() || (lichHen != null && bn.getMaBN() == lichHen.getMaBN())) {
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
            oNgay.datNgay(LocalDate.now().plusDays(1));
            cbGio.setSelectedItem("09:00");
            return;
        }

        // Khi sửa, không cho đổi bệnh nhân của lịch hẹn.
        cbBenhNhan.setEnabled(false);

        LocalDateTime ngayGio = lichHen.getNgayGio();
        if (ngayGio != null) {
            oNgay.datNgay(ngayGio.toLocalDate());
            chonGio(ngayGio.toLocalTime());
        }
        oGhiChu.setText(lichHen.getGhiChu() == null ? "" : lichHen.getGhiChu());
        for (int i = 0; i < cbBenhNhan.getItemCount(); i++) {
            if (cbBenhNhan.getItemAt(i).getMaBN() == lichHen.getMaBN()) {
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

    /** Chọn giờ có sẵn trong combobox; nếu chưa có (dữ liệu cũ) thì thêm vào. */
    private void chonGio(LocalTime gio) {
        String chu = gio.format(GIO);
        for (int i = 0; i < cbGio.getItemCount(); i++) {
            if (chu.equals(cbGio.getItemAt(i))) {
                cbGio.setSelectedIndex(i);
                return;
            }
        }
        cbGio.addItem(chu);
        cbGio.setSelectedItem(chu);
    }

    private void luu() {
        BenhNhan bn = (BenhNhan) cbBenhNhan.getSelectedItem();
        BacSi bs = (BacSi) cbBacSi.getSelectedItem();
        if (bn == null) {
            sai(this, cbBenhNhan, "Vui lòng chọn bệnh nhân cho lịch hẹn.");
            return;
        }
        if (bs == null) {
            sai(this, cbBacSi, "Vui lòng chọn bác sĩ cho lịch hẹn.");
            return;
        }
        if (!batBuoc(this, oNgay, "ngày hẹn")) return;

        LocalDate ngay;
        try {
            ngay = oNgay.layNgay();
        } catch (DateTimeParseException e) {
            sai(this, oNgay, "Ngày hẹn không đúng định dạng dd/MM/yyyy.");
            return;
        }
        String gioChu = (String) cbGio.getSelectedItem();
        if (gioChu == null || gioChu.isBlank()) {
            sai(this, cbGio, "Vui lòng chọn giờ hẹn.");
            return;
        }
        LocalDateTime ngayGio = ngay.atTime(LocalTime.parse(gioChu, GIO));
        if (ngayGio.isBefore(LocalDateTime.now())) {
            sai(this, oNgay, "Ngày giờ hẹn phải ở tương lai.");
            return;
        }

        try {
            if (lichHen == null) {
                lichHenDao.tao(bn.getMaBN(), bs.getMaBS(), ngayGio, oGhiChu.getText().trim(), maTK);
                thongBao(this, "Đã tạo lịch hẹn cho " + bn.getHoTen()
                        + " lúc " + ngayGio.format(NGAY_GIO) + ".");
            } else {
                // Giữ nguyên bệnh nhân gốc của lịch hẹn khi sửa.
                lichHenDao.capNhat(lichHen.getMaLH(), lichHen.getMaBN(), bs.getMaBS(), ngayGio,
                        oGhiChu.getText().trim());
                thongBao(this, "Đã cập nhật lịch hẹn.");
            }
            daLuu = true;
            dispose();
        } catch (Exception e) {
            baoLoi(this, e);
        }
    }
}
