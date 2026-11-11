-- =========== 01_MediDent.sql ===========
-- MediDent: tài khoản, phân quyền, lịch sử đăng nhập; Hỗ trợ chỉ là trang hướng dẫn.
-- Bao gồm phân quyền, lịch hẹn, khám, dịch vụ, hóa đơn, khuyến mãi.
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

-- ========== NGHIEP VU NHA KHOA (SQL Server 2012) ==========
-- Khoa chinh maSo su dung IDENTITY int; UI co the dinh dang BN0001 / PK0001.
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
CREATE TABLE dbo.BenhNhan (
 MaBN INT IDENTITY(1,1) PRIMARY KEY, HoTen NVARCHAR(120) NOT NULL,
 NgaySinh DATE NULL,GioiTinh NVARCHAR(15) NULL,Sdt VARCHAR(20) NULL,
 Email VARCHAR(150) NULL,DiaChi NVARCHAR(255) NULL,TienSuBenh NVARCHAR(1000) NULL,
 TrangThai NVARCHAR(30) NOT NULL DEFAULT 'MOI',
 TrangThaiHoSo NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
 Cccd VARCHAR(20) NULL,NgheNghiep NVARCHAR(100) NULL,
 TenNguoiLienHe NVARCHAR(120) NULL,SdtNguoiLienHe VARCHAR(20) NULL,
 DiUng NVARCHAR(1000) NULL,GhiChu NVARCHAR(1000) NULL,
 NgayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 CONSTRAINT CK_BenhNhan_HoSo CHECK(TrangThaiHoSo IN(N'Hoạt động',N'Ngừng hoạt động'))
);
GO
IF OBJECT_ID(N'dbo.BacSi',N'U') IS NULL
CREATE TABLE dbo.BacSi (
 MaBS INT IDENTITY(1,1) PRIMARY KEY, HoTen NVARCHAR(120) NOT NULL,
 ChuyenKhoa NVARCHAR(100) NULL,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động'
);
GO
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
CREATE TABLE dbo.PhongKham (
 MaPhong INT IDENTITY(1,1) PRIMARY KEY,TenPhong NVARCHAR(100) NOT NULL UNIQUE,
 SucChua INT NOT NULL DEFAULT 2,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
 CONSTRAINT CK_PhongKham_SucChua CHECK(SucChua BETWEEN 1 AND 20)
);
GO
IF OBJECT_ID(N'dbo.DichVu',N'U') IS NULL
CREATE TABLE dbo.DichVu (
 MaDV INT IDENTITY(1,1) PRIMARY KEY, TenDV NVARCHAR(150) NOT NULL UNIQUE,
 MoTa NVARCHAR(500) NULL,DonGia DECIMAL(18,2) NOT NULL DEFAULT 0,
 TrangThai NVARCHAR(30) NOT NULL DEFAULT 'DANGAPDUNG',
 CONSTRAINT CK_DichVu_DonGia CHECK(DonGia>=0)
);
GO
IF OBJECT_ID(N'dbo.LichHen',N'U') IS NULL
CREATE TABLE dbo.LichHen (
 MaLH INT IDENTITY(1,1) PRIMARY KEY,MaBN INT NOT NULL REFERENCES dbo.BenhNhan(MaBN),
 MaBS INT NOT NULL REFERENCES dbo.BacSi(MaBS),MaTK INT NULL REFERENCES dbo.TaiKhoan(maTK),
 NgayGio DATETIME2 NOT NULL,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xác nhận',
 GhiChu NVARCHAR(1000) NULL,YeuCau NVARCHAR(300) NULL,NgayGioMoi DATETIME2 NULL
);
GO
IF OBJECT_ID(N'dbo.PhieuKham',N'U') IS NULL
CREATE TABLE dbo.PhieuKham (
 MaPK INT IDENTITY(1,1) PRIMARY KEY,MaBN INT NOT NULL REFERENCES dbo.BenhNhan(MaBN),
 MaPhong INT NULL REFERENCES dbo.PhongKham(MaPhong),MaLH INT NULL REFERENCES dbo.LichHen(MaLH),
 NgayKham DATE NOT NULL DEFAULT CONVERT(date,GETDATE()),SoThuTu INT NOT NULL,
 TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ khám',GhiChu NVARCHAR(1000) NULL,
 CONSTRAINT CK_PhieuKham_STT CHECK(SoThuTu>0)
);
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_PhieuKham_Ngay_STT'
 AND object_id=OBJECT_ID('dbo.PhieuKham'))
CREATE UNIQUE INDEX UX_PhieuKham_Ngay_STT ON dbo.PhieuKham(NgayKham,SoThuTu);
GO
IF OBJECT_ID(N'dbo.KetQuaKham',N'U') IS NULL
CREATE TABLE dbo.KetQuaKham (
 MaPK INT NOT NULL PRIMARY KEY REFERENCES dbo.PhieuKham(MaPK),
 MaBS INT NULL REFERENCES dbo.BacSi(MaBS),TrieuChung NVARCHAR(1000) NULL,
 KetQuaLamSang NVARCHAR(2000) NULL,ChanDoan NVARCHAR(1000) NULL,
 HuongDieuTri NVARCHAR(2000) NULL,DonThuoc NVARCHAR(2000) NULL,
 GhiChu NVARCHAR(1000) NULL,NgayTaiKham DATE NULL,
 NgayCapNhat DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
-- Dịch vụ đã thực hiện: nguồn tiền gốc chính thức cho thanh toán mới.
IF OBJECT_ID(N'dbo.DichVuDieuTri',N'U') IS NULL
CREATE TABLE dbo.DichVuDieuTri (
 MaDDT INT IDENTITY(1,1) PRIMARY KEY,TenDDT NVARCHAR(150) NOT NULL UNIQUE,
 DonGia DECIMAL(18,2) NOT NULL DEFAULT 0,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Đang áp dụng',
 CONSTRAINT CK_DDT_DonGia CHECK(DonGia>=0)
);
GO
IF OBJECT_ID(N'dbo.ChiTietDieuTri',N'U') IS NULL
CREATE TABLE dbo.ChiTietDieuTri (
 MaCT INT IDENTITY(1,1) PRIMARY KEY,MaPK INT NOT NULL REFERENCES dbo.PhieuKham(MaPK),
 MaDDT INT NOT NULL REFERENCES dbo.DichVuDieuTri(MaDDT),NoiDung NVARCHAR(500) NULL,
 SoLuong INT NOT NULL DEFAULT 1,DonGia DECIMAL(18,2) NOT NULL,
 ThanhTien AS (CONVERT(DECIMAL(18,2),SoLuong*DonGia)) PERSISTED,
 CONSTRAINT CK_CTDT_SL CHECK(SoLuong>0),CONSTRAINT CK_CTDT_Gia CHECK(DonGia>=0)
);
GO
IF OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
CREATE TABLE dbo.ThanhToan (
 MaTT INT IDENTITY(1,1) PRIMARY KEY,MaPK INT NOT NULL REFERENCES dbo.PhieuKham(MaPK),
 NgayThanhToan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),SoTien DECIMAL(18,2) NOT NULL,
 PhuongThuc NVARCHAR(40) NOT NULL,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',
 MaGiaoDich VARCHAR(100) NULL,CONSTRAINT CK_TT_SoTien CHECK(SoTien>=0)
);
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_TT_MaGD'
 AND object_id=OBJECT_ID(N'dbo.ThanhToan'))
