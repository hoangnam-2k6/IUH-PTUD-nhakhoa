package UI.LeTan;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static UI.LeTan.GiaoDien.xacNhan;

/**
 * Dựng và lưu file Word thật (.docx) mà không cần thư viện ngoài.
 *
 * File .docx thực chất là một gói zip chứa vài file XML. Lớp này cung cấp các hàm nhỏ để dựng
 * đoạn văn, bảng, ô... đúng thứ tự phần tử mà Word yêu cầu, nên tài liệu mở ra đúng bố cục
 * (khác với file HTML đổi đuôi .doc vốn hay bị lệch bảng, mất màu, sai khổ giấy).
 */
public final class XuatDocx {
    public static final String XANH = "0284C7";
    public static final String XANH_DAM = "0369A1";
    public static final String XANH_NHAT = "E0F2FE";
    public static final String XAM = "64748B";
    public static final String CHU = "0F172A";
    public static final String VIEN = "CBD5E1";
    public static final String LUC = "059669";
    public static final String CAM = "D97706";
    public static final String DO = "DC2626";

    /** Chiều rộng vùng nội dung (twip) của khổ A4 đứng và A4 ngang, lề 2 cm. */
    public static final int RONG_DUNG = 9638;
    public static final int RONG_NGANG = 14570;

    private static File thuMucCuoi = new File(System.getProperty("user.home"));

    private XuatDocx() {
    }

    // ===================== Dựng nội dung =====================

    /** Một đoạn chữ có định dạng; dùng kiểu nối chuỗi: {@code r("abc").dam().co(12).mau(XANH)}. */
    public static final class R {
        private final String chu;
        private boolean dam, nghieng;
        private int co = 21; // nửa điểm: 21 = 10,5 pt
        private String mau = CHU;

        private R(String chu) {
            this.chu = chu == null ? "" : chu;
        }

        public R dam() {
            dam = true;
            return this;
        }

        public R nghieng() {
            nghieng = true;
            return this;
        }

        /** Cỡ chữ theo điểm (pt). */
        public R co(double pt) {
            co = (int) Math.round(pt * 2);
            return this;
        }

        public R mau(String hex) {
            mau = hex;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("<w:r><w:rPr>");
            if (dam) sb.append("<w:b/><w:bCs/>");
            if (nghieng) sb.append("<w:i/><w:iCs/>");
            sb.append("<w:color w:val=\"").append(mau).append("\"/>")
                    .append("<w:sz w:val=\"").append(co).append("\"/><w:szCs w:val=\"").append(co).append("\"/>")
                    .append("</w:rPr><w:t xml:space=\"preserve\">").append(thoat(chu)).append("</w:t></w:r>");
            return sb.toString();
        }
    }

    public static R r(String chu) {
        return new R(chu);
    }

    /** Đoạn văn: căn lề (left/center/right), khoảng cách trước/sau (pt), khung viền tùy chọn. */
    public static String p(String canLe, double truoc, double sau, String vien, Object... chuoi) {
        StringBuilder sb = new StringBuilder("<w:p><w:pPr>");
        if (vien != null && !vien.isEmpty()) sb.append(vien);
        sb.append("<w:spacing w:before=\"").append((int) (truoc * 20)).append("\" w:after=\"")
                .append((int) (sau * 20)).append("\" w:line=\"264\" w:lineRule=\"auto\"/>");
        sb.append("<w:jc w:val=\"").append(canLe == null ? "left" : canLe).append("\"/></w:pPr>");
        for (Object o : chuoi) sb.append(o);
        return sb.append("</w:p>").toString();
    }

    public static String p(String canLe, double truoc, double sau, Object... chuoi) {
        return p(canLe, truoc, sau, "", chuoi);
    }

    /** Viền một cạnh của đoạn văn hoặc ô: {@code canh} là top/bottom/left/right. */
    public static String vienDoan(String canh, int dayTamDiem, String mau) {
        return "<w:pBdr><w:" + canh + " w:val=\"single\" w:sz=\"" + dayTamDiem + "\" w:space=\"4\" w:color=\""
                + mau + "\"/></w:pBdr>";
    }

