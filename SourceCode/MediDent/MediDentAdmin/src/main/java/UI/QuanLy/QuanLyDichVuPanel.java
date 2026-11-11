
package UI.QuanLy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import DAO.DichVuDAO;
import Entity.DichVu;

public class QuanLyDichVuPanel extends JPanel implements ActionListener {

    private JLabel lbltitle, lbltimnhanh;
    private JTextField txttimnhanh;
    private JButton btntim, btnthem, btnsua, btnxoa;
    private JPanel pbtn, dichvu_top, dichvu_center;
    private DefaultTableModel modelDV;
    private JTable tableDV;
    private JScrollPane scrollDV;
    private JComboBox<String> loccombo;
    private List<DichVu> dsDichVu;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    private Runnable moManHinhThemDichVu;
    private Consumer<String> moManHinhSuaDichVu;

    private final DichVuDAO dvDAO = new DichVuDAO();
    private TableRowSorter<DefaultTableModel> sorterDV;

    public QuanLyDichVuPanel() {
        this(null, null);
    }

    public QuanLyDichVuPanel(Runnable callbackThem, Consumer<String> callbackSua) {
        moManHinhThemDichVu = callbackThem;
        moManHinhSuaDichVu = callbackSua;

        taoGiaoDien();
        ganSuKien();
        taiDichvu();
    }

