package UI.QuanLy;

import java.util.List;
import java.util.Map;

import Dao.NghiepVu.DichVuService;
import Dao.NghiepVu.PhanQuyen;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class DichVuPanel extends BangDuLieuPanel {

    private static final String[] COLS = {
            "ID",
            "Tên dịch vụ",
            "Đơn giá",
            "Trạng thái",
            "Mô tả",
    };

    private static final List<Field> FIELDS = List.of(
            new Field("Tên dịch vụ"),
            new Field("Đơn giá", "250000"),
            new Field("Mô tả"),
            new Field("Trạng thái", new String[] { "Đang áp dụng", "Ngừng áp dụng" }));

    public DichVuPanel(PhanQuyen.PhienDangNhap session) {
        this(new DichVuService(session), session == null);
    }

    private DichVuPanel(DichVuService service, boolean preview) {
        super(
                "Dịch vụ nha khoa",
                "Ưu tiên tên dịch vụ • đơn giá • trạng thái để người dùng nhìn thấy thông tin chính đầu tiên",
                "tooth",
                COLS,
                preview,
                service::list);

        action("+ Thêm dịch vụ", true, true, () ->
                form("Thêm dịch vụ", FIELDS, Map.of(), values -> service.save(null, values)));

        action("Sửa", false, true, () -> {
            Map<String, Object> row = selected();
            if (row != null) {
                form(
                        "Sửa dịch vụ #" + row.get("ID"),
                        FIELDS,
                        row,
                        values -> service.save(((Number) row.get("ID")).intValue(), values));
            }
        });

        action("Ngừng áp dụng", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                confirm("Ngừng áp dụng dịch vụ #" + id + "?", () -> service.stop(id));
            }
        });
    }

    public static void main(String[] args) {
        XemTruoc.open("Dịch vụ nha khoa", () -> new DichVuPanel(null));
    }
}
