package UI.QuanLy;

import java.util.List;
import java.util.Map;

import Dao.NghiepVu.NhanVienService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class NhanVienPanel extends BangDuLieuPanel {

    private static final String[] COLS = {
            "ID",
            "Họ tên",
            "Chức vụ",
            "Trạng thái",
            "SĐT",
            "Giới tính",
            "Ngày sinh",
            "Tuổi",
            "Email",
            "Địa chỉ",
    };

    private static final List<Field> FIELDS = List.of(
            new Field("Họ tên"),
            new Field("SĐT"),
            new Field("Ngày sinh", "1995-01-01"),
            new Field("Giới tính", new String[] { "Nam", "Nữ", "Khác" }),
            new Field("Email"),
            new Field("Địa chỉ"),
            new Field("Chức vụ", new String[] { "Bác sĩ", "Lễ tân", "Kế toán", "Nhân viên", "Quản lý phòng khám" }),
            new Field("Trạng thái", new String[] { "Đang làm việc", "Tạm nghỉ", "Nghỉ việc" }));

    public NhanVienPanel(PhanQuyen.PhienDangNhap session) {
        this(new NhanVienService(session), session == null);
    }

    private NhanVienPanel(NhanVienService service, boolean preview) {
        super(
                "Quản lý nhân viên",
                "Ưu tiên hiển thị họ tên • chức vụ • trạng thái để quản lý nhân sự nhanh hơn",
                "staff",
                COLS,
                preview,
                service::list);

        action("+ Thêm nhân viên", true, true, () ->
                form("Thêm hồ sơ nhân viên", FIELDS, Map.of(), values -> service.save(null, values)));

        action("Sửa", false, true, () -> {
            Map<String, Object> row = selected();
            if (row != null) {
                form(
                        "Cập nhật nhân viên #" + row.get("ID"),
                        FIELDS,
                        row,
                        values -> service.save(((Number) row.get("ID")).intValue(), values));
            }
        });

        action("Cho nghỉ việc", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                confirm(
                        "Cho nhân viên #" + id + " nghỉ việc? Hệ thống sẽ từ chối nếu nhân viên còn lịch/ca làm việc.",
                        () -> service.stop(id));
            }
        });
    }

    public static void main(String[] args) {
        XemTruoc.open("Quản lý nhân viên", () -> new NhanVienPanel(null));
    }
}
