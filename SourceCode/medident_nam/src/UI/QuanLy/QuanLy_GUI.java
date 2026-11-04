package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
	private JButton hotro;
	private JPanel mainbar_top_left;
	private JPanel mainbar_top_right;
	private JLabel tenapp2;
	private JLabel hoten;
	private JLabel vaitro;
	private JPanel mainbar_center;
	private CardLayout CardLayout;
	private final Color maumacdinh = Color.white;
	private final Color maukhichon = new Color(52, 152, 219);
	private JButton nutDangChon;
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
		sidebar.setBackground(Color.white);
		//**sidebar.trên
		sidebar_top = new JPanel();
		sidebar_top.setBackground(Color.white);
		tenapp = new JLabel("MEDIDENT");
		//**sidebar.giữa
		//chứa các nút chức năng
		Box chucnang = Box.createVerticalBox();
		chucnang.setMaximumSize(new Dimension(300, 50));
		//text
		lblchucnang = new JLabel("chức năng hệ thống");
		lblchucnang.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
		//nút chức năng
		trangchu = new JButton("Trang Chủ");
		trangchu.setForeground(Color.white);
		trangchu.setBackground(new Color(52, 152, 219));
		trangchu.setPreferredSize(new Dimension(300, 50));
		trangchu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		trangchu.setFocusPainted(false);
		trangchu.setBorderPainted(false);
		
		nhanvien = new JButton("Nhân Viên");
		nhanvien.setForeground(new Color(52, 152, 219));
		nhanvien.setBackground(Color.white);
		nhanvien.setPreferredSize(new Dimension(300, 50));
		nhanvien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		nhanvien.setFocusPainted(false);
		nhanvien.setBorderPainted(false);

		lichhen = new JButton("lịch hẹn");
		lichhen.setForeground(new Color(52, 152, 219));
		lichhen.setBackground(Color.white);
		lichhen.setPreferredSize(new Dimension(300, 50));
		lichhen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		lichhen.setFocusPainted(false);
		lichhen.setBorderPainted(false);
		
		benhnhan = new JButton("bệnh nhân");
		benhnhan.setForeground(new Color(52, 152, 219));
		benhnhan.setBackground(Color.white);
		benhnhan.setPreferredSize(new Dimension(300, 50));
		benhnhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		benhnhan.setFocusPainted(false);
		benhnhan.setBorderPainted(false);
		
		dichvu = new JButton("dịch vụ");
		dichvu.setForeground(new Color(52, 152, 219));
		dichvu.setBackground(Color.white);
		dichvu.setPreferredSize(new Dimension(300, 50));
		dichvu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		dichvu.setFocusPainted(false);
		dichvu.setBorderPainted(false);

		hoadon = new JButton("Hóa Đơn");
		hoadon.setForeground(new Color(52, 152, 219));
		hoadon.setBackground(Color.white);
		hoadon.setPreferredSize(new Dimension(300, 50));
		hoadon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		hoadon.setFocusPainted(false);
		hoadon.setBorderPainted(false);
		
		thongke = new JButton("Thống kê");
		thongke.setForeground(new Color(52, 152, 219));
		thongke.setBackground(Color.white);
		thongke.setPreferredSize(new Dimension(300, 50));
		thongke.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		thongke.setFocusPainted(false);
		thongke.setBorderPainted(false);
		//**sidebar.dưới
		sidebar_bottom = new JPanel();
		sidebar_bottom.setLayout(new BorderLayout());
		sidebar_bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		hotro = new JButton("hỗ trợ");
		hotro.setForeground(new Color(52, 152, 219));
		hotro.setBackground(Color.white);
		hotro.setPreferredSize(new Dimension(300, 50));
		hotro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		hotro.setFocusPainted(false);
		hotro.setBorderPainted(false);
		
		dangxuat = new JButton("đăng xuất");
		dangxuat.setForeground(new Color(52, 152, 219));
		dangxuat.setBackground(Color.white);
		dangxuat.setPreferredSize(new Dimension(300, 50));
		dangxuat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		dangxuat.setFocusPainted(false);
		dangxuat.setBorderPainted(false);
		
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
		sidebar_bottom.add(hotro, BorderLayout.NORTH);
		sidebar_bottom.add(dangxuat, BorderLayout.SOUTH);
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
		mainbar_top.setLayout(new BorderLayout(0,0));
		mainbar_top.setBackground(Color.white);
		
		mainbar_top_left = new JPanel();
		mainbar_top_left.setBackground(Color.white);
		tenapp2 = new JLabel("MEDIDENT  /  Quản Lý Nha Khoa");
		mainbar_top_left.add(tenapp2);
		mainbar_top.add(mainbar_top_left, BorderLayout.WEST);
		
		mainbar_top_right = new JPanel();
		mainbar_top_right.setLayout(new BorderLayout());
		mainbar_top_right.setBackground(Color.white);
		hoten = new JLabel("Trịnh Hoàng Nam");
		vaitro = new JLabel("quản lý Phòng Khám");
		mainbar_top_right.add(hoten, BorderLayout.NORTH);
		mainbar_top_right.add(vaitro, BorderLayout.SOUTH);
		mainbar_top.add(mainbar_top_right, BorderLayout.EAST);
		
		mainbar.add(mainbar_top, BorderLayout.NORTH);
		
		//**mainbar.giữa
		CardLayout = new CardLayout();
		mainbar_center = new JPanel(CardLayout);
		mainbar_center.add(new TrangChuQuanLyPanel(), "trangchu");
		mainbar_center.add(new QuanLyNhanVienPanel(), "nhanvien");
		mainbar_center.add(new QuanLyLichHenPanel(), "lichhen");
		mainbar_center.add(new QuanLyBenhNhanPanel(), "benhnhan");
		mainbar_center.add(new QuanLyDichVuPanel(), "dichvu");
		mainbar_center.add(new QuanLyHoaDonPanel(), "hoadon");
		mainbar_center.add(new BaoCaoThongKePanel(), "thongke");
		
		mainbar.add(mainbar_center, BorderLayout.CENTER);
		
		//add 
		add(sidebar, BorderLayout.WEST);
		add(mainbar, BorderLayout.CENTER);
		
		//listener
		trangchu.addActionListener(this);
		nhanvien.addActionListener(this);
		lichhen.addActionListener(this);
		benhnhan.addActionListener(this);
		dichvu.addActionListener(this);
		hoadon.addActionListener(this);
		thongke.addActionListener(this);
		
		hotro.addActionListener(this);
		
		doiMauNut(trangchu);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object o = e.getSource();
		if(o.equals(trangchu)) {
			CardLayout.show(mainbar_center, "trangchu");
			doiMauNut(trangchu);
		}
		else if(o.equals(nhanvien)) {
			CardLayout.show(mainbar_center, "nhanvien");
			doiMauNut(nhanvien);
		}
		else if(o.equals(lichhen)) {
			CardLayout.show(mainbar_center, "lichhen");
			doiMauNut(lichhen);
		}
		else if(o.equals(benhnhan)) {
			CardLayout.show(mainbar_center, "benhnhan");
			doiMauNut(benhnhan);
		}
		else if(o.equals(dichvu)) {
			CardLayout.show(mainbar_center, "dichvu");
			doiMauNut(dichvu);
		}
		else if(o.equals(hoadon)) {
			CardLayout.show(mainbar_center, "hoadon");
			doiMauNut(hoadon);
		}
		else if(o.equals(thongke)) {
			CardLayout.show(mainbar_center, "thongke");
			doiMauNut(thongke);
		}
		else if(o.equals(hotro)) {
			CardLayout.show(mainbar_center, "hotro");
			doiMauNut(hotro);
		}
	}
	private void doiMauNut(JButton nutMoi) {

	    // Trả nút cũ về màu mặc định
	    if (nutDangChon != null) {
	        nutDangChon.setBackground(maumacdinh);
	        nutDangChon.setForeground(maukhichon);
	    }

	    // Đổi màu nút mới được chọn
	    nutMoi.setBackground(maukhichon);
	    nutMoi.setForeground(maumacdinh);

	    // Cập nhật nút đang được chọn
	    nutDangChon = nutMoi;
	}
	public static void main(String[] args) {
		new QuanLy_GUI().setVisible(true);
	}
}
