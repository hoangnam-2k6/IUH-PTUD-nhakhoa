-- =====================================================================
-- MEDIDENT - PHÂN HỆ LỄ TÂN
-- FILE SQL DUY NHẤT: tạo CSDL, bảng, chỉ mục, dữ liệu mẫu và tài khoản
-- ứng dụng. Chạy một lần trên SQL Server bằng tài khoản có quyền sysadmin
-- (ví dụ xác thực Windows).
--
--   sqlcmd -S ".\SQLEXPRESS" -E -C -f 65001 -i SqlData\MediDentLeTan.sql
--
-- Script có thể chạy lặp lại: phần tạo bảng/dữ liệu đều có kiểm tra tồn tại.
-- Tài khoản đăng nhập ứng dụng:
--   letan   / 123456   (Lễ tân)
--   letan2  / 123456   (Lễ tân)
--   quantri / 123456   (Quản trị viên - chỉ lưu trong dữ liệu, phân hệ Lễ tân chặn đăng nhập)
--
-- Tài khoản SQL Server dùng cho ứng dụng:
--   medident_app / MediDent@123
-- (cấu hình kết nối được nhúng trong src/ConnectDB/KetNoi.java)
-- =====================================================================

SET ANSI_NULLS ON;
GO
SET QUOTED_IDENTIFIER ON;
GO
SET NOCOUNT ON;
GO

-- =====================================================================
-- 1. TẠO CƠ SỞ DỮ LIỆU
-- =====================================================================
IF DB_ID(N'MediDentLeTan') IS NULL
    CREATE DATABASE MediDentLeTan COLLATE Vietnamese_CI_AS;
GO

USE MediDentLeTan;
GO

-- =====================================================================
-- 2. TẠO BẢNG, KHÓA NGOẠI, RÀNG BUỘC VÀ CHỈ MỤC
-- =====================================================================

