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
