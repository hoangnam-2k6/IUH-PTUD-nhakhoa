/*
 * GuiIcons: giao diện hiện hữu theo phân quyền.
 * Chú thích mới: chỉ mô tả trách nhiệm của mã hiện tại; không thay đổi bố cục UI.
 */
package UI.QuanLy;

import UI.Chung.BieuTuong;
import UI.Chung.GiaoDien;
import java.awt.Color;
import javax.swing.Icon;

final class GuiIcons {
    static final String HOME="home",STAFF="staff",DENTAL_SERVICE="dental-service";
    static final String PATIENT="patient",CALENDAR="calendar",INVOICE="invoice";
    static final String REPORT="chart",REVENUE="revenue",PENDING_PAYMENT="pending";
    static final String CANCEL_REQUEST="cancel",TOOTH_LOGO="logo",SHIELD="shield";
    static final String BELL="bell",SEARCH="search",ADD="add",CHECK="check";
    static final String DOWNLOAD="download",LOGOUT="logout",FILTER="filter";
    /** Ngăn tạo lớp tiện ích ánh xạ tên biểu tượng vào Java2D. */
    private GuiIcons() {}
    static Icon icon(String name,Color color,int size) {
        BieuTuong.Loai kind;
        switch(name) {
            case HOME: kind=BieuTuong.Loai.TRANG_CHU;break;
            case STAFF: case PATIENT: kind=BieuTuong.Loai.TAI_KHOAN;break;
            case DENTAL_SERVICE: kind=BieuTuong.Loai.HE_THONG;break;
            case CALENDAR: kind=BieuTuong.Loai.THONG_BAO;break;
            case INVOICE: case REVENUE: case REPORT: kind=BieuTuong.Loai.BAO_CAO;break;
            case ADD: kind=BieuTuong.Loai.THEM;break;
            case DOWNLOAD: kind=BieuTuong.Loai.TAI_XUONG;break;
            case CHECK: kind=BieuTuong.Loai.KIEM_TRA;break;
            case LOGOUT: kind=BieuTuong.Loai.DANG_XUAT;break;
            case SEARCH: case FILTER: kind=BieuTuong.Loai.TIM_KIEM;break;
            case CANCEL_REQUEST: kind=BieuTuong.Loai.HUY;break;
            default: kind=BieuTuong.Loai.PHAN_QUYEN;
        }
        return new BieuTuong(kind,color,size);
    }
}
