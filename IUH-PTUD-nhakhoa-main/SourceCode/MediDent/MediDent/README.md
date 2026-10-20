# MEDIDENT – Dự án tích hợp Admin, Quản lý, điểm nối Lễ tân

**Java Swing · JDK 17 · Maven · JDBC SQL Server 2012 · giao diện một cửa sổ.**

## Đóng gói và chạy

Yêu cầu JDK 17 và Maven. Từ thư mục dự án, chạy:

```powershell
mvn clean package
Copy-Item src\main\resources\db.properties.example target\db.properties
java -jar target\medident-technical_test_ver_0.0.2.jar
```

Lệnh package tạo executable JAR có kèm các dependency JDBC và BCrypt.
Với gói phát hành, sao chép `db.properties.example` thành `db.properties` cạnh
file JAR, rồi điền địa chỉ SQL Server, database, username và password. JAR chỉ
đọc cấu hình cạnh chính nó, không đóng gói cấu hình/mật khẩu của máy build.
Đường dẫn được xác định từ vị trí file JAR, không phụ thuộc thư mục hiện hành
hay ổ đĩa cài đặt. Có thể khởi chạy JAR từ bất kỳ thư mục nào bằng đường dẫn
đến file JAR; tệp cấu hình vẫn phải ở cạnh JAR. Không chia sẻ tệp cấu hình có
mật khẩu.

Logo và các ảnh còn lại được đọc từ tài nguyên classpath bên trong JAR.

### Gói Windows `.exe`

Gói portable Windows được tạo bằng `jpackage` trên JDK 17 và đã kèm runtime
Java. Bản ZIP phát hành là
`FileDongGoi\medident-technical_test_ver_0.0.2_windows-x64.zip`. Giải nén toàn
bộ nội dung; không tách riêng file `.exe` khỏi các thư mục `app` và `runtime`.

Trước lần chạy đầu, sao chép
`app\db.properties.example` thành `app\db.properties`, cập nhật thông tin SQL
Server, sau đó mở `medident-technical_test_ver_0.0.2.exe` ở thư mục gốc.
Máy chạy không cần cài JDK, nhưng phải truy cập được SQL Server đã cấu hình.
Gói này dành cho Windows x64.

Đây là bản tích hợp từ project Admin (mã nguồn MEDIDENT đã tạo trước đó) và 10 file giao diện Quản lý do nhóm cung cấp. Mọi màn hình hoạt động thông qua `KhungChinh.java` duy nhất sau khi đăng nhập. Không sử dụng lại `BaseQuanLyGUI.MainWindow` gây mở trùng cửa sổ.

**Đọc `HUONG_DAN_HOP_NHAT.md` để biết thứ tự import, nâng cấp CSDL và kiểm tra quyền.**

## Thư mục

```text
MediDentAdmin/
├── pom.xml
├── README.md
├── HUONG_DAN_HOP_NHAT.md
├── SQLData/
│   ├── 01_MediDent.sql
│   ├── 02_KiemTraERD.sql
│   ├── 03_DuLieuMau_MediDent_SQL2012.sql
│   └── 04_NangCap_Quyen_HoTro_SQL2012.sql
├── src/main/java/
│   ├── Class/                 # TaiKhoan, VaiTro
│   ├── ConnectDB/             # JDBC, khởi tạo Admin, kiểm tra SQL
│   ├── DAO/                   # KhoDuLieu, QuanLyDAO, BaoCaoDuLieuDAO
│   └── UI/
│       ├── Chung/             # KhungChinh, GiaoDien, BieuTuong, HoTroPage, báo cáo chung
│       ├── DangNhap/          # Màn hình đăng nhập dùng chung
│       ├── QuanTriVien/       # Tài khoản, Phân quyền, Hệ thống, Dashboard, Duyệt hủy, Báo cáo
│       ├── QuanLy/            # Dashboard, Bệnh nhân, Nhân viên, Dịch vụ, Lịch hẹn, Thanh toán
│       └── LeTan/             # Điểm nối trang chủ Lễ tân (chưa tích hợp module Lễ tân của nhóm)
├── src/main/resources/
│   ├── db.properties          # Cấu hình riêng khi chạy trong IDE
│   ├── db.properties.example  # Mẫu cấu hình, không chứa thông tin đăng nhập thật
│   └── Image/logo(1024x884).png
└── TaiLieu/
    ├── CodeQuanLyGoc/        # 10 file bạn gửi (lưu tham khảo, KHÔNG compile)
    └── HuongDanCu/           # Hướng dẫn từ các phiên bản trước
```

## Khái quát phân quyền

- Tài khoản được xác thực từ `TaiKhoan`/`VaiTro`/`NhanVien`, mật khẩu dùng BCrypt.
- `KhungChinh` đọc quyền `VaiTroQuyen` mỗi khi chuyển trang.
- `QuanLyDAO` và các phương thức ghi của `KhoDuLieu` kiểm tra quyền hiện hành **trước khi cập nhật**.
- Quyền bổ sung: `NHANVIEN`, `DICHVU`, `HOTRO`. Chạy script `SQLData/04...` một lần trước khi mở UI mới.
- Các form dùng ID SQL ẩn, danh sách hiển thị STT và các lựa chọn khóa ngoại dạng combo.
- Lễ tân chưa có module nghiệp vụ mới; dùng điểm nối `UI.LeTan.TrangChuLeTan` và các menu dựa vào quyền để gắn code của bạn cùng nhóm sau này.

## Tình trạng kiểm thử

Đã biên dịch toàn bộ mã nguồn Java bằng JDK 17 và tạo executable JAR
`medident-technical_test_ver_0.0.2.jar`. Đã kiểm tra JAR nạp được entry point,
các lớp nghiệp vụ, JDBC, BCrypt và tài nguyên; đã kiểm tra đọc cấu hình cạnh
JAR sau khi di chuyển gói sang đường dẫn khác và chạy từ thư mục làm việc khác.
Mỗi máy phải tạo `db.properties` cạnh JAR. **Chưa kiểm thử đăng nhập/chức năng
thực tế trên SQL Server 2012 RTM của bạn.** Cần chạy các ca kiểm tra trong
`HUONG_DAN_HOP_NHAT.md` trên máy của nhóm.

---

**BẢN CẬP NHẬT V2 (2026-10-12):** Đọc `HUONG_DAN_CAP_NHAT_V2.md` trước khi chạy. Với DB đã có dữ liệu: **chỉ chạy script SQLData/05** sau khi sao lưu. Admin bị giới hạn chức năng; form có ngày tự động và reset mật khẩu tạm theo ngày sinh chỉ dùng trong demo.
