-- MediDent: tài khoản, phân quyền, lịch sử đăng nhập; Hỗ trợ chỉ là trang hướng dẫn.
-- Các quyền nghiệp vụ chỉ được khai báo để phân quyền; giao diện nghiệp vụ chưa triển khai.
-- SQL Server 2012+. Khong xoa du lieu/bang cu neu database da ton tai.
IF DB_ID(N'MediDent') IS NULL CREATE DATABASE MediDent;
GO
USE MediDent;
GO
IF OBJECT_ID(N'dbo.VaiTro',N'U') IS NULL
CREATE TABLE dbo.VaiTro(maVaiTro INT IDENTITY(1,1) PRIMARY KEY,
 tenVaiTro NVARCHAR(100) NOT NULL UNIQUE,moTa NVARCHAR(255) NULL,
 capBacHoTro INT NOT NULL DEFAULT 0);
GO
IF COL_LENGTH('dbo.VaiTro','capBacHoTro') IS NULL
ALTER TABLE dbo.VaiTro ADD capBacHoTro INT NOT NULL DEFAULT 0;
GO
IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
CREATE TABLE dbo.TaiKhoan(maTK INT IDENTITY(1,1) PRIMARY KEY,
 tenDangNhap VARCHAR(60) NOT NULL UNIQUE,matKhau VARCHAR(100) NOT NULL,
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Hoạt động',
 ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),ngayDangNhapCuoi DATETIME2 NULL,
 phaiDoiMatKhau BIT NOT NULL DEFAULT 0,
 maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro));
GO
-- Bổ sung trường đăng nhập nếu nâng cấp từ phiên bản trước.
IF COL_LENGTH('dbo.TaiKhoan','phaiDoiMatKhau') IS NULL
ALTER TABLE dbo.TaiKhoan ADD phaiDoiMatKhau BIT NOT NULL DEFAULT 0;
IF COL_LENGTH('dbo.TaiKhoan','ngayDangNhapCuoi') IS NULL
ALTER TABLE dbo.TaiKhoan ADD ngayDangNhapCuoi DATETIME2 NULL;
IF COL_LENGTH('dbo.TaiKhoan','ngayTao') IS NULL
ALTER TABLE dbo.TaiKhoan ADD ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME();
GO
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
CREATE TABLE dbo.NhanVien(maNV INT IDENTITY(1,1) PRIMARY KEY,
 hoTen NVARCHAR(120) NOT NULL,ngaySinh DATE NULL,gioiTinh NVARCHAR(15) NULL,
 sdt VARCHAR(20) NULL,email VARCHAR(150) NULL,diaChi NVARCHAR(255) NULL,
 chucVu NVARCHAR(100) NULL,trangThai NVARCHAR(20) NOT NULL DEFAULT N'Đang làm việc',
 maTK INT NULL REFERENCES dbo.TaiKhoan(maTK));
GO
IF COL_LENGTH('dbo.NhanVien','ngaySinh') IS NULL
ALTER TABLE dbo.NhanVien ADD ngaySinh DATE NULL;
IF COL_LENGTH('dbo.NhanVien','email') IS NULL
ALTER TABLE dbo.NhanVien ADD email VARCHAR(150) NULL;
IF COL_LENGTH('dbo.NhanVien','trangThai') IS NULL
ALTER TABLE dbo.NhanVien ADD trangThai NVARCHAR(20) NOT NULL DEFAULT N'Đang làm việc';
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_NhanVien_maTK' AND object_id=OBJECT_ID(N'dbo.NhanVien'))
CREATE UNIQUE INDEX UX_NhanVien_maTK ON dbo.NhanVien(maTK) WHERE maTK IS NOT NULL;
GO
IF OBJECT_ID(N'dbo.QuanTriVien',N'U') IS NULL
CREATE TABLE dbo.QuanTriVien(maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV),capDoQuyen NVARCHAR(80) NOT NULL DEFAULT N'Toàn quyền');
GO
IF OBJECT_ID(N'dbo.QuanLy',N'U') IS NULL
CREATE TABLE dbo.QuanLy(maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV),phongBan NVARCHAR(100) NULL);
GO
IF OBJECT_ID(N'dbo.LeTan',N'U') IS NULL
CREATE TABLE dbo.LeTan(maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV),caLamViec NVARCHAR(50) NULL);
GO
IF OBJECT_ID(N'dbo.Quyen',N'U') IS NULL
CREATE TABLE dbo.Quyen(maQuyen VARCHAR(30) PRIMARY KEY,tenQuyen NVARCHAR(100) NOT NULL);
GO
IF OBJECT_ID(N'dbo.VaiTroQuyen',N'U') IS NULL
CREATE TABLE dbo.VaiTroQuyen(maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro),
 maQuyen VARCHAR(30) NOT NULL REFERENCES dbo.Quyen(maQuyen),
 CONSTRAINT PK_VaiTroQuyen PRIMARY KEY(maVaiTro,maQuyen));
