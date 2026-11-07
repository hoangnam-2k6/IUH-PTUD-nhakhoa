
package UI.QuanLy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import Dao.NghiepVu.LichHenService;
import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.QuyTacNghiepVu;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class LichHenPanel extends BangDuLieuPanel {

    private static final String[] CAC_COT = {
        "ID", "Ngày hẹn", "Giờ", "Bệnh nhân", "Bác sĩ",
        "Phòng", "Trạng thái", "Phút", "Lý do", "Mã bệnh nhân", "Mã bác sĩ"
    };

    private final LichHenService dichVu;

    public LichHenPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        super("Quản lý lịch hẹn", "Thêm, sửa, hủy và kiểm tra lịch khám của bệnh nhân",
              "", CAC_COT, phienDangNhap == null, new LichHenService(phienDangNhap)::list);

        dichVu = new LichHenService(phienDangNhap);
        action("+ Tạo lịch hẹn", true, true, this::themLich);
        action("Sửa / Dời lịch", false, true, this::suaLich);
        action("Hủy lịch", false, true, this::huyLich);
        action("Đổi trạng thái", false, true, this::doiTrangThai);
        action("Xếp ca bác sĩ", false, true, this::xepCa);
        action("Xem BN / Bác sĩ", false, false, this::xemDanhMuc);
    }

    // Tao cac truong nhap lich hen
    private List<Field> taoTruongLich() {
        return List.of(
            new Field("Mã bệnh nhân"), new Field("Mã bác sĩ"),
            new Field("Ngày hẹn", LocalDate.now().plusDays(1).toString()),
            new Field("Giờ", "09:00"), new Field("Phút", "60"),
            new Field("Phòng", new String[]{
                "Phòng 01", "Phòng 02", "Phòng 03",
                "Phòng 04", "Phòng 05", "Phòng 06"
            }),
            new Field("Lý do")
        );
    }

    // Kiem tra va luu lich hen
    private void luuLich(Integer maLich, Map<String, String> duLieu) {
        QuyTacNghiepVu.id(duLieu.get("Mã bệnh nhân"), "Mã bệnh nhân");
        QuyTacNghiepVu.id(duLieu.get("Mã bác sĩ"), "Mã bác sĩ");
        QuyTacNghiepVu.date(duLieu.get("Ngày hẹn"), "Ngày hẹn");
        QuyTacNghiepVu.time(duLieu.get("Giờ"), "Giờ");
        QuyTacNghiepVu.durationMinutes(duLieu.get("Phút"), "Số phút");
        QuyTacNghiepVu.text(duLieu.get("Phòng"), "Phòng", 80);
        dichVu.save(maLich, duLieu);
    }

    // Them lich hen
    private void themLich() {
        form("Tạo lịch hẹn mới", taoTruongLich(), Map.of(),
            duLieu -> luuLich(null, duLieu));
    }

    // Sua hoac doi lich hen
    private void suaLich() {
        Map<String, Object> dongDuocChon = selected();
        if (dongDuocChon == null) return;

        int maLich = ((Number) dongDuocChon.get("ID")).intValue();
        form("Dời lịch số " + maLich, taoTruongLich(), dongDuocChon,
            duLieu -> luuLich(maLich, duLieu));
    }

    // Huy lich hen nhung van giu lich su
    private void huyLich() {
        int maLich = selectedId();
        if (maLich == 0) return;

        confirm("Bạn chắc chắn muốn hủy lịch hẹn số " + maLich + "?",() -> dichVu.change(maLich, "Đã hủy"));
    }

    // Doi trang thai lich hen
    private void doiTrangThai() {
        Map<String, Object> dongDuocChon = selected();
        if (dongDuocChon == null) return;

        int maLich = ((Number) dongDuocChon.get("ID")).intValue();
        form("Cập nhật trạng thái lịch số " + maLich,
            List.of(new Field("Trạng thái", new String[]{"Đã xác nhận", "Đã đến", "Chờ khám","Đang khám", "Hoàn tất", "Đã hủy"})),
            Map.of("Trạng thái", dongDuocChon.get("Trạng thái")),duLieu -> dichVu.change(maLich, duLieu.get("Trạng thái")));
    }

    // Xep ca lam viec cua bac si
    private void xepCa() {
        form("Xếp ca làm việc của bác sĩ",
            List.of(
                new Field("Mã bác sĩ"),
                new Field("Ngày", LocalDate.now().plusDays(1).toString()),
                new Field("Giờ bắt đầu", "08:00"),
                new Field("Giờ kết thúc", "11:00"),
                new Field("Tên ca", "Ca sáng")
            ),
            Map.of(), duLieu -> {
                int maBacSi = QuyTacNghiepVu.id(duLieu.get("Mã bác sĩ"), "Mã bác sĩ");
                LocalDate ngay = QuyTacNghiepVu.date(duLieu.get("Ngày"), "Ngày");
                LocalTime gioBatDau = QuyTacNghiepVu.time(duLieu.get("Giờ bắt đầu"), "Giờ bắt đầu");
                LocalTime gioKetThuc = QuyTacNghiepVu.time(duLieu.get("Giờ kết thúc"), "Giờ kết thúc");
                dichVu.addShift(maBacSi, ngay, gioBatDau, gioKetThuc, duLieu.get("Tên ca"));
            });
    }

    // Xem danh sach benh nhan va bac si
    private void xemDanhMuc() {
        infoRows("Danh sách bệnh nhân đang hoạt động", dichVu.patients());
        infoRows("Danh sách bác sĩ đang hoạt động", dichVu.doctors());
    }

    public static void main(String[] args) {
    	 KhungChinh.xemTruoc("Lịch hẹn");
    }
}
