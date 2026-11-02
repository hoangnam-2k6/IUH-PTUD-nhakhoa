-- MEDIDENT - DONG BO COT THIEU CHO PHIEN BAN 02/11/2026
-- SQL Server 2012 tro len. CHAY 1 LAN TREN BAN SAO LUU / DATABASE THU NGHIEM.
-- Khong DROP TABLE, khong xoa du lieu benh nhan, lich hen, hoa don.
-- Chi chay SAU khi da tao CSDL MediDent va cac bang goc.
USE MediDent;
GO

-- 1. trangThaiHoSo: tach Hoat dong/Ngung ho so khoi trang thai dieu tri.
IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
    RAISERROR(N'Thieu bang dbo.BenhNhan. Hay tao DB MediDent truoc.',16,1);
GO
IF COL_LENGTH(N'dbo.BenhNhan', N'trangThaiHoSo') IS NULL
BEGIN
    ALTER TABLE dbo.BenhNhan
    ADD trangThaiHoSo NVARCHAR(30) NOT NULL
        CONSTRAINT DF_MediDent_BenhNhan_TrangThaiHoSo DEFAULT (N'Hoạt động') WITH VALUES;
END;
GO
-- Chi dong bo cac ho so cu neu trangThai cu dung ro nghia 'Ngung ho so'.
UPDATE dbo.BenhNhan SET trangThaiHoSo=N'Ngừng hồ sơ'
WHERE trangThai=N'Ngừng hồ sơ' AND trangThaiHoSo<>N'Ngừng hồ sơ';
GO

-- 2. Rowversion giup hai may khong ghi de ho so cua nhau.
IF COL_LENGTH(N'dbo.BenhNhan', N'phienBan') IS NULL
    ALTER TABLE dbo.BenhNhan ADD phienBan ROWVERSION;
GO
IF COL_LENGTH(N'dbo.NhanVien', N'phienBan') IS NULL
    ALTER TABLE dbo.NhanVien ADD phienBan ROWVERSION;
GO

-- 3. thoiLuongPhut: tao/doi lich, kiem tra trung khoang gio.
IF COL_LENGTH(N'dbo.LichHen', N'thoiLuongPhut') IS NULL
BEGIN
    ALTER TABLE dbo.LichHen
    ADD thoiLuongPhut INT NOT NULL
        CONSTRAINT DF_MediDent_LichHen_ThoiLuong DEFAULT (60) WITH VALUES;
END;
GO
IF NOT EXISTS (
    SELECT 1 FROM sys.check_constraints
    WHERE parent_object_id=OBJECT_ID(N'dbo.LichHen')
      AND name=N'CK_MediDent_LichHen_ThoiLuong')
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.LichHen
                   WHERE thoiLuongPhut<15 OR thoiLuongPhut>240)
        ALTER TABLE dbo.LichHen WITH CHECK ADD
          CONSTRAINT CK_MediDent_LichHen_ThoiLuong
          CHECK (thoiLuongPhut BETWEEN 15 AND 240);
    ELSE
        PRINT N'Co thoi luong lich hen khong hop le, chua them CHECK.';
END;
GO

-- 4. thoiDiemApDung: ghi duoc nhieu lan doi gia trong cung ngay.
IF COL_LENGTH(N'dbo.BangGiaDichVu', N'thoiDiemApDung') IS NULL
    ALTER TABLE dbo.BangGiaDichVu ADD thoiDiemApDung DATETIME2 NULL;
GO

-- 5. Danh muc phong DUNG CHUNG, khong can menu Quan ly phong kham.
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        tenPhong NVARCHAR(80) NOT NULL PRIMARY KEY,
        sucChua INT NOT NULL CONSTRAINT DF_MediDent_Phong_SucChua DEFAULT (2),
        trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_MediDent_Phong_TrangThai DEFAULT (N'Hoạt động'),
        CONSTRAINT CK_MediDent_Phong_SucChua CHECK (sucChua BETWEEN 1 AND 50),
        CONSTRAINT CK_MediDent_Phong_TrangThai CHECK (trangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END;
GO
INSERT dbo.PhongKham (tenPhong,sucChua,trangThai)
SELECT DISTINCT LTRIM(RTRIM(l.phongKham)), 2, N'Hoạt động'
FROM dbo.LichHen l
WHERE NULLIF(LTRIM(RTRIM(l.phongKham)), N'') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM dbo.PhongKham p
      WHERE p.tenPhong=LTRIM(RTRIM(l.phongKham)));
GO
-- Neu DB moi chua co lich hen, cung cap 3 phong TEST co suc chua gia dinh 2.
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham)
BEGIN
    INSERT dbo.PhongKham(tenPhong,sucChua,trangThai)
    VALUES(N'Phòng 01',2,N'Hoạt động'),
          (N'Phòng 02',2,N'Hoạt động'),
          (N'Phòng 03',2,N'Hoạt động');
END;
GO

-- 6. Cot cap so kham cho Le tan (neu chua chay migration tiep nhan).
IF COL_LENGTH(N'dbo.LichHen', N'soThuTuKham') IS NULL
    ALTER TABLE dbo.LichHen ADD soThuTuKham INT NULL;
GO

-- 7. KIEM TRA KET QUA: ca 5 cot quan trong phai tra ve Co.
SELECT N'BenhNhan.trangThaiHoSo' AS Truong,
       CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL THEN N'Thieu' ELSE N'Co' END AS TrangThai
UNION ALL SELECT N'BenhNhan.phienBan',CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'phienBan') IS NULL THEN N'Thieu' ELSE N'Co' END
UNION ALL SELECT N'NhanVien.phienBan',CASE WHEN COL_LENGTH(N'dbo.NhanVien',N'phienBan') IS NULL THEN N'Thieu' ELSE N'Co' END
UNION ALL SELECT N'LichHen.thoiLuongPhut',CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL THEN N'Thieu' ELSE N'Co' END
UNION ALL SELECT N'BangGiaDichVu.thoiDiemApDung',CASE WHEN COL_LENGTH(N'dbo.BangGiaDichVu',N'thoiDiemApDung') IS NULL THEN N'Thieu' ELSE N'Co' END
UNION ALL SELECT N'PhongKham',CASE WHEN OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL THEN N'Thieu' ELSE N'Co' END;
GO

-- 8. CAC DIEU KIEN DE TAO LICH: co bac si DANG LAM VIEC va ca DA XEP LICH.
SELECT maNV, hoTen, chucVu, trangThai FROM dbo.NhanVien
WHERE chucVu=N'Bác sĩ' ORDER BY maNV;
SELECT l.maLichLV,n.hoTen,l.ngayLamViec,l.gioBatDau,l.gioKetThuc,l.trangThai
FROM dbo.LichLamViec l JOIN dbo.NhanVien n ON n.maNV=l.maNV
WHERE l.ngayLamViec>=CONVERT(date,GETDATE())
ORDER BY l.ngayLamViec,l.gioBatDau;
GO
