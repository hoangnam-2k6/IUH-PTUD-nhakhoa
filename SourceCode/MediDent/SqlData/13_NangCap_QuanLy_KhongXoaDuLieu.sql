-- MEDIDENT: migration giai doan Quan Ly. Chay SAU 01,04,07,08 va sao luu DB.
-- Idempotent; khong xoa bang/ho so/hoa don cu; SQL Server 2012+.
USE MediDent;
GO
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL OR OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
    RAISERROR(N'Chua co schema MediDent; chay 01_MediDent.sql truoc.',16,1);
GO
IF COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL
    ALTER TABLE dbo.BenhNhan ADD trangThaiHoSo NVARCHAR(30) NOT NULL CONSTRAINT DF_MD_BenhNhan_HoSo DEFAULT N'Hoạt động' WITH VALUES;
GO
IF COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL
    ALTER TABLE dbo.LichHen ADD thoiLuongPhut INT NOT NULL CONSTRAINT DF_MD_LichHen_Phut DEFAULT(60) WITH VALUES;
GO
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MD_LichHen_Phut' AND parent_object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    IF NOT EXISTS(SELECT 1 FROM dbo.LichHen WHERE thoiLuongPhut<15 OR thoiLuongPhut>240 OR thoiLuongPhut%15<>0)
        ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_MD_LichHen_Phut CHECK(thoiLuongPhut BETWEEN 15 AND 240 AND thoiLuongPhut%15=0);
    ELSE PRINT N'Canh bao: co lich cu sai thoi luong; khong them CHECK';
END;
GO
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
    CREATE TABLE dbo.PhongKham (maPhong INT IDENTITY(1,1) PRIMARY KEY, tenPhong NVARCHAR(80) NOT NULL UNIQUE, trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động', sucChua INT NOT NULL DEFAULT 1 CHECK(sucChua>0));
GO
-- Chi khoi tao danh muc phong khi DB chua co phong, khong ghi de phong cu.
IF NOT EXISTS(SELECT 1 FROM dbo.PhongKham)
    INSERT dbo.PhongKham(tenPhong,trangThai,sucChua) VALUES(N'Phòng 01',N'Hoạt động',1),(N'Phòng 02',N'Hoạt động',1),(N'Phòng 03',N'Hoạt động',1),(N'Phòng 04',N'Hoạt động',1),(N'Phòng 05',N'Hoạt động',1),(N'Phòng 06',N'Hoạt động',1);
GO
-- Luu ngay thuc thu NULL khi giao dich chua nhan tien: KHONG sua du lieu cu.
IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.ThanhToan') AND name=N'ngayThanhToan' AND is_nullable=0)
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        DECLARE @df SYSNAME;
        SELECT @df=dc.name FROM sys.default_constraints dc JOIN sys.columns col
          ON dc.parent_object_id=col.object_id AND dc.parent_column_id=col.column_id
          WHERE col.object_id=OBJECT_ID(N'dbo.ThanhToan') AND col.name=N'ngayThanhToan';
        IF @df IS NOT NULL
            EXEC (N'ALTER TABLE dbo.ThanhToan DROP CONSTRAINT ' + QUOTENAME(@df));
        ALTER TABLE dbo.ThanhToan ALTER COLUMN ngayThanhToan DATETIME2 NULL;
        IF NOT EXISTS (SELECT 1 FROM sys.default_constraints dc JOIN sys.columns col
             ON dc.parent_object_id=col.object_id AND dc.parent_column_id=col.column_id
             WHERE col.object_id=OBJECT_ID(N'dbo.ThanhToan') AND col.name=N'ngayThanhToan')
            ALTER TABLE dbo.ThanhToan ADD CONSTRAINT DF_MD_ThanhToan_NgayThu DEFAULT SYSDATETIME() FOR ngayThanhToan;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO
IF COL_LENGTH(N'dbo.YeuCauHuyPhieuThu',N'maTKNguoiGui') IS NULL
    ALTER TABLE dbo.YeuCauHuyPhieuThu ADD maTKNguoiGui INT NULL;
GO
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_MD_YeuCauHuy_NguoiGui')
    ALTER TABLE dbo.YeuCauHuyPhieuThu WITH CHECK ADD CONSTRAINT FK_MD_YeuCauHuy_NguoiGui FOREIGN KEY(maTKNguoiGui) REFERENCES dbo.TaiKhoan(maTK);
GO
-- Quyet dinh duyet huy trong vai tro Quan ly theo yeu cau moi duoc ghi NhatKyHeThong.
-- Duyet chi giao dich chua thuc thu, khong hoan tien/chinh sua hoa don da thanh toan.
-- Kiem tra du dieu kien truoc khi su dung giao dien:
SELECT name AS TenBang FROM sys.tables WHERE name IN('NhanVien','BenhNhan','DichVu','LichHen','LichLamViec','PhongKham','PhieuKham','ChiTietDieuTri','ThanhToan','HoaDon','ChiPhiHoatDong','YeuCauHuyPhieuThu','PhieuDuyetHuyPhieuThu');
SELECT N'BenhNhan.trangThaiHoSo' AS Truong, COL_LENGTH('dbo.BenhNhan','trangThaiHoSo') AS TinhTrang
UNION ALL SELECT N'LichHen.thoiLuongPhut',COL_LENGTH('dbo.LichHen','thoiLuongPhut');
GO
