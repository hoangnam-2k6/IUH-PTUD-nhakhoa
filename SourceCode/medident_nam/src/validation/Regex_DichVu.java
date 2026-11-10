package validation;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import enums.trangthai_dichvu;

public class Regex_DichVu {

    // =========================
    // 1. KHAI BÁO REGEX
    // =========================

    // Mã dịch vụ: chữ, số, dấu gạch ngang, gạch dưới
    // Tối đa 20 ký tự
    private static final Pattern REGEX_MADV =
            Pattern.compile("^[A-Za-z0-9_-]{1,20}$");

    // Tên dịch vụ: hỗ trợ tiếng Việt, số và dấu câu
    // Tối đa 150 ký tự
    private static final Pattern REGEX_TENDV =
            Pattern.compile(
                "^[\\p{L}\\p{M}\\p{N}]" +
                "[\\p{L}\\p{M}\\p{N}\\s.,:;()/%+&'_-]*$"
            );

    // Mô tả: cho phép chữ, số, dấu câu, ký hiệu và xuống dòng
    private static final Pattern REGEX_MOTA =
            Pattern.compile(
                "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{S}\\p{Zs}\\r\\n\\t]*$"
            );

    // Đơn giá: tối đa 16 chữ số nguyên và 2 chữ số thập phân
    private static final Pattern REGEX_DONGIA_INPUT =
            Pattern.compile("^[0-9]{1,16}([.,][0-9]{0,2})?$");

    private static final Pattern REGEX_DONGIA =
            Pattern.compile("^[0-9]{1,16}(\\.[0-9]{1,2})?$");


    // =========================
    // 2. HÀM HỖ TRỢ
    // =========================

    private static boolean rong(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean kiemTraDoDai(
            String value, int maxLength) {

        return value != null
                && value.trim().length() <= maxLength;
    }


    // =========================
    // 3. MÃ DỊCH VỤ
    // NVARCHAR(20) NOT NULL
    // =========================

    public static boolean isValidMaDV(String maDV) {

        if (rong(maDV)) {
            return false;
        }

        return kiemTraDoDai(maDV, 20)
                && REGEX_MADV.matcher(maDV.trim()).matches();
    }


    // =========================
    // 4. TÊN DỊCH VỤ
    // NVARCHAR(150) NOT NULL
    // =========================

    public static boolean isValidTenDV(String tenDV) {

        if (rong(tenDV)) {
            return false;
        }

        return kiemTraDoDai(tenDV, 150)
                && REGEX_TENDV.matcher(tenDV.trim()).matches();
    }


    // =========================
    // 5. MÔ TẢ
    // NVARCHAR(1000) NULL
    // =========================

    public static boolean isValidMoTa(String moTa) {

        if (rong(moTa)) {
            return true;
        }

        return kiemTraDoDai(moTa, 1000)
                && REGEX_MOTA.matcher(moTa).matches();
    }


    // =========================
    // 6. ĐƠN GIÁ
    // DECIMAL(18,2) NOT NULL
    // Không được âm
    // =========================

    // Kiểm tra khi lấy dữ liệu từ JTextField
    public static boolean isValidDonGia(String donGia) {

        if (rong(donGia)) {
            return false;
        }

        String gia = donGia.trim();

        if (!REGEX_DONGIA.matcher(gia).matches()) {
            return false;
        }

        try {
            return isValidDonGia(new BigDecimal(gia));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Kiểm tra chuỗi tạm thời trong lúc người dùng đang nhập đơn giá.
    // Cho phép chuỗi rỗng và phần thập phân chưa hoàn chỉnh (ví dụ: 1200.).
    public static boolean isValidDonGiaInput(String donGia) {
        if (donGia == null) {
            return false;
        }

        return donGia.isEmpty()
                || REGEX_DONGIA_INPUT.matcher(donGia).matches();
    }

    // Kiểm tra khi dữ liệu đã chuyển sang BigDecimal
    public static boolean isValidDonGia(BigDecimal donGia) {

        if (donGia == null || donGia.signum() < 0) {
            return false;
        }

        BigDecimal gia = donGia.stripTrailingZeros();

        // DECIMAL(18,2):
        // Tối đa 16 chữ số phần nguyên
        // Tối đa 2 chữ số phần thập phân
        return gia.scale() <= 2
                && gia.precision() - gia.scale() <= 16;
    }


    // =========================
    // 7. TRẠNG THÁI DỊCH VỤ
    // NVARCHAR(50) NOT NULL
    // =========================

    public static boolean isValidTrangThai(String trangThai) {

        if (rong(trangThai)) {
            return false;
        }

        if (!kiemTraDoDai(trangThai, 50)) {
            return false;
        }

        try {
            trangthai_dichvu.valueOf(trangThai.trim());
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}