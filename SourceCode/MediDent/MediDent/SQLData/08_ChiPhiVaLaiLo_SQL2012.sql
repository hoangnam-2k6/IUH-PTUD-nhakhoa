-- Bảng chi phí hoạt động dùng cho báo cáo lãi/lỗ tạm tính.

USE MediDent;
GO

IF OBJECT_ID(N'dbo.ChiPhiHoatDong', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiPhiHoatDong (
        maCP INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        ngayChi DATE NOT NULL CONSTRAINT DF_ChiPhi_NgayChi DEFAULT (CONVERT(date, GETDATE())),
        loaiChi NVARCHAR(80) NOT NULL,
        moTa NVARCHAR(500) NOT NULL,
        soTien DECIMAL(18,2) NOT NULL,
        maTKNguoiNhap INT NOT NULL,
        ngayTao DATETIME2 NOT NULL CONSTRAINT DF_ChiPhi_NgayTao DEFAULT SYSDATETIME(),
        CONSTRAINT FK_ChiPhi_TaiKhoan FOREIGN KEY(maTKNguoiNhap)
            REFERENCES dbo.TaiKhoan(maTK),
        CONSTRAINT CK_ChiPhi_SoTien CHECK(soTien > 0),
        CONSTRAINT CK_ChiPhi_LoaiChi CHECK(loaiChi IN
            (N'Vật tư', N'Nhân sự', N'Vận hành', N'Thuê mặt bằng', N'Khác'))
    );
END;
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE object_id = OBJECT_ID(N'dbo.ChiPhiHoatDong')
      AND name = N'IX_ChiPhi_NgayChi'
)
BEGIN
    CREATE INDEX IX_ChiPhi_NgayChi
        ON dbo.ChiPhiHoatDong(ngayChi, loaiChi);
END;
GO

SELECT COUNT(*) AS SoKhoanChiPhi,
       COALESCE(SUM(soTien), 0) AS TongChiPhiDaNhap
FROM dbo.ChiPhiHoatDong;
GO
