package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import javax.swing.*;

/** Lịch hẹn thật, chọn bệnh nhân/bác sĩ theo khóa ngoại từ SQL. */
public final class LichHenGUI extends VanHanhPage {
    private final int actor;
    public LichHenGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Quản lý lịch hẹn", "Lịch thực tế của phòng khám; tránh trùng giờ cho cùng nhân viên.",
                false,
                "Ngày","Giờ","Bệnh nhân","Nhân viên","Phòng","Lý do","Trạng thái");
        actor=user.id;
        themNut("Tạo lịch",true,BieuTuong.Loai.THEM,()->tao());
        themNut("Đổi trạng thái",false,BieuTuong.Loai.SUA,()->doiTrangThai());
        nap(dao::lichHen);
    }
    private void tao() throws Exception {
        // Dialog là form tạm, không tạo cửa sổ chính thứ hai.
        new TaoLichHenGUI(dao,actor,this::taiLai).hien(this);
    }
    private void doiTrangThai() throws Exception {
        QuanLyDAO.Hang r=daChon();if(r==null)return;
        String[] states={"Chờ xác nhận","Đã xác nhận","Đang khám","Hoàn tất","Đã hủy"};
        Object status=JOptionPane.showInputDialog(this,"Chọn trạng thái:",
                "Lịch hẹn #"+r.id,JOptionPane.QUESTION_MESSAGE,null,states,r.cot[6]);
        if(status!=null) { dao.trangThaiLichHen(r.id,status.toString(),actor);taiLai(); }
    }
}
