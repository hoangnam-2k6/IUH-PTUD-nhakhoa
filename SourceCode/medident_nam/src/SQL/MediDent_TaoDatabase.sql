USE MediDent;
GO

-- TẠO BẢNG PHÒNG KHÁM
IF OBJECT_ID(N'dbo.PhongKham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        tenPhong NVARCHAR(50) NOT NULL,
        trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
        ghiChu NVARCHAR(255) NULL,
        CONSTRAINT PK_PhongKham PRIMARY KEY (tenPhong),
        CONSTRAINT CK_Phong_TrangThai CHECK (
            trangThai IN (N'Hoạt động', N'Ngừng sử dụng')
        )
    );
END;
GO

-- BỔ SUNG PHÒNG KHÁM MẪU
INSERT INTO dbo.PhongKham(tenPhong, trangThai)
SELECT v.tenPhong, N'Hoạt động'
FROM (VALUES
    (N'Phòng 01'), (N'Phòng 02'),
    (N'Phòng 03'), (N'Phòng 04')
) v(tenPhong)
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.PhongKham p
    WHERE p.tenPhong = v.tenPhong
);
GO

-- BỔ SUNG PHÒNG ĐÃ SỬ DỤNG TRONG LỊCH
INSERT INTO dbo.PhongKham(tenPhong, trangThai)
SELECT DISTINCT x.phongKham, N'Hoạt động'
FROM (
    SELECT phongKham FROM dbo.LichHen
    UNION
    SELECT phongKham FROM dbo.LichLamViec
) x
WHERE x.phongKham IS NOT NULL
AND LTRIM(RTRIM(x.phongKham)) <> N''
AND NOT EXISTS (
    SELECT 1 FROM dbo.PhongKham p
    WHERE p.tenPhong = x.phongKham
);
GO


-- LỊCH HẸN -> PHÒNG KHÁM
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_LH_Phong'
    AND parent_object_id = OBJECT_ID(N'dbo.LichHen')
)
BEGIN
    ALTER TABLE dbo.LichHen WITH CHECK
    ADD CONSTRAINT FK_LH_Phong
    FOREIGN KEY (phongKham)
    REFERENCES dbo.PhongKham(tenPhong);
END;
GO

-- LỊCH LÀM VIỆC -> PHÒNG KHÁM
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys
    WHERE name = N'FK_LLV_Phong'
    AND parent_object_id = OBJECT_ID(N'dbo.LichLamViec')
)
BEGIN
    ALTER TABLE dbo.LichLamViec WITH CHECK
    ADD CONSTRAINT FK_LLV_Phong
    FOREIGN KEY (phongKham)
    REFERENCES dbo.PhongKham(tenPhong);
END;
GO



USE MediDent;
GO

SELECT COUNT(*) AS TongBang
FROM sys.tables
WHERE schema_id = SCHEMA_ID(N'dbo');

SELECT COUNT(*) AS TongPK
FROM sys.key_constraints
WHERE type = 'PK'
AND parent_object_id IN (
    SELECT object_id FROM sys.tables
    WHERE schema_id = SCHEMA_ID(N'dbo')
);

SELECT COUNT(*) AS TongFK
FROM sys.foreign_keys
WHERE parent_object_id IN (
    SELECT object_id FROM sys.tables
    WHERE schema_id = SCHEMA_ID(N'dbo')
);

SELECT fk.name AS TenFK,
       OBJECT_NAME(fk.parent_object_id) AS BangCon,
       OBJECT_NAME(fk.referenced_object_id) AS BangCha,
       fk.is_disabled,
       fk.is_not_trusted
FROM sys.foreign_keys fk
ORDER BY BangCon, TenFK;