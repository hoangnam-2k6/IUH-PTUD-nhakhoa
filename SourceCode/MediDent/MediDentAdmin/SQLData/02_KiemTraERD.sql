-- CHI DOC: Kiem tra SQL schema va cac con so sau khi chay 01_MediDent.sql.
USE MediDent;
GO
-- 1. Danh sach cac bang trong CSDL hien tai.
SELECT t.name AS TenBang FROM sys.tables t ORDER BY t.name;
GO
-- 2. Doi chieu cac cot can co cho giao dien Admin.
SELECT 'TaiKhoan' AS Bang,'matKhau' AS Cot, COL_LENGTH('dbo.TaiKhoan','matKhau') AS DoDai
UNION ALL SELECT 'TaiKhoan','ngayDangNhapCuoi',COL_LENGTH('dbo.TaiKhoan','ngayDangNhapCuoi')
UNION ALL SELECT 'NhanVien','maTK',COL_LENGTH('dbo.NhanVien','maTK')
UNION ALL SELECT 'NhanVien','hoTen',COL_LENGTH('dbo.NhanVien','hoTen')
UNION ALL SELECT 'NhanVien','email',COL_LENGTH('dbo.NhanVien','email')
UNION ALL SELECT 'NhatKyHeThong','maNK',COL_LENGTH('dbo.NhatKyHeThong','maNK')
UNION ALL SELECT 'ThanhToan','soTien',COL_LENGTH('dbo.ThanhToan','soTien')
UNION ALL SELECT 'PhieuKham','ngayKham',COL_LENGTH('dbo.PhieuKham','ngayKham');
GO
-- 3. Cac FK tu so do ERD. Neu thieu FK, kiem tra SQL tao bang.
SELECT fk.name AS TenFK, OBJECT_NAME(fk.parent_object_id) AS BangCon,
       OBJECT_NAME(fk.referenced_object_id) AS BangCha
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) IN
  ('TaiKhoan','NhanVien','QuanTriVien','QuanLy','LeTan','LichHen','LichLamViec',
  'PhieuKham','HoaDon','ThanhToan','ChiTietDieuTri','NguoiGiamHo',
  'BangGiaDichVu','PhieuDuyetHuyPhieuThu','YeuCauHuyPhieuThu')
ORDER BY BangCon;
GO
-- 4. TaiKhoan join NhanVien, VaiTro; du lieu hiển thi o QuanLyTaiKhoan.
SELECT t.maTK,t.tenDangNhap,n.maNV,n.hoTen,n.email,
       v.tenVaiTro,t.trangThai,t.ngayDangNhapCuoi
FROM dbo.TaiKhoan t
LEFT JOIN dbo.NhanVien n ON n.maTK=t.maTK
JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
ORDER BY t.maTK DESC;
GO
-- 5. So luong that theo du lieu SQL Server.
SELECT (SELECT COUNT(*) FROM dbo.TaiKhoan) AS TongTaiKhoan,
       (SELECT COUNT(*) FROM dbo.VaiTro) AS TongVaiTro,
       (SELECT COUNT(*) FROM dbo.TaiKhoan WHERE trangThai=N'Hoạt động') AS DangHoatDong;
GO
-- 6. Tong tien thanh toan thanh cong, khong dem giao dich cho / huy.
SELECT ISNULL(SUM(soTien),0) AS TongDoanhThu
FROM dbo.ThanhToan
WHERE trangThai IN(N'Thành công',N'Đã thanh toán');
GO
