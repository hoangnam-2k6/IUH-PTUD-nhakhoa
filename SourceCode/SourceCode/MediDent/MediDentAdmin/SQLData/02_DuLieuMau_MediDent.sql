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
