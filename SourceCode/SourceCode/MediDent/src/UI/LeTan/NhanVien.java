package UI.LeTan;
import javax.swing.*;
import java.awt.*;
/** KHUNG SẴN: phân hệ Lễ tân chưa xây dựng. Không thao tác CSDL. */
public class NhanVien extends JPanel {
    public NhanVien() {
        super(new BorderLayout());
        add(new JLabel("Lễ tân: chức năng nhân viên CHƯA TRIỂN KHAI", SwingConstants.CENTER), BorderLayout.CENTER);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("MEDIDENT - Lễ tân (khung)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(700, 450);
            f.setLocationRelativeTo(null);
            f.add(new NhanVien());
            f.setVisible(true);
        }
        );
    }
}
