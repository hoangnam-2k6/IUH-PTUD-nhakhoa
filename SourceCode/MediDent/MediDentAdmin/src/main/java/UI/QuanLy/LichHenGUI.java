package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import UI.Chung.NgayHenChon;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.*;

/** Lịch hẹn thật, chọn bệnh nhân/bác sĩ theo khóa ngoại từ SQL. */
public final class LichHenGUI extends VanHanhPage {
    private final int actor;
    public LichHenGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Quản lý lịch hẹn", "Lịch thực tế của phòng khám; tránh trùng giờ cho cùng nhân viên.",
                "Ngày","Giờ","Bệnh nhân","Nhân viên","Phòng","Lý do","Trạng thái");
        actor=user.id;
        themNut("Tạo lịch",true,BieuTuong.Loai.THEM,()->tao());
        themNut("Đổi trạng thái",false,BieuTuong.Loai.SUA,()->doiTrangThai());
        themNut("Dời lịch",false,BieuTuong.Loai.SUA,()->doiLich());
        themNut("Phân công bác sĩ",false,BieuTuong.Loai.SUA,()->phanCongBacSi());
        nap(dao::lichHen);
    }
    private void tao() throws Exception {
        // Dialog là form tạm, không tạo cửa sổ chính thứ hai.
        new TaoLichHenGUI(dao,actor,this::taiLai).hien(this);
    }
    private void doiTrangThai() throws Exception {
        QuanLyDAO.Hang r=daChon();if(r==null)return;
        // Chỉ hiển thị trạng thái TIẾP THEO hợp lệ, không cho chọn trạng thái tùy ý.
        String current = dao.chiTietLichHen(r.id).trangThai;
        String[] states;
        switch (current) {
            case "Chờ xác nhận": states = new String[]{"Đã xác nhận", "Đã hủy"}; break;
            case "Đã xác nhận": states = new String[]{"Đã đến", "Đã hủy"}; break;
            case "Đã đến": states = new String[]{"Chờ khám", "Đã hủy"}; break;
            case "Chờ phân công": states = new String[]{"Đã hủy"}; break;
            case "Chờ khám": states = new String[]{"Đang khám", "Đã hủy"}; break;
            case "Đang khám": states = new String[]{"Hoàn tất"}; break;
            default:
                JOptionPane.showMessageDialog(this, "Lịch đã kết thúc hoặc hủy, không thể đổi trạng thái.");
                return;
        }
        Object status = JOptionPane.showInputDialog(this,
                "Trạng thái hiện tại: " + current + "\nChọn trạng thái tiếp theo:",
                "Lịch hẹn #" + r.id, JOptionPane.QUESTION_MESSAGE, null, states, states[0]);
        if (status != null) {
            try {
                dao.trangThaiLichHen(r.id, status.toString(), actor);
                taiLai();
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đổi trạng thái", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    private void doiLich() throws Exception {
        QuanLyDAO.Hang row = daChon();
        if (row == null) return;
        QuanLyDAO.ThongTinLichHen old = dao.chiTietLichHen(row.id);
        if (java.util.Arrays.asList("Đang khám", "Hoàn tất", "Đã hủy").contains(old.trangThai)) {
            JOptionPane.showMessageDialog(this, "Lịch đang khám hoặc đã kết thúc/hủy không thể dời.");
            return;
        }

        JComboBox<QuanLyDAO.LuaChon> patients = BieuMau.chon(dao.luaChon("BenhNhan"));
        JComboBox<QuanLyDAO.LuaChon> doctors = BieuMau.chon(dao.luaChon("NhanVien"));
        selectId(patients, old.maBN);
        if (old.maNV != null) selectId(doctors, old.maNV);
        NgayHenChon date = new NgayHenChon();
        date.setNgay(old.ngay);
        JSpinner time = spinner(old.gio);
        JSpinner duration = new JSpinner(new SpinnerNumberModel(old.thoiLuongPhut, 15, 240, 15));
        JComboBox<String> room = new JComboBox<>(dao.danhSachPhong().toArray(new String[0]));
        room.setSelectedItem(old.phong);
        if (room.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có phòng khám hoạt động.");
            return;
        }
        JTextField reason = BieuMau.text(old.lyDo);
        JPanel form = BieuMau.form();
        BieuMau.them(form, "Bệnh nhân *", patients);
        BieuMau.them(form, "Bác sĩ *", doctors);
        BieuMau.them(form, "Ngày hẹn *", date);
        BieuMau.them(form, "Giờ hẹn *", time);
        BieuMau.them(form, "Thời lượng (phút) *", duration);
        BieuMau.them(form, "Phòng", room);
        BieuMau.them(form, "Lý do", reason);

        while (true) {
            if (!BieuMau.xacNhan(this, "Dời lịch #" + row.id, form)) return;
            try {
                QuanLyDAO.LuaChon patient = (QuanLyDAO.LuaChon) patients.getSelectedItem();
                QuanLyDAO.LuaChon doctor = (QuanLyDAO.LuaChon) doctors.getSelectedItem();
                if (patient == null || doctor == null) throw new IllegalArgumentException("Chọn bệnh nhân và bác sĩ.");
                LocalDate newDate = date.getNgay();
                LocalTime newTime = readTime(time);
                dao.doiLichHen(row.id, patient.id, doctor.id, newDate, newTime,
                        (Integer) duration.getValue(), (String) room.getSelectedItem(), reason.getText(), actor);
                taiLai();
                return;
            } catch (java.sql.SQLException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thể dời lịch", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private static void selectId(JComboBox<QuanLyDAO.LuaChon> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id == id) { combo.setSelectedIndex(i); return; }
        }
    }

    private static JSpinner spinner(LocalTime time) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        JSpinner spinner = new JSpinner(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.MINUTE));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
        return spinner;
    }

    private static LocalTime readTime(JSpinner spinner) {
        Date value = (Date) spinner.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private void phanCongBacSi() throws Exception {
        QuanLyDAO.Hang r=daChon();
        if(r==null)return;
        java.util.List<QuanLyDAO.LuaChon> doctors=dao.luaChon("NhanVien");
        JComboBox<QuanLyDAO.LuaChon> combo=BieuMau.chon(doctors);
        QuanLyDAO.ThongTinLichHen lich = dao.chiTietLichHen(r.id);
        if (lich.maNV != null) selectId(combo, lich.maNV);
        if(doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this,"Chưa có bác sĩ đang làm việc có thể phân công.");
            return;
        }
        int result=JOptionPane.showConfirmDialog(this,combo,"Phân công / đổi bác sĩ cho lịch #"+r.id,
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result!=JOptionPane.OK_OPTION)return;
        QuanLyDAO.LuaChon selected=(QuanLyDAO.LuaChon)combo.getSelectedItem();
        if(selected==null)return;
        dao.phanCongBacSi(r.id,selected.id,actor);
        taiLai();
    }
}
