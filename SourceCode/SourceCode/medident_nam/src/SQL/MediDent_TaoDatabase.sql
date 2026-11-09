/* ============================================================
   KHOI TAO DATABASE
   Neu database MediDent chua ton tai thi tao moi.
   Neu project dang dung ten database khac, sua MediDent o
   ca DB_ID, CREATE DATABASE va USE cho trung JDBC URL.
   ============================================================ */
IF DB_ID(N'MediDent') IS NULL
BEGIN
    CREATE DATABASE [MediDent];
END;
GO

USE [MediDent];
GO

/* ============================================================
   SQL Server - Tao 5 bang: NhanVien, BenhNhan, DichVu, HoaDon, LichHen
   Muc dich: phu hop voi cac thuoc tinh Entity Java dang su dung.

   Gia dinh theo code hien tai:
   - maNV, maBN, maDV la String trong Java -> NVARCHAR(20).
   - Ngay sinh la LocalDate -> DATE.
   - Gioi tinh/trang thai la enum -> luu ten enum bang NVARCHAR.
   - Don gia la BigDecimal -> DECIMAL(18,2).
   - Chua tao FK NhanVien.maTK -> TaiKhoan vi chua co DDL/xac nhan
     kieu du lieu va ten khoa chinh cua bang TaiKhoan.

   Luu y: chay script trong dung database cua ung dung.
   ============================================================ */

-- 1. BANG NHANVIEN
IF OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.NhanVien
    (
        maNV       NVARCHAR(20)  NOT NULL,
        hoten      NVARCHAR(150) NOT NULL,
        ngaysinh   DATE          NULL,
        gioitinh   NVARCHAR(20)  NULL,
        sdt        NVARCHAR(20)  NULL,
        email      NVARCHAR(150) NULL,
        diachi     NVARCHAR(300) NULL,
        chucvu     NVARCHAR(100) NULL,
        trangthai  NVARCHAR(50)  NULL,
        maTK       NVARCHAR(20)  NULL,

        CONSTRAINT PK_NhanVien PRIMARY KEY (maNV)
    );
END;
GO

-- 2. BANG BENHNHAN
IF OBJECT_ID(N'dbo.BenhNhan', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BenhNhan
    (
        maBN       NVARCHAR(20)   NOT NULL,
        hoten      NVARCHAR(150)  NOT NULL,
        ngaysinh   DATE           NULL,
        gioitinh   NVARCHAR(20)   NULL,
        sdt        NVARCHAR(20)   NULL,
        email      NVARCHAR(150)  NULL,
        diachi     NVARCHAR(300)  NULL,
        tiensubenh NVARCHAR(1000) NULL,
        trangthai  NVARCHAR(50)   NULL,

        CONSTRAINT PK_BenhNhan PRIMARY KEY (maBN)
    );
END;
GO

-- 3. BANG DICHVU
IF OBJECT_ID(N'dbo.DichVu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DichVu
    (
        maDV      NVARCHAR(20)   NOT NULL,
        tenDV     NVARCHAR(150)  NOT NULL,
        mota      NVARCHAR(1000) NULL,
        dongia    DECIMAL(18,2)  NOT NULL
                  CONSTRAINT DF_DichVu_dongia DEFAULT (0),
        trangthai NVARCHAR(50)   NOT NULL,

        CONSTRAINT PK_DichVu PRIMARY KEY (maDV),
        CONSTRAINT CK_DichVu_dongia CHECK (dongia >= 0),
        CONSTRAINT CK_DichVu_trangthai
            CHECK (trangthai IN (N'dangapdung', N'ngungapdung'))
    );
END;
GO

-- 4. BANG HOADON (chua co rang buoc PK/FK/CHECK)
-- maHD tu tang vi HoaDonDAO.themHoaDon() khong truyen maHD.
-- ngayLap co gia tri mac dinh vi HoaDonDAO.themHoaDon() khong truyen ngayLap.
IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.HoaDon
    (
        maHD      INT IDENTITY(1,1) NOT NULL,
        maTT      INT               NOT NULL,
        maPK      INT               NOT NULL,
        ngayLap   DATETIME2(0)      NOT NULL DEFAULT SYSDATETIME(),
        tongTien  DECIMAL(18,2)     NOT NULL,
        trangthai NVARCHAR(50)      NOT NULL
    );
END;
GO

-- 5. BANG LICHHEN (chua co rang buoc PK/FK/CHECK)
-- maBN, maNV dung NVARCHAR(20) giong BenhNhan.maBN va NhanVien.maNV.
IF OBJECT_ID(N'dbo.LichHen', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.LichHen
    (
        maLichHen     INT IDENTITY(1,1) NOT NULL,
        maBN          NVARCHAR(20)      NOT NULL,
        maNV          NVARCHAR(20)      NULL,
        ngayHen       DATE              NOT NULL,
        gioHen        TIME(0)           NOT NULL,
        thoiLuongPhut INT               NOT NULL DEFAULT 60,
        phongKham     NVARCHAR(50)      NULL,
        trangthai     NVARCHAR(50)      NOT NULL
    );
END;
GO

-- 6. INDEX ho tro tim kiem thuong dung (chi tao neu chua ton tai)
IF NOT EXISTS
(
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_NhanVien_hoten' AND object_id = OBJECT_ID(N'dbo.NhanVien')
)
    CREATE INDEX IX_NhanVien_hoten ON dbo.NhanVien(hoten);
GO

IF NOT EXISTS
(
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_NhanVien_sdt' AND object_id = OBJECT_ID(N'dbo.NhanVien')
)
    CREATE INDEX IX_NhanVien_sdt ON dbo.NhanVien(sdt);
GO

IF NOT EXISTS
(
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_BenhNhan_hoten' AND object_id = OBJECT_ID(N'dbo.BenhNhan')
)
    CREATE INDEX IX_BenhNhan_hoten ON dbo.BenhNhan(hoten);
GO

IF NOT EXISTS
(
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_BenhNhan_sdt' AND object_id = OBJECT_ID(N'dbo.BenhNhan')
)
    CREATE INDEX IX_BenhNhan_sdt ON dbo.BenhNhan(sdt);
GO

IF NOT EXISTS
(
    SELECT 1 FROM sys.indexes
    WHERE name = N'IX_DichVu_tenDV' AND object_id = OBJECT_ID(N'dbo.DichVu')
)
    CREATE INDEX IX_DichVu_tenDV ON dbo.DichVu(tenDV);
GO

/* ============================================================
   DOI CHIEU VOI JAVA
   NhanVien:
     maNV, hoten, ngaysinh, gioitinh, sdt, email,
     diachi, chucvu, trangthai, maTK

   BenhNhan:
     maBN, hoten, ngaysinh, gioitinh, sdt, email,
     diachi, tiensubenh, trangthai

   DichVu:
     maDV, tenDV, mota, dongia, trangthai

   HoaDon:
     maHD, maTT, maPK, ngayLap, tongTien, trangthai

   LichHen:
     maLichHen, maBN, maNV, ngayHen, gioHen,
     thoiLuongPhut, phongKham, trangthai

   Enum can luu dung ten hang enum (phan biet chu hoa/thuong
   khi Java goi Enum.valueOf()).
   ============================================================ */
