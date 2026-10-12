# MEDIDENT Admin — Hướng dẫn chạy từ đầu trong Eclipse (JDK 21)

## 0. Các file quan trọng

- `pom.xml`: Maven, Java 21, Microsoft SQL Server JDBC và BCrypt.
- `src/main/java`: **Source Folder Java**, chứa mã nguồn và lớp `main`.
- `src/main/resources`: **Source Folder resources**, chứa `db.properties` và `Image/logo_ai.png`.
- `SQLData/01_MediDent.sql`: cấu trúc CSDL đề xuất theo ERD đã cung cấp (có kiểu dữ liệu giả định).
- `SQLData/02_KiemTraERD.sql`: kiểm tra các bảng/quan hệ trong SSMS.

**Không** tạo package `src.main.java.Class`. File `src/main/java/Class/TaiKhoan.java` phải có dòng `package Class;`.

## 1. Chuẩn bị Eclipse + JDK

1. Đảm bảo đã cài JDK **21**, ví dụ `C:\Program Files\Java\jdk-21`.
2. Trong Eclipse: `Window > Preferences > Java > Installed JREs`, chọn **jdk-21**.
3. Trong Eclipse: `Window > Preferences > Java > Compiler`, chỉnh **Compiler compliance level = 21** (không phải 25).
4. Đảm bảo bản Eclipse hỗ trợ Maven (menu `Maven` xuất hiện trong Properties hoặc menu chuột phải).

## 2. XÓA THAM CHIẾU project cũ bị lỗi, KHÔNG xóa code trên đĩa

Lỗi người dùng từng gặp: `Cannot nest '.../src/main/resources' inside '.../src'`.
Nguyên nhân: Eclipse của project cũ đã gán **cả `src`** là Java Source Folder.

Cách an toàn nhất:

1. Trong `Package Explorer`, chuột phải project cũ `MediDentAdmin_MediDentAdmin` > `Delete`.
2. **BỎ DẤU TÍCH** `Delete project contents on disk`; nếu Eclipse hỏi xác nhận, bảo đảm vẫn giữ file trên ổ đĩa. Bạn có thể sao lưu folder cũ trước.
3. Giải nén ZIP mới vào một thư mục đơn giản, ví dụ `D:\JavaProjects\MediDentAdmin`.
4. Kiểm tra `D:\JavaProjects\MediDentAdmin\pom.xml` và `D:\JavaProjects\MediDentAdmin\src\main\java` cùng tồn tại.
5. Eclipse > `File > Import > Maven > Existing Maven Projects` > `Next`.
6. Trong `Root Directory`, chọn **đúng thư mục có `pom.xml`**, ví dụ `D:\JavaProjects\MediDentAdmin` (không chọn `src`).
7. Tích project Maven tìm thấy > `Finish` và chờ Eclipse tải thư viện.
8. Chuột phải project mới > `Maven > Update Project...` > tích `Force Update of Snapshots/Releases` > `OK`.
9. Chọn `Project > Clean...` > chọn project > `Clean`.

Kiểm tra trong `Properties > Java Build Path > Source`: chỉ nên thấy `src/main/java` và `src/main/resources` (và có thể có `src/test/java` nếu sau này tạo test), **KHÔNG có một mình `src`**. Nếu vẫn thấy `src`, chọn chính `src` > `Remove` (chỉ remove khỏi build path, không xóa file), sau đó `Add Folder...` và chọn 2 đường dẫn chuẩn ở trên.

Trong `Properties > Java Build Path > Libraries` phải có `JRE System Library [JavaSE-21]` và `Maven Dependencies`.
Trong `Properties > Java Compiler`: dùng Java 21.

## 3. Chuẩn bị SQL Server

1. Mở Microsoft SQL Server Management Studio (SSMS), kết nối SQL Server.
2. **Nếu CSDL là mới và chưa chứa dữ liệu thật:** mở `SQLData/01_MediDent.sql`, xem kỹ chú thích về những kiểu dữ liệu được giả định từ sơ đồ ERD, rồi chạy toàn bộ bằng `Execute`.
3. Kiểm tra có database `MediDent`; script chỉ tạo bảng nếu chưa có và **không phải migration** cho database có schema khác.
4. Chạy `SQLData/02_KiemTraERD.sql` để xem bảng, cột, khóa ngoại và dữ liệu.
5. Bật `SQL Server and Windows Authentication mode` nếu định dùng user SQL, ví dụ `sa`. Kiểm tra user đã bật và có quyền với database.
6. Đảm bảo giao thức TCP/IP được bật (SQL Server Configuration Manager) và đúng port. Với instance dùng port khác 1433, sửa URL ở bước 4. Sau khi đổi thiết lập mạng, khởi động lại SQL Server service khi cần.

**Nếu đã có CSDL cũ trên máy:** sao lưu, đối chiếu cấu trúc và **không chạy chồng** script mới lên schema cũ. Bạn cần migration riêng nếu tên cột/kiểu dữ liệu khác với ERD.

## 4. Sửa cấu hình kết nối

Mở: `src/main/resources/db.properties`

```properties
# VD SQL Server tren may ca nhan, port 1433
# TrustServerCertificate chi danh cho MOI TRUONG PHAT TRIEN
# Database duoc tao tu SQLData/01_MediDent.sql
db.url=jdbc:sqlserver://localhost:1433;databaseName=MediDent;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=MAT_KHAU_SQL_SERVER_CUA_BAN
```