    /** Khung viền bao quanh một đoạn văn (dùng cho "tem" trạng thái). */
    public static String khungDoan(int dayTamDiem, String mau) {
        StringBuilder sb = new StringBuilder("<w:pBdr>");
        for (String c : new String[]{"top", "left", "bottom", "right"}) {
            sb.append("<w:").append(c).append(" w:val=\"single\" w:sz=\"").append(dayTamDiem)
                    .append("\" w:space=\"4\" w:color=\"").append(mau).append("\"/>");
        }
        return sb.append("</w:pBdr>").toString();
    }

    /** Ô bảng. {@code tcPr} là phần thuộc tính thêm (viền, nền) đã đúng thứ tự; có sẵn tcW. */
    public static String o(int rong, String tcPr, String... doan) {
        StringBuilder sb = new StringBuilder("<w:tc><w:tcPr><w:tcW w:w=\"").append(rong).append("\" w:type=\"dxa\"/>")
                .append(tcPr == null ? "" : tcPr).append("</w:tcPr>");
        if (doan.length == 0) sb.append("<w:p/>");
        for (String d : doan) sb.append(d);
        return sb.append("</w:tc>").toString();
    }

    public static String hang(boolean khongTach, String... cacO) {
        StringBuilder sb = new StringBuilder("<w:tr>");
        if (khongTach) sb.append("<w:trPr><w:cantSplit/></w:trPr>");
        for (String c : cacO) sb.append(c);
        return sb.append("</w:tr>").toString();
    }

