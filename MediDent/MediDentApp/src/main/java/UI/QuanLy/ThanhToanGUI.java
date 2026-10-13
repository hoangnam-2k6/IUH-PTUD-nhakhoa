package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import java.math.BigDecimal;
import javax.swing.*;

/** Thanh toán dựa trên phiếu khám và chi tiết điều trị, không điền tay tổng tiền. */
public final class ThanhToanGUI extends VanHanhPage {
    private final int actor;
    public ThanhToanGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Thanh toán và hóa đơn", "Tổng tiền được tự tính từ chi tiết điều trị; có luồng xin hủy chờ Admin duyệt.",
                "Bệnh nhân","Ngày lập","Tổng tiền","Phương thức","Trạng thái","Mã TT");
        actor=user.id;
        themNut("Tạo thanh toán",true,BieuTuong.Loai.THEM,()->tao());
        themNut("Yêu cầu hủy",false,BieuTuong.Loai.HUY,()->huy());
        nap(dao::hoaDon);
    }
    private void tao() throws Exception {
        JPanel f=BieuMau.form();
        JComboBox<QuanLyDAO.LuaChon> phieu=BieuMau.chon(dao.luaChon("PhieuKham"));
        JComboBox<String> pt=BieuMau.trangThai("Tiền mặt","Chuyển khoản","Thẻ ngân hàng");
        BieuMau.them(f,"Phiếu khám *",phieu);
        BieuMau.them(f,"Phương thức thanh toán *",pt);
        if(!BieuMau.xacNhan(this,"Tạo thanh toán",f))return;
        QuanLyDAO.LuaChon item=(QuanLyDAO.LuaChon)phieu.getSelectedItem();
        if(item==null)throw new IllegalArgumentException("Chưa có phiếu khám trong CSDL.");
        BigDecimal value=dao.soTienPhieuKham(item.id);
        if(value.signum()<=0)throw new IllegalArgumentException("Phiếu khám chưa có chi tiết điều trị có tiền.");
        if(JOptionPane.showConfirmDialog(this,"Xác nhận thu "+value.toPlainString()+" đ?",
                "Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        dao.taoThanhToan(item.id,(String)pt.getSelectedItem(),actor);
        taiLai();
    }
    private void huy() throws Exception {
        QuanLyDAO.Hang r=daChon();if(r==null)return;
        String reason=JOptionPane.showInputDialog(this,"Lý do yêu cầu hủy thanh toán #"+r.cot[5]+":");
        if(reason==null)return;
        dao.yeuCauHuy(Integer.parseInt(r.cot[5]),reason,actor);
        JOptionPane.showMessageDialog(this,"Đã gửi yêu cầu, cần Quản trị viên phê duyệt.");
    }
}
