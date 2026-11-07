package UI.QuanLy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import Dao.NghiepVu.LichHenService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class LichHenPanel extends BangDuLieuPanel {

    private static final String[] COLS = {
            "ID",
            "Ngày hẹn",
            "Giờ",
            "Phút",
            "Bệnh nhân",
            "Bác sĩ",
            "Phòng",
            "Trạng thái",
            "Lý do",
            "Mã bệnh nhân",
            "Mã bác sĩ",
    };

    private static final String[] ROOMS = {
            "Phòng 01",
            "Phòng 02",
            "Phòng 03",
            "Phòng 04",
            "Phòng 05",
            "Phòng 06"
    };

    public LichHenPanel(PhanQuyen.PhienDangNhap session) {
        this(new LichHenService(session), session == null);
    }

    private LichHenPanel(LichHenService service, boolean preview) {
        super(
                "Quản lý lịch hẹn",
                "Ưu tiên ngày giờ • bệnh nhân • bác sĩ • phòng để theo dõi lịch nhanh và tránh trùng ca",
                "calendar",
                COLS,
                preview,
                service::list);

        action("+ Tạo lịch hẹn", true, true, () ->
                form("Tạo lịch mới", fields(), Map.of(), values -> service.save(null, values)));

        action("Dời lịch", false, true, () -> {
            Map<String, Object> row = selected();
            if (row != null) {
                form(
                        "Dời lịch #" + row.get("ID"),
                        fields(),
                        row,
                        values -> service.save(((Number) row.get("ID")).intValue(), values));
            }
        });

        action("Đổi trạng thái", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                form(
                        "Đổi trạng thái lịch #" + id,
                        List.of(new Field("Trạng thái", new String[] {
                                "Đã xác nhận", "Đã đến", "Chờ khám", "Đang khám", "Hoàn tất", "Đã hủy"
                        })),
                        Map.of(),
                        values -> service.change(id, values.get("Trạng thái")));
            }
        });

        action("Xếp ca bác sĩ", false, true, () ->
                form(
                        "Xếp ca bác sĩ",
                        List.of(
                                new Field("Mã bác sĩ"),
                                new Field("Ngày", LocalDate.now().plusDays(1).toString()),
                                new Field("Giờ bắt đầu", "08:00"),
                                new Field("Giờ kết thúc", "11:00"),
                                new Field("Tên ca", "Ca sáng")),
                        Map.of(),
                        values -> service.addShift(
                                Dao.NghiepVu.QuyTacNghiepVu.id(values.get("Mã bác sĩ"), "Mã bác sĩ"),
                                Dao.NghiepVu.QuyTacNghiepVu.date(values.get("Ngày"), "Ngày"),
                                LocalTime.parse(values.get("Giờ bắt đầu")),
                                LocalTime.parse(values.get("Giờ kết thúc")),
                                values.get("Tên ca"))));

        action("Bệnh nhân / Bác sĩ", false, false, () -> {
            infoRows("Bệnh nhân đang hoạt động", service.patients());
            infoRows("Bác sĩ đang hoạt động", service.doctors());
        });
    }

    private static List<Field> fields() {
        return List.of(
                new Field("Mã bệnh nhân"),
                new Field("Mã bác sĩ"),
                new Field("Ngày hẹn", LocalDate.now().plusDays(1).toString()),
                new Field("Giờ", "09:00"),
                new Field("Phút", "60"),
                new Field("Phòng", ROOMS),
                new Field("Lý do"));
    }

    public static void main(String[] args) {
        XemTruoc.open("Quản lý lịch hẹn", () -> new LichHenPanel(null));
    }
}
