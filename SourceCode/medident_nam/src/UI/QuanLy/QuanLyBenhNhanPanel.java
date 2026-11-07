package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import DAO.BenhNhanDAO;
import Entity.BenhNhan;

public class QuanLyBenhNhanPanel extends JPanel implements ActionListener{
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
	private JButton btntim;
	//DAO
	BenhNhanDAO bnDAO = new BenhNhanDAO();

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
		
		btntim = new JButton("tìm");
		btntim.setBackground(Color.white);
		btntim.setForeground(new Color(52, 152, 219));
		
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
		pbtn.add(btntim);
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
		
		//listener
		btntim.addActionListener(this);
		btnthem.addActionListener(this);
		btnsua.addActionListener(this);
		btnxoa.addActionListener(this);
		
		//load
		loadbenhnhan();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object o = e.getSource();
		if (o.equals(btntim)) {
			
		}
		else if (o.equals(btnthem)) {
			
		}
		else if (o.equals(btnsua)) {
			
		}
		else if (o.equals(btnxoa)) {
			
		}
	}
	void loadbenhnhan() {
	    modelBN.setRowCount(0);

	    List<BenhNhan> ds = bnDAO.getallbenhnhan();

	    int stt = 1;

	    for (BenhNhan bn : ds) {
	        modelBN.addRow(new Object[]{
	            stt++,
	            bn.getMaBN(),
	            bn.getHoten(),
	            bn.getNgaysinh(),
	            bn.getGioitinh(),
	            bn.getSdt(),
	            bn.getEmail(),
	            bn.getDiachi(),
	            bn.getTiensubenh(),
	            bn.getTrangthai()
	        });
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
		new QuanLyBenhNhanPanel().moframe();
	}
}
