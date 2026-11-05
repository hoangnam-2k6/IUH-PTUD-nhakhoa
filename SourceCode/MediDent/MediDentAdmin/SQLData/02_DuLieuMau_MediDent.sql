-- DU LIEU MAU MEDIDENT - SQL Server 2012+
-- Chay 01_MediDent.sql truoc, sau do chay file nay.
-- Chi them tai khoan neu chua co, khong ghi de tai khoan hien tai.
-- Tai khoan demo (chi dung de hoc/test):
--   admin    / Admin@123     - Quan tri vien
--   quanly   / QuanLy@123    - Quan ly phong kham
--   letan    / LeTan@123     - Le tan
--   letan2   / LeTan2@123    - Le tan
-- Mat khau trong cot matKhau la BCrypt, KHONG luu mat khau dang van ban.

USE MediDent;
GO

DECLARE @adminRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản trị viên');
DECLARE @quanLyRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản lý phòng khám');
DECLARE @leTanRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Lễ tân');

IF @adminRole IS NULL OR @quanLyRole IS NULL OR @leTanRole IS NULL
BEGIN
    RAISERROR(N'Chưa có đủ 3 vai trò. Hãy chạy 01_MediDent.sql trước.', 16, 1);
    RETURN;
END;

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='admin')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('admin','$2a$10$eONAdIuMzjaFqOKfAcQ92uap1psba5T.6QvQwV6./YRoxQ65ry4Xi',N'Hoạt động',0,@adminRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='quanly')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('quanly','$2a$10$ZxRiF6Wst7phqf7pZ6GNbuOWXjoJCjVayeXwZU/BHSAteQHm/yFle',N'Hoạt động',0,@quanLyRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='letan')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('letan','$2a$10$5Lg46JdY7EzO2KLg8DDokOiSzvPhGXkPlg5eIhVASdUu/SLATXGFm',N'Hoạt động',0,@leTanRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='letan2')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('letan2','$2a$10$DOj0jmaj6oO6CyJdGbGEbOY8O57vCXO3ZpxJjkoyOZiIt5f9Oo5vO',N'Hoạt động',0,@leTanRole);

-- Ho so nhan vien giup dang nhap, hien thi ho ten va reset mat khau theo ngay sinh.
IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='admin')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Nguyễn Minh An','1995-01-15',N'Nam','0901000001','admin@medident.vn',N'TP. Hồ Chí Minh',N'Quản trị viên',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='admin';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='quanly')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Trần Minh Quân','1992-03-18',N'Nam','0901000002','quanly@medident.vn',N'TP. Hồ Chí Minh',N'Quản lý phòng khám',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='quanly';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='letan')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Lê Thanh Mai','2001-09-12',N'Nữ','0901000003','letan@medident.vn',N'TP. Hồ Chí Minh',N'Lễ tân',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='letan';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='letan2')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Phạm Ngọc Anh','2002-06-21',N'Nữ','0901000004','letan2@medident.vn',N'TP. Hồ Chí Minh',N'Lễ tân',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='letan2';

-- Dong bo bang phan loai nhan su cho chuc nang Quan ly tai khoan.
INSERT INTO dbo.QuanTriVien(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap='admin' AND NOT EXISTS(SELECT 1 FROM dbo.QuanTriVien q WHERE q.maNV=n.maNV);

INSERT INTO dbo.QuanLy(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap='quanly' AND NOT EXISTS(SELECT 1 FROM dbo.QuanLy q WHERE q.maNV=n.maNV);

INSERT INTO dbo.LeTan(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap IN ('letan','letan2') AND NOT EXISTS(SELECT 1 FROM dbo.LeTan l WHERE l.maNV=n.maNV);

-- Admin demo co quyen xem TOAN BO 14 muc menu theo vai tro.
-- Quyen van theo vai tro, nen moi Admin deu nhan danh sach nay.
-- Khong them quyen quan tri tai khoan cho Quan ly hoac Le tan.
INSERT INTO dbo.VaiTroQuyen(maVaiTro, maQuyen)
SELECT v.maVaiTro, q.maQuyen
FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE v.tenVaiTro=N'Quản trị viên'
  AND NOT EXISTS (
      SELECT 1 FROM dbo.VaiTroQuyen x
      WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen
  );

-- Neu vai tro Quan ly/Le tan chua duoc cap quyen lan nao, gan quyen demo day du.
-- Khong ghi de vai tro da duoc tick/bo tick trong man hinh Phan quyen.
IF NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=@quanLyRole)
    INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
    SELECT @quanLyRole,maQuyen FROM dbo.Quyen
    WHERE maQuyen IN ('HOTRO','QL_NHANVIEN','QL_DICHVU','QL_BENHNHAN',
                      'QL_LICHHEN','QL_THANHTOAN','QL_BAOCAO');

IF NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=@leTanRole)
    INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
    SELECT @leTanRole,maQuyen FROM dbo.Quyen
    WHERE maQuyen IN ('HOTRO','LT_KHAM','LT_TIEPNHAN','LT_LICHHEN',
                      'LT_THANHTOAN','LT_TRACUU');

-- Du lieu ho tro de test: nguoi dung Quen mat khau -> Admin co the xem va reset.
IF NOT EXISTS (SELECT 1 FROM dbo.YeuCauHoTro WHERE tieuDe=N'Quên mật khẩu' AND
    maTK=(SELECT maTK FROM dbo.TaiKhoan WHERE tenDangNhap='letan2'))
    INSERT INTO dbo.YeuCauHoTro(maTK,tieuDe,noiDung,trangThai)
    SELECT maTK,N'Quên mật khẩu',N'Yêu cầu đặt lại mật khẩu (dữ liệu thử nghiệm).',N'Chờ xử lý'
    FROM dbo.TaiKhoan WHERE tenDangNhap='letan2';

SELECT t.tenDangNhap, n.hoTen, v.tenVaiTro, t.trangThai, n.ngaySinh
FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro
JOIN dbo.NhanVien n ON n.maTK=t.maTK
WHERE t.tenDangNhap IN ('admin','quanly','letan','letan2');
-- Kiem tra so quyen menu cua cac tai khoan demo theo vai tro.
SELECT v.tenVaiTro,COUNT(vq.maQuyen) AS soQuyenHienThi
FROM dbo.VaiTro v LEFT JOIN dbo.VaiTroQuyen vq ON vq.maVaiTro=v.maVaiTro
WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')
GROUP BY v.tenVaiTro;
GO
