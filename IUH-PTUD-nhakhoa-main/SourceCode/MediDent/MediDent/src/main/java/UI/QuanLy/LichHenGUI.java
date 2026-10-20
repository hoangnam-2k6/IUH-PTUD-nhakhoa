/*
 * LichHenGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import javax.swing.*;

public final class LichHenGUI extends VanHanhPage {
    private final int actor;
    /** Khởi tạo bảng lịch hẹn và các thao tác tạo/đổi trạng thái. */
    public LichHenGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Quản lý lịch hẹn", "Lịch thực tế của phòng khám; tránh trùng giờ cho cùng nhân viên.",
                "Ngày","Giờ","Bệnh nhân","Nhân viên","Phòng","Lý do","Trạng thái");
        actor=user.id;
        themNut("Tạo lịch",true,BieuTuong.Loai.THEM,()->tao());
        themNut("Đổi trạng thái",false,BieuTuong.Loai.SUA,()->doiTrangThai());
        nap(dao::lichHen);
    }
    /** Lấy thông tin từ form và yêu cầu DAO tạo bản ghi tương ứng. */
    private void tao() throws Exception {

        new TaoLichHenGUI(dao,actor,this::taiLai).hien(this);
    }
    /** Đọc lịch hẹn được chọn và gửi yêu cầu chuyển trạng thái tới DAO. */
    private void doiTrangThai() throws Exception {
        QuanLyDAO.Hang r=daChon();if(r==null)return;
        String[] states={"Chờ xác nhận","Đã xác nhận","Đang khám","Hoàn tất","Đã hủy"};
        Object status=JOptionPane.showInputDialog(this,"Chọn trạng thái:",
                "Lịch hẹn #"+r.id,JOptionPane.QUESTION_MESSAGE,null,states,r.cot[6]);
        if(status!=null) { dao.trangThaiLichHen(r.id,status.toString(),actor);taiLai(); }
    }
}
