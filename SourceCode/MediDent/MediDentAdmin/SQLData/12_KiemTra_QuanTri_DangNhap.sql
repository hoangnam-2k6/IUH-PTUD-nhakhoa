-- Kiểm tra đọc dữ liệu và các quyền; không sinh hay sửa dữ liệu.
USE MediDent;
GO
SELECT name AS TenBang FROM sys.tables ORDER BY name;
SELECT v.tenVaiTro, q.maQuyen FROM dbo.VaiTroQuyen vq
JOIN dbo.VaiTro v ON v.maVaiTro=vq.maVaiTro
JOIN dbo.Quyen q ON q.maQuyen=vq.maQuyen
ORDER BY v.tenVaiTro, q.maQuyen;
SELECT COUNT(*) AS SoTaiKhoan FROM dbo.TaiKhoan;
SELECT COUNT(*) AS SoNhanVien FROM dbo.NhanVien;
SELECT COUNT(*) AS SoLichHen FROM dbo.LichHen;
GO
