package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class QuanLyNhanVienPanel extends JPanel{
	private JLabel lbltest;
	private JPanel nhanvien_top;
	private JPanel nhanvien_center;
	private JPanel nhanvien_top_top;
	private JLabel lbltitle;
	private JLabel lbltimnhanh;
	private JTextField txttimnhanh;
	private JButton btnthem;
	private JButton btnsua;
	private JButton btnxoa;
	private JPanel pbtn;
	private DefaultTableModel modelNV;
	private JTable tableNV;
	private JScrollPane scrollNV;

	public QuanLyNhanVienPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		//trên
		nhanvien_top = new JPanel();
		nhanvien_top.setLayout(new BorderLayout(10,10));
		
		nhanvien_top_top = new JPanel();
		Box boxlbl = Box.createVerticalBox();
		lbltitle = new JLabel("Quản lý nhân viên");
		
		boxlbl.add(lbltitle);
		nhanvien_top_top.add(boxlbl);

		Box nhanvien_top_center = Box.createHorizontalBox();
		
		nhanvien_top_center.setOpaque(true);
		nhanvien_top_center.setBackground(Color.white);
		
		lbltimnhanh = new JLabel("  tìm nhanh:");
		txttimnhanh = new JTextField();
		txttimnhanh.setPreferredSize(new Dimension(300, 25));
		nhanvien_top_center.add(lbltimnhanh);
		//nhanvien_top_center.add(txttimnhanh);
		
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
		
		pbtn.add(txttimnhanh);
		pbtn.add(btnthem);
		pbtn.add(btnsua);
		pbtn.add(btnxoa);
		
		nhanvien_top_center.add(pbtn);
		
		//giữa
		nhanvien_center = new JPanel();
		nhanvien_center.setLayout(new BorderLayout());
		nhanvien_center.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		nhanvien_center.setBackground(Color.white);
		
		String colsNV[] = {"STT", "mã nhân viên", "Họ tên", "ngày sinh", "giới tính", "Số Điện Thoại", "Email", "địa chỉ", "Chức Vụ", "Trạng Thái", "mã tài khoản"};
		modelNV = new DefaultTableModel(colsNV, 0);
		tableNV = new JTable(modelNV);
		tableNV.setRowHeight(20);
		tableNV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableNV.setFillsViewportHeight(true);
		scrollNV = new JScrollPane(tableNV);
		
		nhanvien_center.add(scrollNV);
		//add
		nhanvien_top.add(nhanvien_top_top, BorderLayout.WEST);
		nhanvien_top.add(nhanvien_top_center, BorderLayout.SOUTH);
		add(nhanvien_top, BorderLayout.NORTH);
		
		add(nhanvien_center, BorderLayout.CENTER);
	}
}