-- ---------------------------------------------------------------------
-- Danh mục phòng khám / bác sĩ / dịch vụ
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.PhongKham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhongKham (
        MaPhong    INT IDENTITY(1,1) PRIMARY KEY,
        TenPhong   NVARCHAR(50) NOT NULL UNIQUE,
        SucChua    INT NOT NULL CONSTRAINT DF_PhongKham_SucChua DEFAULT 20,
        TrangThai  NVARCHAR(20) NOT NULL CONSTRAINT DF_PhongKham_TrangThai DEFAULT N'Hoạt động',
        CONSTRAINT CK_PhongKham_SucChua CHECK (SucChua > 0),
        CONSTRAINT CK_PhongKham_TrangThai CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END
GO

IF OBJECT_ID(N'dbo.BacSi', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BacSi (
        MaBS       INT IDENTITY(1,1) PRIMARY KEY,
        HoTen      NVARCHAR(100) NOT NULL,
        ChuyenKhoa NVARCHAR(100) NULL,
        TrangThai  NVARCHAR(20) NOT NULL CONSTRAINT DF_BacSi_TrangThai DEFAULT N'Hoạt động',
        CONSTRAINT CK_BacSi_TrangThai CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END
GO

IF OBJECT_ID(N'dbo.DichVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DichVu (
        MaDV       INT IDENTITY(1,1) PRIMARY KEY,
        TenDichVu  NVARCHAR(200) NOT NULL UNIQUE,
        DonGia     DECIMAL(18,0) NOT NULL,
        TrangThai  NVARCHAR(20) NOT NULL CONSTRAINT DF_DichVu_TrangThai DEFAULT N'Hoạt động',
        CONSTRAINT CK_DichVu_DonGia CHECK (DonGia >= 0),
        CONSTRAINT CK_DichVu_TrangThai CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END
GO

-- ---------------------------------------------------------------------
-- Mã giảm giá do quản lý tạo sẵn (hóa đơn chỉ chọn từ danh mục này)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.MaGiamGia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.MaGiamGia (
        MaGiam     INT IDENTITY(1,1) PRIMARY KEY,
        Ma         NVARCHAR(30) NOT NULL UNIQUE,
        Ten        NVARCHAR(100) NOT NULL,
        LoaiGiam   NVARCHAR(20) NOT NULL,
        GiaTri     DECIMAL(18,2) NOT NULL,
        TrangThai  NVARCHAR(20) NOT NULL CONSTRAINT DF_MaGiamGia_TrangThai DEFAULT N'Hoạt động',
        CONSTRAINT CK_MaGiamGia_LoaiGiam CHECK (LoaiGiam IN (N'Phần trăm', N'Số tiền')),
        CONSTRAINT CK_MaGiamGia_GiaTri CHECK (GiaTri >= 0),
        CONSTRAINT CK_MaGiamGia_TrangThai CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END
GO

-- ---------------------------------------------------------------------
-- Tài khoản đăng nhập (mật khẩu lưu dạng SHA-256, không lưu mật khẩu gốc)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.TaiKhoan', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TaiKhoan (
        MaTK             INT IDENTITY(1,1) PRIMARY KEY,
        TenDangNhap      VARCHAR(50) NOT NULL UNIQUE,
        MatKhauHash      CHAR(64) NOT NULL,
        HoTen            NVARCHAR(100) NOT NULL,
        VaiTro           NVARCHAR(30) NOT NULL,
        TrangThai        NVARCHAR(20) NOT NULL CONSTRAINT DF_TaiKhoan_TrangThai DEFAULT N'Hoạt động',
        NgayTao          DATETIME2 NOT NULL CONSTRAINT DF_TaiKhoan_NgayTao DEFAULT SYSDATETIME(),
        NgayDangNhapCuoi DATETIME2 NULL,
        CONSTRAINT CK_TaiKhoan_VaiTro CHECK (VaiTro IN (N'Lễ tân', N'Quản trị viên')),
        CONSTRAINT CK_TaiKhoan_TrangThai CHECK (TrangThai IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
END
GO

-- ---------------------------------------------------------------------
-- Bệnh nhân
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BenhNhan (
        MaBN            INT IDENTITY(1,1) PRIMARY KEY,
        HoTen           NVARCHAR(100) NOT NULL,
        NgaySinh        DATE NULL,
        GioiTinh        NVARCHAR(10) NULL,
        SoDienThoai     VARCHAR(15) NULL,
        DiaChi          NVARCHAR(200) NULL,
        CCCD            VARCHAR(12) NULL,              -- CMND 9 số hoặc CCCD 12 số
        Email           VARCHAR(100) NULL,
        NgheNghiep      NVARCHAR(100) NULL,
        TenNguoiLienHe  NVARCHAR(100) NULL,
        SdtNguoiLienHe  VARCHAR(15) NULL,
        DiUng           NVARCHAR(300) NULL,
        TienSuBenh      NVARCHAR(500) NULL,
        GhiChu          NVARCHAR(300) NULL,
        TrangThaiHoSo   NVARCHAR(20) NOT NULL CONSTRAINT DF_BenhNhan_TrangThai DEFAULT N'Hoạt động',
        NgayTao         DATETIME2 NOT NULL CONSTRAINT DF_BenhNhan_NgayTao DEFAULT SYSDATETIME(),
        CONSTRAINT CK_BenhNhan_TrangThai CHECK (TrangThaiHoSo IN (N'Hoạt động', N'Ngừng hoạt động'))
    );
    CREATE INDEX IX_BenhNhan_SoDienThoai ON dbo.BenhNhan(SoDienThoai);
    CREATE UNIQUE INDEX UX_BenhNhan_CCCD ON dbo.BenhNhan(CCCD) WHERE CCCD IS NOT NULL;
END
GO

-- ---------------------------------------------------------------------
-- Lịch hẹn (đặt trước khi bệnh nhân đến)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.LichHen', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichHen (
        MaLH       INT IDENTITY(1,1) PRIMARY KEY,
        MaBN       INT NOT NULL,
        MaBS       INT NOT NULL,
        NgayGio    DATETIME2 NOT NULL,
        TrangThai  NVARCHAR(30) NOT NULL CONSTRAINT DF_LichHen_TrangThai DEFAULT N'Chờ xác nhận',
        YeuCau     NVARCHAR(20) NULL,
        NgayGioMoi DATETIME2 NULL,
        GhiChu     NVARCHAR(200) NULL,
        MaTK       INT NULL,
        CONSTRAINT FK_LichHen_BenhNhan FOREIGN KEY (MaBN) REFERENCES dbo.BenhNhan(MaBN),
        CONSTRAINT FK_LichHen_BacSi FOREIGN KEY (MaBS) REFERENCES dbo.BacSi(MaBS),
        CONSTRAINT FK_LichHen_TaiKhoan FOREIGN KEY (MaTK) REFERENCES dbo.TaiKhoan(MaTK),
        CONSTRAINT CK_LichHen_TrangThai CHECK (TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận', N'Đã đến', N'Hoàn thành', N'Đã hủy')),
        CONSTRAINT CK_LichHen_YeuCau CHECK (YeuCau IS NULL OR YeuCau IN (N'Đổi lịch', N'Hủy lịch'))
    );
    CREATE INDEX IX_LichHen_BacSi_NgayGio ON dbo.LichHen(MaBS, NgayGio);
    CREATE INDEX IX_LichHen_NgayGio ON dbo.LichHen(NgayGio);
END
GO

-- ---------------------------------------------------------------------
-- Phiếu khám trong ngày (cấp số thứ tự)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.PhieuKham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhieuKham (
        MaPK      INT IDENTITY(1,1) PRIMARY KEY,
        MaBN      INT NOT NULL,
        MaPhong   INT NULL,
        MaLH      INT NULL,                                -- phiếu được tạo từ lịch hẹn nào
        NgayKham  DATE NOT NULL CONSTRAINT DF_PhieuKham_NgayKham DEFAULT CAST(GETDATE() AS DATE),
        SoThuTu   INT NOT NULL,
        TrangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_PhieuKham_TrangThai DEFAULT N'Chờ khám',
        GhiChu    NVARCHAR(300) NULL,
        CONSTRAINT FK_PhieuKham_BenhNhan FOREIGN KEY (MaBN) REFERENCES dbo.BenhNhan(MaBN),
        CONSTRAINT FK_PhieuKham_PhongKham FOREIGN KEY (MaPhong) REFERENCES dbo.PhongKham(MaPhong),
        CONSTRAINT FK_PhieuKham_LichHen FOREIGN KEY (MaLH) REFERENCES dbo.LichHen(MaLH),
        CONSTRAINT UQ_PhieuKham_Ngay_STT UNIQUE (NgayKham, SoThuTu),
        CONSTRAINT CK_PhieuKham_TrangThai CHECK (TrangThai IN (N'Chờ khám', N'Đang khám', N'Hoàn thành', N'Đã hủy'))
    );
    CREATE INDEX IX_PhieuKham_NgayKham ON dbo.PhieuKham(NgayKham);
    CREATE INDEX IX_PhieuKham_MaBN ON dbo.PhieuKham(MaBN);
END
GO

-- ---------------------------------------------------------------------
-- Dịch vụ chỉ định khi tiếp nhận (nạp sẵn vào hóa đơn)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.PhieuKhamDichVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhieuKhamDichVu (
        MaPK INT NOT NULL,
        MaDV INT NOT NULL,
        CONSTRAINT PK_PhieuKhamDichVu PRIMARY KEY (MaPK, MaDV),
        CONSTRAINT FK_PhieuKhamDichVu_PhieuKham FOREIGN KEY (MaPK)
            REFERENCES dbo.PhieuKham(MaPK) ON DELETE CASCADE,
        CONSTRAINT FK_PhieuKhamDichVu_DichVu FOREIGN KEY (MaDV)
            REFERENCES dbo.DichVu(MaDV)
    );
    CREATE INDEX IX_PhieuKhamDichVu_MaDV ON dbo.PhieuKhamDichVu(MaDV);
END
GO

-- ---------------------------------------------------------------------
-- Hồ sơ kết quả khám (do bác sĩ/lễ tân nhập sau khi khám)
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.KetQuaKham', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.KetQuaKham (
        MaPK           INT NOT NULL PRIMARY KEY,
        MaBS           INT NULL,
        TrieuChung     NVARCHAR(1000) NULL,
        KetQuaLamSang  NVARCHAR(2000) NULL,
        ChanDoan       NVARCHAR(1000) NULL,
        HuongDieuTri   NVARCHAR(2000) NULL,
        DonThuoc       NVARCHAR(2000) NULL,
        GhiChu         NVARCHAR(1000) NULL,
        NgayTaiKham    DATE NULL,
        NgayCapNhat    DATETIME2 NOT NULL CONSTRAINT DF_KetQuaKham_NgayCapNhat DEFAULT SYSDATETIME(),
        CONSTRAINT FK_KetQuaKham_PhieuKham FOREIGN KEY (MaPK) REFERENCES dbo.PhieuKham(MaPK),
        CONSTRAINT FK_KetQuaKham_BacSi FOREIGN KEY (MaBS) REFERENCES dbo.BacSi(MaBS)
    );
END
GO

-- ---------------------------------------------------------------------
-- Hóa đơn và chi tiết hóa đơn
-- ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.HoaDon (
        MaHD          INT IDENTITY(1,1) PRIMARY KEY,
        MaPK          INT NOT NULL,
        MaTK          INT NULL,                                  -- người lập hóa đơn
        NgayLap       DATETIME2 NOT NULL CONSTRAINT DF_HoaDon_NgayLap DEFAULT SYSDATETIME(),
        TongTien      DECIMAL(18,0) NOT NULL,                    -- số phải trả sau giảm giá
        GiamGia       DECIMAL(18,0) NOT NULL CONSTRAINT DF_HoaDon_GiamGia DEFAULT 0,
        MaGiam        INT NULL,                                  -- mã giảm giá chọn từ danh mục
        PhuongThucTT  NVARCHAR(30) NULL,
        NgayThanhToan DATETIME2 NULL,
        TienKhachDua  DECIMAL(18,0) NULL,
        TienThua      DECIMAL(18,0) NULL,
        TrangThai     NVARCHAR(30) NOT NULL CONSTRAINT DF_HoaDon_TrangThai DEFAULT N'Chưa thanh toán',
        GhiChu        NVARCHAR(300) NULL,
        LyDoHuy       NVARCHAR(300) NULL,
        CONSTRAINT FK_HoaDon_PhieuKham FOREIGN KEY (MaPK) REFERENCES dbo.PhieuKham(MaPK),
        CONSTRAINT FK_HoaDon_TaiKhoan FOREIGN KEY (MaTK) REFERENCES dbo.TaiKhoan(MaTK),
        CONSTRAINT FK_HoaDon_MaGiamGia FOREIGN KEY (MaGiam) REFERENCES dbo.MaGiamGia(MaGiam),
        CONSTRAINT CK_HoaDon_TongTien CHECK (TongTien >= 0),
        CONSTRAINT CK_HoaDon_GiamGia CHECK (GiamGia >= 0),
        CONSTRAINT CK_HoaDon_TrangThai CHECK (TrangThai IN (N'Chưa thanh toán', N'Đã thanh toán', N'Đã hủy')),
        CONSTRAINT CK_HoaDon_PhuongThuc CHECK (PhuongThucTT IS NULL OR PhuongThucTT IN (N'Tiền mặt', N'Chuyển khoản', N'Thẻ')),
        CONSTRAINT CK_HoaDon_TienKhachDua CHECK (TienKhachDua IS NULL OR TienKhachDua >= 0),
        CONSTRAINT CK_HoaDon_TienThua CHECK (TienThua IS NULL OR TienThua >= 0)
    );
    CREATE UNIQUE INDEX UX_HoaDon_MaPK_Active ON dbo.HoaDon(MaPK) WHERE TrangThai <> N'Đã hủy';
    CREATE INDEX IX_HoaDon_NgayLap ON dbo.HoaDon(NgayLap);
END
GO

IF OBJECT_ID(N'dbo.ChiTietHoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietHoaDon (
        MaCT       INT IDENTITY(1,1) PRIMARY KEY,
        MaHD       INT NOT NULL,
        MaDV       INT NULL,
        TenDichVu  NVARCHAR(200) NOT NULL,     -- lưu tên tại thời điểm lập hóa đơn
        SoLuong    INT NOT NULL,
        DonGia     DECIMAL(18,0) NOT NULL,
        CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (MaHD) REFERENCES dbo.HoaDon(MaHD) ON DELETE CASCADE,
        CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (MaDV) REFERENCES dbo.DichVu(MaDV),
        CONSTRAINT CK_ChiTietHoaDon_SoLuong CHECK (SoLuong > 0),
        CONSTRAINT CK_ChiTietHoaDon_DonGia CHECK (DonGia >= 0)
    );
    CREATE INDEX IX_ChiTietHoaDon_MaHD ON dbo.ChiTietHoaDon(MaHD);
END
GO

-- =====================================================================
-- 3. NÂNG CẤP BỔ SUNG CHO CSDL CŨ (chạy an toàn, không xóa dữ liệu)
-- =====================================================================
IF COL_LENGTH('dbo.PhongKham', 'SucChua') IS NULL
    ALTER TABLE dbo.PhongKham ADD SucChua INT NOT NULL
        CONSTRAINT DF_PhongKham_SucChua DEFAULT 20;
GO
IF COL_LENGTH('dbo.PhongKham', 'TrangThai') IS NULL
    ALTER TABLE dbo.PhongKham ADD TrangThai NVARCHAR(20) NOT NULL
        CONSTRAINT DF_PhongKham_TrangThai DEFAULT N'Hoạt động';
GO
IF COL_LENGTH('dbo.BacSi', 'TrangThai') IS NULL
    ALTER TABLE dbo.BacSi ADD TrangThai NVARCHAR(20) NOT NULL
        CONSTRAINT DF_BacSi_TrangThai DEFAULT N'Hoạt động';
GO
IF COL_LENGTH('dbo.BenhNhan', 'TrangThaiHoSo') IS NULL
    ALTER TABLE dbo.BenhNhan ADD TrangThaiHoSo NVARCHAR(20) NOT NULL
        CONSTRAINT DF_BenhNhan_TrangThai DEFAULT N'Hoạt động';
GO
IF COL_LENGTH('dbo.PhieuKham', 'MaLH') IS NULL
    ALTER TABLE dbo.PhieuKham ADD MaLH INT NULL;
GO
IF COL_LENGTH('dbo.PhieuKham', 'GhiChu') IS NULL
    ALTER TABLE dbo.PhieuKham ADD GhiChu NVARCHAR(300) NULL;
GO
IF COL_LENGTH('dbo.LichHen', 'MaTK') IS NULL
    ALTER TABLE dbo.LichHen ADD MaTK INT NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'MaTK') IS NULL
    ALTER TABLE dbo.HoaDon ADD MaTK INT NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'TienKhachDua') IS NULL
    ALTER TABLE dbo.HoaDon ADD TienKhachDua DECIMAL(18,0) NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'TienThua') IS NULL
    ALTER TABLE dbo.HoaDon ADD TienThua DECIMAL(18,0) NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'GhiChu') IS NULL
    ALTER TABLE dbo.HoaDon ADD GhiChu NVARCHAR(300) NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'LyDoHuy') IS NULL
    ALTER TABLE dbo.HoaDon ADD LyDoHuy NVARCHAR(300) NULL;
