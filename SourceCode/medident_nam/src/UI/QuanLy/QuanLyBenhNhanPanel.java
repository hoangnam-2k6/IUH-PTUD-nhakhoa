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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class QuanLyBenhNhanPanel extends JPanel{
	private JLabel lbltitle;
	private JLabel lbltimnhanh;
	private JTextField txttimnhanh;
	private JButton btnthem;
	private JButton btnsua;
	private JButton btnxoa;
	private JPanel pbtn;
	private DefaultTableModel modelBN;
	private JTable tableBN;
	private JScrollPane scrollBN;
	private JPanel benhnhan_top;
	private JPanel benhnhan_top_top;
	private JPanel benhnhan_center;

	public QuanLyBenhNhanPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		//trên
		benhnhan_top = new JPanel();
		benhnhan_top.setLayout(new BorderLayout(10,10));
		
		benhnhan_top_top = new JPanel();
		Box boxlbl = Box.createVerticalBox();
		lbltitle = new JLabel("Quản lý bệnh nhân");
		
		boxlbl.add(lbltitle);
		benhnhan_top_top.add(boxlbl);

		Box benhnhan_top_center = Box.createHorizontalBox();
		
		benhnhan_top_center.setOpaque(true);
		benhnhan_top_center.setBackground(Color.white);
		
		lbltimnhanh = new JLabel("  tìm nhanh:");
		txttimnhanh = new JTextField();
		txttimnhanh.setPreferredSize(new Dimension(300, 25));
		benhnhan_top_center.add(lbltimnhanh);
		
		pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		pbtn.setBackground(Color.white);
		
		btnthem = new JButton("+ thêm bệnh nhân");
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
		
		benhnhan_top_center.add(pbtn);
		
		//giữa
		benhnhan_center = new JPanel();
		benhnhan_center.setLayout(new BorderLayout());
		benhnhan_center.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		benhnhan_center.setBackground(Color.white);
		
		String colsBN[] = {"STT", "mã bệnh nhân", "Họ tên", "ngày sinh", "giới tính", "Số Điện Thoại", "email", "địa chỉ", "tiền sử bệnh", "trạng Thái"};
		modelBN = new DefaultTableModel(colsBN, 0);
		tableBN = new JTable(modelBN);
		tableBN.setRowHeight(20);
		tableBN.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableBN.setFillsViewportHeight(true);
		scrollBN = new JScrollPane(tableBN);
		
		benhnhan_center.add(scrollBN);
		//add
		benhnhan_top.add(benhnhan_top_top, BorderLayout.WEST);
		benhnhan_top.add(benhnhan_top_center, BorderLayout.SOUTH);
		add(benhnhan_top, BorderLayout.NORTH);
		
		add(benhnhan_center, BorderLayout.CENTER);
	}
}
