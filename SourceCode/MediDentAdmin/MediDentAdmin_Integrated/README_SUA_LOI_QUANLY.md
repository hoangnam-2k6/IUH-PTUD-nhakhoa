# MEDIDENT – Bản sửa chức năng Quản lý

## 1. Phạm vi

Bản này tập trung xử lý các lỗi nghiệp vụ của vai trò **Quản lý phòng khám** được ghi nhận trong danh sách QL-01 đến QL-20. Không thay đổi cấu trúc vai trò đăng nhập và không xóa dữ liệu lịch sử.

## 2. Các thay đổi chính

### DAO và nghiệp vụ

- `DAO/QuanLyDAO.java`
  - Kiểm tra trùng bệnh nhân ở mức thông tin khớp đầy đủ: họ tên, số điện thoại, ngày sinh. Không đặt UNIQUE số điện thoại vì các thành viên gia đình có thể dùng chung số.
  - Tách trạng thái hồ sơ bệnh nhân (`trangThaiHoSo`) khỏi trạng thái điều trị; ngừng hồ sơ bằng UPDATE, giữ lịch sử và có phương thức khôi phục.
  - Chặn cho nghỉ việc/tạm nghỉ hoặc đổi vai trò bác sĩ khi còn lịch hẹn hay ca làm việc tương lai chưa xử lý.
  - Xóa dịch vụ được đổi thành ngừng áp dụng; chỉ thêm lịch sử giá khi giá thực sự thay đổi.
  - Lịch hẹn có thời lượng; kiểm tra chồng khoảng giờ của bác sĩ, bệnh nhân và phòng; bắt buộc bác sĩ đang làm việc có ca bao trọn khung hẹn.
  - Bổ sung DAO CRUD ca làm việc, dời lịch, phân công bác sĩ; cập nhật state machine cho trạng thái lịch.
  - Thanh toán tiền mặt chỉ ghi nhận sau xác nhận của người dùng; chuyển khoản/thẻ tạo khoản chờ xác minh, có API xác nhận thực thu bằng mã giao dịch.
  - Không duyệt hủy khoản đã thực thu khi chưa có quy trình hoàn tiền; hủy và ghi nhật ký trong transaction.
  - Gửi yêu cầu hỗ trợ và ghi nhật ký trong cùng transaction.

### Giao diện đã tích hợp

- `UI/QuanLy/LichLamViecGUI.java` (mới): màn hình quản lý ca làm việc.
- `UI/Chung/KhungChinh.java`: thêm menu Lịch làm việc cho tài khoản có quyền NHANVIEN.
- `UI/QuanLy/LichHenGUI.java`: thêm dời lịch, phân công bác sĩ và trạng thái trung gian.
- `UI/QuanLy/TaoLichHenGUI.java`: thêm trường thời lượng lịch hẹn.
- `UI/QuanLy/BenhNhanGUI.java`: đổi thao tác xóa thành ngừng hồ sơ, thêm khôi phục.
- `UI/QuanLy/NhanVienGUI.java`: thao tác nghỉ việc có kiểm tra lịch/ca tồn đọng.
- `UI/QuanLy/DichVuGUI.java`: bắt lỗi đơn giá không hợp lệ và giữ form để nhập lại.
- `UI/QuanLy/ThanhToanGUI.java`: phân biệt thu tiền mặt với khoản chờ xác minh, thêm thao tác xác nhận thực thu.
- `DAO/KhoDuLieu.java`: trạng thái tài khoản đăng nhập không còn tự động thay đổi trạng thái nhân sự.

### SQL Server

