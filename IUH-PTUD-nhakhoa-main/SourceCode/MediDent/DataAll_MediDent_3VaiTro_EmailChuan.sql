-- Script dữ liệu tích hợp MediDent cho SQL Server 2012; lệnh DDL/DML giữ nguyên bản gốc.

IF DB_ID(N'MediDent') IS NULL CREATE DATABASE MediDent;
GO
USE MediDent;
GO

IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.TaiKhoan',N'matKhau') IS NULL
BEGIN
    THROW 50001, N'Bang TaiKhoan cu khong co cot matKhau theo ERD. Hay sao luu/migrate DB truoc.', 1;
END
GO

IF OBJECT_ID(N'dbo.VaiTro',N'U') IS NULL
CREATE TABLE dbo.VaiTro (
 maVaiTro INT IDENTITY(1,1) PRIMARY KEY,
 tenVaiTro NVARCHAR(100) NOT NULL UNIQUE,
 moTa NVARCHAR(255) NULL
);
GO
IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
CREATE TABLE dbo.TaiKhoan (
 maTK INT IDENTITY(1,1) PRIMARY KEY,
 tenDangNhap VARCHAR(60) NOT NULL UNIQUE,
 matKhau VARCHAR(100) NOT NULL,                                        
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Hoạt động'
   CHECK (trangThai IN (N'Hoạt động',N'Tạm khóa')),
 ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 ngayDangNhapCuoi DATETIME2 NULL,
 phaiDoiMatKhau BIT NOT NULL DEFAULT 0,                                           
 maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro)
);
GO
IF OBJECT_ID(N'dbo.NhanVien',N'U') IS NULL
CREATE TABLE dbo.NhanVien (
 maNV INT IDENTITY(1,1) PRIMARY KEY,
 hoTen NVARCHAR(120) NOT NULL,
 ngaySinh DATE NULL,
 gioiTinh NVARCHAR(15) NULL,
 sdt VARCHAR(20) NULL,
 email VARCHAR(150) NULL,
 diaChi NVARCHAR(255) NULL,
 chucVu NVARCHAR(100) NULL,
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Đang làm việc',
 maTK INT NULL REFERENCES dbo.TaiKhoan(maTK)
);
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_NhanVien_maTK' AND object_id=OBJECT_ID(N'dbo.NhanVien'))
CREATE UNIQUE INDEX UX_NhanVien_maTK ON dbo.NhanVien(maTK) WHERE maTK IS NOT NULL;
GO
IF OBJECT_ID(N'dbo.QuanTriVien',N'U') IS NULL
CREATE TABLE dbo.QuanTriVien (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), capDoQuyen NVARCHAR(80) NOT NULL DEFAULT N'Toàn quyền');
GO
IF OBJECT_ID(N'dbo.QuanLy',N'U') IS NULL
CREATE TABLE dbo.QuanLy (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), phongBan NVARCHAR(100) NULL);
GO
IF OBJECT_ID(N'dbo.LeTan',N'U') IS NULL
CREATE TABLE dbo.LeTan (maNV INT PRIMARY KEY REFERENCES dbo.NhanVien(maNV), caLamViec NVARCHAR(50) NULL);
GO

IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
CREATE TABLE dbo.BenhNhan (
 maBN INT IDENTITY(1,1) PRIMARY KEY,hoTen NVARCHAR(120) NOT NULL,ngaySinh DATE NULL,
 gioiTinh NVARCHAR(15) NULL,sdt VARCHAR(20) NULL,email VARCHAR(150) NULL,
 diaChi NVARCHAR(255) NULL,tienSuBenh NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL
);
GO
IF OBJECT_ID(N'dbo.NguoiGiamHo',N'U') IS NULL
CREATE TABLE dbo.NguoiGiamHo (
 maGH INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 hoTen NVARCHAR(120) NOT NULL,sdt VARCHAR(20) NULL,quanHe NVARCHAR(60) NULL
);
GO
IF OBJECT_ID(N'dbo.LichLamViec',N'U') IS NULL
CREATE TABLE dbo.LichLamViec (
 maLichLV INT IDENTITY(1,1) PRIMARY KEY,maNV INT NOT NULL REFERENCES dbo.NhanVien(maNV),
 thu NVARCHAR(15) NULL,ngayLamViec DATE NOT NULL,caLamViec NVARCHAR(50) NULL,
 gioBatDau TIME NULL,gioKetThuc TIME NULL,trangThai NVARCHAR(30) NULL
);
GO
IF OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
CREATE TABLE dbo.LichHen (
 maLichHen INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 maNV INT NULL REFERENCES dbo.NhanVien(maNV),phongKham NVARCHAR(80) NULL,
 ngayHen DATE NOT NULL,gioHen TIME NOT NULL,lyDo NVARCHAR(500) NULL,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xác nhận',ghiChu NVARCHAR(1000) NULL
);
GO
IF OBJECT_ID(N'dbo.PhieuKham',N'U') IS NULL
CREATE TABLE dbo.PhieuKham (
 maPK INT IDENTITY(1,1) PRIMARY KEY,maBN INT NOT NULL REFERENCES dbo.BenhNhan(maBN),
 maNV INT NOT NULL REFERENCES dbo.NhanVien(maNV),ngayKham DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 trieuChung NVARCHAR(MAX) NULL,chanDoan NVARCHAR(MAX) NULL,
 phuongPhapDieuTri NVARCHAR(MAX) NULL,ketQua NVARCHAR(MAX) NULL,
 ghiChu NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL
);
GO

