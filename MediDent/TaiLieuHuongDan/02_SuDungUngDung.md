# 02 - Sử dụng ứng dụng

Hướng dẫn thao tác trên từng màn hình của phân hệ **MediDent - Lễ tân**.
Ứng dụng chia thành các mục trên thanh điều hướng bên trái: **Trang chủ**, **Bệnh nhân**,
**Lịch hẹn**, **Khám bệnh**, **Thanh toán & hóa đơn**.

---

## 1. Đăng nhập

1. Mở ứng dụng bằng `run.bat`.
2. Nhập tên đăng nhập và mật khẩu (`letan` / `123456`).
3. Bấm **Đăng nhập** hoặc nhấn **Enter**.

Lưu ý:
- Ô trống hoặc sai định dạng sẽ được **focus** và báo lỗi ngay dưới ô nhập.
- Sai mật khẩu: *Tên đăng nhập hoặc mật khẩu không đúng.*
- Tài khoản quản trị: *Tài khoản này không thuộc phân hệ Lễ tân.*

---

## 2. Trang chủ

Trang chủ là màn hình **chỉ để xem**:

- 4 thẻ thống kê: tổng bệnh nhân, chờ khám, hoàn thành, lịch hẹn hôm nay.
- Bảng phiếu khám trong ngày: **không sửa trực tiếp** trên bảng.
- Ô tìm kiếm + nút **Tìm kiếm** (hoặc **Enter**).

> Trang chủ **không còn chức năng tiếp nhận bệnh nhân**. Các thao tác Bắt đầu / Hoàn thành /
> Hủy / In phiếu nằm ở màn **Khám bệnh**; tiếp nhận và cấp số thứ tự thực hiện qua
> **Check-in / Cấp số** ở màn **Lịch hẹn**.

---

## 3. Bệnh nhân

### Tìm kiếm

1. Nhập họ tên / số điện thoại / CCCD / email.
2. Bấm **Tìm kiếm** hoặc nhấn **Enter**.

### Thêm / sửa hồ sơ

1. Bấm **Thêm bệnh nhân** hoặc chọn một dòng rồi bấm **Sửa**.
2. Nhập thông tin. Các trường **bắt buộc**: Họ tên, Ngày sinh, Số điện thoại.
3. **Ngày sinh** bấm biểu tượng lịch để chọn, không gõ tay.
4. Khối **Tiền sử bệnh** là vùng lớn (6 dòng) để nhập đầy đủ; dị ứng và ghi chú ở bên dưới.
5. Bấm **Lưu**.

Lỗi nhập liệu:
- Ô bắt buộc để trống → viền đỏ, focus vào ô đó, thông báo riêng.
- Số điện thoại / CCCD / ngày sinh sai định dạng → thông báo riêng cho từng loại lỗi.

### Ngừng / kích hoạt hồ sơ

Chọn bệnh nhân → **Ngừng hoạt động** hoặc **Kích hoạt lại**.
Hồ sơ ngừng hoạt động không bị xóa, vẫn tra cứu được lịch sử.

### Lịch sử khám

Chọn bệnh nhân → **Lịch sử khám**:
- Số lượt khám, ngày gần nhất, tổng chi tiêu.
- Danh sách phiếu khám; chọn phiếu để xem kết quả khám và hóa đơn.
- **In phiếu khám** hoặc **Xuất Word** phiếu đang chọn.

---

## 4. Lịch hẹn

### Tạo lịch hẹn

1. Bấm **Thêm lịch hẹn**.
2. Chọn bệnh nhân, bác sĩ, phòng.
3. Chọn **Ngày hẹn** bằng lịch và **Giờ hẹn** bằng combobox (07:00–19:30, mỗi 30 phút).
4. Nhập ghi chú nếu cần, bấm **Lưu**.

Hệ thống kiểm tra:
- Ngày/giờ hẹn phải ở **tương lai**.
- Không trùng khung giờ của cùng bác sĩ (cách nhau tối thiểu 30 phút).

### Sửa lịch hẹn

- Chọn lịch hẹn → **Sửa**.
- **Không được đổi bệnh nhân** khi sửa (ô bệnh nhân bị khóa).
- Có thể đổi bác sĩ, phòng, ngày, giờ, ghi chú.

### Xác nhận / Hủy

- Chọn lịch hẹn → **Xác nhận**.
- **Hủy**: bắt buộc nhập lý do. Lịch đã hoàn thành thì **không còn nhập lý do hủy**.

### Check-in / Cấp số

1. Chọn lịch hẹn đã xác nhận.
2. Bấm **Check-in / Cấp số**.
3. Chọn phòng khám.
4. Chọn **các dịch vụ sẽ thực hiện** (giữ Ctrl để chọn nhiều) — danh sách này được nạp sẵn vào hóa đơn sau khi khám.
5. Hệ thống tạo phiếu khám, chuyển lịch hẹn sang **Đã đến**.

### Tìm kiếm

Nhập từ khóa, chọn trạng thái / khoảng ngày, bấm **Tìm kiếm** hoặc **Enter**.

