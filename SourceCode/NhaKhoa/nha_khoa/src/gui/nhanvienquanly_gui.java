package gui;

import javax.swing.SwingUtilities;

public class nhanvienquanly_gui {
    private nhanvienquanly_gui() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BenhNhanGUI::new);
    }
}