- `SQLData/01_MediDent.sql`: khai báo `BenhNhan.trangThaiHoSo` khi tạo database mới; đồng thời có khối `ALTER TABLE` có điều kiện để bổ sung cột này nếu bảng `BenhNhan` đã tồn tại từ schema cũ. Script cũng chuẩn hóa NULL và tạo CHECK constraint nếu chưa có.
- `SQLData/13_NangCap_QuanLyDAO_SQL2012.sql`: migration cho database đã tồn tại:
  - `BenhNhan.trangThaiHoSo`
  - `LichHen.thoiLuongPhut`
  - Cho phép `ThanhToan.ngayThanhToan` NULL với khoản chưa xác minh
  - Index phục vụ truy vấn lịch và thanh toán
  - Unique filtered index cho `ThanhToan.maGiaoDich` nếu dữ liệu cũ không có mã giao dịch trùng

## 3. Cách áp dụng

1. Sao lưu database MediDent và thử trên bản database test trước.
2. Đảm bảo database đã có các bảng nền từ `SQLData/01_MediDent.sql` và các migration nền đang dùng trong project.
3. Chạy `SQLData/01_MediDent.sql` trên database cần sử dụng. Phần `BenhNhan.trangThaiHoSo` trong script có thể chạy lặp lại an toàn: tạo mới thì khai báo cột trong `CREATE TABLE`; nếu bảng đã có thì bổ sung cột bằng `ALTER TABLE` khi còn thiếu.
4. Chạy `SQLData/13_NangCap_QuanLyDAO_SQL2012.sql` để áp dụng các thay đổi schema khác của bản DAO (thời lượng lịch, thanh toán và index). Script 13 vẫn có kiểm tra tồn tại trước khi thêm.
5. Thay các file Java trong project bằng các file cùng đường dẫn trong gói này.
6. Build lại bằng JDK 17 và Maven trên máy phát triển: `mvn clean package`.
7. Chạy thử trên database test với tài khoản thuộc vai trò Quản lý và kiểm tra các luồng trong mục 4.

**Sao lưu database trước khi chạy script trên dữ liệu thật.** Script `01_MediDent.sql` có các phần tạo bảng khi chưa tồn tại và phần cập nhật riêng `BenhNhan.trangThaiHoSo`; nó không thay thế các migration khác nếu database cũ còn thiếu những cột/bảng khác. Nếu database không tên `MediDent`, sửa dòng `USE MediDent;` ở đầu script cho đúng tên database.

## 4. Kiểm thử thủ công tối thiểu

1. Tạo hai lịch cùng bác sĩ có khoảng giờ chồng nhau (ví dụ 09:00–10:00 và 09:30–10:30): lịch thứ hai phải bị từ chối.
2. Tạo lịch ngoài ca làm của bác sĩ: phải bị từ chối.
3. Tạo lịch cùng bệnh nhân hoặc cùng phòng bị chồng giờ: phải bị từ chối.
4. Mở menu Lịch làm việc, thêm/sửa/ngừng ca; thử ngừng ca đang có lịch: phải bị từ chối.
5. Dời lịch chưa bắt đầu; thử dời lịch đã Đang khám/Hoàn tất/Đã hủy: phải bị từ chối.
6. Tiếp nhận lịch chưa có bác sĩ, phân công bác sĩ trước khi chuyển sang Chờ khám/Đang khám.
7. Tạo hồ sơ bệnh nhân trùng hoàn toàn họ tên + SĐT + ngày sinh: phải báo hồ sơ nghi trùng.
8. Ngừng hồ sơ bệnh nhân đang còn lịch chưa kết thúc: phải bị từ chối; ngừng hồ sơ đã đủ điều kiện và khôi phục lại.
9. Khóa tài khoản đăng nhập không tự đổi trạng thái làm việc của nhân viên.
10. Sửa dịch vụ không đổi giá: không tạo dòng lịch sử giá mới; đổi giá: thêm dòng mới; ngừng dịch vụ không xóa chứng từ cũ.
11. Nhập đơn giá không phải số: form phải giữ nguyên và cho nhập lại.
12. Thanh toán tiền mặt: chỉ ghi đã thanh toán sau xác nhận; chuyển khoản/thẻ: tạo chờ xử lý, sau đó nhập mã giao dịch để xác nhận thực thu.
13. Gửi yêu cầu hủy khoản đã thực thu: bị từ chối; khoản chờ xử lý có thể được yêu cầu duyệt hủy.

