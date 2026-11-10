package validation;

import java.time.LocalDate;
import java.util.regex.Pattern;

import enums.gioitinh;
import enums.trangthaidieutri;

public class Regex_BenhNhan {

    // =====================================
    // 1. KHAI BÁO REGEX
    // =====================================

    // Mã bệnh nhân: 1-20 ký tự
    private static final Pattern REGEX_MABN =
            Pattern.compile("^[A-Za-z0-9_-]{1,20}$");

    // Họ tên: hỗ trợ tiếng Việt có dấu
    private static final Pattern REGEX_HOTEN =
            Pattern.compile(
                "^[\\p{L}\\p{M}]+(?:[ '\\-][\\p{L}\\p{M}]+)*$"
            );

    // Số điện thoại: bắt đầu bằng 0 hoặc +84
    private static final Pattern REGEX_SDT_INPUT =
            Pattern.compile("^[0-9]{0,20}$");

    private static final Pattern REGEX_SDT =
            Pattern.compile(
                "^(?:0[0-9]{9,10}|\\+84[0-9]{9,10})$"
            );

    // Email
    private static final Pattern REGEX_EMAIL =
            Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    // Địa chỉ
    private static final Pattern REGEX_DIACHI =
            Pattern.compile(
                "^[\\p{L}\\p{M}0-9\\s.,/#'()&-]+$"
            );

    // Tiền sử bệnh: hỗ trợ tiếng Việt, số, dấu câu và xuống dòng
    private static final Pattern REGEX_TIENSUBENH =
            Pattern.compile(
                "^[\\p{L}\\p{M}\\p{N}\\p{P}\\p{S}\\p{Zs}\\r\\n\\t]*$"
            );


    // =====================================
    // 2. HÀM HỖ TRỢ
    // =====================================

    private static boolean rong(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean kiemTraDoDai(
            String value, int maxLength) {

        return value != null
                && value.trim().length() <= maxLength;
    }


    // =====================================
    // 3. MÃ BỆNH NHÂN
    // NVARCHAR(20) NOT NULL
    // =====================================

    public static boolean isValidMaBN(String maBN) {

        if (rong(maBN)) {
            return false;
        }

        return kiemTraDoDai(maBN, 20)
                && REGEX_MABN.matcher(maBN.trim()).matches();
    }


    // =====================================
    // 4. HỌ TÊN
    // NVARCHAR(150) NOT NULL
    // =====================================

    public static boolean isValidHoTen(String hoTen) {

        if (rong(hoTen)) {
            return false;
        }

        return kiemTraDoDai(hoTen, 150)
                && REGEX_HOTEN.matcher(hoTen.trim()).matches();
    }


    // =====================================
    // 5. NGÀY SINH
    // DATE NULL
    // =====================================

    public static boolean isValidNgaySinh(LocalDate ngaySinh) {

        // Không nhập ngày sinh vẫn hợp lệ
        if (ngaySinh == null) {
            return true;
        }

        // Ngày sinh không được ở tương lai
        return !ngaySinh.isAfter(LocalDate.now());
    }


    // =====================================
    // 6. GIỚI TÍNH
    // NVARCHAR(20) NULL
    // =====================================

    public static boolean isValidGioiTinh(String gioiTinh) {

        if (rong(gioiTinh)) {
            return true;
        }

        try {
            gioitinh.valueOf(gioiTinh.trim());
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    // =====================================
    // 7. SỐ ĐIỆN THOẠI
    // NVARCHAR(20) NULL
    // =====================================

    public static boolean isValidSDT(String sdt) {

        if (rong(sdt)) {
            return true;
        }

        return kiemTraDoDai(sdt, 20)
                && REGEX_SDT.matcher(sdt.trim()).matches();
    }

    // Kiểm tra dữ liệu tạm thời khi đang nhập số điện thoại.
    public static boolean isValidSDTInput(String sdt) {
        return sdt != null && REGEX_SDT_INPUT.matcher(sdt).matches();
    }


    // =====================================
    // 8. EMAIL
    // NVARCHAR(150) NULL
    // =====================================

    public static boolean isValidEmail(String email) {

        if (rong(email)) {
            return true;
        }

        return kiemTraDoDai(email, 150)
                && REGEX_EMAIL.matcher(email.trim()).matches();
    }


    // =====================================
    // 9. ĐỊA CHỈ
    // NVARCHAR(300) NULL
    // =====================================

    public static boolean isValidDiaChi(String diaChi) {

        if (rong(diaChi)) {
            return true;
        }

        return kiemTraDoDai(diaChi, 300)
                && REGEX_DIACHI.matcher(diaChi.trim()).matches();
    }


    // =====================================
    // 10. TIỀN SỬ BỆNH
    // NVARCHAR(1000) NULL
    // =====================================

    public static boolean isValidTienSuBenh(String tienSuBenh) {

        if (rong(tienSuBenh)) {
            return true;
        }

        return kiemTraDoDai(tienSuBenh, 1000)
                && REGEX_TIENSUBENH.matcher(tienSuBenh).matches();
    }


    // =====================================
    // 11. TRẠNG THÁI ĐIỀU TRỊ
    // NVARCHAR(50) NULL
    // =====================================

    public static boolean isValidTrangThai(String trangThai) {

        if (rong(trangThai)) {
            return true;
        }

        if (!kiemTraDoDai(trangThai, 50)) {
            return false;
        }

        try {
            trangthaidieutri.valueOf(trangThai.trim());
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}