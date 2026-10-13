-- MediDent - NANG CAP GIỚI HẠN ADMIN VÀ BUỘC ĐỔI MẬT KHẨU
-- Dành cho SQL Server 2012. KHÔNG xóa tài khoản, bệnh nhân, hóa đơn.
USE MediDent;
GO
IF COL_LENGTH(N'dbo.TaiKhoan', N'phaiDoiMatKhau') IS NULL
BEGIN
    ALTER TABLE dbo.TaiKhoan
    ADD phaiDoiMatKhau BIT NOT NULL
        CONSTRAINT DF_TaiKhoan_PhaiDoiMatKhau DEFAULT (0);
END;
GO
-- Giới hạn role Admin chỉ các nhóm: Tài khoản, Báo cáo hệ thống, Hệ thống, Hỗ trợ.
-- Thao tác nghiệp vụ bị từ chối thêm ở DAO dù SQL cũ còn cấp quyền.
DELETE vq
FROM dbo.VaiTroQuyen vq
JOIN dbo.VaiTro vt ON vt.maVaiTro=vq.maVaiTro
WHERE vt.tenVaiTro=N'Quản trị viên'
  AND vq.maQuyen IN ('BENHNHAN','LICHHEN','THANHTOAN','NHANVIEN','DICHVU');
GO
-- Các quyền Admin cần dùng nếu chưa có.
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