## 5. Các mục vẫn cần triển khai/duyệt nghiệp vụ riêng

- **QL-01:** Chưa có luồng chuyên môn hoàn chỉnh để bác sĩ tạo/chốt phiếu khám và chi tiết điều trị; cần phân định đúng người được nhập dữ liệu lâm sàng.
- **QL-03:** Source vẫn dùng các phòng mẫu trong form tạo lịch; schema hiện chưa có danh mục phòng và sức chứa. Bản này kiểm tra xung đột cùng phòng, chưa kiểm soát công suất phòng.
- **QL-05:** Có DAO và UI dời lịch cơ bản, chưa có audit riêng before/after theo cấu trúc chuyên dụng.
- **QL-09:** Kiểm tra trùng chính xác theo họ tên + SĐT + ngày sinh; chưa có thuật toán gợi ý hồ sơ gần giống/khác dấu.
- **QL-11:** Chưa thêm rowversion/optimistic locking cho form bệnh nhân và nhân viên.
- **QL-12:** Chưa có quy tắc mapping đã được xác nhận giữa `DichVu` và `DichVuDieuTri`.
- **QL-13:** `BangGiaDichVu.ngayApDung` hiện vẫn là DATE nên chưa ghi được thời điểm đổi giá trong cùng một ngày; lịch sử chỉ được thêm khi giá thay đổi.
- **QL-16:** Xác nhận chuyển khoản/thẻ hiện là thao tác đối chiếu thủ công bằng mã giao dịch; chưa tích hợp ngân hàng/cổng thanh toán.
- **QL-17:** Chưa xây dựng quy trình hoàn tiền. Vì vậy hệ thống chủ động chặn duyệt hủy đối với khoản đã thực thu.
- **QL-18 và QL-20:** Thuộc `BaoCaoDuLieuDAO`/phân hệ báo cáo, không được sửa trong đợt này.
- **QL-19:** `VanHanhPage` tải danh sách ở luồng nền. Trong bản cập nhật tiếp theo, `TrangChuQuanLy` cũng tải KPI và lịch hẹn gần đây bằng `SwingWorker`; DAO `duLieuDashboard()` gom dữ liệu Dashboard thành hai truy vấn trên một kết nối. Một số thao tác CRUD, tải ComboBox/chi tiết form và màn hình thanh toán vẫn còn gọi DAO đồng bộ trên EDT; QL-19 chưa được xử lý triệt để.

## 6. Giới hạn kiểm thử

Toàn bộ mã nguồn Java trong thư mục `src/main/java` đã được biên dịch bằng `javac --release 17`. Khi kiểm tra tại môi trường này, `KhoDuLieu.java` cần chữ ký BCrypt; phép biên dịch dùng BCrypt stub chỉ để kiểm tra cú pháp/kiểu. Chưa thực hiện Maven build với dependency thật, chưa kết nối SQL Server thật và chưa chạy migration trên database của người dùng. Vì vậy kết quả biên dịch không được hiểu là toàn bộ nghiệp vụ đã PASS trên SQL Server.


## 7. Cập nhật tiếp theo – Dashboard Quản lý (QL-19)

- `UI/QuanLy/TrangChuQuanLy.java`: không truy vấn SQL trực tiếp trong constructor; hiển thị trạng thái đang tải, thực hiện tác vụ bằng `SwingWorker` và cập nhật các thẻ KPI/bảng lịch trên EDT khi hoàn tất.
- `DAO/QuanLyDAO.java`: bổ sung `DuLieuDashboard` và `duLieuDashboard()`; một truy vấn lấy 4 KPI, một truy vấn lấy 8 lịch gần đây, dùng chung một kết nối.
- Nếu SQL Server lỗi, trang hiển thị trạng thái lỗi thay vì treo giao diện hoặc hiện số liệu giả.
- Kiểm tra biên dịch ngoại tuyến: `javac --release 17` thành công với BCrypt stub (164 file `.class` sinh ra). Stub chỉ thay chữ ký BCrypt để kiểm tra cú pháp; không xác minh chức năng băm mật khẩu.
- Chưa chạy Maven vì môi trường hiện tại không cài `mvn`; chưa kết nối SQL Server thật để kiểm tra câu truy vấn và dữ liệu.


