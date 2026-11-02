-- MEDIDENT - KIEM TRA 3 LOI LICH/THANH TOAN (CHI DOC, KHONG SUA DU LIEU)
-- SQL Server 2012+. Chay tren CSDL MediDent dang su dung.
USE MediDent;
GO
PRINT N'1. Kieu du lieu thuc te (TIME / DATETIME / DATETIME2)';
SELECT o.name AS Bang, c.name AS Cot, t.name AS KieuDuLieu
FROM sys.objects o
JOIN sys.columns c ON c.object_id=o.object_id
JOIN sys.types t ON t.user_type_id=c.user_type_id
WHERE o.type='U'
  AND o.name IN ('LichLamViec','LichHen','ThanhToan')
  AND c.name IN ('ngayLamViec','gioBatDau','gioKetThuc','ngayHen','gioHen',
                 'thoiLuongPhut','ngayThanhToan','maGiaoDich')
ORDER BY o.name,c.name;
GO
PRINT N'2. Cac ca lam viec tu hom nay; bac si phai DANG LAM VIEC va ca DA XEP LICH';
SELECT TOP 50 l.maLichLV, n.maNV, n.hoTen, n.chucVu, n.trangThai AS TrangThaiNhanVien,
       l.ngayLamViec, CAST(l.gioBatDau AS time) AS GioBatDau,
       CAST(l.gioKetThuc AS time) AS GioKetThuc, l.trangThai AS TrangThaiCa
FROM dbo.LichLamViec l
JOIN dbo.NhanVien n ON n.maNV=l.maNV
WHERE l.ngayLamViec>=CONVERT(date,GETDATE())
ORDER BY l.ngayLamViec,l.gioBatDau;
GO
PRINT N'3. Lich dang mo. Lich trong qua khu khong the doi sang ngay trong qua khu';
SELECT TOP 40 maLichHen, maBN, maNV, ngayHen, CAST(gioHen AS time) AS GioHen,
       thoiLuongPhut, phongKham, trangThai
FROM dbo.LichHen
ORDER BY ngayHen DESC,gioHen DESC;
GO
PRINT N'4. Phieu kham, tong tien chi tiet va so thanh toan da co';
SELECT TOP 50 p.maPK, p.maBN,
       (SELECT COUNT(*) FROM dbo.ChiTietDieuTri d WHERE d.maPK=p.maPK) AS SoChiTiet,
       (SELECT COALESCE(SUM(d.thanhTien),0) FROM dbo.ChiTietDieuTri d WHERE d.maPK=p.maPK) AS TongTien,
       (SELECT COUNT(*) FROM dbo.ThanhToan t WHERE t.maPK=p.maPK
         AND t.trangThai NOT IN (N'Đã hủy',N'Hoàn tiền',N'Thất bại')) AS SoThanhToanConHieuLuc
FROM dbo.PhieuKham p ORDER BY p.maPK DESC;
GO
PRINT N'5. NgayThanhToan NOT NULL: code Java moi se khong INSERT NULL khi tao cho xu ly';
SELECT c.name, t.name AS Kieu, c.is_nullable
FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id
WHERE c.object_id=OBJECT_ID('dbo.ThanhToan') AND c.name='ngayThanhToan';
GO
