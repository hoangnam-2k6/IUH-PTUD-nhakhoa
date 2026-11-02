-- 13: NANG CAP DAO QUAN LY - LICHHEN / HOSO BENH NHAN / THANH TOAN
-- SQL Server 2012+. Chay sau 01_MediDent.sql va cac migration 04, 06, 07, 11.
-- Sao luu database truoc khi chay. Script co the chay lai an toan.
USE MediDent;
GO

-- Tach trang thai ho so khoi trang thai dieu tri (Moi/Dang dieu tri/Hoan tat).
IF COL_LENGTH(N'dbo.BenhNhan', N'trangThaiHoSo') IS NULL
BEGIN
    ALTER TABLE dbo.BenhNhan
      ADD trangThaiHoSo NVARCHAR(20) NOT NULL
          CONSTRAINT DF_BenhNhan_trangThaiHoSo DEFAULT N'Hoạt động' WITH VALUES;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_BenhNhan_trangThaiHoSo'
                 AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_BenhNhan_trangThaiHoSo
      CHECK (trangThaiHoSo IN (N'Hoạt động',N'Ngừng hồ sơ'));
END;
GO

-- Luu thoi luong de kiem tra giao nhau cua cac khoang hen.
-- Mac dinh 60 phut cho cac lich cu; form cu tuong thich se tiep tuc dung mac dinh nay.
IF COL_LENGTH(N'dbo.LichHen', N'thoiLuongPhut') IS NULL
BEGIN
    ALTER TABLE dbo.LichHen
      ADD thoiLuongPhut SMALLINT NOT NULL
          CONSTRAINT DF_LichHen_thoiLuongPhut DEFAULT (60) WITH VALUES;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_LichHen_thoiLuongPhut'
                 AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.LichHen WHERE thoiLuongPhut NOT BETWEEN 15 AND 240)
        PRINT N'Canh bao: co lich hen co thoiLuongPhut ngoai 15-240. Can ra soat truoc khi them CHECK.';
    ELSE
        ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_LichHen_thoiLuongPhut
          CHECK (thoiLuongPhut BETWEEN 15 AND 240);
END;
GO

-- Khoan cho xac minh chua phai la thoi diem thuc thu; cho phep ngayThanhToan NULL.
IF EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id=OBJECT_ID(N'dbo.ThanhToan')
      AND name=N'ngayThanhToan' AND is_nullable=0
)
BEGIN
    ALTER TABLE dbo.ThanhToan ALTER COLUMN ngayThanhToan DATETIME2 NULL;
END;
GO

-- Index phuc vu khoa/range scan khi kiem tra xung dot lich.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'IX_LichHen_NhanVien_Ngay' AND object_id=OBJECT_ID(N'dbo.LichHen'))
    CREATE INDEX IX_LichHen_NhanVien_Ngay ON dbo.LichHen(maNV,ngayHen,gioHen) INCLUDE(thoiLuongPhut,trangThai,maBN,phongKham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'IX_LichHen_Phong_Ngay' AND object_id=OBJECT_ID(N'dbo.LichHen'))
    CREATE INDEX IX_LichHen_Phong_Ngay ON dbo.LichHen(phongKham,ngayHen,gioHen) INCLUDE(thoiLuongPhut,trangThai,maNV,maBN);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'IX_LichHen_BenhNhan_Ngay' AND object_id=OBJECT_ID(N'dbo.LichHen'))
    CREATE INDEX IX_LichHen_BenhNhan_Ngay ON dbo.LichHen(maBN,ngayHen,gioHen) INCLUDE(thoiLuongPhut,trangThai,maNV,phongKham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'IX_LichLamViec_NhanVien_Ngay' AND object_id=OBJECT_ID(N'dbo.LichLamViec'))
    CREATE INDEX IX_LichLamViec_NhanVien_Ngay ON dbo.LichLamViec(maNV,ngayLamViec,gioBatDau,gioKetThuc) INCLUDE(trangThai,caLamViec);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'IX_ThanhToan_PhieuKham_TrangThai' AND object_id=OBJECT_ID(N'dbo.ThanhToan'))
    CREATE INDEX IX_ThanhToan_PhieuKham_TrangThai ON dbo.ThanhToan(maPK,trangThai,maTT);
GO

-- Ma giao dich thuc thu khong duoc trung; chi tao index neu du lieu cu khong co duplicate.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'UX_ThanhToan_maGiaoDich' AND object_id=OBJECT_ID(N'dbo.ThanhToan'))
BEGIN
    IF EXISTS (SELECT maGiaoDich FROM dbo.ThanhToan
               WHERE maGiaoDich IS NOT NULL
               GROUP BY maGiaoDich HAVING COUNT(*) > 1)
        PRINT N'Canh bao: co maGiaoDich trung trong du lieu cu; chua tao unique index.';
    ELSE
        CREATE UNIQUE INDEX UX_ThanhToan_maGiaoDich
            ON dbo.ThanhToan(maGiaoDich) WHERE maGiaoDich IS NOT NULL;
END;
GO

SELECT N'BenhNhan.trangThaiHoSo' AS DaCapNhat,
       CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NOT NULL THEN N'OK' ELSE N'MISSING' END AS KetQua
UNION ALL
SELECT N'LichHen.thoiLuongPhut', CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NOT NULL THEN N'OK' ELSE N'MISSING' END
UNION ALL
SELECT N'ThanhToan.ngayThanhToan nullable', CASE WHEN EXISTS (
    SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.ThanhToan')
      AND name=N'ngayThanhToan' AND is_nullable=1) THEN N'OK' ELSE N'MISSING' END;
GO
