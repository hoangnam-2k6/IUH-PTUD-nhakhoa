-- 04: BỔ SUNG quyền và chức năng hỗ trợ (SQL Server 2012).
-- Chạy SAU 01_MediDent.sql trên DB MediDent hiện có; không xóa bảng.
USE MediDent;
GO
-- Quyền chuyên biệt để Admin có thể bật/tắt trong màn hình Phân quyền.
MERGE dbo.Quyen AS t USING (VALUES
    ('NHANVIEN', N'Quản lý nhân viên'),
    ('DICHVU', N'Quản lý dịch vụ'),
    ('HOTRO', N'Xử lý yêu cầu hỗ trợ')
) s(maQuyen, tenQuyen)
ON t.maQuyen = s.maQuyen
WHEN NOT MATCHED THEN INSERT (maQuyen, tenQuyen) VALUES(s.maQuyen, s.tenQuyen);
GO
-- Bảng yêu cầu hỗ trợ sử dụng chung cho toàn bộ vai trò.
IF OBJECT_ID('dbo.YeuCauHoTro', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.YeuCauHoTro (
        maYC INT IDENTITY(1,1) PRIMARY KEY,
        maTK INT NOT NULL REFERENCES dbo.TaiKhoan(maTK),
        tieuDe NVARCHAR(160) NOT NULL,
        noiDung NVARCHAR(1500) NOT NULL,
        ngayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        trangThai NVARCHAR(30) NOT NULL DEFAULT N'Chờ xử lý',
        phanHoi NVARCHAR(1500) NULL,
        ngayXuLy DATETIME2 NULL
    );
END;
GO
-- Cấp các quyền mới theo vai trò nếu chưa được cấu hình.
-- Cẩn thận thứ tự ưu tiên AND/OR: bổ sung những quyền còn thiếu cho cả Admin.
INSERT INTO dbo.VaiTroQuyen(maVaiTro, maQuyen)
SELECT v.maVaiTro, q.maQuyen
FROM dbo.VaiTro v CROSS JOIN dbo.Quyen q
WHERE ((v.tenVaiTro=N'Quản trị viên' AND q.maQuyen IN ('NHANVIEN','DICHVU','HOTRO'))
 OR (v.tenVaiTro=N'Quản lý phòng khám' AND q.maQuyen IN ('NHANVIEN','DICHVU')))
 AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen x WHERE x.maVaiTro=v.maVaiTro AND x.maQuyen=q.maQuyen);
GO
-- Kiểm tra kết quả sau nâng cấp:
SELECT v.tenVaiTro,q.maQuyen FROM dbo.VaiTroQuyen p
JOIN dbo.VaiTro v ON v.maVaiTro=p.maVaiTro
JOIN dbo.Quyen q ON q.maQuyen=p.maQuyen
ORDER BY v.tenVaiTro, q.maQuyen;
GO
