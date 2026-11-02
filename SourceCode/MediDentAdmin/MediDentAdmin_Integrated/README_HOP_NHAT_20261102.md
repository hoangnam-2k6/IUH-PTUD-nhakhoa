# MEDIDENTADMIN – BẢN HỢP NHẤT CÓ CHỌN LỌC (02/11/2026)

## 1. Phạm vi hợp nhất

Bản này ghép các thay đổi nghiệp vụ từ gói `MediDent_Sua_3Loi_DoiLich_PhanCong_ThanhToan_20261102.zip` vào source hiện có, đồng thời giữ lại hai phần đã sửa ổn định ở bản hiện tại:

- `UI/QuanLy/VanHanhPage.java`: ghép phần mới về giữ khóa chính khi sort/filter, tự tải lại khi quay lại màn hình và `bangVuaKhung()`; đồng thời giữ thanh công cụ 2 hàng, ô tìm kiếm riêng và nhóm nút nằm trong thanh cuộn ngang.
- `UI/QuanLy/TrangChuQuanLy.java`: giữ tải Dashboard bằng `SwingWorker`, không truy vấn SQL trong constructor/EDT.
- `SQLData/01_MediDent.sql`: giữ khai báo `BenhNhan.trangThaiHoSo`, `LichHen.thoiLuongPhut` và khả năng tương thích với luồng thanh toán chờ.
- `src/main/resources/db.properties`: giữ cấu hình kết nối của bản source hiện có; kiểm tra và cập nhật theo máy của bạn nếu cần.

Đã lấy từ gói mới: DAO nghiệp vụ mới (dời lịch, phân công bác sĩ, thanh toán, phòng khám, rowversion, lịch sử giá), UI lịch hẹn/tạo lịch/nhân viên/dịch vụ/lễ tân, giới hạn dữ liệu báo cáo theo vai trò, thông báo schema và bộ test kiểm tra đầu vào.

Trong `DAO/QuanLyDAO.java`, đã giữ toàn bộ DAO mới và ghép lại DTO/phương thức `duLieuDashboard()` để tương thích với Dashboard tải nền được giữ lại.

## 2. SQL – cách áp dụng

**Sao lưu database trước. Không chạy migration lần đầu trên database production khi chưa thử restore.**

Với database mới: chạy `SQLData/01_MediDent.sql`, sau đó chạy các migration nền mà dự án yêu cầu (04, 06, 07, 11), rồi chạy. Với database đã tồn tại, không chạy lại toàn bộ script tạo nền một cách máy móc; kiểm tra những migration nào đã áp dụng và chạy các migration còn thiếu sau khi backup:

1. `SQLData/13_NangCap_QuanLyDAO_SQL2012.sql` (có trong bản hiện tại: index lịch/thanh toán và nullable `ngayThanhToan`).
2. `SQLData/13_DanhMucPhong_ThoiLuong_LichHen_SQL2012.sql` (danh mục phòng, thời lượng lịch, rowversion, thời điểm áp dụng giá và CHECK đơn giá).
3. `SQLData/14_DongBo_CSDL_LoiNhapLieu_SQL2012.sql` (đồng bộ thêm `trangThaiHoSo`, rowversion, thời lượng, phòng khám và `soThuTuKham`; các phần đã tồn tại sẽ được bỏ qua).
4. `SQLData/15_KiemTra_Lich_ThanhToan_CHI_DOC_SQL2012.sql` là script chỉ đọc để kiểm tra, không bắt buộc.

Nếu tên database không phải `MediDent`, sửa câu `USE MediDent;` trong từng script trước khi chạy. Các migration có những bước bổ sung lặp có điều kiện; vẫn cần backup và xem kết quả script sau khi chạy.

Lưu ý: `SQLData/01_MediDent.sql` bản hợp nhất giữ `trangThaiHoSo` trong CREATE TABLE và thêm bước ALTER nếu bảng `BenhNhan` cũ thiếu cột. Không thay script này bằng bản SQL 01 từ gói sửa 3 lỗi vì bản đó đã bỏ trường `trangThaiHoSo` và `thoiLuongPhut` khỏi CREATE TABLE.

## 3. Build và chạy

- Dùng JDK 17.
- Mở project Maven bằng Eclipse/IntelliJ hoặc chạy `mvn clean package` khi máy có Maven và các dependency được cấu hình.
- Kiểm tra `db.properties` theo SQL Server trên máy.
- Cập nhật migration cho database test trước khi mở các màn hình Lịch hẹn, Thanh toán, Nhân viên, Dịch vụ.

## 4. Kiểm thử nên thực hiện

- Bệnh nhân: danh sách, thêm/sửa, ngừng và khôi phục hồ sơ; kiểm tra rowversion khi hai máy cùng sửa.
- Nhân viên: thêm/sửa/nghỉ việc, kiểm tra rowversion và lịch/ca tồn đọng.
- Lịch làm việc: thêm/sửa/ngừng ca; thử xung đột ca.
- Lịch hẹn: tạo lịch, dời lịch, phân công/đổi bác sĩ, kiểm tra ca và sức chứa phòng, kiểm tra chuyển trạng thái hợp lệ.
- Thanh toán: tiền mặt, chuyển khoản/thẻ chờ xử lý, xác nhận thực thu, thử tạo trùng cùng phiếu khám.
- Dashboard: mở trang chủ và kiểm tra UI không bị khựng khi SQL phản hồi chậm.
- Toolbar: thu nhỏ cửa sổ và kiểm tra Tìm nhanh, Tải lại, Thêm, Sửa, Ngừng, Khôi phục không bị che.
- Lễ tân: xác minh chỉ hiển thị dữ liệu bệnh nhân cần thiết và danh sách phòng lấy từ SQL.

## 5. Giới hạn xác minh

Bản hợp nhất được tạo từ hai ZIP source. Đã biên dịch 104 tệp Java bằng `javac --release 17`, tạo 167 tệp `.class`; phép biên dịch dùng BCrypt stub chỉ để kiểm tra cú pháp/quan hệ class, không xác minh BCrypt thật. Maven chưa được chạy vì môi trường không có lệnh `mvn`. Chưa thể xác nhận migration chạy thành công hoặc nghiệp vụ đúng trên SQL Server của bạn nếu chưa kết nối database thực tế.
