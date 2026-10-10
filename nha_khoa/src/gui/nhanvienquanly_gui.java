package gui;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
public class nhanvienquanly_gui {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            new TrangChuQuanLyGUI();
        });
    }
}
