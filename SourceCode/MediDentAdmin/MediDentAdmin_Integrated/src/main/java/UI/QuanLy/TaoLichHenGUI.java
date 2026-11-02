package UI.QuanLy;

import DAO.QuanLyDAO;
import UI.Chung.NgayHenChon;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.*;

/** Form tạo lịch được gọi từ menu Lịch hẹn; không tạo JFrame khác. */
public final class TaoLichHenGUI {
    private final QuanLyDAO dao;
    private final int actor;
    private final Runnable taiLai;
    public TaoLichHenGUI(QuanLyDAO dao,int actor,Runnable taiLai) {
        this.dao=dao;this.actor=actor;this.taiLai=taiLai;
    }
    public void hien(Component parent) throws Exception {
        JPanel f=BieuMau.form();
        JComboBox<QuanLyDAO.LuaChon> benhNhan=BieuMau.chon(dao.luaChon("BenhNhan"));
        JComboBox<QuanLyDAO.LuaChon> nhanVien=BieuMau.chon(dao.luaChon("NhanVien"));
        // Chọn ngày từ lịch tháng, giới hạn tương lai; không gõ 31/02 hoặc ngày sai.
        NgayHenChon ngay = new NgayHenChon();

        // Giờ mặc định 09:00, chỉ chỉnh qua spinner.
        Calendar start = new GregorianCalendar();
        start.set(Calendar.HOUR_OF_DAY, 9);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        JSpinner gio = new JSpinner(new SpinnerDateModel(
                start.getTime(), null, null, Calendar.MINUTE));
        gio.setEditor(new JSpinner.DateEditor(gio, "HH:mm"));
        ((JSpinner.DefaultEditor)gio.getEditor()).getTextField().setEditable(false);
        JSpinner thoiLuong = new JSpinner(new SpinnerNumberModel(
                QuanLyDAO.THOI_LUONG_LICH_MAC_DINH_PHUT, 15, 240, 15));
        JComboBox<String> phong = new JComboBox<>(dao.danhSachPhong().toArray(new String[0]));
        if (phong.getItemCount() == 0) {
            JOptionPane.showMessageDialog(parent, "Chưa có phòng khám trong SQL. Hãy chạy SQLData/14_DongBo_CSDL_LoiNhapLieu_SQL2012.sql.");
            return;
        }
        JTextField lyDo=BieuMau.text("");
        // Nếu Lễ tân vừa tạo bệnh nhân ở máy khác, có thể tải lại ngay khi form đang mở.
        JPanel patientRow = new JPanel(new java.awt.BorderLayout(6, 0));
        patientRow.setOpaque(false);
        patientRow.add(benhNhan, java.awt.BorderLayout.CENTER);
        JButton reloadPatient = new JButton("Tải lại BN");
        reloadPatient.addActionListener(event -> {
            try {
                QuanLyDAO.LuaChon selected = (QuanLyDAO.LuaChon) benhNhan.getSelectedItem();
                java.util.List<QuanLyDAO.LuaChon> current = dao.luaChon("BenhNhan");
                benhNhan.removeAllItems();
                for (QuanLyDAO.LuaChon item : current) benhNhan.addItem(item);
                if (selected != null) {
                    for (int i = 0; i < benhNhan.getItemCount(); i++) {
                        if (benhNhan.getItemAt(i).id == selected.id) {
                            benhNhan.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Lỗi tải bệnh nhân",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        patientRow.add(reloadPatient, java.awt.BorderLayout.EAST);
        BieuMau.them(f,"Bệnh nhân *",patientRow);
        if (nhanVien.getItemCount() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Chưa có nhân viên chức vụ Bác sĩ ở trạng thái Đang làm việc.\n"
                  + "Hãy thêm hồ sơ bác sĩ tại menu Nhân viên (không phải tài khoản Bác sĩ).",
                    "Không có bác sĩ", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BieuMau.them(f,"Nhân viên/bác sĩ phụ trách *",nhanVien);
        BieuMau.them(f,"Ngày hẹn (tự điền, chọn qua lịch) *",ngay);
        BieuMau.them(f,"Giờ hẹn (tự điền, chọn giờ) *",gio);
        BieuMau.them(f,"Thời lượng (phút) *",thoiLuong);
        BieuMau.them(f,"Phòng",phong);
        BieuMau.them(f,"Lý do khám",lyDo);
        // Nếu chọn thời gian bị lỗi, giữ nguyên mọi ô đã nhập để sửa tiếp.
        while (true) {
            if (!BieuMau.xacNhan(parent, "Tạo lịch hẹn", f)) return;
            try {
                QuanLyDAO.LuaChon b = (QuanLyDAO.LuaChon) benhNhan.getSelectedItem();
                QuanLyDAO.LuaChon n = (QuanLyDAO.LuaChon) nhanVien.getSelectedItem();
                if (b == null || n == null)
                    throw new IllegalArgumentException("Cần có bệnh nhân và nhân viên trong CSDL.");
                Date t = (Date) gio.getValue();
                LocalDate date = ngay.getNgay();
                LocalTime time = t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                        .withSecond(0).withNano(0);
                if (date == null || date.isBefore(LocalDate.now())
                        || (date.equals(LocalDate.now()) && !time.isAfter(LocalTime.now())))
                    throw new IllegalArgumentException("Ngày và giờ hẹn phải ở tương lai.");
                dao.taoLichHen(b.id, n.id, date, time, (Integer) thoiLuong.getValue(),
                        (String) phong.getSelectedItem(), lyDo.getText(), actor);
                JOptionPane.showMessageDialog(parent, "Đã tạo lịch hẹn trên SQL Server.");
                taiLai.run();
                return;
            } catch (java.sql.SQLException | IllegalArgumentException loi) {
                JOptionPane.showMessageDialog(parent, loi.getMessage(),
                        "Thông tin lịch hẹn không hợp lệ", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
