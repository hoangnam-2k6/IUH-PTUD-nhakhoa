**BẢN CUỐI:** Vui lòng đọc [`HUONG_DAN_BAN_CUOI.md`](HUONG_DAN_BAN_CUOI.md) để xem phần đã sửa icon Java2D, logo 1024x884, STT và hướng dẫn cập nhật Eclipse.

**Bắt đầu ở đây:** Đọc [`HUONG_DAN_CHAY_ECLIPSE.md`](HUONG_DAN_CHAY_ECLIPSE.md) để khắc phục lỗi `Cannot nest`, Java 25/21, import Maven và chạy SQL Server từ đầu.

# MEDIDENT — Quản trị viên đồng bộ với sơ đồ ERD

Project **Java Swing + SQL Server**, JDK **21**, Maven. Đây là **bản cập nhật** của `MediDentAdmin`, điều chỉnh theo ảnh ERD người dùng cung cấp. Các giao diện Admin dùng `UI/QuanTriVien/*`, **không sửa hoặc phụ thuộc `DungChung.ManHinhTrangChu` của dự án khác**.

## 1. Cấu trúc

```text
MediDentAdmin/
├─ pom.xml
├─ README.md
├─ TaiLieu/
│  └─ SoDoERD.png                   # Ảnh ERD nguồn để đối chiếu
├─ SQLData/
│  ├─ 01_MediDent.sql               # DDL đủ bảng trong ERD + bảng hỗ trợ Admin
│  └─ 02_KiemTraERD.sql            # Chỉ SELECT kiểm tra bảng, khóa ngoại và dữ liệu
└─ src/main/
   ├─ java/
   │  ├─ Class/
   │  │  ├─ TaiKhoan.java           # Model trình bày tài khoản + tên nhân viên
   │  │  └─ VaiTro.java
   │  ├─ ConnectDB/
   │  │  ├─ KetNoi.java             # Cấu hình JDBC qua db.properties
   │  │  ├─ KhoiTaoAdmin.java      # Tạo TK Admin + NhanVien + QuanTriVien (transaction)
   │  │  └─ KiemTraKetNoi.java     # Kiểm tra kết nối và tên cột ERD
   │  ├─ DAO/
   │  │  └─ KhoDuLieu.java         # PreparedStatement, giao dịch, số liệu SQL thật
   │  └─ UI/
   │     ├─ Chung/
   │     │  ├─ GiaoDien.java        # Theme và component dùng chung
   │     │  ├─ KhungChinh.java      # Header, sidebar, điều hướng theo quyền
   │     │  └─ TrangChuTheoVaiTro.java
   │     ├─ DangNhap/
   │     │  └─ DangNhap.java        # Tự xác thực, lấy quyền từ DB
   │     └─ QuanTriVien/
   │        ├─ TrangChuQuanTriVien.java  # Dashboard riêng của Admin
   │        ├─ QuanLyTaiKhoan.java       # Danh sách, tìm/lọc, thêm/sửa/xóa
   │        ├─ QuanLyPhanQuyen.java      # Vai trò và ma trận quyền
   │        ├─ QuanTriHeThong.java       # Lưu cấu hình chung
   │        └─ BaoCaoThongKe.java        # Doanh thu/lượt khám/tài khoản/nhật ký
   └─ resources/
      ├─ db.properties             # Cấu hình SQL Server (đổi thông tin máy bạn)
      └─ Image/logo_ai.png         # Logo AI cho MEDIDENT
```

## 2. Đối chiếu ERD mới

