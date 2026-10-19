-- Đối chiếu cấu trúc các bảng và khóa theo ERD MediDent.

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
