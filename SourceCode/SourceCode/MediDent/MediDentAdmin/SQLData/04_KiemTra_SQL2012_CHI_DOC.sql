-- MEDIDENT 04 - KIỂM TRA CẤU TRÚC VÀ DỮ LIỆU (CHỈ ĐỌC, KHÔNG THAY ĐỔI)
-- Chạy sau script tổng hợp 00 hoặc lần lượt 01,02,03. SQL Server 2012+.
USE MediDent;
GO
PRINT N'1. Các bảng ứng dụng đã tạo';
SELECT name AS TenBang FROM sys.tables
WHERE name IN ('VaiTro','TaiKhoan','NhanVien','QuanTriVien','QuanLy','LeTan',
 'Quyen','VaiTroQuyen','LichSuDangNhap','NhatKyHeThong','BenhNhan','BacSi',
 'PhongKham','DichVu','DichVuDieuTri','ChiTietDieuTri','LichHen','PhieuKham',
 'KetQuaKham','ThanhToan','HoaDon','ChiTietHoaDon','KhuyenMai')
ORDER BY name;
GO
PRINT N'2. Số bản ghi mỗi bảng và tài khoản full có tồn tại';
SELECT
 (SELECT COUNT(*) FROM dbo.BenhNhan) AS BenhNhan,
 (SELECT COUNT(*) FROM dbo.BacSi) AS BacSi,
 (SELECT COUNT(*) FROM dbo.PhongKham) AS PhongKham,
 (SELECT COUNT(*) FROM dbo.DichVu) AS DichVu,
 (SELECT COUNT(*) FROM dbo.LichHen) AS LichHen,
 (SELECT COUNT(*) FROM dbo.PhieuKham) AS PhieuKham,
 (SELECT COUNT(*) FROM dbo.ChiTietDieuTri) AS ChiTietDieuTri,
 (SELECT COUNT(*) FROM dbo.HoaDon) AS HoaDon,
 (SELECT COUNT(*) FROM dbo.ThanhToan) AS ThanhToan,
 (SELECT COUNT(*) FROM dbo.KhuyenMai) AS KhuyenMai,
 (SELECT COUNT(*) FROM dbo.TaiKhoan WHERE tenDangNhap='full') AS FullDemo;
GO
PRINT N'3. Quyền QL_KHUYENMAI cấp cho vai trò Quản lý';
SELECT v.tenVaiTro,q.maQuyen,q.tenQuyen
FROM dbo.VaiTro v JOIN dbo.VaiTroQuyen p ON p.maVaiTro=v.maVaiTro
JOIN dbo.Quyen q ON q.maQuyen=p.maQuyen
WHERE q.maQuyen IN('QL_KHUYENMAI','QL_THANHTOAN','LT_THANHTOAN')
ORDER BY v.tenVaiTro,q.maQuyen;
GO
PRINT N'4. Danh sách tài khoản quản trị (không bao gồm full)';
SELECT t.maTK,t.tenDangNhap,n.hoTen,v.tenVaiTro,t.trangThai
FROM dbo.TaiKhoan t JOIN dbo.NhanVien n ON n.maTK=t.maTK
JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
WHERE t.tenDangNhap<>'full' ORDER BY t.maTK;
GO
PRINT N'5. Phiếu khám đủ điều kiện thu tiền, chưa có hóa đơn';
SELECT p.MaPK,b.HoTen,p.TrangThai,SUM(c.ThanhTien) AS TongTienGoc
FROM dbo.PhieuKham p JOIN dbo.BenhNhan b ON b.MaBN=p.MaBN
JOIN dbo.ChiTietDieuTri c ON c.MaPK=p.MaPK
WHERE p.TrangThai IN(N'Hoàn tất',N'Hoàn thành',N'Đã hoàn thành')
AND NOT EXISTS(SELECT 1 FROM dbo.ThanhToan t WHERE t.MaPK=p.MaPK
               AND t.TrangThai NOT IN(N'Đã hủy',N'Thất bại'))
AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.MaPK=p.MaPK
               AND h.TrangThai<>N'Đã hủy')
GROUP BY p.MaPK,b.HoTen,p.TrangThai ORDER BY p.MaPK;
GO
PRINT N'6. Khuyến mãi còn hiệu lực trong ngày';
SELECT MaKM,TenKM,LoaiGiam,GiaTri,NgayBatDau,NgayKetThuc
FROM dbo.KhuyenMai
WHERE TrangThai=1 AND CONVERT(date,GETDATE()) BETWEEN NgayBatDau AND NgayKetThuc
ORDER BY MaKM;
GO
PRINT N'7. Lịch sử hóa đơn và chi tiết giảm giá snapshot';
SELECT TOP 30 h.MaHD,h.MaPK,h.TrangThai,h.TienGoc,h.TienGiam,h.TongTien,h.MaKM,
       t.PhuongThuc,t.NgayThanhToan
FROM dbo.HoaDon h LEFT JOIN dbo.ThanhToan t ON t.MaTT=h.MaTT
ORDER BY h.MaHD DESC;
GO