> Màn hình không còn cột "Yêu cầu" và nút "Xử lý yêu cầu"; các thao tác đổi lịch được thực hiện trực tiếp bằng **Sửa**.

---

## 5. Khám bệnh

1. Chọn phiếu trong danh sách (tìm bằng nút **Tìm kiếm** hoặc **Enter**).
2. Xem hồ sơ bệnh nhân: dị ứng, tiền sử bệnh, thông tin phiếu.
3. Hồ sơ kết quả khám **chỉ đọc** — lễ tân không nhập/sửa nội dung khám.
4. Các nút thao tác:
   - **Bắt đầu khám**: chuyển phiếu sang *Đang khám*.
   - **Hoàn thành**: xác nhận và chuyển phiếu sang *Hoàn thành*; lịch hẹn liên quan tự cập nhật.
   - **In phiếu khám**.
   - **Xuất Word**: lưu phiếu khám ra file `.doc`.

   > Màn hình này **không lập hóa đơn**. Việc lập hóa đơn thực hiện ở mục **Thanh toán & hóa đơn**.

---

## 6. Thanh toán & hóa đơn

### Lập hóa đơn

1. Bấm **Lập hóa đơn**.
2. Lúc mới mở chưa hiện thông tin. Chọn **Phiếu khám** ở ô phía trên (chỉ hiện phiếu **đã hoàn thành khám** và chưa có hóa đơn; có ô lọc theo tên bệnh nhân / mã phiếu, gõ không dấu cũng được) thì thông tin bệnh nhân và dịch vụ mới hiện ra.
3. Danh sách dịch vụ **đã chọn khi tạo phiếu / check-in** được nạp sẵn, không thêm dịch vụ mới ở bước này.
   - Mỗi dịch vụ là **một dòng**, không nhập số lượng.
   - Chọn dòng → **Xóa dòng** nếu muốn bỏ; **Khôi phục dịch vụ** để lấy lại đủ danh sách của phiếu.
4. Chọn **Mã giảm giá** từ combobox (danh mục do quản lý tạo): *Không giảm giá*, giảm %, hoặc giảm số tiền.
5. Nhập ghi chú, kiểm tra Tạm tính / Tiền giảm / Tổng cộng.
6. Bấm **Lập hóa đơn**. Hóa đơn mới được chọn sẵn trong danh sách và app hỏi có **thu tiền ngay** không.

> Chỉ lập hóa đơn khi bệnh nhân đã hoàn thành khám (phiếu *Hoàn thành*). Phiếu chưa có dịch vụ thì chưa lập được hóa đơn.

### Sửa hóa đơn

- Chỉ sửa được hóa đơn **chưa thanh toán**.
- Khi sửa: **chỉ đổi mã giảm giá và ghi chú**; danh sách dịch vụ bị khóa để tránh sai lệch.

### Thu tiền

1. Chọn hóa đơn chưa thanh toán → **Thu tiền**.
2. Chọn phương thức: Tiền mặt / Chuyển khoản / Thẻ.
3. Nhập **tiền khách đưa** — ô chỉ nhận chữ số, không nhập chữ.
4. Hệ thống tự tính **tiền thừa**.
5. Bấm **Xác nhận thanh toán**.

### Hủy hóa đơn

- Chọn hóa đơn **chưa thanh toán** → **Hủy** → nhập lý do.
- Hóa đơn **đã thanh toán/hoàn thành không còn nhập lý do hủy**.

### In / Xuất Word

- **In hóa đơn** / **Xuất Word** chỉ khả dụng khi hóa đơn **đã thanh toán**.
- Hóa đơn chưa thanh toán sẽ nhận thông báo chặn in.
- **Xuất danh sách (Word)** xuất toàn bộ danh sách hóa đơn đang hiển thị, kể cả chưa thanh toán.

### Lọc danh sách

Nhập từ khóa, chọn trạng thái và khoảng ngày → bấm **Tìm kiếm** hoặc **Enter**.

---

## 7. Xuất file Word

Các nút **Xuất Word** tạo file `.doc` mở được bằng Microsoft Word:

| Màn hình | Nội dung xuất |
|---|---|
| Khám bệnh | Phiếu khám (kèm kết quả khám nếu có) |
| Lịch sử khám | Phiếu khám đang chọn |
| Thanh toán & hóa đơn | Hóa đơn đã thanh toán, hoặc danh sách hóa đơn |

Sau khi lưu, ứng dụng hỏi có mở file bằng Word ngay không.

---

## 8. Mẹo sử dụng

- **Enter** ở ô tìm kiếm thực hiện tìm kiếm, tương đương bấm nút **Tìm kiếm**.
- Mọi bảng căn giữa ô; cột đầu (STT) thẳng hàng giữa các dòng.
- Thông báo lỗi xuất hiện ngay tại ô sai, giúp sửa nhanh.
- Dùng **Làm mới** khi muốn chắc chắn dữ liệu mới nhất từ CSDL.
