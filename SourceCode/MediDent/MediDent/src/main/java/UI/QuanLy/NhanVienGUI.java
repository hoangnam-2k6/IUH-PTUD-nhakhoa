package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import javax.swing.*;

/** Nhân viên nghiệp vụ; tài khoản đăng nhập được cấp riêng tại Admin. */
public final class NhanVienGUI extends VanHanhPage {
    private final int actor;
    public NhanVienGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Nhân viên", "Quản lý hồ sơ nhân sự. Tài khoản đăng nhập do Admin cấp.",
                false,
                "Họ tên","Số điện thoại","Email","Chức vụ","Trạng thái");
        actor=user.id;
        themNut("Thêm nhân viên",true,BieuTuong.Loai.THEM,()->form(0,null));
        themNut("Sửa",false,BieuTuong.Loai.SUA,()->{QuanLyDAO.Hang r=daChon();if(r!=null)form(r.id,r.cot);});
        themNut("Xóa",false,BieuTuong.Loai.XOA,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(JOptionPane.showConfirmDialog(this,"Xóa nhân viên #"+r.id
                    +"? Nếu có dữ liệu liên quan SQL sẽ từ chối.",
                    "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaNhanVien(r.id,actor);
            taiLai();
        });
        nap(dao::nhanVien);
    }
    private void form(int id,String[] old)throws Exception {
        JPanel f=BieuMau.form();
        JTextField ten=BieuMau.text(old==null?"":old[0]);
        JTextField sdt=BieuMau.text(old==null?"":old[1]);
        JTextField email=BieuMau.text(old==null?"":old[2]);
        JComboBox<String> chucVu=BieuMau.trangThai("Quản lý phòng khám","Bác sĩ","Lễ tân","Kế toán","Nhân viên");
        JComboBox<String> status=BieuMau.trangThai("Đang làm việc","Tạm nghỉ","Nghỉ việc");
        if(old!=null){chucVu.setSelectedItem(old[3]);status.setSelectedItem(old[4]);}
        BieuMau.them(f,"Họ tên *",ten);
        BieuMau.them(f,"Điện thoại",sdt);
        BieuMau.them(f,"Email",email);
        BieuMau.them(f,"Chức vụ",chucVu);
        BieuMau.them(f,"Trạng thái",status);
        if(!BieuMau.xacNhan(this,id==0?"Thêm nhân viên":"Sửa nhân viên",f))return;
        dao.luuNhanVien(id,ten.getText(),sdt.getText(),email.getText(),
                (String)chucVu.getSelectedItem(),(String)status.getSelectedItem(),actor);
        taiLai();
    }
}
