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
        JComboBox<String> phong=BieuMau.trangThai("Phòng 01","Phòng 02","Phòng 03");
        JTextField lyDo=BieuMau.text("");
        BieuMau.them(f,"Bệnh nhân *",benhNhan);
        BieuMau.them(f,"Nhân viên/bác sĩ phụ trách *",nhanVien);
        BieuMau.them(f,"Ngày hẹn (tự điền, chọn qua lịch) *",ngay);
        BieuMau.them(f,"Giờ hẹn (tự điền, chọn giờ) *",gio);
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
                dao.taoLichHen(b.id, n.id, date, time,
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
