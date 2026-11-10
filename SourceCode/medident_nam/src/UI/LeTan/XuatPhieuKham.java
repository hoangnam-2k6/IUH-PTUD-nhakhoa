package UI.LeTan;

import Entity.BenhNhan;
import Entity.KetQuaKham;
import Entity.PhieuKham;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Xuất phiếu khám bệnh ra file HTML (UTF-8) để in hoặc lưu PDF.
 * Phiếu có thể kèm theo hồ sơ kết quả khám nếu đã nhập.
 */
public final class XuatPhieuKham {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private XuatPhieuKham() {
    }

    public static String tenFileGoiY(PhieuKham pk) {
        return String.format("PhieuKham_PK%03d.html", pk.getMaPK());
    }

    public static void ghiFile(PhieuKham pk, BenhNhan bn, KetQuaKham kq, Path duongDan) throws IOException {
        Files.writeString(duongDan, taoHtml(pk, bn, kq), StandardCharsets.UTF_8);
    }

    /** Mở hộp thoại lưu file và (tuỳ chọn) mở bằng trình duyệt. */
    public static void xuat(Component cha, PhieuKham pk, BenhNhan bn, KetQuaKham kq) {
        if (pk == null) {
            JOptionPane.showMessageDialog(cha, "Chưa có phiếu khám để in.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        File thuMuc = new File(System.getProperty("user.home"));
        JFileChooser chon = new JFileChooser(thuMuc);
        chon.setDialogTitle("Lưu phiếu khám");
        chon.setFileFilter(new FileNameExtensionFilter("Trang HTML (*.html)", "html"));
        chon.setSelectedFile(new File(thuMuc, tenFileGoiY(pk)));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return;

        File file = chon.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
            file = new File(file.getParentFile(), file.getName() + ".html");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(cha,
                "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ghiFile(pk, bn, kq, file.toPath());
            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu phiếu khám:\n" + file.getAbsolutePath()
                            + "\n\nMở bằng trình duyệt để in ngay bây giờ?",
                    "Xuất phiếu khám thành công", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
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

    public static String taoHtml(PhieuKham pk, BenhNhan bn, KetQuaKham kq) {
        StringBuilder h = new StringBuilder(6000);
        String maPK = String.format("PK%03d", pk.getMaPK());

        h.append("<!DOCTYPE html>\n<html lang=\"vi\"><head><meta charset=\"UTF-8\">")
                .append("<title>Phiếu khám ").append(maPK).append("</title><style>")
                .append("*{box-sizing:border-box}body{font-family:'Segoe UI',Arial,sans-serif;color:#0f172a;background:#f1f5f9;margin:0;padding:24px}")
                .append(".trang{max-width:750px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;padding:34px 38px}")
                .append("table{width:100%;border-collapse:collapse;font-size:13px}td,th{vertical-align:top}")
                .append(".dau{border-bottom:3px solid #0284c7;margin-bottom:18px}.dau td{padding:0 0 14px}")
                .append(".dau h1{margin:0;font-size:19px;color:#0284c7}.dau p{margin:3px 0 0;font-size:12px;color:#64748b}")
                .append(".so{text-align:right}.so b{font-size:15px}")
                .append("h2{text-align:center;margin:8px 0 2px;font-size:21px;letter-spacing:1px}")
                .append(".ngay{text-align:center;color:#64748b;font-size:13px;margin-bottom:16px}")
                .append(".tt{margin-bottom:16px}.tt td{padding:3px 0;width:50%}.tt td span{color:#64748b;display:inline-block;min-width:118px}")
                .append(".stt{margin:12px 0;text-align:center;border:2px dashed #0284c7;padding:10px;border-radius:8px}")
                .append(".stt b{font-size:30px;color:#0369a1}")
                .append(".muc{margin-top:14px}.muc h3{margin:0 0 5px;font-size:13px;color:#0284c7;text-transform:uppercase;letter-spacing:.5px}")
                .append(".muc .noidung{border:1px solid #e2e8f0;border-radius:6px;padding:8px 10px;font-size:13px;min-height:30px;white-space:pre-wrap}")
                .append(".diung{margin:10px 0;border:1px solid #fca5a5;background:#fef2f2;color:#b91c1c;padding:8px 10px;border-radius:6px;font-weight:600;font-size:13px}")
                .append(".ky{margin-top:32px;text-align:center;font-size:13px}.ky td{width:50%}.ky small{color:#64748b;display:block;margin-bottom:52px}")
                .append(".cam{text-align:center;margin-top:22px;color:#64748b;font-size:12px}")
                .append(".nut{max-width:750px;margin:0 auto 14px;text-align:right}.nut button{background:#0284c7;color:#fff;border:0;padding:9px 20px;border-radius:6px;font-size:14px;cursor:pointer}")
                .append("@media print{body{background:#fff;padding:0}.trang{border:0;max-width:none;padding:0}.nut{display:none}@page{margin:12mm}}")
                .append("</style></head><body>\n");

        h.append("<div class=\"nut\"><button onclick=\"window.print()\">In / Lưu PDF</button></div>\n");
        h.append("<div class=\"trang\">\n");

        h.append("<table class=\"dau\"><tr><td><h1>").append(e(XuatHoaDon.TEN_PHONG_KHAM))
                .append("</h1><p>").append(e(XuatHoaDon.DIA_CHI)).append("</p><p>")
                .append(e(XuatHoaDon.LIEN_HE)).append("</p></td>")
                .append("<td class=\"so\"><b>Số phiếu: ").append(maPK).append("</b><p>Ngày: ")
                .append(pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY))
                .append("</p></td></tr></table>\n");

        h.append("<h2>PHIẾU KHÁM BỆNH</h2><div class=\"ngay\">")
                .append(kq != null && kq.getNgayCapNhat() != null
                        ? "Cập nhật kết quả: " + kq.getNgayCapNhat().format(NGAY_GIO)
                        : "Phiếu tiếp nhận bệnh nhân")
                .append("</div>\n");

        List<String[]> thongTin = new ArrayList<>();
        thongTin.add(new String[]{"Bệnh nhân:", bn == null ? pk.getTenBenhNhan() : bn.getHoTen()});
        thongTin.add(new String[]{"Mã bệnh nhân:", bn == null ? pk.getMaBenhNhanHienThi() : String.format("BN%03d", bn.getMaBNSo())});
        if (bn != null) {
            thongTin.add(new String[]{"Ngày sinh:", bn.getNgaySinh() == null ? "" : bn.getNgaySinh().format(NGAY) + " (" + bn.getTuoi() + " tuổi)"});
            thongTin.add(new String[]{"Giới tính:", bn.getGioiTinh()});
            thongTin.add(new String[]{"Số điện thoại:", bn.getSoDienThoai()});
            thongTin.add(new String[]{"Địa chỉ:", bn.getDiaChi()});
        }
        thongTin.add(new String[]{"Ngày khám:", pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY)});
        thongTin.add(new String[]{"Phòng khám:", pk.getTenPhong()});
        thongTin.add(new String[]{"Trạng thái:", pk.getTrangThai()});
        thongTin.add(new String[]{"Bác sĩ phụ trách:", kq == null ? "" : kq.getTenBacSi()});

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

        h.append("<div class=\"stt\">SỐ THỨ TỰ<b>").append(pk.getSoThuTu()).append("</b></div>\n");

        if (bn != null && bn.getDiUng() != null && !bn.getDiUng().isBlank()) {
            h.append("<div class=\"diung\">⚠ CẢNH BÁO DỊ ỨNG: ").append(e(bn.getDiUng())).append("</div>\n");
        }
        if (bn != null && bn.getTienSuBenh() != null && !bn.getTienSuBenh().isBlank()) {
            muc(h, "Tiền sử bệnh", bn.getTienSuBenh());
        }

        if (kq == null) {
            h.append("<div class=\"muc\"><h3>Kết quả khám</h3>")
                    .append("<div class=\"noidung\">Bác sĩ sẽ ghi kết quả sau khi khám.</div></div>\n");
        } else {
            muc(h, "Triệu chứng", kq.getTrieuChung());
            muc(h, "Kết quả lâm sàng", kq.getKetQuaLamSang());
            muc(h, "Chẩn đoán", kq.getChanDoan());
            muc(h, "Hướng điều trị", kq.getHuongDieuTri());
            muc(h, "Đơn thuốc", kq.getDonThuoc());
            muc(h, "Ghi chú", kq.getGhiChu());
            if (kq.getNgayTaiKham() != null) {
                h.append("<div class=\"muc\"><h3>Ngày tái khám</h3><div class=\"noidung\">")
                        .append(kq.getNgayTaiKham().format(NGAY)).append("</div></div>\n");
            }
        }

        h.append("<table class=\"ky\"><tr><td><small>(Ký, ghi rõ họ tên)</small><b>Bệnh nhân / Người nhà</b></td>")
                .append("<td><small>(Ký, ghi rõ họ tên)</small><b>Bác sĩ điều trị</b></td></tr></table>\n");
        h.append("<div class=\"cam\">Vui lòng giữ phiếu này để đối chiếu khi thanh toán và tái khám.</div>\n");
        h.append("</div></body></html>\n");
        return h.toString();
    }

    private static void muc(StringBuilder h, String ten, String noiDung) {
        if (noiDung == null || noiDung.isBlank()) return;
        h.append("<div class=\"muc\"><h3>").append(e(ten))
                .append("</h3><div class=\"noidung\">").append(e(noiDung)).append("</div></div>\n");
    }

    private static void dong(StringBuilder h, String nhan, String giaTri) {
        h.append("<td><span>").append(e(nhan)).append("</span>")
                .append(giaTri == null || giaTri.isBlank() ? "—" : "<b>" + e(giaTri) + "</b>").append("</td>");
    }

    private static String e(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
