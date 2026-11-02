package DAO;

import Class.ChiTietHoaDon;
import Class.HoaDon;
import ConnectDB.KetNoi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Đọc hóa đơn cho trang "Thanh toán & Hóa đơn" (xem danh sách, xem trước và in).
 *
 * LƯU Ý: tên bảng / cột bên dưới là GIẢ ĐỊNH theo cách đặt tên của các bảng đã có
 * (PhieuKham, BenhNhan, NhanVien). Hãy đối chiếu với script tạo CSDL thật:
 *   dbo.HoaDon(maHD, maTT, maPK, ngayLap, tongTien, trangThai)
 *   dbo.ChiTietDieuTri(maPK, maDV, soLuong, donGia, thanhTien)
 *   dbo.DichVu(maDV, tenDV)
 * Nếu khác tên, chỉ cần sửa các câu SQL ở đầu class này, phần giao diện không đổi.
 */
public final class HoaDonDAO {

    private static final String SQL_DANH_SACH =
            "SELECT h.maHD, h.maPK, ISNULL(p.maBN,0) AS maBN, ISNULL(b.hoTen,N'') AS benhNhan, "
            + "ISNULL(CONVERT(varchar(16), h.ngayLap, 120),N'') AS ngayLap, "
            + "ISNULL(h.trangThai,N'') AS trangThai, "
            + "ISNULL(h.tongTien,0) AS tongTien "
            + "FROM dbo.HoaDon h "
            + "LEFT JOIN dbo.PhieuKham p ON p.maPK=h.maPK "
            + "LEFT JOIN dbo.BenhNhan b ON b.maBN=p.maBN "
            + "ORDER BY h.maHD DESC";

    private static final String SQL_HOA_DON_THEO_MA =
            "SELECT TOP 1 h.maHD, h.maPK, h.ngayLap, ISNULL(h.trangThai,N'') AS trangThai, "
            + "ISNULL(h.tongTien,0) AS tongTien, "
            + "ISNULL(p.maBN,0) AS maBN, ISNULL(b.hoTen,N'') AS benhNhan, ISNULL(b.sdt,N'') AS sdt, "
            + "ISNULL(n.hoTen,N'') AS bacSi "
            + "FROM dbo.HoaDon h "
            + "LEFT JOIN dbo.PhieuKham p ON p.maPK=h.maPK "
            + "LEFT JOIN dbo.BenhNhan b ON b.maBN=p.maBN "
            + "LEFT JOIN dbo.NhanVien n ON n.maNV=p.maNV "
            + "WHERE h.maHD=?";

    // Dòng dịch vụ lấy từ ChiTietDieuTri của phiếu khám (CSDL không có bảng ChiTietHoaDon).
    // Giả định ChiTietDieuTri có maDV, soLuong, donGia - nếu khác tên, sửa ở đây.
    private static final String SQL_CHI_TIET =
            "SELECT ISNULL(d.tenDV,N'') AS tenDV, c.soLuong, c.donGia "
            + "FROM dbo.ChiTietDieuTri c "
            + "LEFT JOIN dbo.DichVu d ON d.maDV=c.maDV "
            + "WHERE c.maPK=? ORDER BY c.maDV";

    /**
     * Danh sách mọi hóa đơn (mới nhất trước) để đổ lên bảng.
     * Mỗi dòng: {STT(0), maHD, maPK, maBN, tên bệnh nhân, ngày lập, tổng tiền (BigDecimal), trạng thái}.
     */
    public List<Object[]> danhSach() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = KetNoi.mo();
                PreparedStatement statement = connection.prepareStatement(SQL_DANH_SACH);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new Object[] {
                    0, // STT, được đánh số lại khi đổ lên bảng
                    result.getInt("maHD"),
                    result.getInt("maPK"),
                    result.getInt("maBN"),
                    result.getString("benhNhan"),
                    result.getString("ngayLap"),
                    result.getBigDecimal("tongTien"),
                    result.getString("trangThai")
                });
            }
        }
        return rows;
    }

    /**
     * Đọc một hóa đơn kèm các dòng dịch vụ để xem trước / in.
     * @return hóa đơn, hoặc null nếu không còn mã hóa đơn này
     */
    public HoaDon timTheoMa(int maHD) throws SQLException {
        try (Connection connection = KetNoi.mo()) {
            HoaDon invoice;
            java.math.BigDecimal tongTien;
            try (PreparedStatement statement = connection.prepareStatement(SQL_HOA_DON_THEO_MA)) {
                statement.setInt(1, maHD);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) return null;
                    Timestamp issued = result.getTimestamp("ngayLap");
                    LocalDateTime when = issued == null ? null : issued.toLocalDateTime();
                    tongTien = result.getBigDecimal("tongTien");
                    invoice = new HoaDon(result.getInt("maHD"), result.getInt("maPK"),
                            result.getInt("maBN"), result.getString("benhNhan"),
                            result.getString("sdt"), result.getString("bacSi"),
                            when, result.getString("trangThai"));
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(SQL_CHI_TIET)) {
                statement.setInt(1, invoice.maPK);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        invoice.chiTiet.add(new ChiTietHoaDon(result.getString("tenDV"),
                                result.getInt("soLuong"), result.getBigDecimal("donGia")));
                    }
                }
            } catch (SQLException ex) {
                // Chưa khớp tên cột của ChiTietDieuTri: vẫn in được hóa đơn bằng một dòng
                // theo tổng tiền đã lưu trong HoaDon, thay vì không in được.
                invoice.chiTiet.clear();
                invoice.chiTiet.add(new ChiTietHoaDon("Chi phí khám & điều trị (phiếu khám "
                        + invoice.maPK + ")", 1, tongTien));
            }
            return invoice;
        }
    }
}
