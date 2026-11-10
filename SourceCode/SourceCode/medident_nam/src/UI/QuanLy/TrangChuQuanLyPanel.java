package UI.QuanLy;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class TrangChuQuanLyPanel extends JPanel{
	private JLabel lbltest;

	public TrangChuQuanLyPanel() {
		lbltest = new JLabel("có thể sẽ bổ sung 4 card thể hiện thông tin tổng đơn giản/ cấp thêm 1 bảng được join từ nhiều bảng để thể hiện cac thông tin chung của nhiều bảng");
		add(lbltest);
	}
}
