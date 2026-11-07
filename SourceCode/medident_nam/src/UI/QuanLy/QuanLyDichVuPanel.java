package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import DAO.DichVuDAO;
import Entity.DichVu;

@SuppressWarnings("serial")
public class QuanLyDichVuPanel extends JPanel implements ActionListener {

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
    private List<DichVu> dsDichVu;

    // Callback mở form thêm và sửa dịch vụ
    private Runnable moManHinhThemDichVu;
    private Consumer<String> moManHinhSuaDichVu;

    // DAO
    private final DichVuDAO dvDAO = new DichVuDAO();

    // Hỗ trợ sắp xếp và tìm kiếm trên bảng
    private TableRowSorter<DefaultTableModel> sorterDV;
	private JComboBox loccombo;

    public QuanLyDichVuPanel() {
        this(null, null);
    }

    public QuanLyDichVuPanel(
            Runnable callbackThem,
            Consumer<String> callbackSua) {

        this.moManHinhThemDichVu = callbackThem;
        this.moManHinhSuaDichVu = callbackSua;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] colsDV = {
        		"STT", "mã dịch vụ", "Dịch Vụ",
        		"Mô Tả", "Đơn Giá(vnđ)", "Trạng Thái"
        };
        // Phần trên
        dichvu_top = new JPanel();
        dichvu_top.setLayout(new BorderLayout(10, 10));

        dichvu_top_top = new JPanel();
        Box boxlbl = Box.createVerticalBox();

        lbltitle = new JLabel("Quản lý dịch vụ");
        boxlbl.add(lbltitle);
        dichvu_top_top.add(boxlbl);

        Box dichvu_top_center = Box.createHorizontalBox();
        dichvu_top_center.setOpaque(true);
        dichvu_top_center.setBackground(Color.WHITE);

        lbltimnhanh = new JLabel("  tìm nhanh:");
        txttimnhanh = new JTextField();
        txttimnhanh.setPreferredSize(new Dimension(300, 25));
        dichvu_top_center.add(lbltimnhanh);

        pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pbtn.setBackground(Color.WHITE);
        
        loccombo = new JComboBox<>();
        loccombo.setModel(new DefaultComboBoxModel<>(colsDV));
        
        btntim = new JButton("tìm");
        btntim.setBackground(Color.WHITE);
        btntim.setForeground(new Color(52, 152, 219));

        btnthem = new JButton("+ thêm dịch vụ");
        btnthem.setBackground(Color.WHITE);
        btnthem.setForeground(new Color(52, 152, 219));

        btnsua = new JButton("chỉnh sửa");
        btnsua.setBackground(Color.WHITE);
        btnsua.setForeground(new Color(52, 152, 219));

        btnxoa = new JButton("xóa");
        btnxoa.setBackground(Color.WHITE);
        btnxoa.setForeground(new Color(52, 152, 219));

        pbtn.add(txttimnhanh);
        pbtn.add(loccombo);
        pbtn.add(btntim);
        pbtn.add(btnthem);
        pbtn.add(btnsua);
        pbtn.add(btnxoa);

        dichvu_top_center.add(pbtn);

        // Phần giữa
        dichvu_center = new JPanel();
        dichvu_center.setLayout(new BorderLayout());
        dichvu_center.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
        dichvu_center.setBackground(Color.WHITE);


        modelDV = new DefaultTableModel(colsDV, 0);
        tableDV = new JTable(modelDV);
        tableDV.setRowHeight(20);
        tableDV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableDV.setFillsViewportHeight(true);

        sorterDV = new TableRowSorter<>(modelDV);
        tableDV.setRowSorter(sorterDV);

        scrollDV = new JScrollPane(tableDV);
        dichvu_center.add(scrollDV, BorderLayout.CENTER);

        // Ghép giao diện
        dichvu_top.add(dichvu_top_top, BorderLayout.WEST);
        dichvu_top.add(dichvu_top_center, BorderLayout.SOUTH);

