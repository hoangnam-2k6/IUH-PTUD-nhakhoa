/* ============================================================
   SQL Server - Du lieu mau cho 3 bang:
       dbo.NhanVien, dbo.BenhNhan, dbo.DichVu

   Phu thuoc vao schema trong file:
       TaoBang_NhanVien_BenhNhan_DichVu.sql

   Luu y ve ENUM Java:
   - DichVu.trangthai dung 2 gia tri da khai bao trong CHECK:
       dangapdung, ngungapdung
   - Cac gia tri mau gioitinh / trangthai cua NhanVien va
     BenhNhan ben duoi la ten enum du kien. Neu enum trong Java
     cua ban dung ten khac, hay sua cac chuoi nay cho KHOP CHINH XAC
     truoc khi chay, vi Enum.valueOf() phan biet hoa/thuong.
   - Gia tri mau dang su dung:
       gioitinh: nam, nu
       NhanVien.trangthai: danglamviec
       BenhNhan.trangthai: dangdieutri

   Script co the chay lai: chi them ban ghi neu ma chua ton tai.
   ============================================================ */

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* --------------------------------------------------------
       1. DU LIEU MAU NHANVIEN
       maTK de NULL vi chua co schema bang TaiKhoan de doi chieu.
       -------------------------------------------------------- */
    INSERT INTO dbo.NhanVien
        (maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    SELECT s.maNV, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.chucvu, s.trangthai, s.maTK
    FROM (VALUES
        (N'NV001', N'Nguyen Van An',   CONVERT(date, '1990-03-15', 23), N'nam', N'0901000001', N'an.nv@example.com',    N'Quan 1, TP. Ho Chi Minh', N'Bac si',       N'danglamviec', CAST(NULL AS nvarchar(20))),
        (N'NV002', N'Tran Thi Binh',   CONVERT(date, '1994-07-22', 23), N'nu',  N'0901000002', N'binh.nv@example.com',  N'Thu Duc, TP. Ho Chi Minh', N'Le tan',       N'danglamviec', CAST(NULL AS nvarchar(20))),
        (N'NV003', N'Le Quoc Cuong',   CONVERT(date, '1988-11-09', 23), N'nam', N'0901000003', N'cuong.nv@example.com', N'Go Vap, TP. Ho Chi Minh', N'Bac si',       N'danglamviec', CAST(NULL AS nvarchar(20))),
        (N'NV004', N'Pham Ngoc Dung',  CONVERT(date, '1996-01-30', 23), N'nu',  N'0901000004', N'dung.nv@example.com',  N'Binh Thanh, TP. Ho Chi Minh', N'Tro ly nha khoa', N'danglamviec', CAST(NULL AS nvarchar(20))),
        (N'NV005', N'Vo Minh Khang',   CONVERT(date, '1992-05-18', 23), N'nam', N'0901000005', N'khang.nv@example.com', N'Quan 7, TP. Ho Chi Minh', N'Ky thuat vien', N'danglamviec', CAST(NULL AS nvarchar(20)))
    ) AS s(maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.NhanVien AS t WHERE t.maNV = s.maNV
    );

    /* --------------------------------------------------------
       2. DU LIEU MAU BENHNHAN
       -------------------------------------------------------- */
    INSERT INTO dbo.BenhNhan
        (maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    SELECT s.maBN, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.tiensubenh, s.trangthai
    FROM (VALUES
        (N'BN001', N'Nguyen Thi Mai',  CONVERT(date, '2001-02-12', 23), N'nu',  N'0912000001', N'mai.bn@example.com',  N'Thu Duc, TP. Ho Chi Minh', N'Chua ghi nhan', N'dangdieutri'),
        (N'BN002', N'Tran Van Nam',    CONVERT(date, '1985-09-25', 23), N'nam', N'0912000002', N'nam.bn@example.com',  N'Quan 3, TP. Ho Chi Minh', N'Tien su viem loi', N'dangdieutri'),
        (N'BN003', N'Le Hoang Yen',    CONVERT(date, '1998-04-07', 23), N'nu',  N'0912000003', N'yen.bn@example.com',  N'Go Vap, TP. Ho Chi Minh', N'Di ung thuoc (can xac nhan)', N'dangdieutri'),
        (N'BN004', N'Pham Gia Bao',    CONVERT(date, '2010-12-19', 23), N'nam', N'0912000004', N'bao.bn@example.com',  N'Binh Thanh, TP. Ho Chi Minh', N'Chua ghi nhan', N'dangdieutri'),
        (N'BN005', N'Vo Thanh Truc',   CONVERT(date, '1979-06-03', 23), N'nu',  N'0912000005', N'truc.bn@example.com', N'Quan 7, TP. Ho Chi Minh', N'Tien su cao huyet ap', N'dangdieutri')
    ) AS s(maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.BenhNhan AS t WHERE t.maBN = s.maBN
    );

    /* --------------------------------------------------------
       3. DU LIEU MAU DICHVU
       Hai gia tri trangthai phai dung theo CHECK cua bang DichVu.
       -------------------------------------------------------- */
    INSERT INTO dbo.DichVu (maDV, tenDV, mota, dongia, trangthai)
    SELECT s.maDV, s.tenDV, s.mota, s.dongia, s.trangthai
    FROM (VALUES
        (N'DV001', N'Kham rang tong quat',       N'Kiem tra tinh trang rang mieng tong the',       CAST(100000.00 AS decimal(18,2)), N'dangapdung'),
        (N'DV002', N'Lay cao rang',              N'Lam sach cao rang va mang bam',                 CAST(250000.00 AS decimal(18,2)), N'dangapdung'),
        (N'DV003', N'Tram rang',                 N'Tram phuc hoi rang sau khi lam sach',           CAST(350000.00 AS decimal(18,2)), N'dangapdung'),
        (N'DV004', N'Tay trang rang',            N'Dich vu tay trang rang tham my',                CAST(1800000.00 AS decimal(18,2)), N'dangapdung'),
        (N'DV005', N'Nho rang',                  N'Nho rang theo chi dinh cua bac si',             CAST(500000.00 AS decimal(18,2)), N'dangapdung'),
        (N'DV006', N'Tu van cham soc rang mieng', N'Tu van ve sinh va cham soc rang mieng tai nha', CAST(0.00 AS decimal(18,2)), N'ngungapdung')
    ) AS s(maDV, tenDV, mota, dongia, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.DichVu AS t WHERE t.maDV = s.maDV
    );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/* ------------------------------------------------------------
   4. KIEM TRA DU LIEU SAU KHI CHEN
   ------------------------------------------------------------ */
SELECT * FROM dbo.NhanVien ORDER BY maNV;
SELECT * FROM dbo.BenhNhan ORDER BY maBN;
SELECT * FROM dbo.DichVu ORDER BY maDV;
GO
