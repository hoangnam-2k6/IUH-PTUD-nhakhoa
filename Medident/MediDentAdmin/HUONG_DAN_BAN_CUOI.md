# MEDIDENT ADMIN — BẢN CUỐI SỬA ICON / LOGO / STT (Java 21)

## 1. Đã chỉnh những gì?

1. **Toàn bộ icon của UI Admin**: dùng lớp `UI/Chung/BieuTuong.java` để vẽ Java2D, tránh dùng font Unicode. Bao gồm biểu tượng Trang chủ, Tài khoản, Phân quyền, Hệ thống, Báo cáo, Đăng xuất, Đăng nhập, Thêm, Sửa, Xóa, Lưu, Tải lại, Tìm kiếm, Chuông, Dấu tích, Dấu X và Xuất file.
2. **Logo**: `src/main/resources/Image/logo(1024x884).png` (1024 × 884 px). `GiaoDien.logo(rong,cao)` tự co ảnh theo đúng tỷ lệ và căn giữa, không kéo méo. Sidebar đặt 46 × 46; màn Đăng nhập 82 × 72. Có fallback `logo.png`, `logo_ai.png` và logo tự vẽ để không lỗi `URL.toExternalForm`.
3. **STT các bảng**: màn Tài khoản, Phân quyền và Báo cáo có cột `STT` ở đầu, hiển thị 1 → N (kể cả sau khi lọc hoặc bấm sắp xếp theo tên). ID database **không biến thành STT**, chỉ giữ nội bộ để Sửa/Xóa chính xác.
4. **Code rõ hơn**: các lớp màn hình Java được tách phương thức, xuống dòng và chú thích từng nhóm công việc bằng tiếng Việt. `KhoDuLieu.java` và SQL giữ logic truy vấn đang dùng, tránh làm mất kết nối.
5. **Giao diện tự co giãn**: tiếp tục giữ sửa lỗi lệch phải của `BoxLayout`; bảng nhiều cột cuộn ngang trong khung bảng.
6. Có sẵn `SQLData/01_MediDent.sql`, `02_KiemTraERD.sql` và `03_DuLieuMau_MediDent_SQL2012.sql` từ dự án trước. **KHÔNG chạy lại SQL nếu database đã có dữ liệu.**

## 2. Cấu trúc liên quan

```text
MediDentAdmin/
├── pom.xml
├── HUONG_DAN_BAN_CUOI.md
├── SQLData/
│   ├── 01_MediDent.sql
│   ├── 02_KiemTraERD.sql
│   └── 03_DuLieuMau_MediDent_SQL2012.sql
└── src/main/
    ├── java/
    │   ├── Class/                     TaiKhoan, VaiTro
    │   ├── ConnectDB/                 KetNoi, KiemTraKetNoi, KhoiTaoAdmin
    │   ├── DAO/                       KhoDuLieu
    │   └── UI/
    │       ├── Chung/                 BieuTuong, GiaoDien, KhungChinh, TrangChuTheoVaiTro
    │       ├── DangNhap/              DangNhap
    │       └── QuanTriVien/           Dashboard, tài khoản, phân quyền, hệ thống, báo cáo
    └── resources/
        ├── Image/
        │   ├── logo(1024x884).png     Logo chính
        │   └── logo_ai.png            Logo cũ dự phòng
        └── db.properties             Thông số kết nối SQL Server
```

## 3. Cách thay project trong Eclipse

**Cách A — muốn giữ project đang chạy và dữ liệu hiện tại:**

1. Sao lưu project đang dùng, đặc biệt file `src/main/resources/db.properties` đã kết nối SQL Server thành công.
2. Giải nén ZIP mới vào thư mục riêng, ví dụ `D:\JavaProjects\MediDentAdmin`.
3. **Copy lại db.properties từ project đang chạy sang bản mới**, để không bị mất server, cổng, user và mật khẩu.
4. Trong Eclipse, nếu project cũ có cấu hình Source Folder sai: chuột phải project → `Delete` → **BỎ TÍCH** `Delete project contents on disk`. Thao tác này gỡ tham chiếu trong workspace, không xóa file trên ổ đĩa.
5. `File > Import > Maven > Existing Maven Projects`, chọn đúng thư mục có file `pom.xml` của bản mới.
6. `Properties > Java Build Path > Source`: chỉ khai báo `src/main/java` và `src/main/resources`, **không đặt `src` là source folder**.
7. `Properties > Java Compiler`: `21`. `Java Build Path > Libraries`: `JRE System Library [JavaSE-21]`.
8. Chuột phải project → `Maven > Update Project` → `Force Update`, rồi `Project > Clean`.
9. Chạy `ConnectDB.KiemTraKetNoi`, sau đó `UI.DangNhap.DangNhap` (`Run As > Java Application`).

**Cách B — chỉ thay giao diện trong project đang hoạt động:**

- Thay tất cả file `.java` bên dưới `src/main/java/UI/` bằng file tương ứng trong bản mới (có thêm `UI/Chung/BieuTuong.java`).
- Chép `src/main/resources/Image/logo(1024x884).png` vào dự án cũ.
- Không cần sửa SQLData, `DAO/KhoDuLieu.java` hay mật khẩu.
- Chọn `Refresh (F5) > Project Clean` và chạy lại `DangNhap`.

## 4. Cấu hình SQL Server 2012 RTM tại trường

- Project vẫn dùng Maven/Java 21 như bản đang chạy hiện tại; bản giao diện này không thay driver SQL Server hay giao thức TLS.
- **Dùng lại chính xác db.properties đã kết nối được**. Đừng ghi đè nó bằng ví dụ hoặc mật khẩu `CHANGE_ME`.
- SQL Server 2012 RTM hỗ trợ TLS cũ; nếu gặp lỗi TLS 1.0, không được khẳng định đổi `encrypt=false` là sẽ thành công. Cần sử dụng môi trường/driver đã kiểm chứng và chạy thử trên máy trường trước ngày demo.
- Không tự hạ mức bảo mật Java trên máy chính, không chạy script tạo database chồng lên dữ liệu đã có.

## 5. Quy tắc STT trong màn hình

- **STT = vị trí hiển thị (1..N)** sau tìm kiếm, lọc và sắp xếp cột.
- **ID SQL Server = khóa chính** (`maTK`, `maVaiTro`, ...) được lưu trong đối tượng Java, không hiển thị trong STT.
- Nếu bảng có 10 tài khoản thì hiện STT 1..10, dù ID SQL thực sự có thể là 5, 7, 10,...
- `BaoCaoThongKe` xuất cột STT tương ứng với thứ tự trên màn hình vào file CSV.

## 6. Kiểm tra và phạm vi

- Đã biên dịch toàn bộ file Java bằng `javac --release 21` với lớp BCrypt **giả lập CHỈ để kiểm tra cú pháp** (không kèm lớp giả lập trong ZIP).
- Đã vẽ thử 17 biểu tượng Java2D, kiểm tra tải ảnh và STT 1..N ngay cả khi bảng được sắp xếp.
- Chưa thể xác nhận việc kết nối SQL Server thực tế trên máy trường từ môi trường tạo file. Không có thay đổi tới các query, transaction hay cấu trúc database.

### Ghi chú về ảnh logo

Bản ZIP kèm ảnh `logo(1024x884).png` được tạo lại từ logo MediDent có sẵn trong project. Nếu bạn có bản logo gốc độ phân giải 1024 × 884 chất lượng cao hơn, chỉ cần chép đè file ảnh **đúng tên** vào `src/main/resources/Image/`, không phải sửa mã Java.
