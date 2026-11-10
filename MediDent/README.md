# MediDent - Phân hệ Lễ tân

Ứng dụng Java Swing quản lý tiếp nhận, lịch hẹn, khám bệnh và thanh toán của phòng khám nha khoa.
Toàn bộ chức năng đều ghi/đọc trực tiếp với **SQL Server** theo **mô hình 2 tầng**:

```
UI (Swing)  ──►  DAO (JDBC)  ──►  SQL Server
```

- Không có tầng BUS/Service trung gian.
- Mỗi bảng dữ liệu có một lớp Entity (OOP) và một lớp DAO tương ứng.
- Mật khẩu được lưu dưới dạng băm **SHA-256**, không lưu mật khẩu gốc.

---

## 1. Yêu cầu môi trường

| Thành phần | Phiên bản / ghi chú |
|---|---|
| JDK | 17 trở lên |
| SQL Server | 2019/2022/2025 (bản Express cũng được) |
| Driver JDBC | `lib/mssql-jdbc-12.8.1.jre11.jar` (đã kèm sẵn trong dự án) |
| Hệ điều hành | Windows (có thể chạy trên Linux/macOS bằng lệnh `javac`/`java` tương đương) |

> Dự án đã kèm driver JDBC trong thư mục `lib/`, không cần tải thêm.

---

## 2. Cài đặt cơ sở dữ liệu

Toàn bộ CSDL nằm trong **một file SQL duy nhất**: `SqlData/MediDentLeTan.sql`.
File này tạo database `MediDentLeTan`, toàn bộ bảng/index/ràng buộc, dữ liệu mẫu
và tài khoản SQL Server `medident_app`. Script có thể chạy lặp lại an toàn.

Chạy bằng `sqlcmd` (Windows Authentication):

```bat
sqlcmd -S ".\SQLEXPRESS" -E -C -f 65001 -i SqlData\MediDentLeTan.sql
```

Hoặc mở file bằng SQL Server Management Studio (SSMS) và bấm **Execute**.

> **Quan trọng:** script đã bật sẵn `SET QUOTED_IDENTIFIER ON`. Nếu tự chạy lệnh
> INSERT/UPDATE/DELETE bằng `sqlcmd`, hãy bật option này trước, vì các bảng
> `BenhNhan` và `HoaDon` có filtered index yêu cầu `QUOTED_IDENTIFIER ON`.

---

## 3. Cấu hình kết nối

Cấu hình được nhúng trực tiếp trong `src/ConnectDB/KetNoi.java` (không dùng `db.properties`):

```java
public static final String DB_URL =
        "jdbc:sqlserver://localhost:1433;databaseName=MediDentLeTan;"
                + "encrypt=true;trustServerCertificate=true;loginTimeout=10";
public static final String DB_USER = "medident_app";
public static final String DB_PASSWORD = "MediDent@123";
```

Có thể ghi đè khi chạy mà không cần sửa mã nguồn, bằng biến môi trường:

```bat
set MEDIDENT_DB_URL=jdbc:sqlserver://localhost:1433;databaseName=MediDentLeTan;encrypt=true;trustServerCertificate=true
set MEDIDENT_DB_USER=medident_app
set MEDIDENT_DB_PASSWORD=MediDent@123
```

### Lưu ý về cổng kết nối

Cấu hình dùng **TCP cổng cố định 1433** thay cho `instanceName=SQLEXPRESS` để không phụ thuộc
dịch vụ SQL Server Browser (UDP 1434). Nếu instance dùng cổng động:

1. Mở **SQL Server Configuration Manager** → **SQL Server Network Configuration** → **Protocols**.
2. Bật **TCP/IP**, xem tab **IP Addresses** để biết cổng thực tế.
3. Sửa hằng `DB_URL` trong `src/ConnectDB/KetNoi.java`.

Nếu dùng **Windows Authentication**, đặt `MEDIDENT_DB_USER` và `MEDIDENT_DB_PASSWORD`
thành chuỗi rỗng, thêm `integratedSecurity=true` vào `MEDIDENT_DB_URL` và cấu hình
`mssql-jdbc_auth` trong `PATH`/`java.library.path`.

Kiểm tra nhanh kết nối:

```bat
java -Dfile.encoding=UTF-8 -cp "build\classes;src;lib\mssql-jdbc-12.8.1.jre11.jar" ConnectDB.KiemTraKetNoi
```

---

## 4. Biên dịch và chạy

### Cách 1: Dùng script có sẵn (Windows)

```bat
build.bat     :: biên dịch toàn bộ mã nguồn vào build\classes
run.bat       :: mở màn hình đăng nhập
test.bat      :: chạy kiểm thử tự động toàn bộ luồng DAO
```

### Cách 2: Dòng lệnh thủ công

