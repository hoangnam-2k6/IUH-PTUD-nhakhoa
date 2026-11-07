package UI.QuanLy;

import java.util.List;
import java.util.Map;

import Dao.NghiepVu.BenhNhanService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class BenhNhanPanel extends BangDuLieuPanel {

    private static final String[] COLS = {
            "ID",
            "Họ tên",
            "SĐT",
            "Giới tính",
            "Ngày sinh",
            "Tuổi",
            "Trạng thái",
            "Tiền sử bệnh",
            "Email",
            "Địa chỉ",
    };

    private static final List<Field> FIELDS = List.of(
            new Field("Họ tên"),
            new Field("SĐT"),
            new Field("Ngày sinh", "2000-01-01"),
            new Field("Giới tính", new String[] { "Nam", "Nữ", "Khác" }),
            new Field("Email"),
            new Field("Địa chỉ"),
            new Field("Tiền sử bệnh"));

    public BenhNhanPanel(PhanQuyen.PhienDangNhap session) {
        this(new BenhNhanService(session), session == null);
    }

    private BenhNhanPanel(BenhNhanService service, boolean preview) {
        super(
                "Quản lý bệnh nhân",
                "Ưu tiên hồ sơ bệnh nhân theo họ tên • liên hệ • trạng thái, hạn chế đưa cột quan trọng ra phía sau",
                "patient",
                COLS,
                preview,
                service::list);

        action("+ Thêm bệnh nhân", true, true, () ->
                form("Tạo hồ sơ bệnh nhân", FIELDS, Map.of(), values -> service.save(null, values)));

        action("Sửa", false, true, () -> {
            Map<String, Object> row = selected();
            if (row != null) {
                form(
                        "Chỉnh sửa hồ sơ #" + row.get("ID"),
                        FIELDS,
                        row,
                        values -> service.save(((Number) row.get("ID")).intValue(), values));
            }
        });

        action("Ngừng hồ sơ", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                confirm(
                        "Ngừng hồ sơ bệnh nhân #" + id + "? Lịch khám và hóa đơn cũ vẫn được giữ.",
                        () -> service.stop(id));
            }
        });

        action("Khôi phục", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                confirm("Khôi phục hồ sơ bệnh nhân #" + id + "?", () -> service.restore(id));
            }
        });
    }

    public static void main(String[] args) {
        XemTruoc.open("Quản lý bệnh nhân", () -> new BenhNhanPanel(null));
    }
}
