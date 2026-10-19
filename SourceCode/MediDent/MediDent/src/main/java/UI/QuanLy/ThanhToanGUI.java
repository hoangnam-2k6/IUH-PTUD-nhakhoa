/*
 * ThanhToanGUI: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import Class.TaiKhoan;
import DAO.QuanLyDAO;
import UI.Chung.BieuTuong;
import java.math.BigDecimal;
import javax.swing.*;

public final class ThanhToanGUI extends VanHanhPage {
    private final int actor;
    /** Khởi tạo bảng hóa đơn và nút thu tiền/yêu cầu hủy hiện có. */
    public ThanhToanGUI(QuanLyDAO dao,TaiKhoan user) {
        super(dao,"Thanh toán và hóa đơn", "Tổng tiền được tự tính từ chi tiết điều trị; có luồng xin hủy chờ Admin duyệt.",
                "Mã hóa đơn", "Bệnh nhân", "Ngày lập", "Tổng tiền",
                "Phương thức", "Trạng thái", "Mã thanh toán");
        actor=user.id;
        themNut("Tạo thanh toán",true,BieuTuong.Loai.THEM,()->tao());
        themNut("Yêu cầu hủy",false,BieuTuong.Loai.HUY,()->huy());
        nap(dao::hoaDon);
    }
    /** Lấy thông tin từ form và yêu cầu DAO tạo bản ghi tương ứng. */
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

    /** Lấy mã bản ghi đang chọn và gọi DAO để xử lý nghiệp vụ tương ứng. */
    private void huy() throws Exception {
        QuanLyDAO.Hang hoaDon = daChon();
        if (hoaDon == null) return;

        int maHD = hoaDon.id;
        String tenBenhNhan = hoaDon.cot[1];
        String soTien = hoaDon.cot[3];
        String maTT = hoaDon.cot[6];

        JPanel form = BieuMau.form();
        BieuMau.them(form, "Mã hóa đơn (chỉ đọc)", taoNhanGiaTri(String.valueOf(maHD)));
        BieuMau.them(form, "Mã thanh toán (chỉ đọc)", taoNhanGiaTri(maTT));
        BieuMau.them(form, "Bệnh nhân", taoNhanGiaTri(tenBenhNhan));
        BieuMau.them(form, "Số tiền", taoNhanGiaTri(soTien + " đ"));
        JTextArea lyDo = new JTextArea(3, 30);
        lyDo.setLineWrap(true);
        lyDo.setWrapStyleWord(true);
        BieuMau.them(form, "Lý do hủy *", new JScrollPane(lyDo));

        if (!BieuMau.xacNhan(this, "Yêu cầu hủy HÓA ĐƠN #" + maHD, form)) return;
        String noiDung = lyDo.getText().trim();
        if (noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do hủy.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận gửi yêu cầu hủy HÓA ĐƠN #" + maHD
              + "\nBệnh nhân: " + tenBenhNhan
              + "\nMã thanh toán: " + maTT
              + "\nĐơn chỉ được hủy khi Admin duyệt.",
                "Xác nhận đúng hóa đơn", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;

        dao.yeuCauHuyHoaDon(maHD, noiDung, actor);
        JOptionPane.showMessageDialog(this,
                "Đã gửi yêu cầu hủy hóa đơn #" + maHD + ". Chờ Admin duyệt.");
        taiLai();
    }

    /** Hiển thị số tiền, mã phiếu và người bệnh ở dạng nhãn chỉ đọc. */
    private static JLabel taoNhanGiaTri(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        return label;
    }
}