CREATE UNIQUE INDEX UX_TT_MaGD ON dbo.ThanhToan(MaGiaoDich) WHERE MaGiaoDich IS NOT NULL;
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_TT_MaPK_ThanhCong'
 AND object_id=OBJECT_ID(N'dbo.ThanhToan'))
CREATE UNIQUE INDEX UX_TT_MaPK_ThanhCong ON dbo.ThanhToan(MaPK) WHERE TrangThai=N'Thành công';
GO
IF OBJECT_ID(N'dbo.KhuyenMai',N'U') IS NULL
CREATE TABLE dbo.KhuyenMai (
 MaKM VARCHAR(30) NOT NULL PRIMARY KEY,TenKM NVARCHAR(120) NOT NULL,
 LoaiGiam VARCHAR(12) NOT NULL,GiaTri DECIMAL(18,2) NOT NULL,
 NgayBatDau DATE NOT NULL,NgayKetThuc DATE NOT NULL,
 TrangThai BIT NOT NULL DEFAULT 1,
 CONSTRAINT CK_KM_Loai CHECK(LoaiGiam IN('PHAN_TRAM','SO_TIEN')),
 CONSTRAINT CK_KM_GiaTri CHECK(GiaTri>0 AND (LoaiGiam<>'PHAN_TRAM' OR GiaTri<=100)),
 CONSTRAINT CK_KM_Ngay CHECK(NgayBatDau<=NgayKetThuc)
);
GO
-- Cấp mã khuyến mãi tự động trên SQL Server 2012 (không đặt mã tại giao diện).
IF OBJECT_ID(N'dbo.SeqKhuyenMai', N'SO') IS NULL
    EXEC(N'CREATE SEQUENCE dbo.SeqKhuyenMai AS BIGINT START WITH 100000 INCREMENT BY 1');
GO
-- Thông tin ngân hàng phải do phòng khám nhập. Không tự đặt tài khoản demo để tránh chuyển nhầm.
IF OBJECT_ID(N'dbo.CauHinhChuyenKhoan', N'U') IS NULL
CREATE TABLE dbo.CauHinhChuyenKhoan (
    Id TINYINT NOT NULL PRIMARY KEY CHECK(Id=1),
    BinNganHang VARCHAR(6) NOT NULL DEFAULT '',
    SoTaiKhoan VARCHAR(19) NOT NULL DEFAULT '',
    ChuTaiKhoan NVARCHAR(100) NOT NULL DEFAULT N''
);
GO
IF NOT EXISTS(SELECT 1 FROM dbo.CauHinhChuyenKhoan WHERE Id=1)
INSERT dbo.CauHinhChuyenKhoan(Id,BinNganHang,SoTaiKhoan,ChuTaiKhoan)
VALUES(1,'','',N'');
GO
 UPDATE dbo.CauHinhChuyenKhoan
 SET BinNganHang='123456', SoTaiKhoan='0123456789', ChuTaiKhoan=N'Phòng Khám MediDent'
 WHERE Id=1;
 GO
IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL
CREATE TABLE dbo.HoaDon (
 MaHD INT IDENTITY(1,1) PRIMARY KEY,MaPK INT NOT NULL REFERENCES dbo.PhieuKham(MaPK),
 MaTT INT NULL REFERENCES dbo.ThanhToan(MaTT),NgayLap DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 TongTien DECIMAL(18,2) NOT NULL DEFAULT 0,TrangThai NVARCHAR(30) NOT NULL DEFAULT N'Chưa thanh toán',
 MaKM VARCHAR(30) NULL REFERENCES dbo.KhuyenMai(MaKM),
 TienGoc DECIMAL(18,2) NULL,TienGiam DECIMAL(18,2) NULL,
 NguoiLap INT NULL REFERENCES dbo.TaiKhoan(maTK),PhuongThucTT NVARCHAR(40) NULL,
 TienKhachDua DECIMAL(18,2) NULL,TienThua DECIMAL(18,2) NULL,
 GhiChu NVARCHAR(1000) NULL,LyDoHuy NVARCHAR(1000) NULL,
 CONSTRAINT CK_HD_Tien CHECK(TongTien>=0 AND (TienGoc IS NULL OR TienGoc>=0) AND
 (TienGiam IS NULL OR TienGiam>=0) AND (TienGoc IS NULL OR TienGiam IS NULL OR TongTien=TienGoc-TienGiam))
);
GO
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name=N'UX_HoaDon_PhieuKham_ChuaHuy'
 AND object_id=OBJECT_ID(N'dbo.HoaDon'))
CREATE UNIQUE INDEX UX_HoaDon_PhieuKham_ChuaHuy ON dbo.HoaDon(MaPK)
WHERE TrangThai<>N'Đã hủy';
GO
IF OBJECT_ID(N'dbo.ChiTietHoaDon',N'U') IS NULL
CREATE TABLE dbo.ChiTietHoaDon (
 MaCT INT IDENTITY(1,1) PRIMARY KEY,MaHD INT NOT NULL REFERENCES dbo.HoaDon(MaHD),
 MaDV INT NULL REFERENCES dbo.DichVu(MaDV),TenDichVu NVARCHAR(150) NOT NULL,
 SoLuong INT NOT NULL,DonGia DECIMAL(18,2) NOT NULL,
 ThanhTien AS (CONVERT(DECIMAL(18,2),SoLuong*DonGia)) PERSISTED,
 CONSTRAINT CK_CTHD_SL CHECK(SoLuong>0),CONSTRAINT CK_CTHD_Gia CHECK(DonGia>=0)
);
GO
-- Chỉ cấp quyền mặc định khi bổ sung QUYỀN MỚI lần đầu.
-- Nếu quản trị viên bỏ chọn quyền này về sau, chạy lại SQL không ghi đè thay đổi.
DECLARE @quyenKhuyenMaiMoi BIT=CASE WHEN EXISTS(
    SELECT 1 FROM dbo.Quyen WHERE maQuyen='QL_KHUYENMAI') THEN 0 ELSE 1 END;
MERGE dbo.Quyen AS t USING(VALUES('QL_KHUYENMAI',N'Quản lý mã khuyến mãi')) s(maQuyen,tenQuyen)
 ON t.maQuyen=s.maQuyen WHEN NOT MATCHED THEN INSERT(maQuyen,tenQuyen)VALUES(s.maQuyen,s.tenQuyen);
IF @quyenKhuyenMaiMoi=1
INSERT dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,'QL_KHUYENMAI' FROM dbo.VaiTro v
WHERE v.tenVaiTro=N'Quản lý phòng khám'
AND NOT EXISTS(SELECT 1 FROM dbo.VaiTroQuyen q WHERE q.maVaiTro=v.maVaiTro AND q.maQuyen='QL_KHUYENMAI');
GO
GO

-- =========== 02_DuLieuMau_MediDent.sql ===========
-- DU LIEU MAU MEDIDENT - SQL Server 2012+
-- Chay 01_MediDent.sql truoc, sau do chay file nay.
-- Chi them tai khoan neu chua co, khong ghi de tai khoan hien tai.
-- Tai khoan demo (chi dung de hoc/test):
--   admin    / Admin@123     - Quan tri vien
--   quanly   / QuanLy@123    - Quan ly phong kham
--   letan    / LeTan@123     - Le tan
--   letan2   / LeTan2@123    - Le tan
-- Mat khau trong cot matKhau la BCrypt, KHONG luu mat khau dang van ban.