GO
IF OBJECT_ID(N'dbo.NhatKyHeThong',N'U') IS NULL
CREATE TABLE dbo.NhatKyHeThong(maNK BIGINT IDENTITY(1,1) PRIMARY KEY,
 thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),hanhDong NVARCHAR(200) NOT NULL,
 doiTuong NVARCHAR(150) NULL,moTa NVARCHAR(1000) NULL);
GO
-- Lưu lịch sử đăng nhập của cả ba vai trò, kể cả các lần đăng nhập thất bại.
IF OBJECT_ID(N'dbo.LichSuDangNhap', N'U') IS NULL
CREATE TABLE dbo.LichSuDangNhap (
    maLichSu BIGINT IDENTITY(1,1) PRIMARY KEY,
    maTK INT NULL, -- Giữ lịch sử ngay cả sau khi tài khoản bị xóa.
    tenDangNhap VARCHAR(60) NOT NULL,
    tenVaiTro NVARCHAR(100) NULL,
    thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    ketQua NVARCHAR(30) NOT NULL
);
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.LichSuDangNhap')
  AND name=N'IX_LichSuDangNhap_ThoiGian')
CREATE INDEX IX_LichSuDangNhap_ThoiGian
ON dbo.LichSuDangNhap(thoiGian DESC, maLichSu DESC);
GO
MERGE dbo.Quyen AS t USING (VALUES
 ('TAIKHOAN',N'Quản lý tài khoản'),
 ('PHANQUYEN',N'Phân quyền tài khoản'),
 ('HOTRO',N'Hỗ trợ nội bộ'),
 ('QL_NHANVIEN',N'Quản lý nhân viên'),
 ('QL_DICHVU',N'Quản lý dịch vụ nha khoa'),
 ('QL_BENHNHAN',N'Quản lý bệnh nhân'),
 ('QL_LICHHEN',N'Quản lý lịch hẹn'),
 ('QL_THANHTOAN',N'Thanh toán và hóa đơn (Quản lý)'),
 ('QL_BAOCAO',N'Báo cáo và thống kê (Quản lý)'),
 ('LT_KHAM',N'Khám và điều trị'),
 ('LT_TIEPNHAN',N'Tiếp nhận bệnh nhân'),
 ('LT_LICHHEN',N'Lịch hẹn (Lễ tân)'),
 ('LT_THANHTOAN',N'Thanh toán và hóa đơn (Lễ tân)'),
 ('LT_TRACUU',N'Tra cứu thông tin'))
 s(maQuyen,tenQuyen) ON t.maQuyen=s.maQuyen
 WHEN NOT MATCHED THEN INSERT(maQuyen,tenQuyen) VALUES(s.maQuyen,s.tenQuyen)
WHEN MATCHED THEN UPDATE SET tenQuyen=s.tenQuyen;
GO
MERGE dbo.VaiTro AS t USING (VALUES
 (N'Quản trị viên',N'Quản trị tài khoản và phân quyền',3),
 (N'Quản lý phòng khám',N'Quản lý',2),(N'Lễ tân',N'Lễ tân',1))
 s(tenVaiTro,moTa,capBacHoTro) ON t.tenVaiTro=s.tenVaiTro
 WHEN NOT MATCHED THEN INSERT(tenVaiTro,moTa,capBacHoTro)
 VALUES(s.tenVaiTro,s.moTa,s.capBacHoTro)
 WHEN MATCHED THEN UPDATE SET capBacHoTro=s.capBacHoTro;
GO
-- Chỉ gán quyền mặc định khi chưa có phân quyền nào.
-- Chạy lại script không làm mất trạng thái checkbox đã lưu.
IF NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen)
INSERT dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro, q.maQuyen
FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE (
   (v.tenVaiTro=N'Quản trị viên' AND q.maQuyen IN ('TAIKHOAN','PHANQUYEN','HOTRO'))
OR (v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN
 ('HOTRO','QL_NHANVIEN','QL_DICHVU','QL_BENHNHAN','QL_LICHHEN','QL_THANHTOAN','QL_BAOCAO'))
OR (v.tenVaiTro=N'Lễ tân' AND q.maQuyen IN
 ('HOTRO','LT_KHAM','LT_TIEPNHAN','LT_LICHHEN','LT_THANHTOAN','LT_TRACUU'))
) AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen x
 WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen);
GO