GO
IF COL_LENGTH('dbo.HoaDon', 'MaGiam') IS NULL
    ALTER TABLE dbo.HoaDon ADD MaGiam INT NULL;
GO
IF COL_LENGTH('dbo.ChiTietHoaDon', 'MaDV') IS NULL
    ALTER TABLE dbo.ChiTietHoaDon ADD MaDV INT NULL;
GO

-- Khóa ngoại còn thiếu (kiểm tra theo cột để tránh thêm trùng).
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id
                       AND c.column_id = fkc.parent_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.PhieuKham')
      AND c.name = N'MaLH')
    ALTER TABLE dbo.PhieuKham ADD CONSTRAINT FK_PhieuKham_LichHen
        FOREIGN KEY (MaLH) REFERENCES dbo.LichHen(MaLH);
GO
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id
                       AND c.column_id = fkc.parent_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.LichHen')
      AND c.name = N'MaTK')
    ALTER TABLE dbo.LichHen ADD CONSTRAINT FK_LichHen_TaiKhoan
        FOREIGN KEY (MaTK) REFERENCES dbo.TaiKhoan(MaTK);
GO
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id
                       AND c.column_id = fkc.parent_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.HoaDon')
      AND c.name = N'MaTK')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_TaiKhoan
        FOREIGN KEY (MaTK) REFERENCES dbo.TaiKhoan(MaTK);
