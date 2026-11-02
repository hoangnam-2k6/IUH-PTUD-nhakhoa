-- MEDIDENT - HOAN TAT DONG BO INDEX / KIEM TRA COT CHO QUANLYDAO
-- SQL Server 2012+. Chay SAU 13_DanhMucPhong_ThoiLuong_LichHen_SQL2012.sql.
-- CHI TAO INDEX/CONSTRAINT KHI DU LIEU CU PHU HOP. KHONG UPDATE/DELETE DU LIEU NGHIEP VU.
USE MediDent;
GO

IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
   OR OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
   OR OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
   OR OBJECT_ID(N'dbo.LichLamViec',N'U') IS NULL
   OR OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
BEGIN
    RAISERROR(N'Thieu bang nen (BenhNhan/NhanVien/LichHen/LichLamViec/ThanhToan). Hay chay SQL 01 va SQL 13 truoc.',16,1);
    RETURN;
END;
GO

-- Kiem tra cac cot ma cac index ben duoi se su dung. Dung lai neu schema cu khac,
-- khong tu them/sua cot cu de tranh phat sinh thay doi ngoai y muon.
IF COL_LENGTH(N'dbo.LichHen',N'maNV') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'ngayHen') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'gioHen') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'trangThai') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'maBN') IS NULL
   OR COL_LENGTH(N'dbo.LichHen',N'phongKham') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'maNV') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'ngayLamViec') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'gioBatDau') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'gioKetThuc') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'trangThai') IS NULL
   OR COL_LENGTH(N'dbo.LichLamViec',N'caLamViec') IS NULL
   OR COL_LENGTH(N'dbo.ThanhToan',N'maPK') IS NULL
   OR COL_LENGTH(N'dbo.ThanhToan',N'trangThai') IS NULL
   OR COL_LENGTH(N'dbo.ThanhToan',N'maTT') IS NULL
BEGIN
    RAISERROR(N'Schema cu thieu cot can thiet cho index. Dung migration; khong tu sua du lieu/schema.',16,1);
    RETURN;
END;
GO

-- Index phuc vu kiem tra lich theo nhan vien, phong va benh nhan.
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

-- Ma giao dich khong duoc trung. Neu duplicate cu ton tai, chi canh bao va bo qua index.
IF COL_LENGTH(N'dbo.ThanhToan',N'maGiaoDich') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'UX_ThanhToan_maGiaoDich' AND object_id=OBJECT_ID(N'dbo.ThanhToan'))
BEGIN
    IF EXISTS (SELECT maGiaoDich FROM dbo.ThanhToan
               WHERE maGiaoDich IS NOT NULL
               GROUP BY maGiaoDich HAVING COUNT(*)>1)
        PRINT N'CANH BAO: maGiaoDich duplicate trong du lieu cu; bo qua UNIQUE index, khong sua du lieu.';
    ELSE
        CREATE UNIQUE INDEX UX_ThanhToan_maGiaoDich
            ON dbo.ThanhToan(maGiaoDich) WHERE maGiaoDich IS NOT NULL;
END;
GO

-- Kiem tra rowversion/cot thoi luong/cot timestamp. Khong tu sua ban ghi cu.
SELECT N'BenhNhan.trangThaiHoSo' AS Truong,CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL THEN N'THIEU' ELSE N'CO' END AS TrangThai
UNION ALL SELECT N'BenhNhan.phienBan',CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'phienBan') IS NULL THEN N'THIEU' ELSE N'CO' END
UNION ALL SELECT N'NhanVien.phienBan',CASE WHEN COL_LENGTH(N'dbo.NhanVien',N'phienBan') IS NULL THEN N'THIEU' ELSE N'CO' END
UNION ALL SELECT N'LichHen.thoiLuongPhut',CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL THEN N'THIEU' ELSE N'CO' END
UNION ALL SELECT N'BangGiaDichVu.thoiDiemApDung',CASE WHEN COL_LENGTH(N'dbo.BangGiaDichVu',N'thoiDiemApDung') IS NULL THEN N'THIEU' ELSE N'CO' END
UNION ALL SELECT N'PhongKham',CASE WHEN OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL THEN N'THIEU' ELSE N'CO' END;
GO