USE MediDent;
GO

DECLARE @adminRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản trị viên');
DECLARE @quanLyRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản lý phòng khám');
DECLARE @leTanRole INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Lễ tân');

IF @adminRole IS NULL OR @quanLyRole IS NULL OR @leTanRole IS NULL
BEGIN
    RAISERROR(N'Chưa có đủ 3 vai trò. Hãy chạy 01_MediDent.sql trước.', 16, 1);
    RETURN;
END;

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='admin')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('admin','$2a$10$eONAdIuMzjaFqOKfAcQ92uap1psba5T.6QvQwV6./YRoxQ65ry4Xi',N'Hoạt động',0,@adminRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='quanly')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('quanly','$2a$10$ZxRiF6Wst7phqf7pZ6GNbuOWXjoJCjVayeXwZU/BHSAteQHm/yFle',N'Hoạt động',0,@quanLyRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='letan')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('letan','$2a$10$5Lg46JdY7EzO2KLg8DDokOiSzvPhGXkPlg5eIhVASdUu/SLATXGFm',N'Hoạt động',0,@leTanRole);

IF NOT EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='letan2')
    INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
    VALUES('letan2','$2a$10$DOj0jmaj6oO6CyJdGbGEbOY8O57vCXO3ZpxJjkoyOZiIt5f9Oo5vO',N'Hoạt động',0,@leTanRole);

-- Ho so nhan vien giup dang nhap, hien thi ho ten va reset mat khau theo ngay sinh.
IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='admin')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Nguyễn Minh An','1995-01-15',N'Nam','0901000001','admin@medident.vn',N'TP. Hồ Chí Minh',N'Quản trị viên',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='admin';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='quanly')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Trần Minh Quân','1992-03-18',N'Nam','0901000002','quanly@medident.vn',N'TP. Hồ Chí Minh',N'Quản lý phòng khám',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='quanly';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='letan')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Lê Thanh Mai','2001-09-12',N'Nữ','0901000003','letan@medident.vn',N'TP. Hồ Chí Minh',N'Lễ tân',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='letan';

IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK WHERE t.tenDangNhap='letan2')
    INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
    SELECT N'Phạm Ngọc Anh','2002-06-21',N'Nữ','0901000004','letan2@medident.vn',N'TP. Hồ Chí Minh',N'Lễ tân',N'Đang làm việc',maTK
    FROM dbo.TaiKhoan WHERE tenDangNhap='letan2';

-- Dong bo bang phan loai nhan su cho chuc nang Quan ly tai khoan.
INSERT INTO dbo.QuanTriVien(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap='admin' AND NOT EXISTS(SELECT 1 FROM dbo.QuanTriVien q WHERE q.maNV=n.maNV);

INSERT INTO dbo.QuanLy(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap='quanly' AND NOT EXISTS(SELECT 1 FROM dbo.QuanLy q WHERE q.maNV=n.maNV);

INSERT INTO dbo.LeTan(maNV)
SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
WHERE t.tenDangNhap IN ('letan','letan2') AND NOT EXISTS(SELECT 1 FROM dbo.LeTan l WHERE l.maNV=n.maNV);

-- Admin thường chỉ có menu và quyền Admin. Tài khoản full ngoại lệ riêng trong 03.

-- Neu vai tro Quan ly/Le tan chua duoc cap quyen lan nao, gan quyen demo day du.
-- Khong ghi de vai tro da duoc tick/bo tick trong man hinh Phan quyen.
IF NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=@quanLyRole)
    INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
    SELECT @quanLyRole,maQuyen FROM dbo.Quyen
    WHERE maQuyen IN ('HOTRO','QL_NHANVIEN','QL_DICHVU','QL_BENHNHAN',
                      'QL_LICHHEN','QL_THANHTOAN','QL_KHUYENMAI','QL_BAOCAO');

IF NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen WHERE maVaiTro=@leTanRole)
    INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
    SELECT @leTanRole,maQuyen FROM dbo.Quyen
    WHERE maQuyen IN ('HOTRO','LT_KHAM','LT_TIEPNHAN','LT_LICHHEN',
                      'LT_THANHTOAN','LT_TRACUU');

SELECT t.tenDangNhap, n.hoTen, v.tenVaiTro, t.trangThai, n.ngaySinh
FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON t.maVaiTro=v.maVaiTro
JOIN dbo.NhanVien n ON n.maTK=t.maTK
WHERE t.tenDangNhap IN ('admin','quanly','letan','letan2');
-- Kiem tra so quyen menu cua cac tai khoan demo theo vai tro.
SELECT v.tenVaiTro,COUNT(vq.maQuyen) AS soQuyenHienThi
FROM dbo.VaiTro v LEFT JOIN dbo.VaiTroQuyen vq ON vq.maVaiTro=v.maVaiTro
WHERE v.tenVaiTro IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')
GROUP BY v.tenVaiTro;
GO