GO
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id
                       AND c.column_id = fkc.parent_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.ChiTietHoaDon')
      AND c.name = N'MaDV')
    ALTER TABLE dbo.ChiTietHoaDon ADD CONSTRAINT FK_ChiTietHoaDon_DichVu
        FOREIGN KEY (MaDV) REFERENCES dbo.DichVu(MaDV);
GO
IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_key_columns fkc
    JOIN sys.columns c ON c.object_id = fkc.parent_object_id
                       AND c.column_id = fkc.parent_column_id
    WHERE fkc.parent_object_id = OBJECT_ID(N'dbo.HoaDon')
      AND c.name = N'MaGiam')
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT FK_HoaDon_MaGiamGia
        FOREIGN KEY (MaGiam) REFERENCES dbo.MaGiamGia(MaGiam);
GO

-- Ràng buộc CHECK còn thiếu.
IF OBJECT_ID(N'CK_BenhNhan_TrangThai', N'C') IS NULL
    ALTER TABLE dbo.BenhNhan ADD CONSTRAINT CK_BenhNhan_TrangThai
        CHECK (TrangThaiHoSo IN (N'Hoạt động', N'Ngừng hoạt động'));
GO
IF OBJECT_ID(N'CK_PhongKham_SucChua', N'C') IS NULL
    ALTER TABLE dbo.PhongKham ADD CONSTRAINT CK_PhongKham_SucChua CHECK (SucChua > 0);
