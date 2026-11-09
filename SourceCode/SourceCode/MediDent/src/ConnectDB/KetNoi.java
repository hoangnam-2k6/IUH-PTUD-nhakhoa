
package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class KetNoi {

    // Khong cho tao doi tuong KetNoi
    private KetNoi() {}

    // Mo ket noi den SQL Server
    public static Connection mo() throws SQLException {
        CauHinh.require();

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException loi) {
            throw new SQLException("Thiếu thư viện JDBC SQL Server. Kiểm tra thư viện trong project.", loi);
        }

        Connection ketNoi = DriverManager.getConnection(
            CauHinh.value("db.url"),
            CauHinh.value("db.user"),
            CauHinh.value("db.password")
        );

        if (!"MediDent".equalsIgnoreCase(ketNoi.getCatalog())) {
            ketNoi.close();
            throw new SQLException("Đang kết nối sai database; cần MediDent.");
        }
        return ketNoi;
    }

    // Kiem tra ket noi SQL Server
    public static void kiemTra() throws SQLException {
        try (Connection ketNoi = mo();
             Statement cauLenh = ketNoi.createStatement();
             ResultSet ketQua = cauLenh.executeQuery("SELECT 1")) {

            if (!ketQua.next())
                throw new SQLException("Không thể đọc SQL Server");
        }
    }

    // Kiem tra cau truc database MediDent
    public static void kiemTraCauTruc() throws SQLException {
        String[] danhSachBang = {
            "VaiTro", "TaiKhoan", "QuanLy", "NhanVien", "BenhNhan",
            "DichVu", "BangGiaDichVu", "LichLamViec", "LichHen",
            "PhongKham", "PhieuKham", "ChiTietDieuTri", "ThanhToan",
            "HoaDon", "YeuCauHuyPhieuThu", "PhieuDuyetHuyPhieuThu",
            "ChiPhiHoatDong", "NhatKyHeThong"
        };

        try (Connection ketNoi = mo();
             PreparedStatement lenhKiemTraBang =
                 ketNoi.prepareStatement("SELECT OBJECT_ID(?, N'U')")) {

            // Kiem tra cac bang co ton tai khong
            for (String tenBang : danhSachBang) {
                lenhKiemTraBang.setString(1, "dbo." + tenBang);

                try (ResultSet ketQua = lenhKiemTraBang.executeQuery()) {
                    if (!ketQua.next() || ketQua.getObject(1) == null) {
                        throw new SQLException("Thiếu bảng " + tenBang
                            + ". Hãy chạy SQL 01, 07, 08, 13 theo hướng dẫn.");
                    }
                }
            }

            // Danh sach cot can kiem tra
            String[][] danhSachCot = {
                {"BenhNhan", "trangThaiHoSo"},
                {"LichHen", "thoiLuongPhut"},
                {"ThanhToan", "ngayThanhToan"},
                {"YeuCauHuyPhieuThu", "maTKNguoiGui"}
            };

            // Kiem tra cac cot trong bang
            try (PreparedStatement lenhKiemTraCot =
                 ketNoi.prepareStatement("SELECT COL_LENGTH(?, ?)")) {

                for (String[] thongTinCot : danhSachCot) {
                    String tenBang = thongTinCot[0];
                    String tenCot = thongTinCot[1];

                    lenhKiemTraCot.setString(1, "dbo." + tenBang);
                    lenhKiemTraCot.setString(2, tenCot);

                    try (ResultSet ketQua = lenhKiemTraCot.executeQuery()) {
                        if (!ketQua.next() || ketQua.getObject(1) == null) {
                            throw new SQLException("Thiếu cột " + tenBang + "."
                                + tenCot + ". Chạy SqlData/13_NangCap_QuanLy_KhongXoaDuLieu.sql.");
                        }
                    }
                }
            }
        }
    }

    // Chay truc tiep de kiem tra ket noi va database
    public static void main(String[] args) {
        try {
            System.out.println("[MEDIDENT] Kết nối cấu hình trực tiếp trong ConnectDB/CauHinh.java");
            kiemTraCauTruc();
            System.out.println("[MEDIDENT] Kết nối và kiểm tra cấu trúc SQL thành công.");
        } catch (Exception loi) {
            System.err.println("[MEDIDENT] Lỗi kết nối/cấu trúc: " + loi.getMessage());
            System.exit(1);
        }
    }
}
