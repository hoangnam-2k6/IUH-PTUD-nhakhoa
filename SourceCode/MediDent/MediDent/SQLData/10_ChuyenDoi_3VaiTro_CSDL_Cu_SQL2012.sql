-- Chuyển đổi danh mục vai trò cho cơ sở dữ liệu cũ.

USE MediDent;
GO
SET XACT_ABORT ON;
GO
-- Chỉ tạo bảng VaiTro khi chưa tồn tại trong CSDL.
IF OBJECT_ID(N'dbo.VaiTro',N'U') IS NULL OR OBJECT_ID(N'dbo.TaiKhoan',N'U') IS NULL
BEGIN
    RAISERROR(N'Khong tim thay bang VaiTro/TaiKhoan; vui long chay DataAll SQL truoc.',16,1);
    RETURN;
END;
GO
BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1 FROM dbo.TaiKhoan t JOIN dbo.VaiTro v ON v.maVaiTro=t.maVaiTro
        WHERE v.tenVaiTro NOT IN (N'Quản trị viên', N'Quản lý phòng khám', N'Lễ tân')
          AND t.tenDangNhap NOT IN ('demo_bs1','demo_bs2','demo_bs3','demo_kt1','demo_khac')
    )
    BEGIN
        RAISERROR(N'Co tai khoan that dang su dung vai tro cu. Khong tu chuyen doi/tu xoa. Hay chuyen thu cong sau khi sao luu.',16,1);
    END;

    UPDATE n SET maTK=NULL
    FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK
    WHERE t.tenDangNhap IN ('demo_bs1','demo_bs2','demo_bs3','demo_kt1','demo_khac');

    DELETE FROM dbo.TaiKhoan
    WHERE tenDangNhap IN ('demo_bs1','demo_bs2','demo_bs3','demo_kt1','demo_khac');

    DELETE vq FROM dbo.VaiTroQuyen vq JOIN dbo.VaiTro v ON v.maVaiTro=vq.maVaiTro
    WHERE v.tenVaiTro NOT IN (N'Quản trị viên', N'Quản lý phòng khám', N'Lễ tân');

    DELETE FROM dbo.VaiTro
    WHERE tenVaiTro NOT IN (N'Quản trị viên', N'Quản lý phòng khám', N'Lễ tân')
      AND NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan t WHERE t.maVaiTro=dbo.VaiTro.maVaiTro);

    IF EXISTS (SELECT 1 FROM dbo.VaiTro
               WHERE tenVaiTro NOT IN (N'Quản trị viên', N'Quản lý phòng khám', N'Lễ tân'))
        RAISERROR(N'Van con vai tro khong thuoc 3 vai tro cho phep. Khong duoc xoa tu dong do dang co du lieu lien ket.',16,1);

    COMMIT TRANSACTION;
    PRINT N'Hoan tat: chi con 3 vai tro dang nhap, khong xoa ho so nhan su hay lich kham.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
    DECLARE @loi NVARCHAR(4000)=ERROR_MESSAGE();
    RAISERROR(N'Migration da rollback: %s',16,1,@loi);
END CATCH;
GO
SELECT v.maVaiTro,v.tenVaiTro,(SELECT COUNT(*) FROM dbo.TaiKhoan t WHERE t.maVaiTro=v.maVaiTro) AS SoTaiKhoan
FROM dbo.VaiTro v ORDER BY v.maVaiTro;
GO
SELECT n.maNV,n.hoTen,n.chucVu,n.maTK
FROM dbo.NhanVien n
WHERE n.chucVu=N'Bác sĩ';
GO