GO
IF OBJECT_ID(N'CK_HoaDon_TienKhachDua', N'C') IS NULL
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT CK_HoaDon_TienKhachDua
        CHECK (TienKhachDua IS NULL OR TienKhachDua >= 0);
GO
IF OBJECT_ID(N'CK_HoaDon_TienThua', N'C') IS NULL
    ALTER TABLE dbo.HoaDon ADD CONSTRAINT CK_HoaDon_TienThua
        CHECK (TienThua IS NULL OR TienThua >= 0);
GO

-- Chỉ mục còn thiếu.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_PhieuKham_NgayKham'
                                           AND object_id = OBJECT_ID(N'dbo.PhieuKham'))
    CREATE INDEX IX_PhieuKham_NgayKham ON dbo.PhieuKham(NgayKham);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_PhieuKham_MaBN'
                                           AND object_id = OBJECT_ID(N'dbo.PhieuKham'))
    CREATE INDEX IX_PhieuKham_MaBN ON dbo.PhieuKham(MaBN);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_HoaDon_NgayLap'
                                           AND object_id = OBJECT_ID(N'dbo.HoaDon'))
    CREATE INDEX IX_HoaDon_NgayLap ON dbo.HoaDon(NgayLap);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_HoaDon_MaPK_Active'
                                           AND object_id = OBJECT_ID(N'dbo.HoaDon'))
