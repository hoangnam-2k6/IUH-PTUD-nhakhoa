package UI.QuanTriVien;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class QuanLy_GUI extends JFrame implements ActionListener{
	private JButton trangchu;
	private JButton nhanvien;
	private JButton lichhen;
	private JButton benhnhan;
	private JButton dichvu;
	private JButton dangxuat;
	private JPanel sidebar;
	private JPanel sidebar_bottom;
	private JButton thongke;
	private JPanel sidebar_top;
	private JLabel tenapp;
	private JLabel lblchucnang;
	private JButton hoadon;
	public QuanLy_GUI() {
		setTitle("^-^");
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout(0, 0));
		
		//***sidebar
		sidebar = new JPanel();
		sidebar.setLayout(new BorderLayout(0, 0));
		sidebar.setPreferredSize(new Dimension(250, 0));
		//**sidebar.trên
		sidebar_top = new JPanel();
		tenapp = new JLabel("MEDIDENT");
		//**sidebar.giữa
		//chứa các nút chức năng
		Box chucnang = Box.createVerticalBox();
		chucnang.setMaximumSize(new Dimension(300, 50));
		//text
		lblchucnang = new JLabel("chức năng hệ thống");
		//nút chức năng
		trangchu = new JButton("Trang Chủ");
		trangchu.setForeground(Color.white);
		trangchu.setBackground(new Color(52, 152, 219));
		trangchu.setPreferredSize(new Dimension(300, 50));
		trangchu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		trangchu.setFocusPainted(false);
		
		nhanvien = new JButton("Nhân Viên");
		nhanvien.setForeground(Color.white);
		nhanvien.setBackground(new Color(52, 152, 219));
		nhanvien.setPreferredSize(new Dimension(300, 50));
		nhanvien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		nhanvien.setFocusPainted(false);

		lichhen = new JButton("lịch hẹn");
		lichhen.setForeground(Color.white);
		lichhen.setBackground(new Color(52, 152, 219));
		lichhen.setPreferredSize(new Dimension(300, 50));
		lichhen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		lichhen.setFocusPainted(false);
		
		benhnhan = new JButton("bệnh nhân");
		benhnhan.setForeground(Color.white);
		benhnhan.setBackground(new Color(52, 152, 219));
		benhnhan.setPreferredSize(new Dimension(300, 50));
		benhnhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		benhnhan.setFocusPainted(false);
		
		dichvu = new JButton("dịch vụ");
		dichvu.setForeground(Color.white);
		dichvu.setBackground(new Color(52, 152, 219));
		dichvu.setPreferredSize(new Dimension(300, 50));
		dichvu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		dichvu.setFocusPainted(false);

		hoadon = new JButton("Hóa Đơn");
		hoadon.setForeground(Color.white);
		hoadon.setBackground(new Color(52, 152, 219));
		hoadon.setPreferredSize(new Dimension(300, 50));
		hoadon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		hoadon.setFocusPainted(false);
		
		thongke = new JButton("Thống kê");
		thongke.setForeground(Color.white);
		thongke.setBackground(new Color(52, 152, 219));
		thongke.setPreferredSize(new Dimension(300, 50));
		thongke.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		thongke.setFocusPainted(false);
		//**sidebar.dưới
		sidebar_bottom = new JPanel();
		sidebar_bottom.setLayout(new BorderLayout());
		sidebar_bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		dangxuat = new JButton("đăng xuất");
		dangxuat.setForeground(Color.white);
		dangxuat.setBackground(new Color(52, 152, 219));
		dangxuat.setPreferredSize(new Dimension(300, 50));
		dangxuat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		dangxuat.setFocusPainted(false);
		
		//add
		//trên
		sidebar_top.add(tenapp);
		//giữa
		chucnang.add(Box.createVerticalStrut(10));
		chucnang.add(lblchucnang);
		chucnang.add(Box.createVerticalStrut(10));
		chucnang.add(trangchu);
		chucnang.add(nhanvien);
		chucnang.add(lichhen);
		chucnang.add(benhnhan);
		chucnang.add(dichvu);
		chucnang.add(hoadon);
		chucnang.add(thongke);
		//dưới
		sidebar_bottom.add(dangxuat);
		//--------------------------------------------------
		sidebar.add(sidebar_top, BorderLayout.NORTH);
		sidebar.add(chucnang, BorderLayout.CENTER);
		sidebar.add(sidebar_bottom, BorderLayout.SOUTH);
		
		//***mainbar
		JPanel mainbar;
		mainbar = new JPanel();
		mainbar.setLayout(new BorderLayout());
		//**main.trên
		JPanel mainbar_top = new JPanel();
		mainbar.add(mainbar_top, BorderLayout.NORTH);
		//**mainbar.giữa
		JPanel mainbar_center = new JPanel();
		mainbar.add(mainbar_center, BorderLayout.CENTER);
		//add 
		add(sidebar, BorderLayout.WEST);
		add(mainbar, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		new QuanLy_GUI().setVisible(true);
	}
}