-- ===== DỮ LIỆU DEMO NGHIỆP VỤ / KHÔNG XÓA DỮ LIỆU CŨ =====
USE MediDent;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRAN;
 DECLARE @BN TABLE(ten NVARCHAR(120),sdt VARCHAR(20),gioiTinh NVARCHAR(15));
 INSERT @BN VALUES
 (N'Nguyễn Minh Khoa','0909990101','NAM'),(N'Lê Thanh Trúc','0909990102','NU'),
 (N'Trần Quốc Bảo','0909990103','NAM'),(N'Phạm Thảo My','0909990104','NU'),
 (N'Hoàng Gia Huy','0909990105','NAM'),(N'Võ Khánh Linh','0909990106','NU');
 INSERT dbo.BenhNhan(HoTen,NgaySinh,GioiTinh,Sdt,Email,DiaChi,TienSuBenh,TrangThai,TrangThaiHoSo)
 SELECT b.ten,'2000-05-18',b.gioiTinh,b.sdt,'demo_'+b.sdt+'@medident.vn',N'TP. Hồ Chí Minh',N'Không',
 'DANGDIEUTRI',N'Hoạt động' FROM @BN b
 WHERE NOT EXISTS(SELECT 1 FROM dbo.BenhNhan old WHERE old.Sdt=b.sdt);

 IF NOT EXISTS(SELECT 1 FROM dbo.BacSi WHERE HoTen=N'BS. Nguyễn Hải Nam')
 INSERT dbo.BacSi(HoTen,ChuyenKhoa) VALUES(N'BS. Nguyễn Hải Nam',N'Nha khoa tổng quát');
 IF NOT EXISTS(SELECT 1 FROM dbo.BacSi WHERE HoTen=N'BS. Trần Minh Anh')
 INSERT dbo.BacSi(HoTen,ChuyenKhoa) VALUES(N'BS. Trần Minh Anh',N'Nội nha');
 IF NOT EXISTS(SELECT 1 FROM dbo.PhongKham WHERE TenPhong=N'Phòng khám 01')
 INSERT dbo.PhongKham(TenPhong,SucChua) VALUES(N'Phòng khám 01',3);
 IF NOT EXISTS(SELECT 1 FROM dbo.PhongKham WHERE TenPhong=N'Phòng khám 02')
 INSERT dbo.PhongKham(TenPhong,SucChua) VALUES(N'Phòng khám 02',3);

 DECLARE @DV TABLE(ten NVARCHAR(150),gia DECIMAL(18,2));
 INSERT @DV VALUES(N'Khám tổng quát',150000),(N'Cạo vôi răng',300000),
 (N'Trám răng',450000),(N'Nhổ răng',600000),(N'Điều trị tủy',1200000),
 (N'Tẩy trắng răng',1500000),(N'Chụp X-quang',200000);
 INSERT dbo.DichVu(TenDV,MoTa,DonGia,TrangThai)
 SELECT d.ten,N'Dịch vụ nha khoa demo',d.gia,'DANGAPDUNG' FROM @DV d
 WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVu x WHERE x.TenDV=d.ten);
 INSERT dbo.DichVuDieuTri(TenDDT,DonGia,TrangThai)
 SELECT d.ten,d.gia,N'Đang áp dụng' FROM @DV d
 WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVuDieuTri x WHERE x.TenDDT=d.ten);

 DECLARE @bs INT=(SELECT TOP 1 MaBS FROM dbo.BacSi WHERE HoTen=N'BS. Nguyễn Hải Nam');
 DECLARE @tk INT=(SELECT TOP 1 maTK FROM dbo.TaiKhoan WHERE tenDangNhap='letan');
 INSERT dbo.LichHen(MaBN,MaBS,MaTK,NgayGio,TrangThai,GhiChu)
 SELECT b.MaBN,@bs,@tk,DATEADD(day,2,DATEADD(hour,9+CONVERT(INT,RIGHT(b.Sdt,1)),CAST(CONVERT(date,GETDATE()) AS DATETIME2))),
 N'Đã xác nhận',N'MEDIDENT-DEMO-LH-'+b.Sdt
 FROM dbo.BenhNhan b JOIN @BN m ON m.sdt=b.Sdt
 WHERE NOT EXISTS(SELECT 1 FROM dbo.LichHen x WHERE x.GhiChu=N'MEDIDENT-DEMO-LH-'+b.Sdt);

 ;WITH DanhSach AS (
 SELECT b.MaBN,b.Sdt,ROW_NUMBER() OVER(ORDER BY b.Sdt) AS stt
 FROM dbo.BenhNhan b JOIN @BN m ON m.sdt=b.Sdt
 WHERE NOT EXISTS(SELECT 1 FROM dbo.PhieuKham p WHERE p.GhiChu=N'MEDIDENT-DEMO-PK-'+b.Sdt)
 )
 INSERT dbo.PhieuKham(MaBN,MaPhong,NgayKham,SoThuTu,TrangThai,GhiChu)
 SELECT x.MaBN,(SELECT TOP 1 MaPhong FROM dbo.PhongKham ORDER BY MaPhong),
 CONVERT(date,GETDATE()),(SELECT ISNULL(MAX(SoThuTu),0) FROM dbo.PhieuKham WHERE NgayKham=CONVERT(date,GETDATE())) + x.stt,
 CASE WHEN x.stt%3=0 THEN N'Chờ khám' ELSE N'Hoàn thành' END,N'MEDIDENT-DEMO-PK-'+x.Sdt
 FROM DanhSach x;

 INSERT dbo.KetQuaKham(MaPK,MaBS,TrieuChung,ChanDoan,HuongDieuTri,GhiChu)
 SELECT p.MaPK,@bs,N'Ê buốt răng',N'Viêm lợi nhẹ',N'Đã xử lý',N'Kết quả DEMO'
 FROM dbo.PhieuKham p WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PK-%'
 AND p.TrangThai=N'Hoàn thành'
 AND NOT EXISTS(SELECT 1 FROM dbo.KetQuaKham k WHERE k.MaPK=p.MaPK);

 INSERT dbo.ChiTietDieuTri(MaPK,MaDDT,NoiDung,SoLuong,DonGia)
 SELECT p.MaPK,d.MaDDT,N'Điều trị demo',1,d.DonGia
 FROM dbo.PhieuKham p
 JOIN dbo.BenhNhan b ON b.MaBN=p.MaBN
 JOIN dbo.DichVuDieuTri d ON d.TenDDT=CASE WHEN RIGHT(b.Sdt,1) IN('1','4') THEN N'Điều trị tủy'
 WHEN RIGHT(b.Sdt,1) IN('2','5') THEN N'Cạo vôi răng' ELSE N'Trám răng' END
 WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PK-%' AND p.TrangThai=N'Hoàn thành'
 AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietDieuTri x WHERE x.MaPK=p.MaPK);

 -- 1 hóa đơn DEMO đã thu, số còn lại để thử tự áp dụng khuyến mãi trên UI.
 DECLARE @pk INT=(SELECT TOP 1 p.MaPK FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON b.MaBN=p.MaBN
 WHERE b.Sdt='0909990101' AND p.TrangThai=N'Hoàn thành' ORDER BY p.MaPK DESC);
 IF @pk IS NOT NULL AND NOT EXISTS(SELECT 1 FROM dbo.ThanhToan WHERE MaPK=@pk AND TrangThai=N'Thành công')
 BEGIN
   DECLARE @tien DECIMAL(18,2)=(SELECT SUM(ThanhTien) FROM dbo.ChiTietDieuTri WHERE MaPK=@pk);
   IF @tien>0
   BEGIN
    INSERT dbo.ThanhToan(MaPK,NgayThanhToan,SoTien,PhuongThuc,TrangThai,MaGiaoDich)
    VALUES(@pk,SYSDATETIME(),@tien,N'Tiền mặt',N'Thành công',NULL);
    DECLARE @tt INT=SCOPE_IDENTITY();
    INSERT dbo.HoaDon(MaPK,MaTT,TongTien,TrangThai,TienGoc,TienGiam,NguoiLap,PhuongThucTT,TienKhachDua,TienThua,GhiChu)
    VALUES(@pk,@tt,@tien,N'Đã thanh toán',@tien,0,@tk,N'Tiền mặt',@tien,0,N'Hóa đơn demo trước ưu đãi');
    DECLARE @hd INT=SCOPE_IDENTITY();
    INSERT dbo.ChiTietHoaDon(MaHD,MaDV,TenDichVu,SoLuong,DonGia)
    SELECT @hd,NULL,d.TenDDT,x.SoLuong,x.DonGia FROM dbo.ChiTietDieuTri x
    JOIN dbo.DichVuDieuTri d ON d.MaDDT=x.MaDDT WHERE x.MaPK=@pk;
   END
 END

 INSERT dbo.KhuyenMai(MaKM,TenKM,LoaiGiam,GiaTri,NgayBatDau,NgayKetThuc,TrangThai)
 SELECT v.MaKM,v.TenKM,v.LoaiGiam,v.GiaTri,DATEADD(day,-1,CONVERT(date,GETDATE())),
 DATEADD(day,365,CONVERT(date,GETDATE())),1
 FROM (VALUES('KM10',N'Giảm 10%', 'PHAN_TRAM',CONVERT(DECIMAL(18,2),10)),
 ('KM20',N'Giảm 20%','PHAN_TRAM',CONVERT(DECIMAL(18,2),20)),
 ('KM300',N'Giảm 300.000đ','SO_TIEN',CONVERT(DECIMAL(18,2),300000))) v(MaKM,TenKM,LoaiGiam,GiaTri)
 WHERE NOT EXISTS(SELECT 1 FROM dbo.KhuyenMai k WHERE k.MaKM=v.MaKM);
 COMMIT;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
