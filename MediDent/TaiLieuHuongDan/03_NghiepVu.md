# 03 - Nghiệp vụ phân hệ Lễ tân

Tài liệu mô tả quy trình nghiệp vụ, quy tắc dữ liệu và phân quyền của phân hệ **MediDent - Lễ tân**,
kèm sơ đồ gói (package) và mô hình 2 tầng của mã nguồn.

---

## 1. Phạm vi và vai trò

- Phân hệ phục vụ **quầy lễ tân** của phòng khám nha khoa.
- Chỉ tài khoản vai trò **Lễ tân** được đăng nhập.
- Tài khoản `quantri` vẫn tồn tại trong CSDL (để tham chiếu dữ liệu) nhưng:
  - bị chặn đăng nhập ở tầng DAO;
  - không hiển thị bất kỳ chức năng quản trị nào trong giao diện.

| Vai trò | Đăng nhập | Phạm vi |
|---|---|---|
| Lễ tân | Có | Tiếp nhận, lịch hẹn, khám bệnh, hóa đơn, thanh toán, tra cứu |
| Quản trị | Không | Chỉ tồn tại trong CSDL |

---

## 2. Quy trình nghiệp vụ tổng quát

```
Đặt lịch hẹn
     │
     ▼
Xác nhận lịch hẹn ──► Check-in / Cấp số ──► Phiếu khám (chờ khám)
                                                  │
                                                  ▼
                                            Bắt đầu khám
                                                  │
                                                  ▼
                                          Hoàn thành khám
                                                  │
                                                  ▼
                                          Lập hóa đơn
                                     (dịch vụ chỉ định + mã giảm)
                                                  │
                                                  ▼
                                            Thu tiền
                                                  │
                                                  ▼
                                     In / Xuất Word hóa đơn
```

Khách vãng lai được tạo lịch hẹn rồi **Check-in / Cấp số** tại màn Lịch hẹn;
Trang chủ chỉ hiển thị tổng quan và danh sách phiếu, không tiếp nhận bệnh nhân.

---

## 3. Quy tắc nghiệp vụ chi tiết

### 3.1. Bệnh nhân

| Quy tắc | Mô tả |
|---|---|
| Bắt buộc | Họ tên, Ngày sinh, Số điện thoại |
| Ngày sinh | Chọn từ lịch, không gõ tay; không được ở tương lai |
| Số điện thoại | Chỉ chữ số, độ dài hợp lệ (9–11 số) |
| CCCD | Chỉ chữ số, đúng 12 số nếu có nhập |
| Chống trùng | Không cho trùng CCCD |
| Xóa mềm | "Ngừng hoạt động" thay vì xóa; dữ liệu lịch sử giữ nguyên |
| Tiền sử bệnh | Vùng nhập lớn, hiển thị nổi bật khi tra cứu |
| Dị ứng | Luôn cảnh báo khi mở hồ sơ/khám bệnh |

### 3.2. Lịch hẹn

| Quy tắc | Mô tả |
|---|---|
| Ngày hẹn | Chọn từ lịch |
| Giờ hẹn | Combobox 07:00–19:30, bước 30 phút |
| Thời điểm | Phải ở tương lai |
| Trùng giờ | Cùng bác sĩ phải cách nhau tối thiểu 30 phút |
| Sửa lịch | **Không được đổi bệnh nhân** |
| Hủy | Bắt buộc lý do; lịch đã hoàn thành không còn nhập lý do hủy |
| Trạng thái | Chờ xác nhận → Đã xác nhận → Đã đến → Hoàn thành / Đã hủy |

### 3.3. Phiếu khám

| Quy tắc | Mô tả |
|---|---|
| Cấp số | Mỗi bệnh nhân **một số thứ tự / ngày** |
| Dịch vụ | Chọn khi **check-in** lịch hẹn, lưu vào `PhieuKhamDichVu`; khi lập hóa đơn chỉ được bỏ bớt dòng, không thêm dịch vụ mới |
| Trạng thái | Chờ khám → Đang khám → Hoàn thành / Đã hủy |
| Kết quả khám | Do bác sĩ nhập; lễ tân **chỉ đọc** |
| Hoàn thành | Tự cập nhật lịch hẹn liên quan sang *Hoàn thành* |
| Liên kết lịch hẹn | Tùy chọn (khách vãng lai không có lịch hẹn) |

### 3.4. Hóa đơn