Không nhập mật khẩu Windows ở đây nếu bạn dùng xác thực SQL. Không đẩy file cấu hình có mật khẩu thật lên Git hoặc gửi công khai.

## 5. Kiểm tra kết nối trực tiếp trong Eclipse

Trong Package Explorer:
`src/main/java > ConnectDB > KiemTraKetNoi.java` > chuột phải > `Run As > Java Application`.

Nếu kết nối đúng, Console có thông báo `Kết nối SQL Server: THÀNH CÔNG` và danh sách bảng/cột hợp lệ. Nếu gặp lỗi `Login failed`, kiểm tra user/password SQL và authentication mode; `Connection refused`/`TCP/IP` kiểm tra server và port; `Cannot open database` kiểm tra tên DB/quyền.

## 6. Tạo tài khoản Admin đầu tiên

Mở `ConnectDB/KhoiTaoAdmin.java` > `Run As > Java Application`.

Bản sửa này **tự hiện form Swing** trong Eclipse để nhập tên đăng nhập, họ tên, email, mật khẩu từ 8 ký tự. Chỉ cho khởi tạo khi chưa có tài khoản thuộc vai trò Quản trị viên trong DB. Thao tác thêm `TaiKhoan`, `NhanVien`, `QuanTriVien` được thực hiện trong cùng transaction. Không có tài khoản mặc định.

## 7. Chạy giao diện đăng nhập

Mở `src/main/java/UI/DangNhap/DangNhap.java` > `Run As > Java Application`.

Nhập tên đăng nhập và mật khẩu Admin tạo ở bước 6. Đăng nhập truy vấn SQL Server, đọc vai trò và chuyển vào khung UI chung. Sidebar có các trang:

- `Trang chủ` > `UI/QuanTriVien/TrangChuQuanTriVien.java` (Dashboard riêng của Admin)
- `Tài khoản` > `UI/QuanTriVien/QuanLyTaiKhoan.java`
- `Phân quyền` > `UI/QuanTriVien/QuanLyPhanQuyen.java`
- `Hệ thống` > `UI/QuanTriVien/QuanTriHeThong.java`
- `Báo cáo - thống kê` > `UI/QuanTriVien/BaoCaoThongKe.java`

Header/Sidebar thuộc `UI/Chung/KhungChinh.java` — dùng chung, không đặt vào `TrangChuQuanTriVien` và **không sửa `DungChung.ManHinhTrangChu`** của một project khác.

## 8. Kiểm tra bằng Maven (nếu đã cài Maven CLI)

Mở terminal trong chính thư mục chứa `pom.xml`, chạy:

```cmd
mvn -version
mvn clean compile
mvn exec:java -Dexec.mainClass=UI.DangNhap.DangNhap
```

Không cần chạy dòng lệnh nếu bạn đã chạy thành công trong Eclipse. Maven sẽ tải JDBC, BCrypt và các plugin từ Maven Central, cần kết nối mạng lần đầu.

## 9. Những lỗi thường gặp

| Lỗi | Nguyên nhân / cách khắc phục |
|---|---|
| `java.lang.Object/String cannot be resolved` | Java Build Path không có JRE System Library hợp lệ; chọn JDK 21, Clean |
| `selected JRE does not support compiler compliance level of 25` | Đổi Preferences > Java > Compiler và Project > Java Compiler thành 21 |
| `Cannot nest .../src/main/resources inside .../src` | Remove **`src`** khỏi Java Build Path > Source, để `src/main/java` và `src/main/resources`; hoặc xóa **tham chiếu** project cũ rồi import Maven lại |
| `package src.main.java.Class` | Package sai; import bằng Maven đúng root để `Class/TaiKhoan.java` có `package Class;` |
| `The import org.mindrot... cannot be resolved` | Maven chưa tải `jbcrypt`, chạy Maven > Update Project, kiểm tra Internet |
| `No suitable driver` | Maven Dependencies chưa được tải; kiểm tra JDBC |
| `db.properties không tìm thấy` | `src/main/resources` chưa được gán Resources Source Folder hoặc thiếu file |
| `Login failed for user` | Username/password SQL Server sai, account khóa, hoặc chưa bật SQL Authentication |
| `SSL/PKIX/certificate` | Với môi trường dev, URL mẫu có `encrypt=true;trustServerCertificate=true`; không dùng như cấu hình production |
| `Invalid object name` | Sai database trong URL hoặc bảng chưa tạo/không trùng ERD |

## 10. Phạm vi kiểm thử, lưu ý an toàn

- Project kiểm tra cú pháp/biên dịch Java với JDK 21. **Chưa thể xác nhận chạy trực tiếp trên SQL Server cá nhân của bạn**.
- Ảnh ERD chỉ thể hiện tên bảng và liên kết; kiểu dữ liệu SQL trong script do chúng tôi đề xuất, không phải DDL gốc của máy bạn.
- Dashboard và báo cáo lấy SQL thực. Không có dữ liệu sẽ hiển thị 0, không dùng số liệu demo.
- Nhóm Admin đã có UI chức năng; màn hình nghiệp vụ Quản lý/Lễ tân mới là placeholder theo quyền, chưa hoàn chỉnh.
- Tùy chọn 2FA, sao lưu tự động hiện **chỉ lưu cấu hình**, không đồng nghĩa bảo mật 2FA hoặc backup đã được triển khai.