GO
GO

-- =========== 03_TaiKhoan_Demo_Full.sql ===========
-- Tai khoan full dung de demo, duoc map ngoai le tai GUI; KHONG sua ma tran quyen 3 vai tro.
-- Chay 01_MediDent.sql va 02_DuLieuMau_MediDent.sql truoc.
-- full / MediDent#2026Demo! (chi demo, nen doi mat khau sau khi kiem thu)
USE MediDent;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRAN;
    DECLARE @role INT = (SELECT maVaiTro FROM dbo.VaiTro WHERE tenVaiTro=N'Quản trị viên');
    IF @role IS NULL THROW 50001, N'Chua co vai tro Quan tri vien.',1;
    IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan WHERE tenDangNhap='full')
        INSERT INTO dbo.TaiKhoan(tenDangNhap,matKhau,trangThai,phaiDoiMatKhau,maVaiTro)
        VALUES('full','$2a$11$NaPrqPt1bieyvcFd3nm03eyZmbUgedV1UTMV3gfoW/.DFdfhuwGbm',N'Hoạt động',0,@role);
    -- Chi dong bo tai khoan demo full; khong sua ba vai tro hoac tai khoan khac.
    UPDATE dbo.TaiKhoan SET maVaiTro=@role,matKhau='$2a$11$NaPrqPt1bieyvcFd3nm03eyZmbUgedV1UTMV3gfoW/.DFdfhuwGbm',
        trangThai=N'Hoạt động',phaiDoiMatKhau=0 WHERE tenDangNhap='full';
    IF NOT EXISTS(SELECT 1 FROM dbo.NhanVien WHERE maTK=(SELECT maTK FROM dbo.TaiKhoan WHERE tenDangNhap='full'))
        INSERT INTO dbo.NhanVien(hoTen,ngaySinh,gioiTinh,sdt,email,diaChi,chucVu,trangThai,maTK)
        SELECT N'Phạm Ngọc Phúc','1998-02-25',N'Nam','0901000099','full@medident.vn',
               N'TP. Hồ Chí Minh',N'Quản trị viên',N'Đang làm việc',maTK
        FROM dbo.TaiKhoan WHERE tenDangNhap='full';
    INSERT INTO dbo.QuanTriVien(maNV)
    SELECT n.maNV FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
    WHERE t.tenDangNhap='full' AND NOT EXISTS
        (SELECT 1 FROM dbo.QuanTriVien q WHERE q.maNV=n.maNV);
    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    THROW;
END CATCH;
GO
GO

-- =========== 04_BO_SUNG_NHAT_KY_DEMO_DU_24_BANG ===========
-- Chỉ thêm bản ghi minh họa nếu chưa tồn tại, không ghi đè lịch sử thật.
USE MediDent;
GO
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRAN;

    IF NOT EXISTS (SELECT 1 FROM dbo.NhatKyHeThong
                   WHERE doiTuong=N'MEDIDENT-DEMO-KHOITAO')
        INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa)
        VALUES(N'Khởi tạo dữ liệu mẫu',N'MEDIDENT-DEMO-KHOITAO',
               N'Dữ liệu DEMO, dùng để kiểm thử giao diện nhật ký hệ thống.');

    IF NOT EXISTS (SELECT 1 FROM dbo.NhatKyHeThong
                   WHERE doiTuong=N'MEDIDENT-DEMO-HOADON')
        INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa)
        VALUES(N'Lập hóa đơn',N'MEDIDENT-DEMO-HOADON',
               N'Lập hóa đơn demo từ phiếu khám đã hoàn thành.');

    IF NOT EXISTS (SELECT 1 FROM dbo.NhatKyHeThong
                   WHERE doiTuong=N'MEDIDENT-DEMO-KHUYENMAI')
        INSERT INTO dbo.NhatKyHeThong(hanhDong,doiTuong,moTa)
        VALUES(N'Cấu hình khuyến mãi',N'MEDIDENT-DEMO-KHUYENMAI',
               N'Ví dụ sử dụng KM10, KM20 và KM300.');

    INSERT INTO dbo.LichSuDangNhap(maTK,tenDangNhap,tenVaiTro,thoiGian,ketQua)
    SELECT t.maTK, t.tenDangNhap, v.tenVaiTro,
           DATEADD(MINUTE,-10,SYSDATETIME()),N'Thành công'
    FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
    WHERE t.tenDangNhap IN ('admin','quanly','letan','letan2','full')
      AND NOT EXISTS (SELECT 1 FROM dbo.LichSuDangNhap l
                      WHERE l.tenDangNhap=t.tenDangNhap
                        AND l.ketQua=N'Thành công');

    IF NOT EXISTS (SELECT 1 FROM dbo.LichSuDangNhap
                   WHERE tenDangNhap='demo_sai_mat_khau')
        INSERT INTO dbo.LichSuDangNhap(maTK,tenDangNhap,tenVaiTro,thoiGian,ketQua)
        VALUES(NULL,'demo_sai_mat_khau',NULL,DATEADD(MINUTE,-5,SYSDATETIME()),N'Thất bại');

    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO


-- Bảng lịch làm việc còn thiếu trong bản gốc; giữ nguyên các bảng hiện có.
IF OBJECT_ID(N'dbo.LichLamViec',N'U') IS NULL
CREATE TABLE dbo.LichLamViec (
    maLLV INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    maNV INT NOT NULL REFERENCES dbo.NhanVien(maNV),
    ngayLamViec DATE NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    phongKham NVARCHAR(100) NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Hoạt động',
    CONSTRAINT CK_LLV_ThoiGian CHECK (gioBatDau<gioKetThuc)
);
GO


-- Dữ liệu ca làm việc mẫu cho phần quản lý lịch làm việc.
INSERT INTO dbo.LichLamViec(maNV,ngayLamViec,gioBatDau,gioKetThuc,phongKham,trangThai)
SELECT TOP 3 n.maNV,DATEADD(DAY,1,CONVERT(DATE,GETDATE())),
       CAST('08:00' AS TIME),CAST('12:00' AS TIME),N'Phòng 01',N'Hoạt động'
FROM dbo.NhanVien n
WHERE n.trangThai=N'Đang làm việc'
  AND NOT EXISTS (SELECT 1 FROM dbo.LichLamViec x WHERE x.maNV=n.maNV
                  AND x.ngayLamViec=DATEADD(DAY,1,CONVERT(DATE,GETDATE())))
ORDER BY n.maNV;
GO

