# MEDIDENT – Cấu trúc thư mục theo ảnh phân chia công việc nhóm

**Phạm vi đã triển khai:** Đăng nhập/Đăng ký + 7 màn hình Quản lý (Java Swing, JDBC SQL Server, JDK 17).
**Chưa triển khai:** Vai trò Lễ tân và Quản trị viên; đã tạo sẵn package + class `NhanVien.java` ở mỗi package, chỉ chứa màn hình báo "chưa triển khai" (không ghi SQL).

## Cây thư mục

```text
MediDent/
├── SqlData/                         # SQL 01–13; sử dụng cùng một CSDL MediDent
├── src/
│   ├── UI/
│   │   ├── LeTan/
│   │   │   └── NhanVien.java         # KHUNG, CHƯA TRIỂN KHAI
│   │   ├── QuanTriVien/
│   │   │   └── NhanVien.java         # KHUNG, CHƯA TRIỂN KHAI
│   │   ├── QuanLy/
│   │   │   ├── NhanVien.java         # launcher cho NhanVienPanel
│   │   │   ├── TrangChuPanel.java   # trang chủ SQL
│   │   │   ├── NhanVienPanel.java    # nhân viên SQL
│   │   │   ├── DichVuPanel.java     # dịch vụ SQL
│   │   │   ├── BenhNhanPanel.java   # bệnh nhân SQL
│   │   │   ├── LichHenPanel.java    # lịch hẹn SQL
│   │   │   ├── ThanhToanPanel.java  # thanh toán/hóa đơn SQL
│   │   │   ├── BaoCaoPanel.java     # báo cáo SQL
│   │   │   └── KhungChinh.java       # ghép 7 màn hình bằng CardLayout
│   │   ├── DangNhap/
│   │   │   └── DangNhap.java        # đăng nhập/đăng ký, mở KhungChinh
│   │   └── Chung/                  # UI dùng chung: GiaoDien, BangDuLieuPanel, XemTruoc
│   ├── Dao/
│   │   ├── TruyVanDAO.java             # lệnh JDBC, transaction; không chứa Swing
│   │   └── NghiepVu/               # kiểm tra nghiệp vụ của các phần Quản lý
│   ├── ConnectDB/
│   │   ├── KetNoi.java             # kết nối, kiểm tra CSDL MediDent
│   │   └── CauHinh.java             # đọc src/Resource/db.properties
│   ├── Entity/                    # record dữ liệu; chuẩn bị cho refactor theo Entity
│   └── Resource/
│       ├── Image/                 # icon và ảnh phòng khám
│       └── db.properties          # CHỈ cấu hình mẫu, nhập thông tin máy cá nhân
├── tests/KiemThu/KiemThuNgoaiTuyen.java
├── lib/                            # JDBC SQL Server + BCrypt
├── dist/                           # JAR Java 17
├── pom.xml                         # cấu hình sourceDirectory=src
├── BUILD_JDK17.bat
├── CHAY_MEDIDENT.bat
├── CHAY_TUNG_MAN_HINH.bat
└── KIEM_THU_OFFLINE.bat
```

## Luồng mã nguồn

- `UI.DangNhap.DangNhap` → `Dao.NghiepVu.XacThucService` → `Dao.TruyVanDAO` → `ConnectDB.KetNoi` → **SQL Server MediDent**.
- `UI.QuanLy.KhungChinh` → 7 `JPanel` → `Dao.NghiepVu.*Service` → `Dao.TruyVanDAO` → `ConnectDB.KetNoi`.
- **Không có các chức năng Lễ tân / Quản trị viên thật**. Không gán chúng vào đăng nhập hoặc Sidebar ở giai đoạn này.
- `Entity/*.java` là các record biểu diễn dữ liệu nghiệp vụ được tạo sẵn cho giai đoạn chuẩn hóa DAO; các DAO hiện có đang trả `Map<String,Object>`, chưa sử dụng record. Không nhận vơ rằng đã dùng Entity tại mọi CRUD.

## SQL và cấu hình

1. **Sao lưu CSDL trước.** Dùng các script trong `SqlData` theo tài liệu `KIEM_THU_SQL_THUC_TE.md`, không chạy bừa `10_ChuyenDoi...` trên database đang dùng.
2. Sửa `src/Resource/db.properties`: `db.url`, `db.user`, `db.password`, `register.invite` theo SQL Server của bạn. Tài khoản và mật khẩu chỉ dùng trên máy riêng.
3. `CauHinh` ưu tiên `MEDIDENT_DB_CONFIG` (biến môi trường) hoặc `db.properties` ở gốc nếu có; nếu không, đọc `src/Resource/db.properties`. Vì bảo mật, file này **bị `.gitignore` bỏ qua**: sau clone, sao chép `db.properties.example` thành `src/Resource/db.properties` nếu thiếu.
4. Tệp `src/Resource/db.properties` **KHÔNG được đóng gói vào JAR**. Khi chạy bằng JAR, phải giữ toàn bộ thư mục project.
5. Một database dùng chung: `MediDent`. Mở riêng từng màn hình có thể xem dữ liệu SQL mà không cần đăng nhập; ghi dữ liệu phải đăng nhập để nghiệp vụ kiểm quyền.

