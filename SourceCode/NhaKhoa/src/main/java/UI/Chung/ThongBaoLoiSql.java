package UI.Chung;

import java.sql.SQLException;
import java.util.Locale;

/** [QL-SCHEMA] Cung cấp lỗi có hướng dẫn trong UI; không thực hiện DDL khi ứng dụng chạy. */
public final class ThongBaoLoiSql {
    private ThongBaoLoiSql() { }

    public static boolean thieuCauTruc(Throwable error) {
        for (Throwable e = error; e != null; e = e.getCause()) {
            if (e instanceof SQLException) {
                int code = ((SQLException) e).getErrorCode();
                if (code == 207 || code == 208) return true;
            }
            String m = e.getMessage();
            if (m == null) continue;
            String lower = m.toLowerCase(Locale.ROOT);
            if (lower.contains("trangthaihoso") || lower.contains("thoiluongphut")
                    || lower.contains("chiphihoatdong") || lower.contains("chưa có bảng chi phí")) {
                return true;
            }
        }
        return false;
    }

    public static String huongDan(Throwable error) {
        if (thieuCauTruc(error)) {
            return "CSDL chưa đồng bộ: sao lưu MediDent, chạy SQLData/01_MediDent.sql trong SSMS, rồi bấm Tải lại.";
        }
        String msg = error == null ? null : error.getMessage();
        return (msg == null || msg.isBlank()) ? "Không tải được dữ liệu. Kiểm tra kết nối rồi thử lại." : msg;
    }
}