    // Tao giao dien quan ly dich vu
    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] colsDV = {
            "STT", "Mã dịch vụ", "Dịch vụ", "Mô tả",
            "Đơn giá (VNĐ)", "Trạng thái"
        };

        dichvu_top = new JPanel(new BorderLayout(10, 10));

        lbltitle = new JLabel("Quản lý dịch vụ");
        lbltitle.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel phanTieuDe = new JPanel(new FlowLayout(FlowLayout.LEFT));
        phanTieuDe.add(lbltitle);
        dichvu_top.add(phanTieuDe, BorderLayout.NORTH);

        lbltimnhanh = new JLabel("Tìm nhanh:");
        txttimnhanh = new JTextField(18);
        txttimnhanh.setPreferredSize(new Dimension(220, 30));

        loccombo = new JComboBox<>(colsDV);

        btntim = taoNut("Tìm");
        btnthem = taoNut("+ Thêm dịch vụ");
        btnsua = taoNut("Chỉnh sửa");
        btnxoa = taoNut("Xóa");

        pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pbtn.setBackground(Color.WHITE);
        pbtn.add(lbltimnhanh);
        pbtn.add(txttimnhanh);
        pbtn.add(loccombo);
        pbtn.add(btntim);
        pbtn.add(btnthem);
        pbtn.add(btnsua);
        pbtn.add(btnxoa);

        dichvu_top.add(pbtn, BorderLayout.CENTER);
        add(dichvu_top, BorderLayout.NORTH);

        // Tao bang danh sach dich vu
        dichvu_center = new JPanel(new BorderLayout());
        dichvu_center.setBackground(Color.WHITE);
        dichvu_center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        modelDV = new DefaultTableModel(colsDV, 0) {
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };

        tableDV = new JTable(modelDV);
        tableDV.setRowHeight(25);
        tableDV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableDV.setFillsViewportHeight(true);
        tableDV.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorterDV = new TableRowSorter<>(modelDV);
        tableDV.setRowSorter(sorterDV);

        scrollDV = new JScrollPane(tableDV);
        dichvu_center.add(scrollDV, BorderLayout.CENTER);
        add(dichvu_center, BorderLayout.CENTER);
    }

    // Tao nut chuc nang
    private JButton taoNut(String tenNut) {
    	JButton b = new JButton(tenNut) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(Color.white);
        b.setForeground(TEXT);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Gan su kien cho cac nut
    private void ganSuKien() {
        txttimnhanh.addActionListener(e -> timDichVu());
        btntim.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);
    }

    // Xu ly cac nut chuc nang
    @Override
    public void actionPerformed(ActionEvent e) {
        Object nut = e.getSource();

        if (nut == btntim) {
            timDichVu();
        } else if (nut == btnthem) {
            moFormThemDichVu();
        } else if (nut == btnsua) {
            moFormSuaDichVu();
        } else if (nut == btnxoa) {
            xoaDichVu();
        }
    }

    // Lay danh sach dich vu tu SQL Server
    public void taiDichvu() {
        try {
            dsDichVu = dvDAO.layTatCaDichvu();
            modelDV.setRowCount(0);

            if (dsDichVu == null) return;

            int stt = 1;

            for (DichVu dv : dsDichVu) {
                modelDV.addRow(new Object[]{
                    stt++, dv.layMaDV(), dv.layTenDV(),
                    dv.layMota(), dv.layDongia(), dv.layTrangthai()
                });
            }

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Mo man hinh them dich vu
    private void moFormThemDichVu() {
        if (moManHinhThemDichVu != null) {
            moManHinhThemDichVu.run();
        } else {
            JOptionPane.showMessageDialog(this, "Chức năng thêm dịch vụ chưa được kết nối!");
        }
    }

    // Mo man hinh sua dich vu
    private void moFormSuaDichVu() {
        String maDV = layMaDichVu();

        if (maDV == null) return;

        if (moManHinhSuaDichVu != null) {
            moManHinhSuaDichVu.accept(maDV);
        } else {
            JOptionPane.showMessageDialog(this, "Chức năng sửa dịch vụ chưa được kết nối!");
        }
    }

    // Lay ma dich vu dang chon trong bang
    private String layMaDichVu() {
        int dongChon = tableDV.getSelectedRow();

        if (dongChon == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ trong bảng!");
            return null;
        }

        int dongModel = tableDV.convertRowIndexToModel(dongChon);
        Object maDV = modelDV.getValueAt(dongModel, 1);

        if (maDV == null) {
            JOptionPane.showMessageDialog(this, "Không lấy được mã dịch vụ!");
            return null;
        }

        return maDV.toString();
    }

    // Xoa dich vu
    private void xoaDichVu() {
        String maDV = layMaDichVu();

        if (maDV == null) return;

        String thongBao = "Bạn có chắc chắn muốn xóa dịch vụ " + maDV + " không?";
        int luaChon = JOptionPane.showConfirmDialog(this, thongBao, "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (luaChon != JOptionPane.YES_OPTION) return;

        try {
            boolean ketQua = dvDAO.xoa(maDV);

            if (ketQua) {
                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công!");
                taiDichvu();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại! Dịch vụ có thể đang được sử dụng.");
            }

        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tim kiem dich vu theo cot da chon
    private void timDichVu() {
        String tuKhoa = txttimnhanh.getText().trim().toLowerCase();
        int cotTim = loccombo.getSelectedIndex();

        if (cotTim < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn cột cần tìm!");
            return;
        }

        if (dsDichVu == null || dsDichVu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách dịch vụ không có dữ liệu!");
            return;
        }

        modelDV.setRowCount(0);
        int sttGoc = 1;
        int sttHienThi = 1;

        for (DichVu dv : dsDichVu) {
            Object[] dong = {
                sttGoc, dv.layMaDV(), dv.layTenDV(),
                dv.layMota(), dv.layDongia(), dv.layTrangthai()
            };

            String giaTri = dong[cotTim] == null ? "" : dong[cotTim].toString().toLowerCase();

            if (tuKhoa.isEmpty() || giaTri.contains(tuKhoa)) {
                dong[0] = sttHienThi++;
                modelDV.addRow(dong);
            }

            sttGoc++;
        }

        if (modelDV.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dịch vụ phù hợp!");
        }
    }

    // Hien thi thong bao loi
    private void baoLoi(Exception loi) {
        JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi dịch vụ", JOptionPane.ERROR_MESSAGE);
    }

    // Mo giao dien quan ly dich vu
    public void moKhung() {
        JFrame khung = new JFrame("Quản lý dịch vụ");
        khung.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        khung.setContentPane(this);
        khung.setSize(1000, 750);
        khung.setLocationRelativeTo(null);
        khung.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyDichVuPanel().moKhung());
    }
}
