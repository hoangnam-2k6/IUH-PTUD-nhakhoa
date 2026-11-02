package ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** [QL-TEST] Chỉ đọc: kiểm tra 6 cấu trúc SQL quan trọng trước khi kiểm tra CRUD. */
public final class KiemTraCauTrucQuanLy {
    private KiemTraCauTrucQuanLy() { }

    public static void main(String[] args) {
        String sql = "SELECT DB_NAME() AS db, "
                + "CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL THEN 0 ELSE 1 END AS bn, "
                + "CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL THEN 0 ELSE 1 END AS lich, "
                + "CASE WHEN OBJECT_ID(N'dbo.ChiPhiHoatDong',N'U') IS NULL THEN 0 ELSE 1 END AS cp, "
                + "CASE WHEN OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL THEN 0 ELSE 1 END AS phong, "
                + "CASE WHEN OBJECT_ID(N'dbo.YeuCauHoTro',N'U') IS NULL THEN 0 ELSE 1 END AS hotro, "
                + "CASE WHEN COL_LENGTH(N'dbo.LichHen',N'soThuTuKham') IS NULL THEN 0 ELSE 1 END AS stt";
        try (Connection c = KetNoi.mo(); PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Database dang kiem tra: " + rs.getString("db"));
                System.out.println("BenhNhan.trangThaiHoSo: " + (rs.getInt("bn") == 1 ? "DA CO" : "THIEU"));
                System.out.println("LichHen.thoiLuongPhut: " + (rs.getInt("lich") == 1 ? "DA CO" : "THIEU"));
                System.out.println("ChiPhiHoatDong: " + (rs.getInt("cp") == 1 ? "DA CO" : "THIEU"));
                System.out.println("PhongKham: " + (rs.getInt("phong") == 1 ? "DA CO" : "THIEU"));
                System.out.println("YeuCauHoTro: " + (rs.getInt("hotro") == 1 ? "DA CO" : "THIEU"));
                System.out.println("LichHen.soThuTuKham: " + (rs.getInt("stt") == 1 ? "DA CO" : "THIEU"));
                if (rs.getInt("bn") == 0 || rs.getInt("lich") == 0 || rs.getInt("cp") == 0 || rs.getInt("phong") == 0 || rs.getInt("hotro") == 0 || rs.getInt("stt") == 0) {
                    System.out.println("Sao luu DB truoc, sau do chay SQLData/01_MediDent.sql trong SSMS.");
                } else {
                    System.out.println("Schema 6 doi tuong da san sang; tiep theo can kiem thu CRUD thuc te.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Kiem tra schema khong thanh cong: " + e.getMessage());
        }
    }
}
