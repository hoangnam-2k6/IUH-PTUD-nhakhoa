package UI.LeTan;

import Entity.BenhNhan;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.LeTan.GiaoDien.baoLoi;
import static UI.LeTan.GiaoDien.xacNhan;

/**
 * Xuất hóa đơn ra file HTML (UTF-8, đã định dạng sẵn khổ A5/A4 để in).
 * Mở file bằng trình duyệt rồi bấm Ctrl+P → "Lưu dưới dạng PDF" nếu cần file PDF.
 */
public final class XuatHoaDon {
    // Thông tin phòng khám in ở đầu hóa đơn - sửa lại cho đúng thực tế
    public static final String TEN_PHONG_KHAM = "PHÒNG KHÁM NHA KHOA MEDIDENT";
    public static final String DIA_CHI = "123 Nguyễn Văn Linh, Quận 7, TP. Hồ Chí Minh";
    public static final String LIEN_HE = "Hotline: 028 1234 5678  ·  Email: lienhe@medident.vn";

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] CHU_SO = {"không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] DON_VI = {"", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"};

    private static File thuMucCuoi = new File(System.getProperty("user.home"));

    private XuatHoaDon() {}

    public static String tenFileGoiY(HoaDon hd) {
        return String.format("HoaDon_HD%03d.html", hd.getMaHD());
    }

    public static void ghiFile(HoaDon hd, Path duongDan) throws IOException {
        Files.writeString(duongDan, taoHtml(hd), StandardCharsets.UTF_8);
    }

    /**
     * Mở hộp thoại lưu hóa đơn thành file HTML (in được bằng trình duyệt).
     * Dùng chung cho mọi màn hình để không lặp lại logic chọn file.
     */
    public static void xuat(Component cha, HoaDon hd) {
        if (hd == null) {
            JOptionPane.showMessageDialog(cha, "Không có hóa đơn để xuất.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        luuHtml(cha, tenFileGoiY(hd), taoHtml(hd), "Lưu hóa đơn " + String.format("HD%03d", hd.getMaHD()));
    }

    /** Xuất danh sách hóa đơn đang xem thành một trang HTML để in hoặc lưu trữ. */
    public static void xuatDanhSach(Component cha, List<HoaDon> ds) {
        if (ds == null || ds.isEmpty()) {
            JOptionPane.showMessageDialog(cha, "Danh sách hóa đơn đang trống, không có gì để xuất.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        luuHtml(cha, "DanhSachHoaDon.html", taoHtmlDanhSach(ds),
                "Lưu danh sách " + ds.size() + " hóa đơn");
    }

    /** Ghi file HTML và hỏi người dùng có mở bằng trình duyệt hay không. */
    private static void luuHtml(Component cha, String tenGoiY, String html, String tieuDe) {
        JFileChooser chon = new JFileChooser(thuMucCuoi);
        chon.setDialogTitle(tieuDe);
        chon.setFileFilter(new FileNameExtensionFilter("Trang HTML (*.html)", "html"));
        chon.setSelectedFile(new File(thuMucCuoi, tenGoiY));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return;

        File file = chon.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
            file = new File(file.getParentFile(), file.getName() + ".html");
        }
        if (file.exists() && !xacNhan(cha, "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?", "Xác nhận")) {
            return;
        }
        try {
            Files.writeString(file.toPath(), html, StandardCharsets.UTF_8);
            thuMucCuoi = file.getParentFile();

            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu:\n" + file.getAbsolutePath()
                            + "\n\nMở bằng trình duyệt để in hoặc lưu PDF ngay bây giờ?",
                    "Xuất file thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (mo == JOptionPane.YES_OPTION) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(file.toURI());
                } else {
                    JOptionPane.showMessageDialog(cha,
                            "Máy này không hỗ trợ mở trình duyệt tự động. Hãy mở file thủ công.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(cha, "Không ghi được file: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== HTML =====================

    public static String taoHtml(HoaDon hd) {
        NumberFormat tien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        BenhNhan bn = hd.getBenhNhan();
        String maHD = String.format("HD%03d", hd.getMaHD());

        StringBuilder h = new StringBuilder(6000);
        h.append("<!DOCTYPE html>\n<html lang=\"vi\"><head><meta charset=\"UTF-8\">")
                .append("<title>Hóa đơn ").append(maHD).append("</title><style>")
                .append("*{box-sizing:border-box}body{font-family:'Segoe UI',Arial,sans-serif;color:#0f172a;background:#f1f5f9;margin:0;padding:24px}")
                .append("table{width:100%;border-collapse:collapse;font-size:13px}td,th{vertical-align:top}")
                .append(".trang{max-width:800px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;padding:36px 40px;position:relative}")
                .append(".dau{border-bottom:3px solid #0284c7;margin-bottom:18px}.dau td{padding:0 0 14px}")
                .append(".dau h1{margin:0;font-size:20px;color:#0284c7}.dau p{margin:3px 0 0;font-size:12px;color:#64748b}")
                .append(".so{text-align:right}.so b{font-size:15px}")
                .append("h2{text-align:center;margin:8px 0 2px;font-size:22px;letter-spacing:1px}")
                .append(".ngay{text-align:center;color:#64748b;font-size:13px;margin-bottom:18px}")
                .append(".tt{margin-bottom:18px}.tt td{padding:3px 0;width:50%}.tt td span{color:#64748b;display:inline-block;min-width:132px}")
                .append(".ds th{background:#e0f2fe;text-align:left;padding:8px 10px;border-bottom:1px solid #bae6fd}")
                .append(".ds td{padding:8px 10px;border-bottom:1px solid #e2e8f0}.p{text-align:right!important}.g{text-align:center!important}")
                .append(".tong{width:55%;margin:12px 0 0 auto;font-size:14px}.tong td{padding:4px 0}.tong td+td{text-align:right}")
                .append(".tong .cuoi td{border-top:2px solid #0f172a;padding-top:8px;font-size:17px;font-weight:700;color:#0369a1}")
                .append(".chu{margin-top:14px;font-size:13px}.chu i{color:#334155}")
                .append(".ky{margin-top:34px;text-align:center;font-size:13px}.ky td{width:50%}")
                .append(".ky small{color:#64748b;display:block;margin-bottom:56px}")
                .append(".cam{text-align:center;margin-top:26px;color:#64748b;font-size:12px}")
                .append(".tem{position:absolute;top:78px;right:40px;border:3px solid;padding:4px 14px;font-weight:700;font-size:15px;transform:rotate(8deg);opacity:.85;background:#fff}")
                .append(".xanh{color:#059669}.vang{color:#d97706}.do{color:#dc2626}")
                .append(".nut{max-width:800px;margin:0 auto 14px;text-align:right}.nut button{background:#0284c7;color:#fff;border:0;padding:9px 20px;border-radius:6px;font-size:14px;cursor:pointer}")
                .append("@media print{body{background:#fff;padding:0}.trang{border:0;max-width:none;padding:0}.nut{display:none}@page{margin:12mm}}")
                .append("</style></head><body>\n");

        h.append("<div class=\"nut\"><button onclick=\"window.print()\">In / Lưu PDF</button></div>\n");
        h.append("<div class=\"trang\">\n");

        // Đóng dấu trạng thái
        String loaiTem = "vang";
        if ("Đã thanh toán".equals(hd.getTrangThai())) loaiTem = "xanh";
        else if ("Đã hủy".equals(hd.getTrangThai())) loaiTem = "do";
        h.append("<div class=\"tem ").append(loaiTem).append("\">").append(e(hd.getTrangThai()).toUpperCase(Locale.ROOT)).append("</div>\n");

        h.append("<table class=\"dau\"><tr><td><h1>").append(e(TEN_PHONG_KHAM)).append("</h1><p>")
                .append(e(DIA_CHI)).append("</p><p>").append(e(LIEN_HE)).append("</p></td>")
                .append("<td class=\"so\"><b>Số: ").append(maHD).append("</b><p>Phiếu khám: ")
                .append(String.format("PK%03d", hd.getMaPK())).append("</p></td></tr></table>\n");

        h.append("<h2>HÓA ĐƠN THANH TOÁN</h2><div class=\"ngay\">Ngày lập: ")
                .append(hd.getNgayLap().format(NGAY_GIO)).append("</div>\n");

        // Thông tin bệnh nhân (2 cột)
        List<String[]> thongTin = new ArrayList<>();
        thongTin.add(new String[]{"Bệnh nhân:", bn != null ? bn.getHoTen() : hd.getTenBenhNhan()});
        thongTin.add(new String[]{"Mã bệnh nhân:", bn != null ? String.format("BN%03d", bn.getMaBNSo()) : ""});
        if (bn != null) {
            thongTin.add(new String[]{"Ngày sinh:", bn.getNgaySinh() == null ? "" : bn.getNgaySinh().format(NGAY) + " (" + bn.getTuoi() + " tuổi)"});
            thongTin.add(new String[]{"Giới tính:", bn.getGioiTinh()});
            thongTin.add(new String[]{"Số điện thoại:", bn.getSoDienThoai()});
            thongTin.add(new String[]{"CMND/CCCD:", bn.getCccd()});
            thongTin.add(new String[]{"Địa chỉ:", bn.getDiaChi()});
            thongTin.add(new String[]{"Email:", bn.getEmail()});
        }
        thongTin.add(new String[]{"Ngày khám:", hd.getNgayKham() == null ? "" : hd.getNgayKham().format(NGAY)});
        thongTin.add(new String[]{"STT / Phòng khám:", hd.getSoThuTu() + (hd.getTenPhong() == null ? "" : " / " + hd.getTenPhong())});
        h.append("<table class=\"tt\">");
        for (int i = 0; i < thongTin.size(); i += 2) {
            h.append("<tr>");
            for (int j = i; j < i + 2; j++) {
                if (j < thongTin.size()) dong(h, thongTin.get(j)[0], thongTin.get(j)[1]);
                else h.append("<td></td>");
            }
            h.append("</tr>");
        }
        h.append("</table>\n");

        // Bảng dịch vụ
        h.append("<table class=\"ds\"><thead><tr><th class=\"g\" style=\"width:40px\">STT</th><th>Dịch vụ</th>")
                .append("<th class=\"g\" style=\"width:60px\">SL</th><th class=\"p\" style=\"width:120px\">Đơn giá</th>")
                .append("<th class=\"p\" style=\"width:130px\">Thành tiền</th></tr></thead><tbody>\n");
        int stt = 1;
        for (ChiTietHoaDon ct : hd.getChiTiet()) {
            h.append("<tr><td class=\"g\">").append(stt++).append("</td><td>").append(e(ct.getTenDichVu()))
                    .append("</td><td class=\"g\">").append(ct.getSoLuong())
                    .append("</td><td class=\"p\">").append(tien.format(ct.getDonGia()))
                    .append("</td><td class=\"p\">").append(tien.format(ct.getThanhTien())).append("</td></tr>\n");
        }
        h.append("</tbody></table>\n");

        // Tổng kết
        h.append("<table class=\"tong\"><tr><td>Tạm tính</td><td>").append(tien.format(hd.getTamTinh())).append(" đ</td></tr>");
        if (hd.getGiamGia().signum() > 0) {
            h.append("<tr><td>Giảm giá</td><td>- ").append(tien.format(hd.getGiamGia())).append(" đ</td></tr>");
        }
        h.append("<tr class=\"cuoi\"><td>TỔNG CỘNG</td><td>").append(tien.format(hd.getTongTien())).append(" đ</td></tr>");
        h.append("<tr><td>Phương thức</td><td>").append(e(hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT())).append("</td></tr>");
        if (hd.getNgayThanhToan() != null) {
            h.append("<tr><td>Ngày thanh toán</td><td>").append(hd.getNgayThanhToan().format(NGAY_GIO)).append("</td></tr>");
        }
        if (hd.getTienKhachDua() != null) {
            h.append("<tr><td>Tiền khách đưa</td><td>").append(tien.format(hd.getTienKhachDua())).append(" đ</td></tr>");
        }
        if (hd.getTienThua() != null) {
            h.append("<tr><td>Tiền thừa trả khách</td><td>").append(tien.format(hd.getTienThua())).append(" đ</td></tr>");
        }
        h.append("</table>\n");

        if (hd.getGhiChu() != null && !hd.getGhiChu().isBlank()) {
            h.append("<div class=\"chu\"><b>Ghi chú:</b> <i>").append(e(hd.getGhiChu())).append("</i></div>\n");
        }
        if (hd.getLyDoHuy() != null && !hd.getLyDoHuy().isBlank()) {
            h.append("<div class=\"chu\"><b>Lý do hủy:</b> <i>").append(e(hd.getLyDoHuy())).append("</i></div>\n");
        }

        h.append("<div class=\"chu\"><b>Bằng chữ:</b> <i>").append(e(docSoThanhChu(hd.getTongTien()))).append("</i></div>\n");

        h.append("<table class=\"ky\"><tr><td><small>(Ký, ghi rõ họ tên)</small><b>Người thanh toán</b></td>")
                .append("<td><small>(Ký, ghi rõ họ tên)</small><b>Nhân viên lập hóa đơn</b></td></tr></table>\n");
        h.append("<div class=\"cam\">Cảm ơn quý khách đã tin tưởng ").append(e(TEN_PHONG_KHAM)).append("!</div>\n");
        h.append("</div></body></html>\n");
        return h.toString();
    }

    /** Trang HTML tổng hợp danh sách hóa đơn kèm thống kê nhanh. */
    public static String taoHtmlDanhSach(List<HoaDon> ds) {
        NumberFormat tien = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        BigDecimal tongTatCa = BigDecimal.ZERO;
        BigDecimal tongDaThu = BigDecimal.ZERO;
        BigDecimal tongChuaThu = BigDecimal.ZERO;
        int soDaThu = 0, soChuaThu = 0, soHuy = 0;

        for (HoaDon hd : ds) {
            tongTatCa = tongTatCa.add(hd.getTongTien());
            if (hd.daThanhToan()) {
                soDaThu++;
                tongDaThu = tongDaThu.add(hd.getTongTien());
            } else if (hd.chuaThanhToan()) {
                soChuaThu++;
                tongChuaThu = tongChuaThu.add(hd.getTongTien());
            } else {
                soHuy++;
            }
        }

        StringBuilder h = new StringBuilder(8000);
        h.append("<!DOCTYPE html>\n<html lang=\"vi\"><head><meta charset=\"UTF-8\">")
                .append("<title>Danh sách hóa đơn</title><style>")
                .append("*{box-sizing:border-box}body{font-family:'Segoe UI',Arial,sans-serif;color:#0f172a;background:#f1f5f9;margin:0;padding:24px}")
                .append("table{width:100%;border-collapse:collapse;font-size:13px}td,th{vertical-align:top}")
                .append(".trang{max-width:1100px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;padding:30px 34px}")
                .append(".dau{border-bottom:3px solid #0284c7;margin-bottom:16px}.dau td{padding:0 0 12px}")
                .append(".dau h1{margin:0;font-size:20px;color:#0284c7}.dau p{margin:3px 0 0;font-size:12px;color:#64748b}")
                .append(".so{text-align:right;color:#64748b;font-size:12px}")
                .append("h2{text-align:center;margin:6px 0 4px;font-size:22px;letter-spacing:1px}")
                .append(".ngay{text-align:center;color:#64748b;font-size:13px;margin-bottom:18px}")
                .append(".tk{width:100%;margin-bottom:16px;border-collapse:separate;border-spacing:8px 0}")
                .append(".tk td{background:#e0f2fe;border:1px solid #bae6fd;padding:10px 12px;width:25%}")
                .append(".tk b{display:block;font-size:18px;color:#0369a1;margin-top:2px}")
                .append(".ds th{background:#e0f2fe;text-align:left;padding:8px 9px;border-bottom:1px solid #bae6fd;font-size:12px}")
                .append(".ds td{padding:7px 9px;border-bottom:1px solid #e2e8f0}.p{text-align:right}.g{text-align:center}")
                .append(".xanh{color:#059669}.vang{color:#d97706}.do{color:#dc2626}")
                .append(".nut{max-width:1100px;margin:0 auto 14px;text-align:right}.nut button{background:#0284c7;color:#fff;border:0;padding:9px 20px;border-radius:6px;font-size:14px;cursor:pointer}")
                .append("@media print{body{background:#fff;padding:0}.trang{border:0;max-width:none;padding:0}.nut{display:none}@page{margin:10mm}}")
                .append("</style></head><body>\n");

        h.append("<div class=\"nut\"><button onclick=\"window.print()\">In / Lưu PDF</button></div>\n");
        h.append("<div class=\"trang\">\n");
        h.append("<table class=\"dau\"><tr><td><h1>").append(e(TEN_PHONG_KHAM)).append("</h1><p>")
                .append(e(DIA_CHI)).append("</p><p>").append(e(LIEN_HE)).append("</p></td>")
                .append("<td class=\"so\">Xuất lúc: ").append(LocalDateTime.now().format(NGAY_GIO)).append("</td></tr></table>\n");
        h.append("<h2>DANH SÁCH HÓA ĐƠN</h2><div class=\"ngay\">Tổng cộng ")
                .append(ds.size()).append(" hóa đơn</div>\n");

        h.append("<table class=\"tk\"><tr>")
                .append("<td>Đã thanh toán<b>").append(soDaThu).append(" · ").append(tien.format(tongDaThu)).append(" đ</b></td>")
                .append("<td>Chưa thanh toán<b class=\"vang\">").append(soChuaThu).append(" · ").append(tien.format(tongChuaThu)).append(" đ</b></td>")
                .append("<td>Đã hủy<b class=\"do\">").append(soHuy).append("</b></td>")
                .append("<td>Tổng giá trị<b>").append(tien.format(tongTatCa)).append(" đ</b></td>")
                .append("</tr></table>\n");

        h.append("<table class=\"ds\"><thead><tr><th style=\"width:70px\">Mã HĐ</th><th>Bệnh nhân</th>")
                .append("<th style=\"width:130px\">Ngày lập</th><th class=\"p\" style=\"width:110px\">Tổng tiền</th>")
                .append("<th style=\"width:110px\">Phương thức</th><th style=\"width:120px\">Trạng thái</th>")
                .append("<th style=\"width:130px\">Ngày thanh toán</th></tr></thead><tbody>\n");
        for (HoaDon hd : ds) {
            String lop = hd.daThanhToan() ? "xanh" : (hd.daHuy() ? "do" : "vang");
            h.append("<tr><td class=\"g\">").append(String.format("HD%03d", hd.getMaHD())).append("</td><td>")
                    .append(e(hd.getTenBenhNhan() == null ? "—" : hd.getTenBenhNhan())).append("</td><td>")
                    .append(hd.getNgayLap() == null ? "—" : hd.getNgayLap().format(NGAY_GIO)).append("</td><td class=\"p\">")
                    .append(tien.format(hd.getTongTien())).append(" đ</td><td>")
                    .append(e(hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT())).append("</td><td class=\"")
                    .append(lop).append("\">").append(e(hd.getTrangThai())).append("</td><td>")
                    .append(hd.getNgayThanhToan() == null ? "—" : hd.getNgayThanhToan().format(NGAY_GIO))
                    .append("</td></tr>\n");
        }
        h.append("</tbody></table>\n");
        h.append("<div class=\"cam\" style=\"text-align:center;margin-top:22px;color:#64748b;font-size:12px\">")
                .append(e(TEN_PHONG_KHAM)).append(" · Báo cáo được tạo tự động từ phần mềm MediDent</div>\n");
        h.append("</div></body></html>\n");
        return h.toString();
    }

    private static void dong(StringBuilder h, String nhan, String giaTri) {
        h.append("<td><span>").append(e(nhan)).append("</span>")
                .append(giaTri == null || giaTri.isBlank() ? "—" : "<b>" + e(giaTri) + "</b>").append("</td>");
    }

    /** Thoát ký tự đặc biệt của HTML để dữ liệu nhập vào không phá vỡ trang hóa đơn. */
    private static String e(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ===================== Đọc số thành chữ =====================

    /** Ví dụ: 1.250.000 → "Một triệu hai trăm năm mươi nghìn đồng chẵn." */
    public static String docSoThanhChu(BigDecimal soTien) {
        long so = soTien.longValue();
        if (so == 0) return "Không đồng.";
        int[] nhom = new int[DON_VI.length];
        int dem = 0;
        while (so > 0 && dem < nhom.length) {
            nhom[dem++] = (int) (so % 1000);
            so /= 1000;
        }
        StringBuilder sb = new StringBuilder();
        boolean daCoSo = false;
        for (int i = dem - 1; i >= 0; i--) {
            if (nhom[i] == 0) continue;
            sb.append(docBaChuSo(nhom[i], daCoSo));
            if (!DON_VI[i].isEmpty()) sb.append(' ').append(DON_VI[i]);
            sb.append(' ');
            daCoSo = true;
        }
        String chu = sb.toString().trim() + " đồng chẵn.";
        return Character.toUpperCase(chu.charAt(0)) + chu.substring(1);
    }

    /** Đọc số 1-999. {@code docDayDu}: có nhóm hàng cao hơn phía trước nên phải đọc "không trăm", "lẻ". */
    private static String docBaChuSo(int n, boolean docDayDu) {
        int tram = n / 100, chuc = n % 100 / 10, donVi = n % 10;
        StringBuilder sb = new StringBuilder();
        if (tram > 0 || docDayDu) {
            sb.append(CHU_SO[tram]).append(" trăm");
        }
        if (chuc > 1) {
            sb.append(' ').append(CHU_SO[chuc]).append(" mươi");
            if (donVi == 1) sb.append(" mốt");
            else if (donVi == 5) sb.append(" lăm");
            else if (donVi > 0) sb.append(' ').append(CHU_SO[donVi]);
        } else if (chuc == 1) {
            sb.append(" mười");
            if (donVi == 5) sb.append(" lăm");
            else if (donVi > 0) sb.append(' ').append(CHU_SO[donVi]);
        } else if (donVi > 0) {
            if (tram > 0 || docDayDu) sb.append(" lẻ");
            sb.append(' ').append(CHU_SO[donVi]);
        }
        return sb.toString().trim();
    }
}
