-- Tạo ràng buộc cấp số thứ tự duy nhất theo ngày tiếp nhận.

USE MediDent;
GO

-- Chỉ tạo bảng LichHen khi chưa tồn tại trong CSDL.
IF OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
BEGIN
    RAISERROR(N'Chua ton tai dbo.LichHen. Hay tao database truoc.',16,1);
END
GO

IF OBJECT_ID(N'dbo.LichHen',N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.LichHen',N'soThuTuKham') IS NULL
BEGIN
    ALTER TABLE dbo.LichHen ADD soThuTuKham INT NULL;
    PRINT N'Da them cot soThuTuKham.';
END
GO

IF OBJECT_ID(N'dbo.LichHen',N'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.check_constraints
                   WHERE name=N'CK_LichHen_SoThuTuKham'
                     AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    ALTER TABLE dbo.LichHen ADD CONSTRAINT CK_LichHen_SoThuTuKham
        CHECK (soThuTuKham IS NULL OR soThuTuKham > 0);
END
GO

IF OBJECT_ID(N'dbo.LichHen',N'U') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.indexes
                   WHERE name=N'UX_LichHen_Ngay_STT'
                     AND object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX UX_LichHen_Ngay_STT
    ON dbo.LichHen(ngayHen,soThuTuKham)
    WHERE soThuTuKham IS NOT NULL;
END
GO

SELECT TOP 20 maLichHen,maBN,ngayHen,phongKham,soThuTuKham,trangThai
FROM dbo.LichHen ORDER BY maLichHen DESC;
GO