```bat
mkdir build\classes
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -cp lib\mssql-jdbc-12.8.1.jre11.jar -d build\classes @build\sources.txt
java -Dfile.encoding=UTF-8 -cp "build\classes;src;lib\mssql-jdbc-12.8.1.jre11.jar" UI.DangNhap.DangNhapFrame
```

### Cách 3: Maven (nếu máy đã cài Maven)

```bat
mvn clean package
java -cp "target/classes;src;%USERPROFILE%\.m2\repository\com\microsoft\sqlserver\mssql-jdbc\12.8.1.jre11\mssql-jdbc-12.8.1.jre11.jar" UI.DangNhap.DangNhapFrame
```

> `src` phải nằm trong classpath khi chạy vì ứng dụng đọc ảnh nền đăng nhập từ `Resource/Image`.

---

## 5. Tài khoản demo

Mật khẩu của các tài khoản demo là **`123456`**.

| Tên đăng nhập | Mật khẩu | Vai trò | Ghi chú |
|---|---|---|---|
| `letan` | `123456` | Lễ tân | Tài khoản chính |
| `letan2` | `123456` | Lễ tân | Tài khoản thứ hai |
| `quantri` | `123456` | Quản trị viên | Chỉ còn trong dữ liệu mẫu, không đăng nhập vào phân hệ Lễ tân |

Phân hệ này **chỉ dành cho vai trò Lễ tân**. Tài khoản `quantri` được giữ lại trong CSDL để tham chiếu nhưng bị chặn đăng nhập ở tầng DAO; mọi chức năng quản trị (đổi mật khẩu, quản lý tài khoản) đều không hiển thị trong giao diện.

---

## 6. Chức năng

### 6.1. Đăng nhập
- Bố cục hai cột: ảnh phòng khám nha khoa bên trái, biểu mẫu đăng nhập bên phải.
- Đăng nhập bằng tài khoản lưu trong bảng `TaiKhoan`, so khớp hash SHA-256.
- Chỉ tài khoản vai trò **Lễ tân** được vào hệ thống; tài khoản quản trị bị từ chối.
- Kiểm tra dữ liệu nhập, hiển thị lỗi ngay dưới ô mật khẩu.

### 6.2. Trang chủ (`TrangChuLeTan`)
- Thẻ thống kê: tổng bệnh nhân, số chờ khám, số hoàn thành, số lịch hẹn hôm nay.
- Danh sách phiếu khám trong ngày **chỉ để xem**; trang chủ không thực hiện thao tác nghiệp vụ.
- **Không còn chức năng tiếp nhận bệnh nhân** tại Trang chủ.
- Tìm kiếm bằng nút **Tìm kiếm** hoặc phím **Enter**.
- Các thao tác bắt đầu / hoàn thành / hủy phiếu, in phiếu được chuyển sang màn **Khám bệnh**.
- Việc tiếp nhận / cấp số thứ tự được thực hiện qua **Check-in** ở màn **Lịch hẹn**.

### 6.3. Quản lý bệnh nhân (`TraCuuThongTin`)
- Tìm kiếm theo họ tên, số điện thoại, CCCD, email (bấm nút hoặc Enter).
- Lọc theo trạng thái hồ sơ (Hoạt động / Ngừng hoạt động).
- Thêm mới, chỉnh sửa hồ sơ; chống trùng CCCD.
- **Bắt buộc nhập** họ tên, ngày sinh và số điện thoại; ô sai được focus và có thông báo lỗi riêng.
- Ngày sinh chọn từ **lịch** (date picker), không gõ tay.
- Khối **tiền sử bệnh** được phóng to (6 dòng) để dễ đọc; dị ứng và ghi chú nằm bên dưới.
- Ngừng hoạt động / kích hoạt lại hồ sơ.
- Xem **lịch sử khám**: số lượt, ngày khám gần nhất, tổng chi tiêu, chi tiết từng phiếu,
  kết quả khám, hóa đơn; in hoặc xuất phiếu khám ra Word.
- Cảnh báo dị ứng của bệnh nhân.

### 6.4. Quản lý lịch hẹn (`QuanLyLichHenLeTan`)
- Tạo, sửa lịch hẹn; **ngày hẹn chọn từ lịch**, **giờ hẹn chọn từ combobox** (07:00–19:30, bước 30 phút).
- Khi **sửa lịch hẹn không được đổi bệnh nhân** (ô bệnh nhân bị khóa).
- Kiểm tra trùng khung giờ bác sĩ (cách nhau tối thiểu 30 phút) và thời điểm hẹn phải ở tương lai.
- Xác nhận lịch hẹn, hủy lịch kèm lý do.
- **Check-in / Cấp số**: tạo phiếu khám, tự động chuyển lịch hẹn sang *Đã đến*;
  chọn phòng khám và **các dịch vụ sẽ thực hiện** (nạp sẵn vào hóa đơn sau khi khám).
