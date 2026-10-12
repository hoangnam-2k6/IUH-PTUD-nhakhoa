# Sửa lỗi logo `location is null` (MediDent)

## Nguyên nhân

Hai màn hình `KhungChinh` và `DangNhap` gọi `new ImageIcon(getClass().getResource("/Image/logo_ai.png"))`. Nếu Eclipse không đưa `src/main/resources` vào classpath, `getResource()` trả về `null`, làm `ImageIcon` báo lỗi `URL.toExternalForm() because location is null`. Đây không phải lỗi SQL Server.

## Cách áp dụng nhanh (khuyến nghị, không mất dữ liệu)

1. Sao lưu project hiện tại.
2. Từ ZIP bản vá, chép đè ba file:
   - `src/main/java/UI/Chung/GiaoDien.java`
   - `src/main/java/UI/Chung/KhungChinh.java`
   - `src/main/java/UI/DangNhap/DangNhap.java`
3. Kiểm tra `src/main/resources/Image/logo_ai.png` có tồn tại (bản ZIP đầy đủ đã kèm).
4. Eclipse: chuột phải project > Maven > Update Project; vào Project > Clean.
5. Kiểm tra `Properties > Java Build Path > Source` có hai mục `src/main/java` và `src/main/resources`, KHÔNG dùng `src` như source folder.
6. Chạy lại `UI.DangNhap.DangNhap`.

Thay đổi: Thêm `GiaoDien.logo(int)` tải ảnh qua classpath, xử lý thiếu ảnh bằng logo vẽ dự phòng; thông báo lỗi được chia `Lỗi SQL Server` và `Lỗi ứng dụng`. Không chỉnh DAO, database hoặc tài khoản.

## Kiểm tra nhanh tài nguyên

Nếu dùng Maven, sau khi build phải có `target/classes/Image/logo_ai.png`.
Khi mở Eclipse, nếu logo thực bị thiếu nhưng app vẫn chạy, xem tab Console: `Khong tim thay /Image/logo_ai.png trong classpath`.

Nếu dùng project đã chỉnh SQL trên máy, chỉ dùng ZIP bản vá 3 file, KHÔNG ghi đè `db.properties` hoặc SQLData.
