package UI.QuanLy;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SuaNhanVienPanel extends JPanel {

    private String maNV;
    private Runnable quayLai;

    public SuaNhanVienPanel(String maNV, Runnable callback) {

        this.maNV = maNV;
        this.quayLai = callback;

        // Khởi tạo giao diện sửa nhân viên

        // Dùng maNV để lấy thông tin nhân viên từ DAO
        // Sau đó hiển thị dữ liệu lên các JTextField
    }

    private void huySua() {
        if (quayLai != null) {
            quayLai.run();
        }
    }

    private void suaThanhCong() {
        if (quayLai != null) {
            quayLai.run();
        }
    }
    public void moframe() {
		JFrame frame = new JFrame("");

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setContentPane(this);

		frame.setSize(1000, 750);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		new ThemNhanVienPanel().moframe();
	}
}
