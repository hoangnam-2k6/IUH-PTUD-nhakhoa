package DAO.ThucThe;

import ConnectDB.KetNoi;
import DAO.KhoDuLieu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO nền chỉ đọc, dùng PreparedStatement và chỉ nhận bảng/cột cố định trong mã nguồn.
 * Các thao tác ghi vẫn do DAO nghiệp vụ thực hiện để kiểm tra quyền và ghi nhật ký.
 */
public abstract class CoSoThucTheDAO<T> {
    private final String bang, khoaChinh, quyen;
    private final boolean chiQuanTriVien;
    private final KhoDuLieu phanQuyen = new KhoDuLieu();

    protected CoSoThucTheDAO(String bang, String khoaChinh,
                             String quyen, boolean chiQuanTriVien) {
        this.bang = bang;
        this.khoaChinh = khoaChinh;
        this.quyen = quyen;
        this.chiQuanTriVien = chiQuanTriVien;
    }

    /** Không tin thông tin quyền phía giao diện; kiểm tra tại SQL mỗi lần đọc. */
    private void kiemTraQuyen(int maTaiKhoan) throws SQLException {
        if (maTaiKhoan <= 0 || !phanQuyen.taiKhoanDangHoatDong(maTaiKhoan)
                || !phanQuyen.coQuyenTaiKhoan(maTaiKhoan, quyen)
                || (chiQuanTriVien && !phanQuyen.laQuanTriVienTaiKhoan(maTaiKhoan)))
            throw new SQLException("Tài khoản không có quyền đọc dữ liệu " + bang + ".");
    }

    /** Chuyển hàng ResultSet thành thực thể Java tương ứng. */
    protected abstract T anhXa(ResultSet ketQua) throws SQLException;

    /** Tra cứu theo khóa chính đơn, tham số hóa tránh SQL injection. */
    public T timTheoMa(Object ma, int maTaiKhoan) throws SQLException {
        kiemTraQuyen(maTaiKhoan);
        if (khoaChinh == null) throw new SQLException("Bảng dùng khóa ghép, hãy truy vấn theo khóa ghép.");
        String sql = "SELECT * FROM dbo." + bang + " WHERE " + khoaChinh + "=?";
        try (Connection ketNoi = KetNoi.mo();
             PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setObject(1, ma);
            try (ResultSet ketQua = lenh.executeQuery()) {
                return ketQua.next() ? anhXa(ketQua) : null;
            }
        }
    }

    /** Lấy danh sách giới hạn để không đọc toàn bộ dữ liệu quá lớn. */
    public List<T> layDanhSach(int gioiHan, int maTaiKhoan) throws SQLException {
        kiemTraQuyen(maTaiKhoan);
        if (gioiHan < 1 || gioiHan > 1000) throw new IllegalArgumentException("Giới hạn từ 1 đến 1000.");
        String sql = "SELECT TOP (?) * FROM dbo." + bang
                + (khoaChinh == null ? "" : " ORDER BY " + khoaChinh + " DESC");
        List<T> danhSach = new ArrayList<>();
        try (Connection ketNoi = KetNoi.mo();
             PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            lenh.setInt(1, gioiHan);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) danhSach.add(anhXa(ketQua));
            }
        }
        return danhSach;
    }
}