IF OBJECT_ID(N'dbo.DichVu',N'U') IS NULL
CREATE TABLE dbo.DichVu (maDV INT IDENTITY(1,1) PRIMARY KEY,tenDV NVARCHAR(150) NOT NULL,
 moTa NVARCHAR(500) NULL,donGia DECIMAL(18,2) NOT NULL DEFAULT 0,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.BangGiaDichVu',N'U') IS NULL
CREATE TABLE dbo.BangGiaDichVu (maBG INT IDENTITY(1,1) PRIMARY KEY,
 maDV INT NOT NULL REFERENCES dbo.DichVu(maDV),ngayApDung DATE NOT NULL,
 donGia DECIMAL(18,2) NOT NULL,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.DichVuDieuTri',N'U') IS NULL
CREATE TABLE dbo.DichVuDieuTri (maDDT INT IDENTITY(1,1) PRIMARY KEY,
 tenDDT NVARCHAR(150) NOT NULL,donGia DECIMAL(18,2) NOT NULL DEFAULT 0,trangThai NVARCHAR(30) NULL);
GO
IF OBJECT_ID(N'dbo.ChiTietDieuTri',N'U') IS NULL
CREATE TABLE dbo.ChiTietDieuTri (maCT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),maDDT INT NOT NULL REFERENCES dbo.DichVuDieuTri(maDDT),
 noiDung NVARCHAR(MAX) NULL,soLuong INT NOT NULL DEFAULT 1 CHECK(soLuong>0),
 donGia DECIMAL(18,2) NOT NULL DEFAULT 0,thanhTien AS (CONVERT(DECIMAL(18,2),soLuong*donGia)) PERSISTED);
GO

IF OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
CREATE TABLE dbo.ThanhToan (maTT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),ngayThanhToan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 soTien DECIMAL(18,2) NOT NULL CHECK(soTien>=0),phuongThuc NVARCHAR(40) NULL,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',maGiaoDich VARCHAR(100) NULL);
GO
IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL
CREATE TABLE dbo.HoaDon (maHD INT IDENTITY(1,1) PRIMARY KEY,
 maTT INT NOT NULL REFERENCES dbo.ThanhToan(maTT),maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),
 ngayLap DATETIME2 NOT NULL DEFAULT SYSDATETIME(),tongTien DECIMAL(18,2) NOT NULL DEFAULT 0,
 trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ thanh toán');
GO
IF OBJECT_ID(N'dbo.YeuCauHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.YeuCauHuyPhieuThu (maYC INT IDENTITY(1,1) PRIMARY KEY,
 maTT INT NOT NULL REFERENCES dbo.ThanhToan(maTT),ngayYeuCau DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 lyDo NVARCHAR(500) NOT NULL,trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ duyệt');
GO
IF OBJECT_ID(N'dbo.PhieuDuyetHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.PhieuDuyetHuyPhieuThu (maPD INT IDENTITY(1,1) PRIMARY KEY,
 maYC INT NOT NULL REFERENCES dbo.YeuCauHuyPhieuThu(maYC),
 ngayPheDuyet DATETIME2 NULL,lyDoPheDuyet NVARCHAR(500) NULL);
GO

IF OBJECT_ID(N'dbo.BaoCaoDoanhThu',N'U') IS NULL
CREATE TABLE dbo.BaoCaoDoanhThu (maBC INT IDENTITY(1,1) PRIMARY KEY,
 maHD INT NOT NULL REFERENCES dbo.HoaDon(maHD),tuNgay DATE NOT NULL,denNgay DATE NOT NULL,
 tongDoanhThu DECIMAL(18,2) NOT NULL DEFAULT 0);
GO
IF OBJECT_ID(N'dbo.BaoCaoLuotKham',N'U') IS NULL
CREATE TABLE dbo.BaoCaoLuotKham (maBC INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),tuNgay DATE NOT NULL,denNgay DATE NOT NULL,
 soLuotKham INT NOT NULL DEFAULT 0);
GO

IF OBJECT_ID(N'dbo.NhatKyHeThong',N'U') IS NULL
CREATE TABLE dbo.NhatKyHeThong (maNK BIGINT IDENTITY(1,1) PRIMARY KEY,
 thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),hanhDong NVARCHAR(200) NOT NULL,
 doiTuong NVARCHAR(150) NULL,moTa NVARCHAR(1000) NULL);
GO

IF OBJECT_ID(N'dbo.Quyen',N'U') IS NULL
CREATE TABLE dbo.Quyen (maQuyen VARCHAR(30) PRIMARY KEY,tenQuyen NVARCHAR(100) NOT NULL);
GO
IF OBJECT_ID(N'dbo.VaiTroQuyen',N'U') IS NULL
CREATE TABLE dbo.VaiTroQuyen (maVaiTro INT NOT NULL REFERENCES dbo.VaiTro(maVaiTro),
 maQuyen VARCHAR(30) NOT NULL REFERENCES dbo.Quyen(maQuyen),
 CONSTRAINT PK_VaiTroQuyen PRIMARY KEY(maVaiTro,maQuyen));
GO
IF OBJECT_ID(N'dbo.CauHinh',N'U') IS NULL
CREATE TABLE dbo.CauHinh (Khoa VARCHAR(60) PRIMARY KEY,GiaTri NVARCHAR(500) NOT NULL);
GO

MERGE dbo.Quyen AS t USING (VALUES
 ('TAIKHOAN',N'Quản lý tài khoản và phân quyền'),('BENHNHAN',N'Quản lý bệnh nhân'),
 ('LICHHEN',N'Quản lý lịch hẹn'),('THANHTOAN',N'Quản lý thanh toán'),
 ('BAOCAO',N'Báo cáo thống kê'),('HETHONG',N'Quản trị hệ thống')) s(maQuyen,tenQuyen)
 ON t.maQuyen=s.maQuyen WHEN NOT MATCHED THEN INSERT(maQuyen,tenQuyen) VALUES(s.maQuyen,s.tenQuyen);
