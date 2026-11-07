package UI.QuanLy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ThemNhanVienPanel extends JPanel implements ActionListener{
	private Runnable quayLai;
	public ThemNhanVienPanel() {
		this(null);
	}
	public ThemNhanVienPanel(Runnable callback) {
		this.quayLai = callback;
		//UI
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	// Khi nhấn nút Hủy
	private void huyThem() {
	    if (quayLai != null) {
	        quayLai.run();
	    }
	}

	// Khi thêm nhân viên thành công
	private void themThanhCong() {
	    if (quayLai != null) {
	        quayLai.run();
	    }
	}
	// tự khai báo cửa sổ
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
