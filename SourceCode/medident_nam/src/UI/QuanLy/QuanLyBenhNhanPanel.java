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
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import DAO.BenhNhanDAO;
import Entity.BenhNhan;

@SuppressWarnings("serial")
public class QuanLyBenhNhanPanel extends JPanel implements ActionListener {

    private JLabel lbltitle;
    private JLabel lbltimnhanh;
    private JTextField txttimnhanh;
    private JButton btnthem;
    private JButton btnsua;
    private JButton btnxoa;
    private JButton btntim;
    private JPanel pbtn;
    private DefaultTableModel modelBN;
    private JTable tableBN;
    private JScrollPane scrollBN;
    private JPanel benhnhan_top;
    private JPanel benhnhan_top_top;
    private JPanel benhnhan_center;
    private List<BenhNhan> dsBenhNhan;

    private Runnable callbackThem;
    private Consumer<String> callbackSua;

    private final BenhNhanDAO bnDAO = new BenhNhanDAO();
	private JComboBox loccombo;

    public QuanLyBenhNhanPanel() {
        this(null, null);
    }

    public QuanLyBenhNhanPanel(Runnable callbackThem, Consumer<String> callbackSua) {
        this.callbackThem = callbackThem;
        this.callbackSua = callbackSua;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] colsBN = {
        		"STT", "Mã bệnh nhân", "Họ tên", "Ngày sinh", "Giới tính",
        		"Số điện thoại", "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái"
        };
        // Phần tiêu đề
        benhnhan_top = new JPanel(new BorderLayout(10, 10));
        benhnhan_top_top = new JPanel();
        Box boxlbl = Box.createVerticalBox();
        lbltitle = new JLabel("Quản lý bệnh nhân");
        boxlbl.add(lbltitle);
        benhnhan_top_top.add(boxlbl);

        // Phần tìm kiếm và nút chức năng
        Box benhnhan_top_center = Box.createHorizontalBox();
        benhnhan_top_center.setOpaque(true);
        benhnhan_top_center.setBackground(Color.WHITE);
        lbltimnhanh = new JLabel("  Tìm nhanh:");
        txttimnhanh = new JTextField();
        txttimnhanh.setPreferredSize(new Dimension(300, 25));

        pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pbtn.setBackground(Color.WHITE);
        
        loccombo = new JComboBox<>();
        loccombo.setModel(new DefaultComboBoxModel<>(colsBN));
        btntim = taoNut("Tìm");
        btnthem = taoNut("+ Thêm bệnh nhân");
        btnsua = taoNut("Chỉnh sửa");
        btnxoa = taoNut("Xóa");

        pbtn.add(txttimnhanh);
        pbtn.add(loccombo);
        pbtn.add(btntim);
        pbtn.add(btnthem);
        pbtn.add(btnsua);
        pbtn.add(btnxoa);

        benhnhan_top_center.add(lbltimnhanh);
        benhnhan_top_center.add(pbtn);
        benhnhan_top.add(benhnhan_top_top, BorderLayout.WEST);
        benhnhan_top.add(benhnhan_top_center, BorderLayout.SOUTH);
        add(benhnhan_top, BorderLayout.NORTH);

        // Bảng bệnh nhân
        benhnhan_center = new JPanel(new BorderLayout());
        benhnhan_center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        benhnhan_center.setBackground(Color.WHITE);


        modelBN = new DefaultTableModel(colsBN, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableBN = new JTable(modelBN);
        tableBN.setRowHeight(25);
        tableBN.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableBN.setFillsViewportHeight(true);
        tableBN.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollBN = new JScrollPane(tableBN);
        benhnhan_center.add(scrollBN, BorderLayout.CENTER);
        add(benhnhan_center, BorderLayout.CENTER);

        // Listener
        txttimnhanh.addActionListener(e -> timBenhNhan());
        btntim.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);

        loadbenhnhan();
    }

    private JButton taoNut(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(52, 152, 219));
        button.setFocusPainted(false);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == btntim) {
            timBenhNhan();
        } else if (o == btnthem) {
            if (callbackThem != null) {
                callbackThem.run();
            } else {
                JOptionPane.showMessageDialog(this, "Chưa thiết lập callback thêm bệnh nhân.");
            }

        } else if (o == btnsua) {
            int selectedRow = tableBN.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân cần sửa!");
                return;
            }

            int modelRow = tableBN.convertRowIndexToModel(selectedRow);
            Object value = modelBN.getValueAt(modelRow, 1);
            if (value == null) {
                JOptionPane.showMessageDialog(this, "Không lấy được mã bệnh nhân!");
                return;
            }

            String maBN = value.toString();
            if (callbackSua != null) {
                callbackSua.accept(maBN);
            } else {
                JOptionPane.showMessageDialog(this, "Chưa thiết lập callback sửa bệnh nhân.");
            }

        } else if (o == btnxoa) {
        	xoaBenhNhan();
        }
    }

    public void loadbenhnhan() {

        modelBN.setRowCount(0);

        dsBenhNhan = bnDAO.getallbenhnhan();

        if (dsBenhNhan == null) {
            return;
        }

        int stt = 1;

        for (BenhNhan bn : dsBenhNhan) {

            modelBN.addRow(new Object[] {
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
    private void xoaBenhNhan() {

        // Kiểm tra dòng được chọn
        int row = tableBN.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn bệnh nhân cần xóa!"
            );
            return;
        }

        // Lấy vị trí dòng trong Model
        int modelRow = tableBN.convertRowIndexToModel(row);

        // Lấy mã bệnh nhân ở cột 1
        String maBN = modelBN.getValueAt(modelRow, 1).toString();

        // Xác nhận xóa
        int chon = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn xóa bệnh nhân "
                + maBN + " không?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if (chon == JOptionPane.YES_OPTION) {

            // Gọi DAO thực hiện xóa
            boolean ketQua = bnDAO.delete(maBN);

            if (ketQua) {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa bệnh nhân thành công!"
                );

                // Cập nhật lại bảng
                loadbenhnhan();

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Xóa bệnh nhân thất bại!"
                );
            }
        }
    }
    private void timBenhNhan() {

        String tuKhoa = txttimnhanh.getText().trim();

        int cot = loccombo.getSelectedIndex();

        if (cot < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn cột cần tìm!"
            );
            return;
        }

        if (dsBenhNhan == null || dsBenhNhan.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Danh sách bệnh nhân không có dữ liệu!"
            );
            return;
        }

        modelBN.setRowCount(0);

        int sttGoc = 1;
        int sttHienThi = 1;

        for (BenhNhan bn : dsBenhNhan) {

            Object[] dong = {
                sttGoc,
                bn.getMaBN(),
                bn.getHoten(),
                bn.getNgaysinh(),
                bn.getGioitinh(),
                bn.getSdt(),
                bn.getEmail(),
                bn.getDiachi(),
                bn.getTiensubenh(),
                bn.getTrangthai()
            };

            String giaTri = String.valueOf(dong[cot]);

            if (tuKhoa.isEmpty()
                    || giaTri.toLowerCase().contains(tuKhoa.toLowerCase())) {

                dong[0] = sttHienThi++;

                modelBN.addRow(dong);
            }

            sttGoc++;
        }

        if (modelBN.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Không tìm thấy bệnh nhân phù hợp!"
            );
        }
    }
    public void moframe() {
        JFrame frame = new JFrame("Quản lý bệnh nhân");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyBenhNhanPanel().moframe());
    }
}
