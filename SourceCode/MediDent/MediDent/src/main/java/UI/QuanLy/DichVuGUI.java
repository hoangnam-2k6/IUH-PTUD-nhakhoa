/*
 * DichVuGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import java.math.BigDecimal;
import javax.swing.*;

public final class DichVuGUI extends VanHanhPage {
    private final int actor;
    /** Khởi tạo bảng dịch vụ và các nút thêm/sửa/ngừng áp dụng. */
    public DichVuGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Dịch vụ nha khoa", "Giá dịch vụ được lưu tại SQL Server, không sử dụng dữ liệu mẫu cứng.",
                "Dịch vụ","Đơn giá (đ)","Trạng thái","Mô tả");
        actor=user.id;
        themNut("Thêm dịch vụ",true,BieuTuong.Loai.THEM,()->form(0,null));
        themNut("Sửa",false,BieuTuong.Loai.SUA,()->{QuanLyDAO.Hang r=daChon();if(r!=null)form(r.id,r.cot);});
        themNut("Xóa",false,BieuTuong.Loai.XOA,()->{
            QuanLyDAO.Hang r=daChon();if(r==null)return;
            if(JOptionPane.showConfirmDialog(this,"Ngừng áp dụng dịch vụ #"+r.id
                    +"? Thông tin và lịch sử cũ sẽ được giữ lại.",
                    "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.xoaDichVu(r.id,actor);
            taiLai();
        });
        nap(dao::dichVu);
    }
    /** Nạp thông tin dịch vụ đã chọn vào form và lưu thay đổi qua DAO. */
    private void form(int id,String[] old) throws Exception {
        JPanel f=BieuMau.form();
        JTextField ten=BieuMau.text(old==null?"":old[0]);
        JTextField gia=BieuMau.text(old==null?"0":old[1]);
        JTextField moTa=BieuMau.text(old==null?"":old[3]);
        JComboBox<String> status=BieuMau.trangThai("Đang áp dụng","Ngừng áp dụng");
        if(old!=null)status.setSelectedItem(old[2]);
        BieuMau.them(f,"Tên dịch vụ *",ten);
        BieuMau.them(f,"Đơn giá (chỉ nhập số) *",gia);
        BieuMau.them(f,"Trạng thái",status);
        BieuMau.them(f,"Mô tả",moTa);
        if(!BieuMau.xacNhan(this,id==0?"Thêm dịch vụ":"Sửa dịch vụ",f))return;
        dao.luuDichVu(id,ten.getText(),new BigDecimal(gia.getText().trim()),
                (String)status.getSelectedItem(),moTa.getText(),actor);
        taiLai();
    }
}
