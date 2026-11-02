-- MEDIDENT - MIGRATION CAC COT KY THUAT / DANH MUC PHONG CHO JAVA DAO
-- SQL Server 2012+. CHI BO SUNG SCHEMA/INDEX/LOOKUP ROWS; KHONG UPDATE/DELETE DU LIEU CU.
-- Cac cot nullable duoc bo sung KHONG backfill: ban ghi cu giu NULL; DAO dung ISNULL(..., gia tri mac dinh).
-- Rowversion la metadata do SQL Server tu cap cho tung dong, khong sua cac cot nghiep vu cu.
USE MediDent;
GO

-- 1. Bao dam cac bang nen ton tai truoc khi migration.
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
   OR OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
   OR OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
   OR OBJECT_ID(N'dbo.BangGiaDichVu',N'U') IS NULL
BEGIN
    RAISERROR(N'Thieu bang nen. Hay chay SQLData/01_MediDent.sql truoc.',16,1);
    RETURN;
END;
GO

-- 2. trangThaiHoSo: cot moi NULL tren ho so cu, khong gan gia tri hang loat.
IF COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL
BEGIN
    ALTER TABLE dbo.BenhNhan ADD trangThaiHoSo NVARCHAR(20) NULL
        CONSTRAINT DF_BenhNhan_trangThaiHoSo DEFAULT (N'Hoạt động');
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_BenhNhan_trangThaiHoSo'
                 AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.BenhNhan
               WHERE trangThaiHoSo IS NOT NULL
                 AND trangThaiHoSo NOT IN (N'Hoạt động',N'Ngừng hồ sơ'))
        PRINT N'CANH BAO: trangThaiHoSo co gia tri cu ngoai danh muc; bo qua CHECK, khong sua du lieu.';
    ELSE
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_BenhNhan_trangThaiHoSo
            CHECK (trangThaiHoSo IS NULL OR trangThaiHoSo IN (N'Hoạt động',N'Ngừng hồ sơ'));
END;
GO

-- 3. Thoi luong lich hen: NULL tren lich cu de khong backfill; Java da dung ISNULL(...,60).
IF COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL
BEGIN
    ALTER TABLE dbo.LichHen ADD thoiLuongPhut SMALLINT NULL
        CONSTRAINT DF_LichHen_thoiLuongPhut DEFAULT (60);
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_LichHen_thoiLuongPhut'
                 AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.LichHen
               WHERE thoiLuongPhut IS NOT NULL AND thoiLuongPhut NOT BETWEEN 15 AND 240)
        PRINT N'CANH BAO: co thoiLuongPhut cu ngoai 15-240; bo qua CHECK, khong sua du lieu.';
    ELSE
        ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_LichHen_thoiLuongPhut
            CHECK (thoiLuongPhut IS NULL OR thoiLuongPhut BETWEEN 15 AND 240);
END;
GO

-- 4. Rowversion phuc vu optimistic locking. Khong sua cac cot nghiep vu.
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.BenhNhan') AND name=N'phienBan')
    ALTER TABLE dbo.BenhNhan ADD phienBan ROWVERSION;
GO
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.NhanVien') AND name=N'phienBan')
    ALTER TABLE dbo.NhanVien ADD phienBan ROWVERSION;
GO

-- 5. Thoi diem ap dung gia: de NULL cho lich su cu, khong gan ngay/gio gia tao gia.
IF COL_LENGTH(N'dbo.BangGiaDichVu',N'thoiDiemApDung') IS NULL
    ALTER TABLE dbo.BangGiaDichVu ADD thoiDiemApDung DATETIME2 NULL;
GO

-- 6. Cot STT tiep nhan (fallback neu SQL 06 chua duoc chay).
IF COL_LENGTH(N'dbo.LichHen',N'soThuTuKham') IS NULL
    ALTER TABLE dbo.LichHen ADD soThuTuKham INT NULL;
GO

-- 7. Bang danh muc phong. Neu da co bang, chi kiem tra schema, khong ALTER cot cu.
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        tenPhong NVARCHAR(80) NOT NULL PRIMARY KEY,
        sucChua INT NOT NULL CONSTRAINT DF_MediDent_PhongKham_SucChua DEFAULT (2),
        trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_MediDent_PhongKham_TrangThai DEFAULT (N'Hoạt động'),
        CONSTRAINT CK_MediDent_PhongKham_SucChua CHECK (sucChua BETWEEN 1 AND 50),
        CONSTRAINT CK_MediDent_PhongKham_TrangThai CHECK (trangThai IN (N'Hoạt động',N'Ngừng hoạt động'))
    );