| Quy tắc | Mô tả |
|---|---|
| Dịch vụ | Nạp sẵn từ phiếu khám; có thể thêm/bớt khi **lập mới** |
| Số lượng | Mỗi dòng = 1; **không nhập số lượng** |
| Mã giảm giá | Chỉ **chọn từ danh mục** `MaGiamGia` do quản lý tạo |
| Loại giảm | Theo phần trăm hoặc số tiền; không vượt quá tạm tính |
| Tổng tiền | `TongTien = Tạm tính − Giảm giá` |
| Duy nhất | Mỗi phiếu khám tối đa **một hóa đơn chưa hủy** |
| Sửa | Chỉ hóa đơn **chưa thanh toán**; chỉ đổi mã giảm + ghi chú |
| Hủy | Chỉ hóa đơn chưa thanh toán; bắt buộc lý do |
| Đã thanh toán | Không sửa, không hủy, không nhập lý do hủy |
| In / Word | Chỉ hóa đơn **đã thanh toán** mới được in/xuất lẻ |

### 3.5. Thanh toán

| Quy tắc | Mô tả |
|---|---|
| Phương thức | Tiền mặt / Chuyển khoản / Thẻ |
| Tiền khách đưa | Chỉ nhận chữ số |
| Tiền thừa | `Tiền khách đưa − Tổng tiền`; không âm |
| Thu lại | Không cho thu tiền lần hai |
| Sau thanh toán | Khóa sửa/hủy; mở khóa in hóa đơn |

---

## 4. Trạng thái và chuyển trạng thái

### Lịch hẹn

```
Chờ xác nhận ──► Đã xác nhận ──► Đã đến ──► Hoàn thành
      │               │             │
      └───────────────┴─────────────┴──► Đã hủy
```

### Phiếu khám

```
Chờ khám ──► Đang khám ──► Hoàn thành
    │             │
    └─────────────┴──► Đã hủy
```

### Hóa đơn

```
Chưa thanh toán ──► Đã thanh toán
        │
        └──► Đã hủy
```

---

## 5. Ma trận quyền thao tác (Lễ tân)

| Chức năng | Xem | Thêm | Sửa | Hủy/Xóa | In/Word |
|---|---|---|---|---|---|
| Bệnh nhân | ✔ | ✔ | ✔ | Ngừng hoạt động | — |
| Lịch hẹn | ✔ | ✔ | ✔ (không đổi BN) | ✔ (kèm lý do) | — |
| Phiếu khám | ✔ | ✔ (check-in lịch hẹn) | Trạng thái | ✔ (kèm lý do) | ✔ |
| Kết quả khám | ✔ | — | — | — | ✔ (kèm phiếu) |
| Hóa đơn | ✔ | ✔ | ✔ (hạn chế) | ✔ (chưa TT) | ✔ (đã TT) |
| Thanh toán | ✔ | ✔ | — | — | ✔ |

---

## 6. Quy tắc giao diện

- **Bắt buộc nhập**: ô sai được focus, viền đỏ; thông báo lỗi riêng theo từng loại.
- **Tìm kiếm**: chỉ thực hiện khi bấm **Tìm kiếm** hoặc nhấn **Enter** (không lọc khi đang gõ).
- **Bảng dữ liệu**: mọi ô căn giữa; chữ đầu hàng ở cùng cột thẳng hàng.
- **Ngày**: dùng bộ chọn lịch cho ngày sinh và ngày hẹn.
- **Tiền**: ô nhập tiền chỉ nhận chữ số.
- **Form chỉ đọc**: nền xám, không có nút lưu (kết quả khám, trang chủ).
- **Xuất Word**: nút riêng ở các màn hình có tài liệu in.

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

| Bảng | Khóa chính | Quan hệ chính |
|---|---|---|
| `PhongKham` | `MaPhong` | 1-n `LichHen`, `PhieuKham` |
| `BacSi` | `MaBS` | 1-n `LichHen`, `KetQuaKham` |
| `DichVu` | `MaDV` | 1-n `ChiTietHoaDon`, `PhieuKhamDichVu` |
| `MaGiamGia` | `MaGiam` | 1-n `HoaDon` |
| `TaiKhoan` | `MaTK` | 1-n `HoaDon` (người lập) |
| `BenhNhan` | `MaBN` | 1-n `LichHen`, `PhieuKham` |
| `LichHen` | `MaLH` | 1-n `PhieuKham` |
| `PhieuKham` | `MaPK` | 1-1 `KetQuaKham`; 1-n `ChiTietHoaDon`; n-n `DichVu` |
| `PhieuKhamDichVu` | `(MaPK, MaDV)` | Dịch vụ chỉ định khi check-in |
| `HoaDon` | `MaHD` | 1-n `ChiTietHoaDon` |

