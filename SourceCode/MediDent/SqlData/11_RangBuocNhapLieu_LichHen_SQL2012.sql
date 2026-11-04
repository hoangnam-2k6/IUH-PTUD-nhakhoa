-- 11: RANG BUỘC NHẬP LIỆU VÀ CHỐNG ĐẶT TRÙNG LỊCH (SQL SERVER 2012+).
-- Người thực hiện: DBA/Quản trị CSDL, chạy sau 01-10. KHÔNG xóa dữ liệu.
-- Nếu có dữ liệu cũ không hợp lệ, in cảnh báo và bỏ qua constraint; cần làm sạch thủ công.
USE MediDent;
GO

-- Phân quyền: SQL cho phép NULL vì tài khoản Admin mới có thể chưa bổ sung số điện thoại.
-- UI/DAO bắt buộc SDT khi tạo hồ sơ nhân viên hoặc bệnh nhân.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_NhanVien_SDT'
               AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.NhanVien WHERE sdt IS NOT NULL AND
       (LEN(sdt)<>10 OR sdt LIKE '%[^0-9]%' OR NOT
        (sdt LIKE '03[2-9]%' OR sdt LIKE '05[25689]%' OR sdt LIKE '07[06-9]%'
         OR sdt LIKE '08[1-9]%' OR sdt LIKE '09[0-46-9]%')))
        PRINT N'Dữ liệu cũ NhanVien.sdt không hợp lệ, chưa thêm CK. Rà soát trước khi chạy lại.';
    ELSE
        ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_MediDent_NhanVien_SDT CHECK
        (sdt IS NULL OR (LEN(sdt)=10 AND sdt NOT LIKE '%[^0-9]%' AND
        (sdt LIKE '03[2-9]%' OR sdt LIKE '05[25689]%' OR sdt LIKE '07[06-9]%'
         OR sdt LIKE '08[1-9]%' OR sdt LIKE '09[0-46-9]%')));
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_BenhNhan_SDT'
               AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.BenhNhan WHERE sdt IS NOT NULL AND
       (LEN(sdt)<>10 OR sdt LIKE '%[^0-9]%' OR NOT
        (sdt LIKE '03[2-9]%' OR sdt LIKE '05[25689]%' OR sdt LIKE '07[06-9]%'
         OR sdt LIKE '08[1-9]%' OR sdt LIKE '09[0-46-9]%')))
        PRINT N'Dữ liệu cũ BenhNhan.sdt không hợp lệ, chưa thêm CK. Rà soát trước khi chạy lại.';
    ELSE
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_MediDent_BenhNhan_SDT CHECK
        (sdt IS NULL OR (LEN(sdt)=10 AND sdt NOT LIKE '%[^0-9]%' AND
        (sdt LIKE '03[2-9]%' OR sdt LIKE '05[25689]%' OR sdt LIKE '07[06-9]%'
         OR sdt LIKE '08[1-9]%' OR sdt LIKE '09[0-46-9]%')));
END;
GO

-- Bảo vệ ngày sinh ở CSDL, giảm nguy cơ bỏ qua kiểm tra từ GUI.
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_BenhNhan_NgaySinh'
               AND parent_object_id=OBJECT_ID(N'dbo.BenhNhan'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.BenhNhan WHERE ngaySinh > CONVERT(date,GETDATE())
                OR ngaySinh < DATEADD(year,-100,CONVERT(date,GETDATE())))
        PRINT N'Dữ liệu ngày sinh bệnh nhân cũ không hợp lệ; chưa thể áp dụng CK.';
    ELSE
        ALTER TABLE dbo.BenhNhan WITH CHECK ADD CONSTRAINT CK_MediDent_BenhNhan_NgaySinh CHECK
        (ngaySinh IS NULL OR (ngaySinh<=CONVERT(date,GETDATE()) AND
        ngaySinh>=DATEADD(year,-100,CONVERT(date,GETDATE()))));
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name=N'CK_MediDent_NhanVien_NgaySinh'
               AND parent_object_id=OBJECT_ID(N'dbo.NhanVien'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.NhanVien WHERE ngaySinh > CONVERT(date,GETDATE())
                OR ngaySinh < DATEADD(year,-100,CONVERT(date,GETDATE())))
        PRINT N'Dữ liệu ngày sinh nhân viên cũ không hợp lệ; chưa thể áp dụng CK.';
    ELSE
        ALTER TABLE dbo.NhanVien WITH CHECK ADD CONSTRAINT CK_MediDent_NhanVien_NgaySinh CHECK
        (ngaySinh IS NULL OR (ngaySinh<=CONVERT(date,GETDATE()) AND
        ngaySinh>=DATEADD(year,-100,CONVERT(date,GETDATE()))));
END;
GO

-- Chống hai máy tạo cùng giờ cho cùng nhân viên; chỉ áp dụng các lịch chưa kết thúc.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name=N'UX_MediDent_LichHen_NhanVien_Gio'
               AND object_id=OBJECT_ID(N'dbo.LichHen'))
BEGIN
    IF EXISTS (
        SELECT maNV,ngayHen,gioHen FROM dbo.LichHen
        WHERE maNV IS NOT NULL AND trangThai NOT IN (N'Đã hủy',N'Hoàn tất')
        GROUP BY maNV,ngayHen,gioHen HAVING COUNT(*)>1
    )
        PRINT N'Đã có lịch trùng bác sĩ/giờ; cần xử lý trước khi tạo unique index.';
    ELSE
        CREATE UNIQUE INDEX UX_MediDent_LichHen_NhanVien_Gio
        ON dbo.LichHen(maNV,ngayHen,gioHen)
        WHERE maNV IS NOT NULL AND trangThai <> N'Đã hủy' AND trangThai <> N'Hoàn tất';
END;
GO

-- Chỉ SELECT kiểm tra, không thay đổi dữ liệu hay mật khẩu.
SELECT name AS RangBuoc FROM sys.check_constraints
WHERE name LIKE N'CK_MediDent_%' ORDER BY name;
SELECT name AS ChiMuc FROM sys.indexes
WHERE name=N'UX_MediDent_LichHen_NhanVien_Gio';
GO