BEGIN
    BEGIN TRY
        CREATE UNIQUE INDEX UX_HoaDon_MaPK_Active ON dbo.HoaDon(MaPK)
            WHERE TrangThai <> N'Đã hủy';
    END TRY
    BEGIN CATCH
        PRINT N'Bỏ qua UX_HoaDon_MaPK_Active vì dữ liệu cũ đang có nhiều hóa đơn cùng phiếu khám.';
    END CATCH
END
GO

-- =====================================================================
-- 4. DỮ LIỆU MẪU
-- =====================================================================

-- Phòng khám
IF NOT EXISTS (SELECT 1 FROM dbo.PhongKham)
BEGIN
    INSERT INTO dbo.PhongKham (TenPhong, SucChua) VALUES
    (N'Phòng 1', 20),
    (N'Phòng 2', 20),
    (N'Phòng 3', 15);
END
GO

-- Bác sĩ
IF NOT EXISTS (SELECT 1 FROM dbo.BacSi)
BEGIN
    INSERT INTO dbo.BacSi (HoTen, ChuyenKhoa) VALUES
    (N'BS. Hoàng Minh', N'Nha khoa tổng quát'),
    (N'BS. Lan Anh',    N'Chỉnh nha'),
    (N'BS. Quốc Bảo',   N'Phục hình');
END
GO

-- Bảng giá dịch vụ
IF NOT EXISTS (SELECT 1 FROM dbo.DichVu)
BEGIN
    INSERT INTO dbo.DichVu (TenDichVu, DonGia) VALUES
    (N'Khám & tư vấn',                  200000),
    (N'Cạo vôi răng',                   400000),
    (N'Trám răng composite',            600000),
    (N'Nhổ răng',                       650000),
    (N'Tẩy trắng răng',                2200000),
    (N'Chụp X-quang răng',              150000),
    (N'Điều trị tủy',                  1500000),
    (N'Bọc răng sứ',                   3000000),
    (N'Cấy ghép implant',             15000000);
END
GO

-- Mã giảm giá mẫu (quản lý tạo sẵn, lễ tân chỉ chọn khi lập hóa đơn)
IF NOT EXISTS (SELECT 1 FROM dbo.MaGiamGia)
BEGIN
    INSERT INTO dbo.MaGiamGia (Ma, Ten, LoaiGiam, GiaTri, TrangThai) VALUES
    (N'GIAM10',  N'Khách hàng thân thiết 10%', N'Phần trăm', 10,    N'Hoạt động'),
    (N'GIAM20',  N'Ưu đãi 20%',                N'Phần trăm', 20,    N'Hoạt động'),
    (N'GIAM50K', N'Giảm 50.000 đ',             N'Số tiền',   50000, N'Hoạt động'),
    (N'TRIAN',   N'Tri ân khách cũ 100.000 đ', N'Số tiền',   100000,N'Hoạt động');
END
GO

