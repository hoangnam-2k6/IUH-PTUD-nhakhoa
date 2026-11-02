package UI.QuanLy;

import DAO.QuanLyDAO;
import UI.Chung.NgayChon;
import java.awt.Component;
import java.time.LocalDate;
import javax.swing.*;

/**
 * Form thêm/sửa bệnh nhân - sử dụng như hộp thoại TRONG KhungChinh.
 * Form không mở JFrame, chỉ lưu khi đã xác thực quyền BENHNHAN ở DAO.
 */
public final class ThemBenhNhanGUI {
    private final QuanLyDAO dao;
    private final int actor;
    private final int maBN;
    private final Runnable taiLai;

    public ThemBenhNhanGUI(QuanLyDAO dao,int actor,int maBN,Runnable taiLai) {
        this.dao=dao;this.actor=actor;this.maBN=maBN;this.taiLai=taiLai;
    }

    public void hien(Component parent) throws Exception {
        // Khi sửa, đọc nguyên vẹn cả địa chỉ/tiền sử từ SQL để không ghi đè giá trị cũ.
        String[] old=maBN==0?null:dao.chiTietBenhNhan(maBN);
        JPanel f=BieuMau.form();
        JTextField ten=BieuMau.text(old==null?"":old[0]);
        JTextField sdt=BieuMau.text(old==null?"":old[1]);
        JComboBox<String> gioiTinh=BieuMau.trangThai("Nam","Nữ","Khác","Chưa xác định");
        JComboBox<String> trangThai=BieuMau.trangThai("Mới","Đang điều trị","Hoàn tất");
        NgayChon ngay = new NgayChon();
        if (old != null && old[3] != null && !old[3].isBlank()) {
            ngay.setNgay(BieuMau.ngay(old[3]));
        }
        JTextField email=BieuMau.text(old==null?"":old[4]);
        JTextField diaChi=BieuMau.text(old==null?"":old[5]);
        JTextArea tienSu=new JTextArea(3,25);
        if(old!=null) {
            tienSu.setText(old[6]);
            gioiTinh.setSelectedItem(old[2]);
            trangThai.setSelectedItem(old[7]);
        }
        BieuMau.them(f,"Họ và tên *",ten);
        BieuMau.them(f,"Điện thoại *",sdt);
        BieuMau.them(f,"Giới tính",gioiTinh);
        BieuMau.them(f,"Ngày sinh (chọn ngày có thật)",ngay);
        BieuMau.them(f,"Email",email);
        BieuMau.them(f,"Địa chỉ",diaChi);
        BieuMau.them(f,"Tiền sử bệnh",new JScrollPane(tienSu));
        BieuMau.them(f,"Trạng thái",trangThai);
<<<<<<< Updated upstream:MediDent/MediDentApp/src/main/java/UI/QuanLy/ThemBenhNhanGUI.java
        if(!BieuMau.xacNhan(parent,maBN==0?"Thêm bệnh nhân":"Sửa bệnh nhân",f))return;
        LocalDate birthday=ngay.getNgay();
        dao.luuBenhNhan(maBN,ten.getText(),sdt.getText(),birthday,
                (String)gioiTinh.getSelectedItem(),email.getText(),diaChi.getText(),
                tienSu.getText(),(String)trangThai.getSelectedItem(),actor);
        taiLai.run();
=======
        while (true) {
            if(!BieuMau.xacNhan(parent,maBN==0?"Thêm bệnh nhân":"Sửa bệnh nhân",f))return;
            if (!email.getText().trim().isEmpty() && !EmailHopLe.kiemTra(email.getText())) {
                JOptionPane.showMessageDialog(parent,EmailHopLe.huongDan(),
                        "Email không hợp lệ",JOptionPane.WARNING_MESSAGE);
                email.requestFocusInWindow();
                continue;
            }
            // Giữ nguyên form khi nhập số điện thoại không đúng để người dùng sửa.
            if (!SoDienThoaiHopLe.kiemTra(sdt.getText())) {
                JOptionPane.showMessageDialog(parent, "Số điện thoại phải có 10 chữ số và đầu số Việt Nam hợp lệ.",
                        "Số điện thoại không hợp lệ", JOptionPane.WARNING_MESSAGE);
                sdt.requestFocusInWindow();
                continue;
            }
            LocalDate birthday=ngay.getNgay();
            // Cho người dùng kiểm tra lại trước khi ghi SQL.
            String xacNhan = "Họ tên: " + ten.getText().trim()
                    + "\nSố điện thoại: " + sdt.getText().trim()
                    + "\nNgày sinh: " + (birthday == null ? "Chưa cung cấp" : birthday)
                    + "\nEmail: " + email.getText().trim();
            if (JOptionPane.showConfirmDialog(parent, xacNhan,
                    "Xác nhận thông tin bệnh nhân", JOptionPane.YES_NO_OPTION)
                    != JOptionPane.YES_OPTION) continue;
            try {
                dao.luuBenhNhan(maBN,ten.getText(),sdt.getText(),birthday,
                        (String)gioiTinh.getSelectedItem(),email.getText(),diaChi.getText(),
                        tienSu.getText(),(String)trangThai.getSelectedItem(),
                        old == null ? null : old[8], actor);
                taiLai.run();
                break;
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(),
                        "Không thể lưu bệnh nhân", JOptionPane.WARNING_MESSAGE);
                // Giữ form đang nhập để người dùng sửa, không tạo lại các ô.
            }
        }
>>>>>>> Stashed changes:SourceCode/MediDent/MediDentAdmin/src/main/java/UI/QuanLy/ThemBenhNhanGUI.java
    }
}