        add(dichvu_top, BorderLayout.NORTH);
        add(dichvu_center, BorderLayout.CENTER);

        // Listener
        txttimnhanh.addActionListener(e -> timDichVu());
        btntim.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);

        // Tải dữ liệu
        loaddichvu();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == btntim) {
            timDichVu();
        } else if (o == btnthem) {
            moFormThemDichVu();
        } else if (o == btnsua) {
            moFormSuaDichVu();
        } else if (o == btnxoa) {
            xoaDichVu();
        }
    }

    // Tải danh sách dịch vụ từ database
    public void loaddichvu() {

        modelDV.setRowCount(0);

        dsDichVu = dvDAO.getalldichvu();

        if (dsDichVu == null) {
            return;
        }

        int stt = 1;

        for (DichVu dv : dsDichVu) {

            modelDV.addRow(new Object[] {
                stt++,
                dv.getMaDV(),
                dv.getTenDV(),
                dv.getMota(),
                dv.getDongia(),
                dv.getTrangthai()
            });
        }
    }

    // Mở form thêm dịch vụ
    private void moFormThemDichVu() {
        if (moManHinhThemDichVu != null) {
            moManHinhThemDichVu.run();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Chức năng thêm dịch vụ chưa được kết nối!"
            );
        }
    }

    // Mở form sửa dịch vụ
    private void moFormSuaDichVu() {
        int row = tableDV.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn dịch vụ cần sửa!"
            );
            return;
        }

        int modelRow = tableDV.convertRowIndexToModel(row);
        Object value = modelDV.getValueAt(modelRow, 1);

        if (value == null) {
            JOptionPane.showMessageDialog(
                this,
                "Không lấy được mã dịch vụ cần sửa!"
            );
            return;
        }

        String maDV = value.toString();

        if (moManHinhSuaDichVu != null) {
            moManHinhSuaDichVu.accept(maDV);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Chức năng sửa dịch vụ chưa được kết nối!"
            );
        }
    }

    // Xóa dịch vụ được chọn
    private void xoaDichVu() {
        int row = tableDV.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn dịch vụ cần xóa!"
            );
            return;
        }

        int modelRow = tableDV.convertRowIndexToModel(row);
        Object value = modelDV.getValueAt(modelRow, 1);

        if (value == null) {
            JOptionPane.showMessageDialog(
                this,
                "Không lấy được mã dịch vụ cần xóa!"
            );
            return;
        }

        String maDV = value.toString();

        int chon = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa dịch vụ " + maDV + " không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (chon == JOptionPane.YES_OPTION) {
            boolean ketQua = dvDAO.delete(maDV);

            if (ketQua) {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa dịch vụ thành công!"
                );
                loaddichvu();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa dịch vụ thất bại. Dịch vụ có thể đang được "
                        + "sử dụng trong dữ liệu khác.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void timDichVu() {

        String tuKhoa = txttimnhanh.getText().trim();

        int cot = loccombo.getSelectedIndex();

        if (cot < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn cột cần tìm!"
            );
            return;
        }

        if (dsDichVu == null || dsDichVu.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Danh sách dịch vụ không có dữ liệu!"
            );
            return;
        }

        modelDV.setRowCount(0);

        int sttGoc = 1;
        int sttHienThi = 1;

        for (DichVu dv : dsDichVu) {

            Object[] dong = {
                sttGoc,
                dv.getMaDV(),
                dv.getTenDV(),
                dv.getMota(),
                dv.getDongia(),
                dv.getTrangthai()
            };

            String giaTri = String.valueOf(dong[cot]);

            if (tuKhoa.isEmpty()
                    || giaTri.toLowerCase().contains(tuKhoa.toLowerCase())) {

                dong[0] = sttHienThi++;

                modelDV.addRow(dong);
            }

            sttGoc++;
        }

        if (modelDV.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Không tìm thấy dịch vụ phù hợp!"
            );
        }
    }
    // Chạy riêng panel để kiểm tra giao diện
    public void moframe() {
        JFrame frame = new JFrame("Quản lý dịch vụ");
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
