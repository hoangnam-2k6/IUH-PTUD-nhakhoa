
package UI.QuanLy;

import util.BangStyle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import DAO.BenhNhanDAO;
import Entity.BenhNhan;

public class QuanLyBenhNhanPanel extends JPanel implements ActionListener {

    private JLabel lbltitle, lbltimnhanh;
    private JTextField txttimnhanh;
    private JButton btnthem, btnsua, btnxoa, btntim;
    private JPanel pbtn, benhnhan_top, benhnhan_center;
    private DefaultTableModel modelBN;
    private JTable tableBN;
    private JScrollPane scrollBN;
    private JComboBox<String> loccombo;
    private List<BenhNhan> dsBenhNhan;

    private Runnable callbackThem;
    private Consumer<String> callbackSua;
    private final BenhNhanDAO bnDAO = new BenhNhanDAO();
	private JButton btntailai;

    public QuanLyBenhNhanPanel() {
        this(null, null);
    }

    public QuanLyBenhNhanPanel(Runnable callbackThem, Consumer<String> callbackSua) {
        this.callbackThem = callbackThem;
        this.callbackSua = callbackSua;

        taoGiaoDien();
        ganSuKien();

        try {
            loadbenhnhan();
        } catch (SQLException loi) {
            baoLoi(loi);
        }
    }

    // Tao giao dien benh nhan
    private void taoGiaoDien() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        String[] colsBN = {
            "STT", "Mã bệnh nhân", "Họ tên", "Ngày sinh", "Giới tính",
            "Số điện thoại", "Email", "Địa chỉ", "Tiền sử bệnh", "Trạng thái"
        };

        benhnhan_top = new JPanel(new BorderLayout(10, 10));

        lbltitle = new JLabel("Quản lý bệnh nhân");
        lbltitle.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel phanTieuDe = new JPanel(new FlowLayout(FlowLayout.LEFT));
        phanTieuDe.add(lbltitle);
        benhnhan_top.add(phanTieuDe, BorderLayout.NORTH);

        lbltimnhanh = new JLabel("Tìm nhanh:");
        txttimnhanh = new JTextField(18);
        txttimnhanh.setPreferredSize(new Dimension(220, 30));

        loccombo = new JComboBox<>(colsBN);

        btntim = taoNut("Tìm");
        btntailai = taoNut("tải lại");
        btnthem = taoNut("+ Thêm bệnh nhân");
        btnsua = taoNut("Chỉnh sửa");
        btnxoa = taoNut("Ngừng hoạt động");

        pbtn = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        pbtn.setBackground(Color.WHITE);
        pbtn.add(lbltimnhanh);
        pbtn.add(txttimnhanh);
        pbtn.add(loccombo);
        pbtn.add(btntim);
        pbtn.add(btntailai);
        pbtn.add(btnthem);
        pbtn.add(btnsua);
        pbtn.add(btnxoa);

        benhnhan_top.add(pbtn, BorderLayout.CENTER);
        add(benhnhan_top, BorderLayout.NORTH);

        // Tao bang danh sach benh nhan
        benhnhan_center = new JPanel(new BorderLayout());
        benhnhan_center.setBackground(Color.WHITE);
        benhnhan_center.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        modelBN = new DefaultTableModel(colsBN, 0) {
            @Override
            public boolean isCellEditable(int dong, int cot) {
                return false;
            }
        };

