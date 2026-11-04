package ConnectDB;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
/**
* Doc cau hinh SQL tu file ben ngoai JAR. Khong luu mat khau that trong ma nguon.
* Ho tro chay tu VS Code, Eclipse, JAR va thu muc chua project sau khi clone GitHub.
*/
public final class CauHinh {
    private CauHinh() {
    }
    /** Tim duong dan project tu thu muc dang chay hoac vi tri class/JAR. */
    private static List<Path> thuMucDuAn() {
        Set<Path> danhSach = new LinkedHashSet<>();
        Path hienTai = Paths.get("").toAbsolutePath().normalize();
        timThuMucGoc(hienTai, danhSach);
        try {
            URI uri = CauHinh.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path duongDan = Paths.get(uri).toAbsolutePath().normalize();
            timThuMucGoc(Files.isDirectory(duongDan) ? duongDan : duongDan.getParent(), danhSach);
        }
        catch (Exception ignored) {
            // Neu khong xac dinh duoc JAR, van co duong dan tu thu muc dang chay.
        }
        return new ArrayList<>(danhSach);
    }
    private static void timThuMucGoc(Path batDau, Set<Path> ketQua) {
        for (Path path = batDau; path != null; path = path.getParent()) {
            if (Files.isRegularFile(path.resolve("db.properties.example"))                     || (Files.isRegularFile(path.resolve("pom.xml"))                         && Files.isDirectory(path.resolve("src")))) {
                ketQua.add(path);
            }
        }
    }
    /** Tim file cau hinh, uu tien bien moi truong, goc project, thu muc Resource. */
    private static Path timFile() {
        String chonFile = System.getenv("MEDIDENT_DB_CONFIG");
        if (chonFile != null && !chonFile.isBlank()) {
            Path duongDan = Paths.get(chonFile.trim()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(duongDan)) {
                throw new IllegalStateException("MEDIDENT_DB_CONFIG đang trỏ đến file không tồn tại: " + duongDan);
            }
            return duongDan;
        }
        // Xu ly tung thu muc theo do uu tien: thu muc VS Code hien tai truoc,
        // neu khong thay moi lui ve thu muc chua JAR / class.
        for (Path goc : thuMucDuAn()) {
            for (String ten : new String[]{
                "db.properties", "src/Resource/db.properties", "src/main/resources/db.properties"
            }
            ) {
                Path file = goc.resolve(ten);
                if (Files.isRegularFile(file)) return file;
            }
            // Neu thu muc uu tien co file mau, tao file trong CHINH thu muc do.
            // Khong nhay sang cau hinh cua mot project khac nam trong classpath.
            Path viDu = goc.resolve("db.properties.example");
            if (Files.isRegularFile(viDu)) {
                Path dich = goc.resolve("db.properties");
                try {
                    Files.copy(viDu, dich, StandardCopyOption.COPY_ATTRIBUTES);
                    System.out.println("[MEDIDENT] Đã tạo file cấu hình mẫu: " + dich);
                    System.out.println("[MEDIDENT] Mở file này, nhập mật khẩu SQL và lưu lại trước khi kiểm tra.");
                    return dich;
                }
                catch (IOException ex) {
                    throw new IllegalStateException("Không tạo được db.properties tại: " + dich                             + ". Hãy sao chép db.properties.example thủ công.", ex);
                }
            }
        }
        throw new IllegalStateException("Không tìm thấy db.properties hoặc db.properties.example. "                 + "Hãy mở đúng thư mục MediDent có pom.xml, hoặc thiết lập MEDIDENT_DB_CONFIG.");
    }
    /** Tra ve duong dan file dang duoc dung de de kiem tra khi loi. */
    public static String duongDanDangDoc() {
        if (coDuBienMoiTruong()) return "bien moi truong MEDIDENT_DB_URL / USER / PASSWORD";
        return timFile().toString();
    }
    private static boolean coDuBienMoiTruong() {
        return coBien("MEDIDENT_DB_URL") && coBien("MEDIDENT_DB_USER")                 && coBien("MEDIDENT_DB_PASSWORD");
    }
    private static boolean coBien(String ten) {
        String giaTri = System.getenv(ten);
        return giaTri != null && !giaTri.isBlank();
    }
    /** Doc lai file moi lan kiem tra; khong can restart ung dung sau khi sua file. */
    private static Properties docFile() {
        Path file = timFile();
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Không đọc được file cấu hình: " + file, ex);
        }
        return p;
    }
    /** Giu ten value de cac class nghiep vu cu khong can doi ten method. */
    public static String value(String key) {
        String tenBien = "MEDIDENT_" + key.toUpperCase(Locale.ROOT).replace('.', '_');
        String giaTriMoiTruong = System.getenv(tenBien);
        if (giaTriMoiTruong != null && !giaTriMoiTruong.isBlank()) return giaTriMoiTruong.trim();
        return docFile().getProperty(key, "").trim();
    }
    private static boolean thieu(String giaTri) {
        return giaTri == null || giaTri.isBlank() || giaTri.equalsIgnoreCase("CHANGE_ME")                 || giaTri.equalsIgnoreCase("YOUR_PASSWORD") || giaTri.equalsIgnoreCase("MAT_KHAU_SQL");
    }
    /** Kiem tra tung thuoc tinh; thong bao ro thieu file hay thieu gia tri. */
    public static void require() {
        String nguon = duongDanDangDoc();
        String url = value("db.url");
        String user = value("db.user");
        String password = value("db.password");
        if (thieu(url)) {
            throw new IllegalStateException("Chưa cấu hình db.url trong " + nguon                     + ". Cần URL jdbc:sqlserver://...;databaseName=MediDent.");
        }
        if (!url.startsWith("jdbc:sqlserver://")) {
            throw new IllegalStateException("db.url không phải JDBC SQL Server trong " + nguon);
        }
        if (thieu(user)) {
            throw new IllegalStateException("Chưa nhập db.user trong " + nguon + ". Hãy nhập tài khoản SQL Server.");
        }
        if (thieu(password)) {
            throw new IllegalStateException("Đã tìm thấy db.properties: " + nguon                     + " NHƯNG db.password chưa được nhập (đang để CHANGE_ME hoặc bỏ trống). "                     + "Hãy mở file này, nhập mật khẩu SQL Server và lưu lại.");
        }
    }
}
