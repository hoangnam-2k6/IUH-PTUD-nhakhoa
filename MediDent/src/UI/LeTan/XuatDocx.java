package UI.LeTan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Bộ dựng tài liệu Word (.docx) tối giản, không cần thư viện ngoài.
 * .docx thực chất là file ZIP chứa các tệp XML; lớp này cung cấp các hàm
 * tạo đoạn văn, bảng và đóng gói thành file Word hợp lệ (khổ A4).
 */
final class XuatDocx {
    static final String MAU_CHINH = "1F4E79";   // xanh đậm dùng cho tiêu đề, đường kẻ
    static final String MAU_VIEN = "BFBFBF";    // viền bảng
    static final String MAU_NEN_NHAN = "EAF1F8"; // nền ô nhãn
    static final String MAU_CHU_PHU = "595959";
    static final int RONG_NOI_DUNG = 9638;      // A4 trừ lề trái/phải 2 cm (twips)

    private XuatDocx() {
    }

    // ===================== Ký tự & chữ =====================

    /** Thoát ký tự XML và loại bỏ ký tự điều khiển không hợp lệ. */
    static String e(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '&') sb.append("&amp;");
            else if (c == '<') sb.append("&lt;");
            else if (c == '>') sb.append("&gt;");
            else if (c == '"') sb.append("&quot;");
            else if (c < 0x20 && c != '\t' && c != '\n' && c != '\r') continue;
            else sb.append(c);
        }
        return sb.toString();
    }

    /** Một run chữ; ký tự xuống dòng được đổi thành thẻ ngắt dòng của Word. */
    static String run(String chu, boolean dam, boolean nghieng, int coChuNuaPoint, String mau) {
        StringBuilder rpr = new StringBuilder("<w:rPr>");
        if (dam) rpr.append("<w:b/><w:bCs/>");
        if (nghieng) rpr.append("<w:i/><w:iCs/>");
        if (mau != null) rpr.append("<w:color w:val=\"").append(mau).append("\"/>");
        if (coChuNuaPoint > 0) {
            rpr.append("<w:sz w:val=\"").append(coChuNuaPoint).append("\"/>")
                    .append("<w:szCs w:val=\"").append(coChuNuaPoint).append("\"/>");
        }
        rpr.append("</w:rPr>");

        String[] dong = (chu == null ? "" : chu).replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dong.length; i++) {
            sb.append("<w:r>").append(rpr);
            if (i > 0) sb.append("<w:br/>");
            sb.append("<w:t xml:space=\"preserve\">").append(e(dong[i])).append("</w:t></w:r>");
        }
        return sb.toString();
    }

    /** Đoạn văn. canh: left | center | right | both. */
    static String doan(String noiDungRuns, String canh, int truoc, int sau) {
        return doan(noiDungRuns, canh, truoc, sau, "");
    }

    static String doan(String noiDungRuns, String canh, int truoc, int sau, String ppThem) {
        return "<w:p><w:pPr>" + ppThem
                + "<w:spacing w:before=\"" + truoc + "\" w:after=\"" + sau + "\" w:line=\"264\" w:lineRule=\"auto\"/>"
                + "<w:jc w:val=\"" + canh + "\"/></w:pPr>" + noiDungRuns + "</w:p>";
    }

    /** Đoạn trống nhỏ để ngăn hai bảng liền nhau bị Word gộp làm một. */
    static String khoangCach(int twips) {
        return "<w:p><w:pPr><w:spacing w:before=\"0\" w:after=\"0\" w:line=\"" + twips
                + "\" w:lineRule=\"exact\"/></w:pPr></w:p>";
    }

    // ===================== Bảng =====================

    static String vien(String vi, String kieu, int sz, String mau) {
        return "<w:" + vi + " w:val=\"" + kieu + "\" w:sz=\"" + sz + "\" w:space=\"0\" w:color=\"" + mau + "\"/>";
    }

    /** Mở bảng với các cột có độ rộng cho trước; borders là chuỗi XML bên trong tblBorders. */
    static String moBang(int[] cot, String borders) {
        int tong = 0;
        for (int c : cot) tong += c;
        StringBuilder sb = new StringBuilder("<w:tbl><w:tblPr><w:tblW w:w=\"").append(tong)
                .append("\" w:type=\"dxa\"/>")
                .append("<w:tblBorders>").append(borders).append("</w:tblBorders>")
                .append("<w:tblLayout w:type=\"fixed\"/>")
                .append("<w:tblCellMar><w:top w:w=\"50\" w:type=\"dxa\"/><w:left w:w=\"100\" w:type=\"dxa\"/>")
                .append("<w:bottom w:w=\"50\" w:type=\"dxa\"/><w:right w:w=\"100\" w:type=\"dxa\"/></w:tblCellMar>")
                .append("</w:tblPr><w:tblGrid>");
        for (int c : cot) sb.append("<w:gridCol w:w=\"").append(c).append("\"/>");
        return sb.append("</w:tblGrid>").toString();
    }

    static String bangLuoi() {
        return vien("top", "single", 4, MAU_VIEN) + vien("left", "single", 4, MAU_VIEN)
                + vien("bottom", "single", 4, MAU_VIEN) + vien("right", "single", 4, MAU_VIEN)
                + vien("insideH", "single", 4, MAU_VIEN) + vien("insideV", "single", 4, MAU_VIEN);
    }

    static String khongVien() {
        return vien("top", "nil", 0, "auto") + vien("left", "nil", 0, "auto")
                + vien("bottom", "nil", 0, "auto") + vien("right", "nil", 0, "auto")
                + vien("insideH", "nil", 0, "auto") + vien("insideV", "nil", 0, "auto");
    }

    /** Hàng bảng; chieuCaoToiThieu = 0 nếu để tự co giãn. */
    static String hang(int chieuCaoToiThieu, String cacO) {
        return "<w:tr><w:trPr><w:cantSplit/>"
                + (chieuCaoToiThieu > 0 ? "<w:trHeight w:val=\"" + chieuCaoToiThieu + "\" w:hRule=\"atLeast\"/>" : "")
                + "</w:trPr>" + cacO + "</w:tr>";
    }

    /** Ô bảng. nen = null nếu không tô nền; span = số cột gộp; canhDoc: top | center. */
    static String o(int rong, int span, String nen, String canhDoc, String cacDoan) {
        return "<w:tc><w:tcPr><w:tcW w:w=\"" + rong + "\" w:type=\"dxa\"/>"
                + (span > 1 ? "<w:gridSpan w:val=\"" + span + "\"/>" : "")
                + (nen != null ? "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + nen + "\"/>" : "")
                + "<w:vAlign w:val=\"" + canhDoc + "\"/></w:tcPr>"
                + (cacDoan == null || cacDoan.isEmpty() ? khoangCach(20) : cacDoan) + "</w:tc>";
    }

    /**
     * Ô bảng tuỳ biến: đệm riêng (twips), viền riêng và gộp dọc.
     *
     * @param demDoc   đệm trên/dưới
     * @param demNgang đệm trái/phải
     * @param vienO    chuỗi XML bên trong tcBorders (null nếu dùng viền của bảng)
     * @param gopDoc   null | "restart" (ô đầu khối gộp) | "cont" (ô nối tiếp)
     */
    static String oTuyBien(int rong, int span, String nen, String canhDoc, int demDoc, int demNgang,
                           String vienO, String gopDoc, String cacDoan) {
        return "<w:tc><w:tcPr><w:tcW w:w=\"" + rong + "\" w:type=\"dxa\"/>"
                + (span > 1 ? "<w:gridSpan w:val=\"" + span + "\"/>" : "")
                + ("restart".equals(gopDoc) ? "<w:vMerge w:val=\"restart\"/>" : "cont".equals(gopDoc) ? "<w:vMerge/>" : "")
                + (vienO != null ? "<w:tcBorders>" + vienO + "</w:tcBorders>" : "")
                + (nen != null ? "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + nen + "\"/>" : "")
                + "<w:tcMar><w:top w:w=\"" + demDoc + "\" w:type=\"dxa\"/><w:left w:w=\"" + demNgang
                + "\" w:type=\"dxa\"/><w:bottom w:w=\"" + demDoc + "\" w:type=\"dxa\"/><w:right w:w=\""
                + demNgang + "\" w:type=\"dxa\"/></w:tcMar>"
                + "<w:vAlign w:val=\"" + canhDoc + "\"/></w:tcPr>"
                + (cacDoan == null || cacDoan.isEmpty() ? khoangCach(20) : cacDoan) + "</w:tc>";
    }

    // ===================== Đóng gói =====================

    /** Ghi file .docx gồm phần thân (các phần tử con của w:body, chưa gồm sectPr). */
    static void ghi(File file, String thanXml, String tieuDeTaiLieu) throws IOException {
        String documentXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><w:body>"
                + thanXml
                + "<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/>"
                + "<w:pgMar w:top=\"850\" w:right=\"1134\" w:bottom=\"850\" w:left=\"1134\" "
                + "w:header=\"567\" w:footer=\"567\" w:gutter=\"0\"/></w:sectPr></w:body></w:document>";

        String stylesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:docDefaults><w:rPrDefault><w:rPr>"
                + "<w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:eastAsia=\"Arial\" w:cs=\"Arial\"/>"
                + "<w:sz w:val=\"20\"/><w:szCs w:val=\"20\"/><w:lang w:val=\"vi-VN\" w:eastAsia=\"vi-VN\" w:bidi=\"ar-SA\"/>"
                + "</w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after=\"0\" w:line=\"264\" w:lineRule=\"auto\"/>"
                + "</w:pPr></w:pPrDefault></w:docDefaults>"
                + "<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/><w:qFormat/></w:style>"
                + "<w:style w:type=\"table\" w:default=\"1\" w:styleId=\"TableNormal\"><w:name w:val=\"Normal Table\"/>"
                + "<w:uiPriority w:val=\"99\"/><w:semiHidden/><w:unhideWhenUsed/><w:tblPr><w:tblInd w:w=\"0\" w:type=\"dxa\"/>"
                + "<w:tblCellMar><w:top w:w=\"0\" w:type=\"dxa\"/><w:left w:w=\"108\" w:type=\"dxa\"/>"
                + "<w:bottom w:w=\"0\" w:type=\"dxa\"/><w:right w:w=\"108\" w:type=\"dxa\"/></w:tblCellMar></w:tblPr></w:style>"
                + "</w:styles>";

        String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "<Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>"
                + "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
                + "</Types>";

        String rels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" Target=\"docProps/core.xml\"/>"
                + "</Relationships>";

        String docRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";

        String core = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:title>" + e(tieuDeTaiLieu) + "</dc:title><dc:creator>MediDent</dc:creator></cp:coreProperties>";

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bo, StandardCharsets.UTF_8)) {
            themTep(zip, "[Content_Types].xml", contentTypes);
            themTep(zip, "_rels/.rels", rels);
            themTep(zip, "word/document.xml", documentXml);
            themTep(zip, "word/styles.xml", stylesXml);
            themTep(zip, "word/_rels/document.xml.rels", docRels);
            themTep(zip, "docProps/core.xml", core);
        }
        Files.write(file.toPath(), bo.toByteArray());
    }

    private static void themTep(ZipOutputStream zip, String ten, String noiDung) throws IOException {
        zip.putNextEntry(new ZipEntry(ten));
        zip.write(noiDung.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
