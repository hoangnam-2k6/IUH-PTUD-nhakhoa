package validation;

import java.time.LocalDate;
import java.util.regex.Pattern;

import enums.gioitinh;
import enums.trangthai_NhanVien;

public class Regex_NhanVien {

    // =========================
    // 1. KHAI BÁO REGEX
    // =========================

    // Mã nhân viên: chữ, số, dấu gạch dưới, dấu gạch ngang
    private static final Pattern REGEX_MANV =
            Pattern.compile("^[A-Za-z0-9_-]{1,20}$");

    // Họ tên: hỗ trợ tiếng Việt có dấu, khoảng trắng, dấu nháy và gạch ngang
    private static final Pattern REGEX_HOTEN =
            Pattern.compile("^[\\p{L}\\p{M}]+(?:[ '\\-][\\p{L}\\p{M}]+)*$");

    // Số điện thoại: số Việt Nam bắt đầu bằng 0 hoặc +84
    private static final Pattern REGEX_SDT =
            Pattern.compile("^(?:0[0-9]{9,10}|\\+84[0-9]{9,10})$");

    // Email
    private static final Pattern REGEX_EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Địa chỉ: chữ tiếng Việt, số và các dấu câu thông dụng
    private static final Pattern REGEX_DIACHI =
            Pattern.compile("^[\\p{L}\\p{M}0-9\\s.,/#'()&-]+$");

    // Chức vụ
    private static final Pattern REGEX_CHUCVU =
            Pattern.compile("^[\\p{L}\\p{M}0-9][\\p{L}\\p{M}0-9 .,/'()&-]*$");

    // Mã tài khoản
    private static final Pattern REGEX_MATK =
            Pattern.compile("^[A-Za-z0-9_-]{1,20}$");


    // =========================
    // 2. HÀM HỖ TRỢ
    // =========================

    private static boolean rong(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean kiemTraDoDai(String value, int maxLength) {
        return value != null && value.trim().length() <= maxLength;
    }

    private static boolean kiemTraEnum(
            Class<? extends Enum<?>> enumClass, String value) {

        try {
            Enum.valueOf(
                enumClass.asSubclass(Enum.class),
                value.trim()
            );
            return true;

        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }
    }


    // =========================
    // 3. MÃ NHÂN VIÊN
    // NVARCHAR(20) NOT NULL
    // =========================

    public static boolean laMaNVHopLe(String maNV) {

        if (rong(maNV)) {
            return false;
        }

        return kiemTraDoDai(maNV, 20)
                && REGEX_MANV.matcher(maNV.trim()).matches();
    }


    // =========================
    // 4. HỌ TÊN
    // NVARCHAR(150) NOT NULL
    // =========================

    public static boolean laHoTenHopLe(String hoTen) {

        if (rong(hoTen)) {
            return false;
        }

        return kiemTraDoDai(hoTen, 150)
                && REGEX_HOTEN.matcher(hoTen.trim()).matches();
    }


    // =========================
    // 5. NGÀY SINH
    // DATE NULL
    // Không dùng Regex
    // =========================

    public static boolean laNgaySinhHopLe(LocalDate ngaySinh) {

        return ngaySinh == null
                || !ngaySinh.isAfter(LocalDate.now());
    }


    // =========================
    // 6. GIỚI TÍNH
    // NVARCHAR(20) NULL
    // Kiểm tra theo Enum
    // =========================

    public static boolean laGioiTinhHopLe(String gioiTinh) {

        if (rong(gioiTinh)) {
            return true;
        }

        return kiemTraEnum(gioitinh.class, gioiTinh.trim());
    }


    // =========================
    // 7. SỐ ĐIỆN THOẠI
    // NVARCHAR(20) NULL
    // =========================

    public static boolean laSDTHopLe(String sdt) {

        if (rong(sdt)) {
            return true;
        }

        return kiemTraDoDai(sdt, 20)
                && REGEX_SDT.matcher(sdt.trim()).matches();
    }


    // =========================
    // 8. EMAIL
    // NVARCHAR(150) NULL
    // =========================

    public static boolean laEmailHopLe(String email) {

        if (rong(email)) {
            return true;
        }

        return kiemTraDoDai(email, 150)
                && REGEX_EMAIL.matcher(email.trim()).matches();
    }


    // =========================
    // 9. ĐỊA CHỈ
    // NVARCHAR(300) NULL
    // =========================

    public static boolean laDiaChiHopLe(String diaChi) {

        if (rong(diaChi)) {
            return true;
        }

        return kiemTraDoDai(diaChi, 300)
                && REGEX_DIACHI.matcher(diaChi.trim()).matches();
    }


    // =========================
    // 10. CHỨC VỤ
    // NVARCHAR(100) NULL
    // =========================

    public static boolean laChucVuHopLe(String chucVu) {

        if (rong(chucVu)) {
            return true;
        }

        return kiemTraDoDai(chucVu, 100)
                && REGEX_CHUCVU.matcher(chucVu.trim()).matches();
    }


    // =========================
    // 11. TRẠNG THÁI NHÂN VIÊN
    // NVARCHAR(50) NULL
    // Kiểm tra theo Enum
    // =========================

    public static boolean laTrangThaiHopLe(String trangThai) {

        if (rong(trangThai)) {
            return true;
        }

        return kiemTraDoDai(trangThai, 50)
                && kiemTraEnum(
                    trangthai_NhanVien.class,
                    trangThai.trim()
                );
    }


    // =========================
    // 12. MÃ TÀI KHOẢN
    // NVARCHAR(20) NULL
    // =========================

    public static boolean laMaTKHopLe(String maTK) {

        if (rong(maTK)) {
            return true;
        }

        return kiemTraDoDai(maTK, 20)
                && REGEX_MATK.matcher(maTK.trim()).matches();
    }
    /** Chỉ kiểm tra chuỗi đang nhập; xác thực cuối dùng các hàm la...HopLe hiện có. */
    public static boolean laSDTDangNhapHopLe(String giaTri) {
        return giaTri != null && giaTri.matches("[0-9]{0,10}");
    }
}