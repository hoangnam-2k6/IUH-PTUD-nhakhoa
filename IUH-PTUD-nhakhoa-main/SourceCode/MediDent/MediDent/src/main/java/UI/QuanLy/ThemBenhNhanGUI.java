/*
 * ThemBenhNhanGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import DAO.QuanLyDAO;
import TienIch.EmailHopLe;
import UI.Chung.NgayChon;
import java.awt.Component;
import java.time.LocalDate;
import javax.swing.*;

public final class ThemBenhNhanGUI {
    private final QuanLyDAO dao;
    private final int actor;
    private final int maBN;
    private final Runnable taiLai;

    /** Lưu ngữ cảnh và mã bệnh nhân cho form nhập hồ sơ. */
    public ThemBenhNhanGUI(QuanLyDAO dao,int actor,int maBN,Runnable taiLai) {
        this.dao=dao;this.actor=actor;this.maBN=maBN;this.taiLai=taiLai;
    }

    /** Mở form thêm/sửa hiện tại và chuyển dữ liệu hợp lệ sang DAO. */
    public void hien(Component parent) throws Exception {

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
        while (true) {
            if(!BieuMau.xacNhan(parent,maBN==0?"Thêm bệnh nhân":"Sửa bệnh nhân",f))return;
            if (!email.getText().trim().isEmpty() && !EmailHopLe.kiemTra(email.getText())) {
                JOptionPane.showMessageDialog(parent,EmailHopLe.huongDan(),
                        "Email không hợp lệ",JOptionPane.WARNING_MESSAGE);
                email.requestFocusInWindow();
                continue;
            }
            LocalDate birthday=ngay.getNgay();
            dao.luuBenhNhan(maBN,ten.getText(),sdt.getText(),birthday,
                    (String)gioiTinh.getSelectedItem(),email.getText(),diaChi.getText(),
                    tienSu.getText(),(String)trangThai.getSelectedItem(),actor);
            taiLai.run();
            break;
        }
    }
}
