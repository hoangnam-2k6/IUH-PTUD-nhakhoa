# MEDIDENT – Dự án tích hợp Admin, Quản lý, điểm nối Lễ tân

**Java Swing · JDK 21 · Maven · JDBC SQL Server 2012 · giao diện một cửa sổ.**

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
│   ├── db.properties          # TÙY MÁY, cần nhập URL/username/password
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

Đã kiểm tra cấu trúc và **biên dịch toàn bộ Java với JDK 21 bằng lớp BCrypt giả lập phục vụ kiểm tra cú pháp** (lớp giả lập **không** có trong ZIP). Đã thử render Dashboard Quản lý bằng Java Swing trên màn hình 1200×780 với DAO giả lập số liệu để kiểm tra giao diện. **Chưa kiểm thử thực tế toàn bộ chức năng trên SQL Server 2012 RTM của bạn.** Cần chạy các ca kiểm tra trong `HUONG_DAN_HOP_NHAT.md` trên máy của nhóm.

---

**BẢN CẬP NHẬT V2 (2026-10-12):** Đọc `HUONG_DAN_CAP_NHAT_V2.md` trước khi chạy. Với DB đã có dữ liệu: **chỉ chạy script SQLData/05** sau khi sao lưu. Admin bị giới hạn chức năng; form có ngày tự động và reset mật khẩu tạm theo ngày sinh chỉ dùng trong demo.
