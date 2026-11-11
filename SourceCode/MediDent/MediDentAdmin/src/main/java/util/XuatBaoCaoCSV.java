package util;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
/** Ghi dữ liệu báo cáo JTable thành CSV UTF-8 để mở bằng Excel. */
public final class XuatBaoCaoCSV {
    private XuatBaoCaoCSV() {}
    public static void xuat(Path tep, String loai, String thoiGian, String[] cot, List<Object[]> danhSach) throws IOException {
        try (java.io.BufferedWriter w = Files.newBufferedWriter(tep, StandardCharsets.UTF_8)) {
            w.write('\ufeff'); w.write(o(loai) + "," + o(thoiGian)); w.newLine();
            for (int j=0; j<cot.length; j++) { if(j>0)w.write(','); w.write(o(cot[j])); } w.newLine();
            for (Object[] dong : danhSach) {
                for (int j=0; j<cot.length; j++) {
                    if(j>0) w.write(',');
                    w.write(o(j < dong.length ? dong[j] : ""));
                }
                w.newLine();
            }
        }
    }
    private static String o(Object value) {
        String s = value == null ? "" : value.toString();
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
