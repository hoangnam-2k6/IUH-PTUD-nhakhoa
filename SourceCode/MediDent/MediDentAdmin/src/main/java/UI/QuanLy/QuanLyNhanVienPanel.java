
package UI.QuanLy;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.NhanVienDAO;
import Entity.NhanVien;

public class QuanLyNhanVienPanel extends JPanel implements ActionListener {

    private JLabel lbltitle, lbltimnhanh;
    private JTextField txttimnhanh;
    private JButton btntim, btnthem, btnsua, btnxoa;
    private JPanel nhanvien_top, nhanvien_center, pbtn;
    private DefaultTableModel modelNV;
    private JTable tableNV;
    private JScrollPane scrollNV;
    public static final Color BORDER = new Color(0xE2E8F0);
    public static final Color TEXT = new Color(0x0F172A);
    private JComboBox<String> loccombo;
    private List<NhanVien> dsNhanVien;

    private Runnable moManHinhThemNhanVien;
    private Consumer<String> moManHinhSuaNhanVien;

    private final NhanVienDAO nvDAO = new NhanVienDAO();

    public QuanLyNhanVienPanel() {
        this(null, null);
    }

    public QuanLyNhanVienPanel(Runnable callbackThem, Consumer<String> callbackSua) {
        moManHinhThemNhanVien = callbackThem;
        moManHinhSuaNhanVien = callbackSua;

        taoGiaoDien();
        ganSuKien();
        taiNhanvien();
    }

