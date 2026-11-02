# MEDIDENT – BẢN SỬA NHẬP LIỆU VÀ NGHIỆP VỤ

**Nguồn:** Dự án người dùng gửi `MediDent.rar`. Đối chiếu hai tài liệu:
`MEDIDENT_BaoCao_ToanDien_Loi_NghiepVu_3PhanQuyen.docx` và
`MEDIDENT_PhanChuaKiemTra_LoiDongBo_BaVaiTro.docx`.

## 1. Cách chạy

1. Sao lưu dự án cũ và **backup database MediDent trước khi chạy SQL**.
2. Mở `MediDentAdmin` bằng Eclipse → Import → Existing Maven Projects.
3. Dùng **JDK 17**, Maven tải `mssql-jdbc` và `jbcrypt` từ `pom.xml`.
4. Điền tài khoản SQL của chính bạn vào `src/main/resources/db.properties` (đã thay mật khẩu cũ bằng `CHANGE_ME`). Không đưa mật khẩu thật lên Git.
5. Với **database đã có**: kiểm tra các script `SQLData/01` đến `12` đã chạy đầy đủ, sau đó chạy **`SQLData/14_DongBo_CSDL_LoiNhapLieu_SQL2012.sql`** trong SSMS trên **bản sao DB thử nghiệm**. Script 13 cần thiết vì Java dùng bảng `PhongKham`, các cột `thoiLuongPhut`, `phienBan`, `thoiDiemApDung`.
6. Với **database mới**: chạy các script theo thứ tự `01`, `04`, `05`, `06`, `07`, `08`, `09`, `10`, `11`, **`14`** (các script kiểm tra chỉ SELECT; script 03 là dữ liệu mẫu tùy chọn). Kiểm tra lỗi và dữ liệu thật từng bước, không chạy ồ ạt trên DB đang sử dụng.
7. Chạy lớp `UI.DangNhap.DangNhap`. Dùng đúng ba vai trò đăng nhập: **Quản trị viên / Quản lý phòng khám / Lễ tân**. Bác sĩ chỉ là chức vụ nhân sự.
8. Mục **Phòng khám** ở menu Quản lý đã được bỏ theo yêu cầu. Danh mục phòng vẫn nằm trong SQL để Lễ tân và lịch hẹn sử dụng. `sucChua=2` của dữ liệu mẫu chỉ là giả định demo.
9. Trước khi **Tạo lịch**, vào **Nhân viên** kiểm tra bác sĩ đang làm việc và vào **Lịch làm việc** xếp ca bao trọn giờ hẹn; sau đó mới chọn bác sĩ/ngày/giờ/phòng ở **Lịch hẹn**.

## 2. Những mục đã sửa hoặc bổ sung trong bản này

