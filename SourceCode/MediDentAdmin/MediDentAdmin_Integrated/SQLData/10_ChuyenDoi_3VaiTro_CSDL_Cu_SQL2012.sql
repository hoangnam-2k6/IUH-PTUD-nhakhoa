-- MEDIDENT - KIEM TRA DATABASE CU CO 5+ VAI TRO
-- CHI DOC. KHONG UPDATE, DELETE, MERGE HOAC CHUYEN DOI TU DONG.
-- Neu can chuyen doi vai tro/tai khoan, lap migration rieng sau khi co phe duyet va backup.
USE MediDent;
GO

IF OBJECT_ID(N'dbo.VaiTro',N'U') IS NULL OR OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
BEGIN
    RAISERROR(N'Khong tim thay bang VaiTro/TaiKhoan; hay tao database truoc.',16,1);
    RETURN;
END;
GO

PRINT N'1. Vai tro dang ton tai (chi doc):';
SELECT v.maVaiTro,v.tenVaiTro,
       (SELECT COUNT(*) FROM dbo.TaiKhoan t WHERE t.maVaiTro=v.maVaiTro) AS SoTaiKhoan
FROM dbo.VaiTro v ORDER BY v.maVaiTro;
GO

PRINT N'2. Tai khoan thuoc vai tro ngoai 3 vai tro chuan (chi doc):';
SELECT t.maTK,t.tenDangNhap,v.tenVaiTro,t.trangThai
FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
WHERE v.tenVaiTro NOT IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')
ORDER BY t.maTK;
GO

PRINT N'3. Nhan vien lien ket tai khoan thuoc vai tro cu (chi doc):';
SELECT n.maNV,n.hoTen,n.chucVu,n.maTK,t.tenDangNhap,v.tenVaiTro
FROM dbo.NhanVien n
LEFT JOIN dbo.TaiKhoan t ON t.maTK=n.maTK
LEFT JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
WHERE v.tenVaiTro NOT IN (N'Quản trị viên',N'Quản lý phòng khám',N'Lễ tân')
   OR v.tenVaiTro IS NULL AND n.maTK IS NOT NULL
ORDER BY n.maNV;
GO

PRINT N'4. Script chi bao cao; khong thay doi bat ky ban ghi nao.';
