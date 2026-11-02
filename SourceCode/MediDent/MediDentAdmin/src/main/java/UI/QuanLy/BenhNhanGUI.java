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
                "Tra cứu, thêm, sửa và ngừng hồ sơ bệnh nhân; lịch sử khám được bảo toàn.",
                "Họ tên","Điện thoại","Giới tính","Ngày sinh","Email","Trạng thái");
        actor=user.id;
        themNut("Thêm bệnh nhân",true,BieuTuong.Loai.THEM,()->form(0));
        themNut("Sửa",false,BieuTuong.Loai.SUA,()->{
            QuanLyDAO.Hang r=daChon();if(r!=null)form(r.id);
        });
        themNut("Ngừng hồ sơ",false,BieuTuong.Loai.HUY,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(JOptionPane.showConfirmDialog(this,
                    "Ngừng hồ sơ bệnh nhân #"+r.id+"? Dữ liệu khám và hóa đơn cũ vẫn được giữ.",
                    "Xác nhận ngừng hồ sơ",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaBenhNhan(r.id,actor);
            taiLai();
        });
        themNut("Khôi phục hồ sơ",false,BieuTuong.Loai.TAI_LAI,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(!"Ngừng hồ sơ".equalsIgnoreCase(r.cot[5])) {
                JOptionPane.showMessageDialog(this,"Hồ sơ đang hoạt động.");
                return;
            }
            if(JOptionPane.showConfirmDialog(this,"Khôi phục hồ sơ bệnh nhân #"+r.id+"?",
                    "Xác nhận khôi phục",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.khoiPhucBenhNhan(r.id,actor);
            taiLai();
        });
        nap(dao::benhNhan);
    }

    private void form(int id) throws Exception {
        new ThemBenhNhanGUI(dao,actor,id,this::taiLai).hien(this);
    }
}