| Nhóm lỗi trong tài liệu | Đã làm trong code | File chính |
|---|---|---|
| LT-02, LT-03 | Lọc bỏ STT ẩn; `BN101`, `BN 101`, `PK10` tìm đúng mã, không lẫn `BN1010`/`PK100`. | `UI/LeTan/TrangChuLeTan.java` |
| LT-06, nhập liệu NV | Form nhân viên dùng chung `SoDienThoaiHopLe` với DAO; dữ liệu sai/SQL lỗi giữ nguyên form. | `UI/QuanLy/NhanVienGUI.java` |
| LT-10 | Trước khi lưu bệnh nhân hiển thị thông tin cần xác nhận; lỗi SQL không làm mất nội dung đã nhập. | `UI/QuanLy/ThemBenhNhanGUI.java` |
| LT-12, SYNC-01/02/04 | Tải SQL lại khi mở trang; vẫn có nút **Tải lại** để thấy thay đổi từ máy khác. | `UI/QuanLy/VanHanhPage.java` |
| LT-15 | Form **Tạo lịch** có nút **Tải lại BN** để chọn bệnh nhân mới từ máy khác. | `UI/QuanLy/TaoLichHenGUI.java` |
| LT-01, QL-03 | Phòng là danh mục SQL, Danh mục phòng dùng chung trong SQL, đã bỏ màn quản lý phòng do người dùng yêu cầu; chặn phòng không hợp lệ/đầy. | `DAO/QuanLyDAO.java`, `UI/QuanLy/QuanLyPhongGUI.java`, `UI/LeTan/TrangChuLeTan.java`, SQL 13 |
| LT-19, LT-20, SYNC-03 | Check-in không lùi trạng thái; tiếp nhận chưa gán bác sĩ → `Chờ phân công`, Quản lý phân công → `Chờ khám`. Chặn tiếp nhận hồ sơ đã ngừng. | `UI/LeTan/TrangChuLeTan.java`, `DAO/QuanLyDAO.java` |
| QL-11, TC-03 | Bệnh nhân/nhân viên dùng **rowversion**; nếu hai người cùng sửa thì người sau được yêu cầu tải lại, không ghi đè âm thầm. | `DAO/QuanLyDAO.java`, các form BN/NV, SQL 13 |
| QL-13, SYNC-12 (một phần) | Lưu thời điểm khi **sửa giá dịch vụ**, không ghi đè lịch sử cũ; chưa khép kín quy trình chốt giá phiếu khám. | `DAO/QuanLyDAO.java`, SQL 13 |
| LT-24 | Lễ tân chỉ tải thông tin phiếu khám/hồ sơ phục vụ tiếp nhận, không tự đọc chẩn đoán/tiền sử bệnh qua trang chủ. | `UI/LeTan/TrangChuLeTan.java` |
| AD-12, AD-01 | Checkbox 2FA, khóa phiên, sao lưu, thông báo quyền chưa có backend → khóa, hiển thị **Chưa hỗ trợ**, không cho lưu giả. | `UI/QuanTriVien/QuanTriHeThong.java` |
| QL-20 (một phần) | Khi hiển thị báo cáo Admin, DAO không còn tải doanh thu/chi phí; vẫn cần tăng kiểm tra quyền đọc ở DAO theo actor. | `DAO/BaoCaoDuLieuDAO.java` |
| QL-15 | Kiểm tra giá sai, giữ nguyên biểu mẫu dịch vụ là phần có sẵn đã xác minh trên source. | `UI/QuanLy/DichVuGUI.java` |

### Những chức năng ĐÃ CÓ từ bản gốc, không viết lại

Các hàm lịch làm việc, dời lịch, khóa khoảng lịch bác sĩ, tạo thanh toán chờ đối soát, xác nhận thực thu, gửi yêu cầu hủy và duyệt hủy đã có trong bản RAR này. Bản sửa giữ nguyên các giao dịch đó để tránh phá luồng cũ.

## 3. Các nội dung KHÔNG kết luận là đã hoàn tất

Hai báo cáo gồm nhiều **đề xuất chưa được chốt yêu cầu và test case chưa chạy**. Bản này **không** khẳng định toàn bộ 59 hạng mục hay 25 TC đã PASS.

