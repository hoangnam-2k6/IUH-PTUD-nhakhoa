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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static UI.LeTan.GiaoDien.baoLoi;
import static UI.LeTan.GiaoDien.tien;

/**
 * In hóa đơn ra file Word (.docx) khổ A4: bố cục có dải tiêu đề màu, khung thông tin
 * bệnh nhân / lượt khám, bảng dịch vụ kẻ sọc, khối tổng thanh toán, số tiền bằng chữ
 * và vùng chữ ký. Chỉ hóa đơn đã thanh toán mới được in.
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

    // Bảng màu hóa đơn
    private static final String XANH = XuatDocx.MAU_CHINH;   // dải tiêu đề, tiêu đề bảng
    private static final String XANH_NHAT = "F2F7FC";        // nền khung / hàng sọc
    private static final String VIEN_KHUNG = "C9D9E8";
    private static final String VIEN_MANH = "E1E8F0";
    private static final String CHU_NHAT = "DCE6F1";         // chữ phụ trên nền xanh
    private static final String XANH_LA = "A7F3D0";          // chữ trạng thái trên nền xanh
    private static final String DO = "C00000";
    private static final String XAM = XuatDocx.MAU_CHU_PHU;

    private static File thuMucCuoi = new File(System.getProperty("user.home"));

    private XuatHoaDon() {}

    public static String tenFileGoiY(HoaDon hd) {
        return String.format("HoaDon_HD%03d.docx", hd.getMaHD());
    }

    /**
     * In hóa đơn: lưu thành file Word (.docx) khổ A4 rồi (tuỳ chọn) mở bằng Word để bấm In.
     * Chỉ hóa đơn đã thanh toán mới được in; hóa đơn chưa thanh toán hoặc đã hủy bị chặn tại đây.
     */
    public static void xuat(Component cha, HoaDon hd) {
        if (!kiemTraDuocIn(cha, hd)) return;

        JFileChooser chon = new JFileChooser(thuMucCuoi);
        chon.setDialogTitle("In hóa đơn " + String.format("HD%03d", hd.getMaHD()) + " (Word)");
        chon.setFileFilter(new FileNameExtensionFilter("Tài liệu Word (*.docx)", "docx"));
        chon.setSelectedFile(new File(thuMucCuoi, tenFileGoiY(hd)));
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
            ghiDocx(hd, file);
            thuMucCuoi = file.getParentFile();
            int mo = JOptionPane.showConfirmDialog(cha,
                    "Đã lưu hóa đơn:\n" + file.getAbsolutePath() + "\n\nMở bằng Word để in ngay bây giờ?",
                    "In hóa đơn thành công", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
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
            baoLoi(cha, new IllegalArgumentException("Không ghi được file: " + ex.getMessage()));
        }
    }

    /** Chỉ hóa đơn đã thanh toán mới được in. */
    public static boolean kiemTraDuocIn(Component cha, HoaDon hd) {
        if (hd == null) {
            JOptionPane.showMessageDialog(cha, "Không có hóa đơn để in.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (!hd.daThanhToan()) {
            JOptionPane.showMessageDialog(cha,
                    "Hóa đơn " + String.format("HD%03d", hd.getMaHD())
                            + " đang ở trạng thái \"" + hd.getTrangThai() + "\".\n"
                            + "Chỉ in hóa đơn sau khi đã thanh toán.",
                    "Chưa thể in hóa đơn", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public static void ghiDocx(HoaDon hd, File file) throws IOException {
        XuatDocx.ghi(file, taoThanDocx(hd), "Hóa đơn " + String.format("HD%03d", hd.getMaHD()));
    }

    // ===================== Dựng nội dung hóa đơn (Word) =====================

    public static String taoThanDocx(HoaDon hd) {
        BenhNhan bn = hd.getBenhNhan();
        String maHD = String.format("HD%03d", hd.getMaHD());
        StringBuilder b = new StringBuilder(12000);

        // ---- 1. Dải tiêu đề màu: phòng khám + số hóa đơn ----
        int[] cotDau = {6300, 3338};
        b.append(XuatDocx.moBang(cotDau, XuatDocx.khongVien()));
        String traiDau = XuatDocx.doan(XuatDocx.run(TEN_PHONG_KHAM, true, false, 30, "FFFFFF"), "left", 0, 60)
                + XuatDocx.doan(XuatDocx.run(DIA_CHI, false, false, 18, CHU_NHAT), "left", 0, 20)
                + XuatDocx.doan(XuatDocx.run(LIEN_HE, false, false, 18, CHU_NHAT), "left", 0, 0);
        String phaiDau = XuatDocx.doan(XuatDocx.run("SỐ HÓA ĐƠN", false, false, 16, CHU_NHAT), "right", 0, 0)
                + XuatDocx.doan(XuatDocx.run(maHD, true, false, 40, "FFFFFF"), "right", 0, 40)
                + XuatDocx.doan(XuatDocx.run("● " + hd.getTrangThai().toUpperCase(Locale.ROOT), true, false, 17,
                XANH_LA), "right", 0, 0);
        b.append(XuatDocx.hang(0,
                XuatDocx.oTuyBien(cotDau[0], 1, XANH, "center", 260, 280, null, null, traiDau)
                        + XuatDocx.oTuyBien(cotDau[1], 1, XANH, "center", 260, 280, null, null, phaiDau)));
        b.append("</w:tbl>");

        // ---- 2. Tiêu đề ----
        b.append(XuatDocx.doan(XuatDocx.run("HÓA ĐƠN THANH TOÁN", true, false, 40, XANH), "center", 300, 40));
        b.append(XuatDocx.doan(XuatDocx.run("Ngày lập: " + (hd.getNgayLap() == null ? "—" : hd.getNgayLap().format(NGAY_GIO))
                + "   |   Phiếu khám: " + String.format("PK%03d", hd.getMaPK()), false, true, 19, XAM), "center", 0, 220));

        // ---- 3. Hai khung thông tin ----
        List<String[]> trai = new ArrayList<>();
        trai.add(new String[]{"Họ và tên", bn != null ? bn.getHoTen() : hd.getTenBenhNhan(), "1"});
        trai.add(new String[]{"Mã bệnh nhân", bn != null ? String.format("BN%03d", bn.getMaBN())
                : hd.getMaBenhNhanHienThi(), null});
        if (bn != null) {
            trai.add(new String[]{"Ngày sinh", bn.getNgaySinh() == null ? null
                    : bn.getNgaySinh().format(NGAY) + (bn.getTuoi() == null ? "" : " (" + bn.getTuoi() + " tuổi)"), null});
            trai.add(new String[]{"Giới tính", bn.getGioiTinh(), null});
            trai.add(new String[]{"Điện thoại", bn.getSoDienThoai(), null});
            trai.add(new String[]{"Địa chỉ", bn.getDiaChi(), null});
        }
        List<String[]> phai = new ArrayList<>();
        phai.add(new String[]{"Ngày khám", hd.getNgayKham() == null ? null : hd.getNgayKham().format(NGAY), "1"});
        phai.add(new String[]{"Phòng khám", hd.getTenPhong(), null});
        phai.add(new String[]{"Ngày lập", hd.getNgayLap() == null ? null : hd.getNgayLap().format(NGAY_GIO), null});
        phai.add(new String[]{"Người lập", hd.getTenNguoiLap(), null});
        phai.add(new String[]{"Thanh toán lúc", hd.getNgayThanhToan() == null ? null
                : hd.getNgayThanhToan().format(NGAY_GIO), null});

        int[] cotInfo = {4719, 200, 4719};
        b.append(XuatDocx.moBang(cotInfo, XuatDocx.khongVien()));
        String vienKhung = XuatDocx.vien("top", "single", 6, VIEN_KHUNG) + XuatDocx.vien("left", "single", 6, VIEN_KHUNG)
                + XuatDocx.vien("bottom", "single", 6, VIEN_KHUNG) + XuatDocx.vien("right", "single", 6, VIEN_KHUNG);
        b.append(XuatDocx.hang(0,
                XuatDocx.oTuyBien(cotInfo[0], 1, XANH_NHAT, "top", 140, 200, vienKhung, null,
                        khungThongTin("Thông tin bệnh nhân", trai))
                        + XuatDocx.oTuyBien(cotInfo[1], 1, null, "top", 0, 0, null, null, null)
                        + XuatDocx.oTuyBien(cotInfo[2], 1, XANH_NHAT, "top", 140, 200, vienKhung, null,
                        khungThongTin("Thông tin lượt khám", phai))));
        b.append("</w:tbl>");
        b.append(XuatDocx.khoangCach(200));

        // ---- 4. Bảng dịch vụ ----
        int[] cotDV = {620, 4218, 800, 1900, 2100};
        b.append(XuatDocx.moBang(cotDV, XuatDocx.khongVien()));
        String vienDau = XuatDocx.vien("top", "nil", 0, "auto") + XuatDocx.vien("left", "nil", 0, "auto")
                + XuatDocx.vien("bottom", "nil", 0, "auto") + XuatDocx.vien("right", "nil", 0, "auto");
        b.append("<w:tr><w:trPr><w:cantSplit/><w:tblHeader/></w:trPr>")
                .append(oDV(cotDV[0], XANH, vienDau, "center", "STT", true, "FFFFFF"))
                .append(oDV(cotDV[1], XANH, vienDau, "left", "DỊCH VỤ", true, "FFFFFF"))
                .append(oDV(cotDV[2], XANH, vienDau, "center", "SL", true, "FFFFFF"))
                .append(oDV(cotDV[3], XANH, vienDau, "right", "ĐƠN GIÁ (đ)", true, "FFFFFF"))
                .append(oDV(cotDV[4], XANH, vienDau, "right", "THÀNH TIỀN (đ)", true, "FFFFFF"))
                .append("</w:tr>");
        int stt = 1;
        String vienHang = XuatDocx.vien("top", "nil", 0, "auto") + XuatDocx.vien("left", "nil", 0, "auto")
                + XuatDocx.vien("bottom", "single", 4, VIEN_MANH) + XuatDocx.vien("right", "nil", 0, "auto");
        for (ChiTietHoaDon ct : hd.getChiTiet()) {
            String nen = stt % 2 == 0 ? XANH_NHAT : null;
            b.append(XuatDocx.hang(0,
                    oDV(cotDV[0], nen, vienHang, "center", String.valueOf(stt), false, null)
                            + oDV(cotDV[1], nen, vienHang, "left", ct.getTenDichVu(), false, null)
                            + oDV(cotDV[2], nen, vienHang, "center", String.valueOf(ct.getSoLuong()), false, null)
                            + oDV(cotDV[3], nen, vienHang, "right", tien(ct.getDonGia()), false, null)
                            + oDV(cotDV[4], nen, vienHang, "right", tien(ct.getThanhTien()), true, null)));
            stt++;
        }
        b.append("</w:tbl>");
        b.append(XuatDocx.khoangCach(160));

        // ---- 5. Bằng chữ (trái) + tổng thanh toán (phải) ----
        int[] cotTong = {6838, 2800};
        b.append(XuatDocx.moBang(cotTong, XuatDocx.khongVien()));

        String ghiChu = hd.getGhiChu() == null ? "" : hd.getGhiChu().trim();

        List<String[]> dongTong = new ArrayList<>();
        dongTong.add(new String[]{"Tạm tính", tien(hd.getTamTinh()) + " đ", "thuong"});
        if (hd.getGiamGia().signum() > 0) {
            String ma = hd.getMaGiamCode() == null || hd.getMaGiamCode().isBlank() ? "" : " (" + hd.getMaGiamCode() + ")";
            dongTong.add(new String[]{"Giảm giá" + ma, "- " + tien(hd.getGiamGia()) + " đ", "giam"});
        }
        dongTong.add(new String[]{"TỔNG THANH TOÁN", tien(hd.getTongTien()) + " đ", "tong"});
        dongTong.add(new String[]{"Phương thức", hd.getPhuongThucTT() == null ? "—" : hd.getPhuongThucTT(), "phu"});
        if (hd.getTienKhachDua() != null) {
            dongTong.add(new String[]{"Tiền khách đưa", tien(hd.getTienKhachDua()) + " đ", "phu"});
        }
        if (hd.getTienThua() != null) {
            dongTong.add(new String[]{"Tiền thừa trả khách", tien(hd.getTienThua()) + " đ", "phu"});
        }

        String khongVienO = vienDau;
        for (int i = 0; i < dongTong.size(); i++) {
            String[] d = dongTong.get(i);
            String loai = d[2];
            String nenO = "tong".equals(loai) ? XANH : null;
            String mauNhan = "tong".equals(loai) ? "FFFFFF" : XAM;
            String mauGiaTri = "tong".equals(loai) ? "FFFFFF" : "giam".equals(loai) ? DO : null;
            int coChu = "tong".equals(loai) ? 24 : 20;
            String vien = "tong".equals(loai) ? khongVienO
                    : XuatDocx.vien("top", "nil", 0, "auto") + XuatDocx.vien("left", "nil", 0, "auto")
                    + XuatDocx.vien("bottom", "single", 4, VIEN_MANH) + XuatDocx.vien("right", "nil", 0, "auto");

            b.append(XuatDocx.hang(0,
                    XuatDocx.oTuyBien(cotTong[0], 1, nenO, "center", 80, 140, vien, null,
                    XuatDocx.doan(XuatDocx.run(d[0], "tong".equals(loai), false, 19, mauNhan), "left", 0, 0))
                    + XuatDocx.oTuyBien(cotTong[1], 1, nenO, "center", 80, 140, vien, null,
                    XuatDocx.doan(XuatDocx.run(d[1], !"phu".equals(loai), false, coChu, mauGiaTri), "right", 0, 0))));
        }
        b.append("</w:tbl>");

        // ---- 6. Bằng chữ / ghi chú ----
        b.append(XuatDocx.doan(XuatDocx.run("Bằng chữ: ", true, false, 20, XAM)
                + XuatDocx.run(docSoThanhChu(hd.getTongTien()), true, true, 20, XANH), "left", 200, 60));
        if (!ghiChu.isEmpty()) {
            b.append(XuatDocx.doan(XuatDocx.run("Ghi chú: ", true, false, 19, XAM)
                    + XuatDocx.run(ghiChu, false, false, 19, null), "left", 0, 0));
        }

        // ---- 7. Chân hóa đơn ----
        b.append(XuatDocx.doan(XuatDocx.run("Cảm ơn quý khách đã tin tưởng " + TEN_PHONG_KHAM + "!", true, false, 19, XANH),
                "center", 600, 40,
                "<w:pBdr>" + XuatDocx.vien("top", "single", 6, XANH).replace("w:space=\"0\"", "w:space=\"8\"") + "</w:pBdr>"));
        b.append(XuatDocx.doan(XuatDocx.run("Hóa đơn được lập từ phần mềm MediDent. Vui lòng giữ hóa đơn để đối chiếu khi cần.",
                false, true, 17, XAM), "center", 0, 0));
        return b.toString();
    }

    /** Một khung thông tin: tiêu đề có đường kẻ + các dòng "Nhãn   Giá trị" (bỏ dòng trống). */
    private static String khungThongTin(String tieuDe, List<String[]> dong) {
        StringBuilder sb = new StringBuilder();
        sb.append(XuatDocx.doan(XuatDocx.run(tieuDe.toUpperCase(Locale.ROOT), true, false, 18, XANH), "left", 0, 100,
                "<w:pBdr>" + XuatDocx.vien("bottom", "single", 6, "9FBBD6").replace("w:space=\"0\"", "w:space=\"3\"")
                        + "</w:pBdr>"));
        for (String[] d : dong) {
            if (d[1] == null || d[1].isBlank()) continue;
            sb.append("<w:p><w:pPr><w:tabs><w:tab w:val=\"left\" w:pos=\"1400\"/></w:tabs>")
                    .append("<w:spacing w:before=\"0\" w:after=\"50\" w:line=\"264\" w:lineRule=\"auto\"/>")
                    .append("<w:ind w:left=\"1400\" w:hanging=\"1400\"/></w:pPr>")
                    .append(XuatDocx.run(d[0], false, false, 18, XAM))
                    .append("<w:r><w:tab/></w:r>")
                    .append(XuatDocx.run(d[1], d[2] != null, false, d[2] != null ? 20 : 19, null))
                    .append("</w:p>");
        }
        return sb.toString();
    }

    private static String tieuDeNho(String chu) {
        return XuatDocx.doan(XuatDocx.run(chu.toUpperCase(Locale.ROOT), true, false, 16, XAM), "left", 0, 40);
    }

    /** Ô của bảng dịch vụ. */
    private static String oDV(int rong, String nen, String vien, String canh, String chu, boolean dam, String mau) {
        return XuatDocx.oTuyBien(rong, 1, nen, "center", 110, 120, vien, null,
                XuatDocx.doan(XuatDocx.run(chu, dam, false, 19, mau), canh, 0, 0));
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
