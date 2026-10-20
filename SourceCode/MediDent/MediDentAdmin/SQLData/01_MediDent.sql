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
 diaChi NVARCHAR(255) NULL,tienSuBenh NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL
);
GO
IF OBJECT_ID(N'dbo.NguoiGiamHo',N'U') IS NULL
CREATE TABLE dbo.NguoiGiamHo (
 maGH INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 hoTen NVARCHAR(120) NOT NULL,sdt VARCHAR(20) NULL,quanHe NVARCHAR(60) NULL
);
GO
IF OBJECT_ID(N'dbo.LichLamViec',N'U') IS NULL
CREATE TABLE dbo.LichLamViec (
 maLichLV INT IDENTITY(1,1) PRIMARY KEY,maNV INT NOT NULL REFERENCES dbo.NhanVien(maNV),
 thu NVARCHAR(15) NULL,ngayLamViec DATE NOT NULL,caLamViec NVARCHAR(50) NULL,
 gioBatDau TIME NULL,gioKetThuc TIME NULL,trangThai NVARCHAR(30) NULL
);
GO
IF OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
CREATE TABLE dbo.LichHen (
 maLichHen INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 maNV INT NULL REFERENCES dbo.NhanVien(maNV),phongKham NVARCHAR(80) NULL,
 ngayHen DATE NOT NULL,gioHen TIME NOT NULL,lyDo NVARCHAR(500) NULL,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xác nhận',ghiChu NVARCHAR(1000) NULL
);
GO
IF OBJECT_ID(N'dbo.PhieuKham',N'U') IS NULL
CREATE TABLE dbo.PhieuKham (
 maPK INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 maNV INT NOT NULL REFERENCES dbo.NhanVien(maNV),ngayKham DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 trieuChung NVARCHAR(MAX) NULL,chanDoan NVARCHAR(MAX) NULL,
 phuongPhapDieuTri NVARCHAR(MAX) NULL,ketQua NVARCHAR(MAX) NULL,
 ghiChu NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL
);
GO

-- 03. Dich vu va chi tiet dieu tri.
IF OBJECT_ID(N'dbo.DichVu',N'U') IS NULL
CREATE TABLE dbo.DichVu (maDV INT IDENTITY(1,1) PRIMARY KEY,tenDV NVARCHAR(150) NOT NULL,
 moTa NVARCHAR(500) NULL,donGia DECIMAL(18,2) NOT NULL DEFAULT 0,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.BangGiaDichVu',N'U') IS NULL
CREATE TABLE dbo.BangGiaDichVu (maBG INT IDENTITY(1,1) PRIMARY KEY,
 maDV INT NOT NULL REFERENCES dbo.DichVu(maDV),ngayApDung DATE NOT NULL,
 donGia DECIMAL(18,2) NOT NULL,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.DichVuDieuTri',N'U') IS NULL
CREATE TABLE dbo.DichVuDieuTri (maDDT INT IDENTITY(1,1) PRIMARY KEY,
 tenDDT NVARCHAR(150) NOT NULL,donGia DECIMAL(18,2) NOT NULL DEFAULT 0,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.ChiTietDieuTri',N'U') IS NULL
CREATE TABLE dbo.ChiTietDieuTri (maCT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),maDDT INT NOT NULL REFERENCES dbo.DichVuDieuTri(maDDT),
 noiDung NVARCHAR(MAX) NULL,soLuong INT NOT NULL DEFAULT 1 CHECK(soLuong>0),
 donGia DECIMAL(18,2) NOT NULL DEFAULT 0,thanhTien AS (CONVERT(DECIMAL(18,2),soLuong*donGia)) PERSISTED);
GO

-- 04. Thanh toan, hoa don va phe duyet huy.
IF OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
CREATE TABLE dbo.ThanhToan (maTT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),ngayThanhToan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 soTien DECIMAL(18,2) NOT NULL CHECK(soTien>=0),phuongThuc NVARCHAR(40) NULL,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',maGiaoDich VARCHAR(100) NULL);
GO
IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL
CREATE TABLE dbo.HoaDon (maHD INT IDENTITY(1,1) PRIMARY KEY,
 maTT INT NOT NULL REFERENCES dbo.ThanhToan(maTT),maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),
 ngayLap DATETIME2 NOT NULL DEFAULT SYSDATETIME(),tongTien DECIMAL(18,2) NOT NULL DEFAULT 0,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ thanh toán');
GO
IF OBJECT_ID(N'dbo.YeuCauHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.YeuCauHuyPhieuThu (maYC INT IDENTITY(1,1) PRIMARY KEY,
 maTT INT NOT NULL REFERENCES dbo.ThanhToan(maTT),ngayYeuCau DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 lyDo NVARCHAR(500) NOT NULL,trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ duyệt');
GO
IF OBJECT_ID(N'dbo.PhieuDuyetHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.PhieuDuyetHuyPhieuThu (maPD INT IDENTITY(1,1) PRIMARY KEY,
 maYC INT NOT NULL REFERENCES dbo.YeuCauHuyPhieuThu(maYC),
 ngayPheDuyet DATETIME2 NULL,lyDoPheDuyet NVARCHAR(500) NULL);
GO

-- 05. Bao cao luu snapshot theo ERD (khac voi bao cao tong hop realtime tren UI).
IF OBJECT_ID(N'dbo.BaoCaoDoanhThu',N'U') IS NULL
CREATE TABLE dbo.BaoCaoDoanhThu (maBC INT IDENTITY(1,1) PRIMARY KEY,
 maHD INT NOT NULL REFERENCES dbo.HoaDon(maHD),tuNgay DATE NOT NULL,denNgay DATE NOT NULL,
 tongDoanhThu DECIMAL(18,2) NOT NULL DEFAULT 0);
GO
IF OBJECT_ID(N'dbo.BaoCaoLuotKham',N'U') IS NULL
CREATE TABLE dbo.BaoCaoLuotKham (maBC INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),tuNgay DATE NOT NULL,denNgay DATE NOT NULL,
 soLuotKham INT NOT NULL DEFAULT 0);
GO

-- 06. Nhat ky duoc doi theo TEN COT ERD, luu ai tac dong o doiTuong/moTa.
IF OBJECT_ID(N'dbo.NhatKyHeThong',N'U') IS NULL
CREATE TABLE dbo.NhatKyHeThong (maNK BIGINT IDENTITY(1,1) PRIMARY KEY,
 thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),hanhDong NVARCHAR(200) NOT NULL,
 doiTuong NVARCHAR(150) NULL,moTa NVARCHAR(1000) NULL);
GO

-- 07. BANG BO SUNG: ERD KHONG CO chi tiet quyen / cau hinh, UI Admin can chung.
IF OBJECT_ID(N'dbo.Quyen',N'U') IS NULL
CREATE TABLE dbo.Quyen (maQuyen VARCHAR(30) PRIMARY KEY,tenQuyen NVARCHAR(100) NOT NULL);
GO
IF OBJECT_ID(N'dbo.VaiTroQuyen',N'U') IS NULL
CREATE TABLE dbo.VaiTroQuyen (maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro),
 maQuyen VARCHAR(30) NOT NULL REFERENCES dbo.Quyen(maQuyen),
 CONSTRAINT PK_VaiTroQuyen PRIMARY KEY(maVaiTro,maQuyen));
