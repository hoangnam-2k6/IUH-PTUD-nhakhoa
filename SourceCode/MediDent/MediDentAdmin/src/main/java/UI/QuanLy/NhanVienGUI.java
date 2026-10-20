package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import TienIch.EmailHopLe;
import UI.Chung.BieuTuong;
import javax.swing.*;

/** Nhân viên nghiệp vụ; tài khoản đăng nhập được cấp riêng tại Admin. */
public final class NhanVienGUI extends VanHanhPage {
    private final int actor;
    public NhanVienGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Nhân viên", "Quản lý hồ sơ nhân sự. Tài khoản đăng nhập do Admin cấp.",
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
    /** Quy tắc dùng chung với DAO: đúng đầu số di động Việt Nam và đủ 10 chữ số. */
    private static final String SDT_REGEX =
            "^(?:03[2-9]|05[25689]|070|07[6-9]|08[1-9]|09[0-4]|09[6-9])[0-9]{7}$";
    private static boolean emailHopLe(String email) {
        return EmailHopLe.kiemTra(email);
    }

    private static boolean sdtHopLe(String sdt) {
        return sdt != null && sdt.trim().matches(SDT_REGEX);
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
        BieuMau.them(f,"Điện thoại *",sdt);
        BieuMau.them(f,"Email *",email);
        BieuMau.them(f,"Chức vụ",chucVu);
        BieuMau.them(f,"Trạng thái",status);
        // Nếu nhập sai, mở lại cùng form và giữ nguyên dữ liệu để người dùng sửa.
        while (true) {
            if (!BieuMau.xacNhan(this,id==0?"Thêm nhân viên":"Sửa nhân viên",f)) return;
            if (ten.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,"Họ tên nhân viên không được để trống.",
                        "Dữ liệu không hợp lệ",JOptionPane.WARNING_MESSAGE);
                ten.requestFocusInWindow();
                continue;
            }
            if (!sdtHopLe(sdt.getText().trim())) {
                JOptionPane.showMessageDialog(this,
                        "SĐT phải có đúng 10 chữ số, bắt đầu bằng đầu số di động Việt Nam hợp lệ.",
                        "Số điện thoại không hợp lệ",JOptionPane.WARNING_MESSAGE);
                sdt.requestFocusInWindow();
                continue;
            }
            if (!emailHopLe(email.getText().trim())) {
                JOptionPane.showMessageDialog(this,
                        EmailHopLe.huongDan(),
                        "Email không hợp lệ",JOptionPane.WARNING_MESSAGE);
                email.requestFocusInWindow();
                continue;
            }
            // DAO còn kiểm tra lại dữ liệu và số điện thoại trùng trong SQL Server.
            dao.luuNhanVien(id,ten.getText().trim(),sdt.getText().trim(),email.getText().trim(),
                    (String)chucVu.getSelectedItem(),(String)status.getSelectedItem(),actor);
            taiLai();
            return;
        }
    }
}
