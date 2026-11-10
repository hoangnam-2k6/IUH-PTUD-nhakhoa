package UI.LeTan;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import static UI.LeTan.GiaoDien.xacNhan;

/**
 * Xuất tài liệu ra file Word (.doc) mà không cần thư viện ngoài.
 *
 * Cách làm: nội dung được dựng sẵn dưới dạng HTML UTF-8 (giống bản in),
 * sau đó thêm các thẻ meta của Microsoft Word và lưu với phần mở rộng .doc.
 * Word mở trực tiếp được file này và giữ nguyên bảng, màu, phông chữ.
 */
public final class XuatWord {

    private static final String CSS_WORD =
            "body{background:#fff;padding:0}.trang{border:0;max-width:none;padding:0}"
                    + ".nut{display:none}@page{margin:12mm}";

    private static File thuMucCuoi = new File(System.getProperty("user.home"));

    private XuatWord() {
    }

    /** Chuyển trang HTML in thành tài liệu Word (.doc). */
    public static String thanhWord(String html) {
        if (html == null) return "";
        String ketQua = html
                .replaceFirst("<html[^>]*>",
                        "<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
                                + "xmlns:w=\"urn:schemas-microsoft-com:office:word\" "
                                + "xmlns=\"http://www.w3.org/TR/REC-html40\">")
                .replace("<head>", "<head><meta name=\"ProgId\" content=\"Word.Document\">")
                .replace("</style>", CSS_WORD + "</style>");
        return ketQua;
    }

    /**
     * Mở hộp thoại lưu file Word và hỏi người dùng có mở ngay bằng Word không.
     *
     * @return true nếu đã ghi file thành công.
     */
    public static boolean xuat(Component cha, String tenGoiY, String html, String tieuDe) {
        JFileChooser chon = new JFileChooser(thuMucCuoi);
        chon.setDialogTitle(tieuDe);
        chon.setFileFilter(new FileNameExtensionFilter("Tài liệu Word (*.doc)", "doc"));
        chon.setSelectedFile(new File(thuMucCuoi, tenGoiY));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return false;

        File file = chon.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".doc")) {
            file = new File(file.getParentFile(), file.getName() + ".doc");
        }
        if (file.exists() && !xacNhan(cha, "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?",
                "Xác nhận")) {
            return false;
        }
        try {
            Files.writeString(file.toPath(), thanhWord(html), StandardCharsets.UTF_8);
            thuMucCuoi = file.getParentFile();

            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu tài liệu Word:\n" + file.getAbsolutePath()
                            + "\n\nMở bằng Word ngay bây giờ?",
                    "Xuất Word thành công", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
            if (mo == JOptionPane.YES_OPTION) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(cha,
                            "Máy này không hỗ trợ mở file tự động. Hãy mở file thủ công.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            return true;
        } catch (IOException | UnsupportedOperationException e) {
            JOptionPane.showMessageDialog(cha, "Không ghi được file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
