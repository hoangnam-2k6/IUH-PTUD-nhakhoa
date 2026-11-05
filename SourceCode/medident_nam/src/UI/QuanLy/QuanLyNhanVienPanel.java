package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

public class QuanLyNhanVienPanel extends JPanel{
	private JLabel lbltest;
	private JPanel nhanvien_top;
	private JPanel nhanvien_center;
	private JTabbedPane table_nhanvien;
	private JPanel nhanvien_top_top;
	private JLabel lbltitle;
	private JLabel lbltimnhanh;
	private JTextField txttimnhanh;
	private JButton btnthem;
	private JButton btnsua;
	private JButton btnxoa;
	private JPanel pbtn;

	public QuanLyNhanVienPanel() {
		setLayout(new BorderLayout());
		//trên
		nhanvien_top = new JPanel();
		nhanvien_top.setLayout(new BorderLayout(10,10));
		
		nhanvien_top_top = new JPanel();
		Box boxlbl = Box.createVerticalBox();
		lbltitle = new JLabel("Quản lý nhân viên");
		
		boxlbl.add(lbltitle);
		nhanvien_top_top.add(boxlbl);

		Box nhanvien_top_center = Box.createVerticalBox();
		
		nhanvien_top_center.setOpaque(true);
		nhanvien_top_center.setBackground(Color.white);
		
		lbltimnhanh = new JLabel("tìm nhanh:");
		txttimnhanh = new JTextField();
		nhanvien_top_center.add(lbltimnhanh);
		nhanvien_top_center.add(txttimnhanh);
		
		pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		pbtn.setBackground(Color.white);
		
		btnthem = new JButton("+ thêm nhân viên");
		btnthem.setBackground(Color.white);
		btnthem.setForeground(new Color(52, 152, 219));
		
		btnsua = new JButton("chỉnh sửa");
		btnsua.setBackground(Color.white);
		btnsua.setForeground(new Color(52, 152, 219));
		
		btnxoa = new JButton("xóa");
		btnxoa.setBackground(Color.white);
		btnxoa.setForeground(new Color(52, 152, 219));
		
		pbtn.add(btnthem);
		pbtn.add(btnsua);
		pbtn.add(btnxoa);
		
		nhanvien_top_center.add(pbtn);
		
		//giữa
		nhanvien_center = new JPanel();
		table_nhanvien = new JTabbedPane();
		//add
		nhanvien_top.add(nhanvien_top_top, BorderLayout.WEST);
		nhanvien_top.add(nhanvien_top_center, BorderLayout.SOUTH);
		add(nhanvien_top, BorderLayout.NORTH);
		
		add(nhanvien_center, BorderLayout.CENTER);
	}
}