| Nhóm | Bảng có trong sơ đồ | Liên hệ quan trọng |
|---|---|---|
| Phân quyền & nhân viên | `VaiTro`, `TaiKhoan`, `NhanVien`, `QuanTriVien`, `QuanLy`, `LeTan` | `TaiKhoan.maVaiTro → VaiTro.maVaiTro`, `NhanVien.maTK → TaiKhoan.maTK`, các bảng con có `maNV → NhanVien.maNV` |
| Tiếp nhận/khám | `BenhNhan`, `NguoiGiamHo`, `LichHen`, `LichLamViec`, `PhieuKham` | Bệnh nhân có lịch hẹn, người giám hộ, phiếu khám; nhân viên có lịch làm việc và phiếu khám |
| Dịch vụ | `DichVu`, `BangGiaDichVu`, `DichVuDieuTri`, `ChiTietDieuTri` | Bảng giá → Dịch vụ; Chi tiết điều trị → Phiếu khám và Dịch vụ điều trị |
| Thu chi & báo cáo | `ThanhToan`, `HoaDon`, `YeuCauHuyPhieuThu`, `PhieuDuyetHuyPhieuThu`, `BaoCaoDoanhThu`, `BaoCaoLuotKham` | Thanh toán và hóa đơn gắn phiếu khám; duyệt hủy gắn yêu cầu hủy; báo cáo lưu gắn hóa đơn/phiếu khám |
| Nhật ký | `NhatKyHeThong` | Dùng đúng các cột `maNK, thoiGian, hanhDong, doiTuong, moTa` |

**Các bảng bổ sung không có trong ảnh ERD:** `Quyen`, `VaiTroQuyen`, `CauHinh`. Chúng cần cho tính năng ma trận phân quyền và lưu thiết lập hệ thống ở giao diện. Không dùng `NhatKy` cũ hoặc cột `MatKhauHash`, `HoTen`, `Email` của `TaiKhoan` cũ.

**Lưu ý kiểu dữ liệu:** Ảnh ERD không cung cấp kiểu dữ liệu. File `01_MediDent.sql` sử dụng giả định `INT IDENTITY` cho mã, `DATETIME2`/`DATE`, `NVARCHAR`, `DECIMAL(18,2)` cho số tiền. `ChiTietDieuTri.thanhTien` là cột tính toán `soLuong × donGia`. Đây là mô hình SQL đề xuất từ hình ảnh, **chưa được xác nhận là schema thật đang chạy trên máy bạn**. Nếu bạn đã có file `.sql` hoặc database cũ, phải đối chiếu và di chuyển dữ liệu trước, không chạy chồng một cách tùy ý. Script không xóa bảng hoặc dữ liệu có sẵn và chặn trường hợp phát hiện `TaiKhoan` cũ thiếu cột `matKhau`.

## 3. Luồng dữ liệu đã nối

- Đăng nhập: từ `TaiKhoan.tenDangNhap, matKhau (BCrypt), trangThai, maVaiTro`, `VaiTro.tenVaiTro` và `NhanVien.hoTen`; cập nhật `TaiKhoan.ngayDangNhapCuoi` khi đúng mật khẩu.
- Thêm tài khoản: nhập tên nhân viên, email, mật khẩu, **chọn** vai trò/trạng thái. `maTK`, `maNV` SQL tự sinh; `tenDangNhap` **tự tạo** `NV000001...`. Một transaction tạo `TaiKhoan`, `NhanVien` và subtype (`QuanTriVien`/`QuanLy`/`LeTan` khi áp dụng). Không cần nhập mã hay chức vụ thủ công.
- Sửa tài khoản: cập nhật bảng `TaiKhoan` và `NhanVien` trong một transaction; nếu đổi vai trò, đồng bộ subtype. Xóa tài khoản chỉ thành công khi không có dữ liệu nghiệp vụ cần giữ; trường hợp đã phát sinh nghiệp vụ nên **Tạm khóa**.
- Phân quyền: `VaiTro` + `Quyen` + `VaiTroQuyen`, kiểm tra từ SQL mỗi lần điều hướng, không dựa vào ID vai trò luôn bằng 1.
- Dashboard Admin: số tài khoản, số vai trò, đang hoạt động; số tạo lũy kế 12 tháng; phân bố vai trò và hoạt động từ `NhatKyHeThong`. Không lấy số minh họa từ ảnh.
- Báo cáo Admin: `ThanhToan` **có trạng thái Thành công/Đã thanh toán** tính tiền thực thu; `PhieuKham` tính lượt khám; tỷ lệ quay lại định nghĩa là % bệnh nhân có từ hai phiếu khám trở lên **trong năm đã chọn**. Có báo cáo theo tháng, tài khoản theo vai trò, nhật ký và CSV UTF-8.
- `BaoCaoDoanhThu`/`BaoCaoLuotKham` trong ERD là các bảng **lưu báo cáo (snapshot)**; UI hiện tính báo cáo realtime trực tiếp từ giao dịch/phiếu khám, **chưa có nút ghi snapshot** vào hai bảng đó.