    // Tao giao dien quan ly nhan vien
    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] tenCot = {
            "STT", "Mã nhân viên", "Họ tên", "Ngày sinh", "Giới tính",
            "Số điện thoại", "Email", "Địa chỉ", "Chức vụ", "Trạng thái", "Mã tài khoản"
        };

        nhanvien_top = new JPanel(new BorderLayout(10, 10));

        lbltitle = new JLabel("Quản lý nhân viên");
        lbltitle.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel phanTieuDe = new JPanel(new FlowLayout(FlowLayout.LEFT));
        phanTieuDe.add(lbltitle);
        nhanvien_top.add(phanTieuDe, BorderLayout.NORTH);

        lbltimnhanh = new JLabel("Tìm nhanh:");
        txttimnhanh = new JTextField(18);
        txttimnhanh.setPreferredSize(new Dimension(220, 30));

        loccombo = new JComboBox<>(tenCot);

        btntim = taoNut("Tìm");
        btnthem = taoNut("Thêm nhân viên");
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

        nhanvien_top.add(pbtn, BorderLayout.CENTER);
        add(nhanvien_top, BorderLayout.NORTH);

        // Tao bang danh sach nhan vien
        nhanvien_center = new JPanel(new BorderLayout());
        nhanvien_center.setBackground(Color.WHITE);
        nhanvien_center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        modelNV = new DefaultTableModel(tenCot, 0) {
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };

        tableNV = new JTable(modelNV);
        tableNV.setRowHeight(25);
        tableNV.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableNV.setFillsViewportHeight(true);
        tableNV.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollNV = new JScrollPane(tableNV);
        nhanvien_center.add(scrollNV, BorderLayout.CENTER);
        add(nhanvien_center, BorderLayout.CENTER);
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
        txttimnhanh.addActionListener(e -> timNhanVien());
        btntim.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);
    }

    // Xu ly su kien cac nut
    @Override
    public void actionPerformed(ActionEvent e) {
        Object nut = e.getSource();

        if (nut == btntim) {
            timNhanVien();
        } else if (nut == btnthem) {
            moFormThemNhanVien();
        } else if (nut == btnsua) {
            moFormSuaNhanVien();
        } else if (nut == btnxoa) {
            xoaNhanVien();
        }
    }

    // Lay danh sach nhan vien tu SQL Server
    public void taiNhanvien() {
        try {
            List<NhanVien> duLieuMoi = nvDAO.layTatCaNhanvien();
            dsNhanVien = duLieuMoi;
            modelNV.setRowCount(0);

            if (dsNhanVien == null) return;

            int stt = 1;

            for (NhanVien nv : dsNhanVien) {
                modelNV.addRow(taoDongNhanVien(nv, stt++));
            }
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Chuyen thong tin nhan vien thanh mot dong tren bang
    private Object[] taoDongNhanVien(NhanVien nv, int stt) {
        return new Object[]{
            stt, nv.layMaNV(), nv.layHoten(), nv.layNgaysinh(), nv.layGioitinh(),
            nv.laySdt(), nv.layEmail(), nv.layDiachi(), nv.layChucvu(),
            nv.layTrangthai(), nv.layMaTK()
        };
    }

    // Mo giao dien them nhan vien
    private void moFormThemNhanVien() {
        if (moManHinhThemNhanVien != null) {
            moManHinhThemNhanVien.run();
        } else {
            JOptionPane.showMessageDialog(this, "Chức năng thêm nhân viên chưa được kết nối!");
        }
    }

    // Mo giao dien sua nhan vien
    private void moFormSuaNhanVien() {
        String maNV = layMaNhanVien();

        if (maNV == null) return;

        if (moManHinhSuaNhanVien != null) {
            moManHinhSuaNhanVien.accept(maNV);
        } else {
            JOptionPane.showMessageDialog(this, "Chức năng sửa nhân viên chưa được kết nối!");
        }
    }

    // Lay ma nhan vien dang chon trong bang
    private String layMaNhanVien() {
        int dongChon = tableNV.getSelectedRow();

        if (dongChon == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên trong bảng!");
            return null;
        }

        int dongModel = tableNV.convertRowIndexToModel(dongChon);
        Object maNV = modelNV.getValueAt(dongModel, 1);

        if (maNV == null) {
            JOptionPane.showMessageDialog(this, "Không lấy được mã nhân viên!");
            return null;
        }

        return maNV.toString();
    }

    // Xoa nhan vien
    private void xoaNhanVien() {
        String maNV = layMaNhanVien();

        if (maNV == null) return;

        String thongBao = "Bạn có chắc chắn muốn xóa nhân viên " + maNV + " không?";
        int luaChon = JOptionPane.showConfirmDialog(this, thongBao, "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (luaChon != JOptionPane.YES_OPTION) return;

        try {
            boolean ketQua = nvDAO.xoa(maNV);

            if (ketQua) {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                taiNhanvien();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thất bại!");
            }
        } catch (Exception loi) {
            baoLoi(loi);
        }
    }

    // Tim kiem nhan vien theo cot da chon
    private void timNhanVien() {
        String tuKhoa = txttimnhanh.getText().trim().toLowerCase();
        int cotTim = loccombo.getSelectedIndex();

        if (cotTim < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn cột cần tìm!");
            return;
        }

        if (dsNhanVien == null || dsNhanVien.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách nhân viên không có dữ liệu!");
            return;
        }

        modelNV.setRowCount(0);
        int sttGoc = 1;
        int sttHienThi = 1;

        for (NhanVien nv : dsNhanVien) {
            Object[] dong = taoDongNhanVien(nv, sttGoc++);
            String giaTri = dong[cotTim] == null ? "" : dong[cotTim].toString().toLowerCase();

            if (tuKhoa.isEmpty() || giaTri.contains(tuKhoa)) {
                dong[0] = sttHienThi++;
                modelNV.addRow(dong);
            }
        }

        if (modelNV.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên phù hợp!");
        }
    }

    // Hien thi thong bao loi
    private void baoLoi(Exception loi) {
        JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi nhân viên", JOptionPane.ERROR_MESSAGE);
    }

    // Mo giao dien quan ly nhan vien
    public void moKhung() {
        JFrame khung = new JFrame("Quản lý nhân viên");
        khung.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        khung.setContentPane(this);
        khung.setSize(1000, 750);
        khung.setLocationRelativeTo(null);
        khung.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyNhanVienPanel().moKhung());
    }
}
