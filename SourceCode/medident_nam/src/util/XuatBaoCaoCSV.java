
package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class XuatBaoCaoCSV {

    // Xuất báo cáo thống kê ra file CSV.
    public static void xuat(Path duongDan, String loaiBaoCao,
                            String thoiGian, String[] cot,
                            List<Object[]> duLieu) throws IOException {

        try (BufferedWriter ghi = Files.newBufferedWriter(
                duongDan, StandardCharsets.UTF_8)) {

            // Giúp Excel nhận tiếng Việt có dấu.
            ghi.write('\uFEFF');

            ghi.write("BÁO CÁO THỐNG KÊ MEDIDENT");
            ghi.newLine();

            ghi.write("Loại báo cáo;" + chuanHoa(loaiBaoCao));
            ghi.newLine();

            ghi.write("Thời gian;" + chuanHoa(thoiGian));
            ghi.newLine();
            ghi.newLine();

            // Xuất tiêu đề các cột JTable.
            for (int i = 0; i < cot.length; i++) {
                if (i > 0) ghi.write(";");
                ghi.write(chuanHoa(cot[i]));
            }
            ghi.newLine();

            // Xuất toàn bộ dữ liệu của bảng.
            for (Object[] dong : duLieu) {
                for (int i = 0; i < cot.length; i++) {
                    if (i > 0) ghi.write(";");

                    Object giaTri = i < dong.length ? dong[i] : null;
                    ghi.write(chuanHoa(giaTri));
                }
                ghi.newLine();
            }
        }
    }

    // Chuẩn hóa giá trị để CSV không bị lỗi cột.
    private static String chuanHoa(Object giaTri) {
        if (giaTri == null) return "";

        String chuoi;

        if (giaTri instanceof BigDecimal) {
            chuoi = ((BigDecimal) giaTri).stripTrailingZeros()
                    .toPlainString().replace('.', ',');
        } else {
            chuoi = giaTri.toString();
        }

        // Ngăn Excel thực thi công thức từ dữ liệu văn bản.
        if (giaTri instanceof String && chuoi.trim().matches("^[=+@\\-].*")) {
            chuoi = "'" + chuoi;
        }

        chuoi = chuoi.replace("\"", "\"\"");

        if (chuoi.contains(";") || chuoi.contains("\"")
                || chuoi.contains("\n") || chuoi.contains("\r")) {
            chuoi = "\"" + chuoi + "\"";
        }

        return chuoi;
    }
}
