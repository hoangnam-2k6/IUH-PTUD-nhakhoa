package UI.QuanLy;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Class.TaiKhoan;
import DAO.KhoDuLieu;

public class nhanvienquanly_gui extends JPanel{
    private nhanvienquanly_gui() { }
    private KhoDuLieu db;
    private TaiKhoan user;

    public nhanvienquanly_gui(KhoDuLieu db, TaiKhoan user) {

        this.db = db;
        this.user = user;

        setLayout(new BorderLayout());

        // Khởi tạo các thành phần giao diện ở đây.
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(BenhNhanGUI::new);
    }
    
}
