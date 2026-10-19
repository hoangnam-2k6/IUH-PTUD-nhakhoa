-- Lưu phân cấp hỗ trợ và chống đơn hủy trùng đang chờ.

USE MediDent;
GO

IF COL_LENGTH(N'dbo.VaiTro', N'capBacHoTro') IS NULL
BEGIN
    ALTER TABLE dbo.VaiTro
    ADD capBacHoTro INT NOT NULL
        CONSTRAINT DF_VaiTro_CapBacHoTro DEFAULT (0) WITH VALUES;
END;
GO
UPDATE dbo.VaiTro
SET capBacHoTro = CASE
    WHEN tenVaiTro = N'Quản trị viên' THEN 3
    WHEN tenVaiTro IN (N'Quản lý phòng khám', N'Quản lý') THEN 2
    WHEN tenVaiTro = N'Lễ tân' THEN 1
    ELSE capBacHoTro
END
WHERE tenVaiTro IN (N'Quản trị viên', N'Quản lý phòng khám', N'Quản lý', N'Lễ tân');
GO

IF COL_LENGTH(N'dbo.YeuCauHuyPhieuThu', N'maTKNguoiGui') IS NULL
BEGIN
    ALTER TABLE dbo.YeuCauHuyPhieuThu
    ADD maTKNguoiGui INT NULL;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys
               WHERE name=N'FK_YeuCauHuyPhieuThu_NguoiGui'
                 AND parent_object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    ALTER TABLE dbo.YeuCauHuyPhieuThu WITH CHECK
    ADD CONSTRAINT FK_YeuCauHuyPhieuThu_NguoiGui
        FOREIGN KEY (maTKNguoiGui) REFERENCES dbo.TaiKhoan(maTK);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name=N'IX_YeuCauHuy_NguoiGui'
                 AND object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    CREATE INDEX IX_YeuCauHuy_NguoiGui
    ON dbo.YeuCauHuyPhieuThu(maTKNguoiGui, ngayYeuCau);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name=N'UX_YeuCauHuy_ChoDuyet_maTT'
                 AND object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    IF NOT EXISTS (
        SELECT maTT FROM dbo.YeuCauHuyPhieuThu
        WHERE trangThai=N'Chờ duyệt'
        GROUP BY maTT HAVING COUNT(*) > 1
    )
    BEGIN
        CREATE UNIQUE INDEX UX_YeuCauHuy_ChoDuyet_maTT
        ON dbo.YeuCauHuyPhieuThu(maTT)
        WHERE trangThai=N'Chờ duyệt';
    END
    ELSE PRINT N'CANH BAO: Có nhiều đơn Chờ duyệt cho cùng maTT. Hãy xử lý thủ công trước khi tạo unique index.';
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Quyen WHERE maQuyen='HOTRO')
    INSERT INTO dbo.Quyen(maQuyen,tenQuyen)
    VALUES('HOTRO',N'Trung tâm hỗ trợ và phê duyệt');
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro, 'HOTRO'
FROM dbo.VaiTro v
WHERE v.tenVaiTro=N'Quản trị viên'
  AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen vq
                  WHERE vq.maVaiTro=v.maVaiTro AND vq.maQuyen='HOTRO');
GO

SELECT maVaiTro, tenVaiTro, capBacHoTro
FROM dbo.VaiTro ORDER BY capBacHoTro DESC, maVaiTro;
SELECT TOP 30 maYC, maTT, maTKNguoiGui, ngayYeuCau, trangThai
FROM dbo.YeuCauHuyPhieuThu ORDER BY maYC DESC;
GO
