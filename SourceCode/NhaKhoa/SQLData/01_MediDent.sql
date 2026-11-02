-- MEDIDENT - Tao database theo so do ERD nguoi dung cung cap.
-- SQL Server 2012+; tao moi bang khi chua ton tai; KHONG xoa bang hay du lieu cu.
-- ERD chi cho TEN COT / QUAN HE, khong cho kieu du lieu; cac kieu va NULL sau la gia dinh.
-- IDs duoc chon INT IDENTITY, tien/chi phi DECIMAL(18,2), thoi gian DATETIME2.
-- Quan trong: neu CSDL cua ban da ton tai, DOI CHIEU kieu du lieu truoc khi chay.
IF DB_ID(N'MediDent') IS NULL CREATE DATABASE MediDent;
GO
USE MediDent;
GO
-- Chan truong hop da chay ban admin cu: TaiKhoan cu chua cot matKhau theo ERD.
-- Neu gap loi: sao luu CSDL, migrate bang cu truoc, KHONG xoa du lieu that.
IF OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.TaiKhoan',N'matKhau') IS NULL
BEGIN
    THROW 50001, N'Bang TaiKhoan cu khong co cot matKhau theo ERD. Hay sao luu/migrate DB truoc.', 1;
END
GO

-- 01. VaiTro -> TaiKhoan -> NhanVien: TaiKhoan KHONG chua HoTen / Email.
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
 matKhau VARCHAR(100) NOT NULL, -- BCrypt hash, KHONG phai mat khau goc
 trangThai NVARCHAR(20) NOT NULL DEFAULT N'Hoạt động'
   CHECK (trangThai IN (N'Hoạt động',N'Tạm khóa')),
 ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 ngayDangNhapCuoi DATETIME2 NULL,
 phaiDoiMatKhau BIT NOT NULL DEFAULT 0, -- Buộc đổi mật khẩu sau reset/cấp lần đầu
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

