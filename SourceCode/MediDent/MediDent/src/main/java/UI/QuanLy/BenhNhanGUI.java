/*
 * BenhNhanGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import javax.swing.JOptionPane;

public final class BenhNhanGUI extends VanHanhPage {
    private final int actor;

    /** Khởi tạo màn hình CRUD bệnh nhân, bảng và nút thao tác hiện tại. */
    public BenhNhanGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Quản lý bệnh nhân",
                "Tra cứu, thêm, sửa và xóa an toàn hồ sơ bệnh nhân từ SQL Server.",
                "Họ tên","Điện thoại","Giới tính","Ngày sinh","Email","Trạng thái");
        actor=user.id;
        themNut("Thêm bệnh nhân",true,BieuTuong.Loai.THEM,()->form(0));
        themNut("Sửa",false,BieuTuong.Loai.SUA,()->{
            QuanLyDAO.Hang r=daChon();if(r!=null)form(r.id);
        });
        themNut("Xóa",false,BieuTuong.Loai.XOA,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(JOptionPane.showConfirmDialog(this,
                    "Ngừng hoạt động bệnh nhân #"+r.id+"? Hồ sơ và lịch sử khám vẫn được giữ.",
                    "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaBenhNhan(r.id,actor);
            taiLai();
        });
        nap(dao::benhNhan);
    }

    /** Mở hộp thoại thêm/sửa bệnh nhân theo mã dòng được chọn. */
    private void form(int id) throws Exception {
        new ThemBenhNhanGUI(dao,actor,id,this::taiLai).hien(this);
    }
}
