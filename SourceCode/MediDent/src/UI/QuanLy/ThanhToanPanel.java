package UI.QuanLy;

import java.util.List;
import java.util.Map;

import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.QuyTacNghiepVu;
import Dao.NghiepVu.ThanhToanService;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.XemTruoc;

public final class ThanhToanPanel extends BangDuLieuPanel {

    private static final String[] COLS = {
            "ID",
            "Mã phiếu",
            "Bệnh nhân",
            "Số tiền",
            "Trạng thái",
            "Phương thức",
            "Ngày thu",
            "Mã TT",
    };

    public ThanhToanPanel(PhanQuyen.PhienDangNhap session) {
        this(new ThanhToanService(session), session == null);
    }

    private ThanhToanPanel(ThanhToanService service, boolean preview) {
        super(
                "Thanh toán & hóa đơn",
                "Ưu tiên mã phiếu • bệnh nhân • số tiền • trạng thái để người dùng dễ theo dõi thực thu",
                "bill",
                COLS,
                preview,
                service::list);

        action("+ Tạo thanh toán", true, true, () ->
                form(
                        "Thanh toán phiếu khám hoàn tất",
                        List.of(
                                new Field("Mã phiếu"),
                                new Field("Phương thức", new String[] { "Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng" })),
                        Map.of(),
                        values -> service.create(
                                QuyTacNghiepVu.id(values.get("Mã phiếu"), "Mã phiếu"),
                                values.get("Phương thức"))));

        action("Xác nhận thực thu", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                form(
                        "Đối soát hóa đơn #" + id,
                        List.of(new Field("Mã giao dịch")),
                        Map.of(),
                        values -> service.confirm(id, values.get("Mã giao dịch")));
            }
        });

        action("Gửi yêu cầu hủy", false, true, () -> {
            int id = selectedId();
            if (id > 0) {
                form(
                        "Yêu cầu hủy hóa đơn #" + id,
                        List.of(new Field("Lý do")),
                        Map.of(),
                        values -> service.requestCancel(id, values.get("Lý do")));
            }
        });

        action("Duyệt/Từ chối", false, true, () ->
                form(
                        "Phê duyệt yêu cầu hủy phiếu thu chưa thực nhận",
                        List.of(
                                new Field("Mã yêu cầu"),
                                new Field("Quyết định", new String[] { "Duyệt", "Từ chối" }),
                                new Field("Lý do quyết định")),
                        Map.of(),
                        values -> service.decide(
                                QuyTacNghiepVu.id(values.get("Mã yêu cầu"), "Mã yêu cầu"),
                                "Duyệt".equals(values.get("Quyết định")),
                                values.get("Lý do quyết định"))));

        action("Phiếu chưa lập HĐ", false, false, () ->
                infoRows("Phiếu khám hoàn tất có thể lập hóa đơn", service.completed()));

        action("Xem yêu cầu hủy", false, false, () ->
                infoRows("Yêu cầu hủy", service.requests()));
    }

    public static void main(String[] args) {
        XemTruoc.open("Thanh toán và hóa đơn", () -> new ThanhToanPanel(null));
    }
}