-- 02. Nhom benh nhan, lich hen, phieu kham.
IF OBJECT_ID(N'dbo.BenhNhan',N'U') IS NULL
CREATE TABLE dbo.BenhNhan (
 maBN INT IDENTITY(1,1) PRIMARY KEY,hoTen NVARCHAR(120) NOT NULL,ngaySinh DATE NULL,
 gioiTinh NVARCHAR(15) NULL,sdt VARCHAR(20) NULL,email VARCHAR(150) NULL,
 diaChi NVARCHAR(255) NULL,tienSuBenh NVARCHAR(MAX) NULL,trangThai NVARCHAR(30) NULL,
 -- [CH3-UC028/031][FIX-SCHEMA] Đồng bộ cột sử dụng khi ngừng/khôi phục hồ sơ trong QuanLyDAO.
 trangThaiHoSo NVARCHAR(30) NOT NULL CONSTRAINT DF_MediDent_BenhNhan_TrangThaiHoSo DEFAULT N'Hoạt động'
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
 -- [CH3-UC034/036][FIX-SCHEMA] Bắt buộc để Java kiểm tra chồng khoảng 09:00-10:00/09:30-10:30.
 thoiLuongPhut INT NOT NULL CONSTRAINT DF_MediDent_LichHen_ThoiLuong DEFAULT (60),
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

-- 03. Dich vu va chi tiet dieu tri.
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

-- 04. Thanh toan, hoa don va phe duyet huy.
IF OBJECT_ID(N'dbo.ThanhToan',N'U') IS NULL
CREATE TABLE dbo.ThanhToan (maTT INT IDENTITY(1,1) PRIMARY KEY,
 maPK INT NOT NULL REFERENCES dbo.PhieuKham(maPK),ngayThanhToan DATETIME2 NULL DEFAULT SYSDATETIME(), -- [SYNC-10] NULL khi chờ xác minh thực thu
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

-- 05. Bao cao luu snapshot theo ERD (khac voi bao cao tong hop realtime tren UI).
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

-- 06. Nhat ky duoc doi theo TEN COT ERD, luu ai tac dong o doiTuong/moTa.
IF OBJECT_ID(N'dbo.NhatKyHeThong',N'U') IS NULL
CREATE TABLE dbo.NhatKyHeThong (maNK BIGINT IDENTITY(1,1) PRIMARY KEY,
 thoiGian DATETIME2 NOT NULL DEFAULT SYSDATETIME(),hanhDong NVARCHAR(200) NOT NULL,
 doiTuong NVARCHAR(150) NULL,moTa NVARCHAR(1000) NULL);
GO

-- 07. BANG BO SUNG: ERD KHONG CO chi tiet quyen / cau hinh, UI Admin can chung.
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
-- Tao tai khoan quan tri lan dau bang ConnectDB.KhoiTaoAdmin (mat khau BCrypt).
-- Khong seed tai khoan hoac du lieu benh nhan gia.


-- RANG BUOC EMAIL: kiem tra cu phap co ban, KHONG han che nha cung cap.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_NhanVien_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
    -- Co du lieu cu khong hop le: ghi canh bao, khong tu sua/xoa ho so.
    IF EXISTS(SELECT 1 FROM dbo.NhanVien WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'
    ))
    BEGIN
        PRINT N'Canh bao: dbo.NhanVien co email cu sai cu phap. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.NhanVien WITH NOCHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_NhanVien_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'));
    END
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_BenhNhan_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    -- Co du lieu cu khong hop le: ghi canh bao, khong tu sua/xoa ho so.
    IF EXISTS(SELECT 1 FROM dbo.BenhNhan WHERE email IS NOT NULL AND NOT (
        email=LTRIM(RTRIM(email))
        AND email NOT LIKE '% %' AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
        AND CHARINDEX('@',email)>1
        AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'
    ))
    BEGIN
        PRINT N'Canh bao: dbo.BenhNhan co email cu sai cu phap. CHECK moi duoc them WITH NOCHECK, can lam sach du lieu de tin cay.';
        ALTER TABLE dbo.BenhNhan WITH NOCHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'));
    END
    ELSE
    BEGIN
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_BenhNhan_Email_TenMien CHECK (
            email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
            AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%'
            AND CHARINDEX('@',email)>1
            AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'));
    END
END;
GO

-- [QL-SCHEMA] Đồng bộ luôn cấu trúc chi phí cho CSDL MỚI, tránh báo cáo lỗi thiếu bảng.
IF OBJECT_ID(N'dbo.ChiPhiHoatDong', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiPhiHoatDong (
        maCP INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        ngayChi DATE NOT NULL CONSTRAINT DF_ChiPhi_NgayChi DEFAULT (CONVERT(date,GETDATE())),
        loaiChi NVARCHAR(80) NOT NULL,
        moTa NVARCHAR(500) NOT NULL,
        soTien DECIMAL(18,2) NOT NULL,
        maTKNguoiNhap INT NOT NULL,
        ngayTao DATETIME2 NOT NULL CONSTRAINT DF_ChiPhi_NgayTao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_ChiPhi_TaiKhoan FOREIGN KEY(maTKNguoiNhap) REFERENCES dbo.TaiKhoan(maTK),
        CONSTRAINT CK_ChiPhi_SoTien CHECK (soTien > 0),
        CONSTRAINT CK_ChiPhi_LoaiChi CHECK
            (loaiChi IN (N'Vật tư',N'Nhân sự',N'Vận hành',N'Thuê mặt bằng',N'Khác'))
    );
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ChiPhiHoatDong')
               AND name=N'IX_ChiPhi_NgayChi')
    CREATE INDEX IX_ChiPhi_NgayChi ON dbo.ChiPhiHoatDong(ngayChi,loaiChi);
GO


-- =============================================================
-- [QL-UNIFIED] DONG BO CHUONG 3 + 4 / SQL SERVER 2012+
-- CUNG MOT FILE 01: CHAY DUOC CHO DB MOI VA DB DA CO DU LIEU.
-- Sao luu DB truoc khi chay. Khong DROP TABLE, khong xoa lich/hoa don.
-- TACH GO de SQL Server khong kiem tra cot moi truoc khi ALTER.
-- =============================================================
USE MediDent;
GO
-- [UC028/031, UC034] Nang cap cot cho DB cu; khong ghi de trang thai/lich su.
IF COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL
    ALTER TABLE dbo.BenhNhan ADD trangThaiHoSo NVARCHAR(30) NOT NULL
      CONSTRAINT DF_MediDent_BenhNhan_TrangThaiHoSo DEFAULT N'Hoạt động' WITH VALUES;
IF COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL
    ALTER TABLE dbo.LichHen ADD thoiLuongPhut INT NOT NULL
      CONSTRAINT DF_MediDent_LichHen_ThoiLuong DEFAULT (60) WITH VALUES;
IF COL_LENGTH(N'dbo.LichHen',N'soThuTuKham') IS NULL
    ALTER TABLE dbo.LichHen ADD soThuTuKham INT NULL;
GO
-- [UC028/031/034/038] Chi them CHECK khi du lieu cu hop le, tranh rollback ca migration.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.BenhNhan') AND name=N'CK_MediDent_BenhNhan_TrangThaiHoSo')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.BenhNhan WHERE trangThaiHoSo IS NULL OR trangThaiHoSo NOT IN(N'Hoạt động',N'Ngừng hồ sơ'))
    PRINT N'CANH BAO: trangThaiHoSo cu sai; chua ap CHECK, can xu ly thu cong.';
 ELSE ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_MediDent_BenhNhan_TrangThaiHoSo CHECK(trangThaiHoSo IN(N'Hoạt động',N'Ngừng hồ sơ'));
END;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.LichHen') AND name=N'CK_MediDent_LichHen_ThoiLuong')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.LichHen WHERE thoiLuongPhut IS NULL OR thoiLuongPhut NOT BETWEEN 15 AND 240)
    PRINT N'CANH BAO: thoiLuongPhut cu sai; chua ap CHECK.';
 ELSE ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_MediDent_LichHen_ThoiLuong CHECK(thoiLuongPhut BETWEEN 15 AND 240);
END;
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.LichHen') AND name=N'CK_LichHen_SoThuTuKham')
    ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT CK_LichHen_SoThuTuKham CHECK(soThuTuKham IS NULL OR soThuTuKham>0);
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.LichHen') AND name=N'UX_LichHen_Ngay_STT')
BEGIN
 IF EXISTS(SELECT ngayHen,soThuTuKham FROM dbo.LichHen WHERE soThuTuKham IS NOT NULL GROUP BY ngayHen,soThuTuKham HAVING COUNT(*)>1)
    PRINT N'CANH BAO: STT tiep nhan cu bi trung; chua the tao UNIQUE.';
 ELSE CREATE UNIQUE INDEX UX_LichHen_Ngay_STT ON dbo.LichHen(ngayHen,soThuTuKham) WHERE soThuTuKham IS NOT NULL;
END;
GO
-- [UC045/046/047] Ho tro 3 vai tro - dung chung schema, khong buoc chay SQL04/07.
MERGE dbo.Quyen AS t USING (VALUES
 ('NHANVIEN',N'Quản lý nhân viên'),('DICHVU',N'Quản lý dịch vụ'),('HOTRO',N'Xử lý yêu cầu hỗ trợ')
) s(maQuyen,tenQuyen) ON t.maQuyen=s.maQuyen
WHEN NOT MATCHED THEN INSERT(maQuyen,tenQuyen) VALUES(s.maQuyen,s.tenQuyen);
IF OBJECT_ID(N'dbo.YeuCauHoTro',N'U') IS NULL
    CREATE TABLE dbo.YeuCauHoTro (
      maYC INT IDENTITY(1,1) PRIMARY KEY,
      maTK INT NOT NULL REFERENCES dbo.TaiKhoan(maTK),
      tieuDe NVARCHAR(160) NOT NULL, noiDung NVARCHAR(1500) NOT NULL,
      ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
      trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',
      phanHoi NVARCHAR(1500) NULL, ngayXuLy DATETIME2 NULL
    );
IF COL_LENGTH(N'dbo.TaiKhoan',N'phaiDoiMatKhau') IS NULL
    ALTER TABLE dbo.TaiKhoan ADD phaiDoiMatKhau BIT NOT NULL
       CONSTRAINT DF_TaiKhoan_PhaiDoiMatKhau DEFAULT (0) WITH VALUES;
IF COL_LENGTH(N'dbo.VaiTro',N'capBacHoTro') IS NULL
    ALTER TABLE dbo.VaiTro ADD capBacHoTro INT NOT NULL
       CONSTRAINT DF_VaiTro_CapBacHoTro DEFAULT (0) WITH VALUES;
IF COL_LENGTH(N'dbo.YeuCauHuyPhieuThu',N'maTKNguoiGui') IS NULL
    ALTER TABLE dbo.YeuCauHuyPhieuThu ADD maTKNguoiGui INT NULL;
GO
-- [UC027/045/046] Batch sau ALTER: khong bao invalid column.
UPDATE dbo.VaiTro SET capBacHoTro=CASE
  WHEN tenVaiTro=N'Quản trị viên' THEN 3
  WHEN tenVaiTro=N'Quản lý phòng khám' THEN 2
  WHEN tenVaiTro=N'Lễ tân' THEN 1 ELSE capBacHoTro END
WHERE tenVaiTro IN(N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân');
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE name=N'FK_YeuCauHuyPhieuThu_NguoiGui' AND parent_object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
    ALTER TABLE dbo.YeuCauHuyPhieuThu WITH CHECK ADD CONSTRAINT FK_YeuCauHuyPhieuThu_NguoiGui
      FOREIGN KEY(maTKNguoiGui) REFERENCES dbo.TaiKhoan(maTK);
INSERT dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro,q.maQuyen FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE ((v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN('NHANVIEN','DICHVU'))
    OR (v.tenVaiTro=N'Quản trị viên' AND q.maQuyen IN('TAIKHOAN','HETHONG','BAOCAO','HOTRO')))
AND NOT EXISTS(SELECT 1 FROM dbo.VaiTroQuyen z WHERE z.maVaiTro=v.maVaiTro AND z.maQuyen=q.maQuyen);
-- [NFR02] Giữ phân quyền Admin đúng phạm vi 3 vai trò trong báo cáo.
DELETE z FROM dbo.VaiTroQuyen z JOIN dbo.VaiTro v ON z.maVaiTro=v.maVaiTro
WHERE v.tenVaiTro=N'Quản trị viên' AND z.maQuyen IN('BENHNHAN','LICHHEN','THANHTOAN','NHANVIEN','DICHVU');
GO
-- [UC039/UC034] Phong co that tu SQL, truoc day rollback vi email sai.
-- Tach module phong de email cu KHONG chan tao phong.
IF OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL
    CREATE TABLE dbo.PhongKham(
       maPhong INT IDENTITY(1,1) PRIMARY KEY,
       tenPhong NVARCHAR(80) NOT NULL,
       sucChua INT NOT NULL CONSTRAINT DF_MediDent_Phong_SucChua DEFAULT(1),
       trangThai NVARCHAR(30) NOT NULL CONSTRAINT DF_MediDent_Phong_TrangThai DEFAULT N'Hoạt động',
       CONSTRAINT UQ_MediDent_Phong_Ten UNIQUE(tenPhong),
       CONSTRAINT CK_MediDent_Phong_SucChua CHECK(sucChua=1),
       CONSTRAINT CK_MediDent_Phong_TrangThai CHECK(trangThai IN(N'Hoạt động',N'Ngừng hoạt động'))
    );
GO
-- [QL-03] Bao ton cac ten phong cua lich cu (khong sua lich).
INSERT dbo.PhongKham(tenPhong)
SELECT DISTINCT LTRIM(RTRIM(l.phongKham)) FROM dbo.LichHen l
WHERE NULLIF(LTRIM(RTRIM(l.phongKham)),N'') IS NOT NULL
  AND NOT EXISTS(SELECT 1 FROM dbo.PhongKham p WHERE p.tenPhong=LTRIM(RTRIM(l.phongKham)));
INSERT dbo.PhongKham(tenPhong)
SELECT x.tenPhong FROM (VALUES(N'Phòng 01'),(N'Phòng 02'),(N'Phòng 03'),(N'Phòng 04'),
  (N'Phòng 05'),(N'Phòng 06'),(N'Phòng 07'),(N'Phòng 08')) x(tenPhong)
WHERE NOT EXISTS(SELECT 1 FROM dbo.PhongKham p WHERE p.tenPhong=x.tenPhong);
GO
-- [NFR03] CHECK email cu: KHONG cho rollback tao phong khi legacy email sai.
-- Gan CHECK moi chi khi data cu hop le. Neu khong, bao canh bao, DAO van kiem tra.
IF EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_NhanVien_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
    ALTER TABLE dbo.NhanVien DROP CONSTRAINT CK_NhanVien_Email_TenMien;
IF EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_BenhNhan_Email_TenMien' AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
    ALTER TABLE dbo.BenhNhan DROP CONSTRAINT CK_BenhNhan_Email_TenMien;
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_NhanVien_Email_CuPhap' AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.NhanVien WHERE email IS NOT NULL AND NOT
  (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %' AND email NOT LIKE '%..%'
   AND email NOT LIKE '%@%@%' AND CHARINDEX('@',email)>1 AND email LIKE '_%@_%._%'
   AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'))
  PRINT N'CANH BAO: NhanVien.email cu sai; chua ap CHECK cho den khi xu ly du lieu cu.';
 ELSE ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_MediDent_NhanVien_Email_CuPhap CHECK
  (email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
   AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%' AND CHARINDEX('@',email)>1
   AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%'
   AND RIGHT(email,1)<>'.'));
END;
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_BenhNhan_Email_CuPhap' AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.BenhNhan WHERE email IS NOT NULL AND NOT
  (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %' AND email NOT LIKE '%..%'
   AND email NOT LIKE '%@%@%' AND CHARINDEX('@',email)>1 AND email LIKE '_%@_%._%'
   AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%' AND RIGHT(email,1)<>'.'))
  PRINT N'CANH BAO: BenhNhan.email cu sai; chua ap CHECK cho den khi xu ly du lieu cu.';
 ELSE ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_MediDent_BenhNhan_Email_CuPhap CHECK
  (email IS NULL OR (email=LTRIM(RTRIM(email)) AND email NOT LIKE '% %'
   AND email NOT LIKE '%..%' AND email NOT LIKE '%@%@%' AND CHARINDEX('@',email)>1
   AND email LIKE '_%@_%._%' AND email NOT LIKE '%.@%' AND email NOT LIKE '%@.%'
   AND RIGHT(email,1)<>'.'));
END;
GO
-- [SYNC-10] ngayThanhToan NULL khi chua nhan tien. Sua loi QUOTENAME:
-- tao chuoi vao bien, sau do EXEC sp_executesql @sql (khong goi EXEC(concat+QUOTENAME())).
IF EXISTS(SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.ThanhToan')
 AND name=N'ngayThanhToan' AND is_nullable=0)
BEGIN
 DECLARE @df SYSNAME, @ddl NVARCHAR(MAX);
 SELECT @df=dc.name FROM sys.default_constraints dc JOIN sys.columns sc
   ON dc.parent_object_id=sc.object_id AND dc.parent_column_id=sc.column_id
   WHERE sc.object_id=OBJECT_ID(N'dbo.ThanhToan') AND sc.name=N'ngayThanhToan';
 IF @df IS NOT NULL
 BEGIN
   SET @ddl=N'ALTER TABLE dbo.ThanhToan DROP CONSTRAINT '+QUOTENAME(@df);
   EXEC sys.sp_executesql @ddl;
 END;
 ALTER TABLE dbo.ThanhToan ALTER COLUMN ngayThanhToan DATETIME2 NULL;
END;
GO
-- [NFR03] FK/index khong ap khi legacy du lieu sai; KHONG sua/xoa giao dich cu.
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ThanhToan') AND name=N'UX_MediDent_ThanhToan_GiaoDich')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.ThanhToan WHERE maGiaoDich IS NOT NULL GROUP BY maGiaoDich HAVING COUNT(*)>1)
  PRINT N'CANH BAO: Ma giao dich cu trung; chua the tao unique.';
 ELSE CREATE UNIQUE INDEX UX_MediDent_ThanhToan_GiaoDich ON dbo.ThanhToan(maGiaoDich) WHERE maGiaoDich IS NOT NULL;
END;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.HoaDon') AND name=N'UX_MediDent_HoaDon_MaTT')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.HoaDon GROUP BY maTT HAVING COUNT(*)>1)
  PRINT N'CANH BAO: HoaDon trung maTT cu; chua the tao unique.';
 ELSE CREATE UNIQUE INDEX UX_MediDent_HoaDon_MaTT ON dbo.HoaDon(maTT);
END;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.ThanhToan') AND name=N'UX_MediDent_ThanhToan_Pair')
 CREATE UNIQUE INDEX UX_MediDent_ThanhToan_Pair ON dbo.ThanhToan(maTT,maPK);
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.HoaDon') AND name=N'FK_MediDent_HoaDon_ThanhToan_PhieuKham')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.HoaDon h JOIN dbo.ThanhToan t ON h.maTT=t.maTT WHERE h.maPK<>t.maPK)
  PRINT N'CANH BAO: HoaDon co maPK khac ThanhToan; chua them FK.';
 ELSE ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_MediDent_HoaDon_ThanhToan_PhieuKham
  FOREIGN KEY(maTT,maPK) REFERENCES dbo.ThanhToan(maTT,maPK);
END;
IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.LichHen') AND name=N'FK_MediDent_LichHen_PhongKham')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.LichHen l LEFT JOIN dbo.PhongKham p ON p.tenPhong=l.phongKham WHERE l.phongKham IS NOT NULL AND p.maPhong IS NULL)
  PRINT N'CANH BAO: Phong trong lich cu chua ton tai; chua them FK.';
 ELSE ALTER TABLE dbo.LichHen WITH CHECK ADD CONSTRAINT FK_MediDent_LichHen_PhongKham
  FOREIGN KEY(phongKham) REFERENCES dbo.PhongKham(tenPhong);
END;
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID(N'dbo.ChiTietDieuTri') AND name=N'CK_MediDent_ChiTietDieuTri_DonGia')
BEGIN
 IF EXISTS(SELECT 1 FROM dbo.ChiTietDieuTri WHERE donGia<0)
  PRINT N'CANH BAO: ChiTietDieuTri.donGia cu am; chua ap CHECK.';
 ELSE ALTER TABLE dbo.ChiTietDieuTri WITH CHECK ADD CONSTRAINT CK_MediDent_ChiTietDieuTri_DonGia CHECK(donGia>=0);
END;
GO
-- [QL-03] Phat hien lich cu chong gio; KHONG doi, KHONG xoa.
-- SQL Server 2012 su dung DATEDIFF de tinh giao nhau theo phut.
SELECT TOP 30 a.maLichHen AS LichA,b.maLichHen AS LichB,a.ngayHen,a.phongKham
FROM dbo.LichHen a JOIN dbo.LichHen b
 ON a.maLichHen<b.maLichHen AND a.ngayHen=b.ngayHen AND a.phongKham=b.phongKham
 AND a.trangThai NOT IN(N'Đã hủy',N'Hoàn tất')
 AND b.trangThai NOT IN(N'Đã hủy',N'Hoàn tất')
 AND DATEDIFF(MINUTE,CAST('00:00:00' AS time),a.gioHen)<
     DATEDIFF(MINUTE,CAST('00:00:00' AS time),b.gioHen)+b.thoiLuongPhut
 AND DATEDIFF(MINUTE,CAST('00:00:00' AS time),b.gioHen)<
     DATEDIFF(MINUTE,CAST('00:00:00' AS time),a.gioHen)+a.thoiLuongPhut;
-- [QL-VERIFY] Cac doi tuong phai hien DA CO; voi legacy khong hop le xem PRINT CANH BAO.
SELECT N'BenhNhan.trangThaiHoSo' AS DoiTuong,
 CASE WHEN COL_LENGTH(N'dbo.BenhNhan',N'trangThaiHoSo') IS NULL THEN N'THIEU' ELSE N'DA CO' END AS KetQua
UNION ALL SELECT N'LichHen.thoiLuongPhut',CASE WHEN COL_LENGTH(N'dbo.LichHen',N'thoiLuongPhut') IS NULL THEN N'THIEU' ELSE N'DA CO' END
UNION ALL SELECT N'ChiPhiHoatDong',CASE WHEN OBJECT_ID(N'dbo.ChiPhiHoatDong',N'U') IS NULL THEN N'THIEU' ELSE N'DA CO' END
UNION ALL SELECT N'PhongKham',CASE WHEN OBJECT_ID(N'dbo.PhongKham',N'U') IS NULL THEN N'THIEU' ELSE N'DA CO' END
UNION ALL SELECT N'YeuCauHoTro',CASE WHEN OBJECT_ID(N'dbo.YeuCauHoTro',N'U') IS NULL THEN N'THIEU' ELSE N'DA CO' END;
GO
