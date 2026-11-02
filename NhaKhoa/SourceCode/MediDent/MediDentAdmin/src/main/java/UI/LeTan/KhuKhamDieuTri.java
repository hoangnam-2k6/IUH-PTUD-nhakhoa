package UI.LeTan;

import static UI.LeTan.BangDuLieu.boCuon;
import static UI.LeTan.BangDuLieu.datCot;
import static UI.LeTan.BangDuLieu.taoBang;
import static UI.LeTan.BangDuLieu.taoModel;
import static UI.LeTan.BangDuLieu.taoSorter;
import static UI.LeTan.GiaoDien.MUTED;
import static UI.LeTan.GiaoDien.PRIMARY_DARK;
import static UI.LeTan.GiaoDien.TEXT;
import static UI.LeTan.GiaoDien.label;
import static UI.LeTan.GiaoDien.sectionTitle;
import static UI.LeTan.ThanhTimKiem.cols;

import UI.LeTan.ThanhTimKiem.TieuChi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Khu vực "Khám & điều trị": danh sách phiếu khám (chỉ xem), tìm kiếm / lọc,
 * và nhấp đúp để xem chi tiết. (In hóa đơn nằm ở trang Thanh toán & Hóa đơn.)
 */
public final class KhuKhamDieuTri extends KhungBoGoc {

    private static final String[] TIEU_DE_CHI_TIET = {
        "Mã phiếu khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã nhân viên",
        "Bác sĩ phụ trách", "Ngày khám", "Trạng thái", "Triệu chứng",
        "Chẩn đoán", "Phương pháp điều trị", "Kết quả", "Ghi chú"
    };

    private final DefaultTableModel phieuModel;
    private final JTable phieuTable;
    private final TableRowSorter<DefaultTableModel> phieuSorter;
    private final NhanDem phieuCount = new NhanDem("0");

    public KhuKhamDieuTri() {
        super(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(17, 18, 20, 18));
        setMinimumSize(new Dimension(310, 320));

        // Mã phiếu, mã bệnh nhân và mã bác sĩ đặt ở đầu để dễ nhìn trong khung chia đôi.
        // Các thuộc tính còn lại xem bằng cuộn ngang hoặc nhấp đúp để mở chi tiết.
        phieuModel = taoModel(
                "STT", "Mã phiếu", "Mã BN", "Bệnh nhân", "Mã NV", "Bác sĩ",
                "Ngày khám", "Trạng thái", "Triệu chứng", "Chẩn đoán",
                "Phương pháp điều trị", "Kết quả", "Ghi chú");
        phieuTable = taoBang(phieuModel);
        phieuSorter = taoSorter(phieuTable, phieuModel);
        caiDatCotBang();

        // TieuChi(tên hiển thị, cột mã, cột văn bản, cột SĐT) - số cột tính theo model.
        TieuChi[] criteria = {
            new TieuChi("Tất cả", cols(1, 2), cols(3, 5), cols()),
            new TieuChi("Mã phiếu", cols(1), cols(), cols()),
            new TieuChi("Mã BN", cols(2), cols(), cols()),
            new TieuChi("Tên bệnh nhân", cols(), cols(3), cols()),
            new TieuChi("Mã bác sĩ", cols(4), cols(), cols()),
            new TieuChi("Tên bác sĩ", cols(), cols(5), cols()),
            new TieuChi("Ngày khám", cols(), cols(6), cols()),
            new TieuChi("Trạng thái", cols(), cols(7), cols()),
            new TieuChi("Chẩn đoán", cols(), cols(9), cols())
        };

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(sectionTitle("Khám & điều trị",
                "Danh sách phiếu khám; nhấp đúp để xem chi tiết", phieuCount), BorderLayout.NORTH);
        top.add(ThanhTimKiem.taoThanh(phieuSorter, criteria), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(boCuon(phieuTable), BorderLayout.CENTER);
        add(label("Cuộn ngang để xem đủ thông tin · Nhấp đúp để xem phiếu khám.",
                11, false), BorderLayout.SOUTH);

        phieuTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) hienChiTiet();
            }
        });
    }

    /** Đổ danh sách phiếu khám mới tải lên bảng (STT đánh lại từ 1). */
    public void napDuLieu(List<Object[]> rows) {
        phieuModel.setRowCount(0);
        int stt = 1;
        for (Object[] values : rows) {
            Object[] copy = values.clone();
            copy[0] = stt++; // STT cố định theo thứ tự dữ liệu tải về.
            phieuModel.addRow(copy);
        }
        phieuCount.setText(String.valueOf(rows.size()));
    }

    /** Gắn cách hiển thị riêng cho từng cột của bảng. */
    private void caiDatCotBang() {
        // STT: số mờ, căn giữa.
        datCot(phieuTable, 0, new BoVeCanGiua(MUTED, false));
        // Mã phiếu: in đậm, màu chủ đạo, căn giữa.
        datCot(phieuTable, 1, new BoVeCanGiua(PRIMARY_DARK, true));
        // Mã BN và Mã NV trong bảng phiếu khám: căn giữa.
        datCot(phieuTable, 2, new BoVeCanGiua(TEXT, false));
        datCot(phieuTable, 4, new BoVeCanGiua(TEXT, false));
        // Tên kèm avatar.
        datCot(phieuTable, 3, new BoVeAvatar());
        // Nhãn màu cho trạng thái.
        datCot(phieuTable, 7, new BoVeNhan(false));
    }

    /** Hiển thị đầy đủ thuộc tính phiếu khám, không cần kéo ngang để đọc nội dung dài. */
    private void hienChiTiet() {
        int viewRow = phieuTable.getSelectedRow();
        if (viewRow < 0) return;
        HopThoaiChiTiet.hien(this, phieuModel, phieuTable.convertRowIndexToModel(viewRow),
                TIEU_DE_CHI_TIET, "Chi tiết phiếu khám");
    }
}
