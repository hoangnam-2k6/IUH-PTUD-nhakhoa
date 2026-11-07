package UI.QuanLy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

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

import DAO.NhanVienDAO;
import Entity.NhanVien;

@SuppressWarnings("serial")
public class QuanLyNhanVienPanel extends JPanel implements ActionListener {

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
    private List<NhanVien> dsNhanVien;
    
    private Runnable moManHinhThemNhanVien;
    private Consumer<String> moManHinhSuaNhanVien;

    // DAO
    private final NhanVienDAO nvDAO = new NhanVienDAO();
    private JComboBox<String> loccombo;

    public QuanLyNhanVienPanel() {
        this(null, null);
    }

    public QuanLyNhanVienPanel(Runnable callbackThem, Consumer<String> callbackSua) {
        this.moManHinhThemNhanVien = callbackThem;
        this.moManHinhSuaNhanVien = callbackSua;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] colsNV = {
            "STT", "mã nhân viên", "Họ tên", "ngày sinh", "giới tính",
            "Số Điện Thoại", "Email", "địa chỉ", "Chức Vụ", "Trạng Thái",
            "mã tài khoản"
        };

        // Trên
        nhanvien_top = new JPanel();
        nhanvien_top.setLayout(new BorderLayout(10, 10));

        nhanvien_top_top = new JPanel();
        Box boxlbl = Box.createVerticalBox();
        lbltitle = new JLabel("Quản lý nhân viên");
        boxlbl.add(lbltitle);
        nhanvien_top_top.add(boxlbl);

        Box nhanvien_top_center = Box.createHorizontalBox();
        nhanvien_top_center.setOpaque(true);
        nhanvien_top_center.setBackground(Color.WHITE);

        lbltimnhanh = new JLabel("  tìm nhanh:");
        txttimnhanh = new JTextField();
        txttimnhanh.setPreferredSize(new Dimension(300, 25));
        nhanvien_top_center.add(lbltimnhanh);

        pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pbtn.setBackground(Color.WHITE);

        loccombo = new JComboBox<>();
        loccombo.setModel(new DefaultComboBoxModel<>(colsNV));

        btntim = new JButton("tìm");
        btntim.setBackground(Color.WHITE);
        btntim.setForeground(new Color(52, 152, 219));

        btnthem = new JButton("+ thêm nhân viên");
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
        nhanvien_top_center.add(pbtn);

        // Giữa
        nhanvien_center = new JPanel();
        nhanvien_center.setLayout(new BorderLayout());
        nhanvien_center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        nhanvien_center.setBackground(Color.WHITE);

        modelNV = new DefaultTableModel(colsNV, 0);
        tableNV = new JTable(modelNV);
        tableNV.setRowHeight(20);
        tableNV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableNV.setFillsViewportHeight(true);

        scrollNV = new JScrollPane(tableNV);
        nhanvien_center.add(scrollNV, BorderLayout.CENTER);

        // Add
        nhanvien_top.add(nhanvien_top_top, BorderLayout.WEST);
        nhanvien_top.add(nhanvien_top_center, BorderLayout.SOUTH);
        add(nhanvien_top, BorderLayout.NORTH);
        add(nhanvien_center, BorderLayout.CENTER);

        // Listener
        txttimnhanh.addActionListener(e -> timNhanVien());
        btntim.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);

        // Load
        loadnhanvien();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == btntim) {
           timNhanVien();
        } else if (o == btnthem) {
            if (moManHinhThemNhanVien != null) {
                moManHinhThemNhanVien.run();
            }
        } else if (o == btnsua) {
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
            Object value = modelNV.getValueAt(modelRow, 1);
            if (value == null) {
                JOptionPane.showMessageDialog(this, "Không lấy được mã nhân viên đã chọn!");
                return;
            }

            String maNV = value.toString();

            if (moManHinhSuaNhanVien != null) {
                moManHinhSuaNhanVien.accept(maNV);
            }
        } else if (o == btnxoa) {
        	xoaNhanVien();
        }
    }

    public void loadnhanvien() {

        // Xóa dữ liệu cũ trên bảng
        modelNV.setRowCount(0);

        // Lấy toàn bộ danh sách nhân viên
        dsNhanVien = nvDAO.getallnhanvien();

        if (dsNhanVien == null) {
            return;
        }

        int stt = 1;

        for (NhanVien nv : dsNhanVien) {

            modelNV.addRow(new Object[] {
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
    private void xoaNhanVien() {

        // Kiểm tra dòng được chọn
        int row = tableNV.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn nhân viên cần xóa!"
            );
            return;
        }

        // Lấy vị trí dòng trong Model
        int modelRow = tableNV.convertRowIndexToModel(row);

        // Lấy mã nhân viên ở cột thứ 2
        String maNV = modelNV.getValueAt(modelRow, 1).toString();

        // Xác nhận trước khi xóa
        int chon = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa nhân viên " + maNV + " không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (chon == JOptionPane.YES_OPTION) {

            // Gọi DAO thực hiện xóa
            boolean ketQua = nvDAO.delete(maNV);

            if (ketQua) {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa nhân viên thành công!"
                );

                // Cập nhật lại bảng
                loadnhanvien();

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa nhân viên thất bại!"
                );
            }
        }
    }
    private void timNhanVien() {

    String tuKhoa = txttimnhanh.getText().trim();

    int cot = loccombo.getSelectedIndex();

    if (cot < 0) {
        JOptionPane.showMessageDialog(
            this,
            "Vui lòng chọn cột cần tìm!"
        );
        return;
    }

    if (dsNhanVien == null || dsNhanVien.isEmpty()) {
        JOptionPane.showMessageDialog(
            this,
            "Danh sách nhân viên không có dữ liệu!"
        );
        return;
    }

    // Xóa dữ liệu cũ trên bảng
    modelNV.setRowCount(0);

    int sttGoc = 1;
    int sttHienThi = 1;

    // Duyệt danh sách nhân viên gốc
    for (NhanVien nv : dsNhanVien) {

        Object[] dong = {
            sttGoc,
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
        };

        // Lấy giá trị tại cột đang chọn
        String giaTri = String.valueOf(dong[cot]);

        // Nếu từ khóa rỗng hoặc có chứa từ khóa tìm kiếm
        if (tuKhoa.isEmpty()
                || giaTri.toLowerCase().contains(tuKhoa.toLowerCase())) {

            // Đánh lại số thứ tự hiển thị
            dong[0] = sttHienThi++;

            modelNV.addRow(dong);
        }

        // Tăng STT gốc
        sttGoc++;
    }

    // Kiểm tra kết quả
    if (modelNV.getRowCount() == 0) {

        JOptionPane.showMessageDialog(
            this,
            "Không tìm thấy nhân viên phù hợp!"
        );
    }
}
    // Tự khai báo cửa sổ để chạy riêng panel
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
