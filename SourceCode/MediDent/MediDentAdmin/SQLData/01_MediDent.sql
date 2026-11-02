IF OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
CREATE TABLE dbo.ThanhToan (maTT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),ngayThanhToan DATETIME2 NULL,
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
