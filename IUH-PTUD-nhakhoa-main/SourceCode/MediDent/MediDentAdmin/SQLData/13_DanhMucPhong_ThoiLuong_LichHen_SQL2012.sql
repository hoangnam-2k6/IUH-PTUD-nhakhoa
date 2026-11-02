-- MediDent: cap nhat danh muc phong va thoi luong lich hen (SQL Server 2012+).
-- CHI CHAY TREN DB THU NGHIEM DA SAO LUU. Script khong xoa lich hen.
USE MediDent;
GO

-- 1. Tao bang phong dung chung cho Le tan va Quan ly.
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        tenPhong NVARCHAR(80) NOT NULL PRIMARY KEY,
        sucChua INT NOT NULL CONSTRAINT DF_MediDent_PhongKham_SucChua DEFAULT(2),
        trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_MediDent_PhongKham_TrangThai DEFAULT(N'Hoạt động'),
        CONSTRAINT CK_MediDent_Phong_SucChua CHECK (sucChua BETWEEN 1 AND 50),
        CONSTRAINT CK_MediDent_Phong_TrangThai CHECK (trangThai IN (N'Hoạt động',N'Ngừng hoạt động'))
    );
END;
GO

-- 2. Giu lai ten phong cua nhung lich hen cu (khong mat lien ket tham chieu).
INSERT dbo.PhongKham(tenPhong,sucChua,trangThai)
SELECT DISTINCT LTRIM(RTRIM(l.phongKham)),2,N'Hoạt động'
FROM dbo.LichHen l
WHERE NULLIF(LTRIM(RTRIM(l.phongKham)),N'') IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM dbo.PhongKham p
                  WHERE p.tenPhong=LTRIM(RTRIM(l.phongKham)));
GO

-- 3. Ba phong cho du lieu demo khi chua co phong. Suc chua = 2 la GIA DINH;
--    nguoi quan ly can doi gia tri theo thuc te truoc khi su dung.
INSERT dbo.PhongKham(tenPhong,sucChua,trangThai)
SELECT v.tenPhong,2,N'Hoạt động'
FROM (VALUES (N'Phòng 01'),(N'Phòng 02'),(N'Phòng 03')) v(tenPhong)
WHERE NOT EXISTS (SELECT 1 FROM dbo.PhongKham p WHERE p.tenPhong=v.tenPhong);
GO

-- 4. Code dung thoiLuongPhut de phat hien hai lich hen chong khoang gio.
IF COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL
BEGIN
    ALTER TABLE dbo.LichHen
    ADD thoiLuongPhut INT NOT NULL
        CONSTRAINT DF_MediDent_LichHen_ThoiLuong DEFAULT(60) WITH VALUES;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_MediDent_LichHen_ThoiLuong'
                 AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_MediDent_LichHen_ThoiLuong
        CHECK (thoiLuongPhut BETWEEN 15 AND 240);
END;
GO

-- 5. Rowversion khong cho hai may sua de len nhau trong giao dien BN/NV.
IF COL_LENGTH(N'dbo.BenhNhan',N'phienBan') IS NULL
    ALTER TABLE dbo.BenhNhan ADD phienBan ROWVERSION;
GO
IF COL_LENGTH(N'dbo.NhanVien',N'phienBan') IS NULL
    ALTER TABLE dbo.NhanVien ADD phienBan ROWVERSION;
GO

-- 6. Gia dich vu luu moc thoi gian moi lan sua, ke ca hai lan trong cung ngay.
--    Lich su cu duoc de NULL thoiDiemApDung de khong gia mao gio sua qua khu.
IF COL_LENGTH(N'dbo.BangGiaDichVu',N'thoiDiemApDung') IS NULL
    ALTER TABLE dbo.BangGiaDichVu ADD thoiDiemApDung DATETIME2 NULL;
GO

-- 7. Don gia chi tiet dieu tri khong the am (neu du lieu cu hop le).
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_MediDent_ChiTietDieuTri_DonGia'
                 AND parent_object_id=OBJECT_ID(N'dbo.ChiTietDieuTri'))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.ChiTietDieuTri WHERE donGia < 0)
        ALTER TABLE dbo.ChiTietDieuTri WITH CHECK
        ADD CONSTRAINT CK_MediDent_ChiTietDieuTri_DonGia CHECK (donGia >= 0);
    ELSE
        PRINT N'Du lieu chi tiet dieu tri co don gia am: can sua truoc khi them rang buoc.';
END;
GO

-- 6. Xac nhan sau migration.
SELECT tenPhong,sucChua,trangThai FROM dbo.PhongKham ORDER BY tenPhong;
SELECT name FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.LichHen')
  AND name=N'thoiLuongPhut';
GO
