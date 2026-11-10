# 01 - Cách thực hiện (Cài đặt & chạy ứng dụng)

Tài liệu này hướng dẫn từng bước cài đặt môi trường, tạo cơ sở dữ liệu và chạy phân hệ **MediDent - Lễ tân**.

---

## 1. Chuẩn bị môi trường

| Thành phần | Yêu cầu |
|---|---|
| Hệ điều hành | Windows 10/11 (khuyến nghị) |
| JDK | Java 17 trở lên (`java -version` phải chạy được) |
| SQL Server | 2019 / 2022 / 2025, bản Express cũng được |
| Driver JDBC | Đã kèm trong `lib/mssql-jdbc-12.8.1.jre11.jar` |
| Công cụ SQL | `sqlcmd` hoặc SQL Server Management Studio (SSMS) |

Kiểm tra nhanh:

```bat
java -version
sqlcmd -?
```

> Nếu chưa có `sqlcmd`, có thể mở file `SqlData\MediDentLeTan.sql` bằng SSMS và bấm **Execute**.

---

## 2. Tạo cơ sở dữ liệu

Toàn bộ CSDL nằm trong **một file duy nhất**: `SqlData\MediDentLeTan.sql`.
File này sẽ:

1. Tạo database `MediDentLeTan` (nếu chưa có).
2. Tạo toàn bộ bảng, khóa, chỉ mục, ràng buộc.
3. Tạo login SQL Server `medident_app` với mật khẩu `MediDent@123`.
4. Nạp dữ liệu mẫu: phòng khám, bác sĩ, dịch vụ, mã giảm giá, tài khoản, bệnh nhân, lịch hẹn, phiếu khám, hóa đơn.
5. **Có thể chạy lặp lại an toàn** (idempotent) — không mất dữ liệu đang có.

Chạy bằng `sqlcmd` với Windows Authentication:

```bat
sqlcmd -S ".\SQLEXPRESS" -E -C -f 65001 -i SqlData\MediDentLeTan.sql
```

Kết quả mong đợi ở cuối script:

```
Hoàn tất: CSDL MediDentLeTan sẵn sàng. Đăng nhập ứng dụng: letan / 123456
```

Nếu tên instance khác `SQLEXPRESS`, thay bằng tên thực tế, ví dụ `-S "localhost"`.

---

## 3. Kiểm tra cấu hình kết nối

Cấu hình được nhúng trong `src\ConnectDB\KetNoi.java` (không dùng `db.properties`):

```java
jdbc:sqlserver://localhost:1433;databaseName=MediDentLeTan;encrypt=true;trustServerCertificate=true;loginTimeout=10
```

- Tài khoản: `medident_app`
- Mật khẩu: `MediDent@123`

Có thể ghi đè tạm thời bằng biến môi trường mà không sửa mã nguồn:

```bat
set MEDIDENT_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=MediDentLeTan;encrypt=true;trustServerCertificate=true
set MEDIDENT_DB_USER=medident_app
set MEDIDENT_DB_PASSWORD=MediDent@123
```

### Nếu SQL Server dùng cổng động

1. Mở **SQL Server Configuration Manager**.
2. Vào **SQL Server Network Configuration → Protocols → TCP/IP**.
3. Bật TCP/IP, xem tab **IP Addresses** để biết cổng thực tế.
4. Sửa hằng `DB_URL` trong `KetNoi.java` cho đúng cổng.

Kiểm tra kết nối bằng lớp có sẵn:

```bat
java -Dfile.encoding=UTF-8 -cp "build\classes;src;lib\mssql-jdbc-12.8.1.jre11.jar" ConnectDB.KiemTraKetNoi
```

---

## 4. Biên dịch và chạy

### Cách nhanh (Windows)

```bat
build.bat     :: biên dịch toàn bộ mã nguồn vào build\classes
run.bat       :: mở màn hình đăng nhập
test.bat      :: chạy kiểm thử tự động toàn bộ luồng DAO
```

### Cách thủ công

```bat
mkdir build\classes
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -cp lib\mssql-jdbc-12.8.1.jre11.jar -d build\classes @build\sources.txt
java -Dfile.encoding=UTF-8 -cp "build\classes;src;lib\mssql-jdbc-12.8.1.jre11.jar" UI.DangNhap.DangNhapFrame
```

> Luôn có thư mục `src` trong classpath khi chạy, vì ứng dụng đọc ảnh nền đăng nhập từ `src\Resource\Image`.

---

## 5. Đăng nhập

| Tên đăng nhập | Mật khẩu | Vai trò |
|---|---|---|
| `letan` | `123456` | Lễ tân (dùng chính) |
| `letan2` | `123456` | Lễ tân (dùng thử) |
| `quantri` | `123456` | Quản trị — **bị chặn** ở phân hệ Lễ tân |

Phân hệ chỉ cho phép vai trò **Lễ tân**. Đăng nhập bằng `quantri` sẽ nhận thông báo:

```
Tài khoản này không thuộc phân hệ Lễ tân.
```

---

## 6. Kiểm thử tự động

```bat
test.bat
```

`SmokeTest` chạy 55 tình huống trên CSDL thật, gồm: đăng nhập/phân quyền, mã giảm giá,
bệnh nhân, lịch hẹn, check-in, dịch vụ chỉ định, kết quả khám, hóa đơn, thanh toán, hủy,
xuất HTML. Dữ liệu test dùng CCCD `999999999991` và `999999999992` và được tự động dọn sạch.

Kết quả đạt:

```
=== TẤT CẢ 55 KIỂM TRA ĐỀU ĐẠT ===
```

---

## 7. Xử lý sự cố thường gặp

| Hiện tượng | Cách khắc phục |
|---|---|
| `Không kết nối được tới SQL Server` | Kiểm tra service SQL Server, cổng 1433, đúng `DB_URL` |
| `Login failed for user 'medident_app'` | Chạy lại file SQL; kiểm tra `DB_USER` / `DB_PASSWORD` |
| `Cannot open database "MediDentLeTan"` | Chưa chạy `SqlData\MediDentLeTan.sql` |
| Lỗi `1934` về `QUOTED_IDENTIFIER` | Thêm `SET QUOTED_IDENTIFIER ON;` trước câu lệnh |
| Không thấy ảnh nền đăng nhập | Thiếu `src` trong classpath |
| Tiếng Việt lỗi font trên console | Dùng `-Dfile.encoding=UTF-8` và `chcp 65001` |
| Nút "Xuất Word" không mở được file | Máy chưa cài Word/ứng dụng đọc `.doc`; file vẫn được lưu, mở thủ công được |

---

## 8. Cấu trúc bàn giao

```
MediDent/
├─ src/                  # mã nguồn (Entity, Dao, UI, ConnectDB)
├─ SqlData/              # 1 file SQL duy nhất
├─ lib/                  # driver JDBC
├─ test/                 # SmokeTest
├─ TaiLieuHuongDan/      # 3 tài liệu hỗ trợ
├─ build.bat / run.bat / test.bat
├─ README.md
└─ pom.xml
```