-- Kiểm tra số bản ghi trên 25 bảng, gồm lịch làm việc vừa bổ sung.
-- Phải có dữ liệu ở mỗi bảng; CauHinhChuyenKhoan để trống thông tin ngân hàng có chủ ý.
SELECT N'VaiTro' AS TenBang, COUNT(*) AS SoDong FROM dbo.VaiTro
UNION ALL SELECT N'TaiKhoan', COUNT(*) FROM dbo.TaiKhoan
UNION ALL SELECT N'NhanVien', COUNT(*) FROM dbo.NhanVien
UNION ALL SELECT N'QuanTriVien', COUNT(*) FROM dbo.QuanTriVien
UNION ALL SELECT N'QuanLy', COUNT(*) FROM dbo.QuanLy
UNION ALL SELECT N'LeTan', COUNT(*) FROM dbo.LeTan
UNION ALL SELECT N'Quyen', COUNT(*) FROM dbo.Quyen
UNION ALL SELECT N'VaiTroQuyen', COUNT(*) FROM dbo.VaiTroQuyen
UNION ALL SELECT N'NhatKyHeThong', COUNT(*) FROM dbo.NhatKyHeThong
UNION ALL SELECT N'LichSuDangNhap', COUNT(*) FROM dbo.LichSuDangNhap
UNION ALL SELECT N'BenhNhan', COUNT(*) FROM dbo.BenhNhan
UNION ALL SELECT N'BacSi', COUNT(*) FROM dbo.BacSi
UNION ALL SELECT N'PhongKham', COUNT(*) FROM dbo.PhongKham
UNION ALL SELECT N'DichVu', COUNT(*) FROM dbo.DichVu
UNION ALL SELECT N'LichHen', COUNT(*) FROM dbo.LichHen
UNION ALL SELECT N'PhieuKham', COUNT(*) FROM dbo.PhieuKham
UNION ALL SELECT N'KetQuaKham', COUNT(*) FROM dbo.KetQuaKham
UNION ALL SELECT N'DichVuDieuTri', COUNT(*) FROM dbo.DichVuDieuTri
UNION ALL SELECT N'ChiTietDieuTri', COUNT(*) FROM dbo.ChiTietDieuTri
UNION ALL SELECT N'ThanhToan', COUNT(*) FROM dbo.ThanhToan
UNION ALL SELECT N'KhuyenMai', COUNT(*) FROM dbo.KhuyenMai
UNION ALL SELECT N'CauHinhChuyenKhoan', COUNT(*) FROM dbo.CauHinhChuyenKhoan
UNION ALL SELECT N'HoaDon', COUNT(*) FROM dbo.HoaDon
UNION ALL SELECT N'ChiTietHoaDon', COUNT(*) FROM dbo.ChiTietHoaDon
UNION ALL SELECT N'LichLamViec', COUNT(*) FROM dbo.LichLamViec;
GO

-- QUYỀN NGHIỆP VỤ BẮT BUỘC CHO DATABASE DEMO (không chạm quyền Admin ở app).
USE MediDent;
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v JOIN dbo.Quyen q
 ON ((v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen='QL_KHUYENMAI')
  OR (v.tenVaiTro=N'Lễ tân' AND q.maQuyen='LT_THANHTOAN'))
WHERE NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen x
 WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen);
GO