END;
GO
IF COL_LENGTH(N'dbo.PhongKham',N'tenPhong') IS NULL
   OR COL_LENGTH(N'dbo.PhongKham',N'sucChua') IS NULL
   OR COL_LENGTH(N'dbo.PhongKham',N'trangThai') IS NULL
BEGIN
    RAISERROR(N'dbo.PhongKham da ton tai nhung thieu cot DAO can. Khong tu sua schema cu.',16,1);
    RETURN;
END;
GO

-- 8. Tao cac dong danh muc phong con thieu tu TEN PHONG da co trong LichHen.
-- Chi INSERT dong lookup moi; khong UPDATE lich hen/ten phong du lieu cu.
-- Suc chua khoi tao = 1 (an toan/conservative). DBA can xac nhan suc chua thuc te truoc van hanh.
INSERT INTO dbo.PhongKham(tenPhong,sucChua,trangThai)
SELECT DISTINCT LTRIM(RTRIM(l.phongKham)),1,N'Hoạt động'
FROM dbo.LichHen l
WHERE NULLIF(LTRIM(RTRIM(l.phongKham)),N'') IS NOT NULL
  AND LEN(LTRIM(RTRIM(l.phongKham)))<=80
  AND NOT EXISTS (SELECT 1 FROM dbo.PhongKham p
                  WHERE p.tenPhong=LTRIM(RTRIM(l.phongKham)));
GO

-- Neu DB sach chua co lich/room, tao phong mac dinh cho lan cai dat dau tien.
-- Khong them phong mau khi da co benh nhan hoac lich hen trong DB.
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham)
   AND NOT EXISTS (SELECT 1 FROM dbo.LichHen)
   AND NOT EXISTS (SELECT 1 FROM dbo.BenhNhan)
BEGIN
    INSERT dbo.PhongKham(tenPhong,sucChua,trangThai)
    VALUES (N'Phòng 01',2,N'Hoạt động'),
           (N'Phòng 02',2,N'Hoạt động'),
           (N'Phòng 03',2,N'Hoạt động');
END;
GO

-- 9. CHECK don gia chi tiet dieu tri: chi tao khi bang ton tai va du lieu cu hop le.
IF OBJECT_ID(N'dbo.ChiTietDieuTri',N'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE name=N'CK_MediDent_ChiTietDieuTri_DonGia'
                     AND parent_object_id=OBJECT_ID(N'dbo.ChiTietDieuTri'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.ChiTietDieuTri WHERE donGia<0)
        PRINT N'CANH BAO: co donGia am trong ChiTietDieuTri; bo qua CHECK, khong sua du lieu.';
    ELSE
        ALTER TABLE dbo.ChiTietDieuTri WITH CHECK
            ADD CONSTRAINT CK_MediDent_ChiTietDieuTri_DonGia CHECK (donGia>=0);
END
ELSE IF OBJECT_ID(N'dbo.ChiTietDieuTri',N'U') IS NULL
    PRINT N'BO QUA CHECK donGia: chua co bang dbo.ChiTietDieuTri.';
GO

-- 10. Bao cao ket qua migration va cac gia tri lookup moi.
SELECT N'BenhNhan.trangThaiHoSo' AS Truong,
       CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NOT NULL THEN N'CO' ELSE N'THIEU' END AS TrangThai
UNION ALL SELECT N'BenhNhan.phienBan',CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'phienBan') IS NOT NULL THEN N'CO' ELSE N'THIEU' END
UNION ALL SELECT N'NhanVien.phienBan',CASE WHEN COL_LENGTH(N'dbo.NhanVien',N'phienBan') IS NOT NULL THEN N'CO' ELSE N'THIEU' END
UNION ALL SELECT N'LichHen.thoiLuongPhut',CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NOT NULL THEN N'CO' ELSE N'THIEU' END
UNION ALL SELECT N'LichHen.soThuTuKham',CASE WHEN COL_LENGTH(N'dbo.LichHen',N'soThuTuKham') IS NOT NULL THEN N'CO' ELSE N'THIEU' END
UNION ALL SELECT N'BangGiaDichVu.thoiDiemApDung',CASE WHEN COL_LENGTH(N'dbo.BangGiaDichVu',N'thoiDiemApDung') IS NOT NULL THEN N'CO' ELSE N'THIEU' END
UNION ALL SELECT N'PhongKham',CASE WHEN OBJECT_ID(N'dbo.PhongKham',N'U') IS NOT NULL THEN N'CO' ELSE N'THIEU' END;
GO
SELECT tenPhong,sucChua,trangThai FROM dbo.PhongKham ORDER BY tenPhong;
GO
