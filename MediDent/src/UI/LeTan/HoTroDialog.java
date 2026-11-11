package UI.LeTan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static UI.LeTan.GiaoDien.*;

/**
 * Hộp thoại "Hỗ trợ": liệt kê 3 tài liệu hướng dẫn (file Word) trong thư mục
 * {@code TaiLieuHuongDan} để lễ tân bấm <b>Mở</b> và đọc bằng Microsoft Word.
 */
public final class HoTroDialog extends JDialog {
    private static final String THU_MUC = "TaiLieuHuongDan";

    /** {tên file, tiêu đề, mô tả ngắn} */
    private static final String[][] TAI_LIEU = {
            {"01_CachThucHien.docx", "01 · Cách thực hiện",
                    "Quy trình làm việc từng bước trong ngày và cách xử lý các tình huống thường gặp."},
            {"02_SuDungUngDung.docx", "02 · Sử dụng ứng dụng",
                    "Hướng dẫn thao tác chi tiết trên từng màn hình: bệnh nhân, lịch hẹn, khám bệnh, thanh toán."},
            {"03_NghiepVu.docx", "03 · Nghiệp vụ",
                    "Các quy định của phòng khám: điều kiện, trạng thái, việc được và không được làm."}
    };

    private HoTroDialog(Window cha) {
        super(cha, "Hỗ trợ - Tài liệu hướng dẫn", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel goc = new JPanel(new BorderLayout(0, 14));
        goc.setBackground(NEN_APP);
        goc.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JLabel tieuDe = new JLabel("Tài liệu hướng dẫn dành cho Lễ tân");
        tieuDe.setFont(font(Font.BOLD, 17));
        tieuDe.setForeground(CHU_CHINH);
        JLabel moTa = new JLabel("Bấm \"Mở\" để xem tài liệu bằng Microsoft Word.");
        moTa.setFont(font(Font.PLAIN, 12));
        moTa.setForeground(CHU_PHU);
        JPanel dau = new JPanel(new GridLayout(2, 1, 0, 4));
        dau.setOpaque(false);
        dau.add(tieuDe);
        dau.add(moTa);
        goc.add(dau, BorderLayout.NORTH);

        JPanel ds = new JPanel();
        ds.setLayout(new BoxLayout(ds, BoxLayout.Y_AXIS));
        ds.setOpaque(false);
        for (String[] tl : TAI_LIEU) {
            ds.add(taoDong(tl[0], tl[1], tl[2]));
            ds.add(Box.createVerticalStrut(10));
        }
        goc.add(ds, BorderLayout.CENTER);

        JButton dong = taoNutTrang("Đóng");
        dong.addActionListener(e -> dispose());
        JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        chan.setOpaque(false);
        chan.add(dong);
        goc.add(chan, BorderLayout.SOUTH);

        setContentPane(goc);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(620, 400);
        setResizable(false);
        setLocationRelativeTo(cha);
    }

    private JPanel taoDong(String tenFile, String ten, String mo) {
        JPanel dong = new JPanel(new BorderLayout(14, 0));
        dong.setBackground(NEN_THE);
        dong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(VIEN),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(font(Font.BOLD, 14));
        lblTen.setForeground(MAU_CHINH_DAM);
        JLabel lblMo = new JLabel("<html><div style='width:360px'>" + mo + "</div></html>");
        lblMo.setFont(font(Font.PLAIN, 12));
        lblMo.setForeground(CHU_PHU);
        JPanel chu = new JPanel(new BorderLayout(0, 4));
        chu.setOpaque(false);
        chu.add(lblTen, BorderLayout.NORTH);
        chu.add(lblMo, BorderLayout.CENTER);

        JButton mo2 = taoNut("Mở", MAU_CHINH, MAU_CHINH_DAM);
        mo2.setPreferredSize(new Dimension(84, 34));
        mo2.addActionListener(e -> moTaiLieu(this, tenFile));

        dong.add(chu, BorderLayout.CENTER);
        dong.add(mo2, BorderLayout.EAST);
        return dong;
    }

    /** Hiện hộp thoại Hỗ trợ. */
    public static void hien(Component cha) {
        Window w = cha == null ? null : SwingUtilities.getWindowAncestor(cha);
        if (w == null && cha instanceof Window) w = (Window) cha;
        new HoTroDialog(w).setVisible(true);
    }

    // ===================== Mở file =====================

    private static void moTaiLieu(Component cha, String tenFile) {
        File file = timFile(tenFile);
        if (file == null) {
            thongBao(cha, "Không tìm thấy tài liệu \"" + tenFile + "\".\n"
                    + "Tài liệu phải nằm trong thư mục " + THU_MUC + " cùng thư mục với phần mềm.\n"
                    + "Vui lòng báo bộ phận kỹ thuật.");
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            } else {
                thongBao(cha, "Máy này không tự mở được tài liệu.\nHãy mở thủ công file:\n" + file.getAbsolutePath());
            }
        } catch (IOException | RuntimeException e) {
            thongBao(cha, "Không mở được tài liệu bằng Microsoft Word.\n"
                    + "Hãy mở thủ công file:\n" + file.getAbsolutePath());
        }
    }

    /** Tìm file trong thư mục TaiLieuHuongDan: thư mục chạy, các thư mục cha, và thư mục gốc dự án (suy từ build/classes). */
    private static File timFile(String tenFile) {
        List<File> goc = new ArrayList<>();
        File hienTai = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        for (int i = 0; i < 3 && hienTai != null; i++) {
            goc.add(hienTai);
            hienTai = hienTai.getParentFile();
        }
        try {
            File vt = new File(HoTroDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // build/classes -> build -> gốc dự án
            File g = vt.isFile() ? vt.getParentFile() : vt;
            for (int i = 0; i < 3 && g != null; i++) {
                goc.add(g);
                g = g.getParentFile();
            }
        } catch (URISyntaxException | RuntimeException ignored) {
            // bỏ qua: vẫn còn thư mục chạy
        }
        for (File g : goc) {
            File f = new File(new File(g, THU_MUC), tenFile);
            if (f.isFile()) return f;
        }
        return null;
    }
}
