package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import javax.swing.JOptionPane;

/** Hồ sơ bệnh nhân dùng dbo.BenhNhan và tự tạo STT theo bảng đang hiển thị. */
public final class BenhNhanGUI extends VanHanhPage {
    private final int actor;

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
                    "Xóa bệnh nhân #"+r.id+"? Nếu đã có lịch sử khám, SQL sẽ từ chối.",
                    "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaBenhNhan(r.id,actor);
            taiLai();
        });
        nap(dao::benhNhan);
    }

    private void form(int id) throws Exception {
        new ThemBenhNhanGUI(dao,actor,id,this::taiLai).hien(this);
    }
}
