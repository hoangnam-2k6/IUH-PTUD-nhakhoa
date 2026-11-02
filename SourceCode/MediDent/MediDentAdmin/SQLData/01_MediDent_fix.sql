-- MEDIDENT - Tao database theo so do ERD nguoi dung cung cap.
-- SQL Server 2012+; tao moi bang khi chua ton tai; KHONG xoa bang hay du lieu cu.
-- ERD chi cho TEN COT / QUAN HE, khong cho kieu du lieu; cac kieu va NULL sau la gia dinh.
-- IDs duoc chon INT IDENTITY, tien/chi phi DECIMAL(18,2), thoi gian DATETIME2.
-- Quan trong: neu CSDL cua ban da ton tai, DOI CHIEU kieu du lieu truoc khi chay.
IF DB_ID(N'MediDent') IS NULL CREATE DATABASE MediDent;
GO
USE MediDent;
GO
-- Chan truong hop da chay ban admin cu: TaiKhoan cu chua cot matKhau theo ERD.
-- Neu gap loi: sao luu CSDL, migrate bang cu truoc, KHONG xoa du lieu that.
IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.TaiKhoan',N'matKhau') IS NULL
BEGIN
    THROW 50001, N'Bang TaiKhoan cu khong co cot matKhau theo ERD. Hay sao luu/migrate DB truoc.', 1;
END
GO

-- 01. VaiTro -> TaiKhoan -> NhanVien: TaiKhoan KHONG chua HoTen / Email.
IF OBJECT_ID(N'dbo.VaiTro',N'U') IS NULL
CREATE TABLE dbo.VaiTro (
 maVaiTro INT IDENTITY(1,1) PRIMARY KEY,
 tenVaiTro NVARCHAR(100) NOT NULL UNIQUE,
 moTa NVARCHAR(255) NULL
);
GO
IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
CREATE TABLE dbo.TaiKhoan (
 maTK INT IDENTITY(1,1) PRIMARY KEY,
 tenDangNhap VARCHAR(60) NOT NULL UNIQUE,
 matKhau VARCHAR(100) NOT NULL, -- BCrypt hash, KHONG phai mat khau goc
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Hoạt động'
   CHECK (trangThai IN (N'Hoạt động',N'Tạm khóa')),
 ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 ngayDangNhapCuoi DATETIME2 NULL,
 phaiDoiMatKhau BIT NOT NULL DEFAULT 0, -- Buộc đổi mật khẩu sau reset/cấp lần đầu
 maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro)
);
GO
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
CREATE TABLE dbo.NhanVien (
 maNV INT IDENTITY(1,1) PRIMARY KEY,
 hoTen NVARCHAR(120) NOT NULL,
 ngaySinh DATE NULL,
 gioiTinh NVARCHAR(15) NULL,
 sdt VARCHAR(20) NULL,
 email VARCHAR(150) NULL,
 diaChi NVARCHAR(255) NULL,
 chucVu NVARCHAR(100) NULL,
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Đang làm việc',
 maTK INT NULL REFERENCES dbo.TaiKhoan(maTK)
);
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_NhanVien_maTK' AND object_id=OBJECT_ID(N'dbo.NhanVien'))
CREATE UNIQUE INDEX UX_NhanVien_maTK ON dbo.NhanVien(maTK) WHERE maTK IS NOT NULL;
GO
IF OBJECT_ID(N'dbo.QuanTriVien',N'U') IS NULL
CREATE TABLE dbo.QuanTriVien (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), capDoQuyen NVARCHAR(80) NOT NULL DEFAULT N'Toàn quyền');
GO
IF OBJECT_ID(N'dbo.QuanLy',N'U') IS NULL
CREATE TABLE dbo.QuanLy (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), phongBan NVARCHAR(100) NULL);
GO
IF OBJECT_ID(N'dbo.LeTan',N'U') IS NULL
CREATE TABLE dbo.LeTan (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), caLamViec NVARCHAR(50) NULL);
GO

-- 02. Nhom benh nhan, lich hen, phieu kham.
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
CREATE TABLE dbo.BenhNhan (
 maBN INT IDENTITY(1,1) PRIMARY KEY,hoTen NVARCHAR(120) NOT NULL,ngaySinh DATE NULL,
 gioiTinh NVARCHAR(15) NULL,sdt VARCHAR(20) NULL,email VARCHAR(150) NULL,
 diaChi NVARCHAR(255) NULL,tienSuBenh NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL,
 trangThaiHoSo NVARCHAR(20) NOT NULL DEFAULT N'Hoạt động'
);
GO
-- Dam bao schema cu cung co trangThaiHoSo, khong chi schema tao moi.
-- Bang BenhNhan neu da ton tai se khong duoc tao lai boi lenh IF OBJECT_ID o tren.
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL
BEGIN
    ALTER TABLE dbo.BenhNhan
      ADD trangThaiHoSo NVARCHAR(20) NOT NULL
          CONSTRAINT DF_BenhNhan_trangThaiHoSo DEFAULT N'Hoạt động' WITH VALUES;
END;
GO

-- Chuan hoa cac gia tri NULL cu (neu cot da ton tai tu mot lan nang cap thu cong).
IF COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NOT NULL
BEGIN
    UPDATE dbo.BenhNhan
       SET trangThaiHoSo=N'Hoạt động'
     WHERE trangThaiHoSo IS NULL;
END;
GO

-- Rang buoc cac trang thai ho so hop le; chi tao neu chua co.