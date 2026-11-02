-- ============================================================
-- 07 - TRUNG TAM YEU CAU VA DUYET HUY HOA DON (SQL SERVER 2012)
-- CHAY SAU SQLData/04 VA 05. KHONG XOA DU LIEU CU.
-- SAO LUU DATABASE MediDent TRUOC KHI CHAY.
-- ============================================================
USE MediDent;
GO

-- 1. VaiTro.capBacHoTro la cap xem yeu cau, doc tu SQL moi lan truy van.
-- 3: Quan tri vien, 2: Quan ly phong kham, 1: Le tan.
-- 0: Vai tro chua duoc cap -> chi duoc xem yeu cau cua chinh minh.
IF NOT EXISTS (SELECT 1 FROM sys.columns
               WHERE object_id=OBJECT_ID(N'dbo.VaiTro') AND name=N'capBacHoTro')
BEGIN
    -- Cot tinh: xep cap tu ten vai tro, khong UPDATE/backfill cac dong VaiTro cu.
    -- DAO van doc SELECT v.capBacHoTro nhu mot cot INT binh thuong.
    ALTER TABLE dbo.VaiTro ADD capBacHoTro AS
        (CONVERT(INT, CASE
            WHEN tenVaiTro=N'Quản trị viên' THEN 3
            WHEN tenVaiTro IN (N'Quản lý phòng khám',N'Quản lý') THEN 2
            WHEN tenVaiTro=N'Lễ tân' THEN 1
            ELSE 0
        END));
END;
GO

-- 2. Ban dau YeuCauHuyPhieuThu khong ghi maTK nguoi gui.
-- Them FK nullable de GIU NGUYEN cac yeu cau cu.
-- Don cu chua ro nguoi gui duoc hien cho Admin, khong hien cho cap duoi.
IF COL_LENGTH(N'dbo.YeuCauHuyPhieuThu', N'maTKNguoiGui') IS NULL
BEGIN
    ALTER TABLE dbo.YeuCauHuyPhieuThu
    ADD maTKNguoiGui INT NULL;
END;
GO
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys
               WHERE name=N'FK_YeuCauHuyPhieuThu_NguoiGui'
                 AND parent_object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    IF EXISTS (SELECT 1 FROM dbo.YeuCauHuyPhieuThu y
               LEFT JOIN dbo.TaiKhoan t ON t.maTK=y.maTKNguoiGui
               WHERE y.maTKNguoiGui IS NOT NULL AND t.maTK IS NULL)
        PRINT N'CANH BAO: co maTKNguoiGui cu khong ton tai trong TaiKhoan; bo qua FK, khong sua du lieu.';
    ELSE
        ALTER TABLE dbo.YeuCauHuyPhieuThu WITH CHECK
        ADD CONSTRAINT FK_YeuCauHuyPhieuThu_NguoiGui
            FOREIGN KEY (maTKNguoiGui) REFERENCES dbo.TaiKhoan(maTK);
END;
GO

-- 3. Toi uu tim theo nguoi gui va thoi gian.
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name=N'IX_YeuCauHuy_NguoiGui'
                 AND object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    CREATE INDEX IX_YeuCauHuy_NguoiGui
    ON dbo.YeuCauHuyPhieuThu(maTKNguoiGui, ngayYeuCau);
END;
GO

-- 4. Khong cho cung 1 thanh toan co 2 don CHO DUYET.
-- Neu CSDL cu da trung, KHONG tu xoa don; in canh bao de xu ly thu cong.
IF NOT EXISTS (SELECT 1 FROM sys.indexes
               WHERE name=N'UX_YeuCauHuy_ChoDuyet_maTT'
                 AND object_id=OBJECT_ID(N'dbo.YeuCauHuyPhieuThu'))
BEGIN
    IF NOT EXISTS (
        SELECT maTT FROM dbo.YeuCauHuyPhieuThu
        WHERE trangThai=N'Chờ duyệt'
        GROUP BY maTT HAVING COUNT(*) > 1
    )
    BEGIN
        CREATE UNIQUE INDEX UX_YeuCauHuy_ChoDuyet_maTT
        ON dbo.YeuCauHuyPhieuThu(maTT)
        WHERE trangThai=N'Chờ duyệt';
    END
    ELSE PRINT N'CANH BAO: Có nhiều đơn Chờ duyệt cho cùng maTT. Hãy xử lý thủ công trước khi tạo unique index.';
END;
GO

-- 5. Bao dam Admin co quyen HOTRO (quyen phe duyet va phan hoi).
IF NOT EXISTS (SELECT 1 FROM dbo.Quyen WHERE maQuyen='HOTRO')
    INSERT INTO dbo.Quyen(maQuyen,tenQuyen)
    VALUES('HOTRO',N'Trung tâm hỗ trợ và phê duyệt');
GO
INSERT INTO dbo.VaiTroQuyen(maVaiTro,maQuyen)
SELECT v.maVaiTro, 'HOTRO'
FROM dbo.VaiTro v
WHERE v.tenVaiTro=N'Quản trị viên'
  AND NOT EXISTS (SELECT 1 FROM dbo.VaiTroQuyen vq
                  WHERE vq.maVaiTro=v.maVaiTro AND vq.maQuyen='HOTRO');
GO

-- Neu muon Quan ly duoc PHAN HOI yeu cau cua Quan ly va Le tan,
-- Admin cap quyen HOTRO tu giao dien Phan quyen. Viẹc xem don KHONG can quyen HOTRO.
-- Le tan chi co quyen xem va gui, khong duoc phe duyet huy hoa don.

-- 6. Kiem tra nhanh sau nang cap.
SELECT maVaiTro, tenVaiTro, capBacHoTro
FROM dbo.VaiTro ORDER BY capBacHoTro DESC, maVaiTro;
SELECT TOP 30 maYC, maTT, maTKNguoiGui, ngayYeuCau, trangThai
FROM dbo.YeuCauHuyPhieuThu ORDER BY maYC DESC;
GO