GO
MERGE dbo.VaiTro AS t USING (VALUES
 (N'Quản trị viên',N'Toàn quyền'),(N'Quản lý phòng khám',N'Quản lý vận hành'),
 (N'Lễ tân',N'Tiếp nhận bệnh nhân')) s(tenVaiTro,moTa)
 ON t.tenVaiTro=s.tenVaiTro WHEN NOT MATCHED THEN INSERT(tenVaiTro,moTa) VALUES(s.tenVaiTro,s.moTa);
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE v.tenVaiTro=N'Quản trị viên' AND NOT EXISTS
 (SELECT 1 FROM dbo.VaiTroQuyen z WHERE z.maVaiTro=v.maVaiTro AND z.maQuyen=q.maQuyen);
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v JOIN dbo.Quyen q ON
(v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN('BENHNHAN','LICHHEN','THANHTOAN','BAOCAO')) OR
(v.tenVaiTro=N'Lễ tân' AND q.maQuyen IN('BENHNHAN','LICHHEN','THANHTOAN'))
WHERE NOT EXISTS(SELECT 1 FROM dbo.VaiTroQuyen z WHERE z.maVaiTro=v.maVaiTro AND z.maQuyen=q.maQuyen);
GO
MERGE dbo.CauHinh AS t USING (VALUES
 ('TenHeThong',N'Hệ thống Quản lý Nha khoa MediDent'),('MaDonVi',N'MEDIDENT-HCM-01'),
 ('EmailQuanTri',N'quantri@medident.vn'),('SoDienThoai',N''),('DiaChi',N''),
 ('Bat2FA',N'false'),('KhoaPhien',N'true'),('ThongBaoQuyen',N'false'),('SaoLuuTuDong',N'false')) s(Khoa,GiaTri)
 ON t.Khoa=s.Khoa WHEN NOT MATCHED THEN INSERT(Khoa,GiaTri) VALUES(s.Khoa,s.GiaTri);
GO

USE MediDent;
GO

SELECT t.name AS TenBang FROM sys.tables t ORDER BY t.name;
GO

SELECT 'TaiKhoan' AS Bang,'matKhau' AS Cot, COL_LENGTH('dbo.TaiKhoan','matKhau') AS DoDai
UNION ALL SELECT 'TaiKhoan','ngayDangNhapCuoi',COL_LENGTH('dbo.TaiKhoan','ngayDangNhapCuoi')
UNION ALL SELECT 'NhanVien','maTK',COL_LENGTH('dbo.NhanVien','maTK')
UNION ALL SELECT 'NhanVien','hoTen',COL_LENGTH('dbo.NhanVien','hoTen')
UNION ALL SELECT 'NhanVien','email',COL_LENGTH('dbo.NhanVien','email')
UNION ALL SELECT 'NhatKyHeThong','maNK',COL_LENGTH('dbo.NhatKyHeThong','maNK')
UNION ALL SELECT 'ThanhToan','soTien',COL_LENGTH('dbo.ThanhToan','soTien')
UNION ALL SELECT 'PhieuKham','ngayKham',COL_LENGTH('dbo.PhieuKham','ngayKham');
GO

SELECT fk.name AS TenFK, OBJECT_NAME(fk.parent_object_id) AS BangCon,
       OBJECT_NAME(fk.referenced_object_id) AS BangCha
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) IN
  ('TaiKhoan','NhanVien','QuanTriVien','QuanLy','LeTan','LichHen','LichLamViec',
  'PhieuKham','HoaDon','ThanhToan','ChiTietDieuTri','NguoiGiamHo',
  'BangGiaDichVu','PhieuDuyetHuyPhieuThu','YeuCauHuyPhieuThu')
ORDER BY BangCon;
GO

SELECT t.maTK,t.tenDangNhap,n.maNV,n.hoTen,n.email,
       v.tenVaiTro,t.trangThai,t.ngayDangNhapCuoi
FROM dbo.TaiKhoan t
LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK
JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
ORDER BY t.maTK DESC;
GO

SELECT (SELECT COUNT(*) FROM dbo.TaiKhoan) AS TongTaiKhoan,
       (SELECT COUNT(*) FROM dbo.VaiTro) AS TongVaiTro,
       (SELECT COUNT(*) FROM dbo.TaiKhoan WHERE trangThai=N'Hoạt động') AS DangHoatDong;
GO

SELECT ISNULL(SUM(soTien),0) AS TongDoanhThu
FROM dbo.ThanhToan
WHERE trangThai IN(N'Thành công',N'Đã thanh toán');
GO

