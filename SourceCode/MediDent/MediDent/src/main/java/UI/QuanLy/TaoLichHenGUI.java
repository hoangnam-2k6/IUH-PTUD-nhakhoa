package UI.QuanLy;

import DAO.QuanLyDAO;
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
        // Ngày hôm sau tự chọn, chỉnh bằng nút tăng giảm; tránh gõ ngày sai định dạng.
        LocalDate next = LocalDate.now().plusDays(1);
        Date dateDefault = Date.from(next.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner ngay = new JSpinner(new SpinnerDateModel(
                dateDefault, new Date(), null, Calendar.DAY_OF_MONTH));
        ngay.setEditor(new JSpinner.DateEditor(ngay, "dd/MM/yyyy"));
        ((JSpinner.DefaultEditor)ngay.getEditor()).getTextField().setEditable(false);

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
        if(!BieuMau.xacNhan(parent,"Tạo lịch hẹn",f))return;
        QuanLyDAO.LuaChon b=(QuanLyDAO.LuaChon)benhNhan.getSelectedItem();
        QuanLyDAO.LuaChon n=(QuanLyDAO.LuaChon)nhanVien.getSelectedItem();
        if(b==null || n==null)throw new IllegalArgumentException("Cần có bệnh nhân và nhân viên trong CSDL.");
        Date d = (Date)ngay.getValue();
        Date t = (Date)gio.getValue();
        LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime time = t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
                .withSecond(0).withNano(0);
        if (date.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Ngày hẹn không được nằm trong quá khứ.");
        dao.taoLichHen(b.id,n.id,date,time,(String)phong.getSelectedItem(),lyDo.getText(),actor);
        JOptionPane.showMessageDialog(parent,"Đã tạo lịch hẹn trên SQL Server.");
        taiLai.run();
    }
}
