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
 * In phiếu khám bệnh ra file Word (.docx) khổ A4 (xem {@link #xuat}).
 * Vẫn giữ {@link #taoHtml} để dùng khi cần bản HTML.
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

    public static String tenFileWordGoiY(PhieuKham pk) {
        return String.format("PhieuKham_PK%03d.docx", pk.getMaPK());
    }

    public static void ghiFile(PhieuKham pk, BenhNhan bn, KetQuaKham kq, Path duongDan) throws IOException {
        Files.writeString(duongDan, taoHtml(pk, bn, kq), StandardCharsets.UTF_8);
    }

    private static File thuMucCuoi = new File(System.getProperty("user.home"));

    /**
     * In phiếu khám: xuất ra file Word (.docx) khổ A4 để in hoặc lưu hồ sơ.
     * Hỏi nơi lưu, sau đó (tuỳ chọn) mở bằng Word ngay để người dùng bấm In.
     */
    public static void xuat(Component cha, PhieuKham pk, BenhNhan bn, KetQuaKham kq) {
        if (pk == null) {
            JOptionPane.showMessageDialog(cha, "Chưa có phiếu khám để in.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chon = new JFileChooser(thuMucCuoi);
        chon.setDialogTitle("In phiếu khám " + String.format("PK%03d", pk.getMaPK()) + " (Word)");
        chon.setFileFilter(new FileNameExtensionFilter("Tài liệu Word (*.docx)", "docx"));
        chon.setSelectedFile(new File(thuMucCuoi, tenFileWordGoiY(pk)));
        if (chon.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return;

        File file = chon.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) {
            file = new File(file.getParentFile(), file.getName() + ".docx");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(cha,
                "File \"" + file.getName() + "\" đã tồn tại. Ghi đè?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ghiDocx(pk, bn, kq, file);
            thuMucCuoi = file.getParentFile();
            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu phiếu khám:\n" + file.getAbsolutePath()
                            + "\n\nMở bằng Word để in ngay bây giờ?",
                    "In phiếu khám thành công", JOptionPane.YES_NO_OPTION,
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
        } catch (IOException | UnsupportedOperationException ex) {
            JOptionPane.showMessageDialog(cha, "Không ghi được file: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Giữ lại để các màn hình cũ gọi được; hiện cùng xuất ra Word như {@link #xuat}. */
    public static void xuatWord(Component cha, PhieuKham pk, BenhNhan bn, KetQuaKham kq) {
        xuat(cha, pk, bn, kq);
    }

    public static void ghiDocx(PhieuKham pk, BenhNhan bn, KetQuaKham kq, File file) throws IOException {
        XuatDocx.ghi(file, taoThanDocx(pk, bn, kq), "Phiếu khám " + String.format("PK%03d", pk.getMaPK()));
    }

    // ===================== Dựng nội dung phiếu (Word) =====================

    /** Phần thân tài liệu Word của phiếu khám. */
    static String taoThanDocx(PhieuKham pk, BenhNhan bn, KetQuaKham kq) {
        final String X = XuatDocx.MAU_CHINH;
        final String maPK = String.format("PK%03d", pk.getMaPK());
        final String ngayKham = pk.getNgayKham() == null ? "" : pk.getNgayKham().format(NGAY);
        StringBuilder b = new StringBuilder(12000);

        // ---- Đầu phiếu: thông tin phòng khám (trái) + số phiếu (phải), kẻ chân đậm ----
        int[] cotDau = {6238, 3400};
        b.append(XuatDocx.moBang(cotDau,
                XuatDocx.vien("top", "nil", 0, "auto") + XuatDocx.vien("left", "nil", 0, "auto")
                        + XuatDocx.vien("bottom", "single", 18, X) + XuatDocx.vien("right", "nil", 0, "auto")
                        + XuatDocx.vien("insideH", "nil", 0, "auto") + XuatDocx.vien("insideV", "nil", 0, "auto")));
        String trai = XuatDocx.doan(XuatDocx.run(XuatHoaDon.TEN_PHONG_KHAM.toUpperCase(Locale.ROOT), true, false, 26, X),
                "left", 0, 40)
                + XuatDocx.doan(XuatDocx.run(XuatHoaDon.DIA_CHI, false, false, 18, XuatDocx.MAU_CHU_PHU), "left", 0, 20)
                + XuatDocx.doan(XuatDocx.run(XuatHoaDon.LIEN_HE, false, false, 18, XuatDocx.MAU_CHU_PHU), "left", 0, 60);
        String phai = XuatDocx.doan(XuatDocx.run("Số phiếu: ", false, false, 20, XuatDocx.MAU_CHU_PHU)
                + XuatDocx.run(maPK, true, false, 22, X), "right", 0, 30)
                + XuatDocx.doan(XuatDocx.run("Ngày khám: ", false, false, 20, XuatDocx.MAU_CHU_PHU)
                + XuatDocx.run(ngayKham, true, false, 20, null), "right", 0, 30)
                + XuatDocx.doan(XuatDocx.run("Số thứ tự: ", false, false, 20, XuatDocx.MAU_CHU_PHU)
                + XuatDocx.run(String.valueOf(pk.getSoThuTu()), true, false, 20, null), "right", 0, 60);
        b.append(XuatDocx.hang(0, XuatDocx.o(cotDau[0], 1, null, "top", trai)
                + XuatDocx.o(cotDau[1], 1, null, "top", phai)));
        b.append("</w:tbl>");

        // ---- Tiêu đề ----
        b.append(XuatDocx.doan(XuatDocx.run("PHIẾU KHÁM BỆNH", true, false, 36, X), "center", 280, 30));
        b.append(XuatDocx.doan(XuatDocx.run(kq != null && kq.getNgayCapNhat() != null
                        ? "Kết quả cập nhật lúc " + kq.getNgayCapNhat().format(NGAY_GIO)
                        : "Phiếu tiếp nhận bệnh nhân", false, true, 19, XuatDocx.MAU_CHU_PHU),
                "center", 0, 200));

        // ---- I. Thông tin bệnh nhân ----
        b.append(tieuMuc("I. THÔNG TIN BỆNH NHÂN"));
        String ten = bn == null ? pk.getTenBenhNhan() : bn.getHoTen();
        String maBN = bn == null ? pk.getMaBenhNhanHienThi() : String.format("BN%03d", bn.getMaBN());
        String ngaySinh = "";
        String gioiTinh = "";
        String sdt = "";
        String diaChi = "";
        if (bn != null) {
            ngaySinh = bn.getNgaySinh() == null ? "" : bn.getNgaySinh().format(NGAY)
                    + (bn.getTuoi() == null ? "" : " (" + bn.getTuoi() + " tuổi)");
            gioiTinh = bn.getGioiTinh();
            sdt = bn.getSoDienThoai();
            diaChi = bn.getDiaChi();
        }
        int[] cot4 = {1650, 3169, 1650, 3169};
        b.append(XuatDocx.moBang(cot4, XuatDocx.bangLuoi()));
        b.append(hangDoi(cot4, "Họ và tên", ten, true, "Mã bệnh nhân", maBN, false));
        b.append(hangDoi(cot4, "Ngày sinh", ngaySinh, false, "Giới tính", gioiTinh, false));
        b.append(hangDoi(cot4, "Số điện thoại", sdt, false, "Phòng khám", pk.getTenPhong(), false));
        b.append(hangDai(cot4, "Địa chỉ", diaChi));
        b.append("</w:tbl>");

        // ---- Cảnh báo dị ứng (nếu có) ----
        if (bn != null && bn.getDiUng() != null && !bn.getDiUng().isBlank()) {
            b.append(XuatDocx.khoangCach(100));
            int[] cot1 = {XuatDocx.RONG_NOI_DUNG};
            b.append(XuatDocx.moBang(cot1, XuatDocx.vien("top", "single", 8, "C00000")
                    + XuatDocx.vien("left", "single", 8, "C00000") + XuatDocx.vien("bottom", "single", 8, "C00000")
                    + XuatDocx.vien("right", "single", 8, "C00000")));
            b.append(XuatDocx.hang(0, XuatDocx.o(cot1[0], 1, "FDECEC", "center",
                    XuatDocx.doan(XuatDocx.run("CẢNH BÁO DỊ ỨNG: ", true, false, 20, "C00000")
                            + XuatDocx.run(bn.getDiUng(), true, false, 20, "C00000"), "left", 20, 20))));
            b.append("</w:tbl>");
        }

        // ---- II. Thông tin khám ----
        b.append(tieuMuc("II. THÔNG TIN KHÁM"));
        b.append(XuatDocx.moBang(cot4, XuatDocx.bangLuoi()));
        b.append(hangDoi(cot4, "Ngày khám", ngayKham, false, "Số thứ tự", String.valueOf(pk.getSoThuTu()), true));
        b.append(hangDoi(cot4, "Trạng thái", pk.getTrangThai(), false, "Bác sĩ khám",
                kq == null ? "" : kq.getTenBacSi(), false));
        if (bn != null && bn.getTienSuBenh() != null && !bn.getTienSuBenh().isBlank()) {
            b.append(hangDai(cot4, "Tiền sử bệnh", bn.getTienSuBenh()));
        }
        b.append("</w:tbl>");

        // ---- III. Kết quả khám ----
        b.append(tieuMuc("III. KẾT QUẢ KHÁM"));
        int[] cot2 = {2200, 7438};
        b.append(XuatDocx.moBang(cot2, XuatDocx.bangLuoi()));
        if (kq == null) {
            // Chưa có kết quả: chừa ô trống để bác sĩ ghi tay trên bản in.
            for (String nhan : new String[]{"Triệu chứng", "Chẩn đoán", "Hướng điều trị", "Đơn thuốc"}) {
                b.append(hangKetQua(cot2, nhan, "", 1000));
            }
            b.append(hangKetQua(cot2, "Ngày tái khám", "", 0));
        } else {
            hangKetQuaNeuCo(b, cot2, "Triệu chứng", kq.getTrieuChung());
            hangKetQuaNeuCo(b, cot2, "Kết quả lâm sàng", kq.getKetQuaLamSang());
            hangKetQuaNeuCo(b, cot2, "Chẩn đoán", kq.getChanDoan());
            hangKetQuaNeuCo(b, cot2, "Hướng điều trị", kq.getHuongDieuTri());
            hangKetQuaNeuCo(b, cot2, "Đơn thuốc", kq.getDonThuoc());
            hangKetQuaNeuCo(b, cot2, "Ghi chú", kq.getGhiChu());
            b.append(hangKetQua(cot2, "Ngày tái khám",
                    kq.getNgayTaiKham() == null ? "Không hẹn" : kq.getNgayTaiKham().format(NGAY), 0));
        }
        b.append("</w:tbl>");

        // ---- Chữ ký ----
        int[] cotKy = {4819, 4819};
        b.append(XuatDocx.khoangCach(160));
        b.append(XuatDocx.moBang(cotKy, XuatDocx.khongVien()));
        String ngayThang = pk.getNgayKham() == null ? "Ngày ..... tháng ..... năm ........"
                : "Ngày " + pk.getNgayKham().getDayOfMonth() + " tháng " + pk.getNgayKham().getMonthValue()
                + " năm " + pk.getNgayKham().getYear();
        String kyTrai = XuatDocx.doan(XuatDocx.run(" ", false, false, 20, null), "center", 0, 20)
                + XuatDocx.doan(XuatDocx.run("Bệnh nhân / Người nhà", true, false, 21, null), "center", 0, 10)
                + XuatDocx.doan(XuatDocx.run("(Ký, ghi rõ họ tên)", false, true, 18, XuatDocx.MAU_CHU_PHU), "center", 0, 1100);
        String tenBS = kq == null || kq.getTenBacSi() == null ? "" : kq.getTenBacSi();
        String kyPhai = XuatDocx.doan(XuatDocx.run(ngayThang, false, true, 19, XuatDocx.MAU_CHU_PHU), "center", 0, 20)
                + XuatDocx.doan(XuatDocx.run("Bác sĩ điều trị", true, false, 21, null), "center", 0, 10)
                + XuatDocx.doan(XuatDocx.run("(Ký, ghi rõ họ tên)", false, true, 18, XuatDocx.MAU_CHU_PHU), "center", 0, 900)
                + XuatDocx.doan(XuatDocx.run(tenBS, true, false, 21, null), "center", 0, 0);
        b.append(XuatDocx.hang(0, XuatDocx.o(cotKy[0], 1, null, "top", kyTrai)
                + XuatDocx.o(cotKy[1], 1, null, "top", kyPhai)));
        b.append("</w:tbl>");

        b.append(XuatDocx.doan(XuatDocx.run("Vui lòng giữ phiếu này để đối chiếu khi thanh toán và tái khám.",
                false, true, 18, XuatDocx.MAU_CHU_PHU), "center", 240, 0,
                "<w:pBdr>" + XuatDocx.vien("top", "single", 4, XuatDocx.MAU_VIEN).replace("w:space=\"0\"", "w:space=\"6\"")
                        + "</w:pBdr>"));
        return b.toString();
    }

    /** Thanh tiêu đề mục: chữ trắng, nền xanh đậm. */
    private static String tieuMuc(String chu) {
        return XuatDocx.doan(XuatDocx.run("\u00A0\u00A0" + chu, true, false, 20, "FFFFFF"), "left", 200, 80,
                "<w:keepNext/><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"" + XuatDocx.MAU_CHINH + "\"/>");
    }

    private static String oNhan(int rong, String chu) {
        return XuatDocx.o(rong, 1, XuatDocx.MAU_NEN_NHAN, "center",
                XuatDocx.doan(XuatDocx.run(chu, true, false, 19, XuatDocx.MAU_CHINH), "left", 0, 0));
    }

    private static String oGiaTri(int rong, int span, String chu, boolean dam) {
        String noi = chu == null || chu.isBlank() ? "—" : chu;
        return XuatDocx.o(rong, span, null, "center",
                XuatDocx.doan(XuatDocx.run(noi, dam, false, 20, null), "left", 0, 0));
    }

    /** Hàng 4 cột: nhãn | giá trị | nhãn | giá trị. */
    private static String hangDoi(int[] c, String n1, String v1, boolean dam1, String n2, String v2, boolean dam2) {
        return XuatDocx.hang(0, oNhan(c[0], n1) + oGiaTri(c[1], 1, v1, dam1)
                + oNhan(c[2], n2) + oGiaTri(c[3], 1, v2, dam2));
    }

    /** Hàng nhãn | giá trị trải hết 3 cột còn lại. */
    private static String hangDai(int[] c, String nhan, String giaTri) {
        return XuatDocx.hang(0, oNhan(c[0], nhan) + oGiaTri(c[1] + c[2] + c[3], 3, giaTri, false));
    }

    private static String hangKetQua(int[] c, String nhan, String noiDung, int chieuCaoToiThieu) {
        String noi = noiDung == null ? "" : noiDung;
        String doan = noi.isBlank() ? "" : XuatDocx.doan(XuatDocx.run(noi, false, false, 20, null), "left", 0, 0);
        return XuatDocx.hang(chieuCaoToiThieu,
                XuatDocx.o(c[0], 1, XuatDocx.MAU_NEN_NHAN, "top",
                        XuatDocx.doan(XuatDocx.run(nhan, true, false, 19, XuatDocx.MAU_CHINH), "left", 0, 0))
                        + XuatDocx.o(c[1], 1, null, "top", doan));
    }

    private static void hangKetQuaNeuCo(StringBuilder b, int[] c, String nhan, String noiDung) {
        if (noiDung == null || noiDung.isBlank()) return;
        b.append(hangKetQua(c, nhan, noiDung, 0));
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
        thongTin.add(new String[]{"Mã bệnh nhân:", bn == null ? pk.getMaBenhNhanHienThi() : String.format("BN%03d", bn.getMaBN())});
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
