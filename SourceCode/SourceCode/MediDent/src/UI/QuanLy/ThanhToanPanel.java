
package UI.QuanLy;

import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import Dao.NghiepVu.PhanQuyen;
import Dao.NghiepVu.QuyTacNghiepVu;
import Dao.NghiepVu.ThanhToanService;
import UI.Chung.BangDuLieuPanel;
import UI.Chung.GiaoDien;
import UI.Chung.XemTruoc;

public final class ThanhToanPanel extends BangDuLieuPanel {

    private static final String[] CAC_COT = {
        "ID", "Mã phiếu", "Bệnh nhân", "Số tiền", "Trạng thái",
        "Ngày lập", "Phương thức", "Ngày thu", "Mã TT"
    };

    private final ThanhToanService dichVu;
    private final JComboBox<String> chonThang = new JComboBox<>();
    private final JTextField oNam = new JTextField("");
    private int thangDaLoc = 0;
    private int namDaLoc = 0;

    public ThanhToanPanel(PhanQuyen.PhienDangNhap phienDangNhap) {
        super("Thanh toán và hóa đơn",
              "Lập hóa đơn, ghi nhận thực thu, duyệt hoặc từ chối yêu cầu hủy",
              "", CAC_COT, phienDangNhap == null, new ThanhToanService(phienDangNhap)::list);

        dichVu = new ThanhToanService(phienDangNhap);
        taoBoLocHoaDon();
        đặtNguồnDữLiệu(() -> dichVu.list(namDaLoc, thangDaLoc));

        action("+ Tạo thanh toán", true, true, this::themHoaDon);
        action("Xác nhận thu", false, true, this::xacNhanThu);
        action("Yêu cầu hủy", false, true, this::yeuCauHuy);
        action("Duyệt / Từ chối", false, true, this::duyetHuy);
        action("Phiếu chưa lập HĐ", false, false,
            () -> infoRows("Phiếu có thể lập hóa đơn", dichVu.completed()));
        action("Các yêu cầu hủy", false, false,
            () -> infoRows("Danh sách yêu cầu hủy", dichVu.requests()));
    }

    // Tao bo loc thang va nam
    private void taoBoLocHoaDon() {
        chonThang.addItem("Tất cả");
        for (int i = 1; i <= 12; i++) chonThang.addItem("Tháng " + i);
        chonThang.setSelectedIndex(0);

        GiaoDien.styleInput(chonThang);
        chonThang.setPreferredSize(new java.awt.Dimension(95, 40));

        oNam.setColumns(5);
        GiaoDien.styleInput(oNam);
        oNam.setPreferredSize(new java.awt.Dimension(80, 40));
        oNam.setToolTipText("Để trống năm để xem toàn bộ hóa đơn");

        JButton nutLoc = GiaoDien.button("Lọc tháng", false);
        JButton nutTatCa = GiaoDien.button("Tất cả", false);

        nutLoc.addActionListener(e -> locTheoThang());
        nutTatCa.addActionListener(e -> {
            chonThang.setSelectedIndex(0);
            oNam.setText("");
            thangDaLoc = 0;
            namDaLoc = 0;
            refresh();
        });

        thêmĐiềuKiệnLọc(new JLabel("Tháng:"), chonThang,
            new JLabel("Năm:"), oNam, nutLoc, nutTatCa);
    }

    // Loc hoa don theo thang va nam
    private void locTheoThang() {
        try {
            int thangMoi = chonThang.getSelectedIndex();
            String namNhap = oNam.getText().trim();
            int namMoi = namNhap.isEmpty() ? 0 : QuyTacNghiepVu.id(namNhap, "Năm");

            QuyTacNghiepVu.need(namMoi == 0 || (namMoi >= 2000 && namMoi <= 2100),"Năm phải từ 2000 đến 2100");
            QuyTacNghiepVu.need(thangMoi == 0 || namMoi > 0,"Chọn tháng thì phải nhập năm");

            thangDaLoc = thangMoi;
            namDaLoc = namMoi;
            refresh();
        } catch (Exception loi) {
            GiaoDien.error(this, loi);
        }
    }

    // Tao hoa don tu phieu kham da hoan tat
    private void themHoaDon() {
        form("Lập hóa đơn mới",
            List.of(new Field("Mã phiếu"),new Field("Phương thức", new String[]{"Tiền mặt", "Chuyển khoản", "Thẻ ngân hàng" })),
            Map.of(), duLieu -> {
                int maPhieu = QuyTacNghiepVu.id(duLieu.get("Mã phiếu"), "Mã phiếu");
                dichVu.create(maPhieu, duLieu.get("Phương thức"));
            });
    }

    // Xac nhan da thu tien
    private void xacNhanThu() {
        int maHoaDon = selectedId();
        if (maHoaDon == 0) return;

        form("Đối soát hóa đơn số " + maHoaDon,
            List.of(new Field("Mã giao dịch")), Map.of(), duLieu -> {
                QuyTacNghiepVu.text(duLieu.get("Mã giao dịch"), "Mã giao dịch", 100);
                dichVu.confirm(maHoaDon, duLieu.get("Mã giao dịch"));
            });
    }

    // Gui yeu cau huy hoa don
    private void yeuCauHuy() {
        int maHoaDon = selectedId();
        if (maHoaDon == 0) return;

        form("Đề nghị hủy hóa đơn số " + maHoaDon,
            List.of(new Field("Lý do hủy")), Map.of(), duLieu -> {
                QuyTacNghiepVu.text(duLieu.get("Lý do hủy"), "Lý do hủy", 500);
                dichVu.requestCancel(maHoaDon, duLieu.get("Lý do hủy"));
            });
    }

    // Duyet hoac tu choi yeu cau huy
    private void duyetHuy() {
        form("Xét duyệt yêu cầu hủy",
            List.of(
                new Field("Mã yêu cầu"),
                new Field("Quyết định", new String[]{"Duyệt", "Từ chối"}),
                new Field("Lý do quyết định")
            ),
            Map.of(), duLieu -> {
                int maYeuCau = QuyTacNghiepVu.id(duLieu.get("Mã yêu cầu"), "Mã yêu cầu");
                QuyTacNghiepVu.text(duLieu.get("Lý do quyết định"), "Lý do quyết định", 500);
                dichVu.decide(maYeuCau, "Duyệt".equals(duLieu.get("Quyết định")),duLieu.get("Lý do quyết định"));
            });
    }

    public static void main(String[] args) {
        KhungChinh.xemTruoc("Hóa đơn");
    }
}