## Chạy

- `CHAY_MEDIDENT.bat`: mở `UI.DangNhap.DangNhap`, đăng ký/đăng nhập rồi vào `UI.QuanLy.KhungChinh` có 7 màn hình.
- `CHAY_TUNG_MAN_HINH.bat`: chọn một trong 7 trang, mỗi trang chạy riêng ở chế độ **chỉ đọc**.
- Trong VS Code / Eclipse: chạy `main()` của `UI.QuanLy.NhanVienPanel`, `UI.QuanLy.DichVuPanel`... để xem riêng, hoặc `UI.DangNhap.DangNhap` để vào tổng hợp.
- Biên dịch bằng `BUILD_JDK17.bat`; hoặc Maven (`pom.xml` đã đổi sourceDirectory), không import mặc định như một Maven `src/main/java` cũ.
- Chạy `KIEM_THU_OFFLINE.bat` để kiểm tra quy tắc Java; các bài test này **không thay thế thử nghiệm SQL Server thật**.

## Ghi chú về lớp KetNoi và DAO

`ConnectDB.KetNoi.mo()` là điểm tập trung JDBC; không tạo SQL riêng trong GUI. Các nghiệp vụ thực thi kiểm tra nghiệp vụ và gọi `Dao.TruyVanDAO` để thao tác SQL. Tên `QuanLyDAO` cũ không còn vì đây là bản Quản lý đã tách NghiepVu/DAO từ trước; việc đổi cấu trúc không làm xuất hiện thêm một DAO tổng hợp dài.

## Cảnh báo

- Các màn hình chỉ có thể đọc/ghi SQL thật nếu cấu hình kết nối và CSDL đúng. Trong môi trường đóng gói không có SQL Server của máy bạn, chưa thể khẳng định toàn bộ CRUD chạy end-to-end.
- Các class `UI.LeTan.NhanVien` và `UI.QuanTriVien.NhanVien` chỉ là khung; đừng thuyết trình như đã hoàn thành nghiệp vụ của hai vai trò này.
- Đừng commit mật khẩu `sa` hoặc mã mời đăng ký vào GitHub.


## Cập nhật bản tên class tiếng Việt – lỗi db.properties

- Các class tiếng Anh đã đổi sang tên tiếng Việt không dấu, ví dụ `ConnectDB.CauHinh`, `Dao.TruyVanDAO`, `Dao.NghiepVu.XacThucService`, `UI.QuanLy.KhungChinh`, `UI.Chung.GiaoDien`. Tên `JPanel`, `Service` vẫn được giữ trong một số lớp cho dễ nhận biết loại thành phần; tên nghiệp vụ là tiếng Việt.
- `CauHinh` tìm cấu hình theo thứ tự: `MEDIDENT_DB_CONFIG` (nếu có), `db.properties` ở gốc, `src/Resource/db.properties`, rồi `src/main/resources/db.properties`. Hỗ trợ tìm vị trí project thông qua class/JAR, không phụ thuộc VS Code/Eclipse đặt working directory ở đâu.
- Khi clone từ GitHub và không có file riêng, ứng dụng tự sao chép `db.properties.example` thành `db.properties` ở gốc project (nếu có quyền ghi).
- Nếu `db.properties` tồn tại nhưng `db.password` trống, ứng dụng **báo rõ chưa nhập mật khẩu**, không nói sai là thiếu file.
- Mở `src/Resource/db.properties` hoặc `db.properties` tại gốc (tùy đường dẫn được báo), nhập `db.user` và `db.password` thực tế của SQL Server máy bạn. **Không commit mật khẩu thật lên GitHub.**
- Chạy `ConnectDB.KetNoi.main()` để xem đường dẫn cấu hình và kiểm tra kết nối SQL. Nếu chạy đúng từ ZIP mà vẫn không kết nối, kiểm tra TCP/IP, cổng 1433, SQL Server Authentication và tên database `MediDent`.
- Xem chi tiết các class và lệnh chạy trong `HUONG_DAN_SUA_CAU_HINH_VA_DOI_TEN_CLASS.md`.


## Cập nhật giao diện và ràng buộc (04/11/2026)

Xem `THAY_DOI_GIAO_DIEN_VA_RANG_BUOC.md` để biết file/dòng sửa, quy tắc nhập liệu, thứ tự cột và kết quả kiểm thử. `KIEM_THU_OFFLINE.bat` đã có đầy đủ source test để chạy.
