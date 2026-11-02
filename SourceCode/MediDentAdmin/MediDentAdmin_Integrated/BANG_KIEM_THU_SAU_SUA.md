# BẢNG KIỂM THỬ MEDIDENT SAU KHI SỬA – DÙNG DB THỬ ĐÃ BACKUP

Nguồn: Hai báo cáo QA/BA do nhóm cung cấp, các TC-01 đến TC-25. **Không tự đánh PASS khi chưa có SQL Server thật**. Chỉ các 17 phép thử Java ngoại tuyến trong `src/test/java/KiemTraNhapLieu.java` đã chạy và PASS; các TC phía dưới đều phải kiểm thử tích hợp.

| ID | Nội dung kiểm thử | Trạng thái hiện tại |
|---|---|---|
| TC-01 | CRUD bệnh nhân từ Lễ tân/Quản lý, ngừng hồ sơ và giữ lịch sử | Chưa chạy SQL |
| TC-02 | Mất kết nối sau commit trước phản hồi UI, không INSERT trùng khi thử lại | Chưa chạy SQL |
| TC-03 | Hai máy đồng thời sửa cùng bệnh nhân; bản sau phải báo xung đột rowversion | Chưa chạy 2 máy |
| TC-04 | Ngừng bệnh nhân có lịch/phiếu đang còn hiệu lực | Chưa chạy SQL |
| TC-05 | Hai máy đặt cùng bác sĩ cùng giờ | Chưa chạy 2 máy |
| TC-06 | Hai lịch cùng bác sĩ chồng 09:00–10:00 và 09:30–10:30 | Chưa chạy SQL |
| TC-07 | Tiếp nhận quá sức chứa phòng | Chưa chạy SQL |
| TC-08 | Đặt lịch bác sĩ nghỉ phép/ngừng làm việc | Chưa chạy SQL |
| TC-09 | Chuyển bác sĩ/phòng sau check-in | Chưa chạy SQL |
| TC-10 | Lễ tân gửi hỗ trợ → Quản lý phản hồi → Lễ tân xem | Chưa chạy SQL |
| TC-11 | Lễ tân gửi hủy → Admin xem và duyệt/từ chối | Chưa chạy SQL |
| TC-12 | Hai Admin cùng duyệt một yêu cầu hủy | Chưa chạy 2 máy |
| TC-13 | Thu hồi quyền hỗ trợ lúc Quản lý đang mở form | Chưa chạy 2 máy |
| TC-14 | Mất mạng khi ghi yêu cầu hỗ trợ và audit | Chưa chạy SQL |
| TC-15 | Tiền mặt/chuyển khoản: chỉ xác nhận đã nhận tiền mới ghi đã thu | Chưa chạy SQL |
| TC-16 | Bấm tạo thanh toán hai lần / hai máy thanh toán một phiếu | Chưa chạy 2 máy |
| TC-17 | Hủy hóa đơn chưa thu khác với đã thu | Chưa chạy SQL |
| TC-18 | Hoàn tiền thất bại không được ghi đã hoàn | Chưa triển khai hoàn tiền thật |
| TC-19 | Đối chiếu doanh thu – thực thu – công nợ | Chưa chạy SQL |
| TC-20 | Đăng nhập sai nhiều lần/khóa/mở | Chưa chạy SQL |
| TC-21 | Sao lưu và khôi phục CSDL demo | Chưa thử restore |
| TC-22 | Migration chạy lại không tạo bản ghi trùng | Chưa chạy SQL |
| TC-23 | Maven clean/package từ nguồn mới với thư viện thật | Chưa chạy Maven |
| TC-24 | Màn hình đang mở sau khi Admin thu hồi quyền/khóa tài khoản | Chưa chạy 2 máy |
| TC-25 | Ba vai trò trên chung SQL, refresh liên màn | Chưa chạy 3 tài khoản |

## Các bài thử ngoại tuyến đã chạy

`javac --release 17` thành công (chỉ kiểm tra cú pháp với BCrypt stub **ngoài dự án**, không đóng gói). `java -Djava.awt.headless=true -cp ... KiemTraNhapLieu` đạt **17/17**: email theo chính sách hiện tại, số di động VN, mật khẩu, bộ lọc mã BN/PK và STT.

## Cách ghi kết quả nghiệm thu

Mỗi trường hợp cần ghi ngày kiểm tra, vai trò, dữ liệu đầu vào, các bước bấm, expected, actual, kết quả PASS/FAIL, ảnh và truy vấn `SELECT` chứng minh trạng thái DB. Chạy SQL13 và thao tác ghi trên **bản sao lưu CSDL thử nghiệm**, không thử bằng hồ sơ bệnh nhân thật.