USE MediDent;
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO
BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @MatKhauHash VARCHAR(100) = '$2a$10$z9XT7sr/xPRfQfa8HdmQBuPw1QSlfUalVsXYNtfXB7FRVxxy5bPHW';
    DECLARE @TaiKhoanMau TABLE (
        tenDangNhap VARCHAR(60), hoTen NVARCHAR(120), email VARCHAR(150),
        vaiTro NVARCHAR(100), trangThai NVARCHAR(20), thangTao INT
    );
    INSERT INTO @TaiKhoanMau VALUES
      ('demo_admin', N'Nguyễn Tấn Dự', 'admin@gmail.com', N'Quản trị viên', N'Hoạt động', 1),
      ('demo_ql1', N'Trần Minh Trang', 'trang@gmail.com', N'Quản lý phòng khám', N'Hoạt động', 2),
      ('demo_ql2', N'Phạm Hoàng Khang', 'khang@gmail.com', N'Quản lý phòng khám', N'Hoạt động', 3),
      ('demo_lt1', N'Lê Thu Hương', 'huong@gmail.com', N'Lễ tân', N'Hoạt động', 7),
      ('demo_lt2', N'Phan Thảo Vy', 'thaovy@gmail.com', N'Lễ tân', N'Hoạt động', 8);

    INSERT INTO dbo.TaiKhoan(tenDangNhap, matKhau, trangThai, ngayTao, maVaiTro)
    SELECT m.tenDangNhap, @MatKhauHash, m.trangThai,
           DATEADD(DAY, 5, DATEADD(MONTH, m.thangTao - 1,
                    DATEFROMPARTS(YEAR(GETDATE()), 1, 1))), v.maVaiTro
    FROM @TaiKhoanMau m
    JOIN dbo.VaiTro v ON v.tenVaiTro = m.vaiTro
    WHERE NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan t WHERE t.tenDangNhap = m.tenDangNhap);

    INSERT INTO dbo.NhanVien(hoTen, email, chucVu, trangThai, maTK)
    SELECT m.hoTen, m.email, m.vaiTro,
           CASE WHEN m.trangThai = N'Hoạt động' THEN N'Đang làm việc' ELSE N'Nghỉ việc' END,
           t.maTK
    FROM @TaiKhoanMau m
    JOIN dbo.TaiKhoan t ON t.tenDangNhap = m.tenDangNhap
    WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien n WHERE n.maTK = t.maTK);

    INSERT INTO dbo.QuanTriVien(maNV, capDoQuyen)
    SELECT n.maNV, N'Toàn quyền' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap='demo_admin'
      AND NOT EXISTS(SELECT 1 FROM dbo.QuanTriVien q WHERE q.maNV=n.maNV);
    INSERT INTO dbo.QuanLy(maNV, phongBan)
    SELECT n.maNV, N'Vận hành' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap LIKE 'demo_ql%'
      AND NOT EXISTS(SELECT 1 FROM dbo.QuanLy q WHERE q.maNV=n.maNV);
    INSERT INTO dbo.LeTan(maNV, caLamViec)
    SELECT n.maNV, N'Ca sáng' FROM dbo.NhanVien n
    JOIN dbo.TaiKhoan t ON t.maTK=n.maTK WHERE t.tenDangNhap LIKE 'demo_lt%'
      AND NOT EXISTS(SELECT 1 FROM dbo.LeTan q WHERE q.maNV=n.maNV);

    DECLARE @BenhNhanMau TABLE (hoTen NVARCHAR(120), ngaySinh DATE,
                                gioiTinh NVARCHAR(15), sdt VARCHAR(20), tienSu NVARCHAR(200));
    INSERT INTO @BenhNhanMau VALUES
      (N'Nguyễn Minh Khoa','2001-03-12',N'Nam','0001000001',N'Không ghi nhận'),
      (N'Trần Ngọc Hân','1999-11-23',N'Nữ','0001000002',N'Dị ứng thuốc tê (giả lập)'),
      (N'Lê Hoàng Nam','1996-07-14',N'Nam','0001000003',N'Không ghi nhận'),
      (N'Phạm Thu Hà','1988-09-05',N'Nữ','0001000004',N'Viêm lợi'),
      (N'Võ Thanh Long','2004-02-20',N'Nam','0001000005',N'Không ghi nhận'),
      (N'Đặng Bảo Anh','2012-08-06',N'Nữ','0001000006',N'Không ghi nhận'),
      (N'Bùi Gia Huy','2014-12-11',N'Nam','0001000007',N'Không ghi nhận'),
      (N'Ngô Minh Châu','1994-06-16',N'Nữ','0001000008',N'Ê buốt răng'),
      (N'Đỗ Thuỳ Dương','2000-10-09',N'Nữ','0001000009',N'Không ghi nhận'),
      (N'Hoàng Quốc Duy','1985-01-17',N'Nam','0001000010',N'Cao huyết áp (giả lập)'),
      (N'Phan Nhật Minh','2003-04-27',N'Nam','0001000011',N'Không ghi nhận'),
      (N'Mai Ngọc Linh','1992-05-02',N'Nữ','0001000012',N'Không ghi nhận');

    INSERT INTO dbo.BenhNhan(hoTen, ngaySinh, gioiTinh, sdt, email, diaChi, tienSuBenh, trangThai)
    SELECT m.hoTen, m.ngaySinh, m.gioiTinh, m.sdt,
           'benhnhan'+m.sdt+'@gmail.com', N'TP. Hồ Chí Minh', m.tienSu, N'Đang điều trị'
    FROM @BenhNhanMau m
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BenhNhan b WHERE b.sdt=m.sdt);

    INSERT INTO dbo.NguoiGiamHo(maBN, hoTen, sdt, quanHe)
    SELECT b.maBN, N'Người giám hộ (mẫu)', '0002000001', N'Cha/mẹ'
    FROM dbo.BenhNhan b WHERE b.sdt IN('0001000006','0001000007')
    AND NOT EXISTS(SELECT 1 FROM dbo.NguoiGiamHo g WHERE g.maBN=b.maBN);

    DECLARE @DichVuMau TABLE(ten NVARCHAR(150), moTa NVARCHAR(200), gia DECIMAL(18,2));
    INSERT INTO @DichVuMau VALUES
      (N'Khám tổng quát', N'Khám và tư vấn', 150000),
      (N'Cạo vôi răng', N'Làm sạch cao răng', 300000),
      (N'Trám răng', N'Trám răng sâu', 450000),
      (N'Nhổ răng', N'Nhổ răng thông thường', 600000),
      (N'Tẩy trắng răng', N'Tẩy trắng tại phòng khám', 1500000),
      (N'Điều trị tủy', N'Điều trị nội nha', 1200000),
      (N'Chụp X-quang', N'Chụp phim răng', 200000),
      (N'Tư vấn chỉnh nha', N'Tư vấn niềng răng', 250000);
    INSERT INTO dbo.DichVu(tenDV, moTa, donGia, trangThai)
    SELECT d.ten, d.moTa, d.gia, N'Đang áp dụng' FROM @DichVuMau d
    WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVu t WHERE t.tenDV=d.ten);
    INSERT INTO dbo.BangGiaDichVu(maDV,ngayApDung,donGia,trangThai)
    SELECT d.maDV, DATEFROMPARTS(YEAR(GETDATE()),1,1), d.donGia, N'Đang áp dụng'
    FROM dbo.DichVu d JOIN @DichVuMau m ON m.ten=d.tenDV
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BangGiaDichVu b WHERE b.maDV=d.maDV
                    AND b.ngayApDung=DATEFROMPARTS(YEAR(GETDATE()),1,1));

    INSERT INTO dbo.DichVuDieuTri(tenDDT,donGia,trangThai)
    SELECT d.ten,d.gia,N'Đang áp dụng' FROM @DichVuMau d
    WHERE d.ten IN(N'Cạo vôi răng',N'Trám răng',N'Nhổ răng',N'Điều trị tủy')
      AND NOT EXISTS(SELECT 1 FROM dbo.DichVuDieuTri t WHERE t.tenDDT=d.ten);

    DECLARE @NhanSuKham TABLE(hoTen NVARCHAR(120), email VARCHAR(150));
    INSERT INTO @NhanSuKham VALUES
        (N'Nguyễn Hoàng Việt','nvytviet@gmail.com'),
        (N'Đặng Minh Anh','nvytminhanh@gmail.com'),
        (N'Lê Quốc Bảo','nvytbao@gmail.com');
    INSERT INTO dbo.NhanVien(hoTen,email,chucVu,trangThai,maTK)
    SELECT a.hoTen,a.email,N'Bác sĩ',N'Đang làm việc',NULL
    FROM @NhanSuKham a
    WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien n
                      WHERE n.hoTen=a.hoTen AND n.chucVu=N'Bác sĩ');

    INSERT INTO dbo.LichLamViec(maNV,thu,ngayLamViec,caLamViec,gioBatDau,gioKetThuc,trangThai)
    SELECT n.maNV, N'Theo lịch', DATEADD(DAY,x.ngay,CAST(GETDATE() AS DATE)),
           CASE WHEN x.ngay%2=0 THEN N'Ca sáng' ELSE N'Ca chiều' END,
           CASE WHEN x.ngay%2=0 THEN CAST('08:00' AS TIME) ELSE CAST('13:30' AS TIME) END,
           CASE WHEN x.ngay%2=0 THEN CAST('12:00' AS TIME) ELSE CAST('17:00' AS TIME) END,
           N'Đã xếp lịch'
    FROM dbo.NhanVien n JOIN @NhanSuKham a ON a.hoTen=n.hoTen
    CROSS JOIN (VALUES(0),(1),(2),(3)) x(ngay)
    WHERE n.chucVu=N'Bác sĩ'
      AND NOT EXISTS(SELECT 1 FROM dbo.LichLamViec l WHERE l.maNV=n.maNV
         AND l.ngayLamViec=DATEADD(DAY,x.ngay,CAST(GETDATE() AS DATE)));

    DECLARE @DSBN TABLE(stt INT IDENTITY(1,1), maBN INT);
    DECLARE @DSBS TABLE(stt INT IDENTITY(1,1), maNV INT);
    INSERT INTO @DSBN(maBN) SELECT b.maBN FROM dbo.BenhNhan b JOIN @BenhNhanMau m ON m.sdt=b.sdt ORDER BY b.sdt;
    INSERT INTO @DSBS(maNV) SELECT n.maNV FROM dbo.NhanVien n
      JOIN @NhanSuKham a ON a.hoTen=n.hoTen
      WHERE n.chucVu=N'Bác sĩ' ORDER BY n.maNV;
    DECLARE @SoBN INT=(SELECT COUNT(*) FROM @DSBN), @SoBS INT=(SELECT COUNT(*) FROM @DSBS);
    IF @SoBN < 12 OR @SoBS < 3 RAISERROR(N'Thieu benh nhan hoac nhan su kham mau.',16,1);

    DECLARE @i INT=1, @bn INT, @bs INT, @hen DATE, @gio TIME, @ngayKham DATETIME2;
    WHILE @i<=24
    BEGIN
        SELECT @bn=maBN FROM @DSBN WHERE stt=((@i-1)%@SoBN)+1;
        SELECT @bs=maNV FROM @DSBS WHERE stt=((@i-1)%@SoBS)+1;
        SET @hen=DATEADD(DAY,(@i*2)%23,DATEFROMPARTS(YEAR(GETDATE()),((@i-1)%10)+1,1));
        SET @gio=CASE WHEN @i%2=0 THEN CAST('09:00' AS TIME) ELSE CAST('14:30' AS TIME) END;
        IF NOT EXISTS(SELECT 1 FROM dbo.LichHen WHERE maBN=@bn AND maNV=@bs
               AND ngayHen=@hen AND gioHen=@gio)
            INSERT INTO dbo.LichHen(maBN,maNV,phongKham,ngayHen,gioHen,lyDo,trangThai,ghiChu)
            VALUES(@bn,@bs,N'Phòng 0'+CAST((@i%3)+1 AS NVARCHAR(2)),@hen,@gio,
                   N'Khám nha khoa định kỳ (dữ liệu mẫu)',
                   CASE WHEN @i%5=0 THEN N'Chờ xác nhận' ELSE N'Đã khám' END,
                   N'MEDIDENT DEMO - lịch hẹn');
        SET @i=@i+1;
    END;

    SET @i=1;
    WHILE @i<=30
    BEGIN
        SELECT @bn=maBN FROM @DSBN WHERE stt=((@i-1)%@SoBN)+1;
        SELECT @bs=maNV FROM @DSBS WHERE stt=((@i-1)%@SoBS)+1;
        SET @ngayKham=DATEADD(HOUR,8+(@i%8),CAST(DATEADD(DAY,(@i*3)%24,
            DATEFROMPARTS(YEAR(GETDATE()),((@i-1)%10)+1,1)) AS DATETIME2));
        IF NOT EXISTS(SELECT 1 FROM dbo.PhieuKham
                      WHERE ghiChu=N'MEDIDENT-DEMO-PK-'+CAST(@i AS NVARCHAR(10)))
            INSERT INTO dbo.PhieuKham(maBN,maNV,ngayKham,trieuChung,chanDoan,
                                      phuongPhapDieuTri,ketQua,ghiChu,trangThai)
            VALUES(@bn,@bs,@ngayKham,N'Đau nhức hoặc ê buốt răng',
                   CASE WHEN @i%3=0 THEN N'Sâu răng' ELSE N'Viêm lợi nhẹ' END,
                   N'Khám và điều trị theo chỉ định',N'Tình trạng ổn định',
                   N'MEDIDENT-DEMO-PK-'+CAST(@i AS NVARCHAR(10)),N'Hoàn tất');
        SET @i=@i+1;
    END;

    INSERT INTO dbo.ChiTietDieuTri(maPK,maDDT,noiDung,soLuong,donGia)
    SELECT p.maPK,d.maDDT,N'Điều trị theo phiếu khám mẫu',1,d.donGia
    FROM dbo.PhieuKham p
    JOIN dbo.DichVuDieuTri d ON d.tenDDT =
       CASE WHEN p.maPK%4=0 THEN N'Điều trị tủy'
            WHEN p.maPK%4=1 THEN N'Cạo vôi răng'
            WHEN p.maPK%4=2 THEN N'Trám răng' ELSE N'Nhổ răng' END
    WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietDieuTri c
                     WHERE c.maPK=p.maPK AND c.maDDT=d.maDDT);

    INSERT INTO dbo.ThanhToan(maPK,ngayThanhToan,soTien,phuongThuc,trangThai,maGiaoDich)
    SELECT p.maPK,DATEADD(HOUR,1,p.ngayKham),
           (SELECT SUM(c.thanhTien) FROM dbo.ChiTietDieuTri c WHERE c.maPK=p.maPK),
           CASE WHEN p.maPK%2=0 THEN N'Tiền mặt' ELSE N'Chuyển khoản' END,
           CASE WHEN p.maPK%5=0 THEN N'Chờ xử lý' ELSE N'Thành công' END,
           'MEDIDENT-DEMO-TT-'+CAST(p.maPK AS VARCHAR(15))
    FROM dbo.PhieuKham p
    WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.ThanhToan t WHERE t.maGiaoDich=
                     'MEDIDENT-DEMO-TT-'+CAST(p.maPK AS VARCHAR(15)));

    INSERT INTO dbo.HoaDon(maTT,maPK,ngayLap,tongTien,trangThai)
    SELECT t.maTT,t.maPK,t.ngayThanhToan,t.soTien,
           CASE WHEN t.trangThai=N'Thành công' THEN N'Đã thanh toán' ELSE N'Chờ thanh toán' END
    FROM dbo.ThanhToan t WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maTT=t.maTT);

    INSERT INTO dbo.YeuCauHuyPhieuThu(maTT,ngayYeuCau,lyDo,trangThai)
    SELECT TOP 1 t.maTT,GETDATE(),N'Thanh toán nhập sai (mẫu)',N'Đã từ chối'
    FROM dbo.ThanhToan t WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%'
      AND t.trangThai=N'Chờ xử lý'
      AND NOT EXISTS(SELECT 1 FROM dbo.YeuCauHuyPhieuThu y WHERE y.maTT=t.maTT)
    ORDER BY t.maTT;
    INSERT INTO dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet)
    SELECT y.maYC,GETDATE(),N'Chưa đủ cơ sở hủy (mẫu)'
    FROM dbo.YeuCauHuyPhieuThu y
    WHERE y.lyDo=N'Thanh toán nhập sai (mẫu)'
      AND NOT EXISTS(SELECT 1 FROM dbo.PhieuDuyetHuyPhieuThu p WHERE p.maYC=y.maYC);

    ;WITH DoanhThuThang AS (
        SELECT MIN(h.maHD) AS maHD,
               DATEFROMPARTS(YEAR(t.ngayThanhToan),MONTH(t.ngayThanhToan),1) AS tuNgay,
               EOMONTH(t.ngayThanhToan) AS denNgay,SUM(t.soTien) AS tongDoanhThu
        FROM dbo.ThanhToan t JOIN dbo.HoaDon h ON h.maTT=t.maTT
        WHERE t.maGiaoDich LIKE 'MEDIDENT-DEMO-TT-%' AND t.trangThai=N'Thành công'
        GROUP BY YEAR(t.ngayThanhToan),MONTH(t.ngayThanhToan),EOMONTH(t.ngayThanhToan)
    )
    INSERT INTO dbo.BaoCaoDoanhThu(maHD,tuNgay,denNgay,tongDoanhThu)
    SELECT a.maHD,a.tuNgay,a.denNgay,a.tongDoanhThu
    FROM DoanhThuThang a
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BaoCaoDoanhThu b WHERE b.tuNgay=a.tuNgay);

    ;WITH LuotKhamThang AS (
        SELECT MIN(p.maPK) AS maPK,
               DATEFROMPARTS(YEAR(p.ngayKham),MONTH(p.ngayKham),1) AS tuNgay,
               EOMONTH(p.ngayKham) AS denNgay,COUNT(*) AS soLuotKham
        FROM dbo.PhieuKham p WHERE p.ghiChu LIKE N'MEDIDENT-DEMO-PK-%'
        GROUP BY YEAR(p.ngayKham),MONTH(p.ngayKham),EOMONTH(p.ngayKham)
    )
    INSERT INTO dbo.BaoCaoLuotKham(maPK,tuNgay,denNgay,soLuotKham)
    SELECT a.maPK,a.tuNgay,a.denNgay,a.soLuotKham
    FROM LuotKhamThang a
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BaoCaoLuotKham b WHERE b.tuNgay=a.tuNgay);

    DECLARE @NhatKyMau TABLE(hanhDong NVARCHAR(200),doiTuong NVARCHAR(150),moTa NVARCHAR(1000));
    INSERT INTO @NhatKyMau VALUES
      (N'Cập nhật quyền truy cập', N'VaiTro', N'[DEMO] Phân quyền Quản lý phòng khám'),
      (N'Sao lưu dữ liệu', N'CauHinh', N'[DEMO] Sao lưu hoàn tất'),
      (N'Thêm tài khoản', N'TaiKhoan', N'[DEMO] Tạo nhân viên mẫu'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #1'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #2'),
      (N'Đăng nhập thất bại', N'TaiKhoan', N'[DEMO] Cảnh báo đăng nhập #3');
    INSERT INTO dbo.NhatKyHeThong(thoiGian,hanhDong,doiTuong,moTa)
    SELECT DATEADD(MINUTE, -ROW_NUMBER() OVER (ORDER BY m.moTa)*15, SYSDATETIME()),
           m.hanhDong,m.doiTuong,m.moTa
    FROM @NhatKyMau m WHERE NOT EXISTS
      (SELECT 1 FROM dbo.NhatKyHeThong n WHERE n.moTa=m.moTa);

    COMMIT TRANSACTION;
    PRINT N'HOAN TAT: Da them du lieu mau MediDent (chi cac ban ghi chua ton tai).';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    DECLARE @Loi NVARCHAR(4000)=ERROR_MESSAGE();
    RAISERROR(N'Loi nap du lieu mau: %s',16,1,@Loi);
END CATCH;
GO

SELECT 'VaiTro' AS TenBang, COUNT(*) AS SoDong FROM dbo.VaiTro
UNION ALL SELECT 'TaiKhoan',COUNT(*) FROM dbo.TaiKhoan
UNION ALL SELECT 'NhanVien',COUNT(*) FROM dbo.NhanVien
UNION ALL SELECT 'BenhNhan',COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT 'LichHen',COUNT(*) FROM dbo.LichHen
UNION ALL SELECT 'PhieuKham',COUNT(*) FROM dbo.PhieuKham
UNION ALL SELECT 'ThanhToan',COUNT(*) FROM dbo.ThanhToan
UNION ALL SELECT 'HoaDon',COUNT(*) FROM dbo.HoaDon
UNION ALL SELECT 'NhatKyHeThong',COUNT(*) FROM dbo.NhatKyHeThong;
GO

USE MediDent;
GO

MERGE dbo.Quyen AS t USING (VALUES
    ('NHANVIEN', N'Quản lý nhân viên'),
    ('DICHVU', N'Quản lý dịch vụ'),
    ('HOTRO', N'Xử lý yêu cầu hỗ trợ')
) s(maQuyen, tenQuyen)
ON t.maQuyen = s.maQuyen
WHEN NOT MATCHED THEN INSERT (maQuyen, tenQuyen) VALUES(s.maQuyen, s.tenQuyen);
GO

IF OBJECT_ID('dbo.YeuCauHoTro', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.YeuCauHoTro (
        maYC INT IDENTITY(1,1) PRIMARY KEY,
        maTK INT NOT NULL REFERENCES dbo.TaiKhoan(maTK),
        tieuDe NVARCHAR(160) NOT NULL,
        noiDung NVARCHAR(1500) NOT NULL,
        ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',
        phanHoi NVARCHAR(1500) NULL,
        ngayXuLy DATETIME2 NULL
    );
END;
GO

INSERT INTO dbo.VaiTroQuyen(maVaiTro, maQuyen)
SELECT v.maVaiTro, q.maQuyen
FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE ((v.tenVaiTro=N'Quản trị viên' AND q.maQuyen IN ('NHANVIEN','DICHVU','HOTRO'))
 OR (v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN ('NHANVIEN','DICHVU')))
 AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen x WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen);
GO

SELECT v.tenVaiTro,q.maQuyen FROM dbo.VaiTroQuyen p
JOIN dbo.VaiTro v ON v.maVaiTro=p.maVaiTro
JOIN dbo.Quyen q ON q.maQuyen=p.maQuyen
ORDER BY v.tenVaiTro, q.maQuyen;
GO

USE MediDent;
GO
IF COL_LENGTH(N'dbo.TaiKhoan', N'phaiDoiMatKhau') IS NULL
BEGIN
    ALTER TABLE dbo.TaiKhoan
    ADD phaiDoiMatKhau BIT NOT NULL
        CONSTRAINT DF_TaiKhoan_PhaiDoiMatKhau DEFAULT (0);
END;
GO

DELETE vq
FROM dbo.VaiTroQuyen vq
JOIN dbo.VaiTro vt ON vt.maVaiTro=vq.maVaiTro
WHERE vt.tenVaiTro=N'Quản trị viên'
  AND vq.maQuyen IN ('BENHNHAN','LICHHEN','THANHTOAN','NHANVIEN','DICHVU');
GO

INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen
FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE v.tenVaiTro=N'Quản trị viên'
  AND q.maQuyen IN ('TAIKHOAN','HETHONG','BAOCAO','HOTRO')
  AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen x
                  WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen);