Ràng buộc quan trọng:
- Unique index có điều kiện: mỗi `MaPK` chỉ có **một hóa đơn `TrangThai <> 'Đã hủy'`**.
- Unique: mỗi bệnh nhân **một số thứ tự trong ngày**.
- `PhieuKhamDichVu` xóa theo `PhieuKham` (ON DELETE CASCADE).

---

## 8. Cấu trúc gói (package)

```
MediDent
├─ ConnectDB
│   ├─ KetNoi              # Cấu hình JDBC, mở Connection
│   └─ KiemTraKetNoi       # Kiểm tra kết nối
├─ Entity                  # Lớp thực thể (OOP) ánh xạ bảng
│   ├─ BacSi, BenhNhan, ChiTietHoaDon, DichVu, HoaDon,
│   ├─ KetQuaKham, LichHen, MaGiamGia, PhieuKham,
│   └─ PhongKham, TaiKhoan
├─ Dao                     # Truy vấn JDBC cho từng thực thể
│   ├─ BacSiDao, BenhNhanDao, ChiTietHoaDonDao, DichVuDao,
│   ├─ HoaDonDao, KetQuaKhamDao, LichHenDao, MaGiamGiaDao,
│   ├─ PhieuKhamDao, PhongKhamDao, TaiKhoanDao
├─ UI
│   ├─ DangNhap
│   │   └─ DangNhapFrame
│   └─ LeTan
│       ├─ LeTanFrame              # Khung chính + điều hướng
│       ├─ TrangChuLeTan           # Trang chủ (chỉ đọc)
│       ├─ TraCuuThongTin          # Bệnh nhân
│       ├─ BenhNhanDialog          # Thêm/sửa bệnh nhân
│       ├─ LichSuKhamDialog        # Lịch sử khám
│       ├─ QuanLyLichHenLeTan      # Lịch hẹn
│       ├─ LichHenDialog           # Thêm/sửa lịch hẹn
│       ├─ KhamBenhLeTan           # Khám bệnh
│       ├─ ThanhToanHoaDonLeTan    # Thanh toán & hóa đơn
│       ├─ HoaDonDialog            # Lập/sửa hóa đơn
│       ├─ ThanhToanDialog         # Thu tiền
│       ├─ XuatHoaDon              # HTML/Word hóa đơn
│       ├─ XuatPhieuKham           # HTML/Word phiếu khám
│       ├─ XuatWord                # Ghi file .doc
│       └─ GiaoDien                # Helper UI dùng chung
└─ test
    └─ SmokeTest
```

Mô hình **2 tầng**, không có BUS/Service:

```
UI (Swing) ──► DAO (JDBC) ──► SQL Server
```

- UI gọi trực tiếp DAO; DAO mở `Connection` qua `KetNoi`.
- Entity chỉ mang dữ liệu, không chứa SQL.
- Các thao tác nhiều bước (lập hóa đơn, thu tiền, cấp số) bọc trong transaction tại DAO.

---

## 9. Danh mục mẫu

### Mã giảm giá

| Mã | Tên | Loại | Giá trị |
|---|---|---|---|
| `GIAM10` | Khách hàng thân thiết 10% | Phần trăm | 10 |
| `GIAM20` | Ưu đãi 20% | Phần trăm | 20 |
| `GIAM50K` | Giảm 50.000 đ | Số tiền | 50.000 |
| `TRIAN` | Tri ân khách cũ 100.000 đ | Số tiền | 100.000 |

### Dịch vụ (rút gọn)

Khám & tư vấn, Cạo vôi răng, Trám răng, Nhổ răng, Tẩy trắng, Niềng răng, Implant, ...
(xem đầy đủ trong bảng `DichVu` sau khi chạy SQL).

---

## 10. Kiểm soát chất lượng

- `test.bat` chạy 55 kiểm tra trên CSDL thật, bao phủ toàn bộ luồng nghiệp vụ và các trường hợp bị từ chối.
- Dữ liệu test dùng CCCD `999999999991`, `999999999992` và được dọn tự động.
- Script SQL idempotent: chạy lại nhiều lần không lỗi, không mất dữ liệu.