GO
IF OBJECT_ID(N'dbo.CauHinh',N'U') IS NULL
CREATE TABLE dbo.CauHinh (Khoa VARCHAR(60) PRIMARY KEY,GiaTri NVARCHAR(500) NOT NULL);
GO

MERGE dbo.Quyen AS t USING (VALUES
 ('TAIKHOAN',N'Quản lý tài khoản và phân quyền'),('BENHNHAN',N'Quản lý bệnh nhân'),
 ('LICHHEN',N'Quản lý lịch hẹn'),('THANHTOAN',N'Quản lý thanh toán'),
 ('BAOCAO',N'Báo cáo thống kê'),('HETHONG',N'Quản trị hệ thống')) s(maQuyen,tenQuyen)
 ON t.maQuyen=s.maQuyen WHEN NOT MATCHED THEN INSERT(maQuyen,tenQuyen) VALUES(s.maQuyen,s.tenQuyen);
GO
MERGE dbo.VaiTro AS t USING (VALUES
 (N'Quản trị viên',N'Toàn quyền'),(N'Quản lý phòng khám',N'Quản lý vận hành'),
 (N'Lễ tân',N'Tiếp nhận bệnh nhân')) s(tenVaiTro,moTa)
 ON t.tenVaiTro=s.tenVaiTro WHEN NOT MATCHED THEN INSERT(tenVaiTro,moTa) VALUES(s.tenVaiTro,s.moTa);
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE v.tenVaiTro=N'Quản trị viên' AND NOT EXISTS
 (SELECT 1 FROM dbo.VaiTroQuyen z WHERE z.maVaiTro=v.maVaiTro AND z.maQuyen=q.maQuyen);
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v JOIN dbo.Quyen q ON
(v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN('BENHNHAN','LICHHEN','THANHTOAN','BAOCAO')) OR
(v.tenVaiTro=N'Lễ tân' AND q.maQuyen IN('BENHNHAN','LICHHEN','THANHTOAN'))
WHERE NOT EXISTS(SELECT 1 FROM dbo.VaiTroQuyen z WHERE z.maVaiTro=v.maVaiTro AND z.maQuyen=q.maQuyen);
GO
MERGE dbo.CauHinh AS t USING (VALUES
 ('TenHeThong',N'Hệ thống Quản lý Nha khoa MediDent'),('MaDonVi',N'MEDIDENT-HCM-01'),
 ('EmailQuanTri',N'quantri@medident.vn'),('SoDienThoai',N''),('DiaChi',N''),
 ('Bat2FA',N'false'),('KhoaPhien',N'true'),('ThongBaoQuyen',N'false'),('SaoLuuTuDong',N'false')) s(Khoa,GiaTri)
 ON t.Khoa=s.Khoa WHEN NOT MATCHED THEN INSERT(Khoa,GiaTri) VALUES(s.Khoa,s.GiaTri);
GO
-- Tao tai khoan quan tri lan dau bang ConnectDB.KhoiTaoAdmin (mat khau BCrypt).
-- Khong seed tai khoan hoac du lieu benh nhan gia.


-- RANG BUOC EMAIL: chi chap nhan ten mien da duyet; khong tu cho phep @batky.com.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_NhanVien_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
    -- Co du lieu cu khong hop le: ghi canh bao, khong tu sua/xoa ho so.
    IF EXISTS(SELECT 1 FROM dbo.NhanVien WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')
    ))
    BEGIN
        PRINT N'Canh bao: dbo.NhanVien co email cu sai ten mien. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.NhanVien WITH NOCHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_BenhNhan_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    -- Co du lieu cu khong hop le: ghi canh bao, khong tu sua/xoa ho so.
    IF EXISTS(SELECT 1 FROM dbo.BenhNhan WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')
    ))
    BEGIN
        PRINT N'Canh bao: dbo.BenhNhan co email cu sai ten mien. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.BenhNhan WITH NOCHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
END;
GO