## 8. Khắc phục danh sách Bệnh nhân không nhận dữ liệu

- `DAO/QuanLyDAO.java`: phương thức `benhNhan()` kiểm tra `COL_LENGTH('dbo.BenhNhan','trangThaiHoSo')` trước khi tạo câu SELECT. Nếu database cũ chưa có cột này, DAO đọc danh sách bằng các cột nền và lấy trạng thái từ `trangThai`; nếu đã có cột, DAO vẫn hiển thị trạng thái hồ sơ riêng. Cả hai nhánh trả về cùng cấu trúc 6 cột cho `BenhNhanGUI`/`VanHanhPage`.
- Nguyên nhân tương thích: `01_MediDent.sql` chỉ tạo bảng khi bảng chưa tồn tại. Chạy lại script tạo mới không tự bổ sung cột vào bảng đã có.
- Phần SQL bổ sung đã được nhúng trực tiếp vào `SQLData/01_MediDent.sql`. Khi chạy script này trên database có sẵn bảng `BenhNhan`, khối `COL_LENGTH` sẽ bổ sung cột còn thiếu và gán `Hoạt động` cho các hồ sơ cũ; không xóa bản ghi bệnh nhân. Không cần chạy file 14 riêng nữa.
- Nếu danh sách vẫn trống sau cập nhật, kiểm tra `db.properties` đang trỏ đúng database và chạy `SELECT DB_NAME(), COUNT(*) FROM dbo.BenhNhan;` trên chính database đó.
- Kiểm tra kỹ thuật: đã biên dịch toàn bộ 103 tệp Java bằng `javac --release 17` với BCrypt stub chỉ để thay chữ ký thư viện trong môi trường không có Maven; chưa kết nối SQL Server thực tế, nên chưa thể xác nhận dữ liệu của database người dùng.


## 9. Hợp nhất DAO bệnh nhân vào DAO chính

- `src/main/java/DAO/QuanLyDAO.java` là file DAO đầy đủ của bản v2, đã hợp nhất phần chỉnh sửa từ `QuanLyDAO_BenhNhan_Fix.java` vào đúng phương thức `benhNhan()`; không thay thế DAO đầy đủ bằng một DAO chỉ chứa nghiệp vụ bệnh nhân.
- `benhNhan()` kiểm tra `COL_LENGTH` để chọn truy vấn tương thích schema cũ/mới. Tuy nhiên, các nghiệp vụ thêm/sửa/ngừng/khôi phục vẫn dùng `trangThaiHoSo`; vì vậy cần chạy `SQLData/01_MediDent.sql` để đồng bộ schema trước khi dùng đầy đủ CRUD bệnh nhân.
- File nguồn riêng `QuanLyDAO_BenhNhan_Fix.java` không được dùng như một class thứ hai trong package. Nội dung đã được hợp nhất vào `QuanLyDAO.java` để tránh trùng class `DAO.QuanLyDAO`.


## Cập nhật bố cục thanh công cụ dùng chung

- `UI/QuanLy/VanHanhPage.java`: tách thanh công cụ thành hai hàng: ô tìm nhanh ở hàng trên, nhóm nút thao tác ở hàng dưới.
- Nhóm nút nằm trong `JScrollPane` chỉ cuộn ngang khi thiếu chiều rộng; không tự xuống hàng và không bị cắt ở đáy toolbar.
- Thay đổi áp dụng cho các màn hình kế thừa `VanHanhPage`: Bệnh nhân, Nhân viên, Dịch vụ, Lịch hẹn, Thanh toán và Lịch làm việc.