    /** Bảng cố định cột; {@code canBang} là left/center/right. */
    public static String bang(int[] cot, String canBang, int leTrai, int lePhai, String... cacHang) {
        int tong = 0;
        for (int c : cot) tong += c;
        StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"").append(tong)
                .append("\" w:type=\"dxa\"/><w:jc w:val=\"").append(canBang).append("\"/>")
                .append("<w:tblLayout w:type=\"fixed\"/><w:tblCellMar><w:top w:w=\"40\" w:type=\"dxa\"/>")
                .append("<w:left w:w=\"").append(leTrai).append("\" w:type=\"dxa\"/>")
                .append("<w:bottom w:w=\"40\" w:type=\"dxa\"/>")
                .append("<w:right w:w=\"").append(lePhai).append("\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr><w:tblGrid>");
        for (int c : cot) sb.append("<w:gridCol w:w=\"").append(c).append("\"/>");
        sb.append("</w:tblGrid>");
        for (String h : cacHang) sb.append(h);
        return sb.append("</w:tbl>").toString();
    }

    /** Viền các cạnh của ô, ví dụ {@code vienO("bottom", 4, VIEN, "top", 12, XANH)}; cạnh không nêu thì không viền. */
    public static String vienO(Object... cap) {
        String[] thuTu = {"top", "left", "bottom", "right"};
        StringBuilder sb = new StringBuilder("<w:tcBorders>");
        for (String canh : thuTu) {
            boolean co = false;
            for (int i = 0; i + 2 < cap.length; i += 3) {
                if (canh.equals(cap[i])) {
                    sb.append("<w:").append(canh).append(" w:val=\"single\" w:sz=\"").append(cap[i + 1])
                            .append("\" w:space=\"0\" w:color=\"").append(cap[i + 2]).append("\"/>");
                    co = true;
                }
            }
            if (!co) sb.append("<w:").append(canh).append(" w:val=\"nil\"/>");
        }
        return sb.append("</w:tcBorders>").toString();
    }

    public static String nen(String mau) {
        return "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + mau + "\"/>";
    }

    public static String canGiua() {
        return "<w:vAlign w:val=\"center\"/>";
    }

    // ===================== Đóng gói .docx =====================

    /** Gói phần thân thành file .docx hoàn chỉnh. */
    public static byte[] dongGoi(String than, boolean ngang) {
        String sect = ngang
                ? "<w:pgSz w:w=\"16838\" w:h=\"11906\" w:orient=\"landscape\"/>"
                : "<w:pgSz w:w=\"11906\" w:h=\"16838\"/>";
        String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><w:body>"
                + than
                + "<w:sectPr>" + sect
                + "<w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\" w:header=\"567\" "
                + "w:footer=\"567\" w:gutter=\"0\"/></w:sectPr></w:body></w:document>";

        String styles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:docDefaults><w:rPrDefault><w:rPr>"
                + "<w:rFonts w:ascii=\"Calibri\" w:hAnsi=\"Calibri\" w:eastAsia=\"Calibri\" w:cs=\"Calibri\"/>"
                + "<w:sz w:val=\"21\"/><w:szCs w:val=\"21\"/><w:lang w:val=\"vi-VN\" w:eastAsia=\"vi-VN\"/>"
                + "</w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after=\"0\" w:line=\"264\" "
                + "w:lineRule=\"auto\"/></w:pPr></w:pPrDefault></w:docDefaults>"
                + "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/>"
                + "<w:qFormat/></w:style>"
                + "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/>"
                + "<w:uiPriority w:val=\"99\"/><w:semiHidden/><w:tblPr><w:tblInd w:w=\"0\" w:type=\"dxa\"/>"
                + "<w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"108\" w:type=\"dxa\"/>"
                + "<w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar>"
                + "</w:tblPr></w:style></w:styles>";

        String types = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/word/document.xml\" "
                + "ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "<Override PartName=\"/word/styles.xml\" "
                + "ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>"
                + "</Types>";
        String relsGoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
                + "Target=\"word/document.xml\"/></Relationships>";
        String relsDoc = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" "
                + "Target=\"styles.xml\"/></Relationships>";

        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bo, StandardCharsets.UTF_8)) {
            ghi(zip, "[Content_Types].xml", types);
            ghi(zip, "_rels/.rels", relsGoc);
            ghi(zip, "word/document.xml", document);
            ghi(zip, "word/styles.xml", styles);
            ghi(zip, "word/_rels/document.xml.rels", relsDoc);
            zip.finish();
            return bo.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không dựng được file Word", e);
        }
    }

    private static void ghi(ZipOutputStream zip, String ten, String noiDung) throws IOException {
        zip.putNextEntry(new ZipEntry(ten));
        zip.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    /** Thoát ký tự đặc biệt của XML và bỏ ký tự điều khiển không hợp lệ. */
    public static String thoat(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (char c : s.toCharArray()) {
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else if (c == '"') sb.append("&quot;");
            else if (c >= 0x20 || c == '\t') sb.append(c);
            else if (c == '\n') sb.append(' ');
        }
        return sb.toString();
    }

    // ===================== Lưu file =====================

    /**
     * Mở hộp thoại lưu file .docx rồi hỏi có mở bằng Word ngay không.
     *
     * @return true nếu đã ghi file thành công.
     */
    public static boolean luu(Component cha, String tenGoiY, byte[] noiDung, String tieuDe) {
        JFileChooser chon = new JFileChooser(thuMucCuoi);
        chon.setDialogTitle(tieuDe);
        chon.setFileFilter(new FileNameExtensionFilter("Tài liệu Word (*.docx)", "docx"));
        chon.setSelectedFile(new File(thuMucCuoi, tenGoiY));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return false;

        File file = chon.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) {
            file = new File(file.getParentFile(), file.getName() + ".docx");
        }
        if (file.exists() && !xacNhan(cha, "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?", "Xác nhận")) {
            return false;
        }
        try {
            Files.write(file.toPath(), noiDung);
            thuMucCuoi = file.getParentFile();

            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu tài liệu Word:\n" + file.getAbsolutePath() + "\n\nMở bằng Word ngay bây giờ?",
                    "Xuất Word thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
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
            JOptionPane.showMessageDialog(cha,
                    "Không ghi được file (có thể file đang được mở trong Word): " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}