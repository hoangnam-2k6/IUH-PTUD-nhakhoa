# MediDent — sửa lỗi khoảng cách giao diện

## Nguyên nhân

Ở các trang Quản lý tài khoản, Phân quyền, Hệ thống và Báo cáo, panel trang dùng `BoxLayout(Y_AXIS)` nhưng các thành phần (tiêu đề, thanh bộ lọc, bảng...) khác giá trị `alignmentX` và một số panel có chiều cao tối đa quá lớn. Khi không thống nhất căn lề, BoxLayout tính tọa độ khác nhau, tạo khoảng trống bên trái, đẩy tiêu đề và bảng sang bên phải. Ngoài ra, trang không tự co theo chiều ngang của JScrollPane.

## File đã sửa

1. `src/main/java/UI/Chung/GiaoDien.java`:
   - `trang()`: panel cuộn theo chiều dọc, tự bám chiều ngang viewport và căn trái mọi phần tử.
   - `hang()`: căn trái, chiều cao bằng nội dung thay vì có thể phình ra.
   - `card()`: giữ chiều cao phù hợp, chiếm chiều ngang có sẵn.
   - `cuon()`: chỉ cuộn dọc trang.
   - `wrapTable()`: bảng nhiều cột cuộn ngang BÊN TRONG, không làm lệch toàn bộ trang.
2. `src/main/java/UI/QuanTriVien/QuanLyTaiKhoan.java`:
   - Sửa thanh tìm kiếm/lọc/thêm thành `GridBagLayout`; chiều rộng tự co theo cửa sổ.

Màn hình `TrangChuQuanTriVien` vẫn độc lập; không thay đổi `ManHinhTrangChu` của ứng dụng khác. Không chỉnh cấu trúc SQL hay DAO.

## Nếu muốn sửa trực tiếp project Eclipse hiện có

1. Sao lưu project.
2. Trong ZIP, chỉ lấy 2 file Java kể trên, chép đè đúng thư mục của project hiện tại.
3. `Project > Clean... > Clean`.
4. Nếu là Maven Project, chuột phải project > `Maven > Update Project...` > `OK`.
5. Chạy lại `UI.DangNhap.DangNhap` và mở lần lượt `Tài khoản`, `Phân quyền`, `Hệ thống`, `Báo cáo - thống kê`.
6. Không cần chạy lại các file SQL; các tài khoản/dữ liệu hiện tại giữ nguyên.

## Nếu nhập lại toàn bộ dự án

1. Giải nén cả thư mục `MediDentAdmin` sao cho `pom.xml` nằm trực tiếp trong thư mục.
2. Mở Eclipse > `File > Import > Maven > Existing Maven Projects`.
3. Chọn thư mục chứa `pom.xml`. Project cần JDK 21 và 2 thư mục Source: `src/main/java`, `src/main/resources`.
4. Đặt lại `db.properties` theo SQL Server đang chạy; bản đóng gói dùng mật khẩu mẫu `CHANGE_ME`, không lưu mật khẩu thật.
5. `Maven > Update Project`, `Project > Clean`, rồi chạy `UI.DangNhap.DangNhap`.

## Kiểm tra

Đã biên dịch toàn bộ nguồn Java bằng JDK 21 với lớp BCrypt thay thế **chỉ dành cho kiểm tra cú pháp**. Đã dựng Swing UI với dữ liệu giả lập ngoài dự án để kiểm tra kích thước 1150×750 và 920×750. Chưa kiểm thử trực tiếp trên máy/SQL Server của bạn.