-- ============================================================================
-- BO SUNG 3 BANG CO TRONG medident_nam NHUNG KHONG CO TRONG MediDent_2.
-- Khong thay doi bang thanh toan / khuyen mai hien co cua MediDent_2.
-- ============================================================================
USE MediDent;
GO
IF OBJECT_ID(N'dbo.ChiTietPhieuKham',N'U') IS NULL
CREATE TABLE dbo.ChiTietPhieuKham (
    maCT INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    maPK INT NOT NULL REFERENCES dbo.PhieuKham(MaPK),
    maDV INT NOT NULL REFERENCES dbo.DichVu(MaDV),
    soLuong INT NOT NULL DEFAULT(1),
    donGia DECIMAL(18,2) NOT NULL,
    thanhTien AS (CONVERT(DECIMAL(18,2), soLuong*donGia)) PERSISTED,
    CONSTRAINT CK_CTPK_SL CHECK(soLuong>0),
    CONSTRAINT CK_CTPK_GIA CHECK(donGia>=0)
);
GO
IF OBJECT_ID(N'dbo.YeuCauHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.YeuCauHuyPhieuThu (
    maYC INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    maTT INT NOT NULL REFERENCES dbo.ThanhToan(MaTT),
    lyDo NVARCHAR(500) NOT NULL,
    trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ duyệt',
    ngayYeuCau DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO
IF OBJECT_ID(N'dbo.PhieuDuyetHuyPhieuThu',N'U') IS NULL
CREATE TABLE dbo.PhieuDuyetHuyPhieuThu (
    maPD INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    maYC INT NOT NULL REFERENCES dbo.YeuCauHuyPhieuThu(maYC),
    ngayPheDuyet DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    lyDoPheDuyet NVARCHAR(500) NULL
);
GO

-- ============================================================================
-- DU LIEU DEMO MO RONG: BENH NHAN, DICH VU, LICH, KHAM, HOA DON, YEU CAU.
-- Chi INSERT neu chua co; khong xoa/sua du lieu that tren database da co.
-- ============================================================================
SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRAN;

    DECLARE @DSBN TABLE (sdt VARCHAR(20), hoTen NVARCHAR(120), gioiTinh NVARCHAR(15),ngaySinh DATE);
    INSERT INTO @DSBN VALUES
        ('0909990201',N'Phan Minh Tâm',N'NAM','1994-04-19'),
        ('0909990202',N'Trịnh Ngọc Diệp',N'NU','1998-03-27'),
        ('0909990203',N'Hoàng Trọng Đức',N'NAM','1985-10-02'),
        ('0909990204',N'Lâm Hải Yến',N'NU','2000-06-15'),
        ('0909990205',N'Nguyễn Hoàng Hà',N'NAM','2003-08-05'),
        ('0909990206',N'Võ Phương Anh',N'NU','1996-12-12'),
        ('0909990207',N'Đặng Minh Hiếu',N'NAM','1989-07-30'),
        ('0909990208',N'Đỗ Ngọc Trâm',N'NU','2001-11-23');
    INSERT dbo.BenhNhan(HoTen,NgaySinh,GioiTinh,Sdt,Email,DiaChi,TienSuBenh,TrangThai,TrangThaiHoSo)
    SELECT b.hoTen,b.ngaySinh,b.gioiTinh,b.sdt,'demo_'+b.sdt+'@medident.vn',
           N'TP. Hồ Chí Minh',N'Dữ liệu khám thử',N'DANGDIEUTRI',N'Hoạt động'
    FROM @DSBN b WHERE NOT EXISTS(SELECT 1 FROM dbo.BenhNhan x WHERE x.Sdt=b.sdt);

    INSERT dbo.BacSi(HoTen,ChuyenKhoa)
    SELECT N'BS. Lê Quốc Khánh',N'Chỉnh nha'
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BacSi WHERE HoTen=N'BS. Lê Quốc Khánh');
    INSERT dbo.BacSi(HoTen,ChuyenKhoa)
    SELECT N'BS. Phạm Thu Thủy',N'Nha khoa thẩm mỹ'
    WHERE NOT EXISTS(SELECT 1 FROM dbo.BacSi WHERE HoTen=N'BS. Phạm Thu Thủy');

    INSERT dbo.PhongKham(TenPhong,SucChua)
    SELECT N'Phòng khám 03',2
    WHERE NOT EXISTS(SELECT 1 FROM dbo.PhongKham WHERE TenPhong=N'Phòng khám 03');

    DECLARE @DVD TABLE(ten NVARCHAR(150),gia DECIMAL(18,2));
    INSERT @DVD VALUES(N'Chỉnh nha mắc cài',5000000),(N'Nhổ răng khôn',1500000),
        (N'Phục hình răng sứ',3500000),(N'Tư vấn implant',250000);
    INSERT dbo.DichVu(TenDV,MoTa,DonGia,TrangThai)
    SELECT d.ten,N'Dịch vụ bổ sung demo',d.gia,'DANGAPDUNG' FROM @DVD d
    WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVu x WHERE x.TenDV=d.ten);
    INSERT dbo.DichVuDieuTri(TenDDT,DonGia,TrangThai)
    SELECT d.ten,d.gia,N'Đang áp dụng' FROM @DVD d
    WHERE NOT EXISTS(SELECT 1 FROM dbo.DichVuDieuTri x WHERE x.TenDDT=d.ten);

    DECLARE @bs INT=(SELECT TOP 1 MaBS FROM dbo.BacSi WHERE HoTen=N'BS. Lê Quốc Khánh');
    DECLARE @tk INT=(SELECT TOP 1 maTK FROM dbo.TaiKhoan WHERE tenDangNhap='letan');
    DECLARE @phong INT=(SELECT TOP 1 MaPhong FROM dbo.PhongKham WHERE TenPhong=N'Phòng khám 03');

    INSERT dbo.LichHen(MaBN,MaBS,MaTK,NgayGio,TrangThai,GhiChu)
    SELECT bn.MaBN,@bs,@tk,
           DATEADD(DAY,2+CONVERT(INT,RIGHT(bn.Sdt,1)),DATEADD(HOUR,9,CAST(CONVERT(DATE,GETDATE()) AS DATETIME2))),
           N'Đã xác nhận',N'MEDIDENT-DEMO-PLUS-LH-'+bn.Sdt
    FROM dbo.BenhNhan bn JOIN @DSBN d ON d.sdt=bn.Sdt
    WHERE NOT EXISTS(SELECT 1 FROM dbo.LichHen l WHERE l.GhiChu=N'MEDIDENT-DEMO-PLUS-LH-'+bn.Sdt);

    ;WITH ds AS (
        SELECT bn.MaBN,bn.Sdt,ROW_NUMBER() OVER (ORDER BY bn.Sdt) AS rn
        FROM dbo.BenhNhan bn JOIN @DSBN d ON d.sdt=bn.Sdt
        WHERE NOT EXISTS(SELECT 1 FROM dbo.PhieuKham p WHERE p.GhiChu=N'MEDIDENT-DEMO-PLUS-PK-'+bn.Sdt)
    )
    INSERT dbo.PhieuKham(MaBN,MaPhong,MaLH,NgayKham,SoThuTu,TrangThai,GhiChu)
    SELECT ds.MaBN,@phong,
           (SELECT TOP 1 l.MaLH FROM dbo.LichHen l WHERE l.MaBN=ds.MaBN AND l.GhiChu=N'MEDIDENT-DEMO-PLUS-LH-'+ds.Sdt ORDER BY l.MaLH DESC),
           CONVERT(DATE,GETDATE()),
           (SELECT ISNULL(MAX(p.SoThuTu),0) FROM dbo.PhieuKham p WHERE p.NgayKham=CONVERT(DATE,GETDATE())) + ds.rn,
           N'Hoàn thành',N'MEDIDENT-DEMO-PLUS-PK-'+ds.Sdt
    FROM ds;

    INSERT dbo.KetQuaKham(MaPK,MaBS,TrieuChung,ChanDoan,HuongDieuTri,GhiChu)
    SELECT p.MaPK,@bs,N'Khám tổng quát',N'Theo dõi răng miệng',N'Điều trị theo chỉ định',N'DEMO - có tên bác sĩ trên hóa đơn'
    FROM dbo.PhieuKham p WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PLUS-PK-%'
    AND NOT EXISTS(SELECT 1 FROM dbo.KetQuaKham k WHERE k.MaPK=p.MaPK);

    INSERT dbo.ChiTietDieuTri(MaPK,MaDDT,NoiDung,SoLuong,DonGia)
    SELECT p.MaPK,d.MaDDT,N'Dịch vụ điều trị demo',1,d.DonGia
    FROM dbo.PhieuKham p JOIN dbo.BenhNhan bn ON bn.MaBN=p.MaBN
    JOIN dbo.DichVuDieuTri d ON d.TenDDT=CASE WHEN RIGHT(bn.Sdt,1) IN('1','4','7')
        THEN N'Chỉnh nha mắc cài' WHEN RIGHT(bn.Sdt,1) IN('2','5','8') THEN N'Nhổ răng khôn'
        ELSE N'Phục hình răng sứ' END
    WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PLUS-PK-%'
    AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietDieuTri x WHERE x.MaPK=p.MaPK);

    INSERT dbo.ChiTietPhieuKham(maPK,maDV,soLuong,donGia)
    SELECT p.MaPK,d.MaDV,1,d.DonGia FROM dbo.PhieuKham p
    CROSS JOIN (SELECT TOP 1 MaDV,DonGia FROM dbo.DichVu WHERE TenDV=N'Khám tổng quát') d
    WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PLUS-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietPhieuKham c WHERE c.maPK=p.MaPK);

    -- Tạo thêm hai hóa đơn đã thu để kiểm tra thống kê, lịch sử, thông tin bác sĩ trong PDF.
    DECLARE @dsPK TABLE(maPK INT PRIMARY KEY,soTT INT IDENTITY(1,1));
    INSERT @dsPK(maPK)
    SELECT TOP 2 p.MaPK FROM dbo.PhieuKham p WHERE p.GhiChu LIKE N'MEDIDENT-DEMO-PLUS-PK-%'
      AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.MaPK=p.MaPK AND h.TrangThai<>N'Đã hủy')
    ORDER BY p.MaPK;
    DECLARE @i INT=1,@pk INT,@so DECIMAL(18,2),@tt INT,@hd INT;
    WHILE @i<=2
    BEGIN
        SET @pk=NULL;
        SELECT @pk=maPK FROM @dsPK WHERE soTT=@i;
        IF @pk IS NOT NULL
        BEGIN
            SELECT @so=SUM(ThanhTien) FROM dbo.ChiTietDieuTri WHERE MaPK=@pk;
            IF @so>0
            BEGIN
                INSERT dbo.ThanhToan(MaPK,NgayThanhToan,SoTien,PhuongThuc,TrangThai,MaGiaoDich)
                VALUES(@pk,SYSDATETIME(),@so,N'Tiền mặt',N'Thành công',NULL);
                SET @tt=CONVERT(INT,SCOPE_IDENTITY());
                INSERT dbo.HoaDon(MaPK,MaTT,TongTien,TrangThai,TienGoc,TienGiam,NguoiLap,PhuongThucTT,TienKhachDua,TienThua,GhiChu)
                VALUES(@pk,@tt,@so,N'Đã thanh toán',@so,0,@tk,N'Tiền mặt',@so,0,N'Hóa đơn demo mở rộng');
                SET @hd=CONVERT(INT,SCOPE_IDENTITY());
                INSERT dbo.ChiTietHoaDon(MaHD,MaDV,TenDichVu,SoLuong,DonGia)
                SELECT @hd,NULL,dt.TenDDT,ct.SoLuong,ct.DonGia FROM dbo.ChiTietDieuTri ct
                JOIN dbo.DichVuDieuTri dt ON dt.MaDDT=ct.MaDDT WHERE ct.MaPK=@pk;
            END
        END
        SET @i=@i+1;
    END

    IF NOT EXISTS (SELECT 1 FROM dbo.YeuCauHuyPhieuThu WHERE lyDo=N'DEMO: yêu cầu hủy phiếu thu đã từ chối')
        INSERT dbo.YeuCauHuyPhieuThu(maTT,lyDo,trangThai,ngayYeuCau)
        SELECT TOP 1 MaTT,N'DEMO: yêu cầu hủy phiếu thu đã từ chối',N'Đã từ chối',SYSDATETIME()
        FROM dbo.ThanhToan WHERE TrangThai=N'Thành công' ORDER BY MaTT DESC;
    IF NOT EXISTS (SELECT 1 FROM dbo.PhieuDuyetHuyPhieuThu p
                   JOIN dbo.YeuCauHuyPhieuThu y ON y.maYC=p.maYC
                   WHERE y.lyDo=N'DEMO: yêu cầu hủy phiếu thu đã từ chối')
        INSERT dbo.PhieuDuyetHuyPhieuThu(maYC,ngayPheDuyet,lyDoPheDuyet)
        SELECT TOP 1 maYC,SYSDATETIME(),N'DEMO: từ chối do hồ sơ thiếu căn cứ'
        FROM dbo.YeuCauHuyPhieuThu WHERE lyDo=N'DEMO: yêu cầu hủy phiếu thu đã từ chối';

    IF NOT EXISTS (SELECT 1 FROM dbo.KhuyenMai WHERE MaKM='KMDABAN')
        INSERT dbo.KhuyenMai(MaKM,TenKM,LoaiGiam,GiaTri,NgayBatDau,NgayKetThuc,TrangThai)
        VALUES('KMDABAN',N'Demo ưu đãi đã hết hạn','PHAN_TRAM',15,'2025-01-01','2025-12-31',0);

    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    THROW;
