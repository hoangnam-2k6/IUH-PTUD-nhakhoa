package UI.LeTan;

import Class.TaiKhoan;
import DAO.KhoDuLieu;
import UI.Chung.GiaoDien;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JPanel;

/** Dashboard riêng cho Lễ tân: tiếp nhận bệnh nhân và theo dõi lịch hẹn. */
public class TrangChuLeTan extends JPanel {

    public TrangChuLeTan(KhoDuLieu db, TaiKhoan user) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);

        JPanel noiDung = GiaoDien.trang(
                "Trang chủ Lễ tân",
                "Xin chào " + user.hoTen + ". Theo dõi bệnh nhân và lịch hẹn.");
        add(GiaoDien.cuon(noiDung), BorderLayout.CENTER);

        try {
            int benhNhan = db.dem("SELECT COUNT(*) FROM dbo.BenhNhan");
            int lichHomNay = db.dem(
                    "SELECT COUNT(*) FROM dbo.LichHen WHERE ngayHen = CAST(GETDATE() AS DATE)");
            int choXacNhan = db.dem(
                    "SELECT COUNT(*) FROM dbo.LichHen "
                    + "WHERE trangThai = N'Chờ xác nhận'");
            int lichSapToi = db.dem(
                    "SELECT COUNT(*) FROM dbo.LichHen "
                    + "WHERE ngayHen > CAST(GETDATE() AS DATE) "
                    + "AND ngayHen <= DATEADD(day, 7, CAST(GETDATE() AS DATE))");

            JPanel luoi = new JPanel(new GridLayout(2, 2, 14, 14));
            luoi.setOpaque(false);
            luoi.setAlignmentX(LEFT_ALIGNMENT);
            luoi.setPreferredSize(new Dimension(700, 250));
            luoi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

            luoi.add(the("Tổng bệnh nhân", benhNhan));
            luoi.add(the("Lịch hẹn hôm nay", lichHomNay));
            luoi.add(the("Lịch chờ xác nhận", choXacNhan));
            luoi.add(the("Lịch hẹn 7 ngày tới", lichSapToi));
            noiDung.add(luoi);
            noiDung.add(Box.createVerticalStrut(18));
        } catch (Exception ex) {
            noiDung.add(GiaoDien.label("Không tải được thống kê: " + ex.getMessage(), 12, false));
        }
    }

    /** Tạo thẻ thống kê, dùng chung bảng màu MediDent. */
    private JPanel the(String nhan, int giaTri) {
        JPanel khung = GiaoDien.card(new BorderLayout(0, 12));
        khung.add(GiaoDien.label(nhan, 12, false), BorderLayout.NORTH);
        khung.add(GiaoDien.label(String.valueOf(giaTri), 26, true), BorderLayout.CENTER);
        return khung;
    }
}
