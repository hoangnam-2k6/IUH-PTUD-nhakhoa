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

import DAO.DichVuDAO;
import Entity.DichVu;

public class QuanLyDichVuPanel extends JPanel implements ActionListener{
	private JLabel lbltitle;
	private JLabel lbltimnhanh;
	private JTextField txttimnhanh;
	private JButton btnthem;
	private JButton btnsua;
	private JButton btnxoa;
	private JPanel pbtn;
	private JPanel dichvu_top;
	private JPanel dichvu_top_top;
	private JPanel dichvu_center;
	private DefaultTableModel modelDV;
	private JTable tableDV;
	private JScrollPane scrollDV;
	private JButton btntim;
	//DAO
	DichVuDAO dvDAO = new DichVuDAO();

	public QuanLyDichVuPanel() {
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		//trên
		dichvu_top = new JPanel();
		dichvu_top.setLayout(new BorderLayout(10,10));
		
		dichvu_top_top = new JPanel();
		Box boxlbl = Box.createVerticalBox();
		lbltitle = new JLabel("Quản lý bệnh nhân");
		
		boxlbl.add(lbltitle);
		dichvu_top_top.add(boxlbl);

		Box dichvu_top_center = Box.createHorizontalBox();
		
		dichvu_top_center.setOpaque(true);
		dichvu_top_center.setBackground(Color.white);
		
		lbltimnhanh = new JLabel("  tìm nhanh:");
		txttimnhanh = new JTextField();
		txttimnhanh.setPreferredSize(new Dimension(300, 25));
		dichvu_top_center.add(lbltimnhanh);
		
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
		
		dichvu_top_center.add(pbtn);
		
		//giữa
		dichvu_center = new JPanel();
		dichvu_center.setLayout(new BorderLayout());
		dichvu_center.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		dichvu_center.setBackground(Color.white);
		
		String colsDV[] = {"STT", "mã dịch vụ", "Dịch Vụ", "Mô Tả", "Đơn Giá(vnđ)", "Trạng Thái"};
		modelDV = new DefaultTableModel(colsDV, 0);
		tableDV = new JTable(modelDV);
		tableDV.setRowHeight(20);
		tableDV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableDV.setFillsViewportHeight(true);
		scrollDV = new JScrollPane(tableDV);
		
		dichvu_center.add(scrollDV);
		//add
		dichvu_top.add(dichvu_top_top, BorderLayout.WEST);
		dichvu_top.add(dichvu_top_center, BorderLayout.SOUTH);
		add(dichvu_top, BorderLayout.NORTH);
		
		add(dichvu_center, BorderLayout.CENTER);
		
		//listener
		btntim.addActionListener(this);
		btnthem.addActionListener(this);
		btnsua.addActionListener(this);
		btnxoa.addActionListener(this);
		
		//load
		loaddichvu();
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
	void loaddichvu() {
	    modelDV.setRowCount(0);
	    
	    List<DichVu> ds = dvDAO.getalldichvu();

	    int stt = 1;

	    for (DichVu dv : ds) {
	        modelDV.addRow(new Object[]{
	            stt++,
	            dv.getMaDV(),
	            dv.getTenDV(),
	            dv.getMota(),
	            dv.getDongia(),
	            dv.getTrangthai()
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
		new QuanLyDichVuPanel().moframe();
	}
}
