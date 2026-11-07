-- Tai khoan demo thu hai co day du 14 quyen menu hien co.
-- CHI DANH CHO DEMO: Dang nhap "adminfull" / "MediDent#2026Demo!".
-- Lan dang nhap dau bat buoc doi mat khau. Dung voi SQL Server 2012+.
-- Vai tro Quan tri vien la DUY NHAT duoc dung quyen TAIKHOAN / PHANQUYEN.
USE MediDent;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @maVaiTro INT, @maTK INT, @maNV INT;
SELECT @maVaiTro=maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản trị viên';
IF @maVaiTro IS NULL
BEGIN
    ROLLBACK TRANSACTION;
    RAISERROR(N'Hãy chạy 01_MediDent.sql trước.',16,1);
    RETURN;
END;

-- Không ghi đè mật khẩu, vai trò hoặc hồ sơ đã tồn tại.
IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='adminfull')
    INSERT dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('adminfull','$2a$11$NaPrqPt1bieyvcFd3nm03eyZmbUgedV1UTMV3gfoW/.DFdfhuwGbm',N'Hoạt động',1,@maVaiTro);

IF EXISTS (SELECT 1 FROM dbo.TaiKhoan
           WHERE tenDangNhap='adminfull' AND maVaiTro<>@maVaiTro)
BEGIN
    ROLLBACK TRANSACTION;
    RAISERROR(N'Tài khoản adminfull đã tồn tại nhưng không thuộc vai trò Quản trị viên.',16,1);
    RETURN;
END;
SELECT @maTK=maTK FROM dbo.TaiKhoan WHERE tenDangNhap='adminfull';
IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien WHERE maTK=@maTK)
    INSERT dbo.NhanVien(hoTen,ngaySinh,chucVu,trangThai,maTK)
    VALUES(N'Tài khoản kiểm thử toàn quyền',CONVERT(date,'20000101',112),N'Quản trị viên',N'Đang làm việc',@maTK);
SELECT @maNV=maNV FROM dbo.NhanVien WHERE maTK=@maTK;
IF NOT EXISTS(SELECT 1 FROM dbo.QuanTriVien WHERE maNV=@maNV)
    INSERT dbo.QuanTriVien(maNV) VALUES(@maNV);

-- Admin goc + adminfull cung vai tro quan tri, co 14 quyen menu theo vai tro.
-- Khong cap hai quyen quan tri cho vai tro Quan ly / Le tan.
INSERT dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT @maVaiTro, q.maQuyen FROM dbo.Quyen q
WHERE NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen q2
                  WHERE q2.maVaiTro=@maVaiTro AND q2.maQuyen=q.maQuyen);
COMMIT TRANSACTION;
GO
-- Truy van kiem tra khong sua du lieu.
SELECT t.tenDangNhap,v.tenVaiTro,t.phaiDoiMatKhau,
       COUNT(vq.maQuyen) AS soQuyenMenu
FROM dbo.TaiKhoan t
JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro
LEFT JOIN dbo.VaiTroQuyen vq ON vq.maVaiTro=v.maVaiTro
WHERE t.tenDangNhap IN ('admin','adminfull')
GROUP BY t.tenDangNhap,v.tenVaiTro,t.phaiDoiMatKhau;
GO