GO
SELECT v.tenVaiTro, q.maQuyen FROM dbo.VaiTroQuyen vq
JOIN dbo.VaiTro v ON v.maVaiTro=vq.maVaiTro
JOIN dbo.Quyen q ON q.maQuyen=vq.maQuyen
WHERE v.tenVaiTro=N'Quản trị viên';
GO

USE MediDent;
GO

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

USE MediDent;
GO

IF OBJECT_ID(N'dbo.ChiPhiHoatDong', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiPhiHoatDong (
        maCP INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        ngayChi DATE NOT NULL CONSTRAINT DF_ChiPhi_NgayChi DEFAULT (CONVERT(date, GETDATE())),
        loaiChi NVARCHAR(80) NOT NULL,
        moTa NVARCHAR(500) NOT NULL,
        soTien DECIMAL(18,2) NOT NULL,
        maTKNguoiNhap INT NOT NULL,
        ngayTao DATETIME2 NOT NULL CONSTRAINT DF_ChiPhi_NgayTao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_ChiPhi_TaiKhoan FOREIGN KEY(maTKNguoiNhap)
            REFERENCES dbo.TaiKhoan(maTK),
        CONSTRAINT CK_ChiPhi_SoTien CHECK(soTien > 0),
        CONSTRAINT CK_ChiPhi_LoaiChi CHECK(loaiChi IN
            (N'Vật tư', N'Nhân sự', N'Vận hành', N'Thuê mặt bằng', N'Khác'))
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.ChiPhiHoatDong')
      AND name = N'IX_ChiPhi_NgayChi'
)
BEGIN
    CREATE INDEX IX_ChiPhi_NgayChi
        ON dbo.ChiPhiHoatDong(ngayChi, loaiChi);
END;
GO

SELECT COUNT(*) AS SoKhoanChiPhi,
       COALESCE(SUM(soTien), 0) AS TongChiPhiDaNhap
FROM dbo.ChiPhiHoatDong;
GO

USE MediDent;
GO

SELECT h.maHD AS MaHoaDon,
       h.maTT AS MaThanhToan,
       h.trangThai AS TrangThaiHoaDon,
       t.trangThai AS TrangThaiThanhToan,
       b.maBN AS MaBenhNhan,
       b.hoTen AS TenBenhNhan,
       h.tongTien AS SoTien
FROM dbo.HoaDon h
JOIN dbo.ThanhToan t ON t.maTT = h.maTT
JOIN dbo.PhieuKham pk ON pk.maPK = h.maPK
JOIN dbo.BenhNhan b ON b.maBN = pk.maBN
WHERE h.maHD IN (1, 30)
ORDER BY h.maHD;
GO

SELECT TOP 50 y.maYC AS MaYeuCau,
       h.maHD AS MaHoaDon,
       y.maTT AS MaThanhToan,
       y.ngayYeuCau AS NgayGui,
       y.lyDo AS LyDo,
       y.trangThai AS TrangThaiYeuCau,
       h.trangThai AS TrangThaiHoaDon
FROM dbo.YeuCauHuyPhieuThu y
JOIN dbo.HoaDon h ON h.maTT=y.maTT
WHERE h.maHD IN (1, 30)
ORDER BY y.maYC DESC;
GO

SELECT maTT AS MaThanhToan, COUNT(*) AS SoHoaDon
FROM dbo.HoaDon
GROUP BY maTT
HAVING COUNT(*) > 1;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints
               WHERE name=N'CK_NhanVien_SDT' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
    ALTER TABLE dbo.NhanVien WITH NOCHECK
    ADD CONSTRAINT CK_NhanVien_SDT CHECK (
        sdt IS NULL OR (LEN(sdt)=10 AND sdt NOT LIKE '%[^0-9]%'
          AND LEFT(sdt,2) IN ('03','05','07','08','09'))
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_NhanVien_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN

    IF EXISTS(SELECT 1 FROM dbo.NhanVien WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')
    ))
    BEGIN
        PRINT N'Canh bao: dbo.NhanVien co email cu sai ten mien. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.NhanVien WITH NOCHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_BenhNhan_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN

    IF EXISTS(SELECT 1 FROM dbo.BenhNhan WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')
    ))
    BEGIN
        PRINT N'Canh bao: dbo.BenhNhan co email cu sai ten mien. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.BenhNhan WITH NOCHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND LOWER(SUBSTRING(email,CHARINDEX('@',email)+1,150)) IN (N'gmail.com',N'yahoo.com',N'yahoo.com.vn',N'outlook.com',N'hotmail.com',N'live.com',N'icloud.com',N'proton.me',N'protonmail.com',N'medident.vn')));
    END
END;
GO
