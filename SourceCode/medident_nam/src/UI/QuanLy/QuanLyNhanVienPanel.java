package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.util.function.Consumer;

import DAO.NhanVienDAO;
import Entity.NhanVien;

public class QuanLyNhanVienPanel extends JPanel implements ActionListener, KeyListener{
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
	private JButton btntim;
	private Runnable moManHinhThemNhanVien;
	private Consumer<String> moManHinhSuaNhanVien;
	//DAO
	NhanVienDAO nvDAO = new NhanVienDAO();
	private JComboBox<String> loccombo;
	
	public QuanLyNhanVienPanel() {
		this(null, null);
	}
	
	public QuanLyNhanVienPanel(Runnable callbackThem, Consumer<String> callbackSua) {
		
		this.moManHinhThemNhanVien = callbackThem;
		this.moManHinhSuaNhanVien = callbackSua;
		
		setLayout(new BorderLayout(10, 10));
		setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		String colsNV[] = {"STT", "mã nhân viên", "Họ tên", "ngày sinh", "giới tính", "Số Điện Thoại", "Email", "địa chỉ", "Chức Vụ", "Trạng Thái", "mã tài khoản"};
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
		
		pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		pbtn.setBackground(Color.white);
		
		loccombo = new JComboBox();
		loccombo.setModel(new DefaultComboBoxModel<>(colsNV));
		
		btntim = new JButton("tìm");
		btntim.setBackground(Color.white);
		btntim.setForeground(new Color(52, 152, 219));
		
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
		pbtn.add(loccombo);
		pbtn.add(btntim);
		pbtn.add(btnthem);
		pbtn.add(btnsua);
		pbtn.add(btnxoa);
		
		nhanvien_top_center.add(pbtn);
		
		//giữa
		nhanvien_center = new JPanel();
		nhanvien_center.setLayout(new BorderLayout());
		nhanvien_center.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		nhanvien_center.setBackground(Color.white);
		
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
		
		//listener
		btntim.addActionListener(this);
		btnthem.addActionListener(this);
		btnsua.addActionListener(this);
		btnxoa.addActionListener(this);
		
		//load
		loadnhanvien();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object o = e.getSource();
		if (o.equals(btntim)) {
			
		}
		else if (o.equals(btnthem)) {
			if(moManHinhThemNhanVien != null) {
				moManHinhThemNhanVien.run();
			}
		}
		else if (o.equals(btnsua)) {
			int row = tableNV.getSelectedRow();

	        if (row == -1) {
	            JOptionPane.showMessageDialog(
	                this,
	                "Vui lòng chọn nhân viên cần sửa!"
	            );
	            return;
	        }

	        // Chuyển vị trí dòng hiển thị về vị trí trong Model
	        int modelRow = tableNV.convertRowIndexToModel(row);

	        // Cột 1 là mã nhân viên
	        String maNV = modelNV.getValueAt(modelRow, 1).toString();

	        if (moManHinhSuaNhanVien != null) {
	            moManHinhSuaNhanVien.accept(maNV);
	        }
		}
		else if (o.equals(btnxoa)) {
			
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	void loadnhanvien() {
		modelNV.setRowCount(0);
		
		List<NhanVien> ds = nvDAO.getallnhanvien();
		
		int stt = 1;

	    for (NhanVien nv : ds) {
	        modelNV.addRow(new Object[]{
	            stt++,
	            nv.getMaNV(),
	            nv.getHoten(),
	            nv.getNgaysinh(),
	            nv.getGioitinh(),
	            nv.getSdt(),
	            nv.getEmail(),
	            nv.getDiachi(),
	            nv.getChucvu(),
	            nv.getTrangthai(),
	            nv.getMaTK()
	        });
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
		new QuanLyNhanVienPanel().moframe();
	}

}