- Lọc theo từ khóa, trạng thái và khoảng ngày; tìm bằng nút **Tìm kiếm** hoặc **Enter**.
- Đã bỏ cột và chức năng "Yêu cầu / Xử lý yêu cầu" để màn hình gọn và đúng nghiệp vụ lễ tân.

### 6.5. Khám bệnh (`KhamBenhLeTan`)
- Danh sách phiếu chờ khám / đang khám / hoàn thành.
- Chuyển phiếu sang **Đang khám**, **Hoàn thành** (khi hoàn thành, lịch hẹn liên quan tự chuyển *Hoàn thành*).
- Hồ sơ kết quả khám **chỉ đọc** (triệu chứng, kết quả lâm sàng, chẩn đoán, hướng điều trị,
  đơn thuốc, ghi chú, ngày tái khám) — lễ tân không nhập liệu kết quả khám.
- Hiển thị cảnh báo dị ứng, tiền sử bệnh của bệnh nhân.
- In phiếu khám, **xuất phiếu khám ra Word**. (Lập hóa đơn thực hiện ở mục Thanh toán & hóa đơn.)
- Tìm kiếm bằng nút **Tìm kiếm** hoặc **Enter**.

### 6.6. Thanh toán & hóa đơn (`ThanhToanHoaDonLeTan`)
- **Lập hóa đơn** chọn phiếu khám ngay trong hộp thoại (chỉ phiếu **đã hoàn thành khám**, có ô lọc; lúc mở chưa chọn phiếu nào), lập xong có thể thu tiền ngay.
- **Dịch vụ đã chọn khi check-in được nạp sẵn** vào hóa đơn.
- Không nhập số lượng: mỗi dịch vụ là một dòng (số lượng = 1), khi lập hóa đơn chỉ xóa/khôi phục dòng, không thêm dịch vụ mới.
- **Mã giảm giá chọn từ combobox** danh mục do quản lý tạo (`MaGiamGia`), hỗ trợ giảm theo % hoặc số tiền.
- Chỉ cho phép **một hóa đơn còn hiệu lực** cho mỗi phiếu khám.
- **Sửa hóa đơn chưa thanh toán**: chỉ đổi mã giảm và ghi chú, danh sách dịch vụ bị khóa.
- **Thu tiền**: tiền mặt / chuyển khoản / thẻ, nhập tiền khách đưa (chỉ nhận chữ số), tự động tính tiền thừa.
- Hủy hóa đơn kèm lý do; **hóa đơn đã hoàn thành/đã thanh toán không còn nhập lý do hủy**.
- Lọc theo từ khóa, trạng thái, khoảng ngày; thẻ thống kê doanh thu.
- **Chỉ hóa đơn đã thanh toán mới được in hoặc xuất Word**; danh sách hóa đơn vẫn xuất được.
- Xuất hóa đơn lẻ và danh sách hóa đơn ra **Word (.doc)**; đọc số tiền thành chữ tiếng Việt.

### 6.7. Khác
- Đăng xuất về màn hình đăng nhập.
- Thông báo lỗi thân thiện, có gợi ý khi mất kết nối CSDL.
- Tài liệu hỗ trợ trong thư mục `TaiLieuHuongDan/`: cách thực hiện, sử dụng ứng dụng và nghiệp vụ.

---

## 7. Mô hình dữ liệu

```
PhongKham ─┐
BacSi ─────┼──► LichHen ──► PhieuKham ──► KetQuaKham
           │                    │
BenhNhan ──┴────────────────────┼──► PhieuKhamDichVu ◄── DichVu
                                │
                                └──► HoaDon ──► ChiTietHoaDon
                                        │
MaGiamGia ──────────────────────────────┤
TaiKhoan ───────────────────────────────┘
```

| Bảng | Ý nghĩa |
|---|---|
| `PhongKham` | Phòng khám, sức chứa, trạng thái |
| `BacSi` | Bác sĩ, chuyên khoa |
| `DichVu` | Bảng giá dịch vụ nha khoa |
| `MaGiamGia` | Danh mục mã giảm giá do quản lý tạo (%, số tiền) |
| `TaiKhoan` | Tài khoản đăng nhập (hash SHA-256) |
| `BenhNhan` | Hồ sơ bệnh nhân |
| `LichHen` | Lịch hẹn trước |
| `PhieuKham` | Phiếu khám trong ngày, số thứ tự |
| `PhieuKhamDichVu` | Dịch vụ chỉ định khi check-in (nạp sẵn vào hóa đơn) |
| `KetQuaKham` | Hồ sơ kết quả khám (1-1 với phiếu khám) |
| `HoaDon` | Hóa đơn, trạng thái, thanh toán, mã giảm giá |
| `ChiTietHoaDon` | Các dòng dịch vụ của hóa đơn |

