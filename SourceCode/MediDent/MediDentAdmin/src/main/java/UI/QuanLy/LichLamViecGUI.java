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

/** Quản lý ca làm việc của bác sĩ; dữ liệu được lưu trong dbo.LichLamViec. */
public final class LichLamViecGUI extends VanHanhPage {
    private final int actor;

    public LichLamViecGUI(QuanLyDAO dao, TaiKhoan user) {
        super(dao, "Lịch làm việc", "Xếp ca bác sĩ theo ngày và khoảng giờ; không xếp chồng ca hoặc bỏ ca đang có lịch hẹn.",
                "Bác sĩ", "Ngày", "Ca", "Bắt đầu", "Kết thúc", "Trạng thái");
        actor = user.id;
        themNut("Thêm ca", true, BieuTuong.Loai.THEM, () -> form(0));
        themNut("Sửa ca", false, BieuTuong.Loai.SUA, () -> {
            QuanLyDAO.Hang row = daChon();
            if (row != null) form(row.id);
        });
        themNut("Ngừng xếp lịch", false, BieuTuong.Loai.HUY, () -> ngungCa());
        nap(dao::lichLamViec);
    }

    private void form(int id) throws Exception {
        QuanLyDAO.ThongTinLichLamViec old = id == 0 ? null : dao.chiTietLichLamViec(id);
        java.util.List<QuanLyDAO.LuaChon> doctors = dao.luaChon("NhanVien");
        JComboBox<QuanLyDAO.LuaChon> doctor = BieuMau.chon(doctors);
        if (old != null) {
            for (int i = 0; i < doctor.getItemCount(); i++) {
                if (doctor.getItemAt(i).id == old.maNV) {
                    doctor.setSelectedIndex(i);
                    break;
                }
            }
        }
        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có bác sĩ đang làm việc để xếp ca.");
            return;
        }

        NgayHenChon date = new NgayHenChon();
        if (old != null) date.setNgay(old.ngay);
        JTextField shift = BieuMau.text(old == null ? "Ca sáng" : old.ca);
        // Một số dữ liệu ca cũ có thể chưa có giờ; dùng giờ gợi ý để người dùng hoàn thiện.
        JSpinner start = spinner(old == null || old.gioBatDau == null
                ? LocalTime.of(8, 0) : old.gioBatDau);
        JSpinner end = spinner(old == null || old.gioKetThuc == null
                ? LocalTime.of(12, 0) : old.gioKetThuc);
        JComboBox<String> status = BieuMau.trangThai("Đã xếp lịch", "Ngừng xếp lịch", "Đã hủy");
        if (old != null) status.setSelectedItem(old.trangThai);

        JPanel form = BieuMau.form();
        BieuMau.them(form, "Bác sĩ *", doctor);
        BieuMau.them(form, "Ngày làm việc *", date);
        BieuMau.them(form, "Tên ca *", shift);
        BieuMau.them(form, "Giờ bắt đầu *", start);
        BieuMau.them(form, "Giờ kết thúc *", end);
        BieuMau.them(form, "Trạng thái", status);

        while (true) {
            if (!BieuMau.xacNhan(this, id == 0 ? "Thêm ca làm việc" : "Sửa ca làm việc", form)) return;
            try {
                QuanLyDAO.LuaChon selected = (QuanLyDAO.LuaChon) doctor.getSelectedItem();
                if (selected == null) throw new IllegalArgumentException("Chọn bác sĩ.");
                LocalDate workDate = date.getNgay();
                LocalTime from = readTime(start);
                LocalTime to = readTime(end);
                dao.luuLichLamViec(id, selected.id, workDate, shift.getText(), from, to,
                        (String) status.getSelectedItem(), actor);
                taiLai();
                return;
            } catch (java.sql.SQLException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thể lưu ca làm việc", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void ngungCa() throws Exception {
        QuanLyDAO.Hang row = daChon();
        if (row == null) return;
        if (JOptionPane.showConfirmDialog(this,
                "Ngừng xếp ca #" + row.id + "? Ca đang có lịch hẹn chưa kết thúc sẽ bị từ chối.",
                "Xác nhận ngừng ca", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        dao.xoaLichLamViec(row.id, actor);
        taiLai();
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
}
