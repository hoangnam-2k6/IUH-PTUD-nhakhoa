/* ============================================================
   SQL Server - Du lieu mau cho 5 bang:
       dbo.NhanVien, dbo.BenhNhan, dbo.DichVu,
       dbo.HoaDon,   dbo.LichHen

   Phu thuoc vao schema trong file:
       MediDent_TaoDatabase_VaBang.sql

   Luu y:
   - Gia tri enum (gioitinh, trangthai cua NhanVien/BenhNhan/DichVu)
     duoc luu CHU HOA, khop ten enum Java, vi Enum.valueOf()
     phan biet hoa/thuong. Neu truoc do da chay ban du lieu mau
     cu (chu thuong), buoc 0 se doi sang chu hoa.
   - LichHen va HoaDon co ma tu tang, nen chi chen du lieu mau
     khi bang dang trong.
   - Ngay cua LichHen/HoaDon tinh theo ngay chay script
     (lich hen sap toi + lich da kham truoc do).
   - HoaDon.maTT, maPK la ma giu cho cho 2 bang ThanhToan,
     PhieuKham (chua co trong schema).

   Script co the chay lai nhieu lan.
   File nen duoc luu UTF-8 (co BOM) de SSMS doc dung tieng Viet.
   ============================================================ */
USE MediDent;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    /* --------------------------------------------------------
       0. DOI GIA TRI ENUM CU (CHU THUONG) SANG CHU HOA
       -------------------------------------------------------- */
    UPDATE dbo.NhanVien SET gioitinh = UPPER(gioitinh), trangthai = UPPER(trangthai);
    UPDATE dbo.BenhNhan SET gioitinh = UPPER(gioitinh), trangthai = UPPER(trangthai);
    UPDATE dbo.DichVu   SET trangthai = UPPER(trangthai);

    /* --------------------------------------------------------
       1. NHANVIEN
       Enum: gioitinh (NAM, NU, KHAC);
             trangthai (DANGLAMVIEC, TAMNGHI, NGHIVIEC)
       -------------------------------------------------------- */
    INSERT INTO dbo.NhanVien
        (maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    SELECT s.maNV, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.chucvu, s.trangthai, s.maTK
    FROM (VALUES
        (N'NV001', N'Nguyễn Văn An',  CONVERT(date, '1990-03-15', 23), N'NAM', N'0901000001', N'an.nv@example.com',    N'Quận 1, TP. Hồ Chí Minh',    N'Bác sĩ',          N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV002', N'Trần Thị Bình',  CONVERT(date, '1994-07-22', 23), N'NU',  N'0901000002', N'binh.nv@example.com',  N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Lễ tân',        N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV003', N'Lê Quốc Cường',  CONVERT(date, '1988-11-09', 23), N'NAM', N'0901000003', N'cuong.nv@example.com', N'Gò Vấp, TP. Hồ Chí Minh',   N'Bác sĩ',          N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV004', N'Phạm Ngọc Dung', CONVERT(date, '1996-01-30', 23), N'NU',  N'0901000004', N'dung.nv@example.com',  N'Bình Thạnh, TP. Hồ Chí Minh', N'Trợ lý nha khoa', N'TAMNGHI',     CAST(NULL AS nvarchar(20))),
        (N'NV005', N'Võ Minh Khang',  CONVERT(date, '1992-05-18', 23), N'NAM', N'0901000005', N'khang.nv@example.com', N'Quận 7, TP. Hồ Chí Minh',   N'Kỹ thuật viên',   N'DANGLAMVIEC', CAST(NULL AS nvarchar(20))),
        (N'NV006', N'Đặng Thu Hà',    CONVERT(date, '1991-09-05', 23), N'NU',  N'0901000006', N'ha.nv@example.com',    N'Quận 10, TP. Hồ Chí Minh',  N'Quản lý',         N'DANGLAMVIEC', CAST(NULL AS nvarchar(20)))
    ) AS s(maNV, hoten, ngaysinh, gioitinh, sdt, email, diachi, chucvu, trangthai, maTK)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.NhanVien AS t WHERE t.maNV = s.maNV
    );

    /* --------------------------------------------------------
       2. BENHNHAN
       Enum: gioitinh (NAM, NU, KHAC);
             trangthai (MOI, DANGDIEUTRI, HOANTAT)
       -------------------------------------------------------- */
    INSERT INTO dbo.BenhNhan
        (maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    SELECT s.maBN, s.hoten, s.ngaysinh, s.gioitinh, s.sdt,
           s.email, s.diachi, s.tiensubenh, s.trangthai
    FROM (VALUES
        (N'BN001', N'Nguyễn Thị Mai', CONVERT(date, '2001-02-12', 23), N'NU',  N'0912000001', N'mai.bn@example.com',  N'TP. Thủ Đức, TP. Hồ Chí Minh', N'Chưa ghi nhận',              N'DANGDIEUTRI'),
        (N'BN002', N'Trần Văn Nam',   CONVERT(date, '1985-09-25', 23), N'NAM', N'0912000002', N'nam.bn@example.com',  N'Quận 3, TP. Hồ Chí Minh',      N'Tiền sử viêm lợi',          N'DANGDIEUTRI'),
        (N'BN003', N'Lê Hoàng Yến',   CONVERT(date, '1998-04-07', 23), N'NU',  N'0912000003', N'yen.bn@example.com',  N'Gò Vấp, TP. Hồ Chí Minh',      N'Dị ứng thuốc (cần xác nhận)', N'MOI'),
        (N'BN004', N'Phạm Gia Bảo',   CONVERT(date, '2010-12-19', 23), N'NAM', N'0912000004', N'bao.bn@example.com',  N'Bình Thạnh, TP. Hồ Chí Minh',  N'Chưa ghi nhận',              N'DANGDIEUTRI'),
        (N'BN005', N'Võ Thanh Trúc',  CONVERT(date, '1979-06-03', 23), N'NU',  N'0912000005', N'truc.bn@example.com', N'Quận 7, TP. Hồ Chí Minh',      N'Tiền sử cao huyết áp',      N'HOANTAT'),
        (N'BN006', N'Hoàng Đức Minh', CONVERT(date, '1995-08-14', 23), N'NAM', N'0912000006', N'minh.bn@example.com', N'Quận 5, TP. Hồ Chí Minh',      N'Chưa ghi nhận',              N'MOI')
    ) AS s(maBN, hoten, ngaysinh, gioitinh, sdt, email, diachi, tiensubenh, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.BenhNhan AS t WHERE t.maBN = s.maBN
    );

    /* --------------------------------------------------------
       3. DICHVU
       Enum: trangthai (DANGAPDUNG, NGUNGAPDUNG)
       -------------------------------------------------------- */
    INSERT INTO dbo.DichVu (maDV, tenDV, mota, dongia, trangthai)
    SELECT s.maDV, s.tenDV, s.mota, s.dongia, s.trangthai
    FROM (VALUES
        (N'DV001', N'Khám răng tổng quát',        N'Kiểm tra tình trạng răng miệng tổng thể',          CAST(100000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV002', N'Lấy cao răng',               N'Làm sạch cao răng và mảng bám',                    CAST(250000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV003', N'Trám răng',                  N'Trám phục hồi răng sau khi làm sạch',              CAST(350000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV004', N'Tẩy trắng răng',             N'Dịch vụ tẩy trắng răng thẩm mỹ',                   CAST(1800000.00 AS decimal(18,2)), N'DANGAPDUNG'),
        (N'DV005', N'Nhổ răng',                   N'Nhổ răng theo chỉ định của bác sĩ',                CAST(500000.00 AS decimal(18,2)),  N'DANGAPDUNG'),
        (N'DV006', N'Tư vấn chăm sóc răng miệng', N'Tư vấn vệ sinh và chăm sóc răng miệng tại nhà',   CAST(0.00 AS decimal(18,2)),       N'NGUNGAPDUNG')
    ) AS s(maDV, tenDV, mota, dongia, trangthai)
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.DichVu AS t WHERE t.maDV = s.maDV
    );

    /* --------------------------------------------------------
       4. LICHHEN (chi chen khi bang dang trong)
       trangthai: Chờ xác nhận, Đã xác nhận, Đã khám, Đã hủy
       Bac si: NV001, NV003 (chuc vu "Bác sĩ")
       -------------------------------------------------------- */
    IF NOT EXISTS (SELECT 1 FROM dbo.LichHen)
    BEGIN
        INSERT INTO dbo.LichHen
            (maBN, maNV, ngayHen, gioHen, thoiLuongPhut, phongKham, trangthai)
        SELECT s.maBN, s.maNV, DATEADD(DAY, s.soNgay, CAST(GETDATE() AS date)),
               CAST(s.gioHen AS time(0)), s.thoiLuongPhut, s.phongKham, s.trangthai
        FROM (VALUES
            (N'BN001', N'NV001',  1, N'09:00', 60, N'Phòng 01', N'Chờ xác nhận'),
            (N'BN002', N'NV003',  1, N'10:00', 45, N'Phòng 02', N'Đã xác nhận'),
            (N'BN003', N'NV001',  2, N'14:00', 60, N'Phòng 01', N'Chờ xác nhận'),
            (N'BN004', N'NV003',  3, N'08:30', 30, N'Phòng 02', N'Đã xác nhận'),
            (N'BN005', N'NV001', -3, N'09:00', 60, N'Phòng 01', N'Đã khám'),
            (N'BN001', N'NV003', -7, N'15:00', 90, N'Phòng 02', N'Đã khám'),
            (N'BN002', N'NV001', -2, N'10:30', 60, N'Phòng 01', N'Đã hủy')
        ) AS s(maBN, maNV, soNgay, gioHen, thoiLuongPhut, phongKham, trangthai);
    END;

    /* --------------------------------------------------------
       5. HOADON (chi chen khi bang dang trong)
       trangthai: Chờ thanh toán, Đã thanh toán, Đã hủy
       maTT, maPK la ma giu cho (chua co bang ThanhToan, PhieuKham)
       -------------------------------------------------------- */
    IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon)
    BEGIN
        INSERT INTO dbo.HoaDon (maTT, maPK, ngayLap, tongTien, trangthai)
        SELECT s.maTT, s.maPK, CAST(DATEADD(DAY, s.soNgay, SYSDATETIME()) AS datetime2(0)),
               s.tongTien, s.trangthai
        FROM (VALUES
            (1, 1,  -3, CAST(250000.00 AS decimal(18,2)),  N'Đã thanh toán'),
            (2, 2,  -7, CAST(1800000.00 AS decimal(18,2)), N'Đã thanh toán'),
            (3, 3,  -2, CAST(350000.00 AS decimal(18,2)),  N'Chờ thanh toán'),
            (4, 4,  -1, CAST(500000.00 AS decimal(18,2)),  N'Chờ thanh toán'),
            (5, 5, -10, CAST(100000.00 AS decimal(18,2)),  N'Đã hủy')
        ) AS s(maTT, maPK, soNgay, tongTien, trangthai);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/* ------------------------------------------------------------
   6. KIEM TRA DU LIEU SAU KHI CHEN
   ------------------------------------------------------------ */
SELECT * FROM dbo.NhanVien ORDER BY maNV;
SELECT * FROM dbo.BenhNhan ORDER BY maBN;
SELECT * FROM dbo.DichVu   ORDER BY maDV;
SELECT * FROM dbo.LichHen  ORDER BY maLichHen;
SELECT * FROM dbo.HoaDon   ORDER BY maHD;
GO