END CATCH;
GO

-- KIỂM TRA BAO PHỦ 28 BẢNG (KHÔNG CHỨNG NHẬN KẾT NỐI JDBC).
DECLARE @bang TABLE(ten SYSNAME PRIMARY KEY);
INSERT INTO @bang(ten) VALUES
 (N'VaiTro'),(N'TaiKhoan'),(N'NhanVien'),(N'QuanTriVien'),(N'QuanLy'),(N'LeTan'),
 (N'Quyen'),(N'VaiTroQuyen'),(N'NhatKyHeThong'),(N'LichSuDangNhap'),
 (N'BenhNhan'),(N'BacSi'),(N'PhongKham'),(N'DichVu'),(N'LichHen'),
 (N'PhieuKham'),(N'KetQuaKham'),(N'DichVuDieuTri'),(N'ChiTietDieuTri'),
 (N'ThanhToan'),(N'KhuyenMai'),(N'CauHinhChuyenKhoan'),(N'HoaDon'),
 (N'ChiTietHoaDon'),(N'LichLamViec'),(N'ChiTietPhieuKham'),
 (N'YeuCauHuyPhieuThu'),(N'PhieuDuyetHuyPhieuThu');
DECLARE @bangTen SYSNAME,@so INT,@sql NVARCHAR(MAX);
DECLARE @ketQua TABLE(TenBang SYSNAME,SoDong BIGINT,TrangThai NVARCHAR(30));
DECLARE ds CURSOR LOCAL FAST_FORWARD FOR SELECT ten FROM @bang ORDER BY ten;
OPEN ds;
FETCH NEXT FROM ds INTO @bangTen;
WHILE @@FETCH_STATUS=0
BEGIN
    IF OBJECT_ID(N'dbo.'+@bangTen,N'U') IS NULL
        INSERT @ketQua VALUES(@bangTen,NULL,N'THIẾU BẢNG');
    ELSE
    BEGIN
        SET @sql=N'SELECT @n=COUNT_BIG(*) FROM dbo.'+QUOTENAME(@bangTen);
        DECLARE @n BIGINT;
        EXEC sp_executesql @sql,N'@n BIGINT OUTPUT',@n=@n OUTPUT;
        INSERT @ketQua VALUES(@bangTen,@n,CASE WHEN @n=0 THEN N'CHƯA CÓ DỮ LIỆU' ELSE N'ĐÃ CÓ DỮ LIỆU' END);
    END
    FETCH NEXT FROM ds INTO @bangTen;
END
CLOSE ds;
DEALLOCATE ds;
SELECT TenBang,SoDong,TrangThai FROM @ketQua ORDER BY TenBang;
IF EXISTS(SELECT 1 FROM @ketQua WHERE SoDong IS NULL OR SoDong=0)
    RAISERROR(N'Kiểm tra dữ liệu: vẫn có bảng thiếu hoặc rỗng.',10,1);
GO
-- MEDIDENT - CHI DUNG CHO CSDL DEMO / THU NGHIEM
-- Tao hoa don trang thai CHO THANH TOAN tu nhung phieu kham da hoan tat
-- va CHUA CO HOA DON/THANH TOAN CON HIEU LUC.
-- Khong chay tren CSDL benh nhan that khi chua duoc phe duyet nghiep vu.
-- SQL Server 2012+. Khong sua hoa don da thanh toan.
USE MediDent;
GO
SET XACT_ABORT ON;
GO
BEGIN TRY
    BEGIN TRANSACTION;
    INSERT dbo.HoaDon (MaPK, TongTien, TrangThai, TienGoc, TienGiam, GhiChu)
    SELECT p.MaPK, SUM(ct.ThanhTien), N'Chờ thanh toán', SUM(ct.ThanhTien), 0,
           N'MEDIDENT-DEMO-HOA-DON-CHO'
    FROM dbo.PhieuKham AS p
    JOIN dbo.ChiTietDieuTri AS ct ON ct.MaPK=p.MaPK
    WHERE p.TrangThai IN (N'Hoàn tất',N'Hoàn thành',N'Đã hoàn thành')
      AND NOT EXISTS (SELECT 1 FROM dbo.HoaDon h
                      WHERE h.MaPK=p.MaPK AND h.TrangThai<>N'Đã hủy')
      AND NOT EXISTS (SELECT 1 FROM dbo.ThanhToan t WHERE t.MaPK=p.MaPK
                      AND t.TrangThai NOT IN (N'Đã hủy',N'Thất bại',N'Chờ xử lý',N'Chờ thanh toán'))
    GROUP BY p.MaPK
    HAVING SUM(ct.ThanhTien)>0;

    INSERT dbo.ChiTietHoaDon (MaHD,MaDV,TenDichVu,SoLuong,DonGia)
    SELECT h.MaHD,NULL,d.TenDDT,ct.SoLuong,ct.DonGia
    FROM dbo.HoaDon h
    JOIN dbo.ChiTietDieuTri ct ON ct.MaPK=h.MaPK
    JOIN dbo.DichVuDieuTri d ON d.MaDDT=ct.MaDDT
    WHERE h.GhiChu=N'MEDIDENT-DEMO-HOA-DON-CHO'
      AND h.TrangThai=N'Chờ thanh toán'
      AND NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon c WHERE c.MaHD=h.MaHD);
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
SELECT h.MaHD,h.MaPK,h.TrangThai,h.TongTien
FROM dbo.HoaDon h
WHERE h.TrangThai IN(N'Chờ thanh toán',N'Chưa thanh toán')
ORDER BY h.MaHD DESC;
GO
