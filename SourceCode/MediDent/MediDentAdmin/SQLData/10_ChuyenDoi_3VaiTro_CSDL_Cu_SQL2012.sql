-- MEDIDENT - MIGRATE CSDL DA CO TU 5+ VAI TRO VE 3 VAI TRO
-- SQL Server 2012; CHI CHAY KHI DA BACKUP DB MEDIDENT.
-- Giu nguyen NhanVien, LichHen, PhieuKham; chi bo TAI KHOAN DEMO ngoai 3 vai tro.
-- Tai khoan KHONG phai demo can chuyen doi thu cong. Neu gap loi thi toan bo giao dich rollback.
USE MediDent;
GO
SET XACT_ABORT ON;
GO
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

    -- Ngat lien ket ACCOUNT demo voi NhanVien; khong xoa NhanVien vi lich/benh an lien quan.
    UPDATE n SET maTK=NULL
    FROM dbo.NhanVien n JOIN dbo.TaiKhoan t ON n.maTK=t.maTK
    WHERE t.tenDangNhap IN ('demo_bs1','demo_bs2','demo_bs3','demo_kt1','demo_khac');

    -- Neu tai khoan demo duoc tham chieu boi YeuCauHoTro thi DELETE se bi chan (ROLLBACK), khong phat tan xoa.
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