- **QL-01 / F03 – Phiếu khám & điều trị:** chưa có luồng nhập/chốt nghiệp vụ chuyên môn do người hành nghề phù hợp. Không giao Lễ tân tự chẩn đoán để hoàn thành luồng.
- **QL-17 / AD-10 / TC-17,18 – Hoàn tiền:** chưa có luồng đối soát/hoàn tiền thật. Nghiệp vụ đã thu tiền vẫn phải xử lý hoàn tiền riêng, không tự đánh dấu đã hoàn.
- **QL-20 / AD-02,09,15 – Phân quyền theo từng hành động và dữ liệu:** vẫn chủ yếu dùng các quyền nhóm hiện có; chưa hoàn tất phân quyền `VIEW/CREATE/UPDATE/DEACTIVATE/APPROVE` toàn bộ DAO.
- **AD-05,11,13 – bảo mật/backup/audit nâng cao:** chưa có backup tự động, kiểm tra restore thật, giới hạn đăng nhập theo thiết bị, nhật ký trước/sau đủ cấu trúc.
- **LT-09,11,16,23 – chính sách tiếp nhận/ngày sinh/ưu tiên/đặt giờ:** cần nhóm chốt quy tắc; lịch vẫn dùng chọn giờ dạng spinner, chưa phải lịch khung giờ trống hoàn chỉnh.
- **QL-12,18 – chốt đơn giá điều trị và điều chỉnh sổ chi:** cần xác nhận nghiệp vụ, xây quy trình chứng từ và dữ liệu chuyên môn trước khi đổi schema.
- **TC-02,05,06,12,16,20–25:** cần chạy trên SQL Server và/hoặc **hai máy đồng thời**, không thể thay bằng kiểm tra cú pháp Java.
- **Email:** giữ danh sách tên miền được chấp nhận theo yêu cầu trước của nhóm (`gmail.com`, `yahoo.com`, `outlook.com`, `medident.vn`...). Báo cáo QA đề xuất kiểm tra cú pháp và chấp nhận tên miền tổ chức; **hai chính sách mâu thuẫn**, nên chưa tự thay đổi danh sách. Cả hai cách đều không thể bảo đảm địa chỉ email tồn tại nếu chưa gửi thư xác thực.

## 4. Kết quả kiểm tra thực tế trong môi trường này

- `javac --release 17` biên dịch toàn bộ Java thành công với **lớp BCrypt giả lập chỉ dùng kiểm tra chữ ký**. **Không đưa stub vào file bàn giao.** Đây là kiểm tra cú pháp, KHÔNG chứng minh BCrypt thật hay Maven phụ thuộc đã tải thành công.
- Chạy **17 phép thử offline** ở `src/test/java/KiemTraNhapLieu.java` về Email, SĐT, mật khẩu và lọc danh sách: **17/17 đạt**.
- **Chưa kết nối SQL Server và chưa chạy `mvn clean package` trong môi trường này**. Các thao tác INSERT/UPDATE, migration, race condition, phân quyền thực tế vẫn ở trạng thái **CHƯA KIỂM THỬ TÍCH HỢP**.

## 5. Kiểm thử bắt buộc trên máy của bạn (sau backup DB)

1. **TC-01/03**: hai máy mở cùng BN, người A sửa xong, người B bấm lưu phải báo yêu cầu tải lại; tương tự nhân viên.
2. **TC-05/06**: tạo hai lịch chồng 09:00–10:00 và 09:30–10:30 cho cùng bác sĩ/phòng → lịch thứ hai bị từ chối.
3. **TC-07**: đặt sức chứa phòng = 2, tiếp nhận 3 người chưa hoàn tất → người thứ ba bị từ chối.
4. **TC-10**: Lễ tân gửi hỗ trợ, Quản lý tải lại và phản hồi, Lễ tân tải lại xem phản hồi.
5. **TC-11/12**: hai Admin cùng duyệt yêu cầu hủy → chỉ một người thành công, không tự hoàn tiền.
6. **TC-15/16**: tạo chuyển khoản chưa thực thu → Chờ thanh toán; xác nhận một lần bằng mã giao dịch; không được tạo khoản trùng.
7. **TC-24**: Admin khóa tài khoản khác trong lúc form đang mở, thao tác lưu kế tiếp bị DAO từ chối.
8. **TC-25**: Lễ tân thêm bệnh nhân, Quản lý tải lại danh sách; không cần đăng nhập lại.
9. Kiểm tra `SELECT` dữ liệu trước/sau mỗi thao tác, lưu expected/actual, ảnh và `PASS`/`FAIL`; những TC chưa chạy vẫn để **CHƯA XÁC MINH**.

**Lưu ý:** không dùng DB bệnh nhân thật để kiểm thử xóa, hủy hóa đơn, hoàn tiền hoặc migration.