## 4. Cách chạy trên Eclipse / SQL Server

1. Tạo database mới hoặc xác minh database theo ERD. Mở `SQLData/01_MediDent.sql` bằng SSMS, đọc kỹ cảnh báo *kiểu dữ liệu giả định*, rồi chạy (nếu CSDL của bạn đang có dữ liệu thật, sao lưu và đối chiếu trước).
2. Chạy `SQLData/02_KiemTraERD.sql` để kiểm tra danh sách bảng, cột, khóa ngoại và dữ liệu tài khoản.
3. Sửa `src/main/resources/db.properties`: địa chỉ SQL Server, tên database, SQL user/password. Kích hoạt TCP/IP trong SQL Server Configuration Manager, mở đúng port nếu SQL Server của bạn không dùng 1433. **Không đưa mật khẩu thật lên Git**.
4. Trong Eclipse: **xóa tham chiếu project cũ nhưng KHÔNG xóa mã nguồn trên ổ đĩa**, sau đó `File → Import → Maven → Existing Maven Projects`, chọn **đúng thư mục có `pom.xml`** của ZIP này; chờ Maven tải thư viện. Tuyệt đối không khai báo `src` làm Source Folder (chỉ dùng `src/main/java`, `src/main/resources`).
5. Chạy `ConnectDB.KiemTraKetNoi` (Run As Java Application) để đối chiếu schema thực tế.
6. Lần đầu chạy `ConnectDB.KhoiTaoAdmin` trực tiếp trong Eclipse (`Run As > Java Application`). Ứng dụng tự mở form nhập tên đăng nhập, họ tên, email và mật khẩu (không có mật khẩu mặc định). Có thể dùng Console nếu chạy trong terminal tương tác.
7. Chạy `UI.DangNhap.DangNhap` (Run As Java Application) để đăng nhập và điều hướng.

### Màu giao diện dùng chung

- Primary `#0284C7`, hover `#0369A1`.
- Background `#F8FAFC`, trắng `#FFFFFF`, border `#E2E8F0`.
- Text `#0F172A`, muted `#64748B`.
- Thành công `#10B981`, cảnh báo `#F59E0B`, lỗi `#EF4444`.

## 5. Phạm vi chưa triển khai / cần dữ liệu SQL gốc

- Không có môi trường SQL Server của bạn nên **chưa xác nhận** kết nối hoặc chạy giao dịch thực. Mã Java đã kiểm tra biên dịch bằng `javac` với **stub BCrypt chỉ để kiểm tra cú pháp**, stub không nằm trong gói ZIP; khi chạy thật dùng thư viện BCrypt từ Maven.
- Nếu bản cũ của bạn đã chạy `01_MediDent.sql` cũ, **không được chạy trực tiếp** bản mới để tự sửa schema: cột `TaiKhoan.HoTen/Email/MatKhauHash` cũ và `NhatKy` cũ cần migration có kiểm soát. Hãy cung cấp file `.sql` thực tế / DDL tạo bảng để viết migration không mất dữ liệu.
- Khung chung cho nhiều vai trò có sẵn, nhưng chưa phải toàn bộ màn hình nghiệp vụ của Quản lý/Lễ tân; chỉ nhóm Admin được triển khai sâu. 2FA/sao lưu/khóa phiên chỉ lưu các **giá trị cấu hình**; chưa phải dịch vụ bảo mật/sao lưu hoạt động.
- Logo AI nằm ở tài nguyên `Image/logo_ai.png`. Giao diện Java Swing mô phỏng bố cục, không đảm bảo trùng từng pixel với giao diện mẫu HTML trong ảnh.
