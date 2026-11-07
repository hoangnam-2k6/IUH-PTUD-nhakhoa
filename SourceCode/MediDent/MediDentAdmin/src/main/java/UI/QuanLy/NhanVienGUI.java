package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import TienIch.EmailHopLe;
import TienIch.SoDienThoaiHopLe;
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
        themNut("Cho nghỉ việc",false,BieuTuong.Loai.HUY,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(JOptionPane.showConfirmDialog(this,"Cho nhân viên #"+r.id+" nghỉ việc? "
                    +"Bạn cần chuyển các lịch hẹn và xử lý ca tương lai trước.",
                    "Xác nhận nghỉ việc",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaNhanVien(r.id,actor);
            taiLai();
        });
        nap(dao::nhanVien);
    }
    private static boolean emailHopLe(String email) {
        return EmailHopLe.kiemTra(email);
    }

    private void form(int id,String[] old)throws Exception {
        byte[] phienBanCu = id == 0 ? null : dao.phienBanNhanVien(id);
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
            if (!SoDienThoaiHopLe.kiemTra(sdt.getText())) {
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
            // DAO kiểm tra lại quyền, dữ liệu và SĐT trùng trên SQL Server.
            try {
                dao.luuNhanVien(id,ten.getText().trim(),sdt.getText().trim(),email.getText().trim(),
                        (String)chucVu.getSelectedItem(),(String)status.getSelectedItem(),
                        phienBanCu, actor);
                taiLai();
                return;
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Không thể lưu nhân viên", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