        tableBN = new JTable(modelBN);
        tableBN.setRowHeight(25);
        BangStyle.apDung(tableBN);
        tableBN.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableBN.setFillsViewportHeight(true);
        tableBN.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scrollBN = new JScrollPane(tableBN);
        BangStyle.apDungScrollPane(scrollBN);
        benhnhan_center.add(scrollBN, BorderLayout.CENTER);
        add(benhnhan_center, BorderLayout.CENTER);
    }

    // Tao nut chuc nang
    private JButton taoNut(String tenNut) {
        JButton nut = new JButton(tenNut);
        nut.setBackground(Color.WHITE);
        nut.setForeground(new Color(52, 152, 219));
        nut.setFocusPainted(false);
        return nut;
    }

    // Gan su kien cho cac nut
    private void ganSuKien() {
        txttimnhanh.addActionListener(e -> timBenhNhan());
        btntim.addActionListener(this);
        btntailai.addActionListener(this);
        btnthem.addActionListener(this);
        btnsua.addActionListener(this);
        btnxoa.addActionListener(this);
    }

    // Xu ly su kien cac nut
    @Override
    public void actionPerformed(ActionEvent e) {
        Object nut = e.getSource();

        if (nut == btntim) {
            timBenhNhan();
        } else if (nut == btntailai) {
			try {
				loadbenhnhan();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			txttimnhanh.setText("");
			loccombo.setSelectedIndex(0);
		} else if (nut == btnthem) {
            themBenhNhan();
        } else if (nut == btnsua) {
            suaBenhNhan();
        } else if (nut == btnxoa) {
            try {
                xoaBenhNhan();
            } catch (SQLException loi) {
                baoLoi(loi);
            }
        }
    }

    // Goi giao dien them benh nhan
    private void themBenhNhan() {
        if (callbackThem != null) {
            callbackThem.run();
        } else {
            JOptionPane.showMessageDialog(this, "Chưa thiết lập chức năng thêm bệnh nhân.");
        }
    }

    // Goi giao dien sua benh nhan
    private void suaBenhNhan() {
        String maBN = layMaBenhNhan();

        if (maBN == null) return;

        if (callbackSua != null) {
            callbackSua.accept(maBN);
        } else {
            JOptionPane.showMessageDialog(this, "Chưa thiết lập chức năng sửa bệnh nhân.");
        }
    }

    // Lay ma benh nhan dang chon tren bang
    private String layMaBenhNhan() {
        int dongChon = tableBN.getSelectedRow();

        if (dongChon == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân trong bảng!");
            return null;
        }

        int dongModel = tableBN.convertRowIndexToModel(dongChon);
        Object maBN = modelBN.getValueAt(dongModel, 1);

        if (maBN == null) {
            JOptionPane.showMessageDialog(this, "Không lấy được mã bệnh nhân!");
            return null;
        }

        return maBN.toString();
    }

    // Lay danh sach benh nhan tu SQL
    public void loadbenhnhan() throws SQLException {
        dsBenhNhan = bnDAO.getallbenhnhan();
        modelBN.setRowCount(0);

        if (dsBenhNhan == null) return;

        int stt = 1;

        for (BenhNhan bn : dsBenhNhan) {
            modelBN.addRow(new Object[]{
                stt++, bn.getMaBN(), bn.getHoten(), bn.getNgaysinh(),
                bn.getGioitinh(), bn.getSdt(), bn.getEmail(),
                bn.getDiachi(), bn.getTiensubenh(), bn.getTrangthai()
            });
        }
    }

    // Ngừng hoạt động bệnh nhân bằng trạng thái enum, giữ nguyên lịch sử liên quan.
    private void xoaBenhNhan() throws SQLException {
        String maBN = layMaBenhNhan();

        if (maBN == null) return;

        int dongChon = tableBN.getSelectedRow();
        int dongModel = tableBN.convertRowIndexToModel(dongChon);
        Object giaTriTrangThai = modelBN.getValueAt(dongModel, 9);

        if (trangThaiDaNgung(giaTriTrangThai)) {
            JOptionPane.showMessageDialog(this,
                "Bệnh nhân này đã ở trạng thái NGUNGHOATDONG.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String thongBao = "Chuyển hồ sơ bệnh nhân " + maBN
                + " sang trạng thái NGUNGHOATDONG?\n"
                + "Lịch hẹn, phiếu khám và hóa đơn cũ sẽ được giữ nguyên.";
        int luaChon = JOptionPane.showConfirmDialog(
            this, thongBao, "Xác nhận ngừng hoạt động",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (luaChon != JOptionPane.YES_OPTION) return;

        boolean ketQua = bnDAO.delete(maBN);

        if (ketQua) {
            JOptionPane.showMessageDialog(this,
                "Đã chuyển hồ sơ bệnh nhân sang NGUNGHOATDONG.\n"
                + "Dữ liệu lịch sử vẫn được giữ nguyên.");
            loadbenhnhan();
        } else {
            JOptionPane.showMessageDialog(this,
                "Không thể cập nhật trạng thái bệnh nhân. Vui lòng kiểm tra lại dữ liệu.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean trangThaiDaNgung(Object trangThai) {
        return trangThai != null
                && "NGUNGHOATDONG".equalsIgnoreCase(trangThai.toString().trim());
    }

    // Tim kiem benh nhan theo cot duoc chon
    private void timBenhNhan() {
        String tuKhoa = txttimnhanh.getText().trim().toLowerCase();
        int cotTim = loccombo.getSelectedIndex();

        if (cotTim < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn cột cần tìm!");
            return;
        }

        if (dsBenhNhan == null || dsBenhNhan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách bệnh nhân không có dữ liệu!");
            return;
        }

        modelBN.setRowCount(0);
        int sttGoc = 1;
        int sttHienThi = 1;

        for (BenhNhan bn : dsBenhNhan) {
            Object[] dong = {
                sttGoc, bn.getMaBN(), bn.getHoten(), bn.getNgaysinh(),
                bn.getGioitinh(), bn.getSdt(), bn.getEmail(),
                bn.getDiachi(), bn.getTiensubenh(), bn.getTrangthai()
            };

            String giaTri = dong[cotTim] == null ? "" : dong[cotTim].toString().toLowerCase();

            if (tuKhoa.isEmpty() || giaTri.contains(tuKhoa)) {
                dong[0] = sttHienThi++;
                modelBN.addRow(dong);
            }

            sttGoc++;
        }

        if (modelBN.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bệnh nhân phù hợp!");
        }
    }

    // Hien thi loi SQL
    private void baoLoi(SQLException loi) {
        JOptionPane.showMessageDialog(this, loi.getMessage(), "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    // Mo giao dien benh nhan
    public void moframe() {
        JFrame khung = new JFrame("Quản lý bệnh nhân");
        khung.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        khung.setContentPane(this);
        khung.setSize(1000, 750);
        khung.setLocationRelativeTo(null);
        khung.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyBenhNhanPanel().moframe());
    }
}