Quy ước nghiệp vụ:
- `HoaDon.TongTien` = tổng tiền dịch vụ − `GiamGia`.
- `HoaDon.getTamTinh()` = `TongTien + GiamGia`.
- `HoaDon.MaGiam` tham chiếu `MaGiamGia`; tiền giảm được tính từ mã đã chọn.
- Mỗi phiếu khám chỉ có tối đa một hóa đơn chưa hủy (filtered unique index).
- Mỗi bệnh nhân chỉ được cấp một số thứ tự trong một ngày.
- `PhieuKham` liên kết tùy chọn với `LichHen`; khi hoàn thành phiếu, lịch hẹn được cập nhật.
- Mỗi dòng `ChiTietHoaDon` có số lượng = 1; hóa đơn không cho nhập số lượng.

---

## 8. Cấu trúc mã nguồn

```
MediDent/
├─ src/
│  ├─ ConnectDB/          # KetNoi (JDBC), KiemTraKetNoi
│  ├─ Entity/             # Lớp thực thể OOP cho từng bảng
│  ├─ Dao/                # Truy vấn JDBC cho từng bảng
│  ├─ UI/
│  │  ├─ DangNhap/        # DangNhapFrame
│  │  └─ LeTan/           # Các màn hình nghiệp vụ lễ tân
│  └─ Resource/
│     └─ Image/           # Ảnh nền màn hình đăng nhập
├─ SqlData/
│  └─ MediDentLeTan.sql   # File SQL duy nhất: CSDL + dữ liệu mẫu + tài khoản
├─ lib/                   # Driver mssql-jdbc
├─ test/SmokeTest.java    # Kiểm thử tự động toàn bộ luồng DAO
├─ TaiLieuHuongDan/       # 01_CachThucHien.md, 02_SuDungUngDung.md, 03_NghiepVu.md
├─ build.bat / run.bat / test.bat
└─ pom.xml
```

Ví dụ luồng 2 tầng:

```java
// UI gọi thẳng DAO
HoaDonDao dao = new HoaDonDao();
HoaDon hd = dao.findDayDu(maHD);
dao.thanhToan(maHD, HoaDon.TIEN_MAT, tienKhachDua, ghiChu);

// DAO mở kết nối và làm việc với SQL Server
try (Connection cn = KetNoi.getConnection();
     PreparedStatement ps = cn.prepareStatement(sql)) {
    ...
}
```

---

## 9. Kiểm thử

Chạy:

```bat
test.bat
```

`SmokeTest` kiểm tra 55 tình huống: đăng nhập, phân quyền Lễ tân, băm mật khẩu, danh mục,
mã giảm giá, CRUD bệnh nhân, lịch hẹn, check-in, dịch vụ chỉ định, kết quả khám, hoàn thành phiếu,
lập/sửa/thu/hủy hóa đơn, các trường hợp bị từ chối, lọc dữ liệu, xuất HTML và đọc số tiền thành chữ.

Dữ liệu test dùng CCCD `999999999991` và `999999999992`. `SmokeTest` tự động xóa
toàn bộ dữ liệu này khi kết thúc, nên CSDL luôn giữ nguyên dữ liệu mẫu sạch.

---

## 10. Xử lý sự cố thường gặp

| Hiện tượng | Cách khắc phục |
|---|---|
| `Không kết nối được tới SQL Server` | Kiểm tra service SQL Server đang chạy, cổng 1433, đúng `DB_URL` trong `KetNoi.java` |
| `Login failed for user 'medident_app'` | Chạy lại `SqlData/MediDentLeTan.sql`; kiểm tra `DB_USER`/`DB_PASSWORD` trong `KetNoi.java` |
| `Cannot open database "MediDentLeTan"` | Chưa chạy `SqlData/MediDentLeTan.sql` |
| Lỗi `1934` về `QUOTED_IDENTIFIER` | Thêm `SET QUOTED_IDENTIFIER ON;` trước câu lệnh |
| Không thấy ảnh nền đăng nhập | Khi chạy phải có thư mục `src` trong classpath |
| Tiếng Việt bị lỗi font trên console | Dùng `-Dfile.encoding=UTF-8` và `chcp 65001` |

---

## 11. Ghi chú thiết kế

- Ứng dụng dùng CSDL riêng `MediDentLeTan` để không xung đột với CSDL tổng `MediDent`
  của các phân hệ khác trên cùng máy.
- Các thao tác nhiều bước (lập hóa đơn, thu tiền, cấp số thứ tự) được bọc trong transaction
  tại lớp DAO để bảo đảm toàn vẹn dữ liệu.
- Không lưu mật khẩu gốc; không trả mật khẩu/hash ra giao diện.
- Toàn bộ mã nguồn và script CSDL được lưu bằng UTF-8.