-- Tài khoản (mật khẩu 123456 -> SHA-256)
IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan)
BEGIN
    INSERT INTO dbo.TaiKhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro) VALUES
    ('letan',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Lê Thu Hương',  N'Lễ tân'),
    ('letan2',  '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Phan Thảo Vy',  N'Lễ tân'),
    ('quantri', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Nguyễn Tấn Đệ', N'Quản trị viên');
END
GO

-- Bệnh nhân mẫu
IF NOT EXISTS (SELECT 1 FROM dbo.BenhNhan)
BEGIN
    INSERT INTO dbo.BenhNhan
        (HoTen, NgaySinh, GioiTinh, SoDienThoai, DiaChi, CCCD, Email, NgheNghiep,
         TenNguoiLienHe, SdtNguoiLienHe, DiUng, TienSuBenh, GhiChu, TrangThaiHoSo)
    VALUES
    (N'Nguyễn Văn A', '1990-03-12', N'Nam', '0901234567', N'Quận 1, TP.HCM',
     '079090001234', 'vana@example.com', N'Kỹ sư phần mềm', N'Nguyễn Thị M', '0987000111',
     NULL, NULL, NULL, N'Hoạt động'),
    (N'Trần Thị B', '1985-07-25', N'Nữ', '0912345678', N'Quận 3, TP.HCM',
     '079185004321', 'thib@example.com', N'Kế toán', N'Trần Văn K', '0987000222',
     N'Penicillin', N'Cao huyết áp', N'Hay đau răng hàm dưới', N'Hoạt động'),
    (N'Lê Văn C', '2000-01-01', N'Nam', '0923456789', N'Thủ Đức, TP.HCM',
     '079200005678', NULL, N'Sinh viên', N'Lê Thị H', '0987000333',
     NULL, NULL, NULL, N'Hoạt động'),
    (N'Phạm Thị D', '1995-09-09', N'Nữ', '0934567890', N'Quận 7, TP.HCM',
     '079195009876', 'thid@example.com', N'Giáo viên', N'Phạm Văn T', '0987000444',
     N'Latex', NULL, N'Đang niềng răng', N'Hoạt động'),
    (N'Võ Minh E', '1978-11-30', N'Nam', '0945678901', N'Bình Thạnh, TP.HCM',
     '079078001111', NULL, N'Kinh doanh', N'Võ Thị N', '0987000555',
     N'Aspirin', N'Tiểu đường type 2', N'Cần báo bác sĩ trước khi nhổ răng', N'Hoạt động'),
    (N'Đặng Thị F', '2005-05-20', N'Nữ', '0956789012', N'Gò Vấp, TP.HCM',
     '079305002222', 'thif@example.com', N'Học sinh', N'Đặng Văn P', '0987000666',
     NULL, NULL, NULL, N'Hoạt động');
END
GO

-- Phiếu khám hôm nay
IF NOT EXISTS (SELECT 1 FROM dbo.PhieuKham)
BEGIN
    DECLARE @homNay DATE = CAST(GETDATE() AS DATE);
    INSERT INTO dbo.PhieuKham (MaBN, MaPhong, NgayKham, SoThuTu, TrangThai) VALUES
    (1, 1, @homNay, 1, N'Đang khám'),
    (2, 2, @homNay, 2, N'Hoàn thành'),
    (3, 1, @homNay, 3, N'Hoàn thành'),
    (4, 3, @homNay, 4, N'Chờ khám'),
    (5, 2, @homNay, 5, N'Hoàn thành');
END
GO

-- Dịch vụ chỉ định sẵn khi tiếp nhận (cho các phiếu chưa lập hóa đơn)
IF NOT EXISTS (SELECT 1 FROM dbo.PhieuKhamDichVu)
BEGIN
    INSERT INTO dbo.PhieuKhamDichVu (MaPK, MaDV)
    SELECT pk.MaPK, dv.MaDV
    FROM dbo.PhieuKham pk
    CROSS JOIN dbo.DichVu dv
    WHERE pk.NgayKham = CAST(GETDATE() AS DATE)
      AND pk.MaBN IN (1, 2)
      AND dv.TenDichVu IN (N'Khám & tư vấn', N'Cạo vôi răng')
      AND NOT EXISTS (
          SELECT 1 FROM dbo.HoaDon hd WHERE hd.MaPK = pk.MaPK AND hd.TrangThai <> N'Đã hủy');
END
GO

-- Hồ sơ kết quả khám cho phiếu đã hoàn thành
IF NOT EXISTS (SELECT 1 FROM dbo.KetQuaKham)
BEGIN
    INSERT INTO dbo.KetQuaKham
        (MaPK, MaBS, TrieuChung, KetQuaLamSang, ChanDoan, HuongDieuTri, DonThuoc, GhiChu, NgayTaiKham)
    SELECT pk.MaPK, 1,
           N'Ê buốt răng khi uống nước lạnh',
           N'Men răng tổn thương nhẹ, không sâu tủy',
           N'Viêm tủy có hồi phục',
           N'Trám composite, hướng dẫn chăm sóc răng miệng',
           N'Paracetamol 500mg khi đau',
           N'Tái khám sau 2 tuần',
           DATEADD(DAY, 14, CAST(GETDATE() AS DATE))
    FROM dbo.PhieuKham pk
    WHERE pk.MaBN = 3 AND pk.NgayKham = CAST(GETDATE() AS DATE)
      AND NOT EXISTS (SELECT 1 FROM dbo.KetQuaKham k WHERE k.MaPK = pk.MaPK);
END
GO

-- Lịch hẹn sắp tới
IF NOT EXISTS (SELECT 1 FROM dbo.LichHen)
BEGIN
    DECLARE @homNay DATE = CAST(GETDATE() AS DATE);
    INSERT INTO dbo.LichHen (MaBN, MaBS, NgayGio, TrangThai, YeuCau, NgayGioMoi, GhiChu, MaTK) VALUES
    (4, 1, DATEADD(HOUR, 33, CAST(@homNay AS DATETIME2)), N'Đã xác nhận', NULL, NULL, N'Kiểm tra niềng răng định kỳ', 1),
    (5, 2, DATEADD(HOUR, 34, CAST(@homNay AS DATETIME2)), N'Chờ xác nhận', NULL, NULL, N'Đau răng hàm', 1),
    (6, 3, DATEADD(HOUR, 57, CAST(@homNay AS DATETIME2)), N'Đã xác nhận', N'Đổi lịch',
        DATEADD(HOUR, 81, CAST(@homNay AS DATETIME2)), N'Bận việc gia đình', 1),
    (2, 1, DATEADD(HOUR, 58, CAST(@homNay AS DATETIME2)), N'Đã xác nhận', N'Hủy lịch',
        NULL, N'Không sắp xếp được thời gian', 1);
END
GO

-- Hóa đơn mẫu
IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon)
BEGIN
    DECLARE @pk3 INT = (SELECT TOP 1 MaPK FROM dbo.PhieuKham WHERE MaBN = 3 ORDER BY MaPK);
    DECLARE @pk5 INT = (SELECT TOP 1 MaPK FROM dbo.PhieuKham WHERE MaBN = 5 ORDER BY MaPK);
    DECLARE @maGiam10 INT = (SELECT TOP 1 MaGiam FROM dbo.MaGiamGia WHERE Ma = N'GIAM10');

    INSERT INTO dbo.HoaDon
        (MaPK, MaTK, NgayLap, TongTien, GiamGia, MaGiam, PhuongThucTT, NgayThanhToan,
         TienKhachDua, TienThua, TrangThai, GhiChu)
    VALUES
    (@pk3, 1, SYSDATETIME(), 1080000, 120000, @maGiam10, N'Tiền mặt', SYSDATETIME(),
     1500000, 420000, N'Đã thanh toán', N'Khách hàng thân thiết được giảm 10%'),
    (@pk5, 1, SYSDATETIME(),  850000, 0, NULL, NULL, NULL, NULL, NULL,
     N'Chưa thanh toán', NULL);

    DECLARE @hd1 INT = (SELECT TOP 1 MaHD FROM dbo.HoaDon WHERE MaPK = @pk3 ORDER BY MaHD);
    DECLARE @hd2 INT = (SELECT TOP 1 MaHD FROM dbo.HoaDon WHERE MaPK = @pk5 ORDER BY MaHD);

    INSERT INTO dbo.ChiTietHoaDon (MaHD, MaDV, TenDichVu, SoLuong, DonGia) VALUES
    (@hd1, 1, N'Khám & tư vấn',        1,  200000),
    (@hd1, 2, N'Cạo vôi răng',         1,  400000),
    (@hd1, 3, N'Trám răng composite',  1,  600000),
    (@hd2, 1, N'Khám & tư vấn',        1,  200000),
    (@hd2, 4, N'Nhổ răng',             1,  650000);
END
GO

-- =====================================================================
-- 5. TÀI KHOẢN SQL SERVER DÙNG CHO ỨNG DỤNG
-- =====================================================================
USE master;
GO

IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'medident_app')
BEGIN
    CREATE LOGIN [medident_app]
        WITH PASSWORD = N'MediDent@123',
             CHECK_POLICY = OFF,
             CHECK_EXPIRATION = OFF,
             DEFAULT_DATABASE = [MediDentLeTan];
    PRINT N'Đã tạo login medident_app.';
END
ELSE
    PRINT N'Login medident_app đã tồn tại.';
GO

USE MediDentLeTan;
GO

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'medident_app')
BEGIN
    CREATE USER [medident_app] FOR LOGIN [medident_app];
    PRINT N'Đã tạo user medident_app trong CSDL MediDentLeTan.';
END
ELSE
    PRINT N'User medident_app đã tồn tại trong CSDL.';
GO

-- Chỉ cấp quyền đọc/ghi dữ liệu, không cấp quyền quản trị CSDL.
IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id
    JOIN sys.database_principals u ON u.principal_id = rm.member_principal_id
    WHERE r.name = N'db_datareader' AND u.name = N'medident_app')
    ALTER ROLE db_datareader ADD MEMBER [medident_app];
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.database_role_members rm
    JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id
    JOIN sys.database_principals u ON u.principal_id = rm.member_principal_id
    WHERE r.name = N'db_datawriter' AND u.name = N'medident_app')
    ALTER ROLE db_datawriter ADD MEMBER [medident_app];
GO

PRINT N'Hoàn tất: CSDL MediDentLeTan sẵn sàng. Đăng nhập ứng dụng: letan / 123456';
GO
